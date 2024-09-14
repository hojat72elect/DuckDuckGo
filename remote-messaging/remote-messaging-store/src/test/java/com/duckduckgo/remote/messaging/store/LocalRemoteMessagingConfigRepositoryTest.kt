

package com.duckduckgo.remote.messaging.store

import java.time.LocalDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalRemoteMessagingConfigRepositoryTest {

    @Test
    fun whenRemoteConfigTimestampGreaterThan1DayThenConfigExpired() {
        val remoteMessagingConfig = RemoteMessagingConfig(
            version = 0,
            evaluationTimestamp = databaseTimestampFormatter().format(LocalDateTime.now().minusDays(2L)),
        )

        val expired = remoteMessagingConfig.expired()

        assertTrue(expired)
    }

    @Test
    fun whenRemoteConfigTimestampLessThan1DayThenConfigIsNotExpired() {
        val remoteMessagingConfig = RemoteMessagingConfig(
            version = 0,
            evaluationTimestamp = databaseTimestampFormatter().format(LocalDateTime.now().minusHours(15L)),
        )

        val expired = remoteMessagingConfig.expired()

        assertFalse(expired)
    }
}
