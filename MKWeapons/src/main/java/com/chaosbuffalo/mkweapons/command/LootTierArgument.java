package com.chaosbuffalo.mkweapons.command;

import com.chaosbuffalo.mkweapons.MKWeaponsRegistry;
import com.chaosbuffalo.mkweapons.items.randomization.LootTier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class LootTierArgument implements ArgumentType<ResourceLocation> {
    public LootTierArgument() {
    }

    public static LootTierArgument definition() {
        return new LootTierArgument();
    }

    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider provider
                ? provider.suggestRegistryElements(MKWeaponsRegistry.LOOT_TIER_REGISTRY_KEY, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, builder, context)
                : builder.buildFuture();
    }

    public static <S> ResourceKey<LootTier> get(CommandContext<S> context, String name) {
        ResourceLocation treeId = context.getArgument(name, ResourceLocation.class);
        return ResourceKey.create(MKWeaponsRegistry.LOOT_TIER_REGISTRY_KEY, treeId);
    }
}
