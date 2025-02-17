package com.duckduckgo.newtabpage.impl

import com.duckduckgo.anvil.annotations.ContributesActivePluginPoint
import com.duckduckgo.common.utils.plugins.ActivePluginPoint
import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.newtabpage.api.NewTabPageSection
import com.duckduckgo.newtabpage.api.NewTabPageSectionPlugin
import com.duckduckgo.newtabpage.api.NewTabPageSectionProvider
import com.duckduckgo.newtabpage.api.NewTabPageSectionSettingsPlugin
import com.duckduckgo.newtabpage.impl.settings.NewTabSettingsStore
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import logcat.logcat

@ContributesBinding(
    scope = AppScope::class,
)
class RealNewTabPageSectionProvider @Inject constructor(
    private val newTabPageSections: ActivePluginPoint<NewTabPageSectionPlugin>,
    private val newTabSectionsSettingsPlugins: PluginPoint<NewTabPageSectionSettingsPlugin>,
    private val newTabSettingsStore: NewTabSettingsStore,
) : NewTabPageSectionProvider {
    override fun provideSections(): Flow<List<NewTabPageSectionPlugin>> = flow {
        // store can be empty the first time we check it, so we make sure the content is initialised
        val sectionSettingsPlugins =
            newTabSectionsSettingsPlugins.getPlugins().filter { it.isActive() }
        if (sectionSettingsPlugins.isNotEmpty()) {
            if (newTabSettingsStore.sectionSettings.isEmpty()) {
                val userSections = sectionSettingsPlugins.map { it.name }
                logcat { "New Tab Sections: initialised to $userSections" }
                newTabSettingsStore.sectionSettings = userSections
            } else {
                // some new settings might have appeared, so we want to make sure they are stored
                val sectionsToAdd = mutableListOf<String>()
                val sectionsSetting = sectionSettingsPlugins.map { it.name }
                val userSections = newTabSettingsStore.sectionSettings
                sectionsSetting.forEach { section ->
                    if (userSections.find { it == section } == null) {
                        logcat { "New Tab Sections: New Section found $section" }
                        sectionsToAdd.add(section)
                    }
                }

                if (sectionsToAdd.isNotEmpty()) {
                    val updatedShortcuts = sectionsToAdd.plus(userSections)
                    logcat { "New Tab Sections: updated to $updatedShortcuts" }
                    newTabSettingsStore.sectionSettings = updatedShortcuts
                }

                logcat { "New Tab Sections: Current settings ${newTabSettingsStore.sectionSettings}" }
            }
        }

        val sections = mutableListOf<NewTabPageSectionPlugin>()
        val enabledPlugins = newTabPageSections.getPlugins().filter { it.isUserEnabled() }

        val rmfSection =
            enabledPlugins.find { it.name == NewTabPageSection.REMOTE_MESSAGING_FRAMEWORK.name }
        if (rmfSection != null) {
            sections.add(rmfSection)
        }

        newTabSettingsStore.sectionSettings.forEach { userSetting ->
            val sectionPlugin = enabledPlugins.find { it.name == userSetting }
            if (sectionPlugin != null) {
                sections.add(sectionPlugin)
            }
        }

        logcat { "New Tab Sections: ${sections.map { it.name }}" }

        emit(sections)
    }
}

@ContributesActivePluginPoint(
    scope = AppScope::class,
    boundType = NewTabPageSectionPlugin::class,
)
private interface NewTabPageSectionPluginPointTrigger
