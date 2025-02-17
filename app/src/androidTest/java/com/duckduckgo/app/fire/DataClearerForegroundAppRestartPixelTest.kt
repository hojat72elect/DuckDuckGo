

package com.duckduckgo.app.fire

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.systemsearch.SystemSearchActivity
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DataClearerForegroundAppRestartPixelTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val pixel = mock<Pixel>()
    private val testee = DataClearerForegroundAppRestartPixel(context, pixel)

    @Before
    fun setUp() {
        val preferences = context.getSharedPreferences(DataClearerForegroundAppRestartPixel.FILENAME, Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
    }

    @Test
    fun whenAppRestartsAfterOpenSearchWidgetThenPixelWithIntentIsSent() {
        val intent = SystemSearchActivity.fromWidget(context)
        testee.registerIntent(intent)
        testee.incrementCount()

        testee.firePendingPixels()

        verify(pixel).fire(AppPixelName.FORGET_ALL_AUTO_RESTART_WITH_INTENT)
    }

    @Test
    fun whenAppRestartsAfterOpenExternalLinkThenPixelWithIntentIsSent() {
        val i = givenIntentWithData("https://example.com")
        testee.registerIntent(i)
        testee.incrementCount()

        testee.firePendingPixels()

        verify(pixel).fire(AppPixelName.FORGET_ALL_AUTO_RESTART_WITH_INTENT)
    }

    @Test
    fun whenAppRestartsAfterOpenAnEmptyIntentThenPixelIsSent() {
        val intent = givenEmptyIntent()
        testee.registerIntent(intent)
        testee.incrementCount()

        testee.firePendingPixels()

        verify(pixel).fire(AppPixelName.FORGET_ALL_AUTO_RESTART)
    }

    @Test
    fun whenAllUnsentPixelsAreFiredThenResetCounter() {
        val intent = givenEmptyIntent()
        testee.registerIntent(intent)
        testee.incrementCount()

        testee.firePendingPixels()
        testee.firePendingPixels()

        verify(pixel).fire(AppPixelName.FORGET_ALL_AUTO_RESTART)
    }

    @Test
    fun whenAppRestartedAfterGoingBackFromBackgroundThenPixelIsSent() {
        val intent = SystemSearchActivity.fromWidget(context)
        val mockOwner: LifecycleOwner = mock()

        testee.registerIntent(intent)
        testee.onStop(mockOwner)
        testee.incrementCount()

        testee.firePendingPixels()

        verify(pixel).fire(AppPixelName.FORGET_ALL_AUTO_RESTART)
    }

    private fun givenEmptyIntent(): Intent = Intent(context, BrowserActivity::class.java)

    private fun givenIntentWithData(url: String) = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(url)
    }
}
