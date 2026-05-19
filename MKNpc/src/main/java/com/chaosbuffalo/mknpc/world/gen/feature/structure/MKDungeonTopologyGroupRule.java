package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

public record MKDungeonTopologyGroupRule(
        String topologyGroup,
        int minMainPathPieces,
        int maxMainPathPieces,
        int maxBranchPiecesBeforeCap,
        boolean hasMainPathContinuations,
        @Nullable ResourceLocation mainPathEndingPool
) {
    public static final Codec<MKDungeonTopologyGroupRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("topology_group").forGetter(MKDungeonTopologyGroupRule::topologyGroup),
            Codec.intRange(0, 10).optionalFieldOf("min_main_path_pieces", 0)
                    .forGetter(MKDungeonTopologyGroupRule::minMainPathPieces),
            Codec.intRange(0, 10).optionalFieldOf("max_main_path_pieces", 2)
                    .forGetter(MKDungeonTopologyGroupRule::maxMainPathPieces),
            Codec.intRange(0, 10).optionalFieldOf("max_branch_pieces_before_cap", 10)
                    .forGetter(MKDungeonTopologyGroupRule::maxBranchPiecesBeforeCap),
            Codec.BOOL.optionalFieldOf("has_main_path_continuations", true)
                    .forGetter(MKDungeonTopologyGroupRule::hasMainPathContinuations),
            ResourceLocation.CODEC.optionalFieldOf("main_path_ending_pool")
                    .forGetter(MKDungeonTopologyGroupRule::mainPathEndingPoolOpt)
    ).apply(instance, (topologyGroup, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap,
                       hasMainPathContinuations, mainPathEndingPool) ->
            new MKDungeonTopologyGroupRule(topologyGroup, minMainPathPieces, maxMainPathPieces,
                    maxBranchPiecesBeforeCap,
                    hasMainPathContinuations, mainPathEndingPool.orElse(null))));

    public MKDungeonTopologyGroupRule(String topologyGroup, int minMainPathPieces, int maxMainPathPieces,
                                      boolean hasMainPathContinuations,
                                      @Nullable ResourceLocation mainPathEndingPool) {
        this(topologyGroup, minMainPathPieces, maxMainPathPieces, 10, hasMainPathContinuations, mainPathEndingPool);
    }

    public boolean hasMainPathEndings() {
        return mainPathEndingPool != null;
    }

    public Optional<ResourceLocation> mainPathEndingPoolOpt() {
        return Optional.ofNullable(mainPathEndingPool);
    }
}
