package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MKStructureWorkspace {
    private final UUID id;
    private final BlockPos anchor;
    private final String namespace;
    private final String structureName;
    private final MKStructureFamilyType familyType;
    private final MKWorkspaceDimensions dimensions;
    private final MKWorkspaceMaterialPalette palette;
    private final MKWorkspaceStairAuthoringConfig stairConfig;
    private final MKVerticalAccessPlacement verticalAccessPlacement;
    private final int shellMargin;
    private final int exteriorAirMargin;
    private final int previewMargin;
    private final long createdAt;
    private final long updatedAt;
    private final List<MKWorkspacePieceDefinition> pieces;

    public MKStructureWorkspace(UUID id, BlockPos anchor, String namespace, String structureName,
                                MKStructureFamilyType familyType, MKWorkspaceDimensions dimensions,
                                MKWorkspaceMaterialPalette palette, MKWorkspaceStairAuthoringConfig stairConfig,
                                MKVerticalAccessPlacement verticalAccessPlacement,
                                int shellMargin, int exteriorAirMargin,
                                int previewMargin,
                                long createdAt, long updatedAt, List<MKWorkspacePieceDefinition> pieces) {
        this.id = id;
        this.anchor = anchor;
        this.namespace = namespace;
        this.structureName = structureName;
        this.familyType = familyType;
        this.dimensions = dimensions;
        this.palette = palette;
        this.stairConfig = stairConfig;
        this.verticalAccessPlacement = verticalAccessPlacement;
        this.shellMargin = shellMargin;
        this.exteriorAirMargin = exteriorAirMargin;
        this.previewMargin = previewMargin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pieces = List.copyOf(pieces);
    }

    public static MKStructureWorkspace createDraft(BlockPos anchor) {
        long now = System.currentTimeMillis();
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                anchor,
                "mkdev",
                "tower_workspace",
                MKStructureFamilyType.TOWER,
                MKWorkspaceDimensions.defaultDimensions(),
                MKWorkspaceMaterialPalette.defaultPalette(),
                MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.CENTER,
                1,
                2,
                4,
                now,
                now,
                List.of()
        );
    }

    public static MKStructureWorkspace fromTag(CompoundTag tag) {
        List<MKWorkspacePieceDefinition> pieces = new ArrayList<>();
        for (Tag pieceTag : tag.getList("pieces", Tag.TAG_COMPOUND)) {
            pieces.add(MKWorkspacePieceDefinition.fromTag((CompoundTag) pieceTag));
        }
        return new MKStructureWorkspace(
                tag.getUUID("id"),
                MKWorkspaceNbtUtil.blockPosFromTag(tag.getCompound("anchor")),
                tag.getString("namespace"),
                tag.getString("structureName"),
                MKStructureFamilyType.fromSerializedName(tag.getString("familyType")),
                MKWorkspaceDimensions.fromTag(tag.getCompound("dimensions")),
                MKWorkspaceMaterialPalette.fromTag(tag.getCompound("palette")),
                tag.contains("stairConfig") ? MKWorkspaceStairAuthoringConfig.fromTag(tag.getCompound("stairConfig"))
                        : MKWorkspaceStairAuthoringConfig.defaultConfig(),
                MKVerticalAccessPlacement.fromSerializedName(tag.getString("verticalAccessPlacement")),
                tag.getInt("shellMargin"),
                tag.contains("exteriorAirMargin") ? tag.getInt("exteriorAirMargin") : 2,
                tag.getInt("previewMargin"),
                tag.getLong("createdAt"),
                tag.getLong("updatedAt"),
                pieces
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", id);
        tag.put("anchor", MKWorkspaceNbtUtil.blockPosToTag(anchor));
        tag.putString("namespace", namespace);
        tag.putString("structureName", structureName);
        tag.putString("familyType", familyType.getSerializedName());
        tag.put("dimensions", dimensions.toTag());
        tag.put("palette", palette.toTag());
        tag.put("stairConfig", stairConfig.toTag());
        tag.putString("verticalAccessPlacement", verticalAccessPlacement.getSerializedName());
        tag.putInt("shellMargin", shellMargin);
        tag.putInt("exteriorAirMargin", exteriorAirMargin);
        tag.putInt("previewMargin", previewMargin);
        tag.putLong("createdAt", createdAt);
        tag.putLong("updatedAt", updatedAt);
        ListTag piecesTag = new ListTag();
        for (MKWorkspacePieceDefinition piece : pieces) {
            piecesTag.add(piece.toTag());
        }
        tag.put("pieces", piecesTag);
        return tag;
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>(dimensions.validate());
        List<Integer> allowedTowerHeights = MKWorkspaceDimensions.getAllowedTowerHeights(stairConfig,
                dimensions.hallwayWidth(), 3, 4);
        if (!allowedTowerHeights.contains(dimensions.roomHeight())) {
            errors.add("room height must be one of " + allowedTowerHeights + " for stair mode " +
                    MKVerticalAccessProfile.normalizeMode(stairConfig.mode()).getSerializedName());
        }
        List<Integer> allowedFlatRunLengths = MKWorkspaceDimensions.getAllowedFlatRunLengths(stairConfig,
                dimensions.hallwayWidth(), dimensions.roomHeight(), 4);
        if (!allowedFlatRunLengths.contains(stairConfig.flatRunLength())) {
            errors.add("flat run length must be one of " + allowedFlatRunLengths + " for room height " +
                    dimensions.roomHeight());
        }
        List<Integer> allowedStairWidths = MKWorkspaceDimensions.getAllowedStairWidths(dimensions.hallwayWidth());
        if (!allowedStairWidths.contains(stairConfig.stairWidth())) {
            errors.add("stair width must be one of " + allowedStairWidths + " for shaft size " +
                    dimensions.hallwayWidth());
        }
        List<Integer> allowedEntranceHeights = MKWorkspaceDimensions.getAllowedEntranceHeights(stairConfig,
                dimensions.hallwayWidth(), dimensions.roomHeight(), 3, 4);
        if (!allowedEntranceHeights.contains(dimensions.entranceHeight())) {
            errors.add("entrance height must be one of " + allowedEntranceHeights + " to stay in phase with room height " +
                    dimensions.roomHeight());
        }
        List<Integer> allowedBasementHeights = MKWorkspaceDimensions.getAllowedEntranceHeights(stairConfig,
                dimensions.hallwayWidth(), dimensions.roomHeight(), 3, 4);
        if (!allowedBasementHeights.contains(dimensions.basementHeight())) {
            errors.add("basement height must be one of " + allowedBasementHeights + " to stay in phase with room height " +
                    dimensions.roomHeight());
        }
        if (namespace.isBlank()) {
            errors.add("namespace cannot be blank");
        }
        if (structureName.isBlank()) {
            errors.add("structure name cannot be blank");
        }
        if (shellMargin < 1) {
            errors.add("shell margin must be at least 1");
        }
        if (exteriorAirMargin < 0) {
            errors.add("exterior air margin must be at least 0");
        }
        if (previewMargin < 2) {
            errors.add("preview margin must be at least 2");
        }
        return errors;
    }

    public MKStructureWorkspace withPieces(List<MKWorkspacePieceDefinition> newPieces) {
        return new MKStructureWorkspace(id, anchor, namespace, structureName, familyType, dimensions, palette,
                stairConfig, verticalAccessPlacement, shellMargin, exteriorAirMargin, previewMargin, createdAt,
                System.currentTimeMillis(), newPieces);
    }

    public UUID id() {
        return id;
    }

    public BlockPos anchor() {
        return anchor;
    }

    public String namespace() {
        return namespace;
    }

    public String structureName() {
        return structureName;
    }

    public MKStructureFamilyType familyType() {
        return familyType;
    }

    public MKWorkspaceDimensions dimensions() {
        return dimensions;
    }

    public MKWorkspaceMaterialPalette palette() {
        return palette;
    }

    public MKWorkspaceStairAuthoringConfig stairConfig() {
        return stairConfig;
    }

    public MKVerticalAccessPlacement verticalAccessPlacement() {
        return verticalAccessPlacement;
    }

    public int shellMargin() {
        return shellMargin;
    }

    public int exteriorAirMargin() {
        return exteriorAirMargin;
    }

    public int previewMargin() {
        return previewMargin;
    }

    public long createdAt() {
        return createdAt;
    }

    public long updatedAt() {
        return updatedAt;
    }

    public List<MKWorkspacePieceDefinition> pieces() {
        return pieces;
    }
}

