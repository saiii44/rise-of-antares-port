package com.herocraft.antares.port.runtime

import android.content.Context
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile
import javax.microedition.midlet.MIDlet
import javax.microedition.rms.RecordStore

/**
 * Dedicated Bytecode Runtime Loader for Rise of Antares.
 * Loads the original Z.class from compiled Dalvik bytecode (classes.dex)
 * with direct resource resolution from RiseOfAntares__Nokia_S40_3_240x320.jar.
 */
object JarRuntimeLoader {
    private const val TAG = "AntaresLoader"

    var activeMidlet: MIDlet? = null
        private set

    class AntaresClassLoader(
        dexPath: String,
        optimizedDirectory: String?,
        librarySearchPath: String?,
        parent: ClassLoader,
        private val jarFile: File
    ) : DexClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent) {

        private var zipFile: ZipFile? = null

        init {
            try {
                if (jarFile.exists()) {
                    zipFile = ZipFile(jarFile)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error opening jarFile zip: ${e.message}")
            }
        }

        override fun getResourceAsStream(name: String): InputStream? {
            val cleanName = if (name.startsWith("/")) name.substring(1) else name

            // 1. Prioritize reading directly from authentic Nokia S40 game jar
            try {
                val zf = zipFile
                if (zf != null) {
                    val entry = zf.getEntry(cleanName) ?: zf.getEntry(name)
                    if (entry != null) {
                        val bytes = zf.getInputStream(entry).use { it.readBytes() }
                        Log.i(TAG, "Loaded resource '$name' ($cleanName) from JAR: ${bytes.size} bytes")
                        return ByteArrayInputStream(bytes)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error resolving resource '$name' from JAR: ${e.message}")
            }

            // 2. Fallback to parent / system loader
            return super.getResourceAsStream(name) ?: super.getResourceAsStream(cleanName)
        }
    }

    fun loadAndStart(context: Context): MIDlet {
        if (activeMidlet != null) return activeMidlet!!

        // Set persistent storage directory for RMS
        RecordStore.rootDir = File(context.filesDir, "rms").apply { if (!exists()) mkdirs() }

        // Strategy 1: Check if Z is already present in primary APK classpath
        try {
            val clazz = Class.forName("Z")
            Log.i(TAG, "Z.class found in primary classpath: $clazz")
            val instance = clazz.getDeclaredConstructor().newInstance() as MIDlet
            activeMidlet = instance
            instance.startApp()
            return instance
        } catch (e: Exception) {
            Log.i(TAG, "Z not in primary classpath, using Dalvik DEX loader: ${e.message}")
        }

        // Strategy 2: Load via AntaresClassLoader using separate assets/classes.dex and authentic resource JAR
        val codeCacheDir = context.codeCacheDir
        val dexFile = File(codeCacheDir, "antares_classes.dex")
        val jarFile = File(codeCacheDir, "antares_game.jar")

        // 1. Extract and validate checksum for classes.dex
        extractAssetWithChecksumValidation(context, "classes.dex", dexFile)

        // 2. Extract and validate checksum for authentic antares_game.jar
        extractAssetWithChecksumValidation(context, "antares_game.jar", jarFile)

        val optDir = File(codeCacheDir, "opt").apply { if (!exists()) mkdirs() }

        // Use assets/classes.dex separately for Dalvik bytecode and jarFile for original assets
        val classLoader = AntaresClassLoader(
            dexFile.absolutePath,
            optDir.absolutePath,
            null,
            context.classLoader,
            jarFile
        )

        // Set context class loader on current thread
        Thread.currentThread().contextClassLoader = classLoader

        Log.i(TAG, "Initialized AntaresClassLoader with dexPath: ${dexFile.absolutePath}, resourceJar: ${jarFile.absolutePath}")
        val midletClass = classLoader.loadClass("Z")
        Log.i(TAG, "Successfully loaded class Z: $midletClass")

        val instance = midletClass.getDeclaredConstructor().newInstance() as MIDlet
        activeMidlet = instance

        Log.i(TAG, "Created instance of Z, invoking startApp()...")
        instance.startApp()
        Log.i(TAG, "Z.startApp() executed successfully!")

        return instance
    }

    private fun extractAssetWithChecksumValidation(
        context: Context,
        assetName: String,
        targetFile: File
    ): File {
        val assetBytes = context.assets.open(assetName).use { it.readBytes() }
        val expectedChecksum = sha256Hex(assetBytes)

        var needsExtract = true
        if (targetFile.exists() && targetFile.length() > 0L) {
            try {
                val cachedChecksum = sha256Hex(targetFile.readBytes())
                if (cachedChecksum == expectedChecksum) {
                    Log.i(TAG, "Cache validated for $assetName (SHA-256: $cachedChecksum). Reusing cache.")
                    needsExtract = false
                } else {
                    Log.w(TAG, "Cache stale for $assetName: cached $cachedChecksum != asset $expectedChecksum. Overwriting...")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading cached $assetName: ${e.message}. Re-extracting...")
            }
        }

        if (needsExtract) {
            if (targetFile.exists()) {
                targetFile.setWritable(true)
                targetFile.delete()
            }
            FileOutputStream(targetFile).use { output ->
                output.write(assetBytes)
            }
            targetFile.setReadOnly()
            Log.i(TAG, "Extracted fresh $assetName (${targetFile.length()} bytes, SHA-256: $expectedChecksum, readOnly=${!targetFile.canWrite()})")
        } else {
            targetFile.setReadOnly()
        }

        return targetFile
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
