

package com.duckduckgo.common.ui.view.button

import android.content.Context
import android.util.AttributeSet
import com.duckduckgo.common.ui.view.button.Size.Small
import com.duckduckgo.mobile.android.R
import com.google.android.material.button.MaterialButton

open class DaxButton @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
) : MaterialButton(
    ctx,
    attrs,
    defStyleAttr,
) {

    init {
        val typedArray =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.DaxButton,
                0,
                0,
            )

        val buttonSize = if (typedArray.hasValue(R.styleable.DaxButton_buttonSize)) {
            Size.from(typedArray.getInt(R.styleable.DaxButton_buttonSize, 0))
        } else {
            Small
        }

        typedArray.recycle()

        val sidePadding = if (buttonSize == Small) {
            resources.getDimensionPixelSize(R.dimen.buttonSmallSidePadding)
        } else {
            resources.getDimensionPixelSize(R.dimen.buttonLargeSidePadding)
        }

        val topPadding = if (buttonSize == Small) {
            resources.getDimensionPixelSize(R.dimen.buttonSmallTopPadding)
        } else {
            resources.getDimensionPixelSize(R.dimen.buttonLargeTopPadding)
        }

        minHeight = resources.getDimensionPixelSize(Size.dimension(buttonSize))
        setPadding(sidePadding, topPadding, sidePadding, topPadding)
    }
}

enum class Size {
    Small,
    Large,
    ;

    companion object {
        fun from(size: Int): Size {
            // same order as attrs-button.xml
            return when (size) {
                0 -> Small
                1 -> Large
                else -> Large
            }
        }

        fun dimension(size: Size): Int {
            return when (size) {
                Small -> R.dimen.buttonSmallHeight
                Large -> R.dimen.buttonLargeHeight
                else -> R.dimen.buttonSmallHeight
            }
        }
    }
}
