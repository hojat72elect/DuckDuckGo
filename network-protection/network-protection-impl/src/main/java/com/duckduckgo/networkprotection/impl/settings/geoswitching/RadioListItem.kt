

package com.duckduckgo.networkprotection.impl.settings.geoswitching

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.duckduckgo.common.ui.view.button.RadioButton
import com.duckduckgo.common.ui.view.gone
import com.duckduckgo.common.ui.view.listitem.DaxListItem.ImageBackground
import com.duckduckgo.common.ui.view.show
import com.duckduckgo.common.ui.view.text.DaxTextView
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.networkprotection.impl.R
import com.duckduckgo.networkprotection.impl.databinding.ViewRadioListItemBinding

class RadioListItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.duckduckgo.mobile.android.R.attr.twoLineListItemStyle,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewRadioListItemBinding by viewBinding()

    val radioButton: RadioButton
        get() = binding.radioButton
    val primaryText: DaxTextView
        get() = binding.primaryText
    val secondaryText: DaxTextView
        get() = binding.secondaryText
    val leadingEmojiIconContainer: View
        get() = binding.leadingIconBackground
    val trailingIconContainer: View
        get() = binding.trailingIconContainer

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.RadioListItem,
            0,
            com.duckduckgo.mobile.android.R.style.Widget_DuckDuckGo_TwoLineListItem,
        ).apply {
            binding.radioButton.isChecked = getBoolean(R.styleable.RadioListItem_android_checked, false)

            binding.primaryText.text = getString(R.styleable.RadioListItem_primaryText)

            if (hasValue(R.styleable.RadioListItem_secondaryText)) {
                binding.secondaryText.text = getString(R.styleable.RadioListItem_secondaryText)
            } else {
                binding.secondaryText.gone()
            }

            if (hasValue(R.styleable.RadioListItem_primaryTextColorOverlay)) {
                binding.primaryText.setTextColor((getColorStateList(R.styleable.RadioListItem_primaryTextColorOverlay)))
            }

            if (hasValue(R.styleable.RadioListItem_secondaryTextColorOverlay)) {
                binding.secondaryText.setTextColor(getColorStateList(R.styleable.RadioListItem_secondaryTextColorOverlay))
            }

            if (hasValue(R.styleable.RadioListItem_leadingEmojiIcon)) {
                binding.leadingIcon.text = getString(R.styleable.RadioListItem_leadingEmojiIcon)
            } else {
                binding.leadingIcon.gone()
                binding.leadingIconBackground.gone()
            }

            if (hasValue(R.styleable.RadioListItem_leadingIconBackground)) {
                val type = ImageBackground.from(getInt(R.styleable.RadioListItem_leadingIconBackground, 0))
                binding.leadingIconBackground.setBackgroundResource(ImageBackground.background(type))
                val padding = resources.getDimensionPixelSize(com.duckduckgo.mobile.android.R.dimen.twoLineItemVerticalPadding)
                binding.itemContainer.setPadding(0, padding, 0, padding)
                binding.leadingIconBackground.show()
            }

            if (getBoolean(R.styleable.RadioListItem_showTrailingIcon, false)) {
                binding.trailingIconContainer.show()
            } else {
                binding.trailingIconContainer.gone()
            }
            recycle()
        }
    }

    fun setClickListener(onClick: () -> Unit) {
        binding.itemContainer.setOnClickListener { onClick() }
    }

    /** Sets the primary text title */
    fun setPrimaryText(title: String?) {
        primaryText.text = title
        primaryText.show()
    }

    /** Sets the secondary text title */
    fun setSecondaryText(title: String?) {
        secondaryText.text = title
        secondaryText.show()
    }

    /** Sets the leading icon image drawable */
    fun setLeadingEmojiIcon(emoji: String) {
        binding.leadingIcon.text = emoji
        binding.leadingIcon.show()
        leadingEmojiIconContainer.show()
    }

    /** Sets the item overflow menu click listener */
    fun setTrailingIconClickListener(onClick: (View) -> Unit) {
        trailingIconContainer.setOnClickListener { onClick(trailingIconContainer) }
    }
}
