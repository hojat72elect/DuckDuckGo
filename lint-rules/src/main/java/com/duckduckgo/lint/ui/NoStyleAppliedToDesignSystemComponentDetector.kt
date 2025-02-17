

package com.duckduckgo.lint.ui

import com.android.SdkConstants.ATTR_STYLE
import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Category.Companion.CUSTOM_LINT_CHECKS
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.TextFormat
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Element

@Suppress("UnstableApiUsage")
class NoStyleAppliedToDesignSystemComponentDetector : LayoutDetector() {

    override fun getApplicableElements() = DesignSystemLintExtensions.ANDROID_DESIGN_COMPONENTS

    override fun appliesTo(folderType: ResourceFolderType): Boolean {
        return folderType == ResourceFolderType.LAYOUT
    }

    override fun visitElement(
        context: XmlContext,
        element: Element
    ) {
        if (element.hasAttribute(ATTR_STYLE)) {
            reportIssue(context, element)
        }
    }

    private fun reportIssue(
        context: XmlContext,
        element: Element
    ) {
        context.report(
            issue = STYLE_IN_DESIGN_SYSTEM_COMPONENT,
            location = context.getNameLocation(element),
            message = STYLE_IN_DESIGN_SYSTEM_COMPONENT.getExplanation(TextFormat.RAW)
        )
    }

    companion object {

        val STYLE_IN_DESIGN_SYSTEM_COMPONENT = Issue
            .create(
                id = "StyleInDesignSystemComponent",
                briefDescription = "Design System Components should not be styled.",
                explanation = "Design System Components should not be styled. Consider creating a new Component or use one of the Components already created",
                moreInfo = "https://app.asana.com/0/1202857801505092/list",
                category = CUSTOM_LINT_CHECKS,
                priority = 10,
                severity = Severity.ERROR,
                androidSpecific = true,
                implementation = Implementation(
                    NoStyleAppliedToDesignSystemComponentDetector::class.java,
                    Scope.RESOURCE_FILE_SCOPE
                )
            )
    }
}
