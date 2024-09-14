

package com.duckduckgo.sync.impl.ui.setup

import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.sync.impl.Result
import com.duckduckgo.sync.impl.SyncAccountRepository
import com.duckduckgo.sync.impl.pixels.SyncPixels
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.Command.Error
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.Command.FinishSetupFlow
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.Command.ShowError
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.ViewMode.CreatingAccount
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class SyncCreateAccountViewModelTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val syncRepostitory: SyncAccountRepository = mock()
    private val syncPixels: SyncPixels = mock()

    private val testee = SyncCreateAccountViewModel(
        syncRepostitory,
        syncPixels,
        coroutineTestRule.testDispatcherProvider,
    )

    @Test
    fun whenUserIsNotSignedInThenAccountCreatedAndViewStateUpdated() = runTest {
        whenever(syncRepostitory.createAccount()).thenReturn(Result.Success(true))

        testee.viewState().test {
            val viewState = awaitItem()
            Assert.assertTrue(viewState.viewMode is CreatingAccount)
            cancelAndIgnoreRemainingEvents()
        }

        testee.commands().test {
            val command = awaitItem()
            Assert.assertTrue(command is FinishSetupFlow)
            verify(syncPixels).fireSignupDirectPixel()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenCreateAccountFailsThenEmitError() = runTest {
        whenever(syncRepostitory.createAccount()).thenReturn(Result.Error(1, ""))

        testee.viewState().test {
            val viewState = awaitItem()
            Assert.assertTrue(viewState.viewMode is CreatingAccount)
            cancelAndIgnoreRemainingEvents()
        }

        testee.commands().test {
            val command = awaitItem()
            Assert.assertTrue(command is ShowError)
            verifyNoInteractions(syncPixels)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
