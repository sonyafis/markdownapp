package com.example.markdownapp

import android.app.Application
import com.example.markdownapp.utils.ImageCache

class App : Application() {
    val imageCache = ImageCache()
}