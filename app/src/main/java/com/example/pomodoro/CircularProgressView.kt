package com.example.pomodoro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.min

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Background circle paint
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.WHITE
        alpha = 100 // Semi-transparent to ensure visibility
    }

    // Progress arc paint
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.WHITE // White color
        alpha = 255 // Fully opaque
    }
    
    // Sector paint for filled area
    private val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE // Default white color
    }

    // Dimensions
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private val rect = RectF()

    // Progress
    var progress = 0.25f // 0f to 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }
        
    // Flag to control sector fill visibility
    var showSectorFill = true

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = (min(w, h) / 2f) - 20f // Adjust for stroke width

        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Calculate sweep angle based on progress
        val sweepAngle = 360f * progress
        
        // Draw the sector fill if enabled
        if (showSectorFill) {
            // Create a smaller rect for the sector to avoid overlapping with the stroke
            val sectorRect = RectF(
                centerX - radius + backgroundPaint.strokeWidth/2,
                centerY - radius + backgroundPaint.strokeWidth/2,
                centerX + radius - backgroundPaint.strokeWidth/2,
                centerY + radius - backgroundPaint.strokeWidth/2
            )
            canvas.drawArc(sectorRect, -90f, sweepAngle, true, sectorPaint)
        }
        
        // Draw the progress arc
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)
    }

    fun setProgressColor(color: Int) {
        progressPaint.color = color
        invalidate()
    }

    fun setCircleBackgroundColor(color: Int) {
        backgroundPaint.color = color
        invalidate()
    }
    
    // New method to set the sector fill color
    fun setSectorColor(color: Int) {
        sectorPaint.color = color
        invalidate()
    }
    
    // Overloaded methods that accept resource IDs
    fun setProgressColorResource(colorResId: Int) {
        setProgressColor(ContextCompat.getColor(context, colorResId))
    }

    fun setCircleBackgroundColorResource(colorResId: Int) {
        setCircleBackgroundColor(ContextCompat.getColor(context, colorResId))
    }
    
    fun setSectorColorResource(colorResId: Int) {
        setSectorColor(ContextCompat.getColor(context, colorResId))
    }
}
