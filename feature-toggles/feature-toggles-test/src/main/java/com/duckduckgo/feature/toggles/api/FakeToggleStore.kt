

package com.duckduckgo.feature.toggles.api

class FakeToggleStore : Toggle.Store {
    private val map = mutableMapOf<String, Toggle.State>()

    override fun set(key: String, state: Toggle.State) {
        map[key] = state
    }

    override fun get(key: String): Toggle.State? {
        return map[key]
    }
}

class FakeFeatureToggleFactory {
    companion object {
        fun <T> create(toggles: Class<T>, store: Toggle.Store = FakeToggleStore()): T {
            return FeatureToggles.Builder()
                .store(store)
                .featureName("fakeFeature")
                .build()
                .create(toggles)
        }
    }
}
