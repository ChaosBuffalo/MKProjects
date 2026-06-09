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
        float sprawl,
        Optional<Long> lockedLayoutSeed,
        boolean hasMainPathContinuations,
        @Nullable ResourceLocation mainPathEndingPool
) {
    public static final Codec<MKDungeonTopologyGroupRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("topology_group").forGetter(MKDungeonTopologyGroupRule::topologyGroup),
            Codec.intRange(0, 10).optionalFieldOf("min_main_path_pieces", 0)
                    .forGetter(MKDungeonTopologyGroupRule::minMainPathPieces),
            Codec.intRange(0, 10).optionalFieldOf("max_main_path_pieces", 2)
                    .forGetter(MKDungeonTopologyGroupRule::maxMainPathPieces),
            Codec.intRange(0, 10).optionalFieldOf("max_branch_pieces_before_cap", 0)
                    .forGetter(MKDungeonTopologyGroupRule::maxBranchPiecesBeforeCap),
            Codec.FLOAT.optionalFieldOf("sprawl", 0.5f)
                    .forGetter(MKDungeonTopologyGroupRule::sprawl),
            Codec.LONG.optionalFieldOf("locked_layout_seed")
                    .forGetter(MKDungeonTopologyGroupRule::lockedLayoutSeed),
            Codec.BOOL.optionalFieldOf("has_main_path_continuations", true)
                    .forGetter(MKDungeonTopologyGroupRule::hasMainPathContinuations),
            ResourceLocation.CODEC.optionalFieldOf("main_path_ending_pool")
                    .forGetter(MKDungeonTopologyGroupRule::mainPathEndingPoolOpt)
    ).apply(instance, (topologyGroup, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap, sprawl,
                       lockedLayoutSeed, hasMainPathContinuations, mainPathEndingPool) ->
            new MKDungeonTopologyGroupRule(topologyGroup, minMainPathPieces, maxMainPathPieces,
                    maxBranchPiecesBeforeCap, sprawl, lockedLayoutSeed,
                    hasMainPathContinuations, mainPathEndingPool.orElse(null))));

    public MKDungeonTopologyGroupRule {
        sprawl = Math.max(0.0f, Math.min(1.0f, sprawl));
        lockedLayoutSeed = lockedLayoutSeed == null ? Optional.empty() : lockedLayoutSeed;
    }

    public MKDungeonTopologyGroupRule(String topologyGroup, int minMainPathPieces, int maxMainPathPieces,
                                      boolean hasMainPathContinuations,
                                      @Nullable ResourceLocation mainPathEndingPool) {
        this(topologyGroup, minMainPathPieces, maxMainPathPieces, 10, 0.5f, hasMainPathContinuations,
                mainPathEndingPool);
    }

    public MKDungeonTopologyGroupRule(String topologyGroup, int minMainPathPieces, int maxMainPathPieces,
                                      int maxBranchPiecesBeforeCap, float sprawl,
                                      boolean hasMainPathContinuations,
                                      @Nullable ResourceLocation mainPathEndingPool) {
        this(topologyGroup, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap, sprawl,
                Optional.empty(), hasMainPathContinuations, mainPathEndingPool);
    }

    public boolean hasMainPathEndings() {
        return mainPathEndingPool != null;
    }

    public Optional<ResourceLocation> mainPathEndingPoolOpt() {
        return Optional.ofNullable(mainPathEndingPool);
    }
}
