package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.providers.MKWorkspaceExportManifestLoader;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedConnector;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceScaffoldBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MKStructureWorkspaceImportService {
    public record MKWorkspaceImportResult(UUID workspaceId, int pieceCount) {
    }

    public record MKWorkspaceImportOutcome(@Nullable MKWorkspaceImportResult result, List<String> validationErrors) {
        public static MKWorkspaceImportOutcome success(MKWorkspaceImportResult result) {
            return new MKWorkspaceImportOutcome(result, List.of());
        }

        public static MKWorkspaceImportOutcome failed() {
            return new MKWorkspaceImportOutcome(null, List.of());
        }

        public static MKWorkspaceImportOutcome validationFailed(List<String> validationErrors) {
            return new MKWorkspaceImportOutcome(null, List.copyOf(validationErrors));
        }

        public Optional<MKWorkspaceImportResult> resultOpt() {
            return Optional.ofNullable(result);
        }
    }

    private final MKWorkspaceImportManifestDiscovery discovery = new MKWorkspaceImportManifestDiscovery(
            MKWorkspaceExportManifestLoader.resolveModuleRoot(MKNpc.MODULE_DIRECTORY_NAME), MKNpc.MODID);
    private final MKWorkspaceScaffoldBuilder scaffoldBuilder = new MKWorkspaceScaffoldBuilder();

    public Optional<MKWorkspaceImportResult> importWorkspaceAtAnchor(ServerLevel level, BlockPos anchor,
                                                                     ResourceLocation manifestId) {
        return importWorkspaceAtAnchorDetailed(level, anchor, manifestId).resultOpt();
    }

    public MKWorkspaceImportOutcome importWorkspaceAtAnchorDetailed(ServerLevel level, BlockPos anchor,
                                                                    ResourceLocation manifestId) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        if (data.getWorkspaceByAnchor(anchor).isPresent()) {
            return MKWorkspaceImportOutcome.failed();
        }

        Optional<MKWorkspaceExportManifest> manifestOpt = discovery.loadManifest(manifestId);
        if (manifestOpt.isEmpty()) {
            return MKWorkspaceImportOutcome.failed();
        }

        MKStructureWorkspace workspace = workspaceFromManifest(anchor, manifestOpt.get());
        List<String> validationErrors = workspace.validate();
        if (!validationErrors.isEmpty()) {
            return MKWorkspaceImportOutcome.validationFailed(validationErrors);
        }

        List<MKPlannedPiece> plannedPieces = toPlannedPieces(manifestOpt.get());
        Map<String, MKWorkspaceExportManifest.ExportPiece> exportedByName = manifestOpt.get().pieces().stream()
                .collect(Collectors.toMap(MKWorkspaceExportManifest.ExportPiece::pieceName, piece -> piece));
        Optional<Map<String, StructureTemplate>> templatesByPieceNameOpt =
                preflightImport(level, manifestId, plannedPieces, exportedByName);
        if (templatesByPieceNameOpt.isEmpty()) {
            return MKWorkspaceImportOutcome.failed();
        }
        Map<String, StructureTemplate> templatesByPieceName = templatesByPieceNameOpt.get();

        List<MKWorkspacePieceDefinition> scaffoldedPieces = scaffoldBuilder.build(level, workspace, plannedPieces);

        List<MKWorkspacePieceDefinition> importedPieces = new ArrayList<>();
        for (MKWorkspacePieceDefinition piece : scaffoldedPieces) {
            MKWorkspaceExportManifest.ExportPiece exported = exportedByName.get(piece.pieceName());
            if (exported == null) {
                continue;
            }
            if (MKWorkspaceTemplateReuseTags.isDerived(exported.tags())) {
                importedPieces.add(mergeImportedPiece(piece, exported, workspace));
                continue;
            }
            StructureTemplate template = templatesByPieceName.get(piece.pieceName());
            if (template == null || !placeSavedStructure(level, template, piece.worldOrigin())) {
                return MKWorkspaceImportOutcome.failed();
            }
            importedPieces.add(mergeImportedPiece(piece, exported, workspace));
        }

        MKStructureWorkspace importedWorkspace = workspace.withPieces(importedPieces);
        data.createWorkspace(importedWorkspace);
        return MKWorkspaceImportOutcome.success(new MKWorkspaceImportResult(importedWorkspace.id(),
                importedPieces.size()));
    }

    public List<String> discoverManifestIds() {
        return discovery.discoverCandidates().stream()
                .map(candidate -> candidate.id().toString())
                .toList();
    }

    private Optional<Map<String, StructureTemplate>> preflightImport(ServerLevel level, ResourceLocation manifestId,
                                                                     List<MKPlannedPiece> plannedPieces,
                                                                     Map<String, MKWorkspaceExportManifest.ExportPiece> exportedByName) {
        Map<String, StructureTemplate> templatesByPieceName = new HashMap<>();
        for (MKPlannedPiece plannedPiece : plannedPieces) {
            MKWorkspaceExportManifest.ExportPiece exported = exportedByName.get(plannedPiece.pieceName());
            if (exported == null) {
                MKNpc.LOGGER.warn("Workspace manifest {} is missing exported piece data for planned piece {}",
                        manifestId, plannedPiece.pieceName());
                return Optional.empty();
            }
            if (MKWorkspaceTemplateReuseTags.isDerived(exported.tags())) {
                continue;
            }
            ResourceLocation structureId = ResourceLocation.parse(exported.structureId());
            Optional<StructureTemplate> templateOpt = level.getStructureManager().get(structureId);
            if (templateOpt.isEmpty()) {
                MKNpc.LOGGER.warn("Workspace manifest {} references missing structure template {} for piece {}",
                        manifestId, structureId, plannedPiece.pieceName());
                return Optional.empty();
            }
            templatesByPieceName.put(plannedPiece.pieceName(), templateOpt.get());
        }
        return Optional.of(templatesByPieceName);
    }

    public MKStructureWorkspace workspaceFromManifest(BlockPos anchor, MKWorkspaceExportManifest manifest) {
        return workspaceFromManifest(UUID.randomUUID(), anchor, System.currentTimeMillis(), manifest);
    }

    public MKStructureWorkspace workspaceFromManifest(UUID workspaceId, BlockPos anchor, long createdAt,
                                                      MKWorkspaceExportManifest manifest) {
        MKWorkspaceExportManifest.ExportWorkspaceSettings settings = manifest.settings();
        MKWorkspaceExportManifest.ExportDimensions dimensions = settings.dimensions();
        MKWorkspaceExportManifest.ExportPalette palette = settings.palette();
        MKWorkspaceExportManifest.ExportStairConfig stairConfig = settings.stairConfig();
        MKWorkspaceDimensions workspaceDimensions = new MKWorkspaceDimensions(
                dimensions.roomWidth(),
                dimensions.roomLength(),
                dimensions.entranceHeight(),
                dimensions.roomHeight(),
                dimensions.basementHeight(),
                dimensions.shaftWidth(),
                dimensions.doorwayWidth(),
                dimensions.doorwayHeight()
        );
        MKWorkspaceStairAuthoringConfig workspaceStairConfig = new MKWorkspaceStairAuthoringConfig(
                stairConfig.mode(),
                stairConfig.riseType(),
                stairConfig.stairWidth()
        );
        MKWorkspaceExportManifest.ExportVerticalAccessSpec verticalAccessSpecExport = settings.verticalAccessSpec();
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = new MKWorkspaceVerticalAccessSpec(
                verticalAccessSpecExport.shaftSize(),
                verticalAccessSpecExport.placement(),
                new MKWorkspaceStairAuthoringConfig(
                        verticalAccessSpecExport.stairConfig().mode(),
                        verticalAccessSpecExport.stairConfig().riseType(),
                        verticalAccessSpecExport.stairConfig().stairWidth()
                )
        );
        List<MKWorkspaceRoomFamilyDefinition> familyDefinitions = settings.familyDefinitions().stream()
                .map(family -> {
                    List<MKWorkspaceFamilyHorizontalExitDefinition> horizontalExits = family.horizontalExits().stream()
                            .map(exit -> new MKWorkspaceFamilyHorizontalExitDefinition(
                                    Direction.byName(exit.direction()),
                                    exit.pathKind(),
                                    exit.openingProfileId(),
                                    exit.connectionMode(),
                                    exit.sideOffset(),
                                    exit.verticalOffset()
                            ))
                            .toList();
                    return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                            family.baseName(),
                            family.slotMetadata(),
                            family.verticalAccessGroupId(),
                            family.supportsVerticalAccess(),
                            family.roomWidth(),
                            family.roomLength(),
                            family.roomHeight(),
                            family.horizontalExtrusionMode(),
                            horizontalExits,
                            family.topVoidMargin(),
                            family.bottomVoidMargin(),
                            family.foundationPolicy(),
                            family.paletteOverride());
                })
                .toList();
        List<MKHorizontalOpeningProfile> openingProfiles = settings.openingProfiles().stream()
                .map(profile -> new MKHorizontalOpeningProfile(
                        profile.profileId(),
                        profile.openingWidth(),
                        profile.openingHeight(),
                        profile.allowOnMainPath(),
                        profile.allowOnBranchPath()
                ))
                .toList();
        if (openingProfiles.isEmpty()) {
            openingProfiles = MKHorizontalOpeningProfile.createDefaults(workspaceDimensions);
        }
        List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies = settings.linearRunFamilies().stream()
                .map(linearRun -> new MKWorkspaceLinearRunFamilyDefinition(
                        linearRun.linearRunId(),
                        linearRun.topologySlotId(),
                        linearRun.kind(),
                        linearRun.openingProfileId(),
                        linearRun.length(),
                        linearRun.interiorWidth(),
                        linearRun.interiorHeight(),
                        linearRun.slopeDelta(),
                        linearRun.allowOnMainPath(),
                        linearRun.allowOnBranchPath(),
                        linearRun.projection(),
                        linearRun.supportedShapes(),
                        linearRun.foundationPolicy(),
                        linearRun.paletteOverride()
                ))
                .toList();
        long now = System.currentTimeMillis();
        return new MKStructureWorkspace(
                workspaceId,
                anchor,
                manifest.namespace(),
                manifest.structureName(),
                settings.topologyProfile(),
                workspaceDimensions,
                new MKWorkspaceMaterialPalette(
                        palette.floorBlock(),
                        palette.wallBlock(),
                        palette.ceilingBlock(),
                        palette.stairBlock(),
                        palette.slabBlock(),
                        palette.ladderBlock()
                ),
                workspaceStairConfig,
                settings.verticalAccessPlacement(),
                settings.shellMargin(),
                settings.exteriorAirMargin(),
                settings.previewMargin(),
                verticalAccessSpec,
                familyDefinitions,
                openingProfiles,
                linearRunFamilies,
                createdAt,
                now,
                List.of()
        );
    }

    public List<MKWorkspacePieceDefinition> pieceDefinitionsFromManifest(MKStructureWorkspace workspace,
                                                                         MKWorkspaceExportManifest manifest) {
        return manifest.pieces().stream()
                .map(piece -> pieceDefinitionFromManifest(workspace, piece))
                .toList();
    }

    private List<MKPlannedPiece> toPlannedPieces(MKWorkspaceExportManifest manifest) {
        Map<String, MKWorkspaceExportManifest.ExportPiece> byName = manifest.pieces().stream()
                .collect(Collectors.toMap(MKWorkspaceExportManifest.ExportPiece::pieceName, piece -> piece));
        List<MKPlannedPiece> plannedPieces = new ArrayList<>();
        for (MKWorkspaceExportManifest.ExportTemplateGroup templateGroup : manifest.templateGroups()) {
            for (String pieceName : templateGroup.pieces()) {
                MKWorkspaceExportManifest.ExportPiece piece = byName.get(pieceName);
                if (piece != null) {
                    plannedPieces.add(toPlannedPiece(piece));
                }
            }
        }
        return plannedPieces;
    }

    private MKPlannedPiece toPlannedPiece(MKWorkspaceExportManifest.ExportPiece piece) {
        MKWorkspaceExportManifest.ExportDimensions dimensions = piece.effectiveDimensions();
        List<MKPlannedConnector> connectors = piece.connectors().stream()
                .map(this::toPlannedConnector)
                .toList();
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(piece.tags());
        return new MKPlannedPiece(
                tags.getOrDefault("workspace_topology_slot_id", piece.roleId()),
                piece.pieceName(),
                dimensions.roomWidth(),
                dimensions.roomLength(),
                dimensions.roomHeight(),
                connectors,
                tags
        );
    }

    private MKPlannedConnector toPlannedConnector(MKWorkspaceExportManifest.ExportConnector connector) {
        return new MKPlannedConnector(
                connector.role(),
                Direction.byName(connector.facing()),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset(),
                connector.targetPool().toString(),
                connector.incomingPool().toString()
        );
    }

    private boolean placeSavedStructure(ServerLevel level, StructureTemplate template, BlockPos targetOrigin) {
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);
        template.placeInWorld(level, targetOrigin, targetOrigin, settings, level.getRandom(), Block.UPDATE_ALL);
        return true;
    }

    private MKWorkspacePieceDefinition mergeImportedPiece(MKWorkspacePieceDefinition generatedPiece,
                                                          MKWorkspaceExportManifest.ExportPiece exportedPiece,
                                                          MKStructureWorkspace workspace) {
        List<MKWorkspaceConnectorDefinition> connectors = exportedPiece.connectors().stream()
                .map(this::toConnectorDefinition)
                .toList();
        List<BlockPos> generatedStairPositions = remapGeneratedStairPositions(exportedPiece, generatedPiece.worldOrigin());
        return new MKWorkspacePieceDefinition(
                generatedPiece.pieceId(),
                workspace.id(),
                generatedPiece.pieceName(),
                generatedPiece.roleId(),
                generatedPiece.variantIndex(),
                generatedPiece.effectiveDimensions(),
                generatedPiece.shellMargin(),
                connectors,
                generatedPiece.worldOrigin(),
                generatedPiece.exportBounds(),
                generatedPiece.previewBounds(),
                generatedPiece.structureBlockPos(),
                generatedPiece.signPos(),
                generatedPiece.markerPositions(),
                generatedStairPositions,
                migrateImportedRuntimeTags(workspace, exportedPiece.tags())
        );
    }

    private MKWorkspaceConnectorDefinition toConnectorDefinition(MKWorkspaceExportManifest.ExportConnector connector) {
        return new MKWorkspaceConnectorDefinition(
                connector.role(),
                Direction.byName(connector.facing()),
                new BlockPos(connector.relativePos().x(), connector.relativePos().y(), connector.relativePos().z()),
                connector.openingWidth(),
                connector.openingHeight(),
                connector.lateralOffset(),
                connector.verticalOffset(),
                connector.jigsawName(),
                connector.jigsawTarget(),
                connector.targetPool(),
                connector.incomingPool()
        );
    }

    private MKWorkspacePieceDefinition pieceDefinitionFromManifest(MKStructureWorkspace workspace,
                                                                   MKWorkspaceExportManifest.ExportPiece piece) {
        MKWorkspaceExportManifest.ExportPiecePlacement placement = piece.placement();
        return new MKWorkspacePieceDefinition(
                piece.pieceId(),
                workspace.id(),
                piece.pieceName(),
                piece.roleId(),
                piece.variantIndex(),
                dimensionsFromExport(piece.effectiveDimensions()),
                piece.shellMargin(),
                piece.connectors().stream().map(this::toConnectorDefinition).toList(),
                offsetFromAnchor(workspace.anchor(), placement.worldOriginOffset()),
                boundingBoxFromOffsets(workspace.anchor(), placement.exportBounds()),
                boundingBoxFromOffsets(workspace.anchor(), placement.previewBounds()),
                offsetFromAnchor(workspace.anchor(), placement.structureBlockOffset()),
                offsetFromAnchor(workspace.anchor(), placement.signOffset()),
                piece.markerPositions().stream()
                        .map(position -> offsetFromAnchor(workspace.anchor(), position.offset()))
                        .toList(),
                piece.generatedStairPositions().stream()
                        .map(position -> offsetFromAnchor(workspace.anchor(), position.offset()))
                        .toList(),
                migrateImportedRuntimeTags(workspace, piece.tags())
        );
    }

    static Map<String, String> migrateImportedRuntimeTags(MKStructureWorkspace workspace,
                                                          Map<String, String> sourceTags) {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>(sourceTags);
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID.equals(workspace.topologyProfile().plannerId()) &&
                isKeepCornerStackPiece(tags)) {
            tags.put(MKWorkspaceRuntimePieceInfo.ALLOW_ON_BRANCH_PATH_TAG, "true");
        }
        return tags;
    }

    private static boolean isKeepCornerStackPiece(Map<String, String> tags) {
        String topologySlotId = tags.getOrDefault("workspace_topology_slot_id", "");
        String stackId = tags.getOrDefault("workspace_tower_stack_id", "");
        return topologySlotId.startsWith("keep.corner.") || stackId.startsWith("keep.corner.");
    }

    private MKWorkspaceDimensions dimensionsFromExport(MKWorkspaceExportManifest.ExportDimensions dimensions) {
        return new MKWorkspaceDimensions(
                dimensions.roomWidth(),
                dimensions.roomLength(),
                dimensions.entranceHeight(),
                dimensions.roomHeight(),
                dimensions.basementHeight(),
                dimensions.shaftWidth(),
                dimensions.doorwayWidth(),
                dimensions.doorwayHeight()
        );
    }

    private BoundingBox boundingBoxFromOffsets(BlockPos anchor, MKWorkspaceExportManifest.ExportBoundingBox box) {
        BlockPos min = offsetFromAnchor(anchor, box.minOffset());
        BlockPos max = offsetFromAnchor(anchor, box.maxOffset());
        return new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    private BlockPos offsetFromAnchor(BlockPos anchor, MKWorkspaceExportManifest.ExportBlockPos offset) {
        return anchor.offset(offset.x(), offset.y(), offset.z());
    }

    private List<BlockPos> remapGeneratedStairPositions(MKWorkspaceExportManifest.ExportPiece exportedPiece, BlockPos newOrigin) {
        BlockPos oldOriginOffset = new BlockPos(
                exportedPiece.placement().worldOriginOffset().x(),
                exportedPiece.placement().worldOriginOffset().y(),
                exportedPiece.placement().worldOriginOffset().z()
        );
        List<BlockPos> result = new ArrayList<>();
        for (MKWorkspaceExportManifest.ExportPositionRef pos : exportedPiece.generatedStairPositions()) {
            BlockPos stairOffset = new BlockPos(pos.offset().x(), pos.offset().y(), pos.offset().z());
            result.add(newOrigin.offset(stairOffset.subtract(oldOriginOffset)));
        }
        return result;
    }
}

