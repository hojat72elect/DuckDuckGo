

package com.duckduckgo.app.browser.omnibar.animations

import com.airbnb.lottie.LottieAnimationView
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.global.model.PrivacyShield
import com.duckduckgo.app.global.model.PrivacyShield.PROTECTED
import com.duckduckgo.app.global.model.PrivacyShield.UNKNOWN
import com.duckduckgo.app.global.model.PrivacyShield.UNPROTECTED
import com.duckduckgo.app.global.model.PrivacyShield.WARNING
import com.duckduckgo.common.ui.store.AppTheme
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import timber.log.Timber

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class LottiePrivacyShieldAnimationHelper @Inject constructor(val appTheme: AppTheme) : PrivacyShieldAnimationHelper {

    override fun setAnimationView(
        holder: LottieAnimationView,
        privacyShield: PrivacyShield,
    ) {
        when (privacyShield) {
            PROTECTED -> {
                val res = if (appTheme.isLightModeEnabled()) R.raw.protected_shield else R.raw.dark_protected_shield
                holder.setAnimation(res)
                holder.progress = 0.0f
                Timber.i("Shield: PROTECTED")
            }
            UNPROTECTED -> {
                val res = if (appTheme.isLightModeEnabled()) R.raw.unprotected_shield else R.raw.dark_unprotected_shield
                holder.setAnimation(res)
                holder.progress = 1.0f
                Timber.i("Shield: UNPROTECTED")
            }
            UNKNOWN -> {
                Timber.i("Shield: UNKNOWN")
            }
            WARNING -> {
                val res = if (appTheme.isLightModeEnabled()) R.raw.unprotected_shield else R.raw.dark_unprotected_shield
                holder.setAnimation(res)
                holder.progress = 1.0f
                Timber.i("Shield: WARNING")
            }
        }
    }
}
