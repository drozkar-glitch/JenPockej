package cz.mares.jenpockej

import kotlin.random.Random

data class SlalomGate(
    val lane: Int,          // 0 = levy pruh, 1 = stredni pruh, 2 = pravy pruh
    val step: Int,          // 0 = horizont .. 4 = tesne pred koly formule
    val phaseSlot: Int      // 0..3 dilci takt
)

data class SlalomCrashAnim(
    val crashLane: Int,
    val frame: Int          // 0..3 animace smyku a koure
)

data class SlalomSnapshot(
    val mode: GameMode = GameMode.GAME_A,
    val isRunning: Boolean = false,
    val isDemoMode: Boolean = true,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val score: Int = 0,
    val highScoreA: Int = 0,
    val highScoreB: Int = 0,
    val carLane: Int = 1,   // 0 = vlevo, 1 = stred, 2 = vpravo
    val gates: List<SlalomGate> = listOf(
        SlalomGate(lane = 0, step = 1, phaseSlot = 0),
        SlalomGate(lane = 2, step = 3, phaseSlot = 2)
    ),
    val marshalVisible: Boolean = true,
    val marshalFlagUp: Boolean = false,
    val penaltyHalfPoints: Int = 0, // 0..6 (2 pulbody = 1 pneumatika, max 3 pneumatiky)
    val crashAnim: SlalomCrashAnim? = null,
    val blinkPhase: Boolean = false
)

class SlalomGameEngine(private val media: SoundPlayer) {
    companion object {
        private const val KEY_SLALOM_HIGH_A = "slalom_high_score_a"
        private const val KEY_SLALOM_HIGH_B = "slalom_high_score_b"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
    }

    var state = SlalomSnapshot(
        soundEnabled = media.loadBool(KEY_SOUND, true),
        hapticsEnabled = media.loadBool(KEY_HAPTICS, true),
        highScoreA = media.loadInt(KEY_SLALOM_HIGH_A, 0),
        highScoreB = media.loadInt(KEY_SLALOM_HIGH_B, 0)
    )
        private set

    private var subTickCounter = 0
    private var currentSlot = 0
    private var marshalTimerSlots = 30
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
        marshalTimerSlots = 32
        spawnCooldownSlots = 4

        state = state.copy(
            mode = mode,
            isRunning = true,
            isDemoMode = false,
            isGameOver = false,
            score = 0,
            carLane = 1,
            gates = listOf(SlalomGate(lane = 0, step = 0, phaseSlot = 0)),
            marshalVisible = false,
            marshalFlagUp = false,
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
            carLane = 1,
            gates = listOf(
                SlalomGate(lane = 0, step = 1, phaseSlot = 0),
                SlalomGate(lane = 2, step = 3, phaseSlot = 2)
            ),
            marshalVisible = true,
            marshalFlagUp = false,
            penaltyHalfPoints = 0,
            crashAnim = null
        )
    }

    fun moveCarLeft() {
        moveCarTo((state.carLane - 1).coerceAtLeast(0))
    }

    fun moveCarRight() {
        moveCarTo((state.carLane + 1).coerceAtMost(2))
    }

    private fun moveCarTo(targetLane: Int) {
        if (state.isDemoMode && !state.isRunning) {
            state = state.copy(carLane = targetLane)
            return
        }
        if (!state.isRunning || state.isGameOver || state.crashAnim != null) return

        val changed = state.carLane != targetLane
        state = state.copy(carLane = targetLane)

        if (changed) {
            if (state.soundEnabled) {
                media.play(SoundEffect.BUTTON)
            }
            if (state.hapticsEnabled) {
                media.vibrate(HapticType.MOVE)
            }
        }
    }

    /**
     * Pilovita krivka rychlosti po stovkach bodu (0..99, 100..199, 200..299...)
     * se skokovym zpomalenim na kazde cele stovce bodu.
     */
    fun currentTickDelayMs(): Long {
        if (state.isGameOver) return 450L
        if (state.isDemoMode && !state.isRunning) return 200L
        if (state.crashAnim != null) return 260L

        val hundredTier = (state.score / 100).coerceAtMost(5)
        val withinHundred = state.score % 100
        val speedStep = (withinHundred / 5).coerceIn(0, 19)

        val baseCycleMs: Long
        val peakCycleMs: Long

        if (state.mode == GameMode.GAME_A) {
            baseCycleMs = when (hundredTier) {
                0 -> 940L
                1 -> 880L
                2 -> 830L
                3 -> 780L
                else -> 740L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 460L
                1 -> 425L
                2 -> 395L
                else -> 365L
            }
        } else {
            baseCycleMs = when (hundredTier) {
                0 -> 810L
                1 -> 755L
                2 -> 715L
                else -> 660L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 395L
                1 -> 365L
                else -> 330L
            }
        }

        val fullCycle = baseCycleMs - ((baseCycleMs - peakCycleMs) * speedStep) / 19L
        return (fullCycle / 4L).coerceAtLeast(78L)
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
                spawnCooldownSlots = 5
                state = state.copy(
                    crashAnim = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
                    carLane = 1,
                    gates = if (nowGameOver) {
                        emptyList()
                    } else {
                        listOf(SlalomGate(lane = 0, step = 0, phaseSlot = 0))
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

        // Casovac tratoveho komisare u veze
        marshalTimerSlots--
        var marshalNow = state.marshalVisible
        if (marshalTimerSlots <= 0) {
            marshalNow = !marshalNow
            marshalTimerSlots = if (marshalNow) Random.nextInt(20, 36) else Random.nextInt(38, 64)
        }

        val updatedGates = ArrayList<SlalomGate>()
        var passedRowsCount = 0
        var crashedLane: Int? = null
        var movedSoundSlot: Int? = null
        val slotsEvaluatedAtBottom = HashSet<Int>()

        for (gate in state.gates) {
            if (gate.phaseSlot != currentSlot) {
                updatedGates.add(gate)
            } else {
                movedSoundSlot = gate.lane
                if (gate.step < 4) {
                    // Kuzel se posouva bliz k formuli (pokud dosahne step == 4, rozsviti se jako varovani!)
                    updatedGates.add(gate.copy(step = gate.step + 1))
                } else {
                    // Kuzel UZ BYL na step == 4 z predchoziho taktu -> nyni miji formuli:
                    // Pokud formule porad stoji v pruhu kuzelu, dochazi k narazu!
                    if (state.carLane == gate.lane) {
                        crashedLane = gate.lane
                    } else if (slotsEvaluatedAtBottom.add(gate.phaseSlot)) {
                        passedRowsCount++
                    }
                }
            }
        }

        if (crashedLane != null) {
            val penaltyAdd = if (marshalNow) 1 else 2
            val newPenalty = (state.penaltyHalfPoints + penaltyAdd).coerceAtMost(6)
            state = state.copy(
                gates = emptyList(),
                marshalVisible = marshalNow,
                marshalFlagUp = !state.marshalFlagUp,
                penaltyHalfPoints = newPenalty,
                crashAnim = SlalomCrashAnim(crashLane = crashedLane, frame = 0),
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

        // Generovani novych slalomovych branek na horizontu (step = 0)
        val slotFree = updatedGates.none { it.phaseSlot == currentSlot }
        val horizonFree = updatedGates.none { it.step == 0 }
        if (slotFree && horizonFree && (updatedGates.isEmpty() || spawnCooldownSlots == 0)) {
            // Hlidame, ktere pruhy jsou obsazene na step == 1, abychom nikdy nevytvorili neprojizdnou stenu
            val blockedNearHorizon = updatedGates.filter { it.step <= 1 }.map { it.lane }.toSet()
            val candidateLanes = (0..2).filter { it !in blockedNearHorizon || blockedNearHorizon.size >= 2 }
            val chosenLane = candidateLanes.random()

            updatedGates.add(
                SlalomGate(
                    lane = chosenLane,
                    step = 0,
                    phaseSlot = currentSlot
                )
            )

            // Ve Hre B (nebo pri vyssim skore ve Hre A) se obcas objevi dvojita branka (2 kuzele v 1 rade, 1 pruh volny)
            val doubleGateChance = if (state.mode == GameMode.GAME_B) 0.45f else if (state.score >= 40) 0.22f else 0f
            if (Random.nextFloat() < doubleGateChance) {
                val secondCandidates = (0..2).filter { it != chosenLane && it !in blockedNearHorizon }
                if (secondCandidates.isNotEmpty()) {
                    updatedGates.add(
                        SlalomGate(
                            lane = secondCandidates.random(),
                            step = 0,
                            phaseSlot = currentSlot
                        )
                    )
                }
            }

            val withinHundred = state.score % 100
            spawnCooldownSlots = if (state.mode == GameMode.GAME_A) {
                when {
                    withinHundred < 20 -> 5
                    withinHundred < 60 -> 4
                    else -> 3
                }
            } else {
                when {
                    withinHundred < 20 -> 4
                    else -> 3
                }
            }
            if (movedSoundSlot == null) movedSoundSlot = chosenLane
        }

        val toggleFlag = currentSlot % 2 == 0 && marshalNow
        state = state.copy(
            marshalVisible = marshalNow,
            marshalFlagUp = if (marshalNow) (if (toggleFlag) !state.marshalFlagUp else state.marshalFlagUp) else false,
            blinkPhase = nextBlink
        )

        if (passedRowsCount > 0) {
            awardSlalomPoints(passedRowsCount, updatedGates)
        } else {
            state = state.copy(gates = updatedGates)
            if (state.soundEnabled && movedSoundSlot != null) {
                val tickSound = when (movedSoundSlot) {
                    0 -> SoundEffect.TICK_RAMP_0
                    1 -> SoundEffect.TICK_RAMP_1
                    else -> SoundEffect.TICK_RAMP_2
                }
                media.play(tickSound)
            }
        }
    }

    private fun awardSlalomPoints(points: Int, updatedGates: List<SlalomGate>) {
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
            spawnCooldownSlots = 6
        }

        var bestA = state.highScoreA
        var bestB = state.highScoreB
        if (state.mode == GameMode.GAME_A && newScore > bestA) {
            bestA = newScore
            media.saveInt(KEY_SLALOM_HIGH_A, bestA)
        }
        if (state.mode == GameMode.GAME_B && newScore > bestB) {
            bestB = newScore
            media.saveInt(KEY_SLALOM_HIGH_B, bestB)
        }

        state = state.copy(
            score = newScore,
            highScoreA = bestA,
            highScoreB = bestB,
            gates = updatedGates,
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
        val nextGates = ArrayList<SlalomGate>()
        for (g in state.gates) {
            if (g.phaseSlot != currentSlot) {
                nextGates.add(g)
            } else if (g.step < 4) {
                nextGates.add(g.copy(step = g.step + 1))
            }
        }

        if (nextGates.none { it.phaseSlot == currentSlot } && nextGates.size < 2 && currentSlot % 2 == 0) {
            nextGates.add(SlalomGate(lane = Random.nextInt(3), step = 0, phaseSlot = currentSlot))
        }

        val blockedAtBottom = nextGates.filter { it.step >= 3 }.map { it.lane }.toSet()
        val safeCar = if (state.carLane in blockedAtBottom) {
            (0..2).firstOrNull { it !in blockedAtBottom } ?: state.carLane
        } else {
            state.carLane
        }

        state = state.copy(
            carLane = safeCar,
            gates = nextGates,
            marshalVisible = true,
            marshalFlagUp = nextBlink,
            blinkPhase = nextBlink
        )
    }
}
