

package com.duckduckgo.common.utils.formatters.data

import java.text.NumberFormat
import java.util.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DataSizeFormatterTest {

    private lateinit var testee: DataSizeFormatter

    @Before
    fun setup() {
        testee = DataSizeFormatter(NumberFormat.getNumberInstance(Locale.US).also { it.maximumFractionDigits = 1 })
    }

    @Test
    fun whenNoDataThen0BytesReturned() {
        assertEquals("0 bytes", testee.format(0))
    }

    @Test
    fun whenLessThat1KbThenBytesReturned() {
        assertEquals("100 bytes", testee.format(100))
    }

    @Test
    fun whenExactlyOn1KbThenKbReturned() {
        assertEquals("1 KB", testee.format(1000))
    }

    @Test
    fun whenNotAWholeNumberOfKilobytesThenKbReturned() {
        assertEquals("1.5 KB", testee.format(1501))
    }

    @Test
    fun whenExactly1MegabyteThenMbReturned() {
        assertEquals("1 MB", testee.format(1_000_000))
    }

    @Test
    fun whenExactly1GigabyteThenGbReturned() {
        assertEquals("1 GB", testee.format(1_000_000_000))
    }
}
