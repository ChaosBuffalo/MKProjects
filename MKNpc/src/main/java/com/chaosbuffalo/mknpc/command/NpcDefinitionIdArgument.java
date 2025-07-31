package com.chaosbuffalo.mknpc.command;

import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcRegistries;
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

public class NpcDefinitionIdArgument implements ArgumentType<ResourceLocation> {

    public NpcDefinitionIdArgument() {
    }

    public static NpcDefinitionIdArgument definition() {
        return new NpcDefinitionIdArgument();
    }

    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return context.getSource() instanceof SharedSuggestionProvider provider ?
                provider.suggestRegistryElements(NpcRegistries.NPC_DEFINITIONS, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, builder, context) :
                Suggestions.empty();
    }

    public static <S> ResourceKey<NpcDefinition> get(CommandContext<S> context, String name) {
        ResourceLocation treeId = context.getArgument(name, ResourceLocation.class);
        return ResourceKey.create(NpcRegistries.NPC_DEFINITIONS, treeId);
    }
}
