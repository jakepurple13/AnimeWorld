package com.programmersbox.anime_sources

data class ShowInfo(val name: String, val url: String, internal val sources: ShowApiService) {
    fun getEpisodeInfo() = sources.getEpisodeInfo(this)
}

data class Episode(
    val source: ShowInfo,
    val name: String,
    val description: String,
    val image: String?,
    val genres: List<String>,
    val episodes: List<EpisodeInfo>
)

class EpisodeInfo(val name: String, val url: String, private val sources: ShowApiService) {
    fun getVideoLink() = sources.getVideoLink(this)
    override fun toString(): String = "EpisodeInfo(name=$name, url=$url)"
}

internal class NormalLink(var normal: Normal? = null)
internal class Normal(var storage: Array<Storage>? = emptyArray())
data class Storage(
    var sub: String? = null,
    var source: String? = null,
    var link: String? = null,
    var quality: String? = null,
    var filename: String? = null
)