package com.duckduckgo.autofill.impl.deviceauth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.duckduckgo.autofill.impl.deviceauth.DeviceAuthenticator.AuthConfiguration

abstract class FakeAuthenticator : DeviceAuthenticator {

    sealed interface Result {
        object Success : Result
        object Cancelled : Result
        object Failure : Result
    }

    var authenticateCalled: Boolean = false
    abstract val authResult: Result

    private fun authenticationCalled(onResult: (DeviceAuthenticator.AuthResult) -> Unit) {
        authenticateCalled = true
        when (authResult) {
            is Result.Success -> onResult(DeviceAuthenticator.AuthResult.Success)
            is Result.Cancelled -> onResult(DeviceAuthenticator.AuthResult.UserCancelled)
            is Result.Failure -> onResult(DeviceAuthenticator.AuthResult.Error("Authentication failed"))
        }
    }

    override fun hasValidDeviceAuthentication(): Boolean = true

    override fun authenticate(
        fragment: Fragment,
        config: AuthConfiguration,
        onResult: (DeviceAuthenticator.AuthResult) -> Unit,
    ) {
        authenticationCalled(onResult)
    }

    override fun authenticate(
        fragmentActivity: FragmentActivity,
        config: AuthConfiguration,
        onResult: (DeviceAuthenticator.AuthResult) -> Unit,
    ) {
        authenticationCalled(onResult)
    }

    class AuthorizeEverything(override val authResult: Result = Result.Success) :
        FakeAuthenticator()

    class FailEverything(override val authResult: Result = Result.Failure) : FakeAuthenticator()
    class CancelEverything(override val authResult: Result = Result.Cancelled) : FakeAuthenticator()
}
