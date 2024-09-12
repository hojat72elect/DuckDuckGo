package com.duckduckgo.autofill.impl.ui.credential.management.survey

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import kotlinx.coroutines.withContext

interface AutofillSurveyStore {
    suspend fun hasSurveyBeenTaken(id: String): Boolean
    suspend fun recordSurveyWasShown(id: String)
    suspend fun resetPreviousSurveys()
    suspend fun availableSurveys(): List<SurveyDetails>
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class AutofillSurveyStoreImpl @Inject constructor(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
) : AutofillSurveyStore {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun hasSurveyBeenTaken(id: String): Boolean {
        return withContext(dispatchers.io()) {
            val previousSurveys = prefs.getStringSet(SURVEY_IDS, mutableSetOf()) ?: mutableSetOf()
            previousSurveys.contains(id)
        }
    }

    override suspend fun recordSurveyWasShown(id: String) {
        withContext(dispatchers.io()) {
            val currentValue = prefs.getStringSet(SURVEY_IDS, mutableSetOf()) ?: mutableSetOf()
            val newValue = currentValue.toMutableSet()
            newValue.add(id)
            prefs.edit {
                putStringSet(SURVEY_IDS, newValue)
            }
        }
    }

    override suspend fun resetPreviousSurveys() {
        withContext(dispatchers.io()) {
            prefs.edit {
                remove(SURVEY_IDS)
            }
        }
    }

    override suspend fun availableSurveys(): List<SurveyDetails> {
        return emptyList()
    }

    companion object {
        private const val PREFS_FILE_NAME = "autofill_survey_store"
        private const val SURVEY_IDS = "survey_ids"
    }
}
