package com.ffkit.lite

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
                listOf("Combat","Visual","World","Util","Stealth","Jogo")
                    .forEachIndexed { i, s -> Tab(selected = tab == i,
                        onClick = { tab = i }, text = { Text(s) }) }
            }
            Spacer(Modifier.height(10.dp))
            when (tab) {
                0 -> CombatTab()
                1 -> VisualTab()
                2 -> WorldTab()
                3 -> UtilTab()
                4 -> StealthTab(ctx)
                5 -> GameTab(ctx)
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
    val fov   = remember { mutableStateOf(StateBridge.aimFov) }
    val sm    = remember { mutableStateOf(StateBridge.aimSmooth) }
    val miss  = remember { mutableStateOf(StateBridge.aimMissRate) }
    val react = remember { mutableStateOf(StateBridge.aimReactionMs.toFloat()) }
    val streak= remember { mutableStateOf(StateBridge.aimMaxHsStreak.toFloat()) }
    Column {
        Text("— AIM —", color = Neon.accent)
        SwitchRow("aimbot",       StateBridge.aimEnabled)    { StateBridge.aimEnabled = it }
        SwitchRow("so mirando",   StateBridge.aimOnScope)    { StateBridge.aimOnScope = it }
        SwitchRow("so atirando",  StateBridge.aimOnFire)     { StateBridge.aimOnFire = it }
        SwitchRow("ignorar bots", StateBridge.aimIgnoreBots) { StateBridge.aimIgnoreBots = it }
        SliderRow("fov",       30f..300f, fov.value)  { fov.value = it; StateBridge.aimFov = it }
        SliderRow("suavidade", 1f..10f,   sm.value)   { sm.value = it; StateBridge.aimSmooth = it }
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
        Text("— OUTROS —", color = Neon.accent3)
        SwitchRow("sem recoil",   StateBridge.noRecoil)      { StateBridge.noRecoil = it }
    }
}

@Composable private fun VisualTab() = Column { Text("visual — toggles em runtime", color = Neon.dim) }
@Composable private fun WorldTab()  = Column { Text("world — nao use em ranked",   color = Neon.dim) }
@Composable private fun UtilTab()   = Column { Text("util",                        color = Neon.dim) }

@Composable private fun StealthTab(ctx: Context) = Column {
    SwitchRow("bloquear screenshot/record", StateBridge.flagSecure){ StateBridge.flagSecure = it }
    Button(onClick = { Disguise.cycle(ctx) })   { Text("trocar icone/nome do app") }
    Button(onClick = { Stealth.wipe(ctx) })      { Text("limpar vestigios") }
    Button(onClick = { Stealth.hideRecents(ctx) }){ Text("esconder dos apps recentes") }
    Text("volume- 3x = PANIC", color = Neon.dim)
}

@Composable private fun GameTab(ctx: Context) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { VA.installFromUri(ctx, it) }
    }
    Column {
        Button(onClick = { picker.launch(arrayOf("application/vnd.android.package-archive","*/*")) }) {
            Text(if (VA.isInstalled()) "atualizar APK do Free Fire" else "importar APK do Free Fire")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { VA.launch(ctx) }, enabled = VA.isInstalled()) {
            Text("abrir Free Fire")
        }
    }
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
