

package com.duckduckgo.httpsupgrade.impl

import com.duckduckgo.common.utils.extensions.isCached
import com.duckduckgo.httpsupgrade.api.HttpsUpgradeDataDownloader
import com.duckduckgo.httpsupgrade.api.HttpsUpgrader
import com.duckduckgo.httpsupgrade.store.HttpsBloomFilterSpec
import com.duckduckgo.httpsupgrade.store.HttpsFalsePositivesDao
import io.reactivex.Completable
import io.reactivex.Completable.fromAction
import java.io.IOException
import logcat.logcat

internal class HttpsUpgradeDataDownloaderImpl constructor(
    private val service: HttpsUpgradeService,
    private val httpsUpgrader: HttpsUpgrader,
    private val dataPersister: HttpsDataPersister,
    private val bloomFalsePositivesDao: HttpsFalsePositivesDao,
) : HttpsUpgradeDataDownloader {

    override fun download(): Completable {
        val filter = service.httpsBloomFilterSpec()
            .flatMapCompletable {
                downloadBloomFilter(it)
            }
        val falsePositives = downloadFalsePositives()

        return Completable.mergeDelayError(listOf(filter, falsePositives))
            .doOnComplete {
                logcat { "Https download task completed successfully" }
            }
    }

    private fun downloadBloomFilter(specification: HttpsBloomFilterSpec): Completable {
        return fromAction {
            logcat { "Downloading https bloom filter binary" }

            if (dataPersister.isPersisted(specification)) {
                logcat { "Https bloom data already stored for this spec" }
                return@fromAction
            }

            val call = service.httpsBloomFilter()
            val response = call.execute()
            if (!response.isSuccessful) {
                throw IOException("Status: ${response.code()} - ${response.errorBody()?.string()}")
            }

            val bytes = response.body()!!.bytes()
            dataPersister.persistBloomFilter(specification, bytes)
            httpsUpgrader.reloadData()
        }
    }

    private fun downloadFalsePositives(): Completable {
        logcat { "Downloading HTTPS false positives" }
        return fromAction {
            val call = service.falsePositives()
            val response = call.execute()

            if (response.isCached && bloomFalsePositivesDao.count() > 0) {
                logcat { "Https false positives already cached and stored" }
                return@fromAction
            }

            if (response.isSuccessful) {
                val falsePositives = response.body()!!
                logcat { "Updating https false positives with new data" }
                dataPersister.persistFalsePositives(falsePositives)
            } else {
                throw IOException("Status: ${response.code()} - ${response.errorBody()?.string()}")
            }
        }
    }
}
