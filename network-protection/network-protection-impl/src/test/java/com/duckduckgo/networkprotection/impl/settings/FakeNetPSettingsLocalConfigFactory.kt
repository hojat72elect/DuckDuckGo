

package com.duckduckgo.networkprotection.impl.settings

import com.duckduckgo.feature.toggles.api.FeatureToggles
import com.duckduckgo.feature.toggles.api.Toggle

class FakeNetPSettingsLocalConfigFactory private constructor() {
    companion object {
        fun create(): NetPSettingsLocalConfig {
            return FeatureToggles.Builder()
                .store(
                    object : Toggle.Store {
                        private val map = mutableMapOf<String, Toggle.State>()

                        override fun set(key: String, state: Toggle.State) {
                            map[key] = state
                        }

                        override fun get(key: String): Toggle.State? {
                            return map[key]
                        }
                    },
                )
                .featureName("fakeNetPSettingsLocalConfig")
                .build()
                .create(NetPSettingsLocalConfig::class.java)
        }
    }
}
