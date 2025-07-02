package com.example.markdownapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.markdownapp.App
import com.example.markdownapp.adapters.MarkdownAdapter
import com.example.markdownapp.databinding.ActivityViewerBinding
import com.example.markdownapp.parsers.MarkdownParser
import com.example.markdownapp.utils.ImageCache

class ViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewerBinding
    private lateinit var adapter: MarkdownAdapter
    private lateinit var parser: MarkdownParser
    private lateinit var imageCache: ImageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        parser = MarkdownParser()
        adapter = MarkdownAdapter(this, this)
        imageCache = (application as App).imageCache

        val content = intent.getStringExtra("content") ?: ""
        val elements = parser.parse(content)
        adapter.submitList(elements)

        binding.rvMarkdown.layoutManager = LinearLayoutManager(this)
        binding.rvMarkdown.adapter = adapter

        binding.fabEdit.setOnClickListener {
            val intent = Intent(this, EditorActivity::class.java).apply {
                putExtra("content", content)
            }
            startActivity(intent)
        }
    }
}