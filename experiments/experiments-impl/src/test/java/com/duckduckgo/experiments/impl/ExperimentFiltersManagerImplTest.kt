

package com.duckduckgo.experiments.impl

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.experiments.api.VariantConfig
import com.duckduckgo.experiments.api.VariantFilters
import java.util.Locale
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ExperimentFiltersManagerImplTest {

    private lateinit var testee: ExperimentFiltersManager

    private val mockAppBuildConfig: AppBuildConfig = mock()

    @Before
    fun setup() {
        testee = ExperimentFiltersManagerImpl(
            mockAppBuildConfig,
        )
    }

    @Test
    fun whenVariantComplyWithLocaleFilterThenAddFiltersReturnsTrue() {
        val locale = Locale("en", "US")
        Locale.setDefault(locale)
        val testEntity = addActiveVariant(localeFilter = listOf("en_US"))

        assertTrue(testee.addFilters(testEntity).invoke(mockAppBuildConfig))
    }

    @Test
    fun whenVariantDoesNotComplyWithLocaleFilterThenAddFiltersReturnsFalse() {
        val locale = Locale("en", "US")
        Locale.setDefault(locale)
        val testEntity = addActiveVariant(localeFilter = listOf("de_DE"))

        assertFalse(testee.addFilters(testEntity).invoke(mockAppBuildConfig))
    }

    @Test
    fun whenVariantComplyWithAndroidVersionFilterThenAddFiltersReturnsTrue() {
        whenever(mockAppBuildConfig.sdkInt).thenReturn(33)
        val testEntity = addActiveVariant(androidVersionFilter = listOf("33", "34"))

        assertTrue(testee.addFilters(testEntity).invoke(mockAppBuildConfig))
    }

    @Test
    fun whenVariantDoesNotComplyWithAndroidVersionFilterThenAddFiltersReturnsFalse() {
        whenever(mockAppBuildConfig.sdkInt).thenReturn(32)
        val testEntity = addActiveVariant(androidVersionFilter = listOf("33", "34"))

        assertFalse(testee.addFilters(testEntity).invoke(mockAppBuildConfig))
    }

    @Test
    fun whenVariantComplyWithBothFiltersThenAddFiltersReturnsTrue() {
        val locale = Locale("en", "US")
        Locale.setDefault(locale)
        whenever(mockAppBuildConfig.sdkInt).thenReturn(33)
        val testEntity = addActiveVariant(localeFilter = listOf("en_US"), androidVersionFilter = listOf("33", "34"))

        assertTrue(testee.addFilters(testEntity).invoke(mockAppBuildConfig))
    }

    @Test
    fun whenVariantComplyWithLocaleFiltersAndDoesNotComplyWithAndroidVersionFilterThenAddFiltersReturnsFalse() {
        val locale = Locale("en", "US")
        Locale.setDefault(locale)
        whenever(mockAppBuildConfig.sdkInt).thenReturn(32)
        val testEntity = addActiveVariant(localeFilter = listOf("en_US"), androidVersionFilter = listOf("33", "34"))

        assertFalse(testee.addFilters(testEntity).invoke(mockAppBuildConfig))
    }

    @Test
    fun whenVariantComplyWithAndroidVersionFiltersAndDoesNotComplyWithLocaleFilterThenAddFiltersReturnsFalse() {
        val locale = Locale("en", "US")
        Locale.setDefault(locale)
        whenever(mockAppBuildConfig.sdkInt).thenReturn(33)
        val testEntity = addActiveVariant(localeFilter = listOf("de_DE"), androidVersionFilter = listOf("33", "34"))

        assertFalse(testee.addFilters(testEntity).invoke(mockAppBuildConfig))
    }

    private fun addActiveVariant(
        localeFilter: List<String> = listOf(),
        androidVersionFilter: List<String> = listOf(),
    ): VariantConfig {
        return VariantConfig("key", 1.0, VariantFilters(localeFilter, androidVersionFilter))
    }
}
