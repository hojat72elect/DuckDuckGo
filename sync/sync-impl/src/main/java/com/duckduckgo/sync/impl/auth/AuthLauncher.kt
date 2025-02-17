

package com.duckduckgo.sync.impl.auth

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.sync.impl.auth.DeviceAuthenticator.AuthResult
import com.duckduckgo.sync.impl.auth.DeviceAuthenticator.AuthResult.Error
import com.duckduckgo.sync.impl.auth.DeviceAuthenticator.AuthResult.Success
import com.duckduckgo.sync.impl.auth.DeviceAuthenticator.AuthResult.UserCancelled
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import timber.log.Timber

interface AuthLauncher {

    fun launch(
        @StringRes featureTitleText: Int,
        @StringRes featureAuthText: Int,
        fragmentActivity: FragmentActivity,
        onResult: (AuthResult) -> Unit,
    )
}

@ContributesBinding(AppScope::class)
class RealAuthLauncher @Inject constructor(
    private val context: Context,
    private val appBuildConfig: AppBuildConfig,
    private val deviceAuthorizationGracePeriod: DeviceAuthorizationGracePeriod,
) : AuthLauncher {

    override fun launch(
        @StringRes featureTitleText: Int,
        @StringRes featureAuthText: Int,
        fragmentActivity: FragmentActivity,
        onResult: (AuthResult) -> Unit,
    ) {
        val prompt = BiometricPrompt(
            fragmentActivity,
            ContextCompat.getMainExecutor(context),
            getCallBack(onResult),
        )

        prompt.authenticate(getPromptInfo(titleText = featureTitleText, featureAuthText = featureAuthText))
    }

    private fun getCallBack(
        onResult: (AuthResult) -> Unit,
    ): BiometricPrompt.AuthenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence,
        ) {
            super.onAuthenticationError(errorCode, errString)
            Timber.d("onAuthenticationError: (%d) %s", errorCode, errString)

            if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                onResult(UserCancelled)
            } else {
                onResult(Error(String.format("(%d) %s", errorCode, errString)))
            }
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Timber.d("onAuthenticationSucceeded ${result.authenticationType}")
            deviceAuthorizationGracePeriod.recordSuccessfulAuthorization()
            onResult(Success)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Timber.v("onAuthenticationFailed")
        }
    }

    private fun getPromptInfo(titleText: Int, featureAuthText: Int): BiometricPrompt.PromptInfo {
        val biometricPromptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
        biometricPromptInfoBuilder.setTitle(context.getString(titleText))

        // https://developer.android.com/reference/kotlin/androidx/biometric/BiometricPrompt.PromptInfo.Builder#setallowedauthenticators
        // BIOMETRIC_STRONG | DEVICE_CREDENTIAL is unsupported on API 28-29. Setting an unsupported value on an affected Android version will result in an error when calling build().
        return if (appBuildConfig.sdkInt != Build.VERSION_CODES.Q && appBuildConfig.sdkInt != Build.VERSION_CODES.P) {
            biometricPromptInfoBuilder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
        } else {
            biometricPromptInfoBuilder.setDeviceCredentialAllowed(true)
        }.run {
            this.setSubtitle(context.getString(featureAuthText)).build()
        }
    }
}
