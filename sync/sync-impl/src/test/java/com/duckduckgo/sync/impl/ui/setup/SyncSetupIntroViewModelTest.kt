

package com.duckduckgo.sync.impl.ui.setup

import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen.RECOVERY_INTRO
import com.duckduckgo.sync.impl.ui.setup.SetupAccountActivity.Companion.Screen.SYNC_INTRO
import com.duckduckgo.sync.impl.ui.setup.SyncSetupIntroViewModel.Command.AbortFlow
import com.duckduckgo.sync.impl.ui.setup.SyncSetupIntroViewModel.Command.RecoverDataFlow
import com.duckduckgo.sync.impl.ui.setup.SyncSetupIntroViewModel.Command.StartSetupFlow
import com.duckduckgo.sync.impl.ui.setup.SyncSetupIntroViewModel.ViewMode.CreateAccountIntro
import com.duckduckgo.sync.impl.ui.setup.SyncSetupIntroViewModel.ViewMode.RecoverAccountIntro
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class SyncSetupIntroViewModelTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val testee = SyncSetupIntroViewModel()

    @Test
    fun whenSyncIntroArgumentThenIntroCreateAccountScreenShown() = runTest {
        testee.viewState(SYNC_INTRO).test {
            val viewState = awaitItem()
            Assert.assertTrue(viewState.viewMode is CreateAccountIntro)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenRecoverIntroArgumentThenIntroRecoveryScreenShown() = runTest {
        testee.viewState(RECOVERY_INTRO).test {
            val viewState = awaitItem()
            Assert.assertTrue(viewState.viewMode is RecoverAccountIntro)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenOnTurnSyncOnClickedThenStartSetupFlowCommandSent() = runTest {
        testee.onTurnSyncOnClicked()

        testee.commands().test {
            val command = awaitItem()
            Assert.assertTrue(command is StartSetupFlow)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenOnStartRecoveryDataClickedThenRecoverDataFlowCommandSent() = runTest {
        testee.onStartRecoverDataClicked()

        testee.commands().test {
            val command = awaitItem()
            Assert.assertTrue(command is RecoverDataFlow)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenOnAbortCLickedThenAbortFlowCommandSent() = runTest {
        testee.onAbortClicked()

        testee.commands().test {
            val command = awaitItem()
            Assert.assertTrue(command is AbortFlow)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
