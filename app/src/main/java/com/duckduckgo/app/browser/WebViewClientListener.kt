

package com.duckduckgo.app.browser

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslCertificate
import android.os.Message
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import com.duckduckgo.app.browser.model.BasicAuthenticationRequest
import com.duckduckgo.app.global.model.Site
import com.duckduckgo.app.surrogates.SurrogateResponse
import com.duckduckgo.app.trackerdetection.model.TrackingEvent
import com.duckduckgo.site.permissions.api.SitePermissionsManager.SitePermissions

interface WebViewClientListener {

    fun onPageContentStart(url: String)
    fun navigationStateChanged(newWebNavigationState: WebNavigationState)
    fun pageRefreshed(refreshedUrl: String)
    fun progressChanged(newProgress: Int)
    fun willOverrideUrl(newUrl: String)
    fun redirectTriggeredByGpc()

    fun onSitePermissionRequested(
        request: PermissionRequest,
        sitePermissionsAllowedToAsk: SitePermissions,
    )

    fun onSiteLocationPermissionRequested(
        origin: String,
        callback: GeolocationPermissions.Callback,
    )

    fun titleReceived(newTitle: String, url: String?)
    fun trackerDetected(event: TrackingEvent)
    fun pageHasHttpResources(page: String)
    fun pageHasHttpResources(page: Uri)
    fun onCertificateReceived(certificate: SslCertificate?)

    fun sendEmailRequested(emailAddress: String)
    fun sendSmsRequested(telephoneNumber: String)
    fun dialTelephoneNumberRequested(telephoneNumber: String)
    fun goFullScreen(view: View)
    fun exitFullScreen()
    fun showFileChooser(
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: WebChromeClient.FileChooserParams,
    )

    fun handleAppLink(
        appLink: SpecialUrlDetector.UrlType.AppLink,
        isForMainFrame: Boolean,
    ): Boolean

    fun handleNonHttpAppLink(nonHttpAppLink: SpecialUrlDetector.UrlType.NonHttpAppLink): Boolean
    fun handleCloakedAmpLink(initialUrl: String)
    fun startProcessingTrackingLink()
    fun openMessageInNewTab(message: Message)
    fun recoverFromRenderProcessGone()
    fun requiresAuthentication(request: BasicAuthenticationRequest)
    fun closeCurrentTab()
    fun closeAndSelectSourceTab()
    fun upgradedToHttps()
    fun surrogateDetected(surrogate: SurrogateResponse)
    fun isDesktopSiteEnabled(): Boolean

    fun loginDetected()
    fun dosAttackDetected()
    fun iconReceived(
        url: String,
        icon: Bitmap,
    )

    fun iconReceived(
        visitedUrl: String,
        iconUrl: String,
    )

    fun prefetchFavicon(url: String)
    fun linkOpenedInNewTab(): Boolean
    fun isActiveTab(): Boolean
    fun onReceivedError(errorType: WebViewErrorResponse, url: String)
    fun recordErrorCode(error: String, url: String)
    fun recordHttpErrorCode(statusCode: Int, url: String)

    fun getCurrentTabId(): String

    fun getSite(): Site?
    fun onReceivedSslError(
        handler: SslErrorHandler,
        errorResponse: SslErrorResponse,
    )
    fun onShouldOverride()
}
