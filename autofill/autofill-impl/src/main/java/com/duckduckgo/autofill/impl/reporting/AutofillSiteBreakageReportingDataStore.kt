package com.duckduckgo.autofill.impl.reporting

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.duckduckgo.autofill.impl.reporting.remoteconfig.AutofillSiteBreakageReporting
import com.duckduckgo.autofill.impl.time.TimeProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

interface AutofillSiteBreakageReportingDataStore {

    suspend fun getMinimumNumberOfDaysBeforeReportPromptReshown(): Int
    suspend fun updateMinimumNumberOfDaysBeforeReportPromptReshown(newValue: Int)
    suspend fun recordFeedbackSent(eTldPlusOne: String)
    suspend fun getTimestampLastFeedbackSent(eTldPlusOne: String): Long?
    suspend fun clearAllReports()
}

@SingleInstanceIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AutofillSiteBreakageReportingDataStoreImpl @Inject constructor(
    @AutofillSiteBreakageReporting private val store: DataStore<Preferences>,
    private val timeProvider: TimeProvider,
) : AutofillSiteBreakageReportingDataStore {

    private val daysBeforePromptShownAgainKey = intPreferencesKey("days_before_prompt_shown_again")

    override suspend fun updateMinimumNumberOfDaysBeforeReportPromptReshown(newValue: Int) {
        store.edit {
            it[daysBeforePromptShownAgainKey] = newValue
        }
    }

    override suspend fun recordFeedbackSent(eTldPlusOne: String) {
        store.edit {
            it[timestampLastReportedKey(eTldPlusOne)] = timeProvider.currentTimeMillis()
        }
    }

    override suspend fun getTimestampLastFeedbackSent(eTldPlusOne: String): Long? {
        return store.data.firstOrNull()?.get(timestampLastReportedKey(eTldPlusOne))
    }

    override suspend fun clearAllReports() {
        store.edit { it.clear() }
    }

    override suspend fun getMinimumNumberOfDaysBeforeReportPromptReshown(): Int {
        return store.data.firstOrNull()?.get(daysBeforePromptShownAgainKey)
            ?: DEFAULT_NUMBER_OF_DAYS_BEFORE_REPORT_PROMPT_RESHOWN
    }

    private fun timestampLastReportedKey(eTldPlusOne: String) =
        longPreferencesKey("timestamp_last_reported_$eTldPlusOne")

    companion object {
        private const val DEFAULT_NUMBER_OF_DAYS_BEFORE_REPORT_PROMPT_RESHOWN = 42
    }
}
