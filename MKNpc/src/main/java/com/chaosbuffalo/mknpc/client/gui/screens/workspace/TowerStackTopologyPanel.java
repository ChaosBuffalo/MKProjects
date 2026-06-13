package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomGeometry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKTowerStackSizingReport;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class TowerStackTopologyPanel {
    public void addStackEditor(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                               WorkspaceDraftSession editor, String stackId, String labelPrefix) {
        addStackPreview(screen, content, editor, stackId);
        addStackSettings(screen, content, editor, stackId, labelPrefix);
    }

    public void addStackEditor(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                               WorkspaceDraftSession editor, String stackId, String labelPrefix) {
        addStackPreview(screen, layout.previewContent(), editor, stackId);
        addStackSettings(screen, layout.settingsContent(), editor, stackId, labelPrefix);
    }

    public void addStackPreview(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                WorkspaceDraftSession editor, String stackId) {
        MKTowerStackSizingReport report = MKTowerStackSizingReport.fromSettings(editor.towerStackSettingsForUi(stackId),
                editor.towerStackFamiliesForUi(stackId));
        String selectedSection = normalizedSelectedSection(editor, stackId, report);
        int previewWidth = Math.min(screen.contentWidth(), 320);
        MKTowerStackSidePreview preview = new MKTowerStackSidePreview(previewWidth, 440,
                report, selectedSection, sectionKey -> {
            editor.towerStackPreviewSelection(stackId, sectionKey);
            screen.flagNeedSetup();
        }, controls(screen, editor, stackId));
        content.addWidget(preview);
        content.addConstraintToWidget(new CenterXConstraint(), preview);
        MKFloorTopologyPlanPreview.Controls floorControls = floorPlanControls(screen, editor, stackId);
        if (floorControls.hasFloorTopology(selectedSection)) {
            MKButton floorPlanButton = new MKButton(Component.literal("Floor Plan"), 180, screen.buttonHeight());
            floorPlanButton.setPressedCallback((button, mouseButton) -> {
                screen.openWorkspaceFloorPlanNode(stackId, selectedSection);
                return true;
            });
            content.addWidget(floorPlanButton);
            content.addConstraintToWidget(new CenterXConstraint(), floorPlanButton);
        }
    }

    public void addStackSettings(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                 WorkspaceDraftSession editor, String stackId, String labelPrefix) {
        addFloorRows(screen, content, editor, stackId);
        WorkspaceTopologyUiSupport.addResetRow(screen, content, labelPrefix + " Stack", () -> {
            editor.resetTowerStackDefaults(stackId);
            screen.flagNeedSetup();
        });
    }

    public void addFloorPlanEditor(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                   WorkspaceDraftSession editor, String stackId, String sectionKey) {
        MKFloorTopologyPlanPreview.Controls floorControls = floorPlanControls(screen, editor, stackId);
        if (!floorControls.hasFloorTopology(sectionKey)) {
            WorkspaceTopologyUiSupport.addText(screen, layout.settingsContent(), Component.literal(
                    "The selected tower section does not have floor plan controls."));
            return;
        }
        MKFloorTopologyPlanPreview floorPlan = new MKFloorTopologyPlanPreview(
                layout.previewWidth(), stackId, sectionKey, floorControls);
        layout.previewContent().addWidget(floorPlan);
        layout.previewContent().addConstraintToWidget(new CenterXConstraint(), floorPlan);
    }

    private String normalizedSelectedSection(WorkspaceDraftSession editor, String stackId, MKTowerStackSizingReport report) {
        String selected = editor.towerStackPreviewSelection(stackId);
        boolean valid = report.sections().stream().anyMatch(section -> section.key().equals(selected));
        if (valid) {
            return selected;
        }
        editor.towerStackPreviewSelection(stackId, "entry");
        return "entry";
    }

    private MKTowerStackSidePreview.Controls controls(MKWorkspaceScreen screen, WorkspaceDraftSession editor,
                                                      String stackId) {
        return new MKTowerStackSidePreview.Controls() {
            @Override
            public int stackWidth() {
                return editor.towerStackWidth(stackId);
            }

            @Override
            public void stackWidth(int value) {
                editor.towerStackWidth(stackId, value);
                screen.flagNeedSetup();
            }

            @Override
            public int stackLength() {
                return editor.towerStackLength(stackId);
            }

            @Override
            public void stackLength(int value) {
                editor.towerStackLength(stackId, value);
                screen.flagNeedSetup();
            }

            @Override
            public int shaftSize() {
                return editor.towerStackShaftSize(stackId);
            }

            @Override
            public void shaftSize(int value) {
                editor.towerStackShaftSize(stackId, value);
                screen.flagNeedSetup();
            }

            @Override
            public List<Integer> allowedShaftSizes() {
                return editor.allowedTowerStackShaftSizes(stackId);
            }

            @Override
            public MKVerticalAccessPlacement shaftPlacement() {
                return editor.towerStackVerticalAccessPlacement(stackId);
            }

            @Override
            public void cycleShaftPlacement(boolean reverse) {
                editor.towerStackVerticalAccessPlacement(stackId,
                        WorkspaceTopologyUiSupport.cycleValue(List.of(MKVerticalAccessPlacement.values()),
                                editor.towerStackVerticalAccessPlacement(stackId), reverse));
                screen.flagNeedSetup();
            }

            @Override
            public int heightMin() {
                return 3;
            }

            @Override
            public int heightMax() {
                return MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1;
            }

            @Override
            public int height(String sectionKey) {
                return switch (sectionKey) {
                    case "entry" -> editor.towerStackEntryHeight(stackId);
                    case "main_floor" -> editor.towerStackMainHeight(stackId);
                    case "basement_floor" -> editor.towerStackBasementHeight(stackId);
                    case "basement_entry" -> editor.towerStackBasementEntryHeight(stackId);
                    case "basement_cap", "basement_cap_approach" -> editor.towerStackBasementCapHeight(stackId);
                    case "top_cap", "top_cap_approach" -> editor.towerStackMainCapHeight(stackId);
                    default -> editor.towerStackEntryHeight(stackId);
                };
            }

            @Override
            public void height(String sectionKey, int value) {
                switch (sectionKey) {
                    case "entry" -> editor.towerStackEntryHeight(stackId, value);
                    case "main_floor" -> editor.towerStackMainHeight(stackId, value);
                    case "basement_floor" -> editor.towerStackBasementHeight(stackId, value);
                    case "basement_entry" -> editor.towerStackBasementEntryHeight(stackId, value);
                    case "basement_cap", "basement_cap_approach" -> editor.towerStackBasementCapHeight(stackId, value);
                    case "top_cap", "top_cap_approach" -> editor.towerStackMainCapHeight(stackId, value);
                    default -> {
                    }
                }
                screen.flagNeedSetup();
            }

            @Override
            public boolean hasFloorCounts(String sectionKey) {
                return "main_floor".equals(sectionKey) || "basement_floor".equals(sectionKey);
            }

            @Override
            public int minFloors(String sectionKey) {
                return "main_floor".equals(sectionKey) ?
                        editor.towerStackMinMainFloors(stackId) :
                        editor.towerStackMinBasementFloors(stackId);
            }

            @Override
            public int maxFloors(String sectionKey) {
                return "main_floor".equals(sectionKey) ?
                        editor.towerStackMainFloors(stackId) :
                        editor.towerStackBasementFloors(stackId);
            }

            @Override
            public void adjustMinFloors(String sectionKey, int delta) {
                if ("main_floor".equals(sectionKey)) {
                    editor.towerStackMinMainFloors(stackId, Math.max(0, Math.min(
                            editor.towerStackMainFloors(stackId),
                            editor.towerStackMinMainFloors(stackId) + delta)));
                } else if ("basement_floor".equals(sectionKey)) {
                    editor.towerStackMinBasementFloors(stackId, Math.max(0, Math.min(
                            editor.towerStackBasementFloors(stackId),
                            editor.towerStackMinBasementFloors(stackId) + delta)));
                }
                screen.flagNeedSetup();
            }

            @Override
            public void adjustMaxFloors(String sectionKey, int delta) {
                if ("main_floor".equals(sectionKey)) {
                    editor.towerStackMainFloors(stackId, adjustedFloorCount(
                            editor.allowedTowerStackMainFloorCounts(stackId),
                            editor.towerStackMainFloors(stackId), delta));
                } else if ("basement_floor".equals(sectionKey)) {
                    editor.towerStackBasementFloors(stackId, adjustedFloorCount(
                            editor.allowedTowerStackBasementFloorCounts(stackId),
                            editor.towerStackBasementFloors(stackId), delta));
                }
                screen.flagNeedSetup();
            }

            @Override
            public boolean hasMargin(String sectionKey) {
                return "top_cap".equals(sectionKey) || "basement_cap".equals(sectionKey);
            }

            @Override
            public String marginLabel(String sectionKey) {
                return "VMargin";
            }

            @Override
            public int margin(String sectionKey) {
                return "top_cap".equals(sectionKey) ?
                        editor.towerStackTopCapUpperVoidMargin(stackId) :
                        editor.towerStackBottomCapLowerVoidMargin(stackId);
            }

            @Override
            public int marginMax(String sectionKey) {
                int height = "top_cap".equals(sectionKey) ?
                        editor.towerStackMainCapHeight(stackId) :
                        editor.towerStackBasementCapHeight(stackId);
                return Math.max(0, height - MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT);
            }

            @Override
            public void margin(String sectionKey, int value) {
                if ("top_cap".equals(sectionKey)) {
                    editor.towerStackTopCapUpperVoidMargin(stackId, value);
                } else if ("basement_cap".equals(sectionKey)) {
                    editor.towerStackBottomCapLowerVoidMargin(stackId, value);
                }
                screen.flagNeedSetup();
            }

            @Override
            public boolean basementEntryEnabled() {
                return editor.towerStackBasementEntryEnabled(stackId);
            }

            @Override
            public void toggleBasementEntry() {
                editor.towerStackBasementEntryEnabled(stackId, !editor.towerStackBasementEntryEnabled(stackId));
                screen.flagNeedSetup();
            }

            @Override
            public boolean topCapApproachEnabled() {
                return editor.towerStackTopCapApproachEnabled(stackId);
            }

            @Override
            public void toggleTopCapApproach() {
                editor.towerStackTopCapApproachEnabled(stackId,
                        !editor.towerStackTopCapApproachEnabled(stackId));
                screen.flagNeedSetup();
            }

            @Override
            public boolean basementCapApproachEnabled() {
                return editor.towerStackBasementCapApproachEnabled(stackId);
            }

            @Override
            public void toggleBasementCapApproach() {
                editor.towerStackBasementCapApproachEnabled(stackId,
                        !editor.towerStackBasementCapApproachEnabled(stackId));
                screen.flagNeedSetup();
            }

            @Override
            public List<MKWorkspaceFamilyHorizontalExitDefinition> exits(String sectionKey) {
                List<MKWorkspaceFamilyHorizontalExitDefinition> exits = familyForSection(editor, stackId, sectionKey)
                        .map(MKTowerWorkspaceFamilyDefinition::horizontalExits)
                        .orElse(List.of());
                return "entry".equals(sectionKey) ? entryExitsWithRequiredSouth(editor, exits) : exits;
            }

            @Override
            public Optional<MKWorkspaceFamilyHorizontalExitDefinition> selectedExit(String sectionKey) {
                OptionalInt familyIndex = familyIndexForSection(editor, stackId, sectionKey);
                if (familyIndex.isEmpty()) {
                    return Optional.empty();
                }
                int exitIndex = editor.selectedFamilyExitIndex();
                List<MKWorkspaceFamilyHorizontalExitDefinition> exits =
                        editor.draft().familyDefinitions.get(familyIndex.getAsInt()).horizontalExits();
                return exitIndex >= 0 && exitIndex < exits.size() ? Optional.of(exits.get(exitIndex)) :
                        Optional.empty();
            }

            @Override
            public boolean exitRequired(String sectionKey, Direction direction) {
                if (isRequiredEntryExit(sectionKey, direction)) {
                    return true;
                }
                return exitForDirection(sectionKey, direction)
                        .map(exit -> isRequiredTowerStackExit(sectionKey, exit))
                        .orElse(direction.getAxis().isVertical());
            }

            @Override
            public void selectExit(String sectionKey, Direction direction) {
                familyIndexForSection(editor, stackId, sectionKey).ifPresent(familyIndex -> {
                    int exitIndex = editor.findFamilyExitIndexByDirection(familyIndex, direction);
                    editor.selectedFamilyExitIndex(exitIndex == editor.selectedFamilyExitIndex() ? -1 : exitIndex);
                    screen.flagNeedSetup();
                });
            }

            @Override
            public void toggleExit(String sectionKey, Direction direction) {
                if (direction.getAxis().isVertical()) {
                    return;
                }
                if (isRequiredEntryExit(sectionKey, direction)) {
                    return;
                }
                familyIndexForSection(editor, stackId, sectionKey).ifPresent(familyIndex -> {
                    int exitIndex = editor.findFamilyExitIndexByDirection(familyIndex, direction);
                    if (exitIndex >= 0) {
                        MKWorkspaceFamilyHorizontalExitDefinition exit =
                                editor.draft().familyDefinitions.get(familyIndex).horizontalExits().get(exitIndex);
                        if (isRequiredTowerStackExit(sectionKey, exit)) {
                            return;
                        }
                        if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.BRANCH) {
                            editor.removeFamilyExit(familyIndex, exitIndex);
                            if (editor.selectedFamilyExitIndex() == exitIndex) {
                                editor.selectedFamilyExitIndex(-1);
                            } else if (editor.selectedFamilyExitIndex() > exitIndex) {
                                editor.selectedFamilyExitIndex(editor.selectedFamilyExitIndex() - 1);
                            }
                        } else {
                            String branchOpeningProfileId = editor.ensureCompatibleOpeningProfile(
                                    MKWorkspaceHorizontalExitPathKind.BRANCH, exit.openingProfileId());
                            editor.replaceFamilyExit(familyIndex, exitIndex,
                                    new MKWorkspaceFamilyHorizontalExitDefinition(
                                            exit.direction(),
                                            MKWorkspaceHorizontalExitPathKind.BRANCH,
                                            branchOpeningProfileId,
                            exit.connectionMode(),
                            exit.sideOffset(),
                            exit.verticalOffset(),
                            exit.horizontalExtrusionModeOverride()
                    ));
                            editor.selectedFamilyExitIndex(exitIndex);
                        }
                    } else {
                        editor.selectedFamilyExitIndex(editor.addFamilyBranchExitAtDirection(familyIndex, direction));
                    }
                    screen.flagNeedSetup();
                });
            }

            @Override
            public void cycleSelectedExitDirection(String sectionKey, boolean reverse) {
                updateSelectedExit(sectionKey, exit -> {
                    if (isRequiredTowerStackExit(sectionKey, exit)) {
                        return exit;
                    }
                    MKTowerWorkspaceFamilyDefinition family = selectedFamily(sectionKey).orElse(null);
                    if (family == null) {
                        return exit;
                    }
                    Direction nextDirection = cycleMutableCardinalDirection(sectionKey, exit.direction(), reverse);
                    return new MKWorkspaceFamilyHorizontalExitDefinition(
                            nextDirection,
                            exit.pathKind(),
                            exit.openingProfileId(),
                            exit.connectionMode(),
                            editor.clampSideOffset(family, nextDirection, exit.openingProfileId(), exit.sideOffset()),
                            editor.clampVerticalOffset(family, exit.openingProfileId(), exit.verticalOffset()),
                            exit.horizontalExtrusionModeOverride()
                    );
                });
            }

            @Override
            public void cycleSelectedExitPathKind(String sectionKey, boolean reverse) {
                updateSelectedExit(sectionKey, exit -> {
                    if (isRequiredTowerStackExit(sectionKey, exit)) {
                        return exit;
                    }
                    MKTowerWorkspaceFamilyDefinition family = selectedFamily(sectionKey).orElse(null);
                    if (family == null) {
                        return exit;
                    }
                    MKWorkspaceHorizontalExitPathKind nextPathKind = WorkspaceTopologyUiSupport.cycleValue(
                            allowedPathKinds(sectionKey),
                            exit.pathKind(), reverse);
                    String nextOpeningProfileId =
                            editor.ensureCompatibleOpeningProfile(nextPathKind, exit.openingProfileId());
                    return new MKWorkspaceFamilyHorizontalExitDefinition(
                            exit.direction(),
                            nextPathKind,
                            nextOpeningProfileId,
                            exit.connectionMode(),
                            editor.clampSideOffset(family, exit.direction(), nextOpeningProfileId, exit.sideOffset()),
                            editor.clampVerticalOffset(family, nextOpeningProfileId, exit.verticalOffset()),
                            exit.horizontalExtrusionModeOverride()
                    );
                });
            }

            private List<MKWorkspaceHorizontalExitPathKind> allowedPathKinds(String sectionKey) {
                return List.of(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                        MKWorkspaceHorizontalExitPathKind.BRANCH);
            }

            @Override
            public void cycleSelectedExitConnectionMode(String sectionKey, boolean reverse) {
                updateSelectedExit(sectionKey, exit -> {
                    if (isRequiredTowerStackExit(sectionKey, exit)) {
                        return exit;
                    }
                    return new MKWorkspaceFamilyHorizontalExitDefinition(
                            exit.direction(),
                            exit.pathKind(),
                            exit.openingProfileId(),
                            WorkspaceTopologyUiSupport.cycleValue(List.of(MKWorkspaceHorizontalExitConnectionMode.values()),
                                    exit.connectionMode(), reverse),
                            exit.sideOffset(),
                            exit.verticalOffset(),
                            exit.horizontalExtrusionModeOverride()
                    );
                });
            }

            @Override
            public void cycleSelectedExitOpeningProfile(String sectionKey, boolean reverse) {
                updateSelectedExit(sectionKey, exit -> {
                    MKTowerWorkspaceFamilyDefinition family = selectedFamily(sectionKey).orElse(null);
                    if (family == null) {
                        return exit;
                    }
                    String nextOpeningProfileId =
                            editor.nextOpeningProfileId(exit.pathKind(), exit.openingProfileId(), reverse);
                    return new MKWorkspaceFamilyHorizontalExitDefinition(
                            exit.direction(),
                            exit.pathKind(),
                            nextOpeningProfileId,
                            exit.connectionMode(),
                            editor.clampSideOffset(family, exit.direction(), nextOpeningProfileId, exit.sideOffset()),
                            editor.clampVerticalOffset(family, nextOpeningProfileId, exit.verticalOffset()),
                            exit.horizontalExtrusionModeOverride()
                    );
                });
            }

            @Override
            public int selectedExitSideMin(String sectionKey) {
                return selectedExit(sectionKey)
                        .flatMap(exit -> selectedFamily(sectionKey).map(family ->
                                editor.minSideOffset(family, exit.direction(), exit.openingProfileId())))
                        .orElse(0);
            }

            @Override
            public int selectedExitSideMax(String sectionKey) {
                return selectedExit(sectionKey)
                        .flatMap(exit -> selectedFamily(sectionKey).map(family ->
                                editor.maxSideOffset(family, exit.direction(), exit.openingProfileId())))
                        .orElse(0);
            }

            @Override
            public int selectedExitVerticalMax(String sectionKey) {
                return selectedExit(sectionKey)
                        .flatMap(exit -> selectedFamily(sectionKey).map(family ->
                                editor.maxVerticalOffset(family, exit.openingProfileId())))
                        .orElse(0);
            }

            @Override
            public void selectedExitSideOffset(String sectionKey, int value) {
                OptionalInt familyIndex = familyIndexForSection(editor, stackId, sectionKey);
                if (familyIndex.isPresent() && selectedExit(sectionKey).isPresent()) {
                    editor.updateFamilyExitOffsets(familyIndex.getAsInt(), editor.selectedFamilyExitIndex(),
                            value, null);
                    screen.flagNeedSetup();
                }
            }

            @Override
            public void selectedExitVerticalOffset(String sectionKey, int value) {
                OptionalInt familyIndex = familyIndexForSection(editor, stackId, sectionKey);
                if (familyIndex.isPresent() && selectedExit(sectionKey).isPresent()) {
                    editor.updateFamilyExitOffsets(familyIndex.getAsInt(), editor.selectedFamilyExitIndex(),
                            null, value);
                    screen.flagNeedSetup();
                }
            }

            @Override
            public boolean hasFloorTopology(String sectionKey) {
                return !"top_cap".equals(sectionKey) && !"basement_cap".equals(sectionKey);
            }

            @Override
            public int floorMinMainPathPieces(String sectionKey) {
                return editor.floorTopologyMinMainPathPieces(stackId, sectionKey);
            }

            @Override
            public void floorMinMainPathPieces(String sectionKey, int value) {
                editor.floorTopologyMinMainPathPieces(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxMainPathPieces(String sectionKey) {
                return editor.floorTopologyMaxMainPathPieces(stackId, sectionKey);
            }

            @Override
            public void floorMaxMainPathPieces(String sectionKey, int value) {
                editor.floorTopologyMaxMainPathPieces(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxBranchPiecesBeforeCap(String sectionKey) {
                return editor.floorTopologyMaxBranchPiecesBeforeCap(stackId, sectionKey);
            }

            @Override
            public void floorMaxBranchPiecesBeforeCap(String sectionKey, int value) {
                editor.floorTopologyMaxBranchPiecesBeforeCap(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public MKWorkspaceHallwayLeadInMode floorHallwayLeadInMode(String sectionKey) {
                return editor.floorTopologyHallwayLeadInMode(stackId, sectionKey);
            }

            @Override
            public void cycleFloorHallwayLeadInMode(String sectionKey, boolean reverse) {
                editor.floorTopologyHallwayLeadInMode(stackId, sectionKey,
                        WorkspaceTopologyUiSupport.cycleValue(List.of(MKWorkspaceHallwayLeadInMode.values()),
                                editor.floorTopologyHallwayLeadInMode(stackId, sectionKey), reverse));
                screen.flagNeedSetup();
            }

            @Override
            public int floorManualHallwayLeadInPieces(String sectionKey) {
                return editor.floorTopologyManualHallwayLeadInPieces(stackId, sectionKey);
            }

            @Override
            public void floorManualHallwayLeadInPieces(String sectionKey, int value) {
                editor.floorTopologyManualHallwayLeadInPieces(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorMainHallwaysEnabled(String sectionKey) {
                return editor.floorTopologyMainHallwaysEnabled(stackId, sectionKey);
            }

            @Override
            public void floorMainHallwaysEnabled(String sectionKey, boolean value) {
                editor.floorTopologyMainHallwaysEnabled(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorBranchHallwaysEnabled(String sectionKey) {
                return editor.floorTopologyBranchHallwaysEnabled(stackId, sectionKey);
            }

            @Override
            public void floorBranchHallwaysEnabled(String sectionKey, boolean value) {
                editor.floorTopologyBranchHallwaysEnabled(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorMainCapApproachEnabled(String sectionKey) {
                return editor.floorTopologyMainCapApproachEnabled(stackId, sectionKey);
            }

            @Override
            public void floorMainCapApproachEnabled(String sectionKey, boolean value) {
                editor.floorTopologyMainCapApproachEnabled(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int recommendedHallwayLeadInPieces(String sectionKey) {
                return Math.max(1, Math.ceilDiv(Math.max(editor.towerStackWidth(stackId),
                        editor.towerStackLength(stackId)), 8));
            }

            @Override
            public int floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                return editor.floorTopologyRoomWidth(stackId, sectionKey, kind);
            }

            @Override
            public void floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
                editor.floorTopologyRoomWidth(stackId, sectionKey, kind, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                return editor.floorTopologyRoomLength(stackId, sectionKey, kind);
            }

            @Override
            public void floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
                editor.floorTopologyRoomLength(stackId, sectionKey, kind, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                return editor.floorTopologyRoomHeight(stackId, sectionKey, kind);
            }

            @Override
            public void floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
                editor.floorTopologyRoomHeight(stackId, sectionKey, kind, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomHeightMax(String sectionKey) {
                return editor.floorTopologyRoomHeightMax(stackId, sectionKey);
            }

            private Optional<MKWorkspaceFamilyHorizontalExitDefinition> exitForDirection(String sectionKey,
                                                                                         Direction direction) {
                return exits(sectionKey).stream()
                        .filter(exit -> exit.direction() == direction)
                        .findFirst();
            }

            private Optional<MKTowerWorkspaceFamilyDefinition> selectedFamily(String sectionKey) {
                return familyForSection(editor, stackId, sectionKey);
            }

            private void updateSelectedExit(String sectionKey,
                                            java.util.function.Function<MKWorkspaceFamilyHorizontalExitDefinition,
                                                    MKWorkspaceFamilyHorizontalExitDefinition> updater) {
                OptionalInt familyIndex = familyIndexForSection(editor, stackId, sectionKey);
                Optional<MKWorkspaceFamilyHorizontalExitDefinition> exit = selectedExit(sectionKey);
                if (familyIndex.isEmpty() || exit.isEmpty()) {
                    return;
                }
                editor.replaceFamilyExit(familyIndex.getAsInt(), editor.selectedFamilyExitIndex(),
                        updater.apply(exit.get()));
                screen.flagNeedSetup();
            }
        };
    }

    private MKFloorTopologyPlanPreview.Controls floorPlanControls(MKWorkspaceScreen screen,
                                                                  WorkspaceDraftSession editor,
                                                                  String stackId) {
        return new MKFloorTopologyPlanPreview.Controls() {
            @Override
            public boolean hasFloorTopology(String sectionKey) {
                return !"top_cap".equals(sectionKey) && !"basement_cap".equals(sectionKey);
            }

            @Override
            public int stackWidth(String sectionKey) {
                return editor.towerStackWidth(stackId);
            }

            @Override
            public int stackLength(String sectionKey) {
                return editor.towerStackLength(stackId);
            }

            @Override
            public List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits(String sectionKey) {
                List<MKWorkspaceFamilyHorizontalExitDefinition> exits = familyForSection(editor, stackId, sectionKey)
                        .map(MKTowerWorkspaceFamilyDefinition::horizontalExits)
                        .orElse(List.of());
                return "entry".equals(sectionKey) ? entryExitsWithRequiredSouth(editor, exits) : exits;
            }

            @Override
            public int floorMinMainPathPieces(String sectionKey) {
                return editor.floorTopologyMinMainPathPieces(stackId, sectionKey);
            }

            @Override
            public void floorMinMainPathPieces(String sectionKey, int value) {
                editor.floorTopologyMinMainPathPieces(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxMainPathPieces(String sectionKey) {
                return editor.floorTopologyMaxMainPathPieces(stackId, sectionKey);
            }

            @Override
            public void floorMaxMainPathPieces(String sectionKey, int value) {
                editor.floorTopologyMaxMainPathPieces(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxBranchPiecesBeforeCap(String sectionKey) {
                return editor.floorTopologyMaxBranchPiecesBeforeCap(stackId, sectionKey);
            }

            @Override
            public void floorMaxBranchPiecesBeforeCap(String sectionKey, int value) {
                editor.floorTopologyMaxBranchPiecesBeforeCap(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public MKWorkspaceHallwayLeadInMode floorHallwayLeadInMode(String sectionKey) {
                return editor.floorTopologyHallwayLeadInMode(stackId, sectionKey);
            }

            @Override
            public void cycleFloorHallwayLeadInMode(String sectionKey, boolean reverse) {
                editor.floorTopologyHallwayLeadInMode(stackId, sectionKey,
                        WorkspaceTopologyUiSupport.cycleValue(List.of(MKWorkspaceHallwayLeadInMode.values()),
                                editor.floorTopologyHallwayLeadInMode(stackId, sectionKey), reverse));
                screen.flagNeedSetup();
            }

            @Override
            public int floorManualHallwayLeadInPieces(String sectionKey) {
                return editor.floorTopologyManualHallwayLeadInPieces(stackId, sectionKey);
            }

            @Override
            public void floorManualHallwayLeadInPieces(String sectionKey, int value) {
                editor.floorTopologyManualHallwayLeadInPieces(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorMainHallwaysEnabled(String sectionKey) {
                return editor.floorTopologyMainHallwaysEnabled(stackId, sectionKey);
            }

            @Override
            public void floorMainHallwaysEnabled(String sectionKey, boolean value) {
                editor.floorTopologyMainHallwaysEnabled(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorBranchHallwaysEnabled(String sectionKey) {
                return editor.floorTopologyBranchHallwaysEnabled(stackId, sectionKey);
            }

            @Override
            public void floorBranchHallwaysEnabled(String sectionKey, boolean value) {
                editor.floorTopologyBranchHallwaysEnabled(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorMainCapApproachEnabled(String sectionKey) {
                return editor.floorTopologyMainCapApproachEnabled(stackId, sectionKey);
            }

            @Override
            public void floorMainCapApproachEnabled(String sectionKey, boolean value) {
                editor.floorTopologyMainCapApproachEnabled(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int recommendedHallwayLeadInPieces(String sectionKey) {
                return Math.max(1, Math.ceilDiv(Math.max(editor.towerStackWidth(stackId),
                        editor.towerStackLength(stackId)), 8));
            }

            @Override
            public float floorSprawl(String sectionKey) {
                return editor.floorTopologySprawl(stackId, sectionKey);
            }

            @Override
            public void floorSprawl(String sectionKey, float value) {
                editor.floorTopologySprawl(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorLinksEnabled(String sectionKey) {
                return editor.floorTopologyLinksEnabled(stackId, sectionKey);
            }

            @Override
            public void floorLinksEnabled(String sectionKey, boolean value) {
                editor.floorTopologyLinksEnabled(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public float floorLinkDensity(String sectionKey) {
                return editor.floorTopologyLinkDensity(stackId, sectionKey);
            }

            @Override
            public void floorLinkDensity(String sectionKey, float value) {
                editor.floorTopologyLinkDensity(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxLinksPerFloor(String sectionKey) {
                return editor.floorTopologyMaxLinksPerFloor(stackId, sectionKey);
            }

            @Override
            public void floorMaxLinksPerFloor(String sectionKey, int value) {
                editor.floorTopologyMaxLinksPerFloor(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxLinksPerRoom(String sectionKey) {
                return editor.floorTopologyMaxLinksPerRoom(stackId, sectionKey);
            }

            @Override
            public void floorMaxLinksPerRoom(String sectionKey, int value) {
                editor.floorTopologyMaxLinksPerRoom(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxLinkLength(String sectionKey) {
                return editor.floorTopologyMaxLinkLength(stackId, sectionKey);
            }

            @Override
            public void floorMaxLinkLength(String sectionKey, int value) {
                editor.floorTopologyMaxLinkLength(stackId, sectionKey, value);
                screen.flagNeedSetup();
            }

            @Override
            public long previewSeed(String sectionKey) {
                return editor.floorTopologyPreviewSeed(stackId, sectionKey);
            }

            @Override
            public void rerollPreviewSeed(String sectionKey) {
                editor.rerollFloorTopologyPreviewSeed(stackId, sectionKey);
                screen.flagNeedSetup();
            }

            @Override
            public Optional<Long> lockedLayoutSeed(String sectionKey) {
                return editor.floorTopologyLockedLayoutSeed(stackId, sectionKey);
            }

            @Override
            public void lockLayoutSeed(String sectionKey) {
                editor.lockFloorTopologyLayoutSeed(stackId, sectionKey);
                screen.flagNeedSetup();
            }

            @Override
            public void unlockLayoutSeed(String sectionKey) {
                editor.unlockFloorTopologyLayoutSeed(stackId, sectionKey);
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomHeightMin(String sectionKey) {
                return 3;
            }

            @Override
            public int floorRoomHeightMax(String sectionKey) {
                return editor.floorTopologyRoomHeightMax(stackId, sectionKey);
            }

            @Override
            public List<MKWorkspaceFloorRoomProfile> roomProfiles(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                return editor.floorTopologyRoomProfiles(stackId, sectionKey, kind);
            }

            @Override
            public void floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value) {
                editor.floorTopologyRoomWidth(stackId, sectionKey, kind, index, value);
                screen.flagNeedSetup();
            }

            @Override
            public void floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value) {
                editor.floorTopologyRoomLength(stackId, sectionKey, kind, index, value);
                screen.flagNeedSetup();
            }

            @Override
            public void floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value) {
                editor.floorTopologyRoomHeight(stackId, sectionKey, kind, index, value);
                screen.flagNeedSetup();
            }

            @Override
            public void addRoomProfile(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                editor.floorTopologyAddRoomProfile(stackId, sectionKey, kind);
                screen.flagNeedSetup();
            }

            @Override
            public void removeRoomProfile(String sectionKey, MKWorkspaceFloorRoomKind kind, int index) {
                editor.floorTopologyRemoveRoomProfile(stackId, sectionKey, kind, index);
                screen.flagNeedSetup();
            }

            @Override
            public void setRoomMainExitDirection(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                                 Direction direction) {
                editor.floorTopologySetRoomMainExitDirection(stackId, sectionKey, kind, index, direction);
                screen.flagNeedSetup();
            }

            @Override
            public void setRoomRandomizeMainExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                                 boolean value) {
                editor.floorTopologySetRoomRandomizeMainExit(stackId, sectionKey, kind, index, value);
                screen.flagNeedSetup();
            }

            @Override
            public void toggleRoomBranchExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                             Direction direction) {
                editor.floorTopologyToggleRoomBranchExit(stackId, sectionKey, kind, index, direction);
                screen.flagNeedSetup();
            }

            @Override
            public void toggleRoomLinkCandidateExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                                    Direction direction) {
                editor.floorTopologyToggleRoomLinkCandidateExit(stackId, sectionKey, kind, index, direction);
                screen.flagNeedSetup();
            }
        };
    }

    private List<MKWorkspaceFamilyHorizontalExitDefinition> entryExitsWithRequiredSouth(WorkspaceDraftSession editor,
                                                                                        List<MKWorkspaceFamilyHorizontalExitDefinition> exits) {
        ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> resolved = new ArrayList<>();
        boolean hasRequiredEntry = false;
        for (MKWorkspaceFamilyHorizontalExitDefinition exit : exits) {
            if (exit.direction() == Direction.SOUTH) {
                if (exit.pathKind() == MKWorkspaceHorizontalExitPathKind.INGRESS) {
                    resolved.add(exit);
                    hasRequiredEntry = true;
                }
            } else {
                resolved.add(exit);
            }
        }
        if (!hasRequiredEntry) {
            resolved.add(requiredEntryExit(editor));
        }
        return List.copyOf(resolved);
    }

    private Optional<MKTowerWorkspaceFamilyDefinition> familyForSection(WorkspaceDraftSession editor, String stackId,
                                                                       String sectionKey) {
        OptionalInt index = familyIndexForSection(editor, stackId, sectionKey);
        return index.isPresent() ? Optional.of(editor.draft().familyDefinitions.get(index.getAsInt())) :
                Optional.empty();
    }

    private OptionalInt familyIndexForSection(WorkspaceDraftSession editor, String stackId, String sectionKey) {
        String topologySlotId = stackId + "." + sectionKey;
        Optional<MKTowerWorkspaceStackSlot> slot = MKTowerWorkspaceStackSlot.fromTopologySlotId(topologySlotId);
        if (slot.isEmpty()) {
            return OptionalInt.empty();
        }
        for (int index = 0; index < editor.draft().familyDefinitions.size(); index++) {
            if (editor.draft().familyDefinitions.get(index).topologySlotId().equals(topologySlotId)) {
                return OptionalInt.of(index);
            }
        }
        return OptionalInt.empty();
    }

    private MKWorkspaceFamilyHorizontalExitDefinition requiredEntryExit(WorkspaceDraftSession editor) {
        return new MKWorkspaceFamilyHorizontalExitDefinition(
                Direction.SOUTH,
                MKWorkspaceHorizontalExitPathKind.INGRESS,
                editor.firstCompatibleOpeningProfileId(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT)
                        .orElse("main_opening"),
                MKWorkspaceHorizontalExitConnectionMode.NO_CONNECTION
        );
    }

    private boolean isRequiredTowerStackExit(String sectionKey, MKWorkspaceFamilyHorizontalExitDefinition exit) {
        return exit.isVerticalAccess() ||
                isRequiredEntryExit(sectionKey, exit.direction()) &&
                        exit.pathKind() == MKWorkspaceHorizontalExitPathKind.INGRESS;
    }

    private boolean isRequiredEntryExit(String sectionKey, Direction direction) {
        return "entry".equals(sectionKey) && direction == Direction.SOUTH;
    }

    private Direction cycleCardinalDirection(Direction current, boolean reverse) {
        List<Direction> directions = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
        return WorkspaceTopologyUiSupport.cycleValue(directions,
                current.getAxis().isHorizontal() ? current : Direction.NORTH, reverse);
    }

    private Direction cycleMutableCardinalDirection(String sectionKey, Direction current, boolean reverse) {
        if (!"entry".equals(sectionKey)) {
            return cycleCardinalDirection(current, reverse);
        }
        List<Direction> directions = List.of(Direction.NORTH, Direction.EAST, Direction.WEST);
        Direction normalizedCurrent = current.getAxis().isHorizontal() && directions.contains(current) ?
                current : Direction.NORTH;
        return WorkspaceTopologyUiSupport.cycleValue(directions, normalizedCurrent, reverse);
    }

    private int adjustedFloorCount(List<Integer> allowedCounts, int currentCount, int delta) {
        if (allowedCounts.isEmpty() || delta == 0) {
            return currentCount;
        }
        int normalized = allowedCounts.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - currentCount)))
                .orElse(currentCount);
        if (delta > 0) {
            return allowedCounts.stream()
                    .filter(value -> value > normalized)
                    .findFirst()
                    .orElse(normalized);
        }
        return allowedCounts.stream()
                .filter(value -> value < normalized)
                .reduce((first, second) -> second)
                .orElse(normalized);
    }

    private void addFloorRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor,
                              String stackId) {
        addExtrusionRows(screen, content, editor, stackId);
        addStairRows(screen, content, editor, stackId);
        addFoundationRows(screen, content, editor, stackId);
        screen.addPaletteOverrideRows(content, "Stack Palette Defaults", editor.draftBasePalette(),
                editor.towerStackPaletteOverrideOpt(stackId),
                override -> editor.towerStackPaletteOverride(stackId, override));
    }

    private void addExtrusionRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor,
                                  String stackId) {
        MKButton extrusionButton = new MKButton(Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                editor.towerStackHorizontalExtrusionMode(stackId).getSerializedName())), 180, 20);
        extrusionButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackHorizontalExtrusionMode(stackId, WorkspaceTopologyUiSupport.cycleValue(
                    List.of(MKWorkspaceHorizontalExtrusionMode.values()),
                    editor.towerStackHorizontalExtrusionMode(stackId),
                    WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Horizontal Extrusion")), extrusionButton);
    }

    private void addStairRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor,
                              String stackId) {
        MKButton stairModeButton = new MKButton(Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                editor.towerStackStairMode(stackId).getSerializedName())), 180, 20);
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackStairMode(stackId, WorkspaceTopologyUiSupport.cycleValue(List.of(
                    MKWorkspaceStairMode.AUTO,
                    MKWorkspaceStairMode.RUN_PROFILE,
                    MKWorkspaceStairMode.LADDER,
                    MKWorkspaceStairMode.NONE
            ), editor.towerStackStairMode(stackId), WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Mode")),
                stairModeButton);

        MKButton stairRiseButton = new MKButton(Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                editor.towerStackStairRiseType(stackId).getSerializedName())), 180, 20);
        stairRiseButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackStairRiseType(stackId,
                    WorkspaceTopologyUiSupport.cycleValue(List.of(MKWorkspaceStairRiseType.values()),
                            editor.towerStackStairRiseType(stackId),
                            WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Rise Type")),
                stairRiseButton);

        MKButton stairWidthButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackStairWidth(stackId))), 180, 20);
        stairWidthButton.setPressedCallback((button, mouseButton) -> {
            List<Integer> allowedWidths = MKWorkspaceDimensions.getAllowedStairWidths(editor.towerStackShaftSize(stackId));
            int snapped = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(editor.towerStackShaftSize(stackId),
                    editor.towerStackStairWidth(stackId));
            editor.towerStackStairWidth(stackId,
                    WorkspaceTopologyUiSupport.cycleValue(allowedWidths, snapped,
                            WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Width")),
                stairWidthButton);
    }

    private void addFoundationRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor,
                                   String stackId) {
        MKWorkspaceFoundationPolicy foundationPolicy = editor.towerStackFoundationPolicy(stackId);
        MKButton modeButton = new MKButton(Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                foundationPolicy.mode().getSerializedName())), 180, 20);
        modeButton.setPressedCallback((button, mouseButton) -> {
            MKWorkspaceFoundationMode mode = WorkspaceTopologyUiSupport.cycleValue(
                    List.of(MKWorkspaceFoundationMode.values()), foundationPolicy.mode(),
                    WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            editor.towerStackFoundationPolicy(stackId, foundationPolicyForMode(mode, foundationPolicy));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Foundation Mode")),
                modeButton);

        if (foundationPolicy.mode() == MKWorkspaceFoundationMode.UNIFORM_STATE) {
            ResourceLocation blockId = foundationPolicy.foundationBlockOpt()
                    .orElse(ResourceLocation.parse("minecraft:stone"));
            MKButton blockButton = new MKButton(screen.blockDisplayName(blockId), 180, 20);
            blockButton.setTooltip(Component.literal(blockId.toString()));
            blockButton.setPressedCallback((button, mouseButton) -> {
                screen.openBlockPicker("Choose Foundation Block", blockId, value -> {
                    editor.towerStackFoundationPolicy(stackId, MKWorkspaceFoundationPolicy.uniformBlock(value));
                    screen.refreshPreservingActiveScroll();
                }, false);
                return true;
            });
            WorkspaceTopologyUiSupport.addRow(screen, content,
                    screen.makeWhiteText(Component.literal("Foundation Block")), blockButton);
        }
    }

    private MKWorkspaceFoundationPolicy foundationPolicyForMode(MKWorkspaceFoundationMode mode,
                                                                MKWorkspaceFoundationPolicy current) {
        return switch (mode) {
            case NONE -> MKWorkspaceFoundationPolicy.none();
            case UNIFORM_STATE -> current.foundationBlockOpt()
                    .map(MKWorkspaceFoundationPolicy::uniformBlock)
                    .orElse(MKWorkspaceFoundationPolicy.uniformBlock(ResourceLocation.parse("minecraft:stone")));
            case EXTEND_BOTTOM_BLOCKS -> MKWorkspaceFoundationPolicy.extendBottomBlocks();
            case MASKED_EXTEND_BOTTOM_BLOCKS -> current.maskBlocks().isEmpty() ?
                    MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(
                            List.of(ResourceLocation.parse("minecraft:stone"))) :
                    MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(current.maskBlocks());
        };
    }
}
