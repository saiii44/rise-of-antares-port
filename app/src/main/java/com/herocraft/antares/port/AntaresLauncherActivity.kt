package com.herocraft.antares.port

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Dedicated Rise of Antares Launcher.
 * This is NOT a generic multi-game browser.
 * It immediately and exclusively launches Rise of Antares.
 */
class AntaresLauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup dedicated splash launcher
        val view = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(android.graphics.Color.parseColor("#050811"))
            setPadding(48, 48, 48, 48)
        }

        val title = android.widget.TextView(this).apply {
            text = "RISE OF ANTARES"
            textSize = 28f
            setTextColor(android.graphics.Color.parseColor("#00E5FF"))
            gravity = android.view.Gravity.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
        }

        val subtitle = android.widget.TextView(this).apply {
            text = "Dedicated Android Port • 4X Space Strategy"
            textSize = 14f
            setTextColor(android.graphics.Color.parseColor("#94A3B8"))
            gravity = android.view.Gravity.CENTER
            setPadding(0, 8, 0, 48)
        }

        val btnPlay = Button(this).apply {
            text = "START CAMPAIGN"
            setBackgroundColor(android.graphics.Color.parseColor("#0284C7"))
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                startActivity(Intent(this@AntaresLauncherActivity, AntaresGameActivity::class.java))
            }
        }

        val btnSettings = Button(this).apply {
            text = "PORT SETTINGS (TOUCH / AUDIO)"
            setBackgroundColor(android.graphics.Color.parseColor("#1E293B"))
            setTextColor(android.graphics.Color.parseColor("#00E5FF"))
            setOnClickListener {
                startActivity(Intent(this@AntaresLauncherActivity, AntaresSettingsActivity::class.java))
            }
        }

        view.addView(title)
        view.addView(subtitle)
        view.addView(btnPlay)
        view.addView(btnSettings)

        setContentView(view)
    }
}
