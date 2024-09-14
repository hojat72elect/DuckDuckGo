

package com.duckduckgo.privacy.config.store.features.amplinks

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.privacy.config.store.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealAmpLinksRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    lateinit var testee: RealAmpLinksRepository

    private val mockDatabase: PrivacyConfigDatabase = mock()
    private val mockAmpLinksDao: AmpLinksDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.ampLinksDao()).thenReturn(mockAmpLinksDao)
        testee = RealAmpLinksRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            true,
        )
    }

    @Test
    fun whenRepositoryIsCreatedThenValuesLoadedIntoMemory() {
        givenAmpLinksDaoContainsEntities()

        testee = RealAmpLinksRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            true,
        )

        assertEquals(ampLinkExceptionEntity.toFeatureException(), testee.exceptions.first())
        assertEquals(ampLinkFormatEntity.format, testee.ampLinkFormats.first().toString())
        assertEquals(ampKeywordEntity.keyword, testee.ampKeywords.first())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() = runTest {
        testee = RealAmpLinksRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            true,
        )

        testee.updateAll(listOf(), listOf(), listOf())

        verify(mockAmpLinksDao).updateAll(anyList(), anyList(), anyList())
    }

    @Test
    fun whenUpdateAllThenPreviousValuesAreCleared() = runTest {
        givenAmpLinksDaoContainsEntities()

        testee = RealAmpLinksRepository(
            mockDatabase,
            TestScope(),
            coroutineRule.testDispatcherProvider,
            true,
        )
        assertEquals(1, testee.exceptions.size)
        assertEquals(1, testee.ampLinkFormats.size)
        assertEquals(1, testee.ampKeywords.size)

        reset(mockAmpLinksDao)

        testee.updateAll(listOf(), listOf(), listOf())

        assertEquals(0, testee.exceptions.size)
        assertEquals(0, testee.ampLinkFormats.size)
        assertEquals(0, testee.ampKeywords.size)
    }

    private fun givenAmpLinksDaoContainsEntities() {
        whenever(mockAmpLinksDao.getAllExceptions()).thenReturn(listOf(ampLinkExceptionEntity))
        whenever(mockAmpLinksDao.getAllAmpLinkFormats()).thenReturn(listOf(ampLinkFormatEntity))
        whenever(mockAmpLinksDao.getAllAmpKeywords()).thenReturn(listOf(ampKeywordEntity))
    }

    companion object {
        val ampLinkExceptionEntity = AmpLinkExceptionEntity(
            domain = "https://www.example.com",
            reason = "reason",
        )

        val ampLinkFormatEntity = AmpLinkFormatEntity(
            format = "https?:\\/\\/(?:w{3}\\.)?google\\.\\w{2,}\\/amp\\/s\\/(\\S+)",
        )

        val ampKeywordEntity = AmpKeywordEntity(
            keyword = "keyword",
        )
    }
}
