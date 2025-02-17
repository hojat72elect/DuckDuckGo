

package com.duckduckgo.remote.messaging.fixtures

import com.duckduckgo.remote.messaging.api.JsonMessageAction
import com.duckduckgo.remote.messaging.impl.models.JsonContent
import com.duckduckgo.remote.messaging.impl.models.JsonContentTranslations
import com.duckduckgo.remote.messaging.impl.models.JsonMatchingRule
import com.duckduckgo.remote.messaging.impl.models.JsonRemoteMessage
import com.duckduckgo.remote.messaging.impl.models.JsonRemoteMessagingConfig

object JsonRemoteMessageOM {
    private fun jsonMessageAction(
        type: String = "url",
        value: String = "http://example.com",
        additionalParameters: Map<String, String>? = null,
    ) = JsonMessageAction(
        type = type,
        value = value,
        additionalParameters = additionalParameters,
    )

    fun smallJsonContent(
        titleText: String = "title",
        descriptionText: String = "description",
    ) = JsonContent(
        messageType = "small",
        titleText = titleText,
        descriptionText = descriptionText,
    )

    fun mediumJsonContent(
        titleText: String = "title",
        descriptionText: String = "description",
        placeholder: String = "Announce",
    ) = JsonContent(
        messageType = "medium",
        titleText = titleText,
        descriptionText = descriptionText,
        placeholder = placeholder,
    )

    fun bigSingleActionJsonContent(
        titleText: String = "title",
        descriptionText: String = "description",
        placeholder: String = "Announce",
        primaryActionText: String = "Action1",
        primaryAction: JsonMessageAction = jsonMessageAction(),
    ) = JsonContent(
        messageType = "big_single_action",
        titleText = titleText,
        descriptionText = descriptionText,
        placeholder = placeholder,
        primaryActionText = primaryActionText,
        primaryAction = primaryAction,
    )

    fun bigTwoActionJsonContent(
        titleText: String = "title",
        descriptionText: String = "description",
        placeholder: String = "Announce",
        primaryActionText: String = "Action1",
        primaryAction: JsonMessageAction = jsonMessageAction(),
        secondaryActionText: String = "Action2",
        secondaryAction: JsonMessageAction = jsonMessageAction(),
    ) = JsonContent(
        messageType = "big_two_action",
        titleText = titleText,
        descriptionText = descriptionText,
        placeholder = placeholder,
        primaryActionText = primaryActionText,
        primaryAction = primaryAction,
        secondaryActionText = secondaryActionText,
        secondaryAction = secondaryAction,
    )

    fun promoSingleActionJsonContent(
        titleText: String = "title",
        descriptionText: String = "description",
        placeholder: String = "NewForMacAndWindows",
        actionText: String = "Action",
        action: JsonMessageAction = jsonMessageAction(),
    ) = JsonContent(
        messageType = "promo_single_action",
        titleText = titleText,
        descriptionText = descriptionText,
        placeholder = placeholder,
        actionText = actionText,
        action = action,
    )

    fun emptyJsonContent(messageType: String = "") = JsonContent(messageType = messageType)

    fun aJsonMessage(
        id: String = "id",
        content: JsonContent? = emptyJsonContent(),
        exclusionRules: List<Int> = emptyList(),
        matchingRules: List<Int> = emptyList(),
        translations: Map<String, JsonContentTranslations> = emptyMap(),
    ) = JsonRemoteMessage(
        id = id,
        content = content,
        exclusionRules = exclusionRules,
        matchingRules = matchingRules,
        translations = translations,
    )

    fun aJsonRemoteMessagingConfig(
        version: Long = 0L,
        messages: List<JsonRemoteMessage> = emptyList(),
        rules: List<JsonMatchingRule> = emptyList(),
    ) = JsonRemoteMessagingConfig(
        version = version,
        messages = messages,
        rules = rules,
    )
}
