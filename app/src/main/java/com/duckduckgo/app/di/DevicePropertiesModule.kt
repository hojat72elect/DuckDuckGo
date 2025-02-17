

package com.duckduckgo.app.di

import android.content.Context
import com.duckduckgo.app.global.install.AppInstallStore
import com.duckduckgo.app.global.store.AndroidAppProperties
import com.duckduckgo.app.global.store.AndroidUserBrowserProperties
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.app.usage.app.AppDaysUsedRepository
import com.duckduckgo.app.usage.search.SearchCountDao
import com.duckduckgo.app.widget.ui.WidgetCapabilities
import com.duckduckgo.autofill.api.email.EmailManager
import com.duckduckgo.browser.api.AppProperties
import com.duckduckgo.browser.api.UserBrowserProperties
import com.duckduckgo.common.ui.store.ThemingDataStore
import com.duckduckgo.common.utils.playstore.PlayStoreUtils
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.experiments.api.VariantManager
import com.duckduckgo.savedsites.api.SavedSitesRepository
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.SingleInstanceIn

@Module
@ContributesTo(AppScope::class)
object DevicePropertiesModule {

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun providesAppProperties(
        appContext: Context,
        variantManager: VariantManager,
        playStoreUtils: PlayStoreUtils,
        statisticsStore: StatisticsDataStore,
    ): AppProperties {
        return AndroidAppProperties(
            appContext,
            variantManager,
            playStoreUtils,
            statisticsStore,
        )
    }

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun providesUserBrowserProperties(
        themingDataStore: ThemingDataStore,
        savedSitesRepository: SavedSitesRepository,
        appInstallStore: AppInstallStore,
        widgetCapabilities: WidgetCapabilities,
        emailManager: EmailManager,
        searchCountDao: SearchCountDao,
        appDaysUsedRepository: AppDaysUsedRepository,
    ): UserBrowserProperties {
        return AndroidUserBrowserProperties(
            themingDataStore,
            savedSitesRepository,
            appInstallStore,
            widgetCapabilities,
            emailManager,
            searchCountDao,
            appDaysUsedRepository,
        )
    }
}
