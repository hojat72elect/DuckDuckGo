

package com.duckduckgo.app.global.migrations

import com.duckduckgo.app.settings.db.SettingsDataStore
import com.duckduckgo.privacy.config.api.Gpc
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GpcMigrationPluginTest {

    private val mockSettingsDataStore: SettingsDataStore = mock()
    private val mockGpc: Gpc = mock()

    private lateinit var testee: GpcMigrationPlugin

    @Before
    fun before() {
        testee = GpcMigrationPlugin(mockSettingsDataStore, mockGpc)
    }

    @Test
    fun whenRunIfPreviousSettingWasEnabledThenEnableGpc() {
        whenever(mockSettingsDataStore.globalPrivacyControlEnabled).thenReturn(true)
        testee.run()
        verify(mockGpc).enableGpc()
    }

    @Test
    fun whenRunIfPreviousSettingWasDisabledThenDisableGpc() {
        whenever(mockSettingsDataStore.globalPrivacyControlEnabled).thenReturn(false)
        testee.run()
        verify(mockGpc).disableGpc()
    }
}
