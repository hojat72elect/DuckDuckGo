

package com.duckduckgo.httpsupgrade.impl

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.feature.toggles.api.FeatureToggle
import com.duckduckgo.httpsupgrade.api.HttpsUpgrader
import com.duckduckgo.httpsupgrade.store.HttpsFalsePositivesDao
import com.duckduckgo.privacy.config.api.Https
import com.duckduckgo.privacy.config.api.PrivacyFeatureName
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class HttpsUpgraderTest {

    lateinit var testee: HttpsUpgrader

    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    private var mockHttpsBloomFilterFactory: HttpsBloomFilterFactory = mock()
    private var mockBloomFalsePositiveListDao: HttpsFalsePositivesDao = mock()

    private var mockFeatureToggle: FeatureToggle = mock()
    private var mockHttps: Https = mock()
    private var bloomFilter = BloomFilter(context, BloomFilter.Config.ProbabilityConfig(100, 0.01))

    @Before
    fun before() {
        whenever(mockHttpsBloomFilterFactory.create()).thenReturn(bloomFilter)
        whenever(mockFeatureToggle.isFeatureEnabled(PrivacyFeatureName.HttpsFeatureName.value)).thenReturn(true)
        testee = HttpsUpgraderImpl(mockHttpsBloomFilterFactory, mockBloomFalsePositiveListDao, mockFeatureToggle, mockHttps)
        testee.reloadData()
    }

    @Test
    fun whenFeatureIsDisableTheShouldNotUpgrade() {
        whenever(mockFeatureToggle.isFeatureEnabled(PrivacyFeatureName.HttpsFeatureName.value)).thenReturn(false)
        bloomFilter.add("www.local.url")
        assertFalse(testee.shouldUpgrade(Uri.parse("http://www.local.url")))
    }

    @Test
    fun whenHttpUrlIsInExceptionListThenShouldNotUpgrade() {
        whenever(mockHttps.isAnException("http://www.local.url")).thenReturn(true)
        bloomFilter.add("www.local.url")
        assertFalse(testee.shouldUpgrade(Uri.parse("http://www.local.url")))
    }

    @Test
    fun whenHttpUriIsInBloomThenShouldUpgrade() {
        bloomFilter.add("www.local.url")
        assertTrue(testee.shouldUpgrade(Uri.parse("http://www.local.url")))
    }

    @Test
    fun whenHttpUriIsNotInBloomThenShouldNotUpgrade() {
        bloomFilter.add("www.local.url")
        assertFalse(testee.shouldUpgrade(Uri.parse("http://www.differentlocal.url")))
    }

    @Test
    fun whenHttpsUriThenShouldNotUpgrade() {
        bloomFilter.add("www.local.url")
        assertFalse(testee.shouldUpgrade(Uri.parse("https://www.local.url")))
    }

    @Test
    fun whenHttpUriHasOnlyPartDomainInLocalListThenShouldNotUpgrade() {
        bloomFilter.add("local.url")
        assertFalse(testee.shouldUpgrade(Uri.parse("http://www.local.url")))
    }
}
