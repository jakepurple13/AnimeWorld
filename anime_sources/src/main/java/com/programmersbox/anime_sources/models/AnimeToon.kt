package com.programmersbox.anime_sources.models

import com.programmersbox.anime_sources.*
import com.programmersbox.anime_sources.utils.toJsoup
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.rxutils.invoke
import io.reactivex.Single
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.*

abstract class AnimeToon(allPath: String, recentPath: String) : ShowApi(
    baseUrl = "http://www.animetoon.org",
    allPath = allPath,
    recentPath = recentPath
) {
    protected abstract val sources: Sources
    private fun toShowInfo(element: Element) = ShowInfo(element.text(), element.attr("abs:href"), sources)
    override fun getList(doc: Document): Single<List<ShowInfo>> = Single.create {
        try {
            it(doc.allElements.select("td").select("a[href^=http]").map(this::toShowInfo))
        } catch (e: Exception) {
            it(e)
        }
    }

    override fun getRecent(doc: Document): Single<List<ShowInfo>> = Single.create {
        try {
            var listOfStuff = doc.allElements.select("div.left_col").select("table#updates").select("a[href^=http]")
            if (listOfStuff.size == 0) listOfStuff = doc.allElements.select("div.s_left_col").select("table#updates").select("a[href^=http]")
            it(listOfStuff.map(this::toShowInfo).filter { !it.name.contains("Episode") })
        } catch (e: Exception) {
            it(e)
        }
    }

    @Suppress("RegExpRedundantEscape")
    override fun getVideoLink(info: EpisodeInfo): Single<List<Storage>> = Single.create {
        try {
            val m = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern().matcher(getHtml(info.url))
            val list = arrayListOf<String>()
            while (m.find()) list.add(m.group(1)!!)
            val regex = "(http|https):\\/\\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\%\\&\\-\\_\\?\\.\\=\\/])+(part[0-9])+.(\\w*)"
            when (val htmlc = if (regex.toRegex().toPattern().matcher(list[0]).find()) list else getHtml(list[0])) {
                is ArrayList<*> -> {
                    val urlList = mutableListOf<Storage?>()
                    for (i in htmlc) {
                        val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(getHtml(i.toString()))
                        while (reg.find()) urlList.add(reg.group(1).fromJson<NormalLink>()?.normal?.storage?.get(0))
                    }
                    it(urlList.filterNotNull())
                }
                is String -> {
                    val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(htmlc)
                    while (reg.find()) it(listOfNotNull(reg.group(1).fromJson<NormalLink>()?.normal?.storage?.get(0)))
                }
            }
            it(emptyList())
        } catch (e: Exception) {
            it(e)
        }
    }

    @Throws(IOException::class)
    private fun getHtml(url: String): String {
        // Build and set timeout values for the request.
        val connection = URL(url).openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
        connection.addRequestProperty("Accept-Language", "en-US,en;q=0.5")
        connection.addRequestProperty("Referer", "http://thewebsite.com")
        connection.connect()
        // Read and store the result line by line then return the entire string.
        val in1 = connection.getInputStream()
        val reader = BufferedReader(InputStreamReader(in1))
        val html = reader.readText()
        in1.close()
        return html
    }

    override fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode> = Single.create {
        try {
            fun getStuff(document: Document) = document.allElements.select("div#videos").select("a[href^=http]")
                .map { EpisodeInfo(it.text(), it.attr("abs:href"), source.sources) }
            it(
                Episode(
                    source = source,
                    name = doc.select("div.right_col h1").text(),
                    description = doc.allElements.select("div#series_details").let { element ->
                        if (element.select("span#full_notes").hasText())
                            element.select("span#full_notes").text().removeSuffix("less")
                        else
                            element.select("div:contains(Description:)").select("div").text().let {
                                try {
                                    it.substring(it.indexOf("Description: ") + 13, it.indexOf("Category: "))
                                } catch (e: StringIndexOutOfBoundsException) {
                                    it
                                }
                            }
                    },
                    image = doc.select("div.left_col").select("img[src^=http]#series_image")?.attr("abs:src"),
                    genres = doc.select("span.red_box").select("a[href^=http]").eachText(),
                    episodes = getStuff(doc) + doc.allElements.select("ul.pagination").select(" button[href^=http]")
                        .flatMap { getStuff(it.attr("abs:href").toJsoup()) }
                )
            )
        } catch (e: Exception) {
            it(e)
        }
    }
}

object AnimeToonApi : AnimeToon(
    allPath = "cartoon",
    recentPath = "updates"
) {
    override val sources: Sources get() = Sources.ANIMETOON
}

object AnimeToonDubbed : AnimeToon(
    allPath = "dubbed-anime",
    recentPath = "updates"
) {
    override val sources: Sources get() = Sources.DUBBED_ANIME
}

object AnimeToonMovies : AnimeToon(
    allPath = "movies",
    recentPath = "updates"
) {
    override val sources: Sources get() = Sources.ANIMETOON_MOVIES
}