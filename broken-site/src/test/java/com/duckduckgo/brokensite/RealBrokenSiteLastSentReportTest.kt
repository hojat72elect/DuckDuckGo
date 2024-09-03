package com.duckduckgo.brokensite

import com.duckduckgo.brokensite.api.BrokenSiteLastSentReport
import com.duckduckgo.brokensite.impl.BrokenSiteReportRepository
import com.duckduckgo.brokensite.impl.RealBrokenSiteLastSentReport
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RealBrokenSiteLastSentReportTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val mockBrokenSiteReportRepository: BrokenSiteReportRepository = mock()
    private lateinit var testee: BrokenSiteLastSentReport

    @Before
    fun before() {
        testee = RealBrokenSiteLastSentReport(mockBrokenSiteReportRepository)
    }

    @Test
    fun whenGetLastSentDayCalledWithHostnameThenGetLastSentDayFromRepositoryIsCalled() = runTest {
        val hostname = "www.example.com"

        testee.getLastSentDay(hostname)

        verify(mockBrokenSiteReportRepository).getLastSentDay(hostname)
    }

    @Test
    fun whenSetLastSentDayCalledWithHostnameThenSetLastSentDayFromRepositoryIsCalled() = runTest {
        val hostname = "www.example.com"

        testee.setLastSentDay(hostname)

        verify(mockBrokenSiteReportRepository).setLastSentDay(hostname)
    }
}
