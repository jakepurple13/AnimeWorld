package com.programmersbox.anime_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ShowDbModel::class, EpisodeWatched::class], version = 1)
@TypeConverters(Converters::class)
abstract class ShowDatabase : RoomDatabase() {

    abstract fun showDao(): ShowDao

    companion object {

        @Volatile
        private var INSTANCE: ShowDatabase? = null

        fun getInstance(context: Context): ShowDatabase =
            INSTANCE ?: synchronized(this) { INSTANCE ?: buildDatabase(context).also { INSTANCE = it } }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, ShowDatabase::class.java, "show.db").build()
    }
}