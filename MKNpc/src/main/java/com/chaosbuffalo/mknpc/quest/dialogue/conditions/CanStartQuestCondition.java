package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.capabilities.PlayerQuestingDataHandler;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.chaosbuffalo.mknpc.quest.dialogue.effects.IReceivesChainId;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;
import java.util.UUID;

public class CanStartQuestCondition extends DialogueCondition implements IReceivesChainId {
    public static final Codec<CanStartQuestCondition> CODEC = RecordCodecBuilder.<CanStartQuestCondition>mapCodec(builder ->
            builder.group(
                    UUIDUtil.STRING_CODEC.optionalFieldOf("questId").forGetter(i -> i.questId.equals(Util.NIL_UUID) ? Optional.empty() : Optional.of(i.questId)),
                    Codec.BOOL.fieldOf("allowRepeat").forGetter(i -> i.allowRepeat)
            ).apply(builder, CanStartQuestCondition::new)
    ).codec();

    private UUID questId;
    private final boolean allowRepeat;

    private CanStartQuestCondition(Optional<UUID> questId, boolean allowRepeat) {
        this(questId.orElse(Util.NIL_UUID), allowRepeat);
    }

    public CanStartQuestCondition(UUID questId, boolean allowRepeat) {
        this.questId = questId;
        this.allowRepeat = allowRepeat;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.CAN_START_QUEST.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return MKNpc.getPlayerQuestData(player).map(x -> {
            PlayerQuestingDataHandler.QuestStatus status = x.getQuestStatus(questId);
            if (status == PlayerQuestingDataHandler.QuestStatus.NOT_ON) {
                return true;
            } else {
                return allowRepeat && status != PlayerQuestingDataHandler.QuestStatus.IN_PROGRESS;
            }
        }).orElse(false);
    }

    @Override
    public CanStartQuestCondition copy() {
        return new CanStartQuestCondition(questId, allowRepeat);
    }

    @Override
    public void setChainId(UUID chainId) {
        this.questId = chainId;
    }
}
