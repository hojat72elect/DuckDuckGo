package com.duckduckgo.autofill.store.reporting

import com.duckduckgo.common.utils.DispatcherProvider
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface AutofillSiteBreakageReportingFeatureRepository {
    fun updateAllExceptions(exceptions: List<AutofillSiteBreakageReportingEntity>)
    val exceptions: List<String>
}

class AutofillSiteBreakageReportingFeatureRepositoryImpl(
    val database: AutofillSiteBreakageReportingDatabase,
    coroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    isMainProcess: Boolean,
) : AutofillSiteBreakageReportingFeatureRepository {

    private val dao = database.dao()
    override val exceptions = CopyOnWriteArrayList<String>()

    init {
        coroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun updateAllExceptions(exceptions: List<AutofillSiteBreakageReportingEntity>) {
        dao.updateAll(exceptions)
        loadToMemory()
    }

    private fun loadToMemory() {
        exceptions.clear()
        dao.getAll().map { exceptions.add(it.domain) }
    }
}
