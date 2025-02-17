

package com.duckduckgo.mobile.android.vpn.di

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import androidx.room.Room
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.mobile.android.vpn.Vpn
import com.duckduckgo.mobile.android.vpn.VpnFeaturesRegistry
import com.duckduckgo.mobile.android.vpn.VpnFeaturesRegistryImpl
import com.duckduckgo.mobile.android.vpn.VpnServiceWrapper
import com.duckduckgo.mobile.android.vpn.stats.AppTrackerBlockingStatsRepository
import com.duckduckgo.mobile.android.vpn.stats.RealAppTrackerBlockingStatsRepository
import com.duckduckgo.mobile.android.vpn.store.*
import com.duckduckgo.mobile.android.vpn.trackers.AppTrackerRepository
import com.duckduckgo.mobile.android.vpn.trackers.RealAppTrackerRepository
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.SingleInstanceIn
import javax.inject.Provider

@Module
@ContributesTo(AppScope::class)
object VpnAppModule {

    @SingleInstanceIn(AppScope::class)
    @Provides
    fun providesConnectivityManager(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    @Provides
    fun provideVpnDatabaseCallbackProvider(
        context: Context,
        vpnDatabase: Provider<VpnDatabase>,
    ): VpnDatabaseCallbackProvider {
        return VpnDatabaseCallbackProvider(context, vpnDatabase)
    }

    /**
     * TODO this class should also not be needed in the AppScope.
     * It is needed because the DaggerWorkerFactory is not modular. Easy to fix tho
     */
    @SingleInstanceIn(AppScope::class)
    @Provides
    fun bindVpnDatabase(
        context: Context,
        vpnDatabaseCallbackProvider: VpnDatabaseCallbackProvider,
    ): VpnDatabase {
        return Room.databaseBuilder(context, VpnDatabase::class.java, "vpn.db")
            .enableMultiInstanceInvalidation()
            .fallbackToDestructiveMigrationFrom(*IntRange(1, 17).toList().toIntArray())
            .addMigrations(*VpnDatabase.ALL_MIGRATIONS.toTypedArray())
            .addCallback(vpnDatabaseCallbackProvider.provideCallbacks())
            .build()
    }

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun provideAppTrackerLoader(
        vpnDatabase: VpnDatabase,
    ): AppTrackerRepository {
        return RealAppTrackerRepository(vpnDatabase.vpnAppTrackerBlockingDao(), vpnDatabase.vpnSystemAppsOverridesDao())
    }

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun providesResources(context: Context): Resources {
        return context.resources
    }

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun provideVpnFeaturesRegistry(
        context: Context,
        sharedPreferencesProvider: com.duckduckgo.data.store.api.SharedPreferencesProvider,
        dispatcherProvider: DispatcherProvider,
    ): VpnFeaturesRegistry {
        return VpnFeaturesRegistryImpl(VpnServiceWrapper(context, dispatcherProvider), sharedPreferencesProvider, dispatcherProvider)
    }

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun provideVpnServiceWrapper(
        context: Context,
        dispatcherProvider: DispatcherProvider,
    ): Vpn {
        return VpnServiceWrapper(context, dispatcherProvider)
    }

    @Provides
    fun provideAppTrackerBlockingStatsRepository(
        vpnDatabase: VpnDatabase,
        dispatchers: DispatcherProvider,
    ): AppTrackerBlockingStatsRepository {
        return RealAppTrackerBlockingStatsRepository(vpnDatabase, dispatchers)
    }
}
