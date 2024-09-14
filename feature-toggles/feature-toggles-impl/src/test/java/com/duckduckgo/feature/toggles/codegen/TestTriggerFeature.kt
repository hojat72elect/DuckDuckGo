

package com.duckduckgo.feature.toggles.codegen

import com.duckduckgo.anvil.annotations.ContributesRemoteFeature
import com.duckduckgo.feature.toggles.api.Toggle
import com.duckduckgo.feature.toggles.api.Toggle.DefaultValue
import com.duckduckgo.feature.toggles.api.Toggle.Experiment
import com.duckduckgo.feature.toggles.api.Toggle.InternalAlwaysEnabled

abstract class TriggerTestScope private constructor()

@ContributesRemoteFeature(
    scope = TriggerTestScope::class,
    featureName = "testFeature",
)
interface TestTriggerFeature {
    @DefaultValue(false)
    fun self(): Toggle

    @DefaultValue(false)
    fun fooFeature(): Toggle

    @DefaultValue(false)
    @Experiment
    fun experimentFooFeature(): Toggle

    @DefaultValue(true)
    @InternalAlwaysEnabled
    fun internalDefaultTrue(): Toggle

    @DefaultValue(false)
    @InternalAlwaysEnabled
    fun internalDefaultFalse(): Toggle

    @DefaultValue(true)
    fun defaultTrue(): Toggle

    @DefaultValue(false)
    fun defaultFalse(): Toggle

    @DefaultValue(false)
    fun variantFeature(): Toggle

    @DefaultValue(false)
    @Experiment
    fun experimentDisabledByDefault(): Toggle
}

@ContributesRemoteFeature(
    scope = TriggerTestScope::class,
    featureName = "testFeature",
)
interface AnotherTestTriggerFeature {
    @DefaultValue(false)
    fun self(): Toggle

    @DefaultValue(false)
    fun fooFeature(): Toggle

    @DefaultValue(false)
    fun newFooFeature(): Toggle
}
