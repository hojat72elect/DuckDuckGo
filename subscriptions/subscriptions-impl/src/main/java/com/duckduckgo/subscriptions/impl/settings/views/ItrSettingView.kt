

package com.duckduckgo.subscriptions.impl.settings.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.common.ui.view.gone
import com.duckduckgo.common.ui.view.show
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.ConflatedJob
import com.duckduckgo.common.utils.ViewViewModelFactory
import com.duckduckgo.di.scopes.ViewScope
import com.duckduckgo.navigation.api.GlobalActivityStarter
import com.duckduckgo.subscriptions.impl.SubscriptionsConstants
import com.duckduckgo.subscriptions.impl.databinding.ViewItrSettingsBinding
import com.duckduckgo.subscriptions.impl.settings.views.ItrSettingViewModel.Command
import com.duckduckgo.subscriptions.impl.settings.views.ItrSettingViewModel.Command.OpenItr
import com.duckduckgo.subscriptions.impl.settings.views.ItrSettingViewModel.ViewState
import com.duckduckgo.subscriptions.impl.ui.SubscriptionsWebViewActivityWithParams
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@InjectWith(ViewScope::class)
class ItrSettingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    @Inject
    lateinit var viewModelFactory: ViewViewModelFactory

    @Inject
    lateinit var globalActivityStarter: GlobalActivityStarter

    private var coroutineScope: CoroutineScope? = null

    private val binding: ViewItrSettingsBinding by viewBinding()

    private val viewModel: ItrSettingViewModel by lazy {
        ViewModelProvider(findViewTreeViewModelStoreOwner()!!, viewModelFactory)[ItrSettingViewModel::class.java]
    }

    private var job: ConflatedJob = ConflatedJob()

    override fun onAttachedToWindow() {
        AndroidSupportInjection.inject(this)
        super.onAttachedToWindow()

        findViewTreeLifecycleOwner()?.lifecycle?.addObserver(viewModel)

        binding.itrSettings.setClickListener {
            viewModel.onItr()
        }

        @SuppressLint("NoHardcodedCoroutineDispatcher")
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        job += viewModel.commands()
            .onEach { processCommands(it) }
            .launchIn(coroutineScope!!)

        viewModel.viewState
            .onEach { renderView(it) }
            .launchIn(coroutineScope!!)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        findViewTreeLifecycleOwner()?.lifecycle?.removeObserver(viewModel)
        coroutineScope?.cancel()
        job.cancel()
        coroutineScope = null
    }

    private fun renderView(viewState: ViewState) {
        if (viewState.hasSubscription) {
            binding.itrSettings.show()
        } else {
            binding.itrSettings.gone()
        }
    }

    private fun processCommands(command: Command) {
        when (command) {
            is OpenItr -> {
                globalActivityStarter.start(
                    context,
                    SubscriptionsWebViewActivityWithParams(
                        url = SubscriptionsConstants.ITR_URL,
                    ),
                )
            }
        }
    }
}
