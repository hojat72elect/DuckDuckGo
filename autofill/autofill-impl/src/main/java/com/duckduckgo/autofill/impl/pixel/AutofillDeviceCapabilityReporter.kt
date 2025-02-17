

package com.duckduckgo.autofill.impl.pixel

import androidx.annotation.UiThread
import androidx.lifecycle.LifecycleOwner
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.lifecycle.MainProcessLifecycleObserver
import com.duckduckgo.autofill.impl.deviceauth.DeviceAuthenticator
import com.duckduckgo.autofill.impl.securestorage.SecureStorage
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = MainProcessLifecycleObserver::class,
)
class AutofillDeviceCapabilityReporter @Inject constructor(
    private val pixel: AutofillPixelSender,
    private val secureStorage: SecureStorage,
    private val dispatchers: DispatcherProvider,
    private val deviceAuthenticator: DeviceAuthenticator,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : MainProcessLifecycleObserver {

    @UiThread
    override fun onCreate(owner: LifecycleOwner) {
        Timber.v("Autofill device capability reporter created")

        appCoroutineScope.launch(dispatchers.io()) {
            if (pixel.hasDeterminedCapabilities()) {
                Timber.v("Already determined device autofill capabilities previously")
                return@launch
            }

            try {
                val secureStorageAvailable = secureStorage.canAccessSecureStorage()
                val deviceAuthAvailable = deviceAuthenticator.hasValidDeviceAuthentication()

                Timber.d(
                    "Autofill device capabilities:" +
                        "\nSecure storage available: $secureStorageAvailable" +
                        "\nDevice auth available: $deviceAuthAvailable",
                )

                pixel.sendCapabilitiesPixel(secureStorageAvailable, deviceAuthAvailable)
            } catch (e: Error) {
                Timber.w(e, "Failed to determine device autofill capabilities")
                pixel.sendCapabilitiesUndeterminablePixel()
            }
        }
    }
}
