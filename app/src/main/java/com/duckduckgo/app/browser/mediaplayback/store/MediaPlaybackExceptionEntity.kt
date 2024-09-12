package com.duckduckgo.app.browser.mediaplayback.store

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.duckduckgo.feature.toggles.api.FeatureExceptions

@Entity(tableName = "media_playback_user_gesture_exceptions")
data class MediaPlaybackExceptionEntity(@PrimaryKey val domain: String)

fun MediaPlaybackExceptionEntity.toFeatureException(): FeatureExceptions.FeatureException {
    return FeatureExceptions.FeatureException(domain = this.domain, reason = null)
}
