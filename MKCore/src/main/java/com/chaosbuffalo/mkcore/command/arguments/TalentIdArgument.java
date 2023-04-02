package com.chaosbuffalo.mkcore.command.arguments;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class TalentIdArgument implements ArgumentType<ResourceLocation> {

    public static TalentIdArgument talentId() {
        return new TalentIdArgument();
    }

    @Override
    public ResourceLocation parse(final StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        Stream<String> all = MKCoreRegistry.TALENTS.getValues()
                .stream()
                .map(MKTalent::getTalentId)
                .map(ResourceLocation::toString);

        return SharedSuggestionProvider.suggest(all, builder);
    }
}
