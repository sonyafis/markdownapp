package com.example.markdownapp.models

import android.text.SpannableString

sealed class MarkdownElement

data class HeadingElement(
    val level: Int,
    val text: String
) : MarkdownElement()

data class TextElement(
    val text: SpannableString
) : MarkdownElement()

data class TableElement(
    val rows: List<List<String>>
) : MarkdownElement()

data class ImageElement(
    val altText: String,
    val url: String
) : MarkdownElement()