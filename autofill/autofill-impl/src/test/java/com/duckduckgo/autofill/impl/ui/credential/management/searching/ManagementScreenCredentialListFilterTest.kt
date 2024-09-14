

package com.duckduckgo.autofill.impl.ui.credential.management.searching

import com.duckduckgo.autofill.api.domain.app.LoginCredentials
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock

class ManagementScreenCredentialListFilterTest {

    private val matcher: AutofillCredentialMatcher = mock()
    private val testee = ManagementScreenCredentialListFilter(matcher)

    @Test
    fun whenEmptyListAndEmptyQueryThenEmptyListReturned() = runTest {
        assertTrue(testee.filter(emptyList(), "").isEmpty())
    }

    @Test
    fun whenNonEmptyListAndEmptyQueryThenUnfilteredListReturned() = runTest {
        val originalList = listOf(
            creds(),
            creds(),
        )
        val results = testee.filter(originalList, "")
        assertEquals(2, results.size)
    }

    @Test
    fun whenEmptyListWithAQueryThenEmptyListReturned() = runTest {
        assertTrue(testee.filter(emptyList(), "foo").isEmpty())
    }

    private fun creds(id: Long = 0): LoginCredentials {
        return LoginCredentials(id = id, domain = "example.com", username = "u", password = "p")
    }
}
