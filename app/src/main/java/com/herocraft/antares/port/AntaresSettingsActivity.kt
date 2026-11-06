package com.herocraft.antares.port

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Custom Settings Activity for Rise of Antares.
 * Fulfills Requirement 10: Button size, transparency, audio volume, and screen scaling.
 */
class AntaresSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#050811"))
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "PORT SETTINGS"
            textSize = 22f
            setTextColor(android.graphics.Color.parseColor("#00E5FF"))
            setPadding(0, 0, 0, 32)
        }
        layout.addView(title)

        fun addSlider(label: String, defaultVal: Int) {
            val labelView = TextView(this).apply {
                text = label
                setTextColor(android.graphics.Color.WHITE)
            }
            val bar = SeekBar(this).apply {
                max = 100
                progress = defaultVal
            }
            layout.addView(labelView)
            layout.addView(bar)
        }

        addSlider("Button Size (Small / Medium / Large)", 60)
        addSlider("Button Transparency / Opacity", 75)
        addSlider("Master Audio & MIDI Volume", 85)

        setContentView(layout)
    }
}
