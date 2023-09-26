package com.chaosbuffalo.mkchat.dialogue;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.function.Function;

public class DialogueComponentContents implements ComponentContents {
    private final Function<DialogueContext, Component> valueSupplier;

    public DialogueComponentContents(Function<DialogueContext, Component> argsSupplier) {
        this.valueSupplier = argsSupplier;
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack sourceStack, @Nullable Entity pEntity,
                                    int pRecursionDepth) throws CommandSyntaxException {
        if (sourceStack.getSigningContext() instanceof DumbArgumentSmuggler smuggler) {
            var value = valueSupplier.apply(smuggler.context());
            return value instanceof MutableComponent mutVal ? mutVal : Component.empty().append(value);
        }
        return ComponentContents.super.resolve(sourceStack, pEntity, pRecursionDepth);
    }

    public static MutableComponent create(Function<DialogueContext, Component> valueSupplier) {
        return MutableComponent.create(new DialogueComponentContents(valueSupplier));
    }

    public static CommandSigningContext createSigningContext(DialogueContext context) {
        return new DumbArgumentSmuggler(context);
    }

    // This is so we don't need to copy ComponentUtils.updateForEntity just to add the DialogueContext argument
    private record DumbArgumentSmuggler(DialogueContext context) implements CommandSigningContext {

        @Nullable
        @Override
        public PlayerChatMessage getArgument(String pName) {
            return null;
        }
    }
}
