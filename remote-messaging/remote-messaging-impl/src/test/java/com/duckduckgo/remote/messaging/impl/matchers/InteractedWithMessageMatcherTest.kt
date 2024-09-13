package com.duckduckgo.remote.messaging.impl.matchers

import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.remote.messaging.api.JsonMatchingAttribute
import com.duckduckgo.remote.messaging.api.RemoteMessagingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InteractedWithMessageMatcherTest {

    @get:Rule
    val coroutineTestRule: CoroutineTestRule = CoroutineTestRule()

    private val remoteMessagingRepository: RemoteMessagingRepository = mock()

    @Test
    fun whenMapKeyIsInteractedWithMessageThenReturnMatchingAttribute() = runTest {
        val matcher = InteractedWithMessageMatcher(remoteMessagingRepository)
        val jsonMatchingAttribute = JsonMatchingAttribute(value = listOf("1", "2", "3"))
        val result = matcher.map("interactedWithMessage", jsonMatchingAttribute)
        assertTrue(result is InteractedWithMessageMatchingAttribute)
        assertEquals(
            listOf("1", "2", "3"),
            (result as InteractedWithMessageMatchingAttribute).messageIds
        )
    }

    @Test
    fun whenJsonMatchingAttributeValueIsNullThenReturnNull() = runTest {
        val matcher = InteractedWithMessageMatcher(remoteMessagingRepository)
        val jsonMatchingAttribute = JsonMatchingAttribute(value = null)
        val result = matcher.map("interactedWithMessage", jsonMatchingAttribute)
        assertNull(result)
    }

    @Test
    fun whenJsonMatchingAttributeValueIsEmptyThenReturnNull() = runTest {
        val matcher = InteractedWithMessageMatcher(remoteMessagingRepository)
        val jsonMatchingAttribute = JsonMatchingAttribute(value = emptyList<String>())
        val result = matcher.map("interactedWithMessage", jsonMatchingAttribute)
        assertNull(result)
    }

    @Test
    fun whenJsonMatchingAttributeValueIsNotListThenReturnNull() = runTest {
        val matcher = InteractedWithMessageMatcher(remoteMessagingRepository)
        val jsonMatchingAttribute = JsonMatchingAttribute(value = 1)
        val result = matcher.map("interactedWithMessage", jsonMatchingAttribute)
        assertNull(result)
    }

    @Test
    fun whenDismissedMessageIdMatchesThenReturnTrue() = runTest {
        givenMessageIdDismissed(listOf("1", "2", "3"))
        val matcher = InteractedWithMessageMatcher(remoteMessagingRepository)
        val matchingAttribute = InteractedWithMessageMatchingAttribute(listOf("1", "2", "3"))
        val result = matcher.evaluate(matchingAttribute)!!
        assertTrue(result)
    }

    @Test
    fun whenOneDismissedMessageIdMatchesThenReturnTrue() = runTest {
        givenMessageIdDismissed(listOf("1", "2", "3"))
        val matcher = InteractedWithMessageMatcher(remoteMessagingRepository)
        val matchingAttribute = InteractedWithMessageMatchingAttribute(listOf("1", "4", "5"))
        val result = matcher.evaluate(matchingAttribute)!!
        assertTrue(result)
    }

    @Test
    fun whenNoDismissedMessageIdMatchesThenReturnFalse() = runTest {
        givenMessageIdDismissed(listOf("1", "2", "3"))
        val matcher = InteractedWithMessageMatcher(remoteMessagingRepository)
        val matchingAttribute = InteractedWithMessageMatchingAttribute(listOf("4", "5"))
        val result = matcher.evaluate(matchingAttribute)!!
        assertFalse(result)
    }

    @Test(expected = AssertionError::class)
    fun whenEmptyListInMatchingAttributeThenReturnException() = runTest {
        givenMessageIdDismissed(listOf("1", "2", "3"))
        val matcher = InteractedWithMessageMatcher(remoteMessagingRepository)
        val matchingAttribute = InteractedWithMessageMatchingAttribute(emptyList())
        matcher.evaluate(matchingAttribute)
    }

    private fun givenMessageIdDismissed(listOf: List<String>) {
        whenever(remoteMessagingRepository.dismissedMessages()).thenReturn(listOf)
    }
}
