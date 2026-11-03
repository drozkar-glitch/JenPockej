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
        // Zelenkave pozadi vyplnujici 100 % LCD okna
        drawRoundRect(
            color = LcdBgColor,
            size = size,
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Jemny vnitrni stin pod sklickem LCD
        drawRoundRect(
            color = LcdSilkDark.copy(alpha = 0.22f),
            topLeft = Offset(3f, 3f),
            size = Size(size.width - 6f, size.height - 6f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = 3f)
        )

        // Pevny viewport 1000x520 zajistujici 1:1 proporce bez deformace
        val vp = LcdViewport(size.width, size.height)

        drawCoopRampsAndScenery(vp)
        drawDetailedHare(vp, snapshot.hareVisible, snapshot.hareBellUp)
        drawScoreDigits(vp, snapshot.score)
        drawPenaltyChicksRow(vp, snapshot.penaltyHalfPoints, snapshot.blinkPhase)

        drawDetailedWolfAndBaskets(
            vp = vp,
            wolfPos = snapshot.wolfPosition,
            celebrating = snapshot.celebrationTicks > 0,
            blinkPhase = snapshot.blinkPhase
        )

        drawRollingEggsOnTopOfRamps(vp, snapshot.eggs)
        drawCrashAndRunningChicks(vp, snapshot.crashAnim)

        if (snapshot.isGameOver) {
            drawGameOverFrame(vp, snapshot.blinkPhase)
        }
    }
}

private fun DrawScope.drawCoopRampsAndScenery(vp: LcdViewport) {
    val postStroke = vp.s(7.5f)

    // Svisle sloupky kurniku na levem a pravem kraji
    drawLine(LcdSilkDark, vp.pt(26f, 75f), vp.pt(26f, 478f), postStroke, StrokeCap.Round)
    drawLine(LcdSilkDark, vp.pt(974f, 75f), vp.pt(974f, 478f), postStroke, StrokeCap.Round)

    // 4 sikme zlabky se vzperami
    drawWoodenRamp(vp, startX = 28f, startY = 148f, endX = 278f, endY = 228f)
    drawWoodenRamp(vp, startX = 28f, startY = 308f, endX = 278f, endY = 388f)
    drawWoodenRamp(vp, startX = 972f, startY = 148f, endX = 722f, endY = 228f)
    drawWoodenRamp(vp, startX = 972f, startY = 308f, endX = 722f, endY = 388f)

    // 4 slepice v hnizdech na okrajich
    drawDetailedHen(vp, cx = 62f, cy = 114f, facingRight = true)
    drawDetailedHen(vp, cx = 62f, cy = 274f, facingRight = true)
    drawDetailedHen(vp, cx = 938f, cy = 114f, facingRight = false)
    drawDetailedHen(vp, cx = 938f, cy = 274f, facingRight = false)

    // Trsy travy na zemi
    drawGrassTuft(vp, 355f, 474f)
    drawGrassTuft(vp, 500f, 478f)
    drawGrassTuft(vp, 645f, 474f)
}

private fun DrawScope.drawWoodenRamp(
    vp: LcdViewport,
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float
) {
    // Horni cervena kolejnicka (po ktere se kutali vajicko)
    drawLine(
        color = LcdSilkRed,
        start = vp.pt(startX, startY),
        end = vp.pt(endX, endY),
        strokeWidth = vp.s(7.5f),
        cap = StrokeCap.Round
    )
    // Spodni nosny tram a svisle vzpery
    val drop = 15f
    drawLine(
        color = LcdSilkDark,
        start = vp.pt(startX, startY + drop),
        end = vp.pt(endX, endY + drop),
        strokeWidth = vp.s(3.5f),
        cap = StrokeCap.Round
    )
    for (i in 1..4) {
        val t = i / 5f
        val rx = startX + (endX - startX) * t
        val ry = startY + (endY - startY) * t
        drawLine(
            color = LcdSilkDark,
            start = vp.pt(rx, ry + 2f),
            end = vp.pt(rx, ry + drop),
            strokeWidth = vp.s(3f)
        )
    }
}

private fun DrawScope.drawGrassTuft(vp: LcdViewport, gx: Float, gy: Float) {
    val path = Path().apply {
        moveTo(vp.x(gx), vp.y(gy))
        lineTo(vp.x(gx - 12f), vp.y(gy - 16f))
        moveTo(vp.x(gx), vp.y(gy))
        lineTo(vp.x(gx), vp.y(gy - 20f))
        moveTo(vp.x(gx), vp.y(gy))
        lineTo(vp.x(gx + 12f), vp.y(gy - 16f))
    }
    drawPath(
        path = path,
        color = LcdSilkDark,
        style = Stroke(width = vp.s(3.2f), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawRollingEggsOnTopOfRamps(
    vp: LcdViewport,
    activeEggs: List<EggItem>
) {
    val eggW = vp.s(25f)
    val eggH = vp.s(33f)

    for (ramp in 0..3) {
        for (step in 0..4) {
            val isLit = activeEggs.any { it.ramp == ramp && it.step == step }
            val ink = if (isLit) LcdInkOn else LcdInkGhost
            val pos = eggPositionAboveRail(vp, ramp, step)
            val tilt = when (ramp) {
                0, 1 -> 32f * (step + 1)
                else -> -32f * (step + 1)
            }
            rotate(degrees = tilt, pivot = pos) {
                drawOval(
                    color = ink,
                    topLeft = Offset(pos.x - eggW * 0.5f, pos.y - eggH * 0.5f),
                    size = Size(eggW, eggH)
                )
                if (isLit) {
                    drawCircle(
                        color = LcdBgColor,
                        radius = eggW * 0.15f,
                        center = Offset(pos.x - eggW * 0.14f, pos.y - eggH * 0.16f)
                    )
                }
            }
        }
    }
}

private fun eggPositionAboveRail(vp: LcdViewport, ramp: Int, step: Int): Offset {
    // Posun t od hnizda po okraj zlabku, posazene o 19 jednotek NAD kolejnicku
    val t = 0.22f + (step / 4f) * 0.76f
    val liftAboveRail = 19f
    return when (ramp) {
        0 -> vp.pt(28f + 250f * t, 148f + 80f * t - liftAboveRail)
        1 -> vp.pt(28f + 250f * t, 308f + 80f * t - liftAboveRail)
        2 -> vp.pt(972f - 250f * t, 148f + 80f * t - liftAboveRail)
        else -> vp.pt(972f - 250f * t, 308f + 80f * t - liftAboveRail)
    }
}

private fun DrawScope.drawScoreDigits(vp: LcdViewport, score: Int) {
    val clamped = score.coerceIn(0, 9999)
    val thousands = clamped / 1000
    val hundreds = (clamped / 100) % 10
    val tens = (clamped / 10) % 10
    val units = clamped % 10

    val dw = 38f
    val dh = 66f
    val startX = 590f
    val topY = 34f
    val gap = 14f

    drawSevenSegmentDigit(vp, startX, topY, dw, dh, if (clamped >= 1000) thousands else -1)
    drawSevenSegmentDigit(vp, startX + (dw + gap), topY, dw, dh, if (clamped >= 100) hundreds else -1)
    drawSevenSegmentDigit(vp, startX + (dw + gap) * 2f, topY, dw, dh, if (clamped >= 10) tens else -1)
    drawSevenSegmentDigit(vp, startX + (dw + gap) * 3f, topY, dw, dh, units)
}

private fun DrawScope.drawSevenSegmentDigit(
    vp: LcdViewport,
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
    val sw = vp.s(7.2f)
    val midY = y + dh * 0.5f
    val botY = y + dh

    fun segColor(bit: Int): Color = if ((mask and (1 shl bit)) != 0) LcdInkOn else LcdInkGhost

    drawLine(segColor(0), vp.pt(x, y), vp.pt(x + dw, y), sw, StrokeCap.Round)
    drawLine(segColor(1), vp.pt(x, y), vp.pt(x, midY), sw, StrokeCap.Round)
    drawLine(segColor(2), vp.pt(x + dw, y), vp.pt(x + dw, midY), sw, StrokeCap.Round)
    drawLine(segColor(3), vp.pt(x, midY), vp.pt(x + dw, midY), sw, StrokeCap.Round)
    drawLine(segColor(4), vp.pt(x, midY), vp.pt(x, botY), sw, StrokeCap.Round)
    drawLine(segColor(5), vp.pt(x + dw, midY), vp.pt(x + dw, botY), sw, StrokeCap.Round)
    drawLine(segColor(6), vp.pt(x, botY), vp.pt(x + dw, botY), sw, StrokeCap.Round)
}

private fun DrawScope.drawGameOverFrame(vp: LcdViewport, blinkPhase: Boolean) {
    if (!blinkPhase) return
    drawRoundRect(
        color = LcdSilkRed,
        topLeft = vp.pt(368f, 52f),
        size = Size(vp.s(165f), vp.s(60f)),
        cornerRadius = CornerRadius(vp.s(8f), vp.s(8f)),
        style = Stroke(width = vp.s(4f))
    )
}
