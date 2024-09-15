

package com.duckduckgo.privacy.dashboard.impl.ui

import android.net.Uri
import android.net.http.SslCertificate
import android.net.http.SslCertificate.DName
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.global.model.Site
import com.duckduckgo.app.trackerdetection.model.Entity
import com.duckduckgo.privacy.config.api.ContentBlocking
import com.duckduckgo.privacy.config.api.UnprotectedTemporary
import com.duckduckgo.privacy.dashboard.impl.ui.AppSiteViewStateMapperTest.TestCertificateInfo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock

@RunWith(AndroidJUnit4::class)
internal class AppProtectionStatusViewStateMapperTest {

    private val mockContentBlocking: ContentBlocking = mock()
    private val mockUnprotectedTemporary: UnprotectedTemporary = mock()

    private val testee = AppProtectionStatusViewStateMapper(mockContentBlocking, mockUnprotectedTemporary)

    @Test
    fun whenSiteUrlIsAContentBlockingExceptionThenFeatureNotEnabled() {
        whenever(mockContentBlocking.isAnException(any())).thenReturn(true)

        val viewState = testee.mapFromSite(site())

        assertFalse(viewState.enabledFeatures.contains("contentBlocking"))
    }

    @Test
    fun whenContentBlockingEnabledThenFeatureEnabled() {
        whenever(mockContentBlocking.isAnException(any())).thenReturn(false)

        val viewState = testee.mapFromSite(site())

        assertTrue(viewState.enabledFeatures.contains("contentBlocking"))
    }

    @Test
    fun whenSiteIsInUserAllowListThenAllowListedIsTrue() {
        val viewState = testee.mapFromSite(site(allowListed = true))

        assertTrue(viewState.allowlisted)
    }

    @Test
    fun whenSiteIsUnprotectedTemporaryThenUnprotectedIsTrue() {
        whenever(mockUnprotectedTemporary.isAnException(any())).thenReturn(true)

        val viewState = testee.mapFromSite(site())

        assertTrue(viewState.unprotectedTemporary)
    }

    @Test
    fun whenContentBlockingEnabledThenViewStateProtectionsEnabled() {
        val viewState = testee.mapFromSite(site())

        assertFalse(viewState.allowlisted)
        assertFalse(viewState.denylisted)
        assertTrue(viewState.enabledFeatures.contains("contentBlocking"))
        assertFalse(viewState.unprotectedTemporary)
    }

    private fun site(
        url: String = "http://www.test.com",
        uri: Uri? = Uri.parse(url),
        entity: Entity? = null,
        certificate: TestCertificateInfo? = null,
        allowListed: Boolean? = false,
    ): Site {
        val site: Site = com.nhaarman.mockitokotlin2.mock()
        whenever(site.url).thenReturn(url)
        whenever(site.uri).thenReturn(uri)
        whenever(site.entity).thenReturn(entity)
        whenever(site.userAllowList).thenReturn(allowListed)

        if (certificate != null) {
            val dName = com.nhaarman.mockitokotlin2.mock<DName>().apply {
                whenever(this.cName).thenReturn(certificate.cName)
            }
            val sslCertificate = com.nhaarman.mockitokotlin2.mock<SslCertificate>().apply {
                whenever(issuedTo).thenReturn(dName)
            }
            whenever(site.certificate).thenReturn(sslCertificate)
        }

        return site
    }
}
