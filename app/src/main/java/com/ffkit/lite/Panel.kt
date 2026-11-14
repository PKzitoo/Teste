package com.ffkit.lite

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ffkit v19-maxpack — TODAS as features no painel, cada uma com badge
 * de risco pra tu saber. Deteccao tecnica eh minima (Shamiko esconde);
 * ban vem de uso abusivo + report. Tu controla.
 *
 * Tiers:
 *   🟢 S  safe/invisivel  — pode deixar on sempre
 *   🟡 M  medio/visivel   — usa com cautela
 *   🔴 R  rage/magneto    — so em sala privada / se voce quer banir
 */
@Composable
fun Panel(ctx: Context, onMinimize: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Surface(
        modifier = Modifier.width(230.dp),
        color = Neon.bg,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Neon.stroke),
    ) {
        Column {
            Header(onMinimize)
            Row {
                Sidebar(tab) { tab = it }
                Content(ctx, tab)
            }
            Footer()
        }
    }
}

// ============ HEADER ============

@Composable private fun Header(onMinimize: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = 32.dp)
            .background(Brush.horizontalGradient(
                listOf(Neon.accent2, Neon.accent)))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(Neon.accent3))
        Spacer(Modifier.width(7.dp))
        Text("FFKIT", color = Color.White, fontSize = 11.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.width(5.dp))
        Text("v19-max", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp)
        Spacer(Modifier.weight(1f))
        Text("⋮⋮", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        Spacer(Modifier.width(8.dp))
        HeaderBtn("_") { onMinimize() }
        Spacer(Modifier.width(4.dp))
        HeaderBtn("X") { OverlayController.hide() }
    }
}

@Composable private fun HeaderBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .clickable { try { onClick() } catch (_: Throwable) {} },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 10.sp,
            fontWeight = FontWeight.Bold)
    }
}

// ============ SIDEBAR (5 tabs) ============

@Composable private fun Sidebar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        "A" to "AIM",
        "E" to "ESP",
        "W" to "WEAP",
        "M" to "MOVE",
        "I" to "INTL",
        "+" to "SAFE"
    )
    Column(
        Modifier.width(38.dp)
            .background(Neon.sidebar)
            .padding(vertical = 4.dp, horizontal = 3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { i, (icon, label) ->
            SideIcon(icon, label, selected == i) { onSelect(i) }
        }
    }
}

@Composable private fun SideIcon(icon: String, label: String,
                                 selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Neon.accent else Neon.glass
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(32.dp)
    ) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(5.dp))
                .background(bg)
                .border(1.dp,
                    if (selected) Neon.accent3 else Neon.stroke.copy(alpha = 0.3f),
                    RoundedCornerShape(5.dp))
                .clickable { try { onClick() } catch (_: Throwable) {} },
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = Color.White, fontSize = 12.sp,
                fontWeight = FontWeight.Black)
        }
        Text(label, color = if (selected) Neon.accent3 else Neon.dim,
            fontSize = 6.sp, fontWeight = FontWeight.Bold)
    }
}

// ============ CONTENT ============

@Composable private fun RowScope.Content(ctx: Context, tab: Int) {
    Column(
        Modifier.weight(1f).heightIn(max = 340.dp)
            .background(Neon.panel)
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (tab) {
            0 -> AimTab()
            1 -> EspTab()
            2 -> WeaponTab()
            3 -> MoveTab()
            4 -> IntelTab()
            5 -> SafeTab(ctx)
        }
    }
}

// ============ TAB: AIM ============

@Composable private fun AimTab() {
    Section("AIMBOT", tier = T.S) {
        TierSw("Ativar aimbot",    T.S, StateBridge.aimEnabled)    { StateBridge.aimEnabled = it }
        TierSw("Só atirando",      T.S, StateBridge.aimOnFire)     { StateBridge.aimOnFire = it }
        TierSw("Só mirando (ADS)", T.S, StateBridge.aimOnScope)    { StateBridge.aimOnScope = it }
        TierSw("Ignorar bots",     T.S, StateBridge.aimIgnoreBots) { StateBridge.aimIgnoreBots = it }
        TierSw("Prediction",       T.S, StateBridge.aimPrediction) { StateBridge.aimPrediction = it }
        TierSw("Multi-target",     T.M, StateBridge.aimMultiTarget){ StateBridge.aimMultiTarget = it }
        TierSw("Silent aim",       T.R, StateBridge.aimSilent)     { StateBridge.aimSilent = it }
    }
    Section("MIRA", tier = T.S) {
        Sl("Aim FOV",   30f..600f, StateBridge.aimFov)    { StateBridge.aimFov = it }
        Sl("Suavidade", 1f..10f,   StateBridge.aimSmooth) { StateBridge.aimSmooth = it }
        Sl("Reação ms", 50f..500f, StateBridge.aimReactionMs.toFloat()) {
            StateBridge.aimReactionMs = it.toInt()
        }
    }
    Section("PADRÃO DE TIRO", tier = T.S) {
        PatternPreview()
        Spacer(Modifier.height(4.dp))
        Sl("Tapas (HS)",  0f..10f, StateBridge.shotHeadCount.toFloat()) {
            StateBridge.shotHeadCount = it.toInt()
        }
        Sl("Peitos",      0f..10f, StateBridge.shotBodyCount.toFloat()) {
            StateBridge.shotBodyCount = it.toInt()
        }
        Spacer(Modifier.height(3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Preset("BOT",   5, 0)
            Preset("AGGR",  3, 1)
            Preset("LEGIT", 2, 2)
            Preset("SAFE",  1, 3)
        }
    }
}

// ============ TAB: ESP ============

@Composable private fun EspTab() {
    Section("MODO ESP", tier = T.M) {
        var mode by remember { mutableStateOf(StateBridge.espMode) }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            EspModeBtn("OFF",   0, mode) { mode = 0; StateBridge.espMode = 0 }
            EspModeBtn("LEGIT", 1, mode) { mode = 1; StateBridge.espMode = 1 }
            EspModeBtn("FULL",  2, mode) { mode = 2; StateBridge.espMode = 2 }
            EspModeBtn("AUDIO", 3, mode) { mode = 3; StateBridge.espMode = 3 }
        }
        Spacer(Modifier.height(3.dp))
        Text(when (mode) {
            0 -> "Desligado"
            1 -> "LEGIT: so <80m, delayed 300ms, oculto scopando"
            2 -> "FULL: wallhack completo (RISCO ALTO)"
            3 -> "AUDIO: beep sutil quando inimigo <50m"
            else -> ""
        }, color = Neon.accent3, fontSize = 9.sp)
    }
    if (StateBridge.espMode in listOf(1, 2)) {
        Section("ESP VISUAL", tier = if (StateBridge.espMode == 2) T.R else T.M) {
            TierSw("Caixa 2D",       if (StateBridge.espMode==2) T.R else T.M, StateBridge.espBox)       { StateBridge.espBox = it }
            TierSw("Esqueleto",      T.R, StateBridge.espSkeleton)  { StateBridge.espSkeleton = it }
            TierSw("Barra de vida",  T.M, StateBridge.espHealth)    { StateBridge.espHealth = it }
            TierSw("Distância",      T.M, StateBridge.espDistance)  { StateBridge.espDistance = it }
            TierSw("Nome",           T.M, StateBridge.espName)      { StateBridge.espName = it }
            TierSw("Esconder scope", T.S, StateBridge.espHideScope) { StateBridge.espHideScope = it }
            Sl("Distância max (m)", 20f..500f, StateBridge.espMaxDist) {
                StateBridge.espMaxDist = it
            }
            Sl("Delay ms", 0f..1000f, StateBridge.espDelayMs.toFloat()) {
                StateBridge.espDelayMs = it.toInt()
            }
        }
    }
    if (StateBridge.espMode == 3) {
        Section("ESP AUDIO", tier = T.S) {
            Sl("Distância beep (m)", 10f..150f, StateBridge.espAudioThr) {
                StateBridge.espAudioThr = it
            }
            Text("Quanto mais perto, mais frequente o beep. So tu ouve.",
                color = Neon.dim, fontSize = 9.sp)
        }
    }
}

@Composable private fun EspModeBtn(label: String, idx: Int, cur: Int, onClick: () -> Unit) {
    val sel = cur == idx
    Box(
        Modifier.width(44.dp).heightIn(min = 28.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (sel) Neon.accent else Neon.glass)
            .border(1.dp, if (sel) Neon.accent3 else Neon.stroke.copy(alpha = 0.3f),
                RoundedCornerShape(5.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 9.sp,
            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
    }
}

// ============ TAB: WEAPON ============

@Composable private fun WeaponTab() {
    Section("INVISIVEL (patch mem)", tier = T.S) {
        TierSw("No-Recoil",         T.S, StateBridge.noRecoil)     { StateBridge.noRecoil = it }
        TierSw("No-Spread",         T.S, StateBridge.noSpread)     { StateBridge.noSpread = it }
        TierSw("Instant Reload",    T.S, StateBridge.noReload)     { StateBridge.noReload = it }
    }
    Section("VISIVEL", tier = T.M) {
        TierSw("Fast Fire",          T.M, StateBridge.fastFire)    { StateBridge.fastFire = it }
        TierSw("Infinite Ammo",      T.M, StateBridge.infiniteAmmo){ StateBridge.infiniteAmmo = it }
    }
    Section("RAGE (report magnet)", tier = T.R) {
        TierSw("One-Shot HS",        T.R, StateBridge.oneShotHs)   { StateBridge.oneShotHs = it }
        TierSw("Any gun = AWM dano", T.R, StateBridge.anyGunAwm)   { StateBridge.anyGunAwm = it }
        TierSw("Pierce Walls",       T.R, StateBridge.pierceWalls) { StateBridge.pierceWalls = it }
        TierSw("Magic Bullet",       T.R, StateBridge.magicBullet) { StateBridge.magicBullet = it }
    }
}

// ============ TAB: MOVE ============

@Composable private fun MoveTab() {
    Section("INVISIVEL", tier = T.S) {
        TierSw("No fall damage",    T.S, StateBridge.noFallDmg)    { StateBridge.noFallDmg = it }
        TierSw("Water breath max",  T.S, StateBridge.waterBreath)  { StateBridge.waterBreath = it }
    }
    Section("VISIVEL", tier = T.M) {
        TierSw("Speed Hack",        T.M, StateBridge.speedHack)    { StateBridge.speedHack = it }
        if (StateBridge.speedHack) {
            Sl("Mult", 1f..3f, StateBridge.speedMult)              { StateBridge.speedMult = it }
        }
        TierSw("Super Jump",        T.M, StateBridge.superJump)    { StateBridge.superJump = it }
        TierSw("Bhop",              T.M, StateBridge.bhop)         { StateBridge.bhop = it }
        TierSw("Instant Revive",    T.M, StateBridge.instantRevive){ StateBridge.instantRevive = it }
        TierSw("Wide View (FOV)",   T.M, StateBridge.wideView)     { StateBridge.wideView = it }
    }
    Section("RAGE", tier = T.R) {
        TierSw("Fly Mode",          T.R, StateBridge.flyMode)      { StateBridge.flyMode = it }
        TierSw("No Collision (noclip)", T.R, StateBridge.noCollision) { StateBridge.noCollision = it }
    }
}

// ============ TAB: INTEL ============

@Composable private fun IntelTab() {
    Section("INFO (baixo risco)", tier = T.S) {
        TierSw("Grenade Alert",     T.S, StateBridge.grenadeAlert) { StateBridge.grenadeAlert = it }
        TierSw("Zone Timer",        T.S, StateBridge.zoneTimer)    { StateBridge.zoneTimer = it }
        TierSw("Airdrop Tracker",   T.S, StateBridge.airdropTracker) { StateBridge.airdropTracker = it }
    }
    Section("INFO (medio risco)", tier = T.M) {
        TierSw("Loot ESP",          T.M, StateBridge.lootEsp)      { StateBridge.lootEsp = it }
        TierSw("Vehicle ESP",       T.M, StateBridge.vehicleEsp)   { StateBridge.vehicleEsp = it }
        TierSw("Mini-map enemies",  T.M, StateBridge.miniMapEnemy) { StateBridge.miniMapEnemy = it }
        TierSw("Kill Feed Spy",     T.M, StateBridge.killFeedSpy)  { StateBridge.killFeedSpy = it }
    }
}

// ============ TAB: SAFE ============

@Composable private fun SafeTab(ctx: Context) {
    Section("PROTECAO (deixa sempre ON)", tier = T.S) {
        TierSw("Spectator warning",  T.S, StateBridge.spectatorWarn) { StateBridge.spectatorWarn = it }
        TierSw("Streamer mode (hide em OBS)", T.S, StateBridge.streamerMode) { StateBridge.streamerMode = it }
        TierSw("HS cap dinâmico",    T.S, StateBridge.hsCapDynamic)  { StateBridge.hsCapDynamic = it }
        TierSw("Replay sanitizer",   T.S, StateBridge.replaySanit)   { StateBridge.replaySanit = it }
        TierSw("Signature rotator",  T.S, StateBridge.signatureRot)  { StateBridge.signatureRot = it }
    }
    Section("AUTO-OFF", tier = T.S) {
        Sl("Desliga aim apos N kills (0=off)", 0f..20f, StateBridge.autoOffKills.toFloat()) {
            StateBridge.autoOffKills = it.toInt()
        }
        Text("Depois de X kills na partida, aim desliga sozinho ate a proxima.",
            color = Neon.dim, fontSize = 9.sp)
    }
    Section("STEALTH", tier = T.S) {
        TierSw("Bloquear print/record", T.S, StateBridge.flagSecure) {
            StateBridge.flagSecure = it
            OverlayController.refreshFlags()
        }
        Spacer(Modifier.height(3.dp))
        GhostBtn("📷 LIBERAR PRINT (10s)") { OverlayController.allowPrintFor(ctx, 10) }
        Spacer(Modifier.height(3.dp))
        GhostBtn("Trocar ícone") { Disguise.cycle(ctx) }
        Spacer(Modifier.height(3.dp))
        GhostBtn("Limpar vestígios") { Stealth.wipe(ctx) }
        Spacer(Modifier.height(3.dp))
        GhostBtn("Sumir dos recentes") { Stealth.hideRecents(ctx) }
    }
    Section("INFO", tier = T.S) {
        InfoRow("Versão",  "19.0-maxpack")
        InfoRow("Modo",    "LSPosed + Zygisk")
        InfoRow("PIN",     "1337")
        InfoRow("Tiers",   "🟢 safe  🟡 médio  🔴 rage")
    }
}

@Composable private fun InfoRow(k: String, v: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(k, color = Neon.dim, fontSize = 10.sp, modifier = Modifier.weight(1f))
        Text(v, color = Neon.accent3, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

// ============ PATTERN HELPERS ============

@Composable private fun PatternPreview() {
    val h = StateBridge.shotHeadCount
    val b = StateBridge.shotBodyCount
    val total = h + b
    val hsRate = if (total > 0) (h * 100 / total) else 0
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Taxa HS:", color = Neon.text, fontSize = 10.sp,
                modifier = Modifier.weight(1f))
            Text("$hsRate%", color = when {
                hsRate >= 80 -> Neon.danger
                hsRate >= 50 -> Neon.accent3
                else         -> Neon.dim
            }, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(h.coerceAtMost(10)) {
                Box(Modifier.size(width = 14.dp, height = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Neon.accent3))
            }
            repeat(b.coerceAtMost(10)) {
                Box(Modifier.size(width = 14.dp, height = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Neon.dim))
            }
        }
    }
}

@Composable private fun RowScope.Preset(label: String, h: Int, b: Int) {
    val sel = StateBridge.shotHeadCount == h && StateBridge.shotBodyCount == b
    Box(
        Modifier.weight(1f).heightIn(min = 32.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (sel) Neon.accent else Neon.glass)
            .border(1.dp,
                if (sel) Neon.accent3 else Neon.stroke.copy(alpha = 0.3f),
                RoundedCornerShape(5.dp))
            .clickable {
                StateBridge.shotHeadCount = h
                StateBridge.shotBodyCount = b
            },
        contentAlignment = Alignment.Center
    ) {
        Text("$label\n${h}H+${b}P", color = Color.White, fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// ============ FOOTER ============

@Composable private fun Footer() {
    Row(
        Modifier.fillMaxWidth().background(Neon.sidebar)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            Modifier.weight(1f).heightIn(min = 26.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Neon.danger)
                .clickable { try {
                    StateBridge.panic(); OverlayController.hide()
                } catch (_: Throwable) {} },
            contentAlignment = Alignment.Center
        ) {
            Text("PANIC", color = Color.White, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier.weight(1f).heightIn(min = 26.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Neon.accent2)
                .clickable { try { OverlayController.hide() } catch (_: Throwable) {} },
            contentAlignment = Alignment.Center
        ) {
            Text("HIDE", color = Color.White, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
        }
    }
}

// ============ TIER SYSTEM ============

private enum class T(val color: Color, val dot: String) {
    S(Color(0xFF22C55E), "●"),   // verde safe
    M(Color(0xFFFBBF24), "●"),   // amarelo medio
    R(Color(0xFFEF4444), "●")    // vermelho rage
}

@Composable private fun Section(title: String, tier: T,
                                content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Neon.glass)
            .border(1.dp, Neon.stroke.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tier.dot, color = tier.color, fontSize = 11.sp,
                fontWeight = FontWeight.Black)
            Spacer(Modifier.width(5.dp))
            Text(title, color = Neon.accent3, fontSize = 9.sp,
                fontWeight = FontWeight.Black, letterSpacing = 1.1.sp)
        }
        content()
    }
}

@Composable private fun TierSw(label: String, tier: T, value: Boolean,
                               onChange: (Boolean) -> Unit) {
    var v by remember { mutableStateOf(value) }
    val toggle = { v = !v; try { onChange(v) } catch (_: Throwable) {} }
    val noRipple = remember { MutableInteractionSource() }
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 26.dp)
            .clickable(interactionSource = noRipple, indication = null) { toggle() }
            .padding(vertical = 1.dp)) {
        Box(Modifier.size(16.dp).clip(RoundedCornerShape(3.dp))
            .background(if (v) Neon.accent else Color.Transparent)
            .border(1.dp, if (v) Neon.accent3 else Neon.dim, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (v) Text("✓", color = Color.White, fontSize = 11.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(6.dp))
        Text(label, color = Neon.text, fontSize = 10.sp,
            modifier = Modifier.weight(1f))
        Text(tier.dot, color = tier.color.copy(alpha = 0.7f), fontSize = 10.sp,
            fontWeight = FontWeight.Black)
    }
}

@Composable private fun Sl(label: String, range: ClosedFloatingPointRange<Float>,
                           value: Float, onChange: (Float) -> Unit) {
    var v by remember { mutableStateOf(value) }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Neon.text, fontSize = 10.sp,
                modifier = Modifier.weight(1f))
            Text(formatVal(v), color = Neon.accent3, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
        }
        Slider(
            value = v, onValueChange = {
                v = it; try { onChange(it) } catch (_: Throwable) {}
            },
            valueRange = range,
            modifier = Modifier.height(22.dp),
            colors = SliderDefaults.colors(
                thumbColor = Neon.accent3,
                activeTrackColor = Neon.accent,
                inactiveTrackColor = Neon.dim.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable private fun GhostBtn(label: String, onClick: () -> Unit) {
    val noRipple = remember { MutableInteractionSource() }
    Box(
        Modifier.fillMaxWidth().heightIn(min = 30.dp)
            .clip(RoundedCornerShape(5.dp))
            .border(1.dp, Neon.accent3, RoundedCornerShape(5.dp))
            .clickable(interactionSource = noRipple, indication = null) {
                try { onClick() } catch (_: Throwable) {}
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Neon.accent3, fontSize = 10.sp,
            fontWeight = FontWeight.Bold)
    }
}

private fun formatVal(v: Float): String {
    val r = v.toInt().toFloat()
    return if (v == r) r.toInt().toString() else String.format("%.2f", v)
}
