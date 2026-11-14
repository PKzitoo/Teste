package com.ffkit.lite

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
 * Painel ffkit v17 (lite-recoil).
 * 4 tabs: ASSIST / WEAPON / MACROS / SYS
 * Fundo preto, acentos azul escuro.
 */
@Composable
fun Panel(ctx: Context, onMinimize: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Surface(
        modifier = Modifier.width(250.dp),
        color = Neon.bg,
        shape = RoundedCornerShape(10.dp),
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
        Text("v17-lite", color = Color.White.copy(alpha = 0.6f),
            fontSize = 8.sp)
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

// ============ SIDEBAR ============

@Composable private fun Sidebar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf("A" to "ASSIST", "W" to "WEAP", "M" to "MACRO", "S" to "SYS")
    Column(
        Modifier.width(44.dp).fillMaxHeight()
            .background(Neon.sidebar)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
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
        modifier = Modifier.width(36.dp)
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(7.dp))
                .background(bg)
                .border(1.dp,
                    if (selected) Neon.accent3 else Neon.stroke.copy(alpha = 0.3f),
                    RoundedCornerShape(7.dp))
                .clickable { try { onClick() } catch (_: Throwable) {} },
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = Color.White, fontSize = 14.sp,
                fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(2.dp))
        Text(label, color = if (selected) Neon.accent3 else Neon.dim,
            fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}

// ============ CONTENT ============

@Composable private fun RowScope.Content(ctx: Context, tab: Int) {
    Column(
        Modifier.weight(1f).heightIn(max = 380.dp)
            .background(Neon.panel)
            .padding(horizontal = 7.dp, vertical = 7.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (tab) {
            0 -> AssistTab(ctx)
            1 -> WeaponTab()
            2 -> MacroTab()
            3 -> SysTab(ctx)
        }
    }
}

// ============ TAB: ASSIST ============

@Composable private fun AssistTab(ctx: Context) {
    Section("SHIZUKU") {
        var st by remember { mutableStateOf(ShizukuBridge.state()) }
        LaunchedEffect(Unit) {
            while (true) {
                st = ShizukuBridge.state()
                kotlinx.coroutines.delay(1500)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Status", color = Neon.text, fontSize = 11.sp,
                modifier = Modifier.weight(1f))
            val (c, lbl) = when (st) {
                ShizukuBridge.State.READY         -> Neon.accent3 to "PRONTO"
                ShizukuBridge.State.NO_PERMISSION -> Color(0xFFFFB020) to "SEM PERM"
                ShizukuBridge.State.UNAVAILABLE   -> Neon.danger to "OFF"
            }
            Box(Modifier.size(8.dp).clip(CircleShape).background(c))
            Spacer(Modifier.width(4.dp))
            Text(lbl, color = c, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        if (st == ShizukuBridge.State.NO_PERMISSION) {
            Spacer(Modifier.height(4.dp))
            PrimaryBtn("Pedir permissão") { ShizukuBridge.requestPermission() }
        }
        if (st == ShizukuBridge.State.UNAVAILABLE) {
            Text("Abra o Shizuku e ligue a Depuracao sem fio.",
                color = Neon.dim, fontSize = 9.sp)
        }
    }

    Section("DIAGNOSTICO") {
        PrimaryBtn("TEST SWIPE (debug)") {
            RecoilEngine.testOnce()
            ShizukuBridge.toast(ctx, RecoilEngine.lastError.ifBlank { "enviado" })
        }
        Spacer(Modifier.height(3.dp))
        Text("Toca com o FF aberto no fundo. Se a camera do FF mexer, Shizuku ta OK — problema so no tuning. Se nao mexer, Shizuku nao esta enviando input.",
            color = Neon.dim, fontSize = 9.sp)
        Text("Swipes enviados: ${RecoilEngine.swipesSent}  |  ${RecoilEngine.lastError.ifBlank { "—" }}",
            color = Neon.accent3, fontSize = 9.sp)
    }

    Section("MODO") {
        Sw("SEMPRE LIGADO (sem botão)", StateBridge.alwaysOn) {
            StateBridge.alwaysOn = it
            if (it) RecoilEngine.start() else RecoilEngine.stop()
        }
        Text("Com sempre-ligado: recoil roda continuamente. A camera vai mover sozinha mesmo sem atirar.",
            color = Neon.dim, fontSize = 9.sp)
    }

    Section("BOTAO ASSIST (flutuante)") {
        var visible by remember { mutableStateOf(AssistTrigger.isVisible()) }
        PrimaryBtn(if (visible) "Esconder botão" else "Mostrar botão") {
            AssistTrigger.toggle(ctx)
            visible = AssistTrigger.isVisible()
        }
        Spacer(Modifier.height(3.dp))
        Text("Segure o botão enquanto atira. Toque longo + arrasta pra reposicionar.",
            color = Neon.dim, fontSize = 9.sp)

        Spacer(Modifier.height(4.dp))
        Sl("Tamanho (dp)",   48f..120f, StateBridge.triggerSize.toFloat()) {
            StateBridge.triggerSize = it.toInt(); AssistTrigger.refresh(ctx)
        }
        Sl("Opacidade",      0.2f..1.0f, StateBridge.triggerOpacity) {
            StateBridge.triggerOpacity = it; AssistTrigger.refresh(ctx)
        }
    }

    Section("LOOK PAD (coord do swipe)") {
        Text("Centro do pad direito do FF (olhar em volta).",
            color = Neon.dim, fontSize = 9.sp)
        Sl("X (px)", 100f..2400f, StateBridge.lookPadX.toFloat()) {
            StateBridge.lookPadX = it.toInt()
        }
        Sl("Y (px)", 100f..1200f, StateBridge.lookPadY.toFloat()) {
            StateBridge.lookPadY = it.toInt()
        }
        Sl("Duração swipe ms", 20f..120f, StateBridge.swipeDurationMs.toFloat()) {
            StateBridge.swipeDurationMs = it.toInt()
        }
        Text("Default X=1800 Y=540 Dur=40. Se nao reagir, aumenta duração pra 60-80.",
            color = Neon.dim, fontSize = 9.sp)
    }
}

// ============ TAB: WEAPON ============

@Composable private fun WeaponTab() {
    var slot by remember { mutableStateOf(0) }   // 0=primaria, 1=secundaria
    var pri  by remember { mutableStateOf(StateBridge.weaponPrimary) }
    var sec  by remember { mutableStateOf(StateBridge.weaponSecondary) }
    var active by remember { mutableStateOf(StateBridge.weaponIdx) }

    Section("ARMA ATIVA") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // tab PRI / SEC
            val isPri = active == pri
            Box(
                Modifier.weight(1f).heightIn(min = 36.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (isPri) Neon.accent else Neon.glass)
                    .border(1.dp,
                        if (isPri) Neon.accent3 else Neon.stroke.copy(alpha = 0.3f),
                        RoundedCornerShape(5.dp))
                    .clickable { active = pri; StateBridge.weaponIdx = pri },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PRI", color = Color.White, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold)
                    Text(WeaponPresets.byIndex(pri).shortCode,
                        color = Color.White, fontSize = 11.sp,
                        fontWeight = FontWeight.Black)
                }
            }
            val isSec = active == sec
            Box(
                Modifier.weight(1f).heightIn(min = 36.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (isSec) Neon.accent else Neon.glass)
                    .border(1.dp,
                        if (isSec) Neon.accent3 else Neon.stroke.copy(alpha = 0.3f),
                        RoundedCornerShape(5.dp))
                    .clickable { active = sec; StateBridge.weaponIdx = sec },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SEC", color = Color.White, fontSize = 8.sp,
                        fontWeight = FontWeight.Bold)
                    Text(WeaponPresets.byIndex(sec).shortCode,
                        color = Color.White, fontSize = 11.sp,
                        fontWeight = FontWeight.Black)
                }
            }
        }
        Spacer(Modifier.height(3.dp))
        Text("Toca em PRI ou SEC pra alternar. A ATIVA eh a que o recoil usa.",
            color = Neon.dim, fontSize = 9.sp)
    }

    Section("ESCOLHER ARMA — Slot ${if (slot == 0) "PRI" else "SEC"}") {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.weight(1f).heightIn(min = 28.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (slot == 0) Neon.accent else Neon.glass)
                    .border(1.dp, Neon.accent3.copy(alpha = if (slot == 0) 1f else 0.3f),
                        RoundedCornerShape(5.dp))
                    .clickable { slot = 0 },
                contentAlignment = Alignment.Center
            ) { Text("PRI", color = Color.White, fontSize = 10.sp,
                fontWeight = FontWeight.Bold) }
            Box(
                Modifier.weight(1f).heightIn(min = 28.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (slot == 1) Neon.accent else Neon.glass)
                    .border(1.dp, Neon.accent3.copy(alpha = if (slot == 1) 1f else 0.3f),
                        RoundedCornerShape(5.dp))
                    .clickable { slot = 1 },
                contentAlignment = Alignment.Center
            ) { Text("SEC", color = Color.White, fontSize = 10.sp,
                fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(4.dp))

        val presets = WeaponPresets.LIST
        val current = if (slot == 0) pri else sec
        for (chunk in presets.chunked(3)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                chunk.forEach { p ->
                    val idx = presets.indexOf(p)
                    val sel = current == idx
                    Box(
                        Modifier.weight(1f).heightIn(min = 30.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (sel) Neon.accent else Neon.glass)
                            .border(1.dp,
                                if (sel) Neon.accent3 else Neon.stroke.copy(alpha = 0.3f),
                                RoundedCornerShape(5.dp))
                            .clickable {
                                if (slot == 0) {
                                    pri = idx
                                    StateBridge.weaponPrimary = idx
                                    if (active == StateBridge.weaponIdx) {
                                        active = idx; StateBridge.weaponIdx = idx
                                    }
                                } else {
                                    sec = idx
                                    StateBridge.weaponSecondary = idx
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(p.shortCode, color = Color.White, fontSize = 10.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                repeat(3 - chunk.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(3.dp))
        }

        val p = WeaponPresets.byIndex(current)
        Spacer(Modifier.height(2.dp))
        Text("\"${p.name}\": ${p.notes}", color = Neon.accent3, fontSize = 9.sp)
    }

    Section("INTENSIDADE") {
        Sl("Força geral", 0.3f..2.0f, StateBridge.recoilIntensity) {
            StateBridge.recoilIntensity = it
        }
        Text("1.0 = padrão. >1 = puxa mais forte. <1 = mais suave.",
            color = Neon.dim, fontSize = 9.sp)
    }

    Section("AUTO-DETECT ARMA?") {
        Text("Pra detectar qual arma vc esta usando precisaria ler a tela do FF (MediaProjection). Isso o Garena consegue DETECTAR e banir.",
            color = Neon.dim, fontSize = 9.sp)
        Text("Por isso ficou MANUAL: toca PRI/SEC pra trocar in-game.",
            color = Neon.dim, fontSize = 9.sp)
    }
}

// ============ TAB: MACROS ============

@Composable private fun MacroTab() {
    Section("TIRO") {
        Sw("Burst de 3 (cada tap)",  StateBridge.burstMode)  { StateBridge.burstMode = it }
        Sw("Tap-fire pistolas",       StateBridge.tapFire)    { StateBridge.tapFire = it }
    }
    Section("POSICIONAMENTO") {
        Sw("Auto-crouch ao atirar",   StateBridge.autoCrouch) { StateBridge.autoCrouch = it }
        Text("Agacha automatico durante tiro — menor hitbox.",
            color = Neon.dim, fontSize = 9.sp)
    }
    Section("ROOT ONLY") {
        Text("Aimbot / ESP / Wallhack / No-recoil mem / Speed / Fly",
            color = Neon.dim, fontSize = 10.sp)
        Text("Precisa de root + LSPosed. Ainda nao disponivel aq.",
            color = Neon.dim, fontSize = 9.sp)
    }
}

// ============ TAB: SYS ============

@Composable private fun SysTab(ctx: Context) {
    Section("STEALTH") {
        Sw("Bloquear print/record", StateBridge.flagSecure) { StateBridge.flagSecure = it }
        Spacer(Modifier.height(4.dp))
        GhostBtn("Trocar ícone") { Disguise.cycle(ctx) }
        Spacer(Modifier.height(4.dp))
        GhostBtn("Limpar vestígios") { Stealth.wipe(ctx) }
        Spacer(Modifier.height(4.dp))
        GhostBtn("Sumir dos recentes") { Stealth.hideRecents(ctx) }
    }
    Section("INFO") {
        InfoRow("Versão", "17.0-lite")
        InfoRow("Modo", "Recoil via Shizuku")
        InfoRow("PIN", "1337")
        InfoRow("Root", "não necessário")
    }
}

@Composable private fun InfoRow(k: String, v: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(k, color = Neon.dim, fontSize = 10.sp,
            modifier = Modifier.weight(1f))
        Text(v, color = Neon.accent3, fontSize = 10.sp,
            fontWeight = FontWeight.Bold)
    }
}

// ============ FOOTER ============

@Composable private fun Footer() {
    Row(
        Modifier.fillMaxWidth().background(Neon.sidebar)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.weight(1f).heightIn(min = 30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Neon.danger)
                .clickable { try {
                    RecoilEngine.stop()
                    AssistTrigger.hide()
                    OverlayController.hide()
                } catch (_: Throwable) {} },
            contentAlignment = Alignment.Center
        ) {
            Text("PANIC", color = Color.White, fontSize = 11.sp,
                fontWeight = FontWeight.Bold)
        }
        Box(
            Modifier.weight(1f).heightIn(min = 30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Neon.accent2)
                .clickable { try { OverlayController.hide() } catch (_: Throwable) {} },
            contentAlignment = Alignment.Center
        ) {
            Text("HIDE", color = Color.White, fontSize = 11.sp,
                fontWeight = FontWeight.Bold)
        }
    }
}

// ============ ATOMS ============

@Composable private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .background(Neon.glass)
            .border(1.dp, Neon.stroke.copy(alpha = 0.4f), RoundedCornerShape(7.dp))
            .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(title, color = Neon.accent3, fontSize = 9.sp,
            fontWeight = FontWeight.Black, letterSpacing = 1.2.sp)
        content()
    }
}

@Composable private fun Sw(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    var v by remember { mutableStateOf(value) }
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 26.dp)) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(3.dp))
            .background(if (v) Neon.accent else Color.Transparent)
            .border(1.dp, if (v) Neon.accent3 else Neon.dim, RoundedCornerShape(3.dp))
            .clickable {
                v = !v; try { onChange(v) } catch (_: Throwable) {}
            },
            contentAlignment = Alignment.Center
        ) {
            if (v) Text("✓", color = Color.White, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(label, color = Neon.text, fontSize = 11.sp,
            modifier = Modifier.weight(1f).clickable {
                v = !v; try { onChange(v) } catch (_: Throwable) {}
            })
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

@Composable private fun PrimaryBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.horizontalGradient(
                listOf(Neon.accent2, Neon.accent)))
            .clickable { try { onClick() } catch (_: Throwable) {} },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 11.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable private fun GhostBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 30.dp)
            .clip(RoundedCornerShape(5.dp))
            .border(1.dp, Neon.accent3, RoundedCornerShape(5.dp))
            .clickable { try { onClick() } catch (_: Throwable) {} },
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
