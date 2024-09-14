

package com.duckduckgo.networkprotection.impl.cohort

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.networkprotection.api.NetworkProtectionState
import com.duckduckgo.networkprotection.impl.pixels.NetworkProtectionPixels
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class NetPCohortUpdaterTest {
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var networkProtectionState: NetworkProtectionState

    @Mock
    private lateinit var pixels: NetworkProtectionPixels

    @Mock
    private lateinit var cohortStore: NetpCohortStore
    private lateinit var testee: NetPCohortUpdater

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testee = NetPCohortUpdater(networkProtectionState, cohortStore, coroutineRule.testDispatcherProvider, pixels)
    }

    @Test
    fun whenNetPIsNotRegisteredThenDoNothingWithCohort() = runTest {
        whenever(networkProtectionState.isEnabled()).thenReturn(false)

        testee.onVpnStarted(coroutineRule.testScope)

        verifyNoInteractions(cohortStore)
        verifyNoInteractions(pixels)
    }

    @Test
    fun whenNetPIsRegisteredAndCohortNotSetThenUpdateCohort() = runTest {
        whenever(networkProtectionState.isEnabled()).thenReturn(true)
        whenever(cohortStore.cohortLocalDate).thenReturn(null)

        testee.onVpnStarted(coroutineRule.testScope)

        verify(cohortStore).cohortLocalDate = any()
        verify(pixels).reportEnabled()
    }

    @Test
    fun whenNetPIsRegisteredAndCohortSetThenDoNothingWithCohort() = runTest {
        whenever(networkProtectionState.isEnabled()).thenReturn(true)
        whenever(cohortStore.cohortLocalDate).thenReturn(LocalDate.of(2023, 1, 1))

        testee.onVpnStarted(coroutineRule.testScope)

        verify(cohortStore).cohortLocalDate
        verify(pixels).reportEnabled()
        verifyNoMoreInteractions(cohortStore)
    }

    @Test
    fun whenNetPIsEnabledOnReconfigureAndCohortNotYetSetThenUpdateCohort() = runTest {
        whenever(networkProtectionState.isEnabled()).thenReturn(false)
        whenever(cohortStore.cohortLocalDate).thenReturn(null)
        testee.onVpnStarted(coroutineRule.testScope)
        verifyNoInteractions(cohortStore)

        whenever(networkProtectionState.isEnabled()).thenReturn(true)
        testee.onVpnReconfigured(coroutineRule.testScope)

        verify(cohortStore).cohortLocalDate = any()
        verify(pixels).reportEnabled()
    }

    @Test
    fun whenNetPIsEnabledOnReconfigureAndCohortSetThenDoNothingWithCohort() = runTest {
        whenever(networkProtectionState.isEnabled()).thenReturn(true)
        whenever(cohortStore.cohortLocalDate).thenReturn(LocalDate.of(2023, 1, 1))

        testee.onVpnReconfigured(coroutineRule.testScope)

        verify(cohortStore).cohortLocalDate
        verifyNoMoreInteractions(cohortStore)
        verify(pixels).reportEnabled()
    }
}
