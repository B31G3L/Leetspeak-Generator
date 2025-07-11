package com.beigel.leetSpeak_Generator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Modern RecyclerView Adapter for leet selector with DiffUtil for efficient updates
 * Migrated to Kotlin with improved performance and type safety
 */
class LeetSelectorAdapter(
    private val listener: OnLeetSelectedListener
) : ListAdapter<LeetOption, LeetSelectorAdapter.LeetViewHolder>(LeetOptionDiffCallback()) {

    /**
     * Interface for handling leet selection events
     */
    interface OnLeetSelectedListener {
        fun onLeetSelected(leetOption: LeetOption)
        fun onLeetPreview(leetOption: LeetOption)
        fun onEditLeet(leetOption: LeetOption)
        fun onToggleFavorite(leetOption: LeetOption)
        fun onQuickTest(leetOption: LeetOption)
        fun onShowTable(leetOption: LeetOption)
    }

    /**
     * Constructor for backward compatibility with list parameter
     */
    constructor(
        leetOptions: List<LeetOption>,
        listener: OnLeetSelectedListener
    ) : this(listener) {
        submitList(leetOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leet_selector, parent, false)
        return LeetViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: LeetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Updates the adapter with new options using DiffUtil
     */
    fun updateOptions(newOptions: List<LeetOption>) {
        submitList(newOptions)
    }

    /**
     * ViewHolder for leet option items
     */
    class LeetViewHolder(
        itemView: View,
        private val listener: OnLeetSelectedListener
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardView: MaterialCardView = itemView as MaterialCardView
        private val iconLeet: ImageView = itemView.findViewById(R.id.iconLeet)
        internal val textLeetName: TextView = itemView.findViewById(R.id.textLeetName) // ✅ internal
        private val textLeetDescription: TextView = itemView.findViewById(R.id.textLeetDescription)
        private val textPreview: TextView = itemView.findViewById(R.id.textPreview)
        internal val iconSelected: ImageView = itemView.findViewById(R.id.iconSelected) // ✅ internal statt private
        internal val iconFavorite: ImageView = itemView.findViewById(R.id.iconFavorite) // ✅ internal statt private
        private val buttonEdit: MaterialButton = itemView.findViewById(R.id.buttonEdit)
        private val buttonQuickTest: MaterialButton = itemView.findViewById(R.id.buttonQuickTest)
        private val buttonShowTable: MaterialButton = itemView.findViewById(R.id.buttonShowTable)
        private val previewSection: View = itemView.findViewById(R.id.previewSection)

        fun bind(option: LeetOption) {
            // Set basic information
            iconLeet.setImageResource(option.iconResId)
            textLeetName.text = option.name
            textLeetDescription.text = option.description

            // Generate and set preview
            val preview = generatePreview(option)
            textPreview.text = preview

            // Update selection state
            iconSelected.visibility = if (option.isSelected) View.VISIBLE else View.GONE

            // Update favorite state
            iconFavorite.visibility = if (option.isFavorite) View.VISIBLE else View.GONE

            // Show edit button only for custom leets
            buttonEdit.visibility = if (option.isCustom) View.VISIBLE else View.GONE

            // Update card appearance based on selection
            updateCardAppearance(option.isSelected)

            // Set up click listeners
            setupClickListeners(option)
        }

        /**
         * Updates card appearance based on selection state
         * ✅ Public method für externe Aufrufe
         */
        fun updateCardAppearance(isSelected: Boolean) {
            val context = itemView.context

            if (isSelected) {
                cardView.strokeWidth = 3
                cardView.strokeColor = context.getColor(R.color.accent)
                cardView.cardElevation = 8f
            } else {
                cardView.strokeWidth = 1
                cardView.strokeColor = android.graphics.Color.TRANSPARENT
                cardView.cardElevation = 2f
            }
        }

        /**
         * Sets up all click listeners for the item
         */
        private fun setupClickListeners(option: LeetOption) {
            // Main card click
            cardView.setOnClickListener {
                listener.onLeetSelected(option)
            }

            // Long click for favorites
            cardView.setOnLongClickListener {
                listener.onToggleFavorite(option)
                AnimationHelper.pulse(iconFavorite, 1)
                true
            }

            // Favorite icon click
            iconFavorite.setOnClickListener {
                listener.onToggleFavorite(option)
                AnimationHelper.pulse(iconFavorite, 1)
            }

            // Edit button click
            buttonEdit.setOnClickListener {
                listener.onEditLeet(option)
            }

            // Quick test button click
            buttonQuickTest.setOnClickListener {
                listener.onQuickTest(option)
                AnimationHelper.pulse(buttonQuickTest, 1)
            }

            // Show table button click
            buttonShowTable.setOnClickListener {
                listener.onShowTable(option)
                AnimationHelper.pulse(buttonShowTable, 1)
            }

            // Preview section hover effect
            previewSection.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    listener.onLeetPreview(option)
                }
            }
        }

        /**
         * Generates preview text for the leet option
         */
        private fun generatePreview(option: LeetOption): String {
            return when (option.mode) {
                LeetManager.MODE_SIMPLE -> {
                    LeetTranslator.translate("Hello", LeetTranslator.TranslationMode.SIMPLE, null)
                }
                LeetManager.MODE_EXTENDED -> {
                    LeetTranslator.translate("Hello", LeetTranslator.TranslationMode.EXTENDED, null)
                }
                LeetManager.MODE_CUSTOM -> {
                    // For custom profiles, we would need access to the profile
                    // This is a simplified version that assumes basic translation
                    "H3ll0" // Placeholder - would need actual profile data
                }
                else -> "H3110"
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class LeetOptionDiffCallback : DiffUtil.ItemCallback<LeetOption>() {

        override fun areItemsTheSame(oldItem: LeetOption, newItem: LeetOption): Boolean {
            return if (oldItem.isCustom && newItem.isCustom) {
                oldItem.customIndex == newItem.customIndex
            } else {
                oldItem.mode == newItem.mode && oldItem.isCustom == newItem.isCustom
            }
        }

        override fun areContentsTheSame(oldItem: LeetOption, newItem: LeetOption): Boolean {
            return oldItem == newItem
        }

        /**
         * Optional: Define change payloads for more granular updates
         */
        override fun getChangePayload(oldItem: LeetOption, newItem: LeetOption): Any? {
            val changes = mutableListOf<String>()

            if (oldItem.isSelected != newItem.isSelected) {
                changes.add("selection")
            }

            if (oldItem.isFavorite != newItem.isFavorite) {
                changes.add("favorite")
            }

            if (oldItem.name != newItem.name) {
                changes.add("name")
            }

            return if (changes.isNotEmpty()) changes else null
        }
    }

    /**
     * Partial binding for efficient updates when using payloads
     */
    override fun onBindViewHolder(holder: LeetViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val item = getItem(position)

            @Suppress("UNCHECKED_CAST")
            val changes = payloads.flatMap { it as List<String> }

            if ("selection" in changes) {
                holder.iconSelected.visibility = if (item.isSelected) View.VISIBLE else View.GONE
                holder.updateCardAppearance(item.isSelected)
            }

            if ("favorite" in changes) {
                holder.iconFavorite.visibility = if (item.isFavorite) View.VISIBLE else View.GONE
            }

            if ("name" in changes) {
                holder.textLeetName.text = item.name
            }
        }
    }
}