

package com.duckduckgo.mobile.android.vpn.store

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.mobile.android.vpn.trackers.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.util.*
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor

@OptIn(ExperimentalCoroutinesApi::class)
internal class VpnDatabaseCallback(
    private val context: Context,
    private val vpnDatabase: Provider<VpnDatabase>,
    private val dispatcherProvider: DispatcherProvider,
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        ioThread {
            prepopulateAppTrackerBlockingList()
            prepopulateAppTrackerExclusionList()
            prepopulateAppTrackerExceptionRules()
        }
    }

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        ioThread {
            prepopulateAppTrackerBlockingList()
            prepopulateAppTrackerExclusionList()
            prepopulateAppTrackerExceptionRules()
        }
    }

    @VisibleForTesting
    internal fun prepopulateAppTrackerBlockingList() {
        context.resources.openRawResource(R.raw.full_app_trackers_blocklist).bufferedReader()
            .use { it.readText() }
            .also {
                val blocklist = getFullAppTrackerBlockingList(it)
                with(vpnDatabase.get().vpnAppTrackerBlockingDao()) {
                    insertTrackerBlocklist(blocklist.trackers)
                    insertAppPackages(blocklist.packages)
                    insertTrackerEntities(blocklist.entities)
                }
            }
    }

    private fun prepopulateAppTrackerExclusionList() {
        context.resources.openRawResource(R.raw.app_tracker_app_exclusion_list).bufferedReader()
            .use { it.readText() }
            .also {
                val excludedAppPackages = parseAppTrackerExclusionList(it)
                vpnDatabase.get().vpnAppTrackerBlockingDao().insertExclusionList(excludedAppPackages)
            }
    }

    private fun prepopulateAppTrackerExceptionRules() {
        context.resources.openRawResource(R.raw.app_tracker_exception_rules).bufferedReader()
            .use { it.readText() }
            .also { json ->
                val rules = parseJsonAppTrackerExceptionRules(json)
                vpnDatabase.get().vpnAppTrackerBlockingDao().insertTrackerExceptionRules(rules)
            }
    }

    private fun getFullAppTrackerBlockingList(json: String): AppTrackerBlocklist {
        return AppTrackerJsonParser.parseAppTrackerJson(Moshi.Builder().build(), json)
    }

    private fun parseAppTrackerExclusionList(json: String): List<AppTrackerExcludedPackage> {
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<JsonAppTrackerExclusionList> = moshi.adapter(JsonAppTrackerExclusionList::class.java)
        return adapter.fromJson(json)?.unprotectedApps.orEmpty()
    }

    private fun parseJsonAppTrackerExceptionRules(json: String): List<AppTrackerExceptionRule> {
        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<JsonAppTrackerExceptionRules> = moshi.adapter(JsonAppTrackerExceptionRules::class.java)
        return adapter.fromJson(json)?.rules.orEmpty()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun ioThread(f: () -> Unit) {
        // At most 1 thread will be doing IO
        dispatcherProvider.io().limitedParallelism(1).asExecutor().execute(f)
    }
}
