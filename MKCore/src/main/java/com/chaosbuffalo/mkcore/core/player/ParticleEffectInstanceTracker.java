package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.*;

public class ParticleEffectInstanceTracker implements ISyncObject {

    protected final Map<UUID, ParticleEffectInstance> instanceMap;
    protected final Entity entity;


    public ParticleEffectInstanceTracker(Entity entity) {
        this.entity = entity;
        instanceMap = new HashMap<>();
    }

    public Entity getEntity() {
        return entity;
    }

    public Collection<ParticleEffectInstance> getParticleInstances() {
        return instanceMap.values();
    }

    public boolean addParticleInstance(ParticleEffectInstance instance) {
        ParticleEffectInstance existing = instanceMap.get(instance.getInstanceUUID());
        if (existing != null) {
            MKCore.LOGGER.error("Tried to add same particle instance twice {} to player {}", instance, entity);
            return false;
        } else {
            instanceMap.put(instance.getInstanceUUID(), instance);
            return true;
        }
    }

    public void removeParticleInstance(UUID uuid) {
        instanceMap.remove(uuid);
    }


    @Override
    public void setNotifier(ISyncNotifier notifier) {

    }

    public void clearParticleEffects() {
        var keys = Set.copyOf(instanceMap.keySet());
        keys.forEach(this::removeParticleInstance);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context) {
        return ISyncObject.notImplementedByDesign(this);
    }

    @Override
    public @Nullable Tag writeUpdateValue(SyncContext context) {
        return ISyncObject.notImplementedByDesign(this);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
        if (valueTag instanceof CompoundTag tag) {
            if (tag.contains("effectInstances")) {
                instanceMap.clear();
                ListTag effectsNbt = tag.getList("effectInstances", Tag.TAG_COMPOUND);
                for (Tag effNbt : effectsNbt) {
                    ParticleEffectInstance inst = ParticleEffectInstance.CODEC.parse(NbtOps.INSTANCE, effNbt).getOrThrow();
                    if (inst != null) {
                        addParticleInstance(inst);
                    }
                }
            }
            if (tag.contains("effectInstancesAdd")) {
                ListTag effectsNbt = tag.getList("effectInstancesAdd", Tag.TAG_COMPOUND);
                for (Tag effNbt : effectsNbt) {
                    ParticleEffectInstance inst = ParticleEffectInstance.CODEC.parse(NbtOps.INSTANCE, effNbt).getOrThrow();
                    if (inst != null) {
                        addParticleInstance(inst);
                    }
                }
            }
            if (tag.contains("effectInstancesRemove")) {
                ListTag toRemoveNbt = tag.getList("effectInstancesRemove", Tag.TAG_STRING);
                for (Tag inbt : toRemoveNbt) {
                    UUID id = UUID.fromString(inbt.getAsString());
                    removeParticleInstance(id);
                }
            }
        }
    }


    static class ParticleEffectInstanceTrackerServer extends ParticleEffectInstanceTracker {
        private final List<UUID> toRemoveDirty;
        private final List<ParticleEffectInstance> toAddDirty;
        private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

        public ParticleEffectInstanceTrackerServer(Entity entity) {
            super(entity);
            toRemoveDirty = new ArrayList<>();
            toAddDirty = new ArrayList<>();
        }

        @Override
        public boolean addParticleInstance(ParticleEffectInstance instance) {
            boolean wasAdded = super.addParticleInstance(instance);
            if (wasAdded) {
                toAddDirty.add(instance);
                parentNotifier.notifyUpdate(this);
            }
            return wasAdded;
        }

        @Override
        public void removeParticleInstance(UUID uuid) {
            super.removeParticleInstance(uuid);
            toRemoveDirty.add(uuid);
            parentNotifier.notifyUpdate(this);
        }

        @Override
        public boolean isDirty() {
            return !toRemoveDirty.isEmpty() || !toAddDirty.isEmpty();
        }

        @Override
        public @Nullable Tag writeFullValue(SyncContext context) {
            CompoundTag tag = new CompoundTag();
            ListTag effectsNbt = new ListTag();
            for (ParticleEffectInstance instance : instanceMap.values()) {
                Tag etag = ParticleEffectInstance.CODEC.encodeStart(NbtOps.INSTANCE, instance).getOrThrow();
                effectsNbt.add(etag);
            }
            tag.put("effectInstances", effectsNbt);
            toRemoveDirty.clear();
            toAddDirty.clear();
            return tag;
        }

        @Override
        public @Nullable Tag writeUpdateValue(SyncContext context) {
            CompoundTag tag = new CompoundTag();
            ListTag toRemove = new ListTag();
            for (UUID id : toRemoveDirty) {
                toRemove.add(StringTag.valueOf(id.toString()));
            }
            tag.put("effectInstancesRemove", toRemove);
            toRemoveDirty.clear();
            ListTag toAdd = new ListTag();
            for (ParticleEffectInstance instance : toAddDirty) {
                Tag etag = ParticleEffectInstance.CODEC.encodeStart(NbtOps.INSTANCE, instance).getOrThrow();
                toAdd.add(etag);
            }
            tag.put("effectInstancesAdd", toAdd);
            toAddDirty.clear();
            return tag;
        }

        @Override
        public void setNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }
    }

    public static ParticleEffectInstanceTracker getTracker(Entity entity) {
        if (!entity.level().isClientSide()) {
            return new ParticleEffectInstanceTrackerServer(entity);
        } else {
            return new ParticleEffectInstanceTracker(entity);
        }
    }
}
