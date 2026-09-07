package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MKWorkspaceFoundationPolicy(
        MKWorkspaceFoundationMode mode,
        @Nullable ResourceLocation foundationBlock,
        List<ResourceLocation> maskBlocks
) {
    public static final String MODE_TAG = "workspace_foundation_mode";

    public static final Codec<MKWorkspaceFoundationPolicy> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.FOUNDATION_MODE_CODEC.optionalFieldOf("mode", MKWorkspaceFoundationMode.NONE)
                    .forGetter(MKWorkspaceFoundationPolicy::mode),
            ResourceLocation.CODEC.optionalFieldOf("foundationBlock").forGetter(MKWorkspaceFoundationPolicy::foundationBlockOpt),
            ResourceLocation.CODEC.listOf().optionalFieldOf("maskBlocks", List.of())
                    .forGetter(MKWorkspaceFoundationPolicy::maskBlocks)
    ).apply(instance, (mode, foundationState, maskBlocks) ->
            new MKWorkspaceFoundationPolicy(mode, foundationState.orElse(null), maskBlocks)));

    public MKWorkspaceFoundationPolicy {
        mode = mode == null ? MKWorkspaceFoundationMode.NONE : mode;
        maskBlocks = List.copyOf(maskBlocks);
        if (mode != MKWorkspaceFoundationMode.UNIFORM_STATE) {
            foundationBlock = null;
        }
        if (mode != MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS) {
            maskBlocks = List.of();
        }
    }

    public static MKWorkspaceFoundationPolicy none() {
        return new MKWorkspaceFoundationPolicy(MKWorkspaceFoundationMode.NONE, null, List.of());
    }

    public static MKWorkspaceFoundationPolicy uniformState(BlockState state) {
        return uniformBlock(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    public static MKWorkspaceFoundationPolicy uniformBlock(ResourceLocation blockId) {
        return new MKWorkspaceFoundationPolicy(MKWorkspaceFoundationMode.UNIFORM_STATE, blockId, List.of());
    }

    public static MKWorkspaceFoundationPolicy extendBottomBlocks() {
        return new MKWorkspaceFoundationPolicy(MKWorkspaceFoundationMode.EXTEND_BOTTOM_BLOCKS, null, List.of());
    }

    public static MKWorkspaceFoundationPolicy maskedExtendBottomBlocks(List<ResourceLocation> maskBlocks) {
        return new MKWorkspaceFoundationPolicy(MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS, null, maskBlocks);
    }

    public Optional<ResourceLocation> foundationBlockOpt() {
        return Optional.ofNullable(foundationBlock);
    }

    public boolean enabled() {
        return mode != MKWorkspaceFoundationMode.NONE;
    }

    public List<String> validate(String ownerLabel) {
        List<String> errors = new ArrayList<>();
        if (mode == MKWorkspaceFoundationMode.UNIFORM_STATE && foundationBlock == null) {
            errors.add(ownerLabel + " uniform foundation requires a foundation block");
        }
        if (mode == MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS && maskBlocks.isEmpty()) {
            errors.add(ownerLabel + " masked foundation requires at least one mask block");
        }
        return errors;
    }
}
