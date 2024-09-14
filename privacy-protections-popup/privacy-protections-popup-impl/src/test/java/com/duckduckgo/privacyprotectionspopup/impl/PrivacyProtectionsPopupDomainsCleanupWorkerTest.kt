

package com.duckduckgo.privacyprotectionspopup.impl

import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.privacyprotectionspopup.impl.db.PopupDismissDomainRepository
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.only
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class PrivacyProtectionsPopupDomainsCleanupWorkerTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val popupDismissDomainRepository: PopupDismissDomainRepository = mock()
    private val timeProvider = FakeTimeProvider()

    private val subject = TestListenableWorkerBuilder<PrivacyProtectionsPopupDomainsCleanupWorker>(context = mock())
        .build()
        .also { worker ->
            worker.popupDismissDomainRepository = popupDismissDomainRepository
            worker.timeProvider = timeProvider
        }

    @Test
    fun whenDoWorkThenCleanUpOldEntriesFromPopupDismissDomainRepository() = runTest {
        timeProvider.time = Instant.parse("2023-11-29T10:15:30.000Z")

        val result = subject.doWork()

        verify(popupDismissDomainRepository, only())
            .removeEntriesOlderThan(Instant.parse("2023-10-30T10:15:30.000Z"))

        assertEquals(Result.success(), result)
    }
}
