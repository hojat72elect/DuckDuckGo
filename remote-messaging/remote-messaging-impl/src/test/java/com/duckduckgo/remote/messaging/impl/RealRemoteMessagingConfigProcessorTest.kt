

package com.duckduckgo.remote.messaging.impl

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.remote.messaging.api.RemoteMessagingRepository
import com.duckduckgo.remote.messaging.fixtures.JsonRemoteMessageOM.aJsonRemoteMessagingConfig
import com.duckduckgo.remote.messaging.fixtures.RemoteMessagingConfigOM.aRemoteMessagingConfig
import com.duckduckgo.remote.messaging.fixtures.jsonMatchingAttributeMappers
import com.duckduckgo.remote.messaging.fixtures.messageActionPlugins
import com.duckduckgo.remote.messaging.impl.mappers.RemoteMessagingConfigJsonMapper
import com.duckduckgo.remote.messaging.store.RemoteMessagingCohortStore
import com.duckduckgo.remote.messaging.store.RemoteMessagingConfigRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealRemoteMessagingConfigProcessorTest {

    @get:Rule var coroutineRule = CoroutineTestRule()

    private val appBuildConfig: AppBuildConfig = mock()
    private val remoteMessagingConfigJsonMapper = RemoteMessagingConfigJsonMapper(
        appBuildConfig,
        jsonMatchingAttributeMappers,
        messageActionPlugins,
    )
    private val remoteMessagingConfigRepository = mock<RemoteMessagingConfigRepository>()
    private val remoteMessagingRepository = mock<RemoteMessagingRepository>()
    private val remoteMessagingCohortStore = mock<RemoteMessagingCohortStore>()
    private val remoteMessagingConfigMatcher = RemoteMessagingConfigMatcher(setOf(mock(), mock(), mock()), mock(), remoteMessagingCohortStore)

    private val testee = RealRemoteMessagingConfigProcessor(
        remoteMessagingConfigJsonMapper,
        remoteMessagingConfigRepository,
        remoteMessagingRepository,
        remoteMessagingConfigMatcher,
    )

    @Before
    fun setup() {
        whenever(appBuildConfig.deviceLocale).thenReturn(Locale.US)
    }

    @Test
    fun whenNewVersionThenEvaluate() = runTest {
        whenever(remoteMessagingConfigRepository.get()).thenReturn(
            aRemoteMessagingConfig(version = 0L),
        )

        testee.process(aJsonRemoteMessagingConfig(version = 1L))

        verify(remoteMessagingConfigRepository).insert(any())
    }

    @Test
    fun whenSameVersionThenDoNothing() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        whenever(remoteMessagingConfigRepository.get()).thenReturn(
            aRemoteMessagingConfig(
                version = 1L,
                evaluationTimestamp = dateTimeFormatter.format(LocalDateTime.now()),
            ),
        )

        testee.process(aJsonRemoteMessagingConfig(version = 1L))

        verify(remoteMessagingConfigRepository, times(0)).insert(any())
    }

    @Test
    fun whenSameVersionButExpiredThenEvaluate() = runTest {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        whenever(remoteMessagingConfigRepository.get()).thenReturn(
            aRemoteMessagingConfig(
                version = 0L,
                evaluationTimestamp = dateTimeFormatter.format(LocalDateTime.now().minusDays(2L)),
            ),
        )

        testee.process(aJsonRemoteMessagingConfig(version = 1L))

        verify(remoteMessagingConfigRepository).insert(any())
    }

    @Test
    fun whenSameVersionButInvalidatedThenEvaluate() = runTest {
        whenever(remoteMessagingConfigRepository.get()).thenReturn(
            aRemoteMessagingConfig(
                version = 1L,
                invalidate = true,
            ),
        )

        testee.process(aJsonRemoteMessagingConfig(version = 1L))

        verify(remoteMessagingConfigRepository).insert(any())
    }
}
