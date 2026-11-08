package cz.mares.jenpockej

import kotlin.random.Random

data class JumperCosmonaut(
    val step: Int,              // 0..9 (odrazove body nad mostem jsou 2, 5, 8; 9 = pristani u vozitka)
    val fromLowerHatch: Boolean,// false = horni rampa, true = spodni rampa (ve Hre B)
    val phaseSlot: Int,         // 0..3 dilci takt
    val caughtOnBridge: Boolean = false
)

data class BridgeLavaCrash(
    val zone: Int,              // 0 = leva propast, 1 = stredni propast, 2 = prava propast
    val frame: Int              // 0..3 animace dopadu do lavy
)

data class SpaceBridgeSnapshot(
    val mode: GameMode = GameMode.GAME_A,
    val isRunning: Boolean = false,
    val isDemoMode: Boolean = true,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val score: Int = 0,
    val highScoreA: Int = 0,
    val highScoreB: Int = 0,
    val bridgePos: Int = 0,     // 0 = vlevo, 1 = stred, 2 = vpravo
    val jumpers: List<JumperCosmonaut> = listOf(
        JumperCosmonaut(step = 1, fromLowerHatch = false, phaseSlot = 0),
        JumperCosmonaut(step = 6, fromLowerHatch = false, phaseSlot = 2)
    ),
    val satelliteVisible: Boolean = true,
    val roverBoardingTicks: Int = 0,
    val penaltyHalfPoints: Int = 0, // 0..6 (2 pulbody = 1 cela prilba, max 3 prilby)
    val crashAnim: BridgeLavaCrash? = null,
    val blinkPhase: Boolean = false
)

class SpaceBridgeGameEngine(private val media: SoundPlayer) {
    companion object {
        private const val KEY_BRIDGE_HIGH_A = "bridge_high_score_a"
        private const val KEY_BRIDGE_HIGH_B = "bridge_high_score_b"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"

        val BOUNCE_STEPS = intArrayOf(2, 5, 8)
    }

    var state = SpaceBridgeSnapshot(
        soundEnabled = media.loadBool(KEY_SOUND, true),
        hapticsEnabled = media.loadBool(KEY_HAPTICS, true),
        highScoreA = media.loadInt(KEY_BRIDGE_HIGH_A, 0),
        highScoreB = media.loadInt(KEY_BRIDGE_HIGH_B, 0)
    )
        private set

    private var subTickCounter = 0
    private var currentSlot = 0
    private var satelliteSlotTimer = 30
    private var spawnCooldownSlots = 0

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
        satelliteSlotTimer = 32
        spawnCooldownSlots = 8

        state = state.copy(
            mode = mode,
            isRunning = true,
            isDemoMode = false,
            isGameOver = false,
            score = 0,
            bridgePos = 0,
            jumpers = listOf(
                JumperCosmonaut(step = 0, fromLowerHatch = false, phaseSlot = 0)
            ),
            satelliteVisible = false,
            roverBoardingTicks = 0,
            penaltyHalfPoints = 0,
            crashAnim = null
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
        spawnCooldownSlots = 4
        state = state.copy(
            isRunning = false,
            isDemoMode = true,
            isGameOver = false,
            score = 0,
            bridgePos = 0,
            jumpers = listOf(
                JumperCosmonaut(step = 1, fromLowerHatch = false, phaseSlot = 0),
                JumperCosmonaut(step = 6, fromLowerHatch = false, phaseSlot = 2)
            ),
            satelliteVisible = true,
            roverBoardingTicks = 0,
            penaltyHalfPoints = 0,
            crashAnim = null
        )
    }

    fun moveBridgeLeft() {
        moveBridgeTo((state.bridgePos - 1).coerceAtLeast(0))
    }

    fun moveBridgeRight() {
        moveBridgeTo((state.bridgePos + 1).coerceAtMost(2))
    }

    private fun moveBridgeTo(targetZone: Int) {
        if (state.isDemoMode && !state.isRunning) {
            state = state.copy(bridgePos = targetZone)
            return
        }
        if (!state.isRunning || state.isGameOver || state.crashAnim != null) return

        val changed = state.bridgePos != targetZone
        state = state.copy(bridgePos = targetZone)

        // Pokud v cilove zone (0, 1 nebo 2) prave dopadl kosmonaut na odrazovy bod (step 2, 5 nebo 8),
        // pristavenim mostu ho okamzite zachytime a odrazime dal!
        val bounceStepForZone = BOUNCE_STEPS[targetZone]
        val waitingAtBounce = state.jumpers.any {
            it.step == bounceStepForZone && !it.caughtOnBridge
        }
        if (waitingAtBounce) {
            var bouncedCount = 0
            val updated = state.jumpers.map { j ->
                if (j.step == bounceStepForZone && !j.caughtOnBridge) {
                    bouncedCount++
                    j.copy(step = j.step + 1, caughtOnBridge = false)
                } else {
                    j
                }
            }
            awardBridgePoints(bouncedCount, updated)
            return
        }

        if (changed) {
            if (state.soundEnabled) {
                media.play(SoundEffect.BUTTON)
            }
            if (state.hapticsEnabled) {
                media.vibrate(HapticType.MOVE)
            }
        }
    }

    private fun zoneForBounceStep(step: Int): Int = when (step) {
        2 -> 0
        5 -> 1
        8 -> 2
        else -> -1
    }

    /**
     * Pilovita krivka rychlosti po stovkach bodu (0..99, 100..199, 200..299...)
     * se skokovym zpomalenim na kazde cele stovce bodu.
     */
    fun currentTickDelayMs(): Long {
        if (state.isGameOver) return 450L
        if (state.isDemoMode && !state.isRunning) return 210L
        if (state.crashAnim != null) return 260L

        val hundredTier = (state.score / 100).coerceAtMost(5)
        val withinHundred = state.score % 100
        val speedStep = (withinHundred / 5).coerceIn(0, 19)

        val baseCycleMs: Long
        val peakCycleMs: Long

        if (state.mode == GameMode.GAME_A) {
            baseCycleMs = when (hundredTier) {
                0 -> 980L
                1 -> 910L
                2 -> 850L
                3 -> 800L
                else -> 760L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 480L
                1 -> 440L
                2 -> 415L
                else -> 385L
            }
        } else {
            baseCycleMs = when (hundredTier) {
                0 -> 840L
                1 -> 780L
                2 -> 735L
                else -> 690L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 410L
                1 -> 380L
                else -> 345L
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

        val currentCrash = state.crashAnim
        if (currentCrash != null) {
            if (currentCrash.frame < 3) {
                state = state.copy(
                    crashAnim = currentCrash.copy(frame = currentCrash.frame + 1),
                    blinkPhase = !state.blinkPhase
                )
            } else {
                val nowGameOver = state.penaltyHalfPoints >= 6
                subTickCounter = 0
                currentSlot = 0
                spawnCooldownSlots = 8
                state = state.copy(
                    crashAnim = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
                    bridgePos = 0,
                    roverBoardingTicks = 0,
                    jumpers = if (nowGameOver) {
                        emptyList()
                    } else {
                        listOf(JumperCosmonaut(step = 0, fromLowerHatch = false, phaseSlot = 0))
                    },
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

        // Casovac orbitalni druzice nahore
        satelliteSlotTimer--
        var satNow = state.satelliteVisible
        if (satelliteSlotTimer <= 0) {
            satNow = !satNow
            satelliteSlotTimer = if (satNow) Random.nextInt(20, 36) else Random.nextInt(40, 68)
        }

        val updatedJumpers = ArrayList<JumperCosmonaut>()
        var gainedPoints = 0
        var droppedZone: Int? = null
        var movedStepSound: Int? = null
        var boardedRoverNow = false

        for (jumper in state.jumpers) {
            val bZone = zoneForBounceStep(jumper.step)
            if (bZone >= 0 && jumper.caughtOnBridge) {
                gainedPoints++
                updatedJumpers.add(
                    jumper.copy(
                        step = jumper.step + 1,
                        caughtOnBridge = false
                    )
                )
                continue
            }

            if (jumper.phaseSlot != currentSlot) {
                val lockedNow = jumper.caughtOnBridge ||
                    (bZone >= 0 && state.bridgePos == bZone)
                updatedJumpers.add(jumper.copy(caughtOnBridge = lockedNow))
            } else {
                movedStepSound = jumper.step % 4
                if (bZone >= 0) {
                    // Kosmonaut na odrazovem bode (step 2, 5 nebo 8) dokoncil cekani nad propasti
                    if (jumper.caughtOnBridge || state.bridgePos == bZone) {
                        gainedPoints++
                        updatedJumpers.add(
                            jumper.copy(
                                step = jumper.step + 1,
                                caughtOnBridge = false
                            )
                        )
                    } else {
                        droppedZone = bZone
                    }
                } else if (jumper.step < 9) {
                    val nextStep = jumper.step + 1
                    val nextZone = zoneForBounceStep(nextStep)
                    val preCaught = nextZone >= 0 && state.bridgePos == nextZone
                    updatedJumpers.add(
                        jumper.copy(
                            step = nextStep,
                            caughtOnBridge = jumper.caughtOnBridge || preCaught
                        )
                    )
                } else {
                    // step == 9 -> Kosmonaut bezpecne naskocil do lunarniho vozitka vpravo (+1 bod)
                    gainedPoints++
                    boardedRoverNow = true
                }
            }
        }

        if (droppedZone != null) {
            val penaltyAdd = if (satNow) 1 else 2
            val newPenalty = (state.penaltyHalfPoints + penaltyAdd).coerceAtMost(6)
            state = state.copy(
                jumpers = emptyList(),
                satelliteVisible = satNow,
                roverBoardingTicks = 0,
                penaltyHalfPoints = newPenalty,
                crashAnim = BridgeLavaCrash(zone = droppedZone, frame = 0),
                blinkPhase = nextBlink
            )
            if (state.soundEnabled) {
                media.play(SoundEffect.MISS)
            }
            if (state.hapticsEnabled) {
                media.vibrate(HapticType.MISS)
            }
            return
        }

        if (spawnCooldownSlots > 0) {
            spawnCooldownSlots--
        }

        val withinHundred = state.score % 100
        val maxSimultaneous = if (state.mode == GameMode.GAME_A) {
            when {
                state.score < 4 -> 1
                withinHundred < 25 -> 2
                else -> 3
            }
        } else {
            when {
                state.score < 3 -> 2
                withinHundred < 30 -> 3
                else -> 4
            }
        }

        // Novy kosmonaut vyskoci ze zakladny pouze ve volnem taktu (slotFree)
        // a pouze tehdy, kdyz zadny jiny kosmonaut nema step % 3 == 0 (aby nikdy nedopadli dva najednou do ruznych zon!)
        val slotFree = updatedJumpers.none { it.phaseSlot == currentSlot }
        val noBounceConflict = updatedJumpers.none { it.step % 3 == 0 || it.step <= 1 }
        if (
            slotFree &&
            noBounceConflict &&
            (updatedJumpers.isEmpty() || (updatedJumpers.size < maxSimultaneous && spawnCooldownSlots == 0))
        ) {
            val fromLower = state.mode == GameMode.GAME_B && Random.nextBoolean()
            // Spodni komora ve Hre B vyskakuje rovnou do step = 1 (kratsi oblouk do 1. zony)
            val startStep = if (fromLower && updatedJumpers.none { it.step % 3 == 1 }) 1 else 0
            updatedJumpers.add(
                JumperCosmonaut(
                    step = startStep,
                    fromLowerHatch = fromLower,
                    phaseSlot = currentSlot
                )
            )
            spawnCooldownSlots = if (state.mode == GameMode.GAME_A) 7 else 5
            if (movedStepSound == null) movedStepSound = 0
        }

        val nextRoverTicks = when {
            boardedRoverNow -> 5
            state.roverBoardingTicks > 0 -> state.roverBoardingTicks - 1
            else -> 0
        }

        state = state.copy(
            satelliteVisible = satNow,
            roverBoardingTicks = nextRoverTicks,
            blinkPhase = nextBlink
        )

        if (gainedPoints > 0) {
            awardBridgePoints(gainedPoints, updatedJumpers)
        } else {
            state = state.copy(jumpers = updatedJumpers)
            if (state.soundEnabled && movedStepSound != null) {
                val tickSound = when (movedStepSound) {
                    0 -> SoundEffect.TICK_RAMP_0
                    1 -> SoundEffect.TICK_RAMP_1
                    2 -> SoundEffect.TICK_RAMP_2
                    else -> SoundEffect.TICK_RAMP_3
                }
                media.play(tickSound)
            }
        }
    }

    private fun awardBridgePoints(points: Int, updatedJumpers: List<JumperCosmonaut>) {
        val prevScore = state.score
        val newScore = (prevScore + points).coerceAtMost(9999)
        var newPenalty = state.penaltyHalfPoints
        var bonusTriggered = false

        val prevInThousand = prevScore % 1000
        val newInThousand = newScore % 1000
        if (
            (prevInThousand < 200 && newInThousand >= 200) ||
            (prevInThousand < 500 && newInThousand >= 500) ||
            (prevScore < 1000 && newScore >= 1000)
        ) {
            newPenalty = 0
            bonusTriggered = true
        }

        if ((prevScore / 100) < (newScore / 100)) {
            spawnCooldownSlots = 8
        }

        var bestA = state.highScoreA
        var bestB = state.highScoreB
        if (state.mode == GameMode.GAME_A && newScore > bestA) {
            bestA = newScore
            media.saveInt(KEY_BRIDGE_HIGH_A, bestA)
        }
        if (state.mode == GameMode.GAME_B && newScore > bestB) {
            bestB = newScore
            media.saveInt(KEY_BRIDGE_HIGH_B, bestB)
        }

        state = state.copy(
            score = newScore,
            highScoreA = bestA,
            highScoreB = bestB,
            jumpers = updatedJumpers,
            penaltyHalfPoints = newPenalty
        )

        if (state.soundEnabled) {
            if (bonusTriggered) {
                media.play(SoundEffect.BONUS)
            } else {
                media.play(SoundEffect.CATCH)
            }
        }
        if (state.hapticsEnabled) {
            if (bonusTriggered) {
                media.vibrate(HapticType.BONUS)
            } else {
                media.vibrate(HapticType.CATCH)
            }
        }
    }

    private fun stepDemoTick(nextBlink: Boolean) {
        val nextList = ArrayList<JumperCosmonaut>()
        for (j in state.jumpers) {
            if (j.phaseSlot != currentSlot) {
                nextList.add(j)
            } else if (j.step < 9) {
                nextList.add(j.copy(step = j.step + 1))
            } else {
                nextList.add(j.copy(step = 0))
            }
        }

        val urgentBounce = nextList
            .map { it to zoneForBounceStep(it.step) }
            .firstOrNull { it.second >= 0 }
            ?.second
            ?: nextList
                .map { it to zoneForBounceStep(it.step + 1) }
                .firstOrNull { it.second >= 0 }
                ?.second

        state = state.copy(
            bridgePos = urgentBounce ?: state.bridgePos,
            jumpers = nextList,
            satelliteVisible = true,
            blinkPhase = nextBlink
        )
    }
}
