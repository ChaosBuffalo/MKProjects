package com.chaosbuffalo.mkcore.abilities;


import com.chaosbuffalo.mkcore.core.IMKEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;

import java.util.function.Function;

public abstract class EntityTargetingAbility extends MKAbility{

    public abstract void castAtEntity(IMKEntityData casterData, LivingEntity target, Function<Attribute, Float> skillSupplier);

    @Override
    public void endCast(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context, Function<Attribute, Float> skillSupplier) {
        super.endCast(castingEntity, casterData, context, skillSupplier);
        onCastEnd(castingEntity, casterData, context, skillSupplier);
    }

    protected void onCastEnd(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context,
                             Function<Attribute, Float> skillSupplier) {
        context.getMemory(MKAbilityMemories.ABILITY_TARGET).ifPresent(x -> castAtEntity(casterData, x, skillSupplier));
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.SINGLE_TARGET;
    }
}
