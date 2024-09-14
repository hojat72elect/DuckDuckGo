

package com.duckduckgo.autofill.store.feature.email.incontext

import com.duckduckgo.common.utils.DispatcherProvider
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface EmailProtectionInContextFeatureRepository {
    fun updateAllExceptions(exceptions: List<EmailInContextExceptionEntity>)
    val exceptions: CopyOnWriteArrayList<String>
}

class RealEmailProtectionInContextFeatureRepository(
    val database: EmailProtectionInContextDatabase,
    coroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    isMainProcess: Boolean,
) : EmailProtectionInContextFeatureRepository {

    private val dao = database.emailInContextDao()
    override val exceptions = CopyOnWriteArrayList<String>()

    init {
        coroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun updateAllExceptions(exceptions: List<EmailInContextExceptionEntity>) {
        dao.updateAll(exceptions)
        loadToMemory()
    }

    private fun loadToMemory() {
        exceptions.clear()
        dao.getAll().map { exceptions.add(it.domain) }
    }
}
