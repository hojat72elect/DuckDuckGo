package com.duckduckgo.lint.utils

import com.android.tools.lint.checks.infrastructure.TestFiles

internal val PLUGIN_POINT_API = TestFiles.kt(
    """
                    package com.duckduckgo.common.utils.plugins
    
                    interface PluginPoint<T> {
                        fun getPlugins(): Collection<T>
                    }
                    interface InternalActivePluginPoint<out T : ActivePluginPoint.ActivePlugin> {
                        suspend fun getPlugins(): Collection<T>
                    }
                    interface ActivePlugin {
                        suspend fun isActive(): Boolean = true
                    }
                    typealias ActivePluginPoint<T> = InternalActivePluginPoint<@JvmSuppressWildcards T>
            """
).indented()
internal val PLUGIN_POINT_ANNOTATIONS_API = TestFiles.kt(
    """
                    package com.duckduckgo.anvil.annotations
    
                    annotation class ContributesActivePlugin
                    annotation class ContributesActivePluginPoint(
                        val boundType: KClass<*> = Unit::class,
                    )

            """
).indented()
