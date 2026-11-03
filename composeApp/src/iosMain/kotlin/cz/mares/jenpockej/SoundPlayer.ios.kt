package cz.mares.jenpockej

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.Foundation.NSData
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.UIKit.UIImpactFeedbackGenerator

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class SoundPlayer actual constructor() {
    private val wavCache = HashMap<SoundEffect, NSData>()
    private val activePlayers = ArrayList<AVAudioPlayer>()
    private val defaults = NSUserDefaults.standardUserDefaults
    private val impactGenerator = UIImpactFeedbackGenerator()

    init {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryAmbient, error = null)
            session.setActive(true, error = null)
            impactGenerator.prepare()
        } catch (_: Throwable) {
        }

        for (effect in SoundEffect.entries) {
            val bytes = RetroWaveFactory.buildEffectWav(effect)
            wavCache[effect] = bytes.toNsData()
        }
    }

    actual fun play(effect: SoundEffect) {
        val data = wavCache[effect] ?: return
        try {
            val player = AVAudioPlayer(data = data, error = null)
            player.prepareToPlay()
            player.play()
            activePlayers.removeAll { !it.playing }
            activePlayers.add(player)
            if (activePlayers.size > 6) {
                activePlayers.removeAt(0)
            }
        } catch (_: Throwable) {
        }
    }

    actual fun vibrate(type: HapticType) {
        try {
            when (type) {
                HapticType.MOVE -> {
                    impactGenerator.impactOccurred()
                }
                HapticType.CATCH -> {
                    impactGenerator.impactOccurred()
                    AudioServicesPlaySystemSound(1519u)
                }
                HapticType.BONUS -> {
                    AudioServicesPlaySystemSound(1520u)
                }
                HapticType.MISS -> {
                    AudioServicesPlaySystemSound(1521u)
                }
                HapticType.GAME_OVER -> {
                    AudioServicesPlaySystemSound(4095u)
                }
            }
        } catch (_: Throwable) {
        }
    }

    actual fun loadInt(key: String, defaultVal: Int): Int {
        return if (defaults.objectForKey(key) != null) {
            defaults.integerForKey(key).toInt()
        } else {
            defaultVal
        }
    }

    actual fun saveInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), forKey = key)
    }

    actual fun loadBool(key: String, defaultVal: Boolean): Boolean {
        return if (defaults.objectForKey(key) != null) {
            defaults.boolForKey(key)
        } else {
            defaultVal
        }
    }

    actual fun saveBool(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }

    actual fun release() {
        activePlayers.forEach { it.stop() }
        activePlayers.clear()
        wavCache.clear()
    }

    private fun ByteArray.toNsData(): NSData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}
