

package com.duckduckgo.privacy.dashboard.impl.ui

import android.net.Uri
import android.os.Build.VERSION_CODES
import androidx.core.net.toUri
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.duckduckgo.app.global.model.Site
import com.duckduckgo.app.global.model.domain
import com.duckduckgo.app.privacy.db.UserAllowListRepository
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.COUNT
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.brokensite.api.BrokenSite
import com.duckduckgo.brokensite.api.BrokenSiteSender
import com.duckduckgo.brokensite.api.ReportFlow.DASHBOARD
import com.duckduckgo.browser.api.UserBrowserProperties
import com.duckduckgo.browser.api.brokensite.BrokenSiteContext
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.State
import com.duckduckgo.privacy.config.api.ContentBlocking
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import com.duckduckgo.privacy.dashboard.impl.WebBrokenSiteFormFeature
import com.duckduckgo.privacy.dashboard.impl.di.JsonModule
import com.duckduckgo.privacy.dashboard.impl.pixels.PrivacyDashboardCustomTabPixelNames
import com.duckduckgo.privacy.dashboard.impl.pixels.PrivacyDashboardPixels.*
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.Command.GoBack
import com.duckduckgo.privacy.dashboard.impl.ui.PrivacyDashboardHybridViewModel.Command.LaunchReportBrokenSite
import com.duckduckgo.privacyprotectionspopup.api.PrivacyProtectionsPopupExperimentExternalPixels
import com.duckduckgo.privacyprotectionspopup.api.PrivacyProtectionsToggleUsageListener
import com.nhaarman.mockitokotlin2.mock
import com.squareup.moshi.Moshi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class PrivacyDashboardHybridViewModelTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val androidQAppBuildConfig = mock<AppBuildConfig>().apply {
        whenever(this.sdkInt).thenReturn(VERSION_CODES.Q)
    }

    private val userAllowListRepository = FakeUserAllowListRepository()

    private val contentBlocking = mock<ContentBlocking>()
    private val unprotectedTemporary = mock<UnprotectedTemporary>()
    private val mockUserBrowserProperties: UserBrowserProperties = mock()

    private val pixel = mock<Pixel>()
    private val privacyProtectionsToggleUsageListener: PrivacyProtectionsToggleUsageListener = mock()
    private val privacyProtectionsPopupExperimentExternalPixels: PrivacyProtectionsPopupExperimentExternalPixels = mock {
        runBlocking { whenever(mock.getPixelParams()).thenReturn(emptyMap()) }
    }

    private var webBrokenSiteFormFeatureEnabled = false

    private val webBrokenSiteFormFeature: WebBrokenSiteFormFeature = mock {
        whenever(this.mock.self()).thenReturn(
            object : Toggle {
                override fun isEnabled(): Boolean = webBrokenSiteFormFeatureEnabled
                override fun setEnabled(state: State) = throw UnsupportedOperationException()
                override fun getRawStoredState(): State? = throw UnsupportedOperationException()
            },
        )
    }

    private val brokenSiteSender: BrokenSiteSender = mock()

    private val testee: PrivacyDashboardHybridViewModel by lazy {
        PrivacyDashboardHybridViewModel(
            userAllowListRepository = userAllowListRepository,
            pixel = pixel,
            dispatcher = coroutineRule.testDispatcherProvider,
            siteViewStateMapper = AppSiteViewStateMapper(PublicKeyInfoMapper(androidQAppBuildConfig)),
            requestDataViewStateMapper = AppSiteRequestDataViewStateMapper(),
            protectionStatusViewStateMapper = AppProtectionStatusViewStateMapper(contentBlocking, unprotectedTemporary),
            privacyDashboardPayloadAdapter = AppPrivacyDashboardPayloadAdapter(moshi = JsonModule.moshi(Moshi.Builder().build())),
            autoconsentStatusViewStateMapper = CookiePromptManagementStatusViewStateMapper(),
            protectionsToggleUsageListener = privacyProtectionsToggleUsageListener,
            privacyProtectionsPopupExperimentExternalPixels = privacyProtectionsPopupExperimentExternalPixels,
            userBrowserProperties = mockUserBrowserProperties,
            webBrokenSiteFormFeature = webBrokenSiteFormFeature,
            brokenSiteSender = brokenSiteSender,
            moshi = Moshi.Builder().build(),
        )
    }

    @Test
    fun whenUserClicksOnReportBrokenSiteThenCommandEmitted() = runTest {
        webBrokenSiteFormFeatureEnabled = false

        testee.onReportBrokenSiteSelected()

        testee.commands().test {
            val command = awaitItem()
            Assert.assertTrue(command is LaunchReportBrokenSite)
        }
    }

    @Test
    fun whenUserClicksOnReportBrokenSiteAndWebFormEnabledThenCommandIsNotEmitted() = runTest {
        webBrokenSiteFormFeatureEnabled = true

        testee.onReportBrokenSiteSelected()

        testee.commands().test {
            expectNoEvents()
        }
    }

    @Test
    fun whenSiteChangesThenViewStateUpdates() = runTest {
        testee.onSiteChanged(site())

        testee.viewState.test {
            val viewState = awaitItem()
            assertNotNull(viewState)
            assertEquals("https://example.com", viewState!!.siteViewState.url)
        }
    }

    @Test
    fun whenOnPrivacyProtectionClickedThenUpdateViewState() = runTest {
        testee.onSiteChanged(site(siteAllowed = false))
        testee.onPrivacyProtectionsClicked(enabled = false)

        testee.viewState.test {
            awaitItem()
            val viewState = awaitItem()
            assertTrue(viewState!!.userChangedValues)
            assertFalse(viewState.protectionStatus.allowlisted)
            verify(pixel).fire(PRIVACY_DASHBOARD_ALLOWLIST_ADD)
        }
    }

    @Test
    fun whenOnPrivacyProtectionClickedThenValueStoredInStore() = runTest {
        val site = site(siteAllowed = false)
        testee.onSiteChanged(site)

        userAllowListRepository.domainsInUserAllowListFlow()
            .test {
                assertFalse(site.domain in awaitItem())
                testee.onPrivacyProtectionsClicked(enabled = false)
                assertTrue(site.domain in awaitItem())
            }
    }

    @Test
    fun whenAllowlistIsChangedThenViewStateIsUpdated() = runTest {
        val site = site(siteAllowed = false)
        testee.onSiteChanged(site)

        testee.viewState.filterNotNull().test {
            assertFalse(awaitItem().userChangedValues)
            userAllowListRepository.addDomainToUserAllowList(site.domain!!)
            assertTrue(awaitItem().userChangedValues)
        }
    }

    @Test
    fun whenOnPrivacyProtectionClickedThenListenerIsNotified() = runTest {
        val site = site(siteAllowed = false)
        testee.onSiteChanged(site)

        testee.onPrivacyProtectionsClicked(enabled = false)

        verify(privacyProtectionsToggleUsageListener).onPrivacyProtectionsToggleUsed()
    }

    @Test
    fun whenPrivacyProtectionsPopupExperimentParamsArePresentThenTheyShouldBeIncludedInPixels() = runTest {
        val params = mapOf("test_key" to "test_value")
        whenever(privacyProtectionsPopupExperimentExternalPixels.getPixelParams()).thenReturn(params)
        val site = site(siteAllowed = false)
        testee.onSiteChanged(site)
        testee.onPrivacyProtectionsClicked(enabled = false)
        testee.onPrivacyProtectionsClicked(enabled = true)
        coroutineRule.testScope.advanceUntilIdle()

        verify(pixel).fire(PRIVACY_DASHBOARD_OPENED, params, type = COUNT)
        verify(privacyProtectionsPopupExperimentExternalPixels).tryReportPrivacyDashboardOpened()
        verify(pixel).fire(PRIVACY_DASHBOARD_ALLOWLIST_ADD, params, type = COUNT)
        verify(privacyProtectionsPopupExperimentExternalPixels).tryReportProtectionsToggledFromPrivacyDashboard(protectionsEnabled = false)
        verify(pixel).fire(PRIVACY_DASHBOARD_ALLOWLIST_REMOVE, params, type = COUNT)
        verify(privacyProtectionsPopupExperimentExternalPixels).tryReportProtectionsToggledFromPrivacyDashboard(protectionsEnabled = true)
    }

    @Test
    fun whenOnPrivacyProtectionClickedAndProtectionsEnabledAndOpenedFromCustomTabThenFireCustomTabSpecificPixel() = runTest {
        val site = site(siteAllowed = false)
        testee.onSiteChanged(site)
        testee.onPrivacyProtectionsClicked(enabled = true, dashboardOpenedFromCustomTab = true)
        coroutineRule.testScope.advanceUntilIdle()
        verify(pixel).fire(PrivacyDashboardCustomTabPixelNames.CUSTOM_TABS_PRIVACY_DASHBOARD_ALLOW_LIST_REMOVE)
    }

    @Test
    fun whenOnPrivacyProtectionClickedAndProtectionsDisabledAndOpenedFromCustomTabThenFireCustomTabSpecificPixel() = runTest {
        val site = site(siteAllowed = false)
        testee.onSiteChanged(site)
        testee.onPrivacyProtectionsClicked(enabled = false, dashboardOpenedFromCustomTab = true)
        coroutineRule.testScope.advanceUntilIdle()
        verify(pixel).fire(PrivacyDashboardCustomTabPixelNames.CUSTOM_TABS_PRIVACY_DASHBOARD_ALLOW_LIST_ADD)
    }

    @Test
    fun whenUserClicksOnSubmitReportThenSubmitsReport() = runTest {
        webBrokenSiteFormFeatureEnabled = true

        val siteUrl = "https://example.com"
        val userRefreshCount = 2
        val jsPerformance = doubleArrayOf(1.0, 2.0, 3.0)

        val site: Site = mock { site ->
            whenever(site.uri).thenReturn(siteUrl.toUri())
            whenever(site.url).thenReturn(siteUrl)
            whenever(site.userAllowList).thenReturn(true)
            whenever(site.isDesktopMode).thenReturn(false)
            whenever(site.upgradedHttps).thenReturn(true)
            whenever(site.consentManaged).thenReturn(true)
            whenever(site.errorCodeEvents).thenReturn(listOf("401", "401", "500"))

            val brokenSiteContext: BrokenSiteContext = mock { brokenSiteContext ->
                whenever(brokenSiteContext.userRefreshCount).thenReturn(userRefreshCount)
                whenever(brokenSiteContext.jsPerformance).thenReturn(jsPerformance)
            }
            whenever(site.realBrokenSiteContext).thenReturn(brokenSiteContext)
        }

        testee.onSiteChanged(site)

        val category = "login"
        val description = "I can't sign in!"
        testee.onSubmitBrokenSiteReport(
            payload = """{"category":"$category","description":"$description"}""",
            reportFlow = DASHBOARD,
        )

        val expectedBrokenSite = BrokenSite(
            category = category,
            description = description,
            siteUrl = siteUrl,
            upgradeHttps = true,
            blockedTrackers = "",
            surrogates = "",
            siteType = "mobile",
            urlParametersRemoved = false,
            consentManaged = true,
            consentOptOutFailed = false,
            consentSelfTestFailed = false,
            errorCodes = """["401","401","500"]""",
            httpErrorCodes = "",
            loginSite = null,
            reportFlow = DASHBOARD,
            userRefreshCount = userRefreshCount,
            openerContext = null,
            jsPerformance = jsPerformance.toList(),
        )

        verify(brokenSiteSender).submitBrokenSiteFeedback(expectedBrokenSite)
    }

    @Test
    fun whenUserClicksOnSubmitReportAndSiteUrlIsEmptyThenDoesNotSubmitReport() = runTest {
        webBrokenSiteFormFeatureEnabled = true

        testee.onSiteChanged(site(url = ""))

        val category = "login"
        val description = "I can't sign in!"
        testee.onSubmitBrokenSiteReport(
            payload = """{"category":"$category","description":"$description"}""",
            reportFlow = DASHBOARD,
        )

        verifyNoInteractions(brokenSiteSender)
    }

    @Test
    fun whenUserClicksOnSubmitReportThenCommandIsSent() = runTest {
        webBrokenSiteFormFeatureEnabled = true

        testee.onSiteChanged(site())

        testee.onSubmitBrokenSiteReport(
            payload = """{"category":"login","description":"I can't sign in!"}""",
            reportFlow = DASHBOARD,
        )

        verify(brokenSiteSender).submitBrokenSiteFeedback(any())

        testee.commands().test {
            assertEquals(GoBack, awaitItem())
        }
    }

    private fun site(
        url: String = "https://example.com",
        siteAllowed: Boolean = false,
    ): Site {
        val site: Site = mock()
        whenever(site.uri).thenReturn(url.toUri())
        whenever(site.url).thenReturn(url)
        whenever(site.userAllowList).thenReturn(siteAllowed)
        whenever(site.realBrokenSiteContext).thenReturn(mock())
        return site
    }
}

private class FakeUserAllowListRepository : UserAllowListRepository {

    private val domains = MutableStateFlow<List<String>>(emptyList())

    override fun isUrlInUserAllowList(url: String): Boolean = throw UnsupportedOperationException()

    override fun isUriInUserAllowList(uri: Uri): Boolean = throw UnsupportedOperationException()

    override fun isDomainInUserAllowList(domain: String?): Boolean = domain in domains.value

    override fun domainsInUserAllowList(): List<String> = domains.value

    override fun domainsInUserAllowListFlow(): Flow<List<String>> = domains

    override suspend fun addDomainToUserAllowList(domain: String) = domains.update { it + domain }

    override suspend fun removeDomainFromUserAllowList(domain: String) = domains.update { it - domain }
}
