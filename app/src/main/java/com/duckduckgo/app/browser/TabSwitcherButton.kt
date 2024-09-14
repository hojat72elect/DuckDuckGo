

package com.duckduckgo.app.browser

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.duckduckgo.app.browser.databinding.ViewTabSwitcherButtonBinding
import com.duckduckgo.common.ui.viewbinding.viewBinding

class TabSwitcherButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewTabSwitcherButtonBinding by viewBinding()

    var count = 0
        set(value) {
            field = value
            val text = if (count < 100) "$count" else "~"
            binding.tabCount.text = text
        }

    var hasUnread = false

    fun increment(callback: () -> Unit) {
        fadeOutCount {
            count += 1
            fadeInCount()
            callback()
        }
    }

    fun animateCount() {
        fadeOutCount {
            fadeInCount()
        }
    }

    private fun fadeOutCount(callback: () -> Unit) {
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // otherwise on end keeps being called repeatedly
                binding.tabCount.animate().setListener(null)
                callback()
            }
        }

        binding.tabCount.animate()
            .setDuration(300)
            .alpha(0.0f)
            .setListener(listener)
            .start()
    }

    private fun fadeInCount() {
        binding.tabCount.animate()
            .setDuration(300)
            .alpha(1.0f)
            .start()
    }
}
