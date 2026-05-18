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
            Codec.INT.optionalFieldOf("basementFloors", 1).forGetter(MKTowerWorkspaceFloorSettings::basementFloors),
            Codec.BOOL.optionalFieldOf("topCapApproachEnabled", true)
                    .forGetter(MKTowerWorkspaceFloorSettings::topCapApproachEnabled),
            Codec.BOOL.optionalFieldOf("basementCapApproachEnabled", false)
                    .forGetter(MKTowerWorkspaceFloorSettings::basementCapApproachEnabled)
    ).apply(instance, MKTowerWorkspaceFloorSettings::new));

    private final int mainFloors;
    private final int basementFloors;
    private final boolean topCapApproachEnabled;
    private final boolean basementCapApproachEnabled;

    public MKTowerWorkspaceFloorSettings(int mainFloors, int basementFloors) {
        this(mainFloors, basementFloors, true, false);
    }

    public MKTowerWorkspaceFloorSettings(int mainFloors, int basementFloors, boolean topCapApproachEnabled,
                                         boolean basementCapApproachEnabled) {
        this.mainFloors = Math.max(0, mainFloors);
        this.basementFloors = Math.max(0, basementFloors);
        this.topCapApproachEnabled = topCapApproachEnabled;
        this.basementCapApproachEnabled = basementCapApproachEnabled;
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

    public List<String> validate(MKTowerStackBudget budget) {
        List<String> errors = new ArrayList<>();
        if (mainFloors < 0) {
            errors.add("main floor count must be at least 0");
        }
        if (basementFloors < 0) {
            errors.add("basement floor count must be at least 0");
        }
        List<Integer> allowedMainFloors = allowedMainFloorCounts(budget, basementFloors,
                topCapApproachEnabled, basementCapApproachEnabled);
        if (!allowedMainFloors.contains(mainFloors)) {
            errors.add("main floor count must be one of " + allowedMainFloors + " for the current stack heights");
        }
        List<Integer> allowedBasementFloors = allowedBasementFloorCounts(budget, mainFloors,
                topCapApproachEnabled, basementCapApproachEnabled);
        if (!allowedBasementFloors.contains(basementFloors)) {
            errors.add("basement floor count must be one of " + allowedBasementFloors + " for the current stack heights");
        }
        return errors;
    }

    public static List<Integer> allowedMainFloorCounts(MKTowerStackBudget budget,
                                                       int basementFloors) {
        return allowedMainFloorCounts(budget, basementFloors, true, false);
    }

    public static List<Integer> allowedMainFloorCounts(MKTowerStackBudget budget,
                                                       int basementFloors, boolean topCapApproachEnabled,
                                                       boolean basementCapApproachEnabled) {
        List<Integer> allowed = new ArrayList<>();
        int maxFloors = Math.max(0, MAX_CHAIN_PIECES - getUpwardFixedPieces(topCapApproachEnabled));
        for (int candidate = 0; candidate <= maxFloors; candidate++) {
            if (fitsBudget(budget, candidate, Math.max(0, basementFloors),
                    topCapApproachEnabled, basementCapApproachEnabled)) {
                allowed.add(candidate);
            }
        }
        if (allowed.isEmpty()) {
            allowed.add(0);
        }
        return allowed;
    }

    public static List<Integer> allowedBasementFloorCounts(MKTowerStackBudget budget,
                                                           int mainFloors) {
        return allowedBasementFloorCounts(budget, mainFloors, true, false);
    }

    public static List<Integer> allowedBasementFloorCounts(MKTowerStackBudget budget,
                                                           int mainFloors, boolean topCapApproachEnabled,
                                                           boolean basementCapApproachEnabled) {
        List<Integer> allowed = new ArrayList<>();
        int maxFloors = Math.max(0, MAX_CHAIN_PIECES - getDownwardFixedPieces(basementCapApproachEnabled));
        for (int candidate = 0; candidate <= maxFloors; candidate++) {
            if (fitsBudget(budget, Math.max(0, mainFloors), candidate,
                    topCapApproachEnabled, basementCapApproachEnabled)) {
                allowed.add(candidate);
            }
        }
        if (allowed.isEmpty()) {
            allowed.add(0);
        }
        return allowed;
    }

    private static boolean fitsBudget(MKTowerStackBudget budget, int mainFloors, int basementFloors) {
        return fitsBudget(budget, mainFloors, basementFloors, true, false);
    }

    private static boolean fitsBudget(MKTowerStackBudget budget, int mainFloors, int basementFloors,
                                      boolean topCapApproachEnabled,
                                      boolean basementCapApproachEnabled) {
        int upwardSpan = budget.entryHeight() + (mainFloors * budget.mainFloorHeight()) + budget.topCapHeight() +
                (topCapApproachEnabled ? budget.topCapHeight() : 0);
        int downwardSpan = ((basementFloors + 1) * budget.basementFloorHeight()) + budget.basementCapHeight() +
                (basementCapApproachEnabled ? budget.basementCapHeight() : 0);
        int totalSpan = upwardSpan + downwardSpan;
        return upwardSpan <= DEFAULT_VERTICAL_RADIUS &&
                downwardSpan <= DEFAULT_VERTICAL_RADIUS &&
                totalSpan <= TOTAL_VERTICAL_BUDGET;
    }

    private static int getUpwardFixedPieces(boolean topCapApproachEnabled) {
        return topCapApproachEnabled ? UPWARD_FIXED_PIECES : UPWARD_FIXED_PIECES - 1;
    }

    private static int getDownwardFixedPieces(boolean basementCapApproachEnabled) {
        return basementCapApproachEnabled ? DOWNWARD_FIXED_PIECES + 1 : DOWNWARD_FIXED_PIECES;
    }

    public int mainFloors() {
        return mainFloors;
    }

    public int basementFloors() {
        return basementFloors;
    }

    public boolean topCapApproachEnabled() {
        return topCapApproachEnabled;
    }

    public boolean basementCapApproachEnabled() {
        return basementCapApproachEnabled;
    }
}
