package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.IMKEntityData;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.effects.MKEffectTickAction;
import com.chaosbuffalo.mkcore.network.EntityEffectPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityEffectHandler {
    protected final IMKEntityData entityData;
    protected final Map<UUID, EffectSource> sources = new HashMap<>();

    public EntityEffectHandler(IMKEntityData entityData) {
        this.entityData = entityData;
    }

    public class EffectSource {
        private final UUID sourceId;
        protected final Map<MKEffect, MKActiveEffect> activeEffectMap = new HashMap<>();

        public EffectSource(UUID sourceId) {
            this.sourceId = sourceId;
        }

        public void tick() {
            if (isEmpty())
                return;

            List<MKActiveEffect> activeEffects = ImmutableList.copyOf(activeEffectMap.values());
            activeEffects.forEach(active -> {
                MKEffectTickAction action = active.tick(entityData);
                if (action == MKEffectTickAction.Update) {
                    onEffectUpdated(active);
                } else if (action == MKEffectTickAction.Remove) {
                    removeEffectInstance(active);
                }
            });
        }

        private void removeEffectInstance(MKActiveEffect expiredInstance) {
            MKCore.LOGGER.debug("EntityEffectHandler.removeEffectInstance {} from {}", expiredInstance, entityData.getEntity());
            activeEffectMap.remove(expiredInstance.getEffect());
            // Run the callbacks after removal, so they won't see the effect as active
            onEffectRemoved(expiredInstance);
        }

        public void removeEffect(MKEffect effect) {
//            MKCore.LOGGER.debug("EntityEffectHandler.removeEffect {} from {}", effect, entityData.getEntity());
            MKActiveEffect expiredInstance = activeEffectMap.get(effect);
            if (expiredInstance != null) {
                removeEffectInstance(expiredInstance);
            }
        }

        public void addEffect(MKActiveEffect activeEffect) {
            if (!activeEffect.getState().validateOnApply(entityData, activeEffect)) {
                MKCore.LOGGER.warn("Effect {} failed apply validation", activeEffect);
                return;
            }
            if (activeEffect.getBehaviour().isTimed()) {
//                MKCore.LOGGER.debug("EntityEffectHandler.addEffect timed {} to {}", activeEffect, entityData.getEntity());
                MKActiveEffect existing = activeEffectMap.get(activeEffect.getEffect());
                if (existing == null) {
                    activeEffectMap.put(activeEffect.getEffect(), activeEffect);
                    onNewEffect(activeEffect);
                } else {
                    existing.getState().combine(existing, activeEffect);
                    onEffectUpdated(existing);
                }
            } else {
//                MKCore.LOGGER.debug("EntityEffectHandler.addEffect instant {} to {}", activeEffect, entityData.getEntity());
                if (entityData.isServerSide()) {
                    activeEffect.getState().performEffect(entityData, activeEffect);
                }
            }
        }

        // Server-side only
        private void loadEffect(MKActiveEffect activeEffect) {
//            MKCore.LOGGER.debug("EntityEffectHandler.EffectSource.loadEffect {}", activeEffect);
            activeEffect.getEffect().onInstanceLoaded(entityData, activeEffect);
            activeEffectMap.put(activeEffect.getEffect(), activeEffect);
        }

        // Server-side only
        private void onLevelReady(MKActiveEffect activeEffect) {
//            MKCore.LOGGER.debug("EntityEffectHandler.onLevelReady {}", activeEffect);
            activeEffect.getEffect().onInstanceReady(entityData, activeEffect);
        }

        // Server-side only
        protected void onNewEffect(MKActiveEffect activeEffect) {
//            MKCore.LOGGER.debug("EntityEffectHandler.onNewEffect {}", activeEffect);
            if (entityData.isServerSide()) {
                activeEffect.getEffect().onInstanceAdded(entityData, activeEffect);
                sendEffectSet(activeEffect);
            }
        }

        // Called on both sides
        protected void onEffectUpdated(MKActiveEffect activeEffect) {
//            MKCore.LOGGER.debug("EntityEffectHandler.onEffectUpdated {}", activeEffect);
            if (entityData.isServerSide()) {
                if (activeEffect.getEffect().onInstanceUpdated(entityData, activeEffect)) {
                    removeEffectInstance(activeEffect);
                } else {
                    sendEffectSet(activeEffect);
                }
            }
        }

        // Called on both sides
        protected void onEffectRemoved(MKActiveEffect activeEffect) {
//            MKCore.LOGGER.debug("EntityEffectHandler.onEffectRemoved {}", activeEffect);
            if (entityData.isServerSide()) {
                activeEffect.getEffect().onInstanceRemoved(entityData, activeEffect);
                if (!activeEffect.getBehaviour().isExpired()) {
                    // If it was removed early we need to tell the client
                    sendEffectRemove(activeEffect);
                }
            }
        }

        public boolean isEffectActive(MKEffect effect) {
            return activeEffectMap.containsKey(effect);
        }

        public void clearEffects() {
            MKCore.LOGGER.debug("EntityEffectHandler.clearEffects");
            List<MKActiveEffect> remove = new ArrayList<>(activeEffectMap.values());
            remove.forEach(this::removeEffectInstance);
        }

        public boolean hasEffects() {
            return !activeEffectMap.isEmpty();
        }

        public boolean isEmpty() {
            return activeEffectMap.isEmpty();
        }

        public Stream<MKActiveEffect> effectsStream() {
            return activeEffectMap.values().stream();
        }

        public void onLevelReady() {
            if (hasEffects()) {
                activeEffectMap.values().forEach(this::onLevelReady);
            }
        }

        public void onDeath() {
            MKCore.LOGGER.debug("EffectSource.onDeath {} {}", activeEffectMap.size(), sourceId);
            activeEffectMap.clear();
        }

        public void sendAllEffectsToPlayer(ServerPlayer playerEntity) {
            if (hasEffects()) {
                EntityEffectPacket packet = new EntityEffectPacket(entityData, sourceId, activeEffectMap.values());
                PacketHandler.sendMessage(packet, playerEntity);
            }
        }

        public Tag serializeStorage() {
            ListTag list = new ListTag();
            activeEffectMap.forEach((effect, activeEffect) -> {
                if (!activeEffect.getBehaviour().isTemporary()) {
                    list.add(activeEffect.serializeStorage());
                }
            });

            return list;
        }

        public void deserializeStorage(CompoundTag nbt, String tagName) {

            ListTag list = nbt.getList(tagName, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                MKActiveEffect instance = MKActiveEffect.deserializeStorage(sourceId, entry);
                if (instance != null) {
                    loadEffect(instance);
                }
            }
        }

        public void clientSetEffect(MKActiveEffect activeEffect) {
            activeEffectMap.put(activeEffect.getEffect(), activeEffect);
        }

        public void clientRemoveEffect(MKActiveEffect activeEffect) {
            removeEffectInstance(activeEffect);
        }

        public void clientSetAllEffects(List<MKActiveEffect> activeEffects) {
            activeEffectMap.clear();
            for (MKActiveEffect instance : activeEffects) {
                activeEffectMap.put(instance.getEffect(), instance);
            }
        }
    }

    protected void sendEffectSet(MKActiveEffect activeEffect) {
        sendEffectPacket(activeEffect, EntityEffectPacket.Action.SET);
    }

    protected void sendEffectRemove(MKActiveEffect activeEffect) {
        sendEffectPacket(activeEffect, EntityEffectPacket.Action.REMOVE);
    }

    private void sendEffectPacket(MKActiveEffect activeEffect, EntityEffectPacket.Action action) {
        if (entityData.isServerSide()) {
            EntityEffectPacket packet = new EntityEffectPacket(entityData, activeEffect, action);
            if (entityData.getEntity().isAddedToWorld()) {
                PacketHandler.sendToTrackingAndSelf(packet, entityData.getEntity());
            } else if (entityData.getEntity().getType() != EntityType.PLAYER) {
                MKCore.LOGGER.warn("Tried to send effect {} ({}) to {} but not in world", activeEffect, action, entityData.getEntity());
            }
        }
    }

    protected EffectSource getOrCreateSource(UUID sourceId) {
        return sources.computeIfAbsent(sourceId, EffectSource::new);
    }

    protected boolean canTick() {
        return entityData.getEntity().isAlive();
    }

    public void tick() {
        if (!hasEffects() || !canTick())
            return;

        sources.values().forEach(EffectSource::tick);

        checkEmpty();
    }

    public void onJoinLevel() {
        if (entityData.isServerSide() && hasEffects()) {
            sources.values().forEach(EffectSource::onLevelReady);
        }
    }

    public void onDeath() {
        MKCore.LOGGER.debug("EntityEventHandler.onDeath");
        if (hasEffects()) {
            sources.values().forEach(EffectSource::onDeath);
        }
    }

    public void sendAllEffectsToPlayer(ServerPlayer playerEntity) {
        sources.forEach((sourceId, source) -> source.sendAllEffectsToPlayer(playerEntity));
    }

    public boolean hasEffects() {
        return !sources.isEmpty();
    }

    public boolean isEffectActive(MKEffect effect) {
        if (!hasEffects())
            return false;
        return sources.values().stream().anyMatch(s -> s.isEffectActive(effect));
    }

    private void checkEmpty() {
        sources.values().removeIf(EffectSource::isEmpty);
    }

    public void removeEffect(MKEffect effect) {
        if (hasEffects()) {
            sources.values().forEach(s -> s.removeEffect(effect));
        }
    }

    public void removeEffect(UUID sourceId, MKEffect effect) {
        EffectSource source = sources.get(sourceId);
        if (source != null) {
            source.removeEffect(effect);
        }
    }

    public void clearEffects() {
        if (hasEffects()) {
            sources.values().forEach(EffectSource::clearEffects);
        }
    }

    public void addEffect(MKEffectBuilder<?> builder) {
        addEffect(builder.getSourceId(), builder.createApplication());
    }

    public void addEffect(MKActiveEffect activeEffect) {
        addEffect(activeEffect.getSourceId(), activeEffect);
    }

    public void addEffect(UUID sourceId, MKActiveEffect effectInstance) {
        getOrCreateSource(sourceId).addEffect(effectInstance);
    }

    public Collection<MKActiveEffect> effects(MKEffect effect) {
        return sources.values().stream()
                .flatMap(EffectSource::effectsStream)
                .filter(activeEffect -> activeEffect.getEffect() == effect)
                .collect(Collectors.toList());
    }

    public Collection<MKActiveEffect> effects() {
        return sources.values().stream()
                .flatMap(EffectSource::effectsStream)
                .collect(Collectors.toList());
    }

    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();

        ListTag list = new ListTag();
        sources.forEach((sourceId, source) -> {
            if (source.hasEffects()) {
                CompoundTag entry = new CompoundTag();
                entry.putUUID("uuid", sourceId);
                entry.put("effects", source.serializeStorage());
                list.add(entry);
            }
        });
        nbt.put("sources", list);

        return nbt;
    }

    public void deserialize(CompoundTag nbt) {
        ListTag list = nbt.getList("sources", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID sourceId = entry.getUUID("uuid");
            getOrCreateSource(sourceId).deserializeStorage(entry, "effects");
        }
    }

    public void clientSetEffect(UUID sourceId, MKActiveEffect effectInstance) {
        getOrCreateSource(sourceId).clientSetEffect(effectInstance);
    }

    public void clientRemoveEffect(UUID sourceId, MKActiveEffect effectInstance) {
        EffectSource source = sources.get(sourceId);
        if (source != null) {
            source.clientRemoveEffect(effectInstance);
        }
    }

    public void clientSetAllEffects(UUID sourceId, List<MKActiveEffect> instances) {
        getOrCreateSource(sourceId).clientSetAllEffects(instances);
    }
}
