package com.chaosbuffalo.mkcore.abilities;

import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.utils.TargetUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public abstract class PositionTargetingAbility extends EntityTargetingAbility {

    public PositionTargetingAbility() {
        super();
    }

    public abstract void castAtPosition(IMKEntityData casterData, Vec3 position);

    @Override
    public void castAtEntity(IMKEntityData casterData, LivingEntity target) {
        castAtPosition(casterData, target.position());
    }

    @Override
    protected void onCastEnd(LivingEntity castingEntity, IMKEntityData casterData, AbilityContext context) {
        context.getMemory(MKAbilityMemories.ABILITY_POSITION_TARGET)
                .flatMap(TargetUtil.LivingOrPosition::getPosition).ifPresent(x -> castAtPosition(casterData, x));
    }

    @Override
    public AbilityTargetSelector getTargetSelector() {
        return AbilityTargeting.POSITION_INCLUDE_ENTITIES;
    }
}
