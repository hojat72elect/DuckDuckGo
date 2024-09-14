

package com.duckduckgo.app.global.rating

import com.duckduckgo.app.browser.rating.db.AppEnjoymentRepository
import com.duckduckgo.app.usage.app.AppDaysUsedRepository
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("RemoveExplicitTypeArguments", "PrivatePropertyName")
class InitialPromptDeciderTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var testee: InitialPromptDecider

    private val mockAppDaysUsedRepository: AppDaysUsedRepository = mock()
    private val mockAppEnjoymentRepository: AppEnjoymentRepository = mock()

    private val NOT_ENOUGH_DAYS = (MINIMUM_DAYS_USAGE_BEFORE_FIRST_PROMPT - 1).toLong()
    private val EXACT_NUMBER_OF_DAYS = MINIMUM_DAYS_USAGE_BEFORE_FIRST_PROMPT.toLong()
    private val MORE_THAN_ENOUGH_DAYS = (MINIMUM_DAYS_USAGE_BEFORE_FIRST_PROMPT + 1).toLong()

    @Before
    fun setup() {
        testee = InitialPromptDecider(mockAppDaysUsedRepository, mockAppEnjoymentRepository)
    }

    @Test
    fun whenUserHasNotSeenPromptBeforeAndNotUsedTheAppEnoughThenShouldNotSeePrompt() = runTest {
        whenever(mockAppDaysUsedRepository.getNumberOfDaysAppUsed()).thenReturn(NOT_ENOUGH_DAYS)
        whenever(mockAppEnjoymentRepository.canUserBeShownFirstPrompt()).thenReturn(true)
        assertFalse(testee.shouldShowPrompt())
    }

    @Test
    fun whenUserHasNotSeenPromptBeforeAndUsedTheAppExactEnoughDaysThenShouldSeePrompt() = runTest {
        whenever(mockAppDaysUsedRepository.getNumberOfDaysAppUsed()).thenReturn(EXACT_NUMBER_OF_DAYS)
        whenever(mockAppEnjoymentRepository.canUserBeShownFirstPrompt()).thenReturn(true)
        assertTrue(testee.shouldShowPrompt())
    }

    @Test
    fun whenUserHasNotSeenPromptBeforeAndUsedTheAppMoreThanEnoughDaysThenShouldSeePrompt() = runTest {
        whenever(mockAppDaysUsedRepository.getNumberOfDaysAppUsed()).thenReturn(MORE_THAN_ENOUGH_DAYS)
        whenever(mockAppEnjoymentRepository.canUserBeShownFirstPrompt()).thenReturn(true)
        assertTrue(testee.shouldShowPrompt())
    }

    @Test
    fun whenUserHasSeenPromptBeforeAndNotUsedTheAppEnoughThenShouldNotSeePrompt() = runTest {
        whenever(mockAppDaysUsedRepository.getNumberOfDaysAppUsed()).thenReturn(NOT_ENOUGH_DAYS)
        whenever(mockAppEnjoymentRepository.canUserBeShownFirstPrompt()).thenReturn(false)
        assertFalse(testee.shouldShowPrompt())
    }

    @Test
    fun whenUserHasSeenPromptBeforeAndUsedTheAppExactEnoughDaysThenShouldNotSeePrompt() = runTest {
        whenever(mockAppDaysUsedRepository.getNumberOfDaysAppUsed()).thenReturn(EXACT_NUMBER_OF_DAYS)
        whenever(mockAppEnjoymentRepository.canUserBeShownFirstPrompt()).thenReturn(false)
        assertFalse(testee.shouldShowPrompt())
    }

    @Test
    fun whenUserHasSeenPromptBeforeAndUsedTheAppMoreThanEnoughDaysThenShouldNotSeePrompt() = runTest {
        whenever(mockAppDaysUsedRepository.getNumberOfDaysAppUsed()).thenReturn(MORE_THAN_ENOUGH_DAYS)
        whenever(mockAppEnjoymentRepository.canUserBeShownFirstPrompt()).thenReturn(false)
        assertFalse(testee.shouldShowPrompt())
    }
}
