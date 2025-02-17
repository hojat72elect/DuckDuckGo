

package com.duckduckgo.mobile.android.vpn.feature.removal

import androidx.work.ExistingWorkPolicy.KEEP
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.VpnScope
import com.duckduckgo.mobile.android.vpn.dao.VpnFeatureRemoverState
import com.duckduckgo.mobile.android.vpn.service.VpnServiceCallbacks
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor.VpnStopReason
import com.duckduckgo.mobile.android.vpn.store.VpnDatabase
import com.squareup.anvil.annotations.ContributesMultibinding
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import logcat.logcat

@ContributesMultibinding(
    scope = VpnScope::class,
    boundType = VpnServiceCallbacks::class,
)
class VpnFeatureRemoverStateListener @Inject constructor(
    private val workManager: WorkManager,
    private val vpnDatabase: VpnDatabase,
    private val dispatcherProvider: DispatcherProvider,
) : VpnServiceCallbacks {

    override fun onVpnStarted(coroutineScope: CoroutineScope) {
        coroutineScope.launch(dispatcherProvider.io()) {
            logcat { "FeatureRemoverVpnStateListener, new state ENABLED. Descheduling automatic feature removal" }
            resetState()
        }
    }

    private suspend fun resetState() {
        vpnDatabase.vpnFeatureRemoverDao().getState()?.let {
            logcat { "FeatureRemoverVpnStateListener, feature was removed, setting it back to not removed" }
            vpnDatabase.vpnFeatureRemoverDao().insert(VpnFeatureRemoverState(isFeatureRemoved = false))
        }
        workManager.cancelAllWorkByTag(VpnFeatureRemoverWorker.WORKER_VPN_FEATURE_REMOVER_TAG)
    }

    override fun onVpnStopped(
        coroutineScope: CoroutineScope,
        vpnStopReason: VpnStopReason,
    ) {
        if (vpnStopReason is VpnStopReason.SELF_STOP) {
            coroutineScope.launch(dispatcherProvider.io()) {
                logcat { "FeatureRemoverVpnStateListener, new state DISABLED and it was MANUALLY. Scheduling automatic feature removal" }
                scheduleFeatureRemoval()
            }
        }
    }

    private fun scheduleFeatureRemoval() {
        logcat { "Scheduling the VpnFeatureRemoverWorker worker 7 days from now" }
        val request = OneTimeWorkRequestBuilder<VpnFeatureRemoverWorker>()
            .setInitialDelay(7, TimeUnit.DAYS)
            .addTag(VpnFeatureRemoverWorker.WORKER_VPN_FEATURE_REMOVER_TAG)
            .build()

        workManager.enqueueUniqueWork(
            VpnFeatureRemoverWorker.WORKER_VPN_FEATURE_REMOVER_TAG,
            KEEP,
            request,
        )
    }
}
