package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

public record MKDungeonCategoryRule(
        String category,
        int minMainPathPieces,
        int maxMainPathPieces,
        int maxBranchPiecesBeforeCap,
        boolean hasMainPathContinuations,
        @Nullable ResourceLocation mainPathEndingPool
) {
    public static final Codec<MKDungeonCategoryRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("category").forGetter(MKDungeonCategoryRule::category),
            Codec.intRange(0, 10).optionalFieldOf("min_main_path_pieces", 0)
                    .forGetter(MKDungeonCategoryRule::minMainPathPieces),
            Codec.intRange(0, 10).optionalFieldOf("max_main_path_pieces", 2)
                    .forGetter(MKDungeonCategoryRule::maxMainPathPieces),
            Codec.intRange(0, 10).optionalFieldOf("max_branch_pieces_before_cap", 10)
                    .forGetter(MKDungeonCategoryRule::maxBranchPiecesBeforeCap),
            Codec.BOOL.optionalFieldOf("has_main_path_continuations", true)
                    .forGetter(MKDungeonCategoryRule::hasMainPathContinuations),
            ResourceLocation.CODEC.optionalFieldOf("main_path_ending_pool")
                    .forGetter(MKDungeonCategoryRule::mainPathEndingPoolOpt)
    ).apply(instance, (category, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap,
                       hasMainPathContinuations, mainPathEndingPool) ->
            new MKDungeonCategoryRule(category, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap,
                    hasMainPathContinuations, mainPathEndingPool.orElse(null))));

    public MKDungeonCategoryRule(String category, int minMainPathPieces, int maxMainPathPieces,
                                 boolean hasMainPathContinuations,
                                 @Nullable ResourceLocation mainPathEndingPool) {
        this(category, minMainPathPieces, maxMainPathPieces, 10, hasMainPathContinuations, mainPathEndingPool);
    }

    public boolean hasMainPathEndings() {
        return mainPathEndingPool != null;
    }

    public Optional<ResourceLocation> mainPathEndingPoolOpt() {
        return Optional.ofNullable(mainPathEndingPool);
    }
}
