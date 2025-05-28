package com.example.pomodoro

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var timeLeftInMillis: Long = 0
    private var completedPomodoros = 0

    // Timer durations in milliseconds
    private var pomodoroDuration: Long = 25 * 60 * 1000 // Default: 25 minutes
    private var shortBreakDuration: Long = 5 * 60 * 1000 // Default: 5 minutes
    private var longBreakDuration: Long = 15 * 60 * 1000 // Default: 15 minutes

    // Current timer mode
    private var currentMode = TimerMode.POMODORO

    // Theme list
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
        try {
            super.onCreate(savedInstanceState)
            
            // Apply theme before setting content view
            val sharedPreferences = getSharedPreferences("AppTheme", Context.MODE_PRIVATE)
            val selectedThemeId = sharedPreferences.getInt("selected_theme", 0)
            
            // Set the theme based on saved preference
            when (selectedThemeId) {
                0 -> setTheme(R.style.AppTheme_Lavender)
                1 -> setTheme(R.style.AppTheme_MintGreen)
                2 -> setTheme(R.style.AppTheme_SoftBlue)
                3 -> setTheme(R.style.AppTheme_Peach)
                4 -> setTheme(R.style.AppTheme_DarkMode)
                else -> setTheme(R.style.AppTheme_Lavender)
            }
            
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // Set navigation bar for gesture navigation
            setNavigationBarForGestures()
            
            try {
                // Initialize SharedPreferences - already initialized in the theme code above
                // Just hide the action bar for a more immersive experience
                supportActionBar?.hide()
            } catch (e: Exception) {
                e.printStackTrace()
                // Continue even if action bar fails
            }
            
            try {
                // Get timer durations from intent with safe defaults
                val pomodoroMinutes = intent?.getIntExtra(HomeActivity.EXTRA_POMODORO_DURATION, 25) ?: 25
                val shortBreakMinutes = intent?.getIntExtra(HomeActivity.EXTRA_SHORT_BREAK_DURATION, 5) ?: 5
                val longBreakMinutes = intent?.getIntExtra(HomeActivity.EXTRA_LONG_BREAK_DURATION, 15) ?: 15
                
                // Convert minutes to milliseconds (Long)
                pomodoroDuration = pomodoroMinutes.toLong() * 60L * 1000L
                shortBreakDuration = shortBreakMinutes.toLong() * 60L * 1000L
                longBreakDuration = longBreakMinutes.toLong() * 60L * 1000L
            } catch (e: Exception) {
                e.printStackTrace()
                // Use default values if intent extras fail
                pomodoroDuration = 25 * 60 * 1000L
                shortBreakDuration = 5 * 60 * 1000L
                longBreakDuration = 15 * 60 * 1000L
            }
            
            try {
                // Apply the selected theme
                val selectedThemePosition = ThemeManager.getSelectedThemeId(this)
                ThemeManager.applyTheme(this, selectedThemePosition)
            } catch (e: Exception) {
                e.printStackTrace()
                // Continue with default theme if theme application fails
            }
            
            // Set up button click listeners
            setupButtonListeners()
            
            // Set initial timer mode to Pomodoro
            setTimerMode(TimerMode.POMODORO)
            
            // Automatically start the timer when the activity is created
            startTimer()
        } catch (e: Exception) {
            e.printStackTrace()
            // Last resort error handling
            try {
                // Try to show a simple layout if everything else fails
                setContentView(R.layout.activity_main)
            } catch (e2: Exception) {
                e2.printStackTrace()
                // Nothing more we can do
                finish()
            }
        }
    }

    private fun setupButtonListeners() {
        // Reset button
        binding.resetButton.setOnClickListener {
            resetTimer()
            // Automatically restart timer after reset
            startTimer()
        }
    }

    private fun setTimerMode(mode: TimerMode) {
        currentMode = mode
        resetTimer()
        
        when (mode) {
            TimerMode.POMODORO -> {
                timeLeftInMillis = pomodoroDuration
                binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.timerContainer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.lavender_primary))
                binding.circularProgressView.setProgressColorResource(R.color.lavender_tertiary)
                binding.circularProgressView.setCircleBackgroundColorResource(R.color.
                )
                binding.circularProgressView.setSectorColor(Color.WHITE)
                updateButtonColors(R.color.lavender_accent)
            }
            TimerMode.SHORT_BREAK -> {
                timeLeftInMillis = shortBreakDuration
                binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.timerContainer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.lavender_primary))
                binding.circularProgressView.setProgressColorResource(R.color.lavender_primary)
                binding.circularProgressView.setCircleBackgroundColorResource(R.color.white)
                binding.circularProgressView.setSectorColor(Color.WHITE)
                updateButtonColors(R.color.lavender_secondary)
            }
            TimerMode.LONG_BREAK -> {
                timeLeftInMillis = longBreakDuration
                binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                binding.timerContainer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.lavender_primary))
                binding.circularProgressView.setProgressColorResource(R.color.lavender_primary)
                binding.circularProgressView.setCircleBackgroundColorResource(R.color.white)
                binding.circularProgressView.setSectorColor(Color.WHITE)
                updateButtonColors(R.color.lavender_accent)
            }
        }
        
        updateTimerText()
    }
    
    private fun updateButtonColors(colorResId: Int) {
        // Keep reset button text white
        binding.resetButton.setTextColor(Color.WHITE)
    }
    
    private fun applyTheme(theme: ThemeItem) {
        try {
            // Apply theme colors to the UI
            val primaryColor = ContextCompat.getColor(this, theme.primaryColorResId)
            val secondaryColor = ContextCompat.getColor(this, theme.secondaryColorResId)
            val accentColor = ContextCompat.getColor(this, theme.accentColorResId)
            val textColor = ContextCompat.getColor(this, theme.textColorResId)
            val tertiaryColor = ContextCompat.getColor(this, theme.tertiaryColorResId)
            
            // Update status bar color
            window.statusBarColor = accentColor
            
            // Set light/dark status bar icons based on theme brightness
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android 11+ use WindowInsetsController
                val windowInsetsController = window.insetsController
                if (isDarkColor(accentColor)) {
                    // Dark background, light icons
                    windowInsetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
                } else {
                    // Light background, dark icons
                    windowInsetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            } else {
                // For Android 10 and below, use the deprecated method as fallback
                @Suppress("DEPRECATION")
                if (isDarkColor(accentColor)) {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                } else {
                    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or 
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
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
            
            // Update CardView background
            binding.timerContainer.setCardBackgroundColor(primaryColor)
            
            // Update reset button
            binding.resetButton.backgroundTintList = ColorStateList.valueOf(secondaryColor)
            binding.resetButton.setTextColor(textColor)
            
            // Update progress colors
            binding.circularProgressView.setCircleBackgroundColor(primaryColor)
            binding.circularProgressView.setProgressColor(accentColor)
            
            // Update timer text and label colors
            binding.timerTextView.setTextColor(textColor)
            binding.timerLabelTextView.setTextColor(textColor)
        } catch (e: Exception) {
            // Fallback to default colors if there's an error
            e.printStackTrace()
        }
    }
    
    private fun isDarkColor(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
    
    private fun setNavigationBarForGestures() {
        try {
            // Get the selected theme
            val sharedPrefs = getSharedPreferences("AppTheme", Context.MODE_PRIVATE)
            val selectedThemeId = sharedPrefs.getInt("selected_theme", 0)
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            // For Android 11+ use WindowInsetsController
                            window.insetsController?.setSystemBarsAppearance(
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                            )
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // For Android 8.0-10, use the deprecated method
                            @Suppress("DEPRECATION")
                            var flags = window.decorView.systemUiVisibility
                            @Suppress("DEPRECATION")
                            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            @Suppress("DEPRECATION")
                            window.decorView.systemUiVisibility = flags
                        }
                    } else {
                        // For button navigation, use theme color
                        window.navigationBarColor = themeAccentColor
                        
                        // Set navigation bar icons color based on theme brightness
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            // For Android 11+ use WindowInsetsController
                            if (isDarkColor(themeAccentColor)) {
                                // For dark themes, use light icons
                                window.insetsController?.setSystemBarsAppearance(
                                    0,
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                                )
                            } else {
                                // For light themes, use dark icons
                                window.insetsController?.setSystemBarsAppearance(
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                                )
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            // For Android 8.0-10, use the deprecated method
                            @Suppress("DEPRECATION")
                            var flags = window.decorView.systemUiVisibility
                            if (isDarkColor(themeAccentColor)) {
                                // For dark themes, use light icons
                                @Suppress("DEPRECATION")
                                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                            } else {
                                // For light themes, use dark icons
                                @Suppress("DEPRECATION")
                                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            }
                            @Suppress("DEPRECATION")
                            window.decorView.systemUiVisibility = flags
                        }
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For older versions, just use theme color (can't detect gesture mode)
                window.navigationBarColor = themeAccentColor
                
                // Set navigation bar icons color based on theme brightness
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // For Android 11+ use WindowInsetsController
                    if (isDarkColor(themeAccentColor)) {
                        // For dark themes, use light icons
                        window.insetsController?.setSystemBarsAppearance(
                            0,
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                    } else {
                        // For light themes, use dark icons
                        window.insetsController?.setSystemBarsAppearance(
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                        )
                    }
                } else {
                    // For Android 8.0-10, use the deprecated method
                    @Suppress("DEPRECATION")
                    var flags = window.decorView.systemUiVisibility
                    if (isDarkColor(themeAccentColor)) {
                        // For dark themes, use light icons
                        @Suppress("DEPRECATION")
                        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                    } else {
                        // For light themes, use dark icons
                        @Suppress("DEPRECATION")
                        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    }
                    @Suppress("DEPRECATION")
                    window.decorView.systemUiVisibility = flags
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If detection fails, just continue without changing navigation bar
        }
    }

    private fun startTimer() {
        // Start the countdown timer
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isTimerRunning = false
                
                // Play notification sound
                val mediaPlayer = MediaPlayer.create(this@MainActivity, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                
                // If pomodoro is completed, increment counter (kept internally)
                if (currentMode == TimerMode.POMODORO) {
                    completedPomodoros++
                    
                    // Automatically switch to break after pomodoro
                    if (completedPomodoros % 4 == 0) {
                        // After every 4 pomodoros, take a long break
                        setTimerMode(TimerMode.LONG_BREAK)
                    } else {
                        // Otherwise take a short break
                        setTimerMode(TimerMode.SHORT_BREAK)
                    }
                } else {
                    // After break, switch back to pomodoro
                    setTimerMode(TimerMode.POMODORO)
                }
                
                // Automatically start the next timer
                startTimer()
            }
        }.start()

        isTimerRunning = true
    }

    private fun pauseTimer() {
        timer?.cancel()
        isTimerRunning = false
    }

    private fun resetTimer() {
        timer?.cancel()
        isTimerRunning = false
        
        when (currentMode) {
            TimerMode.POMODORO -> timeLeftInMillis = pomodoroDuration
            TimerMode.SHORT_BREAK -> timeLeftInMillis = shortBreakDuration
            TimerMode.LONG_BREAK -> timeLeftInMillis = longBreakDuration
        }
        
        updateTimerText()
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)
        binding.timerTextView.text = timeFormatted
        
        // Update the timer label based on current mode
        binding.timerLabelTextView.text = when (currentMode) {
            TimerMode.POMODORO -> "FOCUS"
            TimerMode.SHORT_BREAK -> "SHORT BREAK"
            TimerMode.LONG_BREAK -> "LONG BREAK"
        }
        
        // Update circular progress
        val totalDuration = when (currentMode) {
            TimerMode.POMODORO -> pomodoroDuration
            TimerMode.SHORT_BREAK -> shortBreakDuration
            TimerMode.LONG_BREAK -> longBreakDuration
        }
        val progress = 1f - (timeLeftInMillis.toFloat() / totalDuration.toFloat())
        binding.circularProgressView.progress = progress
    }

    override fun onStop() {
        super.onStop()
        pauseTimer()
    }

    enum class TimerMode {
        POMODORO, SHORT_BREAK, LONG_BREAK
    }
}
