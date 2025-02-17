

package com.duckduckgo.mobile.android.vpn.stats

import androidx.annotation.WorkerThread
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.common.utils.formatters.time.DatabaseDateFormatter
import com.duckduckgo.mobile.android.vpn.model.*
import com.duckduckgo.mobile.android.vpn.store.VpnDatabase
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.*

interface AppTrackerBlockingStatsRepository {

    data class TimeWindow(
        val value: Long,
        val unit: TimeUnit,
    ) {
        fun asString(): String {
            return DatabaseDateFormatter.timestamp(LocalDateTime.now().minusSeconds(unit.toSeconds(value)))
        }
    }

    private fun noEndDate(): String {
        return DatabaseDateFormatter.timestamp(LocalDateTime.of(9999, 1, 1, 0, 0))
    }

    fun insert(tracker: List<VpnTracker>)

    fun getVpnTrackers(
        startTime: () -> String,
        endTime: String = noEndDate(),
    ): Flow<List<VpnTracker>>

    fun getMostRecentVpnTrackers(startTime: () -> String): Flow<List<VpnTrackerWithEntity>>

    fun getVpnTrackersSync(
        startTime: () -> String,
        endTime: String = noEndDate(),
    ): List<VpnTracker>

    fun getTrackersForAppFromDate(
        date: String,
        packageName: String,
    ): Flow<List<VpnTrackerWithEntity>>

    fun getBlockedTrackersCountBetween(
        startTime: () -> String,
        endTime: String = noEndDate(),
    ): Flow<Int>

    fun getTrackingAppsCountBetween(
        startTime: () -> String,
        endTime: String = noEndDate(),
    ): Flow<Int>

    suspend fun containsVpnTrackers(): Boolean

    fun deleteTrackersUntil(startTime: String)

    fun getLatestTracker(): Flow<VpnTracker?>

    fun getTrackersForApp(appPackage: String): List<VpnTracker>

    fun deleteAllTrackers()
}

class RealAppTrackerBlockingStatsRepository constructor(
    vpnDatabase: VpnDatabase,
    private val dispatchers: DispatcherProvider,
) : AppTrackerBlockingStatsRepository {

    private val trackerDao = vpnDatabase.vpnTrackerDao()

    override fun insert(tracker: List<VpnTracker>) {
        trackerDao.insert(tracker)
    }

    override fun getVpnTrackers(
        startTime: () -> String,
        endTime: String,
    ): Flow<List<VpnTracker>> {
        return trackerDao.getTrackersBetween(startTime(), endTime)
            .conflate()
            .map { it.asListOfVpnTracker() }
            .distinctUntilChanged()
            .map { it.filter { tracker -> tracker.timestamp >= startTime() } }
            .flowOn(dispatchers.io())
    }

    @WorkerThread
    override fun getVpnTrackersSync(
        startTime: () -> String,
        endTime: String,
    ): List<VpnTracker> {
        return trackerDao.getTrackersBetweenSync(startTime(), endTime)
            .filter { it.trackerEntity != null }
            .map { it.tracker }
            .filter { tracker -> tracker.timestamp >= startTime() }
    }

    @WorkerThread
    override fun getMostRecentVpnTrackers(startTime: () -> String): Flow<List<VpnTrackerWithEntity>> {
        return trackerDao.getPagedTrackersSince(startTime())
            .conflate()
            .map { it.asListOfVpnTrackerWithEntity() }
            .distinctUntilChanged()
    }

    @WorkerThread
    override fun getTrackersForAppFromDate(
        date: String,
        packageName: String,
    ): Flow<List<VpnTrackerWithEntity>> {
        return trackerDao.getTrackersForAppFromDate(date, packageName)
            .conflate()
            .map { it.asListOfVpnTrackerWithEntity() }
            .distinctUntilChanged()
    }

    @WorkerThread
    override fun getBlockedTrackersCountBetween(
        startTime: () -> String,
        endTime: String,
    ): Flow<Int> {
        return trackerDao.getTrackersCountBetween(startTime(), endTime)
            .conflate()
            .distinctUntilChanged()
    }

    @WorkerThread
    override fun getTrackingAppsCountBetween(
        startTime: () -> String,
        endTime: String,
    ): Flow<Int> {
        return trackerDao.getTrackingAppsCountBetween(startTime(), endTime)
            .conflate()
            .distinctUntilChanged()
    }

    override suspend fun containsVpnTrackers(): Boolean {
        return trackerDao.tableIsNotEmpty()
    }

    override fun deleteTrackersUntil(startTime: String) {
        trackerDao.deleteOldDataUntil(startTime)
    }

    override fun getLatestTracker(): Flow<VpnTracker?> {
        return trackerDao.getLatestTracker()
            .filter { it?.trackerEntity != null }
            .map { it?.tracker }
    }

    override fun getTrackersForApp(appPackage: String): List<VpnTracker> {
        return trackerDao.getTrackersForApp(appPackage)
            .filter { it.trackerEntity != null }
            .map { it.tracker }
    }

    override fun deleteAllTrackers() {
        trackerDao.deleteAllTrackers()
    }
}
