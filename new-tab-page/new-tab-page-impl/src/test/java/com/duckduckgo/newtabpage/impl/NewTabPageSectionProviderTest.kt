package com.duckduckgo.newtabpage.impl

import app.cash.turbine.test
import com.duckduckgo.newtabpage.api.NewTabPageSection
import com.duckduckgo.newtabpage.api.NewTabPageSectionProvider
import com.duckduckgo.newtabpage.impl.settings.NewTabSettingsStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NewTabPageSectionProviderTest {

    private lateinit var testee: NewTabPageSectionProvider

    private var newTabSettingsStore: NewTabSettingsStore = mock()

    @Before
    fun setup() {
        whenever(newTabSettingsStore.sectionSettings).thenReturn(
            listOf(
                NewTabPageSection.APP_TRACKING_PROTECTION.name,
                NewTabPageSection.FAVOURITES.name,
                NewTabPageSection.SHORTCUTS.name,
            ),
        )
    }

    @Test
    fun whenNoSectionsActiveThenNoPluginsReturned() = runTest {
        testee = RealNewTabPageSectionProvider(
            disabledSectionPlugins,
            disabledSectionSettingsPlugins,
            newTabSettingsStore,
        )

        testee.provideSections().test {
            expectMostRecentItem().also {
                assertTrue(it.isEmpty())
            }
        }
    }

    @Test
    fun whenSectionsAllActiveThenPluginsReturnedInOrder() = runTest {
        testee = RealNewTabPageSectionProvider(
            enabledSectionPlugins,
            activeSectionSettingsPlugins,
            newTabSettingsStore,
        )

        testee.provideSections().test {
            expectMostRecentItem().also {
                assertTrue(it[0].name == NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name)
                assertTrue(it[1].name == NewTabPageSection.APP_TRACKING_PROTECTION.name)
                assertTrue(it[2].name == NewTabPageSection.FAVOURITES.name)
                assertTrue(it[3].name == NewTabPageSection.SHORTCUTS.name)
            }
        }
    }

    @Test
    fun whenUserDisabledFavoritesThenPluginsReturnedInOrder() = runTest {
        whenever(newTabSettingsStore.sectionSettings).thenReturn(
            listOf(
                NewTabPageSection.APP_TRACKING_PROTECTION.name,
                NewTabPageSection.SHORTCUTS.name,
            ),
        )

        testee = RealNewTabPageSectionProvider(
            enabledSectionPlugins,
            activeSectionSettingsPlugins,
            newTabSettingsStore,
        )

        testee.provideSections().test {
            expectMostRecentItem().also {
                assertTrue(it[0].name == NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name)
                assertTrue(it[1].name == NewTabPageSection.APP_TRACKING_PROTECTION.name)
                assertTrue(it[2].name == NewTabPageSection.SHORTCUTS.name)
            }
        }
    }

    @Test
    fun whenRemoteDisabledFavoritesThenPluginsReturnedInOrder() = runTest {
        whenever(newTabSettingsStore.sectionSettings).thenReturn(
            listOf(
                NewTabPageSection.APP_TRACKING_PROTECTION.name,
                NewTabPageSection.FAVOURITES.name,
                NewTabPageSection.SHORTCUTS.name,
            ),
        )

        testee = RealNewTabPageSectionProvider(
            favoriteDisabledSectionPlugins,
            activeSectionSettingsPlugins,
            newTabSettingsStore,
        )

        testee.provideSections().test {
            expectMostRecentItem().also {
                assertTrue(it[0].name == NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name)
                assertTrue(it[1].name == NewTabPageSection.APP_TRACKING_PROTECTION.name)
                assertTrue(it[2].name == NewTabPageSection.SHORTCUTS.name)
            }
        }
    }

    @Test
    fun whenDisabledFavoritesSettingThenPluginsReturnedInOrder() = runTest {
        whenever(newTabSettingsStore.sectionSettings).thenReturn(
            listOf(
                NewTabPageSection.APP_TRACKING_PROTECTION.name,
                NewTabPageSection.SHORTCUTS.name,
            ),
        )

        testee = RealNewTabPageSectionProvider(
            enabledSectionsPlugins,
            activeSectionSettingsPlugins,
            newTabSettingsStore,
        )

        testee.provideSections().test {
            expectMostRecentItem().also {
                assertTrue(it[0].name == NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name)
                assertTrue(it[1].name == NewTabPageSection.APP_TRACKING_PROTECTION.name)
                assertTrue(it[2].name == NewTabPageSection.SHORTCUTS.name)
            }
        }
    }
}
