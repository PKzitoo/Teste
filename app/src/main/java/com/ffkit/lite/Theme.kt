package com.ffkit.lite
import androidx.compose.ui.graphics.Color

/**
 * Paleta ROOT v16: preto solido + azul escuro, sem transparencia.
 * Nada mais de roxo neon / FFH4X.  Visual limpo tipo Uriel 2.0.
 */
object Neon {
    val bg       = Color(0xFF000000)   // preto puro solido (sem alpha)
    val panel    = Color(0xFF07080D)   // base levemente azulada
    val glass    = Color(0xFF0B1324)   // cards com tint azul
    val sidebar  = Color(0xFF040611)   // mais escuro que o fundo
    val stroke   = Color(0xFF1E40AF)   // linha azul media
    val accent   = Color(0xFF2563EB)   // azul primario (botoes, selected)
    val accent2  = Color(0xFF1E3A8A)   // azul mais escuro (gradiente)
    val accent3  = Color(0xFF60A5FA)   // azul claro pastel (status/labels)
    val danger   = Color(0xFFFF3860)   // vermelho PANIC
    val text     = Color(0xFFFFFFFF)
    val dim      = Color(0xFF6B7280)   // cinza azulado pra labels
}
