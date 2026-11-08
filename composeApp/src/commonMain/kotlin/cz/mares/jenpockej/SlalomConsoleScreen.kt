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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SlalomPrintGreen = Color(0xFF4C6E34)
private val SlalomPrintRed = Color(0xFFC82820)
private val SlalomPrintAsphalt = Color(0xFF9EA698)
private val SlalomPrintCurbWhite = Color(0xFFF2F4EC)
private val SlalomPrintDark = Color(0xFF1E221E)

@Composable
fun SlalomConsoleBody(
    uiState: SlalomSnapshot,
    onBackToHub: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onStartA: () -> Unit,
    onStartB: () -> Unit,
    onResetDemo: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    val outerPlasticColor = Color(0xFF7A8C72)
    val metalPlateBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE4E6DD),
            Color(0xFFD4D7CC),
            Color(0xFFC5C9BC)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerPlasticColor)
            .padding(6.dp)
            .background(metalPlateBrush, RoundedCornerShape(10.dp))
            .border(2.dp, Color(0xFF5A6953), RoundedCornerShape(10.dp))
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
                                color = SlalomPrintDark,
                                radius = r,
                                style = Stroke(width = 2.8f)
                            )
                            drawCircle(
                                color = SlalomPrintDark,
                                radius = r * 0.16f,
                                center = Offset(center.x - r * 0.72f, center.y)
                            )
                        }
                        Text(
                            text = "ИМ",
                            color = SlalomPrintDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Text(
                        text = "◀ VÝBĚR HER",
                        color = SlalomPrintRed,
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
                    SlalomActionButton(
                        label = "◀ VLEVO",
                        isActive = uiState.carLane > 0 && uiState.isRunning,
                        onClick = onMoveLeft
                    )
                }
            }

            // STŘEDNÍ PANEL: Rámeček s nápisem АВТОСЛАЛОМ a LCD displejem
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

                            val topGapHalf = 92.dp.toPx()
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
                            drawPath(leftBracket, color = SlalomPrintGreen, style = frameStroke)
                            drawPath(rightBracket, color = SlalomPrintGreen, style = frameStroke)
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
                                        Color(0xFF63735B),
                                        Color(0xFF88997F),
                                        Color(0xFF6F8066)
                                    )
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .border(2.dp, Color(0xFF43523D), RoundedCornerShape(8.dp))
                            .padding(5.dp)
                    ) {
                        SlalomLcdScreen(
                            snapshot = uiState,
                            onLeftHalfTap = onMoveLeft,
                            onRightHalfTap = onMoveRight,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color(0xFF333D2E), RoundedCornerShape(4.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(topHeaderH)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "А В Т О С Л А Л О М",
                            color = SlalomPrintRed,
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
                            text = "ЭЛЕКТРОНИКА ИМ 23",
                            color = SlalomPrintDark,
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
                        color = SlalomPrintDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "© 2026 Created by Pavel Mareš",
                        color = SlalomPrintDark,
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
                        SlalomOvalMenuButton(
                            label = "игра А",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_A,
                            onClick = onStartA
                        )
                        SlalomOvalMenuButton(
                            label = "игра Б",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_B,
                            onClick = onStartB
                        )
                        SlalomOvalMenuButton(
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
                        SlalomSmallRoundButton(
                            label = if (uiState.soundEnabled) "ZVUK" else "TICH",
                            enabled = uiState.soundEnabled,
                            onClick = onToggleSound
                        )
                        SlalomSmallRoundButton(
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
                    SlalomActionButton(
                        label = "VPRAVO ▶",
                        isActive = uiState.carLane < 2 && uiState.isRunning,
                        onClick = onMoveRight
                    )
                }
            }
        }
    }
}

@Composable
private fun SlalomActionButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val ringColor = if (isActive) Color(0xFFFFD166) else Color(0xFF55634F)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFBCC4B6), Color(0xFF687362))
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
            color = SlalomPrintDark,
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
private fun SlalomOvalMenuButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outerRim = if (isSelected) SlalomPrintRed else Color(0xFF64705E)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF7F8C79))
                .border(1.5.dp, outerRim, RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE1E6DC), Color(0xFF9AA694))
                    )
                )
        )
        Text(
            text = label,
            color = SlalomPrintDark,
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
private fun SlalomSmallRoundButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val centerColor = if (enabled) SlalomPrintDark else Color(0xFF6E7A68)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF7F8C79))
                .border(1.dp, Color(0xFF444F3F), CircleShape)
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(centerColor)
        )
        Text(
            text = label,
            color = SlalomPrintDark,
            fontSize = 7.sp,
            lineHeight = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SlalomLcdScreen(
    snapshot: SlalomSnapshot,
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

            // 1. Barevný potisk závodního okruhu (perspektivní silnice, obrubníky, věž časomíry, lesy)
            drawSlalomTrackBackdrop(vp)

            // 2. Jemný stín aktivních LCD segmentů
            translate(left = vp.s(2.4f), top = vp.s(3.0f)) {
                drawSlalomLcdLayer(vp, snapshot, isShadowPass = true)
            }

            // 3. Hlavní vrstva LCD segmentů
            drawSlalomLcdLayer(vp, snapshot, isShadowPass = false)
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
                    text = "CÍL ZÁVODU • STISKNI ИГРА А / Б",
                    color = Color(0xFFCFD4C4),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

private fun DrawScope.drawSlalomTrackBackdrop(vp: LcdViewport) {
    // 1. Zelené travnaté svahy po obou stranách trati a horizont v y = 148
    val leftGrass = Path().apply {
        moveTo(vp.x(0f), vp.y(148f))
        lineTo(vp.x(374f), vp.y(148f))
        lineTo(vp.x(126f), vp.y(538f))
        lineTo(vp.x(0f), vp.y(538f))
        close()
    }
    val rightGrass = Path().apply {
        moveTo(vp.x(626f), vp.y(148f))
        lineTo(vp.x(1000f), vp.y(148f))
        lineTo(vp.x(1000f), vp.y(538f))
        lineTo(vp.x(874f), vp.y(538f))
        close()
    }
    drawPath(leftGrass, color = SlalomPrintGreen)
    drawPath(rightGrass, color = SlalomPrintGreen)

    // Věž časomíry a tribuna vlevo na horizontu
    drawRoundRect(
        color = SlalomPrintRed,
        topLeft = vp.pt(68f, 56f),
        size = Size(vp.x(68f), vp.y(92f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
    drawRoundRect(
        color = SlalomPrintCurbWhite,
        topLeft = vp.pt(76f, 68f),
        size = Size(vp.x(52f), vp.y(26f)),
        cornerRadius = CornerRadius(vp.s(2f), vp.s(2f))
    )

    // 2. Lichoběžník závodní dráhy s červeno-bílými obrubníky a dělícími čarami 3 pruhů
    val roadPolygon = Path().apply {
        moveTo(vp.x(374f), vp.y(148f))
        lineTo(vp.x(626f), vp.y(148f))
        lineTo(vp.x(874f), vp.y(538f))
        lineTo(vp.x(126f), vp.y(538f))
        close()
    }
    drawPath(roadPolygon, color = SlalomPrintAsphalt, style = Stroke(width = vp.s(3.0f)))

    // Červené a bílé obrubníky na okrajích vozovky
    drawLine(SlalomPrintRed, vp.pt(374f, 148f), vp.pt(126f, 538f), vp.s(10f), StrokeCap.Round)
    drawLine(SlalomPrintRed, vp.pt(626f, 148f), vp.pt(874f, 538f), vp.s(10f), StrokeCap.Round)
    drawLine(SlalomPrintCurbWhite, vp.pt(378f, 152f), vp.pt(134f, 534f), vp.s(3.2f), StrokeCap.Round)
    drawLine(SlalomPrintCurbWhite, vp.pt(622f, 152f), vp.pt(866f, 534f), vp.s(3.2f), StrokeCap.Round)

    // Přerušované bílé čáry dělící vozovku na 3 jízdní pruhy
    for (seg in 0..4) {
        val t0 = seg * 0.20f + 0.03f
        val t1 = t0 + 0.11f
        val y0 = 148f + (538f - 148f) * t0
        val y1 = 148f + (538f - 148f) * t1

        val leftDivX0 = 458f + (375f - 458f) * t0
        val leftDivX1 = 458f + (375f - 458f) * t1
        val rightDivX0 = 542f + (625f - 542f) * t0
        val rightDivX1 = 542f + (625f - 542f) * t1

        drawLine(SlalomPrintCurbWhite, vp.pt(leftDivX0, y0), vp.pt(leftDivX1, y1), vp.s(4.2f), StrokeCap.Round)
        drawLine(SlalomPrintCurbWhite, vp.pt(rightDivX0, y0), vp.pt(rightDivX1, y1), vp.s(4.2f), StrokeCap.Round)
    }

    // Startovní/cílový transparent nad horizontem trati
    drawRoundRect(
        color = SlalomPrintRed,
        topLeft = vp.pt(368f, 122f),
        size = Size(vp.x(264f), vp.y(22f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
    drawLine(SlalomPrintDark, vp.pt(374f, 144f), vp.pt(374f, 154f), vp.s(4.0f))
    drawLine(SlalomPrintDark, vp.pt(626f, 144f), vp.pt(626f, 154f), vp.s(4.0f))
}

private fun DrawScope.drawSlalomLcdLayer(
    vp: LcdViewport,
    snapshot: SlalomSnapshot,
    isShadowPass: Boolean
) {
    fun segColor(active: Boolean): Color {
        return if (isShadowPass) {
            if (active) LcdShadowTint else Color.Transparent
        } else {
            if (active) LcdInkOn else LcdInkGhost
        }
    }

    // 1. Traťový komisař s šachovnicovým praporkem u věže vlevo nahoře
    drawTrackMarshal(
        vp = vp,
        marshalCol = segColor(snapshot.marshalVisible),
        flagUpCol = segColor(snapshot.marshalVisible && snapshot.marshalFlagUp),
        flagDownCol = segColor(snapshot.marshalVisible && !snapshot.marshalFlagUp)
    )

    // 2. Čtyřmístné skóre vpravo nahoře + 3 trestné pneumatiky pod ním
    val clamped = snapshot.score.coerceIn(0, 9999)
    val digits = intArrayOf(
        if (clamped >= 1000) (clamped / 1000) % 10 else 0,
        (clamped / 100) % 10,
        (clamped / 10) % 10,
        clamped % 10
    )
    for (i in 0..3) {
        drawSlalomSevenSeg(
            vp = vp,
            x = 676f + i * 48f,
            y = 24f,
            dw = 32f,
            dh = 58f,
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
        drawPenaltyTire(vp, cx = 702f + idx * 58f, cy = 112f, col = segColor(isLit))
    }

    // 3. Slalomové překážky (kužely s praporky) v 3 pruzích × 5 perspektivních krocích (0..4)
    drawPerspectiveGates(vp, snapshot.gates, ::segColor)

    // 4. Závodní formule ve 3 jízdních pruzích (0 = vlevo, 1 = střed, 2 = vpravo)
    for (lane in 0..2) {
        val activeCar = snapshot.crashAnim == null && snapshot.carLane == lane
        drawFormulaCarAtLane(vp, lane = lane, col = segColor(activeCar))
    }

    // 5. Animace smyku / nárazu formule
    drawFormulaCrashAnim(vp, snapshot.crashAnim, ::segColor)

    // 6. Nápisy GAME A / GAME B
    val gameACol = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_A)
    val gameBCol = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_B)
    drawSlalomModeLabel(vp, startX = 20f, topY = 558f, isB = false, col = gameACol)
    drawSlalomModeLabel(vp, startX = 858f, topY = 558f, isB = true, col = gameBCol)
}

private fun DrawScope.drawTrackMarshal(
    vp: LcdViewport,
    marshalCol: Color,
    flagUpCol: Color,
    flagDownCol: Color
) {
    val mx = 194f
    val my = 96f
    if (marshalCol != Color.Transparent) {
        drawCircle(marshalCol, radius = vp.s(11f), center = vp.pt(mx, my - 14f))
        val body = Path().apply {
            moveTo(vp.x(mx - 15f), vp.y(my))
            lineTo(vp.x(mx + 15f), vp.y(my))
            lineTo(vp.x(mx + 18f), vp.y(my + 36f))
            lineTo(vp.x(mx - 18f), vp.y(my + 36f))
            close()
        }
        drawPath(body, color = marshalCol)
    }

    if (flagUpCol != Color.Transparent) {
        drawLine(flagUpCol, vp.pt(mx + 12f, my + 8f), vp.pt(mx + 48f, my - 24f), vp.s(4.5f), StrokeCap.Round)
        drawRoundRect(
            color = flagUpCol,
            topLeft = vp.pt(mx + 48f, my - 34f),
            size = Size(vp.x(28f), vp.y(18f)),
            cornerRadius = CornerRadius(vp.s(2f), vp.s(2f))
        )
        drawRect(LcdBgColor, topLeft = vp.pt(mx + 56f, my - 30f), size = Size(vp.x(10f), vp.y(10f)))
    }

    if (flagDownCol != Color.Transparent) {
        drawLine(flagDownCol, vp.pt(mx + 12f, my + 12f), vp.pt(mx + 48f, my + 24f), vp.s(4.5f), StrokeCap.Round)
        drawRoundRect(
            color = flagDownCol,
            topLeft = vp.pt(mx + 48f, my + 18f),
            size = Size(vp.x(26f), vp.y(16f)),
            cornerRadius = CornerRadius(vp.s(2f), vp.s(2f))
        )
    }
}

private fun slalomLaneCenter(lane: Int, step: Int): Offset {
    // 5 perspektivních úrovní od horizontu (step = 0) až těsně před přední křídlo formule (step = 4)
    val stepT = floatArrayOf(0.08f, 0.24f, 0.42f, 0.62f, 0.82f)[step.coerceIn(0, 4)]
    val y = 154f + (432f - 154f) * stepT
    val leftLaneX = 416f + (254f - 416f) * stepT
    val rightLaneX = 584f + (746f - 584f) * stepT
    val x = when (lane) {
        0 -> leftLaneX
        1 -> 500f
        else -> rightLaneX
    }
    return Offset(x, y)
}

private fun DrawScope.drawPerspectiveGates(
    vp: LcdViewport,
    gates: List<SlalomGate>,
    segColor: (Boolean) -> Color
) {
    val scales = floatArrayOf(0.52f, 0.68f, 0.85f, 1.04f, 1.24f)

    for (lane in 0..2) {
        for (step in 0..4) {
            val isLit = gates.any { it.lane == lane && it.step == step }
            val col = segColor(isLit)
            if (col == Color.Transparent) continue

            val pt = slalomLaneCenter(lane, step)
            val sc = scales[step]
            val halfW = 26f * sc
            val h = 28f * sc

            // Slalomový kužel / bariéra s pruhy
            val cone = Path().apply {
                moveTo(vp.x(pt.x - halfW * 0.35f), vp.y(pt.y - h))
                lineTo(vp.x(pt.x + halfW * 0.35f), vp.y(pt.y - h))
                lineTo(vp.x(pt.x + halfW), vp.y(pt.y))
                lineTo(vp.x(pt.x - halfW), vp.y(pt.y))
                close()
            }
            drawPath(cone, color = col)
            drawLine(
                color = LcdBgColor,
                start = vp.pt(pt.x - halfW * 0.55f, pt.y - h * 0.45f),
                end = vp.pt(pt.x + halfW * 0.55f, pt.y - h * 0.45f),
                strokeWidth = vp.s(3.2f * sc),
                cap = StrokeCap.Round
            )
            // Podstava kuželu
            drawLine(
                color = col,
                start = vp.pt(pt.x - halfW * 1.18f, pt.y + 2f),
                end = vp.pt(pt.x + halfW * 1.18f, pt.y + 2f),
                strokeWidth = vp.s(4.5f * sc),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawFormulaCarAtLane(
    vp: LcdViewport,
    lane: Int,
    col: Color
) {
    if (col == Color.Transparent) return
    val cx = floatArrayOf(254f, 500f, 746f)[lane.coerceIn(0, 2)]
    val cy = 482f
    val outline = Stroke(width = vp.s(3.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Přední přítlačné křídlo a špička formule
    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 40f, cy - 42f),
        size = Size(vp.x(80f), vp.y(9f)),
        cornerRadius = CornerRadius(vp.s(3f), vp.s(3f))
    )

    // 2. Přední a zadní široká závodní kola se vzorkem
    for ((wx, wy, ww, wh) in listOf(
        floatArrayOf(cx - 52f, cy - 28f, 18f, 24f),
        floatArrayOf(cx + 34f, cy - 28f, 18f, 24f),
        floatArrayOf(cx - 58f, cy + 8f, 22f, 30f),
        floatArrayOf(cx + 36f, cy + 8f, 22f, 30f)
    )) {
        drawRoundRect(
            color = col,
            topLeft = vp.pt(wx, wy),
            size = Size(vp.x(ww), vp.y(wh)),
            cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
        )
        drawLine(LcdBgColor, vp.pt(wx + 3f, wy + wh * 0.5f), vp.pt(wx + ww - 3f, wy + wh * 0.5f), vp.s(2.2f))
    }

    // 3. Trup monopostu formule a zadní přítlačné křídlo
    val body = Path().apply {
        moveTo(vp.x(cx - 12f), vp.y(cy - 40f))
        lineTo(vp.x(cx + 12f), vp.y(cy - 40f))
        lineTo(vp.x(cx + 30f), vp.y(cy - 4f))
        lineTo(vp.x(cx + 28f), vp.y(cy + 36f))
        lineTo(vp.x(cx - 28f), vp.y(cy + 36f))
        lineTo(vp.x(cx - 30f), vp.y(cy - 4f))
        close()
    }
    drawPath(body, color = col)

    // Kokpit a přilba pilota
    drawCircle(LcdBgColor, radius = vp.s(12f), center = vp.pt(cx, cy - 4f))
    drawCircle(col, radius = vp.s(7.5f), center = vp.pt(cx, cy - 4f), style = outline)

    // Zadní spoiler formule
    drawRoundRect(
        color = LcdBgColor,
        topLeft = vp.pt(cx - 36f, cy + 28f),
        size = Size(vp.x(72f), vp.y(10f)),
        cornerRadius = CornerRadius(vp.s(3f), vp.s(3f))
    )
    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 36f, cy + 28f),
        size = Size(vp.x(72f), vp.y(10f)),
        cornerRadius = CornerRadius(vp.s(3f), vp.s(3f)),
        style = outline
    )
}

private fun DrawScope.drawFormulaCrashAnim(
    vp: LcdViewport,
    crashAnim: SlalomCrashAnim?,
    segColor: (Boolean) -> Color
) {
    val laneCenters = floatArrayOf(254f, 500f, 746f)
    for (lane in 0..2) {
        val active = crashAnim != null && crashAnim.crashLane == lane
        val col = segColor(active)
        if (col == Color.Transparent) continue

        val cx = laneCenters[lane]
        val cy = 476f
        // Oblak kouře a hvězdice nárazu
        drawCircle(col, radius = vp.s(26f), center = vp.pt(cx, cy), style = Stroke(width = vp.s(4.5f)))
        drawLine(col, vp.pt(cx - 38f, cy - 22f), vp.pt(cx + 38f, cy + 22f), vp.s(4.5f), StrokeCap.Round)
        drawLine(col, vp.pt(cx - 38f, cy + 22f), vp.pt(cx + 38f, cy - 22f), vp.s(4.5f), StrokeCap.Round)
    }
}

private fun DrawScope.drawPenaltyTire(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    col: Color
) {
    if (col == Color.Transparent) return
    drawCircle(col, radius = vp.s(11f), center = vp.pt(cx, cy))
    drawCircle(LcdBgColor, radius = vp.s(5f), center = vp.pt(cx, cy))
    drawCircle(col, radius = vp.s(2f), center = vp.pt(cx, cy))
}

private fun DrawScope.drawSlalomSevenSeg(
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

private fun DrawScope.drawSlalomModeLabel(
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
