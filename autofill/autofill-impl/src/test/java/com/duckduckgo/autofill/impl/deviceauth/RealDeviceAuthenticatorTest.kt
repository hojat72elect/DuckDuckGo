

package com.duckduckgo.autofill.impl.deviceauth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.autofill.impl.R.string
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealDeviceAuthenticatorTest {

    private lateinit var testee: RealDeviceAuthenticator

    @Mock
    private lateinit var appBuildConfig: AppBuildConfig

    @Mock
    private lateinit var deviceAuthChecker: SupportedDeviceAuthChecker

    @Mock
    private lateinit var authLauncher: AuthLauncher

    @Mock
    private lateinit var autofillAuthorizationGracePeriod: AutofillAuthorizationGracePeriod

    @Mock
    private lateinit var fragment: Fragment

    @Mock
    private lateinit var fragmentActivity: FragmentActivity

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testee = RealDeviceAuthenticator(
            deviceAuthChecker,
            appBuildConfig,
            authLauncher,
            autofillAuthorizationGracePeriod,
        )
        whenever(autofillAuthorizationGracePeriod.isAuthRequired()).thenReturn(true)
    }

    @Test
    fun whenSdkIs28ThenValidAuthenticationShouldCheckLegacy() {
        whenever(appBuildConfig.sdkInt).thenReturn(28)

        testee.hasValidDeviceAuthentication()

        verify(deviceAuthChecker).supportsLegacyAuthentication()
    }

    @Test
    fun whenSdkIs29ThenValidAuthenticationShouldCheckLegacy() {
        whenever(appBuildConfig.sdkInt).thenReturn(29)

        testee.hasValidDeviceAuthentication()

        verify(deviceAuthChecker).supportsLegacyAuthentication()
    }

    @Test
    fun whenSdkIsNot28Or29ThenValidAuthenticationShouldCheckLegacy() {
        whenever(appBuildConfig.sdkInt).thenReturn(30)

        testee.hasValidDeviceAuthentication()

        verify(deviceAuthChecker).supportsStrongAuthentication()
    }

    @Test
    fun whenAuthenticateToAccessCredentialsIsCalledWithFragmentThenLaunchAuthLauncher() {
        testee.authenticate(fragment) {}
        verifyAuthDialogLaunched()
    }

    @Test
    fun whenAuthenticateToAccessCredentialsIsCalledWithActivityThenLaunchAuthLauncher() {
        testee.authenticate(fragment) {}
        verifyAuthDialogLaunched()
    }

    private fun verifyAuthDialogLaunched() {
        verify(authLauncher).launch(
            featureTitleText = eq(string.biometric_prompt_title),
            featureAuthText = eq(string.autofill_auth_text_for_access),
            fragment = eq(fragment),
            onResult = any(),
        )
    }

    @Test
    fun whenAuthGracePeriodActiveThenNoDeviceAuthLaunchedWhenAccessingCredentials() {
        whenever(autofillAuthorizationGracePeriod.isAuthRequired()).thenReturn(false)
        testee.authenticate(fragmentActivity) {}
        verifyAuthNotLaunched()
    }

    private fun verifyAuthNotLaunched() {
        verify(authLauncher, never()).launch(any(), any(), any<Fragment>(), any())
        verify(authLauncher, never()).launch(any(), any(), any<FragmentActivity>(), any())
    }
}
