package com.ffkit.lite

/**
 * Padroes de recuo por arma do Free Fire.
 *
 * Cada preset tem:
 *  • pullY: puxada vertical por tick (px). Positivo = puxa pra BAIXO
 *           (compensa a arma pulando pra cima).
 *  • pullX: deriva horizontal por tick (px). Geralmente pequena,
 *           as armas no FF tem recuo meio diagonal esquerda/direita.
 *  • interval: ms entre swipes. 30-40ms = 25-33 Hz, fluido.
 *  • burstLen: numero inicial de ticks mais fortes (primeiros tiros
 *              em geral tem recuo MAIOR, depois estabiliza)
 *  • burstMult: multiplicador aplicado durante os primeiros burstLen
 *               ticks (ex: 1.6x a forca inicial)
 */
data class WeaponPreset(
    val name: String,
    val shortCode: String,
    val pullY: Float,
    val pullX: Float,
    val intervalMs: Int,
    val burstLen: Int,
    val burstMult: Float,
    val notes: String = ""
)

object WeaponPresets {
    val LIST = listOf(
        WeaponPreset("AK47",         "AK",   pullY = 9.0f,  pullX = -0.8f, intervalMs = 32,
                     burstLen = 5, burstMult = 1.6f,
                     notes = "pulo alto, drifta pra esquerda"),

        WeaponPreset("M4A1",         "M4",   pullY = 7.0f,  pullX = 0.3f,  intervalMs = 30,
                     burstLen = 4, burstMult = 1.4f,
                     notes = "subida media, ligeira direita"),

        WeaponPreset("SCAR",         "SCAR", pullY = 6.5f,  pullX = 0.5f,  intervalMs = 32,
                     burstLen = 4, burstMult = 1.3f,
                     notes = "parecido com M4 mas mais vertical"),

        WeaponPreset("M7",           "M7",   pullY = 8.5f,  pullX = 0.4f,  intervalMs = 30,
                     burstLen = 4, burstMult = 1.3f,
                     notes = "AR nivel SCAR, mira sobe sozinha ate scopando"),

        WeaponPreset("MP5",          "MP5",  pullY = 4.5f,  pullX = -0.2f, intervalMs = 24,
                     burstLen = 3, burstMult = 1.3f,
                     notes = "fogo rapido, recuo baixo"),

        WeaponPreset("Vector",       "VEC",  pullY = 3.8f,  pullX = 0.6f,  intervalMs = 22,
                     burstLen = 3, burstMult = 1.2f,
                     notes = "SMG rapido, drifta direita"),

        WeaponPreset("UMP",          "UMP",  pullY = 4.0f,  pullX = 0.0f,  intervalMs = 28,
                     burstLen = 3, burstMult = 1.2f,
                     notes = "SMG estavel"),

        WeaponPreset("Skorp",        "SKP",  pullY = 7.0f,  pullX = 0.2f,  intervalMs = 24,
                     burstLen = 2, burstMult = 1.2f,
                     notes = "metralhadora, subida continua e rapida"),

        WeaponPreset("Groza",        "GRZ",  pullY = 8.5f,  pullX = -0.6f, intervalMs = 30,
                     burstLen = 5, burstMult = 1.5f,
                     notes = "AR pesada, pulo alto"),

        WeaponPreset("XM8",          "XM8",  pullY = 6.0f,  pullX = 0.2f,  intervalMs = 30,
                     burstLen = 4, burstMult = 1.3f,
                     notes = "AR balanceada"),

        WeaponPreset("FAMAS",        "FAM",  pullY = 7.5f,  pullX = -0.4f, intervalMs = 34,
                     burstLen = 4, burstMult = 1.4f,
                     notes = "3-shot burst, pulo alto"),

        WeaponPreset("AWM (sniper)", "AWM",  pullY = 0f,    pullX = 0f,    intervalMs = 100,
                     burstLen = 0, burstMult = 1f,
                     notes = "bolt-action, sem recoil continuo"),

        WeaponPreset("M1887",        "M18",  pullY = 0f,    pullX = 0f,    intervalMs = 100,
                     burstLen = 0, burstMult = 1f,
                     notes = "shotgun, sem recoil continuo"),

        WeaponPreset("Customizado",  "CUS",  pullY = 6f,    pullX = 0f,    intervalMs = 30,
                     burstLen = 4, burstMult = 1.4f,
                     notes = "ajusta nos sliders")
    )

    fun byIndex(i: Int): WeaponPreset =
        LIST.getOrNull(i) ?: LIST[0]
}
