package com.duckduckgo.autofill.impl.ui.credential.management.survey

import androidx.core.net.toUri
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.app.usage.app.AppDaysUsedRepository
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.autofill.impl.store.InternalAutofillStore
import com.duckduckgo.autofill.impl.ui.credential.management.survey.AutofillSurveyImpl.Companion.SurveyParams.IN_APP
import com.duckduckgo.autofill.impl.ui.credential.management.survey.AutofillSurveyImpl.Companion.SurveyParams.NUMBER_PASSWORD_BUCKET_LOTS
import com.duckduckgo.autofill.impl.ui.credential.management.survey.AutofillSurveyImpl.Companion.SurveyParams.NUMBER_PASSWORD_BUCKET_MANY
import com.duckduckgo.autofill.impl.ui.credential.management.survey.AutofillSurveyImpl.Companion.SurveyParams.NUMBER_PASSWORD_BUCKET_NONE
import com.duckduckgo.autofill.impl.ui.credential.management.survey.AutofillSurveyImpl.Companion.SurveyParams.NUMBER_PASSWORD_BUCKET_SOME
import com.duckduckgo.browser.api.UserBrowserProperties
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

interface AutofillSurvey {
    suspend fun firstUnusedSurvey(): SurveyDetails?
    suspend fun recordSurveyAsUsed(id: String)
}

@ContributesBinding(AppScope::class)
class AutofillSurveyImpl @Inject constructor(
    private val statisticsStore: StatisticsDataStore,
    private val userBrowserProperties: UserBrowserProperties,
    private val appBuildConfig: AppBuildConfig,
    private val appDaysUsedRepository: AppDaysUsedRepository,
    private val dispatchers: DispatcherProvider,
    private val autofillSurveyStore: AutofillSurveyStore,
    private val internalAutofillStore: InternalAutofillStore,
) : AutofillSurvey {

    override suspend fun firstUnusedSurvey(): SurveyDetails? {
        return withContext(dispatchers.io()) {
            if (!canShowSurvey()) return@withContext null
            val survey =
                autofillSurveyStore.availableSurveys().firstOrNull { !surveyTakenPreviously(it.id) }
                    ?: return@withContext null
            return@withContext survey.copy(url = survey.url.addSurveyParameters())
        }
    }

    private fun canShowSurvey(): Boolean {
        return deviceSetToEnglish()
    }

    override suspend fun recordSurveyAsUsed(id: String) {
        autofillSurveyStore.recordSurveyWasShown(id)
    }

    private fun deviceSetToEnglish(): Boolean {
        return appBuildConfig.deviceLocale.language == Locale("en").language
    }

    private suspend fun surveyTakenPreviously(surveyId: String): Boolean {
        return autofillSurveyStore.hasSurveyBeenTaken(surveyId)
    }

    private suspend fun String.addSurveyParameters(): String {
        return withContext(dispatchers.io()) {
            val urlBuilder = toUri()
                .buildUpon()
                .appendQueryParameter(SurveyParams.ATB, statisticsStore.atb?.version ?: "")
                .appendQueryParameter(SurveyParams.ATB_VARIANT, statisticsStore.variant)
                .appendQueryParameter(
                    SurveyParams.DAYS_INSTALLED,
                    "${userBrowserProperties.daysSinceInstalled()}"
                )
                .appendQueryParameter(SurveyParams.ANDROID_VERSION, "${appBuildConfig.sdkInt}")
                .appendQueryParameter(SurveyParams.APP_VERSION, appBuildConfig.versionName)
                .appendQueryParameter(SurveyParams.MANUFACTURER, appBuildConfig.manufacturer)
                .appendQueryParameter(SurveyParams.MODEL, appBuildConfig.model)
                .appendQueryParameter(SurveyParams.SOURCE, IN_APP)
                .appendQueryParameter(
                    SurveyParams.LAST_ACTIVE_DATE,
                    appDaysUsedRepository.getLastActiveDay()
                )
                .appendQueryParameter(
                    SurveyParams.NUMBER_PASSWORDS,
                    bucketSavedPasswords(internalAutofillStore.getCredentialCount().firstOrNull())
                )

            urlBuilder.build().toString()
        }
    }

    private fun bucketSavedPasswords(passwordsSaved: Int?): String {
        return when {
            passwordsSaved == null -> NUMBER_PASSWORD_BUCKET_NONE
            passwordsSaved < 3 -> NUMBER_PASSWORD_BUCKET_NONE
            passwordsSaved < 10 -> NUMBER_PASSWORD_BUCKET_SOME
            passwordsSaved < 50 -> NUMBER_PASSWORD_BUCKET_MANY
            else -> NUMBER_PASSWORD_BUCKET_LOTS
        }
    }

    companion object {

        private object SurveyParams {
            const val ATB = "atb"
            const val ATB_VARIANT = "var"
            const val DAYS_INSTALLED = "delta"
            const val ANDROID_VERSION = "av"
            const val APP_VERSION = "ddgv"
            const val MANUFACTURER = "man"
            const val MODEL = "mo"
            const val LAST_ACTIVE_DATE = "da"
            const val SOURCE = "src"
            const val IN_APP = "in_app"
            const val NUMBER_PASSWORDS = "saved_passwords"
            const val NUMBER_PASSWORD_BUCKET_NONE = "none"
            const val NUMBER_PASSWORD_BUCKET_SOME = "some"
            const val NUMBER_PASSWORD_BUCKET_MANY = "many"
            const val NUMBER_PASSWORD_BUCKET_LOTS = "lots"
        }
    }
}
