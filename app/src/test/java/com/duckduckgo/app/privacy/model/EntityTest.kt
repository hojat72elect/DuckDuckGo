

package com.duckduckgo.app.privacy.model

import com.duckduckgo.app.trackerdetection.model.Entity.Companion.MAJOR_NETWORK_PREVALENCE
import com.duckduckgo.app.trackerdetection.model.TdsEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityTest {

    @Test
    fun whenEntityPrevalenceIsGreaterThanMajorNetworkPrevalenceThenIsMajorIsTrue() {
        assertTrue(TdsEntity("", "", MAJOR_NETWORK_PREVALENCE + 1).isMajor)
    }

    @Test
    fun whenEntityPrevalenceLessOrEqualThanMajorNetworkPrevalenceThenIsMajorIsFalse() {
        assertFalse(TdsEntity("", "", MAJOR_NETWORK_PREVALENCE).isMajor)
    }
}
