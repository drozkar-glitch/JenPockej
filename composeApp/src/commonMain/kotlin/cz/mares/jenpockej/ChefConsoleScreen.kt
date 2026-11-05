package cz.mares.jenpockej

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ChefPrintOrange = Color(0xFFC85A28)
private val ChefPrintCopper = Color(0xFFD97F38)
private val ChefPrintBrown = Color(0xFF754624)
private val ChefPrintDarkWood = Color(0xFF4A2B13)
private val ChefPrintTile = Color(0xFFB8C2B2)
private val ChefPrintRed = Color(0xFFB52B24)

@Composable
fun ChefConsoleBody(
    uiState: ChefSnapshot,
    onBackToHub: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onStartA: () -> Unit,
    onStartB: () -> Unit,
    onResetDemo: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    val outerPlasticColor = Color(0xFFC2A882)
    val metalPlateBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEDE8DC),
            Color(0xFFDFD9CA),
            Color(0xFFD0C9B8)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerPlasticColor)
            .padding(6.dp)
            .background(metalPlateBrush, RoundedCornerShape(10.dp))
            .border(2.dp, Color(0xFF8C7759), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEVÝ PANEL: Klikací logo •ИМ + červené tlačítko VLEVO
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(102.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBackToHub)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension * 0.46f
                            drawCircle(
                                color = Color(0xFF2B2118),
                                radius = r,
                                style = Stroke(width = 2.8f)
                            )
                            drawCircle(
                                color = Color(0xFF2B2118),
                                radius = r * 0.16f,
                                center = Offset(center.x - r * 0.72f, center.y)
                            )
                        }
                        Text(
                            text = "ИМ",
                            color = Color(0xFF2B2118),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Text(
                        text = "◀ VÝBĚR HER",
                        color = ChefPrintRed,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    ChefActionButton(
                        label = "◀ VLEVO",
                        isActive = uiState.chefPos > 0 && uiState.isRunning,
                        onClick = onMoveLeft
                    )
                }
            }

            // STŘEDNÍ PANEL: Rámeček s nápisem ВЕСЁЛЫЙ ПОВАР a LCD displejem
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val topHeaderH = 22.dp
                val botFooterH = 20.dp

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .drawBehind {
                            val topY = (topHeaderH / 2).toPx()
                            val botY = size.height - (botFooterH / 2).toPx()
                            val r = 14.dp.toPx()
                            val d = r * 2f
                            val left = 2.dp.toPx()
                            val right = size.width - 2.dp.toPx()

                            val topGapHalf = 94.dp.toPx()
                            val botGapHalf = 108.dp.toPx()
                            val cx = size.width * 0.5f

                            val leftBracket = Path().apply {
                                moveTo(cx - topGapHalf, topY)
                                lineTo(left + r, topY)
                                arcTo(Rect(left, topY, left + d, topY + d), 270f, -90f, false)
                                lineTo(left, botY - r)
                                arcTo(Rect(left, botY - d, left + d, botY), 180f, -90f, false)
                                lineTo(cx - botGapHalf, botY)
                            }
                            val rightBracket = Path().apply {
                                moveTo(cx + topGapHalf, topY)
                                lineTo(right - r, topY)
                                arcTo(Rect(right - d, topY, right, topY + d), 270f, 90f, false)
                                lineTo(right, botY - r)
                                arcTo(Rect(right - d, botY - d, right, botY), 0f, 90f, false)
                                lineTo(cx + botGapHalf, botY)
                            }

                            val frameStroke = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            drawPath(leftBracket, color = ChefPrintBrown, style = frameStroke)
                            drawPath(rightBracket, color = ChefPrintBrown, style = frameStroke)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 10.dp,
                                end = 10.dp,
                                top = topHeaderH,
                                bottom = botFooterH
                            )
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF8C765C),
                                        Color(0xFFB59E82),
                                        Color(0xFF968066)
                                    )
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .border(2.dp, Color(0xFF5E4B36), RoundedCornerShape(8.dp))
                            .padding(5.dp)
                    ) {
                        ChefLcdScreen(
                            snapshot = uiState,
                            onLeftHalfTap = onMoveLeft,
                            onRightHalfTap = onMoveRight,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color(0xFF4A3D2C), RoundedCornerShape(4.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(topHeaderH)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ВЕСЁЛЫЙ ПОВАР",
                            color = ChefPrintRed,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.4.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(botFooterH)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ЭЛЕКТРОНИКА ИМ 04",
                            color = Color(0xFF2B2118),
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REKORD A: ${uiState.highScoreA}  |  REKORD B: ${uiState.highScoreB}",
                        color = Color(0xFF3B2E22),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "© 2026 Created by Pavel Mareš",
                        color = Color(0xFF3B2E22),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // PRAVÝ PANEL: 3 oválná tlačítka (игра А, игра Б, время) + ZVUK/VIBR + červené tlačítko VPRAVO
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(108.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ChefOvalMenuButton(
                            label = "игра А",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_A,
                            onClick = onStartA
                        )
                        ChefOvalMenuButton(
                            label = "игра Б",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_B,
                            onClick = onStartB
                        )
                        ChefOvalMenuButton(
                            label = "время",
                            isSelected = !uiState.isRunning,
                            onClick = onResetDemo
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        ChefSmallRoundButton(
                            label = if (uiState.soundEnabled) "ZVUK" else "TICH",
                            enabled = uiState.soundEnabled,
                            onClick = onToggleSound
                        )
                        ChefSmallRoundButton(
                            label = if (uiState.hapticsEnabled) "VIBR" else "BEZ",
                            enabled = uiState.hapticsEnabled,
                            onClick = onToggleHaptics
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    ChefActionButton(
                        label = "VPRAVO ▶",
                        isActive = uiState.chefPos < 3 && uiState.isRunning,
                        onClick = onMoveRight
                    )
                }
            }
        }
    }
}

@Composable
private fun ChefActionButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val ringColor = if (isActive) Color(0xFFFFD166) else Color(0xFF6B5B47)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFC4B6A2), Color(0xFF786854))
                    )
                )
                .border(2.dp, ringColor, CircleShape)
                .clickable(onClick = onClick)
                .padding(5.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isActive) {
                            listOf(Color(0xFFFF6357), Color(0xFFD62828), Color(0xFF8F1515))
                        } else {
                            listOf(Color(0xFFE63930), Color(0xFFB8201A), Color(0xFF7A110E))
                        }
                    )
                )
                .border(1.2.dp, Color(0xFF520B09), CircleShape)
        )
        Text(
            text = label,
            color = Color(0xFF2B2118),
            fontSize = 9.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun ChefOvalMenuButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outerRim = if (isSelected) ChefPrintRed else Color(0xFF756856)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF8F8270))
                .border(1.5.dp, outerRim, RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE6DFD3), Color(0xFFA89C8A))
                    )
                )
        )
        Text(
            text = label,
            color = Color(0xFF2B2118),
            fontSize = 9.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun ChefSmallRoundButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val centerColor = if (enabled) Color(0xFF261E16) else Color(0xFF7D7263)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF8F8270))
                .border(1.dp, Color(0xFF4F4336), CircleShape)
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(centerColor)
        )
        Text(
            text = label,
            color = Color(0xFF2B2118),
            fontSize = 7.sp,
            lineHeight = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun ChefLcdScreen(
    snapshot: ChefSnapshot,
    onLeftHalfTap: () -> Unit,
    onRightHalfTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(LcdBgColor)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (offset.x < size.width * 0.5f) {
                        onLeftHalfTap()
                    } else {
                        onRightHalfTap()
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val vp = LcdViewport(size.width, size.height)

            // 1. Barevný kuchyňský potisk na krycím skle (poličky, měděné pánve, sporák, myší díra)
            drawChefKitchenBackdrop(vp)

            // 2. Jemný stín aktivních LCD segmentů
            translate(left = vp.s(2.4f), top = vp.s(3.0f)) {
                drawChefLcdLayer(vp, snapshot, isShadowPass = true)
            }

            // 3. Hlavní vrstva LCD segmentů
            drawChefLcdLayer(vp, snapshot, isShadowPass = false)
        }

        if (snapshot.isGameOver) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .background(Color(0xE6161913), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "KONEC HRY • STISKNI ИГРА А / Б",
                    color = Color(0xFFCFD4C4),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

private fun DrawScope.drawChefKitchenBackdrop(vp: LcdViewport) {
    // 1. Kuchyňské kachličky v pozadí (jemný rastr)
    for (ty in listOf(120f, 220f, 320f, 420f)) {
        drawLine(ChefPrintTile, vp.pt(16f, ty), vp.pt(984f, ty), vp.s(2.0f))
    }

    // 2. Dřevěná polička vlevo nahoře pro kocoura
    drawRoundRect(
        color = ChefPrintBrown,
        topLeft = vp.pt(14f, 142f),
        size = Size(vp.x(146f), vp.y(14f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
    drawLine(ChefPrintDarkWood, vp.pt(28f, 156f), vp.pt(56f, 192f), vp.s(5f), StrokeCap.Round)

    // 3. Závěsná tyč s měděnými hrnci a naběračkami nahoře uprostřed
    drawLine(ChefPrintDarkWood, vp.pt(210f, 28f), vp.pt(520f, 28f), vp.s(5f), StrokeCap.Round)
    for (hx in listOf(255f, 355f, 455f)) {
        drawLine(ChefPrintDarkWood, vp.pt(hx, 28f), vp.pt(hx, 46f), vp.s(3.2f))
        drawRoundRect(
            color = ChefPrintCopper,
            topLeft = vp.pt(hx - 24f, 46f),
            size = Size(vp.x(48f), vp.y(32f)),
            cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
        )
        drawRoundRect(
            color = ChefPrintDarkWood,
            topLeft = vp.pt(hx - 24f, 46f),
            size = Size(vp.x(48f), vp.y(32f)),
            cornerRadius = CornerRadius(vp.s(6f), vp.s(6f)),
            style = Stroke(width = vp.s(2.6f))
        )
    }

    // 4. Kuchyňský sporák s hrncem vpravo dole
    drawRoundRect(
        color = ChefPrintBrown,
        topLeft = vp.pt(872f, 360f),
        size = Size(vp.x(112f), vp.y(178f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
    )
    drawRoundRect(
        color = ChefPrintOrange,
        topLeft = vp.pt(886f, 404f),
        size = Size(vp.x(84f), vp.y(72f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
    for (kx in listOf(894f, 928f, 962f)) {
        drawCircle(ChefPrintDarkWood, radius = vp.s(5f), center = vp.pt(kx, 380f))
    }

    // 5. Myší díra v levém dolním rohu a podlaha kuchyně
    val mouseHole = Path().apply {
        moveTo(vp.x(18f), vp.y(538f))
        cubicTo(
            vp.x(18f), vp.y(492f),
            vp.x(68f), vp.y(492f),
            vp.x(68f), vp.y(538f)
        )
        close()
    }
    drawPath(mouseHole, color = ChefPrintDarkWood)
    drawLine(ChefPrintDarkWood, vp.pt(10f, 538f), vp.pt(990f, 538f), vp.s(6.5f), StrokeCap.Round)
}

private fun DrawScope.drawChefLcdLayer(
    vp: LcdViewport,
    snapshot: ChefSnapshot,
    isShadowPass: Boolean
) {
    fun segColor(active: Boolean): Color {
        return if (isShadowPass) {
            if (active) LcdShadowTint else Color.Transparent
        } else {
            if (active) LcdInkOn else LcdInkGhost
        }
    }

    // 1. Čtyřmístné 7segmentové skóre vpravo nahoře + 3 trestné myšky pod ním
    val clamped = snapshot.score.coerceIn(0, 9999)
    val digits = intArrayOf(
        if (clamped >= 1000) (clamped / 1000) % 10 else 0,
        (clamped / 100) % 10,
        (clamped / 10) % 10,
        clamped % 10
    )
    for (i in 0..3) {
        drawChefSevenSeg(
            vp = vp,
            x = 590f + i * 48f,
            y = 28f,
            dw = 32f,
            dh = 60f,
            value = digits[i],
            onCol = segColor(true),
            offCol = segColor(false)
        )
    }

    for (idx in 0..2) {
        val thresholdFull = (idx + 1) * 2
        val thresholdHalf = thresholdFull - 1
        val isLit = when {
            snapshot.penaltyHalfPoints >= thresholdFull -> true
            snapshot.penaltyHalfPoints == thresholdHalf -> snapshot.blinkPhase
            else -> false
        }
        drawPenaltyMouseHead(vp, cx = 610f + idx * 58f, cy = 122f, col = segColor(isLit))
    }

    // 2. Kocour na poličce vlevo nahoře s pohyblivou tlapkou
    drawShelfCat(
        vp = vp,
        catCol = segColor(snapshot.catVisible),
        pawDownCol = segColor(snapshot.catVisible && snapshot.catHoldingFood),
        pawUpCol = segColor(snapshot.catVisible && !snapshot.catHoldingFood)
    )

    // 3. 4 sloupce padajícího / nadhazovaného jídla (step 0..4)
    drawFallingFoods(vp, snapshot.foods, ::segColor)

    // 4. Kuchař ve vysoké čepici s pánvičkou na 4 pozicích (0..3)
    for (pos in 0..3) {
        val activeChef = snapshot.crashAnim == null && snapshot.chefPos == pos
        drawChefAtPosition(
            vp = vp,
            pos = pos,
            bodyCol = segColor(activeChef),
            panNormalCol = segColor(activeChef && !snapshot.panFlippingUp),
            panFlipCol = segColor(activeChef && snapshot.panFlippingUp)
        )
    }

    // 5. Spadlé jídlo na podlaze a utíkající myška s vidličkou
    drawFloorMouseCrash(vp, snapshot.crashAnim, ::segColor)

    // 6. Nápisy GAME A / GAME B v dolních rozích
    val gameACol = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_A)
    val gameBCol = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_B)
    drawChefModeLabel(vp, startX = 24f, topY = 558f, isB = false, col = gameACol)
    drawChefModeLabel(vp, startX = 858f, topY = 558f, isB = true, col = gameBCol)
}

private fun DrawScope.drawShelfCat(
    vp: LcdViewport,
    catCol: Color,
    pawDownCol: Color,
    pawUpCol: Color
) {
    val cx = 86f
    val cy = 104f
    val outline = Stroke(width = vp.s(3.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    if (catCol != Color.Transparent) {
        // Tělo sedícího kocoura a zatočený ocas
        val body = Path().apply {
            moveTo(vp.x(cx - 28f), vp.y(cy + 36f))
            cubicTo(vp.x(cx - 32f), vp.y(cy + 4f), vp.x(cx + 8f), vp.y(cy + 4f), vp.x(cx + 18f), vp.y(cy + 36f))
            close()
        }
        drawPath(body, color = catCol)

        // Hlava se špičatýma ušima
        drawCircle(color = LcdBgColor, radius = vp.s(16f), center = vp.pt(cx + 6f, cy - 4f))
        drawCircle(color = catCol, radius = vp.s(16f), center = vp.pt(cx + 6f, cy - 4f), style = outline)
        val ears = Path().apply {
            moveTo(vp.x(cx - 6f), vp.y(cy - 14f))
            lineTo(vp.x(cx - 12f), vp.y(cy - 30f))
            lineTo(vp.x(cx + 2f), vp.y(cy - 18f))
            moveTo(vp.x(cx + 10f), vp.y(cy - 18f))
            lineTo(vp.x(cx + 20f), vp.y(cy - 30f))
            lineTo(vp.x(cx + 20f), vp.y(cy - 12f))
        }
        drawPath(ears, color = catCol, style = outline)
        drawCircle(catCol, radius = vp.s(2.6f), center = vp.pt(cx + 2f, cy - 6f))
        drawCircle(catCol, radius = vp.s(2.6f), center = vp.pt(cx + 12f, cy - 6f))
    }

    if (pawUpCol != Color.Transparent) {
        drawLine(pawUpCol, vp.pt(cx + 16f, cy + 12f), vp.pt(cx + 52f, cy - 4f), vp.s(6.0f), StrokeCap.Round)
        drawCircle(pawUpCol, radius = vp.s(5.5f), center = vp.pt(cx + 54f, cy - 4f))
    }
    if (pawDownCol != Color.Transparent) {
        drawLine(pawDownCol, vp.pt(cx + 16f, cy + 16f), vp.pt(196f, 168f), vp.s(6.0f), StrokeCap.Round)
        drawCircle(pawDownCol, radius = vp.s(6.0f), center = vp.pt(200f, 170f))
    }
}

private fun DrawScope.drawFallingFoods(
    vp: LcdViewport,
    foods: List<FoodItem>,
    segColor: (Boolean) -> Color
) {
    val colX = floatArrayOf(226f, 368f, 512f, 656f)
    val stepY = floatArrayOf(124f, 182f, 242f, 304f, 364f)
    val stepAngles = floatArrayOf(-15f, 35f, 95f, 155f, 210f)

    for (col in 0..3) {
        for (step in 0..4) {
            val isLit = foods.any { it.col == col && it.step == step }
            val ink = segColor(isLit)
            if (ink == Color.Transparent) continue

            val center = vp.pt(colX[col], stepY[step])
            val angle = stepAngles[step] + col * 18f

            rotate(degrees = angle, pivot = center) {
                when (col) {
                    0 -> {
                        // 0: Klobása / párek
                        drawRoundRect(
                            color = ink,
                            topLeft = Offset(center.x - vp.x(20f), center.y - vp.y(8f)),
                            size = Size(vp.x(40f), vp.y(16f)),
                            cornerRadius = CornerRadius(vp.s(8f), vp.s(8f))
                        )
                        drawLine(LcdBgColor, Offset(center.x - vp.x(8f), center.y - vp.y(3f)), Offset(center.x + vp.x(8f), center.y - vp.y(3f)), vp.s(2.4f), StrokeCap.Round)
                    }
                    1 -> {
                        // 1: Ryba
                        val fish = Path().apply {
                            moveTo(center.x - vp.x(22f), center.y)
                            quadraticTo(center.x, center.y - vp.y(15f), center.x + vp.x(16f), center.y)
                            lineTo(center.x + vp.x(26f), center.y - vp.y(9f))
                            lineTo(center.x + vp.x(26f), center.y + vp.y(9f))
                            lineTo(center.x + vp.x(16f), center.y)
                            quadraticTo(center.x, center.y + vp.y(15f), center.x - vp.x(22f), center.y)
                            close()
                        }
                        drawPath(fish, color = ink)
                        drawCircle(LcdBgColor, radius = vp.s(2.4f), center = Offset(center.x - vp.x(10f), center.y - vp.y(2f)))
                    }
                    2 -> {
                        // 2: Kotleta na kosti
                        drawOval(
                            color = ink,
                            topLeft = Offset(center.x - vp.x(18f), center.y - vp.y(11f)),
                            size = Size(vp.x(30f), vp.y(22f))
                        )
                        drawCircle(LcdBgColor, radius = vp.s(4f), center = Offset(center.x - vp.x(4f), center.y))
                        drawLine(ink, Offset(center.x + vp.x(10f), center.y), Offset(center.x + vp.x(24f), center.y), vp.s(5f), StrokeCap.Round)
                    }
                    else -> {
                        // 3: Volské oko / lívanec
                        drawOval(
                            color = LcdBgColor,
                            topLeft = Offset(center.x - vp.x(19f), center.y - vp.y(12f)),
                            size = Size(vp.x(38f), vp.y(24f))
                        )
                        drawOval(
                            color = ink,
                            topLeft = Offset(center.x - vp.x(19f), center.y - vp.y(12f)),
                            size = Size(vp.x(38f), vp.y(24f)),
                            style = Stroke(width = vp.s(4.2f))
                        )
                        drawCircle(ink, radius = vp.s(5.5f), center = center)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawChefAtPosition(
    vp: LcdViewport,
    pos: Int,
    bodyCol: Color,
    panNormalCol: Color,
    panFlipCol: Color
) {
    // Kuchař stojí napravo od pánvičky a drží pánvičku přímo pod sloupcem `pos` (226, 368, 512, 656)
    val panCenterX = floatArrayOf(226f, 368f, 512f, 656f)[pos.coerceIn(0, 3)]
    val chefX = panCenterX + 86f
    val chefY = 442f
    val outline = Stroke(width = vp.s(3.6f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    if (bodyCol != Color.Transparent) {
        // 1. Vysoká kuchařská čepice (toque)
        val hatTopY = chefY - 76f
        drawCircle(LcdBgColor, radius = vp.s(13f), center = vp.pt(chefX - 10f, hatTopY))
        drawCircle(LcdBgColor, radius = vp.s(15f), center = vp.pt(chefX + 2f, hatTopY - 4f))
        drawCircle(LcdBgColor, radius = vp.s(13f), center = vp.pt(chefX + 14f, hatTopY))
        drawCircle(bodyCol, radius = vp.s(13f), center = vp.pt(chefX - 10f, hatTopY), style = outline)
        drawCircle(bodyCol, radius = vp.s(15f), center = vp.pt(chefX + 2f, hatTopY - 4f), style = outline)
        drawCircle(bodyCol, radius = vp.s(13f), center = vp.pt(chefX + 14f, hatTopY), style = outline)

        drawRoundRect(
            color = LcdBgColor,
            topLeft = vp.pt(chefX - 16f, hatTopY + 4f),
            size = Size(vp.x(34f), vp.y(22f)),
            cornerRadius = CornerRadius(vp.s(3f), vp.s(3f))
        )
        drawRoundRect(
            color = bodyCol,
            topLeft = vp.pt(chefX - 16f, hatTopY + 4f),
            size = Size(vp.x(34f), vp.y(22f)),
            cornerRadius = CornerRadius(vp.s(3f), vp.s(3f)),
            style = outline
        )

        // 2. Usměvavá hlava kuchaře s knírem
        val headY = chefY - 34f
        drawCircle(LcdBgColor, radius = vp.s(16f), center = vp.pt(chefX, headY))
        drawCircle(bodyCol, radius = vp.s(16f), center = vp.pt(chefX, headY), style = outline)
        drawCircle(bodyCol, radius = vp.s(2.8f), center = vp.pt(chefX - 6f, headY - 4f))
        drawCircle(bodyCol, radius = vp.s(3.8f), center = vp.pt(chefX - 15f, headY))
        // Knír kuchaře
        drawLine(bodyCol, vp.pt(chefX - 16f, headY + 6f), vp.pt(chefX + 2f, headY + 4f), vp.s(3.6f), StrokeCap.Round)

        // 3. Kuchařský rondon a zástěra s knoflíky
        val apron = Path().apply {
            moveTo(vp.x(chefX - 18f), vp.y(chefY - 16f))
            lineTo(vp.x(chefX + 18f), vp.y(chefY - 16f))
            lineTo(vp.x(chefX + 22f), vp.y(chefY + 42f))
            lineTo(vp.x(chefX - 22f), vp.y(chefY + 42f))
            close()
        }
        drawPath(apron, color = LcdBgColor)
        drawPath(apron, color = bodyCol, style = outline)
        drawCircle(bodyCol, radius = vp.s(2.8f), center = vp.pt(chefX - 6f, chefY))
        drawCircle(bodyCol, radius = vp.s(2.8f), center = vp.pt(chefX - 6f, chefY + 16f))

        // 4. Nohy kuchaře na podlaze
        drawLine(bodyCol, vp.pt(chefX - 10f, chefY + 42f), vp.pt(chefX - 16f, 534f), vp.s(7.5f), StrokeCap.Round)
        drawLine(bodyCol, vp.pt(chefX + 10f, chefY + 42f), vp.pt(chefX + 16f, 534f), vp.s(7.5f), StrokeCap.Round)
    }

    // 5. Ruka s pánvičkou (normální vodorovná vs. nadhazující nahoru)
    if (panNormalCol != Color.Transparent) {
        drawChefPanWithArm(vp, chefX = chefX - 16f, shoulderY = chefY - 4f, panX = panCenterX, panY = 408f, tiltDeg = 0f, col = panNormalCol)
    }
    if (panFlipCol != Color.Transparent) {
        drawChefPanWithArm(vp, chefX = chefX - 16f, shoulderY = chefY - 8f, panX = panCenterX, panY = 390f, tiltDeg = 18f, col = panFlipCol)
    }
}

private fun DrawScope.drawChefPanWithArm(
    vp: LcdViewport,
    chefX: Float,
    shoulderY: Float,
    panX: Float,
    panY: Float,
    tiltDeg: Float,
    col: Color
) {
    val handleEndX = panX + 34f
    drawLine(col, vp.pt(chefX, shoulderY), vp.pt(handleEndX, panY), vp.s(7.0f), StrokeCap.Round)

    val pivot = vp.pt(panX, panY)
    rotate(degrees = tiltDeg, pivot = pivot) {
        drawLine(col, vp.pt(panX + 18f, panY), vp.pt(panX + 44f, panY), vp.s(5.5f), StrokeCap.Round)
        val panBody = Path().apply {
            moveTo(vp.x(panX - 28f), vp.y(panY - 4f))
            lineTo(vp.x(panX + 28f), vp.y(panY - 4f))
            lineTo(vp.x(panX + 22f), vp.y(panY + 12f))
            lineTo(vp.x(panX - 22f), vp.y(panY + 12f))
            close()
        }
        drawPath(panBody, color = col)
    }
}

private fun DrawScope.drawFloorMouseCrash(
    vp: LcdViewport,
    crashAnim: ChefCrashAnim?,
    segColor: (Boolean) -> Color
) {
    val colX = floatArrayOf(226f, 368f, 512f, 656f)
    for (c in 0..3) {
        val splatLit = crashAnim != null && crashAnim.droppedCol == c && crashAnim.frame == 0
        val col = segColor(splatLit)
        if (col != Color.Transparent) {
            val sx = colX[c]
            drawOval(col, vp.pt(sx - 22f, 518f), Size(vp.x(44f), vp.y(14f)))
            drawCircle(col, radius = vp.s(3.5f), center = vp.pt(sx - 28f, 512f))
            drawCircle(col, radius = vp.s(3.5f), center = vp.pt(sx + 28f, 512f))
        }
    }

    for (step in 1..3) {
        val mouseLit = crashAnim != null && crashAnim.frame == step
        val col = segColor(mouseLit)
        if (col == Color.Transparent) continue

        val mx = 120f + step * 110f
        val my = 516f
        drawOval(col, vp.pt(mx - 14f, my - 8f), Size(vp.x(28f), vp.y(16f)))
        drawCircle(col, radius = vp.s(6f), center = vp.pt(mx - 12f, my - 10f))
        drawLine(col, vp.pt(mx + 14f, my), vp.pt(mx + 28f, my - 6f), vp.s(2.8f), StrokeCap.Round)
        // Vidlička v tlapce myšky
        drawLine(col, vp.pt(mx - 16f, my - 4f), vp.pt(mx - 28f, my - 14f), vp.s(2.8f), StrokeCap.Round)
    }
}

private fun DrawScope.drawPenaltyMouseHead(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    col: Color
) {
    if (col == Color.Transparent) return
    drawCircle(col, radius = vp.s(9f), center = vp.pt(cx, cy))
    drawCircle(col, radius = vp.s(5.5f), center = vp.pt(cx - 8f, cy - 8f))
    drawCircle(col, radius = vp.s(5.5f), center = vp.pt(cx + 8f, cy - 8f))
    drawCircle(LcdBgColor, radius = vp.s(1.8f), center = vp.pt(cx - 3f, cy - 2f))
    drawCircle(LcdBgColor, radius = vp.s(1.8f), center = vp.pt(cx + 3f, cy - 2f))
}

private fun DrawScope.drawChefSevenSeg(
    vp: LcdViewport,
    x: Float,
    y: Float,
    dw: Float,
    dh: Float,
    value: Int,
    onCol: Color,
    offCol: Color
) {
    val mask = when (value) {
        0 -> 0b1110111
        1 -> 0b0100100
        2 -> 0b1011101
        3 -> 0b1101101
        4 -> 0b0101110
        5 -> 0b1101011
        6 -> 0b1111011
        7 -> 0b0100101
        8 -> 0b1111111
        9 -> 0b1101111
        else -> 0
    }
    val sw = vp.s(6.0f)
    val midY = y + dh * 0.5f
    val botY = y + dh
    val p = 3.0f
    val slantTop = 3.2f
    val slantMid = 1.6f

    fun col(bit: Int): Color = if ((mask and (1 shl bit)) != 0) onCol else offCol
    fun seg(bit: Int, x0: Float, y0: Float, x1: Float, y1: Float) {
        val c = col(bit)
        if (c != Color.Transparent) {
            drawLine(c, vp.pt(x0, y0), vp.pt(x1, y1), sw, StrokeCap.Round)
        }
    }

    seg(0, x + slantTop + p, y, x + slantTop + dw - p, y)
    seg(1, x + slantTop, y + p, x + slantMid, midY - p)
    seg(2, x + slantTop + dw, y + p, x + slantMid + dw, midY - p)
    seg(3, x + slantMid + p, midY, x + slantMid + dw - p, midY)
    seg(4, x + slantMid, midY + p, x, botY - p)
    seg(5, x + slantMid + dw, midY + p, x + dw, botY - p)
    seg(6, x + p, botY, x + dw - p, botY)
}

private fun DrawScope.drawChefModeLabel(
    vp: LcdViewport,
    startX: Float,
    topY: Float,
    isB: Boolean,
    col: Color
) {
    if (col == Color.Transparent) return
    val sw = vp.s(3.2f)
    val h = 16f
    val w = 12f
    var x = startX

    val gPath = Path().apply {
        moveTo(vp.x(x + w), vp.y(topY + 3f))
        lineTo(vp.x(x + 2f), vp.y(topY))
        lineTo(vp.x(x), vp.y(topY + h * 0.5f))
        lineTo(vp.x(x + 2f), vp.y(topY + h))
        lineTo(vp.x(x + w), vp.y(topY + h))
        lineTo(vp.x(x + w), vp.y(topY + h * 0.55f))
        lineTo(vp.x(x + w * 0.5f), vp.y(topY + h * 0.55f))
    }
    drawPath(gPath, color = col, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    x += w + 7f

    val aPath1 = Path().apply {
        moveTo(vp.x(x), vp.y(topY + h))
        lineTo(vp.x(x + w * 0.5f), vp.y(topY))
        lineTo(vp.x(x + w), vp.y(topY + h))
    }
    drawPath(aPath1, color = col, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawLine(col, vp.pt(x + w * 0.2f, topY + h * 0.65f), vp.pt(x + w * 0.8f, topY + h * 0.65f), sw, StrokeCap.Round)
    x += w + 7f

    val mPath = Path().apply {
        moveTo(vp.x(x), vp.y(topY + h))
        lineTo(vp.x(x), vp.y(topY))
        lineTo(vp.x(x + w * 0.55f), vp.y(topY + h * 0.6f))
        lineTo(vp.x(x + w * 1.1f), vp.y(topY))
        lineTo(vp.x(x + w * 1.1f), vp.y(topY + h))
    }
    drawPath(mPath, color = col, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    x += w * 1.1f + 7f

    drawLine(col, vp.pt(x, topY), vp.pt(x, topY + h), sw, StrokeCap.Round)
    drawLine(col, vp.pt(x, topY), vp.pt(x + w * 0.9f, topY), sw, StrokeCap.Round)
    drawLine(col, vp.pt(x, topY + h * 0.5f), vp.pt(x + w * 0.75f, topY + h * 0.5f), sw, StrokeCap.Round)
    drawLine(col, vp.pt(x, topY + h), vp.pt(x + w * 0.9f, topY + h), sw, StrokeCap.Round)
    x += w + 14f

    if (!isB) {
        val aPath2 = Path().apply {
            moveTo(vp.x(x), vp.y(topY + h))
            lineTo(vp.x(x + w * 0.5f), vp.y(topY))
            lineTo(vp.x(x + w), vp.y(topY + h))
        }
        drawPath(aPath2, color = col, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(col, vp.pt(x + w * 0.2f, topY + h * 0.65f), vp.pt(x + w * 0.8f, topY + h * 0.65f), sw, StrokeCap.Round)
    } else {
        drawLine(col, vp.pt(x, topY), vp.pt(x, topY + h), sw, StrokeCap.Round)
        val bLoops = Path().apply {
            moveTo(vp.x(x), vp.y(topY))
            lineTo(vp.x(x + w * 0.75f), vp.y(topY))
            lineTo(vp.x(x + w), vp.y(topY + h * 0.25f))
            lineTo(vp.x(x + w * 0.75f), vp.y(topY + h * 0.5f))
            lineTo(vp.x(x), vp.y(topY + h * 0.5f))
            lineTo(vp.x(x + w * 0.8f), vp.y(topY + h * 0.5f))
            lineTo(vp.x(x + w * 1.05f), vp.y(topY + h * 0.75f))
            lineTo(vp.x(x + w * 0.8f), vp.y(topY + h))
            lineTo(vp.x(x), vp.y(topY + h))
        }
        drawPath(bLoops, color = col, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
