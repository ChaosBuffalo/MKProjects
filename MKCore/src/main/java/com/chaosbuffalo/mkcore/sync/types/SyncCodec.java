package com.chaosbuffalo.mkcore.sync.types;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

public class SyncCodec<T> extends SyncObject<T> {

    private final Codec<T> codec;

    public SyncCodec(String name, T instance, Codec<T> codec) {
        super(name, instance);
        this.codec = codec;
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        if (tag.contains(name)) {
            value = codec.parse(new Dynamic<>(NbtOps.INSTANCE, tag.get(name)))
                    .getOrThrow(false, MKCore.LOGGER::error);
        }
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        codec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(t -> tag.put(name, t));
    }
}
