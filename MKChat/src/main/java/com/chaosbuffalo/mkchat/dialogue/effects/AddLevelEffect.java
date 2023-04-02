package com.chaosbuffalo.mkchat.dialogue.effects;

import com.chaosbuffalo.mkchat.MKChat;
import com.chaosbuffalo.mkchat.dialogue.DialogueNode;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

public class AddLevelEffect extends DialogueEffect {
    private int levelAmount;
    public static ResourceLocation effectTypeName = new ResourceLocation(MKChat.MODID, "dialogue_effect.add_level");

    public AddLevelEffect(int levelAmount) {
        super(effectTypeName);
        this.levelAmount = levelAmount;
    }

    public AddLevelEffect() {
        this(0);
    }

    @Override
    public AddLevelEffect copy() {
        // No runtime mutable state
        return new AddLevelEffect(levelAmount);
    }

    @Override
    public void applyEffect(ServerPlayer player, LivingEntity source, DialogueNode node) {
        player.giveExperienceLevels(levelAmount);
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        this.levelAmount = dynamic.get("amount").asInt(0);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("amount"), ops.createInt(levelAmount));
    }
}
