

package com.duckduckgo.app.browser.rating.db

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.global.rating.PromptCount
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@Suppress("RemoveExplicitTypeArguments")
class AppEnjoymentDatabaseRepositoryTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private lateinit var testee: AppEnjoymentDatabaseRepository

    private lateinit var database: AppDatabase
    private lateinit var dao: AppEnjoymentDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = database.appEnjoymentDao()
        testee = AppEnjoymentDatabaseRepository(dao)
    }

    @After
    fun after() {
        database.close()
    }

    @Test
    fun whenFirstCreatedThenPrompt1CanBeShown() = runTest {
        assertTrue(testee.canUserBeShownFirstPrompt())
    }

    @Test
    fun whenUserGaveFeedbackForPrompt1ThenPrompt1CannotBeShown() = runTest {
        testee.onUserSelectedToGiveFeedback(FIRST_PROMPT)
        assertFalse(testee.canUserBeShownFirstPrompt())
    }

    @Test
    fun whenUserDeclinedToGiveFeedbackForPrompt1ThenPrompt1CannotBeShown() = runTest {
        testee.onUserDeclinedToGiveFeedback(FIRST_PROMPT)
        assertFalse(testee.canUserBeShownFirstPrompt())
    }

    @Test
    fun whenUserGaveRatingForPrompt1ThenPrompt1CannotBeShown() = runTest {
        testee.onUserSelectedToRateApp(FIRST_PROMPT)
        assertFalse(testee.canUserBeShownFirstPrompt())
    }

    @Test
    fun whenUserDeclinedRatingForPrompt1ThenPrompt1CannotBeShown() = runTest {
        testee.onUserDeclinedToRateApp(FIRST_PROMPT)
        assertFalse(testee.canUserBeShownFirstPrompt())
    }

    @Test
    fun whenUserDeclinedToSayWhetherEnjoyingForPrompt1ThenPrompt1CannotBeShown() = runTest {
        testee.onUserDeclinedToSayIfEnjoyingApp(FIRST_PROMPT)
        assertFalse(testee.canUserBeShownFirstPrompt())
    }

    @Test
    fun whenUserGaveFeedbackForPrompt2ThenPrompt2CannotBeShownAgain() = runTest {
        testee.onUserSelectedToGiveFeedback(SECOND_PROMPT)
        assertFalse(testee.canUserBeShownSecondPrompt())
    }

    @Test
    fun whenUserDeclinedToGiveFeedbackForPrompt2ThenPrompt2CannotBeShownAgain() = runTest {
        testee.onUserDeclinedToGiveFeedback(SECOND_PROMPT)
        assertFalse(testee.canUserBeShownSecondPrompt())
    }

    @Test
    fun whenUserGaveRatingForPrompt2ThenPrompt2CannotBeShownAgain() = runTest {
        testee.onUserSelectedToRateApp(SECOND_PROMPT)
        assertFalse(testee.canUserBeShownSecondPrompt())
    }

    @Test
    fun whenUserDeclinedRatingForPrompt2ThenPrompt2CannotBeShownAgain() = runTest {
        testee.onUserDeclinedToRateApp(SECOND_PROMPT)
        assertFalse(testee.canUserBeShownSecondPrompt())
    }

    @Test
    fun whenUserDeclinedToSayWhetherEnjoyingForPrompt2ThenPrompt2CannotBeShownAgain() = runTest {
        testee.onUserDeclinedToSayIfEnjoyingApp(SECOND_PROMPT)
        assertFalse(testee.canUserBeShownSecondPrompt())
    }

    companion object {
        private val FIRST_PROMPT = PromptCount(1)
        private val SECOND_PROMPT = PromptCount(2)
    }
}
