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
import kotlin.math.sqrt

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
        size = Size(vp.x(1000f), vp.y(140f))
    )

    // 2. Dřevěná potápěčská bárka na hladině vlevo nahoře (prodloužená, aby se posádka nepřekrývala)
    val hullOuter = Path().apply {
        moveTo(vp.x(16f), vp.y(102f))
        lineTo(vp.x(282f), vp.y(102f))
        lineTo(vp.x(256f), vp.y(142f))
        lineTo(vp.x(38f), vp.y(142f))
        close()
    }
    drawPath(hullOuter, color = OceanPrintBrown)
    drawPath(hullOuter, color = OceanPrintDarkWood, style = outlineStroke)

    // Palubní lišta a prkna obšití lodi
    drawLine(OceanPrintGold, vp.pt(20f, 109f), vp.pt(278f, 109f), vp.s(4.0f), StrokeCap.Round)
    drawLine(OceanPrintDarkWood, vp.pt(30f, 124f), vp.pt(266f, 124f), vp.s(2.4f), StrokeCap.Round)

    // Kulatá okénka (lukny) na boku bárky
    for (px in listOf(64f, 106f, 148f)) {
        drawCircle(OceanPrintGoldBright, radius = vp.s(5.0f), center = vp.pt(px, 132f))
        drawCircle(OceanPrintDarkWood, radius = vp.s(5.0f), center = vp.pt(px, 132f), style = Stroke(width = vp.s(1.8f)))
    }

    // Naviják se vzduchovou pumpou posunutý na příď lodi vpravo od potápěče
    drawRoundRect(
        color = OceanPrintDarkWood,
        topLeft = vp.pt(238f, 82f),
        size = Size(vp.x(24f), vp.y(20f)),
        cornerRadius = CornerRadius(vp.s(3f), vp.s(3f))
    )
    drawCircle(OceanPrintGold, radius = vp.s(8.5f), center = vp.pt(250f, 90f), style = Stroke(width = vp.s(2.8f)))

    // Provazový žebřík spuštěný z lodi pod hladinu (prodloužený pro potápěče na pozici 1)
    drawLine(OceanPrintDarkWood, vp.pt(186f, 140f), vp.pt(186f, 278f), vp.s(4.2f), StrokeCap.Round)
    drawLine(OceanPrintDarkWood, vp.pt(218f, 140f), vp.pt(218f, 278f), vp.s(4.2f), StrokeCap.Round)
    for (ry in listOf(158f, 178f, 198f, 218f, 238f, 258f, 274f)) {
        drawLine(OceanPrintBrown, vp.pt(186f, ry), vp.pt(218f, ry), vp.s(3.4f), StrokeCap.Round)
    }

    // 3. Vlnitá mořská hladina po celé šířce displeje (nad hlavou chobotnice)
    val wavePath = Path().apply {
        moveTo(vp.x(6f), vp.y(140f))
        var x = 6f
        while (x < 984f) {
            quadraticTo(vp.x(x + 18f), vp.y(130f), vp.x(x + 36f), vp.y(140f))
            quadraticTo(vp.x(x + 54f), vp.y(150f), vp.x(x + 72f), vp.y(140f))
            x += 72f
        }
    }
    drawPath(wavePath, color = OceanPrintBlue, style = Stroke(width = vp.s(4.6f), cap = StrokeCap.Round))

    // 4. Skalní útes vlevo, červený rozvětvený korál a zelené mořské řasy
    val leftRock = Path().apply {
        moveTo(vp.x(0f), vp.y(270f))
        lineTo(vp.x(40f), vp.y(300f))
        lineTo(vp.x(62f), vp.y(368f))
        lineTo(vp.x(50f), vp.y(442f))
        lineTo(vp.x(90f), vp.y(515f))
        lineTo(vp.x(124f), vp.y(540f))
        lineTo(vp.x(0f), vp.y(540f))
        close()
    }
    drawPath(leftRock, color = OceanPrintRock)
    drawPath(leftRock, color = OceanPrintDarkWood, style = Stroke(width = vp.s(2.6f), join = StrokeJoin.Round))

    // Větvící se červený korál na útesu
    val coralBranch = Path().apply {
        moveTo(vp.x(66f), vp.y(512f))
        lineTo(vp.x(66f), vp.y(412f))
        moveTo(vp.x(66f), vp.y(466f))
        lineTo(vp.x(38f), vp.y(432f))
        moveTo(vp.x(66f), vp.y(444f))
        lineTo(vp.x(96f), vp.y(406f))
        moveTo(vp.x(66f), vp.y(426f))
        lineTo(vp.x(50f), vp.y(394f))
    }
    drawPath(
        path = coralBranch,
        color = OceanPrintCoral,
        style = Stroke(width = vp.s(8.5f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Zelené vlnité mořské řasy (chaluhy) u dna vlevo i vpravo
    for (kx in listOf(118f, 142f, 164f, 964f, 984f)) {
        val kelp = Path().apply {
            moveTo(vp.x(kx), vp.y(538f))
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

    for (mx in listOf(836f, 858f, 882f, 906f, 928f)) {
        drawCircle(OceanPrintGoldBright, radius = vp.s(13f), center = vp.pt(mx, 456f))
        drawCircle(OceanPrintDarkWood, radius = vp.s(13f), center = vp.pt(mx, 456f), style = Stroke(width = vp.s(1.8f)))
    }
    for (mx in listOf(848f, 872f, 896f, 918f)) {
        drawCircle(OceanPrintGold, radius = vp.s(11f), center = vp.pt(mx, 444f))
    }

    val chestBox = Path().apply {
        moveTo(vp.x(808f), vp.y(462f))
        lineTo(vp.x(960f), vp.y(462f))
        lineTo(vp.x(950f), vp.y(538f))
        lineTo(vp.x(818f), vp.y(538f))
        close()
    }
    drawPath(chestBox, color = OceanPrintBrown)
    drawPath(chestBox, color = OceanPrintDarkWood, style = outlineStroke)

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

    // 6. Pevná linie mořského dna, na které stojí potápěči
    val seabed = Path().apply {
        moveTo(vp.x(8f), vp.y(538f))
        lineTo(vp.x(220f), vp.y(538f))
        lineTo(vp.x(380f), vp.y(538f))
        lineTo(vp.x(560f), vp.y(538f))
        lineTo(vp.x(750f), vp.y(538f))
        lineTo(vp.x(992f), vp.y(538f))
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

    // 2. Posádka náhradních potápěčů na zádi lodi (s dostatečným odstupem od potápěče s měšcem)
    val reserve1Lit = snapshot.lostDivers < 1
    val reserve2Lit = snapshot.lostDivers < 2
    drawDeckReserveDiver(vp, cx = 52f, cy = 72f, col = segColor(reserve1Lit))
    drawDeckReserveDiver(vp, cx = 96f, cy = 72f, col = segColor(reserve2Lit))

    // 3. Detailní obří chobotnice pod vodní hladinou + 4 zvlněná chapadla s přísavkami
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

    drawCircle(color = LcdBgColor, radius = vp.s(10.5f), center = vp.pt(cx, cy - 10f))
    drawCircle(color = col, radius = vp.s(10.5f), center = vp.pt(cx, cy - 10f), style = outline)
    drawCircle(color = col, radius = vp.s(4.2f), center = vp.pt(cx + 2f, cy - 10f))

    val suitTorso = Path().apply {
        moveTo(vp.x(cx - 13f), vp.y(cy + 2f))
        lineTo(vp.x(cx + 13f), vp.y(cy + 2f))
        lineTo(vp.x(cx + 11f), vp.y(cy + 28f))
        lineTo(vp.x(cx - 11f), vp.y(cy + 28f))
        close()
    }
    drawPath(suitTorso, color = col)
    drawLine(LcdBgColor, vp.pt(cx - 8f, cy + 12f), vp.pt(cx + 8f, cy + 12f), vp.s(2.4f), StrokeCap.Round)
}

private fun DrawScope.drawDetailedGiantOctopusHead(vp: LcdViewport, col: Color) {
    if (col == Color.Transparent) return
    val outline = Stroke(width = vp.s(4.5f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Masivní vak hlavy (plášť) chobotnice posazený pod vodní hladinou (y = 152..278)
    val mantle = Path().apply {
        moveTo(vp.x(636f), vp.y(242f))
        cubicTo(
            vp.x(612f), vp.y(182f),
            vp.x(692f), vp.y(148f),
            vp.x(796f), vp.y(152f)
        )
        cubicTo(
            vp.x(894f), vp.y(156f),
            vp.x(946f), vp.y(204f),
            vp.x(906f), vp.y(256f)
        )
        cubicTo(
            vp.x(884f), vp.y(278f),
            vp.x(830f), vp.y(284f),
            vp.x(762f), vp.y(282f)
        )
        cubicTo(
            vp.x(694f), vp.y(280f),
            vp.x(650f), vp.y(268f),
            vp.x(636f), vp.y(242f)
        )
        close()
    }
    drawPath(mantle, color = LcdBgColor)
    drawPath(mantle, color = col, style = outline)

    // Horní stínovací žebra a skvrny na plášti chobotnice (autentická perokresba bez "škrabošky")
    val dorsalShade = Path().apply {
        moveTo(vp.x(666f), vp.y(194f))
        cubicTo(
            vp.x(710f), vp.y(160f),
            vp.x(845f), vp.y(162f),
            vp.x(892f), vp.y(204f)
        )
        cubicTo(
            vp.x(840f), vp.y(178f),
            vp.x(720f), vp.y(176f),
            vp.x(666f), vp.y(194f)
        )
        close()
    }
    drawPath(dorsalShade, color = col)

    drawOval(col, vp.pt(768f, 176f), Size(vp.x(38f), vp.y(16f)), style = Stroke(width = vp.s(3.2f)))
    drawOval(col, vp.pt(830f, 192f), Size(vp.x(32f), vp.y(15f)), style = Stroke(width = vp.s(3.2f)))
    drawArc(
        color = col,
        startAngle = 195f,
        sweepAngle = 135f,
        useCenter = false,
        topLeft = vp.pt(670f, 198f),
        size = Size(vp.x(66f), vp.y(34f)),
        style = Stroke(width = vp.s(3.8f), cap = StrokeCap.Round)
    )
    drawArc(
        color = col,
        startAngle = 205f,
        sweepAngle = 135f,
        useCenter = false,
        topLeft = vp.pt(738f, 198f),
        size = Size(vp.x(66f), vp.y(34f)),
        style = Stroke(width = vp.s(3.8f), cap = StrokeCap.Round)
    )

    // Boční sifon (tryska) chobotnice vpravo
    val siphon = Path().apply {
        moveTo(vp.x(902f), vp.y(226f))
        lineTo(vp.x(948f), vp.y(214f))
        lineTo(vp.x(956f), vp.y(242f))
        lineTo(vp.x(896f), vp.y(252f))
        close()
    }
    drawPath(siphon, color = LcdBgColor)
    drawPath(siphon, color = col, style = Stroke(width = vp.s(4.0f), join = StrokeJoin.Round))

    // Velké oči chobotnice s nadočnicovým stínem
    drawOval(LcdBgColor, vp.pt(680f, 212f), Size(vp.x(46f), vp.y(50f)))
    drawOval(col, vp.pt(680f, 212f), Size(vp.x(46f), vp.y(50f)), style = outline)
    drawCircle(col, radius = vp.s(10.5f), center = vp.pt(700f, 238f))
    drawCircle(LcdBgColor, radius = vp.s(3.2f), center = vp.pt(697f, 234f))

    drawOval(LcdBgColor, vp.pt(748f, 212f), Size(vp.x(46f), vp.y(50f)))
    drawOval(col, vp.pt(748f, 212f), Size(vp.x(46f), vp.y(50f)), style = outline)
    drawCircle(col, radius = vp.s(10.5f), center = vp.pt(768f, 238f))
    drawCircle(LcdBgColor, radius = vp.s(3.2f), center = vp.pt(765f, 234f))
}

private data class TentacleCurvedSeg(
    val x0: Float,
    val y0: Float,
    val cx: Float,
    val cy: Float,
    val x1: Float,
    val y1: Float,
    val strokeW: Float
)

private fun DrawScope.drawCurvedOctopusTentacles(
    vp: LcdViewport,
    lengths: List<Int>,
    segColor: (Boolean) -> Color
) {
    // 4 výrazně zvlněná chapadla chobotnice, jejichž poslední segment sahá těsně nad přilbu potápěče (y ~ 424..428)
    val tentacles = listOf(
        // Chapadlo 0 (3 segmenty -> vlní se k potápěči na pozici 2, x = 282)
        listOf(
            TentacleCurvedSeg(646f, 256f, 556f, 232f, 502f, 298f, 15.5f),
            TentacleCurvedSeg(502f, 298f, 452f, 362f, 382f, 354f, 12.5f),
            TentacleCurvedSeg(382f, 354f, 312f, 348f, 288f, 424f, 9.8f)
        ),
        // Chapadlo 1 (4 segmenty -> vlní se k potápěči na pozici 3, x = 424)
        listOf(
            TentacleCurvedSeg(696f, 274f, 636f, 268f, 608f, 312f, 15.0f),
            TentacleCurvedSeg(608f, 312f, 580f, 356f, 532f, 350f, 12.8f),
            TentacleCurvedSeg(532f, 350f, 484f, 344f, 464f, 388f, 10.8f),
            TentacleCurvedSeg(464f, 388f, 446f, 424f, 426f, 428f, 9.0f)
        ),
        // Chapadlo 2 (4 segmenty -> vlní se k potápěči na pozici 4, x = 570)
        listOf(
            TentacleCurvedSeg(758f, 280f, 738f, 318f, 700f, 322f, 15.0f),
            TentacleCurvedSeg(700f, 322f, 660f, 326f, 648f, 364f, 12.8f),
            TentacleCurvedSeg(648f, 364f, 636f, 402f, 602f, 398f, 10.8f),
            TentacleCurvedSeg(602f, 398f, 578f, 396f, 574f, 428f, 9.0f)
        ),
        // Chapadlo 3 (3 segmenty -> vlní se k potápěči na pozici 5 před truhlou, x = 720)
        listOf(
            TentacleCurvedSeg(828f, 272f, 832f, 322f, 790f, 334f, 15.5f),
            TentacleCurvedSeg(790f, 334f, 748f, 346f, 760f, 386f, 12.5f),
            TentacleCurvedSeg(760f, 386f, 770f, 420f, 728f, 426f, 9.8f)
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

            // Přísavky vypočtené přesně na spodní/vnější hraně kvadratické křivky chapadla
            for (t in floatArrayOf(0.32f, 0.70f)) {
                val inv = 1f - t
                val bx = inv * inv * seg.x0 + 2f * inv * t * seg.cx + t * t * seg.x1
                val by = inv * inv * seg.y0 + 2f * inv * t * seg.cy + t * t * seg.y1

                // Tečna křivky B'(t) a kolmice směřující na spodní stranu chapadla
                val tx = 2f * inv * (seg.cx - seg.x0) + 2f * t * (seg.x1 - seg.cx)
                val ty = 2f * inv * (seg.cy - seg.y0) + 2f * t * (seg.y1 - seg.cy)
                val len = sqrt(tx * tx + ty * ty).coerceAtLeast(1f)
                // Jednotková normála otočená dolů/doprava
                var nx = -ty / len
                var ny = tx / len
                if (ny < 0f) {
                    nx = -nx
                    ny = -ny
                }

                val offsetDist = seg.strokeW * 0.58f
                val cupX = bx + nx * offsetDist
                val cupY = by + ny * offsetDist

                drawCircle(col, radius = vp.s(4.2f), center = vp.pt(cupX, cupY))
                drawCircle(LcdBgColor, radius = vp.s(1.5f), center = vp.pt(cupX, cupY))
            }
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
    // Souřadnice 6 pozic potápěče:
    // 0 = na palubě lodi (mimo posádku i naviják), 1 = na žebříku pod trupem lodi,
    // 2..5 = olověnými botami pevně na mořském dně (y = 488 -> boty na 538)
    val coords = arrayOf(
        Offset(186f, 56f),   // 0: Na palubě lodi u žebříku
        Offset(202f, 226f),  // 1: Sestupuje po žebříku pod trupem lodi
        Offset(282f, 488f),  // 2: Mořské dno vlevo
        Offset(424f, 488f),  // 3: Mořské dno střed-vlevo
        Offset(570f, 488f),  // 4: Mořské dno střed-vpravo
        Offset(720f, 488f)   // 5: Mořské dno u otevřené truhly s pokladem
    )
    val pt = coords[pos.coerceIn(0, 5)]
    val cx = pt.x
    val cy = pt.y
    val outline = Stroke(width = vp.s(3.6f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Měšec se zlatem nesený potápěčem (s uvázaným hrdlem)
    if (bagCol != Color.Transparent) {
        val bagX = cx - 32f
        val bagY = cy - 2f
        drawOval(
            color = bagCol,
            topLeft = vp.pt(bagX - 13f, bagY - 9f),
            size = Size(vp.x(26f), vp.y(30f))
        )
        val neck = Path().apply {
            moveTo(vp.x(bagX - 5f), vp.y(bagY - 9f))
            lineTo(vp.x(bagX - 10f), vp.y(bagY - 18f))
            lineTo(vp.x(bagX + 10f), vp.y(bagY - 18f))
            lineTo(vp.x(bagX + 5f), vp.y(bagY - 9f))
            close()
        }
        drawPath(neck, color = bagCol)
        drawLine(LcdBgColor, vp.pt(bagX - 6f, bagY - 9f), vp.pt(bagX + 6f, bagY - 9f), vp.s(2.3f), StrokeCap.Round)
    }

    if (bodyCol != Color.Transparent) {
        // 2. Kulatá potápěčská přilba s průzorem a mřížkou
        val headY = cy - 28f
        drawCircle(color = LcdBgColor, radius = vp.s(15.5f), center = vp.pt(cx, headY))
        drawCircle(color = bodyCol, radius = vp.s(15.5f), center = vp.pt(cx, headY), style = outline)
        drawCircle(color = bodyCol, radius = vp.s(8.2f), center = vp.pt(cx + 4f, headY), style = Stroke(width = vp.s(2.6f)))
        drawLine(bodyCol, vp.pt(cx - 4f, headY), vp.pt(cx + 12f, headY), vp.s(2.1f))
        drawLine(bodyCol, vp.pt(cx + 4f, headY - 8f), vp.pt(cx + 4f, headY + 8f), vp.s(2.1f))

        // Dýchací bublinky nad přilbou (pro potápěče pod vodou)
        if (pos >= 1) {
            drawCircle(bodyCol, radius = vp.s(3.0f), center = vp.pt(cx - 13f, headY - 20f), style = Stroke(width = vp.s(2.0f)))
            drawCircle(bodyCol, radius = vp.s(4.2f), center = vp.pt(cx - 7f, headY - 31f), style = Stroke(width = vp.s(2.0f)))
        }

        // 3. Trup potápěče ve skafandru s kovovým hrudním plátem
        val torso = Path().apply {
            moveTo(vp.x(cx - 15f), vp.y(cy - 12f))
            lineTo(vp.x(cx + 15f), vp.y(cy - 12f))
            lineTo(vp.x(cx + 13f), vp.y(cy + 20f))
            lineTo(vp.x(cx - 13f), vp.y(cy + 20f))
            close()
        }
        drawPath(torso, color = bodyCol)
        drawArc(
            color = LcdBgColor,
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = vp.pt(cx - 11f, cy - 16f),
            size = Size(vp.x(22f), vp.y(13f)),
            style = Stroke(width = vp.s(2.6f), cap = StrokeCap.Round)
        )
        drawLine(LcdBgColor, vp.pt(cx - 12f, cy + 12f), vp.pt(cx + 12f, cy + 12f), vp.s(2.6f), StrokeCap.Round)

        // 4. Ruce potápěče
        drawLine(bodyCol, vp.pt(cx - 14f, cy - 6f), vp.pt(cx - 26f, cy + 4f), vp.s(6.8f), StrokeCap.Round)
        if (pos != 5) {
            val armEndX = if (pos == 0) cx + 24f else cx + 28f
            val armEndY = if (pos == 1) cy - 16f else cy + 8f
            drawLine(bodyCol, vp.pt(cx + 14f, cy - 6f), vp.pt(armEndX, armEndY), vp.s(6.8f), StrokeCap.Round)
        }

        // 5. Rozkročené nohy s těžkými olověnými potápěčskými botami
        val stride = if (pos % 2 == 0) 13f else 9f
        drawLine(bodyCol, vp.pt(cx - 7f, cy + 18f), vp.pt(cx - stride, cy + 44f), vp.s(7.8f), StrokeCap.Round)
        drawRoundRect(
            color = bodyCol,
            topLeft = vp.pt(cx - stride - 10f, cy + 40f),
            size = Size(vp.x(20f), vp.y(10f)),
            cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
        )
        drawLine(bodyCol, vp.pt(cx + 7f, cy + 18f), vp.pt(cx + stride, cy + 44f), vp.s(7.8f), StrokeCap.Round)
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
            drawLine(bodyCol, vp.pt(cx + 14f, cy - 4f), vp.pt(cx + 30f, cy + 10f), vp.s(6.5f), StrokeCap.Round)
        }
        if (reachCol != Color.Transparent) {
            drawLine(
                color = reachCol,
                start = vp.pt(cx + 14f, cy - 6f),
                end = vp.pt(836f, 456f),
                strokeWidth = vp.s(7.2f),
                cap = StrokeCap.Round
            )
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
    val cx = 524f
    val cy = 236f
    val outline = Stroke(width = vp.s(3.6f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    drawCircle(color = LcdBgColor, radius = vp.s(15f), center = vp.pt(cx, cy - 22f))
    drawCircle(color = col, radius = vp.s(15f), center = vp.pt(cx, cy - 22f), style = outline)
    drawCircle(color = col, radius = vp.s(6.5f), center = vp.pt(cx + 3f, cy - 22f))

    drawRoundRect(
        color = col,
        topLeft = vp.pt(cx - 15f, cy - 6f),
        size = Size(vp.x(30f), vp.y(32f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
    )
    drawLine(LcdBgColor, vp.pt(cx - 15f, cy + 8f), vp.pt(cx + 15f, cy + 12f), vp.s(4.0f), StrokeCap.Round)

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
