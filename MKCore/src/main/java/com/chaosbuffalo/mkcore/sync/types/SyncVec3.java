package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SyncVec3 implements ISyncObject {
    private Vec3 value;
    private boolean dirty;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    @Nullable
    private Consumer<Vec3> onSetCallback;

    public SyncVec3(Vec3 value) {
        this.value = value;
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
    public void clearDirty() {
        dirty = false;
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context) {
        CompoundTag root = new CompoundTag();
        root.putDouble("x", value.x);
        root.putDouble("y", value.y);
        root.putDouble("z", value.z);
        return root;
    }

    @Override
    public @Nullable Tag writeUpdateValue(SyncContext context) {
        dirty = false;
        return writeFullValue(context);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
        if (valueTag instanceof CompoundTag root) {
            Vec3 prev = value;
            this.value = new Vec3(root.getDouble("x"), root.getDouble("y"), root.getDouble("z"));
            if (onSetCallback != null) {
                onSetCallback.accept(prev);
            }
        }
    }
}
