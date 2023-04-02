package com.chaosbuffalo.mknpc.quest.data.player;

import com.chaosbuffalo.mknpc.quest.rewards.QuestReward;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerQuestReward implements INBTSerializable<CompoundTag> {
    MutableComponent description;


    public PlayerQuestReward(QuestReward questReward){
        this.description = questReward.getDescription();
    }

    public PlayerQuestReward(CompoundTag nbt){
        deserializeNBT(nbt);
    }

    public MutableComponent getDescription() {
        return description;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("description", Component.Serializer.toJson(description));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        description = Component.Serializer.fromJson(nbt.getString("description"));
    }
}
