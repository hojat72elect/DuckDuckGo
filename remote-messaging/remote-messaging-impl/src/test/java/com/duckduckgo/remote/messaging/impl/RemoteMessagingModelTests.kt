package com.duckduckgo.remote.messaging.impl

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.remote.messaging.api.Content
import com.duckduckgo.remote.messaging.api.RemoteMessage
import com.duckduckgo.remote.messaging.api.RemoteMessagingRepository
import com.duckduckgo.remote.messaging.impl.pixels.RemoteMessagingPixels
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RemoteMessagingModelTests {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val remoteMessagingRepository: RemoteMessagingRepository = mock()
    private val remoteMessagingPixels: RemoteMessagingPixels = mock()

    private lateinit var testee: RealRemoteMessageModel

    val remoteMessage = RemoteMessage("id1", Content.Small("", ""), emptyList(), emptyList())

    @Before
    fun setup() {
        testee = RealRemoteMessageModel(
            remoteMessagingRepository,
            remoteMessagingPixels,
            coroutineRule.testDispatcherProvider
        )
    }

    @Test
    fun onMessageShownThenPixelIsFired() = runTest {
        testee.onMessageShown(remoteMessage)

        verify(remoteMessagingPixels).fireRemoteMessageShownPixel(remoteMessage)
    }

    @Test
    fun onMessageDismissedThenPixelIsFiredAndMessageDismissed() = runTest {
        testee.onMessageDismissed(remoteMessage)

        verify(remoteMessagingPixels).fireRemoteMessageDismissedPixel(remoteMessage)
        verify(remoteMessagingRepository).dismissMessage(remoteMessage.id)
    }

    @Test
    fun onPrimaryActionClickedThenPixelFiredAndMessageDismissed() = runTest {
        val action = testee.onPrimaryActionClicked(remoteMessage)

        verify(remoteMessagingPixels).fireRemoteMessagePrimaryActionClickedPixel(remoteMessage)
        verify(remoteMessagingRepository).dismissMessage(remoteMessage.id)
        assertEquals(action, null)
    }

    @Test
    fun onSecondaryActionClickedThenPixelFiredAndMessageDismissed() = runTest {
        val action = testee.onSecondaryActionClicked(remoteMessage)

        verify(remoteMessagingPixels).fireRemoteMessageSecondaryActionClickedPixel(remoteMessage)
        verify(remoteMessagingRepository).dismissMessage(remoteMessage.id)
        assertEquals(action, null)
    }

    @Test
    fun onActionClickedThenPixelFired() = runTest {
        val action = testee.onActionClicked(remoteMessage)

        verify(remoteMessagingPixels).fireRemoteMessageActionClickedPixel(remoteMessage)
        assertEquals(action, null)
    }
}
