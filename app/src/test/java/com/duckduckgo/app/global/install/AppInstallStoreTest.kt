

package com.duckduckgo.app.global.install

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppInstallStoreTest {

    var testee: AppInstallStore = mock()

    @Test
    fun whenInstallationTodayThenDayInstalledIsZero() {
        whenever(testee.installTimestamp).thenReturn(System.currentTimeMillis())
        assertEquals(0, testee.daysInstalled())
    }

    @Test
    fun whenDayAfterInstallationThenDayInstalledIsOne() {
        val timeSinceInstallation = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        whenever(testee.installTimestamp).thenReturn(timeSinceInstallation)
        assertEquals(1, testee.daysInstalled())
    }

    @Test
    fun whenAWeekAfterInstallationThenDayInstalledIsSeven() {
        val timeSinceInstallation = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        whenever(testee.installTimestamp).thenReturn(timeSinceInstallation)
        assertEquals(7, testee.daysInstalled())
    }
}
