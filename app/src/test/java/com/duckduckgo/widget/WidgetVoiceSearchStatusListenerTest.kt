

package com.duckduckgo.widget

import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class WidgetVoiceSearchStatusListenerTest {

    private val widgetUpdater: WidgetUpdater = mock()
    private val testee = WidgetVoiceSearchStatusListener(mock(), widgetUpdater)

    @Test
    fun whenVoiceSearchStatusChangedThenShouldUpdateWidgets() {
        testee.voiceSearchStatusChanged()

        verify(widgetUpdater).updateWidgets(any())
    }
}
