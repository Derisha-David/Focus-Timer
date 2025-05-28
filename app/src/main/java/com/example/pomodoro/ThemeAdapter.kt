package com.example.pomodoro

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ThemeAdapter(
    private val context: Context,
    private val themes: List<ThemeItem>,
    private val onThemeSelected: (ThemeItem) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {

    private var selectedPosition = getSelectedThemePosition()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppTheme", Context.MODE_PRIVATE)

    class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorSample: View = itemView.findViewById(R.id.themeColorSample)
        val themeName: TextView = itemView.findViewById(R.id.themeNameText)
        val radioButton: RadioButton = itemView.findViewById(R.id.themeRadioButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.theme_selection_item, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        try {
            val theme = themes[position]
            
            // Set theme name safely
            try {
                holder.themeName.text = theme.name
            } catch (e: Exception) {
                e.printStackTrace()
                // If setting name fails, use a default
                holder.themeName.text = "Theme ${position + 1}"
            }
            
            // Set color sample safely
            try {
                holder.colorSample.setBackgroundColor(context.getColor(theme.primaryColorResId))
            } catch (e: Exception) {
                e.printStackTrace()
                // If setting color fails, use a default color
                holder.colorSample.setBackgroundColor(android.graphics.Color.LTGRAY)
            }
            
            // Set radio button state
            try {
                holder.radioButton.isChecked = position == selectedPosition
            } catch (e: Exception) {
                e.printStackTrace()
                // Continue even if radio button fails
            }
            
            // Make entire row clickable with better feedback
            holder.itemView.apply {
                isClickable = true
                isFocusable = true
                
                // Set click listener for the entire item
                setOnClickListener {
                    try {
                        // Provide haptic feedback for better user experience
                        performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                        
                        val previousSelected = selectedPosition
                        selectedPosition = position
                        
                        // Update radio buttons
                        try {
                            notifyItemChanged(previousSelected)
                            notifyItemChanged(selectedPosition)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Continue even if updates fail
                        }
                        
                        // Save selected theme
                        try {
                            saveSelectedTheme(position)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Continue even if saving fails
                        }
                        
                        // Notify the activity about the theme change
                        try {
                            onThemeSelected(theme)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Continue even if notification fails
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Last resort error handling
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Last resort error handling for the entire binding process
        }
    }

    override fun getItemCount(): Int = themes.size

    private fun saveSelectedTheme(position: Int) {
        sharedPreferences.edit().apply {
            putInt("selected_theme", position)
            apply()
        }
    }

    private fun getSelectedThemePosition(): Int {
        return sharedPreferences.getInt("selected_theme", 0) // Default to first theme
    }
}

data class ThemeItem(
    val id: Int,
    val name: String,
    val primaryColorResId: Int,
    val secondaryColorResId: Int,
    val tertiaryColorResId: Int,
    val accentColorResId: Int,
    val textColorResId: Int
)
