package com.chaosbuffalo.mknpc.world.gen.workspace.export;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFloorSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public record MKWorkspaceExportManifest(
        int schemaVersion,
        UUID workspaceId,
        String namespace,
        String structureName,
        MKStructureFamilyType familyType,
        String exportedAt,
        long createdAt,
        long updatedAt,
        ExportWorkspaceSettings settings,
        ExportRuntimeHints runtimeHints,
        List<ExportCategory> categories,
        List<ExportPiece> pieces
) {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");

    public static final Codec<MKWorkspaceExportManifest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("schema_version").forGetter(MKWorkspaceExportManifest::schemaVersion),
            UUID_CODEC.fieldOf("workspace_id").forGetter(MKWorkspaceExportManifest::workspaceId),
            Codec.STRING.fieldOf("namespace").forGetter(MKWorkspaceExportManifest::namespace),
            Codec.STRING.fieldOf("structure_name").forGetter(MKWorkspaceExportManifest::structureName),
            familyTypeCodec().fieldOf("family_type").forGetter(MKWorkspaceExportManifest::familyType),
            Codec.STRING.fieldOf("exported_at").forGetter(MKWorkspaceExportManifest::exportedAt),
            Codec.LONG.fieldOf("created_at").forGetter(MKWorkspaceExportManifest::createdAt),
            Codec.LONG.fieldOf("updated_at").forGetter(MKWorkspaceExportManifest::updatedAt),
            ExportWorkspaceSettings.CODEC.fieldOf("settings").forGetter(MKWorkspaceExportManifest::settings),
            ExportRuntimeHints.CODEC.fieldOf("runtime_hints").forGetter(MKWorkspaceExportManifest::runtimeHints),
            ExportCategory.CODEC.listOf().fieldOf("categories").forGetter(MKWorkspaceExportManifest::categories),
            ExportPiece.CODEC.listOf().fieldOf("pieces").forGetter(MKWorkspaceExportManifest::pieces)
    ).apply(instance, MKWorkspaceExportManifest::new));

    public static MKWorkspaceExportManifest fromWorkspace(MKStructureWorkspace workspace, int schemaVersion, String exportedAt) {
        return fromWorkspace(workspace, schemaVersion, exportedAt, ExportRuntimeHints.forWorkspaceIfValid(workspace));
    }

    public static MKWorkspaceExportManifest snapshotFromWorkspace(MKStructureWorkspace workspace, int schemaVersion,
                                                                  String exportedAt) {
        return fromWorkspace(workspace, schemaVersion, exportedAt, ExportRuntimeHints.empty());
    }

    private static MKWorkspaceExportManifest fromWorkspace(MKStructureWorkspace workspace, int schemaVersion,
                                                           String exportedAt, ExportRuntimeHints runtimeHints) {
        return new MKWorkspaceExportManifest(
                schemaVersion,
                workspace.id(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.familyType(),
                exportedAt,
                workspace.createdAt(),
                workspace.updatedAt(),
                new ExportWorkspaceSettings(
                        ExportBlockPos.from(workspace.anchor()),
                        workspace.shellMargin(),
                        workspace.exteriorAirMargin(),
                        workspace.previewMargin(),
                        workspace.verticalAccessPlacement(),
                        ExportDimensions.from(workspace.dimensions()),
                        new ExportPalette(
                                workspace.palette().floorBlock(),
                                workspace.palette().wallBlock(),
                                workspace.palette().ceilingBlock()
                        ),
                        new ExportStairConfig(
                                workspace.stairConfig().mode(),
                                workspace.stairConfig().riseType(),
                                workspace.stairConfig().stairWidth(),
                                workspace.stairConfig().stairBlock(),
                                workspace.stairConfig().slabBlock(),
                                workspace.stairConfig().ladderBlock()
                        ),
                        Optional.of(ExportVerticalAccessSpec.from(workspace.verticalAccessSpec())),
                        Optional.of(ExportFloorSettings.from(workspace.floorSettings())),
                        workspace.categoryProfiles().stream().map(ExportCategoryProfile::from).toList(),
                        workspace.familyDefinitions().stream().map(ExportFamilyDefinition::from).toList(),
                        workspace.openingProfiles().stream().map(ExportOpeningProfile::from).toList(),
                        workspace.hallwayFamilies().stream().map(ExportHallwayFamily::from).toList()
                ),
                runtimeHints,
                buildCategories(workspace),
                workspace.pieces().stream().map(piece -> ExportPiece.from(workspace, piece)).toList()
        );
    }

    public List<String> validateRuntimeStructureExport() {
        ArrayList<String> errors = new ArrayList<>();
        Map<String, List<ExportPiece>> runtimePiecesByBaseName = pieces.stream()
                .filter(piece -> !"template".equals(piece.workspacePieceKind()))
                .collect(Collectors.groupingBy(ExportPiece::baseName, LinkedHashMap::new, Collectors.toList()));
        if (runtimePiecesByBaseName.isEmpty()) {
            errors.add("Workspace " + namespace + ":" + structureName + " does not define any runtime structure pieces");
        }

        LinkedHashSet<String> startBaseNames = pieces.stream()
                .filter(piece -> !"template".equals(piece.workspacePieceKind()))
                .filter(piece -> MKWorkspaceRuntimePieceInfo.fromTags(piece.tags())
                        .map(MKWorkspaceRuntimePieceInfo::start)
                        .orElse(false))
                .map(ExportPiece::baseName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String hintedStartBaseName = runtimeHints.startBaseName();
        if (!hintedStartBaseName.isBlank()) {
            startBaseNames.add(hintedStartBaseName);
        }

        if (startBaseNames.isEmpty()) {
            errors.add("Workspace " + namespace + ":" + structureName + " did not define a runtime start piece");
        } else if (startBaseNames.size() > 1) {
            errors.add("Workspace " + namespace + ":" + structureName + " defined multiple runtime start pieces " +
                    startBaseNames);
        } else {
            String startBaseName = startBaseNames.getFirst();
            if (runtimePiecesByBaseName.getOrDefault(startBaseName, List.of()).isEmpty()) {
                errors.add("Workspace " + namespace + ":" + structureName + " runtime start piece " +
                        startBaseName + " has no exported runtime variants");
            }
        }

        return List.copyOf(errors);
    }

    private static List<ExportCategory> buildCategories(MKStructureWorkspace workspace) {
        Map<String, List<MKWorkspacePieceDefinition>> grouped = workspace.pieces().stream()
                .collect(Collectors.groupingBy(
                        piece -> piece.tags().getOrDefault("workspace_base_name", piece.pieceName()),
                        LinkedHashMap::new,
                        Collectors.toList()));
        return grouped.entrySet().stream()
                .map(entry -> new ExportCategory(
                        entry.getKey(),
                        entry.getValue().get(0).role(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparingInt(MKWorkspacePieceDefinition::variantIndex))
                                .map(MKWorkspacePieceDefinition::pieceName)
                                .toList()
                ))
                .toList();
    }

    private static Codec<MKStructureFamilyType> familyTypeCodec() {
        return Codec.STRING.xmap(MKStructureFamilyType::fromSerializedName, MKStructureFamilyType::getSerializedName);
    }

    private static Codec<MKVerticalAccessPlacement> verticalAccessPlacementCodec() {
        return Codec.STRING.xmap(MKVerticalAccessPlacement::fromSerializedName, MKVerticalAccessPlacement::getSerializedName);
    }

    private static Codec<MKTowerWorkspaceCategory> towerCategoryCodec() {
        return Codec.STRING.xmap(MKTowerWorkspaceCategory::fromSerializedName, MKTowerWorkspaceCategory::getSerializedName);
    }

    private static Codec<MKWorkspacePieceRole> pieceRoleCodec() {
        return Codec.STRING.xmap(MKWorkspacePieceRole::fromSerializedName, MKWorkspacePieceRole::getSerializedName);
    }

    private static Codec<MKWorkspaceHorizontalExtrusionMode> horizontalExtrusionModeCodec() {
        return Codec.STRING.xmap(MKWorkspaceHorizontalExtrusionMode::fromSerializedName,
                MKWorkspaceHorizontalExtrusionMode::getSerializedName);
    }

    private static Codec<MKWorkspaceHorizontalExitConnectionMode> horizontalExitConnectionModeCodec() {
        return Codec.STRING.xmap(MKWorkspaceHorizontalExitConnectionMode::fromSerializedName,
                MKWorkspaceHorizontalExitConnectionMode::getSerializedName);
    }

    private static Codec<MKConnectorRole> connectorRoleCodec() {
        return Codec.STRING.xmap(MKConnectorRole::fromSerializedName, MKConnectorRole::getSerializedName);
    }

    private static Codec<MKWorkspaceStairMode> stairModeCodec() {
        return Codec.STRING.xmap(MKWorkspaceStairMode::fromSerializedName, MKWorkspaceStairMode::getSerializedName);
    }

    private static Codec<MKWorkspaceStairRiseType> stairRiseTypeCodec() {
        return Codec.STRING.xmap(MKWorkspaceStairRiseType::fromSerializedName, MKWorkspaceStairRiseType::getSerializedName);
    }

    private static Codec<MKJigsawPieceRole> jigsawPieceRoleCodec() {
        return MKJigsawPieceRole.CODEC;
    }

    public record ExportBlockPos(int x, int y, int z) {
        public static final Codec<ExportBlockPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("x").forGetter(ExportBlockPos::x),
                Codec.INT.fieldOf("y").forGetter(ExportBlockPos::y),
                Codec.INT.fieldOf("z").forGetter(ExportBlockPos::z)
        ).apply(instance, ExportBlockPos::new));

        public static ExportBlockPos from(BlockPos pos) {
            return new ExportBlockPos(pos.getX(), pos.getY(), pos.getZ());
        }
    }

    public record ExportDimensions(
            int roomWidth,
            int roomLength,
            int entranceHeight,
            int roomHeight,
            int basementHeight,
            int hallwayWidth,
            int doorwayWidth,
            int doorwayHeight
    ) {
        public static final Codec<ExportDimensions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("room_width").forGetter(ExportDimensions::roomWidth),
                Codec.INT.fieldOf("room_length").forGetter(ExportDimensions::roomLength),
                Codec.INT.fieldOf("entrance_height").forGetter(ExportDimensions::entranceHeight),
                Codec.INT.fieldOf("room_height").forGetter(ExportDimensions::roomHeight),
                Codec.INT.fieldOf("basement_height").forGetter(ExportDimensions::basementHeight),
                Codec.INT.fieldOf("hallway_width").forGetter(ExportDimensions::hallwayWidth),
                Codec.INT.fieldOf("doorway_width").forGetter(ExportDimensions::doorwayWidth),
                Codec.INT.fieldOf("doorway_height").forGetter(ExportDimensions::doorwayHeight)
        ).apply(instance, ExportDimensions::new));

        public static ExportDimensions from(MKWorkspaceDimensions dimensions) {
            return new ExportDimensions(
                    dimensions.roomWidth(),
                    dimensions.roomLength(),
                    dimensions.entranceHeight(),
                    dimensions.roomHeight(),
                    dimensions.basementHeight(),
                    dimensions.hallwayWidth(),
                    dimensions.doorwayWidth(),
                    dimensions.doorwayHeight()
            );
        }
    }

    public record ExportPalette(ResourceLocation floorBlock, ResourceLocation wallBlock, ResourceLocation ceilingBlock) {
        public static final Codec<ExportPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("floor_block").forGetter(ExportPalette::floorBlock),
                ResourceLocation.CODEC.fieldOf("wall_block").forGetter(ExportPalette::wallBlock),
                ResourceLocation.CODEC.fieldOf("ceiling_block").forGetter(ExportPalette::ceilingBlock)
        ).apply(instance, ExportPalette::new));
    }

    public record ExportWorkspaceSettings(
            ExportBlockPos anchor,
            int shellMargin,
            int exteriorAirMargin,
            int previewMargin,
            MKVerticalAccessPlacement verticalAccessPlacement,
            ExportDimensions dimensions,
            ExportPalette palette,
            ExportStairConfig stairConfig,
            Optional<ExportVerticalAccessSpec> verticalAccessSpec,
            Optional<ExportFloorSettings> floorSettings,
            List<ExportCategoryProfile> categoryProfiles,
            List<ExportFamilyDefinition> familyDefinitions,
            List<ExportOpeningProfile> openingProfiles,
            List<ExportHallwayFamily> hallwayFamilies
    ) {
        public static final Codec<ExportWorkspaceSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExportBlockPos.CODEC.fieldOf("anchor").forGetter(ExportWorkspaceSettings::anchor),
                Codec.INT.fieldOf("shell_margin").forGetter(ExportWorkspaceSettings::shellMargin),
                Codec.INT.fieldOf("exterior_air_margin").forGetter(ExportWorkspaceSettings::exteriorAirMargin),
                Codec.INT.fieldOf("preview_margin").forGetter(ExportWorkspaceSettings::previewMargin),
                verticalAccessPlacementCodec().fieldOf("vertical_access_placement").forGetter(ExportWorkspaceSettings::verticalAccessPlacement),
                ExportDimensions.CODEC.fieldOf("dimensions").forGetter(ExportWorkspaceSettings::dimensions),
                ExportPalette.CODEC.fieldOf("palette").forGetter(ExportWorkspaceSettings::palette),
                ExportStairConfig.CODEC.fieldOf("stair_config").forGetter(ExportWorkspaceSettings::stairConfig),
                ExportVerticalAccessSpec.CODEC.optionalFieldOf("vertical_access_spec").forGetter(ExportWorkspaceSettings::verticalAccessSpec),
                ExportFloorSettings.CODEC.optionalFieldOf("floor_settings").forGetter(ExportWorkspaceSettings::floorSettings),
                ExportCategoryProfile.CODEC.listOf().optionalFieldOf("category_profiles", List.of()).forGetter(ExportWorkspaceSettings::categoryProfiles),
                ExportFamilyDefinition.CODEC.listOf().optionalFieldOf("family_definitions", List.of()).forGetter(ExportWorkspaceSettings::familyDefinitions),
                ExportOpeningProfile.CODEC.listOf().optionalFieldOf("opening_profiles", List.of()).forGetter(ExportWorkspaceSettings::openingProfiles),
                ExportHallwayFamily.CODEC.listOf().optionalFieldOf("hallway_families", List.of()).forGetter(ExportWorkspaceSettings::hallwayFamilies)
        ).apply(instance, ExportWorkspaceSettings::new));
    }

    public record ExportVerticalAccessSpec(
            int shaftSize,
            MKVerticalAccessPlacement placement,
            ExportStairConfig stairConfig
    ) {
        public static final Codec<ExportVerticalAccessSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("shaft_size").forGetter(ExportVerticalAccessSpec::shaftSize),
                verticalAccessPlacementCodec().fieldOf("placement").forGetter(ExportVerticalAccessSpec::placement),
                ExportStairConfig.CODEC.fieldOf("stair_config").forGetter(ExportVerticalAccessSpec::stairConfig)
        ).apply(instance, ExportVerticalAccessSpec::new));

        public static ExportVerticalAccessSpec from(MKWorkspaceVerticalAccessSpec spec) {
            return new ExportVerticalAccessSpec(
                    spec.shaftSize(),
                    spec.placement(),
                    new ExportStairConfig(
                            spec.stairConfig().mode(),
                            spec.stairConfig().riseType(),
                            spec.stairConfig().stairWidth(),
                            spec.stairConfig().stairBlock(),
                            spec.stairConfig().slabBlock(),
                            spec.stairConfig().ladderBlock()
                    )
            );
        }
    }

    public record ExportFloorSettings(
            int mainFloors,
            int basementFloors,
            boolean topCapApproachEnabled,
            boolean basementCapApproachEnabled
    ) {
        public static final Codec<ExportFloorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("main_floors").forGetter(ExportFloorSettings::mainFloors),
                Codec.INT.fieldOf("basement_floors").forGetter(ExportFloorSettings::basementFloors),
                Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                        .forGetter(ExportFloorSettings::topCapApproachEnabled),
                Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                        .forGetter(ExportFloorSettings::basementCapApproachEnabled)
        ).apply(instance, ExportFloorSettings::new));

        public static ExportFloorSettings from(MKTowerWorkspaceFloorSettings settings) {
            return new ExportFloorSettings(settings.mainFloors(), settings.basementFloors(),
                    settings.topCapApproachEnabled(), settings.basementCapApproachEnabled());
        }
    }

    public record ExportCategoryProfile(
            MKTowerWorkspaceCategory category,
            int roomWidth,
            int roomLength,
            int minMainPathPieces,
            int maxMainPathPieces,
            int maxBranchPiecesBeforeCap,
            Optional<Integer> fullHeight,
            Optional<Integer> defaultHeight,
            Optional<Integer> legacyMinHeight,
            Optional<Integer> maxHeight,
            Optional<Integer> legacyMainOpeningWidth,
            Optional<Integer> legacyMainOpeningHeight,
            Optional<Integer> legacyBranchOpeningWidth,
            Optional<Integer> legacyBranchOpeningHeight
    ) {
        public static final Codec<ExportCategoryProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                towerCategoryCodec().fieldOf("category").forGetter(ExportCategoryProfile::category),
                Codec.INT.fieldOf("room_width").forGetter(ExportCategoryProfile::roomWidth),
                Codec.INT.fieldOf("room_length").forGetter(ExportCategoryProfile::roomLength),
                Codec.INT.optionalFieldOf("min_main_path_pieces", MKTowerWorkspaceCategoryProfile.DEFAULT_MIN_MAIN_PATH_PIECES)
                        .forGetter(ExportCategoryProfile::minMainPathPieces),
                Codec.INT.optionalFieldOf("max_main_path_pieces", MKTowerWorkspaceCategoryProfile.DEFAULT_MAX_MAIN_PATH_PIECES)
                        .forGetter(ExportCategoryProfile::maxMainPathPieces),
                Codec.INT.optionalFieldOf("max_branch_pieces_before_cap",
                                MKTowerWorkspaceCategoryProfile.DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP)
                        .forGetter(ExportCategoryProfile::maxBranchPiecesBeforeCap),
                Codec.INT.optionalFieldOf("full_height").forGetter(ExportCategoryProfile::fullHeight),
                Codec.INT.optionalFieldOf("default_height").forGetter(ExportCategoryProfile::defaultHeight),
                Codec.INT.optionalFieldOf("min_height").forGetter(ExportCategoryProfile::legacyMinHeight),
                Codec.INT.optionalFieldOf("max_height").forGetter(ExportCategoryProfile::maxHeight),
                Codec.INT.optionalFieldOf("main_opening_width").forGetter(ExportCategoryProfile::legacyMainOpeningWidth),
                Codec.INT.optionalFieldOf("main_opening_height").forGetter(ExportCategoryProfile::legacyMainOpeningHeight),
                Codec.INT.optionalFieldOf("branch_opening_width").forGetter(ExportCategoryProfile::legacyBranchOpeningWidth),
                Codec.INT.optionalFieldOf("branch_opening_height").forGetter(ExportCategoryProfile::legacyBranchOpeningHeight)
        ).apply(instance, ExportCategoryProfile::new));

        public static ExportCategoryProfile from(MKTowerWorkspaceCategoryProfile profile) {
            return new ExportCategoryProfile(
                    profile.category(),
                    profile.roomWidth(),
                    profile.roomLength(),
                    profile.minMainPathPieces(),
                    profile.maxMainPathPieces(),
                    profile.maxBranchPiecesBeforeCap(),
                    Optional.of(profile.fullHeight()),
                    Optional.of(profile.fullHeight()),
                    Optional.empty(),
                    Optional.of(profile.fullHeight()),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            );
        }
    }

    public record ExportFamilyDefinition(
            String baseName,
            MKTowerWorkspaceCategory category,
            MKWorkspacePieceRole pieceRole,
            boolean supportsVerticalAccess,
            Optional<Integer> roomWidth,
            Optional<Integer> roomLength,
            Optional<Integer> roomHeight,
            Optional<MKWorkspaceHorizontalExtrusionMode> horizontalExtrusionMode,
            List<ExportFamilyHorizontalExit> horizontalExits,
            Optional<String> legacyBranchExitMask
    ) {
        public static final Codec<ExportFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base_name").forGetter(ExportFamilyDefinition::baseName),
                towerCategoryCodec().fieldOf("category").forGetter(ExportFamilyDefinition::category),
                pieceRoleCodec().fieldOf("piece_role").forGetter(ExportFamilyDefinition::pieceRole),
                Codec.BOOL.fieldOf("supports_vertical_access").forGetter(ExportFamilyDefinition::supportsVerticalAccess),
            Codec.INT.optionalFieldOf("room_width").forGetter(ExportFamilyDefinition::roomWidth),
            Codec.INT.optionalFieldOf("room_length").forGetter(ExportFamilyDefinition::roomLength),
            Codec.INT.optionalFieldOf("room_height").forGetter(ExportFamilyDefinition::roomHeight),
            horizontalExtrusionModeCodec().optionalFieldOf("horizontal_extrusion_mode")
                    .forGetter(ExportFamilyDefinition::horizontalExtrusionMode),
            ExportFamilyHorizontalExit.CODEC.listOf().optionalFieldOf("horizontal_exits", List.of())
                    .forGetter(ExportFamilyDefinition::horizontalExits),
            Codec.STRING.optionalFieldOf("branch_exit_mask").forGetter(ExportFamilyDefinition::legacyBranchExitMask)
        ).apply(instance, ExportFamilyDefinition::new));

        public static ExportFamilyDefinition from(MKTowerWorkspaceFamilyDefinition familyDefinition) {
            return new ExportFamilyDefinition(
                    familyDefinition.baseName(),
                    familyDefinition.category(),
                    familyDefinition.pieceRole(),
                    familyDefinition.supportsVerticalAccess(),
                    Optional.of(familyDefinition.roomWidth()),
                    Optional.of(familyDefinition.roomLength()),
                    Optional.of(familyDefinition.roomHeight()),
                    Optional.of(familyDefinition.horizontalExtrusionMode()),
                    familyDefinition.horizontalExits().stream().map(ExportFamilyHorizontalExit::from).toList(),
                    Optional.of(familyDefinition.legacyBranchExitMask().getSerializedName())
            );
        }
    }

    public record ExportFamilyHorizontalExit(
            String direction,
            MKWorkspaceHorizontalExitPathKind pathKind,
            String openingProfileId,
            Optional<MKWorkspaceHorizontalExitConnectionMode> connectionMode,
            int sideOffset,
            int verticalOffset
    ) {
        public static final Codec<ExportFamilyHorizontalExit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("direction").forGetter(ExportFamilyHorizontalExit::direction),
                Codec.STRING.xmap(MKWorkspaceHorizontalExitPathKind::fromSerializedName,
                        MKWorkspaceHorizontalExitPathKind::getSerializedName)
                        .fieldOf("path_kind").forGetter(ExportFamilyHorizontalExit::pathKind),
                Codec.STRING.fieldOf("opening_profile_id").forGetter(ExportFamilyHorizontalExit::openingProfileId),
                horizontalExitConnectionModeCodec().optionalFieldOf("connection_mode")
                        .forGetter(ExportFamilyHorizontalExit::connectionMode),
                Codec.INT.optionalFieldOf("side_offset", 0).forGetter(ExportFamilyHorizontalExit::sideOffset),
                Codec.INT.optionalFieldOf("vertical_offset", 0).forGetter(ExportFamilyHorizontalExit::verticalOffset)
        ).apply(instance, ExportFamilyHorizontalExit::new));

        public static ExportFamilyHorizontalExit from(MKWorkspaceFamilyHorizontalExitDefinition exit) {
            return new ExportFamilyHorizontalExit(
                    exit.direction().getSerializedName(),
                    exit.pathKind(),
                    exit.openingProfileId(),
                    Optional.of(exit.connectionMode()),
                    exit.sideOffset(),
                    exit.verticalOffset()
            );
        }
    }

    public record ExportOpeningProfile(
            String profileId,
            int openingWidth,
            int openingHeight,
            boolean allowOnMainPath,
            boolean allowOnBranchPath
    ) {
        public static final Codec<ExportOpeningProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("profile_id").forGetter(ExportOpeningProfile::profileId),
                Codec.INT.fieldOf("opening_width").forGetter(ExportOpeningProfile::openingWidth),
                Codec.INT.fieldOf("opening_height").forGetter(ExportOpeningProfile::openingHeight),
                Codec.BOOL.fieldOf("allow_on_main_path").forGetter(ExportOpeningProfile::allowOnMainPath),
                Codec.BOOL.fieldOf("allow_on_branch_path").forGetter(ExportOpeningProfile::allowOnBranchPath)
        ).apply(instance, ExportOpeningProfile::new));

        public static ExportOpeningProfile from(MKHorizontalOpeningProfile profile) {
            return new ExportOpeningProfile(
                    profile.profileId(),
                    profile.openingWidth(),
                    profile.openingHeight(),
                    profile.allowOnMainPath(),
                    profile.allowOnBranchPath()
            );
        }
    }

    public record ExportHallwayFamily(
            String hallwayId,
            String openingProfileId,
            int length,
            int interiorWidth,
            int interiorHeight,
            int slopeDelta,
            boolean allowOnMainPath,
            boolean allowOnBranchPath,
            ResourceLocation floorBlock,
            ResourceLocation wallBlock,
            ResourceLocation ceilingBlock
    ) {
        public static final Codec<ExportHallwayFamily> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("hallway_id").forGetter(ExportHallwayFamily::hallwayId),
                Codec.STRING.fieldOf("opening_profile_id").forGetter(ExportHallwayFamily::openingProfileId),
                Codec.INT.fieldOf("length").forGetter(ExportHallwayFamily::length),
                Codec.INT.fieldOf("interior_width").forGetter(ExportHallwayFamily::interiorWidth),
                Codec.INT.fieldOf("interior_height").forGetter(ExportHallwayFamily::interiorHeight),
                Codec.INT.fieldOf("slope_delta").forGetter(ExportHallwayFamily::slopeDelta),
                Codec.BOOL.fieldOf("allow_on_main_path").forGetter(ExportHallwayFamily::allowOnMainPath),
                Codec.BOOL.fieldOf("allow_on_branch_path").forGetter(ExportHallwayFamily::allowOnBranchPath),
                ResourceLocation.CODEC.fieldOf("floor_block").forGetter(ExportHallwayFamily::floorBlock),
                ResourceLocation.CODEC.fieldOf("wall_block").forGetter(ExportHallwayFamily::wallBlock),
                ResourceLocation.CODEC.fieldOf("ceiling_block").forGetter(ExportHallwayFamily::ceilingBlock)
        ).apply(instance, ExportHallwayFamily::new));

        public static ExportHallwayFamily from(MKHallwayFamilyDefinition hallwayFamily) {
            return new ExportHallwayFamily(
                    hallwayFamily.hallwayId(),
                    hallwayFamily.openingProfileId(),
                    hallwayFamily.length(),
                    hallwayFamily.interiorWidth(),
                    hallwayFamily.interiorHeight(),
                    hallwayFamily.slopeDelta(),
                    hallwayFamily.allowOnMainPath(),
                    hallwayFamily.allowOnBranchPath(),
                    hallwayFamily.floorBlock(),
                    hallwayFamily.wallBlock(),
                    hallwayFamily.ceilingBlock()
            );
        }
    }

    public record ExportRuntimeHints(
            String startBaseName,
            List<ExportRuntimeCategory> categories,
            List<ExportRuntimePool> pools
    ) {
        public static final Codec<ExportRuntimeHints> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("start_base_name").forGetter(ExportRuntimeHints::startBaseName),
                ExportRuntimeCategory.CODEC.listOf().fieldOf("categories").forGetter(ExportRuntimeHints::categories),
                ExportRuntimePool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(ExportRuntimeHints::pools)
        ).apply(instance, ExportRuntimeHints::new));

        public static ExportRuntimeHints forWorkspace(MKStructureWorkspace workspace) {
            List<ExportRuntimeCategory> categories = buildCategories(workspace).stream()
                    .map(category -> ExportRuntimeCategory.forCategory(workspace, category))
                    .flatMap(java.util.Optional::stream)
                    .toList();
            String startBaseName = findStartBaseName(workspace);
            return new ExportRuntimeHints(startBaseName, categories, buildRuntimePools(workspace));
        }

        public static ExportRuntimeHints forWorkspaceIfValid(MKStructureWorkspace workspace) {
            try {
                return forWorkspace(workspace);
            } catch (IllegalStateException ignored) {
                return empty();
            }
        }

        public static ExportRuntimeHints empty() {
            return new ExportRuntimeHints("", List.of(), List.of());
        }
    }

    public record ExportRuntimePool(
            String baseName,
            ResourceLocation poolId,
            List<String> childBaseNames
    ) {
        public static final Codec<ExportRuntimePool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base_name").forGetter(ExportRuntimePool::baseName),
                ResourceLocation.CODEC.fieldOf("pool_id").forGetter(ExportRuntimePool::poolId),
                Codec.STRING.listOf().fieldOf("child_base_names").forGetter(ExportRuntimePool::childBaseNames)
        ).apply(instance, ExportRuntimePool::new));
    }

    public record ExportRuntimeCategory(
            String baseName,
            MKWorkspacePieceRole role,
            ExportRuntimePieceMetadata pieceMetadata
    ) {
        public static final Codec<ExportRuntimeCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base_name").forGetter(ExportRuntimeCategory::baseName),
                pieceRoleCodec().fieldOf("role").forGetter(ExportRuntimeCategory::role),
                ExportRuntimePieceMetadata.CODEC.fieldOf("piece_metadata").forGetter(ExportRuntimeCategory::pieceMetadata)
        ).apply(instance, ExportRuntimeCategory::new));

        public static java.util.Optional<ExportRuntimeCategory> forCategory(MKStructureWorkspace workspace, ExportCategory category) {
            java.util.Optional<MKWorkspaceRuntimePieceInfo> runtimeInfo = workspace.pieces().stream()
                    .filter(piece -> category.baseName().equals(piece.tags().getOrDefault("workspace_base_name", piece.pieceName())))
                    .map(MKWorkspacePieceDefinition::tags)
                    .map(MKWorkspaceRuntimePieceInfo::fromTags)
                    .flatMap(java.util.Optional::stream)
                    .findFirst();
            if (runtimeInfo.isEmpty()) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(new ExportRuntimeCategory(
                    category.baseName(),
                    category.role(),
                    ExportRuntimePieceMetadata.from(runtimeInfo.get())
            ));
        }
    }

    public record ExportRuntimePieceMetadata(
            MKJigsawPieceRole role,
            int progressionDelta,
            int verticalLevelDelta,
            boolean allowOnMainPath,
            boolean allowOnBranchPath,
            boolean terminal,
            boolean topCapOnly,
            String category,
            boolean mainPathEnding,
            boolean branchCap
    ) {
        public static final Codec<ExportRuntimePieceMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                jigsawPieceRoleCodec().fieldOf("role").forGetter(ExportRuntimePieceMetadata::role),
                Codec.INT.fieldOf("progression_delta").forGetter(ExportRuntimePieceMetadata::progressionDelta),
                Codec.INT.fieldOf("vertical_level_delta").forGetter(ExportRuntimePieceMetadata::verticalLevelDelta),
                Codec.BOOL.fieldOf("allow_on_main_path").forGetter(ExportRuntimePieceMetadata::allowOnMainPath),
                Codec.BOOL.fieldOf("allow_on_branch_path").forGetter(ExportRuntimePieceMetadata::allowOnBranchPath),
                Codec.BOOL.fieldOf("terminal").forGetter(ExportRuntimePieceMetadata::terminal),
                Codec.BOOL.fieldOf("top_cap_only").forGetter(ExportRuntimePieceMetadata::topCapOnly),
                Codec.STRING.optionalFieldOf("category", "").forGetter(ExportRuntimePieceMetadata::category),
                Codec.BOOL.optionalFieldOf("main_path_ending", false).forGetter(ExportRuntimePieceMetadata::mainPathEnding),
                Codec.BOOL.optionalFieldOf("branch_cap", false).forGetter(ExportRuntimePieceMetadata::branchCap)
        ).apply(instance, ExportRuntimePieceMetadata::new));

        public static ExportRuntimePieceMetadata from(MKWorkspaceRuntimePieceInfo runtimeInfo) {
            return new ExportRuntimePieceMetadata(
                    runtimeInfo.role(),
                    runtimeInfo.progressionDelta(),
                    runtimeInfo.verticalLevelDelta(),
                    runtimeInfo.allowOnMainPath(),
                    runtimeInfo.allowOnBranchPath(),
                    runtimeInfo.terminal(),
                    runtimeInfo.topCapOnly(),
                    runtimeInfo.category(),
                    runtimeInfo.mainPathEnding(),
                    runtimeInfo.branchCap()
            );
        }
    }

    public record ExportStairConfig(MKWorkspaceStairMode mode, MKWorkspaceStairRiseType riseType, int stairWidth,
                                    ResourceLocation stairBlock, ResourceLocation slabBlock,
                                    ResourceLocation ladderBlock) {
        public static final Codec<ExportStairConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                stairModeCodec().fieldOf("mode").forGetter(ExportStairConfig::mode),
                stairRiseTypeCodec().fieldOf("rise_type").forGetter(ExportStairConfig::riseType),
                Codec.INT.fieldOf("stair_width").forGetter(ExportStairConfig::stairWidth),
                ResourceLocation.CODEC.fieldOf("stair_block").forGetter(ExportStairConfig::stairBlock),
                ResourceLocation.CODEC.fieldOf("slab_block").forGetter(ExportStairConfig::slabBlock),
                ResourceLocation.CODEC.fieldOf("ladder_block").forGetter(ExportStairConfig::ladderBlock)
        ).apply(instance, ExportStairConfig::new));
    }

    public record ExportCategory(String baseName, MKWorkspacePieceRole role, List<String> pieces) {
        public static final Codec<ExportCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base_name").forGetter(ExportCategory::baseName),
                pieceRoleCodec().fieldOf("role").forGetter(ExportCategory::role),
                Codec.STRING.listOf().fieldOf("pieces").forGetter(ExportCategory::pieces)
        ).apply(instance, ExportCategory::new));
    }

    public record ExportPositionRef(ExportBlockPos absolute, ExportBlockPos offset) {
        public static final Codec<ExportPositionRef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExportBlockPos.CODEC.fieldOf("absolute").forGetter(ExportPositionRef::absolute),
                ExportBlockPos.CODEC.fieldOf("offset").forGetter(ExportPositionRef::offset)
        ).apply(instance, ExportPositionRef::new));

        public static ExportPositionRef from(BlockPos absolute, BlockPos anchor) {
            return new ExportPositionRef(ExportBlockPos.from(absolute), ExportBlockPos.from(absolute.subtract(anchor)));
        }
    }

    public record ExportBoundingBox(ExportBlockPos min, ExportBlockPos max, ExportBlockPos minOffset, ExportBlockPos maxOffset,
                                    int sizeX, int sizeY, int sizeZ) {
        public static final Codec<ExportBoundingBox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExportBlockPos.CODEC.fieldOf("min").forGetter(ExportBoundingBox::min),
                ExportBlockPos.CODEC.fieldOf("max").forGetter(ExportBoundingBox::max),
                ExportBlockPos.CODEC.fieldOf("min_offset").forGetter(ExportBoundingBox::minOffset),
                ExportBlockPos.CODEC.fieldOf("max_offset").forGetter(ExportBoundingBox::maxOffset),
                Codec.INT.fieldOf("size_x").forGetter(ExportBoundingBox::sizeX),
                Codec.INT.fieldOf("size_y").forGetter(ExportBoundingBox::sizeY),
                Codec.INT.fieldOf("size_z").forGetter(ExportBoundingBox::sizeZ)
        ).apply(instance, ExportBoundingBox::new));

        public static ExportBoundingBox from(BoundingBox box, BlockPos anchor) {
            BlockPos min = new BlockPos(box.minX(), box.minY(), box.minZ());
            BlockPos max = new BlockPos(box.maxX(), box.maxY(), box.maxZ());
            return new ExportBoundingBox(
                    ExportBlockPos.from(min),
                    ExportBlockPos.from(max),
                    ExportBlockPos.from(min.subtract(anchor)),
                    ExportBlockPos.from(max.subtract(anchor)),
                    box.getXSpan(),
                    box.getYSpan(),
                    box.getZSpan()
            );
        }
    }

    public record ExportConnector(
            MKConnectorRole role,
            String facing,
            ExportBlockPos relativePos,
            int openingWidth,
            int openingHeight,
            int lateralOffset,
            int verticalOffset,
            ResourceLocation jigsawName,
            ResourceLocation jigsawTarget,
            ResourceLocation targetPool,
            ResourceLocation incomingPool
    ) {
        public static final Codec<ExportConnector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                connectorRoleCodec().fieldOf("role").forGetter(ExportConnector::role),
                Codec.STRING.fieldOf("facing").forGetter(ExportConnector::facing),
                ExportBlockPos.CODEC.fieldOf("relative_pos").forGetter(ExportConnector::relativePos),
                Codec.INT.fieldOf("opening_width").forGetter(ExportConnector::openingWidth),
                Codec.INT.fieldOf("opening_height").forGetter(ExportConnector::openingHeight),
                Codec.INT.optionalFieldOf("lateral_offset", 0).forGetter(ExportConnector::lateralOffset),
                Codec.INT.optionalFieldOf("vertical_offset", 0).forGetter(ExportConnector::verticalOffset),
                ResourceLocation.CODEC.fieldOf("jigsaw_name").forGetter(ExportConnector::jigsawName),
                ResourceLocation.CODEC.fieldOf("jigsaw_target").forGetter(ExportConnector::jigsawTarget),
                ResourceLocation.CODEC.fieldOf("target_pool").forGetter(ExportConnector::targetPool),
                ResourceLocation.CODEC.optionalFieldOf("incoming_pool", EMPTY_POOL).forGetter(ExportConnector::incomingPool)
        ).apply(instance, ExportConnector::new));

        public static ExportConnector from(MKWorkspaceConnectorDefinition connector) {
            return new ExportConnector(
                    connector.role(),
                    connector.facing().getSerializedName(),
                    ExportBlockPos.from(connector.relativePos()),
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
    }

    public record ExportPiece(
            UUID pieceId,
            String pieceName,
            String baseName,
            MKWorkspacePieceRole role,
            int variantIndex,
            String workspacePieceKind,
            String structureId,
            int shellMargin,
            ExportDimensions effectiveDimensions,
            ExportPiecePlacement placement,
            List<ExportPositionRef> markerPositions,
            List<ExportPositionRef> generatedStairPositions,
            Map<String, String> tags,
            List<ExportConnector> connectors
    ) {
        public static final Codec<ExportPiece> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUID_CODEC.fieldOf("piece_id").forGetter(ExportPiece::pieceId),
                Codec.STRING.fieldOf("piece_name").forGetter(ExportPiece::pieceName),
                Codec.STRING.fieldOf("base_name").forGetter(ExportPiece::baseName),
                pieceRoleCodec().fieldOf("role").forGetter(ExportPiece::role),
                Codec.INT.fieldOf("variant_index").forGetter(ExportPiece::variantIndex),
                Codec.STRING.fieldOf("workspace_piece_kind").forGetter(ExportPiece::workspacePieceKind),
                Codec.STRING.fieldOf("structure_id").forGetter(ExportPiece::structureId),
                Codec.INT.fieldOf("shell_margin").forGetter(ExportPiece::shellMargin),
                ExportDimensions.CODEC.fieldOf("effective_dimensions").forGetter(ExportPiece::effectiveDimensions),
                ExportPiecePlacement.CODEC.fieldOf("placement").forGetter(ExportPiece::placement),
                ExportPositionRef.CODEC.listOf().fieldOf("marker_positions").forGetter(ExportPiece::markerPositions),
                ExportPositionRef.CODEC.listOf().fieldOf("generated_stair_positions").forGetter(ExportPiece::generatedStairPositions),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("tags").forGetter(ExportPiece::tags),
                ExportConnector.CODEC.listOf().fieldOf("connectors").forGetter(ExportPiece::connectors)
        ).apply(instance, ExportPiece::new));

        public static ExportPiece from(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
            return new ExportPiece(
                    piece.pieceId(),
                    piece.pieceName(),
                    piece.tags().getOrDefault("workspace_base_name", piece.pieceName()),
                    piece.role(),
                    piece.variantIndex(),
                    piece.tags().getOrDefault("workspace_piece_kind", "instance"),
                    workspace.namespace() + ":" + workspace.structureName() + "/" + piece.pieceName(),
                    piece.shellMargin(),
                    ExportDimensions.from(piece.effectiveDimensions()),
                    new ExportPiecePlacement(
                            ExportBlockPos.from(piece.worldOrigin()),
                            ExportBlockPos.from(piece.worldOrigin().subtract(workspace.anchor())),
                            ExportBlockPos.from(piece.structureBlockPos()),
                            ExportBlockPos.from(piece.structureBlockPos().subtract(workspace.anchor())),
                            ExportBlockPos.from(piece.signPos()),
                            ExportBlockPos.from(piece.signPos().subtract(workspace.anchor())),
                            ExportBoundingBox.from(piece.exportBounds(), workspace.anchor()),
                            ExportBoundingBox.from(piece.previewBounds(), workspace.anchor())
                    ),
                    piece.markerPositions().stream().map(pos -> ExportPositionRef.from(pos, workspace.anchor())).toList(),
                    piece.generatedStairPositions().stream().map(pos -> ExportPositionRef.from(pos, workspace.anchor())).toList(),
                    piece.tags().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new)),
                    piece.connectors().stream().map(ExportConnector::from).toList()
            );
        }
    }

    public record ExportPiecePlacement(
            ExportBlockPos worldOrigin,
            ExportBlockPos worldOriginOffset,
            ExportBlockPos structureBlockPos,
            ExportBlockPos structureBlockOffset,
            ExportBlockPos signPos,
            ExportBlockPos signOffset,
            ExportBoundingBox exportBounds,
            ExportBoundingBox previewBounds
    ) {
        public static final Codec<ExportPiecePlacement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExportBlockPos.CODEC.fieldOf("world_origin").forGetter(ExportPiecePlacement::worldOrigin),
                ExportBlockPos.CODEC.fieldOf("world_origin_offset").forGetter(ExportPiecePlacement::worldOriginOffset),
                ExportBlockPos.CODEC.fieldOf("structure_block_pos").forGetter(ExportPiecePlacement::structureBlockPos),
                ExportBlockPos.CODEC.fieldOf("structure_block_offset").forGetter(ExportPiecePlacement::structureBlockOffset),
                ExportBlockPos.CODEC.fieldOf("sign_pos").forGetter(ExportPiecePlacement::signPos),
                ExportBlockPos.CODEC.fieldOf("sign_offset").forGetter(ExportPiecePlacement::signOffset),
                ExportBoundingBox.CODEC.fieldOf("export_bounds").forGetter(ExportPiecePlacement::exportBounds),
                ExportBoundingBox.CODEC.fieldOf("preview_bounds").forGetter(ExportPiecePlacement::previewBounds)
        ).apply(instance, ExportPiecePlacement::new));
    }

    private static String findStartBaseName(MKStructureWorkspace workspace) {
        LinkedHashSet<String> startBaseNames = workspace.pieces().stream()
                .filter(piece -> !"template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance")))
                .filter(piece -> MKWorkspaceRuntimePieceInfo.fromTags(piece.tags()).map(MKWorkspaceRuntimePieceInfo::start).orElse(false))
                .map(piece -> piece.tags().getOrDefault("workspace_base_name", piece.pieceName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (startBaseNames.isEmpty()) {
            throw new IllegalStateException("Workspace " + workspace.namespace() + ":" + workspace.structureName() +
                    " did not define a runtime start piece");
        }
        if (startBaseNames.size() > 1) {
            throw new IllegalStateException("Workspace " + workspace.namespace() + ":" + workspace.structureName() +
                    " defined multiple runtime start pieces " + startBaseNames);
        }
        return startBaseNames.getFirst();
    }

    private static List<ExportRuntimePool> buildRuntimePools(MKStructureWorkspace workspace) {
        LinkedHashMap<ResourceLocation, LinkedHashSet<String>> childrenByPool = new LinkedHashMap<>();
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            if ("template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance"))) {
                continue;
            }
            String baseName = piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
            Optional<MKWorkspaceRuntimePieceInfo> runtimeInfo = MKWorkspaceRuntimePieceInfo.fromTags(piece.tags());
            for (MKWorkspaceConnectorDefinition connector : piece.connectors()) {
                if (connector.incomingPool().equals(EMPTY_POOL)) {
                    continue;
                }
                if (isBranchCapRuntimePool(workspace, connector.incomingPool()) &&
                        !runtimeInfo.map(MKWorkspaceRuntimePieceInfo::branchCap).orElse(false)) {
                    continue;
                }
                if (isBranchRuntimePool(workspace, connector.incomingPool()) &&
                        runtimeInfo.map(MKWorkspaceRuntimePieceInfo::allowOnBranchPath).orElse(false) == false) {
                    continue;
                }
                if (!isBranchRuntimePool(workspace, connector.incomingPool()) &&
                        runtimeInfo.map(MKWorkspaceRuntimePieceInfo::allowOnMainPath).orElse(true) == false) {
                    continue;
                }
                childrenByPool.computeIfAbsent(connector.incomingPool(), key -> new LinkedHashSet<>()).add(baseName);
            }
        }
        return childrenByPool.entrySet().stream()
                .map(entry -> new ExportRuntimePool(
                        derivePoolBaseName(workspace, entry.getKey()),
                        entry.getKey(),
                        List.copyOf(entry.getValue())
                ))
                .toList();
    }

    private static String derivePoolBaseName(MKStructureWorkspace workspace, ResourceLocation poolId) {
        String prefix = workspace.structureName() + "/";
        if (poolId.getNamespace().equals(workspace.namespace()) && poolId.getPath().startsWith(prefix)) {
            return poolId.getPath().substring(prefix.length());
        }
        return poolId.toString();
    }

    private static boolean isBranchRuntimePool(MKStructureWorkspace workspace, ResourceLocation poolId) {
        String path = runtimePoolPath(workspace, poolId);
        return path.startsWith("hallways/branch/") || path.startsWith("rooms/branch/") ||
                path.startsWith("branch_caps/");
    }

    private static boolean isBranchCapRuntimePool(MKStructureWorkspace workspace, ResourceLocation poolId) {
        return runtimePoolPath(workspace, poolId).startsWith("branch_caps/");
    }

    private static String runtimePoolPath(MKStructureWorkspace workspace, ResourceLocation poolId) {
        String prefix = workspace.structureName() + "/";
        return poolId.getNamespace().equals(workspace.namespace()) && poolId.getPath().startsWith(prefix) ?
                poolId.getPath().substring(prefix.length()) : poolId.getPath();
    }
}


