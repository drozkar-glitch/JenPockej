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

private val OceanPrintBlue = Color(0xFF1E5B75)
private val OceanPrintLightSky = Color(0xFFD8E2D5)
private val OceanPrintTeal = Color(0xFF346B63)
private val OceanPrintKelp = Color(0xFF426E36)
private val OceanPrintCoral = Color(0xFFB5372A)
private val OceanPrintRock = Color(0xFF7B7362)
private val OceanPrintGold = Color(0xFFE09F24)
private val OceanPrintGoldBright = Color(0xFFF7C948)
private val OceanPrintBrown = Color(0xFF784720)
private val OceanPrintDarkWood = Color(0xFF4F2D12)

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
            // LEVÝ PANEL: Klikací logo •ИМ (návrat do výběru her) + tlačítka NA LOĎ a VLEVO
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                    Text(
                        text = "◀ VÝBĚR HER",
                        color = Color(0xFF1E5B75),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    OceanActionButton(
                        label = "▲ NA LOĎ",
                        isActive = uiState.diverPos <= 1 && uiState.isRunning,
                        onClick = onMoveLeftOrUp
                    )
                    OceanActionButton(
                        label = "◀ VLEVO",
                        isActive = uiState.diverPos in 2..5 && uiState.isRunning,
                        onClick = onMoveLeftOrUp
                    )
                }
            }

            // STŘEDNÍ PANEL: Rámeček s nápisem ТАЙНЫ ОКЕАНА a zapuštěným LCD displejem
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

            // PRAVÝ PANEL: 3 oválná tlačítka (игра А, игра Б, время) + ZVUK/VIBR + tlačítka VPRAVO a POKLAD
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
                        verticalArrangement = Arrangement.spacedBy(5.dp),
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
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    OceanActionButton(
                        label = "VPRAVO ▶",
                        isActive = uiState.diverPos in 0..4 && uiState.isRunning,
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
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFB5C2C4), Color(0xFF67787D))
                    )
                )
                .border(2.dp, ringColor, CircleShape)
                .clickable(onClick = onClick)
                .padding(4.5.dp)
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
            lineHeight = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 1.dp)
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
                .height(18.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF7D8E94))
                .border(1.5.dp, outerRim, RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(2.5.dp)
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
            lineHeight = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
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

            // 1. Barevný potisk mořského světa na krycím skle (loď, útes, korály, řasy, okovaná truhla)
            drawOceanPrintedBackdrop(vp)

            // 2. Jemný stín aktivních LCD segmentů
            translate(left = vp.s(2.4f), top = vp.s(3.0f)) {
                drawOceanLcdLayer(vp, snapshot, isShadowPass = true)
            }

            // 3. Hlavní vrstva autentických LCD segmentů
            drawOceanLcdLayer(vp, snapshot, isShadowPass = false)
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

private fun DrawScope.drawOceanPrintedBackdrop(vp: LcdViewport) {
    val outlineStroke = Stroke(width = vp.s(3.0f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Světlejší pás oblohy nad vodní hladinou
    drawRect(
        color = OceanPrintLightSky,
        topLeft = Offset.Zero,
        size = Size(vp.x(1000f), vp.y(144f))
    )

    // 2. Dřevěná potápěčská bárka na hladině vlevo nahoře (trup s prkny, příď, zábradlí a kotva)
    val hullOuter = Path().apply {
        moveTo(vp.x(18f), vp.y(104f))
        lineTo(vp.x(266f), vp.y(104f))
        lineTo(vp.x(242f), vp.y(148f))
        lineTo(vp.x(44f), vp.y(148f))
        close()
    }
    drawPath(hullOuter, color = OceanPrintBrown)
    drawPath(hullOuter, color = OceanPrintDarkWood, style = outlineStroke)

    // Palubní lišta a prkna obšití lodi
    drawLine(OceanPrintGold, vp.pt(22f, 112f), vp.pt(262f, 112f), vp.s(4.2f), StrokeCap.Round)
    drawLine(OceanPrintDarkWood, vp.pt(32f, 128f), vp.pt(252f, 128f), vp.s(2.4f), StrokeCap.Round)

    // Kulatá okénka (lukny) na boku bárky
    for (px in listOf(72f, 112f, 152f)) {
        drawCircle(OceanPrintGoldBright, radius = vp.s(5.5f), center = vp.pt(px, 136f))
        drawCircle(OceanPrintDarkWood, radius = vp.s(5.5f), center = vp.pt(px, 136f), style = Stroke(width = vp.s(2f)))
    }

    // Naviják se vzduchovou pumpou na přídi lodi
    drawRoundRect(
        color = OceanPrintDarkWood,
        topLeft = vp.pt(212f, 84f),
        size = Size(vp.x(26f), vp.y(20f)),
        cornerRadius = CornerRadius(vp.s(3f), vp.s(3f))
    )
    drawCircle(OceanPrintGold, radius = vp.s(9f), center = vp.pt(225f, 92f), style = Stroke(width = vp.s(3f)))

    // Provazový žebřík spuštěný z lodi pod hladinu
    drawLine(OceanPrintDarkWood, vp.pt(184f, 144f), vp.pt(184f, 246f), vp.s(4.2f), StrokeCap.Round)
    drawLine(OceanPrintDarkWood, vp.pt(216f, 144f), vp.pt(216f, 246f), vp.s(4.2f), StrokeCap.Round)
    for (ry in listOf(160f, 178f, 196f, 214f, 232f)) {
        drawLine(OceanPrintBrown, vp.pt(184f, ry), vp.pt(216f, ry), vp.s(3.6f), StrokeCap.Round)
    }

    // 3. Vlnitá mořská hladina po celé šířce displeje
    val wavePath = Path().apply {
        moveTo(vp.x(6f), vp.y(146f))
        var x = 6f
        while (x < 984f) {
            quadraticTo(vp.x(x + 18f), vp.y(134f), vp.x(x + 36f), vp.y(146f))
            quadraticTo(vp.x(x + 54f), vp.y(156f), vp.x(x + 72f), vp.y(146f))
            x += 72f
        }
    }
    drawPath(wavePath, color = OceanPrintBlue, style = Stroke(width = vp.s(4.8f), cap = StrokeCap.Round))

    // 4. Skalní útes vlevo, červený rozvětvený korál a zelené mořské řasy
    val leftRock = Path().apply {
        moveTo(vp.x(0f), vp.y(265f))
        lineTo(vp.x(42f), vp.y(295f))
        lineTo(vp.x(64f), vp.y(365f))
        lineTo(vp.x(52f), vp.y(440f))
        lineTo(vp.x(92f), vp.y(515f))
        lineTo(vp.x(128f), vp.y(542f))
        lineTo(vp.x(0f), vp.y(542f))
        close()
    }
    drawPath(leftRock, color = OceanPrintRock)
    drawPath(leftRock, color = OceanPrintDarkWood, style = Stroke(width = vp.s(2.6f), join = StrokeJoin.Round))

    // Větvící se červený korál na útesu
    val coralBranch = Path().apply {
        moveTo(vp.x(68f), vp.y(512f))
        lineTo(vp.x(68f), vp.y(410f))
        moveTo(vp.x(68f), vp.y(465f))
        lineTo(vp.x(38f), vp.y(430f))
        moveTo(vp.x(68f), vp.y(442f))
        lineTo(vp.x(98f), vp.y(404f))
        moveTo(vp.x(68f), vp.y(425f))
        lineTo(vp.x(52f), vp.y(392f))
    }
    drawPath(
        path = coralBranch,
        color = OceanPrintCoral,
        style = Stroke(width = vp.s(8.5f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Zelené vlnité mořské řasy (chaluhy) u dna vlevo i vpravo
    for (kx in listOf(118f, 142f, 164f, 962f, 982f)) {
        val kelp = Path().apply {
            moveTo(vp.x(kx), vp.y(540f))
            cubicTo(
                vp.x(kx - 14f), vp.y(500f),
                vp.x(kx + 14f), vp.y(462f),
                vp.x(kx - 6f), vp.y(426f)
            )
        }
        drawPath(
            path = kelp,
            color = OceanPrintKelp,
            style = Stroke(width = vp.s(6.0f), cap = StrokeCap.Round)
        )
    }

    // 5. Otevřená dřevěná okovaná truhla plná zlatých mincí vpravo dole
    // Zadní odklopené víko truhly
    val lidPath = Path().apply {
        moveTo(vp.x(812f), vp.y(458f))
        cubicTo(
            vp.x(818f), vp.y(416f),
            vp.x(944f), vp.y(416f),
            vp.x(956f), vp.y(458f)
        )
        close()
    }
    drawPath(lidPath, color = OceanPrintDarkWood)
    drawPath(lidPath, color = OceanPrintGold, style = Stroke(width = vp.s(3.2f)))

    // Hromada zlatých mincí a drahokamů v truhle
    for (mx in listOf(836f, 858f, 882f, 906f, 928f)) {
        drawCircle(OceanPrintGoldBright, radius = vp.s(13f), center = vp.pt(mx, 456f))
        drawCircle(OceanPrintDarkWood, radius = vp.s(13f), center = vp.pt(mx, 456f), style = Stroke(width = vp.s(1.8f)))
    }
    for (mx in listOf(848f, 872f, 896f, 918f)) {
        drawCircle(OceanPrintGold, radius = vp.s(11f), center = vp.pt(mx, 444f))
    }

    // Spodní truhlík s kováním a zámkem
    val chestBox = Path().apply {
        moveTo(vp.x(808f), vp.y(462f))
        lineTo(vp.x(960f), vp.y(462f))
        lineTo(vp.x(950f), vp.y(538f))
        lineTo(vp.x(818f), vp.y(538f))
        close()
    }
    drawPath(chestBox, color = OceanPrintBrown)
    drawPath(chestBox, color = OceanPrintDarkWood, style = outlineStroke)

    // Zlaté pásy kování a zámek truhly
    drawLine(OceanPrintGold, vp.pt(810f, 464f), vp.pt(958f, 464f), vp.s(5.5f), StrokeCap.Round)
    drawLine(OceanPrintGold, vp.pt(838f, 464f), vp.pt(842f, 536f), vp.s(5.5f))
    drawLine(OceanPrintGold, vp.pt(930f, 464f), vp.pt(926f, 536f), vp.s(5.5f))
    drawRoundRect(
        color = OceanPrintGoldBright,
        topLeft = vp.pt(874f, 474f),
        size = Size(vp.x(20f), vp.y(24f)),
        cornerRadius = CornerRadius(vp.s(3f), vp.s(3f))
    )
    drawCircle(OceanPrintDarkWood, radius = vp.s(3f), center = vp.pt(884f, 486f))

    // 6. Členité mořské dno s kameny
    val seabed = Path().apply {
        moveTo(vp.x(8f), vp.y(540f))
        lineTo(vp.x(210f), vp.y(540f))
        lineTo(vp.x(240f), vp.y(534f))
        lineTo(vp.x(380f), vp.y(542f))
        lineTo(vp.x(550f), vp.y(538f))
        lineTo(vp.x(740f), vp.y(542f))
        lineTo(vp.x(992f), vp.y(540f))
    }
    drawPath(seabed, color = OceanPrintTeal, style = Stroke(width = vp.s(6.5f), cap = StrokeCap.Round))
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

    // 1. Čtyřmístné zkosené 7segmentové skóre nahoře uprostřed
    val clamped = snapshot.score.coerceIn(0, 9999)
    val digits = intArrayOf(
        if (clamped >= 1000) (clamped / 1000) % 10 else 0,
        (clamped / 100) % 10,
        (clamped / 10) % 10,
        clamped % 10
    )
    for (i in 0..3) {
        drawOceanSlantedSevenSeg(
            vp = vp,
            x = 342f + i * 48f,
            y = 30f,
            dw = 32f,
            dh = 62f,
            value = digits[i],
            onCol = segColor(true),
            offCol = segColor(false)
        )
    }

    // 2. Posádka náhradních potápěčů na palubě lodi (indikátor zbývajících životů)
    val reserve1Lit = snapshot.lostDivers < 1
    val reserve2Lit = snapshot.lostDivers < 2
    drawDeckReserveDiver(vp, cx = 74f, cy = 74f, col = segColor(reserve1Lit))
    drawDeckReserveDiver(vp, cx = 128f, cy = 74f, col = segColor(reserve2Lit))

    // 3. Detailní obří chobotnice vpravo nahoře + 4 zakřivená článkovaná chapadla s přísavkami
    drawDetailedGiantOctopusHead(vp, col = segColor(true))
    drawCurvedOctopusTentacles(vp, snapshot.tentacleLengths, ::segColor)

    // 4. Potápěč na všech 6 pozicích (0 = na lodi, 1 = žebřík, 2..4 = dno, 5 = u truhly)
    val caughtActive = snapshot.caughtAnimFrame != null
    for (pos in 0..5) {
        val activeAtPos = !caughtActive && snapshot.diverPos == pos
        val hasBag = (activeAtPos && snapshot.bagGoldCount > 0) ||
            (pos == 0 && snapshot.unloadBonusTicks > 0)
        val reachingChest = pos == 5 && activeAtPos && snapshot.grabbingAtChest

        drawDetailedDiverAtPosition(
            vp = vp,
            pos = pos,
            bodyCol = segColor(activeAtPos),
            bagCol = segColor(hasBag),
            reachCol = segColor(reachingChest)
        )
    }

    // 5. Animace potápěče sevřeného v chapadlech chobotnice uprostřed
    val wavePhase = (snapshot.caughtAnimFrame ?: 0) % 2 == 0
    drawCaughtDiverInTentacles(
        vp = vp,
        col = segColor(caughtActive),
        wavePhase = wavePhase
    )

    // 6. Autentické LCD indikátory GAME A a GAME B v dolních rozích
    val gameAInk = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_A)
    val gameBInk = segColor(!snapshot.isRunning || snapshot.mode == GameMode.GAME_B)
    drawOceanModeWord(vp, startX = 24f, topY = 558f, isB = false, col = gameAInk)
    drawOceanModeWord(vp, startX = 858f, topY = 558f, isB = true, col = gameBInk)
}

private fun DrawScope.drawDeckReserveDiver(vp: LcdViewport, cx: Float, cy: Float, col: Color) {
    if (col == Color.Transparent) return
    val outline = Stroke(width = vp.s(3.2f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Kulatá potápěčská přilba s průzorem
    drawCircle(color = LcdBgColor, radius = vp.s(11f), center = vp.pt(cx, cy - 10f))
    drawCircle(color = col, radius = vp.s(11f), center = vp.pt(cx, cy - 10f), style = outline)
    drawCircle(color = col, radius = vp.s(4.5f), center = vp.pt(cx + 2f, cy - 10f))

    // Ramenní límec a trup potápěče v lodi
    val suitTorso = Path().apply {
        moveTo(vp.x(cx - 14f), vp.y(cy + 2f))
        lineTo(vp.x(cx + 14f), vp.y(cy + 2f))
        lineTo(vp.x(cx + 12f), vp.y(cy + 28f))
        lineTo(vp.x(cx - 12f), vp.y(cy + 28f))
        close()
    }
    drawPath(suitTorso, color = col)
    drawLine(LcdBgColor, vp.pt(cx - 9f, cy + 12f), vp.pt(cx + 9f, cy + 12f), vp.s(2.4f), StrokeCap.Round)
}

private fun DrawScope.drawDetailedGiantOctopusHead(vp: LcdViewport, col: Color) {
    if (col == Color.Transparent) return
    val outline = Stroke(width = vp.s(4.2f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Masivní vak hlavy (plášť) chobotnice podle originálního LCD IM-03
    val mantle = Path().apply {
        moveTo(vp.x(634f), vp.y(192f))
        cubicTo(
            vp.x(606f), vp.y(112f),
            vp.x(685f), vp.y(42f),
            vp.x(795f), vp.y(48f)
        )
        cubicTo(
            vp.x(895f), vp.y(54f),
            vp.x(946f), vp.y(132f),
            vp.x(906f), vp.y(198f)
        )
        cubicTo(
            vp.x(886f), vp.y(232f),
            vp.x(838f), vp.y(246f),
            vp.x(764f), vp.y(246f)
        )
        cubicTo(
            vp.x(692f), vp.y(246f),
            vp.x(646f), vp.y(224f),
            vp.x(634f), vp.y(192f)
        )
        close()
    }
    drawPath(mantle, color = col)

    // Vnitřní světlé vybrání hlavy s kresbou záhybů (aby hlava nebyla jen prázdný ovál ani jednolitý flek)
    val innerCutout = Path().apply {
        moveTo(vp.x(652f), vp.y(182f))
        cubicTo(
            vp.x(636f), vp.y(124f),
            vp.x(700f), vp.y(66f),
            vp.x(792f), vp.y(70f)
        )
        cubicTo(
            vp.x(874f), vp.y(74f),
            vp.x(914f), vp.y(136f),
            vp.x(882f), vp.y(188f)
        )
        cubicTo(
            vp.x(845f), vp.y(158f),
            vp.x(705f), vp.y(156f),
            vp.x(652f), vp.y(182f)
        )
        close()
    }
    drawPath(innerCutout, color = LcdBgColor)

    // Skvrny a vrásnění na hlavě chobotnice
    drawOval(col, vp.pt(765f, 88f), Size(vp.x(38f), vp.y(18f)), style = Stroke(width = vp.s(3.2f)))
    drawOval(col, vp.pt(825f, 108f), Size(vp.x(32f), vp.y(16f)), style = Stroke(width = vp.s(3.2f)))
    drawArc(
        color = col,
        startAngle = 200f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = vp.pt(680f, 86f),
        size = Size(vp.x(75f), vp.y(42f)),
        style = Stroke(width = vp.s(3.5f), cap = StrokeCap.Round)
    )

    // Boční sifon (tryska) chobotnice vpravo
    val siphon = Path().apply {
        moveTo(vp.x(896f), vp.y(178f))
        lineTo(vp.x(944f), vp.y(164f))
        lineTo(vp.x(952f), vp.y(194f))
        lineTo(vp.x(892f), vp.y(208f))
        close()
    }
    drawPath(siphon, color = LcdBgColor)
    drawPath(siphon, color = col, style = outline)

    // Výrazné nadočnicové oblouky a velké šelmovské oči chobotnice
    drawOval(LcdBgColor, vp.pt(676f, 162f), Size(vp.x(46f), vp.y(52f)))
    drawOval(col, vp.pt(676f, 162f), Size(vp.x(46f), vp.y(52f)), style = outline)
    drawCircle(col, radius = vp.s(10f), center = vp.pt(696f, 190f))
    drawCircle(LcdBgColor, radius = vp.s(3.2f), center = vp.pt(693f, 186f))

    drawOval(LcdBgColor, vp.pt(744f, 162f), Size(vp.x(46f), vp.y(52f)))
    drawOval(col, vp.pt(744f, 162f), Size(vp.x(46f), vp.y(52f)), style = outline)
    drawCircle(col, radius = vp.s(10f), center = vp.pt(764f, 190f))
    drawCircle(LcdBgColor, radius = vp.s(3.2f), center = vp.pt(761f, 186f))
}

private data class TentacleCurvedSeg(
    val x0: Float,
    val y0: Float,
    val cx: Float,
    val cy: Float,
    val x1: Float,
    val y1: Float,
    val strokeW: Float,
    val cup1: Offset,
    val cup2: Offset
)

private fun DrawScope.drawCurvedOctopusTentacles(
    vp: LcdViewport,
    lengths: List<Int>,
    segColor: (Boolean) -> Color
) {
    // 4 organicky prohnutá chapadla chobotnice s přísavkami
    val tentacles = listOf(
        // Chapadlo 0 (3 segmenty -> stáčí se doleva k pozici potápěče 2)
        listOf(
            TentacleCurvedSeg(646f, 214f, 578f, 226f, 522f, 260f, 15f, Offset(594f, 244f), Offset(552f, 262f)),
            TentacleCurvedSeg(522f, 260f, 456f, 286f, 404f, 326f, 12f, Offset(476f, 304f), Offset(434f, 324f)),
            TentacleCurvedSeg(404f, 326f, 344f, 352f, 304f, 396f, 9.5f, Offset(362f, 368f), Offset(326f, 390f))
        ),
        // Chapadlo 1 (4 segmenty -> stáčí se k pozici potápěče 3)
        listOf(
            TentacleCurvedSeg(692f, 236f, 646f, 262f, 608f, 294f, 15f, Offset(662f, 276f), Offset(628f, 296f)),
            TentacleCurvedSeg(608f, 294f, 566f, 322f, 530f, 356f, 12.5f, Offset(580f, 336f), Offset(550f, 358f)),
            TentacleCurvedSeg(530f, 356f, 492f, 384f, 464f, 414f, 10.5f, Offset(506f, 396f), Offset(480f, 416f)),
            TentacleCurvedSeg(464f, 414f, 442f, 434f, 434f, 458f, 8.5f, Offset(454f, 438f), Offset(444f, 454f))
        ),
        // Chapadlo 2 (4 segmenty -> stáčí se k pozici potápěče 4)
        listOf(
            TentacleCurvedSeg(756f, 242f, 732f, 276f, 706f, 308f, 15f, Offset(742f, 284f), Offset(722f, 306f)),
            TentacleCurvedSeg(706f, 308f, 678f, 340f, 652f, 372f, 12.5f, Offset(690f, 348f), Offset(670f, 370f)),
            TentacleCurvedSeg(652f, 372f, 628f, 400f, 606f, 428f, 10.5f, Offset(640f, 404f), Offset(620f, 424f)),
            TentacleCurvedSeg(606f, 428f, 588f, 444f, 582f, 464f, 8.5f, Offset(600f, 446f), Offset(592f, 460f))
        ),
        // Chapadlo 3 (3 segmenty -> stáčí se k pozici potápěče 5 přímo před truhlou)
        listOf(
            TentacleCurvedSeg(828f, 236f, 816f, 284f, 796f, 326f, 15f, Offset(824f, 286f), Offset(810f, 316f)),
            TentacleCurvedSeg(796f, 326f, 778f, 366f, 758f, 404f, 12f, Offset(788f, 368f), Offset(772f, 396f)),
            TentacleCurvedSeg(758f, 404f, 742f, 430f, 738f, 456f, 9.5f, Offset(754f, 428f), Offset(748f, 448f))
        )
    )

    for (tIdx in tentacles.indices) {
        val segList = tentacles[tIdx]
        val activeLen = lengths.getOrElse(tIdx) { 0 }
        for (sIdx in segList.indices) {
            val isLit = (sIdx + 1) <= activeLen
            val col = segColor(isLit)
            if (col == Color.Transparent) continue

            val seg = segList[sIdx]
            val curve = Path().apply {
                moveTo(vp.x(seg.x0), vp.y(seg.y0))
                quadraticTo(
                    vp.x(seg.cx), vp.y(seg.cy),
                    vp.x(seg.x1), vp.y(seg.y1)
                )
            }
            drawPath(
                path = curve,
                color = col,
                style = Stroke(width = vp.s(seg.strokeW), cap = StrokeCap.Round)
            )

            // Přísavky na spodní straně každého článku chapadla
            drawCircle(col, radius = vp.s(4.6f), center = vp.pt(seg.cup1.x, seg.cup1.y))
            drawCircle(LcdBgColor, radius = vp.s(1.6f), center = vp.pt(seg.cup1.x, seg.cup1.y))
            drawCircle(col, radius = vp.s(4.2f), center = vp.pt(seg.cup2.x, seg.cup2.y))
            drawCircle(LcdBgColor, radius = vp.s(1.5f), center = vp.pt(seg.cup2.x, seg.cup2.y))
        }
    }
}

private fun DrawScope.drawDetailedDiverAtPosition(
    vp: LcdViewport,
    pos: Int,
    bodyCol: Color,
    bagCol: Color,
    reachCol: Color
) {
    // Souřadnice 6 pozic potápěče (0 = loď, 1 = žebřík, 2..4 = dno, 5 = u truhly)
    val coords = arrayOf(
        Offset(176f, 68f),   // 0: Na palubě lodi u žebříku
        Offset(200f, 206f),  // 1: Sestupuje po žebříku pod hladinou
        Offset(278f, 468f),  // 2: Mořské dno vlevo
        Offset(418f, 488f),  // 3: Mořské dno střed-vlevo
        Offset(566f, 492f),  // 4: Mořské dno střed-vpravo
        Offset(718f, 484f)   // 5: Mořské dno u otevřené truhly s pokladem
    )
    val pt = coords[pos.coerceIn(0, 5)]
    val cx = pt.x
    val cy = pt.y
    val outline = Stroke(width = vp.s(3.6f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Měšec se zlatem nesený potápěčem (s uvázaným hrdlem)
    if (bagCol != Color.Transparent) {
        val bagX = cx - 34f
        val bagY = cy - 4f
        drawOval(
            color = bagCol,
            topLeft = vp.pt(bagX - 14f, bagY - 10f),
            size = Size(vp.x(28f), vp.y(32f))
        )
        // Hrdlo měšce
        val neck = Path().apply {
            moveTo(vp.x(bagX - 6f), vp.y(bagY - 10f))
            lineTo(vp.x(bagX - 11f), vp.y(bagY - 20f))
            lineTo(vp.x(bagX + 11f), vp.y(bagY - 20f))
            lineTo(vp.x(bagX + 6f), vp.y(bagY - 10f))
            close()
        }
        drawPath(neck, color = bagCol)
        drawLine(LcdBgColor, vp.pt(bagX - 7f, bagY - 10f), vp.pt(bagX + 7f, bagY - 10f), vp.s(2.4f), StrokeCap.Round)
    }

    if (bodyCol != Color.Transparent) {
        // 2. Kulatá potápěčská přilba s průzorem a mřížkou
        val headY = cy - 28f
        drawCircle(color = LcdBgColor, radius = vp.s(16f), center = vp.pt(cx, headY))
        drawCircle(color = bodyCol, radius = vp.s(16f), center = vp.pt(cx, headY), style = outline)
        // Kulaté okénko přilby s křížovou mřížkou
        drawCircle(color = bodyCol, radius = vp.s(8.5f), center = vp.pt(cx + 4f, headY), style = Stroke(width = vp.s(2.8f)))
        drawLine(bodyCol, vp.pt(cx - 4f, headY), vp.pt(cx + 12f, headY), vp.s(2.2f))
        drawLine(bodyCol, vp.pt(cx + 4f, headY - 8f), vp.pt(cx + 4f, headY + 8f), vp.s(2.2f))

        // Dýchací bublinky nad přilbou (pro potápěče pod vodou)
        if (pos >= 1) {
            drawCircle(bodyCol, radius = vp.s(3.2f), center = vp.pt(cx - 12f, headY - 22f), style = Stroke(width = vp.s(2.2f)))
            drawCircle(bodyCol, radius = vp.s(4.5f), center = vp.pt(cx - 6f, headY - 34f), style = Stroke(width = vp.s(2.2f)))
        }

        // 3. Trup potápěče ve skafandru s kovovým hrudním plátem
        val torso = Path().apply {
            moveTo(vp.x(cx - 16f), vp.y(cy - 12f))
            lineTo(vp.x(cx + 16f), vp.y(cy - 12f))
            lineTo(vp.x(cx + 14f), vp.y(cy + 20f))
            lineTo(vp.x(cx - 14f), vp.y(cy + 20f))
            close()
        }
        drawPath(torso, color = bodyCol)
        // Světlý hrudní límec a opasek se závažím
        drawArc(
            color = LcdBgColor,
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = vp.pt(cx - 12f, cy - 16f),
            size = Size(vp.x(24f), vp.y(14f)),
            style = Stroke(width = vp.s(2.8f), cap = StrokeCap.Round)
        )
        drawLine(LcdBgColor, vp.pt(cx - 13f, cy + 12f), vp.pt(cx + 13f, cy + 12f), vp.s(2.8f), StrokeCap.Round)

        // 4. Ruce potápěče
        // Zadní ruka držící měšec / žebřík
        drawLine(bodyCol, vp.pt(cx - 14f, cy - 6f), vp.pt(cx - 28f, cy + 4f), vp.s(7.0f), StrokeCap.Round)
        // Přední ruka (pokud zrovna nenabírá zlato z truhly)
        if (pos != 5) {
            val armEndY = if (pos == 1) cy - 18f else cy + 8f
            drawLine(bodyCol, vp.pt(cx + 14f, cy - 6f), vp.pt(cx + 30f, armEndY), vp.s(7.0f), StrokeCap.Round)
        }

        // 5. Rozkročené nohy s těžkými olověnými potápěčskými botami
        val stride = if (pos % 2 == 0) 14f else 9f
        // Levá noha + bota
        drawLine(bodyCol, vp.pt(cx - 7f, cy + 18f), vp.pt(cx - stride, cy + 44f), vp.s(8.0f), StrokeCap.Round)
        drawRoundRect(
            color = bodyCol,
            topLeft = vp.pt(cx - stride - 10f, cy + 40f),
            size = Size(vp.x(20f), vp.y(10f)),
            cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
        )
        // Pravá noha + bota
        drawLine(bodyCol, vp.pt(cx + 7f, cy + 18f), vp.pt(cx + stride, cy + 44f), vp.s(8.0f), StrokeCap.Round)
        drawRoundRect(
            color = bodyCol,
            topLeft = vp.pt(cx + stride - 6f, cy + 40f),
            size = Size(vp.x(20f), vp.y(10f)),
            cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
        )
    }

    // 6. Ruka potápěče na pozici 5 (normální vs. natažená do otevřené truhly pro zlato)
    if (pos == 5) {
        if (bodyCol != Color.Transparent) {
            drawLine(bodyCol, vp.pt(cx + 14f, cy - 4f), vp.pt(cx + 32f, cy + 10f), vp.s(6.5f), StrokeCap.Round)
        }
        if (reachCol != Color.Transparent) {
            drawLine(
                color = reachCol,
                start = vp.pt(cx + 14f, cy - 6f),
                end = vp.pt(836f, 456f),
                strokeWidth = vp.s(7.5f),
                cap = StrokeCap.Round
            )
            // Hrudka zlata v ruce nad truhlou
            drawCircle(reachCol, radius = vp.s(8.5f), center = vp.pt(842f, 454f))
        }
    }
}

private fun DrawScope.drawCaughtDiverInTentacles(
    vp: LcdViewport,
    col: Color,
    wavePhase: Boolean
) {
    if (col == Color.Transparent) return
    val cx = 536f
    val cy = 222f
    val outline = Stroke(width = vp.s(3.6f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Přilba zmítajícího se potápěče
    drawCircle(color = LcdBgColor, radius = vp.s(15f), center = vp.pt(cx, cy - 22f))
    drawCircle(color = col, radius = vp.s(15f), center = vp.pt(cx, cy - 22f), style = outline)
    drawCircle(color = col, radius = vp.s(6.5f), center = vp.pt(cx + 3f, cy - 22f))

    // Trup sevřený chapadlem
    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 15f, cy - 6f),
        size = Size(vp.x(30f), vp.y(32f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
    )
    drawLine(LcdBgColor, vp.pt(cx - 15f, cy + 8f), vp.pt(cx + 15f, cy + 12f), vp.s(4.0f), StrokeCap.Round)

    // Máchající ruce a nohy
    val armY = if (wavePhase) cy - 24f else cy + 14f
    val legSpread = if (wavePhase) 22f else 8f
    drawLine(col, vp.pt(cx - 14f, cy), vp.pt(cx - 40f, armY), vp.s(6.5f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 14f, cy), vp.pt(cx + 40f, armY), vp.s(6.5f), StrokeCap.Round)
    drawLine(col, vp.pt(cx - 7f, cy + 24f), vp.pt(cx - legSpread, cy + 48f), vp.s(7.0f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 7f, cy + 24f), vp.pt(cx + legSpread, cy + 48f), vp.s(7.0f), StrokeCap.Round)
}

private fun DrawScope.drawOceanSlantedSevenSeg(
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

private fun DrawScope.drawOceanModeWord(
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
