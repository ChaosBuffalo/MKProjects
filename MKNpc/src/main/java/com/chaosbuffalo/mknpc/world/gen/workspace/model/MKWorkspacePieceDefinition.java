package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MKWorkspacePieceDefinition {
    public static final Codec<MKWorkspacePieceDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.UUID_CODEC.fieldOf("pieceId").forGetter(MKWorkspacePieceDefinition::pieceId),
            MKWorkspaceCodecs.UUID_CODEC.fieldOf("workspaceId").forGetter(MKWorkspacePieceDefinition::workspaceId),
            Codec.STRING.fieldOf("pieceName").forGetter(MKWorkspacePieceDefinition::pieceName),
            Codec.STRING.fieldOf("roleId").forGetter(MKWorkspacePieceDefinition::roleId),
            Codec.INT.fieldOf("variantIndex").forGetter(MKWorkspacePieceDefinition::variantIndex),
            MKWorkspaceDimensions.CODEC.fieldOf("effectiveDimensions").forGetter(MKWorkspacePieceDefinition::effectiveDimensions),
            Codec.INT.fieldOf("shellMargin").forGetter(MKWorkspacePieceDefinition::shellMargin),
            MKWorkspaceConnectorDefinition.CODEC.listOf().optionalFieldOf("connectors", List.of())
                    .forGetter(MKWorkspacePieceDefinition::connectors),
            MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("worldOrigin").forGetter(MKWorkspacePieceDefinition::worldOrigin),
            MKWorkspaceCodecs.BOUNDING_BOX_CODEC.fieldOf("exportBounds").forGetter(MKWorkspacePieceDefinition::exportBounds),
            MKWorkspaceCodecs.BOUNDING_BOX_CODEC.fieldOf("previewBounds").forGetter(MKWorkspacePieceDefinition::previewBounds),
            MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("structureBlockPos").forGetter(MKWorkspacePieceDefinition::structureBlockPos),
            MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("signPos").forGetter(MKWorkspacePieceDefinition::signPos),
            MKWorkspaceCodecs.BLOCK_POS_CODEC.listOf().optionalFieldOf("markerPositions", List.of())
                    .forGetter(MKWorkspacePieceDefinition::markerPositions),
            MKWorkspaceCodecs.BLOCK_POS_CODEC.listOf().optionalFieldOf("generatedStairPositions", List.of())
                    .forGetter(MKWorkspacePieceDefinition::generatedStairPositions),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("tags", Map.of())
                    .forGetter(MKWorkspacePieceDefinition::tags)
    ).apply(instance, MKWorkspacePieceDefinition::new));

    private final UUID pieceId;
    private final UUID workspaceId;
    private final String pieceName;
    private final String roleId;
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
        this(pieceId, workspaceId, pieceName, role.getSerializedName(), variantIndex, effectiveDimensions, shellMargin,
                connectors, worldOrigin, exportBounds, previewBounds, structureBlockPos, signPos, markerPositions,
                generatedStairPositions, tags);
    }

    public MKWorkspacePieceDefinition(UUID pieceId, UUID workspaceId, String pieceName, String roleId,
                                      int variantIndex, MKWorkspaceDimensions effectiveDimensions, int shellMargin,
                                      List<MKWorkspaceConnectorDefinition> connectors, BlockPos worldOrigin,
                                      BoundingBox exportBounds, BoundingBox previewBounds, BlockPos structureBlockPos,
                                      BlockPos signPos, List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions, Map<String, String> tags) {
        this.pieceId = pieceId;
        this.workspaceId = workspaceId;
        this.pieceName = pieceName;
        this.roleId = roleId;
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
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "workspace piece definition");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "workspace piece definition");
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

    public String roleId() {
        return roleId;
    }

    public MKWorkspacePieceRole role() {
        MKWorkspacePieceRole parsed = parseWorkspacePieceRole(roleId);
        if (parsed != null) {
            return parsed;
        }
        String topologySlotId = tags.getOrDefault("workspace_topology_slot_id", roleId);
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(topologySlotId)
                .map(MKTowerWorkspaceStackSlot::pieceRole)
                .orElseGet(this::legacyRoleFromTags);
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
        return new MKWorkspacePieceDefinition(pieceId, workspaceId, pieceName, roleId, variantIndex, effectiveDimensions,
                shellMargin, connectors, worldOrigin, exportBounds, previewBounds, structureBlockPos, signPos,
                markerPositions, newGeneratedStairPositions, newTags);
    }

    private MKWorkspacePieceRole legacyRoleFromTags() {
        if ("linear_run".equals(tags.get("tower_piece_kind"))) {
            return MKWorkspacePieceRole.HALLWAY;
        }
        if (roleId.endsWith(".entry") || roleId.endsWith("_entry") || roleId.contains(".gate.")) {
            return MKWorkspacePieceRole.ENTRY;
        }
        if (roleId.endsWith(".top_cap") || roleId.endsWith("_top_cap")) {
            return MKWorkspacePieceRole.TOP_CAP;
        }
        if (roleId.endsWith(".top_cap_approach") || roleId.endsWith("_top_cap_approach")) {
            return MKWorkspacePieceRole.TOP_CAP_APPROACH;
        }
        if (roleId.endsWith(".basement_cap") || roleId.endsWith("_basement_cap")) {
            return MKWorkspacePieceRole.BASEMENT_CAP;
        }
        if (roleId.endsWith(".basement_cap_approach") || roleId.endsWith("_basement_cap_approach")) {
            return MKWorkspacePieceRole.BASEMENT_CAP_APPROACH;
        }
        return MKWorkspaceRuntimePieceInfo.fromTags(tags)
                .map(MKWorkspaceRuntimePieceInfo::role)
                .map(this::legacyRoleForJigsawRole)
                .orElse(MKWorkspacePieceRole.FLOOR_MAIN);
    }

    private MKWorkspacePieceRole legacyRoleForJigsawRole(MKJigsawPieceRole role) {
        return switch (role) {
            case TOP_CAP -> MKWorkspacePieceRole.TOP_CAP;
            case TOP_CAP_APPROACH -> MKWorkspacePieceRole.TOP_CAP_APPROACH;
            case BASEMENT_CAP_APPROACH -> MKWorkspacePieceRole.BASEMENT_CAP_APPROACH;
            case TERMINAL -> MKWorkspacePieceRole.BASEMENT_CAP;
            default -> MKWorkspacePieceRole.FLOOR_MAIN;
        };
    }

    private static MKWorkspacePieceRole parseWorkspacePieceRole(String roleId) {
        for (MKWorkspacePieceRole role : MKWorkspacePieceRole.values()) {
            if (role.getSerializedName().equals(roleId)) {
                return role;
            }
        }
        return null;
    }
}
