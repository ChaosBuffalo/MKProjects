package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                MKTowerWorkspaceCategoryProfile.createDefaults(dimensions,
                        MKWorkspaceVerticalAccessSpec.fromLegacy(dimensions, verticalAccessPlacement, stairConfig)),
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                MKHorizontalOpeningProfile.createDefaults(MKTowerWorkspaceCategoryProfile.createDefaults(dimensions,
                        MKWorkspaceVerticalAccessSpec.fromLegacy(dimensions, verticalAccessPlacement, stairConfig))),
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
                MKTowerWorkspaceCategoryProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions(),
                        MKWorkspaceVerticalAccessSpec.defaultSpec()),
                MKTowerWorkspaceFamilyDefinition.createDefaults(),
                MKHorizontalOpeningProfile.createDefaults(MKTowerWorkspaceCategoryProfile.createDefaults(
                        MKWorkspaceDimensions.defaultDimensions(), MKWorkspaceVerticalAccessSpec.defaultSpec())),
                List.of(),
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
        MKWorkspaceDimensions dimensions = MKWorkspaceDimensions.fromTag(tag.getCompound("dimensions"));
        MKWorkspaceStairAuthoringConfig stairConfig = tag.contains("stairConfig") ?
                MKWorkspaceStairAuthoringConfig.fromTag(tag.getCompound("stairConfig")) :
                MKWorkspaceStairAuthoringConfig.defaultConfig();
        MKVerticalAccessPlacement placement = MKVerticalAccessPlacement.fromSerializedName(tag.getString("verticalAccessPlacement"));
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = tag.contains("verticalAccessSpec") ?
                MKWorkspaceVerticalAccessSpec.fromTag(tag.getCompound("verticalAccessSpec")) :
                MKWorkspaceVerticalAccessSpec.fromLegacy(dimensions, placement, stairConfig);
        List<MKTowerWorkspaceCategoryProfile> categoryProfiles = new ArrayList<>();
        if (tag.contains("categoryProfiles", Tag.TAG_LIST)) {
            for (Tag profileTag : tag.getList("categoryProfiles", Tag.TAG_COMPOUND)) {
                categoryProfiles.add(MKTowerWorkspaceCategoryProfile.fromTag((CompoundTag) profileTag));
            }
        }
        if (categoryProfiles.isEmpty()) {
            categoryProfiles = MKTowerWorkspaceCategoryProfile.createDefaults(dimensions, verticalAccessSpec);
        }
        List<MKTowerWorkspaceFamilyDefinition> familyDefinitions = new ArrayList<>();
        if (tag.contains("familyDefinitions", Tag.TAG_LIST)) {
            for (Tag familyTag : tag.getList("familyDefinitions", Tag.TAG_COMPOUND)) {
                familyDefinitions.add(MKTowerWorkspaceFamilyDefinition.fromTag((CompoundTag) familyTag));
            }
        }
        familyDefinitions = MKTowerWorkspaceFamilyDefinition.normalize(familyDefinitions);
        List<MKHorizontalOpeningProfile> openingProfiles = new ArrayList<>();
        if (tag.contains("openingProfiles", Tag.TAG_LIST)) {
            for (Tag openingTag : tag.getList("openingProfiles", Tag.TAG_COMPOUND)) {
                openingProfiles.add(MKHorizontalOpeningProfile.fromTag((CompoundTag) openingTag));
            }
        }
        if (openingProfiles.isEmpty()) {
            openingProfiles = MKHorizontalOpeningProfile.createDefaults(categoryProfiles);
        }
        List<MKHallwayFamilyDefinition> hallwayFamilies = new ArrayList<>();
        if (tag.contains("hallwayFamilies", Tag.TAG_LIST)) {
            for (Tag hallwayTag : tag.getList("hallwayFamilies", Tag.TAG_COMPOUND)) {
                hallwayFamilies.add(MKHallwayFamilyDefinition.fromTag((CompoundTag) hallwayTag));
            }
        }
        return new MKStructureWorkspace(
                tag.getUUID("id"),
                MKWorkspaceNbtUtil.blockPosFromTag(tag.getCompound("anchor")),
                tag.getString("namespace"),
                tag.getString("structureName"),
                MKStructureFamilyType.fromSerializedName(tag.getString("familyType")),
                dimensions,
                MKWorkspaceMaterialPalette.fromTag(tag.getCompound("palette")),
                stairConfig,
                placement,
                tag.getInt("shellMargin"),
                tag.contains("exteriorAirMargin") ? tag.getInt("exteriorAirMargin") : 2,
                tag.getInt("previewMargin"),
                verticalAccessSpec,
                categoryProfiles,
                familyDefinitions,
                openingProfiles,
                hallwayFamilies,
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
        tag.put("verticalAccessSpec", verticalAccessSpec.toTag());
        tag.putInt("shellMargin", shellMargin);
        tag.putInt("exteriorAirMargin", exteriorAirMargin);
        tag.putInt("previewMargin", previewMargin);
        tag.putLong("createdAt", createdAt);
        tag.putLong("updatedAt", updatedAt);
        ListTag categoryProfilesTag = new ListTag();
        for (MKTowerWorkspaceCategoryProfile categoryProfile : categoryProfiles) {
            categoryProfilesTag.add(categoryProfile.toTag());
        }
        tag.put("categoryProfiles", categoryProfilesTag);
        ListTag familyDefinitionsTag = new ListTag();
        for (MKTowerWorkspaceFamilyDefinition familyDefinition : familyDefinitions) {
            familyDefinitionsTag.add(familyDefinition.toTag());
        }
        tag.put("familyDefinitions", familyDefinitionsTag);
        ListTag openingProfilesTag = new ListTag();
        for (MKHorizontalOpeningProfile openingProfile : openingProfiles) {
            openingProfilesTag.add(openingProfile.toTag());
        }
        tag.put("openingProfiles", openingProfilesTag);
        ListTag hallwayFamiliesTag = new ListTag();
        for (MKHallwayFamilyDefinition hallwayFamily : hallwayFamilies) {
            hallwayFamiliesTag.add(hallwayFamily.toTag());
        }
        tag.put("hallwayFamilies", hallwayFamiliesTag);
        ListTag piecesTag = new ListTag();
        for (MKWorkspacePieceDefinition piece : pieces) {
            piecesTag.add(piece.toTag());
        }
        tag.put("pieces", piecesTag);
        return tag;
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
            errors.addAll(familyDefinition.validate(familyDefinitions));
            if (categoryProfile(familyDefinition.category()).isEmpty()) {
                errors.add("family " + familyDefinition.baseName() + " references missing category profile " +
                        familyDefinition.category().getSerializedName());
            }
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
            int bandCap = verticalAccessSpec.getBandCapForReusableHeight(mainProfile.get().defaultHeight());
            List<Integer> allowedEntranceHeights = MKWorkspaceDimensions.getAllowedEntranceHeights(
                    verticalAccessSpec.stairConfig(),
                    verticalAccessSpec.shaftSize(),
                    mainProfile.get().defaultHeight(),
                    3,
                    4
            );
            Optional<MKTowerWorkspaceCategoryProfile> entryProfile = categoryProfile(MKTowerWorkspaceCategory.ENTRY);
            if (entryProfile.isPresent() && entryProfile.get().supportsVerticalAccess() &&
                    !allowedEntranceHeights.contains(entryProfile.get().defaultHeight())) {
                errors.add("entry default height must be one of " + allowedEntranceHeights +
                        " to stay in phase with main room height " + mainProfile.get().defaultHeight());
            }
            Optional<MKTowerWorkspaceCategoryProfile> basementProfile = categoryProfile(MKTowerWorkspaceCategory.BASEMENT);
            if (basementProfile.isPresent() && basementProfile.get().supportsVerticalAccess() &&
                    !allowedEntranceHeights.contains(basementProfile.get().defaultHeight())) {
                errors.add("basement default height must be one of " + allowedEntranceHeights +
                        " to stay in phase with main room height " + mainProfile.get().defaultHeight());
            }
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

