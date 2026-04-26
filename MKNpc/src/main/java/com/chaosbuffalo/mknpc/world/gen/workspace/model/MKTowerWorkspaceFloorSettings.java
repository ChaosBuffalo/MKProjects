package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public class MKTowerWorkspaceFloorSettings {
    // These mirror the current tower runtime envelope in NpcStructures.
    public static final int DEFAULT_VERTICAL_RADIUS = 96;
    public static final int DEFAULT_MAX_CHAIN_DEPTH = 12;
    private static final int TOTAL_VERTICAL_BUDGET = (DEFAULT_VERTICAL_RADIUS * 2) + 1;
    private static final int MAX_CHAIN_PIECES = DEFAULT_MAX_CHAIN_DEPTH + 1;
    private static final int UPWARD_FIXED_PIECES = 3;
    private static final int DOWNWARD_FIXED_PIECES = 3;

    public static final Codec<MKTowerWorkspaceFloorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("mainFloors", 1).forGetter(MKTowerWorkspaceFloorSettings::mainFloors),
            Codec.INT.optionalFieldOf("basementFloors", 1).forGetter(MKTowerWorkspaceFloorSettings::basementFloors)
    ).apply(instance, MKTowerWorkspaceFloorSettings::new));

    private final int mainFloors;
    private final int basementFloors;

    public MKTowerWorkspaceFloorSettings(int mainFloors, int basementFloors) {
        this.mainFloors = Math.max(0, mainFloors);
        this.basementFloors = Math.max(0, basementFloors);
    }

    public static MKTowerWorkspaceFloorSettings defaultSettings() {
        return new MKTowerWorkspaceFloorSettings(1, 1);
    }

    public static MKTowerWorkspaceFloorSettings fromTag(CompoundTag tag) {
        return MKWorkspaceCodecs.parseNbt(CODEC, tag, "tower workspace floor settings");
    }

    public CompoundTag toTag() {
        return MKWorkspaceCodecs.encodeNbt(CODEC, this, "tower workspace floor settings");
    }

    public List<String> validate(List<MKTowerWorkspaceCategoryProfile> categoryProfiles) {
        List<String> errors = new ArrayList<>();
        if (mainFloors < 0) {
            errors.add("main floor count must be at least 0");
        }
        if (basementFloors < 0) {
            errors.add("basement floor count must be at least 0");
        }
        List<Integer> allowedMainFloors = allowedMainFloorCounts(categoryProfiles, basementFloors);
        if (!allowedMainFloors.contains(mainFloors)) {
            errors.add("main floor count must be one of " + allowedMainFloors + " for the current category heights");
        }
        List<Integer> allowedBasementFloors = allowedBasementFloorCounts(categoryProfiles, mainFloors);
        if (!allowedBasementFloors.contains(basementFloors)) {
            errors.add("basement floor count must be one of " + allowedBasementFloors + " for the current category heights");
        }
        return errors;
    }

    public static List<Integer> allowedMainFloorCounts(List<MKTowerWorkspaceCategoryProfile> categoryProfiles,
                                                       int basementFloors) {
        List<Integer> allowed = new ArrayList<>();
        int maxFloors = Math.max(0, MAX_CHAIN_PIECES - UPWARD_FIXED_PIECES);
        for (int candidate = 0; candidate <= maxFloors; candidate++) {
            if (fitsBudget(categoryProfiles, candidate, Math.max(0, basementFloors))) {
                allowed.add(candidate);
            }
        }
        if (allowed.isEmpty()) {
            allowed.add(0);
        }
        return allowed;
    }

    public static List<Integer> allowedBasementFloorCounts(List<MKTowerWorkspaceCategoryProfile> categoryProfiles,
                                                           int mainFloors) {
        List<Integer> allowed = new ArrayList<>();
        int maxFloors = Math.max(0, MAX_CHAIN_PIECES - DOWNWARD_FIXED_PIECES);
        for (int candidate = 0; candidate <= maxFloors; candidate++) {
            if (fitsBudget(categoryProfiles, Math.max(0, mainFloors), candidate)) {
                allowed.add(candidate);
            }
        }
        if (allowed.isEmpty()) {
            allowed.add(0);
        }
        return allowed;
    }

    private static boolean fitsBudget(List<MKTowerWorkspaceCategoryProfile> categoryProfiles, int mainFloors,
                                      int basementFloors) {
        MKTowerWorkspaceCategoryProfile entry = findProfile(categoryProfiles, MKTowerWorkspaceCategory.ENTRY);
        MKTowerWorkspaceCategoryProfile main = findProfile(categoryProfiles, MKTowerWorkspaceCategory.MAIN);
        MKTowerWorkspaceCategoryProfile basement = findProfile(categoryProfiles, MKTowerWorkspaceCategory.BASEMENT);
        MKTowerWorkspaceCategoryProfile topCap = findProfile(categoryProfiles, MKTowerWorkspaceCategory.TOP_CAP);
        MKTowerWorkspaceCategoryProfile basementCap = findProfile(categoryProfiles, MKTowerWorkspaceCategory.BASEMENT_CAP);
        if (entry == null || main == null || basement == null || topCap == null || basementCap == null) {
            return false;
        }
        int upwardSpan = entry.fullHeight() + (mainFloors * main.fullHeight()) + (2 * topCap.fullHeight());
        int downwardSpan = ((basementFloors + 1) * basement.fullHeight()) + basementCap.fullHeight();
        int totalSpan = upwardSpan + downwardSpan;
        return upwardSpan <= DEFAULT_VERTICAL_RADIUS &&
                downwardSpan <= DEFAULT_VERTICAL_RADIUS &&
                totalSpan <= TOTAL_VERTICAL_BUDGET;
    }

    private static MKTowerWorkspaceCategoryProfile findProfile(List<MKTowerWorkspaceCategoryProfile> categoryProfiles,
                                                               MKTowerWorkspaceCategory category) {
        return categoryProfiles.stream()
                .filter(profile -> profile.category() == category)
                .findFirst()
                .orElse(null);
    }

    public int mainFloors() {
        return mainFloors;
    }

    public int basementFloors() {
        return basementFloors;
    }
}
