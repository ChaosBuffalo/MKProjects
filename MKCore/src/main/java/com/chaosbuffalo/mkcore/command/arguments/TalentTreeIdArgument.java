package com.chaosbuffalo.mkcore.command.arguments;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
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

public class TalentTreeIdArgument implements ArgumentType<ResourceLocation> {

    public static TalentTreeIdArgument talentTreeId() {
        return new TalentTreeIdArgument();
    }

    @Override
    public ResourceLocation parse(final StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context,
                                                              final SuggestionsBuilder builder) {

        return context.getSource() instanceof SharedSuggestionProvider provider
                ? provider.suggestRegistryElements(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, builder, context)
                : builder.buildFuture();
    }

    public static <S> ResourceKey<TalentTreeDefinition> get(CommandContext<S> context, String name) {
        ResourceLocation treeId = context.getArgument(name, ResourceLocation.class);
        return ResourceKey.create(MKCoreRegistry.TALENT_TREE_REGISTRY_KEY, treeId);
    }
}
