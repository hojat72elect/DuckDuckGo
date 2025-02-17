

package com.duckduckgo.lint.strings

import com.android.SdkConstants
import com.android.ide.common.resources.usage.ResourceUsageModel
import com.android.tools.lint.detector.api.Category.Companion.CUSTOM_LINT_CHECKS
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.ResourceXmlDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Node

@Suppress("UnstableApiUsage")
class PlaceholderDetector : ResourceXmlDetector(), XmlScanner {

    private fun ignoreFile(context: Context) =
            ResourceUsageModel.isAnalyticsFile(context.file) ||
            !context.project.reportIssues


    override fun visitDocument(context: XmlContext, document: Document) {
        if (ignoreFile(context)) {
            return
        }

        val root = document.documentElement
        val translatable: Attr? = root?.getAttributeNode(SdkConstants.ATTR_TRANSLATABLE)
        if (translatable?.value != SdkConstants.VALUE_FALSE) {
            if (context.file.parentFile?.name == SdkConstants.FD_RES_VALUES) {
                visitNode(context, document)
            }
            return
        }
    }

    private fun visitNode(context: XmlContext, node: Node) {
        val nodeType = node.nodeType
        if (nodeType == Node.TEXT_NODE) {
            val text = node.nodeValue
            if (PLACEHOLDERS.firstOrNull { text.contains(it) } != null) {
                context.report(
                    PLACEHOLDER_MISSING_POSITION, node, location = context.getNameLocation(node),
                    "Placeholder is missing the position in a string"
                )
            }
        }

        // Visit children
        val childNodes = node.childNodes
        var i = 0
        val n = childNodes.length
        while (i < n) {
            val child = childNodes.item(i)
            visitNode(context, child)
            i++
        }
    }


    companion object {
        val PLACEHOLDERS = listOf(
            "%s", "%d", "%f", "%o",
        )

        val PLACEHOLDER_MISSING_POSITION = Issue.create(
            id = "PlaceholderMissingPosition",
            briefDescription = "Placeholder is missing the position in a string",
            explanation = """
            A string that has placeholders must have a position for the strings, i.e. %1$\d instead of %s""",
            category = CUSTOM_LINT_CHECKS,
            priority = 10,
            severity = Severity.ERROR,
            implementation = Implementation(
                PlaceholderDetector::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }
}
