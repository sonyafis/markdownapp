package com.example.markdownapp.utils

import android.graphics.Bitmap
import android.util.LruCache

class ImageCache {
    private val cache = LruCache<String, Bitmap>(10 * 1024 * 1024) // 10MB cache

    fun put(key: String, bitmap: Bitmap) {
        cache.put(key, bitmap)
    }

    fun get(key: String): Bitmap? {
        return cache.get(key)
    }

    fun clear() {
        cache.evictAll()
    }
}