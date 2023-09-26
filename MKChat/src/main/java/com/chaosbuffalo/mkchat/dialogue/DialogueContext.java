package com.chaosbuffalo.mkchat.dialogue;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public record DialogueContext(LivingEntity speaker, ServerPlayer player, DialogueObject dialogueObject) {

    public MutableComponent evaluate(Component message) {
        return evaluate(speaker, message, this);
    }

    public static MutableComponent evaluate(LivingEntity speaker, Component message, DialogueContext context) {
        var ctx = DialogueComponentContents.createSigningContext(context);
        CommandSourceStack sourceStack = speaker.createCommandSourceStack().withSigningContext(ctx);

        try {
            return ComponentUtils.updateForEntity(sourceStack, message, context.player(), 0);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
