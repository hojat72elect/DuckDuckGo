

package com.duckduckgo.common.ui.store

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.common.ui.DuckDuckGoTheme
import javax.inject.Inject

class ThemingSharedPreferences @Inject constructor(
    private val context: Context,
) :
    ThemingDataStore {

    private val themePrefMapper = ThemePrefsMapper()

    override var theme: DuckDuckGoTheme
        get() = selectedThemeSavedValue()
        set(theme) = preferences.edit { putString(KEY_THEME, themePrefMapper.prefValue(theme)) }

    override fun isCurrentlySelected(theme: DuckDuckGoTheme): Boolean {
        return selectedThemeSavedValue() == theme
    }

    private fun selectedThemeSavedValue(): DuckDuckGoTheme {
        val savedValue = preferences.getString(KEY_THEME, null)
        return themePrefMapper.themeFrom(savedValue, DuckDuckGoTheme.SYSTEM_DEFAULT)
    }

    private val preferences: SharedPreferences by lazy { context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE) }

    private class ThemePrefsMapper {

        companion object {
            private const val THEME_LIGHT = "LIGHT"
            private const val THEME_DARK = "DARK"
            private const val THEME_SYSTEM_DEFAULT = "SYSTEM_DEFAULT"
        }

        fun prefValue(theme: DuckDuckGoTheme) =
            when (theme) {
                DuckDuckGoTheme.SYSTEM_DEFAULT -> THEME_SYSTEM_DEFAULT
                DuckDuckGoTheme.LIGHT -> THEME_LIGHT
                DuckDuckGoTheme.DARK -> THEME_DARK
            }

        fun themeFrom(
            value: String?,
            defValue: DuckDuckGoTheme,
        ) =
            when (value) {
                THEME_LIGHT -> DuckDuckGoTheme.LIGHT
                THEME_DARK -> DuckDuckGoTheme.DARK
                THEME_SYSTEM_DEFAULT -> DuckDuckGoTheme.SYSTEM_DEFAULT
                else -> defValue
            }
    }

    companion object {
        const val FILENAME = "com.duckduckgo.app.settings_activity.settings"
        const val KEY_THEME = "THEME"
    }
}
