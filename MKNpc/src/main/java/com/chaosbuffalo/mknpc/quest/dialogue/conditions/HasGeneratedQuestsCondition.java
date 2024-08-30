package com.chaosbuffalo.mknpc.quest.dialogue.conditions;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueConditionType;
import com.chaosbuffalo.mknpc.capabilities.IEntityNpcData;
import com.chaosbuffalo.mknpc.dialogue.NpcDialogueConditionTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class HasGeneratedQuestsCondition extends DialogueCondition {
    private static final HasGeneratedQuestsCondition INSTANCE = new HasGeneratedQuestsCondition();
    public static final MapCodec<HasGeneratedQuestsCondition> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return NpcDialogueConditionTypes.HAS_GENERATED_QUESTS.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return IEntityNpcData.get(source)
                .map(IEntityNpcData::hasGeneratedQuest)
                .orElse(false);
    }

    @Override
    public HasGeneratedQuestsCondition copy() {
        return this;
    }
}
