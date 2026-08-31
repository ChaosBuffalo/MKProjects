package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MKWorkspacePieceDefinition {
    public static final String TAG_PLANNER_ID = "workspace_planner_id";

    public static final Codec<MKWorkspacePieceDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SerializedIdentity.CODEC.fieldOf("identity").forGetter(MKWorkspacePieceDefinition::serializedIdentity),
            SerializedGeometry.CODEC.fieldOf("geometry").forGetter(MKWorkspacePieceDefinition::serializedGeometry),
            SerializedPlacement.CODEC.fieldOf("placement").forGetter(MKWorkspacePieceDefinition::serializedPlacement),
            SerializedSidecars.CODEC.fieldOf("sidecars").forGetter(MKWorkspacePieceDefinition::serializedSidecars)
    ).apply(instance, MKWorkspacePieceDefinition::fromSerializedData));

    private final UUID pieceId;
    private final UUID workspaceId;
    private final String pieceName;
    private final String roleId;
    private final MKWorkspacePlannerId plannerId;
    private final int variantIndex;
    private final MKWorkspaceDimensions effectiveDimensions;
    private final List<MKWorkspaceConnectorDefinition> connectors;
    private final BlockPos worldOrigin;
    private final BoundingBox exportBounds;
    private final BoundingBox previewBounds;
    private final BlockPos structureBlockPos;
    private final BlockPos signPos;
    private final List<BlockPos> markerPositions;
    private final List<BlockPos> generatedStairPositions;
    private final Map<String, String> tags;

    private record SerializedIdentity(UUID pieceId,
                                      UUID workspaceId,
                                      String pieceName,
                                      String roleId,
                                      Optional<MKWorkspacePlannerId> plannerId,
                                      int variantIndex) {
        private static final Codec<SerializedIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MKWorkspaceCodecs.UUID_CODEC.fieldOf("pieceId").forGetter(SerializedIdentity::pieceId),
                MKWorkspaceCodecs.UUID_CODEC.fieldOf("workspaceId").forGetter(SerializedIdentity::workspaceId),
                Codec.STRING.fieldOf("pieceName").forGetter(SerializedIdentity::pieceName),
                Codec.STRING.fieldOf("roleId").forGetter(SerializedIdentity::roleId),
                MKWorkspacePlannerId.CODEC.optionalFieldOf("plannerId").forGetter(SerializedIdentity::plannerId),
                Codec.INT.fieldOf("variantIndex").forGetter(SerializedIdentity::variantIndex)
        ).apply(instance, SerializedIdentity::new));
    }

    private record SerializedGeometry(MKWorkspaceDimensions effectiveDimensions,
                                      Optional<Integer> legacyShellMargin,
                                      Optional<Integer> legacyVerticalShellMargin,
                                      List<MKWorkspaceConnectorDefinition> connectors) {
        private static final Codec<SerializedGeometry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MKWorkspaceDimensions.CODEC.fieldOf("effectiveDimensions")
                        .forGetter(SerializedGeometry::effectiveDimensions),
                Codec.INT.optionalFieldOf("shellMargin").forGetter(SerializedGeometry::legacyShellMargin),
                Codec.INT.optionalFieldOf("verticalShellMargin").forGetter(SerializedGeometry::legacyVerticalShellMargin),
                MKWorkspaceConnectorDefinition.CODEC.listOf().optionalFieldOf("connectors", List.of())
                        .forGetter(SerializedGeometry::connectors)
        ).apply(instance, SerializedGeometry::new));
    }

    private record SerializedPlacement(BlockPos worldOrigin,
                                       BoundingBox exportBounds,
                                       BoundingBox previewBounds,
                                       BlockPos structureBlockPos,
                                       BlockPos signPos) {
        private static final Codec<SerializedPlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("worldOrigin").forGetter(SerializedPlacement::worldOrigin),
                MKWorkspaceCodecs.BOUNDING_BOX_CODEC.fieldOf("exportBounds")
                        .forGetter(SerializedPlacement::exportBounds),
                MKWorkspaceCodecs.BOUNDING_BOX_CODEC.fieldOf("previewBounds")
                        .forGetter(SerializedPlacement::previewBounds),
                MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("structureBlockPos")
                        .forGetter(SerializedPlacement::structureBlockPos),
                MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("signPos").forGetter(SerializedPlacement::signPos)
        ).apply(instance, SerializedPlacement::new));
    }

    private record SerializedSidecars(List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions,
                                      Map<String, String> tags) {
        private static final Codec<SerializedSidecars> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MKWorkspaceCodecs.BLOCK_POS_CODEC.listOf().optionalFieldOf("markerPositions", List.of())
                        .forGetter(SerializedSidecars::markerPositions),
                MKWorkspaceCodecs.BLOCK_POS_CODEC.listOf().optionalFieldOf("generatedStairPositions", List.of())
                        .forGetter(SerializedSidecars::generatedStairPositions),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("tags", Map.of())
                        .forGetter(SerializedSidecars::tags)
        ).apply(instance, SerializedSidecars::new));
    }

    private static MKWorkspacePieceDefinition fromSerializedData(SerializedIdentity identity,
                                                                 SerializedGeometry geometry,
                                                                 SerializedPlacement placement,
                                                                 SerializedSidecars sidecars) {
        return new MKWorkspacePieceDefinition(
                identity.pieceId(),
                identity.workspaceId(),
                identity.pieceName(),
                identity.roleId(),
                identity.plannerId().orElse(null),
                identity.variantIndex(),
                geometry.effectiveDimensions(),
                geometry.connectors(),
                placement.worldOrigin(),
                placement.exportBounds(),
                placement.previewBounds(),
                placement.structureBlockPos(),
                placement.signPos(),
                sidecars.markerPositions(),
                sidecars.generatedStairPositions(),
                sidecars.tags()
        );
    }

    private SerializedIdentity serializedIdentity() {
        return new SerializedIdentity(pieceId, workspaceId, pieceName, roleId, Optional.of(plannerId), variantIndex);
    }

    private SerializedGeometry serializedGeometry() {
        return new SerializedGeometry(effectiveDimensions, Optional.empty(), Optional.empty(), connectors);
    }

    private SerializedPlacement serializedPlacement() {
        return new SerializedPlacement(worldOrigin, exportBounds, previewBounds, structureBlockPos, signPos);
    }

    private SerializedSidecars serializedSidecars() {
        return new SerializedSidecars(markerPositions, generatedStairPositions, tags);
    }

    public MKWorkspacePieceDefinition(UUID pieceId, UUID workspaceId, String pieceName, String roleId,
                                      int variantIndex, MKWorkspaceDimensions effectiveDimensions, int shellMargin,
                                      List<MKWorkspaceConnectorDefinition> connectors, BlockPos worldOrigin,
                                      BoundingBox exportBounds, BoundingBox previewBounds, BlockPos structureBlockPos,
                                      BlockPos signPos, List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions, Map<String, String> tags) {
        this(pieceId, workspaceId, pieceName, roleId, MKWorkspacePlannerId.of(roleId).child(pieceName),
                variantIndex, effectiveDimensions, connectors, worldOrigin, exportBounds, previewBounds,
                structureBlockPos, signPos, markerPositions, generatedStairPositions, tags);
    }

    public MKWorkspacePieceDefinition(UUID pieceId, UUID workspaceId, String pieceName, String roleId,
                                      int variantIndex, MKWorkspaceDimensions effectiveDimensions, int shellMargin,
                                      int verticalShellMargin,
                                      List<MKWorkspaceConnectorDefinition> connectors, BlockPos worldOrigin,
                                      BoundingBox exportBounds, BoundingBox previewBounds, BlockPos structureBlockPos,
                                      BlockPos signPos, List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions, Map<String, String> tags) {
        this(pieceId, workspaceId, pieceName, roleId, MKWorkspacePlannerId.of(roleId).child(pieceName),
                variantIndex, effectiveDimensions, connectors, worldOrigin, exportBounds, previewBounds,
                structureBlockPos, signPos, markerPositions, generatedStairPositions, tags);
    }

    public MKWorkspacePieceDefinition(UUID pieceId, UUID workspaceId, String pieceName, String roleId,
                                      MKWorkspacePlannerId plannerId,
                                      int variantIndex, MKWorkspaceDimensions effectiveDimensions, int shellMargin,
                                      List<MKWorkspaceConnectorDefinition> connectors, BlockPos worldOrigin,
                                      BoundingBox exportBounds, BoundingBox previewBounds, BlockPos structureBlockPos,
                                      BlockPos signPos, List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions, Map<String, String> tags) {
        this(pieceId, workspaceId, pieceName, roleId, plannerId, variantIndex, effectiveDimensions, connectors,
                worldOrigin, exportBounds, previewBounds, structureBlockPos, signPos, markerPositions,
                generatedStairPositions, tags);
    }

    public MKWorkspacePieceDefinition(UUID pieceId, UUID workspaceId, String pieceName, String roleId,
                                      MKWorkspacePlannerId plannerId,
                                      int variantIndex, MKWorkspaceDimensions effectiveDimensions, int shellMargin,
                                      int verticalShellMargin,
                                      List<MKWorkspaceConnectorDefinition> connectors, BlockPos worldOrigin,
                                      BoundingBox exportBounds, BoundingBox previewBounds, BlockPos structureBlockPos,
                                      BlockPos signPos, List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions, Map<String, String> tags) {
        this(pieceId, workspaceId, pieceName, roleId, plannerId, variantIndex, effectiveDimensions, connectors,
                worldOrigin, exportBounds, previewBounds, structureBlockPos, signPos, markerPositions,
                generatedStairPositions, tags);
    }

    public MKWorkspacePieceDefinition(UUID pieceId, UUID workspaceId, String pieceName, String roleId,
                                      int variantIndex, MKWorkspaceDimensions effectiveDimensions,
                                      List<MKWorkspaceConnectorDefinition> connectors, BlockPos worldOrigin,
                                      BoundingBox exportBounds, BoundingBox previewBounds, BlockPos structureBlockPos,
                                      BlockPos signPos, List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions, Map<String, String> tags) {
        this(pieceId, workspaceId, pieceName, roleId, MKWorkspacePlannerId.of(roleId).child(pieceName), variantIndex,
                effectiveDimensions, connectors, worldOrigin, exportBounds, previewBounds, structureBlockPos, signPos,
                markerPositions, generatedStairPositions, tags);
    }

    public MKWorkspacePieceDefinition(UUID pieceId, UUID workspaceId, String pieceName, String roleId,
                                      MKWorkspacePlannerId plannerId,
                                      int variantIndex, MKWorkspaceDimensions effectiveDimensions,
                                      List<MKWorkspaceConnectorDefinition> connectors, BlockPos worldOrigin,
                                      BoundingBox exportBounds, BoundingBox previewBounds, BlockPos structureBlockPos,
                                      BlockPos signPos, List<BlockPos> markerPositions,
                                      List<BlockPos> generatedStairPositions, Map<String, String> tags) {
        this.pieceId = pieceId;
        this.workspaceId = workspaceId;
        this.pieceName = pieceName;
        this.roleId = roleId;
        this.plannerId = resolvePlannerId(pieceName, roleId, plannerId, tags);
        this.variantIndex = variantIndex;
        this.effectiveDimensions = effectiveDimensions;
        this.connectors = List.copyOf(connectors);
        this.worldOrigin = worldOrigin;
        this.exportBounds = exportBounds;
        this.previewBounds = previewBounds;
        this.structureBlockPos = structureBlockPos;
        this.signPos = signPos;
        this.markerPositions = List.copyOf(markerPositions);
        this.generatedStairPositions = List.copyOf(generatedStairPositions);
        this.tags = tagsWithPlannerId(tags, this.plannerId);
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

    public MKWorkspacePlannerId plannerId() {
        return plannerId;
    }

    public int variantIndex() {
        return variantIndex;
    }

    public MKWorkspaceDimensions effectiveDimensions() {
        return effectiveDimensions;
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
        return new MKWorkspacePieceDefinition(pieceId, workspaceId, pieceName, roleId, plannerId, variantIndex,
                effectiveDimensions, connectors, worldOrigin, exportBounds, previewBounds, structureBlockPos,
                signPos, markerPositions, newGeneratedStairPositions, newTags);
    }

    public MKWorkspacePieceDefinition withTags(Map<String, String> newTags) {
        return new MKWorkspacePieceDefinition(pieceId, workspaceId, pieceName, roleId, plannerId, variantIndex,
                effectiveDimensions, connectors, worldOrigin, exportBounds, previewBounds, structureBlockPos,
                signPos, markerPositions, generatedStairPositions, newTags);
    }

    public MKWorkspacePieceDefinition withVariantIndexAndTags(int newVariantIndex, Map<String, String> newTags) {
        return new MKWorkspacePieceDefinition(pieceId, workspaceId, pieceName, roleId, plannerId, newVariantIndex,
                effectiveDimensions, connectors, worldOrigin, exportBounds, previewBounds, structureBlockPos,
                signPos, markerPositions, generatedStairPositions, newTags);
    }

    private static MKWorkspacePlannerId resolvePlannerId(String pieceName, String roleId, MKWorkspacePlannerId plannerId,
                                                         Map<String, String> tags) {
        if (plannerId != null) {
            return plannerId;
        }
        String tagValue = tags == null ? null : tags.get(TAG_PLANNER_ID);
        if (tagValue != null && !tagValue.isBlank()) {
            return MKWorkspacePlannerId.of(tagValue);
        }
        return MKWorkspacePlannerId.of(roleId).child(pieceName);
    }

    private static Map<String, String> tagsWithPlannerId(Map<String, String> tags, MKWorkspacePlannerId plannerId) {
        LinkedHashMap<String, String> resolved = new LinkedHashMap<>(tags == null ? Map.of() : tags);
        resolved.put(TAG_PLANNER_ID, plannerId.value());
        return Map.copyOf(resolved);
    }
}
