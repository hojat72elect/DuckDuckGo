

package com.duckduckgo.voice.impl.listeningmode

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.core.view.postDelayed
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.duckduckgo.anvil.annotations.InjectWith
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.extensions.capitalizeFirstLetter
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.voice.impl.R
import com.duckduckgo.voice.impl.databinding.ActivityVoiceSearchBinding
import com.duckduckgo.voice.impl.listeningmode.VoiceSearchViewModel.Command
import com.duckduckgo.voice.impl.listeningmode.ui.VoiceRecognizingIndicator.Action.INDICATOR_CLICKED
import com.duckduckgo.voice.impl.listeningmode.ui.VoiceRecognizingIndicator.Model
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@InjectWith(ActivityScope::class)
class VoiceSearchActivity : DuckDuckGoActivity() {
    companion object {
        const val EXTRA_VOICE_RESULT = "extra.voice.result"
        const val DELAY_SPEAKNOW_REMINDER_MILLIS = 2000L
        const val VOICE_SEARCH_ERROR = 1
    }

    @Inject lateinit var appBuildConfig: AppBuildConfig

    private val viewModel: VoiceSearchViewModel by bindViewModel()
    private val binding: ActivityVoiceSearchBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0, 0)
        makeBackgroundTransparent()
        setContentView(binding.root)
        configureToolbar()
        configureViews()
        observeViewModel()
    }

    @SuppressLint("NewApi")
    private fun configureViews() {
        if (appBuildConfig.sdkInt >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
        binding.indicator.onAction {
            if (it == INDICATOR_CLICKED) {
                viewModel.userInitiatesSearchComplete()
            }
        }
        binding.speechResults.let {
            it.postDelayed(DELAY_SPEAKNOW_REMINDER_MILLIS) {
                if (it.text.isEmpty()) it.text = getString(R.string.voiceSearchListening)
            }
        }
    }

    private fun configureToolbar() {
        setupToolbar(binding.toolbar)
        supportActionBar?.title = ""
    }

    @SuppressLint("NewApi")
    private fun makeBackgroundTransparent() {
        if (appBuildConfig.sdkInt >= Build.VERSION_CODES.S) {
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setTranslucent(true)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startVoiceSearch()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopVoiceSearch()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) finish()
    }

    override fun onPause() {
        super.onPause()
        binding.indicator.destroy()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    private fun observeViewModel() {
        viewModel.viewState()
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach {
                if (it.result.isNotEmpty()) updateText(it.result)
            }.launchIn(lifecycleScope)

        viewModel.commands()
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach {
                when (it) {
                    is Command.UpdateVoiceIndicator -> handleVolume(it.volume)
                    is Command.HandleSpeechRecognitionSuccess -> handleSuccess(it.result)
                    is Command.TerminateVoiceSearch -> handleError(it.error)
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun handleVolume(normalizedVolume: Float) {
        if (normalizedVolume != 0f) binding.indicator.bind(Model(normalizedVolume))
    }

    private fun handleSuccess(result: String) {
        if (result.isNotEmpty()) {
            updateText(result)
            Intent().apply {
                putExtra(EXTRA_VOICE_RESULT, result.capitalizeFirstLetter())
                setResult(Activity.RESULT_OK, this)
            }
        }
        finish()
    }

    private fun handleError(error: Int) {
        Intent().apply {
            putExtra(EXTRA_VOICE_RESULT, error.toString())
            setResult(VOICE_SEARCH_ERROR, this)
        }
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun updateText(result: String) {
        binding.speechResults.text = "\"${result.capitalizeFirstLetter()}\""
    }
}
