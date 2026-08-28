package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

/** Serialized client intent. It is prepared once and never reused as the apply payload. */
public record MKWorkspaceChangeRequest(
        UUID requestId,
        ResourceLocation operationId,
        BlockPos anchor,
        CompoundTag payload
) {
    public MKWorkspaceChangeRequest {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(operationId, "operationId");
        Objects.requireNonNull(anchor, "anchor");
        payload = payload == null ? new CompoundTag() : payload.copy();
    }

    @Override
    public CompoundTag payload() {
        return payload.copy();
    }
}
