package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkfaction.capabilities.IPlayerFaction;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class FactionReward extends QuestReward {
    public static final MapCodec<FactionReward> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("faction_amount").forGetter(i -> i.factionAmount),
            MKFaction.REFERENCE_CODEC.fieldOf("faction").forGetter(i -> i.faction)
    ).apply(builder, FactionReward::new));

    private final int factionAmount;
    private final Holder<MKFaction> faction;

    public FactionReward(int factionAmount, Holder<MKFaction> faction) {
        this.factionAmount = factionAmount;
        this.faction = faction;
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.FACTION_REWARD.get();
    }

    @Override
    public Component getDescription() {
        return Component.translatable("mknpc.quest_reward.faction.name", factionAmount, MKFaction.getDisplayName(faction.getKey()));
    }

    @Override
    public void grantReward(Player player) {
        IPlayerFaction playerFaction = IPlayerFaction.getOrThrow(player);

        playerFaction.getFactionEntry(faction).ifPresent(r -> r.incrementFaction(factionAmount));

        player.sendSystemMessage(Component.translatable("mknpc.quest_reward.faction.message", factionAmount, MKFaction.getDisplayName(faction.getKey()))
                .withStyle(ChatFormatting.GOLD));
    }
}

