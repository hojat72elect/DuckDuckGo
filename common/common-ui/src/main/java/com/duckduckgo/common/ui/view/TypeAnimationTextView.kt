

package com.duckduckgo.common.ui.view

import android.content.Context
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.duckduckgo.common.utils.extensions.html
import java.text.BreakIterator
import java.text.StringCharacterIterator
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.*

@Suppress("NoHardcodedCoroutineDispatcher")
class TypeAnimationTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()

    private var typingAnimationJob: Job? = null
    private var delayAfterAnimationInMs: Long = 300
    private val breakIterator = BreakIterator.getCharacterInstance()

    var typingDelayInMs: Long = 20
    var textInDialog: Spanned? = null

    fun startTypingAnimation(
        textDialog: String,
        isCancellable: Boolean = true,
        afterAnimation: () -> Unit = {},
    ) {
        textInDialog = textDialog.html(context)
        if (isCancellable) {
            setOnClickListener {
                if (hasAnimationStarted()) {
                    finishAnimation()
                    afterAnimation()
                }
            }
        }
        if (typingAnimationJob?.isActive == true) typingAnimationJob?.cancel()
        typingAnimationJob = launch {
            textInDialog?.let { spanned ->

                breakIterator.text = StringCharacterIterator(spanned.toString())

                var nextIndex = breakIterator.next()
                while (nextIndex != BreakIterator.DONE) {
                    text = spanned.subSequence(0, nextIndex)
                    nextIndex = breakIterator.next()
                    delay(typingDelayInMs)
                }
                delay(delayAfterAnimationInMs)
                afterAnimation()
            }
        }
    }

    fun hasAnimationStarted() = typingAnimationJob?.isActive == true

    fun hasAnimationFinished() = typingAnimationJob?.isCompleted == true

    fun finishAnimation() {
        cancelAnimation()
        textInDialog?.let { text = it }
    }

    fun cancelAnimation() = typingAnimationJob?.cancel()

    override fun onDetachedFromWindow() {
        cancelAnimation()
        super.onDetachedFromWindow()
    }
}
