

package com.duckduckgo.autofill.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import com.duckduckgo.autofill.impl.R
import com.duckduckgo.autofill.sync.CredentialsSyncPausedViewModel.Command.NavigateToCredentials
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.sync.api.engine.FeatureSyncError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CredentialsSyncPausedViewModelTest {
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val realContext = InstrumentationRegistry.getInstrumentation().targetContext
    val credentialsSyncStore = RealCredentialsSyncStore(realContext, coroutineRule.testScope, coroutineRule.testDispatcherProvider)

    val testee = CredentialsSyncPausedViewModel(
        credentialsSyncStore,
        coroutineRule.testDispatcherProvider,
    )

    @Test
    fun whenSyncPausedBecauseOfCollectionLimitReachedThenShowWarningMessage() = runTest {
        givenError(FeatureSyncError.COLLECTION_LIMIT_REACHED)
        testee.viewState().test {
            assertEquals(R.string.credentials_limit_warning, awaitItem().message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenSyncPausedBecauseOfInvalidRequestThenShowWarningMessage() = runTest {
        givenError(FeatureSyncError.INVALID_REQUEST)
        testee.viewState().test {
            assertEquals(R.string.credentials_invalid_request_warning, awaitItem().message)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenUserClicksWarningActionThenNavigateToBookmarks() = runTest {
        testee.commands().test {
            testee.onWarningActionClicked()
            assertEquals(NavigateToCredentials, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    private fun givenError(collectionLimitReached: FeatureSyncError) {
        credentialsSyncStore.isSyncPaused = true
        credentialsSyncStore.syncPausedReason = collectionLimitReached.name
    }
}
