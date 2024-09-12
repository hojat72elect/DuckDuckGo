package com.duckduckgo.common.utils

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver

class KeyboardVisibilityUtil(private val rootView: View) {

    fun addKeyboardVisibilityListener(onKeyboardVisible: () -> Unit) {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (isKeyboardVisible()) {
                        rootView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        onKeyboardVisible()
                    }
                }
            },
        )
    }

    private fun isKeyboardVisible(): Boolean {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height
        val keypadHeight = screenHeight - rect.bottom
        return keypadHeight > screenHeight * KEYBOARD_VISIBILITY_THRESHOLD
    }

    companion object {
        private const val KEYBOARD_VISIBILITY_THRESHOLD = 0.15
    }
}
