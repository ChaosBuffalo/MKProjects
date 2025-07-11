package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Optional;

public class SyncEntity<T extends Entity> implements ISyncObject {
    @Nullable
    private T value;
    private final Class<T> clazz;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncEntity(String name, T value, Class<T> clazz) {
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
    public void clearDirty() {
        dirty = false;
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context) {
        return IntTag.valueOf(value != null ? value.getId() : -1);
    }

    @Override
    public @Nullable Tag writeUpdateValue(SyncContext context) {
        dirty = false;
        return writeFullValue(context);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
        if (valueTag instanceof IntTag intTag) {
            int id = intTag.getId();
            if (id != -1) {
                Entity ent = ClientHandler.handleClient(id);
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

    static class ClientHandler {
        public static Entity handleClient(int entityId) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return null;
            return mc.level.getEntity(entityId);
        }
    }
}

