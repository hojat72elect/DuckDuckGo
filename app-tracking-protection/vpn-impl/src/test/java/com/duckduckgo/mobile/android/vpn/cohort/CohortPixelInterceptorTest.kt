

package com.duckduckgo.mobile.android.vpn.cohort

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.appbuildconfig.api.BuildFlavor
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.common.test.api.FakeChain
import com.duckduckgo.common.test.api.InMemorySharedPreferences
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import com.duckduckgo.mobile.android.vpn.VpnFeaturesRegistry
import java.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CohortPixelInterceptorTest {
    @get:Rule
    @Suppress("unused")
    val coroutineRule = CoroutineTestRule()

    @Mock
    private lateinit var vpnFeaturesRegistry: VpnFeaturesRegistry

    @Mock
    private lateinit var appBuildConfig: AppBuildConfig

    private lateinit var cohortPixelInterceptor: CohortPixelInterceptor
    private lateinit var cohortStore: CohortStore
    private lateinit var cohortCalculator: CohortCalculator

    private val sharedPreferencesProvider = mock<com.duckduckgo.data.store.api.SharedPreferencesProvider>()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        val prefs = InMemorySharedPreferences()
        whenever(
            sharedPreferencesProvider.getSharedPreferences(eq("com.duckduckgo.mobile.atp.cohort.prefs"), eq(true), eq(true)),
        ).thenReturn(prefs)

        whenever(appBuildConfig.flavor).thenReturn(BuildFlavor.PLAY)

        cohortStore = RealCohortStore(sharedPreferencesProvider, vpnFeaturesRegistry, coroutineRule.testDispatcherProvider, appBuildConfig)
        cohortCalculator = RealCohortCalculator()
        cohortPixelInterceptor = CohortPixelInterceptor(cohortCalculator, cohortStore)
    }

    @Test
    fun whenCohortNotSetPixelDropped() {
        Assert.assertNull(cohortStore.getCohortStoredLocalDate())

        val pixelUrl = String.format(PIXEL_TEMPLATE, "m_atp_ev_restart_c")
        val result = cohortPixelInterceptor.intercept(FakeChain(pixelUrl))

        Assert.assertEquals("Dropped ATP pixel because no cohort is assigned", result.message)
        Assert.assertNotEquals(null, result.body)
    }

    @Test
    fun whenCohortSetPixelFired() {
        val date = LocalDate.now().plusDays(3)
        cohortStore.setCohortLocalDate(date)

        val pixelUrl = String.format(PIXEL_TEMPLATE, "m_atp_ev_restart_c")
        val result = cohortPixelInterceptor.intercept(FakeChain(pixelUrl))
        val resultUrl = result.request.url

        Assert.assertEquals(cohortCalculator.calculateCohortForDate(date), resultUrl.queryParameter(CohortPixelInterceptor.COHORT_PARAM))
        Assert.assertEquals("", result.message)
        Assert.assertEquals(null, result.body)
    }

    @Test
    fun whenCohortSetPixelFiredCohortRemovedForException() {
        val date = LocalDate.now().plusDays(3)
        cohortStore.setCohortLocalDate(date)

        val pixelUrl = String.format(PIXEL_TEMPLATE, "m_atp_ev_cpu_usage_above_10")
        val result = cohortPixelInterceptor.intercept(FakeChain(pixelUrl))
        val resultUrl = result.request.url

        Assert.assertEquals(null, resultUrl.queryParameter(CohortPixelInterceptor.COHORT_PARAM))
        Assert.assertEquals("", result.message)
        Assert.assertEquals(null, result.body)
    }

    companion object {
        private const val PIXEL_TEMPLATE = "https://improving.duckduckgo.com/t/%s_android_phone?appVersion=5.135.0&test=1"
    }
}
