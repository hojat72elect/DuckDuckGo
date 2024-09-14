

package com.duckduckgo.macos.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.macos.impl.MacOsPixelNames.MACOS_WAITLIST_SHARE_PRESSED
import com.duckduckgo.macos.impl.MacOsViewModel.Command.GoToWindowsClientSettings
import com.duckduckgo.macos.impl.MacOsViewModel.Command.ShareLink
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class MacOsViewModelTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private var mockPixel: Pixel = mock()
    private lateinit var testee: MacOsViewModel

    @Before
    fun before() {
        testee = MacOsViewModel(mockPixel)
    }

    @Test
    fun whenOnShareClickedAndInviteCodeExistsThenEmitCommandShareInviteCodeWithCorrectCode() = runTest {
        testee.commands.test {
            testee.onShareClicked()
            assertEquals(ShareLink, awaitItem())
        }
    }

    fun whenOnGoToWindowsClickedCalledThenEmitGoToWindowsClientSettingsCommand() = runTest {
        testee.commands.test {
            testee.onGoToWindowsClicked()
            assertEquals(GoToWindowsClientSettings, awaitItem())
        }
    }

    @Test
    fun whenOnShareClickedAThenPixelFired() = runTest {
        testee.onShareClicked()

        verify(mockPixel).fire(MACOS_WAITLIST_SHARE_PRESSED)
    }
}
