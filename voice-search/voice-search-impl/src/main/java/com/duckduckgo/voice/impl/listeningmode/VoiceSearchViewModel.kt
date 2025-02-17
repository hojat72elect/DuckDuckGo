

package com.duckduckgo.voice.impl.listeningmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.anvil.annotations.ContributesViewModel
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.voice.impl.listeningmode.OnDeviceSpeechRecognizer.Event.PartialResultReceived
import com.duckduckgo.voice.impl.listeningmode.OnDeviceSpeechRecognizer.Event.RecognitionFailed
import com.duckduckgo.voice.impl.listeningmode.OnDeviceSpeechRecognizer.Event.RecognitionSuccess
import com.duckduckgo.voice.impl.listeningmode.OnDeviceSpeechRecognizer.Event.RecognitionTimedOut
import com.duckduckgo.voice.impl.listeningmode.OnDeviceSpeechRecognizer.Event.VolumeUpdateReceived
import com.duckduckgo.voice.impl.listeningmode.VoiceSearchViewModel.Command.HandleSpeechRecognitionSuccess
import com.duckduckgo.voice.impl.listeningmode.VoiceSearchViewModel.Command.UpdateVoiceIndicator
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@ContributesViewModel(ActivityScope::class)
class VoiceSearchViewModel @Inject constructor(
    private val speechRecognizer: OnDeviceSpeechRecognizer,
) : ViewModel() {
    data class ViewState(
        val result: String = "",
        val unsentResult: String = "",
    )

    sealed class Command {
        data class UpdateVoiceIndicator(val volume: Float) : Command()
        data class HandleSpeechRecognitionSuccess(val result: String) : Command()
        data class TerminateVoiceSearch(val error: Int) : Command()
    }

    private val viewState = MutableStateFlow(ViewState())

    private val command = Channel<Command>(1, DROP_OLDEST)

    fun viewState(): StateFlow<ViewState> {
        return viewState
    }

    fun commands(): Flow<Command> {
        return command.receiveAsFlow()
    }

    fun startVoiceSearch() {
        if (viewState.value.result.isNotEmpty()) {
            viewModelScope.launch {
                viewState.emit(
                    viewState.value.copy(unsentResult = viewState.value.result),
                )
            }
        }

        speechRecognizer.start {
            when (it) {
                is PartialResultReceived -> showRecognizedSpeech(it.partialResult)
                is RecognitionSuccess -> handleSuccess(it.result)
                is VolumeUpdateReceived -> sendCommand(UpdateVoiceIndicator(it.normalizedVolume))
                is RecognitionFailed -> handleRecognitionFailed(it.error)
                is RecognitionTimedOut -> handleTimeOut(it.error)
            }
        }
    }

    private fun handleTimeOut(error: Int) {
        if (viewState.value.result.isEmpty()) {
            viewModelScope.launch { command.send(Command.TerminateVoiceSearch(error)) }
        } else {
            handleSuccess(viewState.value.result)
        }
    }

    private fun handleRecognitionFailed(error: Int) {
        sendCommand(Command.TerminateVoiceSearch(error))
    }

    fun stopVoiceSearch() {
        speechRecognizer.stop()
    }

    private fun sendCommand(commandToSend: Command) {
        viewModelScope.launch { command.send(commandToSend) }
    }

    private fun handleSuccess(result: String) {
        sendCommand(
            HandleSpeechRecognitionSuccess(
                getFullResult(
                    result,
                    viewState.value.unsentResult,
                ),
            ),
        )
    }

    private fun showRecognizedSpeech(result: String) {
        viewModelScope.launch {
            viewState.emit(
                viewState.value.copy(
                    result = getFullResult(result, viewState.value.unsentResult),
                ),
            )
        }
        if (result.hasReachedWordLimit()) {
            handleSuccess(result)
        }
    }

    private fun String.hasReachedWordLimit(): Boolean {
        return this.isNotEmpty() && this.split(" ").size > 30
    }

    private fun getFullResult(
        result: String,
        unsentResult: String,
    ): String {
        return if (unsentResult.isNotEmpty()) {
            "$unsentResult $result"
        } else {
            result
        }
    }

    fun userInitiatesSearchComplete() {
        sendCommand(
            HandleSpeechRecognitionSuccess(
                getFullResult(
                    viewState.value.result,
                    viewState.value.unsentResult,
                ),
            ),
        )
    }
}
