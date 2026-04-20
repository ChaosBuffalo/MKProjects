package com.chaosbuffalo.mkcore.command;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.AbilitySource;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.command.arguments.AbilityIdArgument;
import com.chaosbuffalo.mkcore.command.arguments.PlayersArgument;
import com.chaosbuffalo.mkcore.core.AbilityDisplayEntry;
import com.chaosbuffalo.mkcore.core.AbilityType;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.PlayerAbilityKnowledge;
import com.chaosbuffalo.mkcore.core.player.PlayerKnownAbility;
import com.chaosbuffalo.mkcore.utils.ChatUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AbilityCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("ability")
                .then(Commands.argument("player", PlayersArgument.player())
                        .then(Commands.literal("learn")
                                .requires(MKCommand::isGM)
                                .then(Commands.argument("ability", AbilityIdArgument.ability())
                                        .suggests(AbilityCommand::suggestUnknownAbilities)
                                        .executes(AbilityCommand::learnAbility)))
                        .then(Commands.literal("unlearn")
                                .requires(MKCommand::isGM)
                                .then(Commands.argument("ability", AbilityIdArgument.ability())
                                        .suggests(AbilityCommand::suggestForgettableAbilities)
                                        .executes(AbilityCommand::unlearnAbility)))
                        .then(Commands.literal("learn_all")
                                .requires(MKCommand::isGM)
                                .executes(AbilityCommand::learnAllAbilities))
                        .then(Commands.literal("unlearn_all")
                                .requires(MKCommand::isGM)
                                .executes(AbilityCommand::unlearnAllAbilities))
                        .then(Commands.literal("list")
                                .executes(AbilityCommand::listAbilities))
                        .then(Commands.literal("pool")
                                .executes(AbilityCommand::showPool))
                )
                ;
    }

    public static CompletableFuture<Suggestions> suggestForgettableAbilities(final CommandContext<CommandSourceStack> context,
                                                                             final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        return SharedSuggestionProvider.suggest(playerData.getAbilities()
                .getKnownStream()
                .filter(info -> info.getSources().stream().anyMatch(s -> s.getSourceType().isSimple()))
                .map(PlayerKnownAbility::getId)
                .map(ResourceLocation::toString), builder);
    }

    static CompletableFuture<Suggestions> suggestUnknownAbilities(final CommandContext<CommandSourceStack> context,
                                                                  final SuggestionsBuilder builder) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");

        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        return SharedSuggestionProvider.suggest(
                getLearnableAbilityIds()
                        .filter(abilityId -> !playerData.getAbilities().knowsAbility(abilityId))
                        .map(ResourceLocation::toString), builder);
    }

    static int learnAbility(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        ResourceLocation abilityId = context.getArgument("ability", ResourceLocation.class);

        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        boolean learned = false;
        MKAbility ability = MKCoreRegistry.getAbility(abilityId);
        if (ability != null) {
            learned = playerData.getAbilities().learnAbility(ability, AbilitySource.ADMIN);
        } else {
            learned = playerData.getAbilities().learnAbilityDefinition(abilityId, AbilitySource.ADMIN);
        }

        if (learned) {
            Component message = Component.literal("Player '")
                    .append(player.getName())
                    .append("' learned ")
                    .append(formatAbilityDisplay(abilityId));
            ChatUtils.sendMessage(commandPlayer, message);
        }

        return Command.SINGLE_SUCCESS;
    }

    static int learnAllAbilities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");

        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        MKCoreRegistry.ABILITIES.forEach(ability -> playerData.getAbilities().learnAbility(ability, AbilitySource.ADMIN));
        getLearnableDefinitionIds().forEach(abilityId ->
                playerData.getAbilities().learnAbilityDefinition(abilityId, AbilitySource.ADMIN));

        return Command.SINGLE_SUCCESS;
    }

    static int unlearnAllAbilities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");

        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        List<ResourceLocation> allAbilities = playerData.getAbilities().getKnownAbilityIds().toList();
        allAbilities.forEach(abilityId -> playerData.getAbilities().unlearnAbility(abilityId, AbilitySource.ADMIN));

        return Command.SINGLE_SUCCESS;
    }

    static int unlearnAbility(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");
        ResourceLocation abilityId = context.getArgument("ability", ResourceLocation.class);

        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        PlayerKnownAbility info = playerData.getAbilities().getKnownAbility(abilityId);
        if (info == null) {
            Component message = Component.translatableWithFallback("mkcore.command.ability.unlearn.not_known",
                    "Player '%s' doesn't know ability %s",
                    player.getName(),
                    Component.translationArg(abilityId));
            ChatUtils.sendMessage(commandPlayer, message);
            return Command.SINGLE_SUCCESS;
        }
        List<AbilitySource> sources = new ArrayList<>(info.getSources());
        sources.forEach(s -> {
            if (s.getSourceType().isSimple()) {
                playerData.getAbilities().unlearnAbility(abilityId, s);
            }
        });

        return Command.SINGLE_SUCCESS;
    }

    static int listAbilities(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");

        if (commandPlayer != player && !context.getSource().hasPermission(Commands.LEVEL_GAMEMASTERS)) {
            ChatUtils.sendMessage(commandPlayer, "Error - Can only query yourself");
            return Command.SINGLE_SUCCESS;
        }

        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        Collection<PlayerKnownAbility> abilities = playerData.getAbilities().getKnownAbilities();
        if (!abilities.isEmpty()) {
            ChatUtils.sendMessageWithBrackets(commandPlayer, Component.literal("Known Abilities - ").append(player.getName()));
            abilities.forEach(info -> {
                ChatUtils.sendMessageWithBrackets(commandPlayer, formatAbilityDisplay(info.getId()));
                info.getSources().forEach(s -> ChatUtils.sendMessage(commandPlayer, "- %s", s.encode()));
            });
        } else {
            ChatUtils.sendMessageWithBrackets(commandPlayer, "No known abilities");
        }

        return Command.SINGLE_SUCCESS;
    }

    static int showPool(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer commandPlayer = context.getSource().getPlayerOrException();
        ServerPlayer player = PlayersArgument.getPlayer(context, "player");

        if (commandPlayer != player && !context.getSource().hasPermission(Commands.LEVEL_GAMEMASTERS)) {
            ChatUtils.sendMessage(commandPlayer, "Error - Can only query yourself");
            return Command.SINGLE_SUCCESS;
        }

        MKPlayerData playerData = MKCore.getPlayerOrThrow(player);
        PlayerAbilityKnowledge abilityKnowledge = playerData.getAbilities();
        int currentSize = abilityKnowledge.getCurrentPoolCount();
        int maxSize = abilityKnowledge.getAbilityPoolSize();
        ChatUtils.sendMessageWithBrackets(commandPlayer, Component.literal("Ability Pool - ").append(player.getName()));
        ChatUtils.sendMessageWithBrackets(commandPlayer, "Pool Size: %d/%d", currentSize, maxSize);
        abilityKnowledge.getPoolAbilityIds().forEach(abilityId -> {
            ChatUtils.sendMessageWithBrackets(commandPlayer,
                    Component.literal("Pool Ability: ").append(formatAbilityDisplay(abilityId)));
        });
        return Command.SINGLE_SUCCESS;
    }

    static Component formatAbilityDisplay(ResourceLocation abilityId) {
        AbilityDisplayEntry display = AbilityDisplayEntry.resolve(abilityId);
        Component result = display.displayName().copy();
        AbilityType type = display.abilityType();
        if (type != null) {
            result = result.copy().append(Component.literal(" [" + type + "]"));
        }
        if (display.definitionBacked()) {
            result = result.copy().append(Component.literal(" {abilities2}"));
        }
        return result.copy().append(Component.literal(" (" + abilityId + ")"));
    }

    private static Stream<ResourceLocation> getLearnableAbilityIds() {
        return Stream.concat(
                MKCoreRegistry.ABILITIES.keySet().stream(),
                getLearnableDefinitionIds()
        );
    }

    private static Stream<ResourceLocation> getLearnableDefinitionIds() {
        return MKCore.getAbilityDefinitionService().getDefinitionIds().stream()
                .filter(AbilityCommand::isLearnableDefinition);
    }

    private static boolean isLearnableDefinition(ResourceLocation abilityId) {
        var definition = MKCore.getAbilityDefinitionService().getDefinition(abilityId);
        if (definition == null) {
            return false;
        }
        ResourceLocation slotFamily = definition.slotFamily();
        return slotFamily.equals(com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys.SLOT_FAMILY_BASIC)
                || slotFamily.equals(com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys.SLOT_FAMILY_PASSIVE)
                || slotFamily.equals(com.chaosbuffalo.mkcore.abilities2.datagen.AbilityDatagenKeys.SLOT_FAMILY_ULTIMATE);
    }
}
