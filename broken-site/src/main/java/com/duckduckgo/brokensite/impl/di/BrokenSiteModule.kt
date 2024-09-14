

package com.duckduckgo.brokensite.impl.di

import android.content.Context
import androidx.room.Room
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.brokensite.impl.BrokenSiteReportRepository
import com.duckduckgo.brokensite.impl.RealBrokenSiteReportRepository
import com.duckduckgo.brokensite.store.ALL_MIGRATIONS
import com.duckduckgo.brokensite.store.BrokenSiteDatabase
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.Provides
import dagger.SingleInstanceIn
import kotlinx.coroutines.CoroutineScope

@Module
@ContributesTo(AppScope::class)
class BrokenSiteModule {

    @SingleInstanceIn(AppScope::class)
    @Provides
    fun provideBrokenSiteReportRepository(
        database: BrokenSiteDatabase,
        @AppCoroutineScope coroutineScope: CoroutineScope,
        dispatcherProvider: DispatcherProvider,
    ): BrokenSiteReportRepository {
        return RealBrokenSiteReportRepository(database, coroutineScope, dispatcherProvider)
    }

    @Provides
    @SingleInstanceIn(AppScope::class)
    fun provideBrokenSiteDatabase(context: Context): BrokenSiteDatabase {
        return Room.databaseBuilder(context, BrokenSiteDatabase::class.java, "broken_site.db")
            .fallbackToDestructiveMigration()
            .addMigrations(*ALL_MIGRATIONS)
            .build()
    }
}
