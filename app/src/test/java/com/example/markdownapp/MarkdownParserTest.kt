package com.example.markdownapp

import com.example.markdownapp.models.HeadingElement
import com.example.markdownapp.models.TableElement
import com.example.markdownapp.models.TextElement
import com.example.markdownapp.parsers.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownParserTest {
    private val parser = MarkdownParser()

    @Test
    fun testHeadingParsing() {
        val text = "# Heading 1\n## Heading 2"
        val elements = parser.parse(text)

        assertEquals(2, elements.size)
        assertTrue(elements[0] is HeadingElement)
        assertEquals(1, (elements[0] as HeadingElement).level)
        assertEquals("Heading 1", (elements[0] as HeadingElement).text)

        assertTrue(elements[1] is HeadingElement)
        assertEquals(2, (elements[1] as HeadingElement).level)
    }
}