package cz.mares.jenpockej

import kotlin.random.Random

data class FoodItem(
    val col: Int,          // 0 = klobasa, 1 = ryba, 2 = kotleta, 3 = volske oko
    val step: Int,         // 0 = nejvyssi bod pod stropem .. 4 = tesne nad panvickou
    val goingUp: Boolean,  // true = leti nahoru po odrazu z panvicky, false = pada dolu
    val phaseSlot: Int,    // 0..3 dilci takt
    val flippedByChef: Boolean = false
)

data class ChefCrashAnim(
    val droppedCol: Int,
    val frame: Int         // 0 = dopad na podlahu, 1..3 = myska utika s jidlem
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
    val chefPos: Int = 1,                  // 0..3 pozice panvicky kuchare
    val panFlippingUp: Boolean = false,    // kratke prizvednuti panvicky pri odpalu
    val foods: List<FoodItem> = listOf(
        FoodItem(col = 1, step = 1, goingUp = false, phaseSlot = 0),
        FoodItem(col = 2, step = 2, goingUp = true, phaseSlot = 2)
    ),
    val catVisible: Boolean = true,
    val catHoldingFood: Boolean = false,   // kocour tlapkou pridrzel jidlo v sloupci 0
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
        catTimerSlots = 28
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
                FoodItem(col = 1, step = 0, goingUp = false, phaseSlot = 0)
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
                FoodItem(col = 0, step = 1, goingUp = false, phaseSlot = 0),
                FoodItem(col = 2, step = 2, goingUp = true, phaseSlot = 2)
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
        val maxCol = if (state.isRunning && state.mode == GameMode.GAME_A && state.score < 20) 3 else 3
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
        // kuchar ho pri prichodu okamzite nadhodi nahoru!
        val waitingAtBottom = state.foods.any {
            it.col == targetCol && it.step == 4 && !it.goingUp && !it.flippedByChef
        }
        if (waitingAtBottom) {
            var flippedCount = 0
            val updated = state.foods.map { item ->
                if (item.col == targetCol && item.step == 4 && !item.goingUp && !item.flippedByChef) {
                    flippedCount++
                    item.copy(goingUp = true, step = 3, flippedByChef = false)
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
                0 -> 980L
                1 -> 910L
                2 -> 850L
                3 -> 800L
                else -> 760L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 470L
                1 -> 435L
                2 -> 410L
                else -> 380L
            }
        } else {
            baseCycleMs = when (hundredTier) {
                0 -> 830L
                1 -> 770L
                2 -> 730L
                else -> 680L
            }
            peakCycleMs = when (hundredTier) {
                0 -> 400L
                1 -> 375L
                else -> 340L
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
                    panFlippingUp = false,
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
                    panFlippingUp = false,
                    catHoldingFood = false,
                    foods = if (nowGameOver) {
                        emptyList()
                    } else {
                        listOf(FoodItem(col = state.chefPos, step = 0, goingUp = false, phaseSlot = 0))
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
            catTimerSlots = if (catNow) Random.nextInt(18, 34) else Random.nextInt(36, 64)
        }

        val updatedFoods = ArrayList<FoodItem>()
        var gainedPoints = 0
        var droppedCol: Int? = null
        var movedCol: Int? = null
        var catHeldThisTick = false

        for (item in state.foods) {
            // Pokud jidlo v minulem taktu dopadlo na step == 4 primo na pripravenou panvicku,
            // v tomto taktu se odrazi nahoru a zapocita bod
            if (item.step == 4 && !item.goingUp && item.flippedByChef) {
                gainedPoints++
                updatedFoods.add(
                    item.copy(
                        step = 3,
                        goingUp = true,
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
                    if (item.step > 0) {
                        updatedFoods.add(item.copy(step = item.step - 1))
                    } else {
                        // Jidlo dosahlo vrcholu oblouku pod stropem (step == 0) -> zacina padat dolu.
                        // Obcas pri preklopeni pod stropem preskoci do vedlejsiho volneho sloupce,
                        // aby musel kuchar behani po kuchyni neustale kombinovat!
                        val allowedCols = if (state.mode == GameMode.GAME_A && state.score < 30) {
                            listOf(0, 1, 2)
                        } else {
                            listOf(0, 1, 2, 3)
                        }
                        val nextCol = if (Random.nextFloat() < 0.42f) {
                            val freeCols = allowedCols.filter { c ->
                                state.foods.none { it.col == c && it.step <= 2 }
                            }
                            if (freeCols.isNotEmpty()) freeCols.random() else item.col
                        } else {
                            item.col
                        }
                        updatedFoods.add(
                            item.copy(
                                col = nextCol,
                                step = 1,
                                goingUp = false,
                                flippedByChef = false
                            )
                        )
                    }
                } else {
                    // Jidlo pada dolu: Kocour na policce (catNow) muze v levem sloupci (col == 0)
                    // na pozici step == 1 pridrzet klobasu tlapkou o 1 cyklus dele!
                    if (item.col == 0 && item.step == 1 && catNow && !state.catHoldingFood && Random.nextFloat() < 0.35f) {
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
                            updatedFoods.add(
                                item.copy(
                                    step = 3,
                                    goingUp = true,
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

        // Pridani dalsiho kusu jidla do vzduchu podle skore a herního rezimu
        if (spawnCooldownSlots > 0) {
            spawnCooldownSlots--
        }
        val withinHundred = state.score % 100
        val maxSimultaneousFoods = if (state.mode == GameMode.GAME_A) {
            when {
                state.score < 6 -> 1
                withinHundred < 25 -> 2
                else -> 3
            }
        } else {
            when {
                state.score < 4 -> 2
                withinHundred < 35 -> 3
                else -> 4
            }
        }

        val slotFree = updatedFoods.none { it.phaseSlot == currentSlot }
        if (slotFree && updatedFoods.size < maxSimultaneousFoods && spawnCooldownSlots == 0) {
            val allowedCols = if (state.mode == GameMode.GAME_A && state.score < 30) {
                listOf(0, 1, 2)
            } else {
                listOf(0, 1, 2, 3)
            }
            val candidateCols = allowedCols.filter { c ->
                updatedFoods.none { it.col == c && it.step <= 2 }
            }
            if (candidateCols.isNotEmpty()) {
                val newCol = candidateCols.random()
                updatedFoods.add(
                    FoodItem(
                        col = newCol,
                        step = 0,
                        goingUp = false,
                        phaseSlot = currentSlot
                    )
                )
                spawnCooldownSlots = if (state.mode == GameMode.GAME_A) 10 else 7
            }
        }

        state = state.copy(
            catVisible = catNow,
            catHoldingFood = catHeldThisTick,
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
                if (item.step > 0) {
                    nextFoods.add(item.copy(step = item.step - 1))
                } else {
                    val nextCol = (item.col + 1) % 4
                    nextFoods.add(item.copy(col = nextCol, step = 1, goingUp = false))
                }
            } else {
                if (item.step < 4) {
                    nextFoods.add(item.copy(step = item.step + 1))
                } else {
                    flippedNow = true
                    nextFoods.add(item.copy(step = 3, goingUp = true))
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
