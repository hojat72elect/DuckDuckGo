

package com.duckduckgo.common.ui.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.Selection
import android.util.AttributeSet
import android.util.Patterns
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

/**
 * Variant of EditText which detects when the user has dismissed the soft keyboard
 *
 * Register as a listener using the `onBackKeyListener` property.
 */
class KeyboardAwareEditText : AppCompatEditText {
    constructor(context: Context) : this(context, null)
    constructor(
        context: Context,
        attrs: AttributeSet?,
    ) : this(context, attrs, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr)

    private var didSelectQueryFirstTime = false

    var showSuggestionsListener: ShowSuggestionsListener? = null

    private fun Editable.isWebUrl(): Boolean {
        return Patterns.WEB_URL.matcher(this.toString()).matches()
    }

    override fun onFocusChanged(
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?,
    ) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            if (text != null && text?.isWebUrl() == false) {
                if (didSelectQueryFirstTime) {
                    // trigger the text change listener so that we can show autocomplete
                    showSuggestionsListener?.showSuggestions()
                    // cursor at the end of the word
                    setSelection(text!!.length)
                } else {
                    didSelectQueryFirstTime = true
                    post { Selection.selectAll(text) }
                }
            } else if (text?.isWebUrl() == true) {
                // We always want URLs to be selected
                // we need to post for the selectAll to take effect. The wonders of Android layout !
                post { Selection.selectAll(text) }
            }
            // This is triggering multiple keyboard shows, which is unnecessary
            // showKeyboard()
        }
    }

    var onBackKeyListener: OnBackKeyListener? = null

    override fun onKeyPreIme(
        keyCode: Int,
        event: KeyEvent,
    ): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            return onBackKeyListener?.onBackKey() ?: false
        }

        return super.onKeyPreIme(keyCode, event)
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (isFocused) {
            showKeyboard()
        }
    }

    /**
     * Overrides to paste clip data without rich text formatting.
     */
    override fun onTextContextMenuItem(id: Int): Boolean = when (id) {
        android.R.id.paste -> super.onTextContextMenuItem(android.R.id.pasteAsPlainText)
        else -> super.onTextContextMenuItem(id)
    }

    private fun Context.getClipboardManager(): ClipboardManager =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private fun ClipboardManager.convertClipToPlainText() {
        val clip = primaryClip ?: return
        for (i in 0 until clip.itemCount) {
            val text = clip.getItemAt(i).coerceToText(context)
            setPrimaryClip(ClipData.newPlainText(null, text))
        }
    }

    interface OnBackKeyListener {

        fun onBackKey(): Boolean
    }

    interface ShowSuggestionsListener {
        fun showSuggestions()
    }
}
