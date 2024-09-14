

package com.duckduckgo.user.agent.impl

import com.duckduckgo.common.utils.plugins.PluginPoint
import com.duckduckgo.user.agent.api.UserAgentInterceptor

fun provideUserAgentFakePluginPoint(): PluginPoint<UserAgentInterceptor> {
    return object : PluginPoint<UserAgentInterceptor> {
        override fun getPlugins(): Collection<UserAgentInterceptor> {
            return listOf()
        }
    }
}
