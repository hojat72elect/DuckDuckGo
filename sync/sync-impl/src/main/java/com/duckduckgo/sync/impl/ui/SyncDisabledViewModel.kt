

package com.duckduckgo.sync.impl.ui

import android.annotation.SuppressLint
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.sync.api.SyncState
import com.duckduckgo.sync.api.SyncStateMonitor
import com.duckduckgo.sync.impl.R
import com.duckduckgo.sync.impl.SyncFeatureToggle
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map

@SuppressLint("NoLifecycleObserver") // does not subscribe to app lifecycle
class SyncDisabledViewModel(
    private val syncFeatureToggle: SyncFeatureToggle,
    private val syncStateMonitor: SyncStateMonitor,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel(), DefaultLifecycleObserver {

    data class ViewState(
        val message: Int? = null,
    )

    private val mutableViewState = MutableStateFlow(ViewState())

    fun viewState(): Flow<ViewState> = mutableViewState

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        syncStateMonitor.syncState().map { state ->
            mutableViewState.emit(mutableViewState.value.copy(message = getMessage(state)))
        }.flowOn(dispatcherProvider.io()).launchIn(viewModelScope)
    }

    private fun getMessage(state: SyncState): Int? {
        if (!syncFeatureToggle.allowDataSyncing()) {
            if (state == SyncState.OFF) {
                if (syncFeatureToggle.allowDataSyncingOnNewerVersion()) return R.string.sync_flows_disabled_new_version
                return R.string.sync_flows_disabled
            } else {
                if (syncFeatureToggle.allowDataSyncingOnNewerVersion()) return R.string.sync_disabled_authenticated_user_new_version
                return R.string.sync_disabled_authenticated_user
            }
        }

        if (state != SyncState.OFF) return null

        if (!syncFeatureToggle.allowSetupFlows()) {
            if (syncFeatureToggle.allowSetupFlowsOnNewerVersion()) return R.string.sync_flows_disabled_new_version
            return R.string.sync_flows_disabled
        }

        if (!syncFeatureToggle.allowCreateAccount()) {
            if (syncFeatureToggle.allowCreateAccountOnNewerVersion()) return R.string.sync_create_account_disabled_new_version
            return R.string.sync_create_account_disabled
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val syncFeatureToggle: SyncFeatureToggle,
        private val syncStateMonitor: SyncStateMonitor,
        private val dispatcherProvider: DispatcherProvider,
    ) : ViewModelProvider.NewInstanceFactory() {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return with(modelClass) {
                when {
                    isAssignableFrom(SyncDisabledViewModel::class.java) -> SyncDisabledViewModel(
                        syncFeatureToggle,
                        syncStateMonitor,
                        dispatcherProvider,
                    )
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T
        }
    }
}
