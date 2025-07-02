package com.example.markdownapp.utils

import android.content.Context
import android.net.Uri
import java.io.IOException

object FileUtils {
    fun readFileContent(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().use { it.readText() }
        } ?: throw IOException("Could not read file")
    }
}