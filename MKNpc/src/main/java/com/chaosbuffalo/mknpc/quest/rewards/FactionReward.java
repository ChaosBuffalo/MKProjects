package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkfaction.capabilities.IPlayerFaction;
import com.chaosbuffalo.mkfaction.event.MKFactionRegistry;
import com.chaosbuffalo.mkfaction.faction.MKFaction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class FactionReward extends QuestReward {
    public static final MapCodec<FactionReward> MAP_CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codec.INT.fieldOf("faction_amount").forGetter(i -> i.factionAmount),
                    ResourceLocation.CODEC.fieldOf("faction").forGetter(i -> i.faction)
            ).apply(builder, FactionReward::new)
    );

    private final int factionAmount;
    private final ResourceLocation faction;

    public FactionReward(int factionAmount, ResourceLocation faction) {
        this.factionAmount = factionAmount;
        this.faction = faction;
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.FACTION_REWARD.get();
    }

    @Override
    public Component getDescription() {
        MKFaction fac = MKFactionRegistry.getFaction(faction);
        if (fac != null) {
            return Component.translatable("mknpc.quest_reward.faction.name", factionAmount, fac.getDisplayName());
        } else {
            return Component.translatable("mknpc.quest_reward.faction.name", factionAmount, "invalid faction");
        }

    }

    @Override
    public void grantReward(Player player) {
        IPlayerFaction.get(player).ifPresent(x -> {
            x.addRepToFaction(faction, factionAmount);
            MKFaction fac = MKFactionRegistry.getFaction(faction);
            if (fac != null) {
                player.sendSystemMessage(Component.translatable("mknpc.quest_reward.faction.message", factionAmount, fac.getDisplayName())
                        .withStyle(ChatFormatting.GOLD));
            }

        });
    }
}

