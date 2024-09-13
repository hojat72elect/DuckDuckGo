package com.duckduckgo.mobile.android.vpn.apps.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.duckduckgo.mobile.android.vpn.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RestoreDefaultProtectionDialog : DialogFragment() {

    interface RestoreDefaultProtectionDialogListener {
        fun onDefaultProtectionRestored()
    }

    val listener: RestoreDefaultProtectionDialogListener
        get() {
            return if (parentFragment is RestoreDefaultProtectionDialogListener) {
                parentFragment as RestoreDefaultProtectionDialogListener
            } else {
                activity as RestoreDefaultProtectionDialogListener
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val rootView =
            layoutInflater.inflate(R.layout.dialog_tracking_protection_restore_defaults, null)

        val restoreCTA =
            rootView.findViewById<Button>(R.id.trackingProtectionRestoreDefaultsRestore)
        val cancelCTA = rootView.findViewById<Button>(R.id.trackingProtectionRestoreDefaultsCancel)

        val alertDialog = MaterialAlertDialogBuilder(
            requireActivity(),
            com.duckduckgo.mobile.android.R.style.Widget_DuckDuckGo_Dialog
        )
            .setView(rootView)

        isCancelable = false

        configureListeners(restoreCTA, cancelCTA)

        return alertDialog.create()
    }

    private fun configureListeners(
        restoreCTA: Button,
        cancelCTA: Button,
    ) {
        restoreCTA.setOnClickListener {
            dismiss()
            listener.onDefaultProtectionRestored()
        }
        cancelCTA.setOnClickListener {
            dismiss()
        }
    }

    companion object {

        const val TAG_RESTORE_DEFAULT_PROTECTION = "RestoreDefaultProtection"

        fun instance(): RestoreDefaultProtectionDialog {
            return RestoreDefaultProtectionDialog()
        }
    }
}
