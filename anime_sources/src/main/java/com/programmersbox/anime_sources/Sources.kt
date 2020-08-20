package com.programmersbox.anime_sources

import com.programmersbox.anime_sources.models.AnimeToonApi
import com.programmersbox.anime_sources.models.AnimeToonDubbed
import com.programmersbox.anime_sources.models.GogoAnimeApi
import com.programmersbox.anime_sources.utils.toJsoup
import io.reactivex.Single
import org.jsoup.nodes.Document

interface ShowApiService {
    fun getRecent(): Single<List<ShowInfo>>
    fun getList(): Single<List<ShowInfo>>
    fun searchList(text: CharSequence, list: List<ShowInfo>): List<ShowInfo>
    fun getEpisodeInfo(source: ShowInfo): Single<Episode>
    fun getVideoLink(info: EpisodeInfo): Single<List<Storage>>
}

enum class Sources(private val api: ShowApi) : ShowApiService by api {
    GOGOANIME(GogoAnimeApi),
    ANIMETOON(AnimeToonApi), DUBBED_ANIME(AnimeToonDubbed)//, ANIMETOON_MOVIES(AnimeToonMovies), ANIMETOON_DUBBED(AnimeToonDubbed), ANIMETOON_RECENT(AnimeToonRecent)
    //PUTLOCKER(PutLocker), PUTLOCKER_RECENT(PutLockerRecent);
    ;

    companion object {
        fun getSourceByUrl(url: String) = values().find { url.contains(it.name, true) }
        //fun getAll() = values().flatMap(Sources::getList)

        //fun getAllRecent() = arrayOf(GOGOANIME_RECENT, ANIMETOON_RECENT, PUTLOCKER_RECENT).flatMap(Sources::getList)
        //operator fun get(vararg sources: Sources) = sources.flatMap(Sources::getList)
    }
}

abstract class ShowApi(
    internal val baseUrl: String,
    internal val allPath: String,
    internal val recentPath: String
) : ShowApiService {
    private fun recent() = "$baseUrl/$recentPath".toJsoup()
    private fun all() = "$baseUrl/$allPath".toJsoup()

    internal abstract fun getRecent(doc: Document): Single<List<ShowInfo>>
    internal abstract fun getList(doc: Document): Single<List<ShowInfo>>

    override fun searchList(text: CharSequence, list: List<ShowInfo>): List<ShowInfo> =
        if (text.isEmpty()) list else list.filter { it.name.contains(text, true) }

    override fun getRecent() = getRecent(recent())
    override fun getList() = getList(all()).map { it.sortedBy(ShowInfo::name) }
    override fun getEpisodeInfo(source: ShowInfo): Single<Episode> = getEpisodeInfo(source, source.url.toJsoup())
    internal abstract fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode>
}