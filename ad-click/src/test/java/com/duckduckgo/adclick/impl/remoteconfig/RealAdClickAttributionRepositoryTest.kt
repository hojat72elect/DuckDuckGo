

package com.duckduckgo.adclick.impl.remoteconfig

import com.duckduckgo.adclick.impl.store.AdClickAttributionAllowlistEntity
import com.duckduckgo.adclick.impl.store.AdClickAttributionDetectionEntity
import com.duckduckgo.adclick.impl.store.AdClickAttributionExpirationEntity
import com.duckduckgo.adclick.impl.store.AdClickAttributionLinkFormatEntity
import com.duckduckgo.adclick.impl.store.AdClickDao
import com.duckduckgo.adclick.impl.store.AdClickDatabase
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealAdClickAttributionRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    lateinit var testee: RealAdClickAttributionRepository

    private val mockDatabase: AdClickDatabase = mock()
    private val mockAdClickAttributionDao: AdClickDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.adClickDao()).thenReturn(mockAdClickAttributionDao)
        givenAdClickAttributionDaoContainsInfo()

        testee = RealAdClickAttributionRepository(
            database = mockDatabase,
            coroutineScope = TestScope(),
            dispatcherProvider = coroutineRule.testDispatcherProvider,
            isMainProcess = true,
        )
    }

    @Test
    fun whenRepositoryIsCreatedThenAllListsAreLoadedIntoMemory() {
        assertEquals(linkFormatEntity, testee.linkFormats.first())
        assertEquals(allowlistEntity, testee.allowList.first())
        assertEquals(expirationEntity, testee.expirations.first())
        assertEquals(detectionEntity, testee.detections.first())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() =
        runTest {
            testee.updateAll(
                linkFormats = listOf(),
                allowList = listOf(),
                navigationExpiration = 0,
                totalExpiration = 0,
                heuristicDetection = "",
                domainDetection = "",
            )

            verify(mockAdClickAttributionDao).setAll(
                linkFormats = ArgumentMatchers.anyList(),
                allowList = ArgumentMatchers.anyList(),
                expirations = ArgumentMatchers.anyList(),
                detections = ArgumentMatchers.anyList(),
            )
        }

    @Test
    fun whenUpdateAllThenPreviousListsAreCleared() =
        runTest {
            assertEquals(1, testee.linkFormats.size)
            assertEquals(1, testee.allowList.size)
            assertEquals(1, testee.expirations.size)
            assertEquals(1, testee.detections.size)

            reset(mockAdClickAttributionDao)

            testee.updateAll(
                linkFormats = listOf(),
                allowList = listOf(),
                navigationExpiration = 0,
                totalExpiration = 0,
                heuristicDetection = "",
                domainDetection = "",
            )

            assertEquals(0, testee.linkFormats.size)
            assertEquals(0, testee.allowList.size)
            assertEquals(0, testee.expirations.size)
            assertEquals(0, testee.detections.size)
        }

    private fun givenAdClickAttributionDaoContainsInfo() {
        whenever(mockAdClickAttributionDao.getLinkFormats()).thenReturn(listOf(linkFormatEntity))
        whenever(mockAdClickAttributionDao.getAllowList()).thenReturn(listOf(allowlistEntity))
        whenever(mockAdClickAttributionDao.getExpirations()).thenReturn(listOf(expirationEntity))
        whenever(mockAdClickAttributionDao.getDetections()).thenReturn(listOf(detectionEntity))
    }

    companion object {
        val linkFormatEntity = AdClickAttributionLinkFormatEntity(
            url = "good.first-party.site/y.js",
            adDomainParameterName = "",
        )

        val allowlistEntity = AdClickAttributionAllowlistEntity(
            blocklistEntry = "ad-site.site",
            host = "convert.ad-company.site",
        )

        val expirationEntity = AdClickAttributionExpirationEntity(
            id = 1,
            navigationExpiration = 1800,
            totalExpiration = 604800,
        )

        val detectionEntity = AdClickAttributionDetectionEntity(
            id = 1,
            domainDetection = "enabled",
            heuristicDetection = "enabled",
        )
    }
}
