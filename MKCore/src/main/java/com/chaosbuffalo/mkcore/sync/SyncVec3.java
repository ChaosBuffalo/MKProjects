package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SyncVec3 implements ISyncObject{

    private final String name;
    private Vec3 value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    @Nullable
    Consumer<Vec3> onSetCallback;

    public SyncVec3(String name, Vec3 value) {
        this.value = value;
        this.name = name;
    }

    public void setCallback(Consumer<Vec3> onSetCallback) {
        this.onSetCallback = onSetCallback;
    }

    public Vec3 get() {
        return value;
    }

    public void set(Vec3 value) {
        this.value = value;
        this.dirty = true;
        parentNotifier.notifyUpdate(this);
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
            CompoundTag root = tag.getCompound(name);
            Vec3 prev = value;
            this.value = new Vec3(root.getDouble("x"), root.getDouble("y"), root.getDouble("z"));
            if (onSetCallback != null) {
                onSetCallback.accept(prev);
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
        CompoundTag root = new CompoundTag();
        root.putDouble("x", value.x);
        root.putDouble("y", value.y);
        root.putDouble("z", value.z);
        tag.put(name, root);
        dirty = false;
    }
}
