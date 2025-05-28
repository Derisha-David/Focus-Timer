package com.example.pomodoro

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    
    // Default timer durations in minutes
    private var pomodoroDuration = 25
    private var shortBreakDuration = 5
    private var longBreakDuration = 15
    
    // List of available themes
    private val themeList = listOf(
        ThemeItem(
            id = 1,
            name = "Lavender",
            primaryColorResId = R.color.lavender_primary,
            secondaryColorResId = R.color.lavender_secondary,
            tertiaryColorResId = R.color.lavender_tertiary,
            accentColorResId = R.color.lavender_accent,
            textColorResId = R.color.lavender_text
        ),
        ThemeItem(
            id = 2,
            name = "Mint Green",
            primaryColorResId = R.color.mint_primary,
            secondaryColorResId = R.color.mint_secondary,
            tertiaryColorResId = R.color.mint_tertiary,
            accentColorResId = R.color.mint_accent,
            textColorResId = R.color.mint_text
        ),
        ThemeItem(
            id = 3,
            name = "Soft Blue",
            primaryColorResId = R.color.blue_primary,
            secondaryColorResId = R.color.blue_secondary,
            tertiaryColorResId = R.color.blue_tertiary,
            accentColorResId = R.color.blue_accent,
            textColorResId = R.color.blue_text
        ),
        ThemeItem(
            id = 4,
            name = "Peach",
            primaryColorResId = R.color.peach_primary,
            secondaryColorResId = R.color.peach_secondary,
            tertiaryColorResId = R.color.peach_tertiary,
            accentColorResId = R.color.peach_accent,
            textColorResId = R.color.peach_text
        ),
        ThemeItem(
            id = 5,
            name = "Dark Mode",
            primaryColorResId = R.color.dark_primary,
            secondaryColorResId = R.color.dark_secondary,
            tertiaryColorResId = R.color.dark_tertiary,
            accentColorResId = R.color.dark_accent,
            textColorResId = R.color.dark_text
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppTheme", Context.MODE_PRIVATE)
        
        // Apply theme before setting content view
        val selectedThemeId = sharedPreferences.getInt("selected_theme", 0)
        setAppTheme(selectedThemeId)
        
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set navigation bar for gesture navigation
        setNavigationBarForGestures()
        
        // Ensure radio buttons reflect the current theme
        updateButtonsState(selectedThemeId)
        
        // Apply the saved theme
        applyTheme()

        updateTimerDisplays()
        setupEditButtons()
        setupStartButton()
        setupThemeButtons()
    }
    
    private fun updateTimerDisplays() {
        // Update the text displays with current values - showing only numbers
        binding.pomodoroTimeText.text = "$pomodoroDuration"
        binding.shortBreakTimeText.text = "$shortBreakDuration"
        binding.longBreakTimeText.text = "$longBreakDuration"
    }

    private fun setupEditButtons() {
        // Setup Pomodoro card click
        binding.pomodoroCard.setOnClickListener {
            showTimePickerDialog("Pomodoro Duration", 1, 300, pomodoroDuration) { newValue ->
                pomodoroDuration = newValue
                binding.pomodoroTimeText.text = "$pomodoroDuration"
            }
        }
        
        // Setup Short Break card click
        binding.shortBreakCard.setOnClickListener {
            showTimePickerDialog("Short Break Duration", 1, 60, shortBreakDuration) { newValue ->
                shortBreakDuration = newValue
                binding.shortBreakTimeText.text = "$shortBreakDuration"
            }
        }
        
        // Setup Long Break card click
        binding.longBreakCard.setOnClickListener {
            showTimePickerDialog("Long Break Duration", 1, 120, longBreakDuration) { newValue ->
                longBreakDuration = newValue
                binding.longBreakTimeText.text = "$longBreakDuration"
            }
        }
    }
    
    private fun showTimePickerDialog(title: String, minValue: Int, maxValue: Int, currentValue: Int, onValueSelected: (Int) -> Unit) {
        // Create a custom dialog with our circular minutes picker
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_circular_minutes)
        
        // Set dialog title
        val titleView = dialog.findViewById<TextView>(R.id.dialogTitle)
        titleView.text = title
        
        // Get references to views
        val circularSeekBar = dialog.findViewById<CircularSeekBar>(R.id.circularSeekBar)
        val selectedMinutesText = dialog.findViewById<TextView>(R.id.selectedMinutesText)
        
        // Configure the circular seekbar
        circularSeekBar.max = maxValue - minValue
        circularSeekBar.progress = currentValue - minValue
        selectedMinutesText.text = currentValue.toString()
        
        // Update the text when the circular seekbar changes
        circularSeekBar.onProgressChangeListener = { progress ->
            val minutes = progress + minValue
            selectedMinutesText.text = minutes.toString()
        }
        
        // Set up buttons
        val cancelButton = dialog.findViewById<Button>(R.id.cancelButton)
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        confirmButton.setOnClickListener {
            val selectedMinutes = circularSeekBar.progress + minValue
            onValueSelected(selectedMinutes)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun setupStartButton() {
        binding.startButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra(EXTRA_POMODORO_DURATION, pomodoroDuration)
                putExtra(EXTRA_SHORT_BREAK_DURATION, shortBreakDuration)
                putExtra(EXTRA_LONG_BREAK_DURATION, longBreakDuration)
                
                // Pass the selected theme to MainActivity
                val selectedThemePosition = sharedPreferences.getInt("selected_theme", 0)
                putExtra("selected_theme", selectedThemePosition)
            }
            startActivity(intent)
        }
    }
    
    private fun setupThemeButtons() {
        try {
            // Set up click listener for the theme selector card
            binding.themeSelector.setOnClickListener {
                showThemeSelectionDialog()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If button setup fails, we can still continue with the app
        }
    }
    
    private fun showThemeSelectionDialog() {
        try {
            // Create custom dialog with theme cards
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_theme_cards)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            
            // Get current theme
            val savedThemePosition = sharedPreferences.getInt("selected_theme", 0)
            
            // Get theme card views
            val lavenderCard = dialog.findViewById<CardView>(R.id.lavenderCard)
            val mintCard = dialog.findViewById<CardView>(R.id.mintCard)
            val blueCard = dialog.findViewById<CardView>(R.id.blueCard)
            val peachCard = dialog.findViewById<CardView>(R.id.peachCard)
            val darkCard = dialog.findViewById<CardView>(R.id.darkCard)
            val whiteCard = dialog.findViewById<CardView>(R.id.whiteCard)
            val closeButton = dialog.findViewById<Button>(R.id.closeButton)
            
            // Highlight the currently selected theme card
            val themeCards = arrayOf(lavenderCard, mintCard, blueCard, peachCard, darkCard, whiteCard)
            highlightSelectedCard(themeCards, savedThemePosition)
            
            // Set up click listeners for theme cards
            lavenderCard.setOnClickListener {
                updateSelectedTheme(0)
                setAppTheme(0)
                highlightSelectedCard(themeCards, 0)
                dialog.dismiss()
                recreate() // Recreate activity to apply new theme
            }
            
            mintCard.setOnClickListener {
                updateSelectedTheme(1)
                setAppTheme(1)
                highlightSelectedCard(themeCards, 1)
                dialog.dismiss()
                recreate() // Recreate activity to apply new theme
            }
            
            blueCard.setOnClickListener {
                updateSelectedTheme(2)
                setAppTheme(2)
                highlightSelectedCard(themeCards, 2)
                dialog.dismiss()
                recreate() // Recreate activity to apply new theme
            }
            
            peachCard.setOnClickListener {
                updateSelectedTheme(3)
                setAppTheme(3)
                highlightSelectedCard(themeCards, 3)
                dialog.dismiss()
                recreate() // Recreate activity to apply new theme
            }
            
            darkCard.setOnClickListener {
                updateSelectedTheme(4)
                setAppTheme(4)
                highlightSelectedCard(themeCards, 4)
                dialog.dismiss()
                recreate() // Recreate activity to apply new theme
            }
            
            whiteCard.setOnClickListener {
                updateSelectedTheme(5)
                setAppTheme(5)
                highlightSelectedCard(themeCards, 5)
                dialog.dismiss()
                recreate() // Recreate activity to apply new theme
            }
            
            // Set up close button
            closeButton.setOnClickListener {
                dialog.dismiss()
            }
            
            // Show dialog
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
            // If dialog creation fails, we can still continue with the app
        }
    }
    
    private fun highlightSelectedCard(cards: Array<CardView>, selectedIndex: Int) {
        // Reset all cards
        for (i in cards.indices) {
            cards[i].cardElevation = 4f // Default elevation
        }
        
        // Highlight selected card
        if (selectedIndex in cards.indices) {
            cards[selectedIndex].cardElevation = 12f // Higher elevation for selected card
        }
    }
    
    private fun setAppTheme(themeId: Int) {
        when (themeId) {
            0 -> setTheme(R.style.AppTheme_Lavender)
            1 -> setTheme(R.style.AppTheme_MintGreen)
            2 -> setTheme(R.style.AppTheme_SoftBlue)
            3 -> setTheme(R.style.AppTheme_Peach)
            4 -> setTheme(R.style.AppTheme_DarkMode)
            5 -> setTheme(R.style.AppTheme_White)
            else -> setTheme(R.style.AppTheme_Lavender)
        }
    }
    
    private fun updateSelectedTheme(position: Int) {
        try {
            // Save the selected theme position
            sharedPreferences.edit().putInt("selected_theme", position).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateButtonsState(position: Int) {
        try {
            // Update radio buttons
//            binding.lavenderRadio.isChecked = position == 0
//            binding.mintRadio.isChecked = position == 1
//            binding.blueRadio.isChecked = position == 2
//            binding.peachRadio.isChecked = position == 3
//            binding.darkRadio.isChecked = position == 4
            
            // Update Focus button with theme accent color
            val theme = ThemeManager.getThemeById(this, position)
            val accentColor = ContextCompat.getColor(this, theme.accentColorResId)
            binding.startButton.backgroundTintList = ColorStateList.valueOf(accentColor)
            binding.startButton.setTextColor(if (ThemeManager.isDarkColor(accentColor)) Color.WHITE else Color.BLACK)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun applyTheme() {
        val selectedThemePosition = sharedPreferences.getInt("selected_theme", 0)
        if (selectedThemePosition < themeList.size) {
            applyTheme(themeList[selectedThemePosition])
        }
    }
    
    private fun applyTheme(theme: ThemeItem) {
        try {
            // Apply theme colors to the UI
            val primaryColor = getColor(theme.primaryColorResId)
            val secondaryColor = getColor(theme.secondaryColorResId)
            val accentColor = getColor(theme.accentColorResId)
            val textColor = getColor(theme.textColorResId)
            val tertiaryColor = getColor(theme.tertiaryColorResId)
            
            // Update status bar color
            window.statusBarColor = accentColor
            
            // Set light/dark status bar icons based on theme brightness
            if (isDarkColor(accentColor)) {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or 
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            
            // Update action bar if present
            supportActionBar?.let { actionBar ->
                actionBar.setBackgroundDrawable(ColorDrawable(accentColor))
                
                // Try to update title text color
                try {
                    val titleId = resources.getIdentifier("action_bar_title", "id", "android")
                    val titleView = window.decorView.findViewById<TextView>(titleId)
                    titleView?.setTextColor(if (isDarkColor(accentColor)) Color.WHITE else Color.BLACK)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Update background color
            window.decorView.setBackgroundColor(tertiaryColor)
            
            // Update card backgrounds
            binding.timersCardView.setCardBackgroundColor(primaryColor)
            binding.themeCardView.setCardBackgroundColor(primaryColor)
            
            // Update timer cards
            binding.pomodoroCard.setCardBackgroundColor(secondaryColor)
            binding.shortBreakCard.setCardBackgroundColor(secondaryColor)
            binding.longBreakCard.setCardBackgroundColor(secondaryColor)
            
            // Update button
            binding.startButton.backgroundTintList = ColorStateList.valueOf(accentColor)
            binding.startButton.setTextColor(if (isDarkColor(accentColor)) Color.WHITE else Color.BLACK)
            
            // Update theme selection text colors
            updateThemeSelectionColors(textColor, primaryColor)
            
            // Save theme colors to be used by other activities
            saveThemeColors(theme)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun saveThemeColors(theme: ThemeItem) {
        sharedPreferences.edit().apply {
            putInt("theme_primary_color", theme.primaryColorResId)
            putInt("theme_secondary_color", theme.secondaryColorResId)
            putInt("theme_tertiary_color", theme.tertiaryColorResId)
            putInt("theme_accent_color", theme.accentColorResId)
            putInt("theme_text_color", theme.textColorResId)
            apply()
        }
    }
    
    private fun updateThemeSelectionColors(textColor: Int, backgroundColor: Int) {
        try {
            // Find all text views in the theme card and update their colors
            val textViews = findTextViewsInViewGroup(binding.themeCardView)
            for (textView in textViews) {
                textView.setTextColor(textColor)
            }
            
            // Update radio button colors
//            binding.lavenderRadio.buttonTintList = ColorStateList.valueOf(textColor)
//            binding.mintRadio.buttonTintList = ColorStateList.valueOf(textColor)
//            binding.blueRadio.buttonTintList = ColorStateList.valueOf(textColor)
//            binding.peachRadio.buttonTintList = ColorStateList.valueOf(textColor)
//            binding.darkRadio.buttonTintList = ColorStateList.valueOf(textColor)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun findTextViewsInViewGroup(viewGroup: ViewGroup): List<TextView> {
        val result = mutableListOf<TextView>()
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is TextView) {
                result.add(child)
            } else if (child is ViewGroup) {
                result.addAll(findTextViewsInViewGroup(child))
            }
        }
        return result
    }
    
    private fun isDarkColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
    
    private fun setNavigationBarForGestures() {
        try {
            // Get the selected theme
            val selectedThemeId = sharedPreferences.getInt("selected_theme", 0)
            val themeAccentColor = when (selectedThemeId) {
                0 -> ContextCompat.getColor(this, R.color.lavender_accent)
                1 -> ContextCompat.getColor(this, R.color.mint_accent)
                2 -> ContextCompat.getColor(this, R.color.blue_accent)
                3 -> ContextCompat.getColor(this, R.color.peach_accent)
                4 -> ContextCompat.getColor(this, R.color.dark_accent)
                else -> ContextCompat.getColor(this, R.color.lavender_accent)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above, we can detect navigation mode
                val resources = resources
                val resourceId = resources.getIdentifier(
                    "config_navBarInteractionMode", "integer", "android")
                
                if (resourceId > 0) {
                    val interactionMode = resources.getInteger(resourceId)
                    
                    // Gesture mode is typically mode 2
                    val isGestureMode = interactionMode == 2
                    
                    if (isGestureMode) {
                        // For gesture navigation, use white
                        window.navigationBarColor = Color.WHITE
                        
                        // Make navigation bar icons dark for better visibility on white
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            var flags = window.decorView.systemUiVisibility
                            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            window.decorView.systemUiVisibility = flags
                        }
                    } else {
                        // For button navigation, use theme color
                        window.navigationBarColor = themeAccentColor
                        
                        // Set navigation bar icons color based on theme brightness
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            var flags = window.decorView.systemUiVisibility
                            if (isDarkColor(themeAccentColor)) {
                                // For dark themes, use light icons
                                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                            } else {
                                // For light themes, use dark icons
                                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            }
                            window.decorView.systemUiVisibility = flags
                        }
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For older versions, just use theme color (can't detect gesture mode)
                window.navigationBarColor = themeAccentColor
                
                // Set navigation bar icons color based on theme brightness
                var flags = window.decorView.systemUiVisibility
                if (isDarkColor(themeAccentColor)) {
                    // For dark themes, use light icons
                    flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                } else {
                    // For light themes, use dark icons
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
                window.decorView.systemUiVisibility = flags
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If detection fails, fall back to theme color
            applyTheme()
        }
    }

    companion object {
        const val EXTRA_POMODORO_DURATION = "com.example.pomodoro.POMODORO_DURATION"
        const val EXTRA_SHORT_BREAK_DURATION = "com.example.pomodoro.SHORT_BREAK_DURATION"
        const val EXTRA_LONG_BREAK_DURATION = "com.example.pomodoro.LONG_BREAK_DURATION"
    }
}
