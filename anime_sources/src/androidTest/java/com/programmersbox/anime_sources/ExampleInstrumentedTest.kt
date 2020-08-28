package com.programmersbox.anime_sources

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.programmersbox.gsonutils.fromJson
import fr.arnaudguyon.xmltojsonlib.XmlToJson
import io.reactivex.Single
import org.jsoup.nodes.Document
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.programmersbox.anime_sources.test", appContext.packageName)
    }

    @Test
    fun kickass() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
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

        val f = KickAssAnime.getRecent(1)
        /*f.subscribeBy {
            println(it)
        }*/
        println(f.blockingGet())
    }

    object KickAssAnime : ShowApi(
        baseUrl = "https://www1.kickassanime.rs/",
        recentPath = "/feed/latest",
        allPath = "anime-list"
    ) {
        override fun getRecent(doc: Document): Single<List<ShowInfo>> = Single.create { emitter ->
            try {
                //println(doc.html())
                val f = XmlToJson.Builder(doc.html()).build().toString().also { Log.println(Log.ASSERT, "asdf", it) }.fromJson<Base>()
                //val f = doc.text().fromJson<Base>()
                println(f)
                emitter.onSuccess(emptyList())
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        override fun getList(doc: Document): Single<List<ShowInfo>> {
            TODO("Not yet implemented")
        }

        override fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode> = Single.create { emitter ->
            try {
                val regex = "appData = (.*?), dm = '';".toRegex().toPattern().matcher(doc.html())
                val z = (if (regex.find()) regex.group(1) else "").removeSuffix("|| {}").fromJson<KickAssClass>()?.anime

                emitter.onSuccess(
                    Episode(
                        source = source,
                        name = z?.name.orEmpty(),
                        description = z?.description.orEmpty(),
                        image = z?.imageUrl().orEmpty(),
                        genres = z?.genres?.mapNotNull(Genres::name).orEmpty(),
                        episodes = z?.episodes
                            ?.map { EpisodeInfo(name = it.epnum.orEmpty(), url = it.slug.orEmpty(), sources = source.sources) }
                            .orEmpty()
                    )
                )
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        override fun getVideoLink(info: EpisodeInfo): Single<List<Storage>> {
            TODO("Not yet implemented")
        }

        private data class Anime(
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
            val types: List<Any>?,
            val genres: List<Genres>?,
            val aid: String?,
            val favorited: Boolean?,
            val votes: Number?,
            val rating: Boolean?
        ) {
            fun imageUrl() = "https://www1.kickassanime.rs/uploads/$image"
            fun bannerUrl() = "https://www1.kickassanime.rs/uploads/$banner"
        }

        private data class KickAssClass(
            val clip: String?,
            val sig: String?,
            val vt: String?,
            val user: Any?,
            val ax: Any?,
            val notes: Notes?,
            val anime: Anime?
        )

        private data class Episodes(val epnum: String?, val name: Any?, val slug: String?, val createddate: String?, val num: String?)
        private data class Genres(val name: String?, val slug: String?)
        private data class Notes(val adblock: String?, val notes: String?)

        // result generated from /xml

        //data class Atom:link(val rel: String?, val href: String?, val type: String?)

        data class Base(val rss: Rss?)

        data class Channel(
            val image: Image?,
            val item: List<Item142255517>?,
            val docs: String?,
            val link: String?,
            val description: String?,
            val generator: String?,
            val language: String?,
            val title: String?
        )

        data class Image(val link: String?, val width: String?, val description: String?, val title: String?, val url: String?, val height: String?)

        data class Item142255517(val link: String?, val guid: String?, val title: String?, val pubDate: String?)

        data class Rss(val channel: Channel?, val version: String?)


    }
}