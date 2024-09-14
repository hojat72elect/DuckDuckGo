

package com.duckduckgo.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

@Suppress("UnstableApiUsage")
class NoSystemLoadLibraryDetectorTest {
    @Test
    fun whenSystemDotLibraryFoundThenFailWithError() {
        lint()
            .files(kt("""
              package com.duckduckgo.lint
    
                class Duck {
                    fun quack() {
                        System.loadLibrary()
                    }
                }
            """).indented())
            .issues(NoSystemLoadLibraryDetector.NO_SYSTEM_LOAD_LIBRARY)
            .run()
            .expect("""
               src/com/duckduckgo/lint/Duck.kt:5: Error: System.loadLibrary() should not be used. [NoSystemLoadLibrary]
                         System.loadLibrary()
                                ~~~~~~~~~~~
               1 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun whenSystemDotLibraryNotFoundThenFailWithError() {
        lint()
            .files(kt("""
              package com.duckduckgo.lint
    
                fun loadLibrary() {}

                class Duck {
                    fun quack() {
                        loadLibrary()
                    }
                }
            """).indented())
            .allowCompilationErrors()
            .issues(NoSystemLoadLibraryDetector.NO_SYSTEM_LOAD_LIBRARY)
            .run()
            .expectClean()
    }
}
