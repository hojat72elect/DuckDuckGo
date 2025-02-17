

package com.duckduckgo.app.icon.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.duckduckgo.app.ValueCaptorObserver
import com.duckduckgo.app.icon.api.AppIcon
import com.duckduckgo.app.icon.api.IconModifier
import com.duckduckgo.app.pixels.AppPixelName
import com.duckduckgo.app.settings.db.SettingsDataStore
import com.duckduckgo.app.statistics.pixels.Pixel
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ChangeIconViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private var mockCommandObserver: Observer<ChangeIconViewModel.Command> = mock()
    private var mockSettingsDataStore: SettingsDataStore = mock()
    private var mockAppIconModifier: IconModifier = mock()
    private var mockPixel: Pixel = mock()
    private var viewStateObserver: Observer<ChangeIconViewModel.ViewState> = mock()

    private val testee: ChangeIconViewModel by lazy {
        val model = ChangeIconViewModel(mockSettingsDataStore, mockAppIconModifier, mockPixel)
        model.viewState.observeForever(viewStateObserver)
        model.command.observeForever(mockCommandObserver)
        model
    }

    @After
    fun after() {
        testee.viewState.removeObserver(viewStateObserver)
        testee.command.removeObserver(mockCommandObserver)
    }

    @Test
    fun whenStartsReturnsActualAppIcon() {
        val selectedIcon = AppIcon.DEFAULT
        whenever(mockSettingsDataStore.appIcon).thenReturn(selectedIcon)
        val observer = ValueCaptorObserver<ChangeIconViewModel.ViewState>()
        testee.viewState.observeForever(observer)

        testee.start()

        Mockito.verify(mockPixel).fire(AppPixelName.CHANGE_APP_ICON_OPENED)

        val viewState = testee.viewState.value!!
        assertTrue(viewState.appIcons.isNotEmpty())
        assertTrue(viewState.appIcons.find { it.selected }!!.appIcon.componentName == selectedIcon.componentName)
    }

    @Test
    fun whenIconIsSelectedDialogIsShown() {
        val selectedIcon = AppIcon.BLACK
        val selectedIconViewData = ChangeIconViewModel.IconViewData(selectedIcon, true)
        testee.onIconSelected(selectedIconViewData)

        verify(mockCommandObserver).onChanged(any<ChangeIconViewModel.Command.ShowConfirmationDialog>())
    }

    @Test
    fun whenIconIsConfirmedSettingsAreUpdated() {
        val previousIcon = AppIcon.BLUE
        val selectedIcon = AppIcon.BLACK
        val selectedIconViewData = ChangeIconViewModel.IconViewData(selectedIcon, true)

        whenever(mockSettingsDataStore.appIcon).thenReturn(previousIcon)

        testee.onIconConfirmed(selectedIconViewData)

        verify(mockAppIconModifier).changeIcon(previousIcon, selectedIcon)
        verify(mockSettingsDataStore).appIcon = selectedIcon
        verify(mockCommandObserver).onChanged(any<ChangeIconViewModel.Command.IconChanged>())
    }
}
