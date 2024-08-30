package com.chaosbuffalo.mknpc.quest.data.player;

import com.chaosbuffalo.mknpc.quest.rewards.QuestReward;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PlayerQuestReward implements INBTSerializable<CompoundTag> {
    private Component description;


    public PlayerQuestReward(QuestReward questReward) {
        this.description = questReward.getDescription();
    }

    public PlayerQuestReward(HolderLookup.Provider provider, CompoundTag nbt) {
        deserializeNBT(provider, nbt);
    }

    public Component getDescription() {
        return description;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("description", Component.Serializer.toJson(description, provider));
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        description = Component.Serializer.fromJson(nbt.getString("description"), provider);
    }
}
