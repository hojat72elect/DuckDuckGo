package com.duckduckgo.autofill.impl.engagement.store

import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.DAILY
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_ENGAGEMENT_ACTIVE_USER
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_ENGAGEMENT_ENABLED_USER
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_ENGAGEMENT_STACKED_LOGINS
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_TOGGLED_OFF_SEARCH
import com.duckduckgo.autofill.impl.pixel.AutofillPixelNames.AUTOFILL_TOGGLED_ON_SEARCH
import com.duckduckgo.autofill.impl.store.InternalAutofillStore
import com.duckduckgo.autofill.store.engagement.AutofillEngagementDao
import com.duckduckgo.autofill.store.engagement.AutofillEngagementDatabase
import com.duckduckgo.autofill.store.engagement.AutofillEngagementEntity
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber

interface AutofillEngagementRepository {

    /**
     * Record that the user has autofilled today
     */
    suspend fun recordAutofilledToday()

    /**
     * Record that the user has searched today
     */
    suspend fun recordSearchedToday()

    /**
     * Clear all data in the database, optionally preserving today's data
     */
    suspend fun clearData(preserveToday: Boolean = true)
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class DefaultAutofillEngagementRepository @Inject constructor(
    engagementDb: AutofillEngagementDatabase,
    private val pixel: Pixel,
    private val autofillStore: InternalAutofillStore,
    private val engagementBucketing: AutofillEngagementBucketing,
    private val dispatchers: DispatcherProvider,
) : AutofillEngagementRepository {

    private val autofillEngagementDao: AutofillEngagementDao = engagementDb.autofillEngagementDao()

    override suspend fun recordAutofilledToday() {
        withContext(dispatchers.io()) {
            val engagement = todaysEngagement().copy(autofilled = true)
            Timber.v("upserting %s because user autofilled", engagement)
            processEvent(engagement)
        }
    }

    override suspend fun recordSearchedToday() {
        withContext(dispatchers.io()) {
            val engagement = todaysEngagement().copy(searched = true)
            Timber.v("upserting %s because user searched", engagement)
            processEvent(engagement)
            processOnFirstSearchEvent()
        }
    }

    private suspend fun processOnFirstSearchEvent() {
        val numberStoredPasswords = getNumberStoredPasswords()
        val togglePixel =
            if (autofillStore.autofillEnabled) AUTOFILL_TOGGLED_ON_SEARCH else AUTOFILL_TOGGLED_OFF_SEARCH
        val bucket = engagementBucketing.bucketNumberOfSavedPasswords(numberStoredPasswords)
        pixel.fire(togglePixel, mapOf("count_bucket" to bucket), type = DAILY)
    }

    private suspend fun DefaultAutofillEngagementRepository.processEvent(engagement: AutofillEngagementEntity) {
        autofillEngagementDao.upsert(engagement)
        engagement.sendPixelsIfCriteriaMet()
        clearData()
    }

    private suspend fun AutofillEngagementEntity.sendPixelsIfCriteriaMet() {
        val numberStoredPasswords = getNumberStoredPasswords()

        if (autofilled && searched) {
            pixel.fire(AUTOFILL_ENGAGEMENT_ACTIVE_USER, type = DAILY)

            val bucket = engagementBucketing.bucketNumberOfSavedPasswords(numberStoredPasswords)
            pixel.fire(
                AUTOFILL_ENGAGEMENT_STACKED_LOGINS,
                mapOf("count_bucket" to bucket),
                type = DAILY
            )
        }

        if (searched && numberStoredPasswords >= 10 && autofillStore.autofillEnabled) {
            pixel.fire(AUTOFILL_ENGAGEMENT_ENABLED_USER, type = DAILY)
        }
    }

    private suspend fun getNumberStoredPasswords(): Int {
        return autofillStore.getCredentialCount().firstOrNull() ?: 0
    }

    override suspend fun clearData(preserveToday: Boolean) {
        if (preserveToday) {
            autofillEngagementDao.deleteOlderThan(todayString())
        } else {
            autofillEngagementDao.deleteAll()
        }
    }

    private suspend fun todaysEngagement(): AutofillEngagementEntity {
        val key = todayString()
        return autofillEngagementDao.getEngagement(key) ?: entityForToday()
    }

    private fun entityForToday(): AutofillEngagementEntity {
        return AutofillEngagementEntity(date = todayString(), autofilled = false, searched = false)
    }

    private fun todayString(): String {
        return DATE_FORMATTER.format(LocalDate.now())
    }

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
