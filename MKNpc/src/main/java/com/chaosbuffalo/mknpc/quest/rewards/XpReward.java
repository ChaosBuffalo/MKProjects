package com.chaosbuffalo.mknpc.quest.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class XpReward extends QuestReward {
    public static final Codec<XpReward> CODEC = RecordCodecBuilder.<XpReward>mapCodec(builder ->
            builder.group(
                    Codec.INT.fieldOf("xp_amount").forGetter(i -> i.xpAmount)
            ).apply(builder, XpReward::new)
    ).codec();

    private final int xpAmount;

    public XpReward(int xp) {
        this.xpAmount = xp;
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.XP_REWARD.get();
    }

    @Override
    public Component getDescription() {
        return Component.translatable("mknpc.quest_reward.xp.name", xpAmount);
    }

    @Override
    public void grantReward(Player player) {
        player.giveExperiencePoints(xpAmount);
        player.sendSystemMessage(Component.translatable("mknpc.quest_reward.xp.message", xpAmount)
                .withStyle(ChatFormatting.GOLD));
    }
}
