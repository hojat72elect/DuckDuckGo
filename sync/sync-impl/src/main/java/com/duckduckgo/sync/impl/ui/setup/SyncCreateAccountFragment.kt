

package com.duckduckgo.sync.impl.ui.setup

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.common.ui.DuckDuckGoFragment
import com.duckduckgo.common.ui.view.dialog.TextAlertDialogBuilder
import com.duckduckgo.common.ui.view.show
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.FragmentViewModelFactory
import com.duckduckgo.di.scopes.FragmentScope
import com.duckduckgo.sync.impl.R
import com.duckduckgo.sync.impl.R.string
import com.duckduckgo.sync.impl.databinding.FragmentCreateAccountBinding
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.Command
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.Command.AbortFlow
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.Command.Error
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.Command.FinishSetupFlow
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.Command.ShowError
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.ViewMode.CreatingAccount
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.ViewMode.SignedIn
import com.duckduckgo.sync.impl.ui.setup.SyncCreateAccountViewModel.ViewState
import com.google.android.material.snackbar.Snackbar
import javax.inject.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@InjectWith(FragmentScope::class)
class SyncCreateAccountFragment : DuckDuckGoFragment(R.layout.fragment_create_account) {
    @Inject
    lateinit var viewModelFactory: FragmentViewModelFactory

    private val binding: FragmentCreateAccountBinding by viewBinding()

    private val viewModel: SyncCreateAccountViewModel by lazy {
        ViewModelProvider(this, viewModelFactory)[SyncCreateAccountViewModel::class.java]
    }

    private val listener: SetupFlowListener?
        get() = activity as? SetupFlowListener

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        observeUiEvents()
    }

    private fun observeUiEvents() {
        viewModel
            .viewState()
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach { viewState -> renderViewState(viewState) }
            .launchIn(lifecycleScope)

        viewModel
            .commands()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { processCommand(it) }
            .launchIn(lifecycleScope)
    }

    private fun renderViewState(viewState: ViewState) {
        when (viewState.viewMode) {
            is SignedIn -> {
                listener?.launchRecoveryCodeScreen()
            }
            is CreatingAccount -> {
                binding.connecting.show()
            }
        }
    }

    private fun processCommand(it: Command) {
        when (it) {
            AbortFlow -> {
                requireActivity().setResult(Activity.RESULT_CANCELED)
                requireActivity().finish()
            }
            FinishSetupFlow -> listener?.launchRecoveryCodeScreen()
            Error -> {
                Snackbar.make(binding.root, string.sync_general_error, Snackbar.LENGTH_LONG).show()
            }

            is ShowError -> showError(it)
        }
    }

    private fun showError(it: ShowError) {
        val context = context ?: return
        TextAlertDialogBuilder(context)
            .setTitle(R.string.sync_dialog_error_title)
            .setMessage(getString(it.message) + "\n" + it.reason)
            .setPositiveButton(R.string.sync_dialog_error_ok)
            .addEventListener(
                object : TextAlertDialogBuilder.EventListener() {
                    override fun onPositiveButtonClicked() {
                        viewModel.onErrorDialogDismissed()
                    }
                },
            ).show()
    }

    companion object {
        fun instance() = SyncCreateAccountFragment()
    }
}
