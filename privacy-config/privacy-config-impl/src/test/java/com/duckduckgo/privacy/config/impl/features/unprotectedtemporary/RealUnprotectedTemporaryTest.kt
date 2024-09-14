

package com.duckduckgo.privacy.config.impl.features.unprotectedtemporary

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import com.duckduckgo.privacy.config.store.features.unprotectedtemporary.UnprotectedTemporaryRepository
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class RealUnprotectedTemporaryTest {
    private val mockUnprotectedTemporaryRepository: UnprotectedTemporaryRepository = mock()
    lateinit var testee: RealUnprotectedTemporary

    @Before
    fun before() {
        testee = RealUnprotectedTemporary(mockUnprotectedTemporaryRepository)
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
        val exceptions = CopyOnWriteArrayList<FeatureException>()
        whenever(mockUnprotectedTemporaryRepository.exceptions).thenReturn(exceptions)

        assertFalse(testee.isAnException("http://test.example.com"))
    }

    private fun givenThereAreExceptions() {
        val exceptions = CopyOnWriteArrayList<FeatureException>()
        exceptions.add(FeatureException("example.com", "my reason here"))

        whenever(mockUnprotectedTemporaryRepository.exceptions).thenReturn(exceptions)
    }
}
