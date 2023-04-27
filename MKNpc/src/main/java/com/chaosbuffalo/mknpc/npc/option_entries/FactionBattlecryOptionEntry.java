package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.entity.MKEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class FactionBattlecryOptionEntry implements INpcOptionEntry {
    @Nullable
    private Component battlecry;

    public FactionBattlecryOptionEntry() {
        this.battlecry = null;
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

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (battlecry != null) {
            nbt.putString("battlecry", Component.Serializer.toJson(battlecry));
        }

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("battlecry")) {
            battlecry = Component.Serializer.fromJson(nbt.getString("battlecry"));
        }
    }

    @Nullable
    public Component getBattlecry() {
        return battlecry;
    }
}
