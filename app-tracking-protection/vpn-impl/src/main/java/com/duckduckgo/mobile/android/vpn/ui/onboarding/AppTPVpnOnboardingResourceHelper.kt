

package com.duckduckgo.mobile.android.vpn.ui.onboarding

import com.duckduckgo.common.ui.store.AppTheme
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.R
import com.duckduckgo.mobile.android.vpn.ui.onboarding.AppThemeAppTPOnboardingResourceHelper.AppTPOnboadingResource
import com.duckduckgo.mobile.android.vpn.ui.onboarding.AppThemeAppTPOnboardingResourceHelper.AppTPOnboadingResource.TRACKERS_COUNT
import com.duckduckgo.mobile.android.vpn.ui.onboarding.AppThemeAppTPOnboardingResourceHelper.AppTPOnboadingResource.TRACKING_APPS
import com.duckduckgo.mobile.android.vpn.ui.onboarding.AppThemeAppTPOnboardingResourceHelper.AppTPOnboadingResource.VPN
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface AppTPOnboardingResourceHelper {
    fun getHeaderRes(resourceType: AppTPOnboadingResource): Int
}

// TODO: Remove this class when we can rely on the platform day/night/system theme
@ContributesBinding(AppScope::class)
class AppThemeAppTPOnboardingResourceHelper @Inject constructor(val appTheme: AppTheme) : AppTPOnboardingResourceHelper {

    override fun getHeaderRes(
        resourceType: AppTPOnboadingResource,
    ): Int {
        return when (resourceType) {
            TRACKERS_COUNT -> {
                if (appTheme.isLightModeEnabled()) {
                    R.raw.device_shield_tracker_count
                } else {
                    R.raw.device_shield_tracker_count_dark
                }
            }
            TRACKING_APPS -> {
                if (appTheme.isLightModeEnabled()) {
                    R.raw.device_shield_tracking_apps
                } else {
                    R.raw.device_shield_tracking_apps_dark
                }
            }
            VPN ->
                if (appTheme.isLightModeEnabled()) {
                    R.drawable.device_shield_onboarding_page_three_header_transparent
                } else {
                    R.drawable.device_shield_onboarding_page_three_header_dark_transparent
                }
        }
    }

    enum class AppTPOnboadingResource {
        TRACKERS_COUNT,
        TRACKING_APPS,
        VPN,
    }
}
