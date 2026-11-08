package cz.mares.jenpockej

import kotlin.random.Random

data class PuckItem(
    val lane: Int,              // 0 = vlevo nahore, 1 = vlevo dole, 2 = vpravo nahore, 3 = vpravo dole
    val step: Int,              // 0..4 (step 4 = tesne pred lapackou/hokejkou brankare)
    val savedByGoalie: Boolean = false,
    val phaseSlot: Int = 0      // 0..3 dilci takt
)

data class GoalConcededAnim(
    val leftSide: Boolean,
    val frame: Int              // 0 = puk v siti, 1..3 = radujici se utocnik / blikajici majak
)

data class HockeySnapshot(
    val mode: GameMode = GameMode.GAME_A,
    val isRunning: Boolean = false,
    val isDemoMode: Boolean = true,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val score: Int = 0,
    val highScoreA: Int = 0,
    val highScoreB: Int = 0,
    val goaliePosition: BasketPosition = BasketPosition.BOTTOM_LEFT,
    val pucks: List<PuckItem> = listOf(PuckItem(lane = 0, step = 1, phaseSlot = 0)),
    val refereeVisible: Boolean = true,
    val refereeArmUp: Boolean = false,
    val penaltyHalfPoints: Int = 0,
    val goalAnim: GoalConcededAnim? = null,
    val blinkPhase: Boolean = false,
    val celebrationTicks: Int = 0
)

class HockeyGameEngine(private val media: SoundPlayer) {
    companion object {
        private const val KEY_HOCKEY_HIGH_A = "hockey_high_score_a"
        private const val KEY_HOCKEY_HIGH_B = "hockey_high_score_b"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
    }

    var state = HockeySnapshot(
        soundEnabled = media.loadBool(KEY_SOUND, true),
        hapticsEnabled = media.loadBool(KEY_HAPTICS, true),
        highScoreA = media.loadInt(KEY_HOCKEY_HIGH_A, 0),
        highScoreB = media.loadInt(KEY_HOCKEY_HIGH_B, 0)
    )
        private set

    private var subTickCounter = 0
    private var currentSlot = 0
    private var refereeSlotTimer = 34
    private var spawnSlotCooldown = 0
    private var lastSpawnedLane = 0
    private var inactiveLaneForGameA = 3

    fun syncAudioSettings(sound: Boolean, haptics: Boolean) {
        state = state.copy(soundEnabled = sound, hapticsEnabled = haptics)
    }

    fun moveGoalie(target: BasketPosition) {
        if (state.isDemoMode && !state.isRunning) {
            state = state.copy(goaliePosition = target)
            if (state.hapticsEnabled) {
                media.vibrate(HapticType.MOVE)
            }
            return
        }

        val changed = state.goaliePosition != target
        state = state.copy(goaliePosition = target)

        if (state.isRunning && !state.isGameOver && state.goalAnim == null) {
            val readyToSave = state.pucks.filter {
                it.step == 4 && (it.lane == target.rampIndex || it.savedByGoalie)
            }
            if (readyToSave.isNotEmpty()) {
                val remaining = state.pucks.filterNot {
                    it.step == 4 && (it.lane == target.rampIndex || it.savedByGoalie)
                }
                awardHockeyPoints(readyToSave.size, remaining)
                return
            }
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
        inactiveLaneForGameA = Random.nextInt(4)
        val firstLane = (0..3).first { mode == GameMode.GAME_B || it != inactiveLaneForGameA }
        lastSpawnedLane = firstLane
        subTickCounter = 0
        currentSlot = 0
        refereeSlotTimer = 34
        spawnSlotCooldown = 6

        state = state.copy(
            mode = mode,
            isRunning = true,
            isDemoMode = false,
            isGameOver = false,
            score = 0,
            pucks = listOf(PuckItem(lane = firstLane, step = 0, phaseSlot = 0)),
            refereeVisible = false,
            refereeArmUp = false,
            penaltyHalfPoints = 0,
            goalAnim = null,
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
            pucks = listOf(PuckItem(lane = 0, step = 0, phaseSlot = 0)),
            refereeVisible = true,
            penaltyHalfPoints = 0,
            goalAnim = null,
            celebrationTicks = 0
        )
    }

    /**
     * Pilovita krivka rychlosti po stovkach bodu (0..99, 100..199, 200..299...)
     * se skokovym zpomalenim na kazde cele stovce bodu.
     */
    fun currentTickDelayMs(): Long {
        if (state.isGameOver) return 450L
        if (state.isDemoMode && !state.isRunning) return 210L
        if (state.goalAnim != null) return 260L

        val hundredTier = (state.score / 100).coerceAtMost(5)
        val withinHundred = state.score % 100
        val speedStep = (withinHundred / 5).coerceIn(0, 19)

        val baseCycleMs: Long
        val peakCycleMs: Long

        if (state.mode == GameMode.GAME_A) {
            baseCycleMs = when (hundredTier) {
                0 -> 990L
                1 -> 915L
                2 -> 855L
                3 -> 805L
                else -> 750L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 475L
                1 -> 435L
                2 -> 405L
                else -> 365L
            }
        } else {
            baseCycleMs = when (hundredTier) {
                0 -> 835L
                1 -> 775L
                2 -> 735L
                else -> 660L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 405L
                1 -> 375L
                else -> 330L
            }
        }

        val cycleMs = baseCycleMs - ((baseCycleMs - peakCycleMs) * speedStep) / 19L
        return (cycleMs / 4L).coerceAtLeast(80L)
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

        val currentGoal = state.goalAnim
        if (currentGoal != null) {
            if (currentGoal.frame < 3) {
                state = state.copy(
                    goalAnim = currentGoal.copy(frame = currentGoal.frame + 1),
                    blinkPhase = !state.blinkPhase
                )
            } else {
                val nowGameOver = state.penaltyHalfPoints >= 6
                val restartLane = (0..3).first {
                    state.mode == GameMode.GAME_B || it != inactiveLaneForGameA
                }
                currentSlot = 0
                subTickCounter = 0
                spawnSlotCooldown = 6
                state = state.copy(
                    goalAnim = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
                    pucks = if (nowGameOver) {
                        emptyList()
                    } else {
                        listOf(PuckItem(lane = restartLane, step = 0, phaseSlot = 0))
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

        // Casovac rozhodciho u mantinelu
        refereeSlotTimer--
        var refNow = state.refereeVisible
        if (refereeSlotTimer <= 0) {
            refNow = !refNow
            refereeSlotTimer = if (refNow) Random.nextInt(20, 36) else Random.nextInt(42, 70)
        }

        val updatedPucks = ArrayList<PuckItem>()
        var gainedPoints = 0
        var missedLane: Int? = null
        var movedPuckLane: Int? = null

        for (puck in state.pucks) {
            if (puck.step == 4 && puck.savedByGoalie) {
                gainedPoints++
                continue
            }

            if (puck.phaseSlot != currentSlot) {
                val lockedSave = puck.savedByGoalie ||
                    (puck.step == 4 && state.goaliePosition.rampIndex == puck.lane)
                updatedPucks.add(puck.copy(savedByGoalie = lockedSave))
            } else {
                if (puck.step < 4) {
                    val nextStep = puck.step + 1
                    val lockedSave = nextStep == 4 && state.goaliePosition.rampIndex == puck.lane
                    updatedPucks.add(
                        puck.copy(
                            step = nextStep,
                            savedByGoalie = puck.savedByGoalie || lockedSave
                        )
                    )
                    movedPuckLane = puck.lane
                } else {
                    if (puck.savedByGoalie || state.goaliePosition.rampIndex == puck.lane) {
                        gainedPoints++
                    } else {
                        missedLane = puck.lane
                    }
                }
            }
        }

        if (missedLane != null) {
            val penaltyAdd = if (refNow) 1 else 2
            val newPenalty = (state.penaltyHalfPoints + penaltyAdd).coerceAtMost(6)
            val leftGoal = missedLane == 0 || missedLane == 1
            state = state.copy(
                pucks = emptyList(),
                refereeVisible = refNow,
                refereeArmUp = !state.refereeArmUp,
                penaltyHalfPoints = newPenalty,
                goalAnim = GoalConcededAnim(leftSide = leftGoal, frame = 0),
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

        if (state.mode == GameMode.GAME_A && subTickCounter % 80 == 0) {
            val nextInactive = (inactiveLaneForGameA + 1) % 4
            if (updatedPucks.none { it.lane == nextInactive }) {
                inactiveLaneForGameA = nextInactive
            }
        }

        val withinHundred = state.score % 100
        val maxSimultaneousPucks = if (state.mode == GameMode.GAME_A) {
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

        val slotIsFree = updatedPucks.none { it.phaseSlot == currentSlot }
        if (slotIsFree && (updatedPucks.isEmpty() || (updatedPucks.size < maxSimultaneousPucks && spawnSlotCooldown == 0))) {
            val lastSideLeft = lastSpawnedLane == 0 || lastSpawnedLane == 1
            val hasVeryRecent = updatedPucks.any { it.step == 0 }
            val candidateLanes = (0..3).filter { lane ->
                val allowedInMode = state.mode == GameMode.GAME_B || lane != inactiveLaneForGameA
                val freeAtStart = updatedPucks.none { it.lane == lane && it.step <= 1 }
                val sideFair = !hasVeryRecent || ((lane == 0 || lane == 1) == lastSideLeft)
                allowedInMode && freeAtStart && sideFair
            }

            if (candidateLanes.isNotEmpty() && (updatedPucks.isEmpty() || Random.nextFloat() < 0.72f)) {
                val chosen = candidateLanes.random()
                updatedPucks.add(PuckItem(lane = chosen, step = 0, phaseSlot = currentSlot))
                lastSpawnedLane = chosen
                spawnSlotCooldown = minSlotGap
                if (movedPuckLane == null) {
                    movedPuckLane = chosen
                }
            }
        }

        val toggleArm = currentSlot % 2 == 0 && refNow
        state = state.copy(
            refereeVisible = refNow,
            refereeArmUp = if (refNow) (if (toggleArm) !state.refereeArmUp else state.refereeArmUp) else false,
            blinkPhase = nextBlink
        )

        if (gainedPoints > 0) {
            awardHockeyPoints(gainedPoints, updatedPucks)
        } else {
            val nextCel = if (state.celebrationTicks > 0) state.celebrationTicks - 1 else 0
            state = state.copy(
                pucks = updatedPucks,
                celebrationTicks = nextCel
            )
            if (state.soundEnabled && movedPuckLane != null) {
                val tickSound = when (movedPuckLane) {
                    0 -> SoundEffect.TICK_RAMP_0
                    1 -> SoundEffect.TICK_RAMP_1
                    2 -> SoundEffect.TICK_RAMP_2
                    else -> SoundEffect.TICK_RAMP_3
                }
                media.play(tickSound)
            }
        }
    }

    private fun awardHockeyPoints(points: Int, updatedPucks: List<PuckItem>) {
        val prevScore = state.score
        val rawNewScore = prevScore + points
        val reachedThousand = prevScore < 1000 && rawNewScore >= 1000
        val newScore = rawNewScore.coerceAtMost(9999)

        var newPenalty = state.penaltyHalfPoints
        var bonusTriggered = false

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

        if ((prevScore / 100) < (newScore / 100)) {
            spawnSlotCooldown = 6
        }

        val nextCel = when {
            reachedThousand -> 32
            state.celebrationTicks > 0 -> state.celebrationTicks - 1
            else -> 0
        }

        var bestA = state.highScoreA
        var bestB = state.highScoreB
        if (state.mode == GameMode.GAME_A && newScore > bestA) {
            bestA = newScore
            media.saveInt(KEY_HOCKEY_HIGH_A, bestA)
        }
        if (state.mode == GameMode.GAME_B && newScore > bestB) {
            bestB = newScore
            media.saveInt(KEY_HOCKEY_HIGH_B, bestB)
        }

        state = state.copy(
            score = newScore,
            highScoreA = bestA,
            highScoreB = bestB,
            pucks = updatedPucks,
            penaltyHalfPoints = newPenalty,
            celebrationTicks = nextCel
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
        val nextPucks = ArrayList<PuckItem>()
        for (p in state.pucks) {
            if (p.phaseSlot != currentSlot) {
                nextPucks.add(p)
            } else if (p.step < 4) {
                nextPucks.add(p.copy(step = p.step + 1))
            }
        }

        val slotFree = nextPucks.none { it.phaseSlot == currentSlot }
        if (slotFree && nextPucks.size < 2 && currentSlot % 2 == 0) {
            val lane = Random.nextInt(4)
            if (nextPucks.none { it.lane == lane && it.step <= 1 }) {
                nextPucks.add(PuckItem(lane = lane, step = 0, phaseSlot = currentSlot))
            }
        }

        val mostUrgent = nextPucks.maxByOrNull { it.step }
        val autoGoalie = when (mostUrgent?.lane) {
            0 -> BasketPosition.TOP_LEFT
            1 -> BasketPosition.BOTTOM_LEFT
            2 -> BasketPosition.TOP_RIGHT
            3 -> BasketPosition.BOTTOM_RIGHT
            else -> state.goaliePosition
        }

        state = state.copy(
            goaliePosition = autoGoalie,
            pucks = nextPucks,
            refereeVisible = true,
            refereeArmUp = nextBlink,
            blinkPhase = nextBlink
        )
    }
}
