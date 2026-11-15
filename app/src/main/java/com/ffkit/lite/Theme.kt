package com.ffkit.lite
import androidx.compose.ui.graphics.Color

/**
 * Paleta JZN-style: preto + roxo neon. Vermelho vira roxo, cinza
 * permanece. Fundo de cards em cinza escuro suave, nao transparente.
 */
object Neon {
    val bg       = Color(0xFF000000)   // preto puro solido
    val panel    = Color(0xFF0A0B12)   // content area
    val card     = Color(0xFF15161F)   // card background
    val cardHi   = Color(0xFF1E1E2E)   // card active
    val glass    = Color(0xFF15161F)   // alias pra compat
    val sidebar  = Color(0xFF07080F)   // mais escuro que panel
    val stroke   = Color(0xFF3B0764)   // roxo escuro pra borda
    val accent   = Color(0xFF7C3AED)   // roxo principal (botoes)
    val accent2  = Color(0xFF5B21B6)   // roxo mais escuro (gradiente)
    val accent3  = Color(0xFFA78BFA)   // roxo claro (destaque/label)
    val danger   = Color(0xFFEF4444)   // vermelho PANIC
    val text     = Color(0xFFFFFFFF)
    val dim      = Color(0xFF6B7280)   // cinza azulado label
}
