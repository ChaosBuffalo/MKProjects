package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.command.arguments.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;

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

    public static void registerArguments() {
        ArgumentTypes.register("ability_id", AbilityIdArgument.class, new EmptyArgumentSerializer<>(AbilityIdArgument::ability));
        ArgumentTypes.register("ability_group", HotBarCommand.AbilityGroupArgument.class, new EmptyArgumentSerializer<>(HotBarCommand.AbilityGroupArgument::abilityGroup));
        ArgumentTypes.register("talent_id", TalentIdArgument.class, new EmptyArgumentSerializer<>(TalentIdArgument::talentId));
        ArgumentTypes.register("talent_tree_id", TalentTreeIdArgument.class, new EmptyArgumentSerializer<>(TalentTreeIdArgument::talentTreeId));
        ArgumentTypes.register("talent_line_id", TalentLineIdArgument.class, new EmptyArgumentSerializer<>(TalentLineIdArgument::talentLine));
        ArgumentTypes.register("bone_id", BipedBoneArgument.class, new EmptyArgumentSerializer<>(BipedBoneArgument::BipedBone));
        ArgumentTypes.register("particle_animation_id", ParticleAnimationArgument.class, new EmptyArgumentSerializer<>(ParticleAnimationArgument::ParticleAnimation));
        ArgumentTypes.register("ability_source_type_id", AbilitySourceTypeArgument.class, new EmptyArgumentSerializer<>(AbilitySourceTypeArgument::abilitySourceType));
    }

}
