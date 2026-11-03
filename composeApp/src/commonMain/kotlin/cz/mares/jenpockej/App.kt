package cz.mares.jenpockej

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun App() {
    val soundPlayer = remember { SoundPlayer() }
    val engine = remember { GameEngine(media = soundPlayer) }
    var uiState by remember { mutableStateOf(engine.state) }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(engine.currentTickDelayMs())
            engine.stepTick()
            uiState = engine.state
        }
    }

    MaterialTheme {
        val outerPlasticColor = Color(0xFFB7B2A6)
        val metalPlateBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE2DFD7),
                Color(0xFFD6D3C9),
                Color(0xFFCBC7BD)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(outerPlasticColor)
                .padding(6.dp)
                .background(metalPlateBrush, RoundedCornerShape(10.dp))
                .border(2.dp, Color(0xFF969185), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // LEVY PANEL: Kulate logo •ИМ nahore + 2 tmava tlacitka se sipkami dole
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(102.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val r = size.minDimension * 0.46f
                            drawCircle(
                                color = Color(0xFF232724),
                                radius = r,
                                style = Stroke(width = 2.8f)
                            )
                            drawCircle(
                                color = Color(0xFF232724),
                                radius = r * 0.16f,
                                center = Offset(center.x - r * 0.72f, center.y)
                            )
                        }
                        Text(
                            text = "ИМ",
                            color = Color(0xFF232724),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        ConsoleCornerButton(
                            isLeft = true,
                            isTop = true,
                            isActive = uiState.wolfPosition == BasketPosition.TOP_LEFT,
                            onClick = {
                                engine.moveWolf(BasketPosition.TOP_LEFT)
                                uiState = engine.state
                            }
                        )
                        ConsoleCornerButton(
                            isLeft = true,
                            isTop = false,
                            isActive = uiState.wolfPosition == BasketPosition.BOTTOM_LEFT,
                            onClick = {
                                engine.moveWolf(BasketPosition.BOTTOM_LEFT)
                                uiState = engine.state
                            }
                        )
                    }
                }

                // STREDNI PANEL: Cista prerusovana cerna linka v ose napisu + zapusteny LCD
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

                                val topGapHalf = 86.dp.toPx()
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
                                drawPath(leftBracket, color = Color(0xFF232724), style = frameStroke)
                                drawPath(rightBracket, color = Color(0xFF232724), style = frameStroke)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Zapusteny kovovy prolis s cistym odstupem od horniho i spodniho napisu
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
                                            Color(0xFF8B8880),
                                            Color(0xFFB8B5AC),
                                            Color(0xFF9C9990)
                                        )
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .border(2.dp, Color(0xFF6B6962), RoundedCornerShape(8.dp))
                                .padding(5.dp)
                        ) {
                            JenPockejLcdScreen(
                                snapshot = uiState,
                                onCornerTap = { target ->
                                    engine.moveWolf(target)
                                    uiState = engine.state
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.5.dp, Color(0xFF4A4E46), RoundedCornerShape(4.dp))
                            )
                        }

                        // Horni napis НУ, ПОГОДИ! vycentrovany presne do osy horni cerne linky
                        Box(
                            modifier = Modifier
                                .height(topHeaderH)
                                .align(Alignment.TopCenter),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "НУ , ПОГОДИ !",
                                color = Color(0xFFB52B24),
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.4.sp
                            )
                        }

                        // Spodni napis ЭЛЕКТРОНИКА ИМ 02 vycentrovany presne do osy spodni cerne linky
                        Box(
                            modifier = Modifier
                                .height(botFooterH)
                                .align(Alignment.BottomCenter),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ЭЛЕКТРОНИКА ИМ 02",
                                color = Color(0xFF232724),
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
                            color = Color(0xFF3A3D3A),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "© 2026 Created by Pavel Mareš",
                            color = Color(0xFF3A3D3A),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // PRAVY PANEL: 3 ovalna tlacitka (игра А, игра Б, время) + 2 mala tlacitka + 2 herni tlacitka dole
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
                            SilverOvalMenuButton(
                                label = "игра А",
                                isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_A,
                                onClick = {
                                    engine.startNewGame(GameMode.GAME_A)
                                    uiState = engine.state
                                }
                            )
                            SilverOvalMenuButton(
                                label = "игра Б",
                                isSelected = uiState.isRunning && uiState.mode == GameMode.GAME_B,
                                onClick = {
                                    engine.startNewGame(GameMode.GAME_B)
                                    uiState = engine.state
                                }
                            )
                            SilverOvalMenuButton(
                                label = "время",
                                isSelected = !uiState.isRunning,
                                onClick = {
                                    engine.resetToDemo()
                                    uiState = engine.state
                                }
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Canvas(modifier = Modifier.size(16.dp)) {
                                val p = Path().apply {
                                    moveTo(size.width * 0.55f, size.height * 0.12f)
                                    lineTo(size.width * 0.15f, size.height * 0.76f)
                                    lineTo(size.width * 0.85f, size.height * 0.76f)
                                    close()
                                }
                                drawPath(
                                    path = p,
                                    color = Color(0xFF232724),
                                    style = Stroke(width = 2f)
                                )
                                drawCircle(
                                    color = Color(0xFF232724),
                                    radius = 2.2f,
                                    center = Offset(size.width * 0.5f, size.height * 0.88f)
                                )
                            }

                            SmallRecessedBlackButton(
                                label = if (uiState.soundEnabled) "ZVUK" else "TICH",
                                enabled = uiState.soundEnabled,
                                onClick = {
                                    engine.toggleSound()
                                    uiState = engine.state
                                }
                            )

                            SmallRecessedBlackButton(
                                label = if (uiState.hapticsEnabled) "VIBR" else "BEZ",
                                enabled = uiState.hapticsEnabled,
                                onClick = {
                                    engine.toggleHaptics()
                                    uiState = engine.state
                                }
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        ConsoleCornerButton(
                            isLeft = false,
                            isTop = true,
                            isActive = uiState.wolfPosition == BasketPosition.TOP_RIGHT,
                            onClick = {
                                engine.moveWolf(BasketPosition.TOP_RIGHT)
                                uiState = engine.state
                            }
                        )
                        ConsoleCornerButton(
                            isLeft = false,
                            isTop = false,
                            isActive = uiState.wolfPosition == BasketPosition.BOTTOM_RIGHT,
                            onClick = {
                                engine.moveWolf(BasketPosition.BOTTOM_RIGHT)
                                uiState = engine.state
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SilverOvalMenuButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val outerRim = if (isSelected) Color(0xFFB52B24) else Color(0xFF706E66)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF8C8980))
                .border(1.5.dp, outerRim, RoundedCornerShape(50))
                .clickable(onClick = onClick)
                .padding(3.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFD8D5CC), Color(0xFF9E9B92))
                    )
                )
        )
        Text(
            text = label,
            color = Color(0xFF232724),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
    }
}

@Composable
private fun SmallRecessedBlackButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val centerColor = if (enabled) Color(0xFF222622) else Color(0xFF6E6B64)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF8A877E))
                .border(1.dp, Color(0xFF4A4842), CircleShape)
                .clickable(onClick = onClick)
                .padding(2.5.dp)
                .clip(CircleShape)
                .background(centerColor)
        )
        Text(
            text = label,
            color = Color(0xFF232724),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConsoleCornerButton(
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
            ArrowPointerTriangle(isLeft = true, isTop = isTop)
            Spacer(modifier = Modifier.width(4.dp))
        }

        val ringColor = if (isActive) Color(0xFFB52B24) else Color(0xFF6E6C64)
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFFB8B5AC), Color(0xFF7A776E))
                    )
                )
                .border(2.dp, ringColor, CircleShape)
                .clickable(onClick = onClick)
                .padding(6.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isActive) {
                            listOf(Color(0xFF5A5D58), Color(0xFF222522))
                        } else {
                            listOf(Color(0xFF484B47), Color(0xFF1B1D1B))
                        }
                    )
                )
                .border(1.dp, Color(0xFF141614), CircleShape)
        )

        if (!isLeft) {
            Spacer(modifier = Modifier.width(4.dp))
            ArrowPointerTriangle(isLeft = false, isTop = isTop)
        }
    }
}

@Composable
private fun ArrowPointerTriangle(isLeft: Boolean, isTop: Boolean) {
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
        drawPath(path = path, color = Color(0xFF232724))
    }
}
