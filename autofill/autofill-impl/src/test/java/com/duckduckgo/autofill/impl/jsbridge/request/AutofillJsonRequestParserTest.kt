

package com.duckduckgo.autofill.impl.jsbridge.request

import com.duckduckgo.common.test.FileUtilities
import com.squareup.moshi.Moshi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AutofillJsonRequestParserTest {

    private val moshi = Moshi.Builder().build()
    private val testee = AutofillJsonRequestParser(moshi)

    @Test
    fun whenUsernameAndPasswordBothProvidedThenBothInResponse() = runTest {
        val parsed = "storeFormData_usernameAndPasswordProvided".parseStoreFormDataJson()
        assertEquals("dax@duck.com", parsed.credentials!!.username)
        assertEquals("123456", parsed.credentials!!.password)
    }

    @Test
    fun whenUsernameAndPasswordBothMissingThenBothAreNull() = runTest {
        val parsed = "storeFormData_usernameAndPasswordMissing".parseStoreFormDataJson()
        assertNull(parsed.credentials!!.username)
        assertNull(parsed.credentials!!.password)
    }

    @Test
    fun whenUsernameAndPasswordBothNullThenBothAreNullInParsedObject() = runTest {
        val parsed = "storeFormData_usernameAndPasswordNull".parseStoreFormDataJson()
        assertNull(parsed.credentials!!.username)
        assertNull(parsed.credentials!!.password)
    }

    @Test
    fun whenAdditionalUnknownPropertiesInRequestThenStillParses() = runTest {
        val parsed = "storeFormData_additionalUnknownPropertiesIncluded".parseStoreFormDataJson()
        assertEquals("dax@duck.com", parsed.credentials!!.username)
        assertEquals("123456", parsed.credentials!!.password)
    }

    @Test
    fun whenUsernameMissingThenPasswordPopulated() = runTest {
        val parsed = "storeFormData_usernameMissing".parseStoreFormDataJson()
        assertNull(parsed.credentials!!.username)
        assertEquals("123456", parsed.credentials!!.password)
    }

    @Test
    fun whenPasswordMissingThenUsernamePopulated() = runTest {
        val parsed = "storeFormData_passwordMissing".parseStoreFormDataJson()
        assertEquals("dax@duck.com", parsed.credentials!!.username)
        assertNull(parsed.credentials!!.password)
    }

    @Test
    fun whenTopLevelCredentialsObjectMissingThenParsesWithoutError() = runTest {
        val parsed = "storeFormData_topLevelDataMissing".parseStoreFormDataJson()
        assertNull(parsed.credentials)
    }

    @Test
    fun whenStoreFormDataRequestIsEmptyThenExceptionThrown() = runTest {
        val result = testee.parseStoreFormDataRequest("")
        assertTrue(result.isFailure)
    }

    @Test
    fun whenStoreFormDataRequestIsMalformedJSONThenExceptionThrown() = runTest {
        val result = testee.parseStoreFormDataRequest("invalid json")
        assertTrue(result.isFailure)
    }

    private suspend fun String.parseStoreFormDataJson(): AutofillStoreFormDataRequest {
        val json = this.loadJsonFile()
        assertNotNull("Failed to load specified JSON file: $this")
        return testee.parseStoreFormDataRequest(json).getOrThrow()
    }

    private fun String.loadJsonFile(): String {
        return FileUtilities.loadText(
            AutofillJsonRequestParserTest::class.java.classLoader!!,
            "json/$this.json",
        )
    }
}
