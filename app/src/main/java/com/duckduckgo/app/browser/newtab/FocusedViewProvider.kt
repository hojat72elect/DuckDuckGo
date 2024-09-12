package com.duckduckgo.app.browser.newtab

import android.content.Context
import android.view.View
import com.duckduckgo.anvil.annotations.ContributesActivePlugin
import com.duckduckgo.anvil.annotations.ContributesActivePluginPoint
import com.duckduckgo.common.utils.plugins.ActivePluginPoint
import com.duckduckgo.di.scopes.ActivityScope
import com.duckduckgo.newtabpage.api.FocusedViewPlugin
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface FocusedViewProvider {

    fun provideFocusedViewVersion(): Flow<FocusedViewPlugin>
}

@ContributesBinding(
    scope = ActivityScope::class,
)
class RealFocusedViewProvider @Inject constructor(
    private val focusedViewVersions: ActivePluginPoint<FocusedViewPlugin>,
) : FocusedViewProvider {
    override fun provideFocusedViewVersion(): Flow<FocusedViewPlugin> = flow {
        val focusedView = focusedViewVersions.getPlugins().firstOrNull() ?: FocusedLegacyPage()
        emit(focusedView)
    }
}

@ContributesActivePlugin(
    scope = ActivityScope::class,
    boundType = FocusedViewPlugin::class,
    priority = FocusedViewPlugin.PRIORITY_LEGACY_FOCUSED_PAGE,
    supportExperiments = true,
)
class FocusedLegacyPage @Inject constructor() : FocusedViewPlugin {

    override fun getView(context: Context): View {
        return FocusedLegacyView(context)
    }
}

@ContributesActivePlugin(
    scope = ActivityScope::class,
    boundType = FocusedViewPlugin::class,
    priority = FocusedViewPlugin.PRIORITY_NEW_FOCUSED_PAGE,
    defaultActiveValue = false,
    supportExperiments = true,
    internalAlwaysEnabled = true,
)
class FocusedPage @Inject constructor() : FocusedViewPlugin {

    override fun getView(context: Context): View {
        return FocusedView(context)
    }
}

@ContributesActivePluginPoint(
    scope = ActivityScope::class,
    boundType = FocusedViewPlugin::class,
)
private interface FocusedViewPluginPointTrigger
