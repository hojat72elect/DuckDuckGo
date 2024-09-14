

package com.duckduckgo.sync.impl.ui.setup

import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.sync.impl.ui.setup.SyncDeviceConnectedViewModel.Command.FinishSetupFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class SyncShowRecoveryCodeViewModelTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val testee = SyncDeviceConnectedViewModel(
        coroutineTestRule.testDispatcherProvider,
    )

    @Test
    fun whenNextClickedThenEmitFinishSetupCommand() = runTest {
        testee.onDoneClicked()

        testee.commands().test {
            val command = awaitItem()
            Assert.assertTrue(command is FinishSetupFlow)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
