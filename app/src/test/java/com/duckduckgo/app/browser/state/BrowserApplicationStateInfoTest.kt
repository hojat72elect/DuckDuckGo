

package com.duckduckgo.app.browser.state

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.browser.BrowserActivity
import com.duckduckgo.browser.api.BrowserLifecycleObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*

@RunWith(AndroidJUnit4::class)
class BrowserApplicationStateInfoTest {

    private lateinit var browserApplicationStateInfo: BrowserApplicationStateInfo
    private val observer: BrowserLifecycleObserver = mock()
    private val activity = FakeBrowserActivity()

    class FakeBrowserActivity : BrowserActivity() {
        var isConfigChange = false

        override fun isChangingConfigurations(): Boolean {
            return isConfigChange
        }
    }

    @Before
    fun setup() {
        activity.destroyedByBackPress = false
        browserApplicationStateInfo = BrowserApplicationStateInfo(setOf(observer))
    }

    @Test
    fun whenActivityCreatedThenNoop() {
        browserApplicationStateInfo.onActivityCreated(activity, null)

        verifyNoInteractions(observer)
    }

    @Test
    fun whenFirstActivityCreatedAndStartedThenNotifyFreshAppLaunch() {
        browserApplicationStateInfo.onActivityCreated(activity, null)

        browserApplicationStateInfo.onActivityStarted(activity)

        verify(observer).onOpen(true)
    }

    @Test
    fun whenAllActivitiesStopAndRestartThenNotifyAppOpen() {
        browserApplicationStateInfo.onActivityCreated(activity, null)
        browserApplicationStateInfo.onActivityCreated(activity, null)

        browserApplicationStateInfo.onActivityStarted(activity)
        browserApplicationStateInfo.onActivityStarted(activity)

        verify(observer).onOpen(true)

        browserApplicationStateInfo.onActivityStopped(activity)
        browserApplicationStateInfo.onActivityStopped(activity)
        verify(observer).onClose()
        verify(observer, never()).onExit()

        browserApplicationStateInfo.onActivityStarted(activity)
        browserApplicationStateInfo.onActivityStarted(activity)
        verify(observer).onOpen(false)
    }

    @Test
    fun whenAllActivitiesAreDestroyedAndRecreatedThenNotifyFreshAppLaunch() {
        browserApplicationStateInfo.onActivityCreated(activity, null)
        browserApplicationStateInfo.onActivityCreated(activity, null)

        browserApplicationStateInfo.onActivityStarted(activity)
        browserApplicationStateInfo.onActivityStarted(activity)

        verify(observer).onOpen(true)

        browserApplicationStateInfo.onActivityStopped(activity)
        browserApplicationStateInfo.onActivityStopped(activity)
        verify(observer).onClose()

        browserApplicationStateInfo.onActivityDestroyed(activity)
        browserApplicationStateInfo.onActivityDestroyed(activity)
        verify(observer).onClose()
        verify(observer).onExit()

        browserApplicationStateInfo.onActivityStarted(activity)
        browserApplicationStateInfo.onActivityStarted(activity)
        verify(observer).onOpen(true)
    }

    @Test
    fun whenAllActivitiesAreDestroyedByBackPressAndRecreatedThenDoNotNotifyFreshAppLaunch() {
        activity.destroyedByBackPress = true

        browserApplicationStateInfo.onActivityCreated(activity, null)
        browserApplicationStateInfo.onActivityCreated(activity, null)

        browserApplicationStateInfo.onActivityStarted(activity)
        browserApplicationStateInfo.onActivityStarted(activity)

        verify(observer).onOpen(true)

        browserApplicationStateInfo.onActivityStopped(activity)
        browserApplicationStateInfo.onActivityStopped(activity)
        verify(observer).onClose()

        browserApplicationStateInfo.onActivityDestroyed(activity)
        browserApplicationStateInfo.onActivityDestroyed(activity)
        verify(observer).onClose()
        verify(observer, never()).onExit()

        browserApplicationStateInfo.onActivityStarted(activity)
        browserApplicationStateInfo.onActivityStarted(activity)
        verify(observer).onOpen(false)
    }

    @Test
    fun whenAllActivitiesAreDestroyedByConfigChangeAndRecreatedThenDoNotNotifyFreshAppLaunch() {
        activity.isConfigChange = true

        browserApplicationStateInfo.onActivityCreated(activity, null)
        browserApplicationStateInfo.onActivityCreated(activity, null)

        browserApplicationStateInfo.onActivityStarted(activity)
        browserApplicationStateInfo.onActivityStarted(activity)

        verify(observer).onOpen(true)

        browserApplicationStateInfo.onActivityStopped(activity)
        browserApplicationStateInfo.onActivityStopped(activity)
        verify(observer).onClose()

        browserApplicationStateInfo.onActivityDestroyed(activity)
        browserApplicationStateInfo.onActivityDestroyed(activity)
        verify(observer).onClose()
        verify(observer, never()).onExit()

        browserApplicationStateInfo.onActivityStarted(activity)
        browserApplicationStateInfo.onActivityStarted(activity)
        verify(observer).onOpen(false)
    }
}
