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
        isAvailable = true
    ),
    VESELY_KUCHAR(
        code = "ИМ-04",
        titleCz = "Veselý kuchař",
        titleRu = "Весёлый повар",
        subtitle = "Kuchař chytá jídlo na pánvičku",
        isAvailable = true
    ),
    KOSMICKY_MOST(
        code = "ИМ-09",
        titleCz = "Kosmický most",
        titleRu = "Космический мост",
        subtitle = "Kosmonauti a most přes lávu",
        isAvailable = true
    ),
    HOKEJ(
        code = "ИМ-22",
        titleCz = "Veselý hokej",
        titleRu = "Хоккей",
        subtitle = "Brankář chytá střely na branku",
        isAvailable = true
    ),
    AUTOSLALOM(
        code = "ИМ-23",
        titleCz = "Autoslalom",
        titleRu = "Автослалом",
        subtitle = "Závodní formule mezi kužely",
        isAvailable = true
    )
}

@Composable
fun GameHubScreen(
    highScoreA: Int,
    highScoreB: Int,
    oceanHighScoreA: Int = 0,
    oceanHighScoreB: Int = 0,
    chefHighScoreA: Int = 0,
    chefHighScoreB: Int = 0,
    bridgeHighScoreA: Int = 0,
    bridgeHighScoreB: Int = 0,
    hockeyHighScoreA: Int = 0,
    hockeyHighScoreB: Int = 0,
    slalomHighScoreA: Int = 0,
    slalomHighScoreB: Int = 0,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onOpenLeaderboard: () -> Unit,
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

    fun bestScoreFor(game: ClassicGameId): Int = when (game) {
        ClassicGameId.JEN_POCKEJ -> maxOf(highScoreA, highScoreB)
        ClassicGameId.TAJEMSTVI_OCEANU -> maxOf(oceanHighScoreA, oceanHighScoreB)
        ClassicGameId.VESELY_KUCHAR -> maxOf(chefHighScoreA, chefHighScoreB)
        ClassicGameId.KOSMICKY_MOST -> maxOf(bridgeHighScoreA, bridgeHighScoreB)
        ClassicGameId.HOKEJ -> maxOf(hockeyHighScoreA, hockeyHighScoreB)
        ClassicGameId.AUTOSLALOM -> maxOf(slalomHighScoreA, slalomHighScoreB)
    }

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
            HubHeaderBar(
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                onToggleSound = onToggleSound,
                onToggleHaptics = onToggleHaptics,
                onOpenLeaderboard = onOpenLeaderboard
            )

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
                            bestScore = bestScoreFor(game),
                            onClick = { onSelectGame(game) },
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
                            bestScore = bestScoreFor(game),
                            onClick = { onSelectGame(game) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }

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
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onOpenLeaderboard: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                    text = "KOLEKCE ЭЛЕКТРОНИКА  •  6 RETRO HER",
                    color = Color(0xFFB52B24),
                    fontSize = 13.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Vyber hru ze sbírky klasických sovětských digitálních konzolí",
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFD62828), Color(0xFFB52B24))
                        )
                    )
                    .border(1.5.dp, Color(0xFFFFD166), RoundedCornerShape(50))
                    .clickable(onClick = onOpenLeaderboard)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆 REKORDY (TOP 50)",
                    color = Color.White,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Black,
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
    bestScore: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = Brush.verticalGradient(
        listOf(Color(0xFFF4F2EA), Color(0xFFE2DFD4))
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(2.dp, Color(0xFFB52B24), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(7.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MiniConsoleGameIcon(
                game = game,
                modifier = Modifier.size(width = 64.dp, height = 50.dp)
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
                            .background(Color(0xFFB52B24))
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
                    color = Color(0xFF1A1D1A),
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
                text = "▶  HRÁT HRU   (REKORD: $bestScore)",
                color = Color.White,
                fontSize = 9.sp,
                lineHeight = 10.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false
            )
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

        drawRoundRect(
            color = Color(0xFF4A4D47),
            size = size,
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )
        drawRoundRect(
            color = Color(0xFFD8D5CC),
            topLeft = Offset(w * 0.025f, h * 0.03f),
            size = Size(w * 0.95f, h * 0.94f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
        )

        val sx = w * 0.08f
        val sy = h * 0.09f
        val sw = w * 0.84f
        val sh = h * 0.82f

        val btnCol = Color(0xFFD62828)
        val br = w * 0.032f
        drawCircle(btnCol, radius = br, center = Offset(w * 0.045f, h * 0.34f))
        drawCircle(btnCol, radius = br, center = Offset(w * 0.045f, h * 0.68f))
        drawCircle(btnCol, radius = br, center = Offset(w * 0.955f, h * 0.34f))
        drawCircle(btnCol, radius = br, center = Offset(w * 0.955f, h * 0.68f))

        drawRoundRect(
            color = Color(0xFF3E423B),
            topLeft = Offset(sx - 1.5f, sy - 1.5f),
            size = Size(sw + 3f, sh + 3f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        when (game) {
            ClassicGameId.JEN_POCKEJ -> drawRichWolfIllustration(sx, sy, sw, sh)
            ClassicGameId.TAJEMSTVI_OCEANU -> drawRichOctopusIllustration(sx, sy, sw, sh)
            ClassicGameId.VESELY_KUCHAR -> drawRichChefIllustration(sx, sy, sw, sh)
            ClassicGameId.KOSMICKY_MOST -> drawRichSpaceBridgeIllustration(sx, sy, sw, sh)
            ClassicGameId.HOKEJ -> drawRichHockeyIllustration(sx, sy, sw, sh)
            ClassicGameId.AUTOSLALOM -> drawRichSlalomIllustration(sx, sy, sw, sh)
        }
    }
}

private fun DrawScope.drawRichWolfIllustration(sx: Float, sy: Float, sw: Float, sh: Float) {
    val outline = Color(0xFF1C201B)
    val strokeW = sw * 0.026f

    drawRoundRect(
        color = Color(0xFFBDC7AD),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(sw * 0.07f, sw * 0.07f)
    )

    val roof = Path().apply {
        moveTo(sx + sw * 0.03f, sy + sh * 0.28f)
        lineTo(sx + sw * 0.16f, sy + sh * 0.08f)
        lineTo(sx + sw * 0.32f, sy + sh * 0.30f)
    }
    drawPath(roof, color = Color(0xFFB52B24), style = Stroke(width = sw * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    drawLine(
        color = Color(0xFF8C5828),
        start = Offset(sx + sw * 0.04f, sy + sh * 0.44f),
        end = Offset(sx + sw * 0.42f, sy + sh * 0.62f),
        strokeWidth = sw * 0.055f,
        cap = StrokeCap.Round
    )

    drawOval(
        color = Color(0xFFFFFDF7),
        topLeft = Offset(sx + sw * 0.27f, sy + sh * 0.41f),
        size = Size(sw * 0.14f, sh * 0.13f)
    )
    drawOval(
        color = outline,
        topLeft = Offset(sx + sw * 0.27f, sy + sh * 0.41f),
        size = Size(sw * 0.14f, sh * 0.13f),
        style = Stroke(width = strokeW)
    )

    val torso = Path().apply {
        moveTo(sx + sw * 0.58f, sy + sh * 0.52f)
        lineTo(sx + sw * 0.84f, sy + sh * 0.52f)
        lineTo(sx + sw * 0.88f, sy + sh * 0.94f)
        lineTo(sx + sw * 0.54f, sy + sh * 0.94f)
        close()
    }
    drawPath(torso, color = Color(0xFF5D675F))
    drawPath(torso, color = outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    val snout = Path().apply {
        moveTo(sx + sw * 0.64f, sy + sh * 0.34f)
        lineTo(sx + sw * 0.42f, sy + sh * 0.33f)
        cubicTo(
            sx + sw * 0.40f, sy + sh * 0.45f,
            sx + sw * 0.50f, sy + sh * 0.49f,
            sx + sw * 0.65f, sy + sh * 0.47f
        )
        close()
    }
    drawPath(snout, color = Color(0xFFD8DDD0))
    drawPath(snout, color = outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
    drawCircle(color = outline, radius = sw * 0.045f, center = Offset(sx + sw * 0.43f, sy + sh * 0.33f))

    val basket = Path().apply {
        moveTo(sx + sw * 0.22f, sy + sh * 0.63f)
        lineTo(sx + sw * 0.58f, sy + sh * 0.63f)
        lineTo(sx + sw * 0.52f, sy + sh * 0.89f)
        lineTo(sx + sw * 0.28f, sy + sh * 0.89f)
        close()
    }
    drawPath(basket, color = Color(0xFFA6662C))
    drawPath(basket, color = outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
}

private fun DrawScope.drawRichOctopusIllustration(sx: Float, sy: Float, sw: Float, sh: Float) {
    val outline = Color(0xFF172226)
    val strokeW = sw * 0.025f

    drawRoundRect(
        color = Color(0xFF9AC2BA),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(sw * 0.07f, sw * 0.07f)
    )
    drawLine(
        color = Color(0xFF245E75),
        start = Offset(sx + sw * 0.04f, sy + sh * 0.24f),
        end = Offset(sx + sw * 0.96f, sy + sh * 0.24f),
        strokeWidth = sw * 0.035f,
        cap = StrokeCap.Round
    )

    drawRoundRect(
        color = Color(0xFF7D481B),
        topLeft = Offset(sx + sw * 0.08f, sy + sh * 0.72f),
        size = Size(sw * 0.26f, sh * 0.20f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    val octopusColor = Color(0xFFB8323C)
    for ((mx, my, endPt) in listOf(
        Triple(0.56f, 0.62f, 0.38f to 0.84f),
        Triple(0.68f, 0.70f, 0.58f to 0.92f),
        Triple(0.80f, 0.66f, 0.86f to 0.88f)
    )) {
        val tPath = Path().apply {
            moveTo(sx + sw * 0.68f, sy + sh * 0.52f)
            quadraticTo(sx + sw * mx, sy + sh * my, sx + sw * endPt.first, sy + sh * endPt.second)
        }
        drawPath(tPath, color = outline, style = Stroke(width = sw * 0.07f, cap = StrokeCap.Round))
        drawPath(tPath, color = octopusColor, style = Stroke(width = sw * 0.045f, cap = StrokeCap.Round))
    }

    drawOval(
        color = octopusColor,
        topLeft = Offset(sx + sw * 0.50f, sy + sh * 0.24f),
        size = Size(sw * 0.36f, sh * 0.34f)
    )
    drawOval(
        color = outline,
        topLeft = Offset(sx + sw * 0.50f, sy + sh * 0.24f),
        size = Size(sw * 0.36f, sh * 0.34f),
        style = Stroke(width = strokeW)
    )
    drawCircle(Color.White, radius = sw * 0.055f, center = Offset(sx + sw * 0.61f, sy + sh * 0.42f))
    drawCircle(Color.White, radius = sw * 0.055f, center = Offset(sx + sw * 0.73f, sy + sh * 0.42f))
}

private fun DrawScope.drawRichChefIllustration(sx: Float, sy: Float, sw: Float, sh: Float) {
    val outline = Color(0xFF211E1A)
    val strokeW = sw * 0.026f

    drawRoundRect(
        color = Color(0xFFDAD8C5),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(sw * 0.07f, sw * 0.07f)
    )

    val coat = Path().apply {
        moveTo(sx + sw * 0.56f, sy + sh * 0.58f)
        lineTo(sx + sw * 0.88f, sy + sh * 0.58f)
        lineTo(sx + sw * 0.92f, sy + sh * 0.94f)
        lineTo(sx + sw * 0.52f, sy + sh * 0.94f)
        close()
    }
    drawPath(coat, color = Color(0xFFF7F7F2))
    drawPath(coat, color = outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    drawCircle(Color(0xFFF2C9A0), radius = sw * 0.13f, center = Offset(sx + sw * 0.72f, sy + sh * 0.45f))
    drawCircle(outline, radius = sw * 0.13f, center = Offset(sx + sw * 0.72f, sy + sh * 0.45f), style = Stroke(width = strokeW))

    drawCircle(Color.White, radius = sw * 0.10f, center = Offset(sx + sw * 0.72f, sy + sh * 0.20f))
    drawLine(
        color = outline,
        start = Offset(sx + sw * 0.36f, sy + sh * 0.71f),
        end = Offset(sx + sw * 0.60f, sy + sh * 0.71f),
        strokeWidth = sw * 0.045f,
        cap = StrokeCap.Round
    )
    val pan = Path().apply {
        moveTo(sx + sw * 0.08f, sy + sh * 0.67f)
        lineTo(sx + sw * 0.40f, sy + sh * 0.67f)
        lineTo(sx + sw * 0.35f, sy + sh * 0.78f)
        lineTo(sx + sw * 0.13f, sy + sh * 0.78f)
        close()
    }
    drawPath(pan, color = Color(0xFF2B2F2E))
    drawOval(Color(0xFFC43B2B), topLeft = Offset(sx + sw * 0.12f, sy + sh * 0.42f), size = Size(sw * 0.22f, sh * 0.11f))
}

private fun DrawScope.drawRichSpaceBridgeIllustration(sx: Float, sy: Float, sw: Float, sh: Float) {
    val outline = Color(0xFF171A21)
    val strokeW = sw * 0.025f

    drawRoundRect(
        color = Color(0xFFA8B5B8),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(sw * 0.07f, sw * 0.07f)
    )
    drawRoundRect(
        color = Color(0xFF283347),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh * 0.34f),
        cornerRadius = CornerRadius(sw * 0.07f, sw * 0.07f)
    )
    drawCircle(Color(0xFFF2C14E), radius = sw * 0.065f, center = Offset(sx + sw * 0.78f, sy + sh * 0.17f))
    drawRoundRect(
        color = Color(0xFFD93829),
        topLeft = Offset(sx + sw * 0.04f, sy + sh * 0.82f),
        size = Size(sw * 0.92f, sh * 0.14f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color(0xFF3B6E8C),
        topLeft = Offset(sx + sw * 0.28f, sy + sh * 0.64f),
        size = Size(sw * 0.46f, sh * 0.15f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(sx + sw * 0.28f, sy + sh * 0.64f),
        size = Size(sw * 0.46f, sh * 0.15f),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = strokeW)
    )
    drawCircle(Color.White, radius = sw * 0.085f, center = Offset(sx + sw * 0.50f, sy + sh * 0.27f))
    drawCircle(outline, radius = sw * 0.085f, center = Offset(sx + sw * 0.50f, sy + sh * 0.27f), style = Stroke(width = sw * 0.022f))
}

private fun DrawScope.drawRichHockeyIllustration(sx: Float, sy: Float, sw: Float, sh: Float) {
    val outline = Color(0xFF1A1E24)
    val strokeW = sw * 0.026f

    drawRoundRect(
        color = Color(0xFFD6E3E0),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(sw * 0.07f, sw * 0.07f)
    )

    val goalFrame = Path().apply {
        moveTo(sx + sw * 0.14f, sy + sh * 0.84f)
        lineTo(sx + sw * 0.14f, sy + sh * 0.20f)
        lineTo(sx + sw * 0.86f, sy + sh * 0.20f)
        lineTo(sx + sw * 0.86f, sy + sh * 0.84f)
    }
    drawPath(goalFrame, color = Color(0xFFC42B23), style = Stroke(width = sw * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    val jersey = Path().apply {
        moveTo(sx + sw * 0.44f, sy + sh * 0.46f)
        lineTo(sx + sw * 0.76f, sy + sh * 0.46f)
        lineTo(sx + sw * 0.80f, sy + sh * 0.86f)
        lineTo(sx + sw * 0.40f, sy + sh * 0.86f)
        close()
    }
    drawPath(jersey, color = Color(0xFFD62828))
    drawPath(jersey, color = outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    drawOval(Color.White, topLeft = Offset(sx + sw * 0.50f, sy + sh * 0.24f), size = Size(sw * 0.20f, sh * 0.24f))
    drawOval(outline, topLeft = Offset(sx + sw * 0.50f, sy + sh * 0.24f), size = Size(sw * 0.20f, sh * 0.24f), style = Stroke(width = strokeW))
    drawOval(outline, topLeft = Offset(sx + sw * 0.18f, sy + sh * 0.42f), size = Size(sw * 0.15f, sh * 0.09f))
}

private fun DrawScope.drawRichSlalomIllustration(sx: Float, sy: Float, sw: Float, sh: Float) {
    val outline = Color(0xFF181B18)
    val strokeW = sw * 0.025f

    drawRoundRect(
        color = Color(0xFF6B8A47),
        topLeft = Offset(sx, sy),
        size = Size(sw, sh),
        cornerRadius = CornerRadius(sw * 0.07f, sw * 0.07f)
    )

    val road = Path().apply {
        moveTo(sx + sw * 0.36f, sy + sh * 0.06f)
        lineTo(sx + sw * 0.64f, sy + sh * 0.06f)
        lineTo(sx + sw * 0.92f, sy + sh * 0.94f)
        lineTo(sx + sw * 0.08f, sy + sh * 0.94f)
        close()
    }
    drawPath(road, color = Color(0xFFC8CCC2))
    drawPath(road, color = outline, style = Stroke(width = strokeW))

    val carBody = Path().apply {
        moveTo(sx + sw * 0.45f, sy + sh * 0.30f)
        lineTo(sx + sw * 0.55f, sy + sh * 0.30f)
        lineTo(sx + sw * 0.63f, sy + sh * 0.62f)
        lineTo(sx + sw * 0.65f, sy + sh * 0.86f)
        lineTo(sx + sw * 0.35f, sy + sh * 0.86f)
        lineTo(sx + sw * 0.37f, sy + sh * 0.62f)
        close()
    }
    drawPath(carBody, color = Color(0xFFD62828))
    drawPath(carBody, color = outline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
    drawCircle(Color.White, radius = sw * 0.065f, center = Offset(sx + sw * 0.50f, sy + sh * 0.60f))
}
