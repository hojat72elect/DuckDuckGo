package com.duckduckgo.feature.toggles.codegen

import com.duckduckgo.anvil.annotations.ContributesActivePlugin
import com.duckduckgo.anvil.annotations.ContributesActivePluginPoint
import com.duckduckgo.common.utils.plugins.ActivePlugin
import com.duckduckgo.di.scopes.AppScope
import javax.inject.Inject

@ContributesActivePluginPoint(
    scope = AppScope::class,
)
interface MyPlugin : ActivePlugin {
    fun doSomething()
}

interface TriggeredMyPlugin : ActivePlugin {
    fun doSomething()
}

@ContributesActivePluginPoint(
    scope = AppScope::class,
    boundType = TriggeredMyPlugin::class,
)
private interface TriggeredMyPluginTrigger

@ContributesActivePlugin(
    scope = AppScope::class,
    boundType = TriggeredMyPlugin::class,
    defaultActiveValue = false,
)
class FooActiveTriggeredMyPlugin @Inject constructor() : TriggeredMyPlugin {
    override fun doSomething() {
    }
}

@ContributesActivePlugin(
    scope = AppScope::class,
    boundType = MyPlugin::class,
    defaultActiveValue = false,
)
class FooActivePlugin @Inject constructor() : MyPlugin {
    override fun doSomething() {
    }
}

@ContributesActivePlugin(
    scope = AppScope::class,
    boundType = MyPlugin::class,
    priority = 1000,
)
class BarActivePlugin @Inject constructor() : MyPlugin {
    override fun doSomething() {
    }
}

@ContributesActivePlugin(
    scope = AppScope::class,
    boundType = MyPlugin::class,
    priority = 50,
)
class BazActivePlugin @Inject constructor() : MyPlugin {
    override fun doSomething() {
    }
}

@ContributesActivePlugin(
    scope = AppScope::class,
    boundType = MyPlugin::class,
    priority = 50,
    supportExperiments = true,
)
class ExperimentActivePlugin @Inject constructor() : MyPlugin {
    override fun doSomething() {
    }
}

@ContributesActivePlugin(
    scope = AppScope::class,
    boundType = MyPlugin::class,
    priority = 50,
    internalAlwaysEnabled = true,
    supportExperiments = false,
)
class InternalAlwaysEnabledActivePlugin @Inject constructor() : MyPlugin {
    override fun doSomething() {
    }
}
