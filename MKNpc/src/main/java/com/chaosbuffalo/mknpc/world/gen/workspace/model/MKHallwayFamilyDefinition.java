package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MKHallwayFamilyDefinition {
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
        return new MKHallwayFamilyDefinition(
                tag.getString("hallwayId"),
                tag.getString("openingProfileId"),
                tag.getInt("length"),
                tag.getInt("interiorWidth"),
                tag.getInt("interiorHeight"),
                tag.contains("slopeDelta") ? tag.getInt("slopeDelta") : 0,
                tag.contains("allowOnMainPath") && tag.getBoolean("allowOnMainPath"),
                !tag.contains("allowOnBranchPath") || tag.getBoolean("allowOnBranchPath"),
                ResourceLocation.parse(tag.getString("floorBlock")),
                ResourceLocation.parse(tag.getString("wallBlock")),
                ResourceLocation.parse(tag.getString("ceilingBlock"))
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("hallwayId", hallwayId);
        tag.putString("openingProfileId", openingProfileId);
        tag.putInt("length", length);
        tag.putInt("interiorWidth", interiorWidth);
        tag.putInt("interiorHeight", interiorHeight);
        tag.putInt("slopeDelta", slopeDelta);
        tag.putBoolean("allowOnMainPath", allowOnMainPath);
        tag.putBoolean("allowOnBranchPath", allowOnBranchPath);
        tag.putString("floorBlock", floorBlock.toString());
        tag.putString("wallBlock", wallBlock.toString());
        tag.putString("ceilingBlock", ceilingBlock.toString());
        return tag;
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
