package com.programmersbox.anime_sources

import com.programmersbox.anime_sources.models.KissAnimeFree
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

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

    @After
    fun finished() {
        Runtime.getRuntime().exec("say finished").waitFor()
    }

    /*
curl 'https://fcdn.stream/api/source/5dr6xfd4pe2r66r' \
  -H 'authority: fcdn.stream' \
  -H 'accept: *' \
    -H 'x-requested-with: XMLHttpRequest' \
    -H 'user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36' \
    -H 'content-type: application/x-www-form-urlencoded; charset=UTF-8' \
    -H 'origin: https://fcdn.stream' \
    -H 'sec-fetch-site: same-origin' \
    -H 'sec-fetch-mode: cors' \
    -H 'sec-fetch-dest: empty' \
    -H 'referer: https://fcdn.stream/v/5dr6xfd4pe2r66r' \
    -H 'accept-language: en-US,en;q=0.9' \
    --data-raw 'r=https%3A%2F%2Fcdnlow.me%2Fembed%2Fxstream.php%3Furl%3DUmxjMjBzMXZad3lTcC93OXhvT2xaS3BlRTdFeHpUU2pTcEhYelFkc0lDRE9ZMW5CbENVMmhielZ6T2p1bmc5OA%3D%3D%26xstream2%3Dtrue&d=fcdn.stream' \
    --compressed
     */

}