package com.chaosbuffalo.mkcore.abilities.ai;

import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class AbilityDecisionContext {

    private final LivingEntity caster;
    private final List<LivingEntity> friendlies;
    private final List<LivingEntity> enemies;
    private final LivingEntity threatTarget;

    public AbilityDecisionContext(LivingEntity caster, LivingEntity threatTarget,
                                  List<LivingEntity> friendlies, List<LivingEntity> enemies) {
        this.caster = caster;
        this.friendlies = friendlies;
        this.enemies = enemies;
        this.threatTarget = threatTarget;
    }

    public List<LivingEntity> getEnemies() {
        return enemies;
    }

    public List<LivingEntity> getFriendlies() {
        return friendlies;
    }

    public LivingEntity getThreatTarget() {
        return threatTarget;
    }

    public LivingEntity getCaster() {
        return caster;
    }
}
