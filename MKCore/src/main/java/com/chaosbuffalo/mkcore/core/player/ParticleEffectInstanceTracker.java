package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.fx.particles.effect_instances.ParticleEffectInstance;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ParticleEffectInstanceTracker implements ISyncObject {

    protected List<ParticleEffectInstance> particleInstances;
    protected final Entity entity;


    public ParticleEffectInstanceTracker(Entity entity) {
        particleInstances = new ArrayList<>();
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public List<ParticleEffectInstance> getParticleInstances() {
        return particleInstances;
    }

    public boolean addParticleInstance(ParticleEffectInstance instance) {
        Optional<ParticleEffectInstance> alreadyExists = particleInstances.stream().filter(
                x -> x.getInstanceUUID().equals(instance.getInstanceUUID())).findAny();
        if (alreadyExists.isPresent()) {
            MKCore.LOGGER.error("Tried to add same particle instance twice {} to player {}", instance, entity);
            return false;
        } else {
            particleInstances.add(instance);
            return true;
        }
    }

    public void removeParticleInstance(UUID uuid) {
        this.particleInstances = particleInstances.stream().filter(x -> !x.getInstanceUUID().equals(uuid))
                .collect(Collectors.toList());
    }


    @Override
    public void setNotifier(ISyncNotifier notifier) {

    }

    public void clearParticleEffects() {
        for (ParticleEffectInstance inst : particleInstances) {
            removeParticleInstance(inst.getInstanceUUID());
        }
    }

    @Override
    public boolean isDirty() {
        return false;
    }


    @Override
    public void deserializeUpdate(CompoundTag tag) {
        if (tag.contains("effectInstances")) {
            particleInstances.clear();
            ListTag effectsNbt = tag.getList("effectInstances", Tag.TAG_COMPOUND);
            for (Tag effNbt : effectsNbt) {
                Dynamic<?> dyn = new Dynamic<>(NbtOps.INSTANCE, effNbt);
                ResourceLocation type = ParticleEffectInstance.getType(dyn);
                ParticleEffectInstance inst = ParticleAnimationManager.getEffectInstance(type);
                if (inst != null) {
                    inst.deserialize(dyn);
                    addParticleInstance(inst);
                }
            }
        }
        if (tag.contains("effectInstancesAdd")) {
            ListTag effectsNbt = tag.getList("effectInstancesAdd", Tag.TAG_COMPOUND);
            for (Tag effNbt : effectsNbt) {
                Dynamic<?> dyn = new Dynamic<>(NbtOps.INSTANCE, effNbt);
                ResourceLocation type = ParticleEffectInstance.getType(dyn);
                ParticleEffectInstance inst = ParticleAnimationManager.getEffectInstance(type);
                if (inst != null) {
                    inst.deserialize(dyn);
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

    @Override
    public void serializeUpdate(CompoundTag tag) {

    }

    @Override
    public void serializeFull(CompoundTag tag) {

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
        public void serializeFull(CompoundTag tag) {
            ListTag effectsNbt = new ListTag();
            for (ParticleEffectInstance instance : particleInstances) {
                effectsNbt.add(instance.serialize(NbtOps.INSTANCE));
            }
            tag.put("effectInstances", effectsNbt);
            toRemoveDirty.clear();
            toAddDirty.clear();
        }

        @Override
        public void serializeUpdate(CompoundTag tag) {
            ListTag toRemove = new ListTag();
            for (UUID id : toRemoveDirty) {
                toRemove.add(StringTag.valueOf(id.toString()));
            }
            tag.put("effectInstancesRemove", toRemove);
            toRemoveDirty.clear();
            ListTag toAdd = new ListTag();
            for (ParticleEffectInstance instance : toAddDirty) {
                toAdd.add(instance.serialize(NbtOps.INSTANCE));
            }
            tag.put("effectInstancesAdd", toAdd);
            toAddDirty.clear();
        }

        @Override
        public void setNotifier(ISyncNotifier notifier) {
            parentNotifier = notifier;
        }
    }

    public static ParticleEffectInstanceTracker getTracker(Entity entity) {
        if (!entity.getCommandSenderWorld().isClientSide()) {
            return new ParticleEffectInstanceTrackerServer(entity);
        } else {
            return new ParticleEffectInstanceTracker(entity);
        }
    }
}
