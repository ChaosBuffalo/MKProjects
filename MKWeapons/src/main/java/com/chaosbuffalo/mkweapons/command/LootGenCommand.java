package com.chaosbuffalo.mkweapons.command;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkweapons.MKWeaponsRegistry;
import com.chaosbuffalo.mkweapons.items.randomization.LootConstructor;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.CompletableFuture;

public class LootGenCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("gen_loot")
                .then(Commands.argument("loot_tier", LootTierArgument.definition())
                        .then(Commands.argument("loot_slot", LootSlotArgument.definition())
                                .suggests(LootGenCommand::suggestLootSlotsForTier)
                                .then(Commands.argument("difficulty", difficultyArgument())
                                        .executes(LootGenCommand::summon))));
    }

    private static DoubleArgumentType difficultyArgument() {
        return DoubleArgumentType.doubleArg(GameConstants.MIN_DIFFICULTY, GameConstants.MAX_DIFFICULTY);
    }

    static CompletableFuture<Suggestions> suggestLootSlotsForTier(final CommandContext<CommandSourceStack> context,
                                                                  final SuggestionsBuilder builder) {
        ResourceKey<LootTier> tierName = LootTierArgument.get(context, "loot_tier");

        if (context.getSource() instanceof SharedSuggestionProvider provider) {
            LootTier tier = provider.registryAccess().registryOrThrow(MKWeaponsRegistry.LOOT_TIER_REGISTRY_KEY).get(tierName);
            if (tier != null) {
                return SharedSuggestionProvider.suggest(tier.getSlots().stream()
                        .map(LootSlot::getName)
                        .map(ResourceLocation::toString), builder);
            }
        }

        return SharedSuggestionProvider.suggest(LootSlotManager.SLOTS.keySet().stream()
                .map(ResourceLocation::toString), builder);
    }

    static int summon(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ResourceKey<LootTier> tierName = LootTierArgument.get(ctx, "loot_tier");
        ResourceLocation slotName = LootSlotArgument.get(ctx, "loot_slot");
        double difficulty = DoubleArgumentType.getDouble(ctx, "difficulty");

        if (ctx.getSource() instanceof SharedSuggestionProvider provider) {
            LootTier tier = provider.registryAccess().registryOrThrow(MKWeaponsRegistry.LOOT_TIER_REGISTRY_KEY).get(tierName);
            if (tier == null) {
                player.sendSystemMessage(Component.literal("Loot Tier Not Found."));
                return Command.SINGLE_SUCCESS;
            }

            LootSlot slot = LootSlotManager.getSlotFromName(slotName);
            if (slot == null) {
                player.sendSystemMessage(Component.literal("Loot Slot Not Found."));
                return Command.SINGLE_SUCCESS;
            }

            LootConstructor constructor = tier.generateConstructorForSlot(player.getRandom(), slot);
            if (constructor != null) {
                ItemStack stack = constructor.constructItem(player.getRandom(), difficulty);
                if (!stack.isEmpty()) {
                    boolean wasAdded = player.addItem(stack);
                    if (!wasAdded) {
                        player.drop(stack, false);
                    }
                }
            } else {
                player.sendSystemMessage(Component.literal("No LootConstructor generated."));
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}