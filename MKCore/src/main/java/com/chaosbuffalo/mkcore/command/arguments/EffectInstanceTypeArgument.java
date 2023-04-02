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

public class EffectInstanceTypeArgument implements ArgumentType<ResourceLocation> {

    public static EffectInstanceTypeArgument EffectInstanceType() {
        return new EffectInstanceTypeArgument();
    }

    @Override
    public ResourceLocation parse(final StringReader reader) throws CommandSyntaxException {
        return ResourceLocation.read(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        Stream<String> values = ParticleAnimationManager.EFFECT_INSTANCE_DESERIALIZERS.keySet()
                .stream()
                .map(ResourceLocation::toString);
        return SharedSuggestionProvider.suggest(values, builder);
    }
}
