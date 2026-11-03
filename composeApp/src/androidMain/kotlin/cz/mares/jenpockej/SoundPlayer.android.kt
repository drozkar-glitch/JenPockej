package cz.mares.jenpockej

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

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

    actual fun release() {
        pcmCache.clear()
    }
}
