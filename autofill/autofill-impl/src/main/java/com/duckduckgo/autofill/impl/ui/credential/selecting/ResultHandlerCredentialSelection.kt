

package com.duckduckgo.autofill.impl.ui.credential.selecting

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.autofill.api.AutofillEventListener
import com.duckduckgo.autofill.api.AutofillFragmentResultsPlugin
import com.duckduckgo.autofill.api.CredentialAutofillPickerDialog
import com.duckduckgo.autofill.api.domain.app.LoginCredentials
import com.duckduckgo.autofill.impl.deviceauth.DeviceAuthenticator
import com.duckduckgo.autofill.impl.engagement.DataAutofilledListener
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames
import com.duckduckgo.autofill.impl.store.InternalAutofillStore
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@ContributesMultibinding(AppScope::class)
class ResultHandlerCredentialSelection @Inject constructor(
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
    private val dispatchers: DispatcherProvider,
    private val pixel: Pixel,
    private val deviceAuthenticator: DeviceAuthenticator,
    private val appBuildConfig: AppBuildConfig,
    private val autofillStore: InternalAutofillStore,
    private val autofilledListeners: PluginPoint<DataAutofilledListener>,
) : AutofillFragmentResultsPlugin {

    override fun processResult(
        result: Bundle,
        context: Context,
        tabId: String,
        fragment: Fragment,
        autofillCallback: AutofillEventListener,
    ) {
        Timber.d("${this::class.java.simpleName}: processing result")

        val originalUrl = result.getString(CredentialAutofillPickerDialog.KEY_URL) ?: return

        if (result.getBoolean(CredentialAutofillPickerDialog.KEY_CANCELLED)) {
            Timber.v("Autofill: User cancelled credential selection")
            autofillCallback.onNoCredentialsChosenForAutofill(originalUrl)
            return
        }

        appCoroutineScope.launch(dispatchers.io()) {
            processAutofillCredentialSelectionResult(
                result = result,
                browserTabFragment = fragment,
                autofillCallback = autofillCallback,
                originalUrl = originalUrl,
            )
        }
    }

    private suspend fun processAutofillCredentialSelectionResult(
        result: Bundle,
        browserTabFragment: Fragment,
        autofillCallback: AutofillEventListener,
        originalUrl: String,
    ) {
        val selectedCredentials: LoginCredentials =
            result.safeGetParcelable(CredentialAutofillPickerDialog.KEY_CREDENTIALS) ?: return

        selectedCredentials.updateLastUsedTimestamp()

        pixel.fire(AutofillPixelNames.AUTOFILL_AUTHENTICATION_TO_AUTOFILL_SHOWN)

        withContext(dispatchers.main()) {
            deviceAuthenticator.authenticate(
                browserTabFragment,
            ) {
                when (it) {
                    DeviceAuthenticator.AuthResult.Success -> {
                        Timber.v("Autofill: user selected credential to use, and successfully authenticated")
                        pixel.fire(AutofillPixelNames.AUTOFILL_AUTHENTICATION_TO_AUTOFILL_AUTH_SUCCESSFUL)
                        notifyAutofilledListeners()
                        autofillCallback.onShareCredentialsForAutofill(originalUrl, selectedCredentials)
                    }

                    DeviceAuthenticator.AuthResult.UserCancelled -> {
                        Timber.d("Autofill: user selected credential to use, but cancelled without authenticating")
                        pixel.fire(AutofillPixelNames.AUTOFILL_AUTHENTICATION_TO_AUTOFILL_AUTH_CANCELLED)
                        autofillCallback.onNoCredentialsChosenForAutofill(originalUrl)
                    }

                    is DeviceAuthenticator.AuthResult.Error -> {
                        Timber.w("Autofill: user selected credential to use, but there was an error when authenticating: ${it.reason}")
                        pixel.fire(AutofillPixelNames.AUTOFILL_AUTHENTICATION_TO_AUTOFILL_AUTH_FAILURE)
                        autofillCallback.onNoCredentialsChosenForAutofill(originalUrl)
                    }
                }
            }
        }
    }

    private fun notifyAutofilledListeners() {
        autofilledListeners.getPlugins().forEach {
            it.onAutofilledSavedPassword()
        }
    }

    private fun LoginCredentials.updateLastUsedTimestamp() {
        appCoroutineScope.launch(dispatchers.io()) {
            val updated = this@updateLastUsedTimestamp.copy(lastUsedMillis = System.currentTimeMillis())
            autofillStore.updateCredentials(updated, refreshLastUpdatedTimestamp = false)
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("NewApi")
    private inline fun <reified T : Parcelable> Bundle.safeGetParcelable(key: String) =
        if (appBuildConfig.sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, T::class.java)
        } else {
            getParcelable(key)
        }

    override fun resultKey(tabId: String): String {
        return CredentialAutofillPickerDialog.resultKey(tabId)
    }
}
