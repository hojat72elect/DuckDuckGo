

package com.duckduckgo.sync.impl.pixels

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.duckduckgo.anvil.annotations.ContributesWorker
import com.duckduckgo.app.lifecycle.MainProcessLifecycleObserver
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.sync.impl.SyncAccountRepository
import com.squareup.anvil.annotations.ContributesMultibinding
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.HOURS
import javax.inject.Inject
import kotlinx.coroutines.withContext
import timber.log.Timber

@ContributesWorker(AppScope::class)
class SyncDailyReportingWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    @Inject
    lateinit var syncPixels: SyncPixels

    @Inject
    lateinit var syncAccountRepository: SyncAccountRepository

    @Inject
    lateinit var dispatchers: DispatcherProvider

    override suspend fun doWork(): Result {
        return withContext(dispatchers.io()) {
            if (syncAccountRepository.isSignedIn()) {
                syncPixels.fireDailySuccessRatePixel()
            }
            return@withContext Result.success()
        }
    }
}

@ContributesMultibinding(
    scope = AppScope::class,
    boundType = MainProcessLifecycleObserver::class,
)
class SyncDailyReportingWorkerScheduler @Inject constructor(
    private val workManager: WorkManager,
) : MainProcessLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Timber.v("Scheduling sync daily reporting worker")
        val workerRequest = PeriodicWorkRequestBuilder<SyncDailyReportingWorker>(24, HOURS)
            .addTag(DAILY_REPORTING_SYNC_WORKER_TAG)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(DAILY_REPORTING_SYNC_WORKER_TAG, ExistingPeriodicWorkPolicy.REPLACE, workerRequest)
    }

    companion object {
        private const val DAILY_REPORTING_SYNC_WORKER_TAG = "DAILY_REPORTING_SYNC_WORKER_TAG"
    }
}
