package com.duckduckgo.vpn.internal.feature.rules

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.ConflatedJob
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.mobile.android.vpn.stats.AppTrackerBlockingStatsRepository
import com.duckduckgo.mobile.android.vpn.store.VpnDatabase
import com.duckduckgo.mobile.android.vpn.trackers.AppTrackerExceptionRule
import com.duckduckgo.vpn.internal.databinding.ActivityExceptionRulesDebugBinding
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import logcat.logcat

@InjectWith(ActivityScope::class)
class ExceptionRulesDebugActivity : DuckDuckGoActivity(), RuleTrackerView.RuleTrackerListener {

    @Inject
    lateinit var appTrackerBlockingRepository: AppTrackerBlockingStatsRepository

    @Inject
    lateinit var vpnDatabase: VpnDatabase

    @Inject
    lateinit var exclusionRulesRepository: ExclusionRulesRepository

    @Inject
    lateinit var dispatchers: DispatcherProvider

    private val binding: ActivityExceptionRulesDebugBinding by viewBinding()

    private val refreshTickerJob = ConflatedJob()
    private var refreshTickerChannel = MutableStateFlow(System.currentTimeMillis())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.appRule.isVisible = false
        binding.progress.isVisible = true

        vpnDatabase.vpnAppTrackerBlockingDao()
            .getTrackerExceptionRulesFlow()
            .combine(refreshTickerChannel.asStateFlow()) { trackers, _ -> trackers }
            .map { rules ->
                rules to getAppTrackers()
            }
            .onStart { startRefreshTicker() }
            .flowOn(dispatchers.io())
            .onEach {
                val (rules, appTrackers) = it

                // clean up the screen
                binding.appRule.removeAllViews()

                // re-build the screen
                appTrackers.forEach { installAppTracker ->
                    val appView = RuleAppView(this).apply {
                        appIcon =
                            packageManager.safeGetApplicationIcon(installAppTracker.packageName)
                        appName = installAppTracker.name.orEmpty()
                    }
                    binding.appRule.addView(appView)
                    installAppTracker.blockedDomains.forEach { domain ->
                        val domainView = RuleTrackerView(this).apply {
                            this.domain = domain
                            this.isChecked =
                                rules.containsRule(installAppTracker.packageName, domain)
                            this.ruleTrackerListener = this@ExceptionRulesDebugActivity
                            tag = "${installAppTracker.packageName}_$domain"
                        }
                        appView.addTrackerView(domainView)
                    }
                }
                binding.appRule.isVisible = true
                binding.progress.isVisible = false
            }
            .flowOn(dispatchers.main())
            .launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshTickerJob.cancel()
    }

    private fun getAppTrackers(): List<InstalledAppTrackers> {
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .map { InstalledApp(it.packageName, packageManager.getApplicationLabel(it).toString()) }
            .map {
                val blockedTrackers = appTrackerBlockingRepository.getTrackersForApp(it.packageName)
                    .map { tracker -> tracker.domain }
                    .toSortedSet() // dedup
                InstalledAppTrackers(it.packageName, it.name, blockedTrackers)
            }
            .filter { it.blockedDomains.isNotEmpty() }
            .sortedBy { it.name }
            .toList()
    }

    private fun List<AppTrackerExceptionRule>.containsRule(
        packageName: String,
        domain: String,
    ): Boolean {
        forEach { exclusionRule ->
            if (exclusionRule.rule == domain && exclusionRule.packageNames.contains(packageName)) return true
        }

        return false
    }

    private fun PackageManager.safeGetApplicationIcon(packageName: String): Drawable? {
        return runCatching {
            getApplicationIcon(packageName)
        }.getOrNull()
    }

    private fun startRefreshTicker() {
        refreshTickerJob += lifecycleScope.launch {
            while (isActive) {
                refreshTickerChannel.emit(System.currentTimeMillis())
                delay(TimeUnit.SECONDS.toMillis(5))
            }
        }
    }

    override fun onTrackerClicked(
        view: View,
        enabled: Boolean,
    ) {
        lifecycleScope.launch(dispatchers.io()) {
            val tag = (view.tag as String?).orEmpty()
            val (appPackageName, domain) = tag.split("_")
            logcat { "$appPackageName / $domain enabled: $enabled" }
            if (enabled) {
                sendBroadcast(ExceptionRulesDebugReceiver.ruleIntent(appPackageName, domain))
            } else {
                exclusionRulesRepository.removeRule(appPackageName, domain)
            }
        }
    }

    companion object {
        fun intent(context: Context): Intent {
            return Intent(context, ExceptionRulesDebugActivity::class.java)
        }
    }
}

private data class InstalledApp(
    val packageName: String,
    val name: String? = null,
)

private data class InstalledAppTrackers(
    val packageName: String,
    val name: String? = null,
    val blockedDomains: Set<String>,
)
