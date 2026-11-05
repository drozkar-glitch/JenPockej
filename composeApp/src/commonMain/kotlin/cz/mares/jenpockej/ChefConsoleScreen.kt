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
private val ChefPrintGold = Color(0xFFE5A93C)
private val ChefPrintBrown = Color(0xFF754624)
private val ChefPrintDarkWood = Color(0xFF4A2B13)
private val ChefPrintTile = Color(0xFFBFC8B8)
private val ChefPrintRed = Color(0xFFB52B24)

// X souřadnice 4 sloupců jídla (0 = klobása, 1 = ryba, 2 = kotleta, 3 = volské oko)
private val CHEF_FOOD_COL_X = floatArrayOf(224f, 356f, 488f, 620f)
// Y souřadnice 5 výškových úrovní jídla (0 = nejvyšší bod pod hrnci .. 4 = těsně nad pánvičkou)
// Všechny úrovně končí v y = 322, tedy čistě nad čepicí kuchaře (y = 352..534)!
private val CHEF_FOOD_STEP_Y = floatArrayOf(106f, 160f, 214f, 268f, 322f)

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

    val maxChefCol = if (uiState.isRunning && uiState.mode == GameMode.GAME_A) 2 else 3

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
                        isActive = uiState.chefPos < maxChefCol && uiState.isRunning,
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
    val woodStroke = Stroke(width = vp.s(2.6f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Jemné spáry kuchyňského obkladu v pozadí
    for (ty in listOf(136f, 240f, 344f)) {
        drawLine(ChefPrintTile, vp.pt(14f, ty), vp.pt(986f, ty), vp.s(1.8f))
    }

    // 2. Dřevěná kuchyňská polička vlevo nahoře pro kocoura + kořenky u zdi
    drawRoundRect(
        color = ChefPrintBrown,
        topLeft = vp.pt(14f, 140f),
        size = Size(vp.x(142f), vp.y(13f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
    drawRoundRect(
        color = ChefPrintDarkWood,
        topLeft = vp.pt(14f, 140f),
        size = Size(vp.x(142f), vp.y(13f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f)),
        style = woodStroke
    )
    // Vyřezávaná konzole pod poličkou
    val shelfBracket = Path().apply {
        moveTo(vp.x(22f), vp.y(153f))
        lineTo(vp.x(56f), vp.y(153f))
        quadraticTo(vp.x(36f), vp.y(172f), vp.x(22f), vp.y(194f))
        close()
    }
    drawPath(shelfBracket, color = ChefPrintDarkWood)

    // Kořenka na kraji poličky vlevo
    drawRoundRect(
        color = ChefPrintCopper,
        topLeft = vp.pt(20f, 110f),
        size = Size(vp.x(22f), vp.y(30f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f))
    )
    drawRoundRect(
        color = ChefPrintDarkWood,
        topLeft = vp.pt(18f, 104f),
        size = Size(vp.x(26f), vp.y(8f)),
        cornerRadius = CornerRadius(vp.s(2f), vp.s(2f))
    )

    // 3. Závěsná kovaná lišta s měděnými rendlíky, pokličkou a naběračkou nad dráhami jídla
    drawLine(ChefPrintDarkWood, vp.pt(196f, 20f), vp.pt(650f, 20f), vp.s(5.0f), StrokeCap.Round)
    drawCircle(ChefPrintDarkWood, radius = vp.s(4.5f), center = vp.pt(196f, 20f))
    drawCircle(ChefPrintDarkWood, radius = vp.s(4.5f), center = vp.pt(650f, 20f))

    // 3 měděné rendlíky s uchy přímo nad sloupci 0, 1, 2
    for (i in 0..2) {
        val hx = CHEF_FOOD_COL_X[i]
        // Háček
        drawLine(ChefPrintDarkWood, vp.pt(hx, 20f), vp.pt(hx, 35f), vp.s(3.0f), StrokeCap.Round)
        // Ucha hrnce
        drawArc(
            color = ChefPrintDarkWood,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = vp.pt(hx - 30f, 42f),
            size = Size(vp.x(12f), vp.y(16f)),
            style = Stroke(width = vp.s(2.8f), cap = StrokeCap.Round)
        )
        drawArc(
            color = ChefPrintDarkWood,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = vp.pt(hx + 18f, 42f),
            size = Size(vp.x(12f), vp.y(16f)),
            style = Stroke(width = vp.s(2.8f), cap = StrokeCap.Round)
        )
        // Tělo měděného hrnce
        drawRoundRect(
            color = ChefPrintCopper,
            topLeft = vp.pt(hx - 23f, 36f),
            size = Size(vp.x(46f), vp.y(30f)),
            cornerRadius = CornerRadius(vp.s(5f), vp.s(5f))
        )
        drawLine(ChefPrintGold, vp.pt(hx - 18f, 42f), vp.pt(hx + 18f, 42f), vp.s(3.0f), StrokeCap.Round)
        drawRoundRect(
            color = ChefPrintDarkWood,
            topLeft = vp.pt(hx - 23f, 36f),
            size = Size(vp.x(46f), vp.y(30f)),
            cornerRadius = CornerRadius(vp.s(5f), vp.s(5f)),
            style = woodStroke
        )
    }

    // Závěsná měděná pánvička a naběračka nad 4. sloupcem (x = 620)
    val panHangerX = CHEF_FOOD_COL_X[3]
    drawLine(ChefPrintDarkWood, vp.pt(panHangerX, 20f), vp.pt(panHangerX, 38f), vp.s(3.0f), StrokeCap.Round)
    drawCircle(ChefPrintCopper, radius = vp.s(16f), center = vp.pt(panHangerX, 52f))
    drawCircle(ChefPrintGold, radius = vp.s(9f), center = vp.pt(panHangerX, 52f))
    drawCircle(ChefPrintDarkWood, radius = vp.s(16f), center = vp.pt(panHangerX, 52f), style = woodStroke)

    // 4. Kuchyňský sporák s troubou, plamenem a hrncem vpravo dole (mimo dosah kuchaře)
    val stoveLeft = 876f
    val stoveTop = 356f
    // Kouřící hrnec na plotně sporáku
    drawRoundRect(
        color = ChefPrintCopper,
        topLeft = vp.pt(896f, 312f),
        size = Size(vp.x(68f), vp.y(44f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
    )
    drawRoundRect(
        color = ChefPrintDarkWood,
        topLeft = vp.pt(896f, 312f),
        size = Size(vp.x(68f), vp.y(44f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f)),
        style = woodStroke
    )
    // Poklička hrnce
    drawOval(ChefPrintGold, vp.pt(892f, 304f), Size(vp.x(76f), vp.y(14f)))
    drawOval(ChefPrintDarkWood, vp.pt(892f, 304f), Size(vp.x(76f), vp.y(14f)), style = woodStroke)
    drawCircle(ChefPrintDarkWood, radius = vp.s(5f), center = vp.pt(930f, 299f))

    // Tělo sporáku
    drawRoundRect(
        color = ChefPrintBrown,
        topLeft = vp.pt(stoveLeft, stoveTop),
        size = Size(vp.x(108f), vp.y(182f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f))
    )
    drawRoundRect(
        color = ChefPrintDarkWood,
        topLeft = vp.pt(stoveLeft, stoveTop),
        size = Size(vp.x(108f), vp.y(182f)),
        cornerRadius = CornerRadius(vp.s(6f), vp.s(6f)),
        style = woodStroke
    )
    // Dvířka trouby s průzorem
    drawRoundRect(
        color = ChefPrintOrange,
        topLeft = vp.pt(stoveLeft + 14f, stoveTop + 44f),
        size = Size(vp.x(80f), vp.y(78f)),
        cornerRadius = CornerRadius(vp.s(5f), vp.s(5f))
    )
    drawRoundRect(
        color = ChefPrintDarkWood,
        topLeft = vp.pt(stoveLeft + 14f, stoveTop + 44f),
        size = Size(vp.x(80f), vp.y(78f)),
        cornerRadius = CornerRadius(vp.s(5f), vp.s(5f)),
        style = woodStroke
    )
    drawLine(ChefPrintGold, vp.pt(stoveLeft + 22f, stoveTop + 54f), vp.pt(stoveLeft + 86f, stoveTop + 54f), vp.s(3.2f), StrokeCap.Round)
    // Knoflíky hořáků
    for (kx in listOf(896f, 930f, 964f)) {
        drawCircle(ChefPrintGold, radius = vp.s(5.5f), center = vp.pt(kx, stoveTop + 20f))
        drawCircle(ChefPrintDarkWood, radius = vp.s(5.5f), center = vp.pt(kx, stoveTop + 20f), style = Stroke(width = vp.s(2f)))
    }

    // 5. Myší díra v levém dolním rohu a pevná linie podlahy kuchyně
    val mouseHole = Path().apply {
        moveTo(vp.x(16f), vp.y(538f))
        cubicTo(
            vp.x(16f), vp.y(490f),
            vp.x(68f), vp.y(490f),
            vp.x(68f), vp.y(538f)
        )
        close()
    }
    drawPath(mouseHole, color = ChefPrintDarkWood)
    drawArc(
        color = ChefPrintBrown,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = vp.pt(14f, 498f),
        size = Size(vp.x(56f), vp.y(78f)),
        style = Stroke(width = vp.s(3.5f))
    )
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

    // 1. Čtyřmístné 7segmentové skóre vpravo nahoře (x = 700..884, zcela mimo 4. sloupec jídla!)
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
            x = 700f + i * 46f,
            y = 26f,
            dw = 31f,
            dh = 58f,
            value = digits[i],
            onCol = segColor(true),
            offCol = segColor(false)
        )
    }

    // 3 trestné myšky přímo pod číslicemi skóre (x = 724, 784, 844 -> nekolidují se sloupcem 3!)
    for (idx in 0..2) {
        val thresholdFull = (idx + 1) * 2
        val thresholdHalf = thresholdFull - 1
        val isLit = when {
            snapshot.penaltyHalfPoints >= thresholdFull -> true
            snapshot.penaltyHalfPoints == thresholdHalf -> snapshot.blinkPhase
            else -> false
        }
        drawPenaltyMouseHead(vp, cx = 724f + idx * 60f, cy = 114f, col = segColor(isLit))
    }

    // 2. Komiksový kocour na poličce vlevo nahoře s vidličkou v tlapce
    drawShelfCatWithFork(
        vp = vp,
        catCol = segColor(snapshot.catVisible),
        forkDownCol = segColor(snapshot.catVisible && snapshot.catHoldingFood),
        forkUpCol = segColor(snapshot.catVisible && !snapshot.catHoldingFood)
    )

    // 3. 4 sloupce padajícího / nadhazovaného jídla (step 0..4, končí v y = 322 nad čepicí kuchaře)
    drawFallingFoods(vp, snapshot.foods, ::segColor)

    // 4. Komiksový kuchař ve vysoké čepici s pánvičkou na 4 pozicích (0..3)
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

private fun DrawScope.drawShelfCatWithFork(
    vp: LcdViewport,
    catCol: Color,
    forkDownCol: Color,
    forkUpCol: Color
) {
    val cx = 92f
    val cy = 102f
    val outline = Stroke(width = vp.s(3.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    if (catCol != Color.Transparent) {
        // Zatočený kočičí ocas visící přes okraj poličky
        val tail = Path().apply {
            moveTo(vp.x(cx - 26f), vp.y(cy + 34f))
            cubicTo(
                vp.x(cx - 48f), vp.y(cy + 56f),
                vp.x(cx - 24f), vp.y(cy + 82f),
                vp.x(cx - 10f), vp.y(cy + 72f)
            )
        }
        drawPath(tail, color = catCol, style = Stroke(width = vp.s(6.5f), cap = StrokeCap.Round))

        // Tělo sedícího kocoura s bílým bříškem
        val body = Path().apply {
            moveTo(vp.x(cx - 26f), vp.y(cy + 36f))
            cubicTo(
                vp.x(cx - 30f), vp.y(cy + 2f),
                vp.x(cx + 12f), vp.y(cy + 2f),
                vp.x(cx + 20f), vp.y(cy + 36f)
            )
            close()
        }
        drawPath(body, color = catCol)
        drawOval(LcdBgColor, vp.pt(cx - 8f, cy + 12f), Size(vp.x(20f), vp.y(22f)))

        // Špičaté kočičí uši
        val ears = Path().apply {
            moveTo(vp.x(cx - 8f), vp.y(cy - 12f))
            lineTo(vp.x(cx - 16f), vp.y(cy - 34f))
            lineTo(vp.x(cx + 2f), vp.y(cy - 18f))
            lineTo(vp.x(cx + 10f), vp.y(cy - 18f))
            lineTo(vp.x(cx + 24f), vp.y(cy - 34f))
            lineTo(vp.x(vp.x(cx + 20f) / vp.x(1f)), vp.y(cy - 10f))
            close()
        }
        drawPath(ears, color = catCol)

        // Kulatá hlava kocoura se světlým vnitřkem, očima, čumáčkem a fousky
        drawOval(LcdBgColor, vp.pt(cx - 12f, cy - 20f), Size(vp.x(36f), vp.y(30f)))
        drawOval(catCol, vp.pt(cx - 12f, cy - 20f), Size(vp.x(36f), vp.y(30f)), style = outline)

        // Oči a čumáček
        drawOval(catCol, vp.pt(cx - 1f, cy - 12f), Size(vp.x(5f), vp.y(8f)))
        drawOval(catCol, vp.pt(cx + 10f, cy - 12f), Size(vp.x(5f), vp.y(8f)))
        drawCircle(catCol, radius = vp.s(3.0f), center = vp.pt(cx + 6f, cy - 2f))

        // Fousky kocoura
        drawLine(catCol, vp.pt(cx - 4f, cy - 2f), vp.pt(cx - 22f, cy - 6f), vp.s(2.0f), StrokeCap.Round)
        drawLine(catCol, vp.pt(cx - 4f, cy + 1f), vp.pt(cx - 22f, cy + 3f), vp.s(2.0f), StrokeCap.Round)
        drawLine(catCol, vp.pt(cx + 15f, cy - 2f), vp.pt(cx + 32f, cy - 6f), vp.s(2.0f), StrokeCap.Round)
        drawLine(catCol, vp.pt(cx + 15f, cy + 1f), vp.pt(cx + 32f, cy + 3f), vp.s(2.0f), StrokeCap.Round)
    }

    // Horní klidová poloha tlapky s vidličkou
    if (forkUpCol != Color.Transparent) {
        drawCatPawAndFork(
            vp = vp,
            shoulderX = cx + 16f,
            shoulderY = cy + 12f,
            tipX = 166f,
            tipY = 74f,
            col = forkUpCol
        )
    }

    // Dolní poloha tlapky s vidličkou (přidržuje klobásu na pozici col 0, step 0 -> x = 224, y = 106)
    if (forkDownCol != Color.Transparent) {
        drawCatPawAndFork(
            vp = vp,
            shoulderX = cx + 16f,
            shoulderY = cy + 16f,
            tipX = 198f,
            tipY = 104f,
            col = forkDownCol
        )
    }
}

private fun DrawScope.drawCatPawAndFork(
    vp: LcdViewport,
    shoulderX: Float,
    shoulderY: Float,
    tipX: Float,
    tipY: Float,
    col: Color
) {
    val wristX = (shoulderX + tipX) * 0.52f
    val wristY = (shoulderY + tipY) * 0.52f
    // Tlapka kocoura
    drawLine(col, vp.pt(shoulderX, shoulderY), vp.pt(wristX, wristY), vp.s(6.2f), StrokeCap.Round)
    drawCircle(col, radius = vp.s(5.0f), center = vp.pt(wristX, wristY))
    // Násada vidličky a 3 hroty
    drawLine(col, vp.pt(wristX, wristY), vp.pt(tipX, tipY), vp.s(3.2f), StrokeCap.Round)
    drawLine(col, vp.pt(tipX, tipY - 6f), vp.pt(tipX, tipY + 6f), vp.s(3.0f), StrokeCap.Round)
    drawLine(col, vp.pt(tipX, tipY - 5f), vp.pt(tipX + 10f, tipY - 4f), vp.s(2.4f), StrokeCap.Round)
    drawLine(col, vp.pt(tipX, tipY), vp.pt(tipX + 11f, tipY), vp.s(2.4f), StrokeCap.Round)
    drawLine(col, vp.pt(tipX, tipY + 5f), vp.pt(tipX + 10f, tipY + 4f), vp.s(2.4f), StrokeCap.Round)
}

private fun DrawScope.drawFallingFoods(
    vp: LcdViewport,
    foods: List<FoodItem>,
    segColor: (Boolean) -> Color
) {
    val stepAngles = floatArrayOf(-12f, 28f, 78f, 138f, 192f)

    for (col in 0..3) {
        for (step in 0..4) {
            val isLit = foods.any { it.col == col && it.step == step }
            val ink = segColor(isLit)
            if (ink == Color.Transparent) continue

            val center = vp.pt(CHEF_FOOD_COL_X[col], CHEF_FOOD_STEP_Y[step])
            val angle = stepAngles[step] + col * 14f

            rotate(degrees = angle, pivot = center) {
                when (col) {
                    0 -> {
                        // Sloupec 0: Klobása / párek se zavázanými konci
                        drawRoundRect(
                            color = ink,
                            topLeft = Offset(center.x - vp.x(21f), center.y - vp.y(8f)),
                            size = Size(vp.x(42f), vp.y(16f)),
                            cornerRadius = CornerRadius(vp.s(8f), vp.s(8f))
                        )
                        drawLine(
                            color = LcdBgColor,
                            start = Offset(center.x - vp.x(10f), center.y - vp.y(2.5f)),
                            end = Offset(center.x + vp.x(10f), center.y - vp.y(2.5f)),
                            strokeWidth = vp.s(2.4f),
                            cap = StrokeCap.Round
                        )
                    }
                    1 -> {
                        // Sloupec 1: Ryba s ploutví a okem
                        val fish = Path().apply {
                            moveTo(center.x - vp.x(22f), center.y)
                            quadraticTo(center.x - vp.x(2f), center.y - vp.y(15f), center.x + vp.x(15f), center.y)
                            lineTo(center.x + vp.x(25f), center.y - vp.y(9f))
                            lineTo(center.x + vp.x(25f), center.y + vp.y(9f))
                            lineTo(center.x + vp.x(15f), center.y)
                            quadraticTo(center.x - vp.x(2f), center.y + vp.y(15f), center.x - vp.x(22f), center.y)
                            close()
                        }
                        drawPath(fish, color = ink)
                        drawCircle(LcdBgColor, radius = vp.s(2.5f), center = Offset(center.x - vp.x(11f), center.y - vp.y(2f)))
                    }
                    2 -> {
                        // Sloupec 2: Kotleta / řízek na kosti
                        drawOval(
                            color = ink,
                            topLeft = Offset(center.x - vp.x(19f), center.y - vp.y(11f)),
                            size = Size(vp.x(30f), vp.y(22f))
                        )
                        drawCircle(LcdBgColor, radius = vp.s(3.8f), center = Offset(center.x - vp.x(5f), center.y))
                        drawLine(
                            color = ink,
                            start = Offset(center.x + vp.x(9f), center.y),
                            end = Offset(center.x + vp.x(23f), center.y),
                            strokeWidth = vp.s(5.0f),
                            cap = StrokeCap.Round
                        )
                        drawCircle(ink, radius = vp.s(3.8f), center = Offset(center.x + vp.x(24f), center.y - vp.y(2.5f)))
                        drawCircle(ink, radius = vp.s(3.8f), center = Offset(center.x + vp.x(24f), center.y + vp.y(2.5f)))
                    }
                    else -> {
                        // Sloupec 3: Volské oko / lívanec
                        drawOval(
                            color = LcdBgColor,
                            topLeft = Offset(center.x - vp.x(19f), center.y - vp.y(11f)),
                            size = Size(vp.x(38f), vp.y(22f))
                        )
                        drawOval(
                            color = ink,
                            topLeft = Offset(center.x - vp.x(19f), center.y - vp.y(11f)),
                            size = Size(vp.x(38f), vp.y(22f)),
                            style = Stroke(width = vp.s(4.0f))
                        )
                        drawCircle(ink, radius = vp.s(5.2f), center = center)
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
    // Pánvička je přesně pod sloupcem `pos` (224, 356, 488, 620)
    // a kuchař stojí o 62 jednotek napravo od středu pánvičky,
    // přičemž vršek jeho čepice je v y = 352 (čistě pod nejnižší polohou jídla y = 322!).
    val panCenterX = CHEF_FOOD_COL_X[pos.coerceIn(0, 3)]
    val chefX = panCenterX + 62f
    val outline = Stroke(width = vp.s(3.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    if (bodyCol != Color.Transparent) {
        // 1. Nadýchaná komiksová kuchařská čepice (toque) v y = 352..392
        val hatCloud = Path().apply {
            moveTo(vp.x(chefX - 18f), vp.y(384f))
            cubicTo(vp.x(chefX - 28f), vp.y(366f), vp.x(chefX - 12f), vp.y(352f), vp.x(chefX - 4f), vp.y(360f))
            cubicTo(vp.x(chefX + 2f), vp.y(348f), vp.x(chefX + 18f), vp.y(348f), vp.x(chefX + 20f), vp.y(362f))
            cubicTo(vp.x(chefX + 32f), vp.y(360f), vp.x(chefX + 34f), vp.y(378f), vp.x(chefX + 20f), vp.y(386f))
            close()
        }
        drawPath(hatCloud, color = LcdBgColor)
        drawPath(hatCloud, color = bodyCol, style = outline)

        // Čelenka kuchařské čepice
        drawRoundRect(
            color = LcdBgColor,
            topLeft = vp.pt(chefX - 16f, 380f),
            size = Size(vp.x(34f), vp.y(14f)),
            cornerRadius = CornerRadius(vp.s(3f), vp.s(3f))
        )
        drawRoundRect(
            color = bodyCol,
            topLeft = vp.pt(chefX - 16f, 380f),
            size = Size(vp.x(34f), vp.y(14f)),
            cornerRadius = CornerRadius(vp.s(3f), vp.s(3f)),
            style = outline
        )

        // 2. Komiksová hlava kuchaře z profilu (hledí doleva na pánvičku) s velkým nosem a knírem
        val headY = 410f
        drawCircle(color = LcdBgColor, radius = vp.s(15.5f), center = vp.pt(chefX + 1f, headY))
        drawCircle(color = bodyCol, radius = vp.s(15.5f), center = vp.pt(chefX + 1f, headY), style = outline)

        // Kulatý kuchařský nos vyčnívající doleva
        drawCircle(color = LcdBgColor, radius = vp.s(5.5f), center = vp.pt(chefX - 15f, headY - 1f))
        drawCircle(color = bodyCol, radius = vp.s(5.5f), center = vp.pt(chefX - 15f, headY - 1f), style = Stroke(width = vp.s(3.0f)))

        // Oko a výrazný zatočený knír
        drawCircle(color = bodyCol, radius = vp.s(2.6f), center = vp.pt(chefX - 5f, headY - 5f))
        val mustache = Path().apply {
            moveTo(vp.x(chefX - 20f), vp.y(headY + 7f))
            quadraticTo(vp.x(chefX - 8f), vp.y(headY + 2f), vp.x(chefX + 4f), vp.y(headY + 7f))
        }
        drawPath(mustache, color = bodyCol, style = Stroke(width = vp.s(3.8f), cap = StrokeCap.Round))

        // 3. Kuchařský šátek kolem krku + rondon a zástěra s dvouřadými knoflíky
        val scarf = Path().apply {
            moveTo(vp.x(chefX - 12f), vp.y(425f))
            lineTo(vp.x(chefX + 14f), vp.y(425f))
            lineTo(vp.x(chefX + 2f), vp.y(439f))
            close()
        }
        drawPath(scarf, color = bodyCol)

        val apron = Path().apply {
            moveTo(vp.x(chefX - 16f), vp.y(427f))
            lineTo(vp.x(chefX + 17f), vp.y(427f))
            lineTo(vp.x(chefX + 22f), vp.y(496f))
            lineTo(vp.x(chefX - 21f), vp.y(496f))
            close()
        }
        drawPath(apron, color = LcdBgColor)
        drawPath(apron, color = bodyCol, style = outline)

        // Dvouřadé knoflíky rondonu a pas zástěry
        drawLine(bodyCol, vp.pt(chefX - 18f, 460f), vp.pt(chefX + 19f, 460f), vp.s(2.8f))
        for (by in listOf(440f, 451f)) {
            drawCircle(bodyCol, radius = vp.s(2.4f), center = vp.pt(chefX - 7f, by))
            drawCircle(bodyCol, radius = vp.s(2.4f), center = vp.pt(chefX + 5f, by))
        }

        // Zadní pokrčená ruka kuchaře v bok
        val backArm = Path().apply {
            moveTo(vp.x(chefX + 16f), vp.y(434f))
            lineTo(vp.x(chefX + 29f), vp.y(450f))
            lineTo(vp.x(chefX + 19f), vp.y(464f))
        }
        drawPath(backArm, color = bodyCol, style = Stroke(width = vp.s(5.5f), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // 4. Rozkročené nohy a boty kuchaře pevně na podlaze (y = 534)
        val stride = if (pos % 2 == 0) 13f else 9f
        drawLine(bodyCol, vp.pt(chefX - 8f, 496f), vp.pt(chefX - stride, 530f), vp.s(7.2f), StrokeCap.Round)
        drawOval(bodyCol, vp.pt(chefX - stride - 14f, 524f), Size(vp.x(22f), vp.y(11f)))

        drawLine(bodyCol, vp.pt(chefX + 8f, 496f), vp.pt(chefX + stride, 530f), vp.s(7.2f), StrokeCap.Round)
        drawOval(bodyCol, vp.pt(chefX + stride - 6f, 524f), Size(vp.x(22f), vp.y(11f)))
    }

    // 5. Přední ruka s pánvičkou přímo pod sloupcem `pos` (vodorovná vs. nadhazující)
    if (panNormalCol != Color.Transparent) {
        drawChefPanWithArm(
            vp = vp,
            shoulderX = chefX - 15f,
            shoulderY = 438f,
            panX = panCenterX,
            panY = 368f,
            tiltDeg = 0f,
            col = panNormalCol
        )
    }
    if (panFlipCol != Color.Transparent) {
        drawChefPanWithArm(
            vp = vp,
            shoulderX = chefX - 15f,
            shoulderY = 434f,
            panX = panCenterX,
            panY = 350f,
            tiltDeg = 16f,
            col = panFlipCol
        )
    }
}

private fun DrawScope.drawChefPanWithArm(
    vp: LcdViewport,
    shoulderX: Float,
    shoulderY: Float,
    panX: Float,
    panY: Float,
    tiltDeg: Float,
    col: Color
) {
    val handleGripX = panX + 34f
    val handleGripY = panY + 6f
    val elbowX = (shoulderX + handleGripX) * 0.54f
    val elbowY = shoulderY + 6f

    // Paže kuchaře ohnutá v lokti a držící rukojeť pánvičky
    val armPath = Path().apply {
        moveTo(vp.x(shoulderX), vp.y(shoulderY))
        lineTo(vp.x(elbowX), vp.y(elbowY))
        lineTo(vp.x(handleGripX), vp.y(handleGripY))
    }
    drawPath(
        path = armPath,
        color = col,
        style = Stroke(width = vp.s(6.8f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    val pivot = vp.pt(panX, panY)
    rotate(degrees = tiltDeg, pivot = pivot) {
        // Dlouhá rukojeť pánvičky
        drawLine(col, vp.pt(panX + 20f, panY + 1f), vp.pt(panX + 40f, panY + 7f), vp.s(4.8f), StrokeCap.Round)
        // Tělo pánvičky se zkosenými stěnami
        val panBody = Path().apply {
            moveTo(vp.x(panX - 27f), vp.y(panY - 4f))
            lineTo(vp.x(panX + 27f), vp.y(panY - 4f))
            lineTo(vp.x(panX + 21f), vp.y(panY + 11f))
            lineTo(vp.x(panX - 21f), vp.y(panY + 11f))
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
    for (c in 0..3) {
        val splatLit = crashAnim != null && crashAnim.droppedCol == c && crashAnim.frame == 0
        val col = segColor(splatLit)
        if (col != Color.Transparent) {
            val sx = CHEF_FOOD_COL_X[c]
            drawOval(col, vp.pt(sx - 20f, 520f), Size(vp.x(40f), vp.y(12f)))
            drawCircle(col, radius = vp.s(3.2f), center = vp.pt(sx - 26f, 514f))
            drawCircle(col, radius = vp.s(3.2f), center = vp.pt(sx + 26f, 514f))
        }
    }

    // 3 fáze myšky utíkající po podlaze s kořistí do myší díry vlevo (frame 1 -> 2 -> 3)
    val mouseStepsX = floatArrayOf(380f, 240f, 112f)
    for (step in 1..3) {
        val mouseLit = crashAnim != null && crashAnim.frame == step
        val col = segColor(mouseLit)
        if (col == Color.Transparent) continue

        val mx = mouseStepsX[step - 1]
        val my = 516f
        // Tělo myšky, ouško, hlava a dlouhý ocásek
        drawOval(col, vp.pt(mx - 14f, my - 7f), Size(vp.x(28f), vp.y(15f)))
        drawCircle(col, radius = vp.s(6.5f), center = vp.pt(mx - 14f, my - 6f))
        drawCircle(col, radius = vp.s(5.0f), center = vp.pt(mx - 10f, my - 13f))
        drawCircle(LcdBgColor, radius = vp.s(1.6f), center = vp.pt(mx - 16f, my - 7f))
        drawLine(col, vp.pt(mx + 14f, my + 2f), vp.pt(mx + 32f, my - 5f), vp.s(2.8f), StrokeCap.Round)
        // Vidlička s jídlem nad hlavou myšky
        drawLine(col, vp.pt(mx - 14f, my - 4f), vp.pt(mx - 28f, my - 16f), vp.s(3.0f), StrokeCap.Round)
        drawOval(col, vp.pt(mx - 36f, my - 24f), Size(vp.x(16f), vp.y(9f)))
    }
}

private fun DrawScope.drawPenaltyMouseHead(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    col: Color
) {
    if (col == Color.Transparent) return
    drawCircle(col, radius = vp.s(8.5f), center = vp.pt(cx, cy))
    drawCircle(col, radius = vp.s(5.2f), center = vp.pt(cx - 8f, cy - 8f))
    drawCircle(col, radius = vp.s(5.2f), center = vp.pt(cx + 8f, cy - 8f))
    drawCircle(LcdBgColor, radius = vp.s(1.8f), center = vp.pt(cx - 3f, cy - 2f))
    drawCircle(LcdBgColor, radius = vp.s(1.8f), center = vp.pt(cx + 3f, cy - 2f))
    drawLine(col, vp.pt(cx - 8f, cy + 2f), vp.pt(cx - 17f, cy), vp.s(1.8f), StrokeCap.Round)
    drawLine(col, vp.pt(cx + 8f, cy + 2f), vp.pt(cx + 17f, cy), vp.s(1.8f), StrokeCap.Round)
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
