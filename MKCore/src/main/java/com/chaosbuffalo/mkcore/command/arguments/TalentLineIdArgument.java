package com.chaosbuffalo.mkcore.command.arguments;

import com.chaosbuffalo.mkcore.core.talents.TalentManager;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeDefinition;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceKey;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class TalentLineIdArgument implements ArgumentType<String> {

    public static TalentLineIdArgument talentLine() {
        return new TalentLineIdArgument();
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context,
                                                              final SuggestionsBuilder builder) {
        ResourceKey<TalentTreeDefinition> treeId = TalentTreeIdArgument.get(context, "tree");

        if (context.getSource() instanceof SharedSuggestionProvider provider) {
            TalentTreeDefinition treeDef = TalentManager.getTalentTree(provider.registryAccess(), treeId);
            if (treeDef != null) {
                return SharedSuggestionProvider.suggest(treeDef.getTalentLines().keySet(), builder);
            }
        }

        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    }
}
