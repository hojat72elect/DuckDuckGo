

package com.duckduckgo.common.ui.menu

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import com.duckduckgo.common.ui.view.PopupMenuItemView
import com.duckduckgo.common.ui.view.text.DaxTextView
import com.duckduckgo.mobile.android.R

open class PopupMenu(
    layoutInflater: LayoutInflater,
    resourceId: Int,
    view: View = inflate(layoutInflater, resourceId),
    width: Int = WRAP_CONTENT,
    height: Int = WRAP_CONTENT,
) : PopupWindow(view, width, height, true) {

    init {
        elevation = ELEVATION
        animationStyle = android.R.style.Animation_Dialog
        applyRoundedRippleCorners()
    }

    private fun applyRoundedRippleCorners() {
        (contentView as? ViewGroup)?.let { content ->
            for (i in 0 until content.childCount) {
                val childLabel = (content.getChildAt(i) as? PopupMenuItemView)
                    ?.findViewById<DaxTextView>(R.id.label)

                when {
                    content.childCount == 1 -> R.drawable.ripple_rectangle_rounded
                    i == 0 -> R.drawable.ripple_top_rounded
                    i == content.childCount - 1 -> R.drawable.ripple_bottom_rounded
                    else -> null
                }?.let { backgroundResource ->
                    childLabel?.setBackgroundResource(backgroundResource)
                }
            }
        }
    }

    fun onMenuItemClicked(
        menuView: View,
        onClick: () -> Unit,
    ) {
        menuView.setOnClickListener {
            onClick()
            dismiss()
        }
    }

    fun onMenuItemLongClicked(
        menuView: View,
        onClick: () -> Unit,
    ) {
        menuView.setOnLongClickListener {
            onClick()
            dismiss()
            true
        }
    }

    fun show(
        rootView: View,
        anchorView: View,
    ) {
        val anchorLocation = IntArray(2)
        anchorView.getLocationOnScreen(anchorLocation)
        val x = MARGIN
        val y = anchorLocation[1] + MARGIN
        showAtLocation(rootView, Gravity.TOP or Gravity.END, x, y)
    }

    fun show(
        rootView: View,
        anchorView: View,
        onDismiss: () -> Unit,
    ) {
        show(rootView, anchorView)
        setOnDismissListener(onDismiss)
    }

    fun showAnchoredToView(
        rootView: View,
        anchorView: View,
    ) {
        val anchorLocation = IntArray(2)
        anchorView.getLocationOnScreen(anchorLocation)
        val x = anchorLocation[0] + MARGIN
        val y = anchorLocation[1] + MARGIN
        showAtLocation(rootView, Gravity.NO_GRAVITY, x, y)
    }

    companion object {

        private const val MARGIN = 16
        private const val ELEVATION = 6f

        const val POPUP_DEFAULT_ELEVATION_DP = 4f

        fun inflate(
            layoutInflater: LayoutInflater,
            resourceId: Int,
        ): View {
            return layoutInflater.inflate(resourceId, null)
        }
    }
}
