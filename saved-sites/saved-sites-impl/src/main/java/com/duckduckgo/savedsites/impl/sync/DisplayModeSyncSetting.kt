

package com.duckduckgo.savedsites.impl.sync

import android.annotation.SuppressLint
import android.content.*
import android.util.*
import android.widget.*
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.lifecycle.*
import com.duckduckgo.anvil.annotations.*
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.ConflatedJob
import com.duckduckgo.di.scopes.*
import com.duckduckgo.saved.sites.impl.databinding.*
import com.duckduckgo.savedsites.impl.sync.DisplayModeViewModel.ViewState
import dagger.android.support.*
import javax.inject.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@InjectWith(ViewScope::class)
class DisplayModeSyncSetting @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : FrameLayout(context, attrs, defStyle) {

    @Inject
    lateinit var viewModelFactory: DisplayModeViewModel.Factory

    private var coroutineScope: CoroutineScope? = null

    private var job: ConflatedJob = ConflatedJob()

    private val binding: ViewSyncSettingDisplayModeBinding by viewBinding()

    private val viewModel: DisplayModeViewModel by lazy {
        ViewModelProvider(findViewTreeViewModelStoreOwner()!!, viewModelFactory)[DisplayModeViewModel::class.java]
    }

    override fun onAttachedToWindow() {
        AndroidSupportInjection.inject(this)
        super.onAttachedToWindow()

        binding.syncSettingsOptionFavourites.setOnCheckedChangeListener(
            object : OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    viewModel.onDisplayModeChanged(isChecked)
                }
            },
        )

        @SuppressLint("NoHardcodedCoroutineDispatcher")
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

        job += viewModel.viewState()
            .onEach { render(it) }
            .launchIn(coroutineScope!!)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope?.cancel()
        job.cancel()
        coroutineScope = null
    }

    private fun render(it: ViewState) {
        binding.syncSettingsOptionFavourites.setIsChecked(it.shareFavoritesEnabled)
    }
}
