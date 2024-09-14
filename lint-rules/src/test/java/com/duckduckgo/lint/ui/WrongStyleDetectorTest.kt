

package com.duckduckgo.lint.ui

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.xml
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

@Suppress("UnstableApiUsage")
class WrongStyleDetectorTest {

    @Test
    fun whenStyleCorrectThenSucces() {
        lint().files(
            xml(
                "res/xml/style.xml",
                """
                    <style name="Widget.DuckDuckGo.DaxButton.TextButton.Primary">
                        <item name="android:textColor">@color/button_primary_text_color_selector</item>
                        <item name="backgroundTint">@color/button_primary_container_selector</item>
                        <item name="rippleColor">@color/button_primary_ripple_selector</item>
                        <item name="iconTint">@color/button_primary_text_color_selector</item>
                    </style>
                """,
            ).indented(),
        )
            .issues(WrongStyleDetector.WRONG_STYLE_NAME)
            .run()
            .expectClean()
    }

    @Test
    fun whenStyleNameIsWrongThenFail() {
        lint().files(
            xml(
                "res/xml/style.xml",
                """
                    <style name="SomethingSomethingButton">
                        <item name="android:textColor">@color/button_primary_text_color_selector</item>
                        <item name="backgroundTint">@color/button_primary_container_selector</item>
                        <item name="rippleColor">@color/button_primary_ripple_selector</item>
                        <item name="iconTint">@color/button_primary_text_color_selector</item>
                    </style>
                """,
            ).indented(),
        )
            .issues(WrongStyleDetector.WRONG_STYLE_NAME)
            .run()
            .expect(
                """
                res/xml/style.xml:1: Error: Style names should follow the convention and start with Widget.DuckDuckGo. [WrongStyleName]
                <style name="SomethingSomethingButton">
                 ~~~~~
                1 errors, 0 warnings
            """,
            )
    }

    @Test
    fun whenStyleHasWidthThenFail() {
        lint().files(
            xml(
                "res/xml/style.xml",
                """
                    <style name="Widget.DuckDuckGo.DaxButton.TextButton.Primary">
                        <item name="android:layout_width">match_parent</item>
                        <item name="rippleColor">@color/button_primary_ripple_selector</item>
                        <item name="iconTint">@color/button_primary_text_color_selector</item>
                    </style>
                """,
            ).indented(),
        )
            .issues(WrongStyleDetector.WRONG_STYLE_PARAMETER)
            .run()
            .expect(
                """
                res/xml/style.xml:2: Error: Styles should not modify android:layout_height or android:layout_width [WrongStyleParameter]
                    <item name="android:layout_width">match_parent</item>
                     ~~~~
                1 errors, 0 warnings
            """,
            )
    }

    @Test
    fun whenStyleHasHeighthenFail() {
        lint().files(
            xml(
                "res/xml/style.xml",
                """
                    <style name="Widget.DuckDuckGo.DaxButton.TextButton.Primary">
                        <item name="android:layout_width">match_parent</item>
                        <item name="rippleColor">@color/button_primary_ripple_selector</item>
                        <item name="iconTint">@color/button_primary_text_color_selector</item>
                    </style>
                """,
            ).indented(),
        )
            .issues(WrongStyleDetector.WRONG_STYLE_PARAMETER)
            .run()
            .expect(
                """
                res/xml/style.xml:2: Error: Styles should not modify android:layout_height or android:layout_width [WrongStyleParameter]
                    <item name="android:layout_width">match_parent</item>
                     ~~~~
                1 errors, 0 warnings
            """,
            )
    }
}
