

package com.duckduckgo.app.global.view

import android.widget.EditText

fun EditText.isDifferent(newInput: String?): Boolean = text.toString() != newInput

fun EditText.updateIfDifferent(newInput: String) {
    if (isDifferent(newInput)) {
        setText(newInput)
    }
}
