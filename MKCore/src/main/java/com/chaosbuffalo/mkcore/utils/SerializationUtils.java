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

    public static <D> D serializeItemStack(DynamicOps<D> ops, ItemStack stack) {
        CompoundTag nbt = new CompoundTag();
        stack.save(nbt);
        return ops.createString(fromCompoundNbt(nbt).getString());
    }

    public static <D> ItemStack deserializeItemStack(Dynamic<D> dynamic) {
        Optional<String> nbtString = dynamic.asString().result();
        if (nbtString.isPresent()) {
            try {
                CompoundTag nbt = fromJsonString(nbtString.get());
                return ItemStack.of(nbt);
            } catch (CommandSyntaxException e) {
                MKCore.LOGGER.error("Failed to deserialize nbt string {}",
                        e.getMessage());
            }
        }
        return ItemStack.EMPTY;
    }
}
