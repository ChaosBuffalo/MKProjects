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

public class SimpleProjectileBehavior extends ProjectileCastBehavior{
    protected final boolean doPitch;
    public static final Codec<SimpleProjectileBehavior> CODEC = RecordCodecBuilder.<SimpleProjectileBehavior>mapCodec(builder -> builder.group(
            LocationProvider.CODEC.fieldOf("location").forGetter(i -> i.locationProvider),
            Codec.BOOL.fieldOf("doPitch").forGetter(i -> i.doPitch)
    ).apply(builder, SimpleProjectileBehavior::new)).codec();

    public SimpleProjectileBehavior(LocationProvider provider, boolean doPitch) {
        super(provider);
        this.doPitch = doPitch;
    }

    @Override
    public ProjectileCastBehaviorType<? extends ProjectileCastBehavior> getType() {
        return ProjectileCastBehaviorTypes.SIMPLE.get();
    }

    @Override
    public void startCast(ProjectileAbility ability, IMKEntityData casterData, int castTime, AbilityContext context) {
        float level = context.getSkill(ability.getSkill());
        ProjectileAbilityClientState clientState = new ProjectileAbilityClientState();
        List<BaseProjectileEntity> projectiles = new ArrayList<>();
        for (int i = 0; i < locationProvider.getCount(); i++) {
            AbilityProjectileEntity proj = ability.makeProjectile(casterData, context);
            proj.setOwner(casterData.getEntity());
            proj.setSkillLevel(level);
            proj.setCastTime(castTime);
            LocationProvider.WorldLocationResult location = locationProvider.getPosition(casterData.getEntity(), i);
            proj.setPos(location.worldPosition());
            proj.setXRot(location.rotation().x);
            proj.setYRot(location.rotation().y);
            projectiles.add(proj);
            clientState.addTrackedProjectile(proj, castTime, i, context.getMemory(MKAbilityMemories.ABILITY_TARGET));
            casterData.getRiders().addRider(proj, doPitch);
            casterData.getEntity().level.addFreshEntity(proj);
        }
        context.setClientState(clientState);
        context.setMemory(MKAbilityMemories.CURRENT_PROJECTILES.get(), Optional.of(projectiles));
    }


    @Override
    public void endCastClient(ProjectileAbility ability, IMKEntityData casterData, @Nullable AbilityClientState clientState) {
        super.endCastClient(ability, casterData, clientState);
        ClientHandler.handle(ability, casterData, getLocationProvider(), clientState);
    }

    static class ClientHandler {

        public static void handle(ProjectileAbility ability, IMKEntityData casterData, LocationProvider locationProvider, @Nullable AbilityClientState clientState) {
            if (clientState instanceof ProjectileAbilityClientState projectileState) {
                for (ProjectileAbilityClientState.TrackedProjectile tracked : projectileState.getTrackedProjectiles()) {
                    if (!tracked.getFired()) {
                        tracked.setFired(true);
                        Entity entity = casterData.getEntity().getLevel().getEntity(tracked.getEntityId());
                        if (entity instanceof BaseProjectileEntity proj) {
                            casterData.getRiders().removeRider(proj);
                            LocationProvider.WorldLocationResult loc = locationProvider.getPosition(
                                    casterData.getEntity(), tracked.getIndex());
                            ability.fireProjectile(proj, ability.getProjectileSpeed(), ability.getProjectileInaccuracy(),
                                    casterData.getEntity(),
                                    casterData.getEntity().getLevel().getEntity(tracked.getTargetEntityId()),
                                    loc.rotation().x, loc.rotation().y);
                        }
                    }
                }
            }
        }
    }
}
