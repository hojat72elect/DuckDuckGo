

package com.duckduckgo.app.trackerdetection.api

import com.duckduckgo.app.global.db.AppDatabase
import com.duckduckgo.app.trackerdetection.Client.ClientName.*
import com.duckduckgo.app.trackerdetection.TrackerDataLoader
import com.duckduckgo.app.trackerdetection.db.TdsMetadataDao
import com.duckduckgo.common.utils.extensions.extractETag
import com.duckduckgo.common.utils.store.BinaryDataStore
import io.reactivex.Completable
import java.io.IOException
import javax.inject.Inject
import okhttp3.Headers
import timber.log.Timber

class TrackerDataDownloader @Inject constructor(
    private val trackerListService: TrackerListService,
    private val binaryDataStore: BinaryDataStore,
    private val trackerDataLoader: TrackerDataLoader,
    private val appDatabase: AppDatabase,
    private val metadataDao: TdsMetadataDao,
) {

    fun downloadTds(): Completable {
        return Completable.fromAction {
            Timber.d("Downloading tds.json")

            val call = trackerListService.tds()
            val response = call.execute()

            if (!response.isSuccessful) {
                throw IOException("Status: ${response.code()} - ${response.errorBody()?.string()}")
            }

            val body = response.body()!!
            val eTag = response.headers().extractETag()
            val oldEtag = metadataDao.eTag()
            if (eTag != oldEtag) {
                Timber.d("Updating tds data from server")
                appDatabase.runInTransaction {
                    trackerDataLoader.persistTds(eTag, body)
                    trackerDataLoader.loadTrackers()
                }
            }
        }
    }

    fun clearLegacyLists(): Completable {
        return Completable.fromAction {
            listOf(EASYLIST, EASYPRIVACY, TRACKERSALLOWLIST).forEach {
                if (binaryDataStore.hasData(it.name)) {
                    binaryDataStore.clearData(it.name)
                }
            }
            return@fromAction
        }
    }
}

fun Headers.extractETag(): String {
    return this["eTag"]?.removePrefix("W/")?.removeSurrounding("\"", "\"").orEmpty() // removes weak eTag validator
}
