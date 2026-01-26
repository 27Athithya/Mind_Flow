package com.example.myapplication.utils

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.DialogEmojiPickerBinding
import com.example.myapplication.databinding.ItemEmojiBinding

class EmojiPickerDialog(
    context: Context,
    private val onEmojiSelected: (String) -> Unit
) : Dialog(context) {

    private val binding: DialogEmojiPickerBinding

    init {
        binding = DialogEmojiPickerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        // Set dialog properties
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        setupRecyclerView()
        setupCloseButton()
    }

    private fun setupRecyclerView() {
        val emojis = listOf(
            "🎯", "💪", "🧘", "🥗", "😴", "📚", "🎵", "✈️",
            "🏃", "🧠", "💧", "🌱", "🎨", "💼", "🏠", "❤️",
            "⭐", "🔥", "💡", "🎪", "🏆", "🌟", "🎭", "🎸",
            "📱", "💻", "🎮", "🍎", "🥦", "🏊", "🚶", "🧘‍♀️",
            "☕", "🌅", "🌙", "🏋️", "🚴", "🎬", "📖", "✍️",
            "🌿", "🍃", "🌸", "🌺", "🦋", "🌈", "⚡", "🎨",
            "🎪", "🎭", "🎨", "🎯", "🎲", "🎳", "🎪", "🎨"
        )

        val adapter = EmojiAdapter(emojis) { emoji ->
            onEmojiSelected(emoji)
            dismiss()
        }

        binding.rvEmojis.apply {
            layoutManager = GridLayoutManager(context, 8)
            this.adapter = adapter
        }
    }

    private fun setupCloseButton() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    inner class EmojiAdapter(
        private val emojis: List<String>,
        private val onEmojiClick: (String) -> Unit
    ) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
            val binding = ItemEmojiBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return EmojiViewHolder(binding)
        }

        override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
            holder.bind(emojis[position])
        }

        override fun getItemCount() = emojis.size

        inner class EmojiViewHolder(private val binding: ItemEmojiBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(emoji: String) {
                binding.tvEmoji.text = emoji
                binding.tvEmoji.setOnClickListener {
                    onEmojiClick(emoji)
                }
            }
        }
    }
}
