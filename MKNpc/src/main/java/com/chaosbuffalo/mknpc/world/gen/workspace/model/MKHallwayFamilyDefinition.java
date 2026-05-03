package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MKHallwayFamilyDefinition implements MKWorkspacePaletteFamily {
    public static final Codec<MKHallwayFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("hallwayId").forGetter(MKHallwayFamilyDefinition::hallwayId),
            Codec.STRING.fieldOf("openingProfileId").forGetter(MKHallwayFamilyDefinition::openingProfileId),
            Codec.INT.fieldOf("length").forGetter(MKHallwayFamilyDefinition::length),
            Codec.INT.fieldOf("interiorWidth").forGetter(MKHallwayFamilyDefinition::interiorWidth),
            Codec.INT.fieldOf("interiorHeight").forGetter(MKHallwayFamilyDefinition::interiorHeight),
            Codec.INT.optionalFieldOf("slopeDelta", 0).forGetter(MKHallwayFamilyDefinition::slopeDelta),
            Codec.BOOL.optionalFieldOf("allowOnMainPath", false).forGetter(MKHallwayFamilyDefinition::allowOnMainPath),
            Codec.BOOL.optionalFieldOf("allowOnBranchPath", true).forGetter(MKHallwayFamilyDefinition::allowOnBranchPath),
            ResourceLocation.CODEC.optionalFieldOf("floorBlock").forGetter(hallway -> hallway.legacyFloorBlock),
            ResourceLocation.CODEC.optionalFieldOf("wallBlock").forGetter(hallway -> hallway.legacyWallBlock),
            ResourceLocation.CODEC.optionalFieldOf("ceilingBlock").forGetter(hallway -> hallway.legacyCeilingBlock),
            MKWorkspacePaletteOverride.CODEC.optionalFieldOf("paletteOverride")
                    .forGetter(MKHallwayFamilyDefinition::paletteOverride)
    ).apply(instance, MKHallwayFamilyDefinition::fromSerializedData));

    private final String hallwayId;
    private final String openingProfileId;
    private final int length;
    private final int interiorWidth;
    private final int interiorHeight;
    private final int slopeDelta;
    private final boolean allowOnMainPath;
    private final boolean allowOnBranchPath;
    private final Optional<ResourceLocation> legacyFloorBlock;
    private final Optional<ResourceLocation> legacyWallBlock;
    private final Optional<ResourceLocation> legacyCeilingBlock;
    private final Optional<MKWorkspacePaletteOverride> paletteOverride;

    public MKHallwayFamilyDefinition(String hallwayId, String openingProfileId, int length, int interiorWidth,
                                     int interiorHeight, int slopeDelta, boolean allowOnMainPath,
                                     boolean allowOnBranchPath, ResourceLocation floorBlock,
                                     ResourceLocation wallBlock, ResourceLocation ceilingBlock) {
        this(hallwayId, openingProfileId, length, interiorWidth, interiorHeight, slopeDelta, allowOnMainPath,
                allowOnBranchPath, Optional.of(MKWorkspacePaletteOverride.of(floorBlock, wallBlock, ceilingBlock)));
    }

    public MKHallwayFamilyDefinition(String hallwayId, String openingProfileId, int length, int interiorWidth,
                                     int interiorHeight, int slopeDelta, boolean allowOnMainPath,
                                     boolean allowOnBranchPath,
                                     Optional<MKWorkspacePaletteOverride> paletteOverride) {
        this.hallwayId = hallwayId;
        this.openingProfileId = openingProfileId;
        this.length = length;
        this.interiorWidth = interiorWidth;
        this.interiorHeight = interiorHeight;
        this.slopeDelta = slopeDelta;
        this.allowOnMainPath = allowOnMainPath;
        this.allowOnBranchPath = allowOnBranchPath;
        this.legacyFloorBlock = Optional.empty();
        this.legacyWallBlock = Optional.empty();
        this.legacyCeilingBlock = Optional.empty();
        this.paletteOverride = paletteOverride.filter(override -> !override.isEmpty());
    }

    public static MKHallwayFamilyDefinition fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "hallway family definition");
    }

    public static List<MKHallwayFamilyDefinition> createDefaults(MKWorkspaceDimensions dimensions,
                                                                  MKWorkspaceMaterialPalette palette) {
        return List.of(
                new MKHallwayFamilyDefinition(
                        "main",
                        "main_opening",
                        5,
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        0,
                        true,
                        false,
                        Optional.empty()
                ),
                new MKHallwayFamilyDefinition(
                        "branch",
                        "branch_opening",
                        5,
                        dimensions.doorwayWidth(),
                        dimensions.doorwayHeight(),
                        0,
                        false,
                        true,
                        Optional.empty()
                )
        );
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "hallway family definition");
    }

    public List<String> validate(Set<String> openingProfileIds) {
        List<String> errors = new ArrayList<>();
        if (hallwayId.isBlank()) {
            errors.add("hallway family id cannot be blank");
        }
        if (openingProfileId.isBlank()) {
            errors.add("hallway family opening profile id cannot be blank");
        } else if (!openingProfileIds.contains(openingProfileId)) {
            errors.add("hallway family " + hallwayId + " references missing opening profile " + openingProfileId);
        }
        if (length < 1) {
            errors.add("hallway family " + hallwayId + " length must be at least 1");
        }
        if (interiorWidth < 1) {
            errors.add("hallway family " + hallwayId + " width must be at least 1");
        }
        if (interiorWidth % 2 == 0) {
            errors.add("hallway family " + hallwayId + " width must be odd");
        }
        if (interiorHeight < 2) {
            errors.add("hallway family " + hallwayId + " height must be at least 2");
        }
        if (!allowOnMainPath && !allowOnBranchPath) {
            errors.add("hallway family " + hallwayId + " must be usable on the main path or branch path");
        }
        return errors;
    }

    public String hallwayId() {
        return hallwayId;
    }

    public String openingProfileId() {
        return openingProfileId;
    }

    public int length() {
        return length;
    }

    public int interiorWidth() {
        return interiorWidth;
    }

    public int interiorHeight() {
        return interiorHeight;
    }

    public int slopeDelta() {
        return slopeDelta;
    }

    public boolean allowOnMainPath() {
        return allowOnMainPath;
    }

    public boolean allowOnBranchPath() {
        return allowOnBranchPath;
    }

    public ResourceLocation floorBlock() {
        return paletteOverride.flatMap(MKWorkspacePaletteOverride::floorBlock)
                .orElseGet(() -> legacyFloorBlock.orElse(MKWorkspaceMaterialPalette.defaultPalette().floorBlock()));
    }

    public ResourceLocation wallBlock() {
        return paletteOverride.flatMap(MKWorkspacePaletteOverride::wallBlock)
                .orElseGet(() -> legacyWallBlock.orElse(MKWorkspaceMaterialPalette.defaultPalette().wallBlock()));
    }

    public ResourceLocation ceilingBlock() {
        return paletteOverride.flatMap(MKWorkspacePaletteOverride::ceilingBlock)
                .orElseGet(() -> legacyCeilingBlock.orElse(MKWorkspaceMaterialPalette.defaultPalette().ceilingBlock()));
    }

    @Override
    public String paletteFamilyId() {
        return hallwayId;
    }

    @Override
    public Optional<MKTowerWorkspaceCategory> paletteCategory() {
        return Optional.empty();
    }

    @Override
    public Optional<MKWorkspacePaletteOverride> paletteOverride() {
        return paletteOverride;
    }

    private static MKHallwayFamilyDefinition fromSerializedData(String hallwayId, String openingProfileId, int length,
                                                                int interiorWidth, int interiorHeight, int slopeDelta,
                                                                boolean allowOnMainPath, boolean allowOnBranchPath,
                                                                Optional<ResourceLocation> floorBlock,
                                                                Optional<ResourceLocation> wallBlock,
                                                                Optional<ResourceLocation> ceilingBlock,
                                                                Optional<MKWorkspacePaletteOverride> paletteOverride) {
        Optional<MKWorkspacePaletteOverride> resolvedOverride = paletteOverride;
        if (resolvedOverride.isEmpty() && (floorBlock.isPresent() || wallBlock.isPresent() || ceilingBlock.isPresent())) {
            resolvedOverride = Optional.of(new MKWorkspacePaletteOverride(
                    floorBlock,
                    wallBlock,
                    ceilingBlock,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty()
            ));
        }
        return new MKHallwayFamilyDefinition(hallwayId, openingProfileId, length, interiorWidth, interiorHeight,
                slopeDelta, allowOnMainPath, allowOnBranchPath, resolvedOverride);
    }
}
