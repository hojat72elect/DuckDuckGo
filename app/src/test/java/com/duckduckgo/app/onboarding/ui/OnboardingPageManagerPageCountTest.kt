

package com.duckduckgo.app.onboarding.ui

import com.duckduckgo.app.browser.defaultbrowsing.DefaultBrowserDetector
import com.duckduckgo.app.global.DefaultRoleBrowserDialog
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
class OnboardingPageManagerPageCountTest(private val testCase: TestCase) {

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
    fun ensurePageCountAsExpected() {
        configureDefaultBrowserPageConfig()

        testee.buildPageBlueprints()
        assertEquals(testCase.expectedPageCount, testee.pageCount())
    }

    private fun configureDefaultBrowserPageConfig() {
        if (testCase.defaultBrowserPage) {
            configureDeviceSupportsDefaultBrowser()
        } else {
            configureDeviceDoesNotSupportDefaultBrowser()
        }
    }

    companion object {

        private const val otherVariant = "variant"

        @JvmStatic
        @Parameterized.Parameters(name = "Test case: {index} - {0}")
        fun testData(): Array<TestCase> {
            return arrayOf(
                TestCase(false, 1, otherVariant),
                TestCase(true, 2, otherVariant),
            )
        }
    }

    private fun configureDeviceSupportsDefaultBrowser() {
        whenever(mockDefaultBrowserDetector.deviceSupportsDefaultBrowserConfiguration()).thenReturn(true)
    }

    private fun configureDeviceDoesNotSupportDefaultBrowser() {
        whenever(mockDefaultBrowserDetector.deviceSupportsDefaultBrowserConfiguration()).thenReturn(false)
    }

    data class TestCase(
        val defaultBrowserPage: Boolean,
        val expectedPageCount: Int,
        val variantKey: String,
    )
}
