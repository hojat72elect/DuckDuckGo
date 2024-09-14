

package com.duckduckgo.remote.messaging.fixtures

import com.duckduckgo.remote.messaging.api.Action
import com.duckduckgo.remote.messaging.api.Content
import com.duckduckgo.remote.messaging.api.Content.Placeholder
import com.duckduckgo.remote.messaging.api.Content.Placeholder.ANNOUNCE
import com.duckduckgo.remote.messaging.api.Content.Placeholder.MAC_AND_WINDOWS
import com.duckduckgo.remote.messaging.api.RemoteMessage

@Suppress("MemberVisibilityCanBePrivate")
object RemoteMessageOM {
    fun urlAction(
        url: String = "http://example.com",
    ) = Action.Url(value = url)

    fun smallContent(
        titleText: String = "title",
        descriptionText: String = "description",
    ) = Content.Small(
        titleText = titleText,
        descriptionText = descriptionText,
    )

    fun mediumContent(
        titleText: String = "title",
        descriptionText: String = "description",
        placeholder: Placeholder = ANNOUNCE,
    ) = Content.Medium(
        titleText = titleText,
        descriptionText = descriptionText,
        placeholder = placeholder,
    )

    fun bigSingleActionContent(
        titleText: String = "title",
        descriptionText: String = "description",
        placeholder: Placeholder = ANNOUNCE,
        primaryActionText: String = "Action1",
        primaryAction: Action = urlAction(),
    ) = Content.BigSingleAction(
        titleText = titleText,
        descriptionText = descriptionText,
        placeholder = placeholder,
        primaryActionText = primaryActionText,
        primaryAction = primaryAction,
    )

    fun bigTwoActionsContent(
        titleText: String = "title",
        descriptionText: String = "description",
        placeholder: Placeholder = ANNOUNCE,
        primaryActionText: String = "Action1",
        primaryAction: Action = urlAction(),
        secondaryActionText: String = "Action2",
        secondaryAction: Action = urlAction(),
    ) = Content.BigTwoActions(
        titleText = titleText,
        descriptionText = descriptionText,
        placeholder = placeholder,
        primaryActionText = primaryActionText,
        primaryAction = primaryAction,
        secondaryActionText = secondaryActionText,
        secondaryAction = secondaryAction,
    )

    fun promoSingleActionContent(
        titleText: String = "title",
        descriptionText: String = "description",
        placeholder: Placeholder = MAC_AND_WINDOWS,
        actionText: String = "Action",
        action: Action = urlAction(),
    ) = Content.PromoSingleAction(
        titleText = titleText,
        descriptionText = descriptionText,
        placeholder = placeholder,
        actionText = actionText,
        action = action,
    )

    fun aSmallMessage(
        id: String = "id",
        content: Content = smallContent(),
        exclusionRules: List<Int> = emptyList(),
        matchingRules: List<Int> = emptyList(),
    ): RemoteMessage {
        return RemoteMessage(
            id = id,
            content = content,
            exclusionRules = exclusionRules,
            matchingRules = matchingRules,
        )
    }

    fun aMediumMessage(
        id: String = "id",
        content: Content = mediumContent(),
        exclusionRules: List<Int> = emptyList(),
        matchingRules: List<Int> = emptyList(),
    ): RemoteMessage {
        return RemoteMessage(
            id = id,
            content = content,
            exclusionRules = exclusionRules,
            matchingRules = matchingRules,
        )
    }

    fun aBigSingleActionMessage(
        id: String = "id",
        content: Content = bigSingleActionContent(),
        exclusionRules: List<Int> = emptyList(),
        matchingRules: List<Int> = emptyList(),
    ): RemoteMessage {
        return RemoteMessage(
            id = id,
            content = content,
            exclusionRules = exclusionRules,
            matchingRules = matchingRules,
        )
    }

    fun aBigTwoActionsMessage(
        id: String = "id",
        content: Content = bigTwoActionsContent(),
        exclusionRules: List<Int> = emptyList(),
        matchingRules: List<Int> = emptyList(),
    ): RemoteMessage {
        return RemoteMessage(
            id = id,
            content = content,
            exclusionRules = exclusionRules,
            matchingRules = matchingRules,
        )
    }

    fun aPromoSingleActionMessage(
        id: String = "id",
        content: Content = promoSingleActionContent(),
        exclusionRules: List<Int> = emptyList(),
        matchingRules: List<Int> = emptyList(),
    ): RemoteMessage {
        return RemoteMessage(
            id = id,
            content = content,
            exclusionRules = exclusionRules,
            matchingRules = matchingRules,
        )
    }
}
