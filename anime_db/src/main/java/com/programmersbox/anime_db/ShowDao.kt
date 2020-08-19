package com.programmersbox.anime_db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single


@Dao
interface ShowDao {

    @Query("SELECT * FROM FavoriteShow WHERE showUrl = :url")
    fun getShowById(url: String): Single<ShowDbModel>

    @Query("SELECT * FROM FavoriteShow WHERE showUrl = :url")
    fun getShowByIdFlow(url: String): Flowable<ShowDbModel>

    @Query("SELECT * FROM FavoriteShow WHERE showUrl = :url")
    fun getShowByIdMaybe(url: String): Maybe<ShowDbModel>

    @Update
    fun updateShowById(model: ShowDbModel): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertShow(show: ShowDbModel): Completable

    @Delete
    fun deleteShow(show: ShowDbModel): Completable

    @Query("SELECT * FROM FavoriteShow")
    fun getAllShow(): Flowable<List<ShowDbModel>>

    @Query("SELECT * FROM FavoriteShow")
    fun getAllShowSync(): List<ShowDbModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEpisode(episode: EpisodeWatched): Completable

    @Delete
    fun deleteEpisode(episode: EpisodeWatched): Completable

    @Query("SELECT * FROM EpisodeWatched WHERE showUrl = :url")
    fun getWatchedEpisodesById(url: String): Flowable<List<EpisodeWatched>>

    @Query("SELECT * FROM EpisodeWatched WHERE showUrl = :url")
    fun getWatchedEpisodesByIdNonFlow(url: String): List<EpisodeWatched>

    @Query("SELECT * FROM EpisodeWatched")
    fun getAllEpisodesWatched(): List<EpisodeWatched>

}