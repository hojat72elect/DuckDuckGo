package com.duckduckgo.app.browser.mediaplayback.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class MediaPlaybackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(domains: List<MediaPlaybackExceptionEntity>)

    @Transaction
    open fun updateAll(domains: List<MediaPlaybackExceptionEntity>) {
        deleteAll()
        insertAll(domains)
    }

    @Query("select * from media_playback_user_gesture_exceptions")
    abstract fun getAll(): List<MediaPlaybackExceptionEntity>

    @Query("delete from media_playback_user_gesture_exceptions")
    abstract fun deleteAll()
}
