

package com.duckduckgo.app.about

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.duckduckgo.app.about.AboutDuckDuckGoViewModel.*
import com.duckduckgo.app.about.AboutDuckDuckGoViewModel.Companion.MAX_EASTER_EGG_COUNT
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.subscriptions.api.PrivacyProUnifiedFeedback
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class AboutDuckDuckGoViewModelTest {
    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private lateinit var testee: AboutDuckDuckGoViewModel

    @Mock
    private lateinit var mockAppBuildConfig: AppBuildConfig

    @Mock
    private lateinit var mockPixel: Pixel

    @Mock
    private lateinit var privacyProUnifiedFeedback: PrivacyProUnifiedFeedback

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)

        whenever(mockAppBuildConfig.versionName).thenReturn("name")
        whenever(mockAppBuildConfig.versionCode).thenReturn(1)

        testee = AboutDuckDuckGoViewModel(
            mockAppBuildConfig,
            mockPixel,
            privacyProUnifiedFeedback,
        )
    }

    @Test
    fun whenInitialisedThenViewStateEmittedWithDefaultValues() = runTest {
        testee.viewState().test {
            val value = awaitItem()

            assertEquals("name (1)", value.version)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenOnLearnMoreLinkClickedThenCommandLaunchBrowserWithLearnMoreUrlIsSentAndPixelFired() = runTest {
        testee.commands().test {
            testee.onLearnMoreLinkClicked()

            assertEquals(Command.LaunchBrowserWithLearnMoreUrl, awaitItem())
            verify(mockPixel).fire(AppPixelName.SETTINGS_ABOUT_DDG_LEARN_MORE_PRESSED)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenOnPrivacyPolicyClickedThenCommandLaunchWebViewWithPrivacyPolicyUrlIsSentAndPixelFired() = runTest {
        testee.commands().test {
            testee.onPrivacyPolicyClicked()

            assertEquals(Command.LaunchWebViewWithPrivacyPolicyUrl, awaitItem())
            verify(mockPixel).fire(AppPixelName.SETTINGS_ABOUT_DDG_PRIVACY_POLICY_PRESSED)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenVersionClickedAndNetPWaitlistStateIsOtherThanNotUnlockedThenNoCommandIsSentAndPixelNotSent() = runTest {
        testee.commands().test {
            testee.onVersionClicked()
            verify(mockPixel, never()).fire(AppPixelName.SETTINGS_ABOUT_DDG_VERSION_EASTER_EGG_PRESSED)

            expectNoEvents()
        }
    }

    @Test
    fun whenVersionClickedLessThanMaxTimesAndNetPWaitlistStateIsNotUnlockedThenNoCommandIsSentAndPixelNotSent() = runTest {
        testee.commands().test {
            testee.onVersionClicked()
            verify(mockPixel, never()).fire(AppPixelName.SETTINGS_ABOUT_DDG_VERSION_EASTER_EGG_PRESSED)

            expectNoEvents()
        }
    }

    @Test
    fun whenVersionClickedMaxTimesCounterResetAndPixelSent() = runTest {
        testee.commands().test {
            for (i in 1..MAX_EASTER_EGG_COUNT) {
                testee.onVersionClicked()
            }

            assertTrue(testee.hasResetEasterEggCounter())
            verify(mockPixel).fire(AppPixelName.SETTINGS_ABOUT_DDG_VERSION_EASTER_EGG_PRESSED)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenOnProvideFeedbackClickedThenCommandLaunchFeedbackIsSent() = runTest {
        whenever(privacyProUnifiedFeedback.shouldUseUnifiedFeedback(any())).thenReturn(false)
        testee.commands().test {
            testee.onProvideFeedbackClicked()

            assertEquals(Command.LaunchFeedback, awaitItem())
            verify(mockPixel).fire(AppPixelName.SETTINGS_ABOUT_DDG_SHARE_FEEDBACK_PRESSED)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenOnProvideFeedbackClickedAndUnifiedFeedbackEnabledThenCommandLaunchUnifiedFeedbackIsSent() = runTest {
        whenever(privacyProUnifiedFeedback.shouldUseUnifiedFeedback(any())).thenReturn(true)
        testee.commands().test {
            testee.onProvideFeedbackClicked()

            assertEquals(Command.LaunchPproUnifiedFeedback, awaitItem())
            verify(mockPixel).fire(AppPixelName.SETTINGS_ABOUT_DDG_SHARE_FEEDBACK_PRESSED)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenResetNetPEasterEggCounterIsCalledThenEasterEggCounterIsZero() = runTest {
        testee.onVersionClicked()
        assertFalse(testee.hasResetEasterEggCounter())

        testee.resetEasterEggCounter()

        assertTrue(testee.hasResetEasterEggCounter())
    }
}
