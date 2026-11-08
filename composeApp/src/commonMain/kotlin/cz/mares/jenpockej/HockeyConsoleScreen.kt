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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HockeyPrintRed = Color(0xFFC42B23)
private val HockeyPrintIceBlue = Color(0xFF28668C)
private val HockeyPrintCreaseBlue = Color(0xFF9ABBC8)
private val HockeyPrintBoardGold = Color(0xFFD99A2B)
private val HockeyPrintDark = Color(0xFF1E262E)

@Composable
fun HockeyConsoleBody(
    uiState: HockeySnapshot,
    onBackToHub: () -> Unit,
    onMoveGoalie: (BasketPosition) -> Unit,
    onStartA: () -> Unit,
    onStartB: () -> Unit,
    onResetDemo: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    val outerPlasticColor = Color(0xFF9AAAB4)
    val metalPlateBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE6ECEF),
            Color(0xFFD5DFE4),
            Color(0xFFC4D0D6)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerPlasticColor)
            .padding(6.dp)
            .background(metalPlateBrush, RoundedCornerShape(10.dp))
            .border(2.dp, Color(0xFF6C7E87), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEVÝ PANEL: Klikací logo •ИМ + 2 červená rohová tlačítka vlevo
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
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension * 0.46f
                            drawCircle(
                                color = HockeyPrintDark,
                                radius = r,
                                style = Stroke(width = 2.8f)
                            )
                            drawCircle(
                                color = HockeyPrintDark,
                                radius = r * 0.16f,
                                center = Offset(center.x - r * 0.72f, center.y)
                            )
                        }
                        Text(
                            text = "ИМ",
                            color = HockeyPrintDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Text(
                        text = "◀ VÝBĚR HER",
                        color = HockeyPrintRed,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    HockeyCornerButton(
                        isLeft = true,
                        isTop = true,
                        isActive = uiState.goaliePosition == BasketPosition.TOP_LEFT,
                        onClick = { onMoveGoalie(BasketPosition.TOP_LEFT) }
                    )
                    HockeyCornerButton(
                        isLeft = true,
                        isTop = false,
                        isActive = uiState.goaliePosition == BasketPosition.BOTTOM_LEFT,
                        onClick = { onMoveGoalie(BasketPosition.BOTTOM_LEFT) }
                    )
                }
            }

            // STŘEDNÍ PANEL: Rámeček s nápisem ХОККЕЙ a zapuštěným LCD displejem
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

                            val topGapHalf = 76.dp.toPx()
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
                            drawPath(leftBracket, color = HockeyPrintIceBlue, style = frameStroke)
                            drawPath(rightBracket, color = HockeyPrintIceBlue, style = frameStroke)
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
                                        Color(0xFF5D7A87),
                                        Color(0xFF83A1B0),
                                        Color(0xFF6B8A99)
                                    )
                                ),
                                RoundedCornerShape(8.dp)
                            )
                            .border(2.dp, Color(0xFF3E5661), RoundedCornerShape(8.dp))
                            .padding(5.dp)
                    ) {
                        HockeyLcdScreen(
                            snapshot = uiState,
                            onCornerTap = onMoveGoalie,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color(0xFF2F424A), RoundedCornerShape(4.dp))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(topHeaderH)
                            .align(Alignment.TopCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Х О К К Е Й",
                            color = HockeyPrintRed,
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.6.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(botFooterH)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ЭЛЕКТРОНИКА ИМ 22",
                            color = HockeyPrintDark,
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
                        color = HockeyPrintDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "© 2026 Created by Pavel Mareš",
                        color = HockeyPrintDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // PRAVÝ PANEL: 3 oválná tlačítka (игра А, игра Б, время) + ZVUK/VIBR + 2 červená rohová tlačítka vpravo
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
                        HockeyOvalMenuButton(
                            label = "игра А",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_A,
                            onClick = onStartA
                        )
                        HockeyOvalMenuButton(
                            label = "игра Б",
                            isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_B,
                            onClick = onStartB
                        )
                        HockeyOvalMenuButton(
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
                        HockeySmallRoundButton(
                            label = if (uiState.soundEnabled) "ZVUK" else "TICH",
                            enabled = uiState.soundEnabled,
                            onClick = onToggleSound
                        )
                        HockeySmallRoundButton(
                            label = if (uiState.hapticsEnabled) "VIBR" else "BEZ",
                            enabled = uiState.hapticsEnabled,
                            onClick = onToggleHaptics
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    HockeyCornerButton(
                        isLeft = false,
                        isTop = true,
                        isActive = uiState.goaliePosition == BasketPosition.TOP_RIGHT,
                        onClick = { onMoveGoalie(BasketPosition.TOP_RIGHT) }
                    )
                    HockeyCornerButton(
                        isLeft = false,
                        isTop = false,
                        isActive = uiState.goaliePosition == BasketPosition.BOTTOM_RIGHT,
                        onClick = { onMoveGoalie(BasketPosition.BOTTOM_RIGHT) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HockeyCornerButton(
    isLeft: Boolean,
    isTop: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isLeft) {
            HockeyArrowPointer(isLeft = true, isTop = isTop)
            Spacer(modifier = Modifier.width(4.dp))
        }

        val ringColor = if (isActive) Color(0xFFFFD166) else Color(0xFF52636B)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFB5C4CC), Color(0xFF657680))
                    )
                )
                .border(2.dp, ringColor, CircleShape)
                .clickable(onClick = onClick)
                .padding(5.5.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isActive) {
                            listOf(Color(0xFFFF5A4F), Color(0xFFD62828), Color(0xFF8F1515))
                        } else {
                            listOf(Color(0xFFE63930), Color(0xFFB8201A), Color(0xFF7A110E))
                        }
                    )
                )
                .border(1.2.dp, Color(0xFF520B09), CircleShape)
        )

        if (!isLeft) {
            Spacer(modifier = Modifier.width(4.dp))
            HockeyArrowPointer(isLeft = false, isTop = isTop)
        }
    }
}

@Composable
private fun HockeyArrowPointer(isLeft: Boolean, isTop: Boolean) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            if (isLeft && isTop) {
                moveTo(0f, 0f)
                lineTo(w, h * 0.35f)
                lineTo(w * 0.45f, h)
            } else if (isLeft && !isTop) {
                moveTo(0f, h)
                lineTo(w * 0.45f, 0f)
                lineTo(w, h * 0.65f)
            } else if (!isLeft && isTop) {
                moveTo(w, 0f)
                lineTo(0f, h * 0.35f)
                lineTo(w * 0.55f, h)
            } else {
                moveTo(w, h)
                lineTo(w * 0.55f, 0f)
                lineTo(0f, h * 0.65f)
            }
            close()
        }
        drawPath(path = path, color = HockeyPrintDark)
    }
}

@Composable
private fun HockeyOvalMenuButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outerRim = if (isSelected) HockeyPrintRed else Color(0xFF63747D)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF7E9099))
                .border(1.5.dp, outerRim, RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(3.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFDCE5E8), Color(0xFF98A9B3))
                    )
                )
        )
        Text(
            text = label,
            color = HockeyPrintDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun HockeySmallRoundButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val centerColor = if (enabled) HockeyPrintDark else Color(0xFF6E7F87)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF7E9099))
                .border(1.dp, Color(0xFF425259), CircleShape)
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(centerColor)
        )
        Text(
            text = label,
            color = HockeyPrintDark,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HockeyLcdScreen(
    snapshot: HockeySnapshot,
    onCornerTap: (BasketPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(LcdBgColor)
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    val isLeft = pos.x < size.width * 0.5f
                    val isTop = pos.y < size.height * 0.52f
                    val target = when {
                        isLeft && isTop -> BasketPosition.TOP_LEFT
                        isLeft && !isTop -> BasketPosition.BOTTOM_LEFT
                        !isLeft && isTop -> BasketPosition.TOP_RIGHT
                        else -> BasketPosition.BOTTOM_RIGHT
                    }
                    onCornerTap(target)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val vp = LcdViewport(size.width, size.height)

            // 1. Barevný potisk kluziště (mantinely, brankoviště, 4 útočníci s hokejkami, střelecké dráhy)
            drawHockeyRinkBackdrop(vp)

            // 2. Jemný stín aktivních LCD segmentů
            translate(left = vp.s(2.4f), top = vp.s(3.0f)) {
                drawHockeyLcdLayer(vp, snapshot, isShadowPass = true)
            }

            // 3. Hlavní vrstva LCD segmentů
            drawHockeyLcdLayer(vp, snapshot, isShadowPass = false)
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
                    text = "KONEC ZÁPASU • STISKNI ИГРА А / Б",
                    color = Color(0xFFCFD4C4),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

private fun DrawScope.drawHockeyRinkBackdrop(vp: LcdViewport) {
    // 1. Horní mantinel kluziště a box časoměřiče/rozhodčího vlevo nahoře
    drawLine(HockeyPrintIceBlue, vp.pt(8f, 146f), vp.pt(992f, 146f), vp.s(4.5f), StrokeCap.Round)
    drawLine(HockeyPrintBoardGold, vp.pt(8f, 153f), vp.pt(992f, 153f), vp.s(4.0f), StrokeCap.Round)

    // Box rozhodčího za mantinelem vlevo nahoře
    drawRoundRect(
        color = HockeyPrintIceBlue,
        topLeft = vp.pt(126f, 44f),
        size = Size(vp.x(66f), vp.y(96f)),
        cornerRadius = CornerRadius(vp.s(5f), vp.s(5f)),
        style = Stroke(width = vp.s(4.5f))
    )

    // 2. Modré půlkruhové brankoviště a červená hokejová branka uprostřed za brankářem
    drawArc(
        color = HockeyPrintCreaseBlue,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = vp.pt(336f, 434f),
        size = Size(vp.x(328f), vp.y(140f))
    )
    drawArc(
        color = HockeyPrintRed,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = vp.pt(336f, 434f),
        size = Size(vp.x(328f), vp.y(140f)),
        style = Stroke(width = vp.s(3.5f))
    )

    // Červená konstrukce hokejové branky za zády brankáře
    val goalFrame = Path().apply {
        moveTo(vp.x(364f), vp.y(498f))
        lineTo(vp.x(376f), vp.y(226f))
        lineTo(vp.x(624f), vp.y(226f))
        lineTo(vp.x(636f), vp.y(498f))
    }
    drawPath(
        path = goalFrame,
        color = HockeyPrintRed,
        style = Stroke(width = vp.s(9.5f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Jemná síť branky
    for (nx in listOf(410f, 455f, 500f, 545f, 590f)) {
        drawLine(HockeyPrintCreaseBlue, vp.pt(nx, 232f), vp.pt(nx, 492f), vp.s(1.8f))
    }
    for (ny in listOf(275f, 330f, 385f, 440f)) {
        drawLine(HockeyPrintCreaseBlue, vp.pt(374f, ny), vp.pt(626f, ny), vp.s(1.8f))
    }

    // 3. Čtyři modré střelecké dráhy na ledě (vedoucí od hokejek 4 útočníků k brankáři)
    drawIceLaneLine(vp, x0 = 6f, y0 = 212f, x1 = 76f, y1 = 212f, x2 = 248f, y2 = 292f)
    drawIceLaneLine(vp, x0 = 6f, y0 = 354f, x1 = 76f, y1 = 354f, x2 = 248f, y2 = 434f)
    drawIceLaneLine(vp, x0 = 994f, y0 = 212f, x1 = 924f, y1 = 212f, x2 = 752f, y2 = 292f)
    drawIceLaneLine(vp, x0 = 994f, y0 = 354f, x1 = 924f, y1 = 354f, x2 = 752f, y2 = 434f)

    // 4. Čtyři hokejoví útočníci v dresech s napřaženými hokejkami v rozích kluziště
    drawPrintedSkater(vp, cx = 42f, cy = 168f, facingRight = true, jerseyColor = HockeyPrintRed)
    drawPrintedSkater(vp, cx = 42f, cy = 310f, facingRight = true, jerseyColor = HockeyPrintIceBlue)
    drawPrintedSkater(vp, cx = 958f, cy = 168f, facingRight = false, jerseyColor = HockeyPrintRed)
    drawPrintedSkater(vp, cx = 958f, cy = 310f, facingRight = false, jerseyColor = HockeyPrintIceBlue)

    // Spodní branková čára po celé šířce kluziště
    drawLine(HockeyPrintRed, vp.pt(12f, 536f), vp.pt(988f, 536f), vp.s(5.5f), StrokeCap.Round)
}

private fun DrawScope.drawIceLaneLine(
    vp: LcdViewport,
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float
) {
    val lanePath = Path().apply {
        moveTo(vp.x(x0), vp.y(y0))
        lineTo(vp.x(x1), vp.y(y1))
        lineTo(vp.x(x2), vp.y(y2))
    }
    drawPath(
        path = lanePath,
        color = HockeyPrintIceBlue,
        style = Stroke(width = vp.s(7.5f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawPrintedSkater(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    facingRight: Boolean,
    jerseyColor: Color
) {
    val dir = if (facingRight) 1f else -1f
    // Helma a hlava útočníka
    drawCircle(jerseyColor, radius = vp.s(11f), center = vp.pt(cx, cy - 16f))
    drawCircle(Color(0xFFF5F6F0), radius = vp.s(6.5f), center = vp.pt(cx + dir * 4f, cy - 14f))

    // Dres útočníka
    val jersey = Path().apply {
        moveTo(vp.x(cx - dir * 16f), vp.y(cy - 4f))
        lineTo(vp.x(cx + dir * 14f), vp.y(cy - 4f))
        lineTo(vp.x(cx + dir * 18f), vp.y(cy + 24f))
        lineTo(vp.x(cx - dir * 14f), vp.y(cy + 24f))
        close()
    }
    drawPath(jersey, color = jerseyColor)
    drawLine(Color.White, vp.pt(cx - 12f, cy + 12f), vp.pt(cx + 12f, cy + 12f), vp.s(3.0f))

    // Hokejka útočníka položená na ledě
    val stick = Path().apply {
        moveTo(vp.x(cx + dir * 6f), vp.y(cy + 8f))
        lineTo(vp.x(cx + dir * 28f), vp.y(cy + 36f))
        lineTo(vp.x(cx + dir * 42f), vp.y(cy + 34f))
    }
    drawPath(
        path = stick,
        color = HockeyPrintDark,
        style = Stroke(width = vp.s(4.2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawHockeyLcdLayer(
    vp: LcdViewport,
    snapshot: HockeySnapshot,
    isShadowPass: Boolean
) {
    fun segColor(active: Boolean): Color {
        return if (isShadowPass) {
            if (active) LcdShadowTint else Color.Transparent
        } else {
            if (active) LcdInkOn else LcdInkGhost
        }
    }

    // 1. Rozhodčí v pruhovaném dresu vlevo nahoře s píšťalkou a praporkem
    drawHockeyReferee(
        vp = vp,
        refCol = segColor(snapshot.refereeVisible),
        armUpCol = segColor(snapshot.refereeVisible && snapshot.refereeArmUp),
        armDownCol = segColor(snapshot.refereeVisible && !snapshot.refereeArmUp)
    )

    // 2. Čtyřmístné skóre a 3 trestné brankové majáky pod ním
    val clamped = snapshot.score.coerceIn(0, 9999)
    val digits = intArrayOf(
        if (clamped >= 1000) (clamped / 1000) % 10 else 0,
        (clamped / 100) % 10,
        (clamped / 10) % 10,
        clamped % 10
    )
    for (i in 0..3) {
        drawHockeySevenSeg(
            vp = vp,
            x = 552f + i * 49f,
            y = 32f,
            dw = 33f,
            dh = 64f,
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
        drawPenaltyGoalLamp(vp, cx = 576f + idx * 60f, cy = 132f, col = segColor(isLit))
    }

    // 3. Hokejový brankář uprostřed branky (levý a pravý postoj) + 4 pozice lapačky/hokejky
    val facingLeft = snapshot.goaliePosition == BasketPosition.TOP_LEFT ||
        snapshot.goaliePosition == BasketPosition.BOTTOM_LEFT
    val celebrating = snapshot.celebrationTicks > 0
    val leftGoalieCol = segColor(facingLeft || (celebrating && snapshot.blinkPhase))
    val rightGoalieCol = segColor(!facingLeft || (celebrating && !snapshot.blinkPhase))

    drawGoalieBodyPose(vp, baseX = 446f, facingLeft = true, col = leftGoalieCol)
    drawGoalieBodyPose(vp, baseX = 554f, facingLeft = false, col = rightGoalieCol)

    drawGoalieBlockerAndCatcher(
        vp = vp,
        shoulderX = 418f,
        shoulderY = 342f,
        gloveX = 312f,
        gloveY = 316f,
        isTop = true,
        facingLeft = true,
        col = segColor(snapshot.goaliePosition == BasketPosition.TOP_LEFT)
    )
    drawGoalieBlockerAndCatcher(
        vp = vp,
        shoulderX = 414f,
        shoulderY = 372f,
        gloveX = 310f,
        gloveY = 456f,
        isTop = false,
        facingLeft = true,
        col = segColor(snapshot.goaliePosition == BasketPosition.BOTTOM_LEFT)
    )
    drawGoalieBlockerAndCatcher(
        vp = vp,
        shoulderX = 582f,
        shoulderY = 342f,
        gloveX = 688f,
        gloveY = 316f,
        isTop = true,
        facingLeft = false,
        col = segColor(snapshot.goaliePosition == BasketPosition.TOP_RIGHT)
    )
    drawGoalieBlockerAndCatcher(
        vp = vp,
        shoulderX = 586f,
        shoulderY = 372f,
        gloveX = 690f,
        gloveY = 456f,
        isTop = false,
        facingLeft = false,
        col = segColor(snapshot.goaliePosition == BasketPosition.BOTTOM_RIGHT)
    )

    // 4. Rotující hokejové puky na 4 střeleckých drahách
    drawSlidingPucks(vp, snapshot.pucks, ::segColor)

    // 5. Animace inkasovaného gólu a nápisy GAME A / GAME B
    drawConcededGoalAnim(vp, snapshot.goalAnim, ::segColor)

    val gameACol = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_A)
    val gameBCol = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_B)
    drawHockeyModeLabel(vp, startX = 24f, topY = 556f, isB = false, col = gameACol)
    drawHockeyModeLabel(vp, startX = 858f, topY = 556f, isB = true, col = gameBCol)
}

private fun DrawScope.drawHockeyReferee(
    vp: LcdViewport,
    refCol: Color,
    armUpCol: Color,
    armDownCol: Color
) {
    val rx = 224f
    val ry = 96f
    val outline = Stroke(width = vp.s(3.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    if (refCol != Color.Transparent) {
        // Pruhovaný dres rozhodčího
        val shirt = Path().apply {
            moveTo(vp.x(rx - 36f), vp.y(ry + 6f))
            lineTo(vp.x(rx + 6f), vp.y(ry + 6f))
            lineTo(vp.x(rx + 12f), vp.y(ry + 38f))
            lineTo(vp.x(rx - 36f), vp.y(ry + 38f))
            close()
        }
        drawPath(shirt, color = refCol)
        for (sx in listOf(-24f, -12f, 0f)) {
            drawLine(LcdBgColor, vp.pt(rx + sx, ry + 8f), vp.pt(rx + sx, ry + 36f), vp.s(3.0f))
        }

        // Hlava rozhodčího s přilbou a píšťalkou
        drawCircle(LcdBgColor, radius = vp.s(15f), center = vp.pt(rx - 6f, ry - 10f))
        drawCircle(refCol, radius = vp.s(15f), center = vp.pt(rx - 6f, ry - 10f), style = outline)
        drawCircle(refCol, radius = vp.s(2.8f), center = vp.pt(rx + 1f, ry - 12f))
        // Píšťalka v ústech
        drawLine(refCol, vp.pt(rx + 8f, ry - 6f), vp.pt(rx + 22f, ry - 4f), vp.s(4.5f), StrokeCap.Round)
    }

    if (armUpCol != Color.Transparent) {
        drawLine(armUpCol, vp.pt(rx + 6f, ry + 14f), vp.pt(rx + 48f, ry - 18f), vp.s(5.5f), StrokeCap.Round)
        val flag = Path().apply {
            moveTo(vp.x(rx + 48f), vp.y(ry - 18f))
            lineTo(vp.x(rx + 72f), vp.y(ry - 10f))
            lineTo(vp.x(rx + 48f), vp.y(ry - 2f))
            close()
        }
        drawPath(flag, color = armUpCol)
    }

    if (armDownCol != Color.Transparent) {
        drawLine(armDownCol, vp.pt(rx + 6f, ry + 20f), vp.pt(rx + 44f, ry + 34f), vp.s(5.5f), StrokeCap.Round)
        drawCircle(armDownCol, radius = vp.s(5.5f), center = vp.pt(rx + 46f, ry + 35f))
    }
}

private fun DrawScope.drawGoalieBodyPose(
    vp: LcdViewport,
    baseX: Float,
    facingLeft: Boolean,
    col: Color
) {
    if (col == Color.Transparent) return
    val dir = if (facingLeft) -1f else 1f
    val outline = Stroke(width = vp.s(3.6f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Hokejová brankářská maska s průduchy a mřížkou
    val maskCenter = vp.pt(baseX + dir * 4f, 278f)
    drawOval(
        color = LcdBgColor,
        topLeft = vp.pt(baseX + dir * 4f - 20f, 252f),
        size = Size(vp.x(40f), vp.y(48f))
    )
    drawOval(
        color = col,
        topLeft = vp.pt(baseX + dir * 4f - 20f, 252f),
        size = Size(vp.x(40f), vp.y(48f)),
        style = outline
    )
    // Oči a mřížka brankářské masky
    drawCircle(col, radius = vp.s(3.2f), center = Offset(maskCenter.x + vp.x(dir * 6f), maskCenter.y - vp.y(6f)))
    drawLine(col, vp.pt(baseX + dir * 2f, 282f), vp.pt(baseX + dir * 18f, 282f), vp.s(2.4f))
    drawLine(col, vp.pt(baseX + dir * 2f, 290f), vp.pt(baseX + dir * 16f, 290f), vp.s(2.4f))
    drawLine(col, vp.pt(baseX + dir * 10f, 276f), vp.pt(baseX + dir * 10f, 295f), vp.s(2.4f))

    // 2. Mohutný brankářský dres s číslem a chrániči ramen
    val jersey = Path().apply {
        moveTo(vp.x(baseX - dir * 24f), vp.y(304f))
        lineTo(vp.x(baseX + dir * 26f), vp.y(304f))
        lineTo(vp.x(baseX + dir * 30f), vp.y(406f))
        lineTo(vp.x(baseX - dir * 26f), vp.y(406f))
        close()
    }
    drawPath(jersey, color = col)
    // Světlý pruh na dresu a hvězda na hrudi
    drawLine(LcdBgColor, vp.pt(baseX - 24f, 386f), vp.pt(baseX + 24f, 386f), vp.s(4.2f), StrokeCap.Round)
    drawCircle(LcdBgColor, radius = vp.s(6.5f), center = vp.pt(baseX + dir * 4f, 346f))

    // 3. Široké brankářské betony (chrániče nohou) a brusle na ledě
    for (legOffset in listOf(-16f, 16f)) {
        val lx = baseX + legOffset
        drawRoundRect(
            color = LcdBgColor,
            topLeft = vp.pt(lx - 13f, 408f),
            size = Size(vp.x(26f), vp.y(78f)),
            cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
        )
        drawRoundRect(
            color = col,
            topLeft = vp.pt(lx - 13f, 408f),
            size = Size(vp.x(26f), vp.y(78f)),
            cornerRadius = CornerRadius(vp.s(6f), vp.s(6f)),
            style = outline
        )
        // Řemínky betonů
        drawLine(col, vp.pt(lx - 10f, 430f), vp.pt(lx + 10f, 430f), vp.s(2.6f))
        drawLine(col, vp.pt(lx - 10f, 456f), vp.pt(lx + 10f, 456f), vp.s(2.6f))
        // Nože bruslí
        drawLine(col, vp.pt(lx - 16f, 492f), vp.pt(lx + 16f, 492f), vp.s(4.5f), StrokeCap.Round)
    }
}

private fun DrawScope.drawGoalieBlockerAndCatcher(
    vp: LcdViewport,
    shoulderX: Float,
    shoulderY: Float,
    gloveX: Float,
    gloveY: Float,
    isTop: Boolean,
    facingLeft: Boolean,
    col: Color
) {
    if (col == Color.Transparent) return
    val dir = if (facingLeft) -1f else 1f
    val elbowX = (shoulderX + gloveX) * 0.52f
    val elbowY = if (isTop) shoulderY - 6f else shoulderY + 34f

    // Ruka brankáře
    val arm = Path().apply {
        moveTo(vp.x(shoulderX), vp.y(shoulderY))
        lineTo(vp.x(elbowX), vp.y(elbowY))
        lineTo(vp.x(gloveX - dir * 18f), vp.y(gloveY))
    }
    drawPath(arm, color = col, style = Stroke(width = vp.s(9.0f), cap = StrokeCap.Round, join = StrokeJoin.Round))

    if (isTop) {
        // Horní pozice: otevřená brankářská lapačka (koš) připravená chytit puk z horní dráhy
        drawOval(
            color = LcdBgColor,
            topLeft = vp.pt(gloveX - 28f, gloveY - 22f),
            size = Size(vp.x(56f), vp.y(44f))
        )
        drawOval(
            color = col,
            topLeft = vp.pt(gloveX - 28f, gloveY - 22f),
            size = Size(vp.x(56f), vp.y(44f)),
            style = Stroke(width = vp.s(4.5f))
        )
        // Výplet lapačky
        drawLine(col, vp.pt(gloveX - 18f, gloveY), vp.pt(gloveX + 18f, gloveY), vp.s(2.6f))
        drawLine(col, vp.pt(gloveX, gloveY - 14f), vp.pt(gloveX, gloveY + 14f), vp.s(2.6f))
    } else {
        // Spodní pozice: široká brankářská hokejka a vyrážečka na ledě
        drawRoundRect(
            color = col,
            topLeft = vp.pt(gloveX - 28f, gloveY - 12f),
            size = Size(vp.x(56f), vp.y(22f)),
            cornerRadius = CornerRadius(vp.s(5f), vp.s(5f))
        )
        // Široká čepel brankářské hole na ledě
        val blade = Path().apply {
            moveTo(vp.x(gloveX - dir * 10f), vp.y(gloveY + 6f))
            lineTo(vp.x(gloveX + dir * 28f), vp.y(gloveY + 24f))
            lineTo(vp.x(gloveX - dir * 32f), vp.y(gloveY + 26f))
            close()
        }
        drawPath(blade, color = col)
    }
}

private fun DrawScope.drawSlidingPucks(
    vp: LcdViewport,
    activePucks: List<PuckItem>,
    segColor: (Boolean) -> Color
) {
    val xOffsets = floatArrayOf(0f, 44f, 88f, 132f, 176f)
    val yOffsets = floatArrayOf(0f, 21f, 42f, 63f, 92f)
    val angles = floatArrayOf(-15f, 25f, -35f, 45f, -10f)

    for (lane in 0..3) {
        val isLeft = lane == 0 || lane == 1
        for (step in 0..4) {
            val isLit = activePucks.any { it.lane == lane && it.step == step }
            val col = segColor(isLit)
            if (col == Color.Transparent) continue

            val dx = xOffsets[step]
            val dy = yOffsets[step]
            val pos = when (lane) {
                0 -> vp.pt(94f + dx, 194f + dy)
                1 -> vp.pt(94f + dx, 336f + dy)
                2 -> vp.pt(906f - dx, 194f + dy)
                else -> vp.pt(906f - dx, 336f + dy)
            }
            val tilt = if (isLeft) angles[step] else -angles[step]

            rotate(degrees = tilt, pivot = pos) {
                // Hokejový puk (3D černý kotouč se světlou horní hranou)
                drawRoundRect(
                    color = col,
                    topLeft = Offset(pos.x - vp.x(18f), pos.y - vp.y(9f)),
                    size = Size(vp.x(36f), vp.y(18f)),
                    cornerRadius = CornerRadius(vp.s(7f), vp.s(7f))
                )
                drawOval(
                    color = LcdBgColor,
                    topLeft = Offset(pos.x - vp.x(13f), pos.y - vp.y(6f)),
                    size = Size(vp.x(26f), vp.y(8f))
                )
            }
        }
    }
}

private fun DrawScope.drawConcededGoalAnim(
    vp: LcdViewport,
    goalAnim: GoalConcededAnim?,
    segColor: (Boolean) -> Color
) {
    for (side in 0..1) {
        val isLeft = side == 0
        val active = goalAnim != null && goalAnim.leftSide == isLeft
        val col = segColor(active)
        if (col == Color.Transparent) continue

        val gx = if (isLeft) 232f else 768f
        val gy = 516f
        // Puk v síti a záblesk gólové sirény
        drawRoundRect(
            color = col,
            topLeft = vp.pt(gx - 20f, gy - 8f),
            size = Size(vp.x(40f), vp.y(16f)),
            cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
        )
        drawLine(col, vp.pt(gx - 28f, gy - 14f), vp.pt(gx - 42f, gy - 28f), vp.s(4.0f), StrokeCap.Round)
        drawLine(col, vp.pt(gx, gy - 16f), vp.pt(gx, gy - 34f), vp.s(4.0f), StrokeCap.Round)
        drawLine(col, vp.pt(gx + 28f, gy - 14f), vp.pt(gx + 42f, gy - 28f), vp.s(4.0f), StrokeCap.Round)
    }
}

private fun DrawScope.drawPenaltyGoalLamp(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    col: Color
) {
    if (col == Color.Transparent) return
    // Maják brankového světla s paprsky
    val dome = Path().apply {
        moveTo(vp.x(cx - 12f), vp.y(cy + 10f))
        cubicTo(vp.x(cx - 12f), vp.y(cy - 10f), vp.x(cx + 12f), vp.y(cy - 10f), vp.x(cx + 12f), vp.y(cy + 10f))
        close()
    }
    drawPath(dome, color = col)
    drawLine(col, vp.pt(cx - 16f, cy + 12f), vp.pt(cx + 16f, cy + 12f), vp.s(4.0f), StrokeCap.Round)
    drawLine(col, vp.pt(cx, cy - 10f), vp.pt(cx, cy - 18f), vp.s(2.8f), StrokeCap.Round)
    drawLine(col, vp.pt(cx - 12f, cy - 6f), vp.pt(cx - 19f, cy - 13f), vp.s(2.8f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 12f, cy - 6f), vp.pt(cx + 19f, cy - 13f), vp.s(2.8f), StrokeCap.Round)
}

private fun DrawScope.drawHockeySevenSeg(
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
    val sw = vp.s(6.2f)
    val midY = y + dh * 0.5f
    val botY = y + dh
    val p = 3.0f
    val slantTop = 3.4f
    val slantMid = 1.7f

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

private fun DrawScope.drawHockeyModeLabel(
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
