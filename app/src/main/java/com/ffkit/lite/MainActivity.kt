package com.ffkit.lite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private val pin = "1337"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CvBridge.REQ_PROJECTION) {
            CvBridge.onProjectionResult(applicationContext, resultCode, data)
        }
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var typed by remember { mutableStateOf("") }
                var ok by remember { mutableStateOf(false) }
                if (ok) PanelLauncher() else
                    Column(Modifier.padding(24.dp)) {
                        Text("Calculadora")
                        OutlinedTextField(
                            value = typed,
                            onValueChange = { typed = it },
                            label = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                        )
                        Button(onClick = { if (typed == pin) ok = true }) { Text("=") }
                    }
            }
        }
    }

    @Composable
    private fun PanelLauncher() {
        LaunchedEffect(Unit) {
            if (!Settings.canDrawOverlays(this@MainActivity)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")))
            } else {
                StateBridge.appCtx = applicationContext
                StateManager.save(applicationContext)  // garante state.json inicial
                OverlayController.ensure(applicationContext)
                OverlayController.show()
                OverlayController.openPanel()
                finish()
            }
        }
        Text("carregando...")
    }
}
