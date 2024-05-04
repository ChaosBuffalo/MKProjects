package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.capabilities.IPlayerDialogue;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class AddFlagEffect extends DialogueEffect {
    public static final Codec<AddFlagEffect> CODEC = RecordCodecBuilder.<AddFlagEffect>mapCodec(builder ->
            builder.group(
                    ResourceLocation.CODEC.fieldOf("flag").forGetter(i -> i.flagName)
            ).apply(builder, AddFlagEffect::new)
    ).codec();

    private final ResourceLocation flagName;

    public AddFlagEffect(ResourceLocation flagName) {
        this.flagName = flagName;
    }

    @Override
    public DialogueEffectType<?> getType() {
        return DialogueEffectTypes.ADD_FLAG.get();
    }

    @Override
    public AddFlagEffect copy() {
        // No runtime mutable state
        return this;
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity source, DialogueNode node) {
        IPlayerDialogue.get(player).ifPresent(cap -> cap.getConversationMemory(source).setBoolFlag(flagName, true));
    }
}
