package com.chaosbuffalo.mknpc.quest.rewards;

import com.chaosbuffalo.mkfaction.capabilities.IPlayerFaction;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NotableNpcEntry;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.quest.QuestStructureLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public class NotableFactionOverrideReward extends QuestReward {
    public static final MapCodec<NotableFactionOverrideReward> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QuestStructureLocation.CODEC.fieldOf("structure").forGetter(i -> i.location),
            NpcDefinition.KEY_CODEC.fieldOf("npcDefinition").forGetter(i -> i.npcDefinition),
            Codec.INT.fieldOf("faction_score").forGetter(i -> i.factionScore)
    ).apply(builder, NotableFactionOverrideReward::new));

    private final QuestStructureLocation location;
    private final ResourceKey<NpcDefinition> npcDefinition;
    private final int factionScore;

    public NotableFactionOverrideReward(QuestStructureLocation location, ResourceKey<NpcDefinition> npcDefinition, int factionScore) {
        this.location = location;
        this.npcDefinition = npcDefinition;
        this.factionScore = factionScore;
    }

    @Override
    public QuestRewardType<? extends QuestReward> getType() {
        return QuestRewardTypes.NOTABLE_FACTION_OVERRIDE_REWARD.get();
    }

    @Override
    public Component getDescription() {
        return Component.translatable("mknpc.quest_reward.notable_faction_override.name", factionScore);
    }

    @Override
    public void grantReward(QuestRewardContext context) {
        Optional<NotableNpcEntry> notable = context.questChain()
                .getStructureId(location)
                .flatMap(context.worldNpcData()::getStructureData)
                .flatMap(entry -> entry.getFirstNotableOfType(npcDefinition, context.player().registryAccess()));

        if (notable.isEmpty()) {
            MKNpc.LOGGER.warn("Unable to resolve notable faction override reward target for quest {} at {} {}",
                    context.quest().getQuestName(), location, npcDefinition.location());
            return;
        }

        IPlayerFaction playerFaction = IPlayerFaction.getOrThrow(context.player());
        playerFaction.setNpcFactionOverride(notable.get().getSpawnerId(), factionScore);
        context.player().sendSystemMessage(Component.translatable(
                "mknpc.quest_reward.notable_faction_override.message",
                notable.get().getName().copy().withStyle(ChatFormatting.GOLD),
                factionScore
        ));
    }
}
