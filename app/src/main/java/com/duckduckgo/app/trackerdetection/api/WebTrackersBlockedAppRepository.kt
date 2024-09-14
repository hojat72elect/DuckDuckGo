

package com.duckduckgo.app.trackerdetection.api

import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.trackerdetection.db.WebTrackerBlocked
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class WebTrackersBlockedAppRepository @Inject constructor(appDatabase: AppDatabase) : WebTrackersBlockedRepository {

    private val dao = appDatabase.webTrackersBlockedDao()

    override fun get(
        startTime: () -> String,
        endTime: String,
    ): Flow<List<WebTrackerBlocked>> {
        return dao.getTrackersBetween(startTime(), endTime)
            .distinctUntilChanged()
            .map { it.filter { tracker -> tracker.timestamp >= startTime() } }
    }
}
