package com.example.markdownapp.parsers

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import com.example.markdownapp.models.HeadingElement
import com.example.markdownapp.models.ImageElement
import com.example.markdownapp.models.MarkdownElement
import com.example.markdownapp.models.TableElement
import com.example.markdownapp.models.TextElement

class MarkdownParser {
    private val headingRegex = Regex("^(#{1,6})\\s(.*)")
    private val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    private val italicRegex = Regex("\\*(.*?)\\*")
    private val codeRegex = Regex("`(.*?)`")
    private val tableRegex = Regex("^\\|(.+)\\|$")
    private val imageRegex = Regex("!\\[(.*?)\\]\\((.*?)\\)")

    fun parse(text: String): List<MarkdownElement> {
        val lines = text.split("\n")
        val elements = mutableListOf<MarkdownElement>()

        var inTable = false
        val tableRows = mutableListOf<List<String>>()

        lines.forEach { line ->
            when {
                line.matches(headingRegex) -> {
                    val (hashes, content) = headingRegex.find(line)!!.destructured
                    elements.add(HeadingElement(hashes.length, content.trim()))
                }
                line.matches(tableRegex) && !inTable -> {
                    inTable = true
                    tableRows.add(parseTableRow(line))
                }
                line.matches(tableRegex) && inTable -> {
                    tableRows.add(parseTableRow(line))
                }
                inTable && line.trim().isEmpty() -> {
                    inTable = false
                    elements.add(TableElement(tableRows.toList()))
                    tableRows.clear()
                }
                line.contains(imageRegex) -> {
                    val (altText, url) = imageRegex.find(line)!!.destructured
                    elements.add(ImageElement(altText, url))
                }
                else -> {
                    if (inTable) {
                        tableRows.add(parseTableRow(line))
                    } else {
                        elements.add(TextElement(processInlineMarkdown(line)))
                    }
                }
            }
        }

        return elements
    }

    private fun parseTableRow(line: String): List<String> {
        return line.split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.matches(Regex("^-+$")) }
    }

    private fun processInlineMarkdown(text: String): SpannableString {
        val spannable = SpannableString(text)

        boldRegex.findAll(text).forEach {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                it.range.first,
                it.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        italicRegex.findAll(text).forEach {
            spannable.setSpan(
                StyleSpan(Typeface.ITALIC),
                it.range.first,
                it.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        codeRegex.findAll(text).forEach {
            spannable.setSpan(
                TypefaceSpan("monospace"),
                it.range.first,
                it.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }
}