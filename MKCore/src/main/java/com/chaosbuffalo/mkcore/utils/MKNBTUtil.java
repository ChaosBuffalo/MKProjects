package com.chaosbuffalo.mkcore.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class MKNBTUtil {

    public static void writeResourceLocation(CompoundTag tag, String name, ResourceLocation value) {
        tag.putString(name, value.toString());
    }

    public static ResourceLocation readResourceLocation(CompoundTag tag, String name) {
        String raw = tag.getString(name);
        return new ResourceLocation(raw);
    }

    public static Vec3 readVector3(CompoundTag nbt, String name) {
        CompoundTag tag = nbt.getCompound(name);
        return new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
    }

    public static void writeVector3d(CompoundTag tag, String name, Vec3 value) {
        CompoundTag state = new CompoundTag();
        state.putDouble("X", value.x());
        state.putDouble("Y", value.y());
        state.putDouble("Z", value.z());
        tag.put(name, state);
    }
}
