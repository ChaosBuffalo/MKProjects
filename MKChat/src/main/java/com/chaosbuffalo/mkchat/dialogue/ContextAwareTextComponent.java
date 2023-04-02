package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.List;
import java.util.function.Function;

public class ContextAwareTextComponent extends TextComponent {
    private final Function<DialogueContext, List<Object>> argsSupplier;

    public ContextAwareTextComponent(String msg, Function<DialogueContext, List<Object>> argsSupplier) {
        super(msg);
        this.argsSupplier = argsSupplier;
    }

    public Component getContextFormattedTextComponent(DialogueContext context) {
        return new TranslatableComponent(getText(), argsSupplier.apply(context).toArray());
    }
}
