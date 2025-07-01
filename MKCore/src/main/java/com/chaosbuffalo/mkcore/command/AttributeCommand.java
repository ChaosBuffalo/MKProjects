package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicBoolean;

public class AttributeCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("attribute")
                .then(Commands.literal("dump")
                        .executes(AttributeCommand::learnAbility))
                ;
    }


    static int learnAbility(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        AtomicBoolean emptySent = new AtomicBoolean(false);
        ctx.getSource().registryAccess().registryOrThrow(Registries.ATTRIBUTE).holders().forEach(attr -> {
            var instance = player.getAttribute(attr);
            if (instance != null) {
                var mods = instance.getModifiers();
                if (!mods.isEmpty()) {
                    if (!emptySent.getAndSet(true)) {
                        ChatUtils.sendMessage(player, "");
                        ChatUtils.sendMessage(player, "");
                        ChatUtils.sendMessage(player, "");
                    }
                    ChatUtils.sendMessageWithBrackets(player, "Attribute %s", attr.getRegisteredName());
                    mods.forEach(m -> {
                        ChatUtils.sendMessage(player, " %s", m.id());
                    });
                }
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
