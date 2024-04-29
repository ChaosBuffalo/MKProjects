package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;

public abstract class QuestReward {
    public static final Codec<QuestReward> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            QuestRegistries.QUEST_REWARDS.getCodec().dispatch(QuestReward::getType, QuestRewardType::codec));

    public abstract QuestRewardType<? extends QuestReward> getType();

    public abstract Component getDescription();

    public abstract void grantReward(Player player);
}
