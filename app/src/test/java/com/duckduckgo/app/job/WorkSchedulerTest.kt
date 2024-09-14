

package com.duckduckgo.app.job

import androidx.lifecycle.LifecycleOwner
import com.duckduckgo.app.notification.AndroidNotificationScheduler
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class WorkSchedulerTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val notificationScheduler: AndroidNotificationScheduler = mock()
    private val jobCleaner: JobCleaner = mock()

    private lateinit var testee: AndroidWorkScheduler
    private val mockOwner: LifecycleOwner = mock()

    @Before
    fun before() {
        testee = AndroidWorkScheduler(TestScope(), notificationScheduler, jobCleaner, coroutineRule.testDispatcherProvider)
    }

    @Test
    fun schedulesNextNotificationAndCleansDeprecatedJobs() = runTest {
        testee.onResume(mockOwner)

        verify(notificationScheduler).scheduleNextNotification()
        verify(jobCleaner).cleanDeprecatedJobs()
    }
}
