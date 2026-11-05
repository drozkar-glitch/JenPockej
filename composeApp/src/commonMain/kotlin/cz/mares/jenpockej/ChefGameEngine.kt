package cz.mares.jenpockej

import kotlin.random.Random

data class FoodItem(
    val col: Int,          // 0 = klobasa, 1 = ryba, 2 = kotleta, 3 = volske oko
    val step: Int,         // 0 = nejvyssi bod pod stropem .. 4 = tesne nad panvickou
    val goingUp: Boolean,  // true = stoupa nahoru po odrazu, false = pada dolu
    val phaseSlot: Int,    // 0..3 dilci takt (kazdy sloupec ma vlastni takt)
    val peakStep: Int = 0, // 0 = vysoky vyhoz, 1 = stredni vyhoz, 2 = nizky vyhoz
    val flippedByChef: Boolean = false
)

data class ChefCrashAnim(
    val droppedCol: Int,
    val frame: Int         // 0 = dopad na podlahu, 1..3 = myska utika s jidlem do diry
)

data class ChefSnapshot(
    val mode: GameMode = GameMode.GAME_A,
    val isRunning: Boolean = false,
    val isDemoMode: Boolean = true,
    val isGameOver: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val score: Int = 0,
    val highScoreA: Int = 0,
    val highScoreB: Int = 0,
    val chefPos: Int = 1,                  // 0..2 ve Hre A, 0..3 ve Hre B
    val panFlippingUp: Boolean = false,    // kratke prizvednuti panvicky pri odpalu
    val foods: List<FoodItem> = listOf(
        FoodItem(col = 0, step = 1, goingUp = false, phaseSlot = 0, peakStep = 0),
        FoodItem(col = 1, step = 3, goingUp = true, phaseSlot = 1, peakStep = 0),
        FoodItem(col = 2, step = 2, goingUp = false, phaseSlot = 2, peakStep = 1)
    ),
    val catVisible: Boolean = true,
    val catHoldingFood: Boolean = false,   // kocour vidlickou pridrzel klobasu na col 0, step 0
    val penaltyHalfPoints: Int = 0,        // 0..6 (2 pulbody = 1 cela myska, max 3 mysky)
    val crashAnim: ChefCrashAnim? = null,
    val blinkPhase: Boolean = false
)

class ChefGameEngine(private val media: SoundPlayer) {
    companion object {
        private const val KEY_CHEF_HIGH_A = "chef_high_score_a"
        private const val KEY_CHEF_HIGH_B = "chef_high_score_b"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
    }

    var state = ChefSnapshot(
        soundEnabled = media.loadBool(KEY_SOUND, true),
        hapticsEnabled = media.loadBool(KEY_HAPTICS, true),
        highScoreA = media.loadInt(KEY_CHEF_HIGH_A, 0),
        highScoreB = media.loadInt(KEY_CHEF_HIGH_B, 0)
    )
        private set

    private var subTickCounter = 0
    private var currentSlot = 0
    private var catTimerSlots = 28
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
        catTimerSlots = 24
        spawnCooldownSlots = 8

        state = state.copy(
            mode = mode,
            isRunning = true,
            isDemoMode = false,
            isGameOver = false,
            score = 0,
            chefPos = 1,
            panFlippingUp = false,
            foods = listOf(
                FoodItem(col = 1, step = 0, goingUp = false, phaseSlot = 1, peakStep = 0)
            ),
            catVisible = false,
            catHoldingFood = false,
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
            chefPos = 1,
            panFlippingUp = false,
            foods = listOf(
                FoodItem(col = 0, step = 1, goingUp = false, phaseSlot = 0, peakStep = 0),
                FoodItem(col = 1, step = 3, goingUp = true, phaseSlot = 1, peakStep = 0),
                FoodItem(col = 2, step = 2, goingUp = false, phaseSlot = 2, peakStep = 1)
            ),
            catVisible = true,
            catHoldingFood = false,
            penaltyHalfPoints = 0,
            crashAnim = null
        )
    }

    fun moveChefLeft() {
        moveChefTo((state.chefPos - 1).coerceAtLeast(0))
    }

    fun moveChefRight() {
        val maxCol = if (state.isRunning && state.mode == GameMode.GAME_A) 2 else 3
        moveChefTo((state.chefPos + 1).coerceAtMost(maxCol))
    }

    private fun moveChefTo(targetCol: Int) {
        if (state.isDemoMode && !state.isRunning) {
            state = state.copy(chefPos = targetCol)
            return
        }
        if (!state.isRunning || state.isGameOver || state.crashAnim != null) return

        val changed = state.chefPos != targetCol
        state = state.copy(chefPos = targetCol)

        // Pokud na cilovem sloupci prave ceka jidlo tesne nad panvickou (step == 4, !goingUp),
        // kuchar ho pri prichodu okamzite nadhodi nahoru ve svem sloupci!
        val waitingAtBottom = state.foods.any {
            it.col == targetCol && it.step == 4 && !it.goingUp && !it.flippedByChef
        }
        if (waitingAtBottom) {
            var flippedCount = 0
            val otherFoods = state.foods.filterNot {
                it.col == targetCol && it.step == 4 && !it.goingUp
            }
            val chosenPeak = chooseSafePeakStep(otherFoods)
            val updated = state.foods.map { item ->
                if (item.col == targetCol && item.step == 4 && !item.goingUp && !item.flippedByChef) {
                    flippedCount++
                    item.copy(
                        goingUp = true,
                        step = 3,
                        peakStep = chosenPeak,
                        flippedByChef = false
                    )
                } else {
                    item
                }
            }
            state = state.copy(panFlippingUp = true)
            awardChefPoints(flippedCount, updated)
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

    /**
     * Vybere vysku vyhozeni (peakStep = 0 vysoka, 1 stredni, 2 nizka) podle originalu IM-04 tak,
     * aby dve ruzna jidla nikdy nedopadla na panvicku (step == 4) ve stejnem cyklu.
     */
    private fun chooseSafePeakStep(otherFoods: List<FoodItem>): Int {
        val otherLandingCycles = otherFoods.map { remainingCyclesToLand(it) }.toSet()
        // Ze step = 3 (goingUp = true) trva navrat na step = 4 (goingUp = false):
        // peakStep = 0 -> 7 cyklu (3->2->1->0->1->2->3->4)
        // peakStep = 1 -> 5 cyklu (3->2->1->2->3->4)
        // peakStep = 2 -> 3 cykly (3->2->3->4)
        val withinHundred = state.score % 100
        val allowLowToss = state.mode == GameMode.GAME_B || withinHundred >= 25
        val candidates = if (allowLowToss) listOf(0, 1, 2) else listOf(0, 1)

        val safeCandidates = candidates.filter { peak ->
            val cycles = 7 - 2 * peak
            cycles !in otherLandingCycles
        }
        val pool = if (safeCandidates.isNotEmpty()) safeCandidates else candidates

        val roll = Random.nextFloat()
        return when {
            0 in pool && roll < 0.52f -> 0
            1 in pool && roll < 0.86f -> 1
            else -> pool.random()
        }
    }

    private fun remainingCyclesToLand(item: FoodItem): Int {
        return if (!item.goingUp) {
            (4 - item.step).coerceAtLeast(0)
        } else {
            val upSteps = (item.step - item.peakStep).coerceAtLeast(0)
            val downSteps = 4 - item.peakStep
            upSteps + downSteps
        }
    }

    /**
     * Pilovita krivka rychlosti po stovkach bodu (0..99, 100..199, 200..299...)
     * se skokovym zvolnenim na kazde cele stovce bodu.
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
                0 -> 1000L
                1 -> 920L
                2 -> 860L
                3 -> 810L
                else -> 770L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 490L
                1 -> 450L
                2 -> 420L
                else -> 390L
            }
        } else {
            baseCycleMs = when (hundredTier) {
                0 -> 860L
                1 -> 800L
                2 -> 750L
                else -> 700L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 420L
                1 -> 390L
                else -> 355L
            }
        }

        val fullCycle = baseCycleMs - ((baseCycleMs - peakCycleMs) * speedStep) / 19L
        return (fullCycle / 4L).coerceAtLeast(82L)
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
                    panFlippingUp = false,
                    blinkPhase = !state.blinkPhase
                )
            } else {
                val nowGameOver = state.penaltyHalfPoints >= 6
                val startCol = state.chefPos.coerceIn(0, if (state.mode == GameMode.GAME_A) 2 else 3)
                subTickCounter = 0
                currentSlot = 0
                spawnCooldownSlots = 8
                state = state.copy(
                    crashAnim = null,
                    isGameOver = nowGameOver,
                    isRunning = !nowGameOver,
                    chefPos = startCol,
                    panFlippingUp = false,
                    catHoldingFood = false,
                    foods = if (nowGameOver) {
                        emptyList()
                    } else {
                        listOf(
                            FoodItem(
                                col = startCol,
                                step = 0,
                                goingUp = false,
                                phaseSlot = startCol,
                                peakStep = 0
                            )
                        )
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

        // Casovac kocoura na policce vlevo nahore
        catTimerSlots--
        var catNow = state.catVisible
        if (catTimerSlots <= 0) {
            catNow = !catNow
            catTimerSlots = if (catNow) Random.nextInt(20, 36) else Random.nextInt(36, 60)
        }

        val updatedFoods = ArrayList<FoodItem>()
        var gainedPoints = 0
        var droppedCol: Int? = null
        var movedCol: Int? = null
        var catHeldThisTick = false

        for (item in state.foods) {
            // Pokud jidlo v minulem taktu dopadlo na step == 4 primo na pripravenou panvicku,
            // v tomto taktu se odrazi nahoru ve svem sloupci a zapocita bod
            if (item.step == 4 && !item.goingUp && item.flippedByChef) {
                gainedPoints++
                val others = state.foods.filter { it.col != item.col }
                val nextPeak = chooseSafePeakStep(others)
                updatedFoods.add(
                    item.copy(
                        step = 3,
                        goingUp = true,
                        peakStep = nextPeak,
                        flippedByChef = false
                    )
                )
                continue
            }

            if (item.phaseSlot != currentSlot) {
                val lockedFlip = item.flippedByChef ||
                    (item.step == 4 && !item.goingUp && state.chefPos == item.col)
                updatedFoods.add(item.copy(flippedByChef = lockedFlip))
            } else {
                movedCol = item.col
                if (item.goingUp) {
                    // Jidlo stoupa nahoru az do sve aktualni horni uvrate (peakStep = 0, 1 nebo 2)
                    if (item.step > item.peakStep) {
                        updatedFoods.add(item.copy(step = item.step - 1))
                    } else {
                        // Jidlo dosahlo vrcholu vyhozu -> ve svem sloupci zacina padat zpet dolu
                        updatedFoods.add(
                            item.copy(
                                step = item.step + 1,
                                goingUp = false,
                                flippedByChef = false
                            )
                        )
                    }
                } else {
                    // Jidlo pada dolu: Kocour na policce (catNow) muze v 1. sloupci (col == 0)
                    // na nejvyssi pozici (step == 0) pridrzet klobasu vidlickou o 1 cyklus dele!
                    if (item.col == 0 && item.step == 0 && catNow && !state.catHoldingFood && Random.nextFloat() < 0.42f) {
                        catHeldThisTick = true
                        updatedFoods.add(item)
                    } else if (item.step < 4) {
                        val nextStep = item.step + 1
                        val caughtOnPan = nextStep == 4 && state.chefPos == item.col
                        updatedFoods.add(
                            item.copy(
                                step = nextStep,
                                flippedByChef = item.flippedByChef || caughtOnPan
                            )
                        )
                    } else {
                        // Jidlo na step == 4 dokoncilo cekani nad panvickou
                        if (item.flippedByChef || state.chefPos == item.col) {
                            gainedPoints++
                            val others = state.foods.filter { it.col != item.col }
                            val nextPeak = chooseSafePeakStep(others)
                            updatedFoods.add(
                                item.copy(
                                    step = 3,
                                    goingUp = true,
                                    peakStep = nextPeak,
                                    flippedByChef = false
                                )
                            )
                        } else {
                            droppedCol = item.col
                        }
                    }
                }
            }
        }

        if (droppedCol != null) {
            val penaltyAdd = if (catNow) 1 else 2
            val newPenalty = (state.penaltyHalfPoints + penaltyAdd).coerceAtMost(6)
            state = state.copy(
                foods = emptyList(),
                catVisible = catNow,
                catHoldingFood = false,
                panFlippingUp = false,
                penaltyHalfPoints = newPenalty,
                crashAnim = ChefCrashAnim(droppedCol = droppedCol, frame = 0),
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

        // Postupne zapojovani dalsich sloupcu jidla (ve Hre A sloupce 0..2, ve Hre B sloupce 0..3)
        if (spawnCooldownSlots > 0) {
            spawnCooldownSlots--
        }

        val maxSimultaneousFoods = if (state.mode == GameMode.GAME_A) {
            when {
                state.score < 2 -> 1
                state.score < 8 -> 2
                else -> 3
            }
        } else {
            when {
                state.score < 2 -> 2
                state.score < 8 -> 3
                else -> 4
            }
        }

        val allowedCols = if (state.mode == GameMode.GAME_A) listOf(0, 1, 2) else listOf(0, 1, 2, 3)

        // Kazdy sloupec `c` ma prirazeny svuj vlastni takt `phaseSlot = c`,
        // a nove jidlo se spusti pouze tehdy, kdyz by na panvicku nedopadlo soucasne s jinym jidlem
        if (
            currentSlot in allowedCols &&
            updatedFoods.size < maxSimultaneousFoods &&
            spawnCooldownSlots == 0 &&
            updatedFoods.none { it.col == currentSlot }
        ) {
            val otherLandings = updatedFoods.map { remainingCyclesToLand(it) }.toSet()
            // Nove jidlo na step = 0 dopadne na step = 4 za 4 cykly
            if (4 !in otherLandings && 3 !in otherLandings) {
                updatedFoods.add(
                    FoodItem(
                        col = currentSlot,
                        step = 0,
                        goingUp = false,
                        phaseSlot = currentSlot,
                        peakStep = 0
                    )
                )
                spawnCooldownSlots = if (state.mode == GameMode.GAME_A) 10 else 8
            }
        }

        state = state.copy(
            catVisible = catNow,
            catHoldingFood = if (currentSlot == 0) catHeldThisTick else state.catHoldingFood,
            panFlippingUp = gainedPoints > 0,
            blinkPhase = nextBlink
        )

        if (gainedPoints > 0) {
            awardChefPoints(gainedPoints, updatedFoods)
        } else {
            state = state.copy(foods = updatedFoods)
            if (state.soundEnabled && movedCol != null) {
                val tickSound = when (movedCol) {
                    0 -> SoundEffect.TICK_RAMP_0
                    1 -> SoundEffect.TICK_RAMP_1
                    2 -> SoundEffect.TICK_RAMP_2
                    else -> SoundEffect.TICK_RAMP_3
                }
                media.play(tickSound)
            }
        }
    }

    private fun awardChefPoints(points: Int, updatedFoods: List<FoodItem>) {
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

        var bestA = state.highScoreA
        var bestB = state.highScoreB
        if (state.mode == GameMode.GAME_A && newScore > bestA) {
            bestA = newScore
            media.saveInt(KEY_CHEF_HIGH_A, bestA)
        }
        if (state.mode == GameMode.GAME_B && newScore > bestB) {
            bestB = newScore
            media.saveInt(KEY_CHEF_HIGH_B, bestB)
        }

        state = state.copy(
            score = newScore,
            highScoreA = bestA,
            highScoreB = bestB,
            foods = updatedFoods,
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
        val nextFoods = ArrayList<FoodItem>()
        var flippedNow = false
        for (item in state.foods) {
            if (item.phaseSlot != currentSlot) {
                nextFoods.add(item)
            } else if (item.goingUp) {
                if (item.step > item.peakStep) {
                    nextFoods.add(item.copy(step = item.step - 1))
                } else {
                    nextFoods.add(item.copy(step = item.step + 1, goingUp = false))
                }
            } else {
                if (item.step < 4) {
                    nextFoods.add(item.copy(step = item.step + 1))
                } else {
                    flippedNow = true
                    nextFoods.add(item.copy(step = 3, goingUp = true, peakStep = item.col % 2))
                }
            }
        }

        val urgent = nextFoods.filter { !it.goingUp }.maxByOrNull { it.step }
        val autoChef = urgent?.col ?: state.chefPos

        state = state.copy(
            chefPos = autoChef,
            panFlippingUp = flippedNow,
            foods = nextFoods,
            catVisible = true,
            catHoldingFood = nextBlink,
            blinkPhase = nextBlink
        )
    }
}
