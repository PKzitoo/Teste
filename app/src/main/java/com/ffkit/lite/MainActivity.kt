package com.ffkit.lite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val expectedLogin = "PKxitff"
    private val expectedPass  = "pk020807"

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var authed by remember { mutableStateOf(false) }
                if (authed) PanelLauncher() else LoginScreen(onOk = { authed = true })
            }
        }
    }

    @Composable
    private fun LoginScreen(onOk: () -> Unit) {
        var user by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }

        // paleta "banco verde": fundo claro, botao escuro
        val bgGreen     = Color(0xFF9FE870)
        val darkGreen   = Color(0xFF163300)
        val midGreen    = Color(0xFF3A6B23)
        val fieldBg     = Color.White

        Box(
            Modifier.fillMaxSize().background(bgGreen)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // avatar circular com o ic_wise (mesmo icone do launcher)
                Box(
                    Modifier.size(80.dp).clip(CircleShape).background(darkGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_wise),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).clip(CircleShape)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Entrar na sua conta", color = darkGreen, fontSize = 22.sp,
                    fontWeight = FontWeight.Black)
                Spacer(Modifier.height(4.dp))
                Text("Acesso rápido e seguro",
                    color = darkGreen.copy(alpha = 0.6f), fontSize = 13.sp)

                Spacer(Modifier.height(28.dp))

                Field(
                    label = "Email ou usuário",
                    value = user,
                    onChange = { user = it },
                    bg = fieldBg, text = darkGreen
                )
                Spacer(Modifier.height(10.dp))
                Field(
                    label = "Senha",
                    value = pass,
                    onChange = { pass = it },
                    password = true,
                    bg = fieldBg, text = darkGreen
                )

                Spacer(Modifier.height(20.dp))

                Box(
                    Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(darkGreen)
                        .clickable {
                            if (user == expectedLogin && pass == expectedPass) {
                                onOk()
                            }
                            // credencial errada: silencio total (user nao sabe
                            // se existe outro comportamento)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Entrar", color = bgGreen, fontSize = 15.sp,
                        fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))
                Text("Esqueci minha senha",
                    color = midGreen, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { /* no-op */ })

                Spacer(Modifier.height(36.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Novo por aqui? ",
                        color = darkGreen.copy(alpha = 0.6f), fontSize = 12.sp)
                    Text("Criar conta",
                        color = darkGreen, fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* no-op */ })
                }
            }
        }
    }

    @Composable
    private fun Field(label: String, value: String, onChange: (String) -> Unit,
                      password: Boolean = false,
                      bg: Color, text: Color) {
        Column(Modifier.fillMaxWidth()) {
            Text(label, color = text.copy(alpha = 0.7f), fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            TextField(
                value = value,
                onValueChange = onChange,
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg)
                    .border(1.dp, text.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                singleLine = true,
                visualTransformation = if (password) PasswordVisualTransformation()
                                      else androidx.compose.ui.text.input.VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (password) KeyboardType.Password
                                   else KeyboardType.Text
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = bg,
                    unfocusedContainerColor = bg,
                    focusedTextColor = text,
                    unfocusedTextColor = text,
                    cursorColor = text,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
        }
    }

    @Composable
    private fun PanelLauncher() {
        var msg by remember { mutableStateOf("carregando...") }
        LaunchedEffect(Unit) {
            try {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    msg = "preciso da permissao 'aparecer sobre outros apps'. Volte, conceda em Ajustes -> Apps -> Wise -> Permissoes, e abra o app de novo."
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                    return@LaunchedEffect
                }
                StateBridge.appCtx = applicationContext
                try { StateManager.save(applicationContext) } catch (_: Throwable) {}
                OverlayController.ensure(applicationContext)
                OverlayController.show()
                OverlayController.openPanel()
                finish()
            } catch (t: Throwable) {
                msg = "erro: ${t.javaClass.simpleName}: ${t.message}"
            }
        }
        Text(msg)
    }
}
