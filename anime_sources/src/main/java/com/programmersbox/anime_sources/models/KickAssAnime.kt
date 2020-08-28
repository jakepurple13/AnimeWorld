package com.programmersbox.anime_sources.models

import com.programmersbox.anime_sources.*
import com.programmersbox.anime_sources.utils.toJsoup
import com.programmersbox.gsonutils.fromJson
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import io.reactivex.Single
import io.reactivex.SingleEmitter
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

object KickAssAnime : ShowApi(
    baseUrl = "https://www1.kickassanime.rs",
    recentPath = "feed/latest",
    allPath = "anime-list"
) {

    private fun Document.findJson(): String {
        val regex = "appData = (.*?), dm = '';".toRegex().toPattern().matcher(html())
        return (if (regex.find()) regex.group(1) else "").removeSuffix("|| {}")
    }

    override fun getRecent(doc: Document): Single<List<ShowInfo>> = Single.create { emitter ->
        try {
            val f = XmlToJson.Builder(doc.html()).build().toString().fromJson<RecentInfo.Base>()
                ?.rss
                ?.channel
                ?.item
                ?.map {
                    ShowInfo(name = it.title.orEmpty(), url = it.link?.trim()?.removeSurrounding("\n").orEmpty(), sources = Sources.KISSANIMEFREE)
                }
                .orEmpty()
            emitter.onSuccess(f)
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override fun getList(doc: Document): Single<List<ShowInfo>> = Single.create { emitter ->
        try {
            val z = doc.findJson()
                .fromJson<FullList.Base>()?.animes?.map {
                    ShowInfo(name = it.name.orEmpty(), url = "$baseUrl${it.slug}", sources = Sources.KISSANIMEFREE)
                }.orEmpty()
            emitter.onSuccess(z)
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode> = Single.create { emitter ->
        try {
            val regex = "appData = (.*?), dm = '';".toRegex().toPattern().matcher(doc.html())
            val appData = (if (regex.find()) regex.group(1) else "")

            //println(appData)
            val animeData = ",\"anime\":(.*?), dm = ''".toRegex().toPattern().matcher("$appData, dm = ''")
            val f = JSONObject(appData).getJSONObject("anime").toString()
                .fromJson<EpisodeInformation.Anime>() ?: (if (animeData.find()) animeData.group(1) else "")
                .removeSuffix("} || {}")
                .fromJson<EpisodeInformation.Anime>()
            //println(f)

            if (f == null) {
                emitter.onError(Exception("Something went wrong trying to get information about ${source.name}"))
            } else {
                emitter.onSuccess(
                    Episode(
                        source = source,
                        name = f.name.orEmpty(),
                        description = f.description.orEmpty(),
                        image = "https://www1.kickassanime.rs/uploads/${f.image}",
                        genres = f.genres?.mapNotNull(EpisodeInformation.Genres::name).orEmpty(),
                        episodes = f.episodes
                            ?.sortedByDescending(EpisodeInformation.Episodes::num)
                            ?.map { EpisodeInfo(name = it.epnum.orEmpty(), url = "$baseUrl${it.slug}", sources = source.sources) }
                            .orEmpty()
                    )
                )
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override fun getVideoLink(info: EpisodeInfo): Single<List<Storage>> = Single.create { emitter ->
        try {

            val z = info.url.toJsoup().findJson()
                .fromJson<VideoLinks.Base>()
                ?.episode?.let { it.link1 ?: it.link2 ?: it.link3 ?: it.link4 }

            val f = Jsoup.connect(z).post()
            println(f)

            val storage = Storage(
                link = z,//if(url.find()) url.group(1)!! else "",
                source = info.url,
                quality = "Good",
                sub = "Yes"
            )
            val regex = "^[^\\[]+(.*mp4)".toRegex().toPattern().matcher(storage.link!!)
            storage.filename = if (regex.find()) regex.group(1)!! else "${URI(info.url).path.split("/")[2]} ${info.name}.mp4"

            emitter.onSuccess(storage)
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    private fun <T> SingleEmitter<List<T>>.onSuccess(vararg items: T) = onSuccess(items.toList())

    private object FullList {
        data class Anime(
            val name: String?,
            val en_title: String?,
            val slug: String?,
            val slug_id: String?,
            val description: String?,
            val status: String?,
            val image: String?,
            val banner: String?,
            val startdate: String?,
            val enddate: Any?,
            val broadcast_day: String?,
            val broadcast_time: String?,
            val source: String?,
            val duration: String?,
            val alternate: List<String>?,
            val type: String?,
            val episodes: List<Episodes>?,
            val types: List<Types>?,
            val genres: List<Genres>?,
            val aid: String?,
            val favorited: Boolean?,
            val votes: Number?,
            val rating: Boolean?
        ) {
            fun imageUrl() = "https://www1.kickassanime.rs/uploads/$image"
            fun bannerUrl() = "https://www1.kickassanime.rs/uploads/$banner"
        }

        data class Base(
            val clip: String?,
            val sig: String?,
            val vt: String?,
            val user: Any?,
            val ax: Any?,
            val notes: Notes?,
            val animes: List<Anime>?
        )

        data class Episodes(val epnum: String?, val name: Any?, val slug: String?, val createddate: String?, val num: String?)

        data class Genres(val name: String?, val slug: String?)
        data class Notes(val adblock: String?, val notes: String?)
        data class Types(val name: String?, val slug: String?)
    }

    private object RecentInfo {
        data class Base(val rss: Rss?)

        data class Channel(
            val image: Image?,
            val item: List<Item>?,
            val docs: String?,
            val link: String?,
            val description: String?,
            val generator: String?,
            val language: String?,
            val title: String?
        )

        data class Image(
            val link: String?,
            val width: String?,
            val description: String?,
            val title: String?,
            val url: String?,
            val height: String?
        )

        data class Item(val link: String?, val guid: String?, val title: String?, val pubDate: String?)

        data class Rss(val channel: Channel?, val version: String?)
    }

    object EpisodeInformation {
        data class Anime(
            val name: String?,
            val slug: String?,
            val slug_id: String?,
            val description: String?,
            val image: String?,
            val alternate: String?,
            //val type: String?,
            val episodes: List<Episodes>?,
            //val types: List<Types>?,
            val genres: List<Genres>?
        )

        data class Episodes(val epnum: String?, val name: Any?, val slug: String?, val createddate: String?, val num: String?)
        data class Genres(val name: String?, val slug: String?)
    }

    private object VideoLinks {

        data class Anime(
            val anime_id: String?,
            val name: String?,
            val en_title: Any?,
            val slug: String?,
            val description: String?,
            val status: String?,
            val image: String?,
            val banner: Any?,
            val startdate: String?,
            val enddate: String?,
            val broadcast_day: String?,
            val broadcast_time: String?,
            val source: String?,
            val duration: String?,
            val alternate: String?,
            val site: String?,
            val info_link: Any?,
            val createddate: String?,
            val mal_id: Any?,
            val simkl_id: Any?,
            val types: List<Types746740825>?,
            val genres: List<Genres339033223>?
        )

        data class Base(
            val clip: String?,
            val sig: String?,
            val vt: String?,
            val user: Any?,
            val ax: Any?,
            val notes: Notes?,
            val anime: Anime?,
            val episode: Episode?,
            val ext_servers: List<Ext_servers1667210341>?,
            val episodes: List<Episodes1101683495>?
        )

        data class Episode(
            val name: String?,
            val title: Any?,
            val slug: String?,
            val slug_id: String?,
            val dub: String?,
            val link1: String?,
            val link2: String?,
            val link3: String?,
            val link4: String?,
            val anime_id: String?,
            val sector: String?,
            val createddate: String?,
            val next: Any?,
            val prev: Prev?,
            val epid: String?,
            val rating: Boolean?,
            val votes: Number?,
            val favorited: Boolean?
        )

        data class Episodes1101683495(val epnum: String?, val name: Any?, val slug: String?, val createddate: String?, val num: String?)

        data class Ext_servers1667210341(val name: String?, val link: String?)

        data class Genres339033223(val name: String?, val slug: String?)

        data class Notes(val adblock: String?, val notes: String?)

        data class Prev(val name: String?, val slug: String?, val dub: String?, val title: Any?)

        data class Types746740825(val name: String?, val slug: String?)

    }

}