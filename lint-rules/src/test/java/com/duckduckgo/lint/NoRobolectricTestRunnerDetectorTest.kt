

package com.duckduckgo.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

@Suppress("UnstableApiUsage")
class NoRobolectricTestRunnerDetectorTest {
    @Test
    fun whenRobolectricTestRunnerFoundThenFailWithError() {
        lint()
            .files(kt(
                """
                package com.duckduckgo.lint
                import java.lang.Class

                class Runner
    
                annotation class RunWith(val value: KClass<out Runner?>)
        
                class RobolectricTestRunner
    
                @RunWith(RobolectricTestRunner::class)
                class Duck {
                    fun quack() {                    
                    }
                }
            """
            ).indented())
            .issues(NoRobolectricTestRunnerDetector.NO_ROBOLECTRIC_TEST_RUNNER_ISSUE)
            .run()
            .expect("""
                src/com/duckduckgo/lint/Runner.kt:10: Error: The RobolectricTestRunner parameter must not be used in the RunWith annotation. [NoRobolectricTestRunnerAnnotation]
                @RunWith(RobolectricTestRunner::class)
                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun whenRobolectricTestRunnerNotFoundThenSucceed() {
        lint()
            .files(kt("""
                package com.duckduckgo.lint
                import java.lang.Class

                class Runner
    
                annotation class RunWith(val value: KClass<out Runner?>)
        
                class AndroidJUnit4
    
                @RunWith(AndroidJUnit4::class)
                class Duck {
                    fun quack() {                    
                    }
                }
            """).indented())
            .allowCompilationErrors()
            .issues(NoRobolectricTestRunnerDetector.NO_ROBOLECTRIC_TEST_RUNNER_ISSUE)
            .run()
            .expectClean()
    }

    @Test
    fun whenParameterizedRobolectricTestRunnerNotFoundThenSucceed() {
        lint()
            .files(kt("""
                package com.duckduckgo.lint
                import java.lang.Class

                class Runner
    
                annotation class RunWith(val value: KClass<out Runner?>)
        
                class ParameterizedRobolectricTestRunner
    
                @RunWith(ParameterizedRobolectricTestRunner::class)
                class Duck {
                    fun quack() {                    
                    }
                }
            """).indented())
            .allowCompilationErrors()
            .issues(NoRobolectricTestRunnerDetector.NO_ROBOLECTRIC_TEST_RUNNER_ISSUE)
            .run()
            .expectClean()
    }
}
