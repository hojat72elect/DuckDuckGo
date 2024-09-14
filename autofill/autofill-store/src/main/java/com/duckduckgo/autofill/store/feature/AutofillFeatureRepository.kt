

package com.duckduckgo.autofill.store.feature

import com.duckduckgo.autofill.store.AutofillDao
import com.duckduckgo.autofill.store.AutofillDatabase
import com.duckduckgo.autofill.store.AutofillExceptionEntity
import com.duckduckgo.autofill.store.toFeatureException
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface AutofillFeatureRepository {
    fun updateAllExceptions(exceptions: List<AutofillExceptionEntity>)
    val exceptions: CopyOnWriteArrayList<FeatureException>
}

class RealAutofillFeatureRepository(
    val database: AutofillDatabase,
    coroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    isMainProcess: Boolean,
) : AutofillFeatureRepository {

    private val autofillDao: AutofillDao = database.autofillDao()
    override val exceptions = CopyOnWriteArrayList<FeatureException>()

    init {
        coroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun updateAllExceptions(exceptions: List<AutofillExceptionEntity>) {
        autofillDao.updateAll(exceptions)
        loadToMemory()
    }

    private fun loadToMemory() {
        exceptions.clear()
        autofillDao.getAll().map { exceptions.add(it.toFeatureException()) }
    }
}
