package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.PlayerQuestingDataHandler;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collections;
import java.util.UUID;

public class OnQuestCondition extends DialogueCondition {
    public static final Codec<OnQuestCondition> CODEC = RecordCodecBuilder.<OnQuestCondition>mapCodec(builder ->
            builder.group(
                    UUIDUtil.STRING_CODEC.fieldOf("questId").forGetter(i -> i.questId),
                    Codec.STRING.fieldOf("questStep").forGetter(i -> i.questStep)
            ).apply(builder, OnQuestCondition::new)
    ).codec();

    private final UUID questId;
    private final String questStep;

    public OnQuestCondition(UUID questId, String questStep) {
        this.questId = questId;
        this.questStep = questStep;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.ON_QUEST.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return MKNpc.getPlayerQuestData(player)
                .map(x -> x.getQuestStatus(questId) == PlayerQuestingDataHandler.QuestStatus.IN_PROGRESS &&
                        x.getCurrentQuestSteps(questId).orElse(Collections.emptyList()).contains(questStep))
                .orElse(false);
    }

    @Override
    public OnQuestCondition copy() {
        return new OnQuestCondition(questId, questStep);
    }
}
