

package com.duckduckgo.privacy.config.store.features.gpc

import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.privacy.config.api.GpcException
import com.duckduckgo.privacy.config.api.GpcHeaderEnabledSite
import com.duckduckgo.privacy.config.store.GpcContentScopeConfigEntity
import com.duckduckgo.privacy.config.store.GpcExceptionEntity
import com.duckduckgo.privacy.config.store.GpcHeaderEnabledSiteEntity
import com.duckduckgo.privacy.config.store.PrivacyConfigDatabase
import com.duckduckgo.privacy.config.store.toGpcException
import com.duckduckgo.privacy.config.store.toGpcHeaderEnabledSite
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface GpcRepository {
    fun updateAll(
        exceptions: List<GpcExceptionEntity>,
        headerEnabledSites: List<GpcHeaderEnabledSiteEntity>,
        gpcContentScopeConfig: GpcContentScopeConfigEntity,
    )

    fun enableGpc()
    fun disableGpc()
    fun isGpcEnabled(): Boolean
    val exceptions: CopyOnWriteArrayList<GpcException>
    val headerEnabledSites: CopyOnWriteArrayList<GpcHeaderEnabledSite>
    val gpcContentScopeConfig: String
}

class RealGpcRepository(
    private val gpcDataStore: GpcDataStore,
    val database: PrivacyConfigDatabase,
    coroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    isMainProcess: Boolean,
) : GpcRepository {

    private val gpcExceptionsDao: GpcExceptionsDao = database.gpcExceptionsDao()
    private val gpcHeadersDao: GpcHeadersDao = database.gpcHeadersDao()
    private val gpcContentScopeConfigDao: GpcContentScopeConfigDao = database.gpcContentScopeConfigDao()
    override val exceptions = CopyOnWriteArrayList<GpcException>()
    override val headerEnabledSites = CopyOnWriteArrayList<GpcHeaderEnabledSite>()
    override var gpcContentScopeConfig: String = emptyJson

    init {
        coroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun updateAll(
        exceptions: List<GpcExceptionEntity>,
        headerEnabledSites: List<GpcHeaderEnabledSiteEntity>,
        gpcContentScopeConfig: GpcContentScopeConfigEntity,
    ) {
        gpcExceptionsDao.updateAll(exceptions)
        gpcHeadersDao.updateAll(headerEnabledSites)
        gpcContentScopeConfigDao.insert(gpcContentScopeConfig)
        loadToMemory()
    }

    override fun enableGpc() {
        gpcDataStore.gpcEnabled = true
    }

    override fun disableGpc() {
        gpcDataStore.gpcEnabled = false
    }

    override fun isGpcEnabled(): Boolean = gpcDataStore.gpcEnabled

    private fun loadToMemory() {
        exceptions.clear()
        headerEnabledSites.clear()
        gpcExceptionsDao.getAll().map { exceptions.add(it.toGpcException()) }
        gpcHeadersDao.getAll().map { headerEnabledSites.add(it.toGpcHeaderEnabledSite()) }
        gpcContentScopeConfigDao.getConfig()?.let { entity ->
            gpcContentScopeConfig = entity.config
        }
    }

    companion object {
        const val emptyJson = "{}"
    }
}
