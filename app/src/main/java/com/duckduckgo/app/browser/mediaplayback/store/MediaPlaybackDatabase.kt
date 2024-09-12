package com.duckduckgo.app.browser.mediaplayback.store

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    exportSchema = true,
    version = 1,
    entities = [
        MediaPlaybackExceptionEntity::class,
    ],
)
abstract class MediaPlaybackDatabase : RoomDatabase() {
    abstract fun mediaPlaybackDao(): MediaPlaybackDao
}

val ALL_MIGRATIONS = emptyArray<Migration>()
