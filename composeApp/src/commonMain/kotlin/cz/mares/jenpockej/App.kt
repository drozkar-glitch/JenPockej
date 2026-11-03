package cz.mares.jenpockej

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.text.style.TextOverflow
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF1A1C1E)
        ) {
            // windowInsetsPadding(WindowInsets.safeDrawing) zajisti, ze nic nezasahuje do vyrezu kamery ani okraju
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(Color(0xFFD8DADF), RoundedCornerShape(20.dp))
                    .border(3.dp, Color(0xFF8C1D1D), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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
                    onToggleHaptics = {
                        engine.toggleHaptics()
                        uiState = engine.state
                    },
                    onReset = {
                        engine.resetToDemo()
                        uiState = engine.state
                    }
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Leve ovladaci tlacitka konzole
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(76.dp),
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

                    // Stredovy LCD displej roztazeny na celou plochu ramecku
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 6.dp)
                            .background(Color(0xFF323633), RoundedCornerShape(14.dp))
                            .border(2.5.dp, Color(0xFF8C1D1D), RoundedCornerShape(14.dp))
                            .padding(5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        JenPockejLcdScreen(
                            snapshot = uiState,
                            onCornerTap = { target ->
                                engine.moveWolf(target)
                                uiState = engine.state
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (!uiState.isRunning) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xD91C2318))
                                    .clickable {
                                        engine.startNewGame(uiState.mode)
                                        uiState = engine.state
                                    }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                val promptText = if (uiState.isGameOver) {
                                    "KONEC HRY (${uiState.score} b.) • KLEPNI PRO START"
                                } else {
                                    "DEMO REŽIM • KLEPNI PRO START HRY"
                                }
                                Text(
                                    text = promptText,
                                    color = Color(0xFFE4EDD5),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Prave ovladaci tlacitka konzole
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(76.dp),
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

                // Spodni lista s rekordy a plnym copyrightem
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REKORD A: ${uiState.highScoreA}  |  REKORD B: ${uiState.highScoreB}",
                        color = Color(0xFF2B2D30),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "© 2026 Created by Pavel Mareš",
                        color = Color(0xFF2B2D30),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
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
    onToggleHaptics: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ЭЛЕКТРОНИКА ИМ-02 • JEN POČKEJ!",
                color = Color(0xFF8C1D1D),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val statusLabel = if (snapshot.isRunning) snapshot.mode.label else "DEMO / ČAS"
            Text(
                text = "Režim: $statusLabel • Ну, погоди!",
                color = Color(0xFF3E4247),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
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
                text = if (snapshot.hapticsEnabled) "VIBR: ZAP" else "VIBR: VYP",
                highlighted = snapshot.hapticsEnabled,
                onClick = onToggleHaptics
            )
            SmallSilverPillButton(
                text = "DEMO",
                highlighted = snapshot.isDemoMode && !snapshot.isRunning,
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
    val bgColor = if (highlighted) Color(0xFF9E2424) else Color(0xFF42464D)
    val borderColor = if (highlighted) Color(0xFFFFD166) else Color(0xFF7B818A)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(fill)
                .border(2.5.dp, rim, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = caption,
            color = Color(0xFF222426),
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
