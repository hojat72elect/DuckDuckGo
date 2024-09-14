

package com.duckduckgo.autofill.impl.jsbridge.response

import com.duckduckgo.autofill.impl.domain.javascript.JavascriptCredentials
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Test

class AutofillJsonResponseWriterTest {
    private val testee = AutofillJsonResponseWriter(Moshi.Builder().build())

    @Test
    fun whenGenerateResponseGetAutofillDataTheReturnAutofillDataJson() {
        val expectedJson = "{\n" +
            "  \"success\": {\n" +
            "    \"action\": \"fill\",\n" +
            "    \"credentials\": {\n" +
            "      \"password\": \"password\",\n" +
            "      \"username\": \"test\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"type\": \"getAutofillDataResponse\"\n" +
            "}"
        assertEquals(
            expectedJson,
            testee.generateResponseGetAutofillData(
                JavascriptCredentials(
                    username = "test",
                    password = "password",
                ),
            ),
        )
    }

    @Test
    fun whenGenerateEmptyResponseGetAutofillDataThenReturnEmptyResponseJson() {
        val expectedJson = "{\n" +
            "  \"success\": {\n" +
            "    \"action\": \"none\"\n" +
            "  },\n" +
            "  \"type\": \"getAutofillDataResponse\"\n" +
            "}"
        assertEquals(
            expectedJson,
            testee.generateEmptyResponseGetAutofillData(),
        )
    }
}
