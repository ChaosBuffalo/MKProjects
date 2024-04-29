package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class AddLevelEffect extends DialogueEffect {
    public static final Codec<AddLevelEffect> CODEC = RecordCodecBuilder.<AddLevelEffect>mapCodec(builder ->
            builder.group(
                    Codec.INT.fieldOf("amount").forGetter(i -> i.levelAmount)
            ).apply(builder, AddLevelEffect::new)
    ).codec();

    private final int levelAmount;

    public AddLevelEffect(int levelAmount) {
        super();
        this.levelAmount = levelAmount;
    }

    @Override
    public DialogueEffectType<?> getType() {
        return DialogueEffectTypes.ADD_LEVEL.get();
    }

    @Override
    public AddLevelEffect copy() {
        // No runtime mutable state
        return this;
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity source, DialogueNode node) {
        player.giveExperienceLevels(levelAmount);
    }
}
