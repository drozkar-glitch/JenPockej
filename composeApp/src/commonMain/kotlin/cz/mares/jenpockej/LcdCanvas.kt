package cz.mares.jenpockej

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun JenPockejLcdScreen(
    snapshot: GameSnapshot,
    onCornerTap: (BasketPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.pointerInput(Unit) {
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
        val w = size.width
        val h = size.height

        // Podkladovy zelenkavy LCD panel
        drawRoundRect(
            color = LcdBgColor,
            size = size,
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Jemny vnitrni stin sklicka displeje
        drawRoundRect(
            color = LcdSilkDark.copy(alpha = 0.25f),
            topLeft = Offset(3f, 3f),
            size = Size(w - 6f, h - 6f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = 3f)
        )

        drawCoopRampsAndScenery(w, h)
        drawDetailedHare(w, h, snapshot.hareVisible, snapshot.hareBellUp)
        drawScoreDigits(w, h, snapshot.score)
        drawPenaltyChicksRow(w, h, snapshot.penaltyHalfPoints, snapshot.blinkPhase)

        drawDetailedWolfAndBaskets(
            w = w,
            h = h,
            wolfPos = snapshot.wolfPosition,
            celebrating = snapshot.celebrationTicks > 0,
            blinkPhase = snapshot.blinkPhase
        )

        drawRollingEggs(w, h, snapshot.eggs)
        drawCrashAndRunningChicks(w, h, snapshot.crashAnim)

        if (snapshot.isGameOver) {
            drawGameOverIndicator(w, h, snapshot.blinkPhase)
        }
    }
}

private fun DrawScope.drawCoopRampsAndScenery(w: Float, h: Float) {
    val s = w * 0.00125f
    val railStroke = (w * 0.009f).coerceAtLeast(2.5f)
    val braceStroke = (w * 0.004f).coerceAtLeast(1.5f)

    // 4 sikme zlabky s drevenym srafovanim a podperami u kraje
    drawRampWithBraces(
        start = Offset(w * 0.025f, h * 0.24f),
        end = Offset(w * 0.265f, h * 0.385f),
        railStroke = railStroke,
        braceStroke = braceStroke
    )
    drawRampWithBraces(
        start = Offset(w * 0.025f, h * 0.54f),
        end = Offset(w * 0.265f, h * 0.685f),
        railStroke = railStroke,
        braceStroke = braceStroke
    )
    drawRampWithBraces(
        start = Offset(w * 0.975f, h * 0.24f),
        end = Offset(w * 0.735f, h * 0.385f),
        railStroke = railStroke,
        braceStroke = braceStroke
    )
    drawRampWithBraces(
        start = Offset(w * 0.975f, h * 0.54f),
        end = Offset(w * 0.735f, h * 0.685f),
        railStroke = railStroke,
        braceStroke = braceStroke
    )

    // Svisle sloupky kurniku na levem a pravem okraji
    drawLine(LcdSilkDark, Offset(w * 0.025f, h * 0.14f), Offset(w * 0.025f, h * 0.88f), railStroke)
    drawLine(LcdSilkDark, Offset(w * 0.975f, h * 0.14f), Offset(w * 0.975f, h * 0.88f), railStroke)

    // 4 slepice na bidylkach
    drawDetailedHen(Offset(w * 0.062f, h * 0.20f), s, facingRight = true)
    drawDetailedHen(Offset(w * 0.062f, h * 0.50f), s, facingRight = true)
    drawDetailedHen(Offset(w * 0.938f, h * 0.20f), s, facingRight = false)
    drawDetailedHen(Offset(w * 0.938f, h * 0.50f), s, facingRight = false)

    // Trsy travy a zem na spodnim okraji LCD
    drawGrassTuft(Offset(w * 0.35f, h * 0.88f), s)
    drawGrassTuft(Offset(w * 0.50f, h * 0.89f), s)
    drawGrassTuft(Offset(w * 0.65f, h * 0.88f), s)
}

private fun DrawScope.drawRampWithBraces(
    start: Offset,
    end: Offset,
    railStroke: Float,
    braceStroke: Float
) {
    drawLine(
        color = LcdSilkRed,
        start = start,
        end = end,
        strokeWidth = railStroke,
        cap = StrokeCap.Round
    )
    val underOffset = Offset(0f, railStroke * 2.1f)
    drawLine(
        color = LcdSilkDark,
        start = start + underOffset,
        end = end + underOffset,
        strokeWidth = braceStroke,
        cap = StrokeCap.Round
    )
    for (i in 1..4) {
        val t = i / 5f
        val topPt = Offset(
            x = start.x + (end.x - start.x) * t,
            y = start.y + (end.y - start.y) * t
        )
        drawLine(
            color = LcdSilkDark,
            start = topPt,
            end = topPt + underOffset,
            strokeWidth = braceStroke
        )
    }
}

private fun DrawScope.drawGrassTuft(base: Offset, s: Float) {
    val path = Path().apply {
        moveTo(base.x, base.y)
        lineTo(base.x - 9f * s, base.y - 12f * s)
        moveTo(base.x, base.y)
        lineTo(base.x, base.y - 15f * s)
        moveTo(base.x, base.y)
        lineTo(base.x + 9f * s, base.y - 12f * s)
    }
    drawPath(
        path = path,
        color = LcdSilkDark,
        style = Stroke(width = 2.4f * s, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawRollingEggs(w: Float, h: Float, activeEggs: List<EggItem>) {
    val eggW = w * 0.032f
    val eggH = h * 0.044f

    for (ramp in 0..3) {
        for (step in 0..4) {
            val isLit = activeEggs.any { it.ramp == ramp && it.step == step }
            val ink = if (isLit) LcdInkOn else LcdInkGhost
            val pos = eggStepCoordinate(w, h, ramp, step)
            val tilt = when (ramp) {
                0, 1 -> 34f * (step + 1)
                else -> -34f * (step + 1)
            }
            rotate(degrees = tilt, pivot = pos) {
                drawOval(
                    color = ink,
                    topLeft = Offset(pos.x - eggW * 0.5f, pos.y - eggH * 0.5f),
                    size = Size(eggW, eggH)
                )
                if (isLit) {
                    // Drobny odlesk na kutalejicim se vajicku
                    drawCircle(
                        color = LcdBgColor,
                        radius = eggW * 0.14f,
                        center = Offset(pos.x - eggW * 0.12f, pos.y - eggH * 0.14f)
                    )
                }
            }
        }
    }
}

private fun eggStepCoordinate(w: Float, h: Float, ramp: Int, step: Int): Offset {
    val t = step / 4f
    return when (ramp) {
        0 -> Offset(w * (0.095f + t * 0.165f), h * (0.235f + t * 0.135f))
        1 -> Offset(w * (0.095f + t * 0.165f), h * (0.535f + t * 0.135f))
        2 -> Offset(w * (0.905f - t * 0.165f), h * (0.235f + t * 0.135f))
        else -> Offset(w * (0.905f - t * 0.165f), h * (0.535f + t * 0.135f))
    }
}

private fun DrawScope.drawScoreDigits(w: Float, h: Float, score: Int) {
    val clamped = score.coerceIn(0, 9999)
    val thousands = clamped / 1000
    val hundreds = (clamped / 100) % 10
    val tens = (clamped / 10) % 10
    val units = clamped % 10

    val digitW = w * 0.042f
    val digitH = h * 0.125f
    val startX = w * 0.585f
    val topY = h * 0.065f
    val gap = w * 0.015f

    drawSingleSevenSegment(startX, topY, digitW, digitH, if (clamped >= 1000) thousands else -1)
    drawSingleSevenSegment(startX + (digitW + gap), topY, digitW, digitH, if (clamped >= 100) hundreds else -1)
    drawSingleSevenSegment(startX + (digitW + gap) * 2f, topY, digitW, digitH, if (clamped >= 10) tens else -1)
    drawSingleSevenSegment(startX + (digitW + gap) * 3f, topY, digitW, digitH, units)
}

private fun DrawScope.drawSingleSevenSegment(
    x: Float,
    y: Float,
    dw: Float,
    dh: Float,
    value: Int
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
    val sw = (dw * 0.19f).coerceAtLeast(2.6f)
    val midY = y + dh * 0.5f
    val botY = y + dh

    fun segColor(bit: Int): Color = if ((mask and (1 shl bit)) != 0) LcdInkOn else LcdInkGhost

    drawLine(segColor(0), Offset(x, y), Offset(x + dw, y), sw, StrokeCap.Round)
    drawLine(segColor(1), Offset(x, y), Offset(x, midY), sw, StrokeCap.Round)
    drawLine(segColor(2), Offset(x + dw, y), Offset(x + dw, midY), sw, StrokeCap.Round)
    drawLine(segColor(3), Offset(x, midY), Offset(x + dw, midY), sw, StrokeCap.Round)
    drawLine(segColor(4), Offset(x, midY), Offset(x, botY), sw, StrokeCap.Round)
    drawLine(segColor(5), Offset(x + dw, midY), Offset(x + dw, botY), sw, StrokeCap.Round)
    drawLine(segColor(6), Offset(x, botY), Offset(x + dw, botY), sw, StrokeCap.Round)
}

private fun DrawScope.drawGameOverIndicator(w: Float, h: Float, blinkPhase: Boolean) {
    if (!blinkPhase) return
    drawRoundRect(
        color = LcdSilkRed,
        topLeft = Offset(w * 0.38f, h * 0.05f),
        size = Size(w * 0.18f, h * 0.16f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 3.5f)
    )
}
