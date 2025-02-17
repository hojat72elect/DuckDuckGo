

package com.duckduckgo.common.utils.playstore

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import timber.log.Timber

interface PlayStoreUtils {

    fun installedFromPlayStore(): Boolean
    fun launchPlayStore()
    fun launchPlayStore(appPackage: String)
    fun isPlayStoreInstalled(): Boolean
}

@ContributesBinding(AppScope::class)
class PlayStoreAndroidUtils @Inject constructor(val context: Context) : PlayStoreUtils {

    override fun installedFromPlayStore(): Boolean {
        return try {
            val installSource = context.packageManager.getInstallerPackageName(DDG_APP_PACKAGE)
            return matchesPlayStoreInstallSource(installSource)
        } catch (e: IllegalArgumentException) {
            Timber.w("Can't determine if app was installed from Play Store; assuming it wasn't")
            false
        }
    }

    private fun matchesPlayStoreInstallSource(installSource: String?): Boolean {
        Timber.i("DuckDuckGo app install source detected: $installSource")
        return installSource == PLAY_STORE_PACKAGE
    }

    override fun isPlayStoreInstalled(): Boolean {
        return try {
            if (!isPlayStoreActivityResolvable(context)) {
                Timber.i("Cannot resolve Play Store activity")
                return false
            }

            val isAppEnabled = isPlayStoreAppEnabled()
            Timber.i("The Play Store app is installed " + if (isAppEnabled) "and enabled" else "but disabled")
            return isAppEnabled
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.i("Could not find package details for $PLAY_STORE_PACKAGE; Play Store is not installed")
            false
        }
    }

    private fun isPlayStoreAppEnabled(): Boolean {
        context.packageManager.getPackageInfo(PLAY_STORE_PACKAGE, 0)
        val appInfo = context.packageManager.getApplicationInfo(PLAY_STORE_PACKAGE, 0)
        return appInfo.enabled
    }

    private fun isPlayStoreActivityResolvable(context: Context): Boolean {
        return playStoreIntent().resolveActivity(context.packageManager) != null
    }

    private fun playStoreIntent(appPackage: String = DDG_APP_PACKAGE): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("$PLAY_STORE_URI$appPackage")
            setPackage(PLAY_STORE_PACKAGE)
        }
    }

    override fun launchPlayStore() {
        val intent = playStoreIntent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Could not launch the Play Store")
        }
    }

    override fun launchPlayStore(appPackage: String) {
        val intent = playStoreIntent(appPackage)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Could not launch the Play Store")
        }
    }

    companion object {
        const val PLAY_STORE_PACKAGE = "com.android.vending"
        const val PLAY_STORE_REFERRAL_SERVICE = "com.google.android.finsky.externalreferrer.GetInstallReferrerService"
        private const val PLAY_STORE_URI = "https://play.google.com/store/apps/details?id="
        private const val DDG_APP_PACKAGE = "com.duckduckgo.mobile.android"
    }
}
