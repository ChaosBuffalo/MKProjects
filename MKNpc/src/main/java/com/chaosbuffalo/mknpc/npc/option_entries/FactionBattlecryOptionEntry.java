package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcOptionEntryTypes;
import com.chaosbuffalo.mknpc.npc.options.FactionBattlecryOption;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class FactionBattlecryOptionEntry implements INpcOptionEntry {
    public static final Codec<FactionBattlecryOptionEntry> CODEC = ComponentSerialization.CODEC.xmap(FactionBattlecryOptionEntry::new, FactionBattlecryOptionEntry::getBattlecry);
    public static final MapCodec<FactionBattlecryOptionEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("battlecry").forGetter(i -> i.battlecry)
    ).apply(builder, FactionBattlecryOptionEntry::new));

    @Nullable
    private Component battlecry;

    public FactionBattlecryOptionEntry() {
        this.battlecry = null;
    }

    @Override
    public ResourceLocation getOptionId() {
        return FactionBattlecryOption.NAME;
    }

    @Override
    public boolean isValid() {
        return battlecry != null;
    }

    @Override
    public NpcOptionEntryType<? extends INpcOptionEntry> getType() {
        return NpcOptionEntryTypes.FACTION_BATTLECRY.get();
    }

    public FactionBattlecryOptionEntry(Component text) {
        this.battlecry = text;
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setBattlecry(getBattlecry());
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        if (battlecry != null) {
            nbt.putString("battlecry", Component.Serializer.toJson(battlecry, provider));
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("battlecry")) {
            battlecry = Component.Serializer.fromJson(nbt.getString("battlecry"), provider);
        }
    }

    @Nullable
    public Component getBattlecry() {
        return battlecry;
    }
}
