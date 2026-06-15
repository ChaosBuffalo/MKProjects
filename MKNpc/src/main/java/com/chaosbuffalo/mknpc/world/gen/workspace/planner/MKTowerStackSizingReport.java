package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MKTowerStackSizingReport(
        String stackId,
        int width,
        int length,
        int entryHeight,
        int basementHeight,
        int basementEntryHeight,
        int basementCapHeight,
        int mainHeight,
        int mainCapHeight,
        int minMainFloors,
        int maxMainFloors,
        int minBasementFloors,
        int maxBasementFloors,
        boolean topCapApproachEnabled,
        boolean basementEntryEnabled,
        boolean basementCapApproachEnabled,
        int upwardPieces,
        int downwardPieces,
        int maxVerticalSpan,
        List<SectionInfo> sections
) {
    public record SectionInfo(
            String key,
            String label,
            int height,
            boolean active,
            List<HorizontalExitInfo> horizontalExits
    ) {
    }

    public record HorizontalExitInfo(
            String direction,
            String pathKind,
            int verticalOffset,
            int sideOffset
    ) {
    }

    public static MKTowerStackSizingReport fromSettings(MKWorkspaceVerticalStackSettings settings) {
        return fromSettings(settings, List.of());
    }

    public static MKTowerStackSizingReport fromSettings(MKWorkspaceVerticalStackSettings settings,
                                                        List<MKWorkspaceRoomFamilyDefinition> families) {
        int upwardPieces = 1 + settings.mainFloors() + 1 +
                (settings.topCapApproachEnabled() ? 1 : 0);
        int downwardPieces = settings.basementFloors() <= 0 ? 0 :
                settings.basementFloors() + 1 +
                        (settings.basementEntryEnabled() ? 1 : 0) +
                        (settings.basementCapApproachEnabled() ? 1 : 0);
        int upwardSpan = settings.entryHeight() +
                (settings.mainFloors() * settings.mainHeight()) +
                settings.mainCapHeight() +
                (settings.topCapApproachEnabled() ? settings.mainCapHeight() : 0);
        int downwardSpan = settings.basementFloors() <= 0 ? 0 :
                (settings.basementFloors() * settings.basementHeight()) +
                        (settings.basementEntryEnabled() ? settings.basementEntryHeight() : 0) +
                        settings.basementCapHeight() +
                        (settings.basementCapApproachEnabled() ? settings.basementCapHeight() : 0);
        List<SectionInfo> sections = buildSections(settings, families);
        return new MKTowerStackSizingReport(
                settings.stackId(),
                settings.width(),
                settings.length(),
                settings.entryHeight(),
                settings.basementHeight(),
                settings.basementEntryHeight(),
                settings.basementCapHeight(),
                settings.mainHeight(),
                settings.mainCapHeight(),
                settings.minMainFloors(),
                settings.mainFloors(),
                settings.minBasementFloors(),
                settings.basementFloors(),
                settings.topCapApproachEnabled(),
                settings.basementEntryEnabled(),
                settings.basementCapApproachEnabled(),
                upwardPieces,
                downwardPieces,
                upwardSpan + downwardSpan,
                sections
        );
    }

    private static List<SectionInfo> buildSections(MKWorkspaceVerticalStackSettings settings,
                                                   List<MKWorkspaceRoomFamilyDefinition> families) {
        Map<MKTowerWorkspaceStackSlot, MKWorkspaceRoomFamilyDefinition> familyBySlot = families.stream()
                .filter(family -> MKTowerWorkspaceStackSlot.stackIdForTopologySlot(family.topologySlotId())
                        .filter(settings.stackId()::equals)
                        .isPresent())
                .collect(Collectors.toMap(
                        family -> MKTowerWorkspaceStackSlot.fromTopologySlotId(family.topologySlotId()).orElseThrow(),
                        family -> family,
                        (first, ignored) -> first
                ));
        ArrayList<SectionInfo> sections = new ArrayList<>();
        if (settings.basementFloors() > 0) {
            sections.add(section("basement_cap", "Basement Cap", settings.basementCapHeight(),
                    true, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.BASEMENT_CAP)));
            if (settings.basementCapApproachEnabled()) {
                sections.add(section("basement_cap_approach", "Basement Cap Approach", settings.basementCapHeight(),
                        true, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.BASEMENT_CAP_APPROACH)));
            }
            for (int floor = settings.basementFloors(); floor >= 1; floor--) {
                sections.add(section("basement_floor", "Basement Floor " + floor, settings.basementHeight(),
                        true, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.BASEMENT_FLOOR)));
            }
            if (settings.basementEntryEnabled()) {
                sections.add(section("basement_entry", "Basement Entry", settings.basementEntryHeight(),
                        true, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.BASEMENT_ENTRY)));
            }
        } else {
            sections.add(section("basement_floor", "Basement Floors", settings.basementHeight(),
                    false, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.BASEMENT_FLOOR)));
        }
        sections.add(section("entry", "Entry", settings.entryHeight(),
                true, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.ENTRY)));
        if (settings.mainFloors() > 0) {
            for (int floor = 1; floor <= settings.mainFloors(); floor++) {
                sections.add(section("main_floor", "Main Floor " + floor, settings.mainHeight(),
                        true, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.MAIN_FLOOR)));
            }
        } else {
            sections.add(section("main_floor", "Main Floors", settings.mainHeight(),
                    false, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.MAIN_FLOOR)));
        }
        if (settings.topCapApproachEnabled()) {
            sections.add(section("top_cap_approach", "Main Cap Approach", settings.mainCapHeight(),
                    true, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.TOP_CAP_APPROACH)));
        }
        sections.add(section("top_cap", "Main Cap", settings.mainCapHeight(),
                true, settings, familyBySlot.get(MKTowerWorkspaceStackSlot.TOP_CAP)));
        return List.copyOf(sections);
    }

    private static SectionInfo section(String key, String label, int height,
                                       boolean active, MKWorkspaceVerticalStackSettings settings,
                                       MKWorkspaceRoomFamilyDefinition family) {
        List<HorizontalExitInfo> exits = family == null ? List.of() : family.horizontalOnlyExits().stream()
                .map(MKTowerStackSizingReport::horizontalExitInfo)
                .toList();
        if (exits.isEmpty() && active && "entry".equals(key) && "keep.center".equals(settings.stackId())) {
            exits = List.of(new HorizontalExitInfo("south", "ingress", 0, 0));
        }
        return new SectionInfo(key, label, height, active, exits);
    }

    private static HorizontalExitInfo horizontalExitInfo(MKWorkspaceFamilyHorizontalExitDefinition exit) {
        return new HorizontalExitInfo(
                exit.direction().getSerializedName(),
                exit.pathKind().getSerializedName(),
                exit.verticalOffset(),
                exit.sideOffset()
        );
    }
}
