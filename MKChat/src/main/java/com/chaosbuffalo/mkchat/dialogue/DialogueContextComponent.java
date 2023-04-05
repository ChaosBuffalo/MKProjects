package com.chaosbuffalo.mkchat.dialogue;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class DialogueContextComponent implements ComponentContents {
    private final String key;
    private final Function<DialogueContext, List<Object>> argsSupplier;

    public DialogueContextComponent(String key, Function<DialogueContext, List<Object>> argsSupplier) {
        this.key = key;
        this.argsSupplier = argsSupplier;
    }

    private MutableComponent process(DialogueContext context) {
        return Component.translatable(key, argsSupplier.apply(context).toArray());
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack sourceStack, @Nullable Entity pEntity,
                                    int pRecursionDepth) throws CommandSyntaxException {
        if (sourceStack.getSigningContext() instanceof DumbArgumentSmuggler smuggler) {
            return process(smuggler.context());
        }
        return ComponentContents.super.resolve(sourceStack, pEntity, pRecursionDepth);
    }

    public static MutableComponent create(String msg, Function<DialogueContext, List<Object>> argsSupplier) {
        return MutableComponent.create(new DialogueContextComponent(msg, argsSupplier));
    }

    private static final CommandSourceStack SOURCE_STACK = new CommandSourceStack(CommandSource.NULL, Vec3.ZERO,
            Vec2.ZERO, null, 0, "", CommonComponents.EMPTY, null, null);

    public static MutableComponent evaluate(Component message, DialogueContext context) {
        CommandSourceStack sourceStack = SOURCE_STACK.withSigningContext(new DumbArgumentSmuggler(context));

        try {
            return ComponentUtils.updateForEntity(sourceStack, message, null, 0);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
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
