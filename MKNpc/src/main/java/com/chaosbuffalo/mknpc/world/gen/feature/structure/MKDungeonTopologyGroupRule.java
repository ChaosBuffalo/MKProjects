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
        boolean linksEnabled,
        float linkDensity,
        int maxLinksPerFloor,
        int maxLinksPerRoom,
        int maxLinkLength,
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
            Codec.BOOL.optionalFieldOf("links_enabled", false)
                    .forGetter(MKDungeonTopologyGroupRule::linksEnabled),
            Codec.FLOAT.optionalFieldOf("link_density", 1.0f)
                    .forGetter(MKDungeonTopologyGroupRule::linkDensity),
            Codec.intRange(0, 64).optionalFieldOf("max_links_per_floor", 10)
                    .forGetter(MKDungeonTopologyGroupRule::maxLinksPerFloor),
            Codec.intRange(0, 3).optionalFieldOf("max_links_per_room", 3)
                    .forGetter(MKDungeonTopologyGroupRule::maxLinksPerRoom),
            Codec.intRange(0, 128).optionalFieldOf("max_link_length", 32)
                    .forGetter(MKDungeonTopologyGroupRule::maxLinkLength),
            Codec.LONG.optionalFieldOf("locked_layout_seed")
                    .forGetter(MKDungeonTopologyGroupRule::lockedLayoutSeed),
            Codec.BOOL.optionalFieldOf("has_main_path_continuations", true)
                    .forGetter(MKDungeonTopologyGroupRule::hasMainPathContinuations),
            ResourceLocation.CODEC.optionalFieldOf("main_path_ending_pool")
                    .forGetter(MKDungeonTopologyGroupRule::mainPathEndingPoolOpt)
    ).apply(instance, (topologyGroup, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap, sprawl,
                       linksEnabled, linkDensity, maxLinksPerFloor, maxLinksPerRoom, maxLinkLength,
                       lockedLayoutSeed, hasMainPathContinuations, mainPathEndingPool) ->
            new MKDungeonTopologyGroupRule(topologyGroup, minMainPathPieces, maxMainPathPieces,
                    maxBranchPiecesBeforeCap, sprawl, linksEnabled, linkDensity, maxLinksPerFloor,
                    maxLinksPerRoom, maxLinkLength, lockedLayoutSeed,
                    hasMainPathContinuations, mainPathEndingPool.orElse(null))));

    public MKDungeonTopologyGroupRule {
        sprawl = Math.max(0.0f, Math.min(1.0f, sprawl));
        linkDensity = Math.max(0.0f, Math.min(1.0f, linkDensity));
        maxLinksPerFloor = Math.max(0, Math.min(64, maxLinksPerFloor));
        maxLinksPerRoom = Math.max(0, Math.min(3, maxLinksPerRoom));
        maxLinkLength = Math.max(0, Math.min(128, maxLinkLength));
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
                false, 1.0f, 10, 3, 32, Optional.empty(), hasMainPathContinuations, mainPathEndingPool);
    }

    public MKDungeonTopologyGroupRule(String topologyGroup, int minMainPathPieces, int maxMainPathPieces,
                                      int maxBranchPiecesBeforeCap, float sprawl,
                                      Optional<Long> lockedLayoutSeed,
                                      boolean hasMainPathContinuations,
                                      @Nullable ResourceLocation mainPathEndingPool) {
        this(topologyGroup, minMainPathPieces, maxMainPathPieces, maxBranchPiecesBeforeCap, sprawl,
                false, 1.0f, 10, 3, 32, lockedLayoutSeed, hasMainPathContinuations, mainPathEndingPool);
    }

    public boolean hasMainPathEndings() {
        return mainPathEndingPool != null;
    }

    public Optional<ResourceLocation> mainPathEndingPoolOpt() {
        return Optional.ofNullable(mainPathEndingPool);
    }
}
