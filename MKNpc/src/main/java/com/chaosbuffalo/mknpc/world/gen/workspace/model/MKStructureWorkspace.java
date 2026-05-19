package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MKStructureWorkspace {
    public static final Codec<MKStructureWorkspace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MKWorkspaceCodecs.UUID_CODEC.fieldOf("id").forGetter(MKStructureWorkspace::id),
            MKWorkspaceCodecs.BLOCK_POS_CODEC.fieldOf("anchor").forGetter(MKStructureWorkspace::anchor),
            Codec.STRING.fieldOf("namespace").forGetter(MKStructureWorkspace::namespace),
            Codec.STRING.fieldOf("structureName").forGetter(MKStructureWorkspace::structureName),
            SerializedWorkspaceCore.CODEC.forGetter(MKStructureWorkspace::serializedCore),
            SerializedWorkspaceContent.CODEC.forGetter(MKStructureWorkspace::serializedContent)
    ).apply(instance, MKStructureWorkspace::fromSerializedData));
    public static final Codec<List<MKStructureWorkspace>> LIST_CODEC = CODEC.listOf();

    private final UUID id;
    private final BlockPos anchor;
    private final String namespace;
    private final String structureName;
    private final MKStructureFamilyType familyType;
    private final MKWorkspaceTopologyProfile topologyProfile;
    private final MKWorkspaceDimensions dimensions;
    private final MKWorkspaceMaterialPalette palette;
    private final MKWorkspaceStairAuthoringConfig stairConfig;
    private final MKVerticalAccessPlacement verticalAccessPlacement;
    private final MKWorkspaceVerticalAccessSpec verticalAccessSpec;
    private final List<MKTowerWorkspaceFamilyDefinition> familyDefinitions;
    private final List<MKHorizontalOpeningProfile> openingProfiles;
    private final List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies;
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
                                MKWorkspaceVerticalAccessSpec verticalAccessSpec,
                                List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
                                List<MKHorizontalOpeningProfile> openingProfiles,
                                List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies,
                                long createdAt, long updatedAt, List<MKWorkspacePieceDefinition> pieces) {
        this(id, anchor, namespace, structureName, familyType, MKWorkspaceTopologyProfile.tower(), dimensions, palette,
                stairConfig, verticalAccessPlacement, shellMargin, exteriorAirMargin, previewMargin, verticalAccessSpec,
                familyDefinitions, openingProfiles, linearRunFamilies,
                createdAt, updatedAt, pieces);
    }

    public MKStructureWorkspace(UUID id, BlockPos anchor, String namespace, String structureName,
                                MKStructureFamilyType familyType, MKWorkspaceTopologyProfile topologyProfile,
                                MKWorkspaceDimensions dimensions,
                                MKWorkspaceMaterialPalette palette, MKWorkspaceStairAuthoringConfig stairConfig,
                                MKVerticalAccessPlacement verticalAccessPlacement,
                                int shellMargin, int exteriorAirMargin,
                                int previewMargin,
                                MKWorkspaceVerticalAccessSpec verticalAccessSpec,
                                List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
                                List<MKHorizontalOpeningProfile> openingProfiles,
                                List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies,
                                long createdAt, long updatedAt, List<MKWorkspacePieceDefinition> pieces) {
        this.id = id;
        this.anchor = anchor;
        this.namespace = namespace;
        this.structureName = structureName;
        this.familyType = familyType;
        this.topologyProfile = topologyProfile == null ? MKWorkspaceTopologyProfile.tower() : topologyProfile;
        this.dimensions = dimensions;
        this.palette = palette;
        this.stairConfig = stairConfig;
        this.verticalAccessPlacement = verticalAccessPlacement;
        this.verticalAccessSpec = verticalAccessSpec;
        this.familyDefinitions = List.copyOf(familyDefinitions);
        this.openingProfiles = List.copyOf(openingProfiles);
        this.linearRunFamilies = List.copyOf(linearRunFamilies);
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
                MKWorkspaceVerticalAccessSpec.defaultSpec(),
                MKTowerWorkspaceFamilyDefinition.createDefaults(MKWorkspaceDimensions.defaultDimensions()),
                MKHorizontalOpeningProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()),
                List.of(),
                now,
                now,
                List.of()
        );
    }

    public static MKStructureWorkspace fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "structure workspace");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "structure workspace");
    }

    private static MKStructureWorkspace fromSerializedData(UUID id, BlockPos anchor, String namespace, String structureName,
                                                           SerializedWorkspaceCore core,
                                                           SerializedWorkspaceContent content) {
        MKWorkspaceVerticalAccessSpec resolvedVerticalAccessSpec = core.verticalAccessSpec();
        List<MKTowerWorkspaceFamilyDefinition> resolvedFamilyDefinitions = List.copyOf(content.familyDefinitions());
        List<MKHorizontalOpeningProfile> resolvedOpeningProfiles = content.openingProfiles().isEmpty() ?
                MKHorizontalOpeningProfile.createDefaults(core.dimensions()) : List.copyOf(content.openingProfiles());
        List<MKWorkspaceLinearRunFamilyDefinition> resolvedLinearRunFamilies = List.copyOf(content.linearRunFamilies());
        return new MKStructureWorkspace(
                id,
                anchor,
                namespace,
                structureName,
                core.familyType(),
                core.topologyProfile(),
                core.dimensions(),
                core.palette(),
                core.stairConfig(),
                core.verticalAccessPlacement(),
                core.shellMargin(),
                core.exteriorAirMargin(),
                core.previewMargin(),
                resolvedVerticalAccessSpec,
                resolvedFamilyDefinitions,
                resolvedOpeningProfiles,
                resolvedLinearRunFamilies,
                content.createdAt(),
                content.updatedAt(),
                List.copyOf(content.pieces())
        );
    }

    private SerializedWorkspaceCore serializedCore() {
        return new SerializedWorkspaceCore(
                familyType,
                topologyProfile,
                dimensions,
                palette,
                stairConfig,
                verticalAccessPlacement,
                shellMargin,
                exteriorAirMargin,
                previewMargin,
                verticalAccessSpec
        );
    }

    private SerializedWorkspaceContent serializedContent() {
        return new SerializedWorkspaceContent(
                familyDefinitions,
                openingProfiles,
                linearRunFamilies,
                createdAt,
                updatedAt,
                pieces
        );
    }

    private record SerializedWorkspaceCore(
            MKStructureFamilyType familyType,
            MKWorkspaceTopologyProfile topologyProfile,
            MKWorkspaceDimensions dimensions,
            MKWorkspaceMaterialPalette palette,
            MKWorkspaceStairAuthoringConfig stairConfig,
            MKVerticalAccessPlacement verticalAccessPlacement,
            int shellMargin,
            int exteriorAirMargin,
            int previewMargin,
            MKWorkspaceVerticalAccessSpec verticalAccessSpec
    ) {
        private static final MapCodec<SerializedWorkspaceCore> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                MKWorkspaceCodecs.FAMILY_TYPE_CODEC.optionalFieldOf("familyType", MKStructureFamilyType.TOWER)
                        .forGetter(SerializedWorkspaceCore::familyType),
                MKWorkspaceTopologyProfile.CODEC.optionalFieldOf("topologyProfile", MKWorkspaceTopologyProfile.tower())
                        .forGetter(SerializedWorkspaceCore::topologyProfile),
                MKWorkspaceDimensions.CODEC.fieldOf("dimensions").forGetter(SerializedWorkspaceCore::dimensions),
                MKWorkspaceMaterialPalette.CODEC.optionalFieldOf("palette", MKWorkspaceMaterialPalette.defaultPalette())
                        .forGetter(SerializedWorkspaceCore::palette),
                MKWorkspaceStairAuthoringConfig.CODEC.optionalFieldOf("stairConfig", MKWorkspaceStairAuthoringConfig.defaultConfig())
                        .forGetter(SerializedWorkspaceCore::stairConfig),
                MKWorkspaceCodecs.VERTICAL_ACCESS_PLACEMENT_CODEC.optionalFieldOf("verticalAccessPlacement", MKVerticalAccessPlacement.CENTER)
                        .forGetter(SerializedWorkspaceCore::verticalAccessPlacement),
                Codec.INT.optionalFieldOf("shellMargin", 1).forGetter(SerializedWorkspaceCore::shellMargin),
                Codec.INT.optionalFieldOf("exteriorAirMargin", 2).forGetter(SerializedWorkspaceCore::exteriorAirMargin),
                Codec.INT.optionalFieldOf("previewMargin", 4).forGetter(SerializedWorkspaceCore::previewMargin),
                MKWorkspaceVerticalAccessSpec.CODEC.fieldOf("verticalAccessSpec")
                        .forGetter(SerializedWorkspaceCore::verticalAccessSpec)
        ).apply(instance, SerializedWorkspaceCore::new));
    }

    private record SerializedWorkspaceContent(
            List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
            List<MKHorizontalOpeningProfile> openingProfiles,
            List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies,
            long createdAt,
            long updatedAt,
            List<MKWorkspacePieceDefinition> pieces
    ) {
        private static final MapCodec<SerializedWorkspaceContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                MKTowerWorkspaceFamilyDefinition.CODEC.listOf().optionalFieldOf("familyDefinitions", List.of())
                        .forGetter(SerializedWorkspaceContent::familyDefinitions),
                MKHorizontalOpeningProfile.CODEC.listOf().optionalFieldOf("openingProfiles", List.of())
                        .forGetter(SerializedWorkspaceContent::openingProfiles),
                MKWorkspaceLinearRunFamilyDefinition.CODEC.listOf().optionalFieldOf("linearRunFamilies", List.of())
                        .forGetter(SerializedWorkspaceContent::linearRunFamilies),
                Codec.LONG.optionalFieldOf("createdAt", 0L).forGetter(SerializedWorkspaceContent::createdAt),
                Codec.LONG.optionalFieldOf("updatedAt", 0L).forGetter(SerializedWorkspaceContent::updatedAt),
                MKWorkspacePieceDefinition.CODEC.listOf().optionalFieldOf("pieces", List.of())
                        .forGetter(SerializedWorkspaceContent::pieces)
        ).apply(instance, SerializedWorkspaceContent::new));
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>(verticalAccessSpec.validate());
        for (MKWorkspaceTowerStackSettings settings : topologyProfile.towerStackSettings()) {
            errors.addAll(MKWorkspaceTowerStackFloorCounts.validate(settings).stream()
                    .map(error -> "tower stack " + settings.stackId() + " " + error)
                    .toList());
        }
        for (MKWorkspaceTowerStackSettings settings : topologyProfile.towerStackSettings()) {
            MKWorkspaceVerticalAccessSpec stackSpec = new MKWorkspaceVerticalAccessSpec(
                    settings.shaftSize(), settings.verticalAccessPlacement(), settings.stairConfig());
            for (String error : stackSpec.validate()) {
                errors.add("tower stack " + settings.stackId() + " " + error);
            }
            for (String error : settings.foundationPolicy().validate("tower stack " + settings.stackId())) {
                errors.add(error);
            }
        }
        for (MKTowerWorkspaceFamilyDefinition familyDefinition : familyDefinitions) {
            Optional<Integer> familyMaxHeight = maxRoomHeightForFamily(familyDefinition);
            if (familyMaxHeight.isEmpty()) {
                errors.add("family " + familyDefinition.baseName() + " references missing topology height for slot " +
                        familyDefinition.topologySlotId());
                continue;
            }
            errors.addAll(familyDefinition.validate(familyDefinitions, familyMaxHeight.get(),
                    verticalAccessSpecForFamily(familyDefinition), resolveFamilySettings(familyDefinition)));
        }
        java.util.Set<String> openingProfileIds = new java.util.LinkedHashSet<>();
        java.util.Map<String, MKHorizontalOpeningProfile> openingProfileById = new java.util.LinkedHashMap<>();
        for (MKHorizontalOpeningProfile openingProfile : openingProfiles) {
            errors.addAll(openingProfile.validate());
            if (!openingProfileIds.add(openingProfile.profileId())) {
                errors.add("horizontal opening profile id must be unique: " + openingProfile.profileId());
            }
            openingProfileById.put(openingProfile.profileId(), openingProfile);
        }
        for (MKTowerWorkspaceFamilyDefinition familyDefinition : familyDefinitions) {
            MKWorkspaceResolvedFamilySettings resolvedFamily = resolveFamilySettings(familyDefinition);
            for (MKWorkspaceFamilyHorizontalExitDefinition exit : familyDefinition.horizontalExits()) {
                if (exit.isVerticalAccess()) {
                    continue;
                }
                MKHorizontalOpeningProfile openingProfile = openingProfileById.get(exit.openingProfileId());
                if (openingProfile == null) {
                    errors.add("family " + familyDefinition.baseName() + " references missing opening profile " +
                            exit.openingProfileId());
                    continue;
                }
                if (exit.pathKind().usesMainPath() && !openingProfile.allowOnMainPath()) {
                    errors.add("family " + familyDefinition.baseName() + " cannot use opening profile " +
                            exit.openingProfileId() + " for a main-path exit because it is not main-path compatible");
                }
                if ((exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH ||
                        exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY) &&
                        !openingProfile.allowOnBranchPath()) {
                    errors.add("family " + familyDefinition.baseName() + " cannot use opening profile " +
                            exit.openingProfileId() + " for a branch exit because it is not branch-path compatible");
                }
                if ((exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY ||
                        exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY) &&
                        exit.connectionMode() == MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION) {
                    errors.add("family " + familyDefinition.baseName() + " " +
                            exit.pathKind().getSerializedName() + " must place a connector");
                }
                if (exit.connectionMode() == MKWorkspaceHorizontalExitConnectionMode.LINEAR_RUN &&
                        exit.pathKind() != MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY &&
                        exit.pathKind() != MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY) {
                    boolean hasCompatibleLinearRun = linearRunFamilies.stream().anyMatch(linearRun ->
                            linearRun.openingProfileId().equals(exit.openingProfileId()) &&
                                    (exit.pathKind().usesMainPath() ? linearRun.allowOnMainPath() : linearRun.allowOnBranchPath()));
                    if (!hasCompatibleLinearRun) {
                        errors.add("family " + familyDefinition.baseName() + " uses linear-run exit " +
                                exit.direction().getSerializedName() + " with opening profile " + exit.openingProfileId() +
                                " but no compatible linear run family exists");
                    }
                }
                if (!exit.direction().getAxis().isVertical()) {
                    int sideLength = exit.direction() == net.minecraft.core.Direction.NORTH ||
                            exit.direction() == net.minecraft.core.Direction.SOUTH ?
                            resolvedFamily.roomWidth() : resolvedFamily.roomLength();
                    int halfOpening = openingProfile.openingWidth() / 2;
                    int center = sideLength / 2;
                    int minSideOffset = halfOpening - center;
                    int maxSideOffset = (sideLength - 1 - halfOpening) - center;
                    if (minSideOffset > maxSideOffset) {
                        errors.add("family " + familyDefinition.baseName() + " horizontal exit " +
                                exit.direction().getSerializedName() + " opening width " +
                                openingProfile.openingWidth() + " exceeds side length " + sideLength);
                    } else if (exit.sideOffset() < minSideOffset || exit.sideOffset() > maxSideOffset) {
                        errors.add("family " + familyDefinition.baseName() + " horizontal exit " +
                                exit.direction().getSerializedName() + " side offset " + exit.sideOffset() +
                                " must be between " + minSideOffset + " and " + maxSideOffset);
                    }
                    int maxVerticalOffset = resolvedFamily.roomHeight() - openingProfile.openingHeight();
                    if (maxVerticalOffset < 0) {
                        errors.add("family " + familyDefinition.baseName() + " horizontal exit " +
                                exit.direction().getSerializedName() + " opening height " +
                                openingProfile.openingHeight() + " exceeds room height " + resolvedFamily.roomHeight());
                    } else if (exit.verticalOffset() < 0 || exit.verticalOffset() > maxVerticalOffset) {
                        errors.add("family " + familyDefinition.baseName() + " horizontal exit " +
                                exit.direction().getSerializedName() + " vertical offset " + exit.verticalOffset() +
                                " must be between 0 and " + maxVerticalOffset);
                    }
                }
            }
        }
        for (MKWorkspaceLinearRunFamilyDefinition linearRunFamily : linearRunFamilies) {
            errors.addAll(linearRunFamily.validate(openingProfileIds));
            MKHorizontalOpeningProfile openingProfile = openingProfileById.get(linearRunFamily.openingProfileId());
            if (openingProfile != null) {
                if (linearRunFamily.allowOnMainPath() && !openingProfile.allowOnMainPath()) {
                    errors.add("linear run family " + linearRunFamily.linearRunId() +
                            " cannot allow main path when opening profile " + linearRunFamily.openingProfileId() +
                            " is branch-only");
                }
                if (linearRunFamily.allowOnBranchPath() && !openingProfile.allowOnBranchPath()) {
                    errors.add("linear run family " + linearRunFamily.linearRunId() +
                            " cannot allow branch path when opening profile " + linearRunFamily.openingProfileId() +
                            " is main-only");
                }
            }
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

    private Optional<Integer> maxRoomHeightForFamily(MKTowerWorkspaceFamilyDefinition familyDefinition) {
        Optional<MKWorkspaceTowerStackSettings> stackSettings = towerStackSettingsForFamily(familyDefinition);
        if (stackSettings.isPresent()) {
            return Optional.of(stackSettings.get().height());
        }
        int fallbackHeight = defaultHeightForTopologyGroup(
                MKWorkspaceTopologySlotMetadata.fromFamily(familyDefinition).topologyGroupId());
        return Optional.of(Math.max(fallbackHeight, familyDefinition.roomHeight()));
    }

    private int defaultHeightForTopologyGroup(String topologyGroupId) {
        return switch (topologyGroupId) {
            case "entry" -> dimensions.entranceHeight();
            case "basement", "basement_cap" -> dimensions.basementHeight();
            default -> dimensions.roomHeight();
        };
    }

    private MKWorkspaceVerticalAccessSpec verticalAccessSpecForFamily(MKTowerWorkspaceFamilyDefinition familyDefinition) {
        String stackId = towerStackIdForFamily(familyDefinition.topologySlotId());
        if (stackId.isBlank()) {
            return verticalAccessSpec;
        }
        MKWorkspaceTowerStackSettings settings = topologyProfile.towerStackSettings(stackId)
                .orElse(null);
        if (settings == null) {
            return verticalAccessSpec;
        }
        return new MKWorkspaceVerticalAccessSpec(
                settings.shaftSize(),
                settings.verticalAccessPlacement(),
                settings.stairConfig()
        );
    }

    public MKWorkspaceStairAuthoringConfig stairConfigForPiece(MKWorkspacePieceDefinition piece) {
        String stackId = piece.tags().getOrDefault("workspace_tower_stack_id", "");
        if (stackId.isBlank()) {
            return stairConfig;
        }
        return topologyProfile.towerStackSettings(stackId)
                .map(MKWorkspaceTowerStackSettings::stairConfig)
                .orElse(stairConfig);
    }

    public MKWorkspaceResolvedFamilySettings resolveFamilySettings(MKTowerWorkspaceFamilyDefinition familyDefinition) {
        return MKWorkspaceResolvedFamilySettings.from(this, familyDefinition);
    }

    public Optional<MKWorkspaceTowerStackSettings> towerStackSettingsForFamily(
            MKTowerWorkspaceFamilyDefinition familyDefinition) {
        String stackId = towerStackIdForFamily(familyDefinition.topologySlotId());
        if (stackId.isBlank()) {
            return Optional.empty();
        }
        return topologyProfile.towerStackSettings(stackId);
    }

    private String towerStackIdForFamily(String topologySlotId) {
        if (MKWorkspaceTopologyProfile.TOWER_PROFILE_TYPE.equals(topologyProfile.profileType())) {
            return MKTowerWorkspaceStackSlot.stackIdForTopologySlot(topologySlotId)
                    .filter(stackId -> stackId.equals("tower.primary") || stackId.equals("tower"))
                    .map(stackId -> "tower.primary")
                    .orElse("");
        }
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PROFILE_TYPE.equals(topologyProfile.profileType())) {
            return MKTowerWorkspaceStackSlot.stackIdForTopologySlot(topologySlotId)
                    .filter(stackId -> stackId.equals("keep.center") ||
                            stackId.equals("keep.corner.shared") ||
                            stackId.equals("keep.corner.north_west") ||
                            stackId.equals("keep.corner.north_east") ||
                            stackId.equals("keep.corner.south_east") ||
                            stackId.equals("keep.corner.south_west"))
                    .orElse("");
        }
        return "";
    }

    public MKStructureWorkspace withPieces(List<MKWorkspacePieceDefinition> newPieces) {
        return new MKStructureWorkspace(id, anchor, namespace, structureName, familyType, topologyProfile, dimensions, palette,
                stairConfig, verticalAccessPlacement, shellMargin, exteriorAirMargin, previewMargin, verticalAccessSpec,
                familyDefinitions, openingProfiles, linearRunFamilies,
                createdAt, System.currentTimeMillis(), newPieces);
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

    public MKWorkspaceTopologyProfile topologyProfile() {
        return topologyProfile;
    }

    public MKWorkspaceTopologyPathSettings topologyPathSettings(String topologyGroupId) {
        return topologyProfile.pathSettingsOrDefault(topologyGroupId);
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

    public MKWorkspaceVerticalAccessSpec verticalAccessSpec() {
        return verticalAccessSpec;
    }

    public List<MKTowerWorkspaceFamilyDefinition> familyDefinitions() {
        return familyDefinitions;
    }

    public List<MKHorizontalOpeningProfile> openingProfiles() {
        return openingProfiles;
    }

    public List<MKWorkspaceLinearRunFamilyDefinition> linearRunFamilies() {
        return linearRunFamilies;
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


