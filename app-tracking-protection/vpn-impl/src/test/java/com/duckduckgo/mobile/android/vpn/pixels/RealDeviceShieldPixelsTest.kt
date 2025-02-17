

package com.duckduckgo.mobile.android.vpn.pixels

import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import java.util.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class RealDeviceShieldPixelsTest {

    private val pixel = mock<Pixel>()
    private val sharedPreferencesProvider = mock<com.duckduckgo.data.store.api.SharedPreferencesProvider>()

    lateinit var deviceShieldPixels: DeviceShieldPixels

    @Before
    fun setup() {
        val prefs = InMemorySharedPreferences()
        whenever(
            sharedPreferencesProvider.getSharedPreferences(eq("com.duckduckgo.mobile.android.device.shield.pixels"), eq(true), eq(true)),
        ).thenReturn(prefs)

        deviceShieldPixels = RealDeviceShieldPixels(pixel, sharedPreferencesProvider)
    }

    @Test
    fun whenDeviceShieldEnabledOnSearchThenFireDailyPixel() {
        deviceShieldPixels.deviceShieldEnabledOnSearch()
        deviceShieldPixels.deviceShieldEnabledOnSearch()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_UPON_SEARCH_DAILY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDeviceShieldDisabledOnSearchThenFireDailyPixel() {
        deviceShieldPixels.deviceShieldDisabledOnSearch()
        deviceShieldPixels.deviceShieldDisabledOnSearch()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_DISABLE_UPON_SEARCH_DAILY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenReportEnableThenFireUniqueAndDailyPixel() {
        deviceShieldPixels.reportEnabled()
        deviceShieldPixels.reportEnabled()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_UNIQUE)
        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_DAILY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenReportDisableThenFireDailyPixel() {
        deviceShieldPixels.reportDisabled()
        deviceShieldPixels.reportDisabled()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_DISABLE_DAILY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenEnableFromReminderNotificationThenFireUniqueDailyAndCountPixels() {
        deviceShieldPixels.enableFromReminderNotification()
        deviceShieldPixels.enableFromReminderNotification()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_REMINDER_NOTIFICATION_UNIQUE)
        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_REMINDER_NOTIFICATION_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_REMINDER_NOTIFICATION.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenEnableFromSettingsThenFireUniqueDailyAndCountPixels() {
        deviceShieldPixels.enableFromOnboarding()
        deviceShieldPixels.enableFromOnboarding()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_ONBOARDING_UNIQUE)
        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_ONBOARDING_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_ONBOARDING.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenEnableFromSettingsTileThenFireUniqueDailyAndCountPixels() {
        deviceShieldPixels.enableFromQuickSettingsTile()
        deviceShieldPixels.enableFromQuickSettingsTile()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_SETTINGS_TILE_UNIQUE)
        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_SETTINGS_TILE_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_SETTINGS_TILE.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenEnableFromPrivacyReportThenFireUniqueDailyAndCountPixels() {
        deviceShieldPixels.enableFromSummaryTrackerActivity()
        deviceShieldPixels.enableFromSummaryTrackerActivity()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_SUMMARY_TRACKER_ACTIVITY_UNIQUE)
        verify(pixel).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_SUMMARY_TRACKER_ACTIVITY_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_ENABLE_FROM_SUMMARY_TRACKER_ACTIVITY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDisableFromSettingsTileThenFireDailyAndCountPixels() {
        deviceShieldPixels.disableFromQuickSettingsTile()
        deviceShieldPixels.disableFromQuickSettingsTile()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_DISABLE_FROM_SETTINGS_TILE_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_DISABLE_FROM_SETTINGS_TILE.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidShowDailyNotificationThenFireDailyPixel() {
        deviceShieldPixels.didShowDailyNotification(0)
        deviceShieldPixels.didShowDailyNotification(1)

        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_DAILY_NOTIFICATION.notificationVariant(0))
        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_DAILY_NOTIFICATION.notificationVariant(1))
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidPressDailyNotificationThenFireDailyPixel() {
        deviceShieldPixels.didPressOnDailyNotification(0)
        deviceShieldPixels.didPressOnDailyNotification(1)

        verify(pixel).fire(DeviceShieldPixelNames.DID_PRESS_DAILY_NOTIFICATION.notificationVariant(0))
        verify(pixel).fire(DeviceShieldPixelNames.DID_PRESS_DAILY_NOTIFICATION.notificationVariant(1))
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidShowWeeklyNotificationThenFireDailyPixel() {
        deviceShieldPixels.didShowWeeklyNotification(0)
        deviceShieldPixels.didShowWeeklyNotification(1)

        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_WEEKLY_NOTIFICATION.notificationVariant(0))
        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_WEEKLY_NOTIFICATION.notificationVariant(1))
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidPressWeeklyNotificationThenFireDailyPixel() {
        deviceShieldPixels.didPressOnWeeklyNotification(0)
        deviceShieldPixels.didPressOnWeeklyNotification(1)

        verify(pixel).fire(DeviceShieldPixelNames.DID_PRESS_WEEKLY_NOTIFICATION.notificationVariant(0))
        verify(pixel).fire(DeviceShieldPixelNames.DID_PRESS_WEEKLY_NOTIFICATION.notificationVariant(1))
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidPressOngoingNotificationThenFireDailyAndCountPixels() {
        deviceShieldPixels.didPressOngoingNotification()
        deviceShieldPixels.didPressOngoingNotification()

        verify(pixel).fire(DeviceShieldPixelNames.DID_PRESS_ONGOING_NOTIFICATION_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.DID_PRESS_ONGOING_NOTIFICATION.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidShowReminderNotificationThenFireDailyAndCountPixels() {
        deviceShieldPixels.didShowReminderNotification()
        deviceShieldPixels.didShowReminderNotification()

        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_REMINDER_NOTIFICATION_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.DID_SHOW_REMINDER_NOTIFICATION.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidPressReminderNotificationThenFireDailyAndCountPixels() {
        deviceShieldPixels.didPressReminderNotification()
        deviceShieldPixels.didPressReminderNotification()

        verify(pixel).fire(DeviceShieldPixelNames.DID_PRESS_REMINDER_NOTIFICATION_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.DID_PRESS_REMINDER_NOTIFICATION.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidShowNewTabSummaryThenFireUniqueDailyAndCountPixels() {
        deviceShieldPixels.didShowNewTabSummary()
        deviceShieldPixels.didShowNewTabSummary()

        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_NEW_TAB_SUMMARY_UNIQUE)
        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_NEW_TAB_SUMMARY_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.DID_SHOW_NEW_TAB_SUMMARY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidPressNewTabSummaryThenFireDailyAndCountPixels() {
        deviceShieldPixels.didPressNewTabSummary()
        deviceShieldPixels.didPressNewTabSummary()

        verify(pixel).fire(DeviceShieldPixelNames.DID_PRESS_NEW_TAB_SUMMARY_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.DID_PRESS_NEW_TAB_SUMMARY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidShowSummaryTrackerActivityThenFireUniqueDailyAndCountPixels() {
        deviceShieldPixels.didShowSummaryTrackerActivity()
        deviceShieldPixels.didShowSummaryTrackerActivity()

        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_SUMMARY_TRACKER_ACTIVITY_UNIQUE)
        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_SUMMARY_TRACKER_ACTIVITY_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.DID_SHOW_SUMMARY_TRACKER_ACTIVITY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenDidShowDetailedTrackerActivityThenFireUniqueDailyAndCountPixels() {
        deviceShieldPixels.didShowDetailedTrackerActivity()
        deviceShieldPixels.didShowDetailedTrackerActivity()

        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_DETAILED_TRACKER_ACTIVITY_UNIQUE)
        verify(pixel).fire(DeviceShieldPixelNames.DID_SHOW_DETAILED_TRACKER_ACTIVITY_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.DID_SHOW_DETAILED_TRACKER_ACTIVITY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenStartErrorThenFireDailyAndCountPixels() {
        deviceShieldPixels.startError()
        deviceShieldPixels.startError()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_START_ERROR_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_START_ERROR.pixelName)
        verify(pixel, times(2)).enqueueFire(DeviceShieldPixelNames.VPN_START_ATTEMPT_FAILURE.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenAutomaticRestartThenFireDailyAndCountPixels() {
        deviceShieldPixels.automaticRestart()
        deviceShieldPixels.automaticRestart()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_AUTOMATIC_RESTART_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_AUTOMATIC_RESTART.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenSuddenKillBySystemThenFireDailyAndCountPixels() {
        deviceShieldPixels.suddenKillBySystem()
        deviceShieldPixels.suddenKillBySystem()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_KILLED_BY_SYSTEM_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_KILLED_BY_SYSTEM.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_KILLED.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenSuddenKillByVpnRevokedThenFireDailyAndCountPixels() {
        deviceShieldPixels.suddenKillByVpnRevoked()
        deviceShieldPixels.suddenKillByVpnRevoked()

        verify(pixel).fire(DeviceShieldPixelNames.ATP_KILLED_VPN_REVOKED_DAILY.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_KILLED_VPN_REVOKED.pixelName)
        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_KILLED.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenPrivacyReportArticleDisplayedThenFireCountPixel() {
        deviceShieldPixels.privacyReportArticleDisplayed()
        deviceShieldPixels.privacyReportArticleDisplayed()

        verify(pixel, times(2)).fire(DeviceShieldPixelNames.ATP_DID_SHOW_PRIVACY_REPORT_ARTICLE.pixelName)
        verify(pixel, times(1)).fire(DeviceShieldPixelNames.ATP_DID_SHOW_PRIVACY_REPORT_ARTICLE_DAILY.pixelName)
        verifyNoMoreInteractions(pixel)
    }

    @Test
    fun whenReportUnprotectedAppsBucketCalledThenFirePixels() {
        val bucketSize = 20
        deviceShieldPixels.reportUnprotectedAppsBucket(bucketSize)

        verify(pixel).fire(DeviceShieldPixelNames.ATP_REPORT_UNPROTECTED_APPS_BUCKET.notificationVariant(bucketSize))
        verify(pixel).fire(DeviceShieldPixelNames.ATP_REPORT_UNPROTECTED_APPS_BUCKET_DAILY.notificationVariant(bucketSize))
    }

    private fun DeviceShieldPixelNames.notificationVariant(variant: Int): String {
        return String.format(Locale.US, pixelName, variant)
    }
}
