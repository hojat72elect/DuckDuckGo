

package com.duckduckgo.mobile.android.vpn.apps

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.common.utils.extensions.isDdgApp
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.apps.TrackingProtectionAppsRepository.ProtectionState
import com.duckduckgo.mobile.android.vpn.apps.TrackingProtectionAppsRepository.ProtectionState.PROTECTED
import com.duckduckgo.mobile.android.vpn.apps.TrackingProtectionAppsRepository.ProtectionState.UNPROTECTED
import com.duckduckgo.mobile.android.vpn.apps.TrackingProtectionAppsRepository.ProtectionState.UNPROTECTED_THROUGH_NETP
import com.duckduckgo.mobile.android.vpn.exclusion.SystemAppOverridesProvider
import com.duckduckgo.mobile.android.vpn.trackers.AppTrackerExcludedPackage
import com.duckduckgo.mobile.android.vpn.trackers.AppTrackerManualExcludedApp
import com.duckduckgo.mobile.android.vpn.trackers.AppTrackerRepository
import com.duckduckgo.networkprotection.api.NetworkProtectionExclusionList
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import logcat.logcat

interface TrackingProtectionAppsRepository {

    /** @return the list of installed apps and information about its excluded state */
    suspend fun getAppsAndProtectionInfo(): Flow<List<TrackingProtectionAppInfo>>

    /** @return the list of installed apps currently excluded */
    suspend fun getExclusionAppsList(): List<String>

    fun manuallyExcludedApps(): Flow<List<Pair<String, Boolean>>>

    /** Remove the app to the exclusion list so that its traffic does not go through the VPN */
    suspend fun manuallyEnabledApp(packageName: String)

    /** Add the app to the exclusion list so that its traffic goes through the VPN */
    suspend fun manuallyExcludeApp(packageName: String)

    /** Restore protection to the default list */
    suspend fun restoreDefaultProtectedList()

    /** Returns if an app tracking attempts are being blocked or not */
    suspend fun getAppProtectionStatus(packageName: String): ProtectionState
    enum class ProtectionState {
        PROTECTED,
        UNPROTECTED,
        UNPROTECTED_THROUGH_NETP,
    }
}

@ContributesBinding(
    scope = AppScope::class,
    boundType = TrackingProtectionAppsRepository::class,
)
@ContributesBinding(
    scope = AppScope::class,
    boundType = SystemAppOverridesProvider::class,
)
@SingleInstanceIn(AppScope::class)
class RealTrackingProtectionAppsRepository @Inject constructor(
    private val packageManager: PackageManager,
    private val appTrackerRepository: AppTrackerRepository,
    private val dispatcherProvider: DispatcherProvider,
    private val networkProtectionExclusionList: NetworkProtectionExclusionList,
    private val context: Context,
) : TrackingProtectionAppsRepository, SystemAppOverridesProvider {

    private var installedApps: Sequence<ApplicationInfo> = emptySequence()

    override suspend fun getAppsAndProtectionInfo(): Flow<List<TrackingProtectionAppInfo>> {
        return appTrackerRepository.getAppExclusionListFlow()
            .combine(appTrackerRepository.getManualAppExclusionListFlow()) { ddgExclusionList, manualList ->
                logcat { "getProtectedApps flow" }
                installedApps
                    .map {
                        val isExcluded = isExcludedFromAppTP(it, ddgExclusionList, manualList)
                        runBlocking {
                            TrackingProtectionAppInfo(
                                packageName = it.packageName,
                                name = packageManager.getApplicationLabel(it).toString(),
                                category = it.parseAppCategory(),
                                isExcluded = networkProtectionExclusionList.isExcluded(it.packageName) || isExcluded,
                                knownProblem = hasKnownIssue(it, ddgExclusionList),
                                userModified = isUserModified(it.packageName, manualList),
                            )
                        }
                    }
                    .sortedBy { it.name.lowercase() }
                    .toList()
            }.onStart {
                refreshInstalledApps()
            }.flowOn(dispatcherProvider.io())
    }

    private fun refreshInstalledApps() {
        logcat { "Excluded Apps: refreshInstalledApps" }
        installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filterNot { shouldNotBeShown(it) }
    }

    override suspend fun getExclusionAppsList(): List<String> = withContext(dispatcherProvider.io()) {
        val exclusionList = appTrackerRepository.getAppExclusionList()
        val manualExclusionList = appTrackerRepository.getManualAppExclusionList()
        return@withContext packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filter { isExcludedFromAppTP(it, exclusionList, manualExclusionList) }
            .sortedBy { it.name }
            .map { it.packageName }
            .toList()
    }

    override fun manuallyExcludedApps(): Flow<List<Pair<String, Boolean>>> {
        return appTrackerRepository.getManualAppExclusionListFlow().map { list -> list.map { it.packageId to it.isProtected } }
    }

    private fun shouldNotBeShown(appInfo: ApplicationInfo): Boolean {
        return context.isDdgApp(appInfo.packageName) || isSystemAppAndNotOverridden(appInfo)
    }

    private fun isSystemAppAndNotOverridden(appInfo: ApplicationInfo): Boolean {
        return if (appTrackerRepository.getSystemAppOverrideList().map { it.packageId }.contains(appInfo.packageName)) {
            false
        } else {
            appInfo.isSystemApp()
        }
    }

    private fun isExcludedFromAppTP(
        appInfo: ApplicationInfo,
        ddgExclusionList: List<AppTrackerExcludedPackage>,
        userExclusionList: List<AppTrackerManualExcludedApp>,
    ): Boolean {
        return context.isDdgApp(appInfo.packageName) ||
            isSystemAppAndNotOverridden(appInfo) ||
            isManuallyExcluded(appInfo, ddgExclusionList, userExclusionList)
    }

    private fun isManuallyExcluded(
        appInfo: ApplicationInfo,
        ddgExclusionList: List<AppTrackerExcludedPackage>,
        userExclusionList: List<AppTrackerManualExcludedApp>,
    ): Boolean {
        val userExcludedApp = userExclusionList.find { it.packageId == appInfo.packageName }
        if (userExcludedApp != null) {
            return !userExcludedApp.isProtected
        }

        return ddgExclusionList.any { it.packageId == appInfo.packageName }
    }

    private suspend fun hasKnownIssue(
        appInfo: ApplicationInfo,
        ddgExclusionList: List<AppTrackerExcludedPackage>,
    ): Int {
        if (networkProtectionExclusionList.isExcluded(appInfo.packageName)) {
            return TrackingProtectionAppInfo.EXCLUDED_THROUGH_NETP
        }
        if (BROWSERS.contains(appInfo.packageName)) {
            return TrackingProtectionAppInfo.LOADS_WEBSITES_EXCLUSION_REASON
        }
        if (ddgExclusionList.any { it.packageId == appInfo.packageName }) {
            return TrackingProtectionAppInfo.KNOWN_ISSUES_EXCLUSION_REASON
        }
        return TrackingProtectionAppInfo.NO_ISSUES
    }

    private fun isUserModified(
        packageName: String,
        userExclusionList: List<AppTrackerManualExcludedApp>,
    ): Boolean {
        val userExcludedApp = userExclusionList.find { it.packageId == packageName }
        return userExcludedApp != null
    }

    override suspend fun manuallyEnabledApp(packageName: String) {
        withContext(dispatcherProvider.io()) {
            appTrackerRepository.manuallyEnabledApp(packageName)
        }
    }

    override suspend fun manuallyExcludeApp(packageName: String) {
        withContext(dispatcherProvider.io()) {
            appTrackerRepository.manuallyExcludedApp(packageName)
        }
    }

    override suspend fun restoreDefaultProtectedList() {
        withContext(dispatcherProvider.io()) {
            appTrackerRepository.restoreDefaultProtectedList()
        }
    }

    override suspend fun getAppProtectionStatus(packageName: String): ProtectionState {
        logcat { "TrackingProtectionAppsRepository: Checking $packageName protection status" }
        val appInfo = runCatching { packageManager.getApplicationInfo(packageName, 0) }.getOrElse { return PROTECTED }
        val appExclusionList = appTrackerRepository.getAppExclusionList()
        val manualAppExclusionList = appTrackerRepository.getManualAppExclusionList()

        val isExcludedFromAppTP = isExcludedFromAppTP(appInfo, appExclusionList, manualAppExclusionList)

        return if (networkProtectionExclusionList.isExcluded(packageName)) {
            UNPROTECTED_THROUGH_NETP
        } else if (isExcludedFromAppTP) {
            UNPROTECTED
        } else {
            PROTECTED
        }
    }

    override fun getSystemAppOverridesList(): List<String> = appTrackerRepository.getSystemAppOverrideList().map { it.packageId }

    companion object {
        private val BROWSERS = listOf(
            "com.duckduckgo.mobile.android",
            "com.duckduckgo.mobile.android.debug",
            "com.duckduckgo.mobile.android.vpn",
            "com.duckduckgo.mobile.android.vpn.debug",
            "com.android.chrome",
            "org.mozilla.firefox",
            "com.opera.browser",
            "com.microsoft.emmx",
            "com.brave.browser",
            "com.UCMobile.intl",
            "com.android.browser",
            "com.sec.android.app.sbrowser",
            "info.guardianproject.orfo",
            "org.torproject.torbrowser_alpha",
            "mobi.mgeek.TunnyBrowser",
            "com.linkbubble.playstore",
            "org.adblockplus.browser",
            "arun.com.chromer",
            "com.flynx",
            "com.ghostery.android.ghostery",
            "com.cliqz.browser",
            "com.opera.mini.native",
            "com.uc.browser.en",
            "com.chrome.beta",
            "org.mozilla.firefox_beta",
            "com.opera.browser.beta",
            "com.opera.mini.native.beta",
            "com.sec.android.app.sbrowser.beta",
            "org.mozilla.fennec_fdroid",
            "org.mozilla.rocket",
            "com.chrome.dev",
            "com.chrome.canary",
            "org.mozilla.fennec_aurora",
            "org.mozilla.fennec",
            "com.google.android.apps.chrome",
            "org.chromium.chrome",
            "com.microsoft.bing",
            "com.yahoo.mobile.client.android.search",
            "com.google.android.apps.searchlite",
            "com.baidu.searchbox",
            "ru.yandex.searchplugin",
            "com.ecosia.android",
            "com.qwant.liberty",
            "com.qwantjunior.mobile",
            "com.nhn.android.search",
            "cz.seznam.sbrowser",
            "com.coccoc.trinhduyet",
        )
    }
}
