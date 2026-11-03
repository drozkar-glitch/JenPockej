package cz.mares.jenpockej

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
import androidx.compose.ui.graphics.Brush
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
        // Kovove telo konzole roztazene pres 100 % celeho displeje bez cernych okraju
        val metallicBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE4E6EB),
                Color(0xFFD2D5DC),
                Color(0xFFC6CAD2)
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(metallicBrush)
                .border(4.dp, Color(0xFF8C1D1D))
                .padding(horizontal = 16.dp, vertical = 6.dp),
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
                // Leva dvojice cervenych tlacitek
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(84.dp),
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

                // Velky LCD displej uprostred roztazeny na maximum mezi tlacitky
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                        .background(Color(0xFF2E332F), RoundedCornerShape(14.dp))
                        .border(3.dp, Color(0xFF8C1D1D), RoundedCornerShape(14.dp))
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
                }

                // Prava dvojice cervenych tlacitek
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(84.dp),
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

            // Spodni informacni lista v kovovem ramu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusHint = when {
                    uiState.isGameOver -> "KONEC HRY (${uiState.score} b.) • ZVOL HRA A / B"
                    !uiState.isRunning -> "DEMO REŽIM • SPUŠŤĚNÍ TLAČÍTKEM HRA A / B"
                    else -> "AKTIVNÍ: ${uiState.mode.label}"
                }
                Text(
                    text = "REKORD A: ${uiState.highScoreA}  |  REKORD B: ${uiState.highScoreB}   •   $statusHint",
                    color = Color(0xFF22252A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "© 2026 Created by Pavel Mareš",
                    color = Color(0xFF22252A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
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
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "ЭЛЕКТРОНИКА ИМ-02 • JEN POČKEJ!",
                color = Color(0xFF8C1D1D),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "(Ну, погоди!)",
                color = Color(0xFF3E4247),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
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
    val bgColor = if (highlighted) Color(0xFF9E2424) else Color(0xFF3F434A)
    val borderColor = if (highlighted) Color(0xFFFFD166) else Color(0xFF7B818A)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
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
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = caption,
            color = Color(0xFF222426),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
