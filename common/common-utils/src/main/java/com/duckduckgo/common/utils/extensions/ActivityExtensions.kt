

package com.duckduckgo.common.utils.extensions

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

/**
 * Deep links to the application App Info settings
 * @return `true` if it was able to deep link, otherwise `false`
 */
fun AppCompatActivity.launchApplicationInfoSettings(): Boolean {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.parse("package:$packageName")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    runCatching { startActivity(intent) }

    return true
}

@SuppressLint("InlinedApi")
fun AppCompatActivity.launchAlwaysOnSystemSettings() {
    val intent = Intent(Settings.ACTION_VPN_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

@SuppressLint("InlinedApi")
fun AppCompatActivity.launchSettings() {
    val intent = Intent(Settings.ACTION_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
}

/**
 * Deep links to the battery optimization settings
 * @return `true` if it was able to deep link, otherwise `false`
 */
fun AppCompatActivity.launchIgnoreBatteryOptimizationSettings(): Boolean {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.parse("package:$packageName")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    runCatching { startActivity(intent) }.onFailure {
        val fallback = Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        runCatching { startActivity(fallback) }.onFailure { return false }
    }

    return true
}
