package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.PlayerQuestingDataHandler;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public class OnQuestChainCondition extends DialogueCondition {
    public static final Codec<OnQuestChainCondition> CODEC = RecordCodecBuilder.<OnQuestChainCondition>mapCodec(builder ->
            builder.group(
                    UUIDUtil.STRING_CODEC.optionalFieldOf("questId").forGetter(i -> i.questId.equals(Util.NIL_UUID) ? Optional.empty() : Optional.of(i.questId))
            ).apply(builder, OnQuestChainCondition::new)
    ).codec();

    private final UUID questId;

    private OnQuestChainCondition(Optional<UUID> questId) {
        this(questId.orElse(Util.NIL_UUID));
    }

    public OnQuestChainCondition(UUID questId) {
        this.questId = questId;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.ON_QUEST_CHAIN.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return MKNpc.getPlayerQuestData(player)
                .map(x -> x.getQuestStatus(questId) == PlayerQuestingDataHandler.QuestStatus.IN_PROGRESS)
                .orElse(false);
    }

    @Override
    public OnQuestChainCondition copy() {
        return new OnQuestChainCondition(questId);
    }
}
