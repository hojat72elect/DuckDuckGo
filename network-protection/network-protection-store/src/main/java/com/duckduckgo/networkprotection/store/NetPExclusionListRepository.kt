

package com.duckduckgo.networkprotection.store

import androidx.annotation.WorkerThread
import com.duckduckgo.networkprotection.store.db.NetPExclusionListDao
import com.duckduckgo.networkprotection.store.db.NetPManuallyExcludedApp
import kotlinx.coroutines.flow.Flow

@WorkerThread
interface NetPExclusionListRepository {
    fun getManualAppExclusionList(): List<NetPManuallyExcludedApp>

    fun getManualAppExclusionListFlow(): Flow<List<NetPManuallyExcludedApp>>

    fun getExcludedAppPackages(): List<String>

    fun manuallyExcludeApp(packageName: String)

    fun manuallyExcludeApps(packageNames: List<String>)

    fun manuallyEnableApp(packageName: String)

    fun restoreDefaultProtectedList()
}

class RealNetPExclusionListRepository constructor(
    private val exclusionListDao: NetPExclusionListDao,
) : NetPExclusionListRepository {
    override fun getManualAppExclusionList(): List<NetPManuallyExcludedApp> = exclusionListDao.getManualAppExclusionList()

    override fun getManualAppExclusionListFlow(): Flow<List<NetPManuallyExcludedApp>> = exclusionListDao.getManualAppExclusionListFlow()

    override fun getExcludedAppPackages(): List<String> {
        return getManualAppExclusionList()
            .filter { !it.isProtected }
            .sortedBy { it.packageId }
            .map { it.packageId }
    }

    override fun manuallyExcludeApp(packageName: String) {
        exclusionListDao.insertIntoManualAppExclusionList(NetPManuallyExcludedApp(packageId = packageName, isProtected = false))
    }

    override fun manuallyExcludeApps(packageNames: List<String>) {
        packageNames.map {
            NetPManuallyExcludedApp(packageId = it, isProtected = false)
        }.also {
            exclusionListDao.insertIntoManualAppExclusionList(it)
        }
    }

    override fun manuallyEnableApp(packageName: String) {
        exclusionListDao.insertIntoManualAppExclusionList(NetPManuallyExcludedApp(packageId = packageName, isProtected = true))
    }

    override fun restoreDefaultProtectedList() {
        exclusionListDao.deleteManualAppExclusionList()
    }
}
