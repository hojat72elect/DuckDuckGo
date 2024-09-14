

package com.duckduckgo.app.browser.favicon

import android.content.Context
import com.duckduckgo.app.fire.fireproofwebsite.data.FireproofWebsiteRepository
import com.duckduckgo.app.location.data.LocationPermissionsRepository
import com.duckduckgo.autofill.api.store.AutofillStore
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.savedsites.api.SavedSitesRepository
import com.duckduckgo.savedsites.store.SavedSitesEntitiesDao
import com.duckduckgo.sync.api.favicons.FaviconsFetchingStore
import dagger.Module
import dagger.Provides
import dagger.SingleInstanceIn

@Module
class FaviconModule {

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun faviconManager(
        faviconPersister: FaviconPersister,
        bookmarksDao: SavedSitesEntitiesDao,
        fireproofWebsiteRepository: FireproofWebsiteRepository,
        locationPermissionsRepository: LocationPermissionsRepository,
        savedSitesRepository: SavedSitesRepository,
        faviconDownloader: FaviconDownloader,
        dispatcherProvider: DispatcherProvider,
        autofillStore: AutofillStore,
        faviconsFetchingStore: FaviconsFetchingStore,
        context: Context,
    ): FaviconManager {
        return DuckDuckGoFaviconManager(
            faviconPersister,
            bookmarksDao,
            fireproofWebsiteRepository,
            locationPermissionsRepository,
            savedSitesRepository,
            faviconDownloader,
            dispatcherProvider,
            autofillStore,
            faviconsFetchingStore,
            context,
        )
    }

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun faviconDownloader(
        context: Context,
        dispatcherProvider: DispatcherProvider,
    ): FaviconDownloader {
        return GlideFaviconDownloader(context, dispatcherProvider)
    }
}
