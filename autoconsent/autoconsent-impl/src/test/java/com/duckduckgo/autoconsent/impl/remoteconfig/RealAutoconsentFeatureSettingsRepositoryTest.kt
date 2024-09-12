package com.duckduckgo.autoconsent.impl.remoteconfig

import com.duckduckgo.autoconsent.impl.remoteconfig.AutoconsentFeatureModels.AutoconsentSettings
import com.duckduckgo.autoconsent.impl.store.AutoconsentDao
import com.duckduckgo.autoconsent.impl.store.AutoconsentDatabase
import com.duckduckgo.autoconsent.impl.store.DisabledCmpsEntity
import com.duckduckgo.common.test.CoroutineTestRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RealAutoconsentFeatureSettingsRepositoryTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val mockDatabase: AutoconsentDatabase = mock()
    private val mockDao: AutoconsentDao = mock()

    lateinit var repository: AutoconsentFeatureSettingsRepository

    @Before
    fun before() {
        whenever(mockDatabase.autoconsentDao()).thenReturn(mockDao)
    }

    @Test
    fun whenRepositoryIsCreatedThenExceptionsLoadedIntoMemory() {
        givenDaoContainsDisabledCmps()

        repository = RealAutoconsentFeatureSettingsRepository(
            TestScope(),
            coroutineRule.testDispatcherProvider,
            mockDatabase,
            isMainProcess = true
        )

        assertEquals(disabledCmpName, repository.disabledCMPs.first())
    }

    @Test
    fun whenUpdateAllThenUpdateAllCalled() = runTest {
        repository = RealAutoconsentFeatureSettingsRepository(
            TestScope(),
            coroutineRule.testDispatcherProvider,
            mockDatabase,
            isMainProcess = true
        )

        repository.updateAllSettings(AutoconsentSettings(listOf()))

        verify(mockDao).updateAllDisabledCMPs(anyList())
    }

    @Test
    fun whenUpdateAllThenPreviousExceptionsAreCleared() = runTest {
        givenDaoContainsDisabledCmps()
        repository = RealAutoconsentFeatureSettingsRepository(
            TestScope(),
            coroutineRule.testDispatcherProvider,
            mockDatabase,
            isMainProcess = true
        )

        assertEquals(1, repository.disabledCMPs.size)

        reset(mockDao)

        repository.updateAllSettings(AutoconsentSettings(listOf()))

        assertEquals(0, repository.disabledCMPs.size)
    }

    private fun givenDaoContainsDisabledCmps() {
        whenever(mockDao.getDisabledCmps()).thenReturn(listOf(disabledCmp))
    }

    companion object {
        const val disabledCmpName = "disabledcmp"
        val disabledCmp = DisabledCmpsEntity(disabledCmpName)
    }
}
