package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class SyncVec3 extends SyncObject<Vec3> {
    @Nullable
    private Consumer<Vec3> onSetCallback;

    public SyncVec3(String name, Vec3 value) {
        super(name, value);
    }

    public void setCallback(Consumer<Vec3> onSetCallback) {
        this.onSetCallback = onSetCallback;
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
    public void serializeFull(CompoundTag tag) {
        CompoundTag root = new CompoundTag();
        root.putDouble("x", value.x);
        root.putDouble("y", value.y);
        root.putDouble("z", value.z);
        tag.put(name, root);
    }
}
