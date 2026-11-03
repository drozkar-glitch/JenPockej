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
import kotlin.math.min

internal val LcdBgColor = Color(0xFFA5B392)
internal val LcdInkOn = Color(0xFF181F15)
internal val LcdInkGhost = Color(0xFF919F80)
internal val LcdSilkRed = Color(0xFF8E241E)
internal val LcdSilkDark = Color(0xFF384232)

internal class LcdViewport(canvasWidth: Float, canvasHeight: Float) {
    val u: Float = min(canvasWidth / 1000f, canvasHeight / 520f)
    private val ox: Float = (canvasWidth - 1000f * u) * 0.5f
    private val oy: Float = (canvasHeight - 520f * u) * 0.5f

    fun pt(x: Float, y: Float): Offset = Offset(ox + x * u, oy + y * u)
    fun x(v: Float): Float = ox + v * u
    fun y(v: Float): Float = oy + v * u
    fun s(v: Float): Float = v * u
}

internal fun DrawScope.drawDetailedHen(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    facingRight: Boolean
) {
    val dir = if (facingRight) 1f else -1f

    // Proutene hnizdo pod slepici
    val nest = Path().apply {
        moveTo(vp.x(cx - 26f), vp.y(cy + 10f))
        lineTo(vp.x(cx + 26f), vp.y(cy + 10f))
        lineTo(vp.x(cx + 19f), vp.y(cy + 24f))
        lineTo(vp.x(cx - 19f), vp.y(cy + 24f))
        close()
    }
    drawPath(nest, color = LcdSilkDark)

    // Telo slepice s ocasem a hlavou
    val body = Path().apply {
        moveTo(vp.x(cx - dir * 18f), vp.y(cy + 10f))
        // Ocasni pera
        lineTo(vp.x(cx - dir * 28f), vp.y(cy - 6f))
        lineTo(vp.x(cx - dir * 22f), vp.y(cy - 10f))
        lineTo(vp.x(cx - dir * 27f), vp.y(cy - 18f))
        lineTo(vp.x(cx - dir * 14f), vp.y(cy - 14f))
        // Hrb a krk
        cubicTo(
            vp.x(cx - dir * 4f), vp.y(cy - 14f),
            vp.x(cx + dir * 4f), vp.y(cy - 28f),
            vp.x(cx + dir * 13f), vp.y(cy - 24f)
        )
        // Hlava a hrud
        cubicTo(
            vp.x(cx + dir * 22f), vp.y(cy - 18f),
            vp.x(cx + dir * 22f), vp.y(cy + 4f),
            vp.x(cx + dir * 12f), vp.y(cy + 10f)
        )
        close()
    }
    drawPath(body, color = LcdInkOn)

    // Cerveny hrebinek
    drawCircle(
        color = LcdSilkRed,
        radius = vp.s(4.2f),
        center = vp.pt(cx + dir * 9f, cy - 27f)
    )
    drawCircle(
        color = LcdSilkRed,
        radius = vp.s(3.8f),
        center = vp.pt(cx + dir * 15f, cy - 25f)
    )

    // Zobacek
    val beak = Path().apply {
        moveTo(vp.x(cx + dir * 19f), vp.y(cy - 18f))
        lineTo(vp.x(cx + dir * 29f), vp.y(cy - 14f))
        lineTo(vp.x(cx + dir * 19f), vp.y(cy - 11f))
        close()
    }
    drawPath(beak, color = LcdInkOn)

    // Oko a kridlo
    drawCircle(
        color = LcdBgColor,
        radius = vp.s(2.1f),
        center = vp.pt(cx + dir * 14f, cy - 17f)
    )
    drawArc(
        color = LcdBgColor,
        startAngle = if (facingRight) 20f else 160f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = vp.pt(cx - 10f, cy - 10f),
        size = Size(vp.s(20f), vp.s(14f)),
        style = Stroke(width = vp.s(2.2f), cap = StrokeCap.Round)
    )
}

internal fun DrawScope.drawDetailedHare(
    vp: LcdViewport,
    visible: Boolean,
    bellUp: Boolean
) {
    val hx = 225f
    val hy = 92f

    // Drevene okno a striska chaloupky
    val roof = Path().apply {
        moveTo(vp.x(hx - 62f), vp.y(hy - 40f))
        lineTo(vp.x(hx + 2f), vp.y(hy - 66f))
        lineTo(vp.x(hx + 68f), vp.y(hy - 38f))
    }
    drawPath(
        path = roof,
        color = LcdSilkRed,
        style = Stroke(width = vp.s(4.2f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawLine(
        color = LcdSilkDark,
        start = vp.pt(hx - 50f, hy + 34f),
        end = vp.pt(hx + 58f, hy + 34f),
        strokeWidth = vp.s(4f),
        cap = StrokeCap.Round
    )

    val ink = if (visible) LcdInkOn else LcdInkGhost

    // Dlouhe usi zajicka
    val leftEar = Path().apply {
        moveTo(vp.x(hx - 10f), vp.y(hy - 14f))
        cubicTo(
            vp.x(hx - 24f), vp.y(hy - 44f),
            vp.x(hx - 10f), vp.y(hy - 56f),
            vp.x(hx - 3f), vp.y(hy - 44f)
        )
        lineTo(vp.x(hx - 4f), vp.y(hy - 14f))
        close()
    }
    val rightEar = Path().apply {
        moveTo(vp.x(hx + 2f), vp.y(hy - 14f))
        cubicTo(
            vp.x(hx + 4f), vp.y(hy - 46f),
            vp.x(hx + 20f), vp.y(hy - 56f),
            vp.x(hx + 20f), vp.y(hy - 42f)
        )
        lineTo(vp.x(hx + 9f), vp.y(hy - 12f))
        close()
    }
    drawPath(leftEar, color = ink)
    drawPath(rightEar, color = ink)

    // Kulata hlava s tvickami a trup v okynku
    drawOval(
        color = ink,
        topLeft = vp.pt(hx - 18f, hy - 16f),
        size = Size(vp.s(36f), vp.s(28f))
    )
    drawOval(
        color = ink,
        topLeft = vp.pt(hx - 6f, hy - 8f),
        size = Size(vp.s(28f), vp.s(20f))
    )
    drawRoundRect(
        color = ink,
        topLeft = vp.pt(hx - 15f, hy + 10f),
        size = Size(vp.s(30f), vp.s(23f)),
        cornerRadius = CornerRadius(vp.s(7f), vp.s(7f))
    )

    // Oko, cumacek a limecek zajicka
    drawCircle(color = LcdBgColor, radius = vp.s(2.8f), center = vp.pt(hx + 6f, hy - 5f))
    drawCircle(color = ink, radius = vp.s(1.3f), center = vp.pt(hx + 6.5f, hy - 5f))
    drawCircle(color = LcdBgColor, radius = vp.s(2.2f), center = vp.pt(hx + 16f, hy - 1f))
    drawLine(
        color = LcdBgColor,
        start = vp.pt(hx - 10f, hy + 16f),
        end = vp.pt(hx + 10f, hy + 16f),
        strokeWidth = vp.s(2.4f)
    )

    // Horni a dolni ruka se zvoneckem
    val upInk = if (visible && bellUp) LcdInkOn else LcdInkGhost
    val downInk = if (visible && !bellUp) LcdInkOn else LcdInkGhost

    drawHareBellSegment(vp, hx, hy, isUp = true, ink = upInk)
    drawHareBellSegment(vp, hx, hy, isUp = false, ink = downInk)
}

private fun DrawScope.drawHareBellSegment(
    vp: LcdViewport,
    hx: Float,
    hy: Float,
    isUp: Boolean,
    ink: Color
) {
    val handX = hx + 36f
    val handY = if (isUp) hy - 12f else hy + 14f

    drawLine(
        color = ink,
        start = vp.pt(hx + 12f, hy + 14f),
        end = vp.pt(handX, handY),
        strokeWidth = vp.s(5.5f),
        cap = StrokeCap.Round
    )

    val bell = Path().apply {
        moveTo(vp.x(handX + 8f), vp.y(handY - 4f))
        lineTo(vp.x(handX - 2f), vp.y(handY + 12f))
        lineTo(vp.x(handX + 18f), vp.y(handY + 12f))
        close()
    }
    drawPath(bell, color = ink)
    drawCircle(color = ink, radius = vp.s(3f), center = vp.pt(handX + 8f, handY + 15f))
}

internal fun DrawScope.drawDetailedWolfAndBaskets(
    vp: LcdViewport,
    wolfPos: BasketPosition,
    celebrating: Boolean,
    blinkPhase: Boolean
) {
    val facingLeft = wolfPos == BasketPosition.TOP_LEFT || wolfPos == BasketPosition.BOTTOM_LEFT
    val leftWolfInk = if (facingLeft || (celebrating && blinkPhase)) LcdInkOn else LcdInkGhost
    val rightWolfInk = if (!facingLeft || (celebrating && !blinkPhase)) LcdInkOn else LcdInkGhost

    // Dve postoje vlka uprostred (otoceny vlevo a otoceny vpravo)
    drawWolfFigurePose(vp, cx = 432f, facingLeft = true, ink = leftWolfInk)
    drawWolfFigurePose(vp, cx = 568f, facingLeft = false, ink = rightWolfInk)

    // 4 polohy rukou a proutenych kosiku presne pod konci 4 ramp
    drawWolfArmAndBasket(
        vp = vp,
        shoulderX = 408f,
        shoulderY = 262f,
        elbowX = 366f,
        elbowY = 268f,
        basketX = 304f,
        basketY = 242f,
        ink = if (wolfPos == BasketPosition.TOP_LEFT) LcdInkOn else LcdInkGhost
    )
    drawWolfArmAndBasket(
        vp = vp,
        shoulderX = 406f,
        shoulderY = 284f,
        elbowX = 368f,
        elbowY = 344f,
        basketX = 304f,
        basketY = 402f,
        ink = if (wolfPos == BasketPosition.BOTTOM_LEFT) LcdInkOn else LcdInkGhost
    )
    drawWolfArmAndBasket(
        vp = vp,
        shoulderX = 592f,
        shoulderY = 262f,
        elbowX = 634f,
        elbowY = 268f,
        basketX = 696f,
        basketY = 242f,
        ink = if (wolfPos == BasketPosition.TOP_RIGHT) LcdInkOn else LcdInkGhost
    )
    drawWolfArmAndBasket(
        vp = vp,
        shoulderX = 594f,
        shoulderY = 284f,
        elbowX = 632f,
        elbowY = 344f,
        basketX = 696f,
        basketY = 402f,
        ink = if (wolfPos == BasketPosition.BOTTOM_RIGHT) LcdInkOn else LcdInkGhost
    )
}

private fun DrawScope.drawWolfFigurePose(
    vp: LcdViewport,
    cx: Float,
    facingLeft: Boolean,
    ink: Color
) {
    val dir = if (facingLeft) -1f else 1f

    // 1. Namornicka capka, spicate ucho a rozcuchana cuprina na zatylku
    val capAndHair = Path().apply {
        // Cuprina vzadu na krku
        moveTo(vp.x(cx - dir * 16f), vp.y(196f))
        lineTo(vp.x(cx - dir * 32f), vp.y(192f))
        lineTo(vp.x(cx - dir * 24f), vp.y(182f))
        lineTo(vp.x(cx - dir * 34f), vp.y(174f))
        lineTo(vp.x(cx - dir * 20f), vp.y(166f))
        // Zadni cast capky
        lineTo(vp.x(cx - dir * 24f), vp.y(148f))
        lineTo(vp.x(cx + dir * 4f), vp.y(142f))
        // Spicate ucho trcici nahoru
        lineTo(vp.x(cx + dir * 2f), vp.y(122f))
        lineTo(vp.x(cx + dir * 14f), vp.y(144f))
        // Ksilt capky vepredu
        lineTo(vp.x(cx + dir * 28f), vp.y(148f))
        lineTo(vp.x(cx + dir * 22f), vp.y(158f))
        lineTo(vp.x(cx - dir * 12f), vp.y(162f))
        close()
    }
    drawPath(capAndHair, color = ink)

    // Svetly prouzek na capce
    drawLine(
        color = LcdBgColor,
        start = vp.pt(cx - dir * 18f, 152f),
        end = vp.pt(cx + dir * 8f, 150f),
        strokeWidth = vp.s(2.8f),
        cap = StrokeCap.Round
    )

    // 2. Hlava vlka, dlouhy cumak, vyceneny zub a otevrena tlama
    val headAndSnout = Path().apply {
        moveTo(vp.x(cx - dir * 16f), vp.y(160f))
        lineTo(vp.x(cx + dir * 18f), vp.y(158f))
        // Nadocni oblouk a dlouhy cumak mirne nahoru
        lineTo(vp.x(cx + dir * 26f), vp.y(168f))
        lineTo(vp.x(cx + dir * 64f), vp.y(164f))
        lineTo(vp.x(cx + dir * 62f), vp.y(182f))
        // Horni pysk a spickaty zub
        lineTo(vp.x(cx + dir * 42f), vp.y(186f))
        lineTo(vp.x(cx + dir * 38f), vp.y(195f))
        lineTo(vp.x(cx + dir * 32f), vp.y(186f))
        lineTo(vp.x(cx + dir * 18f), vp.y(188f))
        // Spodni celist (otevrena tlama)
        lineTo(vp.x(cx + dir * 46f), vp.y(202f))
        lineTo(vp.x(cx + dir * 40f), vp.y(212f))
        lineTo(vp.x(cx + dir * 10f), vp.y(204f))
        // Krk
        lineTo(vp.x(cx + dir * 12f), vp.y(226f))
        lineTo(vp.x(cx - dir * 14f), vp.y(226f))
        close()
    }
    drawPath(headAndSnout, color = ink)

    // Velky cerny čenich na spicce cumaku a oko
    drawOval(
        color = ink,
        topLeft = vp.pt(cx + dir * 60f - 7f, 156f),
        size = Size(vp.s(14f), vp.s(11f))
    )
    drawCircle(
        color = LcdBgColor,
        radius = vp.s(4.2f),
        center = vp.pt(cx + dir * 12f, 170f)
    )
    drawCircle(
        color = ink,
        radius = vp.s(2.0f),
        center = vp.pt(cx + dir * 14f, 170f)
    )

    // 3. Trup vlka (namornicka kosile s limeckem a paskem, mirne prohnuta zada)
    val torso = Path().apply {
        moveTo(vp.x(cx - dir * 22f), vp.y(226f))
        lineTo(vp.x(cx + dir * 24f), vp.y(226f))
        // Hrudnik
        cubicTo(
            vp.x(cx + dir * 30f), vp.y(265f),
            vp.x(cx + dir * 24f), vp.y(305f),
            vp.x(cx + dir * 22f), vp.y(338f)
        )
        lineTo(vp.x(cx - dir * 22f), vp.y(338f))
        // Zada
        cubicTo(
            vp.x(cx - dir * 28f), vp.y(300f),
            vp.x(cx - dir * 28f), vp.y(260f),
            vp.x(cx - dir * 22f), vp.y(226f)
        )
        close()
    }
    drawPath(torso, color = ink)

    // Vystrih / limecek u krku a prouzky na triku
    val collar = Path().apply {
        moveTo(vp.x(cx - dir * 6f), vp.y(227f))
        lineTo(vp.x(cx + dir * 14f), vp.y(227f))
        lineTo(vp.x(cx + dir * 6f), vp.y(248f))
        close()
    }
    drawPath(collar, color = LcdBgColor)

    for (stripeY in listOf(266f, 288f, 310f)) {
        drawLine(
            color = LcdBgColor,
            start = vp.pt(cx - 21f, stripeY),
            end = vp.pt(cx + 21f, stripeY),
            strokeWidth = vp.s(3.2f)
        )
    }

    // Pasek s prezkou v pase (y = 334)
    drawLine(
        color = LcdBgColor,
        start = vp.pt(cx - 22f, 332f),
        end = vp.pt(cx + 22f, 332f),
        strokeWidth = vp.s(4.5f)
    )
    drawRect(
        color = ink,
        topLeft = vp.pt(cx + dir * 4f, 328f),
        size = Size(vp.s(10f), vp.s(9f))
    )

    // 4. Hunaty ocas vzadu
    val tail = Path().apply {
        moveTo(vp.x(cx - dir * 22f), vp.y(334f))
        cubicTo(
            vp.x(cx - dir * 52f), vp.y(328f),
            vp.x(cx - dir * 60f), vp.y(362f),
            vp.x(cx - dir * 38f), vp.y(372f)
        )
        cubicTo(
            vp.x(cx - dir * 26f), vp.y(370f),
            vp.x(cx - dir * 24f), vp.y(352f),
            vp.x(cx - dir * 20f), vp.y(344f)
        )
        close()
    }
    drawPath(tail, color = ink)

    // 5. Zvonove kalhoty a boty (rozsirene nohavice s mezerou mezi nohama)
    val legsAndShoes = Path().apply {
        moveTo(vp.x(cx - 22f), vp.y(338f))
        lineTo(vp.x(cx + 22f), vp.y(338f))
        // Prava vnejsi strana nohavice do zvonu
        lineTo(vp.x(cx + 34f), vp.y(442f))
        // Prava bota
        lineTo(vp.x(cx + 46f), vp.y(448f))
        lineTo(vp.x(cx + 46f), vp.y(458f))
        lineTo(vp.x(cx + 8f), vp.y(458f))
        lineTo(vp.x(cx + 8f), vp.y(442f))
        // Rozkrok
        lineTo(vp.x(cx), vp.y(374f))
        // Leva vnitrni strana nohavice
        lineTo(vp.x(cx - 8f), vp.y(442f))
        lineTo(vp.x(cx - 8f), vp.y(458f))
        // Leva bota
        lineTo(vp.x(cx - 46f), vp.y(458f))
        lineTo(vp.x(cx - 46f), vp.y(448f))
        lineTo(vp.x(cx - 34f), vp.y(442f))
        close()
    }
    drawPath(legsAndShoes, color = ink)
}

private fun DrawScope.drawWolfArmAndBasket(
    vp: LcdViewport,
    shoulderX: Float,
    shoulderY: Float,
    elbowX: Float,
    elbowY: Float,
    basketX: Float,
    basketY: Float,
    ink: Color
) {
    val handX = if (basketX < shoulderX) basketX + 26f else basketX - 26f
    val handY = basketY - 8f

    // Paze vlka s ohnutym loktem a pesti drzici kosik
    val armPath = Path().apply {
        moveTo(vp.x(shoulderX), vp.y(shoulderY))
        lineTo(vp.x(elbowX), vp.y(elbowY))
        lineTo(vp.x(handX), vp.y(handY))
    }
    drawPath(
        path = armPath,
        color = ink,
        style = Stroke(width = vp.s(11f), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawCircle(
        color = ink,
        radius = vp.s(7.5f),
        center = vp.pt(handX, handY)
    )

    // Zaobleny prouteny kosik
    val bw = 74f
    val bh = 42f
    val basketBody = Path().apply {
        moveTo(vp.x(basketX - bw * 0.50f), vp.y(basketY - bh * 0.35f))
        lineTo(vp.x(basketX + bw * 0.50f), vp.y(basketY - bh * 0.35f))
        cubicTo(
            vp.x(basketX + bw * 0.44f), vp.y(basketY + bh * 0.45f),
            vp.x(basketX + bw * 0.28f), vp.y(basketY + bh * 0.60f),
            vp.x(basketX), vp.y(basketY + bh * 0.60f)
        )
        cubicTo(
            vp.x(basketX - bw * 0.28f), vp.y(basketY + bh * 0.60f),
            vp.x(basketX - bw * 0.44f), vp.y(basketY + bh * 0.45f),
            vp.x(basketX - bw * 0.50f), vp.y(basketY - bh * 0.35f)
        )
        close()
    }
    drawPath(basketBody, color = ink)

    // Horni obruba kosiku a sikme proutene pleteni
    drawLine(
        color = LcdBgColor,
        start = vp.pt(basketX - bw * 0.42f, basketY - bh * 0.14f),
        end = vp.pt(basketX + bw * 0.42f, basketY - bh * 0.14f),
        strokeWidth = vp.s(2.4f),
        cap = StrokeCap.Round
    )
    for (offset in listOf(-16f, 0f, 16f)) {
        drawLine(
            color = LcdBgColor,
            start = vp.pt(basketX + offset - 6f, basketY - 2f),
            end = vp.pt(basketX + offset + 6f, basketY + 16f),
            strokeWidth = vp.s(2.2f),
            cap = StrokeCap.Round
        )
    }

    // Obloukove ucho kosiku
    drawArc(
        color = ink,
        startAngle = 195f,
        sweepAngle = 150f,
        useCenter = false,
        topLeft = vp.pt(basketX - bw * 0.36f, basketY - bh * 0.85f),
        size = Size(vp.s(bw * 0.72f), vp.s(bh * 0.85f)),
        style = Stroke(width = vp.s(3.8f), cap = StrokeCap.Round)
    )
}

internal fun DrawScope.drawPenaltyChicksRow(
    vp: LcdViewport,
    penaltyHalfPoints: Int,
    blinkPhase: Boolean
) {
    for (idx in 0..2) {
        val thresholdFull = (idx + 1) * 2
        val thresholdHalf = thresholdFull - 1
        val isLit = when {
            penaltyHalfPoints >= thresholdFull -> true
            penaltyHalfPoints == thresholdHalf -> blinkPhase
            else -> false
        }
        val ink = if (isLit) LcdSilkRed else LcdInkGhost
        val cx = 395f + idx * 55f
        val cy = 84f
        drawMiniChick(vp, cx, cy, facingRight = true, ink = ink)
    }
}

internal fun DrawScope.drawCrashAndRunningChicks(
    vp: LcdViewport,
    crashAnim: CrashAnimState?
) {
    for (side in 0..1) {
        val isLeft = side == 0
        val activeThisSide = crashAnim != null && crashAnim.leftSide == isLeft
        val eggInk = if (activeThisSide) LcdSilkRed else LcdInkGhost
        val baseX = if (isLeft) 285f else 715f
        val floorY = 472f

        // Rozbita skorapka s vytekajicim zloutkem na zemi
        val shell = Path().apply {
            moveTo(vp.x(baseX - 28f), vp.y(floorY + 4f))
            lineTo(vp.x(baseX - 18f), vp.y(floorY - 11f))
            lineTo(vp.x(baseX - 6f), vp.y(floorY - 3f))
            lineTo(vp.x(baseX + 2f), vp.y(floorY - 13f))
            lineTo(vp.x(baseX + 12f), vp.y(floorY - 3f))
            lineTo(vp.x(baseX + 22f), vp.y(floorY - 10f))
            lineTo(vp.x(baseX + 30f), vp.y(floorY + 4f))
            close()
        }
        drawPath(shell, color = eggInk)

        // 3 kroky utikajiciho kuratka ke kraji obrazovky
        for (step in 1..3) {
            val stepLit = activeThisSide && crashAnim?.frame == step
            val chickInk = if (stepLit) LcdInkOn else LcdInkGhost
            val dir = if (isLeft) -1f else 1f
            val runX = baseX + dir * step * 62f
            val runY = floorY - 14f
            drawMiniChick(vp, runX, runY, facingRight = !isLeft, ink = chickInk)
        }
    }
}

private fun DrawScope.drawMiniChick(
    vp: LcdViewport,
    cx: Float,
    cy: Float,
    facingRight: Boolean,
    ink: Color
) {
    val dir = if (facingRight) 1f else -1f
    drawOval(
        color = ink,
        topLeft = vp.pt(cx - 12f, cy - 7f),
        size = Size(vp.s(24f), vp.s(16f))
    )
    drawCircle(
        color = ink,
        radius = vp.s(8.5f),
        center = vp.pt(cx + dir * 8f, cy - 10f)
    )
    val beak = Path().apply {
        moveTo(vp.x(cx + dir * 15f), vp.y(cy - 13f))
        lineTo(vp.x(cx + dir * 23f), vp.y(cy - 10f))
        lineTo(vp.x(cx + dir * 15f), vp.y(cy - 7f))
        close()
    }
    drawPath(beak, color = ink)
    drawLine(
        color = ink,
        start = vp.pt(cx - 4f, cy + 8f),
        end = vp.pt(cx - 8f, cy + 18f),
        strokeWidth = vp.s(2.8f),
        cap = StrokeCap.Round
    )
    drawLine(
        color = ink,
        start = vp.pt(cx + 4f, cy + 8f),
        end = vp.pt(cx + 9f, cy + 18f),
        strokeWidth = vp.s(2.8f),
        cap = StrokeCap.Round
    )
}
