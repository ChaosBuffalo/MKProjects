package com.chaosbuffalo.mkcore.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MKCommand {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("mk")
                .then(StatCommand.register())
                .then(CooldownCommand.register())
                .then(AbilityCommand.register())
                .then(EffectCommand.register())
                .then(PersonaCommand.register())
                .then(TalentCommand.register())
                .then(HotBarCommand.register())
                .then(ParticleEffectsCommand.register());
        dispatcher.register(builder);
    }
}
