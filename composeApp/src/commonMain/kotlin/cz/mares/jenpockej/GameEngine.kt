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
    val step: Int
)

data class CrashAnimState(
    val leftSide: Boolean,
    val frame: Int
)

data class GameSnapshot(
    val mode: GameMode = GameMode.GAME_A,
    val isRunning: Boolean = false,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val score: Int = 0,
    val highScoreA: Int = 0,
    val highScoreB: Int = 0,
    val wolfPosition: BasketPosition = BasketPosition.BOTTOM_LEFT,
    val eggs: List<EggItem> = emptyList(),
    val hareVisible: Boolean = false,
    val hareBellUp: Boolean = false,
    val penaltyHalfPoints: Int = 0,
    val crashAnim: CrashAnimState? = null,
    val blinkPhase: Boolean = false
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
    private var hareTimer = 0
    private var spawnCooldown = 0
    private var inactiveRampForGameA = 3

    fun moveWolf(target: BasketPosition) {
        if (state.wolfPosition != target) {
            state = state.copy(wolfPosition = target)
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
        tickCounter = 0
        hareTimer = 10
        spawnCooldown = 0
        state = state.copy(
            mode = mode,
            isRunning = true,
            isGameOver = false,
            score = 0,
            eggs = listOf(EggItem(ramp = 0, step = 0)),
            hareVisible = false,
            hareBellUp = false,
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
        state = state.copy(
            isRunning = false,
            isGameOver = false,
            score = 0,
            eggs = emptyList(),
            hareVisible = false,
            penaltyHalfPoints = 0,
            crashAnim = null
        )
        if (state.soundEnabled) {
            media.play(SoundEffect.BUTTON)
        }
    }

    fun currentTickDelayMs(): Long {
        if (!state.isRunning || state.isGameOver) return 450L
        if (state.crashAnim != null) return 260L

        val baseDelay = if (state.mode == GameMode.GAME_A) 680L else 540L
        val speedUp = ((state.score % 100) * 3L) + ((state.score / 100) * 45L)
        return (baseDelay - speedUp).coerceIn(220L, 680L)
    }

    fun stepTick() {
        val nextBlink = !state.blinkPhase

        if (!state.isRunning || state.isGameOver) {
            state = state.copy(blinkPhase = nextBlink)
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
                state = state.copy(
                    crashAnim = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
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
                Random.nextInt(5, 10)
            } else {
                Random.nextInt(12, 22)
            }
        }

        val updatedEggs = ArrayList<EggItem>()
        var gainedPoints = 0
        var droppedRamp: Int? = null

        for (egg in state.eggs) {
            if (egg.step < 4) {
                updatedEggs.add(egg.copy(step = egg.step + 1))
            } else {
                if (state.wolfPosition.rampIndex == egg.ramp) {
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

        val prevScore = state.score
        val newScore = (prevScore + gainedPoints).coerceAtMost(999)
        var newPenalty = state.penaltyHalfPoints
        var bonusTriggered = false

        if (gainedPoints > 0 && (newScore == 200 || newScore == 500)) {
            if (newPenalty > 0) {
                newPenalty = 0
                bonusTriggered = true
            }
        }

        if (state.mode == GameMode.GAME_A && tickCounter % 18 == 0) {
            inactiveRampForGameA = (inactiveRampForGameA + 1) % 4
        }

        val maxSimultaneousEggs = when {
            state.mode == GameMode.GAME_A && newScore < 30 -> 2
            state.mode == GameMode.GAME_A && newScore < 100 -> 3
            state.mode == GameMode.GAME_A -> 4
            newScore < 20 -> 2
            newScore < 70 -> 3
            else -> 5
        }

        val minGap = if (state.mode == GameMode.GAME_A && newScore < 80) 2 else 1
        if (spawnCooldown > 0) {
            spawnCooldown--
        }

        if (updatedEggs.isEmpty() || (updatedEggs.size < maxSimultaneousEggs && spawnCooldown == 0)) {
            val candidateRamps = (0..3).filter { ramp ->
                val allowedInMode = state.mode == GameMode.GAME_B || ramp != inactiveRampForGameA
                val freeAtStart = updatedEggs.none { it.ramp == ramp && it.step <= 1 }
                allowedInMode && freeAtStart
            }
            if (candidateRamps.isNotEmpty() && (updatedEggs.isEmpty() || Random.nextFloat() < 0.72f)) {
                val chosenRamp = candidateRamps.random()
                updatedEggs.add(EggItem(ramp = chosenRamp, step = 0))
                spawnCooldown = minGap
            }
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
            hareVisible = hareNow,
            hareBellUp = if (hareNow) !state.hareBellUp else false,
            penaltyHalfPoints = newPenalty,
            blinkPhase = nextBlink
        )

        if (state.soundEnabled) {
            when {
                bonusTriggered -> media.play(SoundEffect.BONUS)
                gainedPoints > 0 -> media.play(SoundEffect.CATCH)
                updatedEggs.isNotEmpty() -> {
                    val soundEffect = when (tickCounter % 4) {
                        0 -> SoundEffect.TICK_RAMP_0
                        1 -> SoundEffect.TICK_RAMP_1
                        2 -> SoundEffect.TICK_RAMP_2
                        else -> SoundEffect.TICK_RAMP_3
                    }
                    media.play(soundEffect)
                }
            }
        }

        if (state.hapticsEnabled) {
            when {
                bonusTriggered -> media.vibrate(HapticType.BONUS)
                gainedPoints > 0 -> media.vibrate(HapticType.CATCH)
            }
        }
    }
}
