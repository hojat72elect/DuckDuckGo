

package com.duckduckgo.app.trackerdetection

import android.net.Uri
import com.duckduckgo.app.privacy.db.UserAllowListRepository
import com.duckduckgo.app.trackerdetection.db.TdsCnameEntityDao
import com.duckduckgo.app.trackerdetection.model.TdsCnameEntity
import com.duckduckgo.privacy.config.api.TrackerAllowlist
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CloakedCnameDetectorImplTest {

    private lateinit var testee: CloakedCnameDetector
    private val mockCnameEntityDao: TdsCnameEntityDao = mock()
    private val mockTrackerAllowList: TrackerAllowlist = mock()
    private val mockUri: Uri = mock()
    private val mockUserAllowListRepository: UserAllowListRepository = mock()

    @Before
    fun setup() {
        testee = CloakedCnameDetectorImpl(mockCnameEntityDao, mockTrackerAllowList, mockUserAllowListRepository)
    }

    @Test
    fun whenDetectCnameAndHostIsNullThenReturnNull() {
        whenever(mockUri.host).thenReturn(null)
        assertNull(testee.detectCnameCloakedHost("foo.com", mockUri))
    }

    @Test
    fun whenDetectCnameAndCnameDetectedThenReturnUncloakedHost() {
        whenever(mockUri.host).thenReturn("host.com")
        whenever(mockCnameEntityDao.get(any())).thenReturn(TdsCnameEntity("host.com", "uncloaked-host.com"))
        assertEquals("http://uncloaked-host.com", testee.detectCnameCloakedHost("foo.com", mockUri))
    }

    @Test
    fun whenDetectCnameAndCnameDetectedAndHasSchemeThenReturnUncloakedHostWithScheme() {
        whenever(mockUri.host).thenReturn("host.com")
        whenever(mockUri.scheme).thenReturn("https")
        whenever(mockCnameEntityDao.get(any())).thenReturn(TdsCnameEntity("host.com", "uncloaked-host.com"))
        assertEquals("https://uncloaked-host.com", testee.detectCnameCloakedHost("foo.com", mockUri))
    }

    @Test
    fun whenDetectCnameAndCnameNotDetectedThenReturnNull() {
        whenever(mockUri.host).thenReturn("host.com")
        whenever(mockCnameEntityDao.get(any())).thenReturn(null)
        assertEquals(null, testee.detectCnameCloakedHost("foo.com", mockUri))
    }

    @Test
    fun whenDetectCnameAndCnameDetectedAndHasPathThenReturnUncloakedHostWithPathAppended() {
        whenever(mockUri.host).thenReturn("host.com")
        whenever(mockUri.path).thenReturn("/path")
        whenever(mockCnameEntityDao.get(any())).thenReturn(TdsCnameEntity("host.com", "uncloaked-host.com"))
        assertEquals("http://uncloaked-host.com/path", testee.detectCnameCloakedHost("foo.com", mockUri))
    }

    @Test
    fun whenDetectCnameAndCnameDetectedAndHasSchemeAndPathThenReturnUncloakedHostWithSchemeAndPathAppended() {
        whenever(mockUri.host).thenReturn("host.com")
        whenever(mockUri.path).thenReturn("/path")
        whenever(mockUri.scheme).thenReturn("https")
        whenever(mockCnameEntityDao.get(any())).thenReturn(TdsCnameEntity("host.com", "uncloaked-host.com"))
        assertEquals("https://uncloaked-host.com/path", testee.detectCnameCloakedHost("foo.com", mockUri))
    }

    @Test
    fun whenRequestUrlIsInAllowListThenReturnNull() {
        whenever(mockTrackerAllowList.isAnException(anyString(), anyString())).thenReturn(true)
        assertEquals(null, testee.detectCnameCloakedHost("foo.com", mockUri))
    }

    @Test
    fun whenDetectCnameCloakedHostAndUrlIsInUserAllowListThenReturnNull() {
        whenever(mockUserAllowListRepository.isUriInUserAllowList(any())).thenReturn(true)
        assertNull(testee.detectCnameCloakedHost("foo.com", mockUri))
    }
}
