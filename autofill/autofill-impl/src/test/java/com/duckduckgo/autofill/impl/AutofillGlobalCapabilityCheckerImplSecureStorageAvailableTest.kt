

package com.duckduckgo.autofill.impl

import com.duckduckgo.autofill.api.Autofill
import com.duckduckgo.autofill.api.AutofillFeature
import com.duckduckgo.autofill.api.InternalTestUserChecker
import com.duckduckgo.autofill.impl.deviceauth.DeviceAuthenticator
import com.duckduckgo.autofill.impl.store.InternalAutofillStore
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(Parameterized::class)
class AutofillGlobalCapabilityCheckerImplSecureStorageAvailableTest(
    private val testCase: TestCase,
) {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val autofillFeature: AutofillFeature = mock()
    private val internalTestUserChecker: InternalTestUserChecker = mock()
    private val autofillStore: InternalAutofillStore = mock()
    private val deviceAuthenticator: DeviceAuthenticator = mock()
    private val autofill: Autofill = mock()

    private val testee = AutofillGlobalCapabilityCheckerImpl(
        autofillFeature = autofillFeature,
        internalTestUserChecker = internalTestUserChecker,
        autofillStore = autofillStore,
        deviceAuthenticator = deviceAuthenticator,
        autofill = autofill,
        dispatcherProvider = coroutineTestRule.testDispatcherProvider,
    )

    @Test
    fun runParameterizedTests() = runTest {
        configureDeviceAuthValid(testCase.scenario.isDeviceAuthValid)
        configureDeviceCapableOfAutofill(testCase.scenario.isDeviceCapable)

        assertEquals("${testCase.scenario}", testCase.expectFeatureEnabled, testee.isSecureAutofillAvailable())
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "Test case: {index} - {0}")
        fun testData(): List<TestCase> {
            return listOf(
                TestCase(
                    expectFeatureEnabled = false,
                    scenario = Scenario(
                        isDeviceAuthValid = false,
                        isDeviceCapable = false,
                    ),
                ),
                TestCase(
                    expectFeatureEnabled = false,
                    scenario = Scenario(
                        isDeviceAuthValid = false,
                        isDeviceCapable = true,
                    ),
                ),
                TestCase(
                    expectFeatureEnabled = false,
                    scenario = Scenario(
                        isDeviceAuthValid = true,
                        isDeviceCapable = false,
                    ),
                ),
                TestCase(
                    expectFeatureEnabled = true,
                    scenario = Scenario(
                        isDeviceAuthValid = true,
                        isDeviceCapable = true,
                    ),
                ),
            )
        }
    }

    private fun configureDeviceAuthValid(isValid: Boolean) {
        whenever(deviceAuthenticator.isAuthenticationRequiredForAutofill()).thenReturn(true)
        whenever(deviceAuthenticator.hasValidDeviceAuthentication()).thenReturn(isValid)
    }

    private fun configureDeviceCapableOfAutofill(isCapable: Boolean) {
        whenever(autofillStore.autofillAvailable).thenReturn(isCapable)
    }

    data class TestCase(
        val expectFeatureEnabled: Boolean,
        val scenario: Scenario,
    )

    data class Scenario(
        val isDeviceAuthValid: Boolean,
        val isDeviceCapable: Boolean,
    )
}
