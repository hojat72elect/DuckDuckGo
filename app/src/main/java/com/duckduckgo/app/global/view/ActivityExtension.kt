

package com.duckduckgo.app.global.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.defaultbrowsing.DefaultBrowserSystemSettings
import timber.log.Timber

fun FragmentActivity.launchExternalActivity(intent: Intent) {
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Toast.makeText(this, R.string.no_compatible_third_party_app_installed, Toast.LENGTH_SHORT).show()
    }
}

fun Context.launchDefaultAppActivity() {
    try {
        val intent = DefaultBrowserSystemSettings.intent()
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val errorMessage = getString(R.string.cannotLaunchDefaultAppSettings)
        Timber.w(errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}

fun Context.fadeTransitionConfig(): Bundle? {
    val config = ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
    return config.toBundle()
}

fun Context.noAnimationConfig(): Bundle? =
    ActivityOptionsCompat.makeCustomAnimation(this, 0, 0).toBundle()

fun FragmentActivity.toggleFullScreen() {
    if (isFullScreen()) {
        // If we are exiting full screen, reset the orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    val newUiOptions = window.decorView.systemUiVisibility
        .xor(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        .xor(View.SYSTEM_UI_FLAG_FULLSCREEN)
        .xor(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

    window.decorView.systemUiVisibility = newUiOptions
}

fun FragmentActivity.isFullScreen(): Boolean {
    return window.decorView.systemUiVisibility.and(View.SYSTEM_UI_FLAG_FULLSCREEN) == View.SYSTEM_UI_FLAG_FULLSCREEN
}

fun FragmentActivity.isImmersiveModeEnabled(): Boolean {
    val uiOptions = window.decorView.systemUiVisibility
    return uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY == uiOptions
}
