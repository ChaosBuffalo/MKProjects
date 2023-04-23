package com.chaosbuffalo.mkcore.abilities;


import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.entity.LivingEntity;

public abstract class EntityTargetingAbility extends MKAbility{

    public abstract void castAtEntity(IMKEntityData casterData, LivingEntity target);

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        super.endCast(castingEntity, casterData, context);
        onCastEnd(castingEntity, casterData, context);
    }

    protected void onCastEnd(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(x -> castAtEntity(casterData, x));
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }
}
