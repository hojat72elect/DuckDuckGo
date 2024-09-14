

package com.duckduckgo.mobile.android.vpn

import android.content.ComponentName
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.multiprocess.RemoteListenableWorker
import androidx.work.multiprocess.RemoteWorkerService

/**
 * Use this extension function to make sure your [PeriodicWorkRequest] runs in the VPN process
 */
fun PeriodicWorkRequest.Builder.boundToVpnProcess(applicationId: String): PeriodicWorkRequest.Builder {
    val componentName = ComponentName(applicationId, RemoteWorkerService::class.java.name)
    val data = Data.Builder()
        .putString(RemoteListenableWorker.ARGUMENT_PACKAGE_NAME, componentName.packageName)
        .putString(RemoteListenableWorker.ARGUMENT_CLASS_NAME, componentName.className)
        .build()

    return this.setInputData(data)
}
