package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.npc.NpcOptionEntryTypes;
import com.chaosbuffalo.mknpc.npc.options.FactionNameOption;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;


public class FactionNameOptionEntry implements INpcOptionEntry, INameEntry {
    public static final Codec<FactionNameOptionEntry> CODEC = Codec.STRING.xmap(FactionNameOptionEntry::new, i -> i.name);
    public static final MapCodec<FactionNameOptionEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(i -> i.name)
    ).apply(builder, FactionNameOptionEntry::new));

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
    public NpcOptionEntryType<? extends INpcOptionEntry> getType() {
        return NpcOptionEntryTypes.FACTION_NAME.get();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("name", name);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.name = nbt.getString("name");
    }

    @Override
    public MutableComponent getName() {
        return Component.literal(name);
    }
}
