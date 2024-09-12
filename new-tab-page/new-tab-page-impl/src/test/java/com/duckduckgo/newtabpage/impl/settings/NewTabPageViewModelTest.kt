package com.duckduckgo.newtabpage.impl.settings

import androidx.lifecycle.LifecycleOwner
import app.cash.turbine.test
import com.duckduckgo.common.test.CoroutineTestRule
import com.duckduckgo.newtabpage.api.NewTabPageSection
import com.duckduckgo.newtabpage.api.NewTabPageSectionProvider
import com.duckduckgo.newtabpage.impl.FakeEnabledSectionPlugin
import com.duckduckgo.newtabpage.impl.pixels.NewTabPixels
import com.duckduckgo.newtabpage.impl.view.NewTabPageViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NewTabPageViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    private val sectionProvider: NewTabPageSectionProvider = mock()
    private val pixels: NewTabPixels = mock()
    private val lifecycleOwner: LifecycleOwner = mock()

    private lateinit var testee: NewTabPageViewModel

    @Before
    fun setup() {
        testee =
            NewTabPageViewModel(sectionProvider, pixels, coroutinesTestRule.testDispatcherProvider)
    }

    @Test
    fun whenViewModelStartsThenCorrectStateEmitted() = runTest {
        whenever(sectionProvider.provideSections()).thenReturn(flowOf(emptyList()))
        testee.onResume(lifecycleOwner)
        testee.viewState.test {
            expectMostRecentItem().also {
                assertTrue(it.sections.isEmpty())
                assertFalse(it.loading)
                assertTrue(it.showDax)
            }
        }
    }

    @Test
    fun whenFavouritesShownThenDaxNotVisible() = runTest {
        whenever(sectionProvider.provideSections()).thenReturn(
            flowOf(
                listOf(
                    FakeEnabledSectionPlugin(
                        NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name,
                        true
                    ),
                    FakeEnabledSectionPlugin(NewTabPageSection.APP_TRACKING_PROTECTION.name, true),
                    FakeEnabledSectionPlugin(NewTabPageSection.FAVOURITES.name, true),
                ),
            ),
        )
        testee.onResume(lifecycleOwner)
        testee.viewState.test {
            expectMostRecentItem().also {
                assertTrue(it.sections.size == 3)
                assertFalse(it.loading)
                assertFalse(it.showDax)
            }
        }
    }

    @Test
    fun whenShortcutsShownThenDaxNotVisible() = runTest {
        whenever(sectionProvider.provideSections()).thenReturn(
            flowOf(
                listOf(
                    FakeEnabledSectionPlugin(
                        NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name,
                        true
                    ),
                    FakeEnabledSectionPlugin(NewTabPageSection.APP_TRACKING_PROTECTION.name, true),
                    FakeEnabledSectionPlugin(NewTabPageSection.SHORTCUTS.name, true),
                ),
            ),
        )
        testee.onResume(lifecycleOwner)
        testee.viewState.test {
            expectMostRecentItem().also {
                assertTrue(it.sections.size == 3)
                assertFalse(it.loading)
                assertFalse(it.showDax)
            }
        }
    }

    @Test
    fun whenShortcutsOrFavouritesNotShownThenDaxVisible() = runTest {
        whenever(sectionProvider.provideSections()).thenReturn(
            flowOf(
                listOf(
                    FakeEnabledSectionPlugin(
                        NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name,
                        true
                    ),
                    FakeEnabledSectionPlugin(NewTabPageSection.APP_TRACKING_PROTECTION.name, true),
                ),
            ),
        )
        testee.onResume(lifecycleOwner)
        testee.viewState.test {
            expectMostRecentItem().also {
                assertTrue(it.sections.size == 2)
                assertFalse(it.loading)
                assertTrue(it.showDax)
            }
        }
    }

    @Test
    fun whenSectionsProvidedThenCorrectStateEmitted() = runTest {
        whenever(sectionProvider.provideSections()).thenReturn(
            flowOf(
                listOf(
                    FakeEnabledSectionPlugin(
                        NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name,
                        true
                    ),
                    FakeEnabledSectionPlugin(NewTabPageSection.APP_TRACKING_PROTECTION.name, true),
                    FakeEnabledSectionPlugin(NewTabPageSection.FAVOURITES.name, true),
                    FakeEnabledSectionPlugin(NewTabPageSection.SHORTCUTS.name, true),
                ),
            ),
        )

        testee.onResume(lifecycleOwner)

        testee.viewState.test {
            expectMostRecentItem().also {
                assertTrue(it.sections.isNotEmpty())
                assertTrue(it.sections.size == 4)
                assertFalse(it.loading)
                assertFalse(it.showDax)
            }
        }
    }

    @Test
    fun whenCustomisePageClickedThenPixelSent() {
        testee.onCustomisePageClicked()

        verify(pixels).fireCustomizePagePressedPixel()
    }
}
