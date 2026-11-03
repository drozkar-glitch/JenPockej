package cz.mares.jenpockej

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.min

internal val LcdBgColor = Color(0xFFCFD4C4)
internal val LcdShadowTint = Color(0x22000000)
internal val LcdInkOn = Color(0xFF171A14)
internal val LcdInkGhost = Color(0xFFBDC3B2)

// Barvy staleho potisku sklicka podle originalniho IM-02
private val PrintRed = Color(0xFFA82820)
private val PrintBrown = Color(0xFF8A5E36)
private val PrintGreen = Color(0xFF475827)
private val PrintWhite = Color(0xFFF3F4EE)

internal class LcdViewport(val canvasW: Float, val canvasH: Float) {
    // Zakladni souradnicova sit 1000x600 s jemnym prizpusobenim sirce displeje
    private val scaleX = canvasW / 1000f
    private val scaleY = canvasH / 600f
    val u = min(scaleX, scaleY)

    fun x(v: Float): Float = v * scaleX
    fun y(v: Float): Float = v * scaleY
    fun pt(vx: Float, vy: Float): Offset = Offset(vx * scaleX, vy * scaleY)
    fun s(v: Float): Float = v * u
}

internal fun DrawScope.drawStaticSceneryAndHens(vp: LcdViewport) {
    // 1. Cervena strecha a hnede okno chaloupky zajicka v levem hornim rohu
    val roofPath = Path().apply {
        moveTo(vp.x(6f), vp.y(105f))
        lineTo(vp.x(56f), vp.y(22f))
        lineTo(vp.x(138f), vp.y(128f))
    }
    drawPath(
        path = roofPath,
        color = PrintRed,
        style = Stroke(width = vp.s(13f), cap = StrokeCap.Butt, join = StrokeJoin.Miter)
    )
    // Hnedy komin a okenni ram s mrizkou
    drawRect(
        color = PrintRed,
        topLeft = vp.pt(112f, 58f),
        size = Size(vp.x(22f), vp.y(38f))
    )
    val windowFrame = Path().apply {
        moveTo(vp.x(146f), vp.y(58f))
        lineTo(vp.x(184f), vp.y(46f))
        lineTo(vp.x(184f), vp.y(142f))
        lineTo(vp.x(146f), vp.y(132f))
        close()
    }
    drawPath(
        path = windowFrame,
        color = PrintBrown,
        style = Stroke(width = vp.s(6f), join = StrokeJoin.Round)
    )
    drawLine(PrintBrown, vp.pt(146f, 95f), vp.pt(184f, 92f), vp.s(5f))
    drawLine(PrintBrown, vp.pt(125f, 146f), vp.pt(210f, 146f), vp.s(5f))

    // 2. Horni zeleny ker v pravem hornim rohu
    drawBushCluster(
        vp = vp,
        circles = listOf(
            Triple(925f, 38f, 26f),
            Triple(955f, 44f, 28f),
            Triple(982f, 36f, 26f)
        )
    )
    drawLine(
        color = PrintWhite,
        start = vp.pt(912f, 52f),
        end = vp.pt(988f, 56f),
        strokeWidth = vp.s(3.5f),
        cap = StrokeCap.Round
    )

    // 3. Kere pod spodnimi bidylky vlevo a vpravo
    drawBushCluster(
        vp = vp,
        circles = listOf(
            Triple(25f, 458f, 26f),
            Triple(56f, 462f, 30f),
            Triple(92f, 466f, 25f),
            Triple(210f, 474f, 24f),
            Triple(238f, 478f, 20f)
        )
    )
    drawBushCluster(
        vp = vp,
        circles = listOf(
            Triple(975f, 458f, 26f),
            Triple(944f, 462f, 30f),
            Triple(908f, 466f, 25f),
            Triple(790f, 474f, 24f),
            Triple(762f, 478f, 20f)
        )
    )

    // 4. Hneda drevena bidylka, sloupky a plot pod spodnimi rampami
    // Leva horni rampa
    drawBrownPerch(vp, x0 = 6f, y0 = 212f, x1 = 76f, y1 = 212f, x2 = 260f, y2 = 296f, postX = 165f, postH = 54f)
    // Leva dolni rampa + plot
    drawFenceUnderRamp(vp, startX = 12f, endX = 155f, topY = 365f, bottomY = 445f)
    drawBrownPerch(vp, x0 = 6f, y0 = 356f, x1 = 76f, y1 = 356f, x2 = 260f, y2 = 440f, postX = 165f, postH = 62f)

    // Prava horni rampa
    drawBrownPerch(vp, x0 = 994f, y0 = 212f, x1 = 924f, y1 = 212f, x2 = 740f, y2 = 296f, postX = 835f, postH = 54f)
    // Prava dolni rampa + plot
    drawFenceUnderRamp(vp, startX = 845f, endX = 988f, topY = 365f, bottomY = 445f)
    drawBrownPerch(vp, x0 = 994f, y0 = 356f, x1 = 924f, y1 = 356f, x2 = 740f, y2 = 440f, postX = 835f, postH = 62f)

    // 5. Ctyri bile slepice s cervenym hrebinkem na bidylkach
    drawWhiteHenOnPerch(vp, cx = 42f, cy = 168f, facingRight = true)
    drawWhiteHenOnPerch(vp, cx = 42f, cy = 312f, facingRight = true)
    drawWhiteHenOnPerch(vp, cx = 958f, cy = 168f, facingRight = false)
    drawWhiteHenOnPerch(vp, cx = 958f, cy = 312f, facingRight = false)

    // 6. Trsy travy dole uprostred a po stranach
    drawGrassPatch(vp, startX = 16f, endX = 195f, baseY = 532f)
    drawGrassPatch(vp, startX = 295f, endX = 395f, baseY = 515f)
    drawGrassPatch(vp, startX = 360f, endX = 465f, baseY = 545f)
    drawGrassPatch(vp, startX = 515f, endX = 635f, baseY = 545f)
    drawGrassPatch(vp, startX = 560f, endX = 665f, baseY = 515f)
    drawGrassPatch(vp, startX = 805f, endX = 984f, baseY = 532f)
}

private fun DrawScope.drawBushCluster(
    vp: LcdViewport,
    circles: List<Triple<Float, Float, Float>>
) {
    for ((cx, cy, r) in circles) {
        drawCircle(
            color = PrintGreen,
            radius = vp.s(r),
            center = vp.pt(cx, cy)
        )
    }
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

    // Svisly sloupek pod sikmym bidylkem
    drawLine(
        color = PrintBrown,
        start = vp.pt(postX, postTopY),
        end = vp.pt(postX, postTopY + postH),
        strokeWidth = vp.s(11f),
        cap = StrokeCap.Square
    )

    // Vodorovna podesta pod slepici + sikmy zlabek pro vajicka
    val perchPath = Path().apply {
        moveTo(vp.x(x0), vp.y(y0))
        lineTo(vp.x(x1), vp.y(y1))
        lineTo(vp.x(x2), vp.y(y2))
    }
    drawPath(
        path = perchPath,
        color = PrintBrown,
        style = Stroke(width = vp.s(11f), cap = StrokeCap.Round, join = StrokeJoin.Round)
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
            start = vp.pt(px, topY + (i % 2) * 8f),
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

    // Bila silueta slepice
    val henBody = Path().apply {
        moveTo(vp.x(cx - dir * 28f), vp.y(cy + 36f))
        cubicTo(
            vp.x(cx - dir * 34f), vp.y(cy + 10f),
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

    // Cerveny hrebinek a zobak
    val comb = Path().apply {
        moveTo(vp.x(cx + dir * 4f), vp.y(cy - 22f))
        lineTo(vp.x(cx + dir * 8f), vp.y(cy - 35f))
        lineTo(vp.x(cx + dir * 15f), vp.y(cy - 26f))
        lineTo(vp.x(cx + dir * 22f), vp.y(cy - 33f))
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

    // Oko a kresba kridla
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

private fun DrawScope.drawGrassPatch(
    vp: LcdViewport,
    startX: Float,
    endX: Float,
    baseY: Float
) {
    val path = Path().apply {
        moveTo(vp.x(startX), vp.y(baseY))
        var curX = startX
        while (curX < endX) {
            lineTo(vp.x(curX + 6f), vp.y(baseY - 12f))
            lineTo(vp.x(curX + 13f), vp.y(baseY))
            curX += 13f
        }
        close()
    }
    drawPath(path, color = PrintGreen)
}

internal fun DrawScope.drawHareInWindow(
    vp: LcdViewport,
    visible: Boolean,
    bellUp: Boolean
) {
    val ink = if (visible) LcdInkOn else LcdInkGhost
    val hx = 228f
    val hy = 98f

    // Dlouhe usi zajicka smerujici doleva nahoru
    val ears = Path().apply {
        moveTo(vp.x(hx - 6f), vp.y(hy - 18f))
        cubicTo(vp.x(hx - 28f), vp.y(hy - 56f), vp.x(hx - 14f), vp.y(hy - 66f), vp.x(hx - 2f), vp.y(hy - 40f))
        cubicTo(vp.x(hx - 6f), vp.y(hy - 64f), vp.x(hx + 12f), vp.y(hy - 62f), vp.x(hx + 8f), vp.y(hy - 20f))
        close()
    }
    drawPath(ears, color = ink)
    drawLine(LcdBgColor, vp.pt(hx - 12f, hy - 48f), vp.pt(hx - 5f, hy - 26f), vp.s(2.2f))

    // Obrys hlavy s tvickami a otevrenymi ocima (jako na fotce)
    drawOval(
        color = ink,
        topLeft = vp.pt(hx - 20f, hy - 22f),
        size = Size(vp.x(44f), vp.y(36f)),
        style = Stroke(width = vp.s(3.8f))
    )
    // Oci, cumacek a fousky
    drawOval(ink, vp.pt(hx - 2f, hy - 14f), Size(vp.x(6f), vp.y(10f)))
    drawOval(ink, vp.pt(hx + 8f, hy - 14f), Size(vp.x(6f), vp.y(10f)))
    drawCircle(ink, radius = vp.s(3.2f), center = vp.pt(hx + 18f, hy - 3f))
    drawLine(ink, vp.pt(hx + 16f, hy - 2f), vp.pt(hx + 32f, hy - 8f), vp.s(2f))
    drawLine(ink, vp.pt(hx + 16f, hy + 1f), vp.pt(hx + 33f, hy + 1f), vp.s(2f))

    // Tmavsi trup zajicka vyklanejici se z okna
    val body = Path().apply {
        moveTo(vp.x(hx - 42f), vp.y(hy + 12f))
        lineTo(vp.x(hx - 8f), vp.y(hy + 8f))
        lineTo(vp.x(hx + 6f), vp.y(hy + 32f))
        lineTo(vp.x(hx - 38f), vp.y(hy + 36f))
        close()
    }
    drawPath(body, color = ink)

    // Spodni packa oprena o parapet
    drawOval(
        color = ink,
        topLeft = vp.pt(hx - 8f, hy + 30f),
        size = Size(vp.x(28f), vp.y(12f)),
        style = Stroke(width = vp.s(3.5f))
    )

    // Horni a dolni poloha ruky se zvoneckem
    val upInk = if (visible && bellUp) LcdInkOn else LcdInkGhost
    val downInk = if (visible && !bellUp) LcdInkOn else LcdInkGhost

    drawHareBellHand(vp, startX = hx + 2f, startY = hy + 16f, bellX = hx + 58f, bellY = hy - 16f, ink = upInk)
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
    // 4 sedmisegmentove cislice vpravo nahore (presne jako 0000 na fotce)
    val clamped = score.coerceIn(0, 9999)
    val d0 = if (clamped >= 1000) (clamped / 1000) % 10 else 0
    val d1 = (clamped / 100) % 10
    val d2 = (clamped / 10) % 10
    val d3 = clamped % 10

    val dw = 36f
    val dh = 68f
    val gap = 18f
    val startX = 545f
    val topY = 34f

    drawClassicSevenSeg(vp, startX, topY, dw, dh, d0)
    drawClassicSevenSeg(vp, startX + (dw + gap), topY, dw, dh, d1)
    drawClassicSevenSeg(vp, startX + (dw + gap) * 2f, topY, dw, dh, d2)
    drawClassicSevenSeg(vp, startX + (dw + gap) * 3f, topY, dw, dh, d3)

    // 3 trestna kuratka PRIMO POD CISLICEMI (x = 530..670, y = 138)
    for (idx in 0..2) {
        val thresholdFull = (idx + 1) * 2
        val thresholdHalf = thresholdFull - 1
        val isLit = when {
            penaltyHalfPoints >= thresholdFull -> true
            penaltyHalfPoints == thresholdHalf -> blinkPhase
            else -> false
        }
        val ink = if (isLit) LcdInkOn else LcdInkGhost
        val cx = 540f + idx * 66f
        val cy = 142f
        drawPenaltyChickSymbol(vp, cx, cy, ink)
    }
}

private fun DrawScope.drawPenaltyChickSymbol(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    ink: Color
) {
    // Typicka silueta leziciho/rozplacnuteho kuratka pod skorem z IM-02
    drawOval(
        color = ink,
        topLeft = vp.pt(cx - 22f, cy - 8f),
        size = Size(vp.x(44f), vp.y(18f)),
        style = Stroke(width = vp.s(4f))
    )
    drawCircle(color = ink, radius = vp.s(7.5f), center = vp.pt(cx, cy - 12f))
    drawLine(ink, vp.pt(cx - 14f, cy - 16f), vp.pt(cx + 14f, cy - 12f), vp.s(3.5f), StrokeCap.Round)
    drawLine(ink, vp.pt(cx - 18f, cy + 4f), vp.pt(cx + 18f, cy + 4f), vp.s(4f), StrokeCap.Round)
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
    val sw = vp.s(6.8f)
    val midY = y + dh * 0.5f
    val botY = y + dh
    val p = 3f

    fun col(bit: Int): Color = if ((mask and (1 shl bit)) != 0) LcdInkOn else LcdInkGhost

    drawLine(col(0), vp.pt(x + p, y), vp.pt(x + dw - p, y), sw, StrokeCap.Round)
    drawLine(col(1), vp.pt(x, y + p), vp.pt(x, midY - p), sw, StrokeCap.Round)
    drawLine(col(2), vp.pt(x + dw, y + p), vp.pt(x + dw, midY - p), sw, StrokeCap.Round)
    drawLine(col(3), vp.pt(x + p, midY), vp.pt(x + dw - p, midY), sw, StrokeCap.Round)
    drawLine(col(4), vp.pt(x, midY + p), vp.pt(x, botY - p), sw, StrokeCap.Round)
    drawLine(col(5), vp.pt(x + dw, midY + p), vp.pt(x + dw, botY - p), sw, StrokeCap.Round)
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

    // Dve kreslene postavy vlka uprostred (leva a prava) presne podle fotky
    drawSingleCartoonWolf(vp, baseX = 445f, facingLeft = true, ink = leftInk)
    drawSingleCartoonWolf(vp, baseX = 555f, facingLeft = false, ink = rightInk)

    // 4 samostatne segmenty rukou s kosikem
    drawAuthenticBasketWithArm(
        vp = vp,
        shoulderX = 422f,
        shoulderY = 342f,
        basketX = 335f,
        basketY = 315f,
        facingLeft = true,
        ink = if (wolfPos == BasketPosition.TOP_LEFT) LcdInkOn else LcdInkGhost
    )
    drawAuthenticBasketWithArm(
        vp = vp,
        shoulderX = 418f,
        shoulderY = 368f,
        basketX = 332f,
        basketY = 462f,
        facingLeft = true,
        ink = if (wolfPos == BasketPosition.BOTTOM_LEFT) LcdInkOn else LcdInkGhost
    )
    drawAuthenticBasketWithArm(
        vp = vp,
        shoulderX = 578f,
        shoulderY = 342f,
        basketX = 665f,
        basketY = 315f,
        facingLeft = false,
        ink = if (wolfPos == BasketPosition.TOP_RIGHT) LcdInkOn else LcdInkGhost
    )
    drawAuthenticBasketWithArm(
        vp = vp,
        shoulderX = 582f,
        shoulderY = 368f,
        basketX = 668f,
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

    // 1. Rozcuchana hriva a usi vlka (kresba jako na IM-02)
    val manePath = Path().apply {
        moveTo(vp.x(baseX - dir * 18f), vp.y(265f))
        lineTo(vp.x(baseX - dir * 32f), vp.y(278f))
        lineTo(vp.x(baseX - dir * 22f), vp.y(292f))
        lineTo(vp.x(baseX - dir * 34f), vp.y(308f))
        lineTo(vp.x(baseX - dir * 16f), vp.y(316f))
        lineTo(vp.x(baseX + dir * 8f), vp.y(312f))
        lineTo(vp.x(baseX + dir * 18f), vp.y(278f))
        // Prave a leve ucho s chomaci
        lineTo(vp.x(baseX + dir * 24f), vp.y(252f))
        lineTo(vp.x(baseX + dir * 8f), vp.y(264f))
        lineTo(vp.x(baseX + dir * 2f), vp.y(246f))
        lineTo(vp.x(baseX - dir * 8f), vp.y(262f))
        lineTo(vp.x(baseX - dir * 22f), vp.y(248f))
        close()
    }
    drawPath(manePath, color = ink)

    // Svetly oblicej uvnitr hrivy + oci
    drawOval(
        color = LcdBgColor,
        topLeft = vp.pt(baseX - 12f, 268f),
        size = Size(vp.x(24f), vp.y(28f))
    )
    drawCircle(color = ink, radius = vp.s(2.8f), center = vp.pt(baseX + dir * 5f, 278f))

    // Dlouhy cumak s velkym nosem a sirokym usmevem
    val snout = Path().apply {
        moveTo(vp.x(baseX + dir * 8f), vp.y(282f))
        lineTo(vp.x(baseX + dir * 48f), vp.y(285f))
        cubicTo(
            vp.x(baseX + dir * 52f), vp.y(298f),
            vp.x(baseX + dir * 38f), vp.y(306f),
            vp.x(baseX + dir * 14f), vp.y(302f)
        )
        // Spodni celist
        lineTo(vp.x(baseX + dir * 36f), vp.y(312f))
        lineTo(vp.x(baseX + dir * 26f), vp.y(320f))
        lineTo(vp.x(baseX + dir * 4f), vp.y(312f))
    }
    drawPath(snout, color = ink, style = strokeMain)
    drawCircle(color = ink, radius = vp.s(5.5f), center = vp.pt(baseX + dir * 49f, 283f))

    // 2. Prohnuty tmavy trup vlka (kosile s vystrihem a paskem)
    val torso = Path().apply {
        moveTo(vp.x(baseX - dir * 14f), vp.y(316f))
        lineTo(vp.x(baseX + dir * 16f), vp.y(316f))
        cubicTo(
            vp.x(baseX + dir * 28f), vp.y(352f),
            vp.x(baseX + dir * 24f), vp.y(382f),
            vp.x(baseX + dir * 12f), vp.y(408f)
        )
        lineTo(vp.x(baseX - dir * 20f), vp.y(402f))
        cubicTo(
            vp.x(baseX - dir * 10f), vp.y(372f),
            vp.x(baseX - dir * 8f), vp.y(342f),
            vp.x(baseX - dir * 14f), vp.y(316f)
        )
        close()
    }
    drawPath(torso, color = ink)

    // Svetly limecek u krku a pasek s prezkou
    drawLine(LcdBgColor, vp.pt(baseX, 318f), vp.pt(baseX + dir * 10f, 336f), vp.s(3f))
    drawLine(LcdBgColor, vp.pt(baseX - 18f, 400f), vp.pt(baseX + 18f, 404f), vp.s(4f))

    // 3. Svetle kratasy s obrysem a kytickami/puntiky (presne jako na fotce IM-02)
    val shorts = Path().apply {
        moveTo(vp.x(baseX - dir * 20f), vp.y(402f))
        lineTo(vp.x(baseX + dir * 14f), vp.y(406f))
        lineTo(vp.x(baseX + dir * 42f), vp.y(438f))
        lineTo(vp.x(baseX + dir * 24f), vp.y(454f))
        lineTo(vp.x(baseX - dir * 2f), vp.y(432f))
        lineTo(vp.x(baseX - dir * 24f), vp.y(456f))
        lineTo(vp.x(baseX - dir * 42f), vp.y(440f))
        close()
    }
    drawPath(shorts, color = ink, style = strokeMain)
    // Puntiky na kratasech
    drawCircle(ink, radius = vp.s(2.6f), center = vp.pt(baseX + dir * 18f, 434f))
    drawCircle(ink, radius = vp.s(2.6f), center = vp.pt(baseX - dir * 18f, 436f))
    drawCircle(ink, radius = vp.s(2.6f), center = vp.pt(baseX - dir * 4f, 418f))

    // 4. Pokrcene nohy a tlapky v dynamickem postoji
    val frontLeg = Path().apply {
        moveTo(vp.x(baseX + dir * 32f), vp.y(446f))
        lineTo(vp.x(baseX + dir * 14f), vp.y(488f))
        lineTo(vp.x(baseX + dir * 38f), vp.y(494f))
    }
    val backLeg = Path().apply {
        moveTo(vp.x(baseX - dir * 32f), vp.y(448f))
        lineTo(vp.x(baseX - dir * 12f), vp.y(490f))
        lineTo(vp.x(baseX - dir * 36f), vp.y(496f))
    }
    drawPath(frontLeg, color = ink, style = Stroke(width = vp.s(5.5f), cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(backLeg, color = ink, style = Stroke(width = vp.s(5.5f), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawAuthenticBasketWithArm(
    vp: LcdViewport,
    shoulderX: Float,
    shoulderY: Float,
    basketX: Float,
    basketY: Float,
    facingLeft: Boolean,
    ink: Color
) {
    val dir = if (facingLeft) -1f else 1f
    val handX = basketX - dir * 26f
    val handY = basketY - 6f

    // Dvojita linka paze a prsty ruky drzici kosik (jako na fotce)
    drawLine(
        color = ink,
        start = vp.pt(shoulderX, shoulderY),
        end = vp.pt(handX, handY),
        strokeWidth = vp.s(4.5f),
        cap = StrokeCap.Round
    )
    drawLine(
        color = ink,
        start = vp.pt(shoulderX, shoulderY + 8f),
        end = vp.pt(handX, handY + 7f),
        strokeWidth = vp.s(3.5f),
        cap = StrokeCap.Round
    )

    // Cerny kulaty kosik se svetlym hornim lemem a uchem
    val bw = 62f
    val bh = 40f
    val basketBowl = Path().apply {
        moveTo(vp.x(basketX - bw * 0.5f), vp.y(basketY - bh * 0.25f))
        lineTo(vp.x(basketX + bw * 0.5f), vp.y(basketY - bh * 0.25f))
        cubicTo(
            vp.x(basketX + bw * 0.48f), vp.y(basketY + bh * 0.55f),
            vp.x(basketX + bw * 0.22f), vp.y(basketY + bh * 0.72f),
            vp.x(basketX), vp.y(basketY + bh * 0.72f)
        )
        cubicTo(
            vp.x(basketX - bw * 0.22f), vp.y(basketY + bh * 0.72f),
            vp.x(basketX - bw * 0.48f), vp.y(basketY + bh * 0.55f),
            vp.x(basketX - bw * 0.5f), vp.y(basketY - bh * 0.25f)
        )
        close()
    }
    drawPath(basketBowl, color = ink)

    // Svetla obruba nahore na kosiku
    drawOval(
        color = LcdBgColor,
        topLeft = vp.pt(basketX - bw * 0.44f, basketY - bh * 0.30f),
        size = Size(vp.x(bw * 0.88f), vp.y(bh * 0.26f))
    )
    drawOval(
        color = ink,
        topLeft = vp.pt(basketX - bw * 0.44f, basketY - bh * 0.30f),
        size = Size(vp.x(bw * 0.88f), vp.y(bh * 0.26f)),
        style = Stroke(width = vp.s(3.2f))
    )
}

internal fun DrawScope.drawAuthenticHollowEggs(
    vp: LcdViewport,
    activeEggs: List<EggItem>
) {
    val eggW = vp.s(26f)
    val eggH = vp.s(34f)
    val ringStroke = vp.s(5.2f)

    for (ramp in 0..3) {
        for (step in 0..4) {
            val isLit = activeEggs.any { it.ramp == ramp && it.step == step }
            val ink = if (isLit) LcdInkOn else LcdInkGhost
            val pos = authenticEggCoordinate(vp, ramp, step)

            // Sikme natoceni vajicek presne jako na originalnim LCD
            val tilt = when (ramp) {
                0, 1 -> -38f + step * 14f
                else -> 38f - step * 14f
            }
            rotate(degrees = tilt, pivot = pos) {
                // Svetly vnitrek a silny cerny obrys (duta vajicka z IM-02)
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
    val t = step / 4f
    return when (ramp) {
        0 -> vp.pt(96f + t * 182f, 200f + t * 88f)
        1 -> vp.pt(96f + t * 182f, 344f + t * 88f)
        2 -> vp.pt(904f - t * 182f, 200f + t * 88f)
        else -> vp.pt(904f - t * 182f, 344f + t * 88f)
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

        // Hvezdice rozbiteho vajicka se svetlym stredem a kapkami okolo (jako na fotce)
        val star = Path().apply {
            val pts = 10
            for (i in 0 until pts) {
                val r = if (i % 2 == 0) 20f else 9f
                val angle = (i * 36.0) * (kotlin.math.PI / 180.0)
                val px = starX + (r * kotlin.math.cos(angle)).toFloat()
                val py = starY + (r * kotlin.math.sin(angle)).toFloat()
                if (i == 0) moveTo(vp.x(px), vp.y(py)) else lineTo(vp.x(px), vp.y(py))
            }
            close()
        }
        drawPath(star, color = starInk, style = Stroke(width = vp.s(3.2f)))

        // 4 pozice utikajicich cernych kuratek po trave ke kraji displeje
        for (step in 1..4) {
            val chickLit = activeSide && (crashAnim?.frame == step || (step == 4 && crashAnim?.frame == 3))
            val chickInk = if (chickLit) LcdInkOn else LcdInkGhost
            val dir = if (isLeft) -1f else 1f
            val cx = starX + dir * (step * 44f)
            val cy = 502f
            drawRunningChickSilhouette(vp, cx, cy, facingRight = !isLeft, ink = chickInk)
        }
    }

    // Spodni indikator GAME A (vlevo dole) a GAME B (vpravo dole) primo na LCD
    val gameAInk = if (!isRunning || mode == GameMode.GAME_A) LcdInkOn else LcdInkGhost
    val gameBInk = if (!isRunning || mode == GameMode.GAME_B) LcdInkOn else LcdInkGhost
    drawMiniModeBadge(vp, x = 22f, y = 552f, isGameB = false, ink = gameAInk)
    drawMiniModeBadge(vp, x = 868f, y = 552f, isGameB = true, ink = gameBInk)
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
    // Kridelko a nozicky
    drawLine(ink, vp.pt(cx - dir * 6f, cy), vp.pt(cx - dir * 14f, cy - 6f), vp.s(3f), StrokeCap.Round)
    drawLine(ink, vp.pt(cx - 3f, cy + 10f), vp.pt(cx - 8f, cy + 20f), vp.s(2.8f), StrokeCap.Round)
    drawLine(ink, vp.pt(cx + 4f, cy + 10f), vp.pt(cx + 9f, cy + 20f), vp.s(2.8f), StrokeCap.Round)
}

private fun DrawScope.drawMiniModeBadge(
    vp: LcdViewport,
    x: Float,
    y: Float,
    isGameB: Boolean,
    ink: Color
) {
    // Vektorovy napis GAME A / GAME B v dolnich rozich LCD
    val sw = vp.s(3.2f)
    // Ramecek kolem popisku
    drawRoundRect(
        color = ink,
        topLeft = vp.pt(x, y),
        size = Size(vp.x(108f), vp.y(28f)),
        cornerRadius = CornerRadius(vp.s(4f), vp.s(4f)),
        style = Stroke(width = vp.s(2.2f))
    )
    // Pismeno A nebo B na konci stitku
    val lx = x + 78f
    val ly = y + 6f
    if (!isGameB) {
        // A
        drawLine(ink, vp.pt(lx, ly + 16f), vp.pt(lx + 8f, ly), sw, StrokeCap.Round)
        drawLine(ink, vp.pt(lx + 8f, ly), vp.pt(lx + 16f, ly + 16f), sw, StrokeCap.Round)
        drawLine(ink, vp.pt(lx + 3f, ly + 10f), vp.pt(lx + 13f, ly + 10f), sw, StrokeCap.Round)
    } else {
        // B
        drawLine(ink, vp.pt(lx, ly), vp.pt(lx, ly + 16f), sw, StrokeCap.Round)
        drawOval(ink, vp.pt(lx, ly), Size(vp.x(14f), vp.y(8f)), style = Stroke(width = sw))
        drawOval(ink, vp.pt(lx, ly + 8f), Size(vp.x(15f), vp.y(8f)), style = Stroke(width = sw))
    }
    // Zkratka GM pred pismenem
    drawLine(ink, vp.pt(x + 12f, ly + 8f), vp.pt(x + 62f, ly + 8f), sw, StrokeCap.Round)
}
