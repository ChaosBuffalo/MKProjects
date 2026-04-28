package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MKHallwayFamilyDefinition {
    public static final Codec<MKHallwayFamilyDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("hallwayId").forGetter(MKHallwayFamilyDefinition::hallwayId),
            Codec.STRING.fieldOf("openingProfileId").forGetter(MKHallwayFamilyDefinition::openingProfileId),
            Codec.INT.fieldOf("length").forGetter(MKHallwayFamilyDefinition::length),
            Codec.INT.fieldOf("interiorWidth").forGetter(MKHallwayFamilyDefinition::interiorWidth),
            Codec.INT.fieldOf("interiorHeight").forGetter(MKHallwayFamilyDefinition::interiorHeight),
            Codec.INT.optionalFieldOf("slopeDelta", 0).forGetter(MKHallwayFamilyDefinition::slopeDelta),
            Codec.BOOL.optionalFieldOf("allowOnMainPath", false).forGetter(MKHallwayFamilyDefinition::allowOnMainPath),
            Codec.BOOL.optionalFieldOf("allowOnBranchPath", true).forGetter(MKHallwayFamilyDefinition::allowOnBranchPath),
            ResourceLocation.CODEC.fieldOf("floorBlock").forGetter(MKHallwayFamilyDefinition::floorBlock),
            ResourceLocation.CODEC.fieldOf("wallBlock").forGetter(MKHallwayFamilyDefinition::wallBlock),
            ResourceLocation.CODEC.fieldOf("ceilingBlock").forGetter(MKHallwayFamilyDefinition::ceilingBlock)
    ).apply(instance, MKHallwayFamilyDefinition::new));

    private final String hallwayId;
    private final String openingProfileId;
    private final int length;
    private final int interiorWidth;
    private final int interiorHeight;
    private final int slopeDelta;
    private final boolean allowOnMainPath;
    private final boolean allowOnBranchPath;
    private final ResourceLocation floorBlock;
    private final ResourceLocation wallBlock;
    private final ResourceLocation ceilingBlock;

    public MKHallwayFamilyDefinition(String hallwayId, String openingProfileId, int length, int interiorWidth,
                                     int interiorHeight, int slopeDelta, boolean allowOnMainPath,
                                     boolean allowOnBranchPath, ResourceLocation floorBlock,
                                     ResourceLocation wallBlock, ResourceLocation ceilingBlock) {
        this.hallwayId = hallwayId;
        this.openingProfileId = openingProfileId;
        this.length = length;
        this.interiorWidth = interiorWidth;
        this.interiorHeight = interiorHeight;
        this.slopeDelta = slopeDelta;
        this.allowOnMainPath = allowOnMainPath;
        this.allowOnBranchPath = allowOnBranchPath;
        this.floorBlock = floorBlock;
        this.wallBlock = wallBlock;
        this.ceilingBlock = ceilingBlock;
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
                        palette.floorBlock(),
                        palette.wallBlock(),
                        palette.ceilingBlock()
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
                        palette.floorBlock(),
                        palette.wallBlock(),
                        palette.ceilingBlock()
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
        return floorBlock;
    }

    public ResourceLocation wallBlock() {
        return wallBlock;
    }

    public ResourceLocation ceilingBlock() {
        return ceilingBlock;
    }
}
