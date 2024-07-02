package com.chaosbuffalo.mkcore.abilities.projectiles;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.abilities.client_state.ProjectileAbilityClientState;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.utils.location.LocationProvider;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class ProjectileCastBehavior {

    public static final Codec<ProjectileCastBehavior> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            MKCoreRegistry.PROJECTILE_CAST_BEHAVIOR_TYPES.getCodec().dispatch(ProjectileCastBehavior::getType, ProjectileCastBehaviorType::codec));
    protected final LocationProvider locationProvider;

    public ProjectileCastBehavior(LocationProvider provider) {
        this.locationProvider = provider;
    }

    public void startCast(ProjectileAbility ability, IMKEntityData casterData, int castTime, AbilityContext context) {

    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    public abstract Component describe(IMKEntityData casterData, ProjectileAbility projectileAbility);

    public void endCast(ProjectileAbility ability, IMKEntityData casterData, AbilityContext context) {
        if (context.getClientState() instanceof ProjectileAbilityClientState projectileState) {
            for (ProjectileAbilityClientState.TrackedProjectile tracked : projectileState.getTrackedProjectiles()) {
                if (!tracked.getFired()) {
                    tracked.setFired(true);
                    Entity entity = casterData.getEntity().getLevel().getEntity(tracked.getEntityId());
                    if (entity instanceof BaseProjectileEntity proj) {
                        casterData.getRiders().removeRider(proj);
                        LocationProvider.WorldLocationResult loc = getLocationProvider().getPosition(
                                casterData.getEntity(), tracked.getIndex());
                        ability.fireProjectile(proj, ability.getProjectileSpeed(), ability.getProjectileInaccuracy(),
                                casterData.getEntity(),
                                casterData.getEntity().getLevel().getEntity(tracked.getTargetEntityId()),
                                loc.rotation().x, loc.rotation().y);
                    }
                }

            }
        }
        context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(), Optional.empty());

    }

    public void continueCast(ProjectileAbility ability, IMKEntityData casterData, AbilityContext context, int castTimeLeft, int totalTicks) {
    }

    public void endCastClient(ProjectileAbility ability, IMKEntityData casterData, @Nullable AbilityClientState clientState) {

    }

    public void continueCastClient(ProjectileAbility ability, IMKEntityData casterData, int castTimeLeft, int totalTicks, @Nullable AbilityClientState clientState) {

    }

    public abstract ProjectileCastBehaviorType<? extends ProjectileCastBehavior> getType();


}
