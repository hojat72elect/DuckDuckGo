

package com.duckduckgo.mobile.android.vpn.ui.onboarding

import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import java.time.Instant
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SharedPreferencesVpnStoreTest {

    private val sharedPreferencesProvider = mock<com.duckduckgo.data.store.api.SharedPreferencesProvider>()

    private lateinit var sharedPreferencesVpnStore: SharedPreferencesVpnStore
    private lateinit var preferences: SharedPreferences

    @Before
    fun setup() {
        preferences = InMemorySharedPreferences()
        whenever(
            sharedPreferencesProvider.getSharedPreferences(eq("com.duckduckgo.android.atp.onboarding.store"), eq(true), eq(true)),
        ).thenReturn(preferences)

        sharedPreferencesVpnStore = SharedPreferencesVpnStore(sharedPreferencesProvider)
    }

    @Test
    fun whenOnboardingDidShowThenSetPreferenceValueToTrue() {
        assertFalse(sharedPreferencesVpnStore.didShowOnboarding())

        sharedPreferencesVpnStore.onboardingDidShow()

        assertTrue(sharedPreferencesVpnStore.didShowOnboarding())
    }

    @Test
    fun whenOnboardingDidNotShowThenSetPreferenceValueToFalse() {
        sharedPreferencesVpnStore.onboardingDidShow()

        assertTrue(sharedPreferencesVpnStore.didShowOnboarding())

        sharedPreferencesVpnStore.onboardingDidNotShow()

        assertFalse(sharedPreferencesVpnStore.didShowOnboarding())
    }

    @Test
    fun whenAppTpEnabledCtaDidShowThenSetPreferenceValueToTrue() {
        assertFalse(sharedPreferencesVpnStore.didShowAppTpEnabledCta())

        sharedPreferencesVpnStore.appTpEnabledCtaDidShow()

        assertTrue(sharedPreferencesVpnStore.didShowAppTpEnabledCta())
    }

    @Test
    fun whenIsOnboardingSessionCalledWithoutBeingSetThenReturnFalse() {
        assertTrue(sharedPreferencesVpnStore.getAndSetOnboardingSession())
        assertNotEquals(-1, preferences.getLong("KEY_APP_TP_ONBOARDING_BANNER_EXPIRY_TIMESTAMP", -1))
        assertTrue(sharedPreferencesVpnStore.getAndSetOnboardingSession())
        preferences.edit {
            putLong(
                "KEY_APP_TP_ONBOARDING_BANNER_EXPIRY_TIMESTAMP",
                Instant.now().toEpochMilli().minus(TimeUnit.DAYS.toMillis(1)),
            )
        }
        assertFalse(sharedPreferencesVpnStore.getAndSetOnboardingSession())
    }
}
