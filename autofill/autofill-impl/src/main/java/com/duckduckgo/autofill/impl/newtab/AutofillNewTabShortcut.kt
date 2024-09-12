package com.duckduckgo.autofill.impl.newtab

import android.content.Context
import com.duckduckgo.anvil.annotations.ContributesActivePlugin
import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.autofill.api.AutofillScreens.AutofillSettingsScreen
import com.duckduckgo.autofill.api.AutofillSettingsLaunchSource
import com.duckduckgo.autofill.impl.R
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.navigation.api.GlobalActivityStarter
import com.duckduckgo.newtabpage.api.NewTabPageShortcutPlugin
import com.duckduckgo.newtabpage.api.NewTabShortcut
import javax.inject.Inject

@ContributesActivePlugin(
    AppScope::class,
    boundType = NewTabPageShortcutPlugin::class,
    priority = NewTabPageShortcutPlugin.PRIORITY_AUTOFILL,
)
class AutofillNewTabShortcutPlugin @Inject constructor(
    private val globalActivityStarter: GlobalActivityStarter,
    private val setting: AutofillNewTabShortcutSetting,
) : NewTabPageShortcutPlugin {

    inner class PasswordsShortcut() : NewTabShortcut {
        override fun name(): String = "passwords"
        override fun titleResource(): Int = R.string.newTabPageShortcutPasswords
        override fun iconResource(): Int = R.drawable.ic_shortcut_passwords
    }

    override fun getShortcut(): NewTabShortcut {
        return PasswordsShortcut()
    }

    override fun onClick(context: Context) {
        globalActivityStarter.start(
            context,
            AutofillSettingsScreen(AutofillSettingsLaunchSource.NewTabShortcut)
        )
    }

    override suspend fun isUserEnabled(): Boolean {
        return setting.self().isEnabled()
    }

    override suspend fun setUserEnabled(enabled: Boolean) {
        if (enabled) {
            setting.self().setEnabled(Toggle.State(true))
        } else {
            setting.self().setEnabled(Toggle.State(false))
        }
    }
}

/**
 * Local feature/settings - they will never be in remote config
 */
@ContributesRemoteFeature(
    scope = AppScope::class,
    featureName = "autofillNewTabShortcutSetting",
)
interface AutofillNewTabShortcutSetting {
    @Toggle.DefaultValue(true)
    fun self(): Toggle
}
