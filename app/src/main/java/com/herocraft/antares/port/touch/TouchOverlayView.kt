package com.herocraft.antares.port.touch

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout

/**
 * Dedicated Touchscreen Controller for Rise of Antares.
 * Implements the exact key mapping:
 * - Up: 50
 * - Down: 56
 * - Left: 52
 * - Right: 54
 * - Confirm / Fire: 53
 * - Left soft key: -6
 * - Right soft key: -7
 * - Shortcuts: 49 (Action 1), 51 (Fleet 3), 55 (Science 7), 57 (Design 9), 35 (End Turn #)
 */
class TouchOverlayView(
    context: Context,
    private val onKeyEvent: (keyCode: Int, isPressed: Boolean) -> Unit
) : FrameLayout(context) {

    init {
        // Bottom container for controls
        val bottomContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#B3050811"))
            setPadding(16, 12, 16, 24)
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            )
        }

        // Row 1: Contextual Action Chips (End Turn, Fleet, Science, Design)
        val chipsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 12) }
        }

        fun createChip(label: String, keyCode: Int, isAccent: Boolean = false): Button {
            return Button(context).apply {
                text = label
                textSize = 10f
                setTextColor(if (isAccent) Color.WHITE else Color.parseColor("#00E5FF"))
                setBackgroundColor(if (isAccent) Color.parseColor("#B45309") else Color.parseColor("#1E293B"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(4, 0, 4, 0)
                }
                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> { onKeyEvent(keyCode, true); true }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { onKeyEvent(keyCode, false); true }
                        else -> false
                    }
                }
            }
        }

        chipsRow.addView(createChip("END TURN (#)", 35, true))
        chipsRow.addView(createChip("FLEET (3)", 51))
        chipsRow.addView(createChip("SCIENCE (7)", 55))
        chipsRow.addView(createChip("DESIGN (9)", 57))
        bottomContainer.addView(chipsRow)

        // Row 2: Navigation arrows + Confirm (53) + Back (-7) + Menu (-6)
        val navRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        fun createSquareBtn(label: String, keyCode: Int, color: Int = Color.parseColor("#1E293B")): Button {
            return Button(context).apply {
                text = label
                textSize = 13f
                setTextColor(Color.WHITE)
                setBackgroundColor(color)
                layoutParams = LinearLayout.LayoutParams(110, 110).apply {
                    setMargins(6, 4, 6, 4)
                }
                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> { onKeyEvent(keyCode, true); true }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { onKeyEvent(keyCode, false); true }
                        else -> false
                    }
                }
            }
        }

        // Left Soft Key (-6)
        val btnLsk = Button(context).apply {
            text = "MENU\n(LSK)"
            textSize = 10f
            setTextColor(Color.parseColor("#94A3B8"))
            setBackgroundColor(Color.parseColor("#0F172A"))
            layoutParams = LinearLayout.LayoutParams(120, 120).apply { setMargins(0, 0, 16, 0) }
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> { onKeyEvent(-6, true); true }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { onKeyEvent(-6, false); true }
                    else -> false
                }
            }
        }
        navRow.addView(btnLsk)

        // D-Pad Arrows Cluster (50 = Up, 56 = Down, 52 = Left, 54 = Right)
        val dpadCluster = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        dpadCluster.addView(createSquareBtn("◀", 52)) // Left: 52
        
        val upDownCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        upDownCol.addView(createSquareBtn("▲", 50)) // Up: 50
        upDownCol.addView(createSquareBtn("▼", 56)) // Down: 56
        dpadCluster.addView(upDownCol)

        dpadCluster.addView(createSquareBtn("▶", 54)) // Right: 54
        navRow.addView(dpadCluster)

        // Action Column: Confirm (53) + Back (-7)
        val actionCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(130, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(16, 0, 0, 0)
            }
        }

        val btnConfirm = Button(context).apply {
            text = "OK (5)"
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#0284C7"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80).apply {
                setMargins(0, 0, 0, 8)
            }
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> { onKeyEvent(53, true); true }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { onKeyEvent(53, false); true }
                    else -> false
                }
            }
        }

        val btnBack = Button(context).apply {
            text = "BACK (RSK)"
            textSize = 9f
            setTextColor(Color.parseColor("#EF4444"))
            setBackgroundColor(Color.parseColor("#1E293B"))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 65)
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> { onKeyEvent(-7, true); true }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { onKeyEvent(-7, false); true }
                    else -> false
                }
            }
        }

        actionCol.addView(btnConfirm)
        actionCol.addView(btnBack)
        navRow.addView(actionCol)

        bottomContainer.addView(navRow)
        addView(bottomContainer)
    }
}
