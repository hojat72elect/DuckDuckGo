package com.duckduckgo.user.agent.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import com.duckduckgo.user.agent.store.UserAgentRepository
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RealUserAgentTest {
    private val mockUserAgentRepository: UserAgentRepository = mock()
    private val mockUnprotectedTemporary: UnprotectedTemporary = mock()
    lateinit var testee: RealUserAgent

    @Before
    fun before() {
        testee = RealUserAgent(mockUserAgentRepository, mockUnprotectedTemporary)
    }

    @Test
    fun whenIsExceptionAndDomainIsListedInTheExceptionsListThenReturnTrue() {
        givenThereAreExceptions()

        assertTrue(testee.isException("http://www.example.com"))
    }

    @Test
    fun whenIsExceptionWithSubdomainAndDomainIsListedInTheExceptionsListThenReturnTrue() {
        givenThereAreExceptions()

        assertTrue(testee.isException("http://test.example.com"))
    }

    @Test
    fun whenIsExceptionAndDomainIsNotListedInTheExceptionsListThenReturnFalse() {
        whenever(mockUserAgentRepository.exceptions).thenReturn(CopyOnWriteArrayList())

        assertFalse(testee.isException("http://test.example.com"))
    }

    @Test
    fun whenIsExceptionAndDomainIsListedInTheUnprotectedTemporaryListThenReturnTrue() {
        val url = "http://example.com"
        whenever(mockUnprotectedTemporary.isAnException(url)).thenReturn(true)
        whenever(mockUserAgentRepository.exceptions).thenReturn(CopyOnWriteArrayList())

        assertTrue(testee.isException(url))
    }

    @Test
    fun whenSiteInLegacySitesListThenUseLegacyUserAgent() {
        val url = "http://duckduckgo.com"

        assertTrue(testee.useLegacyUserAgent(url))
    }

    @Test
    fun whenSubdomainInLegacySitesListThenUseLegacyUserAgent() {
        val url = "http://test.duckduckgo.com"

        assertTrue(testee.useLegacyUserAgent(url))
    }

    @Test
    fun whenSiteNotInLegacySitesListThenDoNotUseLegacyUserAgent() {
        val url = "http://example.com"

        assertFalse(testee.useLegacyUserAgent(url))
    }

    private fun givenThereAreExceptions() {
        val exceptions =
            CopyOnWriteArrayList<FeatureException>().apply {
                add(FeatureException("example.com", "my reason here"))
            }
        whenever(mockUserAgentRepository.exceptions).thenReturn(exceptions)
    }
}
