package com.bonfire.shohojsheba.ui.components

import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.text.HtmlCompat

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    val annotated = spanned.toAnnotatedString()
    Text(
        text = annotated,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
}

fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString

    var currentIndex = 0
    val spans = spanned.getSpans(0, spanned.length, Any::class.java)

    while (currentIndex < spanned.length) {
        val nextBulletIndex = spans.find { it is BulletSpan && spanned.getSpanStart(it) == currentIndex }

        if (nextBulletIndex != null) {
            val end = spanned.getSpanEnd(nextBulletIndex)
            append("• ") // bullet
            append(spanned.subSequence(currentIndex, end).trim())
            append("\n")
            currentIndex = end
        } else {
            append(spanned[currentIndex].toString())
            currentIndex++
        }
    }

    // Apply bold + italic styles
    spans.forEach { span ->
        val start = spanned.getSpanStart(span)
        val end = spanned.getSpanEnd(span)

        when (span) {
            is StyleSpan -> when (span.style) {
                android.graphics.Typeface.BOLD ->
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)

                android.graphics.Typeface.ITALIC ->
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)

                android.graphics.Typeface.BOLD_ITALIC ->
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic),
                        start, end
                    )
            }
        }
    }
    append("\n") // Ensure clean ending
}
