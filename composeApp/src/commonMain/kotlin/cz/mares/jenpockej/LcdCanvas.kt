package cz.mares.jenpockej

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
        // Autenticke svetle sedozelene LCD pozadi jako na fotce ИМ-02
        drawRect(color = LcdBgColor, size = size)

        // Jemny horni stin zapusteneho sklicka
        drawRect(
            color = LcdShadowTint,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height * 0.03f)
        )

        val vp = LcdViewport(size.width, size.height)

        // 1. Barevny potisk sklicka (cervena strecha, hneda bidylka, plot, zelen, 4 bile slepice)
        drawStaticSceneryAndHens(vp)

        // 2. Zajicek vyklanejici se z okenka se zvoneckem
        drawHareInWindow(
            vp = vp,
            visible = snapshot.hareVisible,
            bellUp = snapshot.hareBellUp
        )

        // 3. Ctyrmistne skore 0000 a primo pod nim 3 trestna kuratka
        drawScoreAndPenaltyChicks(
            vp = vp,
            score = snapshot.score,
            penaltyHalfPoints = snapshot.penaltyHalfPoints,
            blinkPhase = snapshot.blinkPhase
        )

        // 4. Dve kreslene postavy vlka uprostred a 4 polohy rukou s kosikem
        drawCartoonWolfPosesAndBaskets(
            vp = vp,
            wolfPos = snapshot.wolfPosition,
            celebrating = snapshot.celebrationTicks > 0,
            blinkPhase = snapshot.blinkPhase
        )

        // 5. Obrysova vajicka se svetlym stredem kutalejici se po bidylkach
        drawAuthenticHollowEggs(
            vp = vp,
            activeEggs = snapshot.eggs
        )

        // 6. Hvezdice rozbiteho vajicka a 4 faze utikajicich kuratek dole v trave + napisy GAME A / GAME B
        drawBrokenEggAndRunningChicks(
            vp = vp,
            crashAnim = snapshot.crashAnim,
            mode = snapshot.mode,
            isRunning = snapshot.isRunning
        )
    }
}
