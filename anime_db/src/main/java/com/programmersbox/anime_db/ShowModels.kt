package com.programmersbox.anime_db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.programmersbox.anime_sources.Sources

@Entity(tableName = "FavoriteShow")
data class ShowDbModel(
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @PrimaryKey
    @ColumnInfo(name = "showUrl")
    val showUrl: String,
    @ColumnInfo(name = "imageUrl")
    val imageUrl: String,
    @ColumnInfo(name = "sources")
    val source: Sources,
    @ColumnInfo(name = "numEpisodes", defaultValue = "0")
    var numEpisodes: Int = 0
)

@Entity(tableName = "EpisodeWatched")
data class EpisodeWatched(
    @PrimaryKey
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "showUrl")
    val showUrl: String
)
