package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class PendingGenerationCondition extends DialogueCondition {
    private static final PendingGenerationCondition INSTANCE = new PendingGenerationCondition();
    public static final MapCodec<PendingGenerationCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.PENDING_QUEST_GENERATION.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return IEntityNpcData.get(source)
                .map(x -> x.shouldHaveQuest() && !x.hasGeneratedQuest())
                .orElse(false);
    }

    @Override
    public PendingGenerationCondition copy() {
        return this;
    }
}
