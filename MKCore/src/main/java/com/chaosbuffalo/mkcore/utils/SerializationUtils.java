package com.chaosbuffalo.mkcore.utils;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class SerializationUtils {

    public static Component fromCompoundNbt(CompoundTag nbt) {
        return NbtUtils.toPrettyComponent(nbt);
    }

    public static CompoundTag fromJsonString(String nbtString) throws CommandSyntaxException {
        return TagParser.parseTag(nbtString);
    }
}
