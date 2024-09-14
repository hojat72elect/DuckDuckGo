

package com.duckduckgo.autofill.impl.ui.credential.saving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.autofill.impl.email.incontext.EmailProtectionInContextSignupViewModel.ViewState
import com.duckduckgo.autofill.impl.store.InternalAutofillStore
import com.duckduckgo.autofill.impl.store.NeverSavedSiteRepository
import com.duckduckgo.autofill.impl.ui.credential.saving.declines.AutofillDeclineCounter
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.FragmentScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesViewModel(FragmentScope::class)
class AutofillSavingCredentialsViewModel @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val neverSavedSiteRepository: NeverSavedSiteRepository,
    private val autofillStore: InternalAutofillStore,
    private val autofillDeclineCounter: AutofillDeclineCounter,
) : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState())

    init {
        viewModelScope.launch(dispatchers.io()) {
            val shouldShowExpandedView = autofillDeclineCounter.declineCount() < 2 && autofillDeclineCounter.isDeclineCounterActive()
            _viewState.value = ViewState(shouldShowExpandedView)
            Timber.d("Autofill: AutofillSavingCredentialsViewModel initialized")
        }
    }

    val viewState: Flow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val expandedDialog: Boolean = true,
    )

    fun userPromptedToSaveCredentials() {
        viewModelScope.launch(dispatchers.io()) {
            autofillStore.hasEverBeenPromptedToSaveLogin = true
        }
    }

    fun addSiteToNeverSaveList(originalUrl: String) {
        Timber.d("Autofill: User selected to never save for this site %s", originalUrl)
        viewModelScope.launch(dispatchers.io()) {
            neverSavedSiteRepository.addToNeverSaveList(originalUrl)
        }
    }
}
