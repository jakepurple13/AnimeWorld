package com.programmersbox.anime_sources.models

import com.programmersbox.anime_sources.*
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.gsonutils.getApi
import com.programmersbox.gsonutils.getJsonApi
import io.reactivex.Single
import org.jsoup.nodes.Document
import java.net.URI

object AnimeFlix : ShowApi(
    baseUrl = "https://animeflix.io",
    recentPath = "api/episodes/latest?limit=12&page=",
    allPath = ""
) {
    override val canScroll: Boolean get() = true

    override fun recentPage(page: Int): String = "$page"

    override fun getRecent(page: Int): Single<List<ShowInfo>> = Single.create { emitter ->
        try {
            emitter.onSuccess(
                getJsonApi<Flix.Base3>("$baseUrl/$recentPath${recentPage(page)}")
                    .also { println(it) }
                    ?.data.orEmpty()
                    .map {
                        ShowInfo(name = it.title.orEmpty(), url = it.anime?.slug.orEmpty(), sources = Sources.KISSANIMEFREE)
                            .apply { extras["id"] = it.id?.toInt() }
                    }
            )
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override fun getRecent(doc: Document): Single<List<ShowInfo>> = Single.create { it.onError(Exception("You shouldn't be here")) }

    private object Flix {

        // result generated from /json

        data class Anime(val dynamic_id: Number?, val slug: String?)

        data class Base3(val data: List<Data565513573>?, val links: Links3?, val meta: Meta3?)

        data class Data565513573(
            val id: Number?,
            val title: String?,
            val english_title: Any?,
            val episode_num: String?,
            val type: String?,
            val created_at: String?,
            val thumbnail: String?,
            val anime: Anime?,
            val episode: Episode?
        )

        data class Episode(val id: Number?, val dynamic_id: Number?, val episode_num: String?)

        data class Links3(val first: String?, val last: String?, val prev: Any?, val next: String?)

        data class Meta3(
            val current_page: Number?,
            val from: Number?,
            val last_page: Number?,
            val path: String?,
            val per_page: String?,
            val to: Number?,
            val total: Number?
        )


        /*-----------------*/
        data class Base(val data: List<Data>?, val links: Links?, val meta: Meta?)

        data class Data(
            val id: Number?,
            val dynamic_id: Any?,
            val title: String?,
            val english_title: Any?,
            val slug: String?,
            val status: Any?,
            val description: Any?,
            val year: Any?,
            val season: String?,
            val type: Any?,
            val cover_photo: String?,
            val alternate_titles: Any?,
            val duration: Any?,
            val broadcast_day: String?,
            val broadcast_time: Any?,
            val rating: Any?,
            val rating_scores: Any?,
            val gwa_rating: Number?
        )

        data class Links(val first: String?, val last: String?, val prev: Any?, val next: String?)

        data class Meta(
            val current_page: Number?,
            val from: Number?,
            val last_page: Number?,
            val path: String?,
            val per_page: Number?,
            val to: Number?,
            val total: Number?
        )

    }

    override fun getList(doc: Document): Single<List<ShowInfo>> = Single.create { emitter ->
        try {
            emitter.onSuccess(emptyList())
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override fun getEpisodeInfo(source: ShowInfo): Single<Episode> = Single.create { emitter ->
        try {
            //{base}/api/anime/detail?slug={slug}
            //{base}/api/episodes?anime_id={id}&limit={limit}&page={page}
            //val eps = "$baseUrl/api/episodes?anime_id=${source.url}&limit=100&page=1"
            val eps = "$baseUrl/api/anime/detail?slug=${source.url}"
            getJsonApi<Info.Base>(eps)
                .also { println(it) }
                ?.data
                ?.let {
                    Episode(
                        source = source,
                        name = it.title.orEmpty(),
                        description = it.description.orEmpty(),
                        image = it.cover_photo.orEmpty(),
                        genres = it.genres.orEmpty().mapNotNull(Info.Genres::name),
                        episodes = getJsonApi<Info.Base2>("$baseUrl/api/episodes?anime_id=${it.id}&limit=100&page=1")
                            ?.data
                            ?.sortedByDescending(Info.Data380014128::episode_num)
                            .orEmpty()
                            .map { e ->
                                EpisodeInfo(
                                    name = "Episode ${e.episode_num} - ${e.title}",
                                    url = "$baseUrl/api/videos?episode_id=${e.id}",
                                    sources = source.sources
                                )
                            }
                    )
                }?.let { emitter.onSuccess(it) } ?: emitter.onError(Exception("Something Went Wrong With ${source.name}"))
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode> =
        Single.create { it.onError(Exception("You shouldn't be here")) }

    private object Info {
        data class Anime(
            val id: Number?,
            val dynamic_id: Number?,
            val title: String?,
            val english_title: Any?,
            val slug: String?,
            val status: String?,
            val description: String?,
            val year: String?,
            val season: String?,
            val type: String?,
            val cover_photo: String?,
            val alternate_titles: List<String>?,
            val duration: String?,
            val broadcast_day: String?,
            val broadcast_time: Any?,
            val rating: String?,
            val rating_scores: Number?,
            val gwa_rating: Number?
        )

        data class Base2(val data: List<Data380014128>?, val links: Links?, val meta: Meta?, val anime: Anime?)

        data class Data380014128(
            val id: Number?,
            val dynamic_id: Number?,
            val title: String?,
            val episode_num: String?,
            val airing_date: String?,
            val views: Number?,
            val sub: Number?,
            val dub: Number?,
            val thumbnail: String?
        )

        data class Links(val first: String?, val last: String?, val prev: Any?, val next: String?)

        data class Meta(
            val current_page: Number?,
            val from: Number?,
            val last_page: Number?,
            val path: String?,
            val per_page: Number?,
            val to: Number?,
            val total: Number?
        )

        //------------------
        data class Base(val data: Data?)

        data class Data(
            val id: Number?,
            val dynamic_id: Number?,
            val title: String?,
            val english_title: Any?,
            val slug: String?,
            val status: String?,
            val description: String?,
            val year: String?,
            val season: String?,
            val type: String?,
            val cover_photo: String?,
            val alternate_titles: List<String>?,
            val duration: String?,
            val broadcast_day: String?,
            val broadcast_time: Any?,
            val rating: String?,
            val rating_scores: Number?,
            val gwa_rating: Number?,
            val follower_count: Number?,
            val genres: List<Genres>?
        )

        data class Genres(val name: String?)
    }

    override fun getVideoLink(info: EpisodeInfo): Single<List<Storage>> = Single.create { emitter ->
        try {


            //val u = URL(info.url).readText()
            //println(u)

            /* val f = getApi(info.url)
             println(f)

             val f = getApi(info.url)

             println(f)*/
            val f = getApi(info.url)
            val g = f.fromJson<List<Video.Base>>().orEmpty().firstOrNull { it.type != "hls" }

            println(g)

            val storage = Storage(
                link = g?.file,//if(url.find()) url.group(1)!! else "",
                source = info.url,
                quality = "Good",
                sub = "Yes"
            )
            storage.filename = try {
                val regex = "^[^\\[]+(.*mp4)".toRegex().toPattern().matcher(storage.link!!)
                if (regex.find()) regex.group(1)!! else "${URI(info.url).path.split("/")[2]} ${info.name}.mp4"
            } catch (e: Exception) {
                info.name
            }

            emitter.onSuccess(listOf(storage))
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    private object Video {
        // result generated from /json

        data class Base(
            val id: String?,
            val provider: String?,
            val file: String?,
            val lang: String?,
            val type: String?,
            val hardsub: Boolean?,
            val thumbnail: String?,
            val resolution: String?
        )

    }

}