

package com.duckduckgo.app.onboarding.ui

import com.duckduckgo.app.browser.defaultbrowsing.DefaultBrowserDetector
import com.duckduckgo.app.global.DefaultRoleBrowserDialog
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class OnboardingPageManagerTest {

    private lateinit var testee: OnboardingPageManager
    private val onboardingPageBuilder: OnboardingPageBuilder = mock()
    private val mockDefaultBrowserDetector: DefaultBrowserDetector = mock()
    private val defaultRoleBrowserDialog: DefaultRoleBrowserDialog = mock()

    @Before
    fun setup() {
        testee = OnboardingPageManagerWithTrackerBlocking(
            defaultRoleBrowserDialog,
            onboardingPageBuilder,
            mockDefaultBrowserDetector,
        )
    }

    @Test
    fun whenDDGIsNotDefaultBrowserThenExpectedOnboardingPagesAreTwo() {
        configureDeviceSupportsDefaultBrowser()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        whenever(defaultRoleBrowserDialog.shouldShowDialog()).thenReturn(false)

        testee.buildPageBlueprints()

        assertEquals(2, testee.pageCount())
    }

    @Test
    fun whenDDGIsNotDefaultBrowserAndShouldShowBrowserDialogThenExpectedOnboardingPagesAre1() {
        configureDeviceSupportsDefaultBrowser()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(false)
        whenever(defaultRoleBrowserDialog.shouldShowDialog()).thenReturn(true)

        testee.buildPageBlueprints()

        assertEquals(1, testee.pageCount())
    }

    @Test
    fun whenDDGAsDefaultBrowserThenSinglePageOnBoarding() {
        configureDeviceSupportsDefaultBrowser()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(true)
        whenever(defaultRoleBrowserDialog.shouldShowDialog()).thenReturn(false)

        testee.buildPageBlueprints()

        assertEquals(1, testee.pageCount())
    }

    @Test
    fun whenDDGAsDefaultBrowserAndShouldShowBrowserDialogThenSinglePageOnBoarding() {
        configureDeviceSupportsDefaultBrowser()
        whenever(mockDefaultBrowserDetector.isDefaultBrowser()).thenReturn(true)
        whenever(defaultRoleBrowserDialog.shouldShowDialog()).thenReturn(true)

        testee.buildPageBlueprints()

        assertEquals(1, testee.pageCount())
    }

    @Test
    fun whenDeviceDoesNotSupportDefaultBrowserThenSinglePageOnBoarding() {
        configureDeviceDoesNotSupportDefaultBrowser()
        whenever(defaultRoleBrowserDialog.shouldShowDialog()).thenReturn(false)

        testee.buildPageBlueprints()

        assertEquals(1, testee.pageCount())
    }

    @Test
    fun whenDeviceDoesNotSupportDefaultBrowserAndShouldShowBrowserDialogThenSinglePageOnBoarding() {
        configureDeviceDoesNotSupportDefaultBrowser()
        whenever(defaultRoleBrowserDialog.shouldShowDialog()).thenReturn(true)

        testee.buildPageBlueprints()

        assertEquals(1, testee.pageCount())
    }

    private fun configureDeviceSupportsDefaultBrowser() {
        whenever(mockDefaultBrowserDetector.deviceSupportsDefaultBrowserConfiguration()).thenReturn(true)
    }

    private fun configureDeviceDoesNotSupportDefaultBrowser() {
        whenever(mockDefaultBrowserDetector.deviceSupportsDefaultBrowserConfiguration()).thenReturn(false)
    }
}
