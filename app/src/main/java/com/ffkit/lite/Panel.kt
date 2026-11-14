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
 * Painel ffkit v18-root-legit.
 * Focado em uso com ROOT + LSPosed. Sem overlay de botao, sem Shizuku.
 * O painel escreve state.json e o modulo nativo dentro do FF le e aplica.
 *
 * 3 tabs: AIM / MISC / SYS.  ESP omitido de proposito (user quer "legit"
 * — nada visual atraves da parede).
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
        Text("v18-legit", color = Color.White.copy(alpha = 0.6f),
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
    val items = listOf("A" to "AIM", "M" to "MISC", "S" to "SYS")
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
            0 -> AimTab()
            1 -> MiscTab()
            2 -> SysTab(ctx)
        }
    }
}

// ============ TAB: AIM ============

@Composable private fun AimTab() {
    Section("AIMBOT") {
        Sw("Ativar aimbot",    StateBridge.aimEnabled)    { StateBridge.aimEnabled = it }
        Sw("Só atirando",      StateBridge.aimOnFire)     { StateBridge.aimOnFire = it }
        Sw("Só mirando (ADS)", StateBridge.aimOnScope)    { StateBridge.aimOnScope = it }
        Sw("Ignorar bots",     StateBridge.aimIgnoreBots) { StateBridge.aimIgnoreBots = it }
    }

    Section("MIRA") {
        Sl("Aim FOV",   30f..600f, StateBridge.aimFov)    { StateBridge.aimFov = it }
        Sl("Suavidade", 1f..10f,   StateBridge.aimSmooth) { StateBridge.aimSmooth = it }
        Sl("Reação ms", 50f..500f, StateBridge.aimReactionMs.toFloat()) {
            StateBridge.aimReactionMs = it.toInt()
        }
    }

    Section("PADRÃO DE TIRO (anti-ban)") {
        PatternPreview()
        Spacer(Modifier.height(6.dp))
        Sl("Tapas (HS)",  0f..10f, StateBridge.shotHeadCount.toFloat()) {
            StateBridge.shotHeadCount = it.toInt()
        }
        Sl("Peitos",      0f..10f, StateBridge.shotBodyCount.toFloat()) {
            StateBridge.shotBodyCount = it.toInt()
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Preset("BOT\n5H+0P", 5, 0)
            Preset("AGGR\n3H+1P", 3, 1)
            Preset("LEGIT\n2H+2P", 2, 2)
            Preset("SAFE\n1H+3P", 1, 3)
        }
        Spacer(Modifier.height(3.dp))
        Text("BOT = banna em dias. SAFE = quase nunca banna mas mata devagar. LEGIT = bom meio-termo pra ranqueada.",
            color = Neon.dim, fontSize = 9.sp)
    }
}

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
        // visual pattern
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(h.coerceAtMost(10)) {
                Box(Modifier.size(width = 16.dp, height = 10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Neon.accent3))
            }
            repeat(b.coerceAtMost(10)) {
                Box(Modifier.size(width = 16.dp, height = 10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Neon.dim))
            }
        }
        Spacer(Modifier.height(2.dp))
        Text("verde=cabeça  cinza=peito  (repete em loop)",
            color = Neon.dim, fontSize = 8.sp)
    }
}

@Composable private fun RowScope.Preset(label: String, h: Int, b: Int) {
    val sel = StateBridge.shotHeadCount == h && StateBridge.shotBodyCount == b
    Box(
        Modifier.weight(1f).heightIn(min = 36.dp)
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
        Text(label, color = Color.White, fontSize = 8.sp,
            fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

// ============ TAB: MISC ============

@Composable private fun MiscTab() {
    Section("ARMAS (patch memoria)") {
        Sw("No-Recoil (auto todas armas)", StateBridge.noRecoil) { StateBridge.noRecoil = it }
        Sw("Fast Fire",                     StateBridge.fastFire)  { StateBridge.fastFire = it }
        Spacer(Modifier.height(3.dp))
        Text("No-recoil roda direto na memoria: detecta a arma que vc segura e zera o recuo. Zero tuning, zero botao.",
            color = Neon.dim, fontSize = 9.sp)
    }
    Section("CAMERA") {
        Sw("Wide View (FOV)",   StateBridge.wideView)    { StateBridge.wideView = it }
    }
    Section("USO AGRESSIVO (ban rapido)") {
        Sw("Speed Hack",        StateBridge.speedHack)   { StateBridge.speedHack = it }
        if (StateBridge.speedHack) {
            Sl("Mult", 1f..3f, StateBridge.speedMult) { StateBridge.speedMult = it }
        }
        Sw("Fly Mode",          StateBridge.flyMode)     { StateBridge.flyMode = it }
        Spacer(Modifier.height(3.dp))
        Text("⚠ Speed/Fly sao MUITO visiveis. Use so em privada.",
            color = Neon.danger, fontSize = 9.sp)
    }
    Section("ESP (visual)") {
        Sw("ESP ativo",         StateBridge.espEnabled)  { StateBridge.espEnabled = it }
        if (StateBridge.espEnabled) {
            Sw("Caixa 2D",       StateBridge.espBox)       { StateBridge.espBox = it }
            Sw("Barra de vida",  StateBridge.espHealth)    { StateBridge.espHealth = it }
            Sw("Distância",      StateBridge.espDistance)  { StateBridge.espDistance = it }
            Sw("Nome",           StateBridge.espName)      { StateBridge.espName = it }
            Sl("Distância max", 20f..500f, StateBridge.espMaxDist) {
                StateBridge.espMaxDist = it
            }
        }
        Spacer(Modifier.height(3.dp))
        Text("ESP = wall. Se o inimigo report, replays mostram tu virando pra ele por paredes.",
            color = Neon.dim, fontSize = 9.sp)
    }
}

// ============ TAB: SYS ============

@Composable private fun SysTab(ctx: Context) {
    Section("STEALTH") {
        Sw("Bloquear print/record", StateBridge.flagSecure) {
            StateBridge.flagSecure = it
            OverlayController.refreshFlags()   // aplica na hora
        }
        Spacer(Modifier.height(4.dp))
        GhostBtn("📷 LIBERAR PRINT (10s)") {
            OverlayController.allowPrintFor(ctx, 10)
        }
        Spacer(Modifier.height(4.dp))
        GhostBtn("Trocar ícone") { Disguise.cycle(ctx) }
        Spacer(Modifier.height(4.dp))
        GhostBtn("Limpar vestígios") { Stealth.wipe(ctx) }
        Spacer(Modifier.height(4.dp))
        GhostBtn("Sumir dos recentes") { Stealth.hideRecents(ctx) }
    }
    Section("INFO") {
        InfoRow("Versão",  "18.0-legit")
        InfoRow("Modo",    "LSPosed + Zygisk")
        InfoRow("PIN",     "1337")
        InfoRow("Panic",   "Botão abaixo")
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
                    StateBridge.panic(); OverlayController.hide()
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
    val toggle: () -> Unit = {
        v = !v; try { onChange(v) } catch (_: Throwable) {}
    }
    val noRipple = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp)
            .clickable(interactionSource = noRipple, indication = null) { toggle() }
            .padding(vertical = 2.dp)) {
        // area de hit maior em volta do checkbox (18dp em vez de 14dp)
        Box(Modifier.size(18.dp).clip(RoundedCornerShape(3.dp))
            .background(if (v) Neon.accent else Color.Transparent)
            .border(1.dp, if (v) Neon.accent3 else Neon.dim, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (v) Text("✓", color = Color.White, fontSize = 12.sp,
                fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(8.dp))
        Text(label, color = Neon.text, fontSize = 11.sp,
            modifier = Modifier.weight(1f))
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
    val noRipple = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        Modifier.fillMaxWidth().heightIn(min = 32.dp)
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
