package com.duckduckgo.networkprotection.impl.subscription.settings

import com.duckduckgo.mobile.android.R as CommonR
import app.cash.turbine.test
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.navigation.api.GlobalActivityStarter.ActivityParams
import com.duckduckgo.networkprotection.api.NetworkProtectionAccessState
import com.duckduckgo.networkprotection.api.NetworkProtectionAccessState.NetPAccessState.Locked
import com.duckduckgo.networkprotection.api.NetworkProtectionAccessState.NetPAccessState.UnLocked
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.duckduckgo.networkprotection.api.NetworkProtectionState.ConnectionState.CONNECTED
import com.duckduckgo.networkprotection.api.NetworkProtectionState.ConnectionState.CONNECTING
import com.duckduckgo.networkprotection.api.NetworkProtectionState.ConnectionState.DISCONNECTED
import com.duckduckgo.networkprotection.impl.R
import com.duckduckgo.networkprotection.impl.pixels.NetworkProtectionPixelNames.NETP_SETTINGS_PRESSED
import com.duckduckgo.networkprotection.impl.subscription.settings.ProSettingNetPViewModel.Command
import com.duckduckgo.networkprotection.impl.subscription.settings.ProSettingNetPViewModel.NetPEntryState.Hidden
import com.duckduckgo.networkprotection.impl.subscription.settings.ProSettingNetPViewModel.NetPEntryState.Pending
import com.duckduckgo.networkprotection.impl.subscription.settings.ProSettingNetPViewModel.NetPEntryState.ShowState
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ProSettingNetPViewModelTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val pixel: Pixel = mock()
    private val networkProtectionState: NetworkProtectionState = mock()
    private val networkProtectionAccessState: NetworkProtectionAccessState = mock()
    private lateinit var proSettingNetPViewModel: ProSettingNetPViewModel

    @Before
    fun before() {
        proSettingNetPViewModel = ProSettingNetPViewModel(
            networkProtectionAccessState,
            networkProtectionState,
            coroutineTestRule.testDispatcherProvider,
            pixel,
        )
    }

    @Test
    fun whenNetPSettingClickedThenReturnScreenForCurrentState() = runTest {
        val testScreen = object : ActivityParams {}
        whenever(networkProtectionAccessState.getScreenForCurrentState()).thenReturn(testScreen)

        proSettingNetPViewModel.commands().test {
            proSettingNetPViewModel.onNetPSettingClicked()

            assertEquals(Command.OpenNetPScreen(testScreen), awaitItem())
            verify(pixel).fire(NETP_SETTINGS_PRESSED)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun whenNetPIsNotUnlockedThenNetPEntryStateShouldShowHidden() = runTest {
        whenever(networkProtectionState.getConnectionStateFlow()).thenReturn(flowOf(DISCONNECTED))
        whenever(networkProtectionAccessState.getStateFlow()).thenReturn(flowOf(Locked))

        proSettingNetPViewModel.onStart(mock())

        proSettingNetPViewModel.viewState.test {
            assertEquals(
                Hidden,
                expectMostRecentItem().networkProtectionEntryState,
            )
        }
    }

    @Test
    fun whenNetPStateIsInBetaButNotAcceptedTermsThenNetPEntryStateShouldShowPending() = runTest {
        whenever(networkProtectionState.getConnectionStateFlow()).thenReturn(flowOf(DISCONNECTED))
        whenever(networkProtectionAccessState.getStateFlow()).thenReturn(flowOf(UnLocked))
        whenever(networkProtectionState.isOnboarded()).thenReturn(false)

        proSettingNetPViewModel.onStart(mock())

        proSettingNetPViewModel.viewState.test {
            assertEquals(
                Pending,
                expectMostRecentItem().networkProtectionEntryState,
            )
        }
    }

    @Test
    fun whenNetPStateIsInBetaAndOnboardedAndEnabledThenNetPEntryStateShouldCorrectShowState() =
        runTest {
            whenever(networkProtectionState.getConnectionStateFlow()).thenReturn(flowOf(CONNECTED))
            whenever(networkProtectionAccessState.getStateFlow()).thenReturn(flowOf(UnLocked))
            whenever(networkProtectionState.isOnboarded()).thenReturn(true)

            proSettingNetPViewModel.onStart(mock())

            proSettingNetPViewModel.viewState.test {
                assertEquals(
                    ShowState(
                        icon = CommonR.drawable.ic_check_green_round_16,
                        subtitle = R.string.netpSubscriptionSettingsConnected,
                    ),
                    expectMostRecentItem().networkProtectionEntryState,
                )
            }
        }

    @Test
    fun whenNetPStateIsInBetaAndNotOnboardedAndEnabledThenNetPEntryStateShouldCorrectShowState() =
        runTest {
            whenever(networkProtectionState.getConnectionStateFlow()).thenReturn(flowOf(CONNECTED))
            whenever(networkProtectionAccessState.getStateFlow()).thenReturn(flowOf(UnLocked))
            whenever(networkProtectionState.isOnboarded()).thenReturn(false)

            proSettingNetPViewModel.onStart(mock())

            proSettingNetPViewModel.viewState.test {
                assertEquals(
                    Pending,
                    expectMostRecentItem().networkProtectionEntryState,
                )
            }
        }

    @Test
    fun whenNetPStateIsInBetaOnboardedAndEnabledThenNetPEntryStateShouldCorrectShowState() =
        runTest {
            whenever(networkProtectionState.getConnectionStateFlow()).thenReturn(flowOf(CONNECTED))
            whenever(networkProtectionAccessState.getStateFlow()).thenReturn(flowOf(UnLocked))
            whenever(networkProtectionState.isOnboarded()).thenReturn(true)

            proSettingNetPViewModel.onStart(mock())

            proSettingNetPViewModel.viewState.test {
                assertEquals(
                    ShowState(
                        icon = CommonR.drawable.ic_check_green_round_16,
                        subtitle = R.string.netpSubscriptionSettingsConnected,
                    ),
                    expectMostRecentItem().networkProtectionEntryState,
                )
            }
        }

    @Test
    fun whenNetPStateIsInBetaAndConnectingThenNetPEntryStateShouldCorrectShowState() = runTest {
        whenever(networkProtectionState.getConnectionStateFlow()).thenReturn(flowOf(CONNECTING))
        whenever(networkProtectionAccessState.getStateFlow()).thenReturn(flowOf(UnLocked))
        whenever(networkProtectionState.isOnboarded()).thenReturn(true)

        proSettingNetPViewModel.onStart(mock())

        proSettingNetPViewModel.viewState.test {
            assertEquals(
                ShowState(
                    icon = CommonR.drawable.ic_check_green_round_16,
                    subtitle = R.string.netpSubscriptionSettingsConnecting,
                ),
                expectMostRecentItem().networkProtectionEntryState,
            )
        }
    }

    @Test
    fun whenNetPStateIsInBetaAndDisabledThenNetPEntryStateShouldCorrectShowState() = runTest {
        whenever(networkProtectionState.getConnectionStateFlow()).thenReturn(flowOf(DISCONNECTED))
        whenever(networkProtectionAccessState.getStateFlow()).thenReturn(flowOf(UnLocked))
        whenever(networkProtectionState.isOnboarded()).thenReturn(true)

        proSettingNetPViewModel.onStart(mock())

        proSettingNetPViewModel.viewState.test {
            assertEquals(
                ShowState(
                    icon = CommonR.drawable.ic_exclamation_yellow_16,
                    subtitle = R.string.netpSubscriptionSettingsDisconnected,
                ),
                expectMostRecentItem().networkProtectionEntryState,
            )
        }
    }
}
