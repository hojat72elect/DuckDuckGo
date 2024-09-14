

package com.duckduckgo.privacy.config.impl.features.amplinks

import com.duckduckgo.app.privacy.db.UserAllowListRepository
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import com.duckduckgo.privacy.config.store.features.amplinks.AmpLinksRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RealAmpLinksTest {

    private lateinit var testee: RealAmpLinks
    private val mockAmpLinksRepository: AmpLinksRepository = mock()
    private val mockFeatureToggle: FeatureToggle = mock()
    private val mockUnprotectedTemporary: UnprotectedTemporary = mock()
    private val mockUserAllowListRepository: UserAllowListRepository = mock()

    @Before
    fun setup() {
        testee = RealAmpLinks(mockAmpLinksRepository, mockFeatureToggle, mockUnprotectedTemporary, mockUserAllowListRepository)
    }

    @Test
    fun whenIsExceptionCalledAndDomainIsInUserAllowListThenReturnTrue() {
        whenever(mockUserAllowListRepository.isUrlInUserAllowList(anyString())).thenReturn(true)
        assertTrue(testee.isAnException("test.com"))
    }

    @Test
    fun whenProcessDestinationUrlAndExtractedUrlIsNullThenReturnInitialUrl() {
        val destinationUrl = testee.processDestinationUrl("https://example.com", null)
        assertEquals("https://example.com", destinationUrl)
    }

    @Test
    fun whenProcessDestinationUrlAndExtractedUrlIsAnExceptionThenReturnInitialUrl() {
        whenever(mockUserAllowListRepository.isUrlInUserAllowList(anyString())).thenReturn(true)
        val destinationUrl = testee.processDestinationUrl("https://example.com", "https://foo.com")
        assertEquals("https://example.com", destinationUrl)
    }

    @Test
    fun whenProcessDestinationUrlAndExtractedUrlDoesNotStartWithHttpOrHttpsThenReturnInitialUrl() {
        val destinationUrl = testee.processDestinationUrl("https://example.com", "foo.com")
        assertEquals("https://example.com", destinationUrl)
    }

    @Test
    fun whenProcessDestinationUrlAndExtractedUrlStartsWithHttpThenReturnExtractedUrl() {
        val destinationUrl = testee.processDestinationUrl("https://example.com", "http://foo.com")
        assertEquals("http://foo.com", destinationUrl)
    }

    @Test
    fun whenProcessDestinationUrlAndExtractedUrlStartsWithHttpsThenReturnExtractedUrl() {
        val destinationUrl = testee.processDestinationUrl("https://example.com", "https://foo.com")
        assertEquals("https://foo.com", destinationUrl)
    }
}
