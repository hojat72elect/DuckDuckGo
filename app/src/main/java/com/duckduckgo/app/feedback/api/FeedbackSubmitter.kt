

package com.duckduckgo.app.feedback.api

import android.os.Build
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.MainReason
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.MainReason.APP_IS_SLOW_OR_BUGGY
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.MainReason.MISSING_BROWSING_FEATURES
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.MainReason.NOT_ENOUGH_CUSTOMIZATIONS
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.MainReason.OTHER
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.MainReason.SEARCH_NOT_GOOD_ENOUGH
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.MainReason.WEBSITES_NOT_LOADING
import com.duckduckgo.app.feedback.ui.negative.FeedbackType.SubReason
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.pixels.AppPixelName.FEEDBACK_NEGATIVE_SUBMISSION
import com.duckduckgo.app.statistics.pixels.Pixel
import com.duckduckgo.app.statistics.store.StatisticsDataStore
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.experiments.api.VariantManager
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

interface FeedbackSubmitter {

    suspend fun sendNegativeFeedback(
        mainReason: MainReason,
        subReason: SubReason?,
        openEnded: String,
    )

    suspend fun sendPositiveFeedback(openEnded: String?)
    suspend fun sendBrokenSiteFeedback(
        openEnded: String,
        brokenSite: String?,
    )

    suspend fun sendUserRated()
}

class FireAndForgetFeedbackSubmitter(
    private val feedbackService: FeedbackService,
    private val variantManager: VariantManager,
    private val apiKeyMapper: SubReasonApiMapper,
    private val statisticsDataStore: StatisticsDataStore,
    private val pixel: Pixel,
    private val appCoroutineScope: CoroutineScope,
    private val appBuildConfig: AppBuildConfig,
    private val dispatcherProvider: DispatcherProvider,
) : FeedbackSubmitter {
    override suspend fun sendNegativeFeedback(
        mainReason: MainReason,
        subReason: SubReason?,
        openEnded: String,
    ) {
        Timber.i("User provided negative feedback: {$openEnded}. mainReason = $mainReason, subReason = $subReason")

        val category = categoryFromMainReason(mainReason)
        val subcategory = apiKeyMapper.apiKeyFromSubReason(subReason)

        sendPixel(pixelForNegativeFeedback(category, subcategory))

        appCoroutineScope.launch(dispatcherProvider.io()) {
            runCatching {
                submitFeedback(
                    openEnded = openEnded,
                    rating = NEGATIVE_FEEDBACK,
                    category = category,
                    subcategory = subcategory,
                )
            }
                .onSuccess { Timber.i("Successfully submitted feedback") }
                .onFailure { Timber.w(it, "Failed to send feedback") }
        }
    }

    override suspend fun sendPositiveFeedback(openEnded: String?) {
        Timber.i("User provided positive feedback: {$openEnded}")

        sendPixel(pixelForPositiveFeedback())

        if (openEnded != null) {
            appCoroutineScope.launch(dispatcherProvider.io()) {
                runCatching { submitFeedback(openEnded = openEnded, rating = POSITIVE_FEEDBACK) }
                    .onSuccess { Timber.i("Successfully submitted feedback") }
                    .onFailure { Timber.w(it, "Failed to send feedback") }
            }
        }
    }

    override suspend fun sendBrokenSiteFeedback(
        openEnded: String,
        brokenSite: String?,
    ) {
        Timber.i("User provided broken site report through feedback, url:{$brokenSite}, comment:{$openEnded}")

        val category = categoryFromMainReason(WEBSITES_NOT_LOADING)
        val subcategory = apiKeyMapper.apiKeyFromSubReason(null)
        sendPixel(pixelForNegativeFeedback(category, subcategory))

        appCoroutineScope.launch(dispatcherProvider.io()) {
            runCatching {
                submitFeedback(
                    rating = NEGATIVE_FEEDBACK,
                    url = brokenSite,
                    openEnded = openEnded,
                    category = category,
                )
            }
                .onSuccess { Timber.i("Successfully submitted broken site feedback") }
                .onFailure { Timber.w(it, "Failed to send broken site feedback") }
        }
    }

    override suspend fun sendUserRated() {
        Timber.i("User indicated they'd rate the app")
        sendPixel(pixelForPositiveFeedback())
    }

    private fun sendPixel(pixelName: String) {
        Timber.d("Firing feedback pixel: $pixelName")
        pixel.fire(pixelName)
    }

    private suspend fun submitFeedback(
        openEnded: String,
        rating: String,
        category: String? = null,
        subcategory: String? = null,
        url: String? = null,
        reason: String = FeedbackService.REASON_GENERAL,
    ) {
        feedbackService.submitFeedback(
            reason = reason,
            category = category,
            subcategory = subcategory,
            rating = rating,
            url = url,
            comment = openEnded,
            version = version(),
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            api = appBuildConfig.sdkInt,
            atb = atbWithVariant(),
        )
    }

    private fun categoryFromMainReason(mainReason: MainReason): String {
        return when (mainReason) {
            MISSING_BROWSING_FEATURES -> "browserFeatures"
            WEBSITES_NOT_LOADING -> "brokenSites"
            SEARCH_NOT_GOOD_ENOUGH -> "badResults"
            NOT_ENOUGH_CUSTOMIZATIONS -> "customization"
            APP_IS_SLOW_OR_BUGGY -> "performance"
            OTHER -> "other"
        }
    }

    private fun pixelForNegativeFeedback(
        category: String,
        subcategory: String,
    ): String {
        return String.format(Locale.US, FEEDBACK_NEGATIVE_SUBMISSION.pixelName, NEGATIVE_FEEDBACK, category, subcategory)
    }

    private fun pixelForPositiveFeedback(): String {
        return String.format(Locale.US, AppPixelName.FEEDBACK_POSITIVE_SUBMISSION.pixelName, POSITIVE_FEEDBACK)
    }

    private fun version(): String {
        return appBuildConfig.versionName
    }

    private fun atbWithVariant(): String {
        return statisticsDataStore.atb?.formatWithVariant(variantManager.getVariantKey()) ?: ""
    }

    companion object {
        private const val POSITIVE_FEEDBACK = "positive"
        private const val NEGATIVE_FEEDBACK = "negative"
    }
}
