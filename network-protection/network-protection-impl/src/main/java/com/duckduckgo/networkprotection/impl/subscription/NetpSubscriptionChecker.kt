package com.duckduckgo.networkprotection.impl.subscription

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.duckduckgo.anvil.annotations.ContributesWorker
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.di.scopes.VpnScope
import com.duckduckgo.mobile.android.vpn.service.VpnServiceCallbacks
import com.duckduckgo.mobile.android.vpn.state.VpnStateMonitor.VpnStopReason
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.duckduckgo.subscriptions.api.Subscriptions
import com.squareup.anvil.annotations.ContributesMultibinding
import java.util.concurrent.TimeUnit.MINUTES
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import logcat.logcat

@ContributesMultibinding(VpnScope::class)
class NetpSubscriptionChecker @Inject constructor(
    private val workManager: WorkManager,
    private val networkProtectionState: NetworkProtectionState,
    private val dispatcherProvider: DispatcherProvider,
) : VpnServiceCallbacks {
    override fun onVpnStarted(coroutineScope: CoroutineScope) {
        coroutineScope.launch(dispatcherProvider.io()) {
            runChecker()
        }
    }

    override fun onVpnReconfigured(coroutineScope: CoroutineScope) {
        coroutineScope.launch(dispatcherProvider.io()) {
            runChecker()
        }
    }

    override fun onVpnStopped(
        coroutineScope: CoroutineScope,
        vpnStopReason: VpnStopReason,
    ) {
        logcat { "Sub check: cancelling scheduled checker due to VPN stop" }
        workManager.cancelAllWorkByTag(TAG_WORKER_NETP_SUBS_CHECK)
    }

    private suspend fun runChecker() {
        if (networkProtectionState.isEnabled()) {
            logcat { "Sub check: Scheduling checker" }
            PeriodicWorkRequestBuilder<NetpSubscriptionCheckWorker>(20, MINUTES)
                .addTag(TAG_WORKER_NETP_SUBS_CHECK)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, MINUTES)
                .build().run {
                    workManager.enqueueUniquePeriodicWork(
                        TAG_WORKER_NETP_SUBS_CHECK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        this,
                    )
                }
        }
    }

    companion object {
        internal const val TAG_WORKER_NETP_SUBS_CHECK = "TAG_WORKER_NETP_SUBS_CHECK"
    }
}

@ContributesWorker(AppScope::class)
class NetpSubscriptionCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    @Inject
    lateinit var networkProtectionState: NetworkProtectionState

    @Inject
    lateinit var netpSubscriptionManager: NetpSubscriptionManager

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var subscriptions: Subscriptions

    override suspend fun doWork(): Result {
        logcat { "Sub check: checking entitlement" }
        if (networkProtectionState.isEnabled()) {
            if (netpSubscriptionManager.getVpnStatus().isActive()) {
                logcat { "Sub check: has entitlements" }
            } else {
                logcat { "Sub check: disabling" }
                networkProtectionState.stop()
            }
        } else {
            logcat { "Sub check: cancelling scheduled checker" }
            workManager.cancelAllWorkByTag(NetpSubscriptionChecker.TAG_WORKER_NETP_SUBS_CHECK)
        }

        return Result.success()
    }
}
