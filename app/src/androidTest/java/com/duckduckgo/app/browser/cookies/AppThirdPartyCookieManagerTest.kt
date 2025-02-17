

package com.duckduckgo.app.browser.cookies

import android.content.Context
import android.webkit.WebView
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.annotation.UiThreadTest
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.app.browser.cookies.db.AuthCookiesAllowedDomainsDao
import com.duckduckgo.app.browser.cookies.db.AuthCookiesAllowedDomainsRepository
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.cookies.api.ThirdPartyCookieNames
import com.duckduckgo.cookies.impl.DefaultCookieManagerProvider
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppThirdPartyCookieManagerTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val cookieManagerProvider = DefaultCookieManagerProvider()
    private val cookieManager = cookieManagerProvider.get()!!
    private lateinit var db: AppDatabase
    private lateinit var authCookiesAllowedDomainsDao: AuthCookiesAllowedDomainsDao
    private lateinit var authCookiesAllowedDomainsRepository: AuthCookiesAllowedDomainsRepository
    private lateinit var testee: AppThirdPartyCookieManager
    private lateinit var webView: WebView
    private val thirdPartyCookieNamesMock: ThirdPartyCookieNames = mock()

    @UiThreadTest
    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        authCookiesAllowedDomainsDao = db.authCookiesAllowedDomainsDao()
        authCookiesAllowedDomainsRepository =
            AuthCookiesAllowedDomainsRepository(authCookiesAllowedDomainsDao, coroutinesTestRule.testDispatcherProvider)
        webView = TestWebView(InstrumentationRegistry.getInstrumentation().targetContext)

        whenever(thirdPartyCookieNamesMock.hasExcludedCookieName("$USER_ID_COOKIE=test")).thenReturn(true)

        testee = AppThirdPartyCookieManager(cookieManagerProvider, authCookiesAllowedDomainsRepository, thirdPartyCookieNamesMock)
    }

    @UiThreadTest
    @After
    fun after() {
        cookieManager.removeAllCookies { }
    }

    @UiThreadTest
    @Test
    fun whenProcessUriForThirdPartyCookiesIfDomainIsNotGoogleAuthAndIsNotInTheListThenThirdPartyCookiesDisabled() = runTest {
        testee.processUriForThirdPartyCookies(webView, EXAMPLE_URI)

        assertFalse(cookieManager.acceptThirdPartyCookies(webView))
    }

    @UiThreadTest
    @Test
    fun whenProcessUriForThirdPartyCookiesIfDomainIsNotGoogleAuthAndIsInTheListAndHasCookieThenThirdPartyCookiesEnabled() = runTest {
        givenDomainIsInTheThirdPartyCookieList(EXAMPLE_URI.host!!)
        givenUserIdCookieIsSet()

        testee.processUriForThirdPartyCookies(webView, EXAMPLE_URI)

        assertTrue(cookieManager.acceptThirdPartyCookies(webView))
    }

    @UiThreadTest
    @Test
    fun whenProcessUriForThirdPartyCookiesIfDomainIsNotGoogleAuthAndIsInTheListAndDoesNotHaveCookieThenThirdPartyCookiesDisabled() = runTest {
        givenDomainIsInTheThirdPartyCookieList(EXAMPLE_URI.host!!)

        testee.processUriForThirdPartyCookies(webView, EXAMPLE_URI)

        assertFalse(cookieManager.acceptThirdPartyCookies(webView))
    }

    @UiThreadTest
    @Test
    fun whenProcessUriForThirdPartyCookiesIfDomainIsInTheListAndCookieIsSetThenDomainRemovedFromList() = runTest {
        givenUserIdCookieIsSet()
        givenDomainIsInTheThirdPartyCookieList(EXAMPLE_URI.host!!)

        testee.processUriForThirdPartyCookies(webView, EXAMPLE_URI)

        assertNull(authCookiesAllowedDomainsRepository.getDomain(EXAMPLE_URI.host!!))
    }

    @UiThreadTest
    @Test
    fun whenProcessUriForThirdPartyCookiesIfDomainIsInTheListAndCookieIsNotSetThenDomainRemovedFromList() = runTest {
        givenDomainIsInTheThirdPartyCookieList(EXAMPLE_URI.host!!)

        testee.processUriForThirdPartyCookies(webView, EXAMPLE_URI)

        assertNull(authCookiesAllowedDomainsRepository.getDomain(EXAMPLE_URI.host!!))
    }

    @UiThreadTest
    @Test
    fun whenProcessUriForThirdPartyCookiesIfDomainIsInTheListAndIsFromExceptionListThenDomainNotRemovedFromList() = runTest {
        givenDomainIsInTheThirdPartyCookieList(EXCLUDED_DOMAIN_URI.host!!)

        testee.processUriForThirdPartyCookies(webView, EXCLUDED_DOMAIN_URI)

        assertNotNull(authCookiesAllowedDomainsRepository.getDomain(EXCLUDED_DOMAIN_URI.host!!))
    }

    @UiThreadTest
    @Test
    fun whenProcessUriForThirdPartyCookiesIfUrlIsGoogleAuthAndIsTokenTypeThenDomainAddedToTheList() = runTest {
        testee.processUriForThirdPartyCookies(webView, THIRD_PARTY_AUTH_URI)

        assertNotNull(authCookiesAllowedDomainsRepository.getDomain(EXAMPLE_URI.host!!))
    }

    @UiThreadTest
    @Test
    fun whenProcessUriForThirdPartyCookiesIfUrlIsGoogleAuthAndIsNotTokenTypeThenDomainNotAddedToTheList() = runTest {
        testee.processUriForThirdPartyCookies(webView, NON_THIRD_PARTY_AUTH_URI)

        assertNull(authCookiesAllowedDomainsRepository.getDomain(EXAMPLE_URI.host!!))
    }

    @Test
    fun whenClearAllDataThenDomainDeletedFromDatabase() = runTest {
        givenDomainIsInTheThirdPartyCookieList(EXAMPLE_URI.host!!)

        testee.clearAllData()

        assertNull(authCookiesAllowedDomainsRepository.getDomain(EXAMPLE_URI.host!!))
    }

    @Test
    fun whenClearAllDataIfDomainIsInExclusionListThenDomainNotDeletedFromDatabase() = runTest {
        givenDomainIsInTheThirdPartyCookieList(EXCLUDED_DOMAIN_URI.host!!)

        testee.clearAllData()

        assertNotNull(authCookiesAllowedDomainsRepository.getDomain(EXCLUDED_DOMAIN_URI.host!!))
    }

    private suspend fun givenDomainIsInTheThirdPartyCookieList(domain: String) = runTest {
        withContext(coroutinesTestRule.testDispatcherProvider.io()) {
            authCookiesAllowedDomainsRepository.addDomain(domain)
        }
    }

    private suspend fun givenUserIdCookieIsSet() {
        withContext(coroutinesTestRule.testDispatcherProvider.main()) {
            cookieManager.setCookie("https://accounts.google.com", "$USER_ID_COOKIE=test")
        }
    }

    private class TestWebView(context: Context) : WebView(context)

    companion object {
        val EXCLUDED_DOMAIN_URI = "http://home.nest.com".toUri()
        val EXAMPLE_URI = "http://example.com".toUri()
        val THIRD_PARTY_AUTH_URI =
            "https://accounts.google.com/o/oauth2/auth/identifier?response_type=permission%20id_token&ss_domain=https%3A%2F%2Fexample.com".toUri()
        val NON_THIRD_PARTY_AUTH_URI =
            "https://accounts.google.com/o/oauth2/auth/identifier?response_type=code&ss_domain=https%3A%2F%2Fexample.com".toUri()
        const val USER_ID_COOKIE = "user_id"
    }
}
