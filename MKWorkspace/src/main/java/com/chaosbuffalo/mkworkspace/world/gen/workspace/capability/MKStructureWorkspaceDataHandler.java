package com.chaosbuffalo.mkworkspace.world.gen.workspace.capability;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MKStructureWorkspaceDataHandler implements IMKStructureWorkspaceData {
    private final Level level;
    private final Map<UUID, MKStructureWorkspace> workspacesById = new HashMap<>();
    private final Map<BlockPos, UUID> workspaceByAnchor = new HashMap<>();

    public MKStructureWorkspaceDataHandler(Level level) {
        this.level = level;
    }

    @Override
    public Optional<MKStructureWorkspace> getWorkspace(UUID id) {
        return Optional.ofNullable(workspacesById.get(id));
    }

    @Override
    public Optional<MKStructureWorkspace> getWorkspaceByAnchor(BlockPos anchor) {
        UUID id = workspaceByAnchor.get(anchor);
        if (id == null) {
            return Optional.empty();
        }
        return getWorkspace(id);
    }

    @Override
    public Collection<MKStructureWorkspace> getAllWorkspaces() {
        return workspacesById.values();
    }

    @Override
    public UUID createWorkspace(MKStructureWorkspace workspace) {
        workspacesById.put(workspace.id(), workspace);
        workspaceByAnchor.put(workspace.anchor(), workspace.id());
        return workspace.id();
    }

    @Override
    public void updateWorkspace(MKStructureWorkspace workspace) {
        workspacesById.put(workspace.id(), workspace);
        workspaceByAnchor.put(workspace.anchor(), workspace.id());
    }

    @Override
    public void deleteWorkspace(UUID id) {
        MKStructureWorkspace removed = workspacesById.remove(id);
        if (removed != null) {
            workspaceByAnchor.remove(removed.anchor());
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        Tag workspacesTag = MKStructureWorkspace.LIST_CODEC.encodeStart(NbtOps.INSTANCE, List.copyOf(workspacesById.values()))
                .resultOrPartial(error -> com.chaosbuffalo.mknpc.MKNpc.LOGGER.error(
                        "Failed to encode workspace capability data: {}", error))
                .orElseThrow(() -> new IllegalStateException("Failed to encode workspace capability data"));
        tag.put("workspaces", workspacesTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        workspacesById.clear();
        workspaceByAnchor.clear();
        Tag workspacesTag = nbt.get("workspaces");
        if (workspacesTag == null) {
            return;
        }
        List<MKStructureWorkspace> workspaces = MKStructureWorkspace.LIST_CODEC.parse(NbtOps.INSTANCE, workspacesTag)
                .resultOrPartial(error -> com.chaosbuffalo.mknpc.MKNpc.LOGGER.error(
                        "Failed to parse workspace capability data: {}", error))
                .orElseThrow(() -> new IllegalStateException("Failed to parse workspace capability data"));
        for (MKStructureWorkspace workspace : workspaces) {
            workspacesById.put(workspace.id(), workspace);
            workspaceByAnchor.put(workspace.anchor(), workspace.id());
        }
    }
}
