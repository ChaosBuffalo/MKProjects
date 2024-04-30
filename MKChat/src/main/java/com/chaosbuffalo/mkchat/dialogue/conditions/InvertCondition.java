package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class InvertCondition extends DialogueCondition {
    public static final Codec<InvertCondition> CODEC = RecordCodecBuilder.<InvertCondition>mapCodec(builder ->
            builder.group(
                    DialogueCondition.CODEC.fieldOf("condition").forGetter(i -> i.condition)
            ).apply(builder, InvertCondition::new)
    ).codec();

    private final DialogueCondition condition;

    public InvertCondition(DialogueCondition condition) {
        this.condition = condition;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return DialogueConditionTypes.INVERT.get();
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return !condition.meetsCondition(player, source);
    }

    @Override
    public DialogueCondition copy() {
        return new InvertCondition(condition);
    }
}
