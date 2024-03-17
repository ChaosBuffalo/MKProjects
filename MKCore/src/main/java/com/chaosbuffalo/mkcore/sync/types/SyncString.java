package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SyncString extends SyncObject<String> {

    @Nullable
    private BiConsumer<String, String> onSetCallback;

    public SyncString(String name, String value) {
        super(name, value);
    }

    public void setCallback(BiConsumer<String, String> onSetCallback) {
        this.onSetCallback = onSetCallback;
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        if (tag.contains(name)) {
            String prev = value;
            value = tag.getString(name);
            if (onSetCallback != null) {
                onSetCallback.accept(prev, value);
            }
        }
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        tag.putString(name, value);
    }
}
