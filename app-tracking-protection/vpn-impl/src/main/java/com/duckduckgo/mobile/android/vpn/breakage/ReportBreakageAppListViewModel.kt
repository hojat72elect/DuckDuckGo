

package com.duckduckgo.mobile.android.vpn.breakage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.mobile.android.vpn.apps.TrackingProtectionAppsRepository
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ContributesViewModel(ActivityScope::class)
class ReportBreakageAppListViewModel @Inject constructor(
    private val trackingProtectionAppsRepository: TrackingProtectionAppsRepository,
) : ViewModel() {

    private val selectedAppFlow = MutableStateFlow<InstalledApp?>(null)

    private val command = Channel<ReportBreakageAppListView.Command>(1, BufferOverflow.DROP_OLDEST)
    internal fun commands(): Flow<ReportBreakageAppListView.Command> = command.receiveAsFlow()

    internal suspend fun getInstalledApps(): Flow<ReportBreakageAppListView.State> {
        return trackingProtectionAppsRepository.getAppsAndProtectionInfo()
            .combine(selectedAppFlow.asStateFlow()) { apps, selectedApp ->
                val installedApps = apps.map { InstalledApp(it.packageName, it.name) }
                selectedApp?.let { appSelected ->
                    installedApps.update(appSelected)
                } ?: installedApps
            }
            .map { ReportBreakageAppListView.State(installedApps = it, canSubmit = it.hasSelected()) }
    }

    internal fun onAppSelected(app: InstalledApp) {
        viewModelScope.launch {
            selectedAppFlow.emit(app.copy(isSelected = true))
        }
    }

    internal fun onSubmitBreakage() {
        viewModelScope.launch {
            selectedAppFlow.value?.let { command.send(ReportBreakageAppListView.Command.LaunchBreakageForm(it)) }
        }
    }

    internal fun onBreakageSubmitted(issueReport: IssueReport) {
        viewModelScope.launch {
            selectedAppFlow.value?.let {
                command.send(ReportBreakageAppListView.Command.SendBreakageInfo(issueReport.copy(appPackageId = it.packageName)))
            }
        }
    }

    private fun List<InstalledApp>.update(newValue: InstalledApp): List<InstalledApp> {
        return toMutableList().map {
            if (it.packageName == newValue.packageName) newValue else it
        }
    }

    private fun List<InstalledApp>.hasSelected() = find { it.isSelected } != null
}
