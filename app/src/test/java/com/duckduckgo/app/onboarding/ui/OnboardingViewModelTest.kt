

package com.duckduckgo.app.onboarding.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.duckduckgo.app.onboarding.store.AppStage
import com.duckduckgo.app.onboarding.store.UserStageStore
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@Suppress("EXPERIMENTAL_API_USAGE")
class OnboardingViewModelTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private var userStageStore: UserStageStore = mock()

    private val pageLayout: OnboardingPageManager = mock()

    private val testee: OnboardingViewModel by lazy {
        OnboardingViewModel(userStageStore, pageLayout, coroutineRule.testDispatcherProvider)
    }

    @Test
    fun whenOnboardingDoneThenCompleteStage() = runTest {
        testee.onOnboardingDone()
        verify(userStageStore).stageCompleted(AppStage.NEW)
    }
}
