package com.chaosbuffalo.mknpc.world.gen.workspace.capability;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
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
        ListTag workspacesTag = new ListTag();
        for (MKStructureWorkspace workspace : workspacesById.values()) {
            workspacesTag.add(workspace.toTag());
        }
        tag.put("workspaces", workspacesTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        workspacesById.clear();
        workspaceByAnchor.clear();
        for (Tag workspaceTag : nbt.getList("workspaces", Tag.TAG_COMPOUND)) {
            MKStructureWorkspace workspace = MKStructureWorkspace.fromTag((CompoundTag) workspaceTag);
            workspacesById.put(workspace.id(), workspace);
            workspaceByAnchor.put(workspace.anchor(), workspace.id());
        }
    }
}
