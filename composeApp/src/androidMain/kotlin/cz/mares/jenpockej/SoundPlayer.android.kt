package cz.mares.jenpockej

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object AndroidPlatformContext {
    var appContext: Context? = null
}

actual class SoundPlayer actual constructor() {
    private val pcmCache = HashMap<SoundEffect, ByteArray>()

    init {
        for (effect in SoundEffect.entries) {
            val fullWav = RetroWaveFactory.buildEffectWav(effect)
            if (fullWav.size > 44) {
                pcmCache[effect] = fullWav.copyOfRange(44, fullWav.size)
            }
        }
    }

    actual fun play(effect: SoundEffect) {
        val pcmData = pcmCache[effect] ?: return
        Thread {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(22050)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmData.size.coerceAtLeast(1024))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(pcmData, 0, pcmData.size)
                track.play()
                val sleepMs = (pcmData.size * 1000L) / (22050L * 2L) + 80L
                Thread.sleep(sleepMs)
                track.stop()
                track.release()
            } catch (_: Throwable) {
            }
        }.start()
    }

    @Suppress("DEPRECATION")
    actual fun vibrate(type: HapticType) {
        val ctx = AndroidPlatformContext.appContext ?: return
        try {
            val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (!vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.MOVE -> VibrationEffect.createOneShot(18L, 90)
                    HapticType.CATCH -> VibrationEffect.createWaveform(
                        longArrayOf(0L, 24L, 32L, 34L),
                        intArrayOf(0, 140, 0, 210),
                        -1
                    )
                    HapticType.BONUS -> VibrationEffect.createWaveform(
                        longArrayOf(0L, 40L, 35L, 40L, 35L, 80L),
                        intArrayOf(0, 160, 0, 200, 0, 255),
                        -1
                    )
                    HapticType.MISS -> VibrationEffect.createWaveform(
                        longArrayOf(0L, 85L, 45L, 130L),
                        intArrayOf(0, 220, 0, 255),
                        -1
                    )
                    HapticType.GAME_OVER -> VibrationEffect.createWaveform(
                        longArrayOf(0L, 130L, 55L, 130L, 55L, 250L),
                        intArrayOf(0, 255, 0, 255, 0, 255),
                        -1
                    )
                }
                vibrator.vibrate(effect)
            } else {
                val duration = when (type) {
                    HapticType.MOVE -> 18L
                    HapticType.CATCH -> 45L
                    HapticType.BONUS -> 110L
                    HapticType.MISS -> 170L
                    HapticType.GAME_OVER -> 320L
                }
                vibrator.vibrate(duration)
            }
        } catch (_: Throwable) {
        }
    }

    actual fun loadInt(key: String, defaultVal: Int): Int {
        val prefs = AndroidPlatformContext.appContext
            ?.getSharedPreferences("jen_pockej_prefs", Context.MODE_PRIVATE)
            ?: return defaultVal
        return prefs.getInt(key, defaultVal)
    }

    actual fun saveInt(key: String, value: Int) {
        val prefs = AndroidPlatformContext.appContext
            ?.getSharedPreferences("jen_pockej_prefs", Context.MODE_PRIVATE)
            ?: return
        prefs.edit().putInt(key, value).apply()
    }

    actual fun loadBool(key: String, defaultVal: Boolean): Boolean {
        val prefs = AndroidPlatformContext.appContext
            ?.getSharedPreferences("jen_pockej_prefs", Context.MODE_PRIVATE)
            ?: return defaultVal
        return prefs.getBoolean(key, defaultVal)
    }

    actual fun saveBool(key: String, value: Boolean) {
        val prefs = AndroidPlatformContext.appContext
            ?.getSharedPreferences("jen_pockej_prefs", Context.MODE_PRIVATE)
            ?: return
        prefs.edit().putBoolean(key, value).apply()
    }

    actual fun release() {
        pcmCache.clear()
    }
}
