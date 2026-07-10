package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MKWorkspaceVerticalStackSizingReport(
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

    public static MKWorkspaceVerticalStackSizingReport fromSettings(MKWorkspaceVerticalStackSettings settings) {
        return fromSettings(settings, List.of());
    }

    public static MKWorkspaceVerticalStackSizingReport fromSettings(MKWorkspaceVerticalStackSettings settings,
                                                        List<MKWorkspaceRoomFamilyDefinition> families) {
        return fromSettings(settings, families, List.of());
    }

    public static MKWorkspaceVerticalStackSizingReport fromSettings(MKWorkspaceVerticalStackSettings settings,
                                                        List<MKWorkspaceRoomFamilyDefinition> families,
                                                        List<HorizontalExitInfo> fallbackEntryExits) {
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
        List<SectionInfo> sections = buildSections(settings, families, fallbackEntryExits);
        return new MKWorkspaceVerticalStackSizingReport(
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
                                                   List<MKWorkspaceRoomFamilyDefinition> families,
                                                   List<HorizontalExitInfo> fallbackEntryExits) {
        Map<MKWorkspaceVerticalStackSlot, MKWorkspaceRoomFamilyDefinition> familyBySlot = families.stream()
                .filter(family -> MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(family.settingsTopologySlotIdOrSelf())
                        .filter(settings.stackId()::equals)
                        .isPresent())
                .collect(Collectors.toMap(
                        family -> MKWorkspaceVerticalStackSlot.fromTopologySlotId(family.sourceTopologySlotIdOrSelf()).orElseThrow(),
                        family -> family,
                        (first, ignored) -> first
                ));
        ArrayList<SectionInfo> sections = new ArrayList<>();
        if (settings.basementFloors() > 0) {
            sections.add(section("basement_cap", "Basement Cap", settings.basementCapHeight(),
                    true, familyBySlot.get(MKWorkspaceVerticalStackSlot.BASEMENT_CAP), fallbackEntryExits));
            if (settings.basementCapApproachEnabled()) {
                sections.add(section("basement_cap_approach", "Basement Cap Approach", settings.basementCapHeight(),
                        true, familyBySlot.get(MKWorkspaceVerticalStackSlot.BASEMENT_CAP_APPROACH), fallbackEntryExits));
            }
            for (int floor = settings.basementFloors(); floor >= 1; floor--) {
                sections.add(section("basement_floor", "Basement Floor " + floor, settings.basementHeight(),
                        true, familyBySlot.get(MKWorkspaceVerticalStackSlot.BASEMENT_FLOOR), fallbackEntryExits));
            }
            if (settings.basementEntryEnabled()) {
                sections.add(section("basement_entry", "Basement Entry", settings.basementEntryHeight(),
                        true, familyBySlot.get(MKWorkspaceVerticalStackSlot.BASEMENT_ENTRY), fallbackEntryExits));
            }
        } else {
            sections.add(section("basement_floor", "Basement Floors", settings.basementHeight(),
                    false, familyBySlot.get(MKWorkspaceVerticalStackSlot.BASEMENT_FLOOR), fallbackEntryExits));
        }
        sections.add(section("entry", "Entry", settings.entryHeight(),
                true, familyBySlot.get(MKWorkspaceVerticalStackSlot.ENTRY), fallbackEntryExits));
        if (settings.mainFloors() > 0) {
            for (int floor = 1; floor <= settings.mainFloors(); floor++) {
                sections.add(section("main_floor", "Main Floor " + floor, settings.mainHeight(),
                        true, familyBySlot.get(MKWorkspaceVerticalStackSlot.MAIN_FLOOR), fallbackEntryExits));
            }
        } else {
            sections.add(section("main_floor", "Main Floors", settings.mainHeight(),
                    false, familyBySlot.get(MKWorkspaceVerticalStackSlot.MAIN_FLOOR), fallbackEntryExits));
        }
        if (settings.topCapApproachEnabled()) {
            sections.add(section("top_cap_approach", "Main Cap Approach", settings.mainCapHeight(),
                    true, familyBySlot.get(MKWorkspaceVerticalStackSlot.TOP_CAP_APPROACH), fallbackEntryExits));
        }
        sections.add(section("top_cap", "Main Cap", settings.mainCapHeight(),
                true, familyBySlot.get(MKWorkspaceVerticalStackSlot.TOP_CAP), fallbackEntryExits));
        return List.copyOf(sections);
    }

    private static SectionInfo section(String key, String label, int height,
                                       boolean active, MKWorkspaceRoomFamilyDefinition family,
                                       List<HorizontalExitInfo> fallbackEntryExits) {
        List<HorizontalExitInfo> exits = family == null ? List.of() : family.horizontalOnlyExits().stream()
                .map(MKWorkspaceVerticalStackSizingReport::horizontalExitInfo)
                .toList();
        if (exits.isEmpty() && active && "entry".equals(key)) {
            exits = List.copyOf(fallbackEntryExits == null ? List.of() : fallbackEntryExits);
        }
        return new SectionInfo(key, label, height, active, exits);
    }

    private static HorizontalExitInfo horizontalExitInfo(MKFamilyHorizontalExitDefinition exit) {
        return new HorizontalExitInfo(
                exit.direction().getSerializedName(),
                exit.pathKind().getSerializedName(),
                exit.verticalOffset(),
                exit.sideOffset()
        );
    }
}
