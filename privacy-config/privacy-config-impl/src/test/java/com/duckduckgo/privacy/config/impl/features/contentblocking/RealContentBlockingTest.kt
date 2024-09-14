

package com.duckduckgo.privacy.config.impl.features.contentblocking

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import com.duckduckgo.privacy.config.store.features.contentblocking.ContentBlockingRepository
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RealContentBlockingTest {

    lateinit var testee: RealContentBlocking

    private val mockContentBlockingRepository: ContentBlockingRepository = mock()
    private val mockUnprotectedTemporary: UnprotectedTemporary = mock()
    private val mockFeatureToggle: FeatureToggle = mock()

    @Before
    fun before() {
        givenFeatureIsEnabled()

        testee =
            RealContentBlocking(
                mockContentBlockingRepository,
                mockFeatureToggle,
                mockUnprotectedTemporary,
            )
    }

    @Test
    fun whenIsAnExceptionAndDomainIsListedInTheExceptionsListThenReturnTrue() {
        givenThereAreExceptions()

        assertTrue(testee.isAnException("http://www.example.com"))
    }

    @Test
    fun whenIsAnExceptionWithSubdomainAndDomainIsListedInTheExceptionsListThenReturnTrue() {
        givenThereAreExceptions()

        assertTrue(testee.isAnException("http://test.example.com"))
    }

    @Test
    fun whenIsAnExceptionAndDomainIsNotListedInTheExceptionsListThenReturnFalse() {
        whenever(mockContentBlockingRepository.exceptions).thenReturn(CopyOnWriteArrayList())

        assertFalse(testee.isAnException("http://test.example.com"))
    }

    @Test
    fun whenIsAnExceptionAndDomainIsInTheUnprotectedTemporaryListThenReturnTrue() {
        val url = "http://test.example.com"
        whenever(mockUnprotectedTemporary.isAnException(url)).thenReturn(true)
        whenever(mockContentBlockingRepository.exceptions).thenReturn(CopyOnWriteArrayList())

        assertTrue(testee.isAnException(url))
    }

    @Test
    fun whenIsAnExceptionAndFeatureIsDisabledThenReturnFalse() {
        givenThereAreExceptions()
        givenFeatureIsDisabled()

        assertFalse(testee.isAnException("http://test.example.com"))
    }

    private fun givenThereAreExceptions() {
        val exceptions =
            CopyOnWriteArrayList<FeatureException>().apply {
                add(FeatureException("example.com", "my reason here"))
            }
        whenever(mockContentBlockingRepository.exceptions).thenReturn(exceptions)
    }

    private fun givenFeatureIsEnabled() {
        whenever(
            mockFeatureToggle.isFeatureEnabled(
                PrivacyFeatureName.ContentBlockingFeatureName.value,
                true,
            ),
        )
            .thenReturn(true)
    }

    private fun givenFeatureIsDisabled() {
        whenever(
            mockFeatureToggle.isFeatureEnabled(
                PrivacyFeatureName.ContentBlockingFeatureName.value,
                true,
            ),
        )
            .thenReturn(false)
    }
}
