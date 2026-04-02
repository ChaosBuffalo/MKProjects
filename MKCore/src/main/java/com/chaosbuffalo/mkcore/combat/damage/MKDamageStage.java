package com.chaosbuffalo.mkcore.combat.damage;

@FunctionalInterface
public interface MKDamageStage {
    void apply(MKDamageContext context);
}
