package com.chaosbuffalo.mkcore.command.arguments;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
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

public class ParticleAnimationArgument implements ArgumentType<ResourceLocation> {

    public static ParticleAnimationArgument ParticleAnimation() {
        return new ParticleAnimationArgument();
    }

    @Override
    public ResourceLocation parse(final StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        Stream<String> values = ParticleAnimationManager.ANIMATIONS.keySet()
                .stream()
                .map(ResourceLocation::toString)
                .sorted();
        return SharedSuggestionProvider.suggest(values, builder);
    }
}