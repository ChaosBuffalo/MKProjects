package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.WorldAreaEffectEntry;
import com.chaosbuffalo.mkcore.init.CoreEntities;
import com.chaosbuffalo.targeting_api.TargetingContext;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MKAreaEffectEntity extends AreaEffectCloud implements IEntityAdditionalSpawnData {

    private static final float DEFAULT_RADIUS = 3.0f;
    private static final float DEFAULT_HEIGHT = 1.0f;

    private final List<WorldAreaEffectEntry> effects;
    private boolean particlesDisabled;
    private IMKEntityData ownerData;


    public MKAreaEffectEntity(EntityType<? extends MKAreaEffectEntity> entityType, Level world) {
        super(entityType, world);
        this.particlesDisabled = false;
        effects = new ArrayList<>();
        setRadius(DEFAULT_RADIUS);
    }

    public MKAreaEffectEntity(Level worldIn, double x, double y, double z) {
        this(CoreEntities.AREA_EFFECT.get(), worldIn);
        this.setPos(x, y, z);
        this.duration = 600;
        this.waitTime = 20;
        this.reapplicationDelay = 20;
    }

    @Nonnull
    @Override
    public EntityDimensions getDimensions(@Nonnull Pose poseIn) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, DEFAULT_HEIGHT);
    }

    public void setPeriod(int delay) {
        this.reapplicationDelay = delay;
    }

    public void disableParticle() {
        // TODO
        particlesDisabled = true;
    }

    private boolean isInWaitPhase() {
        return isWaiting();
    }

    private void setInWaitPhase(boolean waitPhase) {
        setWaiting(waitPhase);
    }

    private void entityTick() {
        this.baseTick();
    }

    @Override
    public void tick() {
        entityTick();
        if (this.level.isClientSide()) {
            if (!particlesDisabled) {
                clientUpdate();
            }
        } else {
            if (serverUpdate()) {
                remove(RemovalReason.KILLED);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("ParticlesDisabled", particlesDisabled);
    }

    @Override
    protected void readAdditionalSaveData(@Nonnull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        particlesDisabled = compound.getBoolean("ParticlesDisabled");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(particlesDisabled);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        particlesDisabled = additionalData.readBoolean();
    }

    public void addEffect(MobEffectInstance effect, TargetingContext targetContext) {
        this.effects.add(WorldAreaEffectEntry.forEffect(this, effect, targetContext));
    }

    public void addEffect(MKEffectBuilder<?> effect, TargetingContext targetContext) {
        this.effects.add(WorldAreaEffectEntry.forEffect(this, effect, targetContext));
    }

    public void addDelayedEffect(MobEffectInstance effect, TargetingContext targetContext, int delayTicks) {
        WorldAreaEffectEntry entry = WorldAreaEffectEntry.forEffect(this, effect, targetContext);
        entry.setTickStart(delayTicks);
        this.effects.add(entry);
    }

    public void addDelayedEffect(MKEffectBuilder<?> effect, TargetingContext targetContext, int delayTicks) {
        WorldAreaEffectEntry entry = WorldAreaEffectEntry.forEffect(this, effect, targetContext);
        entry.setTickStart(delayTicks);
        this.effects.add(entry);

    }

    private boolean entityCheck(LivingEntity e) {
        return e != null &&
                EntitySelector.NO_SPECTATORS.test(e) &&
                EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(e) &&
                !victims.containsKey(e) &&
                e.isAffectedByPotions();
    }

    @Nullable
    private IMKEntityData getOwnerData() {
        if (ownerData == null) {
            ownerData = MKCore.getEntityDataOrNull(getOwner());
        }
        return ownerData;
    }

    private boolean serverUpdate() {
        if (tickCount >= waitTime + duration) {
            return true;
        }

        IMKEntityData entityData = getOwnerData();
        if (entityData == null)
            return true;

        boolean stillWaiting = tickCount < waitTime;

        if (isInWaitPhase() != stillWaiting) {
            setInWaitPhase(stillWaiting);
        }

        if (stillWaiting) {
            return false;
        }

        // TODO: FUTURE: see if this can be made dynamic by inspecting the effects
        if (tickCount % 5 != 0) {
            return false;
        }

        victims.entrySet().removeIf(entry -> tickCount >= entry.getValue());

        if (effects.isEmpty()) {
            victims.clear();
            return false;
        }

        // Copy in case callbacks try to add more effects
        List<WorldAreaEffectEntry> targetEffects = new ArrayList<>(effects);
        List<LivingEntity> potentialTargets = this.level.getEntitiesOfClass(LivingEntity.class,
                getBoundingBox(), this::entityCheck);
        if (potentialTargets.isEmpty()) {
            return false;
        }

        float radius = getRadius();
        float maxRange = radius * radius;
        for (LivingEntity target : potentialTargets) {

            double d0 = target.getX() - getX();
            double d1 = target.getZ() - getZ();
            double entityDist = d0 * d0 + d1 * d1;

            if (entityDist > maxRange) {
                continue;
            }

            victims.put(target, tickCount + reapplicationDelay);
            MKCore.getEntityData(target).ifPresent(targetData ->
                    targetEffects.forEach(entry -> {
                        if (entry.getTickStart() <= tickCount - waitTime) {
                            entry.apply(entityData, targetData);
                        }
                    }));
        }
        return false;
    }

    private void clientUpdate() {
        if (tickCount % 5 != 0) {
            return;
        }
        ParticleOptions particle = this.getParticle();

        if (isInWaitPhase()) {
            if (!random.nextBoolean()) {
                return;
            }

            for (int i = 0; i < 2; i++) {
                float f1 = random.nextFloat() * ((float) Math.PI * 2F);
                float f2 = Mth.sqrt(random.nextFloat()) * 0.2F;
                float xOff = Mth.cos(f1) * f2;
                float zOff = Mth.sin(f1) * f2;

                if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
                    int color = random.nextBoolean() ? 16777215 : getColor();
                    int r = color >> 16 & 255;
                    int g = color >> 8 & 255;
                    int b = color & 255;
                    level.addAlwaysVisibleParticle(particle, getX() + xOff, getY(), getZ() + zOff, r / 255f, g / 255f, b / 255f);
                } else {
                    level.addAlwaysVisibleParticle(particle, getX() + xOff, getY(), getZ() + zOff, 0, 0, 0);
                }
            }
        } else {
            float radius = getRadius();
            int particleCount = (int) radius * 10;

            for (int i = 0; i < particleCount; i++) {
                float f6 = random.nextFloat() * ((float) Math.PI * 2F);
                float f7 = Mth.sqrt(random.nextFloat()) * radius;
                float xOffset = Mth.cos(f6) * f7;
                float zOffset = Mth.sin(f6) * f7;

                if (particle == ParticleTypes.ENTITY_EFFECT) {
                    int color = getColor();
                    int r = color >> 16 & 255;
                    int g = color >> 8 & 255;
                    int b = color & 255;
                    level.addAlwaysVisibleParticle(particle, getX() + xOffset, getY(), getZ() + zOffset, r / 255f, g / 255f, b / 255f);
                } else if (particle == ParticleTypes.NOTE) {
                    level.addAlwaysVisibleParticle(particle, getX() + xOffset, getY(), getZ() + zOffset, random.nextInt(24) / 24.0f, 0.009999999776482582D, (0.5D - random.nextDouble()) * 0.15D);
                } else {
                    level.addAlwaysVisibleParticle(particle, getX() + xOffset, getY(), getZ() + zOffset, (0.5D - random.nextDouble()) * 0.15D, 0.009999999776482582D, (0.5D - random.nextDouble()) * 0.15D);
                }
            }
        }
    }

    @Nonnull
    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}