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

    // 6. Vlnite trsy travy dole (misto trojuhelnikove pilky)
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
    // Plny zaobleny pas drnu se stébly travy jako na originalnim potisku
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

    // Dlouhe usi zajicka
    val ears = Path().apply {
        moveTo(vp.x(hx - 6f), vp.y(hy - 18f))
        cubicTo(vp.x(hx - 28f), vp.y(hy - 56f), vp.x(hx - 14f), vp.y(hy - 66f), vp.x(hx - 2f), vp.y(hy - 40f))
        cubicTo(vp.x(hx - 6f), vp.y(hy - 64f), vp.x(hx + 12f), vp.y(hy - 62f), vp.x(hx + 8f), vp.y(hy - 20f))
        close()
    }
    drawPath(ears, color = ink)
    drawLine(LcdBgColor, vp.pt(hx - 12f, hy - 48f), vp.pt(hx - 5f, hy - 26f), vp.s(2.2f))

    // Hlava s tvickami a ocima
    drawOval(
        color = ink,
        topLeft = vp.pt(hx - 20f, hy - 22f),
        size = Size(vp.x(44f), vp.y(36f)),
        style = Stroke(width = vp.s(3.8f))
    )
    drawOval(ink, vp.pt(hx - 2f, hy - 14f), Size(vp.x(6f), vp.y(10f)))
    drawOval(ink, vp.pt(hx + 8f, hy - 14f), Size(vp.x(6f), vp.y(10f)))
    drawCircle(ink, radius = vp.s(3.2f), center = vp.pt(hx + 18f, hy - 3f))
    drawLine(ink, vp.pt(hx + 16f, hy - 2f), vp.pt(hx + 32f, hy - 8f), vp.s(2f))
    drawLine(ink, vp.pt(hx + 16f, hy + 1f), vp.pt(hx + 33f, hy + 1f), vp.s(2f))

    // Trup zajicka
    val body = Path().apply {
        moveTo(vp.x(hx - 40f), vp.y(hy + 12f))
        lineTo(vp.x(hx - 8f), vp.y(hy + 8f))
        lineTo(vp.x(hx + 6f), vp.y(hy + 32f))
        lineTo(vp.x(hx - 36f), vp.y(hy + 36f))
        close()
    }
    drawPath(body, color = ink)

    drawOval(
        color = ink,
        topLeft = vp.pt(hx - 8f, hy + 30f),
        size = Size(vp.x(28f), vp.y(12f)),
        style = Stroke(width = vp.s(3.5f))
    )

    val upInk = if (visible && bellUp) LcdInkOn else LcdInkGhost
    val downInk = if (visible && !bellUp) LcdInkOn else LcdInkGhost

    drawHareBellHand(vp, startX = hx + 2f, startY = hy + 14f, bellX = hx + 58f, bellY = hy - 16f, ink = upInk)
    drawHareBellHand(vp, startX = hx + 2f, startY = hy + 22f, bellX = hx + 46f, bellY = hy + 36f, ink = downInk)
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
        strokeWidth = vp.s(5f),
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

    // 3 plne siluety trestnych kuratek primo pod cislicemi (jako na fotce IM-02)
    for (idx in 0..2) {
        val thresholdFull = (idx + 1) * 2
        val thresholdHalf = thresholdFull - 1
        val isLit = when {
            penaltyHalfPoints >= thresholdFull -> true
            penaltyHalfPoints == thresholdHalf -> blinkPhase
            else -> false
        }
        val ink = if (isLit) LcdInkOn else LcdInkGhost
        val cx = 548f + idx * 62f
        val cy = 138f
        drawSolidPenaltyChick(vp, cx, cy, ink)
    }
}

private fun DrawScope.drawSolidPenaltyChick(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    ink: Color
) {
    // Plna silueta kuratka s hlavickou, zobackem, roztazenymi kridelky a nozickami
    drawCircle(color = ink, radius = vp.s(7.5f), center = vp.pt(cx, cy - 11f))
    // Zobacek a chocholka
    val headTop = Path().apply {
        moveTo(vp.x(cx - 12f), vp.y(cy - 15f))
        lineTo(vp.x(cx - 2f), vp.y(cy - 20f))
        lineTo(vp.x(cx + 12f), vp.y(cy - 13f))
        close()
    }
    drawPath(headTop, color = ink)

    // Telicko a roztazena kridla
    val wingsAndBody = Path().apply {
        moveTo(vp.x(cx - 23f), vp.y(cy - 2f))
        cubicTo(vp.x(cx - 14f), vp.y(cy - 10f), vp.x(cx + 14f), vp.y(cy - 10f), vp.x(cx + 23f), vp.y(cy - 2f))
        lineTo(vp.x(cx + 14f), vp.y(cy + 6f))
        lineTo(vp.x(cx + 21f), vp.y(cy + 12f))
        lineTo(vp.x(cx - 21f), vp.y(cy + 12f))
        lineTo(vp.x(cx - 14f), vp.y(cy + 6f))
        close()
    }
    drawPath(wingsAndBody, color = ink)
    // Drobny svetly pruh uprostred telicka jako na LCD segmentu
    drawLine(LcdBgColor, vp.pt(cx - 10f, cy + 3f), vp.pt(cx + 10f, cy + 3f), vp.s(2.4f), StrokeCap.Round)
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
    // Jemny sklon cislic doprava jako na kalkulackovem displeji
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

    // Dve kreslene postavy vlka uprostred (mirne skrcene v sirokem postoji)
    drawSingleCartoonWolf(vp, baseX = 446f, facingLeft = true, ink = leftInk)
    drawSingleCartoonWolf(vp, baseX = 554f, facingLeft = false, ink = rightInk)

    // 4 pokrcene ruce s tlapkou a kosikem primo pod konci 4 ramp (zadna dlouha naberacka!)
    drawShortBentArmWithBasket(
        vp = vp,
        shoulderX = 420f,
        shoulderY = 352f,
        elbowX = 382f,
        elbowY = 344f,
        basketX = 316f,
        basketY = 322f,
        facingLeft = true,
        ink = if (wolfPos == BasketPosition.TOP_LEFT) LcdInkOn else LcdInkGhost
    )
    drawShortBentArmWithBasket(
        vp = vp,
        shoulderX = 414f,
        shoulderY = 384f,
        elbowX = 378f,
        elbowY = 424f,
        basketX = 316f,
        basketY = 462f,
        facingLeft = true,
        ink = if (wolfPos == BasketPosition.BOTTOM_LEFT) LcdInkOn else LcdInkGhost
    )
    drawShortBentArmWithBasket(
        vp = vp,
        shoulderX = 580f,
        shoulderY = 352f,
        elbowX = 618f,
        elbowY = 344f,
        basketX = 684f,
        basketY = 322f,
        facingLeft = false,
        ink = if (wolfPos == BasketPosition.TOP_RIGHT) LcdInkOn else LcdInkGhost
    )
    drawShortBentArmWithBasket(
        vp = vp,
        shoulderX = 586f,
        shoulderY = 384f,
        elbowX = 622f,
        elbowY = 424f,
        basketX = 684f,
        basketY = 462f,
        facingLeft = false,
        ink = if (wolfPos == BasketPosition.BOTTOM_RIGHT) LcdInkOn else LcdInkGhost
    )
}

private fun DrawScope.drawSingleCartoonWolf(
    vp: LcdViewport,
    baseX: Float,
    facingLeft: Boolean,
    ink: Color
) {
    val dir = if (facingLeft) -1f else 1f
    val strokeMain = Stroke(width = vp.s(3.8f), cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Rozcuchana hriva a usi vlka (posazene nize a sirsi jako na originalnim LCD)
    val manePath = Path().apply {
        moveTo(vp.x(baseX - dir * 20f), vp.y(278f))
        lineTo(vp.x(baseX - dir * 36f), vp.y(290f))
        lineTo(vp.x(baseX - dir * 24f), vp.y(304f))
        lineTo(vp.x(baseX - dir * 38f), vp.y(320f))
        lineTo(vp.x(baseX - dir * 18f), vp.y(332f))
        lineTo(vp.x(baseX + dir * 10f), vp.y(328f))
        lineTo(vp.x(baseX + dir * 20f), vp.y(290f))
        // Usi a chomace srsti nahore
        lineTo(vp.x(baseX + dir * 26f), vp.y(262f))
        lineTo(vp.x(baseX + dir * 8f), vp.y(276f))
        lineTo(vp.x(baseX + dir * 2f), vp.y(256f))
        lineTo(vp.x(baseX - dir * 10f), vp.y(274f))
        lineTo(vp.x(baseX - dir * 26f), vp.y(260f))
        close()
    }
    drawPath(manePath, color = ink)

    // Svetla cast obliceje a oci
    drawOval(
        color = LcdBgColor,
        topLeft = vp.pt(baseX - 13f, 280f),
        size = Size(vp.x(26f), vp.y(30f))
    )
    drawCircle(color = ink, radius = vp.s(3.0f), center = vp.pt(baseX + dir * 6f, 292f))

    // Cumak s velkym nosem a otevrenou tlamou
    val snout = Path().apply {
        moveTo(vp.x(baseX + dir * 10f), vp.y(296f))
        lineTo(vp.x(baseX + dir * 52f), vp.y(298f))
        cubicTo(
            vp.x(baseX + dir * 56f), vp.y(312f),
            vp.x(baseX + dir * 40f), vp.y(320f),
            vp.x(baseX + dir * 16f), vp.y(316f)
        )
        lineTo(vp.x(baseX + dir * 38f), vp.y(328f))
        lineTo(vp.x(baseX + dir * 28f), vp.y(336f))
        lineTo(vp.x(baseX + dir * 6f), vp.y(326f))
    }
    drawPath(snout, color = ink, style = strokeMain)
    drawCircle(color = ink, radius = vp.s(5.8f), center = vp.pt(baseX + dir * 53f, 296f))

    // 2. Prohnuty tmavy trup vlka (kratsi a podsaditejsi)
    val torso = Path().apply {
        moveTo(vp.x(baseX - dir * 16f), vp.y(330f))
        lineTo(vp.x(baseX + dir * 16f), vp.y(330f))
        cubicTo(
            vp.x(baseX + dir * 28f), vp.y(362f),
            vp.x(baseX + dir * 24f), vp.y(390f),
            vp.x(baseX + dir * 14f), vp.y(414f)
        )
        lineTo(vp.x(baseX - dir * 22f), vp.y(408f))
        cubicTo(
            vp.x(baseX - dir * 10f), vp.y(380f),
            vp.x(baseX - dir * 8f), vp.y(352f),
            vp.x(baseX - dir * 16f), vp.y(330f)
        )
        close()
    }
    drawPath(torso, color = ink)

    drawLine(LcdBgColor, vp.pt(baseX, 332f), vp.pt(baseX + dir * 10f, 348f), vp.s(3.2f))
    drawLine(LcdBgColor, vp.pt(baseX - 20f, 406f), vp.pt(baseX + 18f, 410f), vp.s(4f))

    // 3. Siroce rozkrocene svetle kratasy s puntiky
    val shorts = Path().apply {
        moveTo(vp.x(baseX - dir * 22f), vp.y(408f))
        lineTo(vp.x(baseX + dir * 16f), vp.y(412f))
        lineTo(vp.x(baseX + dir * 46f), vp.y(446f))
        lineTo(vp.x(baseX + dir * 26f), vp.y(462f))
        lineTo(vp.x(baseX - dir * 2f), vp.y(438f))
        lineTo(vp.x(baseX - dir * 26f), vp.y(464f))
        lineTo(vp.x(baseX - dir * 46f), vp.y(448f))
        close()
    }
    drawPath(shorts, color = ink, style = strokeMain)
    drawCircle(ink, radius = vp.s(2.8f), center = vp.pt(baseX + dir * 20f, 442f))
    drawCircle(ink, radius = vp.s(2.8f), center = vp.pt(baseX - dir * 20f, 444f))
    drawCircle(ink, radius = vp.s(2.8f), center = vp.pt(baseX - dir * 4f, 424f))

    // 4. Pokrcene nohy a chodidla v podrepu
    val frontLeg = Path().apply {
        moveTo(vp.x(baseX + dir * 36f), vp.y(454f))
        lineTo(vp.x(baseX + dir * 16f), vp.y(494f))
        lineTo(vp.x(baseX + dir * 42f), vp.y(500f))
    }
    val backLeg = Path().apply {
        moveTo(vp.x(baseX - dir * 36f), vp.y(456f))
        lineTo(vp.x(baseX - dir * 14f), vp.y(496f))
        lineTo(vp.x(baseX - dir * 40f), vp.y(502f))
    }
    drawPath(frontLeg, color = ink, style = Stroke(width = vp.s(5.8f), cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(backLeg, color = ink, style = Stroke(width = vp.s(5.8f), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawShortBentArmWithBasket(
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
    val wristX = basketX - dir * 34f
    val wristY = basketY - 8f

    // Kratka pokrcena paze s loktem a kreslenymi prsty tlapky (misto rovnych tycek)
    val upperArm = Path().apply {
        moveTo(vp.x(shoulderX), vp.y(shoulderY))
        lineTo(vp.x(elbowX), vp.y(elbowY))
        lineTo(vp.x(wristX), vp.y(wristY))
    }
    drawPath(
        path = upperArm,
        color = ink,
        style = Stroke(width = vp.s(4.8f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 3 prsty tlapky vlka svirajici ucho kosiku
    drawLine(ink, vp.pt(wristX, wristY), vp.pt(wristX + dir * 14f, wristY - 8f), vp.s(3.4f), StrokeCap.Round)
    drawLine(ink, vp.pt(wristX, wristY), vp.pt(wristX + dir * 16f, wristY - 1f), vp.s(3.4f), StrokeCap.Round)
    drawLine(ink, vp.pt(wristX, wristY + 3f), vp.pt(wristX + dir * 14f, wristY + 6f), vp.s(3.4f), StrokeCap.Round)

    // Siroky zaobleny kosik nakloneny smerem k bidylku (jako na fotce IM-02)
    val tiltDeg = if (facingLeft) -12f else 12f
    val pivot = vp.pt(basketX, basketY)
    val bw = 66f
    val bh = 42f

    rotate(degrees = tiltDeg, pivot = pivot) {
        val basketBowl = Path().apply {
            moveTo(vp.x(basketX - bw * 0.50f), vp.y(basketY - bh * 0.22f))
            lineTo(vp.x(basketX + bw * 0.50f), vp.y(basketY - bh * 0.22f))
            cubicTo(
                vp.x(basketX + bw * 0.48f), vp.y(basketY + bh * 0.58f),
                vp.x(basketX + bw * 0.22f), vp.y(basketY + bh * 0.75f),
                vp.x(basketX), vp.y(basketY + bh * 0.75f)
            )
            cubicTo(
                vp.x(basketX - bw * 0.22f), vp.y(basketY + bh * 0.75f),
                vp.x(basketX - bw * 0.48f), vp.y(basketY + bh * 0.58f),
                vp.x(basketX - bw * 0.50f), vp.y(basketY - bh * 0.22f)
            )
            close()
        }
        drawPath(basketBowl, color = ink)

        // Svetly otvor kosiku nahore s vlnkovym lemem
        drawOval(
            color = LcdBgColor,
            topLeft = vp.pt(basketX - bw * 0.44f, basketY - bh * 0.30f),
            size = Size(vp.x(bw * 0.88f), vp.y(bh * 0.28f))
        )
        drawOval(
            color = ink,
            topLeft = vp.pt(basketX - bw * 0.44f, basketY - bh * 0.30f),
            size = Size(vp.x(bw * 0.88f), vp.y(bh * 0.28f)),
            style = Stroke(width = vp.s(3.4f))
        )
        // Bocni ucho kosiku
        drawArc(
            color = ink,
            startAngle = if (facingLeft) -60f else 120f,
            sweepAngle = 120f,
            useCenter = false,
            topLeft = vp.pt(basketX - dir * 36f - 10f, basketY - 14f),
            size = Size(vp.x(20f), vp.y(22f)),
            style = Stroke(width = vp.s(3.4f), cap = StrokeCap.Round)
        )
    }
}

internal fun DrawScope.drawAuthenticHollowEggs(
    vp: LcdViewport,
    activeEggs: List<EggItem>
) {
    // Podelne elipsy vajicek naklonene po smeru sklonu bidylek
    val eggW = vp.s(31f)
    val eggH = vp.s(21f)
    val ringStroke = vp.s(5.0f)

    for (ramp in 0..3) {
        for (step in 0..4) {
            val isLit = activeEggs.any { it.ramp == ramp && it.step == step }
            val ink = if (isLit) LcdInkOn else LcdInkGhost
            val pos = authenticEggCoordinate(vp, ramp, step)

            val tilt = when (ramp) {
                0, 1 -> 24f + step * 8f
                else -> -24f - step * 8f
            }
            rotate(degrees = tilt, pivot = pos) {
                drawOval(
                    color = LcdBgColor,
                    topLeft = Offset(pos.x - eggW * 0.5f, pos.y - eggH * 0.5f),
                    size = Size(eggW, eggH)
                )
                drawOval(
                    color = ink,
                    topLeft = Offset(pos.x - eggW * 0.5f, pos.y - eggH * 0.5f),
                    size = Size(eggW, eggH),
                    style = Stroke(width = ringStroke)
                )
            }
        }
    }
}

private fun authenticEggCoordinate(vp: LcdViewport, ramp: Int, step: Int): Offset {
    // 5 pozic vajicek podel bidylka, 5. vajicko prepadava z hrany do kosiku
    val t = step / 4f
    return when (ramp) {
        0 -> vp.pt(96f + t * 172f, 198f + t * 88f)
        1 -> vp.pt(96f + t * 172f, 340f + t * 88f)
        2 -> vp.pt(904f - t * 172f, 198f + t * 88f)
        else -> vp.pt(904f - t * 172f, 340f + t * 88f)
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

        // Hvezdice rozbiteho vajicka a 2 postranní kapky
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

        // 4 pozice utikajicich kuratek
        for (step in 1..4) {
            val chickLit = activeSide && (crashAnim?.frame == step || (step == 4 && crashAnim?.frame == 3))
            val chickInk = if (chickLit) LcdInkOn else LcdInkGhost
            val dir = if (isLeft) -1f else 1f
            val cx = starX + dir * (step * 44f)
            val cy = 500f
            drawRunningChickSilhouette(vp, cx, cy, facingRight = !isLeft, ink = chickInk)
        }
    }

    // Skutecne napisy GAME A a GAME B v dolnich rozich LCD (bez ramecku)
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
    // Vykresleni plneho textu "GAME A" nebo "GAME B" v dolnim rohu displeje
    val sw = vp.s(3.4f)
    val h = 18f
    val w = 13f
    var x = startX

    // Pismeno G
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

    // Pismeno A
    drawLetterA(vp, x, topY, w, h, sw, ink)
    x += w + 7f

    // Pismeno M
    val mPath = Path().apply {
        moveTo(vp.x(x), vp.y(topY + h))
        lineTo(vp.x(x), vp.y(topY))
        lineTo(vp.x(x + w * 0.55f), vp.y(topY + h * 0.6f))
        lineTo(vp.x(x + w * 1.1f), vp.y(topY))
        lineTo(vp.x(x + w * 1.1f), vp.y(topY + h))
    }
    drawPath(mPath, color = ink, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    x += w * 1.1f + 7f

    // Pismeno E
    drawLine(ink, vp.pt(x, topY), vp.pt(x, topY + h), sw, StrokeCap.Round)
    drawLine(ink, vp.pt(x, topY), vp.pt(x + w * 0.9f, topY), sw, StrokeCap.Round)
    drawLine(ink, vp.pt(x, topY + h * 0.5f), vp.pt(x + w * 0.75f, topY + h * 0.5f), sw, StrokeCap.Round)
    drawLine(ink, vp.pt(x, topY + h), vp.pt(x + w * 0.9f, topY + h), sw, StrokeCap.Round)
    x += w + 15f

    // Koncove A nebo B
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
