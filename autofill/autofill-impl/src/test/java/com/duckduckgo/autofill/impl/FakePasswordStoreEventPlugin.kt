package com.duckduckgo.autofill.impl

import com.duckduckgo.common.utils.plugins.PluginPoint

class FakePasswordStoreEventPlugin(
    private val plugins: List<PasswordStoreEventListener> = emptyList(),
) : PluginPoint<PasswordStoreEventListener> {

    override fun getPlugins() = plugins
}
