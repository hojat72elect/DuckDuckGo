

package com.duckduckgo.site.permissions.impl.drmblock

import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.di.IsMainProcess
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import com.duckduckgo.site.permissions.store.drmblock.DrmBlockDao
import com.duckduckgo.site.permissions.store.drmblock.DrmBlockExceptionEntity
import com.duckduckgo.site.permissions.store.drmblock.toFeatureException
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface DrmBlockRepository {
    fun updateAll(exceptions: List<DrmBlockExceptionEntity>)
    val exceptions: CopyOnWriteArrayList<FeatureException>
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class RealDrmBlockRepository @Inject constructor(
    val drmBlockDao: DrmBlockDao,
    @AppCoroutineScope appCoroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    @IsMainProcess isMainProcess: Boolean,
) : DrmBlockRepository {

    override val exceptions = CopyOnWriteArrayList<FeatureException>()

    init {
        appCoroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun updateAll(exceptions: List<DrmBlockExceptionEntity>) {
        drmBlockDao.updateAll(exceptions)
        loadToMemory()
    }

    private fun loadToMemory() {
        exceptions.clear()
        drmBlockDao.getAll().map {
            exceptions.add(it.toFeatureException())
        }
    }
}
