package com.example.markdownapp.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.markdownapp.App
import com.example.markdownapp.R
import com.example.markdownapp.models.HeadingElement
import com.example.markdownapp.models.ImageElement
import com.example.markdownapp.models.MarkdownElement
import com.example.markdownapp.models.TableElement
import com.example.markdownapp.models.TextElement
import com.example.markdownapp.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarkdownAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<MarkdownElement, RecyclerView.ViewHolder>(DiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HeadingElement -> TYPE_HEADING
            is TextElement -> TYPE_TEXT
            is TableElement -> TYPE_TABLE
            is ImageElement -> TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADING -> HeadingViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_heading, parent, false)
            )
            TYPE_TEXT -> TextViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_text, parent, false)
            )
            TYPE_TABLE -> TableViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_table, parent, false)
            )
            TYPE_IMAGE -> ImageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_image, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HeadingElement -> (holder as HeadingViewHolder).bind(item)
            is TextElement -> (holder as TextViewHolder).bind(item)
            is TableElement -> (holder as TableViewHolder).bind(item)
            is ImageElement -> (holder as ImageViewHolder).bind(item)
        }
    }

    inner class HeadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tvHeading)

        fun bind(element: HeadingElement) {
            textView.text = element.text
            textView.textSize = when (element.level) {
                1 -> 24f
                2 -> 22f
                3 -> 20f
                4 -> 18f
                5 -> 16f
                6 -> 14f
                else -> 12f
            }
        }
    }

    inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tvText)

        fun bind(element: TextElement) {
            textView.text = element.text
        }
    }

    inner class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tableLayout: TableLayout = itemView.findViewById(R.id.tableLayout)

        fun bind(element: TableElement) {
            tableLayout.removeAllViews()

            element.rows.forEachIndexed { rowIndex, row ->
                val tableRow = TableRow(itemView.context).apply {
                    layoutParams = TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                row.forEach { cell ->
                    val textView = TextView(itemView.context).apply {
                        text = cell
                        setPadding(8, 8, 8, 8)
                        if (rowIndex == 0) {
                            setTypeface(typeface, Typeface.BOLD)
                        }
                    }

                    tableRow.addView(textView)
                }

                tableLayout.addView(tableRow)
            }
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivImage)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)

        fun bind(element: ImageElement) {
            progressBar.visibility = View.VISIBLE
            imageView.visibility = View.GONE

            val imageCache = (itemView.context.applicationContext as App).imageCache
            val cachedBitmap = imageCache.get(element.url)

            if (cachedBitmap != null) {
                imageView.setImageBitmap(cachedBitmap)
                progressBar.visibility = View.GONE
                imageView.visibility = View.VISIBLE
            } else {
                lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val bitmap = NetworkUtils.downloadImage(element.url)
                        imageCache.put(element.url, bitmap)

                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(bitmap)
                            progressBar.visibility = View.GONE
                            imageView.visibility = View.VISIBLE
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            imageView.setImageResource(R.drawable.ic_broken_image)
                            imageView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MarkdownElement>() {
        override fun areItemsTheSame(oldItem: MarkdownElement, newItem: MarkdownElement): Boolean {
            return when {
                oldItem is HeadingElement && newItem is HeadingElement ->
                    oldItem.text == newItem.text && oldItem.level == newItem.level
                oldItem is TextElement && newItem is TextElement ->
                    oldItem.text.toString() == newItem.text.toString()
                oldItem is TableElement && newItem is TableElement ->
                    oldItem.rows == newItem.rows
                oldItem is ImageElement && newItem is ImageElement ->
                    oldItem.url == newItem.url
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: MarkdownElement, newItem: MarkdownElement): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val TYPE_HEADING = 0
        private const val TYPE_TEXT = 1
        private const val TYPE_TABLE = 2
        private const val TYPE_IMAGE = 3
    }
}