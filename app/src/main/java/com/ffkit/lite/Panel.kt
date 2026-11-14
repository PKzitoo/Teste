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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Panel(ctx: Context, onMinimize: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Surface(
        modifier = Modifier.width(300.dp),
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
            .background(Brush.horizontalGradient(
                listOf(Neon.accent2, Neon.accent)))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(Color.White))
        Spacer(Modifier.width(8.dp))
        Text("FFKIT", color = Color.White, fontSize = 11.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.width(6.dp))
        Text("@OLIVIA", color = Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp)
        Spacer(Modifier.weight(1f))
        HeaderBtn("_") { onMinimize() }
        Spacer(Modifier.width(4.dp))
        HeaderBtn("X") { OverlayController.hide() }
    }
}

@Composable private fun HeaderBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { try { onClick() } catch (_: Throwable) {} },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 10.sp,
            fontWeight = FontWeight.Bold)
    }
}

// ============ SIDEBAR ============

@Composable private fun Sidebar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf("A" to "AIM", "E" to "ESP", "C" to "CV", "S" to "SYS")
    Column(
        Modifier.width(52.dp).fillMaxHeight()
            .background(Neon.sidebar)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { i, (icon, label) ->
            SideIcon(icon, label, selected == i) { onSelect(i) }
        }
    }
}

@Composable private fun SideIcon(icon: String, label: String,
                                 selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Neon.accent else Neon.accent2.copy(alpha = 0.4f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(40.dp)
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                .background(bg)
                .border(1.dp,
                    if (selected) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                    RoundedCornerShape(8.dp))
                .clickable { try { onClick() } catch (_: Throwable) {} },
            contentAlignment = Alignment.Center
        ) {
            Text(icon, color = Color.White, fontSize = 18.sp,
                fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(2.dp))
        Text(label, color = if (selected) Neon.accent else Neon.dim,
            fontSize = 7.sp, fontWeight = FontWeight.Bold)
    }
}

// ============ CONTENT ============

@Composable private fun RowScope.Content(ctx: Context, tab: Int) {
    Column(
        Modifier.weight(1f).heightIn(min = 280.dp, max = 400.dp)
            .background(Neon.panel)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (tab) {
            0 -> AimTab()
            1 -> EspTab()
            2 -> CvTab(ctx)
            3 -> SysTab(ctx)
        }
    }
}

@Composable private fun AimTab() {
    Section("AIMBOT") {
        Sw("Ativar aimbot",    StateBridge.aimEnabled)    { StateBridge.aimEnabled = it }
        Sw("Aimbot Legit",     StateBridge.aimOnFire)     { StateBridge.aimOnFire = it }
        Sw("Só mirando",       StateBridge.aimOnScope)    { StateBridge.aimOnScope = it }
        Sw("Ignorar bots",     StateBridge.aimIgnoreBots) { StateBridge.aimIgnoreBots = it }
        BoneSelect()
    }
    Section("MIRA") {
        Sl("Aim FOV",       30f..600f,   StateBridge.aimFov)    { StateBridge.aimFov = it }
        Sl("Força",         1f..10f,     StateBridge.aimSmooth) { StateBridge.aimSmooth = it }
        Sl("Stickiness ms", 100f..1500f, StateBridge.aimStickyMs.toFloat()) {
            StateBridge.aimStickyMs = it.toInt()
        }
    }
    Section("HUMANIZAÇÃO") {
        Sw("Osso aleatório", StateBridge.aimBoneRandom){ StateBridge.aimBoneRandom = it }
        Sl("Erro %",      0f..50f,  StateBridge.aimMissRate * 100f) {
            StateBridge.aimMissRate = it / 100f
        }
        Sl("Reação ms",   50f..500f, StateBridge.aimReactionMs.toFloat()) {
            StateBridge.aimReactionMs = it.toInt()
        }
        Sl("Cap HS seg",  0f..10f,   StateBridge.aimMaxHsStreak.toFloat()) {
            StateBridge.aimMaxHsStreak = it.toInt()
        }
    }
}

@Composable private fun BoneSelect() {
    val labels = listOf("Cabeça", "Pescoço", "Peito")
    var sel by remember { mutableStateOf(StateBridge.aimBone) }
    Column {
        Text("Tipo de Aimbot", color = Neon.dim, fontSize = 10.sp)
        Spacer(Modifier.height(3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            labels.forEachIndexed { i, lb ->
                val active = sel == i
                Box(
                    Modifier.weight(1f).heightIn(min = 26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (active) Neon.accent else Neon.glass)
                        .border(1.dp,
                            if (active) Neon.accent else Neon.stroke.copy(alpha = 0.3f),
                            RoundedCornerShape(5.dp))
                        .clickable { sel = i; try { StateBridge.aimBone = i } catch (_: Throwable) {} },
                    contentAlignment = Alignment.Center
                ) {
                    Text(lb, color = Color.White, fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable private fun EspTab() {
    Section("ESP") {
        Text("ESP ativo por padrão no modo CV.",
            color = Neon.text, fontSize = 10.sp)
        Text("Caixa + ponto na cabeça aparecem em cima de todo inimigo detectado em tempo real.",
            color = Neon.dim, fontSize = 9.sp)
    }
    Section("FILTRO") {
        Sw("Marcar bots", StateBridge.aimIgnoreBots.not().let { true }) { }
        Text("distância max: 150m (pegada pelo CV engine)",
            color = Neon.dim, fontSize = 9.sp)
    }
}

@Composable private fun CvTab(ctx: Context) {
    Section("CV ENGINE") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Status", color = Neon.text, fontSize = 11.sp,
                modifier = Modifier.weight(1f))
            val live = CvBridge.running
            Box(Modifier.size(7.dp).clip(CircleShape)
                .background(if (live) Neon.accent3 else Neon.dim))
            Spacer(Modifier.width(4.dp))
            Text(if (live) "LIGADO" else "DESLIGADO",
                color = if (live) Neon.accent3 else Neon.dim,
                fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        PrimaryBtn("Ativar CV (pede tela)") {
            if (!CvBridge.running) CvBridge.requestProjection(ctx)
        }
        Spacer(Modifier.height(4.dp))
        GhostBtn("Desativar CV") { CvBridge.stop(ctx) }
        Spacer(Modifier.height(4.dp))
        Text("Sem injeção. Não muda nada no FF. Permissão de gravação de tela é inofensiva.",
            color = Neon.dim, fontSize = 9.sp)
    }
}

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
    Section("ATALHOS") {
        Text("VOL+ 3x reabre bolha", color = Neon.text, fontSize = 10.sp)
        Text("VOL- 3x = PANIC total", color = Neon.text, fontSize = 10.sp)
        Text("Toque longo pra arrastar o painel ou a bolha",
            color = Neon.dim, fontSize = 9.sp)
    }
}

// ============ FOOTER ============

@Composable private fun Footer() {
    Row(
        Modifier.fillMaxWidth().background(Neon.sidebar)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.weight(1f).heightIn(min = 28.dp)
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
            Modifier.weight(1f).heightIn(min = 28.dp)
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

// ============ ATOMS ============

@Composable private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Neon.glass)
            .border(1.dp, Neon.stroke.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(title, color = Neon.accent, fontSize = 9.sp,
            fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        content()
    }
}

@Composable private fun Sw(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    var v by remember { mutableStateOf(value) }
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 26.dp)) {
        Box(Modifier.size(14.dp).clip(RoundedCornerShape(3.dp))
            .background(if (v) Neon.accent else Color.Transparent)
            .border(1.dp, if (v) Neon.accent else Neon.dim, RoundedCornerShape(3.dp))
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
            Text(formatVal(v), color = Neon.accent, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
        }
        Slider(
            value = v, onValueChange = {
                v = it; try { onChange(it) } catch (_: Throwable) {}
            },
            valueRange = range,
            modifier = Modifier.height(22.dp),
            colors = SliderDefaults.colors(
                thumbColor = Neon.accent,
                activeTrackColor = Neon.accent,
                inactiveTrackColor = Neon.dim.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable private fun PrimaryBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 32.dp)
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
            .border(1.dp, Neon.accent, RoundedCornerShape(5.dp))
            .clickable { try { onClick() } catch (_: Throwable) {} },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Neon.accent, fontSize = 10.sp,
            fontWeight = FontWeight.Bold)
    }
}

private fun formatVal(v: Float): String {
    val r = v.toInt().toFloat()
    return if (v == r) r.toInt().toString() else String.format("%.1f", v)
}
