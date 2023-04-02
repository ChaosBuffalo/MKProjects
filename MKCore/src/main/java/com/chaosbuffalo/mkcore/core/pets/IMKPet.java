package com.chaosbuffalo.mkcore.core.pets;

import net.minecraft.world.entity.LivingEntity;

public interface IMKPet {

    void addThreat(LivingEntity source, float threatValue, boolean propagate);

    void setNoncombatBehavior(PetNonCombatBehavior behavior);

    float getHighestThreat();

    void clearThreat();

    void enterCombatMovementState(LivingEntity target);

    void enterNonCombatMovementState();

}
