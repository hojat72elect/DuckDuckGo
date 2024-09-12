package com.duckduckgo.autoconsent.impl.remoteconfig

import com.duckduckgo.autoconsent.impl.store.AutoconsentDatabase
import com.duckduckgo.autoconsent.impl.store.AutoconsentExceptionEntity
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface AutoconsentExceptionsRepository {
    fun insertAllExceptions(exceptions: List<FeatureException>)
    val exceptions: CopyOnWriteArrayList<FeatureException>
}

class RealAutoconsentExceptionsRepository(
    coroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    val database: AutoconsentDatabase,
    isMainProcess: Boolean,
) : AutoconsentExceptionsRepository {

    private val dao = database.autoconsentDao()
    override val exceptions = CopyOnWriteArrayList<FeatureException>()

    init {
        coroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun insertAllExceptions(exceptions: List<FeatureException>) {
        dao.updateAllExceptions(exceptions.map {
            AutoconsentExceptionEntity(
                domain = it.domain,
                reason = it.reason ?: ""
            )
        })
        loadToMemory()
    }

    private fun loadToMemory() {
        exceptions.clear()
        val exceptionsEntityList = dao.getExceptions()
        exceptions.addAll(exceptionsEntityList.map {
            FeatureException(
                domain = it.domain,
                reason = it.reason
            )
        })
    }
}
