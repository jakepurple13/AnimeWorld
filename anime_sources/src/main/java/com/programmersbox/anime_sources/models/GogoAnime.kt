package com.programmersbox.anime_sources.models

import com.programmersbox.anime_sources.*
import com.programmersbox.anime_sources.utils.toJsoup
import com.programmersbox.rxutils.invoke
import io.reactivex.Single
import org.jsoup.nodes.Document
import java.net.URI


object GogoAnimeApi : ShowApi(
    baseUrl = "https://www.gogoanime1.com/home",
    allPath = "anime-list",
    recentPath = "latest-episodes"
) {

    override fun getRecent(doc: Document): Single<List<ShowInfo>> = Single.create {
        try {
            it(doc.allElements.select("div.dl-item").map {
                val tempUrl = it.select("div.name").select("a[href^=http]").attr("abs:href")
                ShowInfo(it.select("div.name").text(), tempUrl.substring(0, tempUrl.indexOf("/episode")), this)
            })
        } catch (e: Exception) {
            it(e)
        }
    }

    override fun getList(doc: Document): Single<List<ShowInfo>> = Single.create {
        try {
            it(doc.allElements.select("ul.arrow-list").select("li")
                .map { ShowInfo(it.text(), it.select("a[href^=http]").attr("abs:href"), this) })
        } catch (e: Exception) {
            it(e)
        }
    }

    override fun getVideoLink(info: EpisodeInfo): Single<List<Storage>> = Single.create {
        try {
            val storage = Storage(
                link = info.url.toJsoup().select("a[download^=http]").attr("abs:download"),
                source = info.url,
                quality = "Good",
                sub = "Yes"
            )
            val regex = "^[^\\[]+(.*mp4)".toRegex().toPattern().matcher(storage.link!!)
            storage.filename = if (regex.find()) regex.group(1)!! else "${URI(info.url).path.split("/")[2]} ${info.name}.mp4"
            it(listOf(storage))
        } catch (e: Exception) {
            it(e)
        }
    }

    override fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode> = Single.create {
        try {
            val name = doc.select("div.anime-title").text()
            it(
                Episode(
                    source = source,
                    name = name,
                    description = doc.select("p.anime-details").text(),
                    image = doc.select("div.animeDetail-image").select("img[src^=http]")?.attr("abs:src"),
                    genres = doc.select("div.animeDetail-item:contains(Genres)").select("a[href^=http]").eachText(),
                    episodes = doc.select("ul.check-list").select("li").map {
                        val urlInfo = it.select("a[href^=http]")
                        val epName = urlInfo.text().let { info -> if (info.contains(name)) info.substring(name.length) else info }.trim()
                        EpisodeInfo(epName, urlInfo.attr("abs:href"), source.sources)
                    }.distinctBy(EpisodeInfo::name)
                )
            )
        } catch (e: Exception) {
            it(e)
        }
    }
}

/*
object GogoAnimeMovies : GogoAnimeApi("https://www.gogoanime1.com/home/anime-list") {
    override fun getList(doc: Document): List<ShowInfo> = GogoAnime.getList(doc).filter { it.name.contains("movie", ignoreCase = true) }
}
*/