package cz.mares.jenpockej

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class LeaderboardEntry(
    val nick: String,
    val score: Int,
    val deviceId: String
)

class OnlineScoreManager(private val media: SoundPlayer) {
    companion object {
        private const val BASE_URL = "https://mares-games-default-rtdb.europe-west1.firebasedatabase.app"
        private const val KEY_DEVICE_ID = "online_device_id"
        private const val KEY_PLAYER_NICK = "online_player_nick"
    }

    fun getOrCreateDeviceId(): String {
        val existing = media.loadString(KEY_DEVICE_ID, "")
        if (existing.length >= 10) return existing
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val sb = StringBuilder("dev_")
        repeat(20) {
            sb.append(chars[Random.nextInt(chars.length)])
        }
        val generated = sb.toString()
        media.saveString(KEY_DEVICE_ID, generated)
        return generated
    }

    fun getSavedNick(): String = media.loadString(KEY_PLAYER_NICK, "").trim()

    fun firebaseNodeFor(game: ClassicGameId, mode: GameMode): String {
        val base = when (game) {
            ClassicGameId.JEN_POCKEJ -> "jen_pockej"
            ClassicGameId.TAJEMSTVI_OCEANU -> "tajemstvi_oceanu"
            ClassicGameId.VESELY_KUCHAR -> "vesely_kuchar"
            ClassicGameId.KOSMICKY_MOST -> "kosmicky_most"
            ClassicGameId.HOKEJ -> "vesely_hokej"
            ClassicGameId.AUTOSLALOM -> "autoslalom"
        }
        val suffix = if (mode == GameMode.GAME_A) "a" else "b"
        return "${base}_${suffix}"
    }

    fun sanitizeNick(raw: String): String {
        val allowed = StringBuilder()
        for (ch in raw.trim()) {
            if (ch.isLetterOrDigit() || ch == '_' || ch == '-') {
                allowed.append(ch)
            } else if (ch == ' ' && allowed.isNotEmpty() && allowed.last() != '_') {
                allowed.append('_')
            }
        }
        return allowed.toString().trim('_').take(14)
    }

    fun fetchTopScores(
        game: ClassicGameId,
        mode: GameMode,
        includeZeroScores: Boolean = false,
        onResult: (Boolean, List<LeaderboardEntry>) -> Unit
    ) {
        val node = firebaseNodeFor(game, mode)
        val url = "$BASE_URL/high_scores/$node.json"
        media.httpGet(url) { ok, body ->
            if (!ok) {
                onResult(false, emptyList())
                return@httpGet
            }
            val parsed = parseFirebaseScoresJson(body)
                .filter { includeZeroScores || it.score > 0 }
                .sortedByDescending { it.score }
                .take(50)
            onResult(true, parsed)
        }
    }

    fun registerNickAndSync(
        rawNick: String,
        currentGame: ClassicGameId,
        currentMode: GameMode,
        allLocalScores: List<Triple<ClassicGameId, GameMode, Int>>,
        onComplete: (Boolean, String) -> Unit
    ) {
        val cleanNick = sanitizeNick(rawNick)
        if (cleanNick.length < 2) {
            onComplete(false, "Zadej přezdívku o délce 2 až 14 znaků (písmena a čísla).")
            return
        }

        val myDeviceId = getOrCreateDeviceId()
        val checkNode = firebaseNodeFor(currentGame, currentMode)
        val checkUrl = "$BASE_URL/high_scores/$checkNode/$cleanNick.json"

        media.httpGet(checkUrl) { getOk, body ->
            if (!getOk) {
                onComplete(false, "Nelze se spojit se serverem rekordů. Zkontroluj připojení.")
                return@httpGet
            }

            val trimmed = body.trim()
            if (trimmed.isNotEmpty() && trimmed != "null") {
                val existingDev = extractStringField(trimmed, "deviceId")
                if (existingDev.isNotEmpty() && existingDev != myDeviceId) {
                    onComplete(false, "Přezdívka '$cleanNick' už je obsazená jiným hráčem. Zvol jinou.")
                    return@httpGet
                }
            }

            val existingScore = if (trimmed != "null") extractIntField(trimmed, "score") else 0
            val localForCurrent = allLocalScores
                .firstOrNull { it.first == currentGame && it.second == currentMode }
                ?.third ?: 0
            val scoreToWrite = maxOf(existingScore, localForCurrent)
            val payload = """{"score":$scoreToWrite,"deviceId":"$myDeviceId"}"""

            media.httpPut(checkUrl, payload) { putOk, code, _ ->
                if (!putOk || code == 401 || code == 403) {
                    onComplete(false, "Přezdívka '$cleanNick' je již obsazena. Zvol prosím jinou.")
                    return@httpPut
                }
                media.saveString(KEY_PLAYER_NICK, cleanNick)
                for ((g, m, sc) in allLocalScores) {
                    if (sc > 0) {
                        submitScoreQuietly(g, m, sc)
                    }
                }
                onComplete(true, "Přezdívka '$cleanNick' byla uložena a uzamčena!")
            }
        }
    }

    fun submitScoreQuietly(game: ClassicGameId, mode: GameMode, score: Int) {
        if (score <= 0) return
        val nick = getSavedNick()
        if (nick.length < 2) return
        val myDeviceId = getOrCreateDeviceId()
        val node = firebaseNodeFor(game, mode)
        val url = "$BASE_URL/high_scores/$node/$nick.json"

        media.httpGet(url) { ok, body ->
            if (!ok) return@httpGet
            val trimmed = body.trim()
            if (trimmed.isNotEmpty() && trimmed != "null") {
                val owner = extractStringField(trimmed, "deviceId")
                val remoteScore = extractIntField(trimmed, "score")
                if (owner.isNotEmpty() && owner != myDeviceId) return@httpGet
                if (score <= remoteScore) return@httpGet
            }
            val payload = """{"score":$score,"deviceId":"$myDeviceId"}"""
            media.httpPut(url, payload) { _, _, _ -> }
        }
    }

    private fun parseFirebaseScoresJson(json: String): List<LeaderboardEntry> {
        val text = json.trim()
        if (text.isEmpty() || text == "null" || !text.startsWith("{")) return emptyList()
        val result = ArrayList<LeaderboardEntry>()

        var i = 1
        val n = text.length
        while (i < n) {
            val keyStart = text.indexOf('"', i)
            if (keyStart < 0) break
            val keyEnd = text.indexOf('"', keyStart + 1)
            if (keyEnd < 0) break
            val nick = text.substring(keyStart + 1, keyEnd)

            val braceStart = text.indexOf('{', keyEnd)
            if (braceStart < 0) break
            val braceEnd = text.indexOf('}', braceStart)
            if (braceEnd < 0) break

            val objStr = text.substring(braceStart, braceEnd + 1)
            val scoreVal = extractIntField(objStr, "score")
            val devVal = extractStringField(objStr, "deviceId")
            result.add(LeaderboardEntry(nick = nick, score = scoreVal, deviceId = devVal))

            i = braceEnd + 1
        }
        return result
    }

    private fun extractIntField(obj: String, field: String): Int {
        val token = "\"$field\""
        val idx = obj.indexOf(token)
        if (idx < 0) return 0
        val colon = obj.indexOf(':', idx + token.length)
        if (colon < 0) return 0
        var p = colon + 1
        while (p < obj.length && (obj[p] == ' ' || obj[p] == '"')) p++
        val start = p
        while (p < obj.length && (obj[p].isDigit() || obj[p] == '-')) p++
        return obj.substring(start, p).toIntOrNull() ?: 0
    }

    private fun extractStringField(obj: String, field: String): String {
        val token = "\"$field\""
        val idx = obj.indexOf(token)
        if (idx < 0) return ""
        val colon = obj.indexOf(':', idx + token.length)
        if (colon < 0) return ""
        val firstQuote = obj.indexOf('"', colon + 1)
        if (firstQuote < 0) return ""
        val secondQuote = obj.indexOf('"', firstQuote + 1)
        if (secondQuote < 0) return ""
        return obj.substring(firstQuote + 1, secondQuote)
    }
}

@Composable
fun LeaderboardScreen(
    scoreManager: OnlineScoreManager,
    initialGame: ClassicGameId = ClassicGameId.JEN_POCKEJ,
    initialMode: GameMode = GameMode.GAME_A,
    localBestProvider: (ClassicGameId, GameMode) -> Int,
    allLocalScores: List<Triple<ClassicGameId, GameMode, Int>>,
    onBack: () -> Unit
) {
    var selectedGame by remember { mutableStateOf(initialGame) }
    var selectedMode by remember { mutableStateOf(initialMode) }
    var savedNick by remember { mutableStateOf(scoreManager.getSavedNick()) }
    var nickInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorStatus by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val personalBest = localBestProvider(selectedGame, selectedMode)

    LaunchedEffect(selectedGame, selectedMode, refreshTrigger) {
        isLoading = true
        if (savedNick.isNotBlank() && personalBest > 0) {
            scoreManager.submitScoreQuietly(selectedGame, selectedMode, personalBest)
        }
        scoreManager.fetchTopScores(
            game = selectedGame,
            mode = selectedMode,
            includeZeroScores = false
        ) { ok, list ->
            isLoading = false
            entries = list
            if (!ok) {
                isErrorStatus = true
                statusMessage = "Nepodařilo se načíst online žebříček. Zkontroluj internet."
            }
        }
    }

    val outerBrush = Brush.verticalGradient(
        listOf(Color(0xFF23272A), Color(0xFF181B1E))
    )
    val plateBrush = Brush.verticalGradient(
        listOf(Color(0xFFE4E1D8), Color(0xFFD6D3C9), Color(0xFFC8C4B9))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerBrush)
            .padding(5.dp)
            .background(plateBrush, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF8C887D), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. HORNÍ LIŠTA: Vlevo nadpis ONLINE REKORDY (TOP 50) s weight(1f), vpravo pouze OBNOVIT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ONLINE REKORDY (TOP 50) • ${selectedGame.code} ${selectedGame.titleCz.uppercase()}",
                    color = Color(0xFFB52B24),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2B302C))
                        .border(1.5.dp, Color(0xFFB52B24), RoundedCornerShape(6.dp))
                        .clickable {
                            statusMessage = null
                            refreshTrigger++
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OBNOVIT",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // 2. PŘEPÍNAČ VŠECH 6 HER (ИМ-02 až ИМ-23) A KATEGORIE (HRA A / HRA B)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (game in ClassicGameId.entries) {
                        val isSel = game == selectedGame
                        Box(
                            modifier = Modifier
                                .height(25.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFFB52B24) else Color(0xFFC8C4B8))
                                .border(
                                    1.2.dp,
                                    if (isSel) Color(0xFF7A1510) else Color(0xFF8C887E),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedGame = game }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = game.code,
                                color = if (isSel) Color.White else Color(0xFF232724),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (mode in GameMode.entries) {
                        val isSel = mode == selectedMode
                        Box(
                            modifier = Modifier
                                .height(25.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF1F5D78) else Color(0xFFC8C4B8))
                                .border(
                                    1.2.dp,
                                    if (isSel) Color(0xFF123B4D) else Color(0xFF8C887E),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedMode = mode }
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSel) Color.White else Color(0xFF232724),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // 3. PANEL HRÁČE: Zobrazí zadávací pole POUZE pokud nick ještě není uložen
            if (savedNick.isBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFD5D1C5))
                        .border(1.2.dp, Color(0xFF8C887D), RoundedCornerShape(7.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TVOJE PŘEZDÍVKA:",
                        color = Color(0xFF232724),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFF706D65), RoundedCornerShape(5.dp))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (nickInput.isEmpty()) {
                            Text(
                                text = "Zadej nick (2–14 znaků)...",
                                color = Color(0xFF888888),
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        BasicTextField(
                            value = nickInput,
                            onValueChange = { if (it.length <= 14) nickInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF161913),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            cursorBrush = SolidColor(Color(0xFFB52B24)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFD62828))
                            .border(1.dp, Color(0xFF7A1510), RoundedCornerShape(5.dp))
                            .clickable {
                                scoreManager.registerNickAndSync(
                                    rawNick = nickInput,
                                    currentGame = selectedGame,
                                    currentMode = selectedMode,
                                    allLocalScores = allLocalScores
                                ) { ok, msg ->
                                    isErrorStatus = !ok
                                    statusMessage = msg
                                    if (ok) {
                                        savedNick = scoreManager.getSavedNick()
                                        refreshTrigger++
                                    }
                                }
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ULOŽIT",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFD5D1C5))
                        .border(1.dp, Color(0xFF8C887D), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HRÁČ: $savedNick",
                        color = Color(0xFF1A1D1A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "OSOBNÍ (${selectedMode.label}): $personalBest",
                        color = Color(0xFFB52B24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            if (statusMessage != null) {
                Text(
                    text = statusMessage!!,
                    color = if (isErrorStatus) Color(0xFFB52B24) else Color(0xFF285928),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }

            // 4. TABULKA TOP 50 V LCD STYLU
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LcdBgColor)
                    .border(2.dp, Color(0xFF4A4E46), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "NAČÍTÁM ONLINE ŽEBŘÍČEK...",
                            color = LcdInkOn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (entries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "ZATÍM ŽÁDNÝ ZÁZNAM V TÉTO KATEGORII • BUĎ PRVNÍ!",
                            color = LcdInkOn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        itemsIndexed(entries) { idx, item ->
                            val isMe = savedNick.isNotBlank() &&
                                item.nick.equals(savedNick, ignoreCase = true)
                            val rowBg = if (isMe) Color(0x33B52B24) else Color(0x14000000)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(rowBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${idx + 1}.",
                                    color = if (isMe) Color(0xFFB52B24) else LcdInkOn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(
                                    text = item.nick,
                                    color = if (isMe) Color(0xFFB52B24) else LcdInkOn,
                                    fontSize = 11.sp,
                                    fontWeight = if (isMe) FontWeight.Black else FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${item.score} B.",
                                    color = if (isMe) Color(0xFFB52B24) else LcdInkOn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // 5. SPODNÍ LIŠTA: Tlačítko ZPĚT DO MENU + copyright
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFD62828), Color(0xFFB52B24))
                            )
                        )
                        .border(1.dp, Color(0xFF7A1510), RoundedCornerShape(6.dp))
                        .clickable(onClick = onBack)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◀  ZPĚT DO MENU",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "© 2026 Created by Pavel Mareš",
                    color = Color(0xFF232724),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
