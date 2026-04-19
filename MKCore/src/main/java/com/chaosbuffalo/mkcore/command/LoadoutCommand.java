package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.command.arguments.AbilityIdArgument;
import com.chaosbuffalo.mkcore.core.player.AbilityGroup;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityKnowledge;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class LoadoutCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("loadout")
                .then(Commands.literal("show")
                        .then(Commands.argument("group", AbilityGroupArgument.abilityGroup())
                                .executes(LoadoutCommand::showActionBar)))
                .then(Commands.literal("set")
                        .then(Commands.argument("group", AbilityGroupArgument.abilityGroup())
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.ACTION_BAR_SIZE))
                                        .then(Commands.argument("abilityId", AbilityIdArgument.ability())
                                                .suggests(LoadoutCommand::suggestKnownAbilities)
                                                .executes(LoadoutCommand::setActionBar)))))
                .then(Commands.literal("clear")
                        .then(Commands.argument("group", AbilityGroupArgument.abilityGroup())
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, GameConstants.ACTION_BAR_SIZE))
                                        .executes(LoadoutCommand::clearActionBar))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("group", AbilityGroupArgument.abilityGroup())
                                .executes(LoadoutCommand::resetActionBar)))
                .then(Commands.literal("add")
                        .then(Commands.argument("group", AbilityGroupArgument.abilityGroup())
                                .then(Commands.argument("abilityId", AbilityIdArgument.ability())
                                        .suggests(LoadoutCommand::suggestKnownAbilities)
                                        .executes(LoadoutCommand::addActionBar))))
                .then(Commands.literal("slots")
                        .then(Commands.argument("group", AbilityGroupArgument.abilityGroup())
                                .then(Commands.argument("count", IntegerArgumentType.integer())
                                        .executes(LoadoutCommand::setSlots))))
                ;
    }

    static int setSlots(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        AbilityGroupId group = ctx.getArgument("group", AbilityGroupId.class);
        int count = IntegerArgumentType.getInteger(ctx, "count");

        var playerData = MKCore.getPlayerOrThrow(player);
        AbilityGroup abilityGroup = playerData.getLoadout().getAbilityGroup(group);
        if (!abilityGroup.setBonusSlots(count)) {
            MKCore.LOGGER.error("Failed to update slot count for {}", group);
        }

        return Command.SINGLE_SUCCESS;
    }

    static int setActionBar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        AbilityGroupId group = ctx.getArgument("group", AbilityGroupId.class);
        int slot = IntegerArgumentType.getInteger(ctx, "slot");
        ResourceLocation abilityId = ctx.getArgument("abilityId", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerAbilityKnowledge abilityKnowledge = playerData.getAbilities();
            if (abilityKnowledge.knowsAbility(abilityId)
                    || canSlotAbilityDefinition(ctx.getSource(), group, abilityId)) {
                playerData.getLoadout().getAbilityGroup(group).setSlot(slot, abilityId);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int addActionBar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        AbilityGroupId group = ctx.getArgument("group", AbilityGroupId.class);
        ResourceLocation abilityId = ctx.getArgument("abilityId", ResourceLocation.class);

        MKCore.getPlayer(player).ifPresent(playerData -> {
            PlayerAbilityKnowledge abilityKnowledge = playerData.getAbilities();
            if (abilityKnowledge.knowsAbility(abilityId)
                    || canSlotAbilityDefinition(ctx.getSource(), group, abilityId)) {
                if (!playerData.getLoadout().getAbilityGroup(group).tryEquip(abilityId)) {
                    ChatUtils.sendMessage(player, "No room for ability");
                }
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int clearActionBar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        AbilityGroupId group = ctx.getArgument("group", AbilityGroupId.class);
        int slot = IntegerArgumentType.getInteger(ctx, "slot");

        MKCore.getPlayer(player).ifPresent(playerData ->
                playerData.getLoadout().getAbilityGroup(group).clearSlot(slot));

        return Command.SINGLE_SUCCESS;
    }

    static int resetActionBar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        AbilityGroupId group = ctx.getArgument("group", AbilityGroupId.class);
        MKCore.getPlayer(player).ifPresent(playerData ->
                playerData.getLoadout().getAbilityGroup(group).resetSlots());

        return Command.SINGLE_SUCCESS;
    }

    static int showActionBar(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        AbilityGroupId group = ctx.getArgument("group", AbilityGroupId.class);
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        MKCore.getPlayer(player).ifPresent(playerData -> {
            AbilityGroup container = playerData.getLoadout().getAbilityGroup(group);
            int current = container.getCurrentSlotCount();
            int max = container.getMaximumSlotCount();
            ChatUtils.sendMessageWithBrackets(player, "%s Action Bar (%d/%d slots)", group, current, max);
            for (int i = 0; i < current; i++) {
                ChatUtils.sendMessage(player, "%d: %s", i, container.getSlot(i));
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    public static CompletableFuture<Suggestions> suggestKnownAbilities(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        AbilityGroupId group = context.getArgument("group", AbilityGroupId.class);
        ServerPlayer player = context.getSource().getPlayerOrException();
        Stream<String> loadoutDefinitions = context.getSource().hasPermission(Commands.LEVEL_GAMEMASTERS)
                ? MKCore.getAbilityDefinitionService().getDefinitionIds().stream()
                .filter(abilityId -> MKCore.getAbilityRuntimeService().isLoadoutDefinition(group, abilityId))
                .map(ResourceLocation::toString)
                : Stream.empty();

        Stream<String> playerKnownAbilities = MKCore.getPlayer(player)
                .map(playerData -> Stream.concat(
                                playerData.getAbilities()
                                        .getAbilityInfoStream()
                                        .filter(info -> group.fitsAbilityType(info.getAbilityType()))
                                        .map(MKAbilityInfo::getId),
                                playerData.getAbilities()
                                        .getKnownDefinitionIds()
                                        .filter(abilityId -> MKCore.getAbilityRuntimeService().isLoadoutDefinition(group, abilityId))
                        )
                        .map(ResourceLocation::toString))
                .orElse(Stream.empty());

        return SharedSuggestionProvider.suggest(Stream.concat(playerKnownAbilities, loadoutDefinitions).distinct(),
                builder);
    }

    private static boolean canSlotAbilityDefinition(CommandSourceStack source, AbilityGroupId group, ResourceLocation abilityId) {
        return source.hasPermission(Commands.LEVEL_GAMEMASTERS)
                && MKCore.getAbilityRuntimeService().isLoadoutDefinition(group, abilityId);
    }

    public static class AbilityGroupArgument implements ArgumentType<AbilityGroupId> {

        public static AbilityGroupArgument abilityGroup() {
            return new AbilityGroupArgument();
        }

        @Override
        public AbilityGroupId parse(final StringReader reader) throws CommandSyntaxException {
            return AbilityGroupId.valueOf(reader.readString());
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
            return SharedSuggestionProvider.suggest(Arrays.stream(AbilityGroupId.values()).map(Enum::toString), builder);
        }
    }
}
