package com.duckduckgo.autofill.impl.reporting.remoteconfig

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.di.IsMainProcess
import com.duckduckgo.autofill.store.reporting.ALL_MIGRATIONS
import com.duckduckgo.autofill.store.reporting.AutofillSiteBreakageReportingDatabase
import com.duckduckgo.autofill.store.reporting.AutofillSiteBreakageReportingFeatureRepository
import com.duckduckgo.autofill.store.reporting.AutofillSiteBreakageReportingFeatureRepositoryImpl
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.SingleInstanceIn
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineScope

@Module
@ContributesTo(AppScope::class)
class AutofillSiteBreakageReportingModule {

    @SingleInstanceIn(AppScope::class)
    @Provides
    fun repository(
        database: AutofillSiteBreakageReportingDatabase,
        @AppCoroutineScope appCoroutineScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider,
        @IsMainProcess isMainProcess: Boolean,
    ): AutofillSiteBreakageReportingFeatureRepository {
        return AutofillSiteBreakageReportingFeatureRepositoryImpl(
            database,
            appCoroutineScope,
            dispatcherProvider,
            isMainProcess
        )
    }

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun database(context: Context): AutofillSiteBreakageReportingDatabase {
        return Room.databaseBuilder(
            context,
            AutofillSiteBreakageReportingDatabase::class.java,
            "autofillSiteBreakageReporting.db"
        )
            .fallbackToDestructiveMigration()
            .addMigrations(*ALL_MIGRATIONS)
            .build()
    }

    private val Context.autofillSiteBreakageReportingDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "autofill_site_breakage_reporting",
    )

    @Provides
    @SingleInstanceIn(AppScope::class)
    @AutofillSiteBreakageReporting
    fun provideImportPasswordsDesktopSyncDataStore(context: Context): DataStore<Preferences> {
        return context.autofillSiteBreakageReportingDataStore
    }
}

@Qualifier
annotation class AutofillSiteBreakageReporting
