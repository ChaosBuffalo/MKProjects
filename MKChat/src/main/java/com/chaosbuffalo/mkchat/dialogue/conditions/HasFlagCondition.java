package com.chaosbuffalo.mkchat.dialogue.conditions;

import com.chaosbuffalo.mkchat.capabilities.IPlayerDialogue;
import com.chaosbuffalo.mkchat.dialogue.effects.AddFlagEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class HasFlagCondition extends DialogueCondition {
    public static final Codec<HasFlagCondition> CODEC = RecordCodecBuilder.<HasFlagCondition>mapCodec(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("flag").forGetter(i -> i.flagName)
            ).apply(builder, HasFlagCondition::new)
    ).codec();

    private final ResourceLocation flagName;

    public HasFlagCondition(ResourceLocation flagName) {
        this.flagName = flagName;
    }

    @Override
    public DialogueConditionType<? extends DialogueCondition> getType() {
        return DialogueConditionTypes.HAS_FLAG.get();
    }

    @Override
    public HasFlagCondition copy() {
        return new HasFlagCondition(flagName);
    }

    @Override
    public boolean meetsCondition(ServerPlayer player, LivingEntity source) {
        return IPlayerDialogue.get(player)
                .map(cap -> cap.getConversationMemory(source).getBoolFlag(flagName))
                .orElse(false);
    }
}
