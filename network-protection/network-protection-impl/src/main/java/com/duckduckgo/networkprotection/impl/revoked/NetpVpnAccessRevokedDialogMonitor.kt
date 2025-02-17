package com.duckduckgo.networkprotection.impl.revoked

import android.app.Activity
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.browser.api.ActivityLifecycleCallbacks
import com.duckduckgo.common.utils.ConflatedJob
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.duckduckgo.networkprotection.impl.subscription.NetpSubscriptionManager
import com.duckduckgo.networkprotection.impl.subscription.isExpired
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logcat.logcat

@ContributesMultibinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class NetpVpnAccessRevokedDialogMonitor @Inject constructor(
    private val netpSubscriptionManager: NetpSubscriptionManager,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val accessRevokedDialog: AccessRevokedDialog,
    private val networkProtectionState: NetworkProtectionState,
) : ActivityLifecycleCallbacks {

    private val conflatedJob = ConflatedJob()

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        conflatedJob += coroutineScope.launch(dispatcherProvider.io()) {
            delay(500) // debounce fast screen state changes, eg. resume -> pause -> resume
            if (netpSubscriptionManager.getVpnStatus()
                    .isExpired() && networkProtectionState.isOnboarded()
            ) {
                // we don't want to show this dialog in eg. fresh installs
                withContext(dispatcherProvider.main()) {
                    accessRevokedDialog.showOnce(activity)
                }
                if (networkProtectionState.isEnabled()) {
                    networkProtectionState.stop()
                }
            } else {
                logcat { "VPN access revoke dialog clear shown state" }
                accessRevokedDialog.clearIsShown()
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        conflatedJob.cancel()
    }
}
