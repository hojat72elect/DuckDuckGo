

package com.duckduckgo.lint

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.checks.infrastructure.TestMode
import com.duckduckgo.lint.NoDispatcherComputation.Companion.ISSUE_AVOID_COMPUTATION
import org.junit.Assert.*
import org.junit.Test

class NoDispatcherComputationTest {

    @Test
    fun whenUsingComputationDispatcherThenDetectedAsViolation() {
        val callSite = """
                package com.duckduckgo.lint
                import com.duckduckgo.common.utils.DispatcherProvider

                class Duck(private val dispatcherProvider: DispatcherProvider) {
                    fun quack() {
                        dispatcherProvider.computation()                 
                    }
                }
            """

        assertLintError(listOf(TestFiles.kotlin(callSite).indented(), TestFiles.kt(classDefinitions)),
            """
                src/com/duckduckgo/lint/Duck.kt:6: Error: Avoid using computation() if this is not a valid usecase. [AvoidComputationUsage]
                        dispatcherProvider.computation()                 
                        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
            """)
    }

    private val classDefinitions = """
package com.duckduckgo.common.utils

class DispatcherProvider {

    fun main() = {}
    fun computation() = {}
    fun io() = {}
    fun unconfined() = {}
}
        """

    private fun assertLintError(files: List<TestFile>, expectedError: String) {
        TestLintTask.lint()
            .files(*files.toTypedArray())
            .issues(ISSUE_AVOID_COMPUTATION)
            .testModes(TestMode.DEFAULT)
            .run()
            .expectContains(expectedError)
    }
}
