package com.example.markdownapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {
    fun downloadTextFile(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        return try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    fun downloadImage(url: String): Bitmap {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        return try {
            BitmapFactory.decodeStream(connection.inputStream)
                ?: throw IOException("Could not decode image")
        } finally {
            connection.disconnect()
        }
    }

    fun isValidMarkdownUrl(url: String): Boolean {
        return url.startsWith("https://raw.githubusercontent.com/")
                && url.endsWith(".md")
    }
}