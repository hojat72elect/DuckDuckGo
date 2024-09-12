package com.duckduckgo.autoconsent.impl.remoteconfig

import com.duckduckgo.autoconsent.impl.remoteconfig.AutoconsentFeatureModels.AutoconsentSettings
import com.duckduckgo.autoconsent.impl.store.AutoconsentDatabase
import com.duckduckgo.autoconsent.impl.store.DisabledCmpsEntity
import com.duckduckgo.common.utils.DispatcherProvider
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface AutoconsentFeatureSettingsRepository {
    fun updateAllSettings(settings: AutoconsentSettings)
    val disabledCMPs: CopyOnWriteArrayList<String>
}

class RealAutoconsentFeatureSettingsRepository(
    coroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    val database: AutoconsentDatabase,
    isMainProcess: Boolean,
) : AutoconsentFeatureSettingsRepository {

    override val disabledCMPs = CopyOnWriteArrayList<String>()
    private val dao = database.autoconsentDao()

    init {
        coroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun updateAllSettings(settings: AutoconsentSettings) {
        dao.updateAllDisabledCMPs(settings.disabledCMPs.map { DisabledCmpsEntity(it) })
        loadToMemory()
    }

    private fun loadToMemory() {
        disabledCMPs.clear()
        val disabledCMPsEntityList = dao.getDisabledCmps()
        disabledCMPs.addAll(disabledCMPsEntityList.map { it.name })
    }
}
