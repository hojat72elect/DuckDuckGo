

package com.duckduckgo.httpsupgrade.impl

import androidx.test.platform.app.InstrumentationRegistry
import com.duckduckgo.httpsupgrade.impl.BloomFilter.Config.ProbabilityConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BloomFilterTest {

    private lateinit var testee: BloomFilter
    private val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @Test
    fun whenBloomFilterEmptyThenContainsIsFalse() {
        testee = BloomFilter(context, ProbabilityConfig(FILTER_ELEMENT_COUNT, TARGET_ERROR_RATE))
        assertFalse(testee.contains("abc"))
    }

    @Test
    fun whenBloomFilterContainsElementThenContainsIsTrue() {
        testee = BloomFilter(context, ProbabilityConfig(FILTER_ELEMENT_COUNT, TARGET_ERROR_RATE))
        testee.add("abc")
        assertTrue(testee.contains("abc"))
    }

    companion object {
        const val FILTER_ELEMENT_COUNT = 5000
        const val TARGET_ERROR_RATE = 0.001
    }
}
