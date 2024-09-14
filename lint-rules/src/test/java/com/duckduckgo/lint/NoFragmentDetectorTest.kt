

package com.duckduckgo.lint

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.duckduckgo.lint.NoFragmentDetector.Companion.NO_FRAGMENT_ISSUE
import org.junit.Test

class NoFragmentDetectorTest {

    @Test
    fun whenFragmentExtendedThenFailWithError() {
        TestLintTask.lint()
            .files(TestFiles.kt("""
              package com.duckduckgo.lint
    
                class Fragment

                class Duck : Fragment() {
                    fun quack() {                    
                    }
                }
            """).indented())
            .issues(NO_FRAGMENT_ISSUE)
            .run()
            .expect("""
                src/com/duckduckgo/lint/Fragment.kt:5: Error: Fragment should not be directly extended [NoFragment]
                  class Duck : Fragment() {
                        ~~~~
                1 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun whenFragmentWithParamtExtendedThenFailWithError() {
        TestLintTask.lint()
            .files(TestFiles.kt("""
              package com.duckduckgo.lint
    
                class Fragment(val param: String)

                class Duck(param: String) : Fragment(param) {
                    fun quack() {                    
                    }
                }
            """).indented())
            .issues(NO_FRAGMENT_ISSUE)
            .run()
            .expect("""
                src/com/duckduckgo/lint/Fragment.kt:5: Error: Fragment should not be directly extended [NoFragment]
                  class Duck(param: String) : Fragment(param) {
                        ~~~~
                1 errors, 0 warnings
            """.trimMargin())
    }

    @Test
    fun whenAnyOtherFragmentTypeExtendedThenSuccess() {
        TestLintTask.lint()
            .files(TestFiles.kt("""
              package com.duckduckgo.lint
    
                class DialogFragment(val param: String)

                class Duck(param: String) : DialogFragment(param) {
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