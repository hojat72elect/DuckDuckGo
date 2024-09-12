package com.duckduckgo.app.browser.newtab

import android.content.Context
import android.view.View
import app.cash.turbine.test
import com.duckduckgo.common.utils.plugins.ActivePluginPoint
import com.duckduckgo.newtabpage.api.NewTabPagePlugin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class NewTabPageProviderTest {

    private lateinit var testee: NewTabPageProvider

    @Test
    fun whenLegacyPluginEnabledThenLegacyViewProvided() = runTest {
        testee = RealNewTabPageProvider(legacyPluginEnabled)

        testee.provideNewTabPageVersion().test {
            expectMostRecentItem().also {
                assertTrue(it is LegacyNewTabPlugin)
            }
        }
    }

    @Test
    fun whenNewPluginEnabledThenNewViewProvided() = runTest {
        testee = RealNewTabPageProvider(newPluginEnabled)

        testee.provideNewTabPageVersion().test {
            expectMostRecentItem().also {
                assertTrue(it is NewNewTabPlugin)
            }
        }
    }

    @Test
    fun whenNTPFirstPluginFirstEnabledThenLegacyViewProvided() = runTest {
        testee = RealNewTabPageProvider(ntpFirstPluginsEnabled)

        testee.provideNewTabPageVersion().test {
            expectMostRecentItem().also {
                assertTrue(it is NewNewTabPlugin)
            }
        }
    }

    @Test
    fun whenAllPluginsEnabledThenLegacyViewProvided() = runTest {
        testee = RealNewTabPageProvider(allPluginsEnabled)

        testee.provideNewTabPageVersion().test {
            expectMostRecentItem().also {
                assertTrue(it is LegacyNewTabPlugin)
            }
        }
    }

    @Test
    fun whenNoPluginsEnabledThenLegacyViewProvided() = runTest {
        testee = RealNewTabPageProvider(noPluginsEnabled)

        testee.provideNewTabPageVersion().test {
            expectMostRecentItem().also {
                assertTrue(it is NewTabLegacyPage)
            }
        }
    }

    private val legacyPluginEnabled = object : ActivePluginPoint<NewTabPagePlugin> {
        override suspend fun getPlugins(): Collection<NewTabPagePlugin> {
            return listOf(
                LegacyNewTabPlugin(),
            )
        }
    }

    private val newPluginEnabled = object : ActivePluginPoint<NewTabPagePlugin> {
        override suspend fun getPlugins(): Collection<NewTabPagePlugin> {
            return listOf(
                NewNewTabPlugin(),
            )
        }
    }

    private val allPluginsEnabled = object : ActivePluginPoint<NewTabPagePlugin> {
        override suspend fun getPlugins(): Collection<NewTabPagePlugin> {
            return listOf(
                LegacyNewTabPlugin(),
                NewNewTabPlugin(),
            )
        }
    }

    private val ntpFirstPluginsEnabled = object : ActivePluginPoint<NewTabPagePlugin> {
        override suspend fun getPlugins(): Collection<NewTabPagePlugin> {
            return listOf(
                NewNewTabPlugin(),
                LegacyNewTabPlugin(),
            )
        }
    }

    private val noPluginsEnabled = object : ActivePluginPoint<NewTabPagePlugin> {
        override suspend fun getPlugins(): Collection<NewTabPagePlugin> {
            return emptyList()
        }
    }

    class LegacyNewTabPlugin : NewTabPagePlugin {
        override fun getView(context: Context): View {
            return View(context)
        }
    }

    class NewNewTabPlugin() : NewTabPagePlugin {
        override fun getView(context: Context): View {
            return View(context)
        }
    }
}
