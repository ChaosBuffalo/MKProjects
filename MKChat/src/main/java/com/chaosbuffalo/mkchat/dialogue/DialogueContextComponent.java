package com.chaosbuffalo.mkchat.dialogue;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DialogueContextComponent implements ComponentContents {
    private final String key;
    private final Function<DialogueContext, List<Object>> argsSupplier;

    public DialogueContextComponent(String key, Function<DialogueContext, List<Object>> argsSupplier) {
        this.key = key;
        this.argsSupplier = argsSupplier;
    }

    public MutableComponent process(DialogueContext context) {
        return Component.translatable(key, argsSupplier.apply(context).toArray());
    }

    public static MutableComponent create(String msg, Function<DialogueContext, List<Object>> argsSupplier) {
        return MutableComponent.create(new DialogueContextComponent(msg, argsSupplier));
    }

    public static void process(Component parent, DialogueContext context, Consumer<Component> consumer) {

        if (parent.getContents() instanceof DialogueContextComponent component) {
            consumer.accept(component.process(context));
        } else {
            consumer.accept(MutableComponent.create(parent.getContents()));
        }
        parent.getSiblings().forEach(c -> process(c, context, consumer));
    }
}
