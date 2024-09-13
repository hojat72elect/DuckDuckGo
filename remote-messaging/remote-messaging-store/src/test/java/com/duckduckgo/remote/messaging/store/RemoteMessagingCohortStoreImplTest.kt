package com.duckduckgo.remote.messaging.store

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RemoteMessagingCohortStoreImplTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val db = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext,
        RemoteMessagingDatabase::class.java
    )
        .allowMainThreadQueries()
        .build()
    private val cohortDao = db.remoteMessagingCohortDao()
    private val testee = RemoteMessagingCohortStoreImpl(db, coroutineRule.testDispatcherProvider)

    @Test
    fun whenPercentileIsSetForMessageThenReturnSameValue() = runTest {
        cohortDao.insert(RemoteMessagingCohort(messageId = "message1", percentile = 0.5f))
        assertEquals(0.5f, testee.getPercentile("message1"))
    }

    @Test
    fun whenPercentileIsNotSetForMessageThenItIsCalculated() = runTest {
        val percentile = testee.getPercentile("message1")
        assertTrue(percentile in 0.0f..1.0f)
    }

    @Test
    fun whenMoreThanOneMessageWithCohortThenReturnExpectedCohort() = runTest {
        cohortDao.insert(RemoteMessagingCohort(messageId = "message1", percentile = 0.5f))
        cohortDao.insert(RemoteMessagingCohort(messageId = "message2", percentile = 0.6f))
        assertEquals(0.5f, testee.getPercentile("message1"))
    }
}
