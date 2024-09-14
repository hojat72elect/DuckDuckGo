

package com.duckduckgo.autofill.sync

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.autofill.api.AutofillScreens.AutofillSettingsScreen
import com.duckduckgo.autofill.api.AutofillSettingsLaunchSource
import com.duckduckgo.autofill.impl.databinding.ViewCredentialsSyncPausedWarningBinding
import com.duckduckgo.autofill.sync.CredentialsSyncPausedViewModel.Command
import com.duckduckgo.autofill.sync.CredentialsSyncPausedViewModel.Command.NavigateToCredentials
import com.duckduckgo.autofill.sync.CredentialsSyncPausedViewModel.ViewState
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.ConflatedJob
import com.duckduckgo.common.utils.ViewViewModelFactory
import com.duckduckgo.di.scopes.ViewScope
import com.duckduckgo.navigation.api.GlobalActivityStarter
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@InjectWith(ViewScope::class)
class CredentialsSyncPausedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    @Inject
    lateinit var globalActivityStarter: GlobalActivityStarter

    @Inject
    lateinit var viewModelFactory: ViewViewModelFactory

    private var coroutineScope: CoroutineScope? = null

    private var job: ConflatedJob = ConflatedJob()

    private val binding: ViewCredentialsSyncPausedWarningBinding by viewBinding()

    private val viewModel: CredentialsSyncPausedViewModel by lazy {
        ViewModelProvider(findViewTreeViewModelStoreOwner()!!, viewModelFactory)[CredentialsSyncPausedViewModel::class.java]
    }

    override fun onAttachedToWindow() {
        AndroidSupportInjection.inject(this)
        super.onAttachedToWindow()

        findViewTreeLifecycleOwner()?.lifecycle?.addObserver(viewModel)

        @SuppressLint("NoHardcodedCoroutineDispatcher")
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        viewModel.viewState()
            .onEach { render(it) }
            .launchIn(coroutineScope!!)

        job += viewModel.commands()
            .onEach { processCommands(it) }
            .launchIn(coroutineScope!!)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(viewModel)
        coroutineScope?.cancel()
        job.cancel()
        coroutineScope = null
    }

    private fun processCommands(command: Command) {
        when (command) {
            NavigateToCredentials -> navigateToCredentials()
        }
    }

    private fun render(viewState: ViewState) {
        if (viewState.message != null) {
            this.isVisible = true
            binding.credentialsSyncPausedWarning.setClickableLink(
                WARNING_ACTION_ANNOTATION,
                context.getText(viewState.message),
                onClick = {
                    viewModel.onWarningActionClicked()
                },
            )
        } else {
            this.isVisible = false
        }
    }

    private fun navigateToCredentials() {
        globalActivityStarter.start(this.context, AutofillSettingsScreen(source = AutofillSettingsLaunchSource.Sync))
    }

    companion object {
        const val WARNING_ACTION_ANNOTATION = "manage_logins"
    }
}
