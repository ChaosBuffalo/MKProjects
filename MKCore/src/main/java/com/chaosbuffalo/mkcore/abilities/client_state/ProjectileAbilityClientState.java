package com.chaosbuffalo.mkcore.abilities.client_state;

import com.chaosbuffalo.mkcore.entities.BaseProjectileEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;

public class ProjectileAbilityClientState extends AbilityClientState{
    public static final Codec<ProjectileAbilityClientState> CODEC = RecordCodecBuilder.<ProjectileAbilityClientState>mapCodec(builder -> builder.group(
            TrackedProjectile.CODEC.listOf().fieldOf("tracked").forGetter(i -> i.trackedProjectiles)
    ).apply(builder, ProjectileAbilityClientState::new)).codec();

    public static class TrackedProjectile {
        public static final Codec<TrackedProjectile> CODEC = RecordCodecBuilder.<TrackedProjectile>mapCodec(builder -> builder.group(
                Codec.INT.fieldOf("entityId").forGetter(i -> i.entityId),
                Codec.INT.fieldOf("ticksToFireAt").forGetter(i -> i.ticksToFireAt)
        ).apply(builder, TrackedProjectile::new)).codec();

        private final int entityId;
        private final int ticksToFireAt;
        private boolean fired;


        public TrackedProjectile(int entityId, int ticksToFireAt) {
            this.entityId = entityId;
            this.ticksToFireAt = ticksToFireAt;
            this.fired = false;
        }

        public int getEntityId() {
            return entityId;
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
    }

    protected final List<TrackedProjectile> trackedProjectiles = new ArrayList<>();

    public ProjectileAbilityClientState() {
        super();
    }

    public ProjectileAbilityClientState(List<TrackedProjectile> trackedProjectiles) {
        this.trackedProjectiles.addAll(trackedProjectiles);
    }

    public void addTrackedProjectile(BaseProjectileEntity entity, int ticksToFireAt) {
        trackedProjectiles.add(new TrackedProjectile(entity.getId(), ticksToFireAt));
    }

    public List<TrackedProjectile> getTrackedProjectiles() {
        return trackedProjectiles;
    }

    @Override
    public AbilityClientStateType<? extends AbilityClientState> getType() {
        return AbilityClientStateTypes.PROJECTILE.get();
    }

}
