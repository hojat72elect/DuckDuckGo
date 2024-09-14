

package com.duckduckgo.windows.impl.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.windows.impl.ui.WindowsViewModel.Command.GoToMacClientSettings
import com.duckduckgo.windows.impl.ui.WindowsViewModel.Command.ShareLink
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
internal class WindowsViewModelTest {
    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private var mockPixel: Pixel = mock()

    private lateinit var testee: WindowsViewModel

    @Before
    fun before() {
        testee = WindowsViewModel(mockPixel)
    }

    @Test
    fun whenOnShareClickedThenEmitShareLinkCommand() = runTest {
        testee.commands.test {
            testee.onShareClicked()
            assertEquals(ShareLink, awaitItem())
        }
    }

    @Test
    fun whenOnGoToMacClickedThenEmitGoToMacClientSettingsCommand() = runTest {
        testee.commands.test {
            testee.onGoToMacClicked()
            assertEquals(GoToMacClientSettings, awaitItem())
        }
    }
}
