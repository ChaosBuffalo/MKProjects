package com.chaosbuffalo.mknpc.quest.requirements;

import com.chaosbuffalo.mkchat.dialogue.conditions.DialogueCondition;
import com.chaosbuffalo.mknpc.quest.QuestRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;

public abstract class QuestRequirement {
    public static final Codec<QuestRequirement> CODEC = ExtraCodecs.lazyInitializedCodec(() ->
            QuestRegistries.QUEST_REQUIREMENTS.getCodec().dispatch(QuestRequirement::getType, QuestRequirementType::codec));

    public abstract QuestRequirementType<? extends QuestRequirement> getType();

    public abstract boolean meetsRequirements(Player player);

    public abstract DialogueCondition getDialogueCondition();

}
