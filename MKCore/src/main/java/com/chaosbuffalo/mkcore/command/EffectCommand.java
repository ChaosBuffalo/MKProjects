package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.effects.MKActiveEffect;
import com.chaosbuffalo.mkcore.effects.MKEffectBuilder;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.UUID;

public class EffectCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("effect")
                .then(Commands.literal("list")
                        .executes(EffectCommand::listEffects)
                )
                .then(Commands.literal("clear")
                        .executes(EffectCommand::clearEffects)
                )
                .then(Commands.literal("test")
                        .executes(EffectCommand::testEffects)
                )
                ;
    }

    static int listEffects(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Collection<MobEffectInstance> effects = player.getActiveEffects();
        if (effects.size() > 0) {
            ChatUtils.sendMessageWithBrackets(player, "Active MobEffects");
            for (MobEffectInstance instance : effects) {
                ChatUtils.sendMessage(player, "%s: %d", ForgeRegistries.MOB_EFFECTS.getKey(instance.getEffect()), instance.getDuration());
            }
        } else {
            ChatUtils.sendMessageWithBrackets(player, "No active MobEffects");
        }
        MKCore.getPlayer(player).ifPresent(playerData -> {
            Collection<MKActiveEffect> mkeffects = playerData.getEffects().effects();
            if (mkeffects.size() > 0) {
                ChatUtils.sendMessageWithBrackets(player, "Active MKEffects");
                for (MKActiveEffect instance : mkeffects) {
                    ChatUtils.sendMessage(player, "%s: %d %d", instance.getEffect().getId(), instance.getDuration(), instance.getStackCount());
                }
            } else {
                ChatUtils.sendMessageWithBrackets(player, "No active MKEffects");
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int clearEffects(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.removeAllEffects();
        MKCore.getPlayer(player).ifPresent(playerData -> playerData.getEffects().clearEffects());
        ChatUtils.sendMessageWithBrackets(player, "Effects cleared");

        return Command.SINGLE_SUCCESS;
    }

    static int testEffects(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

//        UUID source = UUID.randomUUID();
        UUID source = player.getUUID();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            MKEffectBuilder<?> newInstance;
            newInstance = MKTestEffects.NEW_HEAL.get().builder(source)
                    .state(s -> s.setScalingParameters(3, 1, 1.f))
                    .periodic(20);
//            newInstance = TestFallCountingEffect.INSTANCE.builder(UUID.randomUUID());
//            newInstance = AbilityMagicDamageEffectNew.INSTANCE.builder(player.getUniqueID()).state(s -> {
//                s.base = 1;
//                s.scale = 1;
//            }).periodic(40);
//            newInstance = MKAbilityDamageEffect.INSTANCE.builder(player).state(s -> {
//                s.setDamageType(CoreDamageTypes.FireDamage);
//                s.setScalingParameters(1, 0);
//            }).periodic(20);
            newInstance.timed(200);
//            newInstance.infinite();
            ChatUtils.sendMessage(player, "Adding effect with UUID %s", newInstance.getSourceId());
            playerData.getEffects().addEffect(newInstance);
        });

        return Command.SINGLE_SUCCESS;
    }
}
