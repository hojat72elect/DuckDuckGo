

package com.duckduckgo.mobile.android.vpn.ui.onboarding

import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface VpnStore {
    fun onboardingDidShow()
    fun onboardingDidNotShow()
    fun didShowOnboarding(): Boolean

    fun didShowAppTpEnabledCta(): Boolean
    fun appTpEnabledCtaDidShow()
    fun getAndSetOnboardingSession(): Boolean

    fun dismissNotifyMeInAppTp()
    fun isNotifyMeInAppTpDismissed(): Boolean
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class SharedPreferencesVpnStore @Inject constructor(
    private val sharedPreferencesProvider: com.duckduckgo.data.store.api.SharedPreferencesProvider,
) : VpnStore {

    private val preferences: SharedPreferences by lazy {
        sharedPreferencesProvider.getSharedPreferences(
            DEVICE_SHIELD_ONBOARDING_STORE_PREFS,
            multiprocess = true,
            migrate = true,
        )
    }

    override fun onboardingDidShow() {
        preferences.edit { putBoolean(KEY_DEVICE_SHIELD_ONBOARDING_LAUNCHED, true) }
    }

    override fun onboardingDidNotShow() {
        preferences.edit { putBoolean(KEY_DEVICE_SHIELD_ONBOARDING_LAUNCHED, false) }
    }

    override fun didShowOnboarding(): Boolean {
        return preferences.getBoolean(KEY_DEVICE_SHIELD_ONBOARDING_LAUNCHED, false)
    }

    override fun didShowAppTpEnabledCta(): Boolean {
        return preferences.getBoolean(KEY_APP_TP_ONBOARDING_VPN_ENABLED_CTA_SHOWN, false)
    }

    override fun appTpEnabledCtaDidShow() {
        preferences.edit { putBoolean(KEY_APP_TP_ONBOARDING_VPN_ENABLED_CTA_SHOWN, true) }
    }

    override fun getAndSetOnboardingSession(): Boolean {
        fun onOnboardingSessionSet() {
            val now = Instant.now().toEpochMilli()
            val expiryTimestamp = now.plus(TimeUnit.HOURS.toMillis(WINDOW_INTERVAL_HOURS))
            preferences.edit { putLong(KEY_APP_TP_ONBOARDING_BANNER_EXPIRY_TIMESTAMP, expiryTimestamp) }
        }

        val expiryTimestamp = preferences.getLong(KEY_APP_TP_ONBOARDING_BANNER_EXPIRY_TIMESTAMP, -1)
        if (expiryTimestamp == -1L) {
            onOnboardingSessionSet()
            return true
        }
        return Instant.now().toEpochMilli() < expiryTimestamp
    }

    override fun dismissNotifyMeInAppTp() {
        preferences.edit { putBoolean(KEY_NOTIFY_ME_IN_APP_TP_DISMISSED, true) }
    }

    override fun isNotifyMeInAppTpDismissed(): Boolean {
        return preferences.getBoolean(KEY_NOTIFY_ME_IN_APP_TP_DISMISSED, false)
    }

    companion object {
        private const val DEVICE_SHIELD_ONBOARDING_STORE_PREFS = "com.duckduckgo.android.atp.onboarding.store"

        private const val KEY_DEVICE_SHIELD_ONBOARDING_LAUNCHED = "KEY_DEVICE_SHIELD_ONBOARDING_LAUNCHED"
        private const val KEY_APP_TP_ONBOARDING_VPN_ENABLED_CTA_SHOWN = "KEY_APP_TP_ONBOARDING_VPN_ENABLED_CTA_SHOWN"
        private const val KEY_APP_TP_ONBOARDING_BANNER_EXPIRY_TIMESTAMP = "KEY_APP_TP_ONBOARDING_BANNER_EXPIRY_TIMESTAMP"
        private const val KEY_NOTIFY_ME_IN_APP_TP_DISMISSED = "KEY_NOTIFY_ME_IN_APP_TP_DISMISSED"
        private const val WINDOW_INTERVAL_HOURS = 24L
    }
}
