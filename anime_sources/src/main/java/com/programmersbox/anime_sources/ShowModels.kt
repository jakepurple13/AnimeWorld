package com.programmersbox.anime_sources

import java.io.Serializable

data class ShowInfo(val name: String, val url: String, val sources: Sources) : Serializable {
    fun getEpisodeInfo() = sources.getEpisodeInfo(this)
    internal val extras = mutableMapOf<String, Any?>()
}

data class Episode(
    val source: ShowInfo,
    val name: String,
    val description: String,
    val image: String?,
    val genres: List<String>,
    val episodes: List<EpisodeInfo>
)

class EpisodeInfo(val name: String, val url: String, private val sources: Sources) {
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