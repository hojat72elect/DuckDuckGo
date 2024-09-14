

package com.duckduckgo.app.webtrackingprotection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.webtrackingprotection.WebTrackingProtectionViewModel.Command
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.privacy.config.api.Gpc
import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class WebTrackingProtectionViewModelTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var testee: WebTrackingProtectionViewModel

    @Mock
    private lateinit var mockGpc: Gpc

    @Mock
    private lateinit var mockFeatureToggle: FeatureToggle

    @Mock
    private lateinit var mockPixel: Pixel

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)

        testee = WebTrackingProtectionViewModel(
            mockGpc,
            mockFeatureToggle,
            mockPixel,
        )
    }

    @Test
    fun whenInitialisedThenGpgDisabled() = runTest {
        whenever(mockFeatureToggle.isFeatureEnabled(eq(PrivacyFeatureName.GpcFeatureName.value), any())).thenReturn(false)
        whenever(mockGpc.isEnabled()).thenReturn(true)

        testee.viewState().test {
            assertFalse(awaitItem().globalPrivacyControlEnabled)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenGpcToggleEnabledAndGpcDisabledThenGpgDisabled() = runTest {
        whenever(mockFeatureToggle.isFeatureEnabled(eq(PrivacyFeatureName.GpcFeatureName.value), any())).thenReturn(true)
        whenever(mockGpc.isEnabled()).thenReturn(false)

        testee.viewState().test {
            assertFalse(awaitItem().globalPrivacyControlEnabled)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenGpcToggleEnabledAndGpcEnabledThenGpgEnabled() = runTest {
        whenever(mockFeatureToggle.isFeatureEnabled(eq(PrivacyFeatureName.GpcFeatureName.value), any())).thenReturn(true)
        whenever(mockGpc.isEnabled()).thenReturn(true)

        testee.viewState().test {
            Assert.assertTrue(awaitItem().globalPrivacyControlEnabled)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenOnGlobalPrivacyControlClickedThenCommandIsLaunchGlobalPrivacyControlAndPixelFired() = runTest {
        testee.commands().test {
            testee.onGlobalPrivacyControlClicked()

            assertEquals(Command.LaunchGlobalPrivacyControl, awaitItem())
            verify(mockPixel).fire(AppPixelName.SETTINGS_GPC_PRESSED)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenOnManageAllowListSelectedThenEmitCommandLaunchAllowListAndSendPixel() = runTest {
        testee.commands().test {
            testee.onManageAllowListSelected()

            assertEquals(Command.LaunchAllowList, awaitItem())
            verify(mockPixel).fire(AppPixelName.SETTINGS_MANAGE_ALLOWLIST)

            cancelAndConsumeRemainingEvents()
        }
    }
}
