package com.duckduckgo.mobile.android.vpn.ui.newtab

import androidx.lifecycle.LifecycleOwner
import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.State
import com.duckduckgo.mobile.android.vpn.pixels.DeviceShieldPixels
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppTrackingProtectionNewTabSettingsViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var testee: AppTrackingProtectionNewTabSettingsViewModel
    private val setting: NewTabAppTrackingProtectionSectionSetting = mock()
    private val lifecycleOwner: LifecycleOwner = mock()
    private val pixels: DeviceShieldPixels = mock()

    @Before
    fun setup() {
        testee = AppTrackingProtectionNewTabSettingsViewModel(
            coroutinesTestRule.testDispatcherProvider,
            setting,
            pixels,
        )
    }

    @Test
    fun whenViewCreatedAndSettingEnabledThenViewStateUpdated() = runTest {
        whenever(setting.self()).thenReturn(
            object : Toggle {
                override fun isEnabled(): Boolean {
                    return true
                }

                override fun setEnabled(state: State) {
                }

                override fun getRawStoredState(): State {
                    return State()
                }
            },
        )
        testee.onCreate(lifecycleOwner)
        testee.viewState.test {
            expectMostRecentItem().also {
                assertTrue(it.enabled)
            }
        }
    }

    @Test
    fun whenViewCreatedAndSettingDisabledThenViewStateUpdated() = runTest {
        whenever(setting.self()).thenReturn(
            object : Toggle {
                override fun isEnabled(): Boolean {
                    return false
                }

                override fun setEnabled(state: State) {
                }

                override fun getRawStoredState(): State {
                    return State()
                }
            },
        )
        testee.onCreate(lifecycleOwner)
        testee.viewState.test {
            expectMostRecentItem().also {
                assertFalse(it.enabled)
            }
        }
    }

    @Test
    fun whenSettingEnabledThenPixelFired() = runTest {
        whenever(setting.self()).thenReturn(
            object : Toggle {
                override fun isEnabled(): Boolean {
                    return false
                }

                override fun setEnabled(state: State) {
                }

                override fun getRawStoredState(): State {
                    return State()
                }
            },
        )
        testee.onSettingEnabled(true)
        verify(pixels).reportNewTabSectionToggled(true)
    }

    @Test
    fun whenSettingDisabledThenPixelFired() = runTest {
        whenever(setting.self()).thenReturn(
            object : Toggle {
                override fun isEnabled(): Boolean {
                    return false
                }

                override fun setEnabled(state: State) {
                }

                override fun getRawStoredState(): State {
                    return State()
                }
            },
        )
        testee.onSettingEnabled(false)
        verify(pixels).reportNewTabSectionToggled(false)
    }
}
