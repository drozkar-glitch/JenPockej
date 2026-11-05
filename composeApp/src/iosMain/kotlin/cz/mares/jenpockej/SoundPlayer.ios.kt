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
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.dataTaskWithRequest
import platform.Foundation.setHTTPBody
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.UIKit.UIImpactFeedbackGenerator
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

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

    actual fun loadString(key: String, defaultVal: String): String {
        return defaults.stringForKey(key) ?: defaultVal
    }

    actual fun saveString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    actual fun httpGet(url: String, callback: (Boolean, String) -> Unit) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            dispatch_async(dispatch_get_main_queue()) { callback(false, "") }
            return
        }
        val request = NSMutableURLRequest(uRL = nsUrl).apply {
            setHTTPMethod("GET")
            setValue("application/json", forHTTPHeaderField = "Accept")
        }
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
            val statusCode = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
            val text = if (data != null) {
                NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString() ?: ""
            } else {
                ""
            }
            val ok = error == null && statusCode in 200..299
            dispatch_async(dispatch_get_main_queue()) {
                callback(ok, text)
            }
        }
        task.resume()
    }

    actual fun httpPut(url: String, bodyJson: String, callback: (Boolean, Int, String) -> Unit) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl == null) {
            dispatch_async(dispatch_get_main_queue()) { callback(false, 0, "") }
            return
        }
        val bodyBytes = bodyJson.encodeToByteArray()
        val request = NSMutableURLRequest(uRL = nsUrl).apply {
            setHTTPMethod("PUT")
            setValue("application/json; charset=utf-8", forHTTPHeaderField = "Content-Type")
            setValue("application/json", forHTTPHeaderField = "Accept")
            setHTTPBody(bodyBytes.toNsData())
        }
        val task = NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
            val statusCode = (response as? NSHTTPURLResponse)?.statusCode?.toInt() ?: 0
            val text = if (data != null) {
                NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString() ?: ""
            } else {
                ""
            }
            val ok = error == null && statusCode in 200..299
            dispatch_async(dispatch_get_main_queue()) {
                callback(ok, statusCode, text)
            }
        }
        task.resume()
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
