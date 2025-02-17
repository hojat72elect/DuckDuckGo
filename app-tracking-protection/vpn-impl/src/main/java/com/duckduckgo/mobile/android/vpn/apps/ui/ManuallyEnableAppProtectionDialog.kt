package com.duckduckgo.mobile.android.vpn.apps.ui

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.duckduckgo.common.utils.extensions.safeGetApplicationIcon
import com.duckduckgo.mobile.android.vpn.R
import com.duckduckgo.mobile.android.vpn.apps.TrackingProtectionAppInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ManuallyEnableAppProtectionDialog : DialogFragment() {

    interface ManuallyEnableAppsProtectionDialogListener {
        fun onAppProtectionEnabled(packageName: String)
        fun onDialogSkipped(position: Int)
    }

    val listener: ManuallyEnableAppsProtectionDialogListener
        get() {
            return if (parentFragment is ManuallyEnableAppsProtectionDialogListener) {
                parentFragment as ManuallyEnableAppsProtectionDialogListener
            } else {
                activity as ManuallyEnableAppsProtectionDialogListener
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val rootView =
            layoutInflater.inflate(R.layout.dialog_tracking_protection_manually_enable_app, null)

        val appIcon = rootView.findViewById<ImageView>(R.id.trackingProtectionAppIcon)
        val title = rootView.findViewById<TextView>(R.id.trackingProtectionTitle)
        val enableCTA = rootView.findViewById<Button>(R.id.trackingProtectionExcludeAppDialogEnable)
        val skipCTA = rootView.findViewById<Button>(R.id.trackingProtectionExcludeAppDialogSkip)

        val alertDialog = MaterialAlertDialogBuilder(
            requireActivity(),
            com.duckduckgo.mobile.android.R.style.Widget_DuckDuckGo_Dialog
        )
            .setView(rootView)

        validateBundleArguments()
        isCancelable = false

        populateAppIcon(appIcon)
        populateTitle(title)
        configureListeners(enableCTA, skipCTA)

        return alertDialog.create()
    }

    private fun populateAppIcon(appIcon: ImageView) {
        val icon = requireActivity().packageManager.safeGetApplicationIcon(getPackageName())
        appIcon.setImageDrawable(icon)
    }

    private fun populateTitle(appName: TextView) {
        appName.text = getString(R.string.atp_ExcludeAppsManuallyEnableAnywayLabel, getAppName())
    }

    private fun getPackageName(): String {
        return requireArguments().getString(KEY_APP_PACKAGE_NAME)!!
    }

    private fun getAppName(): String {
        return requireArguments().getString(KEY_APP_NAME)!!
    }

    private fun getPosition(): Int {
        return requireArguments().getInt(KEY_POSITION)!!
    }

    private fun configureListeners(
        enableCTA: Button,
        skipCTA: Button,
    ) {
        enableCTA.setOnClickListener {
            dismiss()
            listener.onAppProtectionEnabled(getPackageName())
        }
        skipCTA.setOnClickListener {
            dismiss()
            listener.onDialogSkipped(getPosition())
        }
    }

    private fun validateBundleArguments() {
        if (arguments == null) throw IllegalArgumentException("Missing arguments bundle")
        val args = requireArguments()
        if (!args.containsKey(KEY_APP_PACKAGE_NAME)) {
            throw IllegalArgumentException("Bundle arguments required [KEY_APP_PACKAGE_NAME")
        }
        if (args.getString(KEY_APP_NAME) == null) {
            throw IllegalArgumentException("Bundle arguments can't be null [KEY_APP_NAME")
        }
        if (!args.containsKey(KEY_EXCLUDING_REASON)) {
            throw IllegalArgumentException("Bundle arguments can't be null [KEY_EXCLUDING_REASON")
        }
        if (!args.containsKey(KEY_POSITION) == null) {
            throw IllegalArgumentException("Bundle arguments can't be null [KEY_POSITION")
        }
    }

    companion object {

        const val TAG_MANUALLY_EXCLUDE_APPS_ENABLE = "ManuallyExcludedAppsDialogEnable"
        private const val KEY_APP_PACKAGE_NAME = "KEY_APP_PACKAGE_NAME"
        private const val KEY_APP_NAME = "KEY_APP_NAME"
        private const val KEY_EXCLUDING_REASON = "KEY_EXCLUDING_REASON"
        private const val KEY_POSITION = "KEY_POSITION"

        fun instance(
            appInfo: TrackingProtectionAppInfo,
            position: Int,
        ): ManuallyEnableAppProtectionDialog {
            return ManuallyEnableAppProtectionDialog().also { fragment ->
                val bundle = Bundle()
                bundle.putString(KEY_APP_PACKAGE_NAME, appInfo.packageName)
                bundle.putString(KEY_APP_NAME, appInfo.name)
                bundle.putInt(KEY_EXCLUDING_REASON, appInfo.knownProblem)
                bundle.putInt(KEY_POSITION, position)
                fragment.arguments = bundle
            }
        }
    }
}
