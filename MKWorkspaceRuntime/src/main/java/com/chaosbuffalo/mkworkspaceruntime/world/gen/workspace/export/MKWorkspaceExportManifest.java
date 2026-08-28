package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKConnectorRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKFloorMaskPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKHorizontalExitPathKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertAttachmentFace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunPieceShape;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceLinearRunProjection;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceConnectorDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePaletteTags;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceMetadata;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;
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
        String exportedAt,
        long createdAt,
        long updatedAt,
        ExportWorkspaceSettings settings,
        ExportRuntimeHints runtimeHints,
        List<ExportTemplateGroup> templateGroups,
        List<ExportPiece> pieces
) {
    private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
    private static final ResourceLocation EMPTY_POOL = ResourceLocation.parse("minecraft:empty");
    private static final ResourceLocation WALLED_KEEP_PLANNER_ID =
            ResourceLocation.fromNamespaceAndPath("mknpc", "walled_keep");
    private static final String KEEP_SLOT_POOL_PREFIX = "keep_slots/";
    private static final String COURTYARD_CONTENT_KIND = "courtyard";
    private static final String CONTENT_KIND_TAG = "workspace_content_kind";
    private static final String CONTENT_SIZE_TAG = "workspace_content_size";
    private static final String INSERT_SOCKET_MAX_SIZE_TAG = "workspace_insert_socket_max_square_size";
    @Deprecated(forRemoval = false)
    private static final String COURTYARD_SOCKET_MAX_SIZE_TAG = "workspace_courtyard_socket_max_square_size";
    public static final Codec<MKWorkspaceExportManifest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("schema_version").forGetter(MKWorkspaceExportManifest::schemaVersion),
            UUID_CODEC.fieldOf("workspace_id").forGetter(MKWorkspaceExportManifest::workspaceId),
            Codec.STRING.fieldOf("namespace").forGetter(MKWorkspaceExportManifest::namespace),
            Codec.STRING.fieldOf("structure_name").forGetter(MKWorkspaceExportManifest::structureName),
            Codec.STRING.fieldOf("exported_at").forGetter(MKWorkspaceExportManifest::exportedAt),
            Codec.LONG.fieldOf("created_at").forGetter(MKWorkspaceExportManifest::createdAt),
            Codec.LONG.fieldOf("updated_at").forGetter(MKWorkspaceExportManifest::updatedAt),
            ExportWorkspaceSettings.CODEC.fieldOf("settings").forGetter(MKWorkspaceExportManifest::settings),
            ExportRuntimeHints.CODEC.fieldOf("runtime_hints").forGetter(MKWorkspaceExportManifest::runtimeHints),
            ExportTemplateGroup.CODEC.listOf().fieldOf("template_groups").forGetter(MKWorkspaceExportManifest::templateGroups),
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
        boolean includeRuntimeVariants = !runtimeHints.startBaseName().isBlank() ||
                !runtimeHints.templateGroups().isEmpty() ||
                !runtimeHints.pools().isEmpty();
        List<MKWorkspacePieceDefinition> exportPieces = MKFloorMaskVariantExporter.exportPieces(workspace,
                includeRuntimeVariants);
        ExportRuntimeHints resolvedRuntimeHints = includeRuntimeVariants ?
                ExportRuntimeHints.forWorkspace(workspace, exportPieces) :
                runtimeHints;
        return new MKWorkspaceExportManifest(
                schemaVersion,
                workspace.id(),
                workspace.namespace(),
                workspace.structureName(),
                exportedAt,
                workspace.createdAt(),
                workspace.updatedAt(),
                new ExportWorkspaceSettings(
                        ExportBlockPos.from(workspace.anchor()),
                        workspace.shellMargin(),
                        workspace.verticalShellMargin(),
                        workspace.exteriorAirMargin(),
                        workspace.previewMargin(),
                        workspace.verticalAccessPlacement(),
                        ExportDimensions.from(workspace.dimensions()),
                        new ExportPalette(
                                workspace.palette().floorBlock(),
                                workspace.palette().wallBlock(),
                                workspace.palette().ceilingBlock(),
                                workspace.palette().stairBlock(),
                                workspace.palette().slabBlock(),
                                workspace.palette().ladderBlock()
                        ),
                        new ExportStairConfig(
                                workspace.stairConfig().mode(),
                                workspace.stairConfig().riseType(),
                                workspace.stairConfig().stairWidth()
                        ),
                        ExportVerticalAccessSpec.from(workspace.verticalAccessSpec()),
                        workspace.topologyProfile(),
                        workspace.familyDefinitions().stream().map(ExportFamilyDefinition::from).toList(),
                        workspace.openingProfiles().stream().map(ExportOpeningProfile::from).toList(),
                        workspace.linearRunFamilies().stream().map(ExportLinearRunFamily::from).toList(),
                        workspace.insertFamilies().stream().map(ExportInsertFamily::from).toList()
                ),
                resolvedRuntimeHints,
                buildTemplateGroups(exportPieces),
                exportPieces.stream().map(piece -> ExportPiece.from(workspace, piece)).toList()
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

    private static List<ExportTemplateGroup> buildTemplateGroups(List<MKWorkspacePieceDefinition> pieces) {
        Map<String, List<MKWorkspacePieceDefinition>> grouped = pieces.stream()
                .collect(Collectors.groupingBy(
                        piece -> piece.tags().getOrDefault("workspace_base_name", piece.pieceName()),
                        LinkedHashMap::new,
                        Collectors.toList()));
        return grouped.entrySet().stream()
                .map(entry -> new ExportTemplateGroup(
                        entry.getKey(),
                        entry.getValue().get(0).roleId(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparingInt(MKWorkspacePieceDefinition::variantIndex))
                                .map(MKWorkspacePieceDefinition::pieceName)
                                .toList()
                ))
                .toList();
    }

    public MKWorkspaceExportManifest withNormalizedRuntimeHints() {
        ExportRuntimeHints normalizedHints = new ExportRuntimeHints(
                normalizedStartBaseName(),
                runtimeHints.templateGroups(),
                buildRuntimePoolsFromExportPieces(this)
        );
        return new MKWorkspaceExportManifest(
                schemaVersion,
                workspaceId,
                namespace,
                structureName,
                exportedAt,
                createdAt,
                updatedAt,
                settings,
                normalizedHints,
                templateGroups,
                pieces
        );
    }

    private String normalizedStartBaseName() {
        if (!runtimeHints.startBaseName().isBlank()) {
            return runtimeHints.startBaseName();
        }
        return pieces.stream()
                .filter(piece -> !"template".equals(piece.workspacePieceKind()))
                .filter(piece -> MKWorkspaceRuntimePieceInfo.fromTags(piece.tags())
                        .map(MKWorkspaceRuntimePieceInfo::start)
                        .orElse(false))
                .map(ExportPiece::baseName)
                .findFirst()
                .orElse("");
    }

    private static Codec<MKVerticalAccessPlacement> verticalAccessPlacementCodec() {
        return Codec.STRING.xmap(MKVerticalAccessPlacement::fromSerializedName, MKVerticalAccessPlacement::getSerializedName);
    }

    private static Codec<MKWorkspaceHorizontalExtrusionMode> horizontalExtrusionModeCodec() {
        return Codec.STRING.xmap(MKWorkspaceHorizontalExtrusionMode::fromSerializedName,
                MKWorkspaceHorizontalExtrusionMode::getSerializedName);
    }

    private static Codec<MKWorkspaceLinearRunKind> linearRunKindCodec() {
        return Codec.STRING.xmap(MKWorkspaceLinearRunKind::fromSerializedName, MKWorkspaceLinearRunKind::getSerializedName);
    }

    private static Codec<MKWorkspaceLinearRunProjection> linearRunProjectionCodec() {
        return Codec.STRING.xmap(MKWorkspaceLinearRunProjection::fromSerializedName,
                MKWorkspaceLinearRunProjection::getSerializedName);
    }

    private static Codec<MKWorkspaceLinearRunPieceShape> linearRunShapeCodec() {
        return Codec.STRING.xmap(MKWorkspaceLinearRunPieceShape::fromSerializedName,
                MKWorkspaceLinearRunPieceShape::getSerializedName);
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
            int shaftWidth,
            int doorwayWidth,
            int doorwayHeight
    ) {
        public static final Codec<ExportDimensions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("room_width").forGetter(ExportDimensions::roomWidth),
                Codec.INT.fieldOf("room_length").forGetter(ExportDimensions::roomLength),
                Codec.INT.fieldOf("entrance_height").forGetter(ExportDimensions::entranceHeight),
                Codec.INT.fieldOf("room_height").forGetter(ExportDimensions::roomHeight),
                Codec.INT.fieldOf("basement_height").forGetter(ExportDimensions::basementHeight),
                Codec.INT.fieldOf("shaft_width").forGetter(ExportDimensions::shaftWidth),
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
                    dimensions.shaftWidth(),
                    dimensions.doorwayWidth(),
                    dimensions.doorwayHeight()
            );
        }
    }

    public record ExportPalette(ResourceLocation floorBlock, ResourceLocation wallBlock, ResourceLocation ceilingBlock,
                                ResourceLocation stairBlock, ResourceLocation slabBlock,
                                ResourceLocation ladderBlock) {
        public static final Codec<ExportPalette> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("floor_block").forGetter(ExportPalette::floorBlock),
                ResourceLocation.CODEC.fieldOf("wall_block").forGetter(ExportPalette::wallBlock),
                ResourceLocation.CODEC.fieldOf("ceiling_block").forGetter(ExportPalette::ceilingBlock),
                ResourceLocation.CODEC.optionalFieldOf("stair_block", MKWorkspaceMaterialPalette.defaultPalette().stairBlock())
                        .forGetter(ExportPalette::stairBlock),
                ResourceLocation.CODEC.optionalFieldOf("slab_block", MKWorkspaceMaterialPalette.defaultPalette().slabBlock())
                        .forGetter(ExportPalette::slabBlock),
                ResourceLocation.CODEC.optionalFieldOf("ladder_block", MKWorkspaceMaterialPalette.defaultPalette().ladderBlock())
                        .forGetter(ExportPalette::ladderBlock)
        ).apply(instance, ExportPalette::new));
    }

    public record ExportWorkspaceSettings(
            ExportBlockPos anchor,
            int shellMargin,
            int verticalShellMargin,
            int exteriorAirMargin,
            int previewMargin,
            MKVerticalAccessPlacement verticalAccessPlacement,
            ExportDimensions dimensions,
            ExportPalette palette,
            ExportStairConfig stairConfig,
            ExportVerticalAccessSpec verticalAccessSpec,
            MKWorkspaceTopologyProfile topologyProfile,
            List<ExportFamilyDefinition> familyDefinitions,
            List<ExportOpeningProfile> openingProfiles,
            List<ExportLinearRunFamily> linearRunFamilies,
            List<ExportInsertFamily> insertFamilies
    ) {
        public static final Codec<ExportWorkspaceSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExportBlockPos.CODEC.fieldOf("anchor").forGetter(ExportWorkspaceSettings::anchor),
                Codec.INT.fieldOf("shell_margin").forGetter(ExportWorkspaceSettings::shellMargin),
                Codec.INT.optionalFieldOf("vertical_shell_margin", 1).forGetter(ExportWorkspaceSettings::verticalShellMargin),
                Codec.INT.fieldOf("exterior_air_margin").forGetter(ExportWorkspaceSettings::exteriorAirMargin),
                Codec.INT.fieldOf("preview_margin").forGetter(ExportWorkspaceSettings::previewMargin),
                verticalAccessPlacementCodec().fieldOf("vertical_access_placement").forGetter(ExportWorkspaceSettings::verticalAccessPlacement),
                ExportDimensions.CODEC.fieldOf("dimensions").forGetter(ExportWorkspaceSettings::dimensions),
                ExportPalette.CODEC.fieldOf("palette").forGetter(ExportWorkspaceSettings::palette),
                ExportStairConfig.CODEC.fieldOf("stair_config").forGetter(ExportWorkspaceSettings::stairConfig),
                ExportVerticalAccessSpec.CODEC.fieldOf("vertical_access_spec")
                        .forGetter(ExportWorkspaceSettings::verticalAccessSpec),
                MKWorkspaceTopologyProfile.CODEC.optionalFieldOf("topology_profile",
                                MKWorkspaceTopologyProfile.defaults())
                        .forGetter(ExportWorkspaceSettings::topologyProfile),
                ExportFamilyDefinition.CODEC.listOf().optionalFieldOf("family_definitions", List.of()).forGetter(ExportWorkspaceSettings::familyDefinitions),
                ExportOpeningProfile.CODEC.listOf().optionalFieldOf("opening_profiles", List.of()).forGetter(ExportWorkspaceSettings::openingProfiles),
                ExportLinearRunFamily.CODEC.listOf().optionalFieldOf("linear_run_families", List.of()).forGetter(ExportWorkspaceSettings::linearRunFamilies),
                ExportInsertFamily.CODEC.listOf().optionalFieldOf("insert_families", List.of()).forGetter(ExportWorkspaceSettings::insertFamilies)
        ).apply(instance, ExportWorkspaceSettings::new));

        public ExportWorkspaceSettings(ExportBlockPos anchor, int shellMargin, int exteriorAirMargin,
                                       int previewMargin, MKVerticalAccessPlacement verticalAccessPlacement,
                                       ExportDimensions dimensions, ExportPalette palette,
                                       ExportStairConfig stairConfig, ExportVerticalAccessSpec verticalAccessSpec,
                                       MKWorkspaceTopologyProfile topologyProfile,
                                       List<ExportFamilyDefinition> familyDefinitions,
                                       List<ExportOpeningProfile> openingProfiles,
                                       List<ExportLinearRunFamily> linearRunFamilies) {
            this(anchor, shellMargin, 1, exteriorAirMargin, previewMargin, verticalAccessPlacement, dimensions,
                    palette, stairConfig, verticalAccessSpec, topologyProfile, familyDefinitions, openingProfiles,
                    linearRunFamilies, List.of());
        }
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
                            spec.stairConfig().stairWidth()
                    )
            );
        }
    }

    public record ExportFamilyDefinition(
            String baseName,
            MKWorkspaceTopologySlotMetadata slotMetadata,
            String verticalAccessGroupId,
            boolean supportsVerticalAccess,
            int roomWidth,
            int roomLength,
            int roomHeight,
            MKWorkspaceHorizontalExtrusionMode horizontalExtrusionMode,
            List<ExportFamilyHorizontalExit> horizontalExits,
            int topVoidMargin,
            int bottomVoidMargin,
            @Nullable MKWorkspaceFoundationPolicy foundationPolicy,
            @Nullable MKWorkspacePaletteOverride paletteOverride
    ) {
        public static final Codec<ExportFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base_name").forGetter(ExportFamilyDefinition::baseName),
                MKWorkspaceTopologySlotMetadata.CODEC.fieldOf("slot_metadata")
                        .forGetter(ExportFamilyDefinition::slotMetadata),
                Codec.STRING.optionalFieldOf("vertical_access_group_id", "").forGetter(ExportFamilyDefinition::verticalAccessGroupId),
                Codec.BOOL.fieldOf("supports_vertical_access").forGetter(ExportFamilyDefinition::supportsVerticalAccess),
            Codec.INT.optionalFieldOf("room_width", 0).forGetter(ExportFamilyDefinition::roomWidth),
            Codec.INT.optionalFieldOf("room_length", 0).forGetter(ExportFamilyDefinition::roomLength),
            Codec.INT.optionalFieldOf("room_height", 0).forGetter(ExportFamilyDefinition::roomHeight),
            horizontalExtrusionModeCodec().optionalFieldOf("horizontal_extrusion_mode",
                            MKWorkspaceHorizontalExtrusionMode.FULL_BODY)
                    .forGetter(ExportFamilyDefinition::horizontalExtrusionMode),
            ExportFamilyHorizontalExit.CODEC.listOf().optionalFieldOf("horizontal_exits", List.of())
                    .forGetter(ExportFamilyDefinition::horizontalExits),
            Codec.INT.optionalFieldOf("top_void_margin", 0).forGetter(ExportFamilyDefinition::topVoidMargin),
            Codec.INT.optionalFieldOf("bottom_void_margin", 0).forGetter(ExportFamilyDefinition::bottomVoidMargin),
            MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundation_policy")
                    .forGetter(ExportFamilyDefinition::foundationPolicyOverrideOpt),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                    .forGetter(ExportFamilyDefinition::paletteOverrideOpt)
        ).apply(instance, (baseName, slotMetadata, verticalAccessGroupId, supportsVerticalAccess,
                           roomWidth, roomLength, roomHeight,
                           horizontalExtrusionMode, horizontalExits, topVoidMargin, bottomVoidMargin, foundationPolicy,
                           paletteOverride) ->
                new ExportFamilyDefinition(baseName, slotMetadata, verticalAccessGroupId, supportsVerticalAccess,
                        roomWidth, roomLength, roomHeight, horizontalExtrusionMode, horizontalExits,
                        topVoidMargin, bottomVoidMargin, foundationPolicy.orElse(null), paletteOverride.orElse(null))));

        public static ExportFamilyDefinition from(MKWorkspaceRoomFamilyDefinition familyDefinition) {
            MKWorkspaceTopologySlotMetadata metadata = MKWorkspaceTopologySlotMetadata.fromFamily(familyDefinition);
            return new ExportFamilyDefinition(
                    familyDefinition.baseName(),
                    metadata,
                    familyDefinition.verticalAccessGroupId(),
                    familyDefinition.supportsVerticalAccess(),
                    familyDefinition.roomWidth(),
                    familyDefinition.roomLength(),
                    familyDefinition.roomHeight(),
                    familyDefinition.horizontalExtrusionMode(),
                    familyDefinition.horizontalExits().stream().map(ExportFamilyHorizontalExit::from).toList(),
                    familyDefinition.topVoidMargin(),
                    familyDefinition.bottomVoidMargin(),
                    familyDefinition.foundationPolicyOverride(),
                    familyDefinition.paletteOverride()
            );
        }

        public Optional<MKWorkspaceFoundationPolicy> foundationPolicyOverrideOpt() {
            return Optional.ofNullable(foundationPolicy);
        }

        public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
            return Optional.ofNullable(paletteOverride);
        }

        public String topologySlotId() {
            return slotMetadata.topologySlotId();
        }
    }

    public record ExportFamilyHorizontalExit(
            String direction,
            MKHorizontalExitPathKind pathKind,
            String openingProfileId,
            MKWorkspaceHorizontalExitConnectionMode connectionMode,
            int sideOffset,
            int verticalOffset
    ) {
        public static final Codec<ExportFamilyHorizontalExit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("direction").forGetter(ExportFamilyHorizontalExit::direction),
                Codec.STRING.xmap(MKHorizontalExitPathKind::fromSerializedName,
                        MKHorizontalExitPathKind::getSerializedName)
                        .fieldOf("path_kind").forGetter(ExportFamilyHorizontalExit::pathKind),
                Codec.STRING.optionalFieldOf("opening_profile_id", "").forGetter(ExportFamilyHorizontalExit::openingProfileId),
                horizontalExitConnectionModeCodec().optionalFieldOf("connection_mode",
                                MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN)
                        .forGetter(ExportFamilyHorizontalExit::connectionMode),
                Codec.INT.optionalFieldOf("side_offset", 0).forGetter(ExportFamilyHorizontalExit::sideOffset),
                Codec.INT.optionalFieldOf("vertical_offset", 0).forGetter(ExportFamilyHorizontalExit::verticalOffset)
        ).apply(instance, ExportFamilyHorizontalExit::new));

        public static ExportFamilyHorizontalExit from(MKFamilyHorizontalExitDefinition exit) {
            return new ExportFamilyHorizontalExit(
                    exit.direction().getSerializedName(),
                    exit.pathKind(),
                    exit.openingProfileId(),
                    exit.connectionMode(),
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

    public record ExportLinearRunFamily(
            String linearRunId,
            String topologySlotId,
            MKWorkspaceLinearRunKind kind,
            String openingProfileId,
            int length,
            int interiorWidth,
            int interiorHeight,
            int slopeDelta,
            boolean allowOnMainPath,
            boolean allowOnBranchPath,
            MKWorkspaceLinearRunProjection projection,
            List<MKWorkspaceLinearRunPieceShape> supportedShapes,
            MKWorkspaceFoundationPolicy foundationPolicy,
            @Nullable MKWorkspacePaletteOverride paletteOverride
    ) {
        public static final Codec<ExportLinearRunFamily> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("linear_run_id").forGetter(ExportLinearRunFamily::linearRunId),
                Codec.STRING.optionalFieldOf("topology_slot_id", "").forGetter(ExportLinearRunFamily::topologySlotId),
                linearRunKindCodec().optionalFieldOf("kind", MKWorkspaceLinearRunKind.ENCLOSED_CORRIDOR)
                        .forGetter(ExportLinearRunFamily::kind),
                Codec.STRING.fieldOf("opening_profile_id").forGetter(ExportLinearRunFamily::openingProfileId),
                Codec.INT.fieldOf("length").forGetter(ExportLinearRunFamily::length),
                Codec.INT.fieldOf("interior_width").forGetter(ExportLinearRunFamily::interiorWidth),
                Codec.INT.fieldOf("interior_height").forGetter(ExportLinearRunFamily::interiorHeight),
                Codec.INT.fieldOf("slope_delta").forGetter(ExportLinearRunFamily::slopeDelta),
                Codec.BOOL.fieldOf("allow_on_main_path").forGetter(ExportLinearRunFamily::allowOnMainPath),
                Codec.BOOL.fieldOf("allow_on_branch_path").forGetter(ExportLinearRunFamily::allowOnBranchPath),
                linearRunProjectionCodec().optionalFieldOf("projection", MKWorkspaceLinearRunProjection.RIGID)
                        .forGetter(ExportLinearRunFamily::projection),
                linearRunShapeCodec().listOf().optionalFieldOf("supported_shapes", List.of(MKWorkspaceLinearRunPieceShape.STRAIGHT))
                        .forGetter(ExportLinearRunFamily::supportedShapes),
                MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundation_policy", MKWorkspaceFoundationPolicy.none())
                        .forGetter(ExportLinearRunFamily::foundationPolicy),
                MKWorkspacePaletteOverride.CODEC.optionalFieldOf("palette_override")
                        .forGetter(ExportLinearRunFamily::paletteOverrideOpt)
        ).apply(instance, (linearRunId, topologySlotId, kind, openingProfileId, length, interiorWidth, interiorHeight, slopeDelta,
                           allowOnMainPath, allowOnBranchPath, projection, supportedShapes, foundationPolicy,
                           paletteOverride) ->
                new ExportLinearRunFamily(linearRunId, topologySlotId, kind, openingProfileId, length, interiorWidth, interiorHeight,
                        slopeDelta, allowOnMainPath, allowOnBranchPath, projection, supportedShapes, foundationPolicy,
                        paletteOverride.orElse(null))));

        public static ExportLinearRunFamily from(MKWorkspaceLinearRunFamilyDefinition linearRunFamily) {
            return new ExportLinearRunFamily(
                    linearRunFamily.linearRunId(),
                    linearRunFamily.topologySlotId(),
                    linearRunFamily.kind(),
                    linearRunFamily.openingProfileId(),
                    linearRunFamily.length(),
                    linearRunFamily.interiorWidth(),
                    linearRunFamily.interiorHeight(),
                    linearRunFamily.slopeDelta(),
                    linearRunFamily.allowOnMainPath(),
                    linearRunFamily.allowOnBranchPath(),
                    linearRunFamily.projection(),
                    linearRunFamily.supportedShapes(),
                    linearRunFamily.foundationPolicy(),
                    linearRunFamily.paletteOverride()
            );
        }

        public Optional<MKWorkspacePaletteOverride> paletteOverrideOpt() {
            return Optional.ofNullable(paletteOverride);
        }
    }

    public record ExportInsertFamily(
            String familyId,
            MKWorkspaceInsertFamilyKind kind,
            int width,
            int height,
            int depth,
            Optional<MKWorkspaceInsertAttachmentFace> attachmentFace,
            int faceUOffset,
            int faceVOffset,
            String templateJigsawFinalState
    ) {
        public static final Codec<ExportInsertFamily> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("family_id").forGetter(ExportInsertFamily::familyId),
                MKWorkspaceInsertFamilyKind.CODEC.optionalFieldOf("kind", MKWorkspaceInsertFamilyKind.FLOOR_LINK_HALLWAY)
                        .forGetter(ExportInsertFamily::kind),
                Codec.INT.fieldOf("width").forGetter(ExportInsertFamily::width),
                Codec.INT.fieldOf("height").forGetter(ExportInsertFamily::height),
                Codec.INT.fieldOf("depth").forGetter(ExportInsertFamily::depth),
                MKWorkspaceInsertAttachmentFace.CODEC.optionalFieldOf("attachment_face")
                        .forGetter(ExportInsertFamily::attachmentFace),
                Codec.INT.optionalFieldOf("face_u_offset", 0).forGetter(ExportInsertFamily::faceUOffset),
                Codec.INT.optionalFieldOf("face_v_offset", 0).forGetter(ExportInsertFamily::faceVOffset),
                Codec.STRING.optionalFieldOf("template_jigsaw_final_state", "minecraft:air")
                        .forGetter(ExportInsertFamily::templateJigsawFinalState)
        ).apply(instance, ExportInsertFamily::new));

        public ExportInsertFamily(String familyId, MKWorkspaceInsertFamilyKind kind, int width, int height,
                                  int depth) {
            this(familyId, kind, width, height, depth, Optional.empty(), 0, 0, "minecraft:air");
        }

        public static ExportInsertFamily from(MKWorkspaceInsertFamilyDefinition insertFamily) {
            return new ExportInsertFamily(insertFamily.familyId(), insertFamily.kind(), insertFamily.width(),
                    insertFamily.height(), insertFamily.depth(), insertFamily.attachmentFace(),
                    insertFamily.faceUOffset(), insertFamily.faceVOffset(), insertFamily.templateJigsawFinalState());
        }

        public ResourceLocation poolId(MKWorkspaceExportManifest manifest) {
            return MKInsertFamilyPools.poolId(manifest.namespace(), manifest.structureName(), familyId);
        }
    }

    public record ExportRuntimeHints(
            String startBaseName,
            List<ExportRuntimeTemplateGroup> templateGroups,
            List<ExportRuntimePool> pools
    ) {
        public static final Codec<ExportRuntimeHints> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("start_base_name").forGetter(ExportRuntimeHints::startBaseName),
                ExportRuntimeTemplateGroup.CODEC.listOf().fieldOf("template_groups").forGetter(ExportRuntimeHints::templateGroups),
                ExportRuntimePool.CODEC.listOf().optionalFieldOf("pools", List.of()).forGetter(ExportRuntimeHints::pools)
        ).apply(instance, ExportRuntimeHints::new));

        public static ExportRuntimeHints forWorkspace(MKStructureWorkspace workspace) {
            return forWorkspace(workspace, workspace.pieces());
        }

        public static ExportRuntimeHints forWorkspace(MKStructureWorkspace workspace,
                                                      List<MKWorkspacePieceDefinition> pieces) {
            List<ExportRuntimeTemplateGroup> templateGroups = buildTemplateGroups(pieces).stream()
                    .map(templateGroup -> ExportRuntimeTemplateGroup.forTemplateGroup(workspace, pieces, templateGroup))
                    .flatMap(java.util.Optional::stream)
                    .toList();
            String startBaseName = findStartBaseName(pieces);
            return new ExportRuntimeHints(startBaseName, templateGroups, buildRuntimePools(workspace, pieces));
        }

        public static ExportRuntimeHints forWorkspacePreview(MKStructureWorkspace workspace) {
            return forWorkspacePreview(workspace, workspace.pieces());
        }

        public static ExportRuntimeHints forWorkspacePreview(MKStructureWorkspace workspace,
                                                             List<MKWorkspacePieceDefinition> pieces) {
            List<ExportRuntimeTemplateGroup> templateGroups = buildTemplateGroups(pieces).stream()
                    .map(templateGroup -> ExportRuntimeTemplateGroup.forTemplateGroup(workspace, pieces, templateGroup,
                            true))
                    .flatMap(java.util.Optional::stream)
                    .toList();
            return new ExportRuntimeHints(findPreviewStartBaseName(pieces), templateGroups,
                    buildRuntimePools(workspace, pieces, true));
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

    public record ExportRuntimeTemplateGroup(
            String baseName,
            String roleId,
            ExportRuntimePieceMetadata pieceMetadata
    ) {
        public static final Codec<ExportRuntimeTemplateGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base_name").forGetter(ExportRuntimeTemplateGroup::baseName),
                Codec.STRING.fieldOf("role_id").forGetter(ExportRuntimeTemplateGroup::roleId),
                ExportRuntimePieceMetadata.CODEC.fieldOf("piece_metadata").forGetter(ExportRuntimeTemplateGroup::pieceMetadata)
        ).apply(instance, ExportRuntimeTemplateGroup::new));

        public static java.util.Optional<ExportRuntimeTemplateGroup> forTemplateGroup(
                MKStructureWorkspace workspace,
                List<MKWorkspacePieceDefinition> pieces,
                ExportTemplateGroup templateGroup) {
            return forTemplateGroup(workspace, pieces, templateGroup, false);
        }

        public static java.util.Optional<ExportRuntimeTemplateGroup> forTemplateGroup(
                MKStructureWorkspace workspace,
                List<MKWorkspacePieceDefinition> pieces,
                ExportTemplateGroup templateGroup,
                boolean allowTemplateFallback) {
            java.util.Optional<MKWorkspacePieceDefinition> runtimePiece = pieces.stream()
                    .filter(piece -> templateGroup.baseName().equals(piece.tags().getOrDefault("workspace_base_name", piece.pieceName())))
                    .filter(piece -> !"template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance")))
                    .findFirst()
                    .or(() -> allowTemplateFallback ? pieces.stream()
                            .filter(piece -> templateGroup.baseName().equals(piece.tags()
                                    .getOrDefault("workspace_base_name", piece.pieceName())))
                            .filter(piece -> "template".equals(piece.tags()
                                    .getOrDefault("workspace_piece_kind", "instance")))
                            .findFirst() : Optional.empty());
            java.util.Optional<MKWorkspaceRuntimePieceInfo> runtimeInfo = runtimePiece
                    .map(MKWorkspacePieceDefinition::tags)
                    .flatMap(MKWorkspaceRuntimePieceInfo::fromTags)
                    .or(() -> runtimePiece
                            .filter(MKWorkspaceExportManifest::isRuntimeInsertFamilyVariant)
                            .map(MKWorkspaceExportManifest::defaultInsertRuntimePieceInfo));
            if (runtimeInfo.isEmpty() || runtimePiece.isEmpty()) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(new ExportRuntimeTemplateGroup(
                    templateGroup.baseName(),
                    templateGroup.roleId(),
                    ExportRuntimePieceMetadata.from(runtimeInfo.get(), runtimePiece.get(),
                            foundationPolicyForPiece(workspace, runtimePiece.get()))
            ));
        }

        private static MKWorkspaceFoundationPolicy foundationPolicyForPiece(MKStructureWorkspace workspace,
                                                                            MKWorkspacePieceDefinition piece) {
            String linearRunId = piece.tags().get("workspace_linear_run_family_id");
            if (linearRunId != null && !linearRunId.isBlank()) {
                return workspace.linearRunFamilies().stream()
                        .filter(family -> family.linearRunId().equals(linearRunId))
                        .findFirst()
                        .map(MKWorkspaceLinearRunFamilyDefinition::foundationPolicy)
                        .orElse(MKWorkspaceFoundationPolicy.none());
            }
            String familyId = piece.tags().get("workspace_family_id");
            if (familyId != null && !familyId.isBlank()) {
                return workspace.familyDefinitions().stream()
                        .filter(family -> family.baseName().equals(familyId))
                        .findFirst()
                        .map(family -> workspace.resolveFamilySettings(family).foundationPolicy())
                        .orElse(MKWorkspaceFoundationPolicy.none());
            }
            return MKWorkspaceFoundationPolicy.none();
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
            String topologyGroup,
            boolean mainPathEnding,
            boolean branchCap,
            String verticalStackId,
            String verticalStackSlot,
            int minMainFloors,
            int maxMainFloors,
            int minBasementFloors,
            int maxBasementFloors,
            boolean topCapApproachEnabled,
            boolean basementEntryEnabled,
            boolean basementCapApproachEnabled,
            String floorExitMask,
            MKWorkspaceFoundationPolicy foundationPolicy,
            ResourceLocation floorBlock,
            ResourceLocation wallBlock,
            ResourceLocation ceilingBlock,
            List<MKJigsawPieceMetadata.FloorLinkCandidate> floorLinkCandidates,
            List<MKJigsawPieceMetadata.FloorClosableOpening> floorClosableOpenings,
            List<MKJigsawPieceMetadata.FloorRootExit> floorRootExits
    ) {
        public static final Codec<ExportRuntimePieceMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                jigsawPieceRoleCodec().fieldOf("role").forGetter(ExportRuntimePieceMetadata::role),
                Codec.INT.fieldOf("progression_delta").forGetter(ExportRuntimePieceMetadata::progressionDelta),
                Codec.INT.fieldOf("vertical_level_delta").forGetter(ExportRuntimePieceMetadata::verticalLevelDelta),
                Codec.BOOL.fieldOf("allow_on_main_path").forGetter(ExportRuntimePieceMetadata::allowOnMainPath),
                Codec.BOOL.fieldOf("allow_on_branch_path").forGetter(ExportRuntimePieceMetadata::allowOnBranchPath),
                Codec.BOOL.fieldOf("terminal").forGetter(ExportRuntimePieceMetadata::terminal),
                Codec.BOOL.fieldOf("top_cap_only").forGetter(ExportRuntimePieceMetadata::topCapOnly),
                Codec.STRING.optionalFieldOf("topology_group", "").forGetter(ExportRuntimePieceMetadata::topologyGroup),
                Codec.BOOL.optionalFieldOf("main_path_ending", false).forGetter(ExportRuntimePieceMetadata::mainPathEnding),
                Codec.BOOL.optionalFieldOf("branch_cap", false).forGetter(ExportRuntimePieceMetadata::branchCap),
                ExportVerticalStackMetadata.CODEC.forGetter(ExportRuntimePieceMetadata::verticalStackMetadata),
                ExportFloorRuntimeMetadata.CODEC.forGetter(ExportRuntimePieceMetadata::floorRuntimeMetadata)
        ).apply(instance, (role, progressionDelta, verticalLevelDelta, allowOnMainPath, allowOnBranchPath,
                           terminal, topCapOnly, topologyGroup, mainPathEnding, branchCap, verticalStackMetadata,
                           floorRuntimeMetadata) ->
                new ExportRuntimePieceMetadata(role, progressionDelta, verticalLevelDelta, allowOnMainPath,
                        allowOnBranchPath, terminal, topCapOnly, topologyGroup, mainPathEnding, branchCap,
                        verticalStackMetadata.verticalStackId(), verticalStackMetadata.verticalStackSlot(),
                        verticalStackMetadata.minMainFloors(), verticalStackMetadata.maxMainFloors(),
                        verticalStackMetadata.minBasementFloors(), verticalStackMetadata.maxBasementFloors(),
                        verticalStackMetadata.topCapApproachEnabled(), verticalStackMetadata.basementEntryEnabled(),
                        verticalStackMetadata.basementCapApproachEnabled(), floorRuntimeMetadata.floorExitMask(),
                        floorRuntimeMetadata.foundationPolicy(), floorRuntimeMetadata.floorBlock(),
                        floorRuntimeMetadata.wallBlock(), floorRuntimeMetadata.ceilingBlock(),
                        floorRuntimeMetadata.floorLinkCandidates(), floorRuntimeMetadata.floorClosableOpenings(),
                        floorRuntimeMetadata.floorRootExits())));

        public ExportRuntimePieceMetadata {
            floorExitMask = floorExitMask == null ? "" : floorExitMask;
            floorBlock = floorBlock == null ? MKWorkspaceMaterialPalette.defaultPalette().floorBlock() : floorBlock;
            wallBlock = wallBlock == null ? MKWorkspaceMaterialPalette.defaultPalette().wallBlock() : wallBlock;
            ceilingBlock = ceilingBlock == null ? MKWorkspaceMaterialPalette.defaultPalette().ceilingBlock() : ceilingBlock;
            floorLinkCandidates = floorLinkCandidates == null ? List.of() : List.copyOf(floorLinkCandidates);
            floorClosableOpenings = floorClosableOpenings == null ? List.of() : List.copyOf(floorClosableOpenings);
            floorRootExits = floorRootExits == null ? List.of() : List.copyOf(floorRootExits);
        }

        public static ExportRuntimePieceMetadata from(MKWorkspaceRuntimePieceInfo runtimeInfo,
                                                      MKWorkspacePieceDefinition piece,
                                                      MKWorkspaceFoundationPolicy foundationPolicy) {
            Map<String, String> tags = piece.tags();
            return new ExportRuntimePieceMetadata(
                    runtimeInfo.role(),
                    runtimeInfo.progressionDelta(),
                    runtimeInfo.verticalLevelDelta(),
                    runtimeInfo.allowOnMainPath(),
                    runtimeInfo.allowOnBranchPath(),
                    runtimeInfo.terminal(),
                    runtimeInfo.topCapOnly(),
                    runtimeInfo.topologyGroup(),
                    runtimeInfo.mainPathEnding(),
                    runtimeInfo.branchCap(),
                    tags.getOrDefault("workspace_vertical_stack_id", ""),
                    tags.getOrDefault("workspace_vertical_stack_slot", ""),
                    parseInt(tags, "workspace_vertical_stack_min_main_floors", 0),
                    parseInt(tags, "workspace_vertical_stack_main_floors", 0),
                    parseInt(tags, "workspace_vertical_stack_min_basement_floors", 0),
                    parseInt(tags, "workspace_vertical_stack_basement_floors", 0),
                    Boolean.parseBoolean(tags.getOrDefault("workspace_vertical_stack_top_cap_approach_enabled", "true")),
                    Boolean.parseBoolean(tags.getOrDefault("workspace_vertical_stack_basement_entry_enabled", "true")),
                    Boolean.parseBoolean(tags.getOrDefault("workspace_vertical_stack_basement_cap_approach_enabled", "false")),
                    tags.getOrDefault(MKFloorMaskPools.FLOOR_MASK_TAG, ""),
                    foundationPolicy,
                    paletteBlock(tags, MKWorkspacePaletteTags.FLOOR_BLOCK_TAG,
                            MKWorkspaceMaterialPalette.defaultPalette().floorBlock()),
                    paletteBlock(tags, MKWorkspacePaletteTags.WALL_BLOCK_TAG,
                            MKWorkspaceMaterialPalette.defaultPalette().wallBlock()),
                    paletteBlock(tags, MKWorkspacePaletteTags.CEILING_BLOCK_TAG,
                            MKWorkspaceMaterialPalette.defaultPalette().ceilingBlock()),
                    floorLinkCandidates(tags),
                    floorClosableOpenings(piece),
                    floorRootExits(piece)
            );
        }

        public ExportRuntimePieceMetadata withPieceDerivedFloorMetadata(MKWorkspacePieceDefinition piece) {
            return new ExportRuntimePieceMetadata(
                    role,
                    progressionDelta,
                    verticalLevelDelta,
                    allowOnMainPath,
                    allowOnBranchPath,
                    terminal,
                    topCapOnly,
                    topologyGroup,
                    mainPathEnding,
                    branchCap,
                    verticalStackId,
                    verticalStackSlot,
                    minMainFloors,
                    maxMainFloors,
                    minBasementFloors,
                    maxBasementFloors,
                    topCapApproachEnabled,
                    basementEntryEnabled,
                    basementCapApproachEnabled,
                    piece.tags().getOrDefault(MKFloorMaskPools.FLOOR_MASK_TAG, floorExitMask),
                    foundationPolicy,
                    paletteBlock(piece.tags(), MKWorkspacePaletteTags.FLOOR_BLOCK_TAG, floorBlock),
                    paletteBlock(piece.tags(), MKWorkspacePaletteTags.WALL_BLOCK_TAG, wallBlock),
                    paletteBlock(piece.tags(), MKWorkspacePaletteTags.CEILING_BLOCK_TAG, ceilingBlock),
                    floorLinkCandidates(piece.tags()),
                    floorClosableOpenings(piece),
                    floorRootExits(piece)
            );
        }

        private static ResourceLocation paletteBlock(Map<String, String> tags, String key, ResourceLocation fallback) {
            String value = tags.get(key);
            if (value == null || value.isBlank()) {
                return fallback;
            }
            try {
                return ResourceLocation.parse(value);
            } catch (Exception ignored) {
                return fallback;
            }
        }

        private static List<MKJigsawPieceMetadata.FloorRootExit> floorRootExits(MKWorkspacePieceDefinition piece) {
            if (!isFloorTopologyRootPiece(piece)) {
                return List.of();
            }
            return piece.connectors().stream()
                    .filter(connector -> connector.role() == MKConnectorRole.MAIN_BACK ||
                            connector.role() == MKConnectorRole.BRANCH)
                    .filter(connector -> connector.facing().getAxis().isHorizontal())
                    .map(connector -> floorRootExit(connector))
                    .flatMap(Optional::stream)
                    .toList();
        }

        private static Optional<MKJigsawPieceMetadata.FloorRootExit> floorRootExit(
                MKWorkspaceConnectorDefinition connector) {
            Optional<String> topologyGroup = floorTopologyGroup(connector.targetPool());
            if (topologyGroup.isEmpty()) {
                return Optional.empty();
            }
            MKHorizontalExitPathKind pathKind = connector.role() == MKConnectorRole.MAIN_BACK ?
                    MKHorizontalExitPathKind.MAIN_EXIT :
                    MKHorizontalExitPathKind.BRANCH;
            return Optional.of(new MKJigsawPieceMetadata.FloorRootExit(
                    topologyGroup.orElseThrow(),
                    connector.facing(),
                    pathKind.getSerializedName(),
                    openingProfileFromPool(connector.targetPool()).orElse("")
            ));
        }

        private static Optional<String> floorTopologyGroup(ResourceLocation pool) {
            String path = pool.getPath();
            if (path.contains("/masks/")) {
                return Optional.empty();
            }
            int floorPlanMarker = path.indexOf("floor_plan/");
            if (floorPlanMarker < 0) {
                return Optional.empty();
            }
            int topologyStart = floorPlanMarker + "floor_plan/".length();
            int topologyMarker = minPositive(
                    path.indexOf("/rooms/", topologyStart),
                    path.indexOf("/linear_runs/", topologyStart),
                    path.indexOf("/branch_caps/", topologyStart),
                    path.indexOf("/main_caps/", topologyStart),
                    path.indexOf("/main_cap_approaches/", topologyStart)
            );
            if (topologyMarker < 0) {
                return Optional.empty();
            }
            String topologyGroup = path.substring(topologyStart, topologyMarker);
            return topologyGroup.isBlank() ? Optional.empty() : Optional.of(topologyGroup);
        }

        private static Optional<String> openingProfileFromPool(ResourceLocation pool) {
            String path = pool.getPath();
            int marker = path.lastIndexOf('/');
            if (marker < 0 || marker == path.length() - 1) {
                return Optional.empty();
            }
            String opening = path.substring(marker + 1);
            return opening.isBlank() ? Optional.empty() : Optional.of(opening);
        }

        private static int minPositive(int... values) {
            int result = -1;
            for (int value : values) {
                if (value >= 0 && (result < 0 || value < result)) {
                    result = value;
                }
            }
            return result;
        }

        private static List<MKJigsawPieceMetadata.FloorClosableOpening> floorClosableOpenings(
                MKWorkspacePieceDefinition piece) {
            if (!exportsFloorClosableOpenings(piece)) {
                return List.of();
            }
            return piece.connectors().stream()
                    .filter(connector -> isFloorClosableOpening(piece, connector))
                    .filter(connector -> connector.facing().getAxis().isHorizontal())
                    .map(connector -> new MKJigsawPieceMetadata.FloorClosableOpening(
                            connector.facing(),
                            connector.relativePos().getX(),
                            connector.relativePos().getY(),
                            connector.relativePos().getZ(),
                            connector.openingWidth(),
                            connector.openingHeight(),
                            MKFloorConnectorPatch.closureDepth(piece, connector)
                    ))
                    .toList();
        }

        private static boolean isFloorClosableOpening(MKWorkspacePieceDefinition piece,
                                                      MKWorkspaceConnectorDefinition connector) {
            return connector.role() == MKConnectorRole.MAIN_BACK ||
                    connector.role() == MKConnectorRole.BRANCH;
        }

        private static boolean exportsFloorClosableOpenings(MKWorkspacePieceDefinition piece) {
            return isGeneratedFloorTopologyPiece(piece) || isFloorTopologyRootPiece(piece);
        }

        private static boolean isGeneratedFloorTopologyPiece(MKWorkspacePieceDefinition piece) {
            String pieceKind = piece.tags().get("tower_piece_kind");
            return "floor_plan_room".equals(pieceKind) || "floor_plan_linear_run".equals(pieceKind);
        }

        private static boolean isFloorTopologyRootPiece(MKWorkspacePieceDefinition piece) {
            Optional<MKWorkspaceVerticalStackSlot> slot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(
                    piece.tags().getOrDefault("workspace_topology_slot_id", piece.roleId()));
            if (slot.isEmpty() || !"floor".equals(slot.get().roleKind()) ||
                    slot.get() == MKWorkspaceVerticalStackSlot.ENTRY) {
                return false;
            }
            return piece.connectors().stream()
                    .filter(connector -> connector.role() == MKConnectorRole.MAIN_BACK ||
                            connector.role() == MKConnectorRole.BRANCH)
                    .filter(connector -> connector.facing().getAxis().isHorizontal())
                    .anyMatch(connector -> floorRootExit(connector).isPresent());
        }

        private static List<MKJigsawPieceMetadata.FloorLinkCandidate> floorLinkCandidates(Map<String, String> tags) {
            int count = parseInt(tags, MKFloorMaskVariantExporter.CLOSED_CONNECTOR_COUNT_TAG, 0);
            if (count <= 0) {
                return List.of();
            }
            ArrayList<MKJigsawPieceMetadata.FloorLinkCandidate> candidates = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                String prefix = MKFloorMaskVariantExporter.CLOSED_CONNECTOR_PREFIX + i + "_";
                String role = tags.getOrDefault(prefix + "role", "");
                if (!MKConnectorRole.LINK_CANDIDATE.getSerializedName().equals(role) &&
                        !MKConnectorRole.BRANCH.getSerializedName().equals(role) &&
                        !MKConnectorRole.MAIN_BACK.getSerializedName().equals(role)) {
                    continue;
                }
                Direction facing = Direction.byName(tags.getOrDefault(prefix + "facing", ""));
                if (facing == null || !facing.getAxis().isHorizontal()) {
                    continue;
                }
                candidates.add(new MKJigsawPieceMetadata.FloorLinkCandidate(
                        facing,
                        parseInt(tags, prefix + "x", 0),
                        parseInt(tags, prefix + "y", 0),
                        parseInt(tags, prefix + "z", 0),
                        parseInt(tags, prefix + "opening_width", 1),
                        parseInt(tags, prefix + "opening_height", 2),
                        parseInt(tags, prefix + "lateral_offset", 0),
                        parseInt(tags, prefix + "vertical_offset", 0),
                        parseInt(tags, prefix + "closure_depth", 2)
                ));
            }
            return List.copyOf(candidates);
        }

        private static int parseInt(Map<String, String> tags, String key, int fallback) {
            String value = tags.get(key);
            if (value == null || value.isBlank()) {
                return fallback;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }

        private ExportVerticalStackMetadata verticalStackMetadata() {
            return new ExportVerticalStackMetadata(verticalStackId, verticalStackSlot, minMainFloors, maxMainFloors,
                    minBasementFloors, maxBasementFloors, topCapApproachEnabled, basementEntryEnabled,
                    basementCapApproachEnabled);
        }

        private ExportFloorRuntimeMetadata floorRuntimeMetadata() {
            return new ExportFloorRuntimeMetadata(floorExitMask, foundationPolicy, floorBlock, wallBlock, ceilingBlock,
                    floorLinkCandidates, floorClosableOpenings, floorRootExits);
        }
    }

    private record ExportFloorRuntimeMetadata(
            String floorExitMask,
            MKWorkspaceFoundationPolicy foundationPolicy,
            ResourceLocation floorBlock,
            ResourceLocation wallBlock,
            ResourceLocation ceilingBlock,
            List<MKJigsawPieceMetadata.FloorLinkCandidate> floorLinkCandidates,
            List<MKJigsawPieceMetadata.FloorClosableOpening> floorClosableOpenings,
            List<MKJigsawPieceMetadata.FloorRootExit> floorRootExits
    ) {
        private static final MapCodec<ExportFloorRuntimeMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("floor_exit_mask", "").forGetter(ExportFloorRuntimeMetadata::floorExitMask),
                MKWorkspaceFoundationPolicy.CODEC.optionalFieldOf("foundation_policy", MKWorkspaceFoundationPolicy.none())
                        .forGetter(ExportFloorRuntimeMetadata::foundationPolicy),
                ResourceLocation.CODEC.optionalFieldOf("floor_block", MKWorkspaceMaterialPalette.defaultPalette().floorBlock())
                        .forGetter(ExportFloorRuntimeMetadata::floorBlock),
                ResourceLocation.CODEC.optionalFieldOf("wall_block", MKWorkspaceMaterialPalette.defaultPalette().wallBlock())
                        .forGetter(ExportFloorRuntimeMetadata::wallBlock),
                ResourceLocation.CODEC.optionalFieldOf("ceiling_block", MKWorkspaceMaterialPalette.defaultPalette().ceilingBlock())
                        .forGetter(ExportFloorRuntimeMetadata::ceilingBlock),
                MKJigsawPieceMetadata.FloorLinkCandidate.CODEC.listOf()
                        .optionalFieldOf("floor_link_candidates", List.of())
                        .forGetter(ExportFloorRuntimeMetadata::floorLinkCandidates),
                MKJigsawPieceMetadata.FloorClosableOpening.CODEC.listOf()
                        .optionalFieldOf("floor_closable_openings", List.of())
                        .forGetter(ExportFloorRuntimeMetadata::floorClosableOpenings),
                MKJigsawPieceMetadata.FloorRootExit.CODEC.listOf()
                        .optionalFieldOf("floor_root_exits", List.of())
                        .forGetter(ExportFloorRuntimeMetadata::floorRootExits)
        ).apply(instance, ExportFloorRuntimeMetadata::new));
    }

    private record ExportVerticalStackMetadata(
            String verticalStackId,
            String verticalStackSlot,
            int minMainFloors,
            int maxMainFloors,
            int minBasementFloors,
            int maxBasementFloors,
            boolean topCapApproachEnabled,
            boolean basementEntryEnabled,
            boolean basementCapApproachEnabled
    ) {
        private static final MapCodec<ExportVerticalStackMetadata> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("vertical_stack_id", "").forGetter(ExportVerticalStackMetadata::verticalStackId),
                Codec.STRING.optionalFieldOf("vertical_stack_slot", "").forGetter(ExportVerticalStackMetadata::verticalStackSlot),
                Codec.INT.optionalFieldOf("min_main_floors", 0).forGetter(ExportVerticalStackMetadata::minMainFloors),
                Codec.INT.optionalFieldOf("max_main_floors", 0).forGetter(ExportVerticalStackMetadata::maxMainFloors),
                Codec.INT.optionalFieldOf("min_basement_floors", 0).forGetter(ExportVerticalStackMetadata::minBasementFloors),
                Codec.INT.optionalFieldOf("max_basement_floors", 0).forGetter(ExportVerticalStackMetadata::maxBasementFloors),
                Codec.BOOL.optionalFieldOf("top_cap_approach_enabled", true)
                        .forGetter(ExportVerticalStackMetadata::topCapApproachEnabled),
                Codec.BOOL.optionalFieldOf("basement_entry_enabled", true)
                        .forGetter(ExportVerticalStackMetadata::basementEntryEnabled),
                Codec.BOOL.optionalFieldOf("basement_cap_approach_enabled", false)
                        .forGetter(ExportVerticalStackMetadata::basementCapApproachEnabled)
        ).apply(instance, ExportVerticalStackMetadata::new));
    }

    public record ExportStairConfig(MKWorkspaceStairMode mode, MKWorkspaceStairRiseType riseType, int stairWidth) {
        public static final Codec<ExportStairConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                stairModeCodec().fieldOf("mode").forGetter(ExportStairConfig::mode),
                stairRiseTypeCodec().fieldOf("rise_type").forGetter(ExportStairConfig::riseType),
                Codec.INT.fieldOf("stair_width").forGetter(ExportStairConfig::stairWidth)
        ).apply(instance, ExportStairConfig::new));
    }

    public record ExportTemplateGroup(String baseName, String roleId, List<String> pieces) {
        public static final Codec<ExportTemplateGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("base_name").forGetter(ExportTemplateGroup::baseName),
                Codec.STRING.fieldOf("role_id").forGetter(ExportTemplateGroup::roleId),
                Codec.STRING.listOf().fieldOf("pieces").forGetter(ExportTemplateGroup::pieces)
        ).apply(instance, ExportTemplateGroup::new));
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
            ResourceLocation incomingPool,
            String jigsawOrientation,
            String jigsawFinalState,
            String jigsawJoint
    ) {
        private static final ResourceLocation DEFAULT_EMPTY_POOL = ResourceLocation.parse("minecraft:empty");

        public static final Codec<ExportConnector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MKConnectorRole.CODEC.fieldOf("role").forGetter(ExportConnector::role),
                Codec.STRING.fieldOf("facing").forGetter(ExportConnector::facing),
                ExportBlockPos.CODEC.fieldOf("relative_pos").forGetter(ExportConnector::relativePos),
                Codec.INT.fieldOf("opening_width").forGetter(ExportConnector::openingWidth),
                Codec.INT.fieldOf("opening_height").forGetter(ExportConnector::openingHeight),
                Codec.INT.optionalFieldOf("lateral_offset", 0).forGetter(ExportConnector::lateralOffset),
                Codec.INT.optionalFieldOf("vertical_offset", 0).forGetter(ExportConnector::verticalOffset),
                ResourceLocation.CODEC.fieldOf("jigsaw_name").forGetter(ExportConnector::jigsawName),
                ResourceLocation.CODEC.fieldOf("jigsaw_target").forGetter(ExportConnector::jigsawTarget),
                ResourceLocation.CODEC.fieldOf("target_pool").forGetter(ExportConnector::targetPool),
                ResourceLocation.CODEC.optionalFieldOf("incoming_pool", DEFAULT_EMPTY_POOL)
                        .forGetter(ExportConnector::incomingPool),
                Codec.STRING.optionalFieldOf("jigsaw_orientation", "")
                        .forGetter(ExportConnector::jigsawOrientation),
                Codec.STRING.optionalFieldOf("jigsaw_final_state",
                                MKWorkspaceConnectorDefinition.DEFAULT_JIGSAW_FINAL_STATE)
                        .forGetter(ExportConnector::jigsawFinalState),
                Codec.STRING.optionalFieldOf("jigsaw_joint", MKWorkspaceConnectorDefinition.DEFAULT_JIGSAW_JOINT)
                        .forGetter(ExportConnector::jigsawJoint)
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
                    connector.incomingPool(),
                    connector.jigsawOrientation(),
                    connector.jigsawFinalState(),
                    connector.jigsawJoint()
            );
        }
    }

    public record ExportPiece(
            UUID pieceId,
            String pieceName,
            String baseName,
            String roleId,
            String plannerId,
            int variantIndex,
            String workspacePieceKind,
            String structureId,
            Optional<Integer> legacyShellMargin,
            Optional<Integer> legacyVerticalShellMargin,
            ExportDimensions effectiveDimensions,
            ExportPiecePlacement placement,
            List<ExportPositionRef> markerPositions,
            List<ExportPositionRef> generatedStairPositions,
            Map<String, String> tags,
            List<ExportConnector> connectors
    ) {
        private static final Codec<UUID> PIECE_UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);

        public static final Codec<ExportPiece> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PIECE_UUID_CODEC.fieldOf("piece_id").forGetter(ExportPiece::pieceId),
                Codec.STRING.fieldOf("piece_name").forGetter(ExportPiece::pieceName),
                Codec.STRING.fieldOf("base_name").forGetter(ExportPiece::baseName),
                Codec.STRING.fieldOf("role_id").forGetter(ExportPiece::roleId),
                Codec.STRING.optionalFieldOf("planner_id", "").forGetter(ExportPiece::plannerId),
                Codec.INT.fieldOf("variant_index").forGetter(ExportPiece::variantIndex),
                Codec.STRING.fieldOf("workspace_piece_kind").forGetter(ExportPiece::workspacePieceKind),
                Codec.STRING.fieldOf("structure_id").forGetter(ExportPiece::structureId),
                Codec.INT.optionalFieldOf("shell_margin").forGetter(ExportPiece::legacyShellMargin),
                Codec.INT.optionalFieldOf("vertical_shell_margin").forGetter(ExportPiece::legacyVerticalShellMargin),
                ExportDimensions.CODEC.fieldOf("effective_dimensions").forGetter(ExportPiece::effectiveDimensions),
                ExportPiecePlacement.CODEC.fieldOf("placement").forGetter(ExportPiece::placement),
                ExportPositionRef.CODEC.listOf().fieldOf("marker_positions").forGetter(ExportPiece::markerPositions),
                ExportPositionRef.CODEC.listOf().fieldOf("generated_stair_positions").forGetter(ExportPiece::generatedStairPositions),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("tags").forGetter(ExportPiece::tags),
                ExportConnector.CODEC.listOf().fieldOf("connectors").forGetter(ExportPiece::connectors)
        ).apply(instance, ExportPiece::new));

        public ExportPiece {
            plannerId = plannerId == null || plannerId.isBlank() ? roleId + "." + pieceName : plannerId;
        }

        public static ExportPiece from(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
            return new ExportPiece(
                    piece.pieceId(),
                    piece.pieceName(),
                    piece.tags().getOrDefault("workspace_base_name", piece.pieceName()),
                    piece.roleId(),
                    piece.plannerId().value(),
                    piece.variantIndex(),
                    piece.tags().getOrDefault("workspace_piece_kind", "instance"),
                    workspace.namespace() + ":" + workspace.structureName() + "/" + piece.pieceName(),
                    Optional.empty(),
                    Optional.empty(),
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

    private static String findStartBaseName(List<MKWorkspacePieceDefinition> pieces) {
        LinkedHashSet<String> startBaseNames = pieces.stream()
                .filter(piece -> !"template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance")))
                .filter(piece -> MKWorkspaceRuntimePieceInfo.fromTags(piece.tags()).map(MKWorkspaceRuntimePieceInfo::start).orElse(false))
                .map(piece -> piece.tags().getOrDefault("workspace_base_name", piece.pieceName()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (startBaseNames.isEmpty()) {
            throw new IllegalStateException("Workspace did not define a runtime start piece");
        }
        if (startBaseNames.size() > 1) {
            throw new IllegalStateException("Workspace defined multiple runtime start pieces " + startBaseNames);
        }
        return startBaseNames.getFirst();
    }

    private static String findPreviewStartBaseName(List<MKWorkspacePieceDefinition> pieces) {
        return pieces.stream()
                .filter(piece -> MKWorkspaceRuntimePieceInfo.fromTags(piece.tags())
                        .map(MKWorkspaceRuntimePieceInfo::start)
                        .orElse(false))
                .map(piece -> piece.tags().getOrDefault("workspace_base_name", piece.pieceName()))
                .findFirst()
                .orElse("");
    }

    private static List<ExportRuntimePool> buildRuntimePools(MKStructureWorkspace workspace,
                                                             List<MKWorkspacePieceDefinition> pieces) {
        return buildRuntimePools(workspace, pieces, false);
    }

    private static List<ExportRuntimePool> buildRuntimePools(MKStructureWorkspace workspace,
                                                             List<MKWorkspacePieceDefinition> pieces,
                                                             boolean allowTemplatePieces) {
        LinkedHashMap<ResourceLocation, LinkedHashSet<String>> childrenByPool = new LinkedHashMap<>();
        ResourceLocation plannerId = workspace.topologyProfile().plannerId();
        for (MKWorkspacePieceDefinition piece : pieces) {
            if (!allowTemplatePieces && "template".equals(piece.tags()
                    .getOrDefault("workspace_piece_kind", "instance"))) {
                continue;
            }
            String baseName = piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
            Optional<MKWorkspaceRuntimePieceInfo> runtimeInfo = MKWorkspaceRuntimePieceInfo.fromTags(piece.tags());
            for (MKWorkspaceConnectorDefinition connector : piece.connectors()) {
                if (connector.incomingPool().equals(EMPTY_POOL)) {
                    continue;
                }
                String runtimePoolPath = runtimePoolPath(workspace, connector.incomingPool());
                if (usesRuntimePathFilters(plannerId, runtimePoolPath)) {
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
                }
                if (!allowsRuntimePoolChild(plannerId, runtimePoolPath, piece.tags())) {
                    continue;
                }
                childrenByPool.computeIfAbsent(connector.incomingPool(), key -> new LinkedHashSet<>()).add(baseName);
                addFloorMaskPoolChild(childrenByPool, connector.incomingPool(), baseName, piece.tags());
            }
        }
        addInsertFamilyPools(workspace, pieces, childrenByPool, allowTemplatePieces);
        return childrenByPool.entrySet().stream()
                .map(entry -> new ExportRuntimePool(
                        derivePoolBaseName(workspace, entry.getKey()),
                        entry.getKey(),
                        List.copyOf(entry.getValue())
                ))
                .toList();
    }

    private static List<ExportRuntimePool> buildRuntimePoolsFromExportPieces(MKWorkspaceExportManifest manifest) {
        LinkedHashMap<ResourceLocation, LinkedHashSet<String>> childrenByPool = new LinkedHashMap<>();
        ResourceLocation plannerId = manifest.settings().topologyProfile().plannerId();
        for (ExportPiece piece : manifest.pieces()) {
            if ("template".equals(piece.workspacePieceKind())) {
                continue;
            }
            Optional<MKWorkspaceRuntimePieceInfo> runtimeInfo = MKWorkspaceRuntimePieceInfo.fromTags(piece.tags());
            for (ExportConnector connector : piece.connectors()) {
                if (connector.incomingPool().equals(EMPTY_POOL)) {
                    continue;
                }
                String runtimePoolPath = runtimePoolPath(manifest, connector.incomingPool());
                if (usesRuntimePathFilters(plannerId, runtimePoolPath)) {
                    if (isBranchCapRuntimePool(manifest, connector.incomingPool()) &&
                            !runtimeInfo.map(MKWorkspaceRuntimePieceInfo::branchCap).orElse(false)) {
                        continue;
                    }
                    if (isBranchRuntimePool(manifest, connector.incomingPool()) &&
                            runtimeInfo.map(MKWorkspaceRuntimePieceInfo::allowOnBranchPath).orElse(false) == false) {
                        continue;
                    }
                    if (!isBranchRuntimePool(manifest, connector.incomingPool()) &&
                            runtimeInfo.map(MKWorkspaceRuntimePieceInfo::allowOnMainPath).orElse(true) == false) {
                        continue;
                    }
                }
                if (!allowsRuntimePoolChild(plannerId, runtimePoolPath, piece.tags())) {
                    continue;
                }
                childrenByPool.computeIfAbsent(connector.incomingPool(), key -> new LinkedHashSet<>())
                        .add(piece.baseName());
                addFloorMaskPoolChild(childrenByPool, connector.incomingPool(), piece.baseName(), piece.tags());
            }
        }
        addInsertFamilyPools(manifest, childrenByPool);
        return childrenByPool.entrySet().stream()
                .map(entry -> new ExportRuntimePool(
                        derivePoolBaseName(manifest, entry.getKey()),
                        entry.getKey(),
                        List.copyOf(entry.getValue())
                ))
                .toList();
    }

    private static void addInsertFamilyPools(MKStructureWorkspace workspace,
                                             List<MKWorkspacePieceDefinition> pieces,
                                             LinkedHashMap<ResourceLocation, LinkedHashSet<String>> childrenByPool) {
        addInsertFamilyPools(workspace, pieces, childrenByPool, false);
    }

    private static void addInsertFamilyPools(MKStructureWorkspace workspace,
                                             List<MKWorkspacePieceDefinition> pieces,
                                             LinkedHashMap<ResourceLocation, LinkedHashSet<String>> childrenByPool,
                                             boolean allowTemplatePieces) {
        for (MKWorkspaceInsertFamilyDefinition insertFamily : workspace.insertFamilies()) {
            LinkedHashSet<String> childBaseNames = pieces.stream()
                    .filter(MKWorkspaceExportManifest::isRuntimeInsertFamilyVariant)
                    .filter(piece -> insertFamily.familyId().equals(piece.tags().get(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID)))
                    .filter(piece -> insertFamily.kind().getSerializedName().equals(piece.tags()
                            .getOrDefault(MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND,
                                    insertFamily.kind().getSerializedName())))
                    .map(piece -> piece.tags().getOrDefault("workspace_base_name", piece.pieceName()))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (!childBaseNames.isEmpty()) {
                childrenByPool.put(MKInsertFamilyPools.poolId(workspace.namespace(),
                        workspace.structureName(), insertFamily.familyId()), childBaseNames);
            }
        }
    }

    private static void addInsertFamilyPools(MKWorkspaceExportManifest manifest,
                                             LinkedHashMap<ResourceLocation, LinkedHashSet<String>> childrenByPool) {
        for (ExportInsertFamily insertFamily : manifest.settings().insertFamilies()) {
            LinkedHashSet<String> childBaseNames = manifest.pieces().stream()
                    .filter(MKWorkspaceExportManifest::isRuntimeInsertFamilyVariant)
                    .filter(piece -> insertFamily.familyId().equals(piece.tags()
                            .get(MKInsertFamilyPools.TAG_INSERT_FAMILY_ID)))
                    .filter(piece -> insertFamily.kind().getSerializedName().equals(piece.tags()
                            .getOrDefault(MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND,
                                    insertFamily.kind().getSerializedName())))
                    .map(ExportPiece::baseName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            if (!childBaseNames.isEmpty()) {
                childrenByPool.put(insertFamily.poolId(manifest), childBaseNames);
            }
        }
    }

    private static boolean isRuntimeInsertFamilyVariant(MKWorkspacePieceDefinition piece) {
        return effectiveVariantIndex(piece.tags(), piece.variantIndex()) > 0 &&
                !"template".equals(piece.tags().getOrDefault("workspace_piece_kind", "instance"));
    }

    private static boolean isRuntimeInsertFamilyVariant(ExportPiece piece) {
        return effectiveVariantIndex(piece.tags(), piece.variantIndex()) > 0 &&
                !"template".equals(piece.workspacePieceKind());
    }

    private static int effectiveVariantIndex(Map<String, String> tags, int fallback) {
        String value = tags.get("workspace_variant_index");
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static MKWorkspaceRuntimePieceInfo defaultInsertRuntimePieceInfo(MKWorkspacePieceDefinition piece) {
        return new MKWorkspaceRuntimePieceInfo(false, MKJigsawPieceRole.ROOM, 0, 0,
                true, true, true, false, defaultInsertTopologyGroup(piece.tags()), false, true);
    }

    private static String defaultInsertTopologyGroup(Map<String, String> tags) {
        String runtimeGroup = tags.get(MKWorkspaceRuntimePieceInfo.TOPOLOGY_GROUP_TAG);
        if (runtimeGroup != null && !runtimeGroup.isBlank()) {
            return runtimeGroup;
        }
        String workspaceGroup = tags.get("workspace_topology_group");
        if (workspaceGroup != null && !workspaceGroup.isBlank()) {
            return workspaceGroup;
        }
        String slotId = tags.getOrDefault("workspace_topology_slot_id", "");
        int separator = slotId.indexOf('.');
        if (separator > 0) {
            return slotId.substring(0, separator);
        }
        return "";
    }

    private static void addFloorMaskPoolChild(LinkedHashMap<ResourceLocation, LinkedHashSet<String>> childrenByPool,
                                              ResourceLocation basePool,
                                              String baseName,
                                              Map<String, String> tags) {
        String mask = tags.get(MKFloorMaskPools.FLOOR_MASK_TAG);
        if (mask == null || mask.isBlank()) {
            return;
        }
        childrenByPool.computeIfAbsent(MKFloorMaskPools.maskPool(basePool, mask),
                        key -> new LinkedHashSet<>())
                .add(baseName);
    }

    private static boolean usesRuntimePathFilters(ResourceLocation plannerId, String runtimePoolPath) {
        return !WALLED_KEEP_PLANNER_ID.equals(plannerId) || !runtimePoolPath.startsWith(KEEP_SLOT_POOL_PREFIX);
    }

    private static boolean allowsRuntimePoolChild(ResourceLocation plannerId, String runtimePoolPath,
                                                  Map<String, String> childTags) {
        if (!WALLED_KEEP_PLANNER_ID.equals(plannerId) || !isCourtyardContentSocketRuntimePool(runtimePoolPath)) {
            return true;
        }
        return courtyardContentFitsSocket(childTags);
    }

    private static boolean isCourtyardContentSocketRuntimePool(String runtimePoolPath) {
        return runtimePoolPath.startsWith(KEEP_SLOT_POOL_PREFIX + "keep/courtyard/") &&
                !runtimePoolPath.startsWith(KEEP_SLOT_POOL_PREFIX + "keep/courtyard/path/");
    }

    private static boolean courtyardContentFitsSocket(Map<String, String> tags) {
        if (!COURTYARD_CONTENT_KIND.equals(tags.getOrDefault(CONTENT_KIND_TAG, ""))) {
            return false;
        }
        int contentSize = parsePositiveInt(tags.get(CONTENT_SIZE_TAG));
        int socketMaxSize = parsePositiveInt(tags.getOrDefault(INSERT_SOCKET_MAX_SIZE_TAG,
                tags.get(COURTYARD_SOCKET_MAX_SIZE_TAG)));
        return contentSize > 0 && socketMaxSize > 0 && contentSize <= socketMaxSize;
    }

    private static int parsePositiveInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String derivePoolBaseName(MKStructureWorkspace workspace, ResourceLocation poolId) {
        String prefix = workspace.structureName() + "/";
        if (poolId.getNamespace().equals(workspace.namespace()) && poolId.getPath().startsWith(prefix)) {
            return poolId.getPath().substring(prefix.length());
        }
        return poolId.toString();
    }

    private static String derivePoolBaseName(MKWorkspaceExportManifest manifest, ResourceLocation poolId) {
        String prefix = manifest.structureName() + "/";
        if (poolId.getNamespace().equals(manifest.namespace()) && poolId.getPath().startsWith(prefix)) {
            return poolId.getPath().substring(prefix.length());
        }
        return poolId.toString();
    }

    private static boolean isBranchRuntimePool(MKStructureWorkspace workspace, ResourceLocation poolId) {
        return isBranchRuntimePoolPath(runtimePoolPath(workspace, poolId));
    }

    private static boolean isBranchRuntimePool(MKWorkspaceExportManifest manifest, ResourceLocation poolId) {
        return isBranchRuntimePoolPath(runtimePoolPath(manifest, poolId));
    }

    private static boolean isBranchRuntimePoolPath(String path) {
        return path.startsWith("linear_runs/branch/") ||
                path.startsWith("rooms/branch/") ||
                path.contains("/linear_runs/branch/") ||
                path.contains("/rooms/branch/") ||
                path.startsWith("branch_caps/");
    }

    private static boolean isBranchCapRuntimePool(MKStructureWorkspace workspace, ResourceLocation poolId) {
        return runtimePoolPath(workspace, poolId).startsWith("branch_caps/");
    }

    private static boolean isBranchCapRuntimePool(MKWorkspaceExportManifest manifest, ResourceLocation poolId) {
        return runtimePoolPath(manifest, poolId).startsWith("branch_caps/");
    }

    private static String runtimePoolPath(MKStructureWorkspace workspace, ResourceLocation poolId) {
        String prefix = workspace.structureName() + "/";
        return poolId.getNamespace().equals(workspace.namespace()) && poolId.getPath().startsWith(prefix) ?
                poolId.getPath().substring(prefix.length()) : poolId.getPath();
    }

    private static String runtimePoolPath(MKWorkspaceExportManifest manifest, ResourceLocation poolId) {
        String prefix = manifest.structureName() + "/";
        return poolId.getNamespace().equals(manifest.namespace()) && poolId.getPath().startsWith(prefix) ?
                poolId.getPath().substring(prefix.length()) : poolId.getPath();
    }
}
