package com.duckduckgo.cookies.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CookiesFeatureNameUtilTest {

    @Test
    fun whenQueryHasWhenThenReturnRedactedBase64Stacktrace() {
        val ss = redactStacktraceInBase64(WITH_WHEN)
        assertEquals(WITH_WHEN_BASE634, ss)
    }

    @Test
    fun whenQueryDoesNotHaveWhenThenReturnBase64Stacktrace() {
        val ss = redactStacktraceInBase64(WITHOUT_WHEN)
        assertEquals(WITHOUT_WHEN_BASE64, ss)
    }

    @Test
    fun whenQueryOneLineThenReturnBase64Stacktrace() {
        val ss = redactStacktraceInBase64(WITHOUT_NEW_LINE)
        assertEquals(WITHOUT_NEW_LINE_BASE64, ss)
    }

    companion object {
        const val WITH_WHEN = """
            Error on line 1
            UPDATE test
            SET test=1
            WHERE test=0
            This is a test error
        """
        const val WITHOUT_WHEN = """
            Error on line 1
            This is a test error
        """

        const val WITHOUT_NEW_LINE =
            "Error on line 1 UPDATE test SET test=1 WHERE test=0 This is a test error "

        const val WITH_WHEN_BASE634 =
            "CiAgICAgICAgICAgIEVycm9yIG9uIGxpbmUgMQogICAgICAgICAgICBVUERBVEUgdGVzdAogICAgICAgICAgICBTRVQgdGVzdD0xCiAgICAgICAgICAgIAogICAgICAg" +
                    "ICAgICBUaGlzIGlzIGEgdGVzdCBlcnJvcgogICAgICAgIA"

        const val WITHOUT_WHEN_BASE64 =
            "CiAgICAgICAgICAgIEVycm9yIG9uIGxpbmUgMQogICAgICAgICAgICBUaGlzIGlzIGEgdGVzdCBlcnJvcgogICAgICAgIA"

        const val WITHOUT_NEW_LINE_BASE64 =
            "RXJyb3Igb24gbGluZSAxIFVQREFURSB0ZXN0IFNFVCB0ZXN0PTEgV0hFUkUgdGVzdD0wIFRoaXMgaXMgYSB0ZXN0IGVycm9yIA"
    }
}
