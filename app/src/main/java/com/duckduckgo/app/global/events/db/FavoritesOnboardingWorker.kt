

package com.duckduckgo.app.global.events.db

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

@Deprecated(message = "Worker used during Favorites Onboarding experiment", level = DeprecationLevel.ERROR)
class FavoritesOnboardingWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork() = Result.success()
}
