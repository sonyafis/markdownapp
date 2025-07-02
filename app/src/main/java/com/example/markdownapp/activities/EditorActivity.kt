package com.example.markdownapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.markdownapp.databinding.ActivityEditorBinding

class EditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val content = intent.getStringExtra("content") ?: ""
        binding.etMarkdown.setText(content)

        binding.btnBold.setOnClickListener { wrapSelection("**") }
        binding.btnItalic.setOnClickListener { wrapSelection("*") }
        binding.btnCode.setOnClickListener { wrapSelection("`") }
        binding.btnSave.setOnClickListener { saveAndPreview() }
    }

    private fun wrapSelection(wrapper: String) {
        val selectionStart = binding.etMarkdown.selectionStart
        val selectionEnd = binding.etMarkdown.selectionEnd

        if (selectionStart == selectionEnd) {
            // No selection, just insert wrapper
            binding.etMarkdown.text.insert(selectionStart, "$wrapper$wrapper")
            binding.etMarkdown.setSelection(selectionStart + wrapper.length)
        } else {
            // Wrap selection
            val text = binding.etMarkdown.text
            text.replace(selectionStart, selectionEnd, "$wrapper${text.subSequence(selectionStart, selectionEnd)}$wrapper")
            binding.etMarkdown.setSelection(selectionEnd + 2 * wrapper.length)
        }
    }

    private fun saveAndPreview() {
        val content = binding.etMarkdown.text.toString()
        val intent = Intent(this, ViewerActivity::class.java).apply {
            putExtra("content", content)
        }
        startActivity(intent)
        finish()
    }
}