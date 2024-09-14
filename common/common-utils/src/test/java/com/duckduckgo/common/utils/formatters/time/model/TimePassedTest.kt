

package com.duckduckgo.common.utils.formatters.time.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimePassedTest {

    private val resources = getApplicationContext<Context>().resources

    @Test
    fun whenOnlyHoursPassedThenFormatsProperTime() {
        val timePassed = TimePassed(1, 0, 0)
        assertEquals("1 hr 0 min 0 sec", timePassed.format(resources = resources))
    }

    @Test
    fun whenOnlyMinutesPassedThenFormatsProperTime() {
        val timePassed = TimePassed(0, 10, 0)
        assertEquals("0 hr 10 min 0 sec", timePassed.format(resources = resources))
    }

    @Test
    fun whenOnlySecondsPassedThenFormatsProperTime() {
        val timePassed = TimePassed(0, 0, 25)
        assertEquals("0 hr 0 min 25 sec", timePassed.format(resources = resources))
    }

    @Test
    fun whenHoursAndMinutesPassedThenFormatsProperTime() {
        val timePassed = TimePassed(1, 10, 0)
        assertEquals("1 hr 10 min 0 sec", timePassed.format(resources = resources))
    }

    @Test
    fun whenHoursAndSecondsPassedThenFormatsProperTime() {
        val timePassed = TimePassed(1, 0, 30)
        assertEquals("1 hr 0 min 30 sec", timePassed.format(resources = resources))
    }

    @Test
    fun whenMinutesAndSecondsPassedThenFormatsProperTime() {
        val timePassed = TimePassed(0, 10, 10)
        assertEquals("0 hr 10 min 10 sec", timePassed.format(resources = resources))
    }

    @Test
    fun whenOnlyHoursPassedThenShortFormatsProperTime() {
        val timePassed = TimePassed(1, 0, 0)
        assertEquals("1h ago", timePassed.shortFormat(resources))
    }

    @Test
    fun whenOnlyMinutesPassedThenShortFormatsProperTime() {
        val timePassed = TimePassed(0, 10, 0)
        assertEquals("10m ago", timePassed.shortFormat(resources))
    }

    @Test
    fun whenOnlySecondsPassedThenShortFormatsProperTime() {
        val timePassed = TimePassed(0, 0, 45)
        assertEquals("Just Now", timePassed.shortFormat(resources))
    }

    @Test
    fun whenOnlyFewSecondsPassedThenShortFormatsProperTime() {
        val timePassed = TimePassed(0, 0, 25)
        assertEquals("Just Now", timePassed.shortFormat(resources))
    }

    @Test
    fun whenHoursAndMinutesPassedThenShortFormatsProperTime() {
        val timePassed = TimePassed(1, 10, 0)
        assertEquals("1h ago", timePassed.shortFormat(resources))
    }

    @Test
    fun whenHoursAndSecondsPassedThenShortFormatsProperTime() {
        val timePassed = TimePassed(1, 0, 30)
        assertEquals("1h ago", timePassed.shortFormat(resources))
    }

    @Test
    fun whenMinutesAndSecondsPassedShortThenFormatsProperTime() {
        val timePassed = TimePassed(0, 10, 10)
        assertEquals("10m ago", timePassed.shortFormat(resources))
    }
}
