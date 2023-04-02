package com.chaosbuffalo.mkweapons.command;

import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class LootSlotArgument implements ArgumentType<ResourceLocation> {
    public LootSlotArgument() {
    }

    public static LootSlotArgument definition() {
        return new LootSlotArgument();
    }

    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(LootSlotManager.SLOTS.keySet().stream()
                .map(ResourceLocation::toString), builder);
    }
}

