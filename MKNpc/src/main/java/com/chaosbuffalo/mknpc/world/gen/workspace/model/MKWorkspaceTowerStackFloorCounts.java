package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.ArrayList;
import java.util.List;

public final class MKWorkspaceTowerStackFloorCounts {
    // These mirror the current tower runtime envelope in NpcStructures.
    public static final int DEFAULT_VERTICAL_RADIUS = 96;
    public static final int DEFAULT_MAX_CHAIN_DEPTH = 12;
    public static final int DEFAULT_MAIN_FLOORS = 1;
    public static final int DEFAULT_BASEMENT_FLOORS = 1;
    public static final boolean DEFAULT_TOP_CAP_APPROACH_ENABLED = true;
    public static final boolean DEFAULT_BASEMENT_CAP_APPROACH_ENABLED = false;
    private static final int TOTAL_VERTICAL_BUDGET = (DEFAULT_VERTICAL_RADIUS * 2) + 1;
    private static final int MAX_CHAIN_PIECES = DEFAULT_MAX_CHAIN_DEPTH + 1;
    private static final int UPWARD_FIXED_PIECES = 3;
    private static final int DOWNWARD_FIXED_PIECES = 3;

    private MKWorkspaceTowerStackFloorCounts() {
    }

    public static List<String> validate(MKWorkspaceTowerStackSettings settings) {
        List<String> errors = new ArrayList<>();
        if (settings.mainFloors() < 0) {
            errors.add("main floor count must be at least 0");
        }
        if (settings.basementFloors() < 0) {
            errors.add("basement floor count must be at least 0");
        }
        MKTowerStackBudget budget = MKTowerStackBudget.fromStackSettings(settings);
        List<Integer> allowedMainFloors = allowedMainFloorCounts(budget, settings.basementFloors(),
                settings.topCapApproachEnabled(), settings.basementCapApproachEnabled());
        if (!allowedMainFloors.contains(settings.mainFloors())) {
            errors.add("main floor count must be one of " + allowedMainFloors + " for the current stack heights");
        }
        List<Integer> allowedBasementFloors = allowedBasementFloorCounts(budget, settings.mainFloors(),
                settings.topCapApproachEnabled(), settings.basementCapApproachEnabled());
        if (!allowedBasementFloors.contains(settings.basementFloors())) {
            errors.add("basement floor count must be one of " + allowedBasementFloors + " for the current stack heights");
        }
        return errors;
    }

    public static List<Integer> allowedMainFloorCounts(MKTowerStackBudget budget,
                                                       int basementFloors,
                                                       boolean topCapApproachEnabled,
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
                                                           int mainFloors,
                                                           boolean topCapApproachEnabled,
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
}
