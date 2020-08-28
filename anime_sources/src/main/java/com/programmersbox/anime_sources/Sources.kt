package com.programmersbox.anime_sources

import com.programmersbox.anime_sources.models.*
import com.programmersbox.anime_sources.utils.toJsoup
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document

interface ShowApiService {
    val baseUrl: String
    val canScroll: Boolean get() = false
    fun getRecent(page: Int = 1): Single<List<ShowInfo>>
    fun getList(page: Int = 1): Single<List<ShowInfo>>
    fun searchList(text: CharSequence, list: List<ShowInfo>): List<ShowInfo>
    fun getEpisodeInfo(source: ShowInfo): Single<Episode>
    fun getVideoLink(info: EpisodeInfo): Single<List<Storage>>
}

enum class Sources(private val api: ShowApi) : ShowApiService by api {
    GOGOANIME(GogoAnimeApi),
    ANIMETOON(AnimeToonApi), DUBBED_ANIME(AnimeToonDubbed), ANIMETOON_MOVIES(AnimeToonMovies),

    //PUTLOCKER(PutLocker), PUTLOCKER_RECENT(PutLockerRecent);
    KISSANIMEFREE(KissAnimeFree),
    //KICKASSANIME(KickAssAnime)
    //ANIMEFLIX(AnimeFlix)
    ;

    companion object {
        fun getSourceByUrl(url: String) = values().find { url.contains(it.name, true) }
        //fun getAll() = values().flatMap(Sources::getList)

        //fun getAllRecent() = arrayOf(GOGOANIME_RECENT, ANIMETOON_RECENT, PUTLOCKER_RECENT).flatMap(Sources::getList)
        //operator fun get(vararg sources: Sources) = sources.flatMap(Sources::getList)
    }
}

abstract class ShowApi(
    override val baseUrl: String,
    internal val allPath: String,
    internal val recentPath: String
) : ShowApiService {
    //TODO: Add page
    private fun recent(page: Int = 1) = "$baseUrl/$recentPath${recentPage(page)}".toJsoup()
    private fun all(page: Int = 1) = "$baseUrl/$allPath${allPage(page)}".toJsoup()

    internal open fun recentPage(page: Int): String = ""
    internal open fun allPage(page: Int): String = ""

    internal abstract fun getRecent(doc: Document): Single<List<ShowInfo>>
    internal abstract fun getList(doc: Document): Single<List<ShowInfo>>

    override fun searchList(text: CharSequence, list: List<ShowInfo>): List<ShowInfo> =
        if (text.isEmpty()) list else list.filter { it.name.contains(text, true) }

    override fun getRecent(page: Int) = Single.create<Document> { it.onSuccess(recent(page)) }
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .flatMap { getRecent(it) }

    override fun getList(page: Int) = Single.create<Document> { it.onSuccess(all(page)) }
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .flatMap { getList(it) }
        .map { it.sortedBy(ShowInfo::name) }

    override fun getEpisodeInfo(source: ShowInfo): Single<Episode> = Single.create<Document> { it.onSuccess(source.url.toJsoup()) }
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .flatMap { getEpisodeInfo(source, it) }

    internal abstract fun getEpisodeInfo(source: ShowInfo, doc: Document): Single<Episode>
}