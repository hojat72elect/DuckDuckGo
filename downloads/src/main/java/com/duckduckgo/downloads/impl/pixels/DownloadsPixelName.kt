package com.duckduckgo.downloads.impl.pixels

import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin
import com.duckduckgo.common.utils.plugins.pixel.PixelParamRemovalPlugin.PixelParameter
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import javax.inject.Inject

internal enum class DownloadsPixelName(override val pixelName: String) : Pixel.PixelName {
    DOWNLOAD_REQUEST_STARTED("m_download_request_started"),
    DOWNLOAD_REQUEST_SUCCEEDED("m_download_request_succeeded"),
    DOWNLOAD_REQUEST_FAILED("m_download_request_failed"),
    DOWNLOAD_REQUEST_CANCELLED("m_download_request_cancelled"),
    DOWNLOAD_REQUEST_CANCELLED_BY_USER("m_download_request_cancelled_by_user"),
    DOWNLOAD_REQUEST_RETRIED("m_download_request_retried"),
}

@ContributesMultibinding(AppScope::class)
class DownloadPixelsParamRemoval @Inject constructor() : PixelParamRemovalPlugin {
    override fun names(): List<Pair<String, Set<PixelParameter>>> {
        return DownloadsPixelName.entries.map {
            it.pixelName to PixelParameter.removeOSVersion()
        }
    }
}
