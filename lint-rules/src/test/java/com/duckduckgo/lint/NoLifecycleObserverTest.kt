

package com.duckduckgo.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

@Suppress("UnstableApiUsage")
class NoLifecycleObserverTest {
    @Test
    fun whenLifecycleObserverExtendedThenFailWithError() {
        lint()
            .files(kt("""
              package com.duckduckgo.lint
    
                class LifecycleObserver

                class Duck : LifecycleObserver() {
                    fun quack() {                    
                    }
                }
            """).indented())
            .issues(NoLifecycleObserverDetector.NO_LIFECYCLE_OBSERVER_ISSUE)
            .run()
            .expect("""
                src/com/duckduckgo/lint/LifecycleObserver.kt:5: Error: LifecycleObserver should not be directly extended [NoLifecycleObserver]
                  class Duck : LifecycleObserver() {
                        ~~~~
                1 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun whenDefaultLifecycleObserverExtendedThenFail() {
        lint()
            .files(kt("""
              package com.duckduckgo.lint
    
                class DefaultLifecycleObserver

                class Duck : DefaultLifecycleObserver() {
                    fun quack() {                    
                    }
                }
            """).indented())
            .allowCompilationErrors()
            .issues(NoLifecycleObserverDetector.NO_LIFECYCLE_OBSERVER_ISSUE)
            .run()
            .expect("""
                src/com/duckduckgo/lint/DefaultLifecycleObserver.kt:5: Error: LifecycleObserver should not be directly extended [NoLifecycleObserver]
                  class Duck : DefaultLifecycleObserver() {
                        ~~~~
                1 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun whenLifecycleObserverNotFoundThenSucceed() {
        lint()
            .files(kt("""
              package com.duckduckgo.lint
    
                class Duck {
                    fun quack() {                    
                    }
                }
            """).indented())
            .allowCompilationErrors()
            .issues(NoLifecycleObserverDetector.NO_LIFECYCLE_OBSERVER_ISSUE)
            .run()
            .expectClean()
    }
}
