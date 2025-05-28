package com.example.pomodoro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paints
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.WHITE
    }

    // Revolution colors (from lightest to darker)
    private val revolutionPaints = listOf(
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 20f
            color = Color.parseColor("#E9E0FF") // Extremely light lavender - 1st revolution (1-60)
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 20f
            color = Color.parseColor("#DFD2FF") // Very light lavender - 2nd revolution (61-120)
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 20f
            color = Color.parseColor("#D4C4FF") // Light lavender - 3rd revolution (121-180)
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 20f
            color = Color.parseColor("#C9B6FF") // Medium lavender - 4th revolution (181-240)
        },
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 20f
            color = Color.parseColor("#B69CFF") // Original lavender accent - 5th revolution (241-300)
        }
    )

    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#B69CFF") // Default thumb color (will be updated based on revolution)
    }

    // Dimensions
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private val thumbRadius = 15f
    private val rect = RectF()

    // Progress
    var max = 300
        set(value) {
            field = value
            invalidate()
        }

    var progress = 25
        set(value) {
            val newValue = value.coerceIn(0, max)
            if (field != newValue) {
                field = newValue
                onProgressChangeListener?.invoke(field)
                invalidate()
            }
        }

    // Visual representation settings
    private val minutesPerRevolution = 60 // Each revolution represents 60 minutes

    // Calculate the number of revolutions needed based on max value
    private val revolutionsForMax: Int
        get() = (max + minutesPerRevolution - 1) / minutesPerRevolution // Ceiling division

    // Listener
    var onProgressChangeListener: ((Int) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f - 30f // Leave space for the thumb

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
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // Calculate angles for drawing
        val degreesPerUnit = 360f / minutesPerRevolution // Each minute is x degrees
        val sweepAngle = degreesPerUnit * progress

        // Determine which revolution we're in (0-based index)
        val currentRevolution = (progress / minutesPerRevolution).coerceAtMost(revolutionsForMax - 1)
        val remainingSweep = (progress % minutesPerRevolution) * degreesPerUnit

        // Draw completed revolutions
        for (i in 0 until currentRevolution) {
            // Make sure we don't go out of bounds with our paint array
            val paintIndex = i.coerceAtMost(revolutionPaints.size - 1)
            canvas.drawCircle(centerX, centerY, radius, revolutionPaints[paintIndex])
        }

        // Draw current revolution progress arc
        val currentPaintIndex = currentRevolution.coerceAtMost(revolutionPaints.size - 1)
        canvas.drawArc(rect, -90f, remainingSweep, false, revolutionPaints[currentPaintIndex])

        // Draw thumb
        val angle = Math.toRadians((-90 + (progress % minutesPerRevolution) * degreesPerUnit).toDouble())
        val thumbX = centerX + radius * cos(angle).toFloat()
        val thumbY = centerY + radius * sin(angle).toFloat()

        // Always use the original lavender accent color for the thumb
        thumbPaint.color = Color.parseColor("#B69CFF") // Original lavender accent
        canvas.drawCircle(thumbX, thumbY, thumbRadius, thumbPaint)
    }

    // Track previous angle for handling multiple revolutions
    private var previousAngleDegrees = 0.0
    private var accumulatedAngleDegrees = 0.0
    private var isFirstTouch = true
    private var currentRevolutionCount = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if touch is near the circle
                val touchRadius = Math.sqrt(Math.pow((x - centerX).toDouble(), 2.0) + Math.pow((y - centerY).toDouble(), 2.0))
                if (Math.abs(touchRadius - radius) > 50) {
                    return false // Ignore touches too far from the circle
                }

                // Calculate the angle from the center to the touch point
                val angle = atan2((y - centerY).toDouble(), (x - centerX).toDouble())

                // Convert angle to degrees and adjust to start from the top (0 degrees)
                var degrees = Math.toDegrees(angle) + 90
                if (degrees < 0) degrees += 360

                previousAngleDegrees = degrees

                // On first touch, set accumulated angle and revolution count based on current progress
                if (isFirstTouch) {
                    currentRevolutionCount = progress / minutesPerRevolution
                    val remainingProgress = progress % minutesPerRevolution
                    accumulatedAngleDegrees = remainingProgress * (360.0 / minutesPerRevolution)
                    isFirstTouch = false
                }

                return true
            }

            MotionEvent.ACTION_MOVE -> {
                // Calculate the angle from the center to the touch point
                val angle = atan2((y - centerY).toDouble(), (x - centerX).toDouble())

                // Convert angle to degrees and adjust to start from the top (0 degrees)
                var degrees = Math.toDegrees(angle) + 90
                if (degrees < 0) degrees += 360

                // Calculate the delta angle, handling the 0/360 boundary
                var deltaDegrees = degrees - previousAngleDegrees
                if (deltaDegrees > 180) deltaDegrees -= 360
                if (deltaDegrees < -180) deltaDegrees += 360

                // Update accumulated angle
                accumulatedAngleDegrees += deltaDegrees

                // Handle revolution changes
                if (accumulatedAngleDegrees >= 360) {
                    currentRevolutionCount++
                    accumulatedAngleDegrees -= 360
                } else if (accumulatedAngleDegrees < 0) {
                    if (currentRevolutionCount > 0) {
                        currentRevolutionCount--
                        accumulatedAngleDegrees += 360
                    } else {
                        accumulatedAngleDegrees = 0.0
                    }
                }

                // Limit to max revolutions
                if (currentRevolutionCount >= revolutionsForMax) {
                    currentRevolutionCount = revolutionsForMax - 1
                    accumulatedAngleDegrees = 360.0
                }

                // Calculate progress based on the revolution count and accumulated angle
                val progressInCurrentRevolution = (accumulatedAngleDegrees / 360.0 * minutesPerRevolution).toInt()
                progress = (currentRevolutionCount * minutesPerRevolution) + progressInCurrentRevolution

                previousAngleDegrees = degrees
                return true
            }

            MotionEvent.ACTION_UP -> {
                isFirstTouch = true
            }
        }
        return super.onTouchEvent(event)
    }
}
