package com.ffkit.lite

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Panel(ctx: Context, onMinimize: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("AIM", "ESP", "CV", "WORLD", "UTIL", "STEALTH")
    Surface(
        modifier = Modifier.width(260.dp),
        color = Neon.bg,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Neon.stroke),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            // header compacto
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp))
                    .background(Brush.linearGradient(listOf(Neon.accent, Neon.accent2))))
                Spacer(Modifier.width(6.dp))
                Text("ffkit", color = Neon.text, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                MiniBtn("_", Neon.accent2) { onMinimize() }
                Spacer(Modifier.width(4.dp))
                MiniBtn("x", Neon.danger) { OverlayController.hide() }
            }
            Spacer(Modifier.height(6.dp))

            // tabs horizontais compactas
            Row(Modifier.fillMaxWidth().horizontalChips()) {
                tabs.forEachIndexed { i, s ->
                    TabChip(s, tab == i) { tab = i }
                }
            }
            Spacer(Modifier.height(8.dp))

            // conteudo rolavel
            Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                when (tab) {
                    0 -> AimTab()
                    1 -> EspTab()
                    2 -> CvTab(ctx)
                    3 -> WorldTab()
                    4 -> UtilTab()
                    5 -> StealthTab(ctx)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Btn("PANIC", Neon.danger, Modifier.weight(1f)) {
                    StateBridge.panic(); OverlayController.hide()
                }
                Btn("HIDE", Neon.accent3, Modifier.weight(1f)) { OverlayController.hide() }
            }
            Spacer(Modifier.height(4.dp))
            Text("VOL+ 3x reabre  •  VOL- 3x PANIC", color = Neon.dim, fontSize = 9.sp)
        }
    }
}

@Composable private fun AimTab() = SectionCol {
    Section("AIMBOT") {
        Sw("ligado",         StateBridge.aimEnabled)    { StateBridge.aimEnabled = it }
        Sw("só mirando",     StateBridge.aimOnScope)    { StateBridge.aimOnScope = it }
        Sw("só atirando",    StateBridge.aimOnFire)     { StateBridge.aimOnFire = it }
        Sw("ignorar bots",   StateBridge.aimIgnoreBots) { StateBridge.aimIgnoreBots = it }
    }
    Section("MIRA") {
        Sl("fov",    30f..600f, StateBridge.aimFov)      { StateBridge.aimFov = it }
        Sl("força",  1f..10f,   StateBridge.aimSmooth)   { StateBridge.aimSmooth = it }
        Sl("sticky", 100f..1500f, StateBridge.aimStickyMs.toFloat()) {
            StateBridge.aimStickyMs = it.toInt()
        }
    }
    Section("HUMANIZAÇÃO") {
        Sw("osso aleatório", StateBridge.aimBoneRandom) { StateBridge.aimBoneRandom = it }
        Sl("erro %",   0f..50f, StateBridge.aimMissRate * 100f) {
            StateBridge.aimMissRate = it / 100f
        }
        Sl("reação",  50f..500f, StateBridge.aimReactionMs.toFloat()) {
            StateBridge.aimReactionMs = it.toInt()
        }
        Sl("cap HS",   0f..10f, StateBridge.aimMaxHsStreak.toFloat()) {
            StateBridge.aimMaxHsStreak = it.toInt()
        }
    }
    Section("OUTROS") {
        Sw("sem recoil", StateBridge.noRecoil) { StateBridge.noRecoil = it }
    }
}

@Composable private fun EspTab() = SectionCol {
    Section("VISUAL") {
        Text("ESP está sempre ligado no modo CV", color = Neon.dim, fontSize = 10.sp)
    }
}

@Composable private fun CvTab(ctx: Context) = SectionCol {
    val activity = ctx as? android.app.Activity
    Section("CV ENGINE") {
        StatusRow("status", if (CvBridge.running) "LIGADO" else "DESLIGADO",
                  if (CvBridge.running) Neon.accent3 else Neon.dim)
        Spacer(Modifier.height(6.dp))
        Btn("ligar CV (pede tela)", Neon.accent, Modifier.fillMaxWidth()) {
            if (!CvBridge.running && activity != null) CvBridge.requestProjection(activity)
        }
        Spacer(Modifier.height(4.dp))
        Btn("desligar CV", Neon.dim, Modifier.fillMaxWidth()) { CvBridge.stop(ctx) }
        Spacer(Modifier.height(6.dp))
        Text("sem injeção, roda com FF original, 0 ban.", color = Neon.dim, fontSize = 9.sp)
    }
}

@Composable private fun WorldTab() = SectionCol {
    Section("MUNDO") {
        Sw("speed hack",    StateBridge.speedHack) { StateBridge.speedHack = it }
        Text("não use em ranked — ban rápido", color = Neon.danger, fontSize = 9.sp)
    }
}

@Composable private fun UtilTab() = SectionCol {
    Section("UTILS") {
        Text("sem opções no modo CV", color = Neon.dim, fontSize = 10.sp)
    }
}

@Composable private fun StealthTab(ctx: Context) = SectionCol {
    Section("STEALTH") {
        Sw("bloquear print/record", StateBridge.flagSecure) { StateBridge.flagSecure = it }
        Spacer(Modifier.height(4.dp))
        Btn("trocar ícone do app", Neon.accent2, Modifier.fillMaxWidth()) { Disguise.cycle(ctx) }
        Spacer(Modifier.height(4.dp))
        Btn("limpar vestígios",    Neon.accent3, Modifier.fillMaxWidth()) { Stealth.wipe(ctx) }
        Spacer(Modifier.height(4.dp))
        Btn("sumir dos recentes",  Neon.accent,  Modifier.fillMaxWidth()) { Stealth.hideRecents(ctx) }
    }
}

// ============ atomos UI ============

@Composable private fun SectionCol(content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
}

@Composable private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Neon.glass)
            .border(1.dp, Neon.stroke.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(title, color = Neon.accent, fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.em)
        content()
    }
}

@Composable private fun Sw(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    var v by remember { mutableStateOf(value) }
    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp)) {
        Text(label, color = Neon.text, fontSize = 11.sp,
            modifier = Modifier.weight(1f))
        Switch(checked = v, onCheckedChange = {
            v = it; try { onChange(it) } catch (_: Throwable) {}
        }, modifier = Modifier.scale(0.75f))
    }
}

@Composable private fun Sl(label: String, range: ClosedFloatingPointRange<Float>,
                           value: Float, onChange: (Float) -> Unit) {
    var v by remember { mutableStateOf(value) }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Neon.text, fontSize = 10.sp,
                modifier = Modifier.weight(1f))
            Text(formatVal(v), color = Neon.accent2, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
        }
        Slider(
            value = v, onValueChange = {
                v = it; try { onChange(it) } catch (_: Throwable) {}
            },
            valueRange = range,
            modifier = Modifier.height(20.dp),
            colors = SliderDefaults.colors(
                thumbColor = Neon.accent,
                activeTrackColor = Neon.accent,
                inactiveTrackColor = Neon.dim.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable private fun StatusRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Neon.text, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun Btn(label: String, color: androidx.compose.ui.graphics.Color,
                             modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.heightIn(min = 30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .clickableSafe(onClick),
        color = androidx.compose.ui.graphics.Color.Transparent
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 6.dp),
            contentAlignment = Alignment.Center) {
            Text(label, color = color, fontSize = 10.sp,
                fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun MiniBtn(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Box(
        Modifier.size(22.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(5.dp))
            .clickableSafe(onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) Neon.accent else Neon.dim
    Box(
        Modifier.padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) color.copy(alpha = 0.25f) else androidx.compose.ui.graphics.Color.Transparent)
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .clickableSafe(onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(label, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatVal(v: Float): String {
    val r = v.toInt().toFloat()
    return if (v == r) r.toInt().toString() else String.format("%.1f", v)
}

private fun Modifier.clickableSafe(onClick: () -> Unit) =
    this.then(androidx.compose.foundation.clickable { try { onClick() } catch (_: Throwable) {} })

private fun Modifier.horizontalChips() = this.then(
    androidx.compose.foundation.horizontalScroll(androidx.compose.foundation.rememberScrollState(null) as androidx.compose.foundation.ScrollState)
)
