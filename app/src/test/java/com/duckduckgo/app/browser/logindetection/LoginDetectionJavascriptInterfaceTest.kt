

package com.duckduckgo.app.browser.logindetection

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class LoginDetectionJavascriptInterfaceTest {

    @Test
    fun whenLoginDetectedThenNotifyCallback() {
        val loginDetected = mock<() -> Unit>()
        val loginDetectionInterface = LoginDetectionJavascriptInterface(loginDetected)

        loginDetectionInterface.loginDetected()

        verify(loginDetected).invoke()
    }
}
