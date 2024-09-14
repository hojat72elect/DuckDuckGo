

package com.duckduckgo.installation.impl.installer

import androidx.lifecycle.LifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.COUNT
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.installation.impl.installer.InstallationPixelName.APP_INSTALLER_PACKAGE_NAME
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.robolectric.RuntimeEnvironment

@RunWith(AndroidJUnit4::class)
class InstallSourceLifecycleObserverTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val mockLifecycleOwner = mock<LifecycleOwner>()
    private val mockPixel = mock<Pixel>()
    private val context = RuntimeEnvironment.getApplication()
    private val mockInstallSourceExtractor = mock<InstallSourceExtractor>()

    private val testee = InstallSourceLifecycleObserver(
        context = context,
        pixel = mockPixel,
        dispatchers = coroutineTestRule.testDispatcherProvider,
        appCoroutineScope = coroutineTestRule.testScope,
        installSourceExtractor = mockInstallSourceExtractor,
    )

    @Test
    fun whenNotPreviouslyProcessedThenPixelSent() = runTest {
        testee.onCreate(mockLifecycleOwner)
        verify(mockPixel).fire(eq(APP_INSTALLER_PACKAGE_NAME), any(), any(), eq(COUNT))
    }

    @Test
    fun whenPreviouslyProcessedThenPixelNotSent() = runTest {
        testee.recordInstallSourceProcessed()
        testee.onCreate(mockLifecycleOwner)
        verify(mockPixel, never()).fire(eq(APP_INSTALLER_PACKAGE_NAME), any(), any(), eq(COUNT))
    }
}
