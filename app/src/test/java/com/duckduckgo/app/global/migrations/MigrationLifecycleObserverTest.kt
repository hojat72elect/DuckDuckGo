

package com.duckduckgo.app.global.migrations

import androidx.lifecycle.LifecycleOwner
import com.duckduckgo.app.global.migrations.MigrationLifecycleObserver.Companion.CURRENT_VERSION
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.common.utils.plugins.migrations.MigrationPlugin
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MigrationLifecycleObserverTest {

    private val mockMigrationStore: MigrationStore = mock()
    private val pluginPoint = FakeMigrationPluginPoint()

    private lateinit var testee: MigrationLifecycleObserver
    private val mockOwner: LifecycleOwner = mock()

    @Before
    fun before() {
        testee = MigrationLifecycleObserver(pluginPoint, mockMigrationStore)
    }

    @Test
    fun whenMigrateIfStoredVersionIsLowerThanCurrentThenRunMigrations() {
        whenever(mockMigrationStore.version).thenReturn(CURRENT_VERSION - 1)

        testee.onCreate(mockOwner)

        val plugin = pluginPoint.getPlugins().first() as FakeMigrationPlugin
        assertEquals(1, plugin.count)
    }

    @Test
    fun whenMigrateIfStoredVersionIsLowerThanCurrentThenStoreCurrentVersion() {
        whenever(mockMigrationStore.version).thenReturn(CURRENT_VERSION - 1)

        testee.onCreate(mockOwner)

        verify(mockMigrationStore).version = CURRENT_VERSION
    }

    @Test
    fun whenMigrateIfStoredVersionIsHigherThanCurrentThenDoNotRunMigrations() {
        whenever(mockMigrationStore.version).thenReturn(CURRENT_VERSION + 1)

        testee.onCreate(mockOwner)

        val plugin = pluginPoint.getPlugins().first() as FakeMigrationPlugin
        assertEquals(0, plugin.count)
    }

    @Test
    fun whenMigrateIfStoredVersionIsEqualsThanCurrentThenDoNotRunMigrations() {
        whenever(mockMigrationStore.version).thenReturn(CURRENT_VERSION)

        testee.onCreate(mockOwner)

        val plugin = pluginPoint.getPlugins().first() as FakeMigrationPlugin
        assertEquals(0, plugin.count)
    }

    internal class FakeMigrationPluginPoint : PluginPoint<MigrationPlugin> {
        val plugin = FakeMigrationPlugin()
        override fun getPlugins(): Collection<MigrationPlugin> {
            return listOf(plugin)
        }
    }

    internal class FakeMigrationPlugin(override val version: Int = CURRENT_VERSION) :
        MigrationPlugin {
        var count = 0

        override fun run() {
            count++
        }
    }
}
