

package com.duckduckgo.mobile.android.vpn.feature.removal

import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.dao.VpnFeatureRemoverState
import com.duckduckgo.mobile.android.vpn.service.TrackerBlockingVpnService
import com.duckduckgo.mobile.android.vpn.service.VpnReminderNotificationWorker
import com.duckduckgo.mobile.android.vpn.stats.AppTrackerBlockingStatsRepository
import com.duckduckgo.mobile.android.vpn.store.VpnDatabase
import com.duckduckgo.mobile.android.vpn.ui.notification.AndroidDeviceShieldAlertNotificationBuilder
import com.duckduckgo.mobile.android.vpn.ui.notification.DeviceShieldNotificationScheduler.Companion.VPN_DAILY_NOTIFICATION_ID
import com.duckduckgo.mobile.android.vpn.ui.notification.DeviceShieldNotificationScheduler.Companion.VPN_WEEKLY_NOTIFICATION_ID
import com.duckduckgo.mobile.android.vpn.ui.notification.DeviceShieldNotificationScheduler.Companion.WORKER_VPN_DAILY_NOTIFICATION_NAME
import com.duckduckgo.mobile.android.vpn.ui.notification.DeviceShieldNotificationScheduler.Companion.WORKER_VPN_WEEKLY_NOTIFICATION_TAG
import com.duckduckgo.mobile.android.vpn.ui.onboarding.VpnStore
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface VpnFeatureRemover {
    fun manuallyRemoveFeature()
    fun scheduledRemoveFeature()
    suspend fun isFeatureRemoved(): Boolean
}

@ContributesBinding(scope = AppScope::class, boundType = VpnFeatureRemover::class)
class DefaultVpnFeatureRemover @Inject constructor(
    private val vpnStore: VpnStore,
    private val notificationManager: NotificationManagerCompat,
    private val vpnDatabase: VpnDatabase,
    private val appTrackerBlockingRepository: AppTrackerBlockingStatsRepository,
    // we use the Provider to avoid a cycle dependency
    private val workManagerProvider: Provider<WorkManager>,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : VpnFeatureRemover {

    override fun manuallyRemoveFeature() {
        appCoroutineScope.launch(dispatcherProvider.io()) {
            removeVpnFeature()
            disableNotifications()
            disableNotificationReminders()
            removeNotificationChannels()
            deleteAllVpnTrackers()
        }
    }

    private fun disableNotificationReminders() {
        val workManager = workManagerProvider.get()
        workManager.cancelAllWorkByTag(VpnReminderNotificationWorker.WORKER_VPN_REMINDER_UNDESIRED_TAG)
        workManager.cancelAllWorkByTag(VpnReminderNotificationWorker.WORKER_VPN_REMINDER_DAILY_TAG)
        workManager.cancelAllWorkByTag(WORKER_VPN_DAILY_NOTIFICATION_NAME)
        workManager.cancelAllWorkByTag(WORKER_VPN_WEEKLY_NOTIFICATION_TAG)
    }

    override fun scheduledRemoveFeature() {
        manuallyRemoveFeature()
        resetAppTPOnboarding()
    }

    override suspend fun isFeatureRemoved(): Boolean {
        return vpnDatabase.vpnFeatureRemoverDao().getState()?.isFeatureRemoved ?: false
    }

    private fun disableNotifications() {
        notificationManager.cancel(TrackerBlockingVpnService.VPN_REMINDER_NOTIFICATION_ID)
        notificationManager.cancel(VPN_DAILY_NOTIFICATION_ID)
        notificationManager.cancel(VPN_WEEKLY_NOTIFICATION_ID)
    }

    private fun removeNotificationChannels() {
        notificationManager.deleteNotificationChannel(AndroidDeviceShieldAlertNotificationBuilder.VPN_ALERTS_CHANNEL_ID)
    }

    private fun resetAppTPOnboarding() {
        vpnStore.onboardingDidNotShow()
    }

    private fun deleteAllVpnTrackers() {
        appTrackerBlockingRepository.deleteAllTrackers()
    }

    private suspend fun removeVpnFeature() {
        vpnDatabase.vpnFeatureRemoverDao().insert(VpnFeatureRemoverState(isFeatureRemoved = true))
    }
}
