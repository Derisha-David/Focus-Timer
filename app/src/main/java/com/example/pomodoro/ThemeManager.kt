package com.example.pomodoro

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * A utility class to manage theme application across the app
 */
object ThemeManager {
    
    /**
     * Apply the selected theme to an activity
     */
    fun applyTheme(activity: Activity, themeId: Int) {
        val theme = getThemeById(activity, themeId)
        applyThemeColors(activity, theme)
    }
    
    /**
     * Get a theme by its ID
     */
    fun getThemeById(context: Context, themeId: Int): ThemeItem {
        return when (themeId) {
            1 -> ThemeItem(
                id = 1,
                name = "Mint Green",
                primaryColorResId = R.color.mint_primary,
                secondaryColorResId = R.color.mint_secondary,
                tertiaryColorResId = R.color.mint_tertiary,
                accentColorResId = R.color.mint_accent,
                textColorResId = R.color.mint_text
            )
            2 -> ThemeItem(
                id = 2,
                name = "Soft Blue",
                primaryColorResId = R.color.blue_primary,
                secondaryColorResId = R.color.blue_secondary,
                tertiaryColorResId = R.color.blue_tertiary,
                accentColorResId = R.color.blue_accent,
                textColorResId = R.color.blue_text
            )
            3 -> ThemeItem(
                id = 3,
                name = "Peach",
                primaryColorResId = R.color.peach_primary,
                secondaryColorResId = R.color.peach_secondary,
                tertiaryColorResId = R.color.peach_tertiary,
                accentColorResId = R.color.peach_accent,
                textColorResId = R.color.peach_text
            )
            4 -> ThemeItem(
                id = 4,
                name = "Dark Mode",
                primaryColorResId = R.color.dark_primary,
                secondaryColorResId = R.color.dark_secondary,
                tertiaryColorResId = R.color.dark_tertiary,
                accentColorResId = R.color.dark_accent,
                textColorResId = R.color.dark_text
            )
            5 -> ThemeItem(
                id = 5,
                name = "White",
                primaryColorResId = R.color.white_primary,
                secondaryColorResId = R.color.white_secondary,
                tertiaryColorResId = R.color.white_tertiary,
                accentColorResId = R.color.white_accent,
                textColorResId = R.color.white_text
            )
            else -> ThemeItem(
                id = 0,
                name = "Lavender",
                primaryColorResId = R.color.lavender_primary,
                secondaryColorResId = R.color.lavender_secondary,
                tertiaryColorResId = R.color.lavender_tertiary,
                accentColorResId = R.color.lavender_accent,
                textColorResId = R.color.lavender_text
            )
        }
    }
    
    /**
     * Apply theme colors to an activity
     */
    private fun applyThemeColors(activity: Activity, theme: ThemeItem) {
        val context = activity.applicationContext
        val window = activity.window
        
        // Get colors from resources
        val primaryColor = ContextCompat.getColor(context, theme.primaryColorResId)
        val secondaryColor = ContextCompat.getColor(context, theme.secondaryColorResId)
        val accentColor = ContextCompat.getColor(context, theme.accentColorResId)
        val textColor = ContextCompat.getColor(context, theme.textColorResId)
        val tertiaryColor = ContextCompat.getColor(context, theme.tertiaryColorResId)
        
        // Apply colors to window
        applyWindowColors(window, accentColor, tertiaryColor)
        
        // Apply colors to action bar if present
        activity.actionBar?.setBackgroundDrawable(ColorDrawable(accentColor))
        activity.actionBar?.let { actionBar ->
            try {
                val titleId = activity.resources.getIdentifier("action_bar_title", "id", "android")
                val titleView = window.decorView.findViewById<TextView>(titleId)
                titleView?.setTextColor(if (isDarkColor(accentColor)) Color.WHITE else Color.BLACK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Apply colors to support action bar if present
        if (activity is androidx.appcompat.app.AppCompatActivity) {
            activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(accentColor))
            try {
                val titleId = activity.resources.getIdentifier("action_bar_title", "id", "android")
                val titleView = window.decorView.findViewById<TextView>(titleId)
                titleView?.setTextColor(if (isDarkColor(accentColor)) Color.WHITE else Color.BLACK)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Save theme in preferences
        saveSelectedTheme(context, theme.id)
    }
    
    /**
     * Apply colors to window (status bar, navigation bar, background)
     */
    private fun applyWindowColors(window: Window, accentColor: Int, backgroundColor: Int) {
        try {
            // Force update status bar color immediately
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            
            // Set status bar color
            window.statusBarColor = accentColor
            
            // Set status bar icons color based on background brightness
            if (isDarkColor(accentColor)) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or 
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            
            // Set navigation bar color
            window.navigationBarColor = accentColor
            
            // Set window background
            window.decorView.setBackgroundColor(backgroundColor)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Apply theme colors to a button
     */
    fun applyButtonTheme(button: Button, accentColor: Int) {
        button.backgroundTintList = ColorStateList.valueOf(accentColor)
        button.setTextColor(if (isDarkColor(accentColor)) Color.WHITE else Color.BLACK)
    }
    
    /**
     * Check if a color is dark (to determine text color)
     */
    fun isDarkColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
    
    /**
     * Save the selected theme ID in SharedPreferences
     */
    private fun saveSelectedTheme(context: Context, themeId: Int) {
        val sharedPreferences = context.getSharedPreferences("AppTheme", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("selected_theme", themeId).apply()
    }
    
    /**
     * Get the selected theme ID from SharedPreferences
     */
    fun getSelectedThemeId(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("AppTheme", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("selected_theme", 0) // Default to Lavender
    }
}
