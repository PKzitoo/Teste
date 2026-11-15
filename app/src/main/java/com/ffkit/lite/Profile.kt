package com.ffkit.lite

import androidx.compose.runtime.mutableIntStateOf

/**
 * Perfis de uso: LEGIT (seguro, 25% HS, so safe-tier),
 * AGGR (mediano, ESP legit + algumas features M-tier, 75% HS),
 * RAGE (tudo on, 100% HS, wallhack full).
 *
 * refreshKey eh observavel por Compose — toda vez que apply() rodar,
 * o key bumpa e o painel recompose, atualizando switches/sliders na
 * hora sem precisar fechar/abrir.
 */
object Profile {
    enum class P { LEGIT, AGGR, RAGE, CUSTOM }

    val refreshKey = mutableIntStateOf(0)
    fun bump() { refreshKey.intValue = refreshKey.intValue + 1 }

    fun apply(p: P) {
        when (p) {
            P.LEGIT -> applyLegit()
            P.AGGR  -> applyAggr()
            P.RAGE  -> applyRage()
            P.CUSTOM -> {}
        }
        bump()
    }

    /** Detecta qual perfil bate com o state atual (ou CUSTOM) */
    fun current(): P = when {
        matchesLegit() -> P.LEGIT
        matchesAggr()  -> P.AGGR
        matchesRage()  -> P.RAGE
        else           -> P.CUSTOM
    }

    private fun applyLegit() {
        val s = StateBridge
        // AIM: so o basico
        s.aimEnabled = true
        s.aimOnFire = true
        s.aimOnScope = true
        s.aimIgnoreBots = true
        s.aimPrediction = true
        s.aimMultiTarget = false
        s.aimSilent = false
        s.aimFov = 70f
        s.aimSmooth = 4f
        s.aimReactionMs = 280
        s.shotHeadCount = 1
        s.shotBodyCount = 3  // 25% HS
        // ESP: desligado (zero wall)
        s.espMode = 0
        // WEAP: so no-recoil
        s.noRecoil = true
        s.noSpread = false
        s.noReload = false
        s.fastFire = false
        s.infiniteAmmo = false
        s.oneShotHs = false
        s.anyGunAwm = false
        s.pierceWalls = false
        s.magicBullet = false
        // MOVE: so os invisiveis
        s.noFallDmg = true
        s.waterBreath = true
        s.speedHack = false
        s.superJump = false
        s.bhop = false
        s.instantRevive = false
        s.wideView = false
        s.flyMode = false
        s.noCollision = false
        // INTEL: so baixo-risco
        s.grenadeAlert = true
        s.zoneTimer = true
        s.airdropTracker = true
        s.lootEsp = false
        s.vehicleEsp = false
        s.miniMapEnemy = false
        s.killFeedSpy = false
        // SAFE: TUDO on
        s.spectatorWarn = true
        s.streamerMode = true
        s.hsCapDynamic = true
        s.replaySanit = true
        s.signatureRot = true
        s.autoOffKills = 15  // desliga apos 15 kills
        s.flagSecure = true
    }

    private fun applyAggr() {
        val s = StateBridge
        s.aimEnabled = true
        s.aimOnFire = true
        s.aimOnScope = false  // aim livre tambem
        s.aimIgnoreBots = true
        s.aimPrediction = true
        s.aimMultiTarget = true
        s.aimSilent = false
        s.aimFov = 150f
        s.aimSmooth = 3f
        s.aimReactionMs = 180
        s.shotHeadCount = 3
        s.shotBodyCount = 1  // 75% HS
        s.espMode = 1  // LEGIT ESP
        s.espBox = true
        s.espSkeleton = false
        s.espHealth = true
        s.espDistance = true
        s.espName = false
        s.espHideScope = true
        s.espMaxDist = 150f
        s.noRecoil = true
        s.noSpread = true
        s.noReload = true
        s.fastFire = true
        s.infiniteAmmo = false
        s.oneShotHs = false
        s.anyGunAwm = false
        s.pierceWalls = false
        s.magicBullet = false
        s.noFallDmg = true
        s.waterBreath = true
        s.speedHack = true
        s.speedMult = 1.3f
        s.superJump = false
        s.bhop = false
        s.instantRevive = true
        s.wideView = true
        s.flyMode = false
        s.noCollision = false
        s.grenadeAlert = true
        s.zoneTimer = true
        s.airdropTracker = true
        s.lootEsp = true
        s.vehicleEsp = true
        s.miniMapEnemy = true
        s.killFeedSpy = false
        s.spectatorWarn = true
        s.streamerMode = true
        s.hsCapDynamic = true
        s.replaySanit = true
        s.signatureRot = true
        s.autoOffKills = 10
        s.flagSecure = true
    }

    private fun applyRage() {
        val s = StateBridge
        // TUDO OP
        s.aimEnabled = true
        s.aimOnFire = false  // dispara sem precisar segurar fire
        s.aimOnScope = false
        s.aimIgnoreBots = false  // mira tambem em bot pra inflar K/D
        s.aimPrediction = true
        s.aimMultiTarget = true
        s.aimSilent = true
        s.aimFov = 600f
        s.aimSmooth = 1f  // snap maximo
        s.aimReactionMs = 50
        s.shotHeadCount = 5
        s.shotBodyCount = 0  // 100% HS
        s.espMode = 2  // FULL wallhack
        s.espBox = true
        s.espSkeleton = true
        s.espHealth = true
        s.espDistance = true
        s.espName = true
        s.espHideScope = false
        s.espMaxDist = 500f
        s.noRecoil = true
        s.noSpread = true
        s.noReload = true
        s.fastFire = true
        s.infiniteAmmo = true
        s.oneShotHs = true
        s.anyGunAwm = true
        s.pierceWalls = true
        s.magicBullet = true
        s.noFallDmg = true
        s.waterBreath = true
        s.speedHack = true
        s.speedMult = 2f
        s.superJump = true
        s.bhop = true
        s.instantRevive = true
        s.wideView = true
        s.flyMode = true
        s.noCollision = true
        s.grenadeAlert = true
        s.zoneTimer = true
        s.airdropTracker = true
        s.lootEsp = true
        s.vehicleEsp = true
        s.miniMapEnemy = true
        s.killFeedSpy = true
        s.spectatorWarn = true
        s.streamerMode = true
        s.hsCapDynamic = false  // desativa cap pro RAGE funcionar
        s.replaySanit = true
        s.signatureRot = true
        s.autoOffKills = 0
        s.flagSecure = true
    }

    // ===== DETECCAO =====

    private fun matchesLegit(): Boolean {
        val s = StateBridge
        return s.shotHeadCount == 1 && s.shotBodyCount == 3 &&
               s.espMode == 0 && !s.speedHack && !s.flyMode &&
               !s.oneShotHs && !s.pierceWalls
    }
    private fun matchesAggr(): Boolean {
        val s = StateBridge
        return s.shotHeadCount == 3 && s.shotBodyCount == 1 &&
               s.espMode == 1 && s.speedHack && !s.flyMode &&
               !s.oneShotHs
    }
    private fun matchesRage(): Boolean {
        val s = StateBridge
        return s.shotHeadCount == 5 && s.shotBodyCount == 0 &&
               s.espMode == 2 && s.oneShotHs && s.pierceWalls
    }

    /**
     * Intensidade 0-100 baseada em TODAS as features ligadas.
     * Pondera por tier (S=1, M=3, R=8).
     */
    fun intensity(): Int {
        val s = StateBridge
        var score = 0
        // shot pattern contribui ate 30 pontos
        val total = s.shotHeadCount + s.shotBodyCount
        if (total > 0) score += (s.shotHeadCount * 30 / total)
        // S tier: pouco impacto
        if (s.aimEnabled) score += 2
        if (s.aimPrediction) score += 1
        if (s.noRecoil) score += 2
        if (s.noSpread) score += 1
        if (s.noReload) score += 1
        if (s.noFallDmg) score += 1
        // M tier: medio
        if (s.aimMultiTarget) score += 3
        if (!s.aimOnScope && s.aimEnabled) score += 3  // aim livre
        if (!s.aimIgnoreBots) score += 2
        if (s.fastFire) score += 3
        if (s.infiniteAmmo) score += 3
        if (s.speedHack) score += 3
        if (s.superJump) score += 3
        if (s.bhop) score += 3
        if (s.instantRevive) score += 3
        if (s.wideView) score += 2
        if (s.lootEsp) score += 2
        if (s.vehicleEsp) score += 2
        if (s.miniMapEnemy) score += 2
        if (s.killFeedSpy) score += 2
        // ESP mode
        score += when (s.espMode) { 1 -> 5; 2 -> 15; 3 -> 2; else -> 0 }
        // R tier: alto
        if (s.aimSilent) score += 8
        if (s.oneShotHs) score += 8
        if (s.anyGunAwm) score += 8
        if (s.pierceWalls) score += 10
        if (s.magicBullet) score += 8
        if (s.flyMode) score += 10
        if (s.noCollision) score += 10
        return score.coerceIn(0, 100)
    }
}
