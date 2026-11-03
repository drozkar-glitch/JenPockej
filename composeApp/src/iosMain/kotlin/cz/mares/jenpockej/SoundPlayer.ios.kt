package cz.mares.jenpockej

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual class SoundPlayer actual constructor() {
    private val wavCache = HashMap<SoundEffect, NSData>()
    private val activePlayers = ArrayList<AVAudioPlayer>()

    init {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryAmbient, error = null)
            session.setActive(true, error = null)
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

    actual fun release() {
        activePlayers.forEach { it.stop() }
        activePlayers.clear()
        wavCache.clear()
    }

    private fun ByteArray.toNsData(): NSData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}
