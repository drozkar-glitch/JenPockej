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

internal val LcdBgColor = Color(0xFFA3B091)
internal val LcdInkOn = Color(0xFF1A2117)
internal val LcdInkGhost = Color(0xFF909D80)
internal val LcdSilkRed = Color(0xFF8B231D)
internal val LcdSilkDark = Color(0xFF3A4434)

internal fun DrawScope.drawDetailedHen(
    origin: Offset,
    scale: Float,
    facingRight: Boolean
) {
    val dir = if (facingRight) 1f else -1f

    // Prouteny kosek (hnizdo) pod slepici
    val nestPath = Path().apply {
        moveTo(origin.x - dir * 18f * scale, origin.y + 6f * scale)
        lineTo(origin.x + dir * 16f * scale, origin.y + 6f * scale)
        lineTo(origin.x + dir * 11f * scale, origin.y + 16f * scale)
        lineTo(origin.x - dir * 13f * scale, origin.y + 16f * scale)
        close()
    }
    drawPath(nestPath, color = LcdSilkDark)

    // Telo slepice a ocasni pera
    val bodyPath = Path().apply {
        moveTo(origin.x - dir * 14f * scale, origin.y + 5f * scale)
        cubicTo(
            origin.x - dir * 20f * scale, origin.y - 6f * scale,
            origin.x - dir * 18f * scale, origin.y - 18f * scale,
            origin.x - dir * 10f * scale, origin.y - 13f * scale
        )
        cubicTo(
            origin.x - dir * 2f * scale, origin.y - 12f * scale,
            origin.x + dir * 4f * scale, origin.y - 18f * scale,
            origin.x + dir * 9f * scale, origin.y - 15f * scale
        )
        cubicTo(
            origin.x + dir * 16f * scale, origin.y - 11f * scale,
            origin.x + dir * 15f * scale, origin.y + 3f * scale,
            origin.x + dir * 8f * scale, origin.y + 6f * scale
        )
        close()
    }
    drawPath(bodyPath, color = LcdInkOn)

    // Hrebinek na hlave
    drawCircle(
        color = LcdSilkRed,
        radius = 3.2f * scale,
        center = Offset(origin.x + dir * 6f * scale, origin.y - 18f * scale)
    )
    drawCircle(
        color = LcdSilkRed,
        radius = 3.0f * scale,
        center = Offset(origin.x + dir * 10f * scale, origin.y - 17f * scale)
    )

    // Zobacek
    val beakPath = Path().apply {
        moveTo(origin.x + dir * 13f * scale, origin.y - 12f * scale)
        lineTo(origin.x + dir * 20f * scale, origin.y - 9f * scale)
        lineTo(origin.x + dir * 13f * scale, origin.y - 7f * scale)
        close()
    }
    drawPath(beakPath, color = LcdInkOn)

    // Oko slepice (svetly bod)
    drawCircle(
        color = LcdBgColor,
        radius = 1.6f * scale,
        center = Offset(origin.x + dir * 9.5f * scale, origin.y - 11.5f * scale)
    )
}

internal fun DrawScope.drawDetailedHare(
    w: Float,
    h: Float,
    visible: Boolean,
    bellUp: Boolean
) {
    val hx = w * 0.215f
    val hy = h * 0.165f
    val s = w * 0.00125f

    // Striska a okno chaloupky zajicka (staly potisk obrazovky)
    val roofPath = Path().apply {
        moveTo(hx - 48f * s, hy - 32f * s)
        lineTo(hx + 2f * s, hy - 52f * s)
        lineTo(hx + 54f * s, hy - 30f * s)
    }
    drawPath(
        path = roofPath,
        color = LcdSilkRed,
        style = Stroke(width = 3.2f * s, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawLine(
        color = LcdSilkDark,
        start = Offset(hx - 40f * s, hy + 26f * s),
        end = Offset(hx + 46f * s, hy + 26f * s),
        strokeWidth = 3f * s,
        cap = StrokeCap.Round
    )

    val hareInk = if (visible) LcdInkOn else LcdInkGhost

    // Dlouhe usi zajicka
    val leftEar = Path().apply {
        moveTo(hx - 8f * s, hy - 10f * s)
        cubicTo(
            hx - 18f * s, hy - 34f * s,
            hx - 6f * s, hy - 44f * s,
            hx - 2f * s, hy - 36f * s
        )
        cubicTo(
            hx + 1f * s, hy - 28f * s,
            hx - 2f * s, hy - 14f * s,
            hx - 4f * s, hy - 10f * s
        )
        close()
    }
    val rightEar = Path().apply {
        moveTo(hx + 2f * s, hy - 10f * s)
        cubicTo(
            hx + 2f * s, hy - 34f * s,
            hx + 14f * s, hy - 44f * s,
            hx + 16f * s, hy - 34f * s
        )
        cubicTo(
            hx + 16f * s, hy - 24f * s,
            hx + 8f * s, hy - 14f * s,
            hx + 6f * s, hy - 10f * s
        )
        close()
    }
    drawPath(leftEar, color = hareInk)
    drawPath(rightEar, color = hareInk)

    // Hlava a trup zajicka
    drawOval(
        color = hareInk,
        topLeft = Offset(hx - 14f * s, hy - 13f * s),
        size = Size(28f * s, 22f * s)
    )
    drawRoundRect(
        color = hareInk,
        topLeft = Offset(hx - 12f * s, hy + 7f * s),
        size = Size(24f * s, 18f * s),
        cornerRadius = CornerRadius(6f * s, 6f * s)
    )

    // Oko a usmev zajicka
    drawCircle(
        color = LcdBgColor,
        radius = 2.2f * s,
        center = Offset(hx + 5f * s, hy - 4f * s)
    )
    drawCircle(
        color = LcdBgColor,
        radius = 1.8f * s,
        center = Offset(hx + 11f * s, hy + 1f * s)
    )

    // Horni a dolni poloha tlapky se zvoneckem
    val upInk = if (visible && bellUp) LcdInkOn else LcdInkGhost
    val downInk = if (visible && !bellUp) LcdInkOn else LcdInkGhost

    drawHareBellArm(hx, hy, s, isUp = true, ink = upInk)
    drawHareBellArm(hx, hy, s, isUp = false, ink = downInk)
}

private fun DrawScope.drawHareBellArm(
    hx: Float,
    hy: Float,
    s: Float,
    isUp: Boolean,
    ink: Color
) {
    val armEndY = if (isUp) hy - 10f * s else hy + 14f * s
    val bellCenterX = hx + 32f * s
    val bellTopY = if (isUp) hy - 14f * s else hy + 10f * s

    drawLine(
        color = ink,
        start = Offset(hx + 10f * s, hy + 10f * s),
        end = Offset(bellCenterX - 4f * s, armEndY),
        strokeWidth = 4.2f * s,
        cap = StrokeCap.Round
    )

    val bellPath = Path().apply {
        moveTo(bellCenterX, bellTopY)
        lineTo(bellCenterX - 8f * s, bellTopY + 12f * s)
        lineTo(bellCenterX + 8f * s, bellTopY + 12f * s)
        close()
    }
    drawPath(bellPath, color = ink)
    drawCircle(
        color = ink,
        radius = 2.4f * s,
        center = Offset(bellCenterX, bellTopY + 14f * s)
    )
}

internal fun DrawScope.drawDetailedWolfAndBaskets(
    w: Float,
    h: Float,
    wolfPos: BasketPosition,
    celebrating: Boolean,
    blinkPhase: Boolean
) {
    val facingLeft = wolfPos == BasketPosition.TOP_LEFT || wolfPos == BasketPosition.BOTTOM_LEFT
    val leftWolfInk = if (facingLeft || (celebrating && blinkPhase)) LcdInkOn else LcdInkGhost
    val rightWolfInk = if (!facingLeft || (celebrating && !blinkPhase)) LcdInkOn else LcdInkGhost

    // Dve hlavni figury vlka (otoceny doleva a otoceny doprava) jako na originalnim LCD
    drawWolfCharacterPose(
        w = w,
        h = h,
        centerX = w * 0.435f,
        facingLeft = true,
        ink = leftWolfInk
    )
    drawWolfCharacterPose(
        w = w,
        h = h,
        centerX = w * 0.565f,
        facingLeft = false,
        ink = rightWolfInk
    )

    // 4 samostatne segmenty rukou s proutenym kosikem
    drawWolfArmWithBasket(
        w = w,
        h = h,
        shoulder = Offset(w * 0.405f, h * 0.53f),
        basketCenter = Offset(w * 0.295f, h * 0.43f),
        ink = if (wolfPos == BasketPosition.TOP_LEFT) LcdInkOn else LcdInkGhost
    )
    drawWolfArmWithBasket(
        w = w,
        h = h,
        shoulder = Offset(w * 0.405f, h * 0.57f),
        basketCenter = Offset(w * 0.295f, h * 0.72f),
        ink = if (wolfPos == BasketPosition.BOTTOM_LEFT) LcdInkOn else LcdInkGhost
    )
    drawWolfArmWithBasket(
        w = w,
        h = h,
        shoulder = Offset(w * 0.595f, h * 0.53f),
        basketCenter = Offset(w * 0.705f, h * 0.43f),
        ink = if (wolfPos == BasketPosition.TOP_RIGHT) LcdInkOn else LcdInkGhost
    )
    drawWolfArmWithBasket(
        w = w,
        h = h,
        shoulder = Offset(w * 0.595f, h * 0.57f),
        basketCenter = Offset(w * 0.705f, h * 0.72f),
        ink = if (wolfPos == BasketPosition.BOTTOM_RIGHT) LcdInkOn else LcdInkGhost
    )
}

private fun DrawScope.drawWolfCharacterPose(
    w: Float,
    h: Float,
    centerX: Float,
    facingLeft: Boolean,
    ink: Color
) {
    val dir = if (facingLeft) -1f else 1f
    val s = w * 0.00125f
    val headY = h * 0.40f

    // Namornicka cepice a spicaty slech
    val capAndEarPath = Path().apply {
        moveTo(centerX - dir * 14f * s, headY - 12f * s)
        lineTo(centerX - dir * 4f * s, headY - 26f * s)
        lineTo(centerX + dir * 12f * s, headY - 22f * s)
        lineTo(centerX + dir * 20f * s, headY - 14f * s)
        lineTo(centerX + dir * 6f * s, headY - 11f * s)
        // Spicate ucho
        lineTo(centerX - dir * 4f * s, headY - 34f * s)
        lineTo(centerX - dir * 10f * s, headY - 12f * s)
        close()
    }
    drawPath(capAndEarPath, color = ink)

    // Hlava vlka, dlouhy cumak, nos a pootevrena tlama
    val headAndSnoutPath = Path().apply {
        moveTo(centerX - dir * 12f * s, headY - 12f * s)
        lineTo(centerX + dir * 12f * s, headY - 12f * s)
        // Horni linie cumaku
        lineTo(centerX + dir * 38f * s, headY - 6f * s)
        lineTo(centerX + dir * 36f * s, headY + 2f * s)
        // Horni celist
        lineTo(centerX + dir * 16f * s, headY + 4f * s)
        // Spodni celist (otevrena tlama)
        lineTo(centerX + dir * 30f * s, headY + 11f * s)
        lineTo(centerX + dir * 24f * s, headY + 15f * s)
        lineTo(centerX + dir * 8f * s, headY + 11f * s)
        lineTo(centerX - dir * 10f * s, headY + 12f * s)
        close()
    }
    drawPath(headAndSnoutPath, color = ink)

    // Kulata spicka nosu a oko
    drawCircle(
        color = ink,
        radius = 4.2f * s,
        center = Offset(centerX + dir * 39f * s, headY - 6f * s)
    )
    drawCircle(
        color = LcdBgColor,
        radius = 2.4f * s,
        center = Offset(centerX + dir * 8f * s, headY - 5f * s)
    )

    // Trup vlka (namornicke pruhovane triko)
    val torsoTop = headY + 12f * s
    val waistY = h * 0.65f
    val torsoPath = Path().apply {
        moveTo(centerX - dir * 12f * s, torsoTop)
        lineTo(centerX + dir * 13f * s, torsoTop)
        lineTo(centerX + dir * 16f * s, waistY)
        lineTo(centerX - dir * 15f * s, waistY)
        close()
    }
    drawPath(torsoPath, color = ink)

    // Svetle prouzky na triku vlka
    for (stripeIdx in 1..3) {
        val sy = torsoTop + (waistY - torsoTop) * (stripeIdx * 0.23f)
        drawLine(
            color = LcdBgColor,
            start = Offset(centerX - 13f * s, sy),
            end = Offset(centerX + 13f * s, sy),
            strokeWidth = 2.4f * s
        )
    }

    // Hunaty ocas vlka vzadu
    val tailPath = Path().apply {
        moveTo(centerX - dir * 13f * s, waistY - 4f * s)
        cubicTo(
            centerX - dir * 30f * s, waistY - 2f * s,
            centerX - dir * 34f * s, waistY + 14f * s,
            centerX - dir * 22f * s, waistY + 18f * s
        )
        cubicTo(
            centerX - dir * 16f * s, waistY + 16f * s,
            centerX - dir * 14f * s, waistY + 8f * s,
            centerX - dir * 12f * s, waistY + 4f * s
        )
        close()
    }
    drawPath(tailPath, color = ink)

    // Kalhoty do zvonu a boty
    val footY = h * 0.86f
    val legsPath = Path().apply {
        moveTo(centerX - 15f * s, waistY + 1f * s)
        lineTo(centerX + 15f * s, waistY + 1f * s)
        lineTo(centerX + 21f * s, footY - 4f * s)
        lineTo(centerX + 26f * s, footY)
        lineTo(centerX + 4f * s, footY)
        lineTo(centerX, waistY + 16f * s)
        lineTo(centerX - 4f * s, footY)
        lineTo(centerX - 26f * s, footY)
        lineTo(centerX - 21f * s, footY - 4f * s)
        close()
    }
    drawPath(legsPath, color = ink)
}

private fun DrawScope.drawWolfArmWithBasket(
    w: Float,
    h: Float,
    shoulder: Offset,
    basketCenter: Offset,
    ink: Color
) {
    val s = w * 0.00125f
    val elbow = Offset(
        x = (shoulder.x + basketCenter.x) * 0.5f,
        y = (shoulder.y + basketCenter.y) * 0.5f + 8f * s
    )

    // Paze vlka
    drawLine(
        color = ink,
        start = shoulder,
        end = elbow,
        strokeWidth = 6.5f * s,
        cap = StrokeCap.Round
    )
    drawLine(
        color = ink,
        start = elbow,
        end = Offset(basketCenter.x, basketCenter.y - 6f * s),
        strokeWidth = 6.0f * s,
        cap = StrokeCap.Round
    )

    // Prouteny kosik
    val bw = w * 0.082f
    val bh = h * 0.075f
    val basketPath = Path().apply {
        moveTo(basketCenter.x - bw * 0.52f, basketCenter.y - bh * 0.35f)
        lineTo(basketCenter.x + bw * 0.52f, basketCenter.y - bh * 0.35f)
        lineTo(basketCenter.x + bw * 0.34f, basketCenter.y + bh * 0.55f)
        lineTo(basketCenter.x - bw * 0.34f, basketCenter.y + bh * 0.55f)
        close()
    }
    drawPath(basketPath, color = ink)

    // Vzorek prouti na kosiku
    drawLine(
        color = LcdBgColor,
        start = Offset(basketCenter.x - bw * 0.36f, basketCenter.y + bh * 0.08f),
        end = Offset(basketCenter.x + bw * 0.36f, basketCenter.y + bh * 0.08f),
        strokeWidth = 2f * s
    )
    drawLine(
        color = LcdBgColor,
        start = Offset(basketCenter.x - bw * 0.16f, basketCenter.y - bh * 0.25f),
        end = Offset(basketCenter.x - bw * 0.12f, basketCenter.y + bh * 0.45f),
        strokeWidth = 2f * s
    )
    drawLine(
        color = LcdBgColor,
        start = Offset(basketCenter.x + bw * 0.16f, basketCenter.y - bh * 0.25f),
        end = Offset(basketCenter.x + bw * 0.12f, basketCenter.y + bh * 0.45f),
        strokeWidth = 2f * s
    )

    // Ucho kosiku
    drawArc(
        color = ink,
        startAngle = 190f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(basketCenter.x - bw * 0.36f, basketCenter.y - bh * 0.90f),
        size = Size(bw * 0.72f, bh * 0.90f),
        style = Stroke(width = 3.2f * s)
    )
}

internal fun DrawScope.drawPenaltyChicksRow(
    w: Float,
    h: Float,
    penaltyHalfPoints: Int,
    blinkPhase: Boolean
) {
    val s = w * 0.00125f
    for (idx in 0..2) {
        val thresholdFull = (idx + 1) * 2
        val thresholdHalf = thresholdFull - 1
        val isLit = when {
            penaltyHalfPoints >= thresholdFull -> true
            penaltyHalfPoints == thresholdHalf -> blinkPhase
            else -> false
        }
        val ink = if (isLit) LcdSilkRed else LcdInkGhost
        val cx = w * (0.39f + idx * 0.062f)
        val cy = h * 0.145f

        drawMiniChickFigure(Offset(cx, cy), s, facingRight = true, ink = ink)
    }
}

internal fun DrawScope.drawCrashAndRunningChicks(
    w: Float,
    h: Float,
    crashAnim: CrashAnimState?
) {
    val s = w * 0.00125f
    for (side in 0..1) {
        val isLeft = side == 0
        val activeThisSide = crashAnim != null && crashAnim.leftSide == isLeft
        val eggInk = if (activeThisSide) LcdSilkRed else LcdInkGhost
        val baseX = if (isLeft) w * 0.25f else w * 0.75f
        val floorY = h * 0.89f

        // Rozbita skorapka a rozlity zloutek
        val splashPath = Path().apply {
            moveTo(baseX - 24f * s, floorY + 4f * s)
            lineTo(baseX - 14f * s, floorY - 8f * s)
            lineTo(baseX - 4f * s, floorY - 2f * s)
            lineTo(baseX + 2f * s, floorY - 10f * s)
            lineTo(baseX + 12f * s, floorY - 2f * s)
            lineTo(baseX + 22f * s, floorY - 8f * s)
            lineTo(baseX + 26f * s, floorY + 4f * s)
            close()
        }
        drawPath(splashPath, color = eggInk)

        // 3 faze utikajiciho kuratka smerem ke kraji obrazovky
        for (step in 1..3) {
            val stepLit = activeThisSide && crashAnim?.frame == step
            val chickInk = if (stepLit) LcdInkOn else LcdInkGhost
            val dir = if (isLeft) -1f else 1f
            val runX = baseX + dir * step * (w * 0.056f)
            val runY = floorY - h * 0.035f

            drawMiniChickFigure(
                center = Offset(runX, runY),
                s = s * 0.92f,
                facingRight = !isLeft,
                ink = chickInk
            )
        }
    }
}

private fun DrawScope.drawMiniChickFigure(
    center: Offset,
    s: Float,
    facingRight: Boolean,
    ink: Color
) {
    val dir = if (facingRight) 1f else -1f
    // Telicko a hlava kuratka
    drawOval(
        color = ink,
        topLeft = Offset(center.x - 10f * s, center.y - 6f * s),
        size = Size(20f * s, 14f * s)
    )
    drawCircle(
        color = ink,
        radius = 7f * s,
        center = Offset(center.x + dir * 7f * s, center.y - 8f * s)
    )
    // Zobacek
    val beak = Path().apply {
        moveTo(center.x + dir * 13f * s, center.y - 10f * s)
        lineTo(center.x + dir * 19f * s, center.y - 8f * s)
        lineTo(center.x + dir * 13f * s, center.y - 6f * s)
        close()
    }
    drawPath(beak, color = ink)
    // Nozicky
    drawLine(
        color = ink,
        start = Offset(center.x - 3f * s, center.y + 7f * s),
        end = Offset(center.x - 6f * s, center.y + 15f * s),
        strokeWidth = 2.4f * s
    )
    drawLine(
        color = ink,
        start = Offset(center.x + 4f * s, center.y + 7f * s),
        end = Offset(center.x + 8f * s, center.y + 15f * s),
        strokeWidth = 2.4f * s
    )
}
