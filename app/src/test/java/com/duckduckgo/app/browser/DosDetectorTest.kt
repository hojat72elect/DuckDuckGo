

package com.duckduckgo.app.browser

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.browser.DosDetector.Companion.DOS_TIME_WINDOW_MS
import com.duckduckgo.app.browser.DosDetector.Companion.MAX_REQUESTS_COUNT
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DosDetectorTest {

    val testee: DosDetector = DosDetector()

    @Test
    fun whenLessThanMaxRequestsCountCallsWithSameUrlThenReturnFalse() {
        for (i in 0 until MAX_REQUESTS_COUNT) {
            assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
        }
    }

    @Test
    fun whenMoreThanMaxRequestsCountCallsWithSameUrlThenLastCallReturnsTrue() {
        for (i in 0..MAX_REQUESTS_COUNT) {
            assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
        }
        assertTrue(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
    }

    @Test
    fun whenMoreThanMaxRequestsCountCallsWithSameUrlAndDelayGreaterThanLimitThenReturnFalse() {
        runBlocking {
            for (i in 0..MAX_REQUESTS_COUNT) {
                assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
            }
            delay((DOS_TIME_WINDOW_MS + 100).toLong())
            assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
        }
    }

    @Test
    fun whenMoreThanMaxRequestsCountCallsWithSameUrlAndDelayGreaterThanLimitThenCountIsResetSoNextAndSubsequentRequestsReturnFalse() {
        runBlocking {
            for (i in 0..MAX_REQUESTS_COUNT) {
                assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
            }
            delay((DOS_TIME_WINDOW_MS + 100).toLong())
            assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
            assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
        }
    }

    @Test
    fun whenMultipleRequestsFromDifferentUrlsThenReturnFalse() {
        for (i in 0 until MAX_REQUESTS_COUNT * 2) {
            if (i % 2 == 0) {
                assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
            } else {
                assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example2.com")))
            }
        }
    }

    @Test
    fun whenMaxRequestsReceivedConsecutivelyFromDifferentUrlsThenReturnFalse() {
        for (i in 0 until MAX_REQUESTS_COUNT) {
            assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example.com")))
        }
        for (i in 0 until MAX_REQUESTS_COUNT) {
            assertFalse(testee.isUrlGeneratingDos(Uri.parse("http://example2.com")))
        }
    }
}
