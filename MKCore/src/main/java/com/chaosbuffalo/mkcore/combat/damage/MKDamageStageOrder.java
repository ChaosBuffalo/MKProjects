package com.chaosbuffalo.mkcore.combat.damage;

public enum MKDamageStageOrder {
    PRE_MODIFIERS,
    WEAPON_MODIFIERS,
    ACCESSORY_MODIFIERS,
    BONUS_DAMAGE,
    CRIT,
    RESISTANCE,
    ATTACKER_TRIGGERS,
    VICTIM_TRIGGERS,
    POST_REACTIONS,
    FINAL_MODIFIERS
}
