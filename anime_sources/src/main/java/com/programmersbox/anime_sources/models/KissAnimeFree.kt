package com.programmersbox.anime_sources.models

import com.programmersbox.anime_sources.*
import com.programmersbox.anime_sources.utils.toJsoup
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.rxutils.invoke
import io.reactivex.Single
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

object KissAnimeFree : ShowApi(
    baseUrl = "https://kissanimefree.net",
    recentPath = "status/ongoing/",
    allPath = ""
) {

    override fun getRecent(doc: Document): Single<List<ShowInfo>> = Single.create {
        try {
            it.onSuccess(doc.select("span.movie-title").map { s ->
                ShowInfo(name = s.text(), s.select("a[href^=http]").attr("abs:href"), Sources.KISSANIMEFREE)
            })
        } catch (e: Exception) {
            it.onError(e)
        }
    }

    override fun getList(doc: Document): Single<List<ShowInfo>> = Single.create {
        it.onSuccess(emptyList())
    }

    override fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode> = Single.create { emitter ->
        try {
            val e = Episode(
                source = source,
                name = doc.select("div.movies-data").select("div.info").select("div.film").text(),
                description = doc.select("div.description").text(),
                image = doc.select("div.img").select("img[src^=http]")?.attr("abs:src"),
                genres = doc.select("div.movies-data:contains(Genres)").select("a[href^=http]").eachText(),
                episodes = doc.select("div.server").attr("data-id")
                    .let { "https://kissanimefree.net/load-list-episode/?pstart=undefined&id=$it&ide=" }.toJsoup()
                    .select("li").map {
                        EpisodeInfo(name = it.text(), url = it.select("a[href^=http]").attr("abs:href"), sources = source.sources)
                    }
            )
            emitter.onSuccess(e)
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    override fun getVideoLink(info: EpisodeInfo): Single<List<Storage>> = Single.create { emitter ->
        //println(info.url)
        //println(info.url.toJsoup())
        try {
            val doc = info.url.toJsoup()
            //println(doc)
            val regex1 =
                "'<iframe src=\"(.*?)\" scrolling=\"no\" frameborder=\"0\" width=\"700\" height=\"430\" allowfullscreen=\"true\" webkitallowfullscreen=\"true\" mozallowfullscreen=\"true\"></iframe>'".toRegex()
                    .toPattern().matcher(doc.toString())

            val url = if (regex1.find()) regex1.group(1) else null

            println(url)

            val next = "https:$url".toJsoup()

            //println(next)

            val second = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern().matcher(next.toString())

            val t = if (second.find()) second.group(1) else null

            println(t)

            Jsoup.connect(t?.replace("/v/", "/api/source/"))
                .header("referrer", t)
                .header("origin", "https://fcdn.stream")
                .ignoreContentType(true)
                .header("content-type", "application/json")
                .header("mime-type", "application/json")
                .header("r", "https:$url")
                .requestBody("r=https:$url")
                .post()
                .text()
                .fromJson<Base>()
                .also { println(it) }
                ?.data
                ?.mapNotNull { it.file }
                .orEmpty()
                .map {
                    val storage = Storage(
                        link = it,//if(url.find()) url.group(1)!! else "",
                        source = info.url,
                        quality = "Good",
                        sub = "https:$url"
                    )
                    val regex = "^[^\\[]+(.*mp4)".toRegex().toPattern().matcher(storage.link!!)
                    storage.filename = if (regex.find()) regex.group(1)!! else "${URI(info.url).path.split("/")[2]} ${info.name}.mp4"
                    storage
                }
                .take(1)
                .let { emitter(it) }
        } catch (e: Exception) {
            emitter(e)
        }
    }

    private data class Base(
        val success: Boolean?,
        val player: Player?,
        val data: List<Data1827179427>?,
        val captions: List<Any>?,
        val is_vr: Boolean?
    )

    private data class Data1827179427(val file: String?, val label: String?, val type: String?)

    private data class IncomePop(val pre: String?)

    private data class OptCast(val appid: String?)

    private data class Player(
        val poster_file: String?,
        val logo_file: String?,
        val logo_position: String?,
        val logo_link: String?,
        val logo_margin: Number?,
        val aspectratio: String?,
        val powered_text: String?,
        val powered_url: String?,
        val css_background: String?,
        val css_text: String?,
        val css_menu: String?,
        val css_mntext: String?,
        val css_caption: String?,
        val css_cttext: String?,
        val css_ctsize: String?,
        val css_ctopacity: String?,
        val css_icon: String?,
        val css_ichover: String?,
        val css_tsprogress: String?,
        val css_tsrail: String?,
        val css_button: String?,
        val css_bttext: String?,
        val opt_autostart: Boolean?,
        val opt_title: Boolean?,
        val opt_quality: Boolean?,
        val opt_caption: Boolean?,
        val opt_download: Boolean?,
        val opt_sharing: Boolean?,
        val opt_playrate: Boolean?,
        val opt_mute: Boolean?,
        val opt_loop: Boolean?,
        val opt_vr: Boolean?,
        val opt_cast: OptCast?,
        val opt_nodefault: Boolean?,
        val opt_forceposter: Boolean?,
        val opt_parameter: Boolean?,
        val restrict_domain: String?,
        val restrict_action: String?,
        val restrict_target: String?,
        val adb_enable: Boolean?,
        val adb_offset: String?,
        val adb_text: String?,
        val ads_adult: Boolean?,
        val ads_pop: Boolean?,
        val ads_vast: Boolean?,
        val ads_free: Number?,
        val trackingId: String?,
        val viewId: String?,
        val income: Boolean?,
        val incomePop: IncomePop?,
        val resume_text: String?,
        val resume_yes: String?,
        val resume_no: String?,
        val resume_enable: Boolean?,
        val css_ctedge: String?,
        val logger: String?,
        val revenue: String?,
        val revenue_fallback: String?,
        val revenue_track: String?
    )

}