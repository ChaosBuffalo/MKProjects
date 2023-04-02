package com.chaosbuffalo.mkcore.command.arguments;

import com.chaosbuffalo.mkcore.client.rendering.skeleton.BipedSkeleton;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class BipedBoneArgument implements ArgumentType<String> {

    private static final List<String> bones = new ArrayList<>();

    static {
        bones.add(BipedSkeleton.ROOT_BONE_NAME);
        bones.add(BipedSkeleton.CHEST_BONE_NAME);
        bones.add(BipedSkeleton.NECK_BONE_NAME);
        bones.add(BipedSkeleton.LEFT_ARM_BONE_NAME);
        bones.add(BipedSkeleton.LEFT_LEG_BONE_NAME);
        bones.add(BipedSkeleton.LEFT_HAND_BONE_NAME);
        bones.add(BipedSkeleton.LEFT_FOOT_BONE_NAME);
        bones.add(BipedSkeleton.RIGHT_ARM_BONE_NAME);
        bones.add(BipedSkeleton.RIGHT_LEG_BONE_NAME);
        bones.add(BipedSkeleton.RIGHT_HAND_BONE_NAME);
        bones.add(BipedSkeleton.RIGHT_FOOT_BONE_NAME);
        bones.add(BipedSkeleton.PELVIS_BONE_NAME);
        bones.add(BipedSkeleton.BODY_BONE_NAME);
        bones.add(BipedSkeleton.HEAD_BONE_NAME);
    }

    public static BipedBoneArgument BipedBone() {
        return new BipedBoneArgument();
    }


    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        Stream<String> values = bones.stream();
        return SharedSuggestionProvider.suggest(values, builder);
    }
}
