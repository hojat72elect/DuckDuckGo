package com.duckduckgo.mobile.android.vpn.ui.newtab

import android.annotation.SuppressLint
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ViewScope
import com.duckduckgo.feature.toggles.api.Toggle.State
import com.duckduckgo.mobile.android.vpn.pixels.DeviceShieldPixels
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("NoLifecycleObserver") // we don't observe app lifecycle
@ContributesViewModel(ViewScope::class)
class AppTrackingProtectionNewTabSettingsViewModel @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val setting: NewTabAppTrackingProtectionSectionSetting,
    private val pixel: DeviceShieldPixels,
) : ViewModel(), DefaultLifecycleObserver {

    private val _viewState = MutableStateFlow(ViewState(true))
    val viewState = _viewState.asStateFlow()

    data class ViewState(val enabled: Boolean)

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        viewModelScope.launch(dispatchers.io()) {
            val isEnabled = setting.self().isEnabled()
            withContext(dispatchers.main()) {
                _viewState.update { ViewState(isEnabled) }
            }
        }
    }

    fun onSettingEnabled(enabled: Boolean) {
        setting.self().setEnabled(State(enabled))
        pixel.reportNewTabSectionToggled(enabled)
    }
}
