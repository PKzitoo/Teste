package com.ffkit.lite

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * ffkit v20 — redesign JZN-style.
 * Layout:
 *   Header (36dp)
 *   TopTabs horizontal (~40dp)
 *   Row:
 *     LeftCard (branding + perfil + status)   1 weight
 *     FeatureList (cards com icone+titulo+sub) 2 weight
 *   Footer (PANIC/HIDE)
 */
@Composable
fun Panel(ctx: Context, onMinimize: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    // OBSERVA refreshKey: quando Profile.apply() bumpar, tudo recompose
    val refresh = Profile.refreshKey.intValue
    Surface(
        modifier = Modifier.width(440.dp),   // bem mais largo pros lados
        color = Neon.bg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Neon.stroke),
    ) {
        Column {
            Header(onMinimize)
            TopTabs(tab) { tab = it }
            Row(
                Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LeftCard(refresh)
                FeatureList(ctx, tab, refresh, Modifier.weight(2f))
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
            .heightIn(min = 30.dp)
            .background(Brush.horizontalGradient(
                listOf(Neon.accent2, Neon.accent)))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).heightIn(min = 32.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, drag ->
                        change.consume()
                        OverlayController.moveBy(drag.x.toInt(), drag.y.toInt())
                    }
                }
        ) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(Neon.accent3))
            Spacer(Modifier.width(8.dp))
            Text("FFKIT", color = Color.White, fontSize = 13.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.width(6.dp))
            // tag do dono em roxo claro — bem destacado
            Box(
                Modifier.clip(RoundedCornerShape(3.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text("@pkxit", color = Neon.accent3, fontSize = 10.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
            Spacer(Modifier.width(5.dp))
            Text("v20", color = Color.White.copy(alpha = 0.55f), fontSize = 8.sp)
            Spacer(Modifier.weight(1f))
            Text("⋮⋮", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        HeaderBtn("_") { onMinimize() }
        Spacer(Modifier.width(5.dp))
        HeaderBtn("X") { OverlayController.hide() }
    }
}

@Composable private fun HeaderBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(22.dp).clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .clickable { try { onClick() } catch (_: Throwable) {} },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 11.sp,
            fontWeight = FontWeight.Bold)
    }
}

// ============ TOP TABS ============

@Composable private fun TopTabs(selected: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf(
        "AIM"  to "🎯",
        "ESP"  to "👁",
        "WEAP" to "🔫",
        "MOVE" to "🏃",
        "INTL" to "📡",
        "SAFE" to "🛡"
    )
    Row(
        Modifier.fillMaxWidth().background(Neon.sidebar)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        tabs.forEachIndexed { i, (label, icon) ->
            TopTab(label, icon, selected == i, Modifier.weight(1f)) { onSelect(i) }
        }
    }
}

@Composable private fun TopTab(label: String, icon: String, selected: Boolean,
                               modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .heightIn(min = 28.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (selected) Neon.accent else Neon.card)
            .border(1.dp,
                if (selected) Neon.accent3 else Neon.stroke.copy(alpha = 0.4f),
                RoundedCornerShape(5.dp))
            .clickable { try { onClick() } catch (_: Throwable) {} }
            .padding(vertical = 1.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 11.sp)
            Spacer(Modifier.width(3.dp))
            Text(label, color = if (selected) Color.White else Neon.dim,
                fontSize = 7.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp)
        }
    }
}

// ============ LEFT CARD (branding + perfil + status) ============

@Composable private fun LeftCard(@Suppress("UNUSED_PARAMETER") refresh: Int) {
    // observa refreshKey DIRETO aqui — garante recompose quando qualquer
    // FCard bumpar, sem depender do Panel pai
    @Suppress("UNUSED_VARIABLE")
    val localRefresh = Profile.refreshKey.intValue
    val current = Profile.current()
    val intensity = Profile.intensity()
    Column(
        Modifier.width(84.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Neon.card)
            .border(1.dp, Neon.stroke.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(52.dp).clip(CircleShape)
                .background(Brush.radialGradient(
                    listOf(Neon.accent, Neon.accent2, Color.Black)))
                .border(1.dp, Neon.accent3, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("FF", color = Color.White, fontSize = 16.sp,
                fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
        }
        Spacer(Modifier.height(5.dp))
        Text("FFKIT", color = Color.White, fontSize = 9.sp,
            fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        Text("@pkxit", color = Neon.accent3, fontSize = 9.sp,
            fontWeight = FontWeight.Black)
        Spacer(Modifier.height(6.dp))

        val aimOn = StateBridge.aimEnabled
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape)
                .background(if (aimOn) Color(0xFF22C55E) else Neon.dim))
            Spacer(Modifier.width(3.dp))
            Text(if (aimOn) "ACTIVE" else "IDLE",
                color = if (aimOn) Color(0xFF22C55E) else Neon.dim,
                fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))

        Text("PERFIL", color = Neon.accent3, fontSize = 6.sp,
            fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        Spacer(Modifier.height(3.dp))
        ProfileBtn("LEGIT", current == Profile.P.LEGIT, Profile.P.LEGIT)
        Spacer(Modifier.height(2.dp))
        ProfileBtn("AGGR",  current == Profile.P.AGGR,  Profile.P.AGGR)
        Spacer(Modifier.height(2.dp))
        ProfileBtn("RAGE",  current == Profile.P.RAGE,  Profile.P.RAGE)
        if (current == Profile.P.CUSTOM) {
            Spacer(Modifier.height(2.dp))
            Text("CUSTOM", color = Neon.dim, fontSize = 7.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))

        Text("INTENSIDADE", color = Neon.dim, fontSize = 6.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text("$intensity%", color = when {
            intensity >= 70 -> Neon.danger
            intensity >= 40 -> Color(0xFFFBBF24)
            else            -> Color(0xFF22C55E)
        }, fontSize = 17.sp, fontWeight = FontWeight.Black)
    }
}

@Composable private fun ProfileBtn(label: String, sel: Boolean, p: Profile.P) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 22.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (sel) Neon.accent else Neon.cardHi)
            .border(1.dp, if (sel) Neon.accent3 else Neon.stroke.copy(alpha = 0.4f),
                RoundedCornerShape(4.dp))
            .clickable { Profile.apply(p) },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 9.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

// ============ FEATURE LIST ============

@Composable private fun FeatureList(ctx: Context, tab: Int, refresh: Int,
                                     modifier: Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // key(refresh): forca sub-composable a rerodar quando refreshKey muda
        // (Profile.apply() -> Profile.bump() -> todos FCards resyncam)
        key(refresh) {
            when (tab) {
                0 -> AimFeatures()
                1 -> EspFeatures()
                2 -> WeapFeatures()
                3 -> MoveFeatures()
                4 -> IntelFeatures()
                5 -> SafeFeatures(ctx)
            }
        }
    }
}

@Composable private fun AimFeatures() {
    FCard("🎯", "Aim Helper", "Logica central de mira",
        active = StateBridge.aimEnabled, tier = T.S) {
        StateBridge.aimEnabled = it
    }
    FCard("🔒", "Aim Lock", "So dispara quando atirando",
        active = StateBridge.aimOnFire, tier = T.S) {
        StateBridge.aimOnFire = it
    }
    FCard("🔭", "Aim Only ADS", "So quando scopando/mirando",
        active = StateBridge.aimOnScope, tier = T.S) {
        StateBridge.aimOnScope = it
    }
    FCard("🤖", "Ignore Bots", "Pula NPCs na partida",
        active = StateBridge.aimIgnoreBots, tier = T.S) {
        StateBridge.aimIgnoreBots = it
    }
    FCard("🎲", "Prediction", "Prediz movimento do alvo",
        active = StateBridge.aimPrediction, tier = T.S) {
        StateBridge.aimPrediction = it
    }
    FCard("🔄", "Multi Target", "Troca alvo ao matar",
        active = StateBridge.aimMultiTarget, tier = T.M) {
        StateBridge.aimMultiTarget = it
    }
    FCard("👻", "Silent Aim", "Mira sem mover camera",
        active = StateBridge.aimSilent, tier = T.R) {
        StateBridge.aimSilent = it
    }
    SliderCard("Aim FOV", "px (saltos 50)", 30f..630f, 50f, StateBridge.aimFov) {
        StateBridge.aimFov = it
    }
    SliderCard("Suavidade", "1-10", 1f..10f, 1f, StateBridge.aimSmooth) {
        StateBridge.aimSmooth = it
    }
    SliderCard("Reação ms", "ms (saltos 50)", 50f..500f, 50f, StateBridge.aimReactionMs.toFloat()) {
        StateBridge.aimReactionMs = it.toInt()
    }
    SliderCard("Tapas HS", "0-10", 0f..10f, 1f, StateBridge.shotHeadCount.toFloat()) {
        StateBridge.shotHeadCount = it.toInt()
    }
    SliderCard("Peitos", "0-10", 0f..10f, 1f, StateBridge.shotBodyCount.toFloat()) {
        StateBridge.shotBodyCount = it.toInt()
    }
}

@Composable private fun EspFeatures() {
    // Mode picker (OFF/LEGIT/FULL/AUDIO)
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        EspModeChip("OFF",   0, Modifier.weight(1f))
        EspModeChip("LEGIT", 1, Modifier.weight(1f))
        EspModeChip("FULL",  2, Modifier.weight(1f))
        EspModeChip("AUDIO", 3, Modifier.weight(1f))
    }
    val sub = when (StateBridge.espMode) {
        1 -> "LEGIT: <80m delayed 300ms"
        2 -> "FULL: wallhack completo (🔴 risco)"
        3 -> "AUDIO: beep quando <50m"
        else -> "ESP desligado"
    }
    Text(sub, color = Neon.accent3, fontSize = 9.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))

    if (StateBridge.espMode in listOf(1, 2)) {
        FCard("📦", "Box 2D", "Caixa ao redor do inimigo",
            active = StateBridge.espBox, tier = if (StateBridge.espMode == 2) T.R else T.M) {
            StateBridge.espBox = it
        }
        FCard("💀", "Esqueleto", "Linhas dos ossos",
            active = StateBridge.espSkeleton, tier = T.R) { StateBridge.espSkeleton = it }
        FCard("❤", "HP Bar", "Barra de vida",
            active = StateBridge.espHealth, tier = T.M) { StateBridge.espHealth = it }
        FCard("📏", "Distance", "Metros ate o inimigo",
            active = StateBridge.espDistance, tier = T.M) { StateBridge.espDistance = it }
        FCard("👤", "Nome", "Nick do jogador",
            active = StateBridge.espName, tier = T.M) { StateBridge.espName = it }
        FCard("👁", "Hide scoping", "Oculta ESP ao scopar",
            active = StateBridge.espHideScope, tier = T.S) { StateBridge.espHideScope = it }
        SliderCard("Max distance", "metros (saltos 20)", 20f..500f, 20f, StateBridge.espMaxDist) {
            StateBridge.espMaxDist = it
        }
    }
    if (StateBridge.espMode == 3) {
        SliderCard("Beep distance", "metros (saltos 10)", 10f..150f, 10f, StateBridge.espAudioThr) {
            StateBridge.espAudioThr = it
        }
    }
}

@Composable private fun EspModeChip(label: String, idx: Int, mod: Modifier) {
    val sel = StateBridge.espMode == idx
    Box(
        mod.heightIn(min = 30.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (sel) Neon.accent else Neon.card)
            .border(1.dp, if (sel) Neon.accent3 else Neon.stroke.copy(alpha = 0.4f),
                RoundedCornerShape(5.dp))
            .clickable { StateBridge.espMode = idx },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 10.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable private fun WeapFeatures() {
    FCard("⚡", "No Recoil", "Zera recuo (invisivel)",
        active = StateBridge.noRecoil, tier = T.S) { StateBridge.noRecoil = it }
    FCard("🎯", "No Spread", "Sem espalhamento de bala",
        active = StateBridge.noSpread, tier = T.S) { StateBridge.noSpread = it }
    FCard("⚙", "Instant Reload", "Reload instantaneo",
        active = StateBridge.noReload, tier = T.S) { StateBridge.noReload = it }
    FCard("🔥", "Fast Fire", "Cadencia acelerada",
        active = StateBridge.fastFire, tier = T.M) { StateBridge.fastFire = it }
    FCard("♾", "Infinite Ammo", "Municao infinita",
        active = StateBridge.infiniteAmmo, tier = T.M) { StateBridge.infiniteAmmo = it }
    FCard("💥", "One-Shot HS", "Mata com 1 tapa",
        active = StateBridge.oneShotHs, tier = T.R) { StateBridge.oneShotHs = it }
    FCard("🏹", "Any gun = AWM", "Dano maximo em tudo",
        active = StateBridge.anyGunAwm, tier = T.R) { StateBridge.anyGunAwm = it }
    FCard("🧱", "Pierce Walls", "Balas atravessam paredes",
        active = StateBridge.pierceWalls, tier = T.R) { StateBridge.pierceWalls = it }
    FCard("🌀", "Magic Bullet", "Bala curva pro alvo",
        active = StateBridge.magicBullet, tier = T.R) { StateBridge.magicBullet = it }
}

@Composable private fun MoveFeatures() {
    FCard("🪶", "No Fall Damage", "Nao morre de queda",
        active = StateBridge.noFallDmg, tier = T.S) { StateBridge.noFallDmg = it }
    FCard("💧", "Water Breath", "Respiracao infinita d'agua",
        active = StateBridge.waterBreath, tier = T.S) { StateBridge.waterBreath = it }
    FCard("💨", "Speed Hack", "Velocidade turbo",
        active = StateBridge.speedHack, tier = T.M) { StateBridge.speedHack = it }
    if (StateBridge.speedHack) {
        SliderCard("Mult", "1-3x", 1f..3f, 1f, StateBridge.speedMult) {
            StateBridge.speedMult = it
        }
    }
    FCard("🦘", "Super Jump", "Pulo 2x mais alto",
        active = StateBridge.superJump, tier = T.M) { StateBridge.superJump = it }
    FCard("🔂", "Bhop", "Pulos encadeados",
        active = StateBridge.bhop, tier = T.M) { StateBridge.bhop = it }
    FCard("💊", "Instant Revive", "Revive aliado em 1s",
        active = StateBridge.instantRevive, tier = T.M) { StateBridge.instantRevive = it }
    FCard("🔭", "Wide View", "FOV estendido",
        active = StateBridge.wideView, tier = T.M) { StateBridge.wideView = it }
    FCard("🕊", "Fly Mode", "Voa livre pelo mapa",
        active = StateBridge.flyMode, tier = T.R) { StateBridge.flyMode = it }
    FCard("👻", "No Collision", "Atravessa paredes (noclip)",
        active = StateBridge.noCollision, tier = T.R) { StateBridge.noCollision = it }
}

@Composable private fun IntelFeatures() {
    FCard("💣", "Grenade Alert", "Seta de granada caindo",
        active = StateBridge.grenadeAlert, tier = T.S) { StateBridge.grenadeAlert = it }
    FCard("⏱", "Zone Timer", "Countdown fechamento zona",
        active = StateBridge.zoneTimer, tier = T.S) { StateBridge.zoneTimer = it }
    FCard("📦", "Airdrop Tracker", "Marca airdrops",
        active = StateBridge.airdropTracker, tier = T.S) { StateBridge.airdropTracker = it }
    FCard("💎", "Loot ESP", "Destaca itens no chao",
        active = StateBridge.lootEsp, tier = T.M) { StateBridge.lootEsp = it }
    FCard("🚗", "Vehicle ESP", "Marca veiculos",
        active = StateBridge.vehicleEsp, tier = T.M) { StateBridge.vehicleEsp = it }
    FCard("🗺", "Mini-map Enemies", "Inimigos no mini-mapa",
        active = StateBridge.miniMapEnemy, tier = T.M) { StateBridge.miniMapEnemy = it }
    FCard("💀", "Kill Feed Spy", "Ve kills fora da tela",
        active = StateBridge.killFeedSpy, tier = T.M) { StateBridge.killFeedSpy = it }
}

@Composable private fun SafeFeatures(ctx: Context) {
    FCard("👀", "Spectator Warn", "Avisa quando te espectam",
        active = StateBridge.spectatorWarn, tier = T.S) { StateBridge.spectatorWarn = it }
    FCard("📺", "Streamer Mode", "Oculta em OBS/record",
        active = StateBridge.streamerMode, tier = T.S) { StateBridge.streamerMode = it }
    FCard("📉", "HS Cap Dinamico", "Ajusta pattern automatico",
        active = StateBridge.hsCapDynamic, tier = T.S) { StateBridge.hsCapDynamic = it }
    FCard("🧹", "Replay Sanitizer", "Limpa dados suspeitos",
        active = StateBridge.replaySanit, tier = T.S) { StateBridge.replaySanit = it }
    FCard("🔑", "Signature Rotator", "Troca assinatura diario",
        active = StateBridge.signatureRot, tier = T.S) { StateBridge.signatureRot = it }
    FCard("🔒", "FLAG_SECURE", "Bloqueia print/record",
        active = StateBridge.flagSecure, tier = T.S) {
        StateBridge.flagSecure = it
        OverlayController.refreshFlags()
    }
    SliderCard("Auto-off kills", "0=desligado", 0f..20f, 1f,
        StateBridge.autoOffKills.toFloat()) {
        StateBridge.autoOffKills = it.toInt()
    }
    ActionCard("📷", "Liberar print (10s)") {
        OverlayController.allowPrintFor(ctx, 10)
    }
    ActionCard("🎨", "Trocar icone") { Disguise.cycle(ctx) }
    ActionCard("🗑", "Limpar vestigios") { Stealth.wipe(ctx) }
    ActionCard("🫥", "Sumir dos recentes") { Stealth.hideRecents(ctx) }
}

// ============ FOOTER ============

@Composable private fun Footer() {
    Row(
        Modifier.fillMaxWidth().background(Neon.sidebar)
            .padding(horizontal = 6.dp, vertical = 4.dp),
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
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
        Box(
            Modifier.weight(1f).heightIn(min = 26.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Neon.accent2)
                .clickable { try { OverlayController.hide() } catch (_: Throwable) {} },
            contentAlignment = Alignment.Center
        ) {
            Text("HIDE", color = Color.White, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

// ============ ATOMS ============

private enum class T(val color: Color) {
    S(Color(0xFF22C55E)),  // verde safe
    M(Color(0xFFFBBF24)),  // amarelo medio
    R(Color(0xFFEF4444))   // vermelho rage
}

/** Feature card compacto (JZN-inline): 28dp, so titulo + toggle, sem subtitle.
 *  remember(active) com key(refresh) do pai garante sync quando Profile.apply() roda. */
@Composable private fun FCard(icon: String, title: String,
                              @Suppress("UNUSED_PARAMETER") subtitle: String,
                              active: Boolean, tier: T,
                              onToggle: (Boolean) -> Unit) {
    var v by remember(active) { mutableStateOf(active) }
    val toggle = {
        v = !v
        try { onToggle(v) } catch (_: Throwable) {}
        Profile.bump()
    }
    Row(
        Modifier.fillMaxWidth().heightIn(min = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (v) Neon.cardHi else Neon.card)
            .border(1.dp,
                if (v) Neon.accent.copy(alpha = 0.6f) else Neon.stroke.copy(alpha = 0.3f),
                RoundedCornerShape(6.dp))
            .clickable { toggle() }
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 13.sp)
        Spacer(Modifier.width(6.dp))
        Text(title, color = Color.White, fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f))
        Box(Modifier.size(5.dp).clip(CircleShape).background(tier.color))
        Spacer(Modifier.width(4.dp))
        Box(
            Modifier.width(22.dp).height(11.dp).clip(RoundedCornerShape(5.5.dp))
                .background(if (v) Neon.accent else Neon.dim.copy(alpha = 0.4f))
                .padding(1.dp),
            contentAlignment = if (v) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(Color.White))
        }
    }
}

/** SliderCard: FLUIDO (sem snap interno), mas valor exibido/armazenado
 *  arredondado pro step. Visualmente desliza suave, logicamente respeita steps. */
@Composable private fun SliderCard(title: String,
                                   @Suppress("UNUSED_PARAMETER") subtitle: String,
                                   range: ClosedFloatingPointRange<Float>,
                                   step: Float,
                                   value: Float, onChange: (Float) -> Unit) {
    // raw = valor cru enquanto arrasta (permite movimento suave)
    // v   = valor snap-pro-step pra onChange + display
    var raw by remember(value) { mutableStateOf(value) }
    val v = (Math.round(raw / step) * step).toFloat().coerceIn(
        range.start, range.endInclusive)
    Column(
        Modifier.fillMaxWidth().heightIn(min = 46.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Neon.card)
            .border(1.dp, Neon.stroke.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Color.White, fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f))
            Text(v.toInt().toString(), color = Neon.accent3, fontSize = 13.sp,
                fontWeight = FontWeight.Black)
        }
        Slider(
            value = raw,
            onValueChange = { newRaw ->
                raw = newRaw
                val snapped = (Math.round(newRaw / step) * step).toFloat()
                    .coerceIn(range.start, range.endInclusive)
                try { onChange(snapped) } catch (_: Throwable) {}
                Profile.bump()
            },
            valueRange = range,
            // SEM steps interno -> arrasto 100% fluido
            modifier = Modifier.fillMaxWidth().height(26.dp),
            colors = SliderDefaults.colors(
                thumbColor = Neon.accent3,
                activeTrackColor = Neon.accent,
                inactiveTrackColor = Neon.dim.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable private fun ActionCard(icon: String, title: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Neon.card)
            .border(1.dp, Neon.accent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable { try { onClick() } catch (_: Throwable) {} }
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(title, color = Neon.accent3, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("→", color = Neon.accent3, fontSize = 11.sp)
    }
}

private fun formatVal(v: Float): String {
    val r = v.toInt().toFloat()
    return if (v == r) r.toInt().toString() else String.format("%.2f", v)
}
