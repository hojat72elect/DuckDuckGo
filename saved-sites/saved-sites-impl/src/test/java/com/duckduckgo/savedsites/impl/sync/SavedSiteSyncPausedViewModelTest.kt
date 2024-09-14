

package com.duckduckgo.savedsites.impl.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.saved.sites.impl.R
import com.duckduckgo.savedsites.impl.sync.SavedSiteSyncPausedViewModel.Command.NavigateToBookmarks
import com.duckduckgo.sync.api.engine.FeatureSyncError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedSiteSyncPausedViewModelTest {
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val realContext = InstrumentationRegistry.getInstrumentation().targetContext
    val savedSitesSyncStore = RealSavedSitesSyncStore(realContext, coroutineRule.testScope, coroutineRule.testDispatcherProvider)

    val testee = SavedSiteSyncPausedViewModel(
        savedSitesSyncStore,
        coroutineRule.testDispatcherProvider,
    )

    @Test
    fun whenSyncNotPausedThenShowNoWarningMessage() = runTest {
        givenNoError()
        testee.viewState().test {
            assertNull(awaitItem().message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenSyncPausedBecauseOfCollectionLimitReachedThenShowWarningMessage() = runTest {
        givenError(FeatureSyncError.COLLECTION_LIMIT_REACHED)
        testee.viewState().test {
            assertEquals(R.string.saved_site_limit_warning, awaitItem().message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenSyncPausedBecauseOfInvalidRequestThenShowWarningMessage() = runTest {
        givenError(FeatureSyncError.INVALID_REQUEST)
        testee.viewState().test {
            assertEquals(R.string.saved_site_invalid_warning, awaitItem().message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenUserClicksWarningActionThenNavigateToBookmarks() = runTest {
        testee.commands().test {
            testee.onWarningActionClicked()
            assertEquals(NavigateToBookmarks, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun givenNoError() {
        savedSitesSyncStore.isSyncPaused = false
    }

    private fun givenError(collectionLimitReached: FeatureSyncError) {
        savedSitesSyncStore.isSyncPaused = true
        savedSitesSyncStore.syncPausedReason = collectionLimitReached.name
    }
}
