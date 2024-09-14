

package com.duckduckgo.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

@Suppress("UnstableApiUsage")
class NoSingletonDetectorTest {
    @Test
    fun whenSingletonAnnotationFoundThenFailWithError() {
        lint()
            .files(kt("""
              package com.duckduckgo.lint
    
                annotation class Singleton

                @Singleton
                class Duck {
                    fun quack() {                    
                    }
                }
            """).indented())
            .issues(NoSingletonDetector.NO_SINGLETON_ISSUE)
            .run()
            .expect("""
               src/com/duckduckgo/lint/Singleton.kt:5: Error: The Singleton annotation must not be used [NoSingletonAnnotation]
                 @Singleton
                 ~~~~~~~~~~
               1 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun whenSingletonAnnotationNotFoundThenSucceed() {
        lint()
            .files(kt("""
              package com.duckduckgo.lint
    
                class Duck {
                    fun quack() {                    
                    }
                }
            """).indented())
            .allowCompilationErrors()
            .issues(NoSingletonDetector.NO_SINGLETON_ISSUE)
            .run()
            .expectClean()
    }
}
