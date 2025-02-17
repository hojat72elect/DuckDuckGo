

package com.duckduckgo.app.trackerdetection

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleOwner
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.lifecycle.MainProcessLifecycleObserver
import com.duckduckgo.app.pixels.remoteconfig.OptimizeTrackerEvaluationRCWrapper
import com.duckduckgo.app.trackerdetection.api.TdsJson
import com.duckduckgo.app.trackerdetection.db.TdsCnameEntityDao
import com.duckduckgo.app.trackerdetection.db.TdsDomainEntityDao
import com.duckduckgo.app.trackerdetection.db.TdsEntityDao
import com.duckduckgo.app.trackerdetection.db.TdsMetadataDao
import com.duckduckgo.app.trackerdetection.db.TdsTrackerDao
import com.duckduckgo.app.trackerdetection.model.TdsMetadata
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesMultibinding
import com.squareup.moshi.Moshi
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okio.buffer
import okio.source
import timber.log.Timber

@WorkerThread
@ContributesMultibinding(
    scope = AppScope::class,
    boundType = MainProcessLifecycleObserver::class,
)
class TrackerDataLoader @Inject constructor(
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
    private val trackerDetector: TrackerDetector,
    private val tdsTrackerDao: TdsTrackerDao,
    private val tdsEntityDao: TdsEntityDao,
    private val tdsDomainEntityDao: TdsDomainEntityDao,
    private val tdsCnameEntityDao: TdsCnameEntityDao,
    private val tdsMetadataDao: TdsMetadataDao,
    private val context: Context,
    private val appDatabase: AppDatabase,
    private val moshi: Moshi,
    private val urlToTypeMapper: UrlToTypeMapper,
    private val dispatcherProvider: DispatcherProvider,
    private val optimizeTrackerEvaluationRCWrapper: OptimizeTrackerEvaluationRCWrapper,
) : MainProcessLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        appCoroutineScope.launch(dispatcherProvider.io()) { loadData() }
    }

    private fun loadData() {
        Timber.d("Loading tracker data")
        loadTds()
    }

    private fun loadTds() {
        val count = tdsTrackerDao.count()
        if (count == 0) {
            updateTdsFromFile()
        }
        loadTrackers()
    }

    private fun updateTdsFromFile() {
        Timber.d("Updating tds from file")
        runCatching {
            val adapter = moshi.adapter(TdsJson::class.java)
            val inputStream = context.resources.openRawResource(R.raw.tds).source()

            val tdsJson = adapter.fromJson(inputStream.buffer())
            tdsJson?.let {
                persistTds(DEFAULT_ETAG, it)
            }
        }
    }
    fun persistTds(
        eTag: String,
        tdsJson: TdsJson,
    ) {
        appDatabase.runInTransaction {
            tdsMetadataDao.tdsDownloadSuccessful(TdsMetadata(eTag = eTag))
            tdsEntityDao.updateAll(tdsJson.jsonToEntities())
            tdsDomainEntityDao.updateAll(tdsJson.jsonToDomainEntities())
            tdsTrackerDao.updateAll(tdsJson.jsonToTrackers().values)
            tdsCnameEntityDao.updateAll(tdsJson.jsonToCnameEntities())
        }
    }

    fun loadTrackers() {
        val trackers = tdsTrackerDao.getAll()
        Timber.d("Loaded ${trackers.size} tds trackers from DB")
        val client = TdsClient(Client.ClientName.TDS, trackers, urlToTypeMapper, optimizeTrackerEvaluationRCWrapper.enabled)
        trackerDetector.addClient(client)
    }

    companion object {
        const val DEFAULT_ETAG = "961c7d692c985496126cad2d64231243"
    }
}
