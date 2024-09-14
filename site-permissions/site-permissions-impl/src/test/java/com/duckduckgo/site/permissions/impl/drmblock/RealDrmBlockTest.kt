

package com.duckduckgo.site.permissions.impl.drmblock

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.privacy.db.UserAllowListRepository
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.concurrent.CopyOnWriteArrayList
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealDrmBlockTest {

    private val mockToggle: Toggle = mock()
    private val mockDrmBlockFeature: DrmBlockFeature = mock()
    private val mockDrmBlockRepository: DrmBlockRepository = mock()
    private val mockUserAllowListRepository: UserAllowListRepository = mock()
    private val mockUnprotectedTemporary: UnprotectedTemporary = mock()

    val testee: RealDrmBlock = RealDrmBlock(mockDrmBlockFeature, mockDrmBlockRepository, mockUserAllowListRepository, mockUnprotectedTemporary)

    val url = "https://open.spotify.com"

    @Before
    fun before() {
        whenever(mockDrmBlockFeature.self()).thenReturn(mockToggle)
    }

    @Test
    fun whenFeatureDisabledThenFalseIsReturned() {
        assertFalse(testee.isDrmBlockedForUrl(url))
    }

    @Test
    fun whenFeatureEnabledUrlNotBlockedThenFalseIsReturned() {
        givenFeatureIsEnabled()
        givenUrlIsNotInExceptionList()

        assertFalse(testee.isDrmBlockedForUrl(url))
    }

    @Test
    fun whenFeatureEnabledUrlBlockedUserAllowedThenFalseIsReturned() {
        givenFeatureIsEnabled()
        givenUrlIsInExceptionList()
        givenUriIsInUserAllowList()

        assertFalse(testee.isDrmBlockedForUrl(url))
    }

    @Test
    fun whenFeatureEnabledUrlBlockedNotUserAllowedUnprotectedTempThenFalseIsReturned() {
        givenFeatureIsEnabled()
        givenUrlIsInExceptionList()
        givenUrlIsInUnprotectedTemporary()

        assertFalse(testee.isDrmBlockedForUrl(url))
    }

    @Test
    fun whenFeatureEnabledUrlBlockedNotUserAllowedNotUnprotectedTempThenTrueIsReturned() {
        givenFeatureIsEnabled()
        givenUrlIsInExceptionList()

        assertTrue(testee.isDrmBlockedForUrl(url))
    }

    private fun givenFeatureIsEnabled() {
        whenever(mockDrmBlockFeature.self().isEnabled()).thenReturn(true)
    }

    private fun givenUrlIsInExceptionList() {
        val exceptions = CopyOnWriteArrayList<FeatureException>().apply { add(FeatureException("open.spotify.com", null)) }
        whenever(mockDrmBlockRepository.exceptions).thenReturn(exceptions)
    }

    private fun givenUrlIsNotInExceptionList() {
        whenever(mockDrmBlockRepository.exceptions).thenReturn(CopyOnWriteArrayList<FeatureException>())
    }

    private fun givenUriIsInUserAllowList() {
        whenever(mockUserAllowListRepository.isUriInUserAllowList(any())).thenReturn(true)
    }

    private fun givenUrlIsInUnprotectedTemporary() {
        whenever(mockUnprotectedTemporary.isAnException(any())).thenReturn(true)
    }
}
