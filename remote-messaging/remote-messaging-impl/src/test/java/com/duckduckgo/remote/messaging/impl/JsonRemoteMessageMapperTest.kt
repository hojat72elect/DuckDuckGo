

package com.duckduckgo.remote.messaging.impl

import com.duckduckgo.remote.messaging.api.RemoteMessage
import com.duckduckgo.remote.messaging.fixtures.JsonRemoteMessageOM.aJsonMessage
import com.duckduckgo.remote.messaging.fixtures.JsonRemoteMessageOM.bigSingleActionJsonContent
import com.duckduckgo.remote.messaging.fixtures.JsonRemoteMessageOM.bigTwoActionJsonContent
import com.duckduckgo.remote.messaging.fixtures.JsonRemoteMessageOM.emptyJsonContent
import com.duckduckgo.remote.messaging.fixtures.JsonRemoteMessageOM.mediumJsonContent
import com.duckduckgo.remote.messaging.fixtures.JsonRemoteMessageOM.promoSingleActionJsonContent
import com.duckduckgo.remote.messaging.fixtures.JsonRemoteMessageOM.smallJsonContent
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.aBigSingleActionMessage
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.aBigTwoActionsMessage
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.aMediumMessage
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.aPromoSingleActionMessage
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.aSmallMessage
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.bigSingleActionContent
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.bigTwoActionsContent
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.mediumContent
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.promoSingleActionContent
import com.duckduckgo.remote.messaging.fixtures.RemoteMessageOM.smallContent
import com.duckduckgo.remote.messaging.fixtures.messageActionPlugins
import com.duckduckgo.remote.messaging.impl.mappers.mapToRemoteMessage
import com.duckduckgo.remote.messaging.impl.models.JsonContentTranslations
import com.duckduckgo.remote.messaging.impl.models.JsonRemoteMessage
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class JsonRemoteMessageMapperTest(private val testCase: TestCase) {

    @Test
    fun whenJsonMessageThenReturnMessage() {
        val remoteMessages = testCase.jsonRemoteMessages.mapToRemoteMessage(Locale.FRANCE, messageActionPlugins)

        assertEquals(testCase.expectedMessages, remoteMessages)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun parameters() = arrayOf(
            TestCase(
                listOf(
                    aJsonMessage(id = "id1", content = smallJsonContent()),
                    aJsonMessage(id = "id2", content = mediumJsonContent()),
                    aJsonMessage(id = "id3", content = bigSingleActionJsonContent()),
                    aJsonMessage(id = "id4", content = bigTwoActionJsonContent()),
                    aJsonMessage(id = "id5", content = promoSingleActionJsonContent()),
                ),
                listOf(
                    aSmallMessage(id = "id1"),
                    aMediumMessage(id = "id2"),
                    aBigSingleActionMessage(id = "id3"),
                    aBigTwoActionsMessage(id = "id4"),
                    aPromoSingleActionMessage(id = "id5"),
                ),
            ),
            TestCase(
                listOf(
                    aJsonMessage(id = "id1", content = emptyJsonContent()),
                    aJsonMessage(id = "id2", content = smallJsonContent()),
                    aJsonMessage(id = "id3", content = mediumJsonContent()),
                    aJsonMessage(id = "id4", content = bigSingleActionJsonContent()),
                    aJsonMessage(id = "id5", content = bigTwoActionJsonContent()),
                    aJsonMessage(id = "id6", content = promoSingleActionJsonContent()),
                ),
                listOf(
                    aSmallMessage(id = "id2"),
                    aMediumMessage(id = "id3"),
                    aBigSingleActionMessage(id = "id4"),
                    aBigTwoActionsMessage(id = "id5"),
                    aPromoSingleActionMessage(id = "id6"),
                ),
            ),
            TestCase(
                listOf(
                    aJsonMessage(id = "id1", content = emptyJsonContent()),
                    aJsonMessage(id = "id1", content = emptyJsonContent(messageType = "small")),
                    aJsonMessage(id = "id1", content = emptyJsonContent(messageType = "medium")),
                ),
                emptyList(),
            ),
            TestCase(
                listOf(
                    aJsonMessage(id = ""),
                    aJsonMessage(content = emptyJsonContent()),
                    aJsonMessage(content = null),
                ),
                emptyList(),
            ),
            TestCase(
                listOf(
                    aJsonMessage(
                        id = "id1",
                        content = smallJsonContent(),
                        translations = mapOf("fr" to frenchTranslations()),
                    ),
                    aJsonMessage(
                        id = "id2",
                        content = mediumJsonContent(),
                        translations = mapOf("fr" to frenchTranslations()),
                    ),
                    aJsonMessage(
                        id = "id3",
                        content = bigSingleActionJsonContent(),
                        translations = mapOf("fr" to frenchTranslations()),
                    ),
                    aJsonMessage(
                        id = "id4",
                        content = bigTwoActionJsonContent(),
                        translations = mapOf("fr" to frenchTranslations()),
                    ),
                    aJsonMessage(
                        id = "id5",
                        content = promoSingleActionJsonContent(),
                        translations = mapOf("fr" to frenchTranslations()),
                    ),
                ),
                listOf(
                    aSmallMessage(
                        id = "id1",
                        smallContent(titleText = frenchTranslations().titleText, descriptionText = frenchTranslations().descriptionText),
                    ),
                    aMediumMessage(
                        id = "id2",
                        mediumContent(
                            titleText = frenchTranslations().titleText,
                            descriptionText = frenchTranslations().descriptionText,
                        ),
                    ),
                    aBigSingleActionMessage(
                        id = "id3",
                        bigSingleActionContent(
                            titleText = frenchTranslations().titleText,
                            descriptionText = frenchTranslations().descriptionText,
                            primaryActionText = frenchTranslations().primaryActionText,
                        ),
                    ),
                    aBigTwoActionsMessage(
                        id = "id4",
                        bigTwoActionsContent(
                            titleText = frenchTranslations().titleText,
                            descriptionText = frenchTranslations().descriptionText,
                            primaryActionText = frenchTranslations().primaryActionText,
                            secondaryActionText = frenchTranslations().secondaryActionText,
                        ),
                    ),
                    aPromoSingleActionMessage(
                        id = "id5",
                        promoSingleActionContent(
                            titleText = frenchTranslations().titleText,
                            descriptionText = frenchTranslations().descriptionText,
                            actionText = frenchTranslations().actionText,
                        ),
                    ),
                ),
            ),
        )

        private fun frenchTranslations() = JsonContentTranslations(
            titleText = "Bonjour",
            descriptionText = "la description",
            primaryActionText = "action principale",
            secondaryActionText = "action secondaire",
            actionText = "action principale",
        )
    }

    data class TestCase(
        val jsonRemoteMessages: List<JsonRemoteMessage>,
        val expectedMessages: List<RemoteMessage>,
    )
}
