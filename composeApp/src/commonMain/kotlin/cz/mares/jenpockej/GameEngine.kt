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

class GameEngine(private val onSound: (SoundEffect) -> Unit) {
    var state = GameSnapshot()
        private set

    private var tickCounter = 0
    private var hareTimer = 0
    private var spawnCooldown = 0
    private var inactiveRampForGameA = 3

    fun moveWolf(target: BasketPosition) {
        if (state.wolfPosition != target) {
            state = state.copy(wolfPosition = target)
            if (state.soundEnabled) {
                onSound(SoundEffect.BUTTON)
            }
        }
    }

    fun toggleSound() {
        val next = !state.soundEnabled
        state = state.copy(soundEnabled = next)
        if (next) {
            onSound(SoundEffect.BUTTON)
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
            onSound(SoundEffect.BONUS)
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
            onSound(SoundEffect.BUTTON)
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
                if (nowGameOver && state.soundEnabled) {
                    onSound(SoundEffect.GAME_OVER)
                }
            }
            return
        }

        tickCounter++

        // Logika zajicka s rolnickou
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

        // Posun existujicich vajicek
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

        // Pokud vajicko spadlo mimo kosik
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
                onSound(SoundEffect.MISS)
            }
            return
        }

        // Pricteni skore a kontrola bonusu na 200 a 500 bodech
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

        // Obcasna obmena neaktivni rampy ve Hre A
        if (state.mode == GameMode.GAME_A && tickCounter % 18 == 0) {
            inactiveRampForGameA = (inactiveRampForGameA + 1) % 4
        }

        // Nasazeni noveho vajicka
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

        val bestA = if (state.mode == GameMode.GAME_A && newScore > state.highScoreA) newScore else state.highScoreA
        val bestB = if (state.mode == GameMode.GAME_B && newScore > state.highScoreB) newScore else state.highScoreB

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
                bonusTriggered -> onSound(SoundEffect.BONUS)
                gainedPoints > 0 -> onSound(SoundEffect.CATCH)
                updatedEggs.isNotEmpty() -> {
                    val soundEffect = when (tickCounter % 4) {
                        0 -> SoundEffect.TICK_RAMP_0
                        1 -> SoundEffect.TICK_RAMP_1
                        2 -> SoundEffect.TICK_RAMP_2
                        else -> SoundEffect.TICK_RAMP_3
                    }
                    onSound(soundEffect)
                }
            }
        }
    }
}
