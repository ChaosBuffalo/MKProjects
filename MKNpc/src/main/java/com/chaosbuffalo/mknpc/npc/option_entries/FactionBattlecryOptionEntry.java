package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.NpcOptionEntryTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class FactionBattlecryOptionEntry implements INpcOptionEntry {
    public static final MapCodec<FactionBattlecryOptionEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ComponentSerialization.CODEC.fieldOf("battlecry").forGetter(i -> i.battlecry)
    ).apply(builder, FactionBattlecryOptionEntry::new));

    @Nullable
    private final Component battlecry;

    public FactionBattlecryOptionEntry() {
        this.battlecry = null;
    }

    public FactionBattlecryOptionEntry(Component text) {
        this.battlecry = text;
    }

    @Override
    public boolean isValid() {
        return battlecry != null;
    }

    @Override
    public NpcOptionEntryType<? extends INpcOptionEntry> getType() {
        return NpcOptionEntryTypes.FACTION_BATTLECRY.get();
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof MKEntity mkEntity) {
            mkEntity.setBattlecry(getBattlecry());
        }
    }

    @Nullable
    public Component getBattlecry() {
        return battlecry;
    }
}
