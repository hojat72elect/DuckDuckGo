

package com.duckduckgo.app.dev.settings.db

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject

interface DevSettingsDataStore {
    var nextTdsEnabled: Boolean
    var overrideUA: Boolean
    var selectedUA: UAOverride
}

@ContributesBinding(AppScope::class)
class DevSettingsSharedPreferences @Inject constructor(private val context: Context) : DevSettingsDataStore {
    override var nextTdsEnabled: Boolean
        get() = preferences.getBoolean(KEY_NEXT_TDS_ENABLED, false)
        set(enabled) = preferences.edit { putBoolean(KEY_NEXT_TDS_ENABLED, enabled) }

    override var overrideUA: Boolean
        get() = preferences.getBoolean(KEY_OVERRIDE_UA, false)
        set(enabled) = preferences.edit { putBoolean(KEY_OVERRIDE_UA, enabled) }

    override var selectedUA: UAOverride
        get() = selectedUASavedValue()
        set(value) = preferences.edit { putString(KEY_SELECTED_UA, value.name) }

    private val preferences: SharedPreferences by lazy { context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE) }

    private fun selectedUASavedValue(): UAOverride {
        runCatching {
            val savedValue = preferences.getString(KEY_SELECTED_UA, null) ?: return UAOverride.DEFAULT
            return UAOverride.valueOf(savedValue)
        }
        return UAOverride.DEFAULT
    }
    companion object {
        const val FILENAME = "com.duckduckgo.app.dev_settings_activity.dev_settings"
        const val KEY_NEXT_TDS_ENABLED = "KEY_NEXT_TDS_ENABLED"
        const val KEY_OVERRIDE_UA = "KEY_OVERRIDE_UA"
        const val KEY_SELECTED_UA = "KEY_SELECTED_UA"
    }
}

enum class UAOverride {
    FIREFOX,
    DEFAULT,
    WEBVIEW,
}
