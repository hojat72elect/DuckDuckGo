

package com.duckduckgo.mobile.android.vpn.trackers

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

/** This table contains the list of app trackers to be blocked */
@Entity(tableName = "vpn_app_tracker_blocking_list")
data class AppTracker(
    @PrimaryKey val hostname: String,
    val trackerCompanyId: Int,
    @Embedded val owner: TrackerOwner,
    @Embedded val app: TrackerApp,
    @Deprecated("This field is no longer used. Stays to avoid db migration, SQLite doesn't allow rename columns")
    val isCdn: Boolean = false,
)

@Entity(tableName = "vpn_app_tracker_blocking_list_metadata")
data class AppTrackerMetadata(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val eTag: String?,
)

@Entity(tableName = "vpn_app_tracker_blocking_app_packages")
data class AppTrackerPackage(
    @PrimaryKey val packageName: String,
    val entityName: String,
)

@Entity(tableName = "vpn_app_tracker_exclusion_list")
data class AppTrackerExcludedPackage(
    @field:Json(name = "packageName")
    @PrimaryKey
    val packageId: String,
    val reason: String,
)

@Entity(tableName = "vpn_app_tracker_exclusion_list_metadata")
data class AppTrackerExclusionListMetadata(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val eTag: String?,
)

@Entity(tableName = "vpn_app_tracker_system_app_override_list")
data class AppTrackerSystemAppOverridePackage(
    @PrimaryKey val packageId: String,
)

@Entity(tableName = "vpn_app_tracker_system_app_override_list_metadata")
data class AppTrackerSystemAppOverrideListMetadata(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val eTag: String?,
)

@Entity(tableName = "vpn_app_tracker_exception_rules")
data class AppTrackerExceptionRule(
    @PrimaryKey
    val rule: String,
    val packageNames: List<String>,
)

@Entity(tableName = "vpn_app_tracker_exception_rules_metadata")
data class AppTrackerExceptionRuleMetadata(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val eTag: String?,
)

@Entity(tableName = "vpn_app_tracker_manual_exclusion_list")
data class AppTrackerManualExcludedApp(
    @PrimaryKey val packageId: String,
    val isProtected: Boolean,
)

@Entity(tableName = "vpn_app_tracker_entities")
data class AppTrackerEntity(
    @PrimaryKey val trackerCompanyId: Int,
    val entityName: String,
    val score: Int,
    val signals: List<String>,
)

data class JsonAppBlockingList(
    val version: String,
    val trackers: Map<String, JsonAppTracker>,
    val packageNames: Map<String, String>,
    val entities: Map<String, JsonTrackingSignal>,
)

class JsonAppTracker(
    val owner: TrackerOwner,
    @field:Json(name = "default")
    val defaultAction: String? = null,
)

class JsonTrackingSignal(
    val score: Int,
    val signals: List<String>,
)

data class TrackerOwner(
    val name: String,
    val displayName: String,
)

@Deprecated("This obj is no longer used. Stays to avoid db migration, SQLite doesn't allow renaming columns")
data class TrackerApp(
    val score: Int,
    val prevalence: Double,
)

sealed class AppTrackerType {
    data class FirstParty(val tracker: AppTracker) : AppTrackerType()
    data class ThirdParty(val tracker: AppTracker) : AppTrackerType()
    object NotTracker : AppTrackerType()
}

data class AppTrackerBlocklist(
    val version: String,
    val trackers: List<AppTracker>,
    val packages: List<AppTrackerPackage>,
    val entities: List<AppTrackerEntity>,
)

/** JSON Model that represents the app exclusion list */
data class JsonAppTrackerExclusionList(
    val unprotectedApps: List<AppTrackerExcludedPackage>,
)

/** JSON Model that represents the system app overrides */
data class JsonAppTrackerSystemAppOverrides(
    val rules: List<String>,
)

/** JSON Model that represents the app tracker rule list */
data class JsonAppTrackerExceptionRules(
    val rules: List<AppTrackerExceptionRule>,
)
