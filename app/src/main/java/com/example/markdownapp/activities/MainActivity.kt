package com.example.markdownapp.activities

import android.content.Intent
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
        val url = binding.etUrl.text.toString()
        if (url.isBlank()) {
            Toast.makeText(this, "Please enter URL", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val content = NetworkUtils.downloadTextFile(url)
                withContext(Dispatchers.Main) {
                    openViewer(content)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileRequestCode && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val content = FileUtils.readFileContent(this, uri)
                    openViewer(content)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openViewer(content: String) {
        val intent = Intent(this, ViewerActivity::class.java).apply {
            putExtra("content", content)
        }
        startActivity(intent)
    }
}