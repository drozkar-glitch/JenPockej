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
    val caughtByWolf: Boolean = false
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
    val eggs: List<EggItem> = listOf(EggItem(0, 1)),
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

    private var tickCounter = 0
    private var hareTimer = 10
    private var spawnCooldown = 0
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

        // Pokud je na cilove rampe vajicko na posledni pozici (step == 4) a neprobiha animace padu,
        // okamzite ho zachytime do kosiku, aby hrac mohl hned prebehnout jinam
        if (state.isRunning && !state.isGameOver && state.crashAnim == null) {
            val rescuedCount = state.eggs.count {
                it.ramp == target.rampIndex && it.step == 4 && !it.caughtByWolf
            }
            if (rescuedCount > 0) {
                val remainingEggs = state.eggs.filterNot {
                    it.ramp == target.rampIndex && it.step == 4
                }
                awardPointsAndSave(rescuedCount, remainingEggs)
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
        tickCounter = 0
        hareTimer = 10
        spawnCooldown = 3

        state = state.copy(
            mode = mode,
            isRunning = true,
            isDemoMode = false,
            isGameOver = false,
            score = 0,
            eggs = listOf(EggItem(ramp = firstRamp, step = 0)),
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
        tickCounter = 0
        spawnCooldown = 2
        state = state.copy(
            isRunning = false,
            isDemoMode = true,
            isGameOver = false,
            score = 0,
            eggs = listOf(EggItem(ramp = 0, step = 0)),
            hareVisible = true,
            penaltyHalfPoints = 0,
            crashAnim = null,
            celebrationTicks = 0
        )
        if (state.soundEnabled) {
            media.play(SoundEffect.BUTTON)
        }
    }

    fun currentTickDelayMs(): Long {
        if (state.isGameOver) return 450L
        if (state.isDemoMode && !state.isRunning) return 600L
        if (state.crashAnim != null) return 280L

        // Plynule tempo s odpocinkovymi vlnami po kazde stovce bodu (jako v originalnim IM-02)
        val baseDelay = if (state.mode == GameMode.GAME_A) 820L else 680L
        val minDelay = if (state.mode == GameMode.GAME_A) 380L else 320L
        val withinHundred = (state.score % 100) * 3L
        val hundredTier = (state.score / 100).coerceAtMost(5) * 38L

        return (baseDelay - withinHundred - hundredTier).coerceIn(minDelay, baseDelay)
    }

    fun stepTick() {
        val nextBlink = !state.blinkPhase

        if (state.isGameOver) {
            state = state.copy(blinkPhase = nextBlink)
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
                    blinkPhase = nextBlink
                )
            } else {
                val nowGameOver = state.penaltyHalfPoints >= 6
                val restartRamp = (0..3).first {
                    state.mode == GameMode.GAME_B || it != inactiveRampForGameA
                }
                spawnCooldown = 3
                state = state.copy(
                    crashAnim = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
                    eggs = if (nowGameOver) emptyList() else listOf(EggItem(restartRamp, 0)),
                    blinkPhase = nextBlink
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

        tickCounter++

        hareTimer--
        var hareNow = state.hareVisible
        if (hareTimer <= 0) {
            hareNow = !hareNow
            hareTimer = if (hareNow) {
                Random.nextInt(6, 11)
            } else {
                Random.nextInt(12, 20)
            }
        }

        val updatedEggs = ArrayList<EggItem>()
        var gainedPoints = 0
        var droppedRamp: Int? = null

        for (egg in state.eggs) {
            if (egg.step < 4) {
                val nextStep = egg.step + 1
                // Pokud vlk uz stoji pod rampou ve chvili, kdy vajicko dorazi na konec (step == 4),
                // oznacime ho jako bezpecne zachycene (i kdyby hrac vzapeti prebehl jinam)
                val lockedCatch = nextStep == 4 && state.wolfPosition.rampIndex == egg.ramp
                updatedEggs.add(
                    egg.copy(
                        step = nextStep,
                        caughtByWolf = egg.caughtByWolf || lockedCatch
                    )
                )
            } else {
                if (egg.caughtByWolf || state.wolfPosition.rampIndex == egg.ramp) {
                    gainedPoints++
                } else {
                    droppedRamp = egg.ramp
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

        if (state.mode == GameMode.GAME_A && tickCounter % 24 == 0) {
            inactiveRampForGameA = (inactiveRampForGameA + 1) % 4
        }

        val maxSimultaneousEggs = when {
            state.mode == GameMode.GAME_A && state.score < 25 -> 2
            state.mode == GameMode.GAME_A && state.score < 90 -> 3
            state.mode == GameMode.GAME_A -> 4
            state.score < 20 -> 2
            state.score < 70 -> 3
            else -> 4
        }

        val minGap = when {
            state.mode == GameMode.GAME_A && state.score < 25 -> 3
            state.mode == GameMode.GAME_A && state.score < 120 -> 2
            state.mode == GameMode.GAME_A -> 2
            state.score < 15 -> 3
            state.score < 90 -> 2
            else -> 1
        }

        if (spawnCooldown > 0) {
            spawnCooldown--
        }

        if (updatedEggs.isEmpty() || (updatedEggs.size < maxSimultaneousEggs && spawnCooldown == 0)) {
            val lastSideLeft = lastSpawnedRamp == 0 || lastSpawnedRamp == 1
            val hasTightPreviousEgg = updatedEggs.any { it.step <= 1 }

            val candidateRamps = (0..3).filter { ramp ->
                val allowedInMode = state.mode == GameMode.GAME_B || ramp != inactiveRampForGameA
                val freeAtStart = updatedEggs.none { it.ramp == ramp && it.step <= 1 }
                // Pokud je predchozi vajicko tesne za startem, dalsi vyjede na stejne strane
                val sideFair = !hasTightPreviousEgg || ((ramp == 0 || ramp == 1) == lastSideLeft)
                allowedInMode && freeAtStart && sideFair
            }

            if (candidateRamps.isNotEmpty() && (updatedEggs.isEmpty() || Random.nextFloat() < 0.75f)) {
                val chosenRamp = candidateRamps.random()
                updatedEggs.add(EggItem(ramp = chosenRamp, step = 0))
                lastSpawnedRamp = chosenRamp
                spawnCooldown = minGap
            }
        }

        state = state.copy(
            hareVisible = hareNow,
            hareBellUp = if (hareNow) !state.hareBellUp else false,
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
            if (state.soundEnabled && updatedEggs.isNotEmpty()) {
                val tickSound = when (tickCounter % 4) {
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

        if (
            (prevScore < 200 && newScore >= 200) ||
            (prevScore < 500 && newScore >= 500) ||
            reachedThousand
        ) {
            newPenalty = 0
            bonusTriggered = true
        }

        val nextCelebration = when {
            reachedThousand -> 16
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
        tickCounter++
        val nextEggs = ArrayList<EggItem>()
        for (egg in state.eggs) {
            if (egg.step < 4) {
                nextEggs.add(egg.copy(step = egg.step + 1))
            }
        }
        if (nextEggs.size < 2 && tickCounter % 2 == 0) {
            val ramp = Random.nextInt(4)
            if (nextEggs.none { it.ramp == ramp && it.step <= 1 }) {
                nextEggs.add(EggItem(ramp = ramp, step = 0))
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
