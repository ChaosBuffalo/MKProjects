package com.chaosbuffalo.mkcore.abilities.projectiles;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.abilities.client_state.ProjectileAbilityClientState;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.utils.location.LocationProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BurstProjectileBehavior extends ProjectileCastBehavior{
    protected final LocationProvider locationProvider;
    public static final Codec<BurstProjectileBehavior> CODEC = RecordCodecBuilder.<BurstProjectileBehavior>mapCodec(builder -> builder.group(
            LocationProvider.CODEC.fieldOf("location").forGetter(i -> i.locationProvider)
    ).apply(builder, BurstProjectileBehavior::new)).codec();

    public BurstProjectileBehavior(LocationProvider provider) {
        this.locationProvider = provider;
    }

    @Override
    public ProjectileCastBehaviorType<? extends ProjectileCastBehavior> getType() {
       return ProjectileCastBehaviorTypes.BURST.get();
    }

    @Override
    public void startCast(ProjectileAbility ability, IMKEntityData casterData, int castTime, AbilityContext context) {
        float level = context.getSkill(ability.getSkill());
        ProjectileAbilityClientState clientState = new ProjectileAbilityClientState();
        List<BaseProjectileEntity> projectiles = new ArrayList<>();
        int count = locationProvider.getCount();
        int ticksBetween = castTime / (count + 1);
        for (int i = 0; i < count; i++) {
            AbilityProjectileEntity proj = ability.makeProjectile(casterData, context);
            proj.setOwner(casterData.getEntity());
            proj.setSkillLevel(level);
            int thisCast = ticksBetween * (i + 1);
            proj.setCastTime(thisCast);
            LocationProvider.WorldLocationResult location = locationProvider.getPosition(casterData.getEntity(), i);
            proj.setPos(location.worldPosition());
            proj.setXRot(location.rotation().x);
            proj.setYRot(location.rotation().y);
            projectiles.add(proj);
            clientState.addTrackedProjectile(proj, thisCast, context.getMemory(MKAbilityMemories.ABILITY_TARGET));
            casterData.getRiders().addRider(proj);
            casterData.getEntity().level.addFreshEntity(proj);
        }
        context.setClientState(clientState);
        context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(), Optional.of(projectiles));
    }

    protected void handleProjectileContinue(ProjectileAbility ability, IMKEntityData casterData,
                                            int castTimeLeft, int totalTicks,
                                            ProjectileAbilityClientState projectileClientState,
                                            @Nullable AbilityContext context) {
        for (ProjectileAbilityClientState.TrackedProjectile proj : projectileClientState.getTrackedProjectiles()) {
            if (proj.getTicksToFireAt() <= totalTicks - castTimeLeft && !proj.getFired()) {
                proj.setFired(true);
                Entity entity = casterData.getEntity().getLevel().getEntity(proj.getEntityId());
                if (entity instanceof BaseProjectileEntity projEnt) {
                    casterData.getRiders().removeRider(projEnt);
                    ability.fireProjectile(projEnt, ability.getProjectileSpeed(), ability.getProjectileInaccuracy(), casterData.getEntity(),
                            casterData.getEntity().getLevel().getEntity(proj.getTargetEntityId()));
                    if (context != null) {
                        context.getMemory(MKAbilityMemories.CURRENT_PROJECTILES).ifPresent(projectiles -> {
                            context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(),
                                    Optional.of(projectiles.stream().filter(x -> x != projEnt).toList()));
                        });
                    }

                }
            }
        }
    }


    @Override
    public void continueCast(ProjectileAbility ability, IMKEntityData casterData, AbilityContext context, int castTimeLeft, int totalTicks) {
        super.continueCast(ability, casterData, context, castTimeLeft, totalTicks);
        if (context.getClientState() instanceof ProjectileAbilityClientState projectileClientState) {
            handleProjectileContinue(ability, casterData, castTimeLeft, totalTicks, projectileClientState, context);
        }
    }

    @Override
    public void continueCastClient(ProjectileAbility ability, IMKEntityData casterData, int castTimeLeft, int totalTicks, @Nullable AbilityClientState clientState) {
        super.continueCastClient(ability, casterData, castTimeLeft, totalTicks, clientState);
        ClientHandler.handleContinue(ability, casterData, castTimeLeft, totalTicks, clientState, this);
    }

    @Override
    public void endCastClient(ProjectileAbility ability, IMKEntityData casterData, @Nullable AbilityClientState clientState) {
        super.endCastClient(ability, casterData, clientState);
        ClientHandler.handleEnd(ability, casterData, clientState);
    }

    static class ClientHandler {
        public static void handleContinue(ProjectileAbility ability, IMKEntityData casterData, int castTimeLeft, int totalTicks,
                                          @Nullable AbilityClientState clientState, BurstProjectileBehavior behavior) {
            if (clientState instanceof ProjectileAbilityClientState projectileClientState) {
                behavior.handleProjectileContinue(ability, casterData, castTimeLeft, totalTicks, projectileClientState, null);
            }
        }

        public static void handleEnd(ProjectileAbility ability, IMKEntityData casterData, @Nullable AbilityClientState clientState) {
            if (clientState instanceof ProjectileAbilityClientState projectileState) {
                for (ProjectileAbilityClientState.TrackedProjectile tracked : projectileState.getTrackedProjectiles()) {
                    if (!tracked.getFired()) {
                        tracked.setFired(true);
                        Entity entity = casterData.getEntity().getLevel().getEntity(tracked.getEntityId());
                        if (entity instanceof BaseProjectileEntity proj) {
                            casterData.getRiders().removeRider(proj);
                            ability.fireProjectile(proj, ability.getProjectileSpeed(), ability.getProjectileInaccuracy(),
                                    casterData.getEntity(), casterData.getEntity().getLevel().getEntity(tracked.getTargetEntityId()));
                        }
                    }
                }
            }
        }
    }
}