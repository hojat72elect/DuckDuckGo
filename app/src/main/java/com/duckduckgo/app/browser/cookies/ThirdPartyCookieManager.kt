

package com.duckduckgo.app.browser.cookies

import android.net.Uri
import android.webkit.WebView
import androidx.core.net.toUri
import com.duckduckgo.app.browser.cookies.db.AuthCookieAllowedDomainEntity
import com.duckduckgo.app.browser.cookies.db.AuthCookiesAllowedDomainsRepository
import com.duckduckgo.common.utils.DefaultDispatcherProvider
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.cookies.api.CookieManagerProvider
import com.duckduckgo.cookies.api.ThirdPartyCookieNames
import kotlinx.coroutines.withContext
import timber.log.Timber

interface ThirdPartyCookieManager {
    suspend fun processUriForThirdPartyCookies(
        webView: WebView,
        uri: Uri,
    )

    suspend fun clearAllData()
}

class AppThirdPartyCookieManager(
    private val cookieManagerProvider: CookieManagerProvider,
    private val authCookiesAllowedDomainsRepository: AuthCookiesAllowedDomainsRepository,
    private val thirdPartyCookieNames: ThirdPartyCookieNames,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
) : ThirdPartyCookieManager {

    override suspend fun processUriForThirdPartyCookies(
        webView: WebView,
        uri: Uri,
    ) {
        if (uri.host == GOOGLE_ACCOUNTS_HOST) {
            addHostToList(uri)
        } else {
            processThirdPartyCookiesSetting(webView, uri)
        }
    }

    override suspend fun clearAllData() {
        authCookiesAllowedDomainsRepository.deleteAll(hostsThatAlwaysRequireThirdPartyCookies)
    }

    private suspend fun processThirdPartyCookiesSetting(
        webView: WebView,
        uri: Uri,
    ) {
        val host = uri.host ?: return
        val domain = authCookiesAllowedDomainsRepository.getDomain(host)
        withContext(dispatchers.main()) {
            if (domain != null && hasExcludedCookieName()) {
                Timber.d("Cookies enabled for $uri")
                cookieManagerProvider.get()?.setAcceptThirdPartyCookies(webView, true)
            } else {
                Timber.d("Cookies disabled for $uri")
                cookieManagerProvider.get()?.setAcceptThirdPartyCookies(webView, false)
            }
            domain?.let { deleteHost(it) }
        }
    }

    private suspend fun deleteHost(authCookieAllowedDomainEntity: AuthCookieAllowedDomainEntity) {
        if (hostsThatAlwaysRequireThirdPartyCookies.contains(authCookieAllowedDomainEntity.domain)) return
        authCookiesAllowedDomainsRepository.removeDomain(authCookieAllowedDomainEntity)
    }

    private suspend fun addHostToList(uri: Uri) {
        val ssDomain = uri.getQueryParameter(SS_DOMAIN)
        val accessType = uri.getQueryParameter(RESPONSE_TYPE)
        ssDomain?.let {
            if (accessType?.contains(CODE) == false) {
                ssDomain.toUri().host?.let {
                    authCookiesAllowedDomainsRepository.addDomain(it)
                }
            }
        }
    }

    private fun hasExcludedCookieName(): Boolean {
        return cookieManagerProvider.get()?.getCookie(GOOGLE_ACCOUNTS_URL)?.split(";")?.firstOrNull {
            thirdPartyCookieNames.hasExcludedCookieName(it)
        } != null
    }

    // See https://app.asana.com/0/1125189844152671/1200029737431978 for mor context about the below values
    companion object {
        private const val SS_DOMAIN = "ss_domain"
        private const val RESPONSE_TYPE = "response_type"
        private const val CODE = "code"
        const val GOOGLE_ACCOUNTS_URL = "https://accounts.google.com"
        const val GOOGLE_ACCOUNTS_HOST = "accounts.google.com"
        val hostsThatAlwaysRequireThirdPartyCookies = listOf("home.nest.com")
    }
}
