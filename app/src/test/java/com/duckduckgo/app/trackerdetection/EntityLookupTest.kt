

package com.duckduckgo.app.trackerdetection

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.trackerdetection.db.TdsDomainEntityDao
import com.duckduckgo.app.trackerdetection.db.TdsEntityDao
import com.duckduckgo.app.trackerdetection.model.TdsDomainEntity
import com.duckduckgo.app.trackerdetection.model.TdsEntity
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class EntityLookupTest {

    private val mockEntityDao: TdsEntityDao = mock()
    private val mockDomainEntityDao: TdsDomainEntityDao = mock()
    private var testee = TdsEntityLookup(mockEntityDao, mockDomainEntityDao)

    @Test
    fun whenUrlContainsOneUnmatchedDomainThenNoValueIsReturned() {
        val url = "a.com"
        whenever(mockDomainEntityDao.get(url)).thenReturn(null)

        val entity = testee.entityForUrl("https://$url")
        verify(mockDomainEntityDao).get("a.com")
        assertNull(entity)
    }

    @Test
    fun whenUrlContainsOneMatchingDomainThenValueIsReturned() {
        val url = "a.com"
        whenever(mockDomainEntityDao.get(url)).thenReturn(anEntityDomain())
        whenever(mockEntityDao.get(anEntityName())).thenReturn(anEntity())
        val entity = testee.entityForUrl("https://$url")
        assertNotNull(entity)
    }

    @Test
    fun whenUrlContainsOneMatchingDomainThenDomainIsSearchedFor() {
        val url = "a.com"
        testee.entityForUrl("https://$url")
        verify(mockDomainEntityDao).get("a.com")
        verify(mockDomainEntityDao, never()).get("com")
    }

    @Test
    fun whenUrlContainsOneUnmatchedSubDomainAndOneMatchingDomainThenValueIsReturned() {
        val url = "a.b.com"
        whenever(mockDomainEntityDao.get(url)).thenReturn(anEntityDomain())
        whenever(mockEntityDao.get(anEntityName())).thenReturn(anEntity())
        val entity = testee.entityForUrl("https://$url")
        assertNotNull(entity)
        verify(mockDomainEntityDao).get("a.b.com")
    }

    @Test
    fun whenUrlContainsOneMultipartTldThenTldIsSearchedForInDb() {
        val url = "a.co.uk"
        testee.entityForUrl("https://$url")
        verify(mockDomainEntityDao).get("a.co.uk")
        verify(mockDomainEntityDao).get("co.uk")
    }

    @Test
    fun whenUrlContainsManyUnmatchedSubdomainsThenAllIntermediateValuesAreSearchedFor() {
        val url = "a.b.c.com"
        whenever(mockDomainEntityDao.get("a.b.c.com")).thenReturn(null)
        whenever(mockDomainEntityDao.get("b.c.com")).thenReturn(null)
        whenever(mockDomainEntityDao.get("c.com")).thenReturn(null)

        val entity = testee.entityForUrl("https://$url")
        assertNull(entity)
        verify(mockDomainEntityDao).get("a.b.c.com")
        verify(mockDomainEntityDao).get("b.c.com")
        verify(mockDomainEntityDao).get("b.c.com")
        verify(mockDomainEntityDao).get("c.com")
    }

    @Test
    fun whenUrlContainsManyMatchingSubdomainsThenSearchingStopsWhenValueFound() {
        val url = "a.b.c.com"
        whenever(mockDomainEntityDao.get("a.b.c.com")).thenReturn(null)
        whenever(mockDomainEntityDao.get("b.c.com")).thenReturn(anEntityDomain())
        whenever(mockDomainEntityDao.get("c.com")).thenReturn(null)
        whenever(mockEntityDao.get(anEntityName())).thenReturn(anEntity())

        val entity = testee.entityForUrl("https://$url")
        assertNotNull(entity)

        verify(mockDomainEntityDao).get("a.b.c.com")
        verify(mockDomainEntityDao).get("b.c.com")
        verify(mockDomainEntityDao, never()).get("c.com")
    }

    private fun anEntityName() = "Entity Name"
    private fun anEntityDomain() = TdsDomainEntity("", anEntityName())
    private fun anEntity() = TdsEntity(anEntityName(), "", 0.0)
}
