package com.chaosbuffalo.mkcore.abilities.projectiles;

import com.chaosbuffalo.mkcore.abilities.AbilityContext;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.client_state.ProjectileAbilityClientState;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientState;
import com.chaosbuffalo.mkcore.entities.AbilityProjectileEntity;
import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.chaosbuffalo.mkcore.utils.location.LocationProvider;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Optional;

public class SingleProjectileBehavior extends ProjectileCastBehavior {
    protected final LocationProvider locationProvider;
    public static final Codec<SingleProjectileBehavior> CODEC = RecordCodecBuilder.<SingleProjectileBehavior>mapCodec(builder -> builder.group(
            LocationProvider.CODEC.fieldOf("location").forGetter(i -> i.locationProvider)
    ).apply(builder, SingleProjectileBehavior::new)).codec();

    public SingleProjectileBehavior(LocationProvider provider) {
        this.locationProvider = provider;
    }

    @Override
    public void startCast(ProjectileAbility ability, IMKEntityData casterData, int castTime, AbilityContext context) {
        float level = context.getSkill(ability.getSkill());
        AbilityProjectileEntity proj = ability.makeProjectile(casterData, context);
        proj.setOwner(casterData.getEntity());
        proj.setSkillLevel(level);
        proj.setCastTime(castTime);
        LocationProvider.WorldLocationResult location = locationProvider.getPosition(casterData.getEntity(), 0);
        proj.setPos(location.worldPosition());
        proj.setXRot(location.rotation().x);
        proj.setYRot(location.rotation().y);
        context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(), Optional.of(Lists.newArrayList(proj)));
        ProjectileAbilityClientState clientState = new ProjectileAbilityClientState();
        clientState.addTrackedProjectile(proj, castTime);
        context.setClientState(clientState);
        casterData.getRiders().addRider(proj);
        casterData.getEntity().level.addFreshEntity(proj);
    }

    @Override
    public ProjectileCastBehaviorType<? extends ProjectileCastBehavior> getType() {
        return ProjectileCastBehaviorTypes.SINGLE.get();
    }

    @Override
    public void endCastClient(ProjectileAbility ability, IMKEntityData casterData, @Nullable AbilityClientState clientState) {
        super.endCastClient(ability, casterData, clientState);
        ClientHandler.handle(ability, casterData, clientState);
    }

    static class ClientHandler {

        public static void handle(ProjectileAbility ability, IMKEntityData casterData, @Nullable AbilityClientState clientState) {
            if (casterData.getEntity() instanceof LocalPlayer) {
                if (clientState instanceof ProjectileAbilityClientState projectileState) {
                    for (ProjectileAbilityClientState.TrackedProjectile tracked : projectileState.getTrackedProjectiles()) {
                        if (!tracked.getFired()) {
                            tracked.setFired(true);
                            Entity entity = casterData.getEntity().getLevel().getEntity(tracked.getEntityId());
                            if (entity instanceof BaseProjectileEntity proj) {
                                casterData.getRiders().removeRider(proj);
                                proj.shoot(proj, proj.getXRot(), proj.getYRot(),
                                        0, ability.getProjectileSpeed(), ability.getProjectileInaccuracy());
                            }
                        }
                    }
                }
            }
        }
    }

}
