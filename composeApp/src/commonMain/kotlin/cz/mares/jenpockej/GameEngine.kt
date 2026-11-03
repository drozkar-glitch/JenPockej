package cz.mares.jenpockej

import kotlin.random.Random

enum class BasketPosition(val rampIndex: Int) {
    TOP_LEFT(0),
    BOTTOM_LEFT(1),
    TOP_RIGHT(2),
    BOTTOM_RIGHT(3)
}

enum class GameMode(val label: String) {
    GAME_A("HRA A"),
    GAME_B("HRA B")
}

data class EggItem(
    val ramp: Int,
    val step: Int,
    val caughtByWolf: Boolean = false,
    val phaseSlot: Int = 0
)

data class CrashAnimState(
    val leftSide: Boolean,
    val frame: Int
)

data class GameSnapshot(
    val mode: GameMode = GameMode.GAME_A,
    val isRunning: Boolean = false,
    val isDemoMode: Boolean = true,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val score: Int = 0,
    val highScoreA: Int = 0,
    val highScoreB: Int = 0,
    val wolfPosition: BasketPosition = BasketPosition.BOTTOM_LEFT,
    val eggs: List<EggItem> = listOf(EggItem(ramp = 0, step = 1, phaseSlot = 0)),
    val hareVisible: Boolean = true,
    val hareBellUp: Boolean = false,
    val penaltyHalfPoints: Int = 0,
    val crashAnim: CrashAnimState? = null,
    val blinkPhase: Boolean = false,
    val celebrationTicks: Int = 0
)

class GameEngine(private val media: SoundPlayer) {
    companion object {
        private const val KEY_HIGH_A = "high_score_a"
        private const val KEY_HIGH_B = "high_score_b"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
    }

    var state = GameSnapshot(
        soundEnabled = media.loadBool(KEY_SOUND, true),
        hapticsEnabled = media.loadBool(KEY_HAPTICS, true),
        highScoreA = media.loadInt(KEY_HIGH_A, 0),
        highScoreB = media.loadInt(KEY_HIGH_B, 0)
    )
        private set

    // 4 dilci takty (sloty 0..3) tvori 1 cely krok vajicka jako na cipu KB1013VK1-2
    private var subTickCounter = 0
    private var currentSlot = 0
    private var hareSlotTimer = 36
    private var spawnSlotCooldown = 0
    private var lastSpawnedRamp = 0
    private var inactiveRampForGameA = 3

    fun moveWolf(target: BasketPosition) {
        if (state.isDemoMode && !state.isRunning) {
            state = state.copy(wolfPosition = target)
            if (state.hapticsEnabled) {
                media.vibrate(HapticType.MOVE)
            }
            return
        }

        val positionChanged = state.wolfPosition != target
        state = state.copy(wolfPosition = target)

        // Pokud na cilove rampe ceka vajicko na hrane bidylka (step == 4) nebo uz bylo
        // zachyceno na predchozi rampe, okamzite ho zapocitame do kosiku
        if (state.isRunning && !state.isGameOver && state.crashAnim == null) {
            val readyToCollect = state.eggs.filter {
                it.step == 4 && (it.ramp == target.rampIndex || it.caughtByWolf)
            }
            if (readyToCollect.isNotEmpty()) {
                val remainingEggs = state.eggs.filterNot {
                    it.step == 4 && (it.ramp == target.rampIndex || it.caughtByWolf)
                }
                awardPointsAndSave(readyToCollect.size, remainingEggs)
                return
            }
        }

        if (positionChanged) {
            if (state.soundEnabled) {
                media.play(SoundEffect.BUTTON)
            }
            if (state.hapticsEnabled) {
                media.vibrate(HapticType.MOVE)
            }
        }
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
        inactiveRampForGameA = Random.nextInt(4)
        val firstRamp = (0..3).first { mode == GameMode.GAME_B || it != inactiveRampForGameA }
        lastSpawnedRamp = firstRamp
        subTickCounter = 0
        currentSlot = 0
        hareSlotTimer = 36
        // Prvni dalsi vajicko vyjede az po 6 dilcich taktech (1,5 cyklu)
        spawnSlotCooldown = 6

        state = state.copy(
            mode = mode,
            isRunning = true,
            isDemoMode = false,
            isGameOver = false,
            score = 0,
            eggs = listOf(EggItem(ramp = firstRamp, step = 0, phaseSlot = 0)),
            hareVisible = false,
            hareBellUp = false,
            penaltyHalfPoints = 0,
            crashAnim = null,
            celebrationTicks = 0
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
        spawnSlotCooldown = 4
        state = state.copy(
            isRunning = false,
            isDemoMode = true,
            isGameOver = false,
            score = 0,
            eggs = listOf(EggItem(ramp = 0, step = 0, phaseSlot = 0)),
            hareVisible = true,
            penaltyHalfPoints = 0,
            crashAnim = null,
            celebrationTicks = 0
        )
        if (state.soundEnabled) {
            media.play(SoundEffect.BUTTON)
        }
    }

    /**
     * Vraci prodlevu jednoho dilciho taktu (slotu).
     * 4 dilci takty tvori 1 cely krok vajicka.
     * Rychlost sleduje pilovitou krivku po stovkach bodu (0..99, 100..199, 200..299...)
     * presne podle originalniho casovani IM-02.
     */
    fun currentTickDelayMs(): Long {
        if (state.isGameOver) return 450L
        if (state.isDemoMode && !state.isRunning) return 210L
        if (state.crashAnim != null) return 260L

        val cycleMs = fullEggStepCycleMs(state.score, state.mode)
        return (cycleMs / 4L).coerceAtLeast(80L)
    }

    private fun fullEggStepCycleMs(score: Int, mode: GameMode): Long {
        val hundredTier = (score / 100).coerceAtMost(5)
        val withinHundred = score % 100
        // Rychlost se v ramci kazde stovky stupnuje po 5bodicovych stupnich (0..19)
        val speedStep = (withinHundred / 5).coerceIn(0, 19)

        val baseCycleMs: Long
        val peakCycleMs: Long

        if (mode == GameMode.GAME_A) {
            // HRA A: Zacina na 1000 ms/krok, pred stovkou zrychli k 480..360 ms
            // a pri dosazeni kazde cele stovky (100, 200, 300...) skokove zvolni
            baseCycleMs = when (hundredTier) {
                0 -> 1000L
                1 -> 920L
                2 -> 860L
                3 -> 810L
                4 -> 770L
                else -> 740L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 480L
                1 -> 440L
                2 -> 410L
                3 -> 390L
                4 -> 375L
                else -> 360L
            }
        } else {
            // HRA B: Vsechny 4 rampy aktivni, zacina na 840 ms/krok a zrychluje k 410..330 ms
            baseCycleMs = when (hundredTier) {
                0 -> 840L
                1 -> 780L
                2 -> 740L
                3 -> 700L
                4 -> 670L
                else -> 650L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 410L
                1 -> 380L
                2 -> 360L
                3 -> 345L
                else -> 330L
            }
        }

        return baseCycleMs - ((baseCycleMs - peakCycleMs) * speedStep) / 19L
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
                val restartRamp = (0..3).first {
                    state.mode == GameMode.GAME_B || it != inactiveRampForGameA
                }
                currentSlot = 0
                subTickCounter = 0
                spawnSlotCooldown = 6
                state = state.copy(
                    crashAnim = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
                    eggs = if (nowGameOver) {
                        emptyList()
                    } else {
                        listOf(EggItem(ramp = restartRamp, step = 0, phaseSlot = 0))
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

        // Casovac zajicka u okna (pocitano v dilcich taktech)
        hareSlotTimer--
        var hareNow = state.hareVisible
        if (hareSlotTimer <= 0) {
            hareNow = !hareNow
            hareSlotTimer = if (hareNow) {
                Random.nextInt(20, 36)
            } else {
                Random.nextInt(44, 72)
            }
        }

        val updatedEggs = ArrayList<EggItem>()
        var gainedPoints = 0
        var droppedRamp: Int? = null
        var movedEggRamp: Int? = null

        for (egg in state.eggs) {
            // Pokud vajicko uz v minulem taktu dopadlo na 5. pozici (step == 4) nad nastaveny kosik,
            // po kratkem zobrazeni na hrane bidylka ho v nasledujicim taktu zapocitame do skore
            if (egg.step == 4 && egg.caughtByWolf) {
                gainedPoints++
                continue
            }

            if (egg.phaseSlot != currentSlot) {
                // Toto vajicko ma jiny takt – v tomto slotu stoji na miste,
                // ale hlidame, zda pod nej hrac prave nepostavil kosik na 5. pozici
                val lockedNow = egg.caughtByWolf ||
                    (egg.step == 4 && state.wolfPosition.rampIndex == egg.ramp)
                updatedEggs.add(egg.copy(caughtByWolf = lockedNow))
            } else {
                // Prisel takt tohoto vajicka -> posouva se o 1 krok vpred
                if (egg.step < 4) {
                    val nextStep = egg.step + 1
                    val lockedCatch = nextStep == 4 && state.wolfPosition.rampIndex == egg.ramp
                    updatedEggs.add(
                        egg.copy(
                            step = nextStep,
                            caughtByWolf = egg.caughtByWolf || lockedCatch
                        )
                    )
                    movedEggRamp = egg.ramp
                } else {
                    // Vajicko na 5. pozici (step == 4) dokoncilo svuj cely cyklus cekani na hrane
                    if (egg.caughtByWolf || state.wolfPosition.rampIndex == egg.ramp) {
                        gainedPoints++
                    } else {
                        droppedRamp = egg.ramp
                    }
                }
            }
        }

        if (droppedRamp != null) {
            val penaltyAdd = if (hareNow) 1 else 2
            val newPenalty = (state.penaltyHalfPoints + penaltyAdd).coerceAtMost(6)
            val leftCrash = droppedRamp == 0 || droppedRamp == 1
            state = state.copy(
                eggs = emptyList(),
                hareVisible = hareNow,
                hareBellUp = !state.hareBellUp,
                penaltyHalfPoints = newPenalty,
                crashAnim = CrashAnimState(leftSide = leftCrash, frame = 0),
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

        // Ve Hre A se neaktivni 4. rampa obcas vystrida (jen pokud na nove neaktivni rampe neni vajicko)
        if (state.mode == GameMode.GAME_A && subTickCounter % 80 == 0) {
            val nextInactive = (inactiveRampForGameA + 1) % 4
            if (updatedEggs.none { it.ramp == nextInactive }) {
                inactiveRampForGameA = nextInactive
            }
        }

        val withinHundred = state.score % 100
        val maxSimultaneousEggs = if (state.mode == GameMode.GAME_A) {
            when {
                state.score < 5 -> 1
                withinHundred < 12 && state.score < 100 -> 2
                withinHundred < 75 -> 3
                else -> 4
            }
        } else {
            when {
                state.score < 4 -> 2
                withinHundred < 10 && state.score < 100 -> 2
                withinHundred < 45 -> 3
                else -> 4
            }
        }

        // Minimalni rozestup v dilcich taktech (slotech) mezi vypustenim dalsiho vajicka
        val minSlotGap = if (state.mode == GameMode.GAME_A) {
            when {
                withinHundred < 15 -> 6
                withinHundred < 50 -> 5
                else -> 3
            }
        } else {
            when {
                withinHundred < 15 -> 5
                withinHundred < 50 -> 4
                else -> 3
            }
        }

        if (spawnSlotCooldown > 0) {
            spawnSlotCooldown--
        }

        // Nove vajicko muze vyjet pouze tehdy, kdyz je aktualni takt (currentSlot) volny!
        // Tim je 100% zaruceno, ze kazde vajicko na obrazovce ma svuj vlastni takt.
        val slotIsFree = updatedEggs.none { it.phaseSlot == currentSlot }
        if (slotIsFree && (updatedEggs.isEmpty() || (updatedEggs.size < maxSimultaneousEggs && spawnSlotCooldown == 0))) {
            val lastSideLeft = lastSpawnedRamp == 0 || lastSpawnedRamp == 1
            val hasVeryRecentEgg = updatedEggs.any { it.step == 0 }

            val candidateRamps = (0..3).filter { ramp ->
                val allowedInMode = state.mode == GameMode.GAME_B || ramp != inactiveRampForGameA
                val freeAtPerchStart = updatedEggs.none { it.ramp == ramp && it.step <= 1 }
                val sideFair = !hasVeryRecentEgg || ((ramp == 0 || ramp == 1) == lastSideLeft)
                allowedInMode && freeAtPerchStart && sideFair
            }

            if (candidateRamps.isNotEmpty() && (updatedEggs.isEmpty() || Random.nextFloat() < 0.72f)) {
                val chosenRamp = candidateRamps.random()
                updatedEggs.add(
                    EggItem(
                        ramp = chosenRamp,
                        step = 0,
                        phaseSlot = currentSlot
                    )
                )
                lastSpawnedRamp = chosenRamp
                spawnSlotCooldown = minSlotGap
                if (movedEggRamp == null) {
                    movedEggRamp = chosenRamp
                }
            }
        }

        val toggleHareBell = currentSlot % 2 == 0 && hareNow
        state = state.copy(
            hareVisible = hareNow,
            hareBellUp = if (hareNow) (if (toggleHareBell) !state.hareBellUp else state.hareBellUp) else false,
            blinkPhase = nextBlink
        )

        if (gainedPoints > 0) {
            awardPointsAndSave(gainedPoints, updatedEggs)
        } else {
            val nextCelebration = if (state.celebrationTicks > 0) state.celebrationTicks - 1 else 0
            state = state.copy(
                eggs = updatedEggs,
                celebrationTicks = nextCelebration
            )
            // Pipne presne ta rampa, na ktere se v tomto dilcim taktu pohnulo vajicko
            if (state.soundEnabled && movedEggRamp != null) {
                val tickSound = when (movedEggRamp) {
                    0 -> SoundEffect.TICK_RAMP_0
                    1 -> SoundEffect.TICK_RAMP_1
                    2 -> SoundEffect.TICK_RAMP_2
                    else -> SoundEffect.TICK_RAMP_3
                }
                media.play(tickSound)
            }
        }
    }

    private fun awardPointsAndSave(gainedPoints: Int, updatedEggs: List<EggItem>) {
        val prevScore = state.score
        val rawNewScore = prevScore + gainedPoints
        val reachedThousand = prevScore < 1000 && rawNewScore >= 1000
        val newScore = rawNewScore.coerceAtMost(9999)
        var newPenalty = state.penaltyHalfPoints
        var bonusTriggered = false

        // Na 200 a 500 bodech (v kazde tisicovce) se smazou trestna kuratka
        val prevInThousand = prevScore % 1000
        val newInThousand = newScore % 1000
        if (
            (prevInThousand < 200 && newInThousand >= 200) ||
            (prevInThousand < 500 && newInThousand >= 500) ||
            reachedThousand
        ) {
            newPenalty = 0
            bonusTriggered = true
        }

        // Pri prechodu pres celou stovku (100, 200, 300...) pridame kratky oddech pred dalsim vajickem
        if ((prevScore / 100) < (newScore / 100)) {
            spawnSlotCooldown = 6
        }

        val nextCelebration = when {
            reachedThousand -> 32
            state.celebrationTicks > 0 -> state.celebrationTicks - 1
            else -> 0
        }

        var bestA = state.highScoreA
        var bestB = state.highScoreB
        if (state.mode == GameMode.GAME_A && newScore > bestA) {
            bestA = newScore
            media.saveInt(KEY_HIGH_A, bestA)
        }
        if (state.mode == GameMode.GAME_B && newScore > bestB) {
            bestB = newScore
            media.saveInt(KEY_HIGH_B, bestB)
        }

        state = state.copy(
            score = newScore,
            highScoreA = bestA,
            highScoreB = bestB,
            eggs = updatedEggs,
            penaltyHalfPoints = newPenalty,
            celebrationTicks = nextCelebration
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
        val nextEggs = ArrayList<EggItem>()
        for (egg in state.eggs) {
            if (egg.phaseSlot != currentSlot) {
                nextEggs.add(egg)
            } else if (egg.step < 4) {
                nextEggs.add(egg.copy(step = egg.step + 1))
            }
        }

        val slotFree = nextEggs.none { it.phaseSlot == currentSlot }
        if (slotFree && nextEggs.size < 2 && currentSlot % 2 == 0) {
            val ramp = Random.nextInt(4)
            if (nextEggs.none { it.ramp == ramp && it.step <= 1 }) {
                nextEggs.add(EggItem(ramp = ramp, step = 0, phaseSlot = currentSlot))
            }
        }

        val mostUrgentEgg = nextEggs.maxByOrNull { it.step }
        val autoWolf = when (mostUrgentEgg?.ramp) {
            0 -> BasketPosition.TOP_LEFT
            1 -> BasketPosition.BOTTOM_LEFT
            2 -> BasketPosition.TOP_RIGHT
            3 -> BasketPosition.BOTTOM_RIGHT
            else -> state.wolfPosition
        }
        state = state.copy(
            wolfPosition = autoWolf,
            eggs = nextEggs,
            hareVisible = true,
            hareBellUp = nextBlink,
            blinkPhase = nextBlink
        )
    }
}
