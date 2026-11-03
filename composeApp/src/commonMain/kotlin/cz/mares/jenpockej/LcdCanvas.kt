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

private val LcdBgColor = Color(0xFF9BA889)
private val LcdInkOn = Color(0xFF1C2318)
private val LcdInkGhost = Color(0xFF889677)
private val LcdAccentRed = Color(0xFF7A2822)

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
        drawRoundRect(
            color = LcdBgColor,
            size = size,
            cornerRadius = CornerRadius(14f, 14f)
        )

        val w = size.width
        val h = size.height

        drawHenAndRamps(w, h)
        drawHareWithBell(w, h, snapshot.hareVisible, snapshot.hareBellUp)
        drawSevenSegmentScore(w, h, snapshot.score)
        drawPenaltyChicks(w, h, snapshot.penaltyHalfPoints, snapshot.blinkPhase)
        drawWolfAndBaskets(w, h, snapshot.wolfPosition)
        drawRollingEggs(w, h, snapshot.eggs)
        drawCrashAndRunningChick(w, h, snapshot.crashAnim)

        if (snapshot.isGameOver) {
            drawGameOverBanner(w, h, snapshot.blinkPhase)
        }
    }
}

private fun DrawScope.drawHenAndRamps(w: Float, h: Float) {
    val strokeW = (w * 0.009f).coerceAtLeast(2.2f)

    // 4 sikme zlabky pro kutaleni vajicek
    drawLine(LcdInkOn, Offset(w * 0.04f, h * 0.25f), Offset(w * 0.27f, h * 0.39f), strokeW)
    drawLine(LcdInkOn, Offset(w * 0.04f, h * 0.55f), Offset(w * 0.27f, h * 0.69f), strokeW)
    drawLine(LcdInkOn, Offset(w * 0.96f, h * 0.25f), Offset(w * 0.73f, h * 0.39f), strokeW)
    drawLine(LcdInkOn, Offset(w * 0.96f, h * 0.55f), Offset(w * 0.73f, h * 0.69f), strokeW)

    // Slepice na bidylkach v rozich
    drawHenSilhouette(Offset(w * 0.055f, h * 0.19f), w * 0.032f, facingRight = true)
    drawHenSilhouette(Offset(w * 0.055f, h * 0.49f), w * 0.032f, facingRight = true)
    drawHenSilhouette(Offset(w * 0.945f, h * 0.19f), w * 0.032f, facingRight = false)
    drawHenSilhouette(Offset(w * 0.945f, h * 0.49f), w * 0.032f, facingRight = false)
}

private fun DrawScope.drawHenSilhouette(center: Offset, r: Float, facingRight: Boolean) {
    val dir = if (facingRight) 1f else -1f
    drawOval(
        color = LcdInkOn,
        topLeft = Offset(center.x - r, center.y - r * 0.7f),
        size = Size(r * 2f, r * 1.4f)
    )
    drawCircle(
        color = LcdInkOn,
        radius = r * 0.55f,
        center = Offset(center.x + dir * r * 0.75f, center.y - r * 0.65f)
    )
    // Zobacek
    drawLine(
        color = LcdInkOn,
        start = Offset(center.x + dir * r * 1.2f, center.y - r * 0.65f),
        end = Offset(center.x + dir * r * 1.55f, center.y - r * 0.55f),
        strokeWidth = (r * 0.25f).coerceAtLeast(2f)
    )
}

private fun DrawScope.drawHareWithBell(w: Float, h: Float, visible: Boolean, bellUp: Boolean) {
    val ink = if (visible) LcdInkOn else LcdInkGhost
    val hx = w * 0.23f
    val hy = h * 0.13f
    val r = w * 0.028f

    // Hlava a usi zajicka
    drawCircle(ink, radius = r, center = Offset(hx, hy))
    drawOval(
        color = ink,
        topLeft = Offset(hx - r * 0.75f, hy - r * 2.3f),
        size = Size(r * 0.55f, r * 1.5f)
    )
    drawOval(
        color = ink,
        topLeft = Offset(hx + r * 0.15f, hy - r * 2.3f),
        size = Size(r * 0.55f, r * 1.5f)
    )

    // Packa se zvoneckem (nahore nebo dole)
    val bellUpInk = if (visible && bellUp) LcdInkOn else LcdInkGhost
    val bellDownInk = if (visible && !bellUp) LcdInkOn else LcdInkGhost

    drawCircle(bellUpInk, radius = r * 0.48f, center = Offset(hx + r * 1.55f, hy - r * 0.65f))
    drawLine(bellUpInk, Offset(hx + r * 0.8f, hy - r * 0.2f), Offset(hx + r * 1.45f, hy - r * 0.6f), 3f)

    drawCircle(bellDownInk, radius = r * 0.48f, center = Offset(hx + r * 1.55f, hy + r * 0.55f))
    drawLine(bellDownInk, Offset(hx + r * 0.8f, hy + r * 0.1f), Offset(hx + r * 1.45f, hy + r * 0.5f), 3f)
}

private fun DrawScope.drawRollingEggs(w: Float, h: Float, activeEggs: List<EggItem>) {
    for (ramp in 0..3) {
        for (step in 0..4) {
            val isLit = activeEggs.any { it.ramp == ramp && it.step == step }
            val ink = if (isLit) LcdInkOn else LcdInkGhost
            val pos = eggStepCoordinate(w, h, ramp, step)
            val tilt = when (ramp) {
                0, 1 -> 28f * (step + 1)
                else -> -28f * (step + 1)
            }
            rotate(degrees = tilt, pivot = pos) {
                drawOval(
                    color = ink,
                    topLeft = Offset(pos.x - w * 0.018f, pos.y - h * 0.022f),
                    size = Size(w * 0.036f, h * 0.044f)
                )
            }
        }
    }
}

private fun eggStepCoordinate(w: Float, h: Float, ramp: Int, step: Int): Offset {
    val t = step / 4f
    return when (ramp) {
        0 -> Offset(w * (0.09f + t * 0.17f), h * (0.24f + t * 0.13f))
        1 -> Offset(w * (0.09f + t * 0.17f), h * (0.54f + t * 0.13f))
        2 -> Offset(w * (0.91f - t * 0.17f), h * (0.24f + t * 0.13f))
        else -> Offset(w * (0.91f - t * 0.17f), h * (0.54f + t * 0.13f))
    }
}

private fun DrawScope.drawWolfAndBaskets(w: Float, h: Float, wolfPos: BasketPosition) {
    val facingLeft = wolfPos == BasketPosition.TOP_LEFT || wolfPos == BasketPosition.BOTTOM_LEFT
    val leftBodyInk = if (facingLeft) LcdInkOn else LcdInkGhost
    val rightBodyInk = if (!facingLeft) LcdInkOn else LcdInkGhost

    // Silueta vlka otocena doleva a doprava
    drawWolfFigure(w, h, centerX = w * 0.44f, facingLeft = true, ink = leftBodyInk)
    drawWolfFigure(w, h, centerX = w * 0.56f, facingLeft = false, ink = rightBodyInk)

    // 4 polohy kosiku
    drawBasketAt(
        center = Offset(w * 0.30f, h * 0.41f),
        w = w,
        ink = if (wolfPos == BasketPosition.TOP_LEFT) LcdInkOn else LcdInkGhost
    )
    drawBasketAt(
        center = Offset(w * 0.30f, h * 0.71f),
        w = w,
        ink = if (wolfPos == BasketPosition.BOTTOM_LEFT) LcdInkOn else LcdInkGhost
    )
    drawBasketAt(
        center = Offset(w * 0.70f, h * 0.41f),
        w = w,
        ink = if (wolfPos == BasketPosition.TOP_RIGHT) LcdInkOn else LcdInkGhost
    )
    drawBasketAt(
        center = Offset(w * 0.70f, h * 0.71f),
        w = w,
        ink = if (wolfPos == BasketPosition.BOTTOM_RIGHT) LcdInkOn else LcdInkGhost
    )
}

private fun DrawScope.drawWolfFigure(
    w: Float,
    h: Float,
    centerX: Float,
    facingLeft: Boolean,
    ink: Color
) {
    val dir = if (facingLeft) -1f else 1f
    val headY = h * 0.42f
    val torsoTop = h * 0.48f
    val torsoBottom = h * 0.74f

    // Hlava vlka a cumak
    drawCircle(ink, radius = w * 0.036f, center = Offset(centerX, headY))
    drawOval(
        color = ink,
        topLeft = Offset(
            if (facingLeft) centerX - w * 0.075f else centerX + w * 0.015f,
            headY - h * 0.018f
        ),
        size = Size(w * 0.06f, h * 0.038f)
    )

    // Usi vlka
    val earPath = Path().apply {
        moveTo(centerX - dir * w * 0.01f, headY - h * 0.04f)
        lineTo(centerX - dir * w * 0.025f, headY - h * 0.11f)
        lineTo(centerX + dir * w * 0.015f, headY - h * 0.05f)
        close()
    }
    drawPath(earPath, color = ink)

    // Trup a nohy
    drawRoundRect(
        color = ink,
        topLeft = Offset(centerX - w * 0.038f, torsoTop),
        size = Size(w * 0.076f, torsoBottom - torsoTop),
        cornerRadius = CornerRadius(8f, 8f)
    )
    val legStroke = (w * 0.018f).coerceAtLeast(4f)
    drawLine(ink, Offset(centerX - w * 0.02f, torsoBottom), Offset(centerX - w * 0.03f, h * 0.87f), legStroke)
    drawLine(ink, Offset(centerX + w * 0.02f, torsoBottom), Offset(centerX + w * 0.03f, h * 0.87f), legStroke)
}

private fun DrawScope.drawBasketAt(center: Offset, w: Float, ink: Color) {
    val bw = w * 0.078f
    val bh = w * 0.048f
    val basketPath = Path().apply {
        moveTo(center.x - bw * 0.5f, center.y - bh * 0.4f)
        lineTo(center.x + bw * 0.5f, center.y - bh * 0.4f)
        lineTo(center.x + bw * 0.35f, center.y + bh * 0.5f)
        lineTo(center.x - bw * 0.35f, center.y + bh * 0.5f)
        close()
    }
    drawPath(basketPath, color = ink)
    drawArc(
        color = ink,
        startAngle = 190f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(center.x - bw * 0.38f, center.y - bh * 0.95f),
        size = Size(bw * 0.76f, bh * 0.95f),
        style = Stroke(width = (w * 0.008f).coerceAtLeast(2.5f))
    )
}

private fun DrawScope.drawPenaltyChicks(w: Float, h: Float, penaltyHalfPoints: Int, blinkPhase: Boolean) {
    for (idx in 0..2) {
        val thresholdFull = (idx + 1) * 2
        val thresholdHalf = thresholdFull - 1
        val isLit = when {
            penaltyHalfPoints >= thresholdFull -> true
            penaltyHalfPoints == thresholdHalf -> blinkPhase
            else -> false
        }
        val ink = if (isLit) LcdAccentRed else LcdInkGhost
        val cx = w * (0.38f + idx * 0.065f)
        val cy = h * 0.14f
        val r = w * 0.018f
        drawCircle(ink, radius = r, center = Offset(cx, cy))
        drawCircle(ink, radius = r * 0.72f, center = Offset(cx + r * 0.7f, cy - r * 0.55f))
    }
}

private fun DrawScope.drawCrashAndRunningChick(w: Float, h: Float, crashAnim: CrashAnimState?) {
    // Rozbite vajicko vlevo a vpravo dole + 3 kroky utikajiciho kuratka
    for (side in 0..1) {
        val isLeft = side == 0
        val activeThisSide = crashAnim != null && crashAnim.leftSide == isLeft
        val eggInk = if (activeThisSide) LcdAccentRed else LcdInkGhost
        val baseX = if (isLeft) w * 0.25f else w * 0.75f
        val floorY = h * 0.90f

        drawOval(
            color = eggInk,
            topLeft = Offset(baseX - w * 0.032f, floorY - h * 0.02f),
            size = Size(w * 0.064f, h * 0.035f)
        )

        for (step in 1..3) {
            val stepLit = activeThisSide && crashAnim?.frame == step
            val chickInk = if (stepLit) LcdInkOn else LcdInkGhost
            val dir = if (isLeft) -1f else 1f
            val runX = baseX + dir * step * (w * 0.055f)
            val runY = floorY - h * 0.025f
            drawCircle(chickInk, radius = w * 0.015f, center = Offset(runX, runY))
            drawLine(chickInk, Offset(runX, runY + w * 0.014f), Offset(runX - dir * 5f, runY + w * 0.028f), 2.5f)
            drawLine(chickInk, Offset(runX, runY + w * 0.014f), Offset(runX + dir * 5f, runY + w * 0.028f), 2.5f)
        }
    }
}

private fun DrawScope.drawSevenSegmentScore(w: Float, h: Float, score: Int) {
    val clamped = score.coerceIn(0, 999)
    val hundreds = clamped / 100
    val tens = (clamped / 10) % 10
    val units = clamped % 10

    val digitW = w * 0.048f
    val digitH = h * 0.13f
    val startX = w * 0.62f
    val topY = h * 0.07f
    val gap = w * 0.018f

    drawSingleDigit(startX, topY, digitW, digitH, if (clamped >= 100) hundreds else -1)
    drawSingleDigit(startX + digitW + gap, topY, digitW, digitH, if (clamped >= 10) tens else -1)
    drawSingleDigit(startX + (digitW + gap) * 2f, topY, digitW, digitH, units)
}

private fun DrawScope.drawSingleDigit(x: Float, y: Float, dw: Float, dh: Float, value: Int) {
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
    val sw = (dw * 0.18f).coerceAtLeast(2.8f)
    val midY = y + dh * 0.5f
    val botY = y + dh

    fun segColor(bit: Int): Color = if ((mask and (1 shl bit)) != 0) LcdInkOn else LcdInkGhost

    // 0: horni, 1: levy horni, 2: pravy horni, 3: stredni, 4: levy dolni, 5: pravy dolni, 6: dolni
    drawLine(segColor(0), Offset(x, y), Offset(x + dw, y), sw, StrokeCap.Round)
    drawLine(segColor(1), Offset(x, y), Offset(x, midY), sw, StrokeCap.Round)
    drawLine(segColor(2), Offset(x + dw, y), Offset(x + dw, midY), sw, StrokeCap.Round)
    drawLine(segColor(3), Offset(x, midY), Offset(x + dw, midY), sw, StrokeCap.Round)
    drawLine(segColor(4), Offset(x, midY), Offset(x, botY), sw, StrokeCap.Round)
    drawLine(segColor(5), Offset(x + dw, midY), Offset(x + dw, botY), sw, StrokeCap.Round)
    drawLine(segColor(6), Offset(x, botY), Offset(x + dw, botY), sw, StrokeCap.Round)
}

private fun DrawScope.drawGameOverBanner(w: Float, h: Float, blinkPhase: Boolean) {
    if (!blinkPhase) return
    drawRoundRect(
        color = LcdAccentRed,
        topLeft = Offset(w * 0.36f, h * 0.82f),
        size = Size(w * 0.28f, h * 0.08f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 4f)
    )
}
