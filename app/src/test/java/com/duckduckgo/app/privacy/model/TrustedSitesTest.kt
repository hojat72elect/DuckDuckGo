

package com.duckduckgo.app.privacy.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrustedSitesTest {

    @Test
    fun whenSiteIsInTrustedListThenIsTrustedIsTrue() {
        assertTrue(TrustedSites.isTrusted("https://www.duckduckgo.com"))
    }

    @Test
    fun whenSiteIsNotInTrustedListThenIsTrustedIsFalse() {
        assertFalse(TrustedSites.isTrusted("https://www.example.com"))
    }
}
