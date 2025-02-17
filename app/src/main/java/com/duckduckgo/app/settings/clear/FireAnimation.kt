

package com.duckduckgo.app.settings.clear

import com.duckduckgo.app.browser.R
import com.duckduckgo.app.settings.clear.FireAnimation.HeroAbstract
import com.duckduckgo.app.settings.clear.FireAnimation.HeroFire
import com.duckduckgo.app.settings.clear.FireAnimation.HeroWater
import com.duckduckgo.app.settings.clear.FireAnimation.None
import com.duckduckgo.app.statistics.pixels.Pixel
import java.io.Serializable

sealed class FireAnimation(
    val resId: Int,
    val nameResId: Int,
) : Serializable {
    object HeroFire : FireAnimation(R.raw.hero_fire_inferno, R.string.settingsHeroFireAnimation)
    object HeroWater : FireAnimation(R.raw.hero_water_whirlpool, R.string.settingsHeroWaterAnimation)
    object HeroAbstract : FireAnimation(R.raw.hero_abstract_airstream, R.string.settingsHeroAbstractAnimation)
    object None : FireAnimation(-1, R.string.settingsNoneAnimation)

    fun getOptionIndex(): Int {
        return when (this) {
            HeroFire -> 1
            HeroWater -> 2
            HeroAbstract -> 3
            None -> 4
        }
    }

    fun Int.getAnimationForIndex(): FireAnimation {
        return when (this) {
            2 -> HeroWater
            3 -> HeroAbstract
            4 -> None
            else -> HeroFire
        }
    }
}

fun FireAnimation.getPixelValue() = when (this) {
    HeroFire -> Pixel.PixelValues.FIRE_ANIMATION_INFERNO
    HeroWater -> Pixel.PixelValues.FIRE_ANIMATION_WHIRLPOOL
    HeroAbstract -> Pixel.PixelValues.FIRE_ANIMATION_AIRSTREAM
    None -> Pixel.PixelValues.FIRE_ANIMATION_NONE
}
