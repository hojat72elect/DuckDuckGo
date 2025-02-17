package com.duckduckgo.remote.messaging.impl.pixels

import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.remote.messaging.api.RemoteMessage
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface RemoteMessagingPixels {

    fun fireRemoteMessageShownPixel(remoteMessage: RemoteMessage)
    fun fireRemoteMessageDismissedPixel(remoteMessage: RemoteMessage)
    fun fireRemoteMessagePrimaryActionClickedPixel(remoteMessage: RemoteMessage)
    fun fireRemoteMessageSecondaryActionClickedPixel(remoteMessage: RemoteMessage)
    fun fireRemoteMessageActionClickedPixel(remoteMessage: RemoteMessage)
    fun fireRemoteMessageSharedPixel(remoteMessage: Map<String, String>)
}

@ContributesBinding(AppScope::class)
class RealRemoteMessagingPixels @Inject constructor(
    private val pixel: Pixel,
) : RemoteMessagingPixels {
    override fun fireRemoteMessageShownPixel(remoteMessage: RemoteMessage) {
        pixel.fire(
            pixel = RemoteMessagingPixelName.REMOTE_MESSAGE_SHOWN_UNIQUE,
            parameters = remoteMessage.asPixelParams(),
            type = PixelType.UNIQUE
        )
        pixel.fire(
            pixel = RemoteMessagingPixelName.REMOTE_MESSAGE_SHOWN,
            parameters = remoteMessage.asPixelParams()
        )
    }

    override fun fireRemoteMessageDismissedPixel(remoteMessage: RemoteMessage) {
        pixel.fire(
            pixel = RemoteMessagingPixelName.REMOTE_MESSAGE_DISMISSED,
            parameters = remoteMessage.asPixelParams()
        )
    }

    override fun fireRemoteMessagePrimaryActionClickedPixel(remoteMessage: RemoteMessage) {
        pixel.fire(
            pixel = RemoteMessagingPixelName.REMOTE_MESSAGE_PRIMARY_ACTION_CLICKED,
            parameters = remoteMessage.asPixelParams()
        )
    }

    override fun fireRemoteMessageSecondaryActionClickedPixel(remoteMessage: RemoteMessage) {
        pixel.fire(
            pixel = RemoteMessagingPixelName.REMOTE_MESSAGE_SECONDARY_ACTION_CLICKED,
            parameters = remoteMessage.asPixelParams()
        )
    }

    override fun fireRemoteMessageActionClickedPixel(remoteMessage: RemoteMessage) {
        pixel.fire(
            pixel = RemoteMessagingPixelName.REMOTE_MESSAGE_ACTION_CLICKED,
            parameters = remoteMessage.asPixelParams()
        )
    }

    override fun fireRemoteMessageSharedPixel(pixelsParams: Map<String, String>) {
        pixel.fire(
            pixel = RemoteMessagingPixelName.REMOTE_MESSAGE_SHARED,
            parameters = pixelsParams
        )
    }
}

private fun RemoteMessage.asPixelParams(): Map<String, String> =
    mapOf(Pixel.PixelParameter.MESSAGE_SHOWN to this.id)
