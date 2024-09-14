

package com.duckduckgo.privacy.config.store

import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealPrivacyConfigRepositoryTest {

    lateinit var testee: RealPrivacyConfigRepository

    private val mockDatabase: PrivacyConfigDatabase = mock()
    private val mockPrivacyConfigDao: PrivacyConfigDao = mock()

    @Before
    fun before() {
        whenever(mockDatabase.privacyConfigDao()).thenReturn(mockPrivacyConfigDao)
        testee = RealPrivacyConfigRepository(mockDatabase)
    }

    @Test
    fun whenInsertPrivacyConfigThenCallInsert() {
        val privacyConfig = PrivacyConfig(id = 1, version = 1, readme = "readme", eTag = "eTag", timestamp = "2023-01-01")

        testee.insert(privacyConfig)

        verify(mockPrivacyConfigDao).insert(privacyConfig)
    }

    @Test
    fun whenDeleteThenCallDelete() {
        testee.delete()

        verify(mockPrivacyConfigDao).delete()
    }

    @Test
    fun whenGetThenCallGet() {
        testee.get()

        verify(mockPrivacyConfigDao).get()
    }
}
