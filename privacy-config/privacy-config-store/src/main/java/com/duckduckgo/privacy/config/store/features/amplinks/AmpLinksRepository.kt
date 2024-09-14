

package com.duckduckgo.privacy.config.store.features.amplinks

import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.feature.toggles.api.FeatureExceptions.FeatureException
import com.duckduckgo.privacy.config.store.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

interface AmpLinksRepository {
    fun updateAll(exceptions: List<AmpLinkExceptionEntity>, ampLinkFormats: List<AmpLinkFormatEntity>, ampKeywords: List<AmpKeywordEntity>)
    val exceptions: List<FeatureException>
    val ampLinkFormats: List<Regex>
    val ampKeywords: List<String>
}

class RealAmpLinksRepository(
    val database: PrivacyConfigDatabase,
    coroutineScope: CoroutineScope,
    dispatcherProvider: DispatcherProvider,
    isMainProcess: Boolean,
) : AmpLinksRepository {

    private val ampLinksDao: AmpLinksDao = database.ampLinksDao()

    override val exceptions = CopyOnWriteArrayList<FeatureException>()
    override val ampLinkFormats = CopyOnWriteArrayList<Regex>()
    override val ampKeywords = CopyOnWriteArrayList<String>()

    init {
        coroutineScope.launch(dispatcherProvider.io()) {
            if (isMainProcess) {
                loadToMemory()
            }
        }
    }

    override fun updateAll(
        exceptions: List<AmpLinkExceptionEntity>,
        ampLinkFormats: List<AmpLinkFormatEntity>,
        ampKeywords: List<AmpKeywordEntity>,
    ) {
        ampLinksDao.updateAll(exceptions, ampLinkFormats, ampKeywords)
        loadToMemory()
    }

    private fun loadToMemory() {
        exceptions.clear()
        ampLinksDao.getAllExceptions().map {
            exceptions.add(it.toFeatureException())
        }

        ampLinkFormats.clear()
        ampLinksDao.getAllAmpLinkFormats().map {
            ampLinkFormats.add(it.format.toRegex(RegexOption.IGNORE_CASE))
        }

        ampKeywords.clear()
        ampLinksDao.getAllAmpKeywords().map {
            ampKeywords.add(it.keyword)
        }
    }
}
