package com.chaosbuffalo.mkcore.abilities.client_state;

import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectileAbilityClientState extends AbilityClientState{
    public static final Codec<ProjectileAbilityClientState> CODEC = RecordCodecBuilder.<ProjectileAbilityClientState>mapCodec(builder -> builder.group(
            TrackedProjectile.CODEC.listOf().fieldOf("tracked").forGetter(i -> i.trackedProjectiles)
    ).apply(builder, ProjectileAbilityClientState::new)).codec();

    public static class TrackedProjectile {
        public static final Codec<TrackedProjectile> CODEC = RecordCodecBuilder.<TrackedProjectile>mapCodec(builder -> builder.group(
                Codec.INT.fieldOf("entityId").forGetter(i -> i.entityId),
                Codec.INT.fieldOf("ticksToFireAt").forGetter(i -> i.ticksToFireAt),
                Codec.INT.fieldOf("targetEntityId").forGetter(i -> i.targetEntityId),
                Codec.INT.fieldOf("index").forGetter(i -> i.index)
        ).apply(builder, TrackedProjectile::new)).codec();

        private final int entityId;
        private final int ticksToFireAt;
        private final int targetEntityId;
        private boolean fired;
        private final int index;


        public TrackedProjectile(int entityId, int ticksToFireAt, int targetEntityId, int index) {
            this.entityId = entityId;
            this.ticksToFireAt = ticksToFireAt;
            this.targetEntityId = targetEntityId;
            this.fired = false;
            this.index = index;
        }

        public int getEntityId() {
            return entityId;
        }

        public int getTargetEntityId() {
            return targetEntityId;
        }

        public int getTicksToFireAt() {
            return ticksToFireAt;
        }

        public void setFired(boolean fired) {
            this.fired = fired;
        }

        public boolean getFired() {
            return fired;
        }

        public int getIndex() {
            return index;
        }
    }

    protected final List<TrackedProjectile> trackedProjectiles = new ArrayList<>();

    public ProjectileAbilityClientState() {
        super();
    }

    public ProjectileAbilityClientState(List<TrackedProjectile> trackedProjectiles) {
        this.trackedProjectiles.addAll(trackedProjectiles);
    }

    public void addTrackedProjectile(BaseProjectileEntity entity, int ticksToFireAt, int index, Optional<LivingEntity> target) {
        trackedProjectiles.add(new TrackedProjectile(entity.getId(), ticksToFireAt, target.map(Entity::getId).orElse(-1), index));
    }

    public List<TrackedProjectile> getTrackedProjectiles() {
        return trackedProjectiles;
    }

    @Override
    public AbilityClientStateType<? extends AbilityClientState> getType() {
        return AbilityClientStateTypes.PROJECTILE.get();
    }

}
