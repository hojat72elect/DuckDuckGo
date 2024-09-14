

package com.duckduckgo.installation.impl.installer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface InstallSourceExtractor {
    fun extract(): String?
}

@ContributesBinding(AppScope::class)
class RealInstallSourceExtractor @Inject constructor(
    private val context: Context,
    private val appBuildConfig: AppBuildConfig,
) : InstallSourceExtractor {

    @SuppressLint("NewApi")
    override fun extract(): String? {
        return if (appBuildConfig.sdkInt >= VERSION_CODES.R) {
            installationSourceModern(context.packageName)
        } else {
            installationSourceLegacy(context.packageName)
        }
    }

    @Suppress("DEPRECATION")
    private fun installationSourceLegacy(packageName: String): String? {
        return context.packageManager.getInstallerPackageName(packageName)
    }

    @RequiresApi(VERSION_CODES.R)
    private fun installationSourceModern(packageName: String): String? {
        return context.packageManager.getInstallSourceInfo(packageName).installingPackageName
    }
}
