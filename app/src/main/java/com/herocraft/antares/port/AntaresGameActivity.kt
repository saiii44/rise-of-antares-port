package com.herocraft.antares.port

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.herocraft.antares.port.runtime.JarRuntimeLoader
import com.herocraft.antares.port.touch.TouchOverlayView
import javax.microedition.lcdui.Canvas
import javax.microedition.lcdui.Graphics
import kotlin.concurrent.thread

class AntaresGameActivity : AppCompatActivity() {

    private var gameSurfaceView: AntaresGameSurfaceView? = null
    private var touchOverlay: TouchOverlayView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set portrait native resolution 240x320
        Canvas.screenWidth = 240
        Canvas.screenHeight = 320

        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        )

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        // Hardware-accelerated Game Surface View (240x320 native resolution)
        gameSurfaceView = AntaresGameSurfaceView(this)
        root.addView(gameSurfaceView)

        // Dedicated Touchscreen Overlay
        touchOverlay = TouchOverlayView(this) { keyCode, isPressed ->
            dispatchKey(keyCode, isPressed)
        }
        root.addView(touchOverlay)

        setContentView(root)

        // Boot the original game MIDlet in a background thread
        thread(start = true, name = "AntaresMainLoop") {
            try {
                JarRuntimeLoader.loadAndStart(this@AntaresGameActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun dispatchKey(keyCode: Int, isPressed: Boolean) {
        val activeCanvas = Canvas.activeCanvas ?: return
        try {
            if (isPressed) {
                activeCanvas.keyPressed(keyCode)
            } else {
                activeCanvas.keyReleased(keyCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            JarRuntimeLoader.activeMidlet?.pauseApp()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            JarRuntimeLoader.activeMidlet?.startApp()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameSurfaceView?.stopRendering()
        try {
            JarRuntimeLoader.activeMidlet?.destroyApp(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class AntaresGameSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {

    private val surfaceWidth = 240
    private val surfaceHeight = 320
    private val bufferBitmap = Bitmap.createBitmap(surfaceWidth, surfaceHeight, Bitmap.Config.RGB_565)
    private val bufferGraphics = Graphics(bufferBitmap)
    private val dstRect = Rect()
    private val filterPaint = Paint().apply { isFilterBitmap = false }

    @Volatile
    private var isRunning = false
    private var renderThread: Thread? = null

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true
        renderThread = Thread(this, "AntaresRenderLoop").apply { start() }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val targetAspect = surfaceWidth.toFloat() / surfaceHeight.toFloat()
        val screenAspect = width.toFloat() / height.toFloat()

        if (screenAspect > targetAspect) {
            val drawWidth = (height * targetAspect).toInt()
            val left = (width - drawWidth) / 2
            dstRect.set(left, 0, left + drawWidth, height)
        } else {
            val drawHeight = (width / targetAspect).toInt()
            val top = (height - drawHeight) / 2
            dstRect.set(0, top, width, top + drawHeight)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        stopRendering()
    }

    fun stopRendering() {
        isRunning = false
        renderThread?.interrupt()
        renderThread = null
    }

    override fun run() {
        while (isRunning) {
            var canvas: android.graphics.Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val activeCanvas = Canvas.activeCanvas
                    if (activeCanvas != null) {
                        bufferGraphics.beginFrame()
                        activeCanvas.paint(bufferGraphics)
                    }
                    canvas.drawColor(Color.BLACK)
                    canvas.drawBitmap(bufferBitmap, null, dstRect, filterPaint)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try { holder.unlockCanvasAndPost(canvas) } catch (ignored: Exception) {}
                }
            }

            try {
                Thread.sleep(16) // ~60 FPS
            } catch (ignored: InterruptedException) {
                break
            }
        }
    }
}
