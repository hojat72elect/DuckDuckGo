

package com.duckduckgo.common.ui.view

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.DialogFragment
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.common.utils.extensions.html
import com.duckduckgo.mobile.android.R
import com.duckduckgo.mobile.android.databinding.ViewDaxDialogBinding

interface DaxDialog {
    fun setDaxDialogListener(listener: DaxDialogListener?)
}

interface DaxDialogListener {
    fun onDaxDialogDismiss()
    fun onDaxDialogPrimaryCtaClick()
    fun onDaxDialogSecondaryCtaClick()
    fun onDaxDialogHideClick()
}

class TypewriterDaxDialog : DialogFragment(R.layout.view_dax_dialog), DaxDialog {

    private val binding: ViewDaxDialogBinding by viewBinding()

    private var daxText: String = ""
    private var primaryButtonText: String = ""
    private var secondaryButtonText: String = ""
    private var hideButtonText: String = ""
    private var toolbarDimmed: Boolean = true
    private var dismissible: Boolean = false
    private var typingDelayInMs: Long = DEFAULT_TYPING_DELAY
    private var showHideButton: Boolean = true

    private var daxDialogListener: DaxDialogListener? = null

    override fun setDaxDialogListener(listener: DaxDialogListener?) {
        daxDialogListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window
        val attributes = window?.attributes

        attributes?.gravity = Gravity.BOTTOM
        window?.attributes = attributes
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            if (containsKey(ARG_DAX_TEXT)) {
                getString(ARG_DAX_TEXT)?.let { daxText = it }
            }
            if (containsKey(ARG_PRIMARY_CTA_TEXT)) {
                getString(ARG_PRIMARY_CTA_TEXT)?.let { primaryButtonText = it }
            }
            if (containsKey(ARG_HIDE_CTA_TEXT)) {
                getString(ARG_HIDE_CTA_TEXT)?.let { hideButtonText = it }
            }
            if (containsKey(ARG_SECONDARY_CTA_TEXT)) {
                getString(ARG_SECONDARY_CTA_TEXT)?.let { secondaryButtonText = it }
            }
            if (containsKey(ARG_DISMISSIBLE)) {
                dismissible = getBoolean(ARG_DISMISSIBLE)
            }
            if (containsKey(ARG_TOOLBAR_DIMMED)) {
                toolbarDimmed = getBoolean(ARG_TOOLBAR_DIMMED)
            }
            if (containsKey(ARG_TYPING_DELAY)) {
                typingDelayInMs = getLong(ARG_TYPING_DELAY)
            }
            if (containsKey(ARG_SHOW_HIDE_BUTTON)) {
                showHideButton = getBoolean(ARG_SHOW_HIDE_BUTTON)
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.Widget_DuckDuckGo_DaxDialogFragment
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.attributes?.dimAmount = 0f
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setDialogAndStartAnimation()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (activity != null) {
            binding.dialogText.cancelAnimation()
            daxDialogListener?.onDaxDialogDismiss()
        }
        daxDialogListener = null
        super.onDismiss(dialog)
    }

    private fun setDialogAndStartAnimation() {
        setDialog()
        setListeners()
        binding.dialogText.startTypingAnimation(daxText, true)
    }

    private fun setListeners() {
        with(binding) {
            hideText.setOnClickListener {
                dialogText.cancelAnimation()
                daxDialogListener?.onDaxDialogHideClick()
                dismiss()
            }

            primaryCta.setOnClickListener {
                dialogText.cancelAnimation()
                daxDialogListener?.onDaxDialogPrimaryCtaClick()
                dismiss()
            }

            secondaryCta.setOnClickListener {
                dialogText.cancelAnimation()
                daxDialogListener?.onDaxDialogSecondaryCtaClick()
                dismiss()
            }

            dialogContainer.setOnClickListener {
                if (dismissible) {
                    dialogText.cancelAnimation()
                    dismiss()
                } else {
                    if (!dialogText.hasAnimationFinished()) {
                        dialogText.finishAnimation()
                    }
                }
            }
        }
    }

    private fun setDialog() {
        if (context == null) {
            dismiss()
            return
        }

        context?.let {
            val toolbarColor = if (toolbarDimmed) it.getColorFromAttr(R.attr.daxColorDim) else getColor(it, android.R.color.transparent)
            with(binding) {
                toolbarDialogLayout.setBackgroundColor(toolbarColor)
                hideText.text = hideButtonText
                hiddenText.text = daxText.html(it)
                primaryCta.text = primaryButtonText
                secondaryCta.text = secondaryButtonText
                secondaryCta.visibility = if (secondaryButtonText.isEmpty()) View.GONE else View.VISIBLE
                dialogText.typingDelayInMs = typingDelayInMs
                hideText.visibility = if (showHideButton) View.VISIBLE else View.GONE
            }
        }
    }

    companion object {

        fun newInstance(
            daxText: String,
            primaryButtonText: String,
            secondaryButtonText: String? = "",
            hideButtonText: String,
            toolbarDimmed: Boolean = true,
            dismissible: Boolean = false,
            typingDelayInMs: Long = DEFAULT_TYPING_DELAY,
            showHideButton: Boolean = true,
        ): TypewriterDaxDialog {
            return TypewriterDaxDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_DAX_TEXT, daxText)
                    putString(ARG_PRIMARY_CTA_TEXT, primaryButtonText)
                    putString(ARG_HIDE_CTA_TEXT, hideButtonText)
                    putString(ARG_SECONDARY_CTA_TEXT, secondaryButtonText)
                    putBoolean(ARG_TOOLBAR_DIMMED, toolbarDimmed)
                    putBoolean(ARG_DISMISSIBLE, dismissible)
                    putLong(ARG_TYPING_DELAY, typingDelayInMs)
                    putBoolean(ARG_SHOW_HIDE_BUTTON, showHideButton)
                }
            }
        }

        private const val DEFAULT_TYPING_DELAY: Long = 20
        private const val ARG_DAX_TEXT = "daxText"
        private const val ARG_PRIMARY_CTA_TEXT = "primaryCtaText"
        private const val ARG_HIDE_CTA_TEXT = "hideCtaText"
        private const val ARG_SECONDARY_CTA_TEXT = "secondaryCtaText"
        private const val ARG_TOOLBAR_DIMMED = "toolbarDimmed"
        private const val ARG_DISMISSIBLE = "isDismissible"
        private const val ARG_TYPING_DELAY = "typingDelay"
        private const val ARG_SHOW_HIDE_BUTTON = "showHideButton"
    }
}
