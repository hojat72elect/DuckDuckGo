

package com.duckduckgo.lint.ui

import com.android.tools.lint.checks.infrastructure.TestFiles
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

@Suppress("UnstableApiUsage")
class MissingDividerDetectorTest {
    @Test
    fun when1dpHeightThenFail() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/view.xml",
                    """
                <android.support.design.widget.CoordinatorLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:app="http://schemas.android.com/apk/res-auto"
                    xmlns:tools="http://schemas.android.com/tools"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:background="#eeeeee">

                  <View
                      android:id="@+id/divider"
                      android:layout_width="match_parent"
                      android:layout_height="1dp"/>
                      
                </android.support.design.widget.CoordinatorLayout>
            """
                ).indented()
            )
            .issues(MissingDividerDetector.MISSING_HORIZONTAL_DIVIDER)
            .run()
            .expect(
                """
                res/layout/view.xml:9: Error: 1dp height used in a View. Please, use the [HorizontalDivider] Component from the Design System [MissingHorizontalDivider]
                  <View
                   ~~~~
                1 errors, 0 warnings
            """.trimMargin()
            )
    }

    @Test
    fun when1dpWidthThenFail() {
        lint()
            .files(
                TestFiles.xml(
                    "res/layout/view.xml",
                    """
                <android.support.design.widget.CoordinatorLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    xmlns:app="http://schemas.android.com/apk/res-auto"
                    xmlns:tools="http://schemas.android.com/tools"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:background="#eeeeee">

                  <View
                      android:id="@+id/divider"
                      android:layout_width="1dp"
                      android:layout_height="match_parent"/>
                      
                </android.support.design.widget.CoordinatorLayout>
            """
                ).indented()
            )
            .issues(MissingDividerDetector.MISSING_VERTICAL_DIVIDER)
            .run()
            .expect(
                """
                res/layout/view.xml:9: Error: 1dp width used in a View. Please, use the [VerticalDivider] Component from the Design System [MissingVerticalDivider]
                  <View
                   ~~~~
                1 errors, 0 warnings
            """.trimMargin()
            )
    }
}
