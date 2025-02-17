

package com.duckduckgo.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestMode
import com.duckduckgo.lint.NoHardcodedCoroutineDispatcherDetector.Companion
import com.duckduckgo.lint.NonCancellableDetector.Companion.ISSUE_NON_CANCELLABLE
import org.junit.Assert.*
import org.junit.Test

class NonCancellableDetectorTest {

    @Test
    fun whenUsingNonCancellableToLaunchCoroutineThenDetectedAsAViolation() {
        val callSite = """
              package com.duckduckgo.lint
              import kotlinx.coroutines.CustomScope
                
                class Duck {
                    private val scope = CustomScope()
                    fun quack() {
                       scope.launch(NonCancellable) { }                 
                    }
                }
            """

        assertLintError(listOf(TestFiles.kotlin(callSite).indented(), TestFiles.kt(classDefinitions)),
            """
                src/com/duckduckgo/lint/Duck.kt:7: Error: Avoid using NonCancellable when launching a coroutine. [NonCancellableUsage]
                         scope.launch(NonCancellable) { }                 
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
            """)
    }

    @Test
    fun whenUsingNonCancellableAndDispatcherToLaunchCoroutineThenDetectedAsAViolation() {
        val callSite = """
              package com.duckduckgo.lint
              import kotlinx.coroutines.CustomScope
                
                class Duck {
                    private val scope = CustomScope()
                    fun quack() {
                       scope.launch(dispatcherProvider.io() + NonCancellable) { }                 
                    }
                }
            """

        assertLintError(listOf(TestFiles.kotlin(callSite).indented(), TestFiles.kt(classDefinitions)),
            """
                src/com/duckduckgo/lint/Duck.kt:7: Error: Avoid using NonCancellable when launching a coroutine. [NonCancellableUsage]
                         scope.launch(dispatcherProvider.io() + NonCancellable) { }                 
                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
            """)
    }

    @Test
    fun whenUsingNonCancellableInsideWithContextThenDetectedAsANotViolation() {
        val callSite = """
              package com.duckduckgo.lint
              import kotlinx.coroutines.CustomScope
                
                class Duck {
                    private val scope = CustomScope()
                    fun quack() {
                        scope.launch {
                            withContext(NonCancellable) {
                            }
                        }                
                    }
                }
            """

        assertNotLintError(listOf(TestFiles.kotlin(callSite).indented(), TestFiles.kt(classDefinitions)))
    }

    private val classDefinitions = """
package kotlinx.coroutines

import kotlin.coroutines.CoroutineContext

class CustomScope {
    fun launch(
        context: CoroutineContext
    ): Job {
        return Job()
    }
}
        """

    private fun assertLintError(files: List<TestFile>, expectedError: String) {
        TestLintTask.lint()
            .files(*files.toTypedArray())
            .issues(ISSUE_NON_CANCELLABLE)
            .testModes(TestMode.DEFAULT)
            .run()
            .expectContains(expectedError)
    }

    private fun assertNotLintError(files: List<TestFile>) {
        TestLintTask.lint()
            .files(*files.toTypedArray())
            .issues(ISSUE_NON_CANCELLABLE)
            .testModes(TestMode.DEFAULT)
            .run()
            .expectClean()
    }
}
