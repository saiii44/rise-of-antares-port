package com.herocraft.antares.port.runtime

import android.content.Context
import android.media.MediaPlayer
import java.io.File
import java.io.FileOutputStream

/**
 * High-performance audio engine executing the 3 original MIDI soundtracks.
 */
class MidiAudioEngine(private val context: Context) {
    private var player: MediaPlayer? = null

    fun loadTrack(assetPath: String) {
        try {
            player?.release()
            val tempFile = File.createTempFile("antares_midi", ".mid", context.cacheDir)
            tempFile.deleteOnExit()
            context.assets.open(assetPath).use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            player = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                isLooping = true
                prepare()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        player?.start()
    }

    fun pause() {
        player?.pause()
    }

    fun release() {
        player?.release()
        player = null
    }
}
