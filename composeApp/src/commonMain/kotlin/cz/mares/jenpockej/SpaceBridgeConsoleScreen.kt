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

private val SpacePrintNavy = Color(0xFF223047)
private val SpacePrintSteel = Color(0xFF425970)
private val SpacePrintCliff = Color(0xFF6B5B52)
private val SpacePrintDarkRock = Color(0xFF3E332D)
private val SpacePrintLavaRed = Color(0xFFC93828)
private val SpacePrintLavaGold = Color(0xFFF2A93B)

// X souřadnice 3 zón mobilního mostu nad lávovou propastí
private val BRIDGE_ZONE_X = floatArrayOf(288f, 474f, 660f)

@Composable
fun SpaceBridgeConsoleBody(
    uiState: SpaceBridgeSnapshot,
    onBackToHub: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onStartA: () -> Unit,
    onStartB: () -> Unit,
    onResetDemo: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    val outerPlasticColor = Color(0xFF6E7D8C)
    val metalPlateBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFDCE2E8),
            Color(0xFFCBD3DC),
            Color(0xFFBAC4CE)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerPlasticColor)
            .padding(6.dp)
            .background(metalPlateBrush, RoundedCornerShape(10.dp))
            .border(2.dp, Color(0xFF4E5D6C), RoundedCornerShape(10.dp))
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
                                color = SpacePrintNavy,
                                radius = r,
                                style = Stroke(width = 2.8f)
                            )
                            drawCircle(
                                color = SpacePrintNavy,
                                radius = r * 0.16f,
                                center = Offset(center.x - r * 0.72f, center.y)
                            )
                        }
                        Text(
                            text = "ИМ",
                            color = SpacePrintNavy,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Text(
                        text = "◀ VÝBĚR HER",
                        color = SpacePrintLavaRed,
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
                    SpaceActionButton(
                        label = "◀ MOST VLEVO",
                        isActive = uiState.bridgePos > 0 && uiState.isRunning,
                        onClick = onMoveLeft
                    )
                }
            }

            // STŘEDNÍ PANEL: Rámeček s nápisem КОСМИЧЕСКИЙ МОСТ a LCD displejem
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

                            val topGapHalf = 104.dp.toPx()
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
                            drawPath(leftBracket, color = SpacePrintNavy, style = frameStroke)
                            drawPath(rightBracket, color = SpacePrintNavy, style = frameStroke)
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
                                        Color(0xFF425468),
                                        Color(0xFF63788E),
                                        Color(0xFF4C6075)
                                    )
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .border(2.dp, Color(0xFF2D3B4A), RoundedCornerShape(8.dp))
                            .padding(5.dp)
                    ) {
                        SpaceBridgeLcdScreen(
                            snapshot = uiState,
                            onLeftHalfTap = onMoveLeft,
                            onRightHalfTap = onMoveRight,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color(0xFF28343E), RoundedCornerShape(4.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(topHeaderH)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "КОСМИЧЕСКИЙ МОСТ",
                            color = SpacePrintLavaRed,
                            fontSize = 13.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(botFooterH)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ЭЛЕКТРОНИКА ИМ 09",
                            color = SpacePrintNavy,
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
                        color = SpacePrintNavy,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "© 2026 Created by Pavel Mareš",
                        color = SpacePrintNavy,
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
                        SpaceOvalMenuButton(
                            label = "игра А",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_A,
                            onClick = onStartA
                        )
                        SpaceOvalMenuButton(
                            label = "игра Б",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_B,
                            onClick = onStartB
                        )
                        SpaceOvalMenuButton(
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
                        SpaceSmallRoundButton(
                            label = if (uiState.soundEnabled) "ZVUK" else "TICH",
                            enabled = uiState.soundEnabled,
                            onClick = onToggleSound
                        )
                        SpaceSmallRoundButton(
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
                    SpaceActionButton(
                        label = "MOST VPRAVO ▶",
                        isActive = uiState.bridgePos < 2 && uiState.isRunning,
                        onClick = onMoveRight
                    )
                }
            }
        }
    }
}

@Composable
private fun SpaceActionButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val ringColor = if (isActive) Color(0xFFFFD166) else Color(0xFF4A5B6B)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFB4C0CC), Color(0xFF5F7080))
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
            color = SpacePrintNavy,
            fontSize = 8.sp,
            lineHeight = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SpaceOvalMenuButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outerRim = if (isSelected) SpacePrintLavaRed else Color(0xFF5E6F80)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF768799))
                .border(1.5.dp, outerRim, RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFDCE3EB), Color(0xFF93A3B3))
                    )
                )
        )
        Text(
            text = label,
            color = SpacePrintNavy,
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
private fun SpaceSmallRoundButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val centerColor = if (enabled) SpacePrintNavy else Color(0xFF6B7B8C)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF768799))
                .border(1.dp, Color(0xFF3E4E5E), CircleShape)
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(centerColor)
        )
        Text(
            text = label,
            color = SpacePrintNavy,
            fontSize = 7.sp,
            lineHeight = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun SpaceBridgeLcdScreen(
    snapshot: SpaceBridgeSnapshot,
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

            // 1. Barevný vesmírný potisk krycího skla (základna vlevo, láva dole, vozítko vpravo, Saturn)
            drawSpaceBridgeBackdrop(vp)

            // 2. Jemný stín aktivních LCD segmentů
            translate(left = vp.s(2.4f), top = vp.s(3.0f)) {
                drawSpaceBridgeLcdLayer(vp, snapshot, isShadowPass = true)
            }

            // 3. Hlavní vrstva LCD segmentů
            drawSpaceBridgeLcdLayer(vp, snapshot, isShadowPass = false)
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

private fun DrawScope.drawSpaceBridgeBackdrop(vp: LcdViewport) {
    val outline = Stroke(width = vp.s(2.8f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Planeta Saturn s prstencem a hvězdami nahoře uprostřed
    drawCircle(SpacePrintLavaGold, radius = vp.s(15f), center = vp.pt(268f, 52f))
    drawCircle(SpacePrintNavy, radius = vp.s(15f), center = vp.pt(268f, 52f), style = Stroke(width = vp.s(2.4f)))
    drawLine(SpacePrintNavy, vp.pt(238f, 60f), vp.pt(298f, 44f), vp.s(4.2f), StrokeCap.Round)

    // 2. Kosmická základna na levém skalním útesu se dvěma přechodovými komorami (horní a spodní)
    val leftCliff = Path().apply {
        moveTo(vp.x(0f), vp.y(196f))
        lineTo(vp.x(146f), vp.y(196f))
        lineTo(vp.x(166f), vp.y(242f))
        lineTo(vp.x(138f), vp.y(332f))
        lineTo(vp.x(158f), vp.y(382f))
        lineTo(vp.x(126f), vp.y(538f))
        lineTo(vp.x(0f), vp.y(538f))
        close()
    }
    drawPath(leftCliff, color = SpacePrintCliff)
    drawPath(leftCliff, color = SpacePrintDarkRock, style = outline)

    // Kopule základny a anténa vlevo nahoře
    val dome = Path().apply {
        moveTo(vp.x(14f), vp.y(196f))
        cubicTo(vp.x(18f), vp.y(96f), vp.x(122f), vp.y(96f), vp.x(132f), vp.y(196f))
        close()
    }
    drawPath(dome, color = SpacePrintSteel)
    drawPath(dome, color = SpacePrintNavy, style = outline)
    drawLine(SpacePrintNavy, vp.pt(72f, 118f), vp.pt(72f, 76f), vp.s(3.5f), StrokeCap.Round)
    drawCircle(SpacePrintLavaRed, radius = vp.s(4.5f), center = vp.pt(72f, 72f))

    // Horní a spodní výstupní rampa ze základny
    drawRoundRect(
        color = SpacePrintLavaGold,
        topLeft = vp.pt(108f, 152f),
        size = Size(vp.x(32f), vp.y(42f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
    drawRoundRect(
        color = SpacePrintLavaGold,
        topLeft = vp.pt(106f, 278f),
        size = Size(vp.x(30f), vp.y(38f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )

    // 3. Pravý skalní útes s přistávací plošinou a lunárním vozítkem
    val rightCliff = Path().apply {
        moveTo(vp.x(804f), vp.y(316f))
        lineTo(vp.x(1000f), vp.y(316f))
        lineTo(vp.x(1000f), vp.y(538f))
        lineTo(vp.x(782f), vp.y(538f))
        close()
    }
    drawPath(rightCliff, color = SpacePrintCliff)
    drawPath(rightCliff, color = SpacePrintDarkRock, style = outline)

    // Lunární vozítko (rover) čekající na pravém útesu
    drawRoundRect(
        color = SpacePrintSteel,
        topLeft = vp.pt(846f, 254f),
        size = Size(vp.x(126f), vp.y(48f)),
        cornerRadius = CornerRadius(vp.s(8f), vp.s(8f))
    )
    drawRoundRect(
        color = SpacePrintNavy,
        topLeft = vp.pt(846f, 254f),
        size = Size(vp.x(126f), vp.y(48f)),
        cornerRadius = CornerRadius(vp.s(8f), vp.s(8f)),
        style = outline
    )
    drawRoundRect(
        color = SpacePrintLavaGold,
        topLeft = vp.pt(858f, 264f),
        size = Size(vp.x(42f), vp.y(22f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
    for (wx in listOf(868f, 908f, 948f)) {
        drawCircle(SpacePrintDarkRock, radius = vp.s(12f), center = vp.pt(wx, 308f))
        drawCircle(SpacePrintLavaGold, radius = vp.s(5f), center = vp.pt(wx, 308f))
    }

    // 4. Vřící lávová řeka na dně propasti
    val lavaRiver = Path().apply {
        moveTo(vp.x(126f), vp.y(506f))
        var lx = 126f
        while (lx < 774f) {
            quadraticTo(vp.x(lx + 26f), vp.y(494f), vp.x(lx + 52f), vp.y(506f))
            lx += 52f
        }
        lineTo(vp.x(782f), vp.y(538f))
        lineTo(vp.x(126f), vp.y(538f))
        close()
    }
    drawPath(lavaRiver, color = SpacePrintLavaRed)
    drawLine(SpacePrintLavaGold, vp.pt(142f, 522f), vp.pt(766f, 522f), vp.s(4.5f), StrokeCap.Round)
}

private fun DrawScope.drawSpaceBridgeLcdLayer(
    vp: LcdViewport,
    snapshot: SpaceBridgeSnapshot,
    isShadowPass: Boolean
) {
    fun segColor(active: Boolean): Color {
        return if (isShadowPass) {
            if (active) LcdShadowTint else Color.Transparent
        } else {
            if (active) LcdInkOn else LcdInkGhost
        }
    }

    // 1. Čtyřmístné skóre nahoře uprostřed + 3 trestné přilby kosmonautů
    val clamped = snapshot.score.coerceIn(0, 9999)
    val digits = intArrayOf(
        if (clamped >= 1000) (clamped / 1000) % 10 else 0,
        (clamped / 100) % 10,
        (clamped / 10) % 10,
        clamped % 10
    )
    for (i in 0..3) {
        drawSpaceSevenSeg(
            vp = vp,
            x = 390f + i * 48f,
            y = 26f,
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
        drawPenaltyHelmet(vp, cx = 626f + idx * 52f, cy = 58f, col = segColor(isLit))
    }

    // 2. Orbitální družice vpravo nahoře (bonusový indikátor polovičního trestu)
    drawOrbitalSatellite(vp, col = segColor(snapshot.satelliteVisible))

    // 3. Kosmonauti skákající přes 3 oblouky (kroky 0..9)
    drawJumpingCosmonauts(vp, snapshot.jumpers, snapshot.roverBoardingTicks > 0, ::segColor)

    // 4. Mobilní plazmový most na 3 pozicích nad lávou (0 = vlevo, 1 = střed, 2 = vpravo)
    for (zone in 0..2) {
        val bridgeActive = snapshot.crashAnim == null && snapshot.bridgePos == zone
        drawHoverBridgeAtZone(vp, zone = zone, col = segColor(bridgeActive))
    }

    // 5. Animace pádu kosmonauta do lávy
    drawLavaSplashAnim(vp, snapshot.crashAnim, ::segColor)

    // 6. Nápisy GAME A / GAME B
    val gameACol = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_A)
    val gameBCol = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_B)
    drawSpaceModeLabel(vp, startX = 20f, topY = 558f, isB = false, col = gameACol)
    drawSpaceModeLabel(vp, startX = 858f, topY = 558f, isB = true, col = gameBCol)
}

private fun DrawScope.drawOrbitalSatellite(vp: LcdViewport, col: Color) {
    if (col == Color.Transparent) return
    val cx = 868f
    val cy = 72f
    drawCircle(col, radius = vp.s(12f), center = vp.pt(cx, cy))
    drawCircle(LcdBgColor, radius = vp.s(5f), center = vp.pt(cx, cy))
    // Solární panely družice
    drawLine(col, vp.pt(cx - 34f, cy - 10f), vp.pt(cx - 12f, cy - 3f), vp.s(5.5f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 12f, cy + 3f), vp.pt(cx + 34f, cy + 10f), vp.s(5.5f), StrokeCap.Round)
    drawLine(col, vp.pt(cx, cy + 12f), vp.pt(cx - 8f, cy + 26f), vp.s(3.0f), StrokeCap.Round)
}

private fun DrawScope.drawJumpingCosmonauts(
    vp: LcdViewport,
    jumpers: List<JumperCosmonaut>,
    roverBoarding: Boolean,
    segColor: (Boolean) -> Color
) {
    // 10 kroků parabolické dráhy kosmonauta (odrazové body nad mostem jsou step 2, 5, 8)
    val arcCoords = arrayOf(
        Offset(158f, 174f), // 0: Výskok z horní komory základny
        Offset(224f, 258f), // 1: Klesání do 1. zóny
        Offset(288f, 368f), // 2: ODRAZ 0 (nad levým mostem, x = 288)
        Offset(380f, 212f), // 3: Vrchol 1. oblouku
        Offset(432f, 282f), // 4: Klesání do 2. zóny
        Offset(474f, 368f), // 5: ODRAZ 1 (nad středním mostem, x = 474)
        Offset(566f, 212f), // 6: Vrchol 2. oblouku
        Offset(618f, 282f), // 7: Klesání do 3. zóny
        Offset(660f, 368f), // 8: ODRAZ 2 (nad pravým mostem, x = 660)
        Offset(814f, 266f)  // 9: Doskok na pravý útes k lunárnímu vozítku
    )

    // Spodní výskok pro Hru B
    val lowerStartLit = jumpers.any { it.step == 1 && it.fromLowerHatch }
    drawCosmonautFigure(vp, cx = 166f, cy = 298f, pose = 1, col = segColor(lowerStartLit))

    for (step in 0..9) {
        val isLit = if (step == 9) {
            roverBoarding || jumpers.any { it.step == 9 }
        } else if (step == 1) {
            jumpers.any { it.step == 1 && !it.fromLowerHatch }
        } else {
            jumpers.any { it.step == step }
        }
        val pt = arcCoords[step]
        drawCosmonautFigure(vp, cx = pt.x, cy = pt.y, pose = step % 3, col = segColor(isLit))
    }
}

private fun DrawScope.drawCosmonautFigure(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    pose: Int,
    col: Color
) {
    if (col == Color.Transparent) return
    val outline = Stroke(width = vp.s(3.2f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Batoh podpory života (PLSS) na zádech
    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 19f, cy - 10f),
        size = Size(vp.x(10f), vp.y(22f)),
        cornerRadius = CornerRadius(vp.s(3f), vp.s(3f))
    )

    // Kulatá kosmická přilba s průzorem
    drawCircle(color = LcdBgColor, radius = vp.s(12.5f), center = vp.pt(cx, cy - 20f))
    drawCircle(color = col, radius = vp.s(12.5f), center = vp.pt(cx, cy - 20f), style = outline)
    drawOval(color = col, topLeft = vp.pt(cx - 2f, cy - 25f), size = Size(vp.x(11f), vp.y(10f)))

    // Trup skafandru
    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 11f, cy - 8f),
        size = Size(vp.x(22f), vp.y(24f)),
        cornerRadius = CornerRadius(vp.s(5f), vp.s(5f))
    )

    // Ruce a nohy kosmonauta v letu
    val armLift = if (pose == 0) -14f else 6f
    val legSpread = if (pose == 2) 15f else 9f
    drawLine(col, vp.pt(cx - 10f, cy - 4f), vp.pt(cx - 22f, cy + armLift), vp.s(5.0f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 10f, cy - 4f), vp.pt(cx + 22f, cy + armLift), vp.s(5.0f), StrokeCap.Round)
    drawLine(col, vp.pt(cx - 6f, cy + 15f), vp.pt(cx - legSpread, cy + 32f), vp.s(5.8f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 6f, cy + 15f), vp.pt(cx + legSpread, cy + 32f), vp.s(5.8f), StrokeCap.Round)
}

private fun DrawScope.drawHoverBridgeAtZone(
    vp: LcdViewport,
    zone: Int,
    col: Color
) {
    if (col == Color.Transparent) return
    val cx = BRIDGE_ZONE_X[zone.coerceIn(0, 2)]
    val bridgeY = 426f

    // Pružná odrazová plošina mostu
    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 56f, bridgeY - 8f),
        size = Size(vp.x(112f), vp.y(14f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
    )
    // Nosná konstrukce vznášedla nad lávou a trysky
    val hull = Path().apply {
        moveTo(vp.x(cx - 48f), vp.y(bridgeY + 8f))
        lineTo(vp.x(cx + 48f), vp.y(bridgeY + 8f))
        lineTo(vp.x(cx + 36f), vp.y(bridgeY + 34f))
        lineTo(vp.x(cx - 36f), vp.y(bridgeY + 34f))
        close()
    }
    drawPath(hull, color = col)
    drawLine(LcdBgColor, vp.pt(cx - 32f, bridgeY + 18f), vp.pt(cx + 32f, bridgeY + 18f), vp.s(3.2f), StrokeCap.Round)

    // Dvě postavy operátorů držících most po stranách + trysky nad lávou
    drawCircle(col, radius = vp.s(8f), center = vp.pt(cx - 26f, bridgeY + 44f))
    drawCircle(col, radius = vp.s(8f), center = vp.pt(cx + 26f, bridgeY + 44f))
    drawLine(col, vp.pt(cx - 26f, bridgeY + 54f), vp.pt(cx - 32f, bridgeY + 66f), vp.s(3.5f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 26f, bridgeY + 54f), vp.pt(cx + 32f, bridgeY + 66f), vp.s(3.5f), StrokeCap.Round)
}

private fun DrawScope.drawLavaSplashAnim(
    vp: LcdViewport,
    crashAnim: BridgeLavaCrash?,
    segColor: (Boolean) -> Color
) {
    for (z in 0..2) {
        val active = crashAnim != null && crashAnim.zone == z
        val col = segColor(active)
        if (col == Color.Transparent) continue

        val cx = BRIDGE_ZONE_X[z]
        val cy = 492f
        // Stříkající láva a potápějící se přilba
        drawCircle(col, radius = vp.s(13f), center = vp.pt(cx, cy), style = Stroke(width = vp.s(3.6f)))
        drawLine(col, vp.pt(cx - 18f, cy - 4f), vp.pt(cx - 34f, cy - 22f), vp.s(4.2f), StrokeCap.Round)
        drawLine(col, vp.pt(cx + 18f, cy - 4f), vp.pt(cx + 34f, cy - 22f), vp.s(4.2f), StrokeCap.Round)
    }
}

private fun DrawScope.drawPenaltyHelmet(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    col: Color
) {
    if (col == Color.Transparent) return
    drawCircle(col, radius = vp.s(11f), center = vp.pt(cx, cy))
    drawOval(LcdBgColor, vp.pt(cx - 6f, cy - 6f), Size(vp.x(13f), vp.y(9f)))
}

private fun DrawScope.drawSpaceSevenSeg(
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

private fun DrawScope.drawSpaceModeLabel(
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
