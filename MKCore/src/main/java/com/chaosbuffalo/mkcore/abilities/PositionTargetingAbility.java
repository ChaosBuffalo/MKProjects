package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

public abstract class PositionTargetingAbility extends EntityTargetingAbility {

    public PositionTargetingAbility() {
        super();
    }

    public abstract void castAtPosition(IMKEntityData casterData, Vec3 position, Function<Attribute, Float> skillSupplier);

    @Override
    public void castAtEntity(IMKEntityData casterData, LivingEntity target,
                             Function<Attribute, Float> skillSupplier) {
        castAtPosition(casterData, target.position(), skillSupplier);
    }

    @Override
    protected void onCastEnd(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context,
                             Function<Attribute, Float> skillSupplier) {
        context.getMemory(MKAbilityMemories.ABILITY_POSITION_TARGET)
                .flatMap(TargetUtil.LivingOrPosition::getPosition).ifPresent(
                        x -> castAtPosition(casterData, x, skillSupplier));
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.POSITION_INCLUDE_ENTITIES;
    }
}
