

package com.duckduckgo.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.duckduckgo.lint.WrongPluginPointCollectorDetector.Companion.WRONG_PLUGIN_POINT_ISSUE
import com.duckduckgo.lint.utils.PLUGIN_POINT_ANNOTATIONS_API
import com.duckduckgo.lint.utils.PLUGIN_POINT_API
import org.junit.Test

class WrongPluginPointCollectorDetectorTest {
    @Test
    fun `test normal plugin point constructor parameter collecting active plugins`() {
        lint()
            .files(
                PLUGIN_POINT_API,
                PLUGIN_POINT_ANNOTATIONS_API,
                kt("""
                    package com.test.plugins
    
                    import com.duckduckgo.common.utils.plugins.ActivePlugin
                    import com.duckduckgo.common.utils.plugins.PluginPoint
                    import com.duckduckgo.anvil.annotations.ContributesActivePlugin

                    interface MyPlugin
                    interface MyPluginActivePlugin : ActivePlugin
        
                    class Duck(private val pp: PluginPoint<MyPluginActivePlugin>) {
                        fun quack() {                    
                        }
                    }
            """).indented()
            )
            .issues(WRONG_PLUGIN_POINT_ISSUE)
            .run()
            .expect("""
                src/com/test/plugins/MyPlugin.kt:10: Error: PluginPoint cannot be collector of ActivePlugin(s) [WrongPluginPointCollectorDetector]
                class Duck(private val pp: PluginPoint<MyPluginActivePlugin>) {
                      ~~~~
                src/com/test/plugins/MyPlugin.kt:10: Error: PluginPoint cannot be collector of ActivePlugin(s) [WrongPluginPointCollectorDetector]
                class Duck(private val pp: PluginPoint<MyPluginActivePlugin>) {
                                       ~~
                2 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun `test active plugin point constructor parameter collecting active plugins`() {
        lint()
            .files(
                PLUGIN_POINT_API,
                PLUGIN_POINT_ANNOTATIONS_API,
                kt("""
                    package com.test.plugins
    
                    import com.duckduckgo.common.utils.plugins.ActivePlugin
                    import com.duckduckgo.common.utils.plugins.PluginPoint
                    import com.duckduckgo.anvil.annotations.ContributesActivePlugin
    
                    interface MyPlugin
                    interface MyPluginActivePlugin : ActivePlugin
        
                    class Duck(private val pp: PluginPoint<MyPlugin>) {
                        fun quack() {                    
                        }
                    }
            """).indented())
            .issues(WRONG_PLUGIN_POINT_ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun `test normal plugin point field collecting active plugins`() {
        lint()
            .files(
                PLUGIN_POINT_API,
                PLUGIN_POINT_ANNOTATIONS_API,
                kt("""
                    package com.test.plugins

                    import com.duckduckgo.common.utils.plugins.ActivePlugin
                    import com.duckduckgo.common.utils.plugins.PluginPoint
                    import com.duckduckgo.anvil.annotations.ContributesActivePlugin
    
                    interface MyPlugin
                    interface MyPluginActivePlugin : ActivePlugin
        
                    class Duck {
                        private val pp: PluginPoint<MyPluginActivePlugin>
                        
                        fun quack() {                    
                        }
                    }
            """).indented())
            .issues(WRONG_PLUGIN_POINT_ISSUE)
            .run()
            .expect("""
                src/com/test/plugins/MyPlugin.kt:11: Error: PluginPoint cannot be collector of ActivePlugin(s) [WrongPluginPointCollectorDetector]
                    private val pp: PluginPoint<MyPluginActivePlugin>
                                ~~
                1 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun `test active plugin point field collecting active plugins`() {
        lint()
            .files(
                PLUGIN_POINT_API,
                PLUGIN_POINT_ANNOTATIONS_API,
                kt("""
                    package com.test.plugins
    
                    import com.duckduckgo.common.utils.plugins.ActivePlugin
                    import com.duckduckgo.common.utils.plugins.PluginPoint
                    import com.duckduckgo.anvil.annotations.ContributesActivePlugin
        
                    class Duck {
                        private val pp: PluginPoint<MyPlugin>
                        
                        fun quack() {                    
                        }
                    }
            """).indented())
            .issues(WRONG_PLUGIN_POINT_ISSUE)
            .run()
            .expectClean()
    }
}
