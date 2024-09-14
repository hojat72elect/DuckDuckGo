

package com.duckduckgo.mobile.android.vpn.health

import androidx.annotation.VisibleForTesting
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.di.scopes.VpnScope
import com.duckduckgo.mobile.android.vpn.boundToVpnProcess
import com.duckduckgo.mobile.android.vpn.service.VpnServiceCallbacks
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor.VpnStopReason
import com.squareup.anvil.annotations.ContributesMultibinding
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import logcat.logcat

@ContributesMultibinding(VpnScope::class)
class AppTPCPUMonitor @Inject constructor(
    private val workManager: WorkManager,
    private val appBuildConfig: AppBuildConfig,
) : VpnServiceCallbacks {

    companion object {
        @VisibleForTesting
        const val APP_TRACKER_CPU_MONITOR_WORKER_TAG = "APP_TRACKER_CPU_MONITOR_WORKER_TAG"
    }

    override fun onVpnStarted(coroutineScope: CoroutineScope) {
        logcat { "AppTpSetting.CPUMonitoring is enabled, starting monitoring" }
        val work = PeriodicWorkRequestBuilder<CPUMonitorWorker>(4, TimeUnit.HOURS)
            .boundToVpnProcess(appBuildConfig.applicationId) // this worker is executed in the :vpn process
            .setInitialDelay(10, TimeUnit.MINUTES) // let the CPU usage settle after VPN restart
            .build()

        workManager.enqueueUniquePeriodicWork(APP_TRACKER_CPU_MONITOR_WORKER_TAG, ExistingPeriodicWorkPolicy.KEEP, work)
    }

    override fun onVpnStopped(
        coroutineScope: CoroutineScope,
        vpnStopReason: VpnStopReason,
    ) {
        logcat { "AppTpSetting.CPUMonitoring - stopping" }
        workManager.cancelUniqueWork(APP_TRACKER_CPU_MONITOR_WORKER_TAG)
    }
}
