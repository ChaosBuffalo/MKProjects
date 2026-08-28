package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public final class MKWorkspaceChangePayloads {
    private MKWorkspaceChangePayloads() {
    }

    public static <T> CompoundTag encode(Codec<T> codec, T value, String description) {
        Tag encoded = codec.encodeStart(NbtOps.INSTANCE, value)
                .resultOrPartial(error -> MKWorkspace.LOGGER.error("Failed to encode {}: {}", description, error))
                .orElseThrow(() -> new IllegalArgumentException("Failed to encode " + description));
        if (encoded instanceof CompoundTag compound) {
            return compound;
        }
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("value", encoded);
        return wrapper;
    }
}
