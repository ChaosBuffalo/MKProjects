package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MKWorkspacePieceDefinition {
    private final UUID pieceId;
    private final UUID workspaceId;
    private final String pieceName;
    private final MKWorkspacePieceRole role;
    private final int variantIndex;
    private final MKWorkspaceDimensions effectiveDimensions;
    private final int shellMargin;
    private final List<MKWorkspaceConnectorDefinition> connectors;
    private final BlockPos worldOrigin;
    private final BoundingBox exportBounds;
    private final BoundingBox previewBounds;
    private final BlockPos structureBlockPos;
    private final BlockPos signPos;
    private final List<BlockPos> markerPositions;
    private final List<BlockPos> generatedStairPositions;
    private final Map<String, String> tags;

    public MKWorkspacePieceDefinition(UUID pieceId, UUID workspaceId, String pieceName, MKWorkspacePieceRole role,
                                      int variantIndex, MKWorkspaceDimensions effectiveDimensions, int shellMargin,
                                      List<MKWorkspaceConnectorDefinition> connectors, BlockPos worldOrigin,
                                      BoundingBox exportBounds, BoundingBox previewBounds, BlockPos structureBlockPos,
                                      BlockPos signPos, List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions, Map<String, String> tags) {
        this.pieceId = pieceId;
        this.workspaceId = workspaceId;
        this.pieceName = pieceName;
        this.role = role;
        this.variantIndex = variantIndex;
        this.effectiveDimensions = effectiveDimensions;
        this.shellMargin = shellMargin;
        this.connectors = List.copyOf(connectors);
        this.worldOrigin = worldOrigin;
        this.exportBounds = exportBounds;
        this.previewBounds = previewBounds;
        this.structureBlockPos = structureBlockPos;
        this.signPos = signPos;
        this.markerPositions = List.copyOf(markerPositions);
        this.generatedStairPositions = List.copyOf(generatedStairPositions);
        this.tags = Map.copyOf(tags);
    }

    public static MKWorkspacePieceDefinition fromTag(CompoundTag tag) {
        List<MKWorkspaceConnectorDefinition> connectors = new ArrayList<>();
        for (Tag entry : tag.getList("connectors", Tag.TAG_COMPOUND)) {
            connectors.add(MKWorkspaceConnectorDefinition.fromTag((CompoundTag) entry));
        }
        List<BlockPos> markerPositions = new ArrayList<>();
        for (Tag entry : tag.getList("markerPositions", Tag.TAG_COMPOUND)) {
            markerPositions.add(MKWorkspaceNbtUtil.blockPosFromTag((CompoundTag) entry));
        }
        List<BlockPos> generatedStairPositions = new ArrayList<>();
        for (Tag entry : tag.getList("generatedStairPositions", Tag.TAG_COMPOUND)) {
            generatedStairPositions.add(MKWorkspaceNbtUtil.blockPosFromTag((CompoundTag) entry));
        }
        Map<String, String> tags = new HashMap<>();
        CompoundTag tagsTag = tag.getCompound("tags");
        for (String key : tagsTag.getAllKeys()) {
            tags.put(key, tagsTag.getString(key));
        }
        return new MKWorkspacePieceDefinition(
                tag.getUUID("pieceId"),
                tag.getUUID("workspaceId"),
                tag.getString("pieceName"),
                MKWorkspacePieceRole.fromSerializedName(tag.getString("role")),
                tag.getInt("variantIndex"),
                MKWorkspaceDimensions.fromTag(tag.getCompound("effectiveDimensions")),
                tag.getInt("shellMargin"),
                connectors,
                MKWorkspaceNbtUtil.blockPosFromTag(tag.getCompound("worldOrigin")),
                MKWorkspaceNbtUtil.boundingBoxFromTag(tag.getCompound("exportBounds")),
                MKWorkspaceNbtUtil.boundingBoxFromTag(tag.getCompound("previewBounds")),
                MKWorkspaceNbtUtil.blockPosFromTag(tag.getCompound("structureBlockPos")),
                MKWorkspaceNbtUtil.blockPosFromTag(tag.getCompound("signPos")),
                markerPositions,
                generatedStairPositions,
                tags
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("pieceId", pieceId);
        tag.putUUID("workspaceId", workspaceId);
        tag.putString("pieceName", pieceName);
        tag.putString("role", role.getSerializedName());
        tag.putInt("variantIndex", variantIndex);
        tag.put("effectiveDimensions", effectiveDimensions.toTag());
        tag.putInt("shellMargin", shellMargin);
        ListTag connectorTags = new ListTag();
        for (MKWorkspaceConnectorDefinition connector : connectors) {
            connectorTags.add(connector.toTag());
        }
        tag.put("connectors", connectorTags);
        tag.put("worldOrigin", MKWorkspaceNbtUtil.blockPosToTag(worldOrigin));
        tag.put("exportBounds", MKWorkspaceNbtUtil.boundingBoxToTag(exportBounds));
        tag.put("previewBounds", MKWorkspaceNbtUtil.boundingBoxToTag(previewBounds));
        tag.put("structureBlockPos", MKWorkspaceNbtUtil.blockPosToTag(structureBlockPos));
        tag.put("signPos", MKWorkspaceNbtUtil.blockPosToTag(signPos));
        ListTag markersTag = new ListTag();
        for (BlockPos markerPos : markerPositions) {
            markersTag.add(MKWorkspaceNbtUtil.blockPosToTag(markerPos));
        }
        tag.put("markerPositions", markersTag);
        ListTag generatedStairsTag = new ListTag();
        for (BlockPos stairPos : generatedStairPositions) {
            generatedStairsTag.add(MKWorkspaceNbtUtil.blockPosToTag(stairPos));
        }
        tag.put("generatedStairPositions", generatedStairsTag);
        CompoundTag tagsTag = new CompoundTag();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            tagsTag.putString(entry.getKey(), entry.getValue());
        }
        tag.put("tags", tagsTag);
        return tag;
    }

    public UUID pieceId() {
        return pieceId;
    }

    public UUID workspaceId() {
        return workspaceId;
    }

    public String pieceName() {
        return pieceName;
    }

    public MKWorkspacePieceRole role() {
        return role;
    }

    public int variantIndex() {
        return variantIndex;
    }

    public MKWorkspaceDimensions effectiveDimensions() {
        return effectiveDimensions;
    }

    public int shellMargin() {
        return shellMargin;
    }

    public List<MKWorkspaceConnectorDefinition> connectors() {
        return connectors;
    }

    public BlockPos worldOrigin() {
        return worldOrigin;
    }

    public BoundingBox exportBounds() {
        return exportBounds;
    }

    public BoundingBox previewBounds() {
        return previewBounds;
    }

    public BlockPos structureBlockPos() {
        return structureBlockPos;
    }

    public BlockPos signPos() {
        return signPos;
    }

    public List<BlockPos> markerPositions() {
        return markerPositions;
    }

    public List<BlockPos> generatedStairPositions() {
        return generatedStairPositions;
    }

    public Map<String, String> tags() {
        return tags;
    }

    public MKWorkspacePieceDefinition withGeneratedStairs(List<BlockPos> newGeneratedStairPositions, Map<String, String> newTags) {
        return new MKWorkspacePieceDefinition(pieceId, workspaceId, pieceName, role, variantIndex, effectiveDimensions,
                shellMargin, connectors, worldOrigin, exportBounds, previewBounds, structureBlockPos, signPos,
                markerPositions, newGeneratedStairPositions, newTags);
    }
}
