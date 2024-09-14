

package com.duckduckgo.sync.impl.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.sync.TestSyncFixtures.jsonConnectKeyEncoded
import com.duckduckgo.sync.TestSyncFixtures.jsonRecoveryKeyEncoded
import com.duckduckgo.sync.impl.AccountErrorCodes.ALREADY_SIGNED_IN
import com.duckduckgo.sync.impl.AccountErrorCodes.CONNECT_FAILED
import com.duckduckgo.sync.impl.AccountErrorCodes.CREATE_ACCOUNT_FAILED
import com.duckduckgo.sync.impl.AccountErrorCodes.GENERIC_ERROR
import com.duckduckgo.sync.impl.AccountErrorCodes.INVALID_CODE
import com.duckduckgo.sync.impl.AccountErrorCodes.LOGIN_FAILED
import com.duckduckgo.sync.impl.Clipboard
import com.duckduckgo.sync.impl.Result.Error
import com.duckduckgo.sync.impl.Result.Success
import com.duckduckgo.sync.impl.SyncAccountRepository
import com.duckduckgo.sync.impl.ui.EnterCodeViewModel.AuthState
import com.duckduckgo.sync.impl.ui.EnterCodeViewModel.AuthState.Idle
import com.duckduckgo.sync.impl.ui.EnterCodeViewModel.Command.LoginSucess
import com.duckduckgo.sync.impl.ui.EnterCodeViewModel.Command.ShowError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
internal class EnterCodeViewModelTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val syncAccountRepository: SyncAccountRepository = mock()
    private val clipboard: Clipboard = mock()

    private val testee = EnterCodeViewModel(
        syncAccountRepository,
        clipboard,
        coroutineTestRule.testDispatcherProvider,
    )

    @Test
    fun whenUIStartsThenViewStateIsIdle() = runTest {
        testee.viewState().test {
            val item = awaitItem()
            assertTrue(item.authState is Idle)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenUserClicksOnPasteCodeThenClipboardIsPasted() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn(jsonRecoveryKeyEncoded)

        testee.onPasteCodeClicked()

        verify(clipboard).pasteFromClipboard()
    }

    @Test
    fun whenUserClicksOnPasteCodeWithRecoveryCodeThenProcessCode() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn(jsonRecoveryKeyEncoded)
        whenever(syncAccountRepository.processCode(jsonRecoveryKeyEncoded)).thenReturn(Success(true))

        testee.onPasteCodeClicked()

        testee.commands().test {
            val command = awaitItem()
            assertTrue(command is LoginSucess)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenUserClicksOnPasteCodeWithConnectCodeThenProcessCode() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn(jsonConnectKeyEncoded)
        whenever(syncAccountRepository.processCode(jsonConnectKeyEncoded)).thenReturn(Success(true))

        testee.onPasteCodeClicked()

        testee.commands().test {
            val command = awaitItem()
            assertTrue(command is LoginSucess)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenPastedInvalidCodeThenAuthStateError() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn("invalid code")
        whenever(syncAccountRepository.processCode("invalid code")).thenReturn(Error(code = INVALID_CODE.code))

        testee.onPasteCodeClicked()

        testee.viewState().test {
            val item = awaitItem()
            assertTrue(item.authState is AuthState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenProcessCodeButUserSignedInThenShowError() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn(jsonRecoveryKeyEncoded)
        whenever(syncAccountRepository.processCode(jsonRecoveryKeyEncoded)).thenReturn(Error(code = ALREADY_SIGNED_IN.code))

        testee.onPasteCodeClicked()

        testee.commands().test {
            val command = awaitItem()
            assertTrue(command is ShowError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenProcessCodeAndLoginFailsThenShowError() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn(jsonRecoveryKeyEncoded)
        whenever(syncAccountRepository.processCode(jsonRecoveryKeyEncoded)).thenReturn(Error(code = LOGIN_FAILED.code))

        testee.onPasteCodeClicked()

        testee.commands().test {
            val command = awaitItem()
            assertTrue(command is ShowError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenProcessCodeAndConnectFailsThenShowError() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn(jsonConnectKeyEncoded)
        whenever(syncAccountRepository.processCode(jsonConnectKeyEncoded)).thenReturn(Error(code = CONNECT_FAILED.code))

        testee.onPasteCodeClicked()

        testee.commands().test {
            val command = awaitItem()
            assertTrue(command is ShowError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenProcessCodeAndCreateAccountFailsThenShowError() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn(jsonRecoveryKeyEncoded)
        whenever(syncAccountRepository.processCode(jsonRecoveryKeyEncoded)).thenReturn(Error(code = CREATE_ACCOUNT_FAILED.code))

        testee.onPasteCodeClicked()

        testee.commands().test {
            val command = awaitItem()
            assertTrue(command is ShowError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun whenProcessCodeAndGenericErrorThenDoNothing() = runTest {
        whenever(clipboard.pasteFromClipboard()).thenReturn(jsonRecoveryKeyEncoded)
        whenever(syncAccountRepository.processCode(jsonRecoveryKeyEncoded)).thenReturn(Error(code = GENERIC_ERROR.code))

        testee.onPasteCodeClicked()

        testee.commands().test {
            cancelAndIgnoreRemainingEvents()
        }
    }
}
