package com.chaosbuffalo.mkcore.sync;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class SyncEntity<T extends Entity> implements ISyncObject {
    private final String name;
    @Nullable
    private T value;
    private final Class<T> clazz;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncEntity(String name, T value, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
        set(value);
    }

    public boolean isValid() {
        return value != null;
    }

    public void set(T value) {
        boolean isPrev = this.value == value;
        this.value = value;
        if (!isPrev) {
            this.dirty = true;
            parentNotifier.notifyUpdate(this);
        }

    }

    public Optional<T> target() {
        return Optional.ofNullable(get());
    }

    @Nullable
    public T get() {
        return value;
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        if (tag.contains(name)) {
            CompoundTag update = tag.getCompound(name);
            boolean isPlayer = update.getBoolean("is_player");
            if (isPlayer) {
                UUID id = update.getUUID("player");
                Entity ent = ClientHandler.handleClient(id);
                if (clazz.isInstance(ent)) {
                    value = clazz.cast(ent);
                } else {
                    value = null;
                }
            } else {
                int id = update.getInt("mob");
                if (id != -1) {
                    Entity ent = ClientHandler.handleClient(tag.getId());
                    if (clazz.isInstance(ent)) {
                        value = clazz.cast(ent);
                    } else {
                        value = null;
                    }
                } else {
                    value = null;
                }
            }
        }
    }

    @Override
    public void serializeUpdate(CompoundTag tag) {
        if (dirty) {
            serializeFull(tag);
            dirty = false;
        }
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        CompoundTag newTag = new CompoundTag();
        if (value instanceof Player) {
            newTag.putBoolean("is_player", true);
            newTag.putUUID("player", value.getUUID());
        } else {
            newTag.putBoolean("is_player", false);
            newTag.putInt("mob", value != null ? value.getId() : -1);
        }
        tag.put(name, newTag);
        dirty = false;
    }

    static class ClientHandler {
        public static Entity handleClient(int entityId) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return null;
            return mc.level.getEntity(entityId);
        }

        public static Entity handleClient(UUID playerId) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return null;
            return mc.level.getPlayerByUUID(playerId);
        }
    }
}

