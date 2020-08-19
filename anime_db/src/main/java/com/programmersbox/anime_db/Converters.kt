package com.programmersbox.anime_db

import androidx.room.TypeConverter
import com.programmersbox.anime_sources.Sources
import com.programmersbox.gsonutils.fromJson
import com.programmersbox.gsonutils.toJson

class Converters {
    /*@TypeConverter
    fun fromChapterModel(value: List<ChapterModel>): String = value.toJson()

    @TypeConverter
    fun stringToChapterModel(value: String): List<ChapterModel>? = value.fromJson<List<ChapterModel>>()*/

    /*@TypeConverter
    fun toShowModel(value: Episode) = ShowDbModel(
        title = value.name,
        description = value.description,
        showUrl = value.source.url,
        imageUrl = value.image ?: "",
        source = value.source.sources,
        numEpisodes = value.episodes.size
    )*/

    @TypeConverter
    fun fromList(value: List<String>): String = value.toJson()

    @TypeConverter
    fun toList(value: String): List<String>? = value.fromJson<List<String>>()

    @TypeConverter
    fun fromSource(value: Sources): String = value.toJson()

    @TypeConverter
    fun toSource(value: String): Sources? = value.fromJson<Sources>()
}