package cz.mares.jenpockej

import kotlin.math.PI
import kotlin.math.sin

enum class SoundEffect {
    TICK_RAMP_0,
    TICK_RAMP_1,
    TICK_RAMP_2,
    TICK_RAMP_3,
    CATCH,
    MISS,
    BONUS,
    GAME_OVER,
    BUTTON
}

enum class HapticType {
    MOVE,
    CATCH,
    MISS,
    BONUS,
    GAME_OVER
}

data class ToneSegment(
    val frequencyHz: Int,
    val durationMs: Int
)

object RetroWaveFactory {
    private const val SAMPLE_RATE = 22050

    fun buildEffectWav(effect: SoundEffect): ByteArray {
        val segments = when (effect) {
            SoundEffect.TICK_RAMP_0 -> listOf(ToneSegment(1750, 26))
            SoundEffect.TICK_RAMP_1 -> listOf(ToneSegment(1480, 26))
            SoundEffect.TICK_RAMP_2 -> listOf(ToneSegment(1960, 26))
            SoundEffect.TICK_RAMP_3 -> listOf(ToneSegment(1320, 26))
            SoundEffect.BUTTON -> listOf(ToneSegment(2400, 18))
            SoundEffect.CATCH -> listOf(
                ToneSegment(2100, 42),
                ToneSegment(0, 14),
                ToneSegment(2850, 65)
            )
            SoundEffect.MISS -> listOf(
                ToneSegment(980, 90),
                ToneSegment(0, 25),
                ToneSegment(820, 90),
                ToneSegment(0, 25),
                ToneSegment(660, 110),
                ToneSegment(0, 25),
                ToneSegment(520, 150)
            )
            SoundEffect.BONUS -> listOf(
                ToneSegment(1560, 55),
                ToneSegment(1970, 55),
                ToneSegment(2350, 55),
                ToneSegment(3120, 110)
            )
            SoundEffect.GAME_OVER -> listOf(
                ToneSegment(1100, 130),
                ToneSegment(0, 35),
                ToneSegment(880, 130),
                ToneSegment(0, 35),
                ToneSegment(660, 150),
                ToneSegment(0, 35),
                ToneSegment(440, 280)
            )
        }
        return encodePcmToWav(segments)
    }

    private fun encodePcmToWav(segments: List<ToneSegment>): ByteArray {
        val totalMs = segments.sumOf { it.durationMs }
        val totalSamples = (SAMPLE_RATE * totalMs) / 1000
        val pcmBytes = ByteArray(totalSamples * 2)

        var sampleCursor = 0
        for (segment in segments) {
            val segSamples = (SAMPLE_RATE * segment.durationMs) / 1000
            for (i in 0 until segSamples) {
                if (sampleCursor >= totalSamples) break
                val sampleValue: Short = if (segment.frequencyHz <= 0) {
                    0
                } else {
                    val phase = (2.0 * PI * segment.frequencyHz * i) / SAMPLE_RATE
                    val rawSquare = if (sin(phase) >= 0.0) 1.0 else -1.0
                    val fade = if (i > segSamples - 40) {
                        (segSamples - i).coerceAtLeast(0) / 40.0
                    } else {
                        1.0
                    }
                    (rawSquare * fade * 9500.0).toInt().toShort()
                }
                val bytePos = sampleCursor * 2
                pcmBytes[bytePos] = (sampleValue.toInt() and 0xFF).toByte()
                pcmBytes[bytePos + 1] = ((sampleValue.toInt() shr 8) and 0xFF).toByte()
                sampleCursor++
            }
        }

        val header = ByteArray(44)
        val fileSizeMinus8 = pcmBytes.size + 36
        val byteRate = SAMPLE_RATE * 2

        writeAscii(header, 0, "RIFF")
        writeIntLE(header, 4, fileSizeMinus8)
        writeAscii(header, 8, "WAVE")
        writeAscii(header, 12, "fmt ")
        writeIntLE(header, 16, 16)
        writeShortLE(header, 20, 1)
        writeShortLE(header, 22, 1)
        writeIntLE(header, 24, SAMPLE_RATE)
        writeIntLE(header, 28, byteRate)
        writeShortLE(header, 32, 2)
        writeShortLE(header, 34, 16)
        writeAscii(header, 36, "data")
        writeIntLE(header, 40, pcmBytes.size)

        return header + pcmBytes
    }

    private fun writeAscii(target: ByteArray, offset: Int, text: String) {
        for (i in text.indices) {
            target[offset + i] = text[i].code.toByte()
        }
    }

    private fun writeIntLE(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value and 0xFF).toByte()
        target[offset + 1] = ((value shr 8) and 0xFF).toByte()
        target[offset + 2] = ((value shr 16) and 0xFF).toByte()
        target[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun writeShortLE(target: ByteArray, offset: Int, value: Int) {
        target[offset] = (value and 0xFF).toByte()
        target[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }
}

expect class SoundPlayer() {
    fun play(effect: SoundEffect)
    fun vibrate(type: HapticType)
    fun loadInt(key: String, defaultVal: Int): Int
    fun saveInt(key: String, value: Int)
    fun loadBool(key: String, defaultVal: Boolean): Boolean
    fun saveBool(key: String, value: Boolean)
    fun loadString(key: String, defaultVal: String): String
    fun saveString(key: String, value: String)
    fun httpGet(url: String, callback: (Boolean, String) -> Unit)
    fun httpPut(url: String, bodyJson: String, callback: (Boolean, Int, String) -> Unit)
    fun release()
}
