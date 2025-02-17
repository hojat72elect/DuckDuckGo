

package com.duckduckgo.app.statistics

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.lifecycle.MainProcessLifecycleObserver
import com.duckduckgo.app.statistics.api.StatisticsUpdater
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.DaggerSet
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.privacy.config.api.PrivacyConfigCallbackPlugin
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

interface AtbInitializerListener {

    /** This method will be called before initializing the ATB */
    suspend fun beforeAtbInit()

    /** @return the timeout in milliseconds after which [beforeAtbInit] will be stopped */
    fun beforeAtbInitTimeoutMillis(): Long
}

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = MainProcessLifecycleObserver::class,
)
@ContributesMultibinding(
    scope = AppScope::class,
    boundType = PrivacyConfigCallbackPlugin::class,
)
@SingleInstanceIn(AppScope::class)
class AtbInitializer @Inject constructor(
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
    private val statisticsDataStore: StatisticsDataStore,
    private val statisticsUpdater: StatisticsUpdater,
    private val listeners: DaggerSet<AtbInitializerListener>,
    private val dispatcherProvider: DispatcherProvider,
) : MainProcessLifecycleObserver, PrivacyConfigCallbackPlugin {

    override fun onResume(owner: LifecycleOwner) {
        appCoroutineScope.launch(dispatcherProvider.io()) { initialize() }
    }

    @VisibleForTesting
    suspend fun initialize() {
        Timber.v("Initialize ATB")
        listeners.forEach {
            withTimeoutOrNull(it.beforeAtbInitTimeoutMillis()) { it.beforeAtbInit() }
        }

        initializeAtb()
    }

    private fun initializeAtb() {
        if (statisticsDataStore.hasInstallationStatistics) {
            statisticsUpdater.refreshAppRetentionAtb()
        }
    }

    override fun onPrivacyConfigDownloaded() {
        if (!statisticsDataStore.hasInstallationStatistics) {
            // First time we initializeAtb
            statisticsUpdater.initializeAtb()
        }
    }
}
