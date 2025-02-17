package com.duckduckgo.subscriptions.impl.feedback.pixels

import com.duckduckgo.app.statistics.pixels.Pixel.PixelType
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.COUNT
import com.duckduckgo.app.statistics.pixels.Pixel.PixelType.DAILY
import com.duckduckgo.subscriptions.impl.pixels.pixelNameSuffix

enum class PrivacyProUnifiedFeedbackPixel(
    private val baseName: String,
    private val types: Set<PixelType>,
    private val withSuffix: Boolean = true,
) {
    PPRO_FEEDBACK_FEATURE_REQUEST(
        baseName = "m_ppro_feedback_feature-request",
        types = setOf(COUNT),
        withSuffix = false,
    ),
    PPRO_FEEDBACK_GENERAL_FEEDBACK(
        baseName = "m_ppro_feedback_general-feedback",
        types = setOf(COUNT),
        withSuffix = false,
    ),
    PPRO_FEEDBACK_REPORT_ISSUE(
        baseName = "m_ppro_feedback_report-issue",
        types = setOf(COUNT),
        withSuffix = false,
    ),
    IMPRESSION_PPRO_FEEDBACK_GENERAL_SCREEN(
        baseName = "m_ppro_feedback_general-screen_show",
        types = setOf(COUNT, DAILY),
        withSuffix = true,
    ),
    IMPRESSION_PPRO_FEEDBACK_ACTION_SCREEN(
        baseName = "m_ppro_feedback_actions-screen_show",
        types = setOf(COUNT, DAILY),
        withSuffix = true,
    ),
    IMPRESSION_PPRO_FEEDBACK_CATEGORY_SCREEN(
        baseName = "m_ppro_feedback_category-screen_show",
        types = setOf(COUNT, DAILY),
        withSuffix = true,
    ),
    IMPRESSION_PPRO_FEEDBACK_SUBCATEGORY_SCREEN(
        baseName = "m_ppro_feedback_subcategory-screen_show",
        types = setOf(COUNT, DAILY),
        withSuffix = true,
    ),
    IMPRESSION_PPRO_FEEDBACK_SUBMIT_SCREEN(
        baseName = "m_ppro_feedback_submit-screen_show",
        types = setOf(COUNT, DAILY),
        withSuffix = true,
    ),
    PPRO_FEEDBACK_SUBMIT_SCREEN_FAQ_CLICK(
        baseName = "m_ppro_feedback_submit-screen-faq_click",
        types = setOf(COUNT, DAILY),
        withSuffix = true,
    ), ;

    fun getPixelNames(): Map<PixelType, String> =
        types.associateWith { type -> if (withSuffix) "${baseName}_${type.pixelNameSuffix}" else baseName }
}
