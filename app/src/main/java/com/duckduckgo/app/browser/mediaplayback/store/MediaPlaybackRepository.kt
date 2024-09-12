package com.duckduckgo.app.browser.mediaplayback.store

import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.di.IsMainProcess
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureExceptions
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface MediaPlaybackRepository {
    fun updateAll(exceptions: List<MediaPlaybackExceptionEntity>)
    val exceptions: CopyOnWriteArrayList<FeatureExceptions.FeatureException>
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class RealMediaPlaybackRepository @Inject constructor(
    private val mediaPlaybackDao: MediaPlaybackDao,
    @AppCoroutineScope appCoroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    @IsMainProcess isMainProcess: Boolean,
) : MediaPlaybackRepository {

    override val exceptions = CopyOnWriteArrayList<FeatureExceptions.FeatureException>()

    init {
        appCoroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun updateAll(exceptions: List<MediaPlaybackExceptionEntity>) {
        mediaPlaybackDao.updateAll(exceptions)
        loadToMemory()
    }

    private fun loadToMemory() {
        exceptions.clear()
        mediaPlaybackDao.getAll().map {
            exceptions.add(it.toFeatureException())
        }
    }
}
