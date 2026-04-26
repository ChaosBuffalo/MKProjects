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
    private final MKWorkspaceDimensions dimensions;
    private final MKWorkspaceMaterialPalette palette;
    private final MKWorkspaceStairAuthoringConfig stairConfig;
    private final MKVerticalAccessPlacement verticalAccessPlacement;
    private final MKWorkspaceVerticalAccessSpec verticalAccessSpec;
    private final List<MKTowerWorkspaceCategoryProfile> categoryProfiles;
    private final List<MKTowerWorkspaceFamilyDefinition> familyDefinitions;
    private final List<MKHorizontalOpeningProfile> openingProfiles;
    private final List<MKHallwayFamilyDefinition> hallwayFamilies;
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
                                List<MKTowerWorkspaceCategoryProfile> categoryProfiles,
                                List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
                                List<MKHorizontalOpeningProfile> openingProfiles,
                                List<MKHallwayFamilyDefinition> hallwayFamilies,
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
        this.verticalAccessSpec = verticalAccessSpec;
        this.categoryProfiles = List.copyOf(categoryProfiles);
        this.familyDefinitions = List.copyOf(familyDefinitions);
        this.openingProfiles = List.copyOf(openingProfiles);
        this.hallwayFamilies = List.copyOf(hallwayFamilies);
        this.shellMargin = shellMargin;
        this.exteriorAirMargin = exteriorAirMargin;
        this.previewMargin = previewMargin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pieces = List.copyOf(pieces);
    }

    public MKStructureWorkspace(UUID id, BlockPos anchor, String namespace, String structureName,
                                MKStructureFamilyType familyType, MKWorkspaceDimensions dimensions,
                                MKWorkspaceMaterialPalette palette, MKWorkspaceStairAuthoringConfig stairConfig,
                                MKVerticalAccessPlacement verticalAccessPlacement,
                                int shellMargin, int exteriorAirMargin,
                                int previewMargin,
                                long createdAt, long updatedAt, List<MKWorkspacePieceDefinition> pieces) {
        this(id, anchor, namespace, structureName, familyType, dimensions, palette, stairConfig, verticalAccessPlacement,
                shellMargin, exteriorAirMargin, previewMargin,
                MKWorkspaceVerticalAccessSpec.fromLegacy(dimensions, verticalAccessPlacement, stairConfig),
                MKTowerWorkspaceCategoryProfile.createDefaults(dimensions),
                MKTowerWorkspaceFamilyDefinition.createDefaults(dimensions),
                MKHorizontalOpeningProfile.createDefaults(dimensions),
                List.of(),
                createdAt, updatedAt, pieces);
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
                MKTowerWorkspaceCategoryProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()),
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
        MKWorkspaceVerticalAccessSpec resolvedVerticalAccessSpec = core.verticalAccessSpec().orElseGet(() ->
                MKWorkspaceVerticalAccessSpec.fromLegacy(core.dimensions(), core.verticalAccessPlacement(), core.stairConfig()));
        List<MKTowerWorkspaceCategoryProfile> resolvedCategoryProfiles = content.categoryProfiles().isEmpty() ?
                MKTowerWorkspaceCategoryProfile.createDefaults(core.dimensions()) : List.copyOf(content.categoryProfiles());
        List<MKTowerWorkspaceFamilyDefinition> resolvedFamilyDefinitions =
                MKTowerWorkspaceFamilyDefinition.normalize(content.familyDefinitions(), resolvedCategoryProfiles);
        List<MKHorizontalOpeningProfile> resolvedOpeningProfiles = content.openingProfiles().isEmpty() ?
                MKHorizontalOpeningProfile.createDefaults(core.dimensions()) : List.copyOf(content.openingProfiles());
        return new MKStructureWorkspace(
                id,
                anchor,
                namespace,
                structureName,
                core.familyType(),
                core.dimensions(),
                core.palette(),
                core.stairConfig(),
                core.verticalAccessPlacement(),
                core.shellMargin(),
                core.exteriorAirMargin(),
                core.previewMargin(),
                resolvedVerticalAccessSpec,
                resolvedCategoryProfiles,
                resolvedFamilyDefinitions,
                resolvedOpeningProfiles,
                List.copyOf(content.hallwayFamilies()),
                content.createdAt(),
                content.updatedAt(),
                List.copyOf(content.pieces())
        );
    }

    private SerializedWorkspaceCore serializedCore() {
        return new SerializedWorkspaceCore(
                familyType,
                dimensions,
                palette,
                stairConfig,
                verticalAccessPlacement,
                shellMargin,
                exteriorAirMargin,
                previewMargin,
                Optional.of(verticalAccessSpec)
        );
    }

    private SerializedWorkspaceContent serializedContent() {
        return new SerializedWorkspaceContent(
                categoryProfiles,
                familyDefinitions,
                openingProfiles,
                hallwayFamilies,
                createdAt,
                updatedAt,
                pieces
        );
    }

    private record SerializedWorkspaceCore(
            MKStructureFamilyType familyType,
            MKWorkspaceDimensions dimensions,
            MKWorkspaceMaterialPalette palette,
            MKWorkspaceStairAuthoringConfig stairConfig,
            MKVerticalAccessPlacement verticalAccessPlacement,
            int shellMargin,
            int exteriorAirMargin,
            int previewMargin,
            Optional<MKWorkspaceVerticalAccessSpec> verticalAccessSpec
    ) {
        private static final MapCodec<SerializedWorkspaceCore> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                MKWorkspaceCodecs.FAMILY_TYPE_CODEC.optionalFieldOf("familyType", MKStructureFamilyType.TOWER)
                        .forGetter(SerializedWorkspaceCore::familyType),
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
                MKWorkspaceVerticalAccessSpec.CODEC.optionalFieldOf("verticalAccessSpec").forGetter(SerializedWorkspaceCore::verticalAccessSpec)
        ).apply(instance, SerializedWorkspaceCore::new));
    }

    private record SerializedWorkspaceContent(
            List<MKTowerWorkspaceCategoryProfile> categoryProfiles,
            List<MKTowerWorkspaceFamilyDefinition> familyDefinitions,
            List<MKHorizontalOpeningProfile> openingProfiles,
            List<MKHallwayFamilyDefinition> hallwayFamilies,
            long createdAt,
            long updatedAt,
            List<MKWorkspacePieceDefinition> pieces
    ) {
        private static final MapCodec<SerializedWorkspaceContent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                MKTowerWorkspaceCategoryProfile.CODEC.listOf().optionalFieldOf("categoryProfiles", List.of())
                        .forGetter(SerializedWorkspaceContent::categoryProfiles),
                MKTowerWorkspaceFamilyDefinition.CODEC.listOf().optionalFieldOf("familyDefinitions", List.of())
                        .forGetter(SerializedWorkspaceContent::familyDefinitions),
                MKHorizontalOpeningProfile.CODEC.listOf().optionalFieldOf("openingProfiles", List.of())
                        .forGetter(SerializedWorkspaceContent::openingProfiles),
                MKHallwayFamilyDefinition.CODEC.listOf().optionalFieldOf("hallwayFamilies", List.of())
                        .forGetter(SerializedWorkspaceContent::hallwayFamilies),
                Codec.LONG.optionalFieldOf("createdAt", 0L).forGetter(SerializedWorkspaceContent::createdAt),
                Codec.LONG.optionalFieldOf("updatedAt", 0L).forGetter(SerializedWorkspaceContent::updatedAt),
                MKWorkspacePieceDefinition.CODEC.listOf().optionalFieldOf("pieces", List.of())
                        .forGetter(SerializedWorkspaceContent::pieces)
        ).apply(instance, SerializedWorkspaceContent::new));
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>(verticalAccessSpec.validate());
        java.util.Set<MKTowerWorkspaceCategory> categoriesWithProfiles = new java.util.LinkedHashSet<>();
        for (MKTowerWorkspaceCategoryProfile categoryProfile : categoryProfiles) {
            if (!categoriesWithProfiles.add(categoryProfile.category())) {
                errors.add("tower workspace category profile must be unique: " + categoryProfile.category().getSerializedName());
            }
            errors.addAll(categoryProfile.validate(verticalAccessSpec));
        }
        for (MKTowerWorkspaceFamilyDefinition familyDefinition : familyDefinitions) {
            Optional<MKTowerWorkspaceCategoryProfile> familyCategory = categoryProfile(familyDefinition.category());
            if (familyCategory.isEmpty()) {
                errors.add("family " + familyDefinition.baseName() + " references missing category profile " +
                        familyDefinition.category().getSerializedName());
                continue;
            }
            errors.addAll(familyDefinition.validate(familyDefinitions, familyCategory.get(), verticalAccessSpec));
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
            for (MKWorkspaceFamilyHorizontalExitDefinition exit : familyDefinition.horizontalExits()) {
                MKHorizontalOpeningProfile openingProfile = openingProfileById.get(exit.openingProfileId());
                if (openingProfile == null) {
                    errors.add("family " + familyDefinition.baseName() + " references missing opening profile " +
                            exit.openingProfileId());
                    continue;
                }
                if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.MAIN && !openingProfile.allowOnMainPath()) {
                    errors.add("family " + familyDefinition.baseName() + " cannot use opening profile " +
                            exit.openingProfileId() + " for a main exit because it is not main-path compatible");
                }
                if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH && !openingProfile.allowOnBranchPath()) {
                    errors.add("family " + familyDefinition.baseName() + " cannot use opening profile " +
                            exit.openingProfileId() + " for a branch exit because it is not branch-path compatible");
                }
            }
        }
        for (MKHallwayFamilyDefinition hallwayFamily : hallwayFamilies) {
            errors.addAll(hallwayFamily.validate(openingProfileIds));
            MKHorizontalOpeningProfile openingProfile = openingProfileById.get(hallwayFamily.openingProfileId());
            if (openingProfile != null) {
                if (hallwayFamily.allowOnMainPath() && !openingProfile.allowOnMainPath()) {
                    errors.add("hallway family " + hallwayFamily.hallwayId() +
                            " cannot allow main path when opening profile " + hallwayFamily.openingProfileId() +
                            " is branch-only");
                }
                if (hallwayFamily.allowOnBranchPath() && !openingProfile.allowOnBranchPath()) {
                    errors.add("hallway family " + hallwayFamily.hallwayId() +
                            " cannot allow branch path when opening profile " + hallwayFamily.openingProfileId() +
                            " is main-only");
                }
            }
        }
        Optional<MKTowerWorkspaceCategoryProfile> mainProfile = categoryProfile(MKTowerWorkspaceCategory.MAIN);
        if (mainProfile.isPresent()) {
            List<Integer> allowedBandHeights = MKWorkspaceDimensions.getAllowedBandHeights(
                    verticalAccessSpec.stairConfig(),
                    verticalAccessSpec.shaftSize(),
                    mainProfile.get().fullHeight(),
                    3,
                    16
            );
            for (MKTowerWorkspaceCategory category : List.of(
                    MKTowerWorkspaceCategory.MAIN,
                    MKTowerWorkspaceCategory.ENTRY,
                    MKTowerWorkspaceCategory.BASEMENT,
                    MKTowerWorkspaceCategory.TOP_CAP,
                    MKTowerWorkspaceCategory.BASEMENT_CAP)) {
                Optional<MKTowerWorkspaceCategoryProfile> profile = categoryProfile(category);
                if (profile.isPresent() && !allowedBandHeights.contains(profile.get().fullHeight())) {
                    errors.add(category.getSerializedName() + " full height must be one of " +
                            allowedBandHeights + " to stay in phase with main room height " +
                            mainProfile.get().fullHeight());
                }
            }
            int bandCap = verticalAccessSpec.getBandCapForRequestedHeight(mainProfile.get().fullHeight());
            for (MKHallwayFamilyDefinition hallwayFamily : hallwayFamilies) {
                int hallwayTopHeight = hallwayFamily.interiorHeight() + Math.abs(hallwayFamily.slopeDelta());
                if (hallwayTopHeight > bandCap) {
                    errors.add("hallway family " + hallwayFamily.hallwayId() + " height " + hallwayTopHeight +
                            " exceeds main vertical band cap " + bandCap);
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

    public MKStructureWorkspace withPieces(List<MKWorkspacePieceDefinition> newPieces) {
        return new MKStructureWorkspace(id, anchor, namespace, structureName, familyType, dimensions, palette,
                stairConfig, verticalAccessPlacement, shellMargin, exteriorAirMargin, previewMargin, verticalAccessSpec,
                categoryProfiles, familyDefinitions, openingProfiles, hallwayFamilies,
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

    public List<MKTowerWorkspaceCategoryProfile> categoryProfiles() {
        return categoryProfiles;
    }

    public Optional<MKTowerWorkspaceCategoryProfile> categoryProfile(MKTowerWorkspaceCategory category) {
        return categoryProfiles.stream().filter(profile -> profile.category() == category).findFirst();
    }

    public List<MKTowerWorkspaceFamilyDefinition> familyDefinitions() {
        return familyDefinitions;
    }

    public List<MKHorizontalOpeningProfile> openingProfiles() {
        return openingProfiles;
    }

    public List<MKHallwayFamilyDefinition> hallwayFamilies() {
        return hallwayFamilies;
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


