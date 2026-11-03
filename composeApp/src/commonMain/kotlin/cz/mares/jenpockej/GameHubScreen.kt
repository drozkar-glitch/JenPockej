package cz.mares.jenpockej

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ClassicGameId(
    val code: String,
    val titleCz: String,
    val titleRu: String,
    val subtitle: String,
    val isAvailable: Boolean
) {
    JEN_POCKEJ(
        code = "ИМ-02",
        titleCz = "Jen počkej!",
        titleRu = "Ну, погоди!",
        subtitle = "Vlk chytá vajíčka do košíku",
        isAvailable = true
    ),
    TAJEMSTVI_OCEANU(
        code = "ИМ-03",
        titleCz = "Tajemství oceánu",
        titleRu = "Тайны океана",
        subtitle = "Potápěč, poklad a chobotnice",
        isAvailable = false
    ),
    VESELY_KUCHAR(
        code = "ИМ-04",
        titleCz = "Veselý kuchař",
        titleRu = "Весёлый повар",
        subtitle = "Kuchař chytá jídlo na pánvičku",
        isAvailable = false
    ),
    KOSMICKY_MOST(
        code = "ИМ-09",
        titleCz = "Kosmický most",
        titleRu = "Космический мост",
        subtitle = "Záchrana kosmonautů přes lávu",
        isAvailable = false
    ),
    HOKEJ(
        code = "ИМ-22",
        titleCz = "Veselý hokej",
        titleRu = "Хоккей",
        subtitle = "Brankář chytá střely na branku",
        isAvailable = false
    ),
    AUTOSLALOM(
        code = "ИМ-23",
        titleCz = "Autoslalom",
        titleRu = "Автослалом",
        subtitle = "Závodní formule mezi kužely",
        isAvailable = false
    )
}

@Composable
fun GameHubScreen(
    highScoreA: Int,
    highScoreB: Int,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onSelectGame: (ClassicGameId) -> Unit
) {
    val outerFrameBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF23272A),
            Color(0xFF181B1E)
        )
    )
    val consoleMetalBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE4E1D8),
            Color(0xFFD6D3C9),
            Color(0xFFC8C4B9)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerFrameBrush)
            .padding(5.dp)
            .background(consoleMetalBrush, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF8C887D), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HORNÍ HLAVIČKA S PEVNOU VÝŠKOU 38.dp (nikdy nevytlačí karty her)
            HubHeaderBar(
                highScoreA = highScoreA,
                highScoreB = highScoreB,
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                onToggleSound = onToggleSound,
                onToggleHaptics = onToggleHaptics
            )

            // MŘÍŽKA 3 x 2 KARET HER PŘES CELÝ STŘED OBRAZOVKY
            val allGames = ClassicGameId.entries
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (game in allGames.take(3)) {
                        GameCatalogCard(
                            game = game,
                            highScoreA = highScoreA,
                            highScoreB = highScoreB,
                            onClick = {
                                if (game.isAvailable) {
                                    onSelectGame(game)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (game in allGames.drop(3)) {
                        GameCatalogCard(
                            game = game,
                            highScoreA = highScoreA,
                            highScoreB = highScoreB,
                            onClick = {
                                if (game.isAvailable) {
                                    onSelectGame(game)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            // SPODNÍ PATIČKA S PEVNOU VÝŠKOU 18.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIP: Ve hře se vrátíš do výběru her klepnutím na kulaté logo •ИМ vlevo nahoře",
                    color = Color(0xFF424542),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "© 2026 Created by Pavel Mareš",
                    color = Color(0xFF232724),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun HubHeaderBar(
    highScoreA: Int,
    highScoreB: Int,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Levý blok s logem a titulkem (má weight(1f), aby nikdy neutlačil pravá tlačítka)
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD5D1C6))
                    .border(2.dp, Color(0xFF232724), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "•ИМ",
                    color = Color(0xFF232724),
                    fontSize = 11.sp,
                    lineHeight = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "KOLEKCE ЭЛЕКТРОНИКА  •  RETRO HRY",
                    color = Color(0xFFB52B24),
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Vyber hru ze sbírky klasických digitálních konzolí",
                    color = Color(0xFF4A4E4A),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Pravý blok: Rekord + jednořádková tlačítka Zvuk a Vibrace
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFC6C2B7))
                    .border(1.dp, Color(0xFF8C887D), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "REKORD A: $highScoreA | B: $highScoreB",
                    color = Color(0xFF232724),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            HubTogglePill(
                label = if (soundEnabled) "ZVUK: ZAP" else "ZVUK: VYP",
                active = soundEnabled,
                onClick = onToggleSound
            )
            HubTogglePill(
                label = if (hapticsEnabled) "VIBR: ZAP" else "VIBR: VYP",
                active = hapticsEnabled,
                onClick = onToggleHaptics
            )
        }
    }
}

@Composable
private fun HubTogglePill(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    val bg = if (active) Color(0xFF2B302C) else Color(0xFF9E9A90)
    val border = if (active) Color(0xFFB52B24) else Color(0xFF706D65)
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 9.sp,
            lineHeight = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun GameCatalogCard(
    game: ClassicGameId,
    highScoreA: Int,
    highScoreB: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (game.isAvailable) {
        Brush.verticalGradient(
            listOf(Color(0xFFF4F2EA), Color(0xFFE2DFD4))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFDAD7CD), Color(0xFFCAC6BC))
        )
    }
    val borderColor = if (game.isAvailable) Color(0xFFB52B24) else Color(0xFF949086)
    val borderWidth = if (game.isAvailable) 2.dp else 1.2.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
            .clickable(enabled = game.isAvailable, onClick = onClick)
            .padding(7.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Horní část karty: Vektorová mini-konzole + popis hry
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniConsoleGameIcon(
                game = game,
                modifier = Modifier.size(width = 58.dp, height = 46.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (game.isAvailable) Color(0xFFB52B24) else Color(0xFF6B6961)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = game.code,
                            color = Color.White,
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Text(
                        text = game.titleRu,
                        color = Color(0xFF5A5850),
                        fontSize = 8.sp,
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = game.titleCz,
                    color = if (game.isAvailable) Color(0xFF1A1D1A) else Color(0xFF454744),
                    fontSize = 12.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = game.subtitle,
                    color = Color(0xFF575A56),
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Spodní akční tlačítko karty
        if (game.isAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFD62828), Color(0xFFB52B24))
                        )
                    )
                    .border(1.dp, Color(0xFF7A1510), RoundedCornerShape(6.dp))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶  HRÁT HRU   (REKORD: ${maxOf(highScoreA, highScoreB)})",
                    color = Color.White,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(25.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFB5B1A6))
                    .border(1.dp, Color(0xFF8C887E), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PŘIPRAVUJEME",
                    color = Color(0xFF424441),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun MiniConsoleGameIcon(
    game: ClassicGameId,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Kovové tělo mini-konzole
        drawRoundRect(
            color = Color(0xFF8C8980),
            size = size,
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )
        drawRoundRect(
            color = Color(0xFFD8D5CC),
            topLeft = Offset(w * 0.03f, h * 0.04f),
            size = Size(w * 0.94f, h * 0.92f),
            cornerRadius = CornerRadius(w * 0.11f, w * 0.11f)
        )

        // 4 červená tlačítka po stranách mini-konzole
        val btnColor = if (game.isAvailable) Color(0xFFD62828) else Color(0xFF9E3A34)
        val btnR = w * 0.055f
        drawCircle(btnColor, radius = btnR, center = Offset(w * 0.11f, h * 0.36f))
        drawCircle(btnColor, radius = btnR, center = Offset(w * 0.11f, h * 0.68f))
        drawCircle(btnColor, radius = btnR, center = Offset(w * 0.89f, h * 0.36f))
        drawCircle(btnColor, radius = btnR, center = Offset(w * 0.89f, h * 0.68f))

        // Zelenkavé LCD okénko uprostřed ikony
        val sx = w * 0.20f
        val sy = h * 0.15f
        val sw = w * 0.60f
        val sh = h * 0.70f

        drawRoundRect(
            color = Color(0xFF5E615A),
            topLeft = Offset(sx - 2f, sy - 2f),
            size = Size(sw + 4f, sh + 4f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFFBAC4AA),
            topLeft = Offset(sx, sy),
            size = Size(sw, sh),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Vlastní kresba pro každou z 6 her uvnitř LCD
        when (game) {
            ClassicGameId.JEN_POCKEJ -> drawMiniWolfIcon(sx, sy, sw, sh)
            ClassicGameId.TAJEMSTVI_OCEANU -> drawMiniOctopusIcon(sx, sy, sw, sh)
            ClassicGameId.VESELY_KUCHAR -> drawMiniChefIcon(sx, sy, sw, sh)
            ClassicGameId.KOSMICKY_MOST -> drawMiniSpaceBridgeIcon(sx, sy, sw, sh)
            ClassicGameId.HOKEJ -> drawMiniHockeyIcon(sx, sy, sw, sh)
            ClassicGameId.AUTOSLALOM -> drawMiniSlalomIcon(sx, sy, sw, sh)
        }
    }
}

private fun DrawScope.drawMiniWolfIcon(sx: Float, sy: Float, sw: Float, sh: Float) {
    val ink = Color(0xFF1A1E17)
    val brown = Color(0xFF8C5828)

    drawLine(
        color = brown,
        start = Offset(sx + sw * 0.08f, sy + sh * 0.35f),
        end = Offset(sx + sw * 0.48f, sy + sh * 0.56f),
        strokeWidth = sw * 0.06f,
        cap = StrokeCap.Round
    )
    drawOval(
        color = Color(0xFFF7F5EB),
        topLeft = Offset(sx + sw * 0.25f, sy + sh * 0.26f),
        size = Size(sw * 0.18f, sh * 0.14f)
    )
    drawOval(
        color = ink,
        topLeft = Offset(sx + sw * 0.25f, sy + sh * 0.26f),
        size = Size(sw * 0.18f, sh * 0.14f),
        style = Stroke(width = sw * 0.035f)
    )

    val basket = Path().apply {
        moveTo(sx + sw * 0.32f, sy + sh * 0.62f)
        lineTo(sx + sw * 0.64f, sy + sh * 0.62f)
        lineTo(sx + sw * 0.57f, sy + sh * 0.85f)
        lineTo(sx + sw * 0.39f, sy + sh * 0.85f)
        close()
    }
    drawPath(basket, color = brown)
    drawPath(basket, color = ink, style = Stroke(width = sw * 0.03f))

    drawCircle(color = ink, radius = sw * 0.14f, center = Offset(sx + sw * 0.72f, sy + sh * 0.42f))
    drawOval(
        color = Color(0xFFF3F4EE),
        topLeft = Offset(sx + sw * 0.62f, sy + sh * 0.20f),
        size = Size(sw * 0.22f, sh * 0.14f)
    )
    drawLine(
        color = ink,
        start = Offset(sx + sw * 0.65f, sy + sh * 0.44f),
        end = Offset(sx + sw * 0.48f, sy + sh * 0.42f),
        strokeWidth = sw * 0.08f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = ink,
        start = Offset(sx + sw * 0.74f, sy + sh * 0.52f),
        end = Offset(sx + sw * 0.74f, sy + sh * 0.88f),
        strokeWidth = sw * 0.15f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawMiniOctopusIcon(sx: Float, sy: Float, sw: Float, sh: Float) {
    val ink = Color(0xFF1A1E17)
    val seaBlue = Color(0xFF28556B)

    drawLine(
        color = seaBlue,
        start = Offset(sx + sw * 0.1f, sy + sh * 0.18f),
        end = Offset(sx + sw * 0.9f, sy + sh * 0.18f),
        strokeWidth = sw * 0.04f,
        cap = StrokeCap.Round
    )

    drawOval(
        color = ink,
        topLeft = Offset(sx + sw * 0.36f, sy + sh * 0.24f),
        size = Size(sw * 0.34f, sh * 0.34f)
    )
    drawCircle(color = Color(0xFFBAC4AA), radius = sw * 0.035f, center = Offset(sx + sw * 0.47f, sy + sh * 0.38f))
    drawCircle(color = Color(0xFFBAC4AA), radius = sw * 0.035f, center = Offset(sx + sw * 0.59f, sy + sh * 0.38f))

    for (i in -2..2) {
        val tx = sx + sw * (0.52f + i * 0.12f)
        drawLine(
            color = ink,
            start = Offset(sx + sw * 0.52f, sy + sh * 0.52f),
            end = Offset(tx, sy + sh * 0.84f),
            strokeWidth = sw * 0.055f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawMiniChefIcon(sx: Float, sy: Float, sw: Float, sh: Float) {
    val ink = Color(0xFF1A1E17)

    drawRoundRect(
        color = Color(0xFFF5F6F0),
        topLeft = Offset(sx + sw * 0.52f, sy + sh * 0.14f),
        size = Size(sw * 0.28f, sh * 0.28f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = ink,
        topLeft = Offset(sx + sw * 0.52f, sy + sh * 0.14f),
        size = Size(sw * 0.28f, sh * 0.28f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = sw * 0.035f)
    )

    drawCircle(color = ink, radius = sw * 0.11f, center = Offset(sx + sw * 0.66f, sy + sh * 0.50f))
    drawRoundRect(
        color = ink,
        topLeft = Offset(sx + sw * 0.54f, sy + sh * 0.60f),
        size = Size(sw * 0.24f, sh * 0.28f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    drawLine(
        color = ink,
        start = Offset(sx + sw * 0.16f, sy + sh * 0.68f),
        end = Offset(sx + sw * 0.56f, sy + sh * 0.68f),
        strokeWidth = sw * 0.07f,
        cap = StrokeCap.Round
    )
    drawOval(
        color = Color(0xFFA82820),
        topLeft = Offset(sx + sw * 0.18f, sy + sh * 0.34f),
        size = Size(sw * 0.22f, sh * 0.12f)
    )
}

private fun DrawScope.drawMiniSpaceBridgeIcon(sx: Float, sy: Float, sw: Float, sh: Float) {
    val ink = Color(0xFF1A1E17)

    drawRoundRect(
        color = ink,
        topLeft = Offset(sx + sw * 0.22f, sy + sh * 0.64f),
        size = Size(sw * 0.56f, sh * 0.16f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawCircle(color = ink, radius = sw * 0.07f, center = Offset(sx + sw * 0.32f, sy + sh * 0.84f))
    drawCircle(color = ink, radius = sw * 0.07f, center = Offset(sx + sw * 0.68f, sy + sh * 0.84f))

    drawCircle(color = ink, radius = sw * 0.11f, center = Offset(sx + sw * 0.50f, sy + sh * 0.28f))
    drawCircle(color = Color(0xFFF3F4EE), radius = sw * 0.06f, center = Offset(sx + sw * 0.50f, sy + sh * 0.28f))
    drawLine(
        color = ink,
        start = Offset(sx + sw * 0.50f, sy + sh * 0.38f),
        end = Offset(sx + sw * 0.50f, sy + sh * 0.56f),
        strokeWidth = sw * 0.12f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawMiniHockeyIcon(sx: Float, sy: Float, sw: Float, sh: Float) {
    val ink = Color(0xFF1A1E17)
    val red = Color(0xFFA82820)

    val goal = Path().apply {
        moveTo(sx + sw * 0.16f, sy + sh * 0.82f)
        lineTo(sx + sw * 0.16f, sy + sh * 0.24f)
        lineTo(sx + sw * 0.84f, sy + sh * 0.24f)
        lineTo(sx + sw * 0.84f, sy + sh * 0.82f)
    }
    drawPath(goal, color = red, style = Stroke(width = sw * 0.05f, join = StrokeJoin.Round))

    drawCircle(color = ink, radius = sw * 0.11f, center = Offset(sx + sw * 0.52f, sy + sh * 0.42f))
    drawRoundRect(
        color = ink,
        topLeft = Offset(sx + sw * 0.40f, sy + sh * 0.52f),
        size = Size(sw * 0.24f, sh * 0.30f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    val stick = Path().apply {
        moveTo(sx + sw * 0.42f, sy + sh * 0.58f)
        lineTo(sx + sw * 0.26f, sy + sh * 0.82f)
        lineTo(sx + sw * 0.14f, sy + sh * 0.80f)
    }
    drawPath(stick, color = ink, style = Stroke(width = sw * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    drawCircle(color = ink, radius = sw * 0.06f, center = Offset(sx + sw * 0.26f, sy + sh * 0.44f))
}

private fun DrawScope.drawMiniSlalomIcon(sx: Float, sy: Float, sw: Float, sh: Float) {
    val ink = Color(0xFF1A1E17)
    val red = Color(0xFFA82820)

    drawLine(red, Offset(sx + sw * 0.38f, sy + sh * 0.16f), Offset(sx + sw * 0.12f, sy + sh * 0.86f), sw * 0.04f)
    drawLine(red, Offset(sx + sw * 0.62f, sy + sh * 0.16f), Offset(sx + sw * 0.88f, sy + sh * 0.86f), sw * 0.04f)

    drawRoundRect(
        color = ink,
        topLeft = Offset(sx + sw * 0.41f, sy + sh * 0.42f),
        size = Size(sw * 0.18f, sh * 0.38f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawLine(ink, Offset(sx + sw * 0.32f, sy + sh * 0.48f), Offset(sx + sw * 0.68f, sy + sh * 0.48f), sw * 0.07f, StrokeCap.Round)
    drawLine(ink, Offset(sx + sw * 0.30f, sy + sh * 0.72f), Offset(sx + sw * 0.70f, sy + sh * 0.72f), sw * 0.08f, StrokeCap.Round)
}
