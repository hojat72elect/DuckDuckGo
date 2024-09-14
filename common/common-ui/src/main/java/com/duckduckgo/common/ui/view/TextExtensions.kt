

package com.duckduckgo.common.ui.view

import android.content.Context
import android.text.Annotation
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.duckduckgo.common.ui.spans.DuckDuckGoClickableSpan
import com.duckduckgo.mobile.android.R

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true,
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

fun Context.defaultSelectableItemBackground(
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true,
): Int {
    theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, resolveRefs)
    return typedValue.resourceId
}

fun TextView.addClickableLink(
    annotation: String,
    textSequence: CharSequence,
    onClick: () -> Unit,
) {
    val fullText = textSequence as SpannedString
    val spannableString = SpannableString(fullText)
    val annotations = fullText.getSpans(0, fullText.length, Annotation::class.java)
    val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick()
        }
    }

    annotations?.find { it.value == annotation }?.let {
        spannableString.apply {
            setSpan(
                clickableSpan,
                fullText.getSpanStart(it),
                fullText.getSpanEnd(it),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            setSpan(
                UnderlineSpan(),
                fullText.getSpanStart(it),
                fullText.getSpanEnd(it),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
            setSpan(
                ForegroundColorSpan(
                    context.getColorFromAttr(R.attr.daxColorAccentBlue),
                ),
                fullText.getSpanStart(it),
                fullText.getSpanEnd(it),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    text = spannableString
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.addClickableSpan(
    textSequence: CharSequence,
    spans: List<Pair<String, DuckDuckGoClickableSpan>>,
) {
    val fullText = textSequence as SpannedString
    val spannableString = SpannableString(fullText)
    val annotations = fullText.getSpans(0, fullText.length, Annotation::class.java)

    spans.forEach { span ->
        annotations?.find { it.value == span.first }?.let {
            spannableString.apply {
                setSpan(
                    span.second,
                    fullText.getSpanStart(it),
                    fullText.getSpanEnd(it),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                setSpan(
                    ForegroundColorSpan(
                        context.getColorFromAttr(R.attr.daxColorAccentBlue),
                    ),
                    fullText.getSpanStart(it),
                    fullText.getSpanEnd(it),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
            }
        }
    }

    text = spannableString
    movementMethod = LinkMovementMethod.getInstance()
}
