package com.chaosbuffalo.mkcore.entities;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.WorldAreaEffectEntry;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.targeting_api.TargetingContext;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.*;

public abstract class BaseEffectEntity extends Entity implements IEntityAdditionalSpawnData {
    protected final List<WorldAreaEffectEntry> effects;
    protected final int WAIT_LAG = 5;
    protected final int DEFAULT_VISUAL_TICK_RATE = 5;
    protected final Map<Entity, Integer> reapplicationDelayMap = Maps.newHashMap();
    protected int duration = 600;
    protected int waitTime = 20;
    protected int tickRate = 5;
    @Nullable
    protected ParticleDisplay particles;
    @Nullable
    protected ParticleDisplay waitingParticles;
    protected LivingEntity owner;
    protected UUID ownerUniqueId;
    private IMKEntityData ownerData;

    public static class ParticleDisplay {
        public enum DisplayType {
            CONTINUOUS,
            ONCE
        }

        protected ResourceLocation particles;
        protected int tickRate;
        protected DisplayType type;

        public ParticleDisplay(ResourceLocation particleName, int tickRate, DisplayType type) {
            particles = particleName;
            this.tickRate = tickRate;
            this.type = type;
        }

        public ResourceLocation getParticles() {
            return particles;
        }

        public DisplayType getType() {
            return type;
        }

        public int getTickRate() {
            return tickRate;
        }

        public boolean shouldTick(int ticksExisted, int offset) {
            switch (type) {
                case ONCE:
                    return ticksExisted - offset == 0;
                case CONTINUOUS:
                default:
                    return ticksExisted % getTickRate() == 0;

            }
        }

        public void write(FriendlyByteBuf buffer) {
            buffer.writeResourceLocation(particles);
            buffer.writeInt(tickRate);
            buffer.writeEnum(type);
        }

        public static ParticleDisplay read(FriendlyByteBuf buffer) {
            ResourceLocation loc = buffer.readResourceLocation();
            int tickRate = buffer.readInt();
            DisplayType type = buffer.readEnum(DisplayType.class);
            return new ParticleDisplay(loc, tickRate, type);
        }
    }

    public BaseEffectEntity(EntityType<? extends BaseEffectEntity> entityType, Level world) {
        super(entityType, world);
        this.effects = new ArrayList<>();
        particles = null;
    }

    @Override
    protected void defineSynchedData() {

    }

    public void setParticles(ResourceLocation particles) {
        this.particles = new ParticleDisplay(particles, DEFAULT_VISUAL_TICK_RATE, ParticleDisplay.DisplayType.CONTINUOUS);
    }

    public void setWaitingParticles(ResourceLocation waitingParticles) {
        this.waitingParticles = new ParticleDisplay(waitingParticles,
                DEFAULT_VISUAL_TICK_RATE, ParticleDisplay.DisplayType.CONTINUOUS);
    }

    public void setParticles(@Nullable ParticleDisplay display) {
        this.particles = display;
    }

    public void setWaitingParticles(@Nullable ParticleDisplay display) {
        this.waitingParticles = display;
    }


    public void setTickRate(int tickRate) {
        this.tickRate = tickRate;
    }


    public void addEffect(MobEffectInstance effect, TargetingContext targetContext) {
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

    public void addEffect(MKEffectBuilder<?> effect, TargetingContext targetContext) {
        this.effects.add(WorldAreaEffectEntry.forEffect(this, effect, targetContext));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            clientUpdate();
        } else {
            if (serverUpdate()) {
                remove(RemovalReason.KILLED);
            }
        }
    }

    protected void spawnClientParticles(ParticleDisplay display) {
        ParticleAnimation anim = ParticleAnimationManager.getAnimation(display.getParticles());
        if (anim != null) {
            anim.spawn(getCommandSenderWorld(), position(), null);
        }
    }

    private void clientUpdate() {
        ParticleDisplay display = isWaiting() ? waitingParticles : particles;
        if (display != null && display.shouldTick(tickCount, isWaiting() ? 0 : waitTime)) {
            spawnClientParticles(display);
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(owner.getId());
        buffer.writeInt(tickRate);
        buffer.writeInt(waitTime);
        buffer.writeInt(tickCount);
        buffer.writeBoolean(particles != null);
        if (particles != null) {
            particles.write(buffer);
        }
        buffer.writeBoolean(waitingParticles != null);
        if (waitingParticles != null) {
            waitingParticles.write(buffer);
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity ent = getCommandSenderWorld().getEntity(additionalData.readInt());
        if (ent instanceof LivingEntity) {
            owner = (LivingEntity) ent;
        }
        tickRate = additionalData.readInt();
        waitTime = additionalData.readInt();
        tickCount = additionalData.readInt();
        boolean hasParticles = additionalData.readBoolean();
        if (hasParticles) {
            particles = ParticleDisplay.read(additionalData);
        }
        boolean hasWaiting = additionalData.readBoolean();
        if (hasWaiting) {
            waitingParticles = ParticleDisplay.read(additionalData);
        }
    }

    public void setOwner(@Nullable LivingEntity ownerIn) {
        this.owner = ownerIn;
        this.ownerUniqueId = ownerIn == null ? null : ownerIn.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner == null && this.ownerUniqueId != null && this.level instanceof ServerLevel) {
            Entity entity = ((ServerLevel) this.level).getEntity(this.ownerUniqueId);
            if (entity instanceof LivingEntity) {
                this.owner = (LivingEntity) entity;
            }
        }

        return this.owner;
    }

    public boolean isWaiting() {
        return tickCount < waitTime;
    }

    @Nullable
    private IMKEntityData getOwnerData() {
        if (ownerData == null) {
            ownerData = MKCore.getEntityDataOrNull(getOwner());
        }
        return ownerData;
    }

    protected abstract Collection<LivingEntity> getEntitiesInBounds();

    private boolean serverUpdate() {
        if (tickCount > waitTime + duration + WAIT_LAG + 1) {
            return true;
        }
        IMKEntityData entityData = getOwnerData();
        if (entityData == null)
            return true;

        // lets recalc waiting to include a wait lag so that the server isnt damaging before the client responds
        boolean stillWaiting = tickCount <= waitTime + WAIT_LAG;

        if (stillWaiting) {
            return false;
        }

        reapplicationDelayMap.entrySet().removeIf(entry -> tickCount >= entry.getValue());

        if (effects.isEmpty()) {
            reapplicationDelayMap.clear();
            return false;
        }

        Collection<LivingEntity> result = getEntitiesInBounds();

        if (result.isEmpty()) {
            return false;
        }

        for (LivingEntity target : result) {
            reapplicationDelayMap.put(target, tickCount + tickRate);
            MKCore.getEntityData(target).ifPresent(targetData ->
                    effects.forEach(entry -> {
                        if (entry.getTickStart() <= tickCount - waitTime - WAIT_LAG) {
                            entry.apply(entityData, targetData);
                        }
                    }));
        }


        return false;
    }

    protected boolean entityCheck(LivingEntity e) {
        return e != null &&
                EntitySelector.NO_SPECTATORS.test(e) &&
                EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(e) &&
                !reapplicationDelayMap.containsKey(e) &&
                e.isAffectedByPotions();
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected void writeVector(FriendlyByteBuf buffer, Vec3 vector) {
        buffer.writeDouble(vector.x());
        buffer.writeDouble(vector.y());
        buffer.writeDouble(vector.z());
    }

    protected Vec3 readVector(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
