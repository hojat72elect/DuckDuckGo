

package com.duckduckgo.app.global.rating

import com.duckduckgo.app.browser.rating.db.AppEnjoymentRepository
import com.duckduckgo.app.usage.app.AppDaysUsedRepository
import com.duckduckgo.common.test.CoroutineTestRule
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("RemoveExplicitTypeArguments")
class SecondaryPromptDeciderTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var testee: SecondaryPromptDecider

    private val mockAppDaysUsedRepository: AppDaysUsedRepository = mock()
    private val mockAppEnjoymentRepository: AppEnjoymentRepository = mock()

    @Before
    fun setup() = runTest {
        testee = SecondaryPromptDecider(mockAppDaysUsedRepository, mockAppEnjoymentRepository)
        whenever(mockAppEnjoymentRepository.dateUserDismissedFirstPrompt()).thenReturn(Date())
        whenever(mockAppEnjoymentRepository.canUserBeShownSecondPrompt()).thenReturn(true)
    }

    @Test
    fun whenUserHasUsedTheAppForAWhileSinceSeeingFirstPromptThenTheySeeSecondPrompt() = runTest {
        configureLotsOfAppUsage()
        assertTrue(testee.shouldShowPrompt())
    }

    @Test
    fun whenUserHasNotUsedTheAppMuchSinceSeeingFirstPromptThenTheyDoNotSeeSecondPrompt() = runTest {
        whenever(mockAppEnjoymentRepository.canUserBeShownSecondPrompt()).thenReturn(true)
        configureNotALotOfAppUsage()
        assertFalse(testee.shouldShowPrompt())
    }

    @Test
    fun whenUserHasAlreadyRatedOrGaveFeedbackThenTheyDoNoSeeASecondPromptEvenAfterALotOfUsage() = runTest {
        whenever(mockAppEnjoymentRepository.canUserBeShownSecondPrompt()).thenReturn(false)
        configureLotsOfAppUsage()
        assertFalse(testee.shouldShowPrompt())
    }

    private suspend fun configureLotsOfAppUsage() {
        whenever(mockAppDaysUsedRepository.getNumberOfDaysAppUsedSinceDate(any())).thenReturn(Long.MAX_VALUE)
    }

    private suspend fun configureNotALotOfAppUsage() {
        whenever(mockAppDaysUsedRepository.getNumberOfDaysAppUsedSinceDate(any())).thenReturn(0)
    }
}
