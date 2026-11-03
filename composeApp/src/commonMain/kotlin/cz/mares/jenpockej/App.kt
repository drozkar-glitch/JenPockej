package cz.mares.jenpockej

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun App() {
    val soundPlayer = remember { SoundPlayer() }
    val engine = remember { GameEngine(onSound = { soundPlayer.play(it) }) }
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF232528)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Horni kovovy stitek a ovladaci tlacitka konzole
                TopConsoleBar(
                    snapshot = uiState,
                    onStartA = {
                        engine.startNewGame(GameMode.GAME_A)
                        uiState = engine.state
                    },
                    onStartB = {
                        engine.startNewGame(GameMode.GAME_B)
                        uiState = engine.state
                    },
                    onToggleSound = {
                        engine.toggleSound()
                        uiState = engine.state
                    },
                    onReset = {
                        engine.resetToDemo()
                        uiState = engine.state
                    }
                )

                // Hlavni telo kapesni hry s rohovymi tlacitky a LCD uprostred
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFD6D8DC), RoundedCornerShape(22.dp))
                        .border(3.dp, Color(0xFF8C1D1D), RoundedCornerShape(22.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Leve sloupce tlacitek (Horni levy + Dolni levy)
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RedArcadeButton(
                            label = "↖",
                            caption = "HORNÍ LEVÝ",
                            isActive = uiState.wolfPosition == BasketPosition.TOP_LEFT,
                            onClick = {
                                engine.moveWolf(BasketPosition.TOP_LEFT)
                                uiState = engine.state
                            }
                        )
                        RedArcadeButton(
                            label = "↙",
                            caption = "DOLNÍ LEVÝ",
                            isActive = uiState.wolfPosition == BasketPosition.BOTTOM_LEFT,
                            onClick = {
                                engine.moveWolf(BasketPosition.BOTTOM_LEFT)
                                uiState = engine.state
                            }
                        )
                    }

                    // Stredovy ramecek s LCD obrazovkou
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp)
                            .background(Color(0xFF3A3E3B), RoundedCornerShape(16.dp))
                            .border(3.dp, Color(0xFF8C1D1D), RoundedCornerShape(16.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        JenPockejLcdScreen(
                            snapshot = uiState,
                            onCornerTap = { target ->
                                engine.moveWolf(target)
                                uiState = engine.state
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .aspectRatio(1.45f, matchHeightConstraintsFirst = true)
                        )

                        // Stavova informacni vrstva pred startem nebo po konci hry
                        if (!uiState.isRunning) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 6.dp)
                                    .background(Color(0xCC1C2318), RoundedCornerShape(8.dp))
                                    .clickable {
                                        engine.startNewGame(uiState.mode)
                                        uiState = engine.state
                                    }
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                            ) {
                                val promptText = if (uiState.isGameOver) {
                                    "KONEC HRY (${uiState.score} b.) • KLEPNI PRO NOVOU HRU"
                                } else {
                                    "KLEPNI ZDE NEBO NA 'HRA A / B' PRO START"
                                }
                                Text(
                                    text = promptText,
                                    color = Color(0xFFDCE6CB),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Prave sloupce tlacitek (Horni pravy + Dolni pravy)
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RedArcadeButton(
                            label = "↗",
                            caption = "HORNÍ PRAVÝ",
                            isActive = uiState.wolfPosition == BasketPosition.TOP_RIGHT,
                            onClick = {
                                engine.moveWolf(BasketPosition.TOP_RIGHT)
                                uiState = engine.state
                            }
                        )
                        RedArcadeButton(
                            label = "↘",
                            caption = "DOLNÍ PRAVÝ",
                            isActive = uiState.wolfPosition == BasketPosition.BOTTOM_RIGHT,
                            onClick = {
                                engine.moveWolf(BasketPosition.BOTTOM_RIGHT)
                                uiState = engine.state
                            }
                        )
                    }
                }

                // Paticka s rekordy a autorskym copyrightem
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REKORD A: ${uiState.highScoreA}  |  REKORD B: ${uiState.highScoreB}",
                        color = Color(0xFFB8BDC4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "© 2026 Created by Pavel Mareš",
                        color = Color(0xFFD0D4DA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TopConsoleBar(
    snapshot: GameSnapshot,
    onStartA: () -> Unit,
    onStartB: () -> Unit,
    onToggleSound: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ЭЛЕКТРОНИКА ИМ-02 • JEN POČKEJ!",
                color = Color(0xFFF2F4F7),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Režim: ${snapshot.mode.label} • Vlk a Zajíček",
                color = Color(0xFFA6ACB5),
                fontSize = 11.sp
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallSilverPillButton(
                text = "HRA A",
                highlighted = snapshot.isRunning && snapshot.mode == GameMode.GAME_A,
                onClick = onStartA
            )
            SmallSilverPillButton(
                text = "HRA B",
                highlighted = snapshot.isRunning && snapshot.mode == GameMode.GAME_B,
                onClick = onStartB
            )
            SmallSilverPillButton(
                text = if (snapshot.soundEnabled) "ZVUK: ZAP" else "ZVUK: VYP",
                highlighted = snapshot.soundEnabled,
                onClick = onToggleSound
            )
            SmallSilverPillButton(
                text = "RESET",
                highlighted = false,
                onClick = onReset
            )
        }
    }
}

@Composable
private fun SmallSilverPillButton(
    text: String,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (highlighted) Color(0xFF9E2424) else Color(0xFF474B52)
    val borderColor = if (highlighted) Color(0xFFFFB3B3) else Color(0xFF8C929C)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RedArcadeButton(
    label: String,
    caption: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val fill = if (isActive) Color(0xFFD62828) else Color(0xFF9E1F1F)
    val rim = if (isActive) Color(0xFFFFD166) else Color(0xFF5A1212)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(74.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(fill)
                .border(3.dp, rim, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = caption,
            color = Color(0xFF2B2D30),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
