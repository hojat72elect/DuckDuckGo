

package com.duckduckgo.remote.messaging.impl

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.remote.messaging.api.Action
import com.duckduckgo.remote.messaging.api.Content
import com.duckduckgo.remote.messaging.api.Content.Placeholder.ANNOUNCE
import com.duckduckgo.remote.messaging.api.Content.Placeholder.APP_UPDATE
import com.duckduckgo.remote.messaging.api.Content.Placeholder.CRITICAL_UPDATE
import com.duckduckgo.remote.messaging.api.RemoteMessage
import com.duckduckgo.remote.messaging.fixtures.jsonMatchingAttributeMappers
import com.duckduckgo.remote.messaging.fixtures.messageActionPlugins
import com.duckduckgo.remote.messaging.impl.mappers.RemoteMessagingConfigJsonMapper
import com.duckduckgo.remote.messaging.impl.models.*
import com.duckduckgo.remote.messaging.impl.models.JsonRemoteMessagingConfig
import com.squareup.moshi.Moshi
import java.io.BufferedReader
import java.util.Locale.US
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RemoteMessagingConfigJsonMapperTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val appBuildConfig = mock<AppBuildConfig>().apply {
        whenever(this.deviceLocale).thenReturn(US)
    }

    @Test
    fun whenValidJsonParsedThenMessagesMappedIntoRemoteConfig() = runTest {
        val result = getConfigFromJson("json/remote_messaging_config.json")

        val testee = RemoteMessagingConfigJsonMapper(appBuildConfig, jsonMatchingAttributeMappers, messageActionPlugins)

        val config = testee.map(result)

        assertEquals(5, config.messages.size)
        val bigSingleActionMessage = RemoteMessage(
            id = "8274589c-8aeb-4322-a737-3852911569e3",
            content = Content.BigSingleAction(
                titleText = "title",
                descriptionText = "description",
                placeholder = ANNOUNCE,
                primaryActionText = "Ok",
                primaryAction = Action.Url(
                    value = "https://duckduckgo.com",
                ),
            ),
            matchingRules = emptyList(),
            exclusionRules = emptyList(),
        )
        assertEquals(bigSingleActionMessage, config.messages[0])

        val smallMessage = RemoteMessage(
            id = "26780792-49fe-4e25-ae27-aa6a2e6f013b",
            content = Content.Small(
                titleText = "Here goes a title",
                descriptionText = "description",
            ),
            matchingRules = listOf(5, 6),
            exclusionRules = listOf(7, 8, 9),
        )
        assertEquals(smallMessage, config.messages[2])

        val mediumMessage = RemoteMessage(
            id = "c3549d64-b388-41d8-9649-33e6e2674e8e",
            content = Content.Medium(
                titleText = "Here goes a title",
                descriptionText = "description",
                placeholder = CRITICAL_UPDATE,
            ),
            matchingRules = emptyList(),
            exclusionRules = emptyList(),
        )
        assertEquals(mediumMessage, config.messages[3])

        val bigTwoActions = RemoteMessage(
            id = "c2d0a1f1-6157-434f-8145-38416037d339",
            content = Content.BigTwoActions(
                titleText = "Here goes a title",
                descriptionText = "description",
                placeholder = APP_UPDATE,
                primaryActionText = "Ok",
                primaryAction = Action.PlayStore(
                    value = "com.duckduckgo.mobile.android",
                ),
                secondaryActionText = "Cancel",
                secondaryAction = Action.Dismiss,
            ),
            matchingRules = emptyList(),
            exclusionRules = emptyList(),
        )
        assertEquals(bigTwoActions, config.messages[4])
    }

    @Test
    fun whenValidJsonParsedThenRulesMappedIntoRemoteConfig() = runTest {
        val result = getConfigFromJson("json/remote_messaging_config.json")

        val testee = RemoteMessagingConfigJsonMapper(appBuildConfig, jsonMatchingAttributeMappers, messageActionPlugins)

        val config = testee.map(result)

        assertEquals(3, config.rules.size)

        val rule5Attributes = config.rules.find { it.id == 5 }?.attributes
        assertEquals(0.5f, config.rules.find { it.id == 5 }?.targetPercentile?.before)
        assertEquals(21, rule5Attributes?.size)
        val localeMA = Locale(listOf("en-US", "en-GB"), fallback = true)
        assertEquals(localeMA, rule5Attributes?.first())
        assertTrue(rule5Attributes?.get(1) is Api)

        val rule6Attributes = config.rules.find { it.id == 6 }?.attributes
        assertNull(config.rules.find { it.id == 6 }?.targetPercentile)
        val locale2MA = Locale(listOf("en-GB"), fallback = null)
        assertEquals(locale2MA, rule6Attributes?.first())
        assertEquals(1, rule6Attributes?.size)

        val rule7Attributes = config.rules.find { it.id == 7 }?.attributes
        val defaultBrowserMA = DefaultBrowser(value = true, fallback = null)
        assertNull(config.rules.find { it.id == 7 }?.targetPercentile)
        assertEquals(defaultBrowserMA, rule7Attributes?.first())
        assertEquals(1, rule7Attributes?.size)
    }

    @Test
    fun whenJsonMessagesHaveUnknownTypesThenMessagesNotMappedIntoConfig() = runTest {
        val result = getConfigFromJson("json/remote_messaging_config_unsupported_items.json")

        val testee = RemoteMessagingConfigJsonMapper(appBuildConfig, jsonMatchingAttributeMappers, messageActionPlugins)

        val config = testee.map(result)

        assertEquals(0, config.messages.size)
    }

    @Test
    fun whenJsonMessagesHaveUnknownTypesThenRulesMappedIntoConfig() = runTest {
        val result = getConfigFromJson("json/remote_messaging_config_unsupported_items.json")

        val testee = RemoteMessagingConfigJsonMapper(appBuildConfig, jsonMatchingAttributeMappers, messageActionPlugins)

        val config = testee.map(result)

        assertEquals(2, config.rules.size)

        val unknown = Unknown(fallback = true)
        assertEquals(unknown, config.rules.find { it.id == 6 }?.attributes?.get(0))

        val defaultBrowser = DefaultBrowser(value = true, fallback = null)
        assertEquals(defaultBrowser, config.rules.find { it.id == 7 }?.attributes?.get(0))
    }

    @Test
    fun whenJsonMessagesMalformedOrMissingInformationThenMessagesNotParsedIntoConfig() = runTest {
        val result = getConfigFromJson("json/remote_messaging_config_malformed.json")

        val testee = RemoteMessagingConfigJsonMapper(appBuildConfig, jsonMatchingAttributeMappers, messageActionPlugins)

        val config = testee.map(result)

        assertEquals(1, config.messages.size)
        val smallMessage = RemoteMessage(
            id = "26780792-49fe-4e25-ae27-aa6a2e6f013b",
            content = Content.Small(
                titleText = "Here goes a title",
                descriptionText = "description",
            ),
            matchingRules = listOf(5, 6),
            exclusionRules = listOf(7, 8, 9),
        )
        assertEquals(smallMessage, config.messages[0])
    }

    @Test
    fun whenJsonMatchingAttributesMalformedThenParsedAsUnknownIntoConfig() = runTest {
        val result = getConfigFromJson("json/remote_messaging_config_malformed.json")

        val testee = RemoteMessagingConfigJsonMapper(appBuildConfig, jsonMatchingAttributeMappers, messageActionPlugins)

        val config = testee.map(result)

        assertEquals(2, config.rules.size)
        assertEquals(3, config.rules.find { it.id == 6 }?.attributes?.size)

        val matchingAttr = listOf(Locale(), Unknown(fallback = true), WebView(fallback = false))
        assertEquals(matchingAttr, config.rules.find { it.id == 6 }?.attributes)
    }

    @Test
    fun whenUnknownMatchingAttributeDoesNotProvideFallbackThenFallbackIsNull() = runTest {
        val result = getConfigFromJson("json/remote_messaging_config_malformed.json")

        val testee = RemoteMessagingConfigJsonMapper(appBuildConfig, jsonMatchingAttributeMappers, messageActionPlugins)

        val config = testee.map(result)

        assertEquals(Unknown(null), config.rules.find { it.id == 7 }?.attributes?.first())
    }

    private fun getConfigFromJson(resourceName: String): JsonRemoteMessagingConfig {
        val jsonString = FileUtilities.loadText(resourceName)
        val moshi = Moshi.Builder().build()
        val jsonAdapter = moshi.adapter(JsonRemoteMessagingConfig::class.java)

        return jsonAdapter.fromJson(jsonString)!!
    }
}

object FileUtilities {

    fun loadText(resourceName: String): String = readResource(resourceName).use { it.readText() }

    private fun readResource(resourceName: String): BufferedReader {
        return javaClass.classLoader!!.getResource(resourceName).openStream().bufferedReader()
    }
}
