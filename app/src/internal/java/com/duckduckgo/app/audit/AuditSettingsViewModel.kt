

package com.duckduckgo.app.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.app.privacy.db.UserAllowListDao
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.ActivityScope
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ContributesViewModel(ActivityScope::class)
class AuditSettingsViewModel @Inject constructor(
    private val userAllowListDao: UserAllowListDao,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    data class ViewState(
        val nextTdsEnabled: Boolean = false,
        val startupTraceEnabled: Boolean = false,
    )

    sealed class Command {
        data class GoToUrl(val url: String) : Command()
    }

    private val command = Channel<Command>(1, BufferOverflow.DROP_OLDEST)

    fun commands(): Flow<Command> {
        return command.receiveAsFlow()
    }

    fun goToUrl(
        url: String,
        protectionsEnabled: Boolean = true,
    ) {
        viewModelScope.launch {
            if (protectionsEnabled) {
                reAddProtections()
            } else {
                removeProtections()
            }
            command.send(Command.GoToUrl(url))
        }
    }

    fun onDestroy() {
        viewModelScope.launch {
            reAddProtections()
        }
    }

    private suspend fun removeProtections() {
        withContext(dispatchers.io()) {
            domainsUsed.map {
                userAllowListDao.insert(it)
            }
        }
    }

    private suspend fun reAddProtections() {
        withContext(dispatchers.io()) {
            domainsUsed.map {
                userAllowListDao.delete(it)
            }
        }
    }

    companion object {
        const val STEP_1 = "https://cnn.com"
        const val STEP_2 = "https://gizmodo.com"
        const val STEP_3 = "https://httpbin.org/basic-auth/u/pw"
        const val REQUEST_BLOCKING = "https://privacy-test-pages.site/privacy-protections/request-blocking/?run"
        const val HTTPS_UPGRADES = "http://privacy-test-pages.site/privacy-protections/https-upgrades/?run"
        const val FIRE_BUTTON_STORE = "https://privacy-test-pages.site/privacy-protections/storage-blocking/?store"
        const val FIRE_BUTTON_RETRIEVE = "https://privacy-test-pages.site/privacy-protections/storage-blocking/?retrive"
        const val COOKIES_3P_STORE = "https://privacy-test-pages.site/privacy-protections/storage-blocking/?store"
        const val COOKIES_3P_RETRIEVE = "https://privacy-test-pages.site/privacy-protections/storage-blocking/?retrive"
        const val GPC = "https://privacy-test-pages.site/privacy-protections/gpc/?run"
        const val GPC_OTHER = "https://global-privacy-control.glitch.me/"
        const val SURROGATES = "https://privacy-test-pages.site/privacy-protections/surrogates/"
        val domainsUsed = listOf("privacy-test-pages.site", "privacy-test-pages.site")
    }
}
