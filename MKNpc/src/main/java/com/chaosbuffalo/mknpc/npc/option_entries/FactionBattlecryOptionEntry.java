package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import com.chaosbuffalo.mknpc.npc.options.FactionBattlecryOption;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class FactionBattlecryOptionEntry implements INpcOptionEntry {
    public static final Codec<FactionBattlecryOptionEntry> CODEC = ExtraCodecs.COMPONENT
            .xmap(FactionBattlecryOptionEntry::new, FactionBattlecryOptionEntry::getBattlecry);

    @Nullable
    private final Component battlecry;

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

    public FactionBattlecryOptionEntry(Component text) {
        this.battlecry = text;
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
