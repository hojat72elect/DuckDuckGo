

package com.duckduckgo.autofill.impl.ui.credential.selecting

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.autofill.api.domain.app.LoginCredentials
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(AndroidJUnit4::class)
class CredentialTextExtractorTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val testee = CredentialTextExtractor(context)

    @Test
    fun whenMissingUsernameWithDomainPresentThenDomainUsedInString() {
        val result = testee.usernameOrPlaceholder(missingUsername())
        assertEquals("Password for example.com", result)
    }

    @Test
    fun whenMissingUsernameAndMissingDomainThenPlaceholderUsedString() {
        val result = testee.usernameOrPlaceholder(missingUsernameAndDomain())
        assertEquals("Password for site", result)
    }

    private fun missingUsername(): LoginCredentials {
        return credentials(username = null)
    }

    private fun missingUsernameAndDomain(): LoginCredentials {
        return credentials(username = null, domain = null)
    }

    private fun credentials(username: String? = "username", password: String? = "pw", domain: String? = "example.com"): LoginCredentials {
        return LoginCredentials(username = username, password = password, domain = domain)
    }
}
