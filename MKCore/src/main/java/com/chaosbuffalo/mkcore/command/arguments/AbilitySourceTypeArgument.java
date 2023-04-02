package com.chaosbuffalo.mkcore.command.arguments;

import com.chaosbuffalo.mkcore.abilities.AbilitySourceType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbilitySourceTypeArgument implements ArgumentType<AbilitySourceType> {

    public static AbilitySourceTypeArgument abilitySourceType() {
        return new AbilitySourceTypeArgument();
    }

    @Override
    public AbilitySourceType parse(StringReader reader) throws CommandSyntaxException {
        return AbilitySourceType.valueOf(reader.readString());
    }

    private Stream<AbilitySourceType> simpleTypes() {
        return Arrays.stream(AbilitySourceType.values())
                .filter(AbilitySourceType::isSimple)
                .sorted();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(simpleTypes().map(Enum::toString), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return simpleTypes()
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
