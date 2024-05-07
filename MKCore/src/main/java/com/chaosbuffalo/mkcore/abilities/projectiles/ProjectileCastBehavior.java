package com.chaosbuffalo.mkcore.abilities.projectiles;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;

public abstract class ProjectileCastBehavior {

    public static final Codec<ProjectileCastBehavior> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            MKCoreRegistry.PROJECTILE_CAST_BEHAVIOR_TYPES.getCodec().dispatch(ProjectileCastBehavior::getType, ProjectileCastBehaviorType::codec));

    public void startCast(ProjectileAbility ability, IMKEntityData casterData, int castTime, AbilityContext context) {

    }

    public void endCast(ProjectileAbility ability, IMKEntityData casterData, AbilityContext context) {
        ability.fireCurrentProjectiles(casterData, context);
    }

    public void continueCast(ProjectileAbility ability, IMKEntityData casterData, AbilityContext context, int castTimeLeft, int totalTicks) {
    }

    public void endCastClient(ProjectileAbility ability, IMKEntityData casterData) {

    }

    public abstract ProjectileCastBehaviorType<? extends ProjectileCastBehavior> getType();


}
