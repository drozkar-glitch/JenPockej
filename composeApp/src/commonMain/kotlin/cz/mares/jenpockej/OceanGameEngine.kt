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

    // 4 dilci takty (sloty 0..3) tvori 1 cely cyklus chobotnice jako na cipu KB1013VK1-2
    private var subTickCounter = 0
    private var currentSlot = 0
    private val tentacleExtending = booleanArrayOf(true, false, true, false)
    private val tentaclePauseSlots = intArrayOf(0, 1, 0, 1)
    private var grabCooldownSlots = 0

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
        grabCooldownSlots = 0
        val initialLengths = if (mode == GameMode.GAME_A) {
            listOf(1, 2, 0, 1)
        } else {
            listOf(2, 2, 1, 1)
        }
        tentacleExtending[0] = true
        tentacleExtending[1] = false
        tentacleExtending[2] = true
        tentacleExtending[3] = false
        for (i in 0..3) {
            tentaclePauseSlots[i] = i % 2
        }

        state = state.copy(
            mode = mode,
            isRunning = true,
            isDemoMode = false,
            isGameOver = false,
            score = 0,
            diverPos = 1,
            bagGoldCount = 0,
            grabbingAtChest = false,
            tentacleLengths = initialLengths,
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
        grabCooldownSlots = 0
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
        grabCooldownSlots = 0

        // Pokud potapec vyleze ze zebriku (1) na palubu lodi (0) s pytlem zlata,
        // vylozi poklad na palubu (+3 bonusove body)
        if (nextPos == 0) {
            if (state.bagGoldCount > 0) {
                state = state.copy(
                    diverPos = 0,
                    bagGoldCount = 0,
                    grabbingAtChest = false,
                    unloadBonusTicks = 5
                )
                awardOceanPoints(3, isBonus = true)
            } else {
                state = state.copy(
                    diverPos = 0,
                    grabbingAtChest = false
                )
                if (state.soundEnabled) {
                    media.play(SoundEffect.BUTTON)
                }
                if (state.hapticsEnabled) {
                    media.vibrate(HapticType.MOVE)
                }
            }
            return
        }

        state = state.copy(
            diverPos = nextPos,
            grabbingAtChest = false
        )

        // Pokud hrac sam vstoupi primo pod plne natazene chapadlo, chobotnice ho chyti
        if (isTentacleFullyExtendedAtDiverPos(nextPos)) {
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
     * Pohyb potapece doprava / nabrani zlata z otevrene truhly (tlacitka vpravo)
     */
    fun moveRightOrGrabGold() {
        if (!state.isRunning || state.isGameOver || state.caughtAnimFrame != null) return

        val cur = state.diverPos
        if (cur < 5) {
            val nextPos = cur + 1
            state = state.copy(
                diverPos = nextPos,
                grabbingAtChest = false
            )

            // Pokud hrac vstoupi primo pod plne natazene chapadlo, chobotnice ho chyti
            if (isTentacleFullyExtendedAtDiverPos(nextPos)) {
                triggerDiverCaught()
                return
            }

            // Pri prichodu k truhle (nextPos == 5) si potapec musi v dalsim stisku
            // (nebo po kratkem taktu) nabrat zlato – tim je hra napinava jako na IM-03
            grabCooldownSlots = 0
            if (state.soundEnabled) {
                media.play(SoundEffect.BUTTON)
            }
            if (state.hapticsEnabled) {
                media.vibrate(HapticType.MOVE)
            }
        } else {
            // Potapec uz stoji u truhly (diverPos == 5) a natahuje ruku pro zlato (+1 bod).
            // Ochrana proti spamovani tlacitka: mezi nabranim dvou minci musi uplynout 2 dilci takty.
            if (grabCooldownSlots > 0) return

            if (isTentacleFullyExtendedAtDiverPos(5)) {
                triggerDiverCaught()
                return
            }

            grabCooldownSlots = 2
            state = state.copy(
                bagGoldCount = (state.bagGoldCount + 1).coerceAtMost(99),
                grabbingAtChest = true
            )
            awardOceanPoints(1, isBonus = false)
        }
    }

    private fun isTentacleFullyExtendedAtDiverPos(pos: Int): Boolean {
        val tIndex = pos - 2
        if (tIndex !in 0..3) return false
        return state.tentacleLengths[tIndex] >= MAX_TENTACLE_LEN[tIndex]
    }

    /**
     * Prodleva jednoho dilciho taktu (slotu).
     * 4 dilci takty = 1 cely cyklus vsech 4 chapadel.
     * Rychlost sleduje pilovitou krivku po stovkach bodu (0..99, 100..199, 200..299...)
     * se skokovym zpomalenim na kazde cele stovce.
     */
    fun currentTickDelayMs(): Long {
        if (state.isGameOver) return 450L
        if (state.isDemoMode && !state.isRunning) return 220L
        if (state.caughtAnimFrame != null) return 270L

        val hundredTier = (state.score / 100).coerceAtMost(5)
        val withinHundred = state.score % 100
        val speedStep = (withinHundred / 5).coerceIn(0, 19)

        val baseCycleMs: Long
        val peakCycleMs: Long

        if (state.mode == GameMode.GAME_A) {
            // HRA A: Zacina na 1000 ms/cyklus (250 ms na krok chapadla),
            // pred stovkou zrychli k 480..380 ms a na cele stovce opet zvolni
            baseCycleMs = when (hundredTier) {
                0 -> 1000L
                1 -> 920L
                2 -> 860L
                3 -> 810L
                4 -> 780L
                else -> 750L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 480L
                1 -> 440L
                2 -> 415L
                3 -> 395L
                4 -> 385L
                else -> 375L
            }
        } else {
            // HRA B: Svizznejsi tempo od zacatku (840 ms/cyklus) a agresivnejsi chapadla
            baseCycleMs = when (hundredTier) {
                0 -> 840L
                1 -> 780L
                2 -> 735L
                3 -> 700L
                4 -> 675L
                else -> 650L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 410L
                1 -> 380L
                2 -> 360L
                3 -> 345L
                4 -> 335L
                else -> 325L
            }
        }

        val fullCycle = baseCycleMs - ((baseCycleMs - peakCycleMs) * speedStep) / 19L
        return (fullCycle / 4L).coerceAtLeast(80L)
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
                subTickCounter = 0
                currentSlot = 0
                grabCooldownSlots = 0
                for (i in 0..3) {
                    tentacleExtending[i] = i % 2 == 0
                    tentaclePauseSlots[i] = i % 2
                }
                state = state.copy(
                    caughtAnimFrame = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
                    // Po ztrate potapece zacina dalsi potapec pripraveny na zebriku (pos = 1)
                    diverPos = if (nowGameOver) 0 else 1,
                    bagGoldCount = 0,
                    grabbingAtChest = false,
                    tentacleLengths = listOf(1, 1, 0, 0),
                    unloadBonusTicks = 0,
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

        if (grabCooldownSlots > 0) {
            grabCooldownSlots--
        }

        // Po vylozeni pokladu na palube lodi (diverPos == 0) potapec automaticky
        // sestoupi na zebrik (diverPos = 1), jakmile dobehne kratke zobrazeni bonusu
        val nextUnloadTicks = if (state.unloadBonusTicks > 0) state.unloadBonusTicks - 1 else 0
        val autoStepDownToLadder = state.diverPos == 0 && state.unloadBonusTicks == 1

        // V kazdem dilcim taktu (currentSlot 0..3) se pohne odpovidajici chapadlo chobotnice (0..3)
        val tIdx = currentSlot
        val maxLen = MAX_TENTACLE_LEN[tIdx]
        val currentLengths = state.tentacleLengths.toMutableList()
        var curLen = currentLengths[tIdx]
        val dangerDiverPos = tIdx + 2
        var caughtNow = false

        if (tentaclePauseSlots[tIdx] > 0) {
            tentaclePauseSlots[tIdx]--
        } else {
            if (tentacleExtending[tIdx]) {
                if (curLen < maxLen) {
                    val nextLen = curLen + 1
                    // Ochrana proti neresitelne pasti: nenechame plne natahnout vsechna sousedni chapadla najednou
                    val fullyExtendedCount = (0..3).count { idx ->
                        idx != tIdx && currentLengths[idx] >= MAX_TENTACLE_LEN[idx]
                    }
                    val maxAllowedFull = if (state.mode == GameMode.GAME_A) 1 else 2

                    if (nextLen == maxLen && fullyExtendedCount >= maxAllowedFull) {
                        // Chapadlo na chvili zavaha tesne pred dnem a zacne couvat
                        tentacleExtending[tIdx] = false
                        curLen = (curLen - 1).coerceAtLeast(0)
                        currentLengths[tIdx] = curLen
                    } else {
                        // DULEZITE: Kdyz chapadlo prave dosahne sve maximalni delky (nextLen == maxLen),
                        // pouze se rozsviti nad hlavou potapece jako varovani!
                        // Potapece chyti az tehdy, kdyz pod nim potapec zustane stat do dalsiho taktu.
                        curLen = nextLen
                        currentLengths[tIdx] = curLen
                    }
                } else {
                    // Chapadlo UZ BYLO plne natazene (curLen == maxLen) z predchoziho cyklu!
                    // Pokud pod nim potapec porad stoji, chobotnice ho v tomto taktu popadne.
                    if (state.diverPos == dangerDiverPos) {
                        caughtNow = true
                    } else {
                        tentacleExtending[tIdx] = false
                        curLen = (curLen - 1).coerceAtLeast(0)
                        currentLengths[tIdx] = curLen
                    }
                }
            } else {
                // Chapadlo se zatahuje zpet k hlave chobotnice
                if (curLen > 0) {
                    curLen--
                    currentLengths[tIdx] = curLen

                    // Autenticky klamavy pohyb (feint) z IM-03:
                    // Chapadlo se obcas nevrati az do 0, ale v polovine se zastavi a znovu vyrazi dolu!
                    val withinHundred = state.score % 100
                    val feintChance = if (state.mode == GameMode.GAME_A) {
                        if (withinHundred < 40) 0.12f else 0.25f
                    } else {
                        if (withinHundred < 40) 0.24f else 0.38f
                    }
                    if (curLen >= 1 && Random.nextFloat() < feintChance) {
                        tentacleExtending[tIdx] = true
                    }
                } else {
                    // Chapadlo je plne zatazene u hlavy (curLen == 0)
                    val withinHundred = state.score % 100
                    val pauseChance = if (state.mode == GameMode.GAME_A) {
                        if (withinHundred < 50) 0.50f else 0.25f
                    } else {
                        if (withinHundred < 50) 0.25f else 0.10f
                    }
                    if (Random.nextFloat() < pauseChance) {
                        tentaclePauseSlots[tIdx] = 1
                    }
                    tentacleExtending[tIdx] = true
                }
            }
        }

        state = state.copy(
            diverPos = if (autoStepDownToLadder) 1 else state.diverPos,
            tentacleLengths = currentLengths,
            grabbingAtChest = if (grabCooldownSlots == 0) false else state.grabbingAtChest,
            unloadBonusTicks = nextUnloadTicks,
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
        grabCooldownSlots = 0
        state = state.copy(
            lostDivers = nextLost,
            caughtAnimFrame = 0,
            grabbingAtChest = false,
            unloadBonusTicks = 0
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

        // Na 200 a 500 bodech (v kazde tisicovce) se obnovi ztraceni potapeci
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

        // Pri prechodu pres celou stovku (100, 200, 300...) kratce pozdrzime chapadla pro oddech
        if ((prevScore / 100) < (newScore / 100)) {
            for (i in 0..3) {
                tentacleExtending[i] = false
                tentaclePauseSlots[i] = 1
            }
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
