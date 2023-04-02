package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ResetAttackSwingPacket;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.chaosbuffalo.mkcore.utils.EntityUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class CooldownCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("cd")
                .then(Commands.literal("new")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("ticks", IntegerArgumentType.integer())
                                        .executes(CooldownCommand::newTimer)
                                )
                        )
                )
                .then(Commands.literal("list")
                        .executes(CooldownCommand::listTimer)
                )
                .then(Commands.literal("reset")
                        .executes(CooldownCommand::resetTimers)
                )
                .then(Commands.literal("attack_reset")
                        .executes(CooldownCommand::resetAttackCd)
                );
    }

    static int resetAttackCd(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        double cooldownPeriod = EntityUtils.getCooldownPeriod(player);

        PacketHandler.sendMessage(new ResetAttackSwingPacket((int) Math.round(cooldownPeriod)), player);

        return Command.SINGLE_SUCCESS;
    }

    static int newTimer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(ctx, "name");
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");

        MKCore.getPlayer(player).ifPresent(playerData -> {
            playerData.getStats().setTimer(MKCore.makeRL(name), ticks);
            ChatUtils.sendMessageWithBrackets(player, "Created timer %s with %d ticks", name, ticks);
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listTimer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MKCore.getPlayer(player).ifPresent(playerData -> playerData.getStats().printActiveCooldowns());

        return Command.SINGLE_SUCCESS;
    }

    static int resetTimers(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MKCore.getPlayer(player).ifPresent(playerData -> playerData.getStats().resetAllTimers());

        return Command.SINGLE_SUCCESS;
    }
}
