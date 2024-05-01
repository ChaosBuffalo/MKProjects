package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.npc.options.FactionNameOption;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public class FactionNameOptionEntry implements INpcOptionEntry, INameEntry {
    public static final Codec<FactionNameOptionEntry> CODEC = Codec.STRING.xmap(FactionNameOptionEntry::new, i -> i.name);

    private String name;

    public FactionNameOptionEntry(String name) {
        this.name = name;
    }

    @Override
    public ResourceLocation getOptionId() {
        return FactionNameOption.NAME;
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (!name.isEmpty() && entity instanceof LivingEntity) {
            entity.setCustomName(getName());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", name);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.name = nbt.getString("name");
    }

    @Override
    public MutableComponent getName() {
        return Component.literal(name);
    }
}
