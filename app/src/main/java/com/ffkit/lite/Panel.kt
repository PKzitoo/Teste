package com.ffkit.lite

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Panel(ctx: Context, onMinimize: () -> Unit) {
    var tab by remember { mutableStateOf(0) }
    Surface(Modifier.width(320.dp), color = Neon.bg, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(14.dp)) {
            Text("ffkit", color = Neon.accent)
            TabRow(selectedTabIndex = tab) {
                listOf("Combat","Visual","CV","World","Util","Stealth","Jogo")
                    .forEachIndexed { i, s -> Tab(selected = tab == i,
                        onClick = { tab = i }, text = { Text(s) }) }
            }
            Spacer(Modifier.height(10.dp))
            when (tab) {
                0 -> CombatTab()
                1 -> VisualTab()
                2 -> CvTab(ctx)
                3 -> WorldTab()
                4 -> UtilTab()
                5 -> StealthTab(ctx)
                6 -> GameTab(ctx)
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Button(onClick = onMinimize) { Text("minimizar") }
                Button(onClick = { OverlayController.hide() }) { Text("esconder") }
                Button(onClick = { StateBridge.panic(); OverlayController.hide() },
                    colors = ButtonDefaults.buttonColors(containerColor = Neon.danger)) {
                    Text("PANIC")
                }
            }
            Text("VOL+ 3x reabre a bolha  •  VOL- 3x PANIC",
                color = Neon.dim)
        }
    }
}

@Composable private fun CombatTab() {
    val fov    = remember { mutableStateOf(StateBridge.aimFov) }
    val sm     = remember { mutableStateOf(StateBridge.aimSmooth) }
    val miss   = remember { mutableStateOf(StateBridge.aimMissRate) }
    val react  = remember { mutableStateOf(StateBridge.aimReactionMs.toFloat()) }
    val streak = remember { mutableStateOf(StateBridge.aimMaxHsStreak.toFloat()) }
    val sticky = remember { mutableStateOf(StateBridge.aimStickyMs.toFloat()) }
    Column {
        Text("— AIM —", color = Neon.accent)
        SwitchRow("aimbot",       StateBridge.aimEnabled)    { StateBridge.aimEnabled = it }
        SwitchRow("so mirando",   StateBridge.aimOnScope)    { StateBridge.aimOnScope = it }
        SwitchRow("so atirando",  StateBridge.aimOnFire)     { StateBridge.aimOnFire = it }
        SwitchRow("ignorar bots", StateBridge.aimIgnoreBots) { StateBridge.aimIgnoreBots = it }
        SliderRow("fov (raio em px)",  30f..600f, fov.value)  { fov.value = it; StateBridge.aimFov = it }
        SliderRow("forca do pull 1-10", 1f..10f,  sm.value)  { sm.value = it; StateBridge.aimSmooth = it }
        SliderRow("stickiness (ms)",   100f..1500f, sticky.value) {
            sticky.value = it; StateBridge.aimStickyMs = it.toInt()
        }
        Spacer(Modifier.height(6.dp))
        Text("— COMPORTAMENTO (anti-ban) —", color = Neon.accent2)
        SwitchRow("osso aleatorio",  StateBridge.aimBoneRandom){ StateBridge.aimBoneRandom = it }
        SliderRow("taxa de erro %",      0f..50f, miss.value * 100f) {
            miss.value = it / 100f; StateBridge.aimMissRate = miss.value
        }
        SliderRow("atraso reacao (ms)", 50f..500f, react.value) {
            react.value = it; StateBridge.aimReactionMs = it.toInt()
        }
        SliderRow("cap HS consec.",     0f..10f,  streak.value) {
            streak.value = it; StateBridge.aimMaxHsStreak = it.toInt()
        }
        Spacer(Modifier.height(6.dp))
        Text("DICA: forca 3-5 = humano, 6-7 = agressivo mas safe, 8+ = snap",
            color = Neon.dim)
        Spacer(Modifier.height(6.dp))
        Text("— OUTROS —", color = Neon.accent3)
        SwitchRow("sem recoil",   StateBridge.noRecoil)      { StateBridge.noRecoil = it }
    }
}

@Composable private fun VisualTab() = Column { Text("visual — ESP ligado por padrao", color = Neon.dim) }
@Composable private fun WorldTab()  = Column { Text("world — nao use em ranked",   color = Neon.dim) }
@Composable private fun UtilTab()   = Column { Text("util",                        color = Neon.dim) }

@Composable private fun CvTab(ctx: android.content.Context) = Column {
    val activity = ctx as? android.app.Activity
    Text("CV externo — zero injecao, roda com FF original", color = Neon.accent3)
    androidx.compose.foundation.layout.Spacer(Modifier.height(6.dp))
    Text("Status: ${if (CvBridge.running) "LIGADO" else "DESLIGADO"}", color = Neon.text)
    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
    Button(onClick = {
        if (!CvBridge.running && activity != null) CvBridge.requestProjection(activity)
    }) { Text("ligar CV (pede permissao de tela)") }
    androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
    Button(onClick = { CvBridge.stop(ctx) }) { Text("desligar CV") }
    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
    Text("ao ligar, o Android pede permissao de 'gravar tela'. " +
         "Nao gera video — eh a unica forma de ler o que o FF mostra " +
         "sem injetar no processo dele.", color = Neon.dim)
}

@Composable private fun StealthTab(ctx: Context) = Column {
    SwitchRow("bloquear screenshot/record", StateBridge.flagSecure){ StateBridge.flagSecure = it }
    Button(onClick = { Disguise.cycle(ctx) })   { Text("trocar icone/nome do app") }
    Button(onClick = { Stealth.wipe(ctx) })      { Text("limpar vestigios") }
    Button(onClick = { Stealth.hideRecents(ctx) }){ Text("esconder dos apps recentes") }
    Text("volume- 3x = PANIC", color = Neon.dim)
}

@Composable private fun GameTab(ctx: Context) = Column {
    Text("rota VA desativada (stubs). Use CV ou LSPatch.", color = Neon.dim)
}

@Composable private fun SwitchRow(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Neon.text, modifier = Modifier.weight(1f))
        Switch(checked = value, onCheckedChange = onChange)
    }
}

@Composable private fun SliderRow(label: String, range: ClosedFloatingPointRange<Float>,
                                   value: Float, onChange: (Float) -> Unit) {
    Column { Text(label, color = Neon.text)
        Slider(value = value, onValueChange = onChange, valueRange = range) }
}
