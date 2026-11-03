package cz.mares.jenpockej

import kotlin.random.Random

data class OceanSnapshot(
    val mode: GameMode = GameMode.GAME_A,
    val isRunning: Boolean = false,
    val isDemoMode: Boolean = true,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val score: Int = 0,
    val highScoreA: Int = 0,
    val highScoreB: Int = 0,
    // Pozice potapece: 0 = na lodi, 1 = na zebriku pod lodi, 2..4 = morske dno, 5 = u truhly s pokladem
    val diverPos: Int = 0,
    val bagGoldCount: Int = 0,
    val grabbingAtChest: Boolean = false,
    // Delky 4 chapadel chobotnice: max delky jsou [3, 4, 4, 3]
    val tentacleLengths: List<Int> = listOf(1, 2, 1, 0),
    val lostDivers: Int = 0,
    val caughtAnimFrame: Int? = null,
    val blinkPhase: Boolean = false,
    val unloadBonusTicks: Int = 0
)

class OceanGameEngine(private val media: SoundPlayer) {
    companion object {
        private const val KEY_OCEAN_HIGH_A = "ocean_high_score_a"
        private const val KEY_OCEAN_HIGH_B = "ocean_high_score_b"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"

        val MAX_TENTACLE_LEN = intArrayOf(3, 4, 4, 3)
    }

    var state = OceanSnapshot(
        soundEnabled = media.loadBool(KEY_SOUND, true),
        hapticsEnabled = media.loadBool(KEY_HAPTICS, true),
        highScoreA = media.loadInt(KEY_OCEAN_HIGH_A, 0),
        highScoreB = media.loadInt(KEY_OCEAN_HIGH_B, 0)
    )
        private set

    private var subTickCounter = 0
    private var currentSlot = 0
    private val tentacleExtending = booleanArrayOf(true, true, false, true)
    private val tentaclePauseSlots = intArrayOf(0, 0, 0, 0)

    fun syncAudioSettings(sound: Boolean, haptics: Boolean) {
        state = state.copy(soundEnabled = sound, hapticsEnabled = haptics)
    }

    fun toggleSound() {
        val next = !state.soundEnabled
        state = state.copy(soundEnabled = next)
        media.saveBool(KEY_SOUND, next)
        if (next) {
            media.play(SoundEffect.BUTTON)
        }
    }

    fun toggleHaptics() {
        val next = !state.hapticsEnabled
        state = state.copy(hapticsEnabled = next)
        media.saveBool(KEY_HAPTICS, next)
        if (next) {
            media.vibrate(HapticType.MOVE)
        }
    }

    fun startNewGame(mode: GameMode) {
        subTickCounter = 0
        currentSlot = 0
        for (i in 0..3) {
            tentacleExtending[i] = i % 2 == 0
            tentaclePauseSlots[i] = i
        }
        state = state.copy(
            mode = mode,
            isRunning = true,
            isDemoMode = false,
            isGameOver = false,
            score = 0,
            diverPos = 0,
            bagGoldCount = 0,
            grabbingAtChest = false,
            tentacleLengths = listOf(1, 1, 0, 0),
            lostDivers = 0,
            caughtAnimFrame = null,
            unloadBonusTicks = 0
        )
        if (state.soundEnabled) {
            media.play(SoundEffect.BONUS)
        }
        if (state.hapticsEnabled) {
            media.vibrate(HapticType.CATCH)
        }
    }

    fun resetToDemo() {
        subTickCounter = 0
        currentSlot = 0
        state = state.copy(
            isRunning = false,
            isDemoMode = true,
            isGameOver = false,
            score = 0,
            diverPos = 2,
            bagGoldCount = 1,
            grabbingAtChest = false,
            tentacleLengths = listOf(1, 2, 1, 1),
            lostDivers = 0,
            caughtAnimFrame = null,
            unloadBonusTicks = 0
        )
    }

    /**
     * Pohyb potapece doleva / nahoru na lod (tlacitka vlevo)
     */
    fun moveLeftOrUp() {
        if (!state.isRunning || state.isGameOver || state.caughtAnimFrame != null) return

        val cur = state.diverPos
        if (cur <= 0) return

        val nextPos = cur - 1
        // Pokud se potapec vraci na lod (nextPos == 0) a nese v pytli zlato,
        // vylozi poklad na palubu a ziska +3 bonusove body!
        if (nextPos == 0 && state.bagGoldCount > 0) {
            state = state.copy(
                diverPos = 0,
                bagGoldCount = 0,
                grabbingAtChest = false,
                unloadBonusTicks = 8
            )
            awardOceanPoints(3, isBonus = true)
            return
        }

        state = state.copy(
            diverPos = nextPos,
            grabbingAtChest = false
        )

        if (checkImmediateTentacleCollision(nextPos)) {
            triggerDiverCaught()
            return
        }

        if (state.soundEnabled) {
            media.play(SoundEffect.BUTTON)
        }
        if (state.hapticsEnabled) {
            media.vibrate(HapticType.MOVE)
        }
    }

    /**
     * Pohyb potapece doprava / nabrani zlata z truhly (tlacitka vpravo)
     */
    fun moveRightOrGrabGold() {
        if (!state.isRunning || state.isGameOver || state.caughtAnimFrame != null) return

        val cur = state.diverPos
        if (cur < 5) {
            val nextPos = cur + 1
            val autoGrabAtChest = nextPos == 5
            state = state.copy(
                diverPos = nextPos,
                grabbingAtChest = autoGrabAtChest
            )

            if (checkImmediateTentacleCollision(nextPos)) {
                triggerDiverCaught()
                return
            }

            if (autoGrabAtChest) {
                state = state.copy(bagGoldCount = (state.bagGoldCount + 1).coerceAtMost(9))
                awardOceanPoints(1, isBonus = false)
            } else {
                if (state.soundEnabled) {
                    media.play(SoundEffect.BUTTON)
                }
                if (state.hapticsEnabled) {
                    media.vibrate(HapticType.MOVE)
                }
            }
        } else {
            // Potapec uz stoji u truhly (pos == 5) -> dalsi stisk nabere dalsi hrudku zlata (+1 bod)
            if (checkImmediateTentacleCollision(5)) {
                triggerDiverCaught()
                return
            }
            state = state.copy(
                bagGoldCount = (state.bagGoldCount + 1).coerceAtMost(9),
                grabbingAtChest = !state.grabbingAtChest
            )
            awardOceanPoints(1, isBonus = false)
        }
    }

    private fun checkImmediateTentacleCollision(pos: Int): Boolean {
        val tIndex = when (pos) {
            2 -> 0
            3 -> 1
            4 -> 2
            5 -> 3
            else -> return false
        }
        return state.tentacleLengths[tIndex] >= MAX_TENTACLE_LEN[tIndex]
    }

    fun currentTickDelayMs(): Long {
        if (state.isGameOver) return 450L
        if (state.isDemoMode && !state.isRunning) return 220L
        if (state.caughtAnimFrame != null) return 280L

        val hundredTier = (state.score / 100).coerceAtMost(5)
        val withinHundred = state.score % 100
        val speedStep = (withinHundred / 5).coerceIn(0, 19)

        val baseCycleMs: Long
        val peakCycleMs: Long

        if (state.mode == GameMode.GAME_A) {
            baseCycleMs = when (hundredTier) {
                0 -> 960L
                1 -> 900L
                2 -> 840L
                3 -> 800L
                else -> 760L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 480L
                1 -> 440L
                2 -> 410L
                else -> 380L
            }
        } else {
            baseCycleMs = when (hundredTier) {
                0 -> 820L
                1 -> 760L
                2 -> 720L
                else -> 680L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 400L
                1 -> 370L
                else -> 340L
            }
        }

        val fullCycle = baseCycleMs - ((baseCycleMs - peakCycleMs) * speedStep) / 19L
        return (fullCycle / 4L).coerceAtLeast(85L)
    }

    fun stepTick() {
        subTickCounter++
        currentSlot = subTickCounter % 4
        val nextBlink = if (currentSlot % 2 == 0) !state.blinkPhase else state.blinkPhase

        if (state.isGameOver) {
            state = state.copy(blinkPhase = !state.blinkPhase)
            return
        }

        if (state.isDemoMode && !state.isRunning) {
            stepDemoTick(nextBlink)
            return
        }

        // Animace chyceneho potapece v chapadlech chobotnice
        val caughtFrame = state.caughtAnimFrame
        if (caughtFrame != null) {
            if (caughtFrame < 4) {
                state = state.copy(
                    caughtAnimFrame = caughtFrame + 1,
                    blinkPhase = !state.blinkPhase
                )
                if (state.soundEnabled) {
                    media.play(SoundEffect.MISS)
                }
            } else {
                val nowGameOver = state.lostDivers >= 3
                state = state.copy(
                    caughtAnimFrame = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
                    diverPos = 0,
                    bagGoldCount = 0,
                    grabbingAtChest = false,
                    tentacleLengths = listOf(1, 1, 0, 0),
                    blinkPhase = !state.blinkPhase
                )
                if (nowGameOver) {
                    if (state.soundEnabled) {
                        media.play(SoundEffect.GAME_OVER)
                    }
                    if (state.hapticsEnabled) {
                        media.vibrate(HapticType.GAME_OVER)
                    }
                }
            }
            return
        }

        // V kazdem dilcim taktu (currentSlot 0..3) se pohne odpovidajici chapadlo chobotnice
        val tIdx = currentSlot
        val maxLen = MAX_TENTACLE_LEN[tIdx]
        val currentLengths = state.tentacleLengths.toMutableList()
        var curLen = currentLengths[tIdx]
        var caughtNow = false

        if (tentaclePauseSlots[tIdx] > 0) {
            tentaclePauseSlots[tIdx]--
        } else {
            if (tentacleExtending[tIdx]) {
                if (curLen < maxLen) {
                    curLen++
                    currentLengths[tIdx] = curLen
                    // Pokud chapadlo prave dosahlo sve maximalni delky a potapec stoji v jeho dosahu:
                    val dangerDiverPos = tIdx + 2
                    if (curLen == maxLen && state.diverPos == dangerDiverPos) {
                        caughtNow = true
                    }
                } else {
                    // Chapadlo je plne natazene -> pokud pod nim potapec porad stoji, chobotnice ho chyti
                    val dangerDiverPos = tIdx + 2
                    if (state.diverPos == dangerDiverPos) {
                        caughtNow = true
                    } else {
                        tentacleExtending[tIdx] = false
                        curLen = (curLen - 1).coerceAtLeast(0)
                        currentLengths[tIdx] = curLen
                    }
                }
            } else {
                if (curLen > 0) {
                    curLen--
                    currentLengths[tIdx] = curLen
                } else {
                    // Chapadlo je zatazene -> ve Hre A obcas kratce pocka, ve Hre B vyjede skoro hned
                    val pauseChance = if (state.mode == GameMode.GAME_A) 0.45f else 0.20f
                    if (Random.nextFloat() < pauseChance) {
                        tentaclePauseSlots[tIdx] = Random.nextInt(1, 3)
                    }
                    tentacleExtending[tIdx] = true
                }
            }
        }

        state = state.copy(
            tentacleLengths = currentLengths,
            grabbingAtChest = false,
            unloadBonusTicks = if (state.unloadBonusTicks > 0) state.unloadBonusTicks - 1 else 0,
            blinkPhase = nextBlink
        )

        if (caughtNow) {
            triggerDiverCaught()
            return
        }

        if (state.soundEnabled) {
            val tickSound = when (currentSlot) {
                0 -> SoundEffect.TICK_RAMP_0
                1 -> SoundEffect.TICK_RAMP_1
                2 -> SoundEffect.TICK_RAMP_2
                else -> SoundEffect.TICK_RAMP_3
            }
            media.play(tickSound)
        }
    }

    private fun triggerDiverCaught() {
        val nextLost = (state.lostDivers + 1).coerceAtMost(3)
        state = state.copy(
            lostDivers = nextLost,
            caughtAnimFrame = 0,
            grabbingAtChest = false
        )
        if (state.soundEnabled) {
            media.play(SoundEffect.MISS)
        }
        if (state.hapticsEnabled) {
            media.vibrate(HapticType.MISS)
        }
    }

    private fun awardOceanPoints(points: Int, isBonus: Boolean) {
        val prevScore = state.score
        val newScore = (prevScore + points).coerceAtMost(9999)
        var newLost = state.lostDivers
        var lifeRestored = false

        val prevInThousand = prevScore % 1000
        val newInThousand = newScore % 1000
        if (
            (prevInThousand < 200 && newInThousand >= 200) ||
            (prevInThousand < 500 && newInThousand >= 500) ||
            (prevScore < 1000 && newScore >= 1000)
        ) {
            newLost = 0
            lifeRestored = true
        }

        var bestA = state.highScoreA
        var bestB = state.highScoreB
        if (state.mode == GameMode.GAME_A && newScore > bestA) {
            bestA = newScore
            media.saveInt(KEY_OCEAN_HIGH_A, bestA)
        }
        if (state.mode == GameMode.GAME_B && newScore > bestB) {
            bestB = newScore
            media.saveInt(KEY_OCEAN_HIGH_B, bestB)
        }

        state = state.copy(
            score = newScore,
            highScoreA = bestA,
            highScoreB = bestB,
            lostDivers = newLost
        )

        if (state.soundEnabled) {
            if (isBonus || lifeRestored) {
                media.play(SoundEffect.BONUS)
            } else {
                media.play(SoundEffect.CATCH)
            }
        }
        if (state.hapticsEnabled) {
            if (isBonus || lifeRestored) {
                media.vibrate(HapticType.BONUS)
            } else {
                media.vibrate(HapticType.CATCH)
            }
        }
    }

    private fun stepDemoTick(nextBlink: Boolean) {
        val updated = state.tentacleLengths.toMutableList()
        val maxL = MAX_TENTACLE_LEN[currentSlot]
        updated[currentSlot] = (updated[currentSlot] + 1) % (maxL + 1)

        val nextDiver = if (subTickCounter % 3 == 0) {
            val candidate = (state.diverPos + 1) % 6
            val tIdx = candidate - 2
            if (tIdx in 0..3 && updated[tIdx] >= MAX_TENTACLE_LEN[tIdx]) {
                state.diverPos
            } else {
                candidate
            }
        } else {
            state.diverPos
        }

        state = state.copy(
            diverPos = nextDiver,
            bagGoldCount = if (nextDiver >= 3) 2 else 1,
            grabbingAtChest = nextDiver == 5 && nextBlink,
            tentacleLengths = updated,
            blinkPhase = nextBlink
        )
    }
}
