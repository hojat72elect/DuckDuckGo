package com.duckduckgo.privacyprotectionspopup.impl

import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.COUNT
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.DAILY
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.UNIQUE
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class PrivacyProtectionsPopupPixelNameTest(
    private val pixel: PrivacyProtectionsPopupPixelName,
) {

    @Test
    fun pixelNameShouldHaveCorrectPrefix() {
        val pixelName = pixel.pixelName
        val requiredPrefix = "m_privacy_protections_popup_"
        assertTrue(
            "Pixel name should start with '$requiredPrefix': $pixelName",
            pixelName.startsWith(requiredPrefix),
        )
    }

    @Test
    fun pixelNameSuffixShouldMatchPixelType() {
        val pixelName = pixel.pixelName
        val requiredSuffix = when (pixel.type) {
            COUNT -> "_c"
            DAILY -> "_d"
            UNIQUE -> "_u"
        }
        assertTrue(
            "Pixel name should end with '$requiredSuffix': $pixelName",
            pixelName.endsWith(requiredSuffix),
        )
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun data(): Collection<Array<PrivacyProtectionsPopupPixelName>> =
            PrivacyProtectionsPopupPixelName.entries.map { arrayOf(it) }
    }
}
