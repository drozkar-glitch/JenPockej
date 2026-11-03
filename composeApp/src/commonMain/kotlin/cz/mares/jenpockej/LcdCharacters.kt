package cz.mares.jenpockej

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal val LcdBgColor = Color(0xFFCFD4C4)
internal val LcdShadowTint = Color(0x22000000)
internal val LcdInkOn = Color(0xFF161913)
internal val LcdInkGhost = Color(0xFFBDC2B0)

// Barvy potisku kryciho sklicka podle originalni konzole IM-02
private val PrintRed = Color(0xFFA82820)
private val PrintBrown = Color(0xFF8C6239)
private val PrintGreen = Color(0xFF465726)
private val PrintGableGrey = Color(0xFF8C9482)
private val PrintWhite = Color(0xFFF3F4EE)

internal class LcdViewport(val canvasW: Float, val canvasH: Float) {
    private val scaleX = canvasW / 1000f
    private val scaleY = canvasH / 600f
    val u = min(scaleX, scaleY)

    fun x(v: Float): Float = v * scaleX
    fun y(v: Float): Float = v * scaleY
    fun pt(vx: Float, vy: Float): Offset = Offset(vx * scaleX, vy * scaleY)
    fun s(v: Float): Float = v * u
}

internal fun DrawScope.drawStaticSceneryAndHens(vp: LcdViewport) {
    // 1. Stit a cervena strecha chaloupky v levem hornim rohu
    val gableFill = Path().apply {
        moveTo(vp.x(4f), vp.y(118f))
        lineTo(vp.x(56f), vp.y(22f))
        lineTo(vp.x(138f), vp.y(128f))
        close()
    }
    drawPath(gableFill, color = PrintGableGrey)

    drawRect(
        color = PrintRed,
        topLeft = vp.pt(108f, 54f),
        size = Size(vp.x(22f), vp.y(42f))
    )

    val roofStroke = Path().apply {
        moveTo(vp.x(4f), vp.y(114f))
        lineTo(vp.x(56f), vp.y(20f))
        lineTo(vp.x(142f), vp.y(132f))
    }
    drawPath(
        path = roofStroke,
        color = PrintRed,
        style = Stroke(width = vp.s(13f), cap = StrokeCap.Square, join = StrokeJoin.Miter)
    )

    // Hnede okno s okenicemi u zajicka
    val windowFrame = Path().apply {
        moveTo(vp.x(148f), vp.y(58f))
        lineTo(vp.x(182f), vp.y(46f))
        lineTo(vp.x(182f), vp.y(140f))
        lineTo(vp.x(148f), vp.y(130f))
        close()
    }
    drawPath(
        path = windowFrame,
        color = PrintBrown,
        style = Stroke(width = vp.s(5.5f), join = StrokeJoin.Round)
    )
    drawLine(PrintBrown, vp.pt(148f, 94f), vp.pt(182f, 90f), vp.s(5f))
    drawLine(PrintBrown, vp.pt(122f, 144f), vp.pt(195f, 144f), vp.s(5f))

    // 2. Zeleny ker v pravem hornim rohu s bilym zvlnenym prokreslenim
    drawBushCluster(
        vp = vp,
        circles = listOf(
            Triple(918f, 34f, 25f),
            Triple(948f, 42f, 28f),
            Triple(980f, 34f, 26f)
        ),
        highlightY = 50f,
        highlightStartX = 905f,
        highlightEndX = 990f
    )

    // 3. Kere pod spodnimi bidylky vlevo a vpravo
    drawBushCluster(
        vp = vp,
        circles = listOf(
            Triple(26f, 452f, 26f),
            Triple(58f, 458f, 30f),
            Triple(95f, 464f, 24f),
            Triple(206f, 472f, 23f),
            Triple(234f, 476f, 19f)
        ),
        highlightY = 472f,
        highlightStartX = 16f,
        highlightEndX = 108f
    )
    drawBushCluster(
        vp = vp,
        circles = listOf(
            Triple(974f, 452f, 26f),
            Triple(942f, 458f, 30f),
            Triple(905f, 464f, 24f),
            Triple(794f, 472f, 23f),
            Triple(766f, 476f, 19f)
        ),
        highlightY = 472f,
        highlightStartX = 892f,
        highlightEndX = 984f
    )

    // 4. Hneda drevena bidylka, sloupky a plot pod spodnimi rampami
    drawBrownPerch(vp, x0 = 4f, y0 = 212f, x1 = 74f, y1 = 212f, x2 = 248f, y2 = 292f, postX = 160f, postH = 54f)
    drawFenceUnderRamp(vp, startX = 14f, endX = 148f, topY = 362f, bottomY = 442f)
    drawBrownPerch(vp, x0 = 4f, y0 = 354f, x1 = 74f, y1 = 354f, x2 = 248f, y2 = 434f, postX = 160f, postH = 60f)

    drawBrownPerch(vp, x0 = 996f, y0 = 212f, x1 = 926f, y1 = 212f, x2 = 752f, y2 = 292f, postX = 840f, postH = 54f)
    drawFenceUnderRamp(vp, startX = 852f, endX = 986f, topY = 362f, bottomY = 442f)
    drawBrownPerch(vp, x0 = 996f, y0 = 354f, x1 = 926f, y1 = 354f, x2 = 752f, y2 = 434f, postX = 840f, postH = 60f)

    // 5. Ctyri bile slepice s cervenym hrebinkem na bidylkach
    drawWhiteHenOnPerch(vp, cx = 40f, cy = 166f, facingRight = true)
    drawWhiteHenOnPerch(vp, cx = 40f, cy = 308f, facingRight = true)
    drawWhiteHenOnPerch(vp, cx = 960f, cy = 166f, facingRight = false)
    drawWhiteHenOnPerch(vp, cx = 960f, cy = 308f, facingRight = false)

    // 6. Vlnite trsy travy dole
    drawOrganicGrassRow(vp, startX = 16f, endX = 188f, baseY = 530f)
    drawOrganicGrassRow(vp, startX = 310f, endX = 398f, baseY = 514f)
    drawOrganicGrassRow(vp, startX = 355f, endX = 468f, baseY = 544f)
    drawOrganicGrassRow(vp, startX = 518f, endX = 638f, baseY = 544f)
    drawOrganicGrassRow(vp, startX = 565f, endX = 668f, baseY = 514f)
    drawOrganicGrassRow(vp, startX = 812f, endX = 984f, baseY = 530f)
}

private fun DrawScope.drawBushCluster(
    vp: LcdViewport,
    circles: List<Triple<Float, Float, Float>>,
    highlightY: Float,
    highlightStartX: Float,
    highlightEndX: Float
) {
    for ((cx, cy, r) in circles) {
        drawCircle(
            color = PrintGreen,
            radius = vp.s(r),
            center = vp.pt(cx, cy)
        )
    }
    val wave = Path().apply {
        moveTo(vp.x(highlightStartX), vp.y(highlightY))
        var x = highlightStartX
        while (x < highlightEndX - 14f) {
            quadraticTo(
                vp.x(x + 8f), vp.y(highlightY - 6f),
                vp.x(x + 16f), vp.y(highlightY)
            )
            x += 16f
        }
    }
    drawPath(
        path = wave,
        color = PrintWhite,
        style = Stroke(width = vp.s(2.6f), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawBrownPerch(
    vp: LcdViewport,
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    postX: Float,
    postH: Float
) {
    val t = (postX - x1) / (x2 - x1)
    val postTopY = y1 + (y2 - y1) * t

    drawLine(
        color = PrintBrown,
        start = vp.pt(postX, postTopY),
        end = vp.pt(postX, postTopY + postH),
        strokeWidth = vp.s(11f),
        cap = StrokeCap.Square
    )

    val perchPath = Path().apply {
        moveTo(vp.x(x0), vp.y(y0))
        lineTo(vp.x(x1), vp.y(y1))
        lineTo(vp.x(x2), vp.y(y2))
    }
    drawPath(
        path = perchPath,
        color = PrintBrown,
        style = Stroke(width = vp.s(11f), cap = StrokeCap.Butt, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawFenceUnderRamp(
    vp: LcdViewport,
    startX: Float,
    endX: Float,
    topY: Float,
    bottomY: Float
) {
    val steps = 4
    val stepW = (endX - startX) / steps
    for (i in 0..steps) {
        val px = startX + i * stepW
        drawLine(
            color = PrintBrown,
            start = vp.pt(px, topY + i * 10f),
            end = vp.pt(px, bottomY),
            strokeWidth = vp.s(6.5f)
        )
    }
}

private fun DrawScope.drawWhiteHenOnPerch(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    facingRight: Boolean
) {
    val dir = if (facingRight) 1f else -1f

    val henBody = Path().apply {
        moveTo(vp.x(cx - dir * 30f), vp.y(cy + 36f))
        cubicTo(
            vp.x(cx - dir * 36f), vp.y(cy + 8f),
            vp.x(cx - dir * 28f), vp.y(cy - 18f),
            vp.x(cx - dir * 6f), vp.y(cy - 8f)
        )
        cubicTo(
            vp.x(cx + dir * 2f), vp.y(cy - 28f),
            vp.x(cx + dir * 22f), vp.y(cy - 28f),
            vp.x(cx + dir * 24f), vp.y(cy - 6f)
        )
        cubicTo(
            vp.x(cx + dir * 32f), vp.y(cy + 14f),
            vp.x(cx + dir * 22f), vp.y(cy + 36f),
            vp.x(cx + dir * 4f), vp.y(cy + 36f)
        )
        close()
    }
    drawPath(henBody, color = PrintWhite)
    drawPath(
        path = henBody,
        color = PrintBrown,
        style = Stroke(width = vp.s(2.8f), join = StrokeJoin.Round)
    )

    val comb = Path().apply {
        moveTo(vp.x(cx + dir * 4f), vp.y(cy - 22f))
        lineTo(vp.x(cx + dir * 8f), vp.y(cy - 36f))
        lineTo(vp.x(cx + dir * 15f), vp.y(cy - 26f))
        lineTo(vp.x(cx + dir * 22f), vp.y(cy - 34f))
        lineTo(vp.x(cx + dir * 23f), vp.y(cy - 20f))
        close()
    }
    drawPath(comb, color = PrintRed)

    val beak = Path().apply {
        moveTo(vp.x(cx + dir * 23f), vp.y(cy - 12f))
        lineTo(vp.x(cx + dir * 35f), vp.y(cy - 7f))
        lineTo(vp.x(cx + dir * 23f), vp.y(cy - 2f))
        close()
    }
    drawPath(beak, color = PrintRed)

    drawCircle(color = LcdInkOn, radius = vp.s(2.4f), center = vp.pt(cx + dir * 15f, cy - 11f))
    drawArc(
        color = PrintBrown,
        startAngle = if (facingRight) 10f else 150f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = vp.pt(cx - 16f, cy + 2f),
        size = Size(vp.x(32f), vp.y(22f)),
        style = Stroke(width = vp.s(3f), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawOrganicGrassRow(
    vp: LcdViewport,
    startX: Float,
    endX: Float,
    baseY: Float
) {
    val turf = Path().apply {
        moveTo(vp.x(startX), vp.y(baseY))
        var curX = startX
        var idx = 0
        while (curX < endX - 8f) {
            val h = if (idx % 2 == 0) 11f else 7f
            val lean = if (idx % 3 == 0) -3f else 3f
            quadraticTo(
                vp.x(curX + 5f + lean), vp.y(baseY - h),
                vp.x(curX + 10f), vp.y(baseY - 2f)
            )
            curX += 10f
            idx++
        }
        lineTo(vp.x(endX), vp.y(baseY + 4f))
        lineTo(vp.x(startX), vp.y(baseY + 4f))
        close()
    }
    drawPath(turf, color = PrintGreen)
}

internal fun DrawScope.drawHareInWindow(
    vp: LcdViewport,
    visible: Boolean,
    bellUp: Boolean
) {
    val ink = if (visible) LcdInkOn else LcdInkGhost
    val hx = 226f
    val hy = 96f
    val outline = Stroke(width = vp.s(3.5f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Trup zajicka v tmavem triku vyklanejici se z okna
    val body = Path().apply {
        moveTo(vp.x(hx - 42f), vp.y(hy + 10f))
        lineTo(vp.x(hx - 6f), vp.y(hy + 6f))
        lineTo(vp.x(hx + 8f), vp.y(hy + 32f))
        lineTo(vp.x(hx - 38f), vp.y(hy + 36f))
        close()
    }
    drawPath(body, color = ink)

    // 2. Dlouhe usi se svetlym vnitrekem (komiksova kresba)
    val leftEar = Path().apply {
        moveTo(vp.x(hx - 8f), vp.y(hy - 18f))
        cubicTo(vp.x(hx - 30f), vp.y(hy - 56f), vp.x(hx - 14f), vp.y(hy - 66f), vp.x(hx - 2f), vp.y(hy - 38f))
        close()
    }
    val rightEar = Path().apply {
        moveTo(vp.x(hx - 2f), vp.y(hy - 18f))
        cubicTo(vp.x(hx - 6f), vp.y(hy - 62f), vp.x(hx + 14f), vp.y(hy - 62f), vp.x(hx + 8f), vp.y(hy - 18f))
        close()
    }
    drawPath(leftEar, color = LcdBgColor)
    drawPath(leftEar, color = ink, style = outline)
    drawPath(rightEar, color = LcdBgColor)
    drawPath(rightEar, color = ink, style = outline)

    // 3. Svetla hlava zajicka s baculatou tvickama a cernym obrysem
    val headPath = Path().apply {
        moveTo(vp.x(hx - 18f), vp.y(hy - 6f))
        cubicTo(vp.x(hx - 22f), vp.y(hy - 24f), vp.x(hx + 14f), vp.y(hy - 26f), vp.x(hx + 20f), vp.y(hy - 8f))
        cubicTo(vp.x(hx + 28f), vp.y(hy - 2f), vp.x(hx + 26f), vp.y(hy + 14f), vp.x(hx + 6f), vp.y(hy + 14f))
        cubicTo(vp.x(hx - 12f), vp.y(hy + 14f), vp.x(hx - 20f), vp.y(hy + 6f), vp.x(hx - 18f), vp.y(hy - 6f))
        close()
    }
    drawPath(headPath, color = LcdBgColor)
    drawPath(headPath, color = ink, style = outline)

    // Oci, cumacek, usmev a fousky zajicka
    drawOval(ink, vp.pt(hx - 2f, hy - 15f), Size(vp.x(5.5f), vp.y(9.5f)))
    drawOval(ink, vp.pt(hx + 8f, hy - 15f), Size(vp.x(5.5f), vp.y(9.5f)))
    drawCircle(ink, radius = vp.s(3.2f), center = vp.pt(hx + 21f, hy - 3f))
    drawLine(ink, vp.pt(hx + 18f, hy - 2f), vp.pt(hx + 34f, hy - 8f), vp.s(2.0f), StrokeCap.Round)
    drawLine(ink, vp.pt(hx + 18f, hy + 1f), vp.pt(hx + 35f, hy + 1f), vp.s(2.0f), StrokeCap.Round)
    drawArc(
        color = ink,
        startAngle = 10f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = vp.pt(hx + 4f, hy + 1f),
        size = Size(vp.x(12f), vp.y(7f)),
        style = Stroke(width = vp.s(2.4f), cap = StrokeCap.Round)
    )

    // Spodni packa oprena o parapet
    drawOval(
        color = LcdBgColor,
        topLeft = vp.pt(hx - 8f, hy + 29f),
        size = Size(vp.x(26f), vp.y(11f))
    )
    drawOval(
        color = ink,
        topLeft = vp.pt(hx - 8f, hy + 29f),
        size = Size(vp.x(26f), vp.y(11f)),
        style = Stroke(width = vp.s(3.2f))
    )

    // Horni a dolni poloha ruky se zvoneckem
    val upInk = if (visible && bellUp) LcdInkOn else LcdInkGhost
    val downInk = if (visible && !bellUp) LcdInkOn else LcdInkGhost

    drawHareBellHand(vp, startX = hx + 4f, startY = hy + 14f, bellX = hx + 58f, bellY = hy - 16f, ink = upInk)
    drawHareBellHand(vp, startX = hx + 4f, startY = hy + 22f, bellX = hx + 46f, bellY = hy + 36f, ink = downInk)
}

private fun DrawScope.drawHareBellHand(
    vp: LcdViewport,
    startX: Float,
    startY: Float,
    bellX: Float,
    bellY: Float,
    ink: Color
) {
    drawLine(
        color = ink,
        start = vp.pt(startX, startY),
        end = vp.pt(bellX - 10f, bellY),
        strokeWidth = vp.s(4.8f),
        cap = StrokeCap.Round
    )
    val bell = Path().apply {
        moveTo(vp.x(bellX - 6f), vp.y(bellY - 6f))
        lineTo(vp.x(bellX - 16f), vp.y(bellY + 14f))
        lineTo(vp.x(bellX + 6f), vp.y(bellY + 14f))
        close()
    }
    drawPath(bell, color = ink)
    drawCircle(color = LcdBgColor, radius = vp.s(2.4f), center = vp.pt(bellX - 5f, bellY + 6f))
    drawCircle(color = ink, radius = vp.s(3.5f), center = vp.pt(bellX - 5f, bellY + 17f))
}

internal fun DrawScope.drawScoreAndPenaltyChicks(
    vp: LcdViewport,
    score: Int,
    penaltyHalfPoints: Int,
    blinkPhase: Boolean
) {
    val clamped = score.coerceIn(0, 9999)
    val d0 = if (clamped >= 1000) (clamped / 1000) % 10 else 0
    val d1 = (clamped / 100) % 10
    val d2 = (clamped / 10) % 10
    val d3 = clamped % 10

    val dw = 33f
    val dh = 66f
    val gap = 16f
    val startX = 552f
    val topY = 34f

    drawClassicSevenSeg(vp, startX, topY, dw, dh, d0)
    drawClassicSevenSeg(vp, startX + (dw + gap), topY, dw, dh, d1)
    drawClassicSevenSeg(vp, startX + (dw + gap) * 2f, topY, dw, dh, d2)
    drawClassicSevenSeg(vp, startX + (dw + gap) * 3f, topY, dw, dh, d3)

    // 3 kuratka pod cislicemi skore
    for (idx in 0..2) {
        val thresholdFull = (idx + 1) * 2
        val thresholdHalf = thresholdFull - 1
        val isLit = when {
            penaltyHalfPoints >= thresholdFull -> true
            penaltyHalfPoints == thresholdHalf -> blinkPhase
            else -> false
        }
        val ink = if (isLit) LcdInkOn else LcdInkGhost
        val cx = 555f + idx * 58f
        val cy = 136f
        drawProfilePenaltyChick(vp, cx, cy, ink)
    }
}

private fun DrawScope.drawProfilePenaltyChick(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    ink: Color
) {
    drawCircle(
        color = ink,
        radius = vp.s(8.5f),
        center = vp.pt(cx - 8f, cy - 8f)
    )
    val beak = Path().apply {
        moveTo(vp.x(cx - 15f), vp.y(cy - 11f))
        lineTo(vp.x(cx - 24f), vp.y(cy - 8f))
        lineTo(vp.x(cx - 15f), vp.y(cy - 5f))
        close()
    }
    drawPath(beak, color = ink)

    drawCircle(
        color = LcdBgColor,
        radius = vp.s(1.8f),
        center = vp.pt(cx - 10f, cy - 9f)
    )

    val body = Path().apply {
        moveTo(vp.x(cx - 12f), vp.y(cy - 2f))
        cubicTo(
            vp.x(cx - 14f), vp.y(cy + 12f),
            vp.x(cx + 12f), vp.y(cy + 14f),
            vp.x(cx + 18f), vp.y(cy - 4f)
        )
        lineTo(vp.x(cx + 22f), vp.y(cy - 10f))
        lineTo(vp.x(cx + 12f), vp.y(cy - 6f))
        close()
    }
    drawPath(body, color = ink)

    drawArc(
        color = LcdBgColor,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = vp.pt(cx - 4f, cy - 3f),
        size = Size(vp.x(14f), vp.y(8f)),
        style = Stroke(width = vp.s(2f), cap = StrokeCap.Round)
    )

    drawLine(ink, vp.pt(cx - 2f, cy + 9f), vp.pt(cx - 5f, cy + 18f), vp.s(2.8f), StrokeCap.Round)
    drawLine(ink, vp.pt(cx + 6f, cy + 9f), vp.pt(cx + 9f, cy + 18f), vp.s(2.8f), StrokeCap.Round)
}

private fun DrawScope.drawClassicSevenSeg(
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
    val sw = vp.s(6.5f)
    val midY = y + dh * 0.5f
    val botY = y + dh
    val p = 3.2f
    val slantTop = 3.5f
    val slantMid = 1.8f

    fun col(bit: Int): Color = if ((mask and (1 shl bit)) != 0) LcdInkOn else LcdInkGhost

    drawLine(col(0), vp.pt(x + slantTop + p, y), vp.pt(x + slantTop + dw - p, y), sw, StrokeCap.Round)
    drawLine(col(1), vp.pt(x + slantTop, y + p), vp.pt(x + slantMid, midY - p), sw, StrokeCap.Round)
    drawLine(col(2), vp.pt(x + slantTop + dw, y + p), vp.pt(x + slantMid + dw, midY - p), sw, StrokeCap.Round)
    drawLine(col(3), vp.pt(x + slantMid + p, midY), vp.pt(x + slantMid + dw - p, midY), sw, StrokeCap.Round)
    drawLine(col(4), vp.pt(x + slantMid, midY + p), vp.pt(x, botY - p), sw, StrokeCap.Round)
    drawLine(col(5), vp.pt(x + slantMid + dw, midY + p), vp.pt(x + dw, botY - p), sw, StrokeCap.Round)
    drawLine(col(6), vp.pt(x + p, botY), vp.pt(x + dw - p, botY), sw, StrokeCap.Round)
}

internal fun DrawScope.drawCartoonWolfPosesAndBaskets(
    vp: LcdViewport,
    wolfPos: BasketPosition,
    celebrating: Boolean,
    blinkPhase: Boolean
) {
    val facingLeft = wolfPos == BasketPosition.TOP_LEFT || wolfPos == BasketPosition.BOTTOM_LEFT
    val leftInk = if (facingLeft || (celebrating && blinkPhase)) LcdInkOn else LcdInkGhost
    val rightInk = if (!facingLeft || (celebrating && !blinkPhase)) LcdInkOn else LcdInkGhost

    // Dve komiksove perokresby vlka uprostred presne podle originalniho displeje IM-02
    drawAuthenticLineArtWolf(vp, baseX = 446f, facingLeft = true, ink = leftInk)
    drawAuthenticLineArtWolf(vp, baseX = 554f, facingLeft = false, ink = rightInk)

    // 4 polohy rukou s rukavem, tlapkou a plnym kosikem
    drawAuthenticArmAndBasket(
        vp = vp,
        shoulderX = 420f,
        shoulderY = 342f,
        elbowX = 382f,
        elbowY = 346f,
        basketX = 312f,
        basketY = 320f,
        facingLeft = true,
        ink = if (wolfPos == BasketPosition.TOP_LEFT) LcdInkOn else LcdInkGhost
    )
    drawAuthenticArmAndBasket(
        vp = vp,
        shoulderX = 414f,
        shoulderY = 368f,
        elbowX = 378f,
        elbowY = 418f,
        basketX = 312f,
        basketY = 458f,
        facingLeft = true,
        ink = if (wolfPos == BasketPosition.BOTTOM_LEFT) LcdInkOn else LcdInkGhost
    )
    drawAuthenticArmAndBasket(
        vp = vp,
        shoulderX = 580f,
        shoulderY = 342f,
        elbowX = 618f,
        elbowY = 346f,
        basketX = 688f,
        basketY = 320f,
        facingLeft = false,
        ink = if (wolfPos == BasketPosition.TOP_RIGHT) LcdInkOn else LcdInkGhost
    )
    drawAuthenticArmAndBasket(
        vp = vp,
        shoulderX = 586f,
        shoulderY = 368f,
        elbowX = 622f,
        elbowY = 418f,
        basketX = 688f,
        basketY = 458f,
        facingLeft = false,
        ink = if (wolfPos == BasketPosition.BOTTOM_RIGHT) LcdInkOn else LcdInkGhost
    )
}

private fun DrawScope.drawAuthenticLineArtWolf(
    vp: LcdViewport,
    baseX: Float,
    facingLeft: Boolean,
    ink: Color
) {
    val dir = if (facingLeft) -1f else 1f
    val outline = Stroke(width = vp.s(3.6f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Cerna rozcuchana hriva vzadu na hlave a krku + 2 spicate vlci usi nahore
    val maneAndEars = Path().apply {
        moveTo(vp.x(baseX - dir * 14f), vp.y(262f))
        lineTo(vp.x(baseX - dir * 24f), vp.y(236f))
        lineTo(vp.x(baseX - dir * 6f), vp.y(250f))
        lineTo(vp.x(baseX - dir * 2f), vp.y(240f))
        lineTo(vp.x(baseX + dir * 4f), vp.y(250f))
        lineTo(vp.x(baseX + dir * 16f), vp.y(234f))
        lineTo(vp.x(baseX + dir * 16f), vp.y(260f))
        lineTo(vp.x(baseX + dir * 6f), vp.y(316f))
        lineTo(vp.x(baseX - dir * 16f), vp.y(318f))
        lineTo(vp.x(baseX - dir * 36f), vp.y(310f))
        lineTo(vp.x(baseX - dir * 22f), vp.y(296f))
        lineTo(vp.x(baseX - dir * 38f), vp.y(286f))
        lineTo(vp.x(baseX - dir * 22f), vp.y(274f))
        lineTo(vp.x(baseX - dir * 34f), vp.y(264f))
        close()
    }
    drawPath(maneAndEars, color = ink)

    val innerEar = Path().apply {
        moveTo(vp.x(baseX + dir * 6f), vp.y(254f))
        lineTo(vp.x(baseX + dir * 13f), vp.y(241f))
        lineTo(vp.x(baseX + dir * 13f), vp.y(256f))
        close()
    }
    drawPath(innerEar, color = LcdBgColor)

    // 2. Svetly oblicej a dlouhy cumak s cernym obrysem
    val faceAndSnout = Path().apply {
        moveTo(vp.x(baseX - dir * 6f), vp.y(260f))
        lineTo(vp.x(baseX + dir * 18f), vp.y(258f))
        lineTo(vp.x(baseX + dir * 22f), vp.y(272f))
        cubicTo(
            vp.x(baseX + dir * 36f), vp.y(272f),
            vp.x(baseX + dir * 48f), vp.y(266f),
            vp.x(baseX + dir * 56f), vp.y(272f)
        )
        cubicTo(
            vp.x(baseX + dir * 58f), vp.y(286f),
            vp.x(baseX + dir * 42f), vp.y(294f),
            vp.x(baseX + dir * 18f), vp.y(292f)
        )
        lineTo(vp.x(baseX + dir * 40f), vp.y(304f))
        cubicTo(
            vp.x(baseX + dir * 34f), vp.y(316f),
            vp.x(baseX + dir * 16f), vp.y(316f),
            vp.x(baseX - dir * 4f), vp.y(306f)
        )
        cubicTo(
            vp.x(baseX - dir * 14f), vp.y(298f),
            vp.x(baseX - dir * 14f), vp.y(274f),
            vp.x(baseX - dir * 6f), vp.y(260f)
        )
        close()
    }
    drawPath(faceAndSnout, color = LcdBgColor)
    drawPath(faceAndSnout, color = ink, style = outline)

    drawOval(
        color = ink,
        topLeft = vp.pt(baseX + dir * 2f - 3.5f, 265f),
        size = Size(vp.x(7f), vp.y(12f))
    )
    drawOval(
        color = ink,
        topLeft = vp.pt(baseX + dir * 11f - 3.5f, 265f),
        size = Size(vp.x(7f), vp.y(12f))
    )
    drawLine(
        color = ink,
        start = vp.pt(baseX - dir * 2f, 261f),
        end = vp.pt(baseX + dir * 16f, 262f),
        strokeWidth = vp.s(2.8f),
        cap = StrokeCap.Round
    )

    drawOval(
        color = ink,
        topLeft = vp.pt(baseX + dir * 52f - 6.5f, 260f),
        size = Size(vp.x(13f), vp.y(16f))
    )
    drawCircle(color = ink, radius = vp.s(1.8f), center = vp.pt(baseX + dir * 32f, 282f))
    drawCircle(color = ink, radius = vp.s(1.8f), center = vp.pt(baseX + dir * 39f, 280f))
    drawLine(
        color = ink,
        start = vp.pt(baseX + dir * 20f, 297f),
        end = vp.pt(baseX + dir * 34f, 299f),
        strokeWidth = vp.s(3.0f),
        cap = StrokeCap.Round
    )

    // 3. Prohnuty tmavy trup vlka
    val torso = Path().apply {
        moveTo(vp.x(baseX - dir * 14f), vp.y(314f))
        lineTo(vp.x(baseX + dir * 16f), vp.y(314f))
        cubicTo(
            vp.x(baseX + dir * 28f), vp.y(348f),
            vp.x(baseX + dir * 24f), vp.y(382f),
            vp.x(baseX + dir * 14f), vp.y(408f)
        )
        lineTo(vp.x(baseX - dir * 20f), vp.y(402f))
        cubicTo(
            vp.x(baseX - dir * 10f), vp.y(374f),
            vp.x(baseX - dir * 8f), vp.y(344f),
            vp.x(baseX - dir * 14f), vp.y(314f)
        )
        close()
    }
    drawPath(torso, color = ink)

    val collar = Path().apply {
        moveTo(vp.x(baseX - dir * 8f), vp.y(315f))
        lineTo(vp.x(baseX + dir * 14f), vp.y(315f))
        lineTo(vp.x(baseX + dir * 5f), vp.y(336f))
        close()
    }
    drawPath(collar, color = LcdBgColor)
    drawLine(LcdBgColor, vp.pt(baseX - 19f, 400f), vp.pt(baseX + 16f, 405f), vp.s(4.0f), StrokeCap.Round)

    // 4. Dvojite vykreslene vlci nohy a tlapky
    val frontLeg = Path().apply {
        moveTo(vp.x(baseX + dir * 24f), vp.y(442f))
        lineTo(vp.x(baseX + dir * 12f), vp.y(488f))
        lineTo(vp.x(baseX + dir * 44f), vp.y(492f))
        cubicTo(
            vp.x(baseX + dir * 48f), vp.y(482f),
            vp.x(baseX + dir * 34f), vp.y(480f),
            vp.x(baseX + dir * 24f), vp.y(480f)
        )
        lineTo(vp.x(baseX + dir * 36f), vp.y(448f))
        close()
    }
    val backLeg = Path().apply {
        moveTo(vp.x(baseX - dir * 36f), vp.y(446f))
        lineTo(vp.x(baseX - dir * 16f), vp.y(492f))
        lineTo(vp.x(baseX - dir * 46f), vp.y(496f))
        cubicTo(
            vp.x(baseX - dir * 50f), vp.y(486f),
            vp.x(baseX - dir * 36f), vp.y(484f),
            vp.x(baseX - dir * 28f), vp.y(484f)
        )
        lineTo(vp.x(baseX - dir * 24f), vp.y(442f))
        close()
    }
    drawPath(frontLeg, color = LcdBgColor)
    drawPath(frontLeg, color = ink, style = outline)
    drawPath(backLeg, color = LcdBgColor)
    drawPath(backLeg, color = ink, style = outline)

    // 5. Siroce rozkrocene svetle kratasy s cernym obrysem a puntiky
    val shorts = Path().apply {
        moveTo(vp.x(baseX - dir * 20f), vp.y(402f))
        lineTo(vp.x(baseX + dir * 16f), vp.y(406f))
        lineTo(vp.x(baseX + dir * 44f), vp.y(442f))
        lineTo(vp.x(baseX + dir * 22f), vp.y(456f))
        lineTo(vp.x(baseX - dir * 2f), vp.y(432f))
        lineTo(vp.x(baseX - dir * 22f), vp.y(458f))
        lineTo(vp.x(baseX - dir * 44f), vp.y(442f))
        close()
    }
    drawPath(shorts, color = LcdBgColor)
    drawPath(shorts, color = ink, style = outline)

    drawCircle(ink, radius = vp.s(2.8f), center = vp.pt(baseX + dir * 18f, 436f))
    drawCircle(ink, radius = vp.s(2.8f), center = vp.pt(baseX - dir * 18f, 438f))
    drawCircle(ink, radius = vp.s(2.8f), center = vp.pt(baseX - dir * 2f, 418f))
    drawLine(ink, vp.pt(baseX - dir * 2f, 408f), vp.pt(baseX - dir * 2f, 432f), vp.s(2.4f), StrokeCap.Round)
}

private fun DrawScope.drawAuthenticArmAndBasket(
    vp: LcdViewport,
    shoulderX: Float,
    shoulderY: Float,
    elbowX: Float,
    elbowY: Float,
    basketX: Float,
    basketY: Float,
    facingLeft: Boolean,
    ink: Color
) {
    val dir = if (facingLeft) -1f else 1f
    val handX = basketX - dir * 28f
    val handY = basketY - 6f
    val outline = Stroke(width = vp.s(3.4f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    val armPolygon = Path().apply {
        moveTo(vp.x(shoulderX), vp.y(shoulderY - 6f))
        lineTo(vp.x(elbowX), vp.y(elbowY - 6f))
        lineTo(vp.x(handX), vp.y(handY - 5f))
        lineTo(vp.x(handX + dir * 10f), vp.y(handY + 2f))
        lineTo(vp.x(handX), vp.y(handY + 7f))
        lineTo(vp.x(elbowX), vp.y(elbowY + 6f))
        lineTo(vp.x(shoulderX), vp.y(shoulderY + 6f))
        close()
    }
    drawPath(armPolygon, color = LcdBgColor)
    drawPath(armPolygon, color = ink, style = outline)

    drawLine(
        color = ink,
        start = vp.pt(shoulderX + dir * 8f, shoulderY - 8f),
        end = vp.pt(shoulderX + dir * 6f, shoulderY + 8f),
        strokeWidth = vp.s(4.2f),
        cap = StrokeCap.Round
    )

    val tiltDeg = if (facingLeft) -10f else 10f
    val pivot = vp.pt(basketX, basketY)
    val bw = 64f
    val bh = 40f

    rotate(degrees = tiltDeg, pivot = pivot) {
        val basketBowl = Path().apply {
            moveTo(vp.x(basketX - bw * 0.50f), vp.y(basketY - bh * 0.22f))
            lineTo(vp.x(basketX + bw * 0.50f), vp.y(basketY - bh * 0.22f))
            cubicTo(
                vp.x(basketX + bw * 0.48f), vp.y(basketY + bh * 0.56f),
                vp.x(basketX + bw * 0.24f), vp.y(basketY + bh * 0.74f),
                vp.x(basketX), vp.y(basketY + bh * 0.74f)
            )
            cubicTo(
                vp.x(basketX - bw * 0.24f), vp.y(basketY + bh * 0.74f),
                vp.x(basketX - bw * 0.48f), vp.y(basketY + bh * 0.56f),
                vp.x(basketX - bw * 0.50f), vp.y(basketY - bh * 0.22f)
            )
            close()
        }
        drawPath(basketBowl, color = ink)

        drawArc(
            color = LcdBgColor,
            startAngle = if (facingLeft) 25f else 115f,
            sweepAngle = 45f,
            useCenter = false,
            topLeft = vp.pt(basketX - bw * 0.36f, basketY - bh * 0.05f),
            size = Size(vp.x(bw * 0.72f), vp.y(bh * 0.62f)),
            style = Stroke(width = vp.s(2.8f), cap = StrokeCap.Round)
        )

        drawOval(
            color = LcdBgColor,
            topLeft = vp.pt(basketX - bw * 0.46f, basketY - bh * 0.30f),
            size = Size(vp.x(bw * 0.92f), vp.y(bh * 0.28f))
        )
        drawOval(
            color = ink,
            topLeft = vp.pt(basketX - bw * 0.46f, basketY - bh * 0.30f),
            size = Size(vp.x(bw * 0.92f), vp.y(bh * 0.28f)),
            style = Stroke(width = vp.s(3.4f))
        )

        drawArc(
            color = ink,
            startAngle = if (facingLeft) -50f else 130f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = vp.pt(basketX - dir * 32f - 10f, basketY - 18f),
            size = Size(vp.x(20f), vp.y(22f)),
            style = Stroke(width = vp.s(3.4f), cap = StrokeCap.Round)
        )
    }
}

internal fun DrawScope.drawAuthenticHollowEggs(
    vp: LcdViewport,
    activeEggs: List<EggItem>
) {
    // Skutecne kutaleni vajicka po 5 krocich:
    // kazdy krok ma vyrazne jiny uhel natoceni (prekulovani pres spicku o 360°)
    val leftRampAngles = floatArrayOf(22f, 88f, 152f, 215f, 285f)
    val rightRampAngles = floatArrayOf(-22f, -88f, -152f, -215f, -285f)

    val halfLen = vp.s(16.5f)
    val halfWid = vp.s(11.2f)
    val ringStroke = vp.s(4.8f)

    for (ramp in 0..3) {
        val isLeftRamp = ramp == 0 || ramp == 1
        for (step in 0..4) {
            val isLit = activeEggs.any { it.ramp == ramp && it.step == step }
            val ink = if (isLit) LcdInkOn else LcdInkGhost
            val pos = authenticEggCoordinate(vp, ramp, step)
            val angleDeg = if (isLeftRamp) leftRampAngles[step] else rightRampAngles[step]

            rotate(degrees = angleDeg, pivot = pos) {
                // Skutecny tvar vajicka (sirsi kulaty spodek a zuzena spicka),
                // aby bylo otaceni o 90° / 180° / 285° na prvni pohled zretelne
                val tipDir = if (isLeftRamp) 1f else -1f
                val eggPath = Path().apply {
                    // Zuzena spicka vajicka
                    moveTo(pos.x + tipDir * halfLen, pos.y)
                    cubicTo(
                        pos.x + tipDir * (halfLen * 0.45f), pos.y - halfWid * 0.92f,
                        pos.x - tipDir * (halfLen * 0.95f), pos.y - halfWid * 1.15f,
                        pos.x - tipDir * halfLen, pos.y
                    )
                    cubicTo(
                        pos.x - tipDir * (halfLen * 0.95f), pos.y + halfWid * 1.15f,
                        pos.x + tipDir * (halfLen * 0.45f), pos.y + halfWid * 0.92f,
                        pos.x + tipDir * halfLen, pos.y
                    )
                    close()
                }
                drawPath(path = eggPath, color = LcdBgColor)
                drawPath(
                    path = eggPath,
                    color = ink,
                    style = Stroke(width = ringStroke, join = StrokeJoin.Round)
                )
            }
        }
    }
}

private fun authenticEggCoordinate(vp: LcdViewport, ramp: Int, step: Int): Offset {
    // 1. az 4. pozice (step 0..3) se kutali po drevenem bidylku,
    // 5. pozice (step 4) prepadava z hrany bidylka smerem dolu do kosiku
    val xOffsets = floatArrayOf(0f, 44f, 88f, 132f, 176f)
    val yOffsets = floatArrayOf(0f, 21f, 42f, 63f, 94f)
    val dx = xOffsets[step.coerceIn(0, 4)]
    val dy = yOffsets[step.coerceIn(0, 4)]

    return when (ramp) {
        0 -> vp.pt(94f + dx, 196f + dy)
        1 -> vp.pt(94f + dx, 338f + dy)
        2 -> vp.pt(906f - dx, 196f + dy)
        else -> vp.pt(906f - dx, 338f + dy)
    }
}

internal fun DrawScope.drawBrokenEggAndRunningChicks(
    vp: LcdViewport,
    crashAnim: CrashAnimState?,
    mode: GameMode,
    isRunning: Boolean
) {
    for (side in 0..1) {
        val isLeft = side == 0
        val activeSide = crashAnim != null && crashAnim.leftSide == isLeft
        val starInk = if (activeSide) LcdInkOn else LcdInkGhost
        val starX = if (isLeft) 232f else 768f
        val starY = 532f

        val star = Path().apply {
            val pts = 12
            for (i in 0 until pts) {
                val r = if (i % 2 == 0) 19f else 9f
                val angle = (i * 30.0) * (PI / 180.0)
                val px = starX + (r * cos(angle)).toFloat()
                val py = starY + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(vp.x(px), vp.y(py)) else lineTo(vp.x(px), vp.y(py))
            }
            close()
        }
        drawPath(star, color = starInk, style = Stroke(width = vp.s(3.2f)))
        drawCircle(color = starInk, radius = vp.s(3.2f), center = vp.pt(starX - 28f, starY - 6f))
        drawCircle(color = starInk, radius = vp.s(3.2f), center = vp.pt(starX + 28f, starY - 4f))

        for (step in 1..4) {
            val chickLit = activeSide && (crashAnim?.frame == step || (step == 4 && crashAnim?.frame == 3))
            val chickInk = if (chickLit) LcdInkOn else LcdInkGhost
            val dir = if (isLeft) -1f else 1f
            val cx = starX + dir * (step * 44f)
            val cy = 500f
            drawRunningChickSilhouette(vp, cx, cy, facingRight = !isLeft, ink = chickInk)
        }
    }

    val gameAInk = if (!isRunning || mode == GameMode.GAME_A) LcdInkOn else LcdInkGhost
    val gameBInk = if (!isRunning || mode == GameMode.GAME_B) LcdInkOn else LcdInkGhost
    drawGameModeWord(vp, startX = 24f, topY = 556f, isB = false, ink = gameAInk)
    drawGameModeWord(vp, startX = 858f, topY = 556f, isB = true, ink = gameBInk)
}

private fun DrawScope.drawRunningChickSilhouette(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    facingRight: Boolean,
    ink: Color
) {
    val dir = if (facingRight) 1f else -1f
    drawCircle(color = ink, radius = vp.s(7.5f), center = vp.pt(cx, cy - 8f))
    drawOval(
        color = ink,
        topLeft = vp.pt(cx - 10f, cy - 3f),
        size = Size(vp.x(20f), vp.y(14f))
    )
    drawLine(ink, vp.pt(cx - dir * 6f, cy), vp.pt(cx - dir * 14f, cy - 6f), vp.s(3f), StrokeCap.Round)
    drawLine(ink, vp.pt(cx - 3f, cy + 10f), vp.pt(cx - 8f, cy + 20f), vp.s(2.8f), StrokeCap.Round)
    drawLine(ink, vp.pt(cx + 4f, cy + 10f), vp.pt(cx + 9f, cy + 20f), vp.s(2.8f), StrokeCap.Round)
}

private fun DrawScope.drawGameModeWord(
    vp: LcdViewport,
    startX: Float,
    topY: Float,
    isB: Boolean,
    ink: Color
) {
    val sw = vp.s(3.4f)
    val h = 18f
    val w = 13f
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
    drawPath(gPath, color = ink, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    x += w + 7f

    drawLetterA(vp, x, topY, w, h, sw, ink)
    x += w + 7f

    val mPath = Path().apply {
        moveTo(vp.x(x), vp.y(topY + h))
        lineTo(vp.x(x), vp.y(topY))
        lineTo(vp.x(x + w * 0.55f), vp.y(topY + h * 0.6f))
        lineTo(vp.x(x + w * 1.1f), vp.y(topY))
        lineTo(vp.x(x + w * 1.1f), vp.y(topY + h))
    }
    drawPath(mPath, color = ink, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    x += w * 1.1f + 7f

    drawLine(ink, vp.pt(x, topY), vp.pt(x, topY + h), sw, StrokeCap.Round)
    drawLine(ink, vp.pt(x, topY), vp.pt(x + w * 0.9f, topY), sw, StrokeCap.Round)
    drawLine(ink, vp.pt(x, topY + h * 0.5f), vp.pt(x + w * 0.75f, topY + h * 0.5f), sw, StrokeCap.Round)
    drawLine(ink, vp.pt(x, topY + h), vp.pt(x + w * 0.9f, topY + h), sw, StrokeCap.Round)
    x += w + 15f

    if (!isB) {
        drawLetterA(vp, x, topY, w, h, sw, ink)
    } else {
        drawLine(ink, vp.pt(x, topY), vp.pt(x, topY + h), sw, StrokeCap.Round)
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
        drawPath(bLoops, color = ink, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawLetterA(
    vp: LcdViewport,
    x: Float,
    topY: Float,
    w: Float,
    h: Float,
    sw: Float,
    ink: Color
) {
    val aPath = Path().apply {
        moveTo(vp.x(x), vp.y(topY + h))
        lineTo(vp.x(x + w * 0.5f), vp.y(topY))
        lineTo(vp.x(x + w), vp.y(topY + h))
    }
    drawPath(aPath, color = ink, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawLine(ink, vp.pt(x + w * 0.2f, topY + h * 0.65f), vp.pt(x + w * 0.8f, topY + h * 0.65f), sw, StrokeCap.Round)
}
