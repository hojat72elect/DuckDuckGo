

package com.duckduckgo.app.widget

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.widget.ui.WidgetCapabilities
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class AddWidgetCompatLauncherTest {
    private val defaultAddWidgetLauncher: AddWidgetLauncher = mock()
    private val legacyAddWidgetLauncher: AddWidgetLauncher = mock()
    private val widgetCapabilities: WidgetCapabilities = mock()
    private val testee = AddWidgetCompatLauncher(
        defaultAddWidgetLauncher,
        legacyAddWidgetLauncher,
        widgetCapabilities,
    )

    @Test
    fun whenAutomaticWidgetAddIsNotSupportedThenDelegateToLegacyAddWidgetLauncher() {
        whenever(widgetCapabilities.supportsAutomaticWidgetAdd).thenReturn(false)

        testee.launchAddWidget(null)

        verify(legacyAddWidgetLauncher).launchAddWidget(null)
    }

    @Test
    fun whenAutomaticWidgetAddIsSupportedThenDelegateToAppWidgetManagerAddWidgetLauncher() {
        whenever(widgetCapabilities.supportsAutomaticWidgetAdd).thenReturn(true)

        testee.launchAddWidget(null)

        verify(defaultAddWidgetLauncher).launchAddWidget(null)
    }
}
