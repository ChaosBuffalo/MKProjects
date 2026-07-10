package com.chaosbuffalo.mkworkspace.world.gen.workspace.capability;

import com.chaosbuffalo.mkworkspace.init.MKWorkspaceAttachments;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface IMKStructureWorkspaceData extends INBTSerializable<CompoundTag> {
    Optional<MKStructureWorkspace> getWorkspace(UUID id);

    Optional<MKStructureWorkspace> getWorkspaceByAnchor(BlockPos anchor);

    Collection<MKStructureWorkspace> getAllWorkspaces();

    UUID createWorkspace(MKStructureWorkspace workspace);

    void updateWorkspace(MKStructureWorkspace workspace);

    void deleteWorkspace(UUID id);

    static IMKStructureWorkspaceData get(ServerLevel level) {
        return level.getData(MKWorkspaceAttachments.STRUCTURE_WORKSPACE_DATA);
    }

    static IMKStructureWorkspaceData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return get(serverLevel);
        }
        throw new IllegalArgumentException("cannot get workspace data for client level");
    }
}
