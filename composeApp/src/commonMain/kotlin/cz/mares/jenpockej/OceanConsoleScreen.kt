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
import androidx.compose.foundation.layout.Spacer
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

private val OceanPrintBlue = Color(0xFF1F5D78)
private val OceanPrintTeal = Color(0xFF2E7A78)
private val OceanPrintCoral = Color(0xFFB83A2E)
private val OceanPrintGold = Color(0xFFD99426)
private val OceanPrintBrown = Color(0xFF7D4B22)

@Composable
fun OceanConsoleBody(
    uiState: OceanSnapshot,
    onBackToHub: () -> Unit,
    onMoveLeftOrUp: () -> Unit,
    onMoveRightOrGrab: () -> Unit,
    onStartA: () -> Unit,
    onStartB: () -> Unit,
    onResetDemo: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    val outerPlasticColor = Color(0xFF8EA8B2)
    val metalPlateBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE0E5E4),
            Color(0xFFD2D8D6),
            Color(0xFFC4CCC9)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerPlasticColor)
            .padding(6.dp)
            .background(metalPlateBrush, RoundedCornerShape(10.dp))
            .border(2.dp, Color(0xFF6B7F85), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEVY PANEL: Klikaci logo •ИМ (navrat do vyberu her) + tlacitka VLEVO / NA LOD
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
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension * 0.46f
                            drawCircle(
                                color = Color(0xFF1A282E),
                                radius = r,
                                style = Stroke(width = 2.8f)
                            )
                            drawCircle(
                                color = Color(0xFF1A282E),
                                radius = r * 0.16f,
                                center = Offset(center.x - r * 0.72f, center.y)
                            )
                        }
                        Text(
                            text = "ИМ",
                            color = Color(0xFF1A282E),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Text(
                        text = "◀ VÝBĚR HER",
                        color = Color(0xFF1F5D78),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    OceanActionButton(
                        label = "▲ NA LOĎ",
                        isActive = uiState.diverPos <= 1 && uiState.isRunning,
                        onClick = onMoveLeftOrUp
                    )
                    OceanActionButton(
                        label = "◀ VLEVO",
                        isActive = uiState.diverPos in 2..4 && uiState.isRunning,
                        onClick = onMoveLeftOrUp
                    )
                }
            }

            // STREDNI PANEL: Ramecek s napisem ТАЙНЫ ОКЕАНА a LCD displejem
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
                            drawPath(leftBracket, color = OceanPrintBlue, style = frameStroke)
                            drawPath(rightBracket, color = OceanPrintBlue, style = frameStroke)
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
                                        Color(0xFF49707A),
                                        Color(0xFF6B939C),
                                        Color(0xFF537982)
                                    )
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .border(2.dp, Color(0xFF345259), RoundedCornerShape(8.dp))
                            .padding(5.dp)
                    ) {
                        OceanLcdScreen(
                            snapshot = uiState,
                            onLeftHalfTap = onMoveLeftOrUp,
                            onRightHalfTap = onMoveRightOrGrab,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color(0xFF334742), RoundedCornerShape(4.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(topHeaderH)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ТАЙНЫ ОКЕАНА",
                            color = Color(0xFF18587A),
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
                            text = "ЭЛЕКТРОНИКА ИМ 03",
                            color = Color(0xFF1A282E),
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
                        color = Color(0xFF28383E),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "© 2026 Created by Pavel Mareš",
                        color = Color(0xFF28383E),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // PRAVY PANEL: 3 ovalna tlacitka (игра А, игра Б, время) + ZVUK/VIBR + tlacitka VPRAVO / POKLAD
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
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        OceanOvalMenuButton(
                            label = "игра А",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_A,
                            onClick = onStartA
                        )
                        OceanOvalMenuButton(
                            label = "игра Б",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_B,
                            onClick = onStartB
                        )
                        OceanOvalMenuButton(
                            label = "время",
                            isSelected = !uiState.isRunning,
                            onClick = onResetDemo
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        OceanSmallRoundButton(
                            label = if (uiState.soundEnabled) "ZVUK" else "TICH",
                            enabled = uiState.soundEnabled,
                            onClick = onToggleSound
                        )
                        OceanSmallRoundButton(
                            label = if (uiState.hapticsEnabled) "VIBR" else "BEZ",
                            enabled = uiState.hapticsEnabled,
                            onClick = onToggleHaptics
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    OceanActionButton(
                        label = "VPRAVO ▶",
                        isActive = uiState.diverPos in 1..4 && uiState.isRunning,
                        onClick = onMoveRightOrGrab
                    )
                    OceanActionButton(
                        label = "POKLAD ★",
                        isActive = uiState.diverPos == 5 && uiState.isRunning,
                        onClick = onMoveRightOrGrab
                    )
                }
            }
        }
    }
}

@Composable
private fun OceanActionButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val ringColor = if (isActive) Color(0xFFFFD166) else Color(0xFF4F646B)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFB5C2C4), Color(0xFF67787D))
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
            color = Color(0xFF1A282E),
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun OceanOvalMenuButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outerRim = if (isSelected) Color(0xFFD62828) else Color(0xFF607075)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF7D8E94))
                .border(1.5.dp, outerRim, RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(3.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFD8E0E0), Color(0xFF95A3A6))
                    )
                )
        )
        Text(
            text = label,
            color = Color(0xFF1A282E),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun OceanSmallRoundButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val centerColor = if (enabled) Color(0xFF1D2A30) else Color(0xFF6C7A80)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF7D8E94))
                .border(1.dp, Color(0xFF425257), CircleShape)
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(centerColor)
        )
        Text(
            text = label,
            color = Color(0xFF1A282E),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OceanLcdScreen(
    snapshot: OceanSnapshot,
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

            // 1. Barevny potisk morskeho sveta na krycim skle (hladina, lod, koraly, truhla)
            drawOceanPrintedBackdrop(vp)

            // 2. Jemny stin aktivnich LCD segmentu
            translate(left = vp.s(2.4f), top = vp.s(3.0f)) {
                drawOceanLcdLayer(vp, snapshot, isShadowPass = true)
            }

            // 3. Hlavni vrstva LCD segmentu
            drawOceanLcdLayer(vp, snapshot, isShadowPass = false)
        }

        if (snapshot.isGameOver) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(Color(0xE6161913), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "KONEC HRY • STISKNI ИГРА А / Б",
                    color = Color(0xFFCFD4C4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        } else if (snapshot.isDemoMode && !snapshot.isRunning) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(Color(0xCC1F5D78), RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "DEMO • ZVOL ИГРА А NEBO ИГРА Б VPRAVO NAHOŘE",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

private fun DrawScope.drawOceanPrintedBackdrop(vp: LcdViewport) {
    // Vlnita vodni hladina v horni casti displeje
    val wavePath = Path().apply {
        moveTo(vp.x(10f), vp.y(148f))
        var x = 10f
        while (x < 980f) {
            quadraticTo(vp.x(x + 22f), vp.y(136f), vp.x(x + 44f), vp.y(148f))
            x += 44f
        }
    }
    drawPath(wavePath, color = OceanPrintBlue, style = Stroke(width = vp.s(4.5f), cap = StrokeCap.Round))

    // Trup potapecske lodky na hladine vlevo nahore + zebrik do vody
    val boatHull = Path().apply {
        moveTo(vp.x(26f), vp.y(118f))
        lineTo(vp.x(258f), vp.y(118f))
        lineTo(vp.x(232f), vp.y(152f))
        lineTo(vp.x(52f), vp.y(152f))
        close()
    }
    drawPath(boatHull, color = OceanPrintBrown)
    drawLine(OceanPrintGold, vp.pt(40f, 130f), vp.pt(242f, 130f), vp.s(4f), StrokeCap.Round)

    // Zebrik z lodi pod hladinu
    drawLine(OceanPrintBrown, vp.pt(188f, 148f), vp.pt(188f, 224f), vp.s(4f))
    drawLine(OceanPrintBrown, vp.pt(214f, 148f), vp.pt(214f, 224f), vp.s(4f))
    for (ry in listOf(164f, 182f, 200f, 218f)) {
        drawLine(OceanPrintBrown, vp.pt(188f, ry), vp.pt(214f, ry), vp.s(3f))
    }

    // Koralovy utes vlevo a morske rasy dole
    val coral = Path().apply {
        moveTo(vp.x(18f), vp.y(540f))
        lineTo(vp.x(18f), vp.y(330f))
        lineTo(vp.x(56f), vp.y(370f))
        lineTo(vp.x(42f), vp.y(450f))
        lineTo(vp.x(78f), vp.y(540f))
        close()
    }
    drawPath(coral, color = OceanPrintCoral)

    // Otevrena truhla s pokladem vpravo dole
    drawRoundRect(
        color = OceanPrintBrown,
        topLeft = vp.pt(816f, 462f),
        size = Size(vp.x(146f), vp.y(76f)),
        cornerRadius = CornerRadius(vp.s(8f), vp.s(8f))
    )
    // Zlate mince v otevrene truhle
    drawOval(
        color = OceanPrintGold,
        topLeft = vp.pt(826f, 444f),
        size = Size(vp.x(126f), vp.y(34f))
    )
    drawRoundRect(
        color = OceanPrintGold,
        topLeft = vp.pt(816f, 462f),
        size = Size(vp.x(146f), vp.y(76f)),
        cornerRadius = CornerRadius(vp.s(8f), vp.s(8f)),
        style = Stroke(width = vp.s(5f))
    )

    // Morske dno
    drawLine(
        color = OceanPrintTeal,
        start = vp.pt(14f, 542f),
        end = vp.pt(986f, 542f),
        strokeWidth = vp.s(6f),
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawOceanLcdLayer(
    vp: LcdViewport,
    snapshot: OceanSnapshot,
    isShadowPass: Boolean
) {
    fun segColor(active: Boolean): Color {
        return if (isShadowPass) {
            if (active) LcdShadowTint else Color.Transparent
        } else {
            if (active) LcdInkOn else LcdInkGhost
        }
    }

    // 1. Ctyrmistne 7segmentove skore nahore uprostred
    val clamped = snapshot.score.coerceIn(0, 9999)
    val digits = intArrayOf(
        if (clamped >= 1000) (clamped / 1000) % 10 else 0,
        (clamped / 100) % 10,
        (clamped / 10) % 10,
        clamped % 10
    )
    for (i in 0..3) {
        drawOceanSevenSegDigit(
            vp = vp,
            x = 340f + i * 48f,
            y = 34f,
            dw = 32f,
            dh = 62f,
            value = digits[i],
            onCol = segColor(true),
            offCol = segColor(false)
        )
    }

    // 2. Zbyvajici potapeci na palube lodi (indikator zivotu: 2 cekajici potapeci v lodi)
    val diverReserve1Lit = snapshot.lostDivers < 1
    val diverReserve2Lit = snapshot.lostDivers < 2
    drawMiniBoatDiver(vp, cx = 74f, cy = 92f, col = segColor(diverReserve1Lit))
    drawMiniBoatDiver(vp, cx = 124f, cy = 92f, col = segColor(diverReserve2Lit))

    // 3. Telo obri chobotnice vpravo nahore + 4 clenita chapadla
    drawGiantOctopusBody(vp, col = segColor(true))
    drawOctopusTentacles(vp, snapshot.tentacleLengths, ::segColor)

    // 4. Potapec na vsech 6 pozicich (0 = na lodi, 1 = zebrik, 2..4 = dno, 5 = u truhly)
    val caughtActive = snapshot.caughtAnimFrame != null
    for (pos in 0..5) {
        val activeAtPos = !caughtActive && snapshot.diverPos == pos
        val hasBag = activeAtPos && snapshot.bagGoldCount > 0
        val reachingChest = pos == 5 && activeAtPos && snapshot.grabbingAtChest
        drawDiverAtPosition(
            vp = vp,
            pos = pos,
            bodyCol = segColor(activeAtPos),
            bagCol = segColor(hasBag || (pos == 0 && snapshot.unloadBonusTicks > 0)),
            reachCol = segColor(reachingChest)
        )
    }

    // 5. Animace chyceneho potapece v chapadlech chobotnice uprostred
    val caughtLit = caughtActive
    drawCaughtDiverInTentacles(
        vp = vp,
        col = segColor(caughtLit),
        wavePhase = (snapshot.caughtAnimFrame ?: 0) % 2 == 0
    )
}

private fun DrawScope.drawMiniBoatDiver(vp: LcdViewport, cx: Float, cy: Float, col: Color) {
    if (col == Color.Transparent) return
    drawCircle(col, radius = vp.s(9f), center = vp.pt(cx, cy - 12f))
    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 10f, cy - 2f),
        size = Size(vp.x(20f), vp.y(22f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
}

private fun DrawScope.drawGiantOctopusBody(vp: LcdViewport, col: Color) {
    if (col == Color.Transparent) return
    val stroke = Stroke(width = vp.s(4.2f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Vak hlavy chobotnice vpravo nahore
    val mantle = Path().apply {
        moveTo(vp.x(640f), vp.y(190f))
        cubicTo(
            vp.x(620f), vp.y(70f),
            vp.x(850f), vp.y(50f),
            vp.x(890f), vp.y(165f)
        )
        cubicTo(
            vp.x(900f), vp.y(215f),
            vp.x(835f), vp.y(245f),
            vp.x(760f), vp.y(242f)
        )
        cubicTo(
            vp.x(690f), vp.y(242f),
            vp.x(650f), vp.y(220f),
            vp.x(640f), vp.y(190f)
        )
        close()
    }
    drawPath(mantle, color = col, style = stroke)

    // Velke oci chobotnice
    drawOval(col, vp.pt(686f, 168f), Size(vp.x(26f), vp.y(34f)), style = stroke)
    drawOval(col, vp.pt(746f, 168f), Size(vp.x(26f), vp.y(34f)), style = stroke)
    drawCircle(col, radius = vp.s(6f), center = vp.pt(698f, 188f))
    drawCircle(col, radius = vp.s(6f), center = vp.pt(758f, 188f))
}

private fun DrawScope.drawOctopusTentacles(
    vp: LcdViewport,
    lengths: List<Int>,
    segColor: (Boolean) -> Color
) {
    // 4 chapadla smerujici k pozicim potapece 2, 3, 4 a 5 (u truhly)
    val tentaclePaths = listOf(
        // Chapadlo 0 (3 segmenty -> miri k pozici 2 vlevo)
        listOf(
            Offset(635f, 215f) to Offset(530f, 255f),
            Offset(530f, 255f) to Offset(425f, 305f),
            Offset(425f, 305f) to Offset(315f, 368f)
        ),
        // Chapadlo 1 (4 segmenty -> miri k pozici 3)
        listOf(
            Offset(680f, 238f) to Offset(610f, 285f),
            Offset(610f, 285f) to Offset(545f, 338f),
            Offset(545f, 338f) to Offset(485f, 392f),
            Offset(485f, 392f) to Offset(435f, 442f)
        ),
        // Chapadlo 2 (4 segmenty -> miri k pozici 4)
        listOf(
            Offset(745f, 244f) to Offset(705f, 294f),
            Offset(705f, 294f) to Offset(665f, 348f),
            Offset(665f, 348f) to Offset(625f, 400f),
            Offset(625f, 400f) to Offset(590f, 448f)
        ),
        // Chapadlo 3 (3 segmenty -> miri k pozici 5 pred truhlou)
        listOf(
            Offset(815f, 236f) to Offset(795f, 300f),
            Offset(795f, 300f) to Offset(775f, 366f),
            Offset(775f, 366f) to Offset(752f, 432f)
        )
    )

    for (tIdx in tentaclePaths.indices) {
        val segments = tentaclePaths[tIdx]
        val activeLen = lengths.getOrElse(tIdx) { 0 }
        for (sIdx in segments.indices) {
            val isLit = (sIdx + 1) <= activeLen
            val col = segColor(isLit)
            if (col == Color.Transparent) continue

            val (start, end) = segments[sIdx]
            drawLine(
                color = col,
                start = vp.pt(start.x, start.y),
                end = vp.pt(end.x, end.y),
                strokeWidth = vp.s(10f - sIdx * 1.4f),
                cap = StrokeCap.Round
            )
            // Prisavky na chapadle
            val midX = (start.x + end.x) * 0.5f
            val midY = (start.y + end.y) * 0.5f
            drawCircle(col, radius = vp.s(4.2f), center = vp.pt(midX - 8f, midY + 4f))
        }
    }
}

private fun DrawScope.drawDiverAtPosition(
    vp: LcdViewport,
    pos: Int,
    bodyCol: Color,
    bagCol: Color,
    reachCol: Color
) {
    val coords = arrayOf(
        Offset(176f, 94f),   // 0: Na palube lodi
        Offset(198f, 232f),  // 1: Na zebriku pod lodi
        Offset(282f, 438f),  // 2: Dno vlevo
        Offset(422f, 478f),  // 3: Dno stred-vlevo
        Offset(572f, 482f),  // 4: Dno stred-vpravo
        Offset(728f, 474f)   // 5: Dno u otevrene truhly
    )
    val pt = coords[pos.coerceIn(0, 5)]
    val cx = pt.x
    val cy = pt.y

    if (bodyCol != Color.Transparent) {
        // Kulata potapecska prilba s mrizkou
        drawCircle(
            color = bodyCol,
            radius = vp.s(15f),
            center = vp.pt(cx, cy - 26f),
            style = Stroke(width = vp.s(4f))
        )
        drawCircle(bodyCol, radius = vp.s(4.5f), center = vp.pt(cx + 5f, cy - 26f))

        // Trup potapece v olovenem skafandru
        drawRoundRect(
            color = bodyCol,
            topLeft = vp.pt(cx - 14f, cy - 10f),
            size = Size(vp.x(28f), vp.y(34f)),
            cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
        )

        // Nohy v tezkych potapecskych botach
        drawLine(bodyCol, vp.pt(cx - 8f, cy + 24f), vp.pt(cx - 16f, cy + 50f), vp.s(6.5f), StrokeCap.Round)
        drawLine(bodyCol, vp.pt(cx + 8f, cy + 24f), vp.pt(cx + 16f, cy + 50f), vp.s(6.5f), StrokeCap.Round)

        // Predni ruka potapece
        drawLine(bodyCol, vp.pt(cx + 10f, cy - 2f), vp.pt(cx + 28f, cy + 10f), vp.s(5.5f), StrokeCap.Round)
    }

    // Vak s pokladem na zadech potapece
    if (bagCol != Color.Transparent) {
        drawOval(
            color = bagCol,
            topLeft = vp.pt(cx - 34f, cy - 12f),
            size = Size(vp.x(22f), vp.y(28f))
        )
    }

    // Natazena ruka sahajici pro zlato primo do otevrene truhly (jen na pozici 5)
    if (pos == 5 && reachCol != Color.Transparent) {
        drawLine(
            color = reachCol,
            start = vp.pt(cx + 24f, cy + 4f),
            end = vp.pt(832f, 462f),
            strokeWidth = vp.s(6.5f),
            cap = StrokeCap.Round
        )
        drawCircle(reachCol, radius = vp.s(7f), center = vp.pt(836f, 462f))
    }
}

private fun DrawScope.drawCaughtDiverInTentacles(
    vp: LcdViewport,
    col: Color,
    wavePhase: Boolean
) {
    if (col == Color.Transparent) return
    val cx = 540f
    val cy = 228f

    drawCircle(col, radius = vp.s(14f), center = vp.pt(cx, cy - 20f), style = Stroke(width = vp.s(4f)))
    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 14f, cy - 4f),
        size = Size(vp.x(28f), vp.y(30f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
    )
    val armY = if (wavePhase) cy - 22f else cy + 12f
    drawLine(col, vp.pt(cx - 14f, cy + 2f), vp.pt(cx - 38f, armY), vp.s(5.5f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 14f, cy + 2f), vp.pt(cx + 38f, armY), vp.s(5.5f), StrokeCap.Round)
}

private fun DrawScope.drawOceanSevenSegDigit(
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
    val sw = vp.s(6f)
    val midY = y + dh * 0.5f
    val botY = y + dh
    val p = 3f

    fun col(bit: Int): Color = if ((mask and (1 shl bit)) != 0) onCol else offCol

    fun seg(bit: Int, x0: Float, y0: Float, x1: Float, y1: Float) {
        val c = col(bit)
        if (c != Color.Transparent) {
            drawLine(c, vp.pt(x0, y0), vp.pt(x1, y1), sw, StrokeCap.Round)
        }
    }

    seg(0, x + p, y, x + dw - p, y)
    seg(1, x, y + p, x, midY - p)
    seg(2, x + dw, y + p, x + dw, midY - p)
    seg(3, x + p, midY, x + dw - p, midY)
    seg(4, x, midY + p, x, botY - p)
    seg(5, x + dw, midY + p, x + dw, botY - p)
    seg(6, x + p, botY, x + dw - p, botY)
}
