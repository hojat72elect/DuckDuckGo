package com.duckduckgo.sync.impl.engine

import android.app.Notification
import android.content.Context

class FakeNotificationBuilder() : SyncNotificationBuilder {
    override fun buildSyncPausedNotification(
        context: Context,
        addNavigationIntent: Boolean,
    ): Notification {
        return Notification()
    }

    override fun buildSyncErrorNotification(context: Context): Notification {
        return Notification()
    }

    override fun buildSyncSignedOutNotification(context: Context): Notification {
        return Notification()
    }
}
