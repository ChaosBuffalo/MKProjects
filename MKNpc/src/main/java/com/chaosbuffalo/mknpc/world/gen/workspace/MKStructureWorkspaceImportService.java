package com.chaosbuffalo.mknpc.world.gen.workspace;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.data.providers.MKWorkspaceExportManifestLoader;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.capability.IMKStructureWorkspaceData;
import com.chaosbuffalo.mknpc.world.gen.workspace.export.MKWorkspaceExportManifest;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFloorSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
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

    private final MKWorkspaceImportManifestDiscovery discovery = new MKWorkspaceImportManifestDiscovery(
            MKWorkspaceExportManifestLoader.resolveModuleRoot(MKNpc.MODULE_DIRECTORY_NAME), MKNpc.MODID);
    private final MKWorkspaceScaffoldBuilder scaffoldBuilder = new MKWorkspaceScaffoldBuilder();

    public Optional<MKWorkspaceImportResult> importWorkspaceAtAnchor(ServerLevel level, BlockPos anchor,
                                                                     ResourceLocation manifestId) {
        IMKStructureWorkspaceData data = IMKStructureWorkspaceData.get(level);
        if (data.getWorkspaceByAnchor(anchor).isPresent()) {
            return Optional.empty();
        }

        Optional<MKWorkspaceExportManifest> manifestOpt = discovery.loadManifest(manifestId);
        if (manifestOpt.isEmpty()) {
            return Optional.empty();
        }

        MKStructureWorkspace workspace = fromManifest(anchor, manifestOpt.get());
        if (!workspace.validate().isEmpty()) {
            return Optional.empty();
        }

        List<MKPlannedPiece> plannedPieces = toPlannedPieces(manifestOpt.get());
        Map<String, MKWorkspaceExportManifest.ExportPiece> exportedByName = manifestOpt.get().pieces().stream()
                .collect(Collectors.toMap(MKWorkspaceExportManifest.ExportPiece::pieceName, piece -> piece));
        Optional<Map<String, StructureTemplate>> templatesByPieceNameOpt =
                preflightImport(level, manifestId, plannedPieces, exportedByName);
        if (templatesByPieceNameOpt.isEmpty()) {
            return Optional.empty();
        }
        Map<String, StructureTemplate> templatesByPieceName = templatesByPieceNameOpt.get();

        List<MKWorkspacePieceDefinition> scaffoldedPieces = scaffoldBuilder.build(level, workspace, plannedPieces);

        List<MKWorkspacePieceDefinition> importedPieces = new ArrayList<>();
        for (MKWorkspacePieceDefinition piece : scaffoldedPieces) {
            MKWorkspaceExportManifest.ExportPiece exported = exportedByName.get(piece.pieceName());
            if (exported == null) {
                continue;
            }
            StructureTemplate template = templatesByPieceName.get(piece.pieceName());
            if (template == null || !placeSavedStructure(level, template, piece.worldOrigin())) {
                return Optional.empty();
            }
            importedPieces.add(mergeImportedPiece(piece, exported, workspace.id()));
        }

        MKStructureWorkspace importedWorkspace = workspace.withPieces(importedPieces);
        data.createWorkspace(importedWorkspace);
        return Optional.of(new MKWorkspaceImportResult(importedWorkspace.id(), importedPieces.size()));
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

    private MKStructureWorkspace fromManifest(BlockPos anchor, MKWorkspaceExportManifest manifest) {
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
                dimensions.hallwayWidth(),
                dimensions.doorwayWidth(),
                dimensions.doorwayHeight()
        );
        MKWorkspaceStairAuthoringConfig workspaceStairConfig = new MKWorkspaceStairAuthoringConfig(
                stairConfig.mode(),
                stairConfig.riseType(),
                stairConfig.flatRunLength(),
                stairConfig.stairWidth(),
                stairConfig.stairBlock(),
                stairConfig.slabBlock(),
                stairConfig.ladderBlock()
        );
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = settings.verticalAccessSpec()
                .map(spec -> new MKWorkspaceVerticalAccessSpec(
                        spec.shaftSize(),
                        spec.placement(),
                        new MKWorkspaceStairAuthoringConfig(
                                spec.stairConfig().mode(),
                                spec.stairConfig().riseType(),
                                spec.stairConfig().flatRunLength(),
                                spec.stairConfig().stairWidth(),
                                spec.stairConfig().stairBlock(),
                                spec.stairConfig().slabBlock(),
                                spec.stairConfig().ladderBlock()
                        )
                ))
                .orElseGet(() -> MKWorkspaceVerticalAccessSpec.fromLegacy(
                        workspaceDimensions,
                        settings.verticalAccessPlacement(),
                        workspaceStairConfig
                ));
        List<MKTowerWorkspaceCategoryProfile> categoryProfiles = settings.categoryProfiles().stream()
                .map(profile -> new MKTowerWorkspaceCategoryProfile(
                        profile.category(),
                        profile.roomWidth(),
                        profile.roomLength(),
                        profile.fullHeight().orElse(profile.defaultHeight().orElse(profile.maxHeight().orElse(3))),
                        profile.minHeight()
                ))
                .toList();
        if (categoryProfiles.isEmpty()) {
            categoryProfiles = MKTowerWorkspaceCategoryProfile.createDefaults(workspaceDimensions);
        }
        MKTowerWorkspaceFloorSettings floorSettings = settings.floorSettings()
                .map(floor -> new MKTowerWorkspaceFloorSettings(floor.mainFloors(), floor.basementFloors()))
                .orElseGet(MKTowerWorkspaceFloorSettings::defaultSettings);
        List<MKTowerWorkspaceFamilyDefinition> familyDefinitions = settings.familyDefinitions().stream()
                .map(family -> new MKTowerWorkspaceFamilyDefinition(
                        family.baseName(),
                        family.category(),
                        family.pieceRole(),
                        family.supportsVerticalAccess(),
                        family.roomWidth().orElse(0),
                        family.roomLength().orElse(0),
                        family.roomHeight().orElse(0),
                        family.horizontalExits().stream()
                                .map(exit -> new MKWorkspaceFamilyHorizontalExitDefinition(
                                        Direction.byName(exit.direction()),
                                        exit.pathKind(),
                                        exit.openingProfileId()
                                ))
                                .toList()
                ))
                .toList();
        familyDefinitions = MKTowerWorkspaceFamilyDefinition.normalize(familyDefinitions, categoryProfiles);
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
        List<MKHallwayFamilyDefinition> hallwayFamilies = settings.hallwayFamilies().stream()
                .map(hallway -> new MKHallwayFamilyDefinition(
                        hallway.hallwayId(),
                        hallway.openingProfileId(),
                        hallway.length(),
                        hallway.interiorWidth(),
                        hallway.interiorHeight(),
                        hallway.slopeDelta(),
                        hallway.allowOnMainPath(),
                        hallway.allowOnBranchPath(),
                        hallway.floorBlock(),
                        hallway.wallBlock(),
                        hallway.ceilingBlock()
                ))
                .toList();
        long now = System.currentTimeMillis();
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                anchor,
                manifest.namespace(),
                manifest.structureName(),
                manifest.familyType(),
                workspaceDimensions,
                new MKWorkspaceMaterialPalette(
                        palette.floorBlock(),
                        palette.wallBlock(),
                        palette.ceilingBlock()
                ),
                workspaceStairConfig,
                settings.verticalAccessPlacement(),
                settings.shellMargin(),
                settings.exteriorAirMargin(),
                settings.previewMargin(),
                verticalAccessSpec,
                floorSettings,
                categoryProfiles,
                familyDefinitions,
                openingProfiles,
                hallwayFamilies,
                now,
                now,
                List.of()
        );
    }

    private List<MKPlannedPiece> toPlannedPieces(MKWorkspaceExportManifest manifest) {
        Map<String, MKWorkspaceExportManifest.ExportPiece> byName = manifest.pieces().stream()
                .collect(Collectors.toMap(MKWorkspaceExportManifest.ExportPiece::pieceName, piece -> piece));
        List<MKPlannedPiece> plannedPieces = new ArrayList<>();
        for (MKWorkspaceExportManifest.ExportCategory category : manifest.categories()) {
            for (String pieceName : category.pieces()) {
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
        return new MKPlannedPiece(
                piece.role(),
                piece.pieceName(),
                dimensions.roomWidth(),
                dimensions.roomLength(),
                dimensions.roomHeight(),
                connectors,
                new LinkedHashMap<>(piece.tags())
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
                                                          UUID workspaceId) {
        List<MKWorkspaceConnectorDefinition> connectors = exportedPiece.connectors().stream()
                .map(this::toConnectorDefinition)
                .toList();
        List<BlockPos> generatedStairPositions = remapGeneratedStairPositions(exportedPiece, generatedPiece.worldOrigin());
        return new MKWorkspacePieceDefinition(
                generatedPiece.pieceId(),
                workspaceId,
                generatedPiece.pieceName(),
                generatedPiece.role(),
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
                new LinkedHashMap<>(exportedPiece.tags())
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

