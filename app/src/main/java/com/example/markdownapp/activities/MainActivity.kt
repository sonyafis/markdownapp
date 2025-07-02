package com.example.markdownapp.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.markdownapp.databinding.ActivityMainBinding
import com.example.markdownapp.utils.FileUtils
import com.example.markdownapp.utils.ImageCache
import com.example.markdownapp.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val fileRequestCode = 1
    private val imageCache = ImageCache()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLocalFile.setOnClickListener { openFilePicker() }
        binding.btnLoadUrl.setOnClickListener { loadFromUrl() }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/markdown"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, fileRequestCode)
    }

    private fun loadFromUrl() {
        var url = binding.etUrl.text.toString().trim()

        if (url.isBlank()) {
            showError("Пожалуйста, введите URL")
            return
        }

        // Удаляем возможные слеши в конце URL
        url = url.removeSuffix("/")

        try {
            // Парсим URL для проверки его валидности
            val uri = Uri.parse(url)
            if (uri.host == null) {
                showError("Неверный формат URL")
                return
            }
        } catch (e: Exception) {
            showError("Неверный формат URL")
            return
        }

        // Преобразование GitHub ссылки в raw-формат
        url = convertToRawGitHubUrl(url)

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val content = NetworkUtils.downloadTextFile(url)
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    if (content.isBlank()) {
                        showError("Файл пустой")
                    } else {
                        openViewer(content)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    handleDownloadError(e, url)
                }
            }
        }
    }

    private fun handleDownloadError(e: Exception, originalUrl: String) {
        when {
            e is java.net.UnknownHostException -> {
                showError("Не удалось подключиться к серверу")
            }
            e is java.net.SocketTimeoutException -> {
                showError("Таймаут соединения")
            }
            e is java.io.FileNotFoundException -> {
                // Попробуем альтернативный URL для GitHub
                if (originalUrl.contains("github.com")) {
                    val alternativeUrl = originalUrl
                        .replace("https://", "http://")
                        .replace("http://", "https://")
                    showError("Файл не найден. Попробуйте: $alternativeUrl")
                } else {
                    showError("Файл не найден по указанному URL")
                }
            }
            else -> {
                showError("Ошибка загрузки: ${e.localizedMessage ?: "Неизвестная ошибка"}")
            }
        }
    }

    private fun convertToRawGitHubUrl(url: String): String {
        return when {
            url.contains("github.com") && !url.contains("raw.githubusercontent.com") -> {
                url.replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/")
            }
            url.startsWith("https://raw.github.com/") -> {
                url.replace("https://raw.github.com/", "https://raw.githubusercontent.com/")
            }
            else -> url
        }
    }

    private fun isValidMarkdownUrl(url: String): Boolean {
        val pattern = Pattern.compile(
            "^https?://(raw\\.)?githubusercontent\\.com/.+\\.md$",
            Pattern.CASE_INSENSITIVE
        )
        return pattern.matcher(url).matches()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileRequestCode && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val content = FileUtils.readFileContent(this, uri)
                    openViewer(content)
                } catch (e: Exception) {
                    showError("File read error: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun openViewer(content: String) {
        Intent(this, ViewerActivity::class.java).apply {
            putExtra("content", content)
            startActivity(this)
        }
    }
}