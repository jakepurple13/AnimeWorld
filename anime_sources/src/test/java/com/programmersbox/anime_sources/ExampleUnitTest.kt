package com.programmersbox.anime_sources

import com.programmersbox.anime_sources.models.AnimeFlix
import com.programmersbox.anime_sources.models.KickAssAnime
import com.programmersbox.anime_sources.models.KissAnimeFree
import com.programmersbox.anime_sources.utils.toJsoup
import com.programmersbox.gsonutils.getApi
import com.programmersbox.gsonutils.getJsonApi
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Test
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runBlocking {
        //println("https://kissanimefree.net/status/ongoing/".toJsoup())
        val k = KissAnimeFree.getRecent().blockingGet().also { println(it) }
        val j = KissAnimeFree.getEpisodeInfo(k.random()).blockingGet().also { println(it) }
        val e = KissAnimeFree.getVideoLink(j.episodes.random()).blockingGet().also { println(it) }
        /*.subscribeBy {
            println(it)
        }*/
        //println("https://kissanimefree.net/episode/rezero-kara-hajimeru-break-time-2nd-season-episode-1/".toJsoup())
    }

    @Test
    fun searchTest() {
        /*------------kissanimefree---------*/
        //kissanimefreeSearch()

        /*------------animetoon---------*/
        animetoonSearch()

        /*------------gogoanime---------*/
        //gogoanimeSearch()
    }

    private fun kissanimefreeSearch() {
        val f = getApi("https://kissanimefree.net/?s=mushi")
        //println(f)
        val s = KissAnimeFree.getList(Jsoup.parse(f)).blockingGet()
        println(s)
    }

    private fun animetoonSearch() {
        val a = Jsoup.connect("http://www.animetoon.org/toon/search?key=dragon").get()
        //println(a)

        val d = a.select("div.right_col").select("h3").select("a[href^=http]")
            .map { ShowInfo(it.text(), it.attr("abs:href"), Sources.ANIMETOON) }
        println(d)
    }

    private fun gogoanimeSearch() {
        val f = getJsonApi<Base>("https://www.gogoanime1.com/search/topSearch?q=mushi")
            ?.data
            .orEmpty()
            .map { ShowInfo(it.name.orEmpty(), "https://www.gogoanime1.com/watch/${it.seo_name}", Sources.GOGOANIME) }

        println(f)
        //https://www.gogoanime1.com/ac/meta/anime/10035/mushi-uta.jpg
        //https://www.gogoanime1.com/watch/mushi-uta
        println(f.first().getEpisodeInfo().blockingGet())
    }

    data class Base(val status: Number?, val data: List<DataShowData>?)

    data class DataShowData(
        val rel: Number?,
        val anime_id: Number?,
        val name: String?,
        val has_image: Number?,
        val seo_name: String?,
        val score_count: Number?,
        val score: Number?,
        val aired: Number?,
        val episodes: List<Episodes>?
    )

    data class Episodes(val episode_id: Number?, val episode_seo_name: String?, val episode_name: String?)


    @After
    fun finished() {
        Runtime.getRuntime().exec("say finished").waitFor()
    }

    @Test
    fun kickass() {
        //https://www1.kickassanime.rs//feed/latest

        /*val f = getApi("https://www1.kickassanime.rs/anime/the-god-of-high-school-103036").orEmpty()
        //println(f)

        val regex = "appData = (.*?), dm = '';".toRegex().toPattern().matcher(f)
        val z = (if (regex.find()) regex.group(1) else "").removeSuffix("|| {}").fromJson<KickAssClass>()?.anime
        println(z)*/

        /*
        background: url("https://www1.kickassanime.rs/uploads/080044.jpg") center center / cover no-repeat;
          var appUrl = 'https://www1.kickassanime.rs/',
           apiUrl = 'https://www1.kickassanime.rs/api/',
            uploadUrl = 'https://www1.kickassanime.rs/uploads/',
             appData = {"clip":"69.121.146.182","sig":"1de0d4fcc3a04187adbebe93039cb4106062b0f78a6e75c20f49b4df0e37392d","vt":"887bb2f1ba7839830a8fbc99522661e4","user":null,"ax":{"5":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/720x90.html\" height=\"100\" width=\"720\" scrolling=\"no\"><\/iframe>","405":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/300x100_v2.html\" width=\"300\" height=\"110\" scrolling=\"no\" ><\/iframe>","20":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/300x250_v3.html?v5\" height=\"300\" scrolling=\"no\"><\/iframe>","30":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/720x90_v3_ab.html\" width=\"720\" height=\"100\" scrolling=\"no\"><\/iframe>","31":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/720x90_v2.html\" width=\"720\" height=\"100\" scrolling=\"no\"><\/iframe>","431":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/300x100.html\" width=\"300\" height=\"110\" scrolling=\"no\" ><\/iframe>","35":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/300x250_v3.html?v5\" height=\"300\" scrolling=\"no\"><\/iframe>","301":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/300x250_v2.html\" height=\"300\" scrolling=\"no\"><\/iframe>","302":"<iframe src=\"https:\/\/www1.kickassanime.rs\/codea\/300x250_v3.html?v5\" height=\"300\" scrolling=\"no\"><\/iframe>","303":"<iframe src=\"https:\/\/www1.kickassanime.rs\/safe\/index.html\" height=\"300\" scrolling=\"no\"><\/iframe>"},"notes":{"adblock":"Do you want KAA to stay online?, then consider white-listing the website in your adblocker to help it stay. Much love, your KAA team","notes":"- Pop-ups on KAA servers returned, but a lot cleaner.\r\n- Use our Chrome extension to stop annoying pop-ups\/ads on Vidstreaming servers and others (Updated weekly) <a href=\"https:\/\/chrome.google.com\/webstore\/detail\/kaa-menhera\/cheknlbiacfddgakdfdlioemnhlolaah\" target=\"_blank\" style=\" font-weight: bold; color: #ef091f; \">Click Here to get it<\/a>\r\n- Join our community in case we got disconnected <a href=\"https:\/\/twitter.com\/OfficalKaa\" target=\"_blank\" style=\" font-weight: bold; color: #ef091f; \">Twitter<\/a> , <a href=\"https:\/\/discord.gg\/GEJk5K7\" target=\"_blank\" style=\" font-weight: bold; color: #ef091f; \">Discord<\/a> , <a href=\"https:\/\/www.reddit.com\/r\/KickAssAnime\/\" target=\"_blank\" style=\" font-weight: bold; color: #ef091f; \">Reddit<\/a>\r\n- Don't hesitate to use our domain shortcut <a href=\"https:\/\/www.kaa.si\/\" target=\"_blank\" style=\" font-weight: bold; color: #ef091f; \">kaa.si<\/a>\r\n- We are in need of Web Developers and UI Designer Lang: Python Expert, Nodejs, Front\/Back end. please contact us for more details.\r\n- <mark>Please share the website as much as you can to keep it alive!<\/mark>"},"anime":{"name":"Olympia Kyklos","en_title":"","slug":"olympia-kyklos","slug_id":"844515","description":"Demetrios was a young man in Ancient Greece who was a potter's apprentice, but also a rather timid otaku. Despite pursuing the arts, he was blessed with natural athletic talent. One day, he gets dragged into a village conflict. Demetrios contemplates how helpless he is, but then is struck by lightning! When he comes to, he is no longer in Greece, but a strange land where he can't understand the language and people look rather different. Yes, he somehow ended up 1964 Tokyo! Of course, Demetrios has no idea what Japan even is. What will become of him?!","status":"Currently Airing","image":"602440.jpg","banner":"624846.jpg","startdate":"2020-04-20","enddate":null,"broadcast_day":"monday","broadcast_time":"21:54","source":"Manga","duration":"5 mins","alternate":["\u30aa\u30ea\u30f3\u30d4\u30a2\u30fb\u30ad\u30e5\u30af\u30ed\u30b9"],"type":"TV Series","episodes":[{"epnum":"Episode 14","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-14-357499","createddate":"2020-08-24 09:33:15","num":"14"},{"epnum":"Episode 13","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-13-303343","createddate":"2020-08-17 09:30:28","num":"13"},{"epnum":"Episode 12","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-12-691993","createddate":"2020-08-10 10:20:15","num":"12"},{"epnum":"Episode 11","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-11-482926","createddate":"2020-08-03 11:25:46","num":"11"},{"epnum":"Episode 10","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-10-209911","createddate":"2020-07-27 10:25:04","num":"10"},{"epnum":"Episode 09","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-09-603200","createddate":"2020-07-20 09:49:09","num":"9"},{"epnum":"Episode 08","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-08-279133","createddate":"2020-07-20 09:48:33","num":"8"},{"epnum":"Episode 07","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-07-772735","createddate":"2020-07-06 10:00:55","num":"7"},{"epnum":"Episode 06","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-06-783122","createddate":"2020-06-29 12:26:52","num":"6"},{"epnum":"Episode 05","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-05-730963","createddate":"2020-06-22 16:38:02","num":"5"},{"epnum":"Episode 04","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-04-101215","createddate":"2020-05-11 11:35:29","num":"4"},{"epnum":"Episode 03","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-03-809537","createddate":"2020-05-04 13:44:18","num":"3"},{"epnum":"Episode 02","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-02-996764","createddate":"2020-04-27 11:03:30","num":"2"},{"epnum":"Episode 01","name":null,"slug":"\/anime\/olympia-kyklos-844515\/episode-01-636888","createddate":"2020-04-20 09:38:12","num":"1"}],"types":[{"name":"TV Series","slug":"TV Series"},{"name":"Spring 2020"}],"genres":[{"name":"Comedy","slug":"\/genre\/comedy"}],"aid":"6272","favorited":false,"votes":23,"rating":false}} || {}, dm = '';
         */

        /*val f = getApi("https://www1.kickassanime.rs/anime-list").toString()

        //println(f)

        val regex = "appData = (.*?), dm = '';".toRegex().toPattern().matcher(f)
        val z = (if (regex.find()) regex.group(1) else "").removeSuffix("|| {}")
            .fromJson<KickAssAnime.KickAssClass2>()?.animes.orEmpty()

        println(z.size)
        println(z.random())
        println()*/
        val list = KickAssAnime.getList(1).blockingGet()
        val f = KickAssAnime.getEpisodeInfo(list.first()).blockingGet()
        println(f)
        println(f.episodes.firstOrNull()?.let { KickAssAnime.getVideoLink(it) }?.blockingGet())
        //println(list.random().getEpisodeInfo().blockingGet())
        /*


        curl 'https://www4.mp4upload.com:282/d/rcxwmck4z3b4quuogguaeikrkzq3yaqhljnl6idxtaiilfj4vhivphbc/video.mp4' \
  -H 'Connection: keep-alive' \
  -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.135 Safari/537.36' \
  -H 'Accept: *' \
        -H 'Sec-Fetch-Site: cross-site' \
        -H 'Sec-Fetch-Mode: no-cors' \
        -H 'Sec-Fetch-Dest: video' \
        -H 'Accept-Language: en-US,en;q=0.9' \
        -H 'Range: bytes=0-' \
        --compressed

         */

        //val url = "https://www1.kickassanime.rs/anime/hacksign-811868"
        //val doc = url.toJsoup()
        //val j = Mockito.mock(JSONObject::class.java)

        /*val url = "https://www1.kickassanime.rs/anime/hacksign-811868"
        val doc = url.toJsoup()
        //println(doc)
        val regex = "appData = (.*?), dm = '';".toRegex().toPattern().matcher(doc.html())
        //"appData = (.*?) ,\"anime\": (.*?), dm = '';".toRegex().toPattern().matcher(doc.html())
        val z = (if (regex.find()) regex.group(1) else "")//.removeSuffix("} || {}")
        println(z)
        val z1 = ",\"anime\":(.*?), dm = ''".toRegex().toPattern().matcher("$z, dm = ''")
        val z2 = (if(z1.find()) z1.group(1) else "").removeSuffix("} || {}")
        println(z2)
        println(z2.fromJson<KickAssAnime.EpisodeInformation.Anime>())*/

        /*val e = f.episodes.firstOrNull()?.url?.let {
            println(it)
            Jsoup.connect(it).get()
        }
        println(e)*/

        //println(f.episodes.firstOrNull()?.getVideoLink()?.blockingGet())

        /*val f = KickAssAnime.getList(1)
        f.subscribeBy {
            println(it)
        }*/
    }

    @Test
    fun nineanime() {
        val f = NineAnime.getRecent().blockingGet()
        //println(f)
        val e = NineAnime.getEpisodeInfo(f.first()).blockingGet()
        println(e)

        val e1 = NineAnime.getEpisodeInfo(f.random()).blockingGet()
        println(e1)

        val z = "https://www10.9anime.to/ajax/film/tooltip/${f.first().url.split(".").last()}"
        val a = Jsoup.connect(z)
            .header("Referer", "https://google.com")
            .post()
        println(a)
    }

    object NineAnime : ShowApi(
        baseUrl = "https://www10.9anime.to",
        recentPath = "updated",
        allPath = ""
    ) {
        override fun getRecent(doc: Document): Single<List<ShowInfo>> = Single.create { emitter ->
            try {
                emitter.onSuccess(
                    doc.select("div.film-list").select("div.item").map {
                        ShowInfo(
                            name = it.select("a").attr("data-jtitle"),
                            url = it.select("a[href^=http]").attr("abs:href"),
                            sources = Sources.KISSANIMEFREE
                        )
                    }
                )
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        override fun getList(doc: Document): Single<List<ShowInfo>> {
            TODO("Not yet implemented")
        }

        /*override fun getEpisodeInfo(source: ShowInfo): Single<Episode> {
            return getEpisodeInfo(source, Jsoup.parse(getApi(source.url).orEmpty()))
        }*/

        override fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode> = Single.create { emitter ->
            try {
                /*emitter.onSuccess(

                )*/
                //println(doc)
                //println(doc)

                val eps = doc.select("div#servers-container")
                //println(eps)

                val eps1 = doc.select("ul.episodes, ul.range, ul.active")
                println(eps1)

                val f = eps1.map {
                    it.select("a")
                }

                //println(f)

                val doc2 = doc.select("div.widget-body")

                emitter.onSuccess(
                    Episode(
                        source = source,
                        name = doc2.select("h2.title").text(),
                        description = doc.select("div.desc").text(),
                        image = doc2.select("img[src^=http]")?.attr("abs:src"),
                        genres = doc2.select("dd").select("a[href*=/genre/]").eachText(),
                        episodes = emptyList()
                        /*doc.select("div.server").attr("data-id")
                            .let { "https://kissanimefree.net/load-list-episode/?pstart=undefined&id=$it&ide=" }.toJsoup()
                            .select("li").map {
                                EpisodeInfo(name = it.text(), url = it.select("a[href^=http]").attr("abs:href"), sources = source.sources)
                            }*/
                    )
                )
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        override fun getVideoLink(info: EpisodeInfo): Single<List<Storage>> {
            TODO("Not yet implemented")
        }

    }

    @Test
    fun animeflix() {
        //val f = "https://animeflix.io/api/anime/recent?limit=100"
        //println(getApi(f))
        //println(Jsoup.connect("https://animeflix.io/api/anime/latest?limit=12").get())

        val f = AnimeFlix.getRecent(2).blockingGet()
        //println(f)
        val e = f.random().let { AnimeFlix.getEpisodeInfo(it).blockingGet() }
        //println(e)
        val v = e?.episodes?.first()?.let { AnimeFlix.getVideoLink(it).blockingGet() }
        //println(v)

        val z = v?.firstOrNull()
        println(z)
        z?.link?.let { downloadFile(it) }

        println("Done")
    }

    private fun downloadFile(url: String) {
        try {
            val website = URL(url)
            val rbc: ReadableByteChannel = Channels.newChannel(website.openStream())
            val fos = FileOutputStream("/Users/jrein/Documents/video.mp4")
            fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
            /*BufferedInputStream(URL(url).openStream()).use { inputStream ->
                FileOutputStream("/Users/jrein/Documents/video.mp4").use { fileOS ->
                    val data = ByteArray(1024)
                    var byteContent: Int
                    while (inputStream.read(data, 0, 1024).also { byteContent = it } != -1) {
                        fileOS.write(data, 0, byteContent)
                    }
                }
            }*/
        } catch (e: IOException) {
            // handles IO exceptions
            e.printStackTrace()
        }
    }

    @Test
    fun yesmovies() {
        val f = "https://yesmovies.ag/movie/filter/series.html".toJsoup()
        //println(f)

        val showInfo = f.select("div.ml-item").map {
            ShowInfo(
                name = it.select("a.ml-mask").attr("title"),
                url = it.select("a.ml-mask").attr("abs:href"),
                sources = Sources.KISSANIMEFREE
            )
        }

        //println(showInfo)

        val e = "${showInfo.first().url.removeSuffix(".html")}/watching.html".toJsoup()
        //println(e)

        val z = e.select("ul#episodes-sv-1")
        println(z)


    }

}