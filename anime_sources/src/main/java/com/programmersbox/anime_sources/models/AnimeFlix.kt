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
            val g = f.fromJson<List<Video.Base>>().orEmpty().firstOrNull { it.type == "hls" }

            println(g)

            //val z = Jsoup.connect(g?.file!!).get()
            //println(z)

            val testStuff = """
                #EXTM3U
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=4110034,RESOLUTION=1280x720,FRAME-RATE=23.974,CODECS="avc1.640028,mp4a.40.2"
                https://a-vrv.akamaized.net/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245797.mp4/index-v1-a1.m3u8?t=exp=1598796976~acl=/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245797.mp4/*~hmac=00cda8de0066c340fbfb055004cfd26c11b478617076288db62ad70a0c90daf0
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=4110034,RESOLUTION=1280x720,FRAME-RATE=23.974,CODECS="avc1.640028,mp4a.40.2"
                https://v.vrv.co/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245797.mp4/index-v1-a1.m3u8?Expires=1598796976&Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cHM6Ly92LnZydi5jby9ldnMxLzg4ZDJiMWU3ZjkyOTgzNzlkN2M1NDA0YmU3MTJjODZiL2Fzc2V0cy9kM2FjZWVlMDAwMTdlZDRkM2U0MDExM2RlMzhlMzhkZl8zMjQ1Nzk3Lm1wNC8qIiwiQ29uZGl0aW9uIjp7IkRhdGVMZXNzVGhhbiI6eyJBV1M6RXBvY2hUaW1lIjoxNTk4Nzk2OTc2fX19XX0_&Signature=IigKNP3uodqIniE1p7kYZm8sWjg3xgfDcTb1U9r4~Wh3JFvVse-X~6cYo2qv6dLHdOrE7NlpGd-Y76NWJaddX8duwjD5vxHamZyB9FRl6iA7yzFTDPx3aKevq2NYx8WNPeB~1BEAIO1fcizxVazPo9-hvoG7XuRpPWa4tjE2JF5drSz25uND2Me~bVMh80beo~aeQRm0rtKrm5y17HQ4o~HlaB~KTo2E8V7PT3A~cv5Yloqac3JklfELm80nZW1PdqKafBSmVomNdMl7quX-LWgf7gfHIZzeO5rVsTVM2sZ6ulO27UBOl~DDrBdgxYmB7Guf2i-6IFFjUMLjkz2YXw__&Key-Pair-Id=APKAJMWSQ5S7ZB3MF5VA
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=8110165,RESOLUTION=1920x1080,FRAME-RATE=23.974,CODECS="avc1.640028,mp4a.40.2"
                https://a-vrv.akamaized.net/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245800.mp4/index-v1-a1.m3u8?t=exp=1598796976~acl=/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245800.mp4/*~hmac=5513bece0bac3f4d2271a0ca936d659f7b488196bc8e34519ce66bc02b160226
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=8110165,RESOLUTION=1920x1080,FRAME-RATE=23.974,CODECS="avc1.640028,mp4a.40.2"
                https://v.vrv.co/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245800.mp4/index-v1-a1.m3u8?Expires=1598796976&Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cHM6Ly92LnZydi5jby9ldnMxLzg4ZDJiMWU3ZjkyOTgzNzlkN2M1NDA0YmU3MTJjODZiL2Fzc2V0cy9kM2FjZWVlMDAwMTdlZDRkM2U0MDExM2RlMzhlMzhkZl8zMjQ1ODAwLm1wNC8qIiwiQ29uZGl0aW9uIjp7IkRhdGVMZXNzVGhhbiI6eyJBV1M6RXBvY2hUaW1lIjoxNTk4Nzk2OTc2fX19XX0_&Signature=d2PZCYNSLmbDQXrOp463Ei4vwcFKffuAqLF4-WyCKkGACF5NHBc2ZOr~PQLZwobzJjqmbg3~ZdmDrd9gfln-6LhXXfxrRxM5P3s3MnV6xgwba77aXXzbXH~thi9On8z3YiNVMSgHQoO3DPaU10mTU~mKBlZcmH13eQ0oMFbvP-hgtsdHWd1kQGNm~N9DlL~NmWV1qyBY1QfibF0TS6Nod4zwLveqsKUh2Cspq9MsvzK8gXNW2h2~aggraOjbvozbUrsvNKfF3L1rTlFS9iafOjE96rtsfDHGXA5kkEx1~vNS4VcH4CfCPnYql2DVj3uZF8~yZGGDRhhwstO5FMwNXw__&Key-Pair-Id=APKAJMWSQ5S7ZB3MF5VA
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2092085,RESOLUTION=848x480,FRAME-RATE=23.974,CODECS="avc1.4d401f,mp4a.40.2"
                https://a-vrv.akamaized.net/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245794.mp4/index-v1-a1.m3u8?t=exp=1598796976~acl=/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245794.mp4/*~hmac=d21698f9634cbd8b47c12a49a4b7b0ceb493128879d4b6391259e0b014ba65a8
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2092085,RESOLUTION=848x480,FRAME-RATE=23.974,CODECS="avc1.4d401f,mp4a.40.2"
                https://v.vrv.co/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245794.mp4/index-v1-a1.m3u8?Expires=1598796976&Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cHM6Ly92LnZydi5jby9ldnMxLzg4ZDJiMWU3ZjkyOTgzNzlkN2M1NDA0YmU3MTJjODZiL2Fzc2V0cy9kM2FjZWVlMDAwMTdlZDRkM2U0MDExM2RlMzhlMzhkZl8zMjQ1Nzk0Lm1wNC8qIiwiQ29uZGl0aW9uIjp7IkRhdGVMZXNzVGhhbiI6eyJBV1M6RXBvY2hUaW1lIjoxNTk4Nzk2OTc2fX19XX0_&Signature=pEJM0t6d~xKxHwLmlkBsTqxlY8zGpcEv-MbrLCQN3wclR9DRCXIxV9Aw1d42atKzEomVBFcoO1IIOjYw4U8USTW4FR7giNgUKYkUC~-09ZjSHiJtQdCFA2hIKaGg15n6zj0aIv4Qr0iq7xJnPTFFgu7FMPf-XMDqRN~LYJ078HncSp3enCKL4jqaxEUlnN2rWOK1BMMNc0Y9h3fQBlo23307p2bibwwRM7C1AmHf6jIb3rSoMEgNEeHR41cSnM~-3Sp4CLiT8oqzRPDAqbeGxTx8U1X7tHuSELeaLufroXxicFYg8MyigB6~Sr4ufW3PA22XCECDMP1tQpVbIM8BwQ__&Key-Pair-Id=APKAJMWSQ5S7ZB3MF5VA
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1091554,RESOLUTION=640x360,FRAME-RATE=23.974,CODECS="avc1.4d401e,mp4a.40.2"
                https://a-vrv.akamaized.net/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245788.mp4/index-v1-a1.m3u8?t=exp=1598796976~acl=/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245788.mp4/*~hmac=6b9fe2fafa642a1d42ebbda33d39a46b334590393248f8ab21d3ee9b137d993b
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=1091554,RESOLUTION=640x360,FRAME-RATE=23.974,CODECS="avc1.4d401e,mp4a.40.2"
                https://v.vrv.co/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245788.mp4/index-v1-a1.m3u8?Expires=1598796976&Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cHM6Ly92LnZydi5jby9ldnMxLzg4ZDJiMWU3ZjkyOTgzNzlkN2M1NDA0YmU3MTJjODZiL2Fzc2V0cy9kM2FjZWVlMDAwMTdlZDRkM2U0MDExM2RlMzhlMzhkZl8zMjQ1Nzg4Lm1wNC8qIiwiQ29uZGl0aW9uIjp7IkRhdGVMZXNzVGhhbiI6eyJBV1M6RXBvY2hUaW1lIjoxNTk4Nzk2OTc2fX19XX0_&Signature=OS0gDeaqYo7Ud-gC40Bt2~ESqO8-~L9dz0mKfZT87zkLk5CafQX82L-aY6lq5INPoB71H6DDB2iywRiFsJYb8OTmA3bJZew1V6MG38BlmNyGfOJBJdH0HGnXcrJD-pa42gI6DKI2BNQKjBf6DCtN6NqRNPabKgN95uXwQOwv7u7gcTXAi8hHYYuAYuWcn8QgoJj6AjNELAOTOVhvevm5px6n9beDHzSmY3d5SnhE4u53GZh-DRP4HfXUs9TzpHyTz4dzEA2mfYWmN7uxpEC4lK7ZDui3aYt20HWGHlkfoYIxlPdNNhHCHOdYqoM3WB56QM-nMrlOzxxFrdHN0grx5g__&Key-Pair-Id=APKAJMWSQ5S7ZB3MF5VA
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=561682,RESOLUTION=428x240,FRAME-RATE=23.974,CODECS="avc1.42c015,mp4a.40.2"
                https://a-vrv.akamaized.net/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245791.mp4/index-v1-a1.m3u8?t=exp=1598796976~acl=/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245791.mp4/*~hmac=96ce2c4c43f3148ab1b8f620a6f772f83737520d8240d354280381d489b9e8f1
                #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=561682,RESOLUTION=428x240,FRAME-RATE=23.974,CODECS="avc1.42c015,mp4a.40.2"
                https://v.vrv.co/evs1/88d2b1e7f9298379d7c5404be712c86b/assets/d3aceee00017ed4d3e40113de38e38df_3245791.mp4/index-v1-a1.m3u8?Expires=1598796976&Policy=eyJTdGF0ZW1lbnQiOlt7IlJlc291cmNlIjoiaHR0cHM6Ly92LnZydi5jby9ldnMxLzg4ZDJiMWU3ZjkyOTgzNzlkN2M1NDA0YmU3MTJjODZiL2Fzc2V0cy9kM2FjZWVlMDAwMTdlZDRkM2U0MDExM2RlMzhlMzhkZl8zMjQ1NzkxLm1wNC8qIiwiQ29uZGl0aW9uIjp7IkRhdGVMZXNzVGhhbiI6eyJBV1M6RXBvY2hUaW1lIjoxNTk4Nzk2OTc2fX19XX0_&Signature=mvquvz8P82d7~sAls5zjuX33wkxy1EeqwyhXKegZShGMv6ivLLtxH6I95FNtJe-6b9k6Q4GWkEc6KhajqvDfvDLI9GZXh4QEvKKUinDaici4kNcA3t1yU4Fwo5xcwdZnziJTboLQlY22CcOTDHU-pxnZdV2UFYHVIiMTCGxnXRQCVF7dQJ5R2s~JWhCghdTopjgpolXlt7JzO4vMukp1KZ8HakYx4hUOtyXKROYdtpNXM-D7pLvNYwToImTa5hXkk5re-jEdTf8gsGfXXBPuJ6~rdwAvgYPbBsrnvq3vSdQ8f-L7EdMimDiYf~m4i1WcfY9aLfssD4E~NlnZeWnU~w__&Key-Pair-Id=APKAJMWSQ5S7ZB3MF5VA
            """.trimIndent()

            val link = if (g?.type == "hls") {
                val z = try {
                    //Jsoup.connect(g.file!!).get()
                    testStuff
                } catch (e: Exception) {
                    getApi(g.file!!)
                }.toString()
                //println(z)
                val data = z.split("\n")
                val reg = "RESOLUTION=(.*)x(.*)".toRegex().toPattern()
                val res = "720"//g.resolution?.removeSuffix("p")
                var l = ""
                var count = 0
                for ((i, s) in data.withIndex()) {
                    val match = reg.matcher(s)
                    val k = if (match.find()) match.group(2)?.split(",")?.get(0) else null
                    //println(k)
                    if (k == res) {
                        l = data[i + 1]
                        break
                    }

                    if (count == 0) {
                        l = data[i + 1]
                    }
                    count++
                }
                println(l)
                val v = getApi(l)
                println(v)
                l
            } else {
                g?.file
            }.toString()

            val storage = Storage(
                link = link,//if(url.find()) url.group(1)!! else "",
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