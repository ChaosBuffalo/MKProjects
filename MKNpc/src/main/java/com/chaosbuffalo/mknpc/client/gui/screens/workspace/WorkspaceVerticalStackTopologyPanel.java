package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorLinkGenerationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFloorRoomProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHallwayLeadInMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomGeometry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWorkspaceVerticalStackSizingReport;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class WorkspaceVerticalStackTopologyPanel {
    public void addStackEditor(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                               WorkspaceDraftSession editor, String stackId, String labelPrefix) {
        addStackEditor(screen, content, editor, stackId, labelPrefix, true);
    }

    public void addStackEditor(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                               WorkspaceDraftSession editor, String stackId, String labelPrefix,
                               boolean showStackSizingControls) {
        addStackPreview(screen, content, editor, stackId, showStackSizingControls);
        addStackSettings(screen, content, editor, stackId, labelPrefix);
    }

    public void addStackEditor(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                               WorkspaceDraftSession editor, String stackId, String labelPrefix) {
        addStackEditor(screen, layout, editor, stackId, labelPrefix, true);
    }

    public void addStackEditor(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                               WorkspaceDraftSession editor, String stackId, String labelPrefix,
                               boolean showStackSizingControls) {
        addStackPreview(screen, layout.previewContent(), editor, stackId, showStackSizingControls);
        addStackSettings(screen, layout.settingsContent(), editor, stackId, labelPrefix);
    }

    public void addStackPreview(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                WorkspaceDraftSession editor, String stackId) {
        addStackPreview(screen, content, editor, stackId, true);
    }

    public void addStackPreview(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                WorkspaceDraftSession editor, String stackId,
                                boolean showStackSizingControls) {
        WorkspaceVerticalStackDraftEditor towerEditor = towerEditor(editor, stackId);
        MKWorkspaceVerticalStackSizingReport report = MKWorkspaceVerticalStackSizingReport.fromSettings(towerEditor.settingsForUi(),
                towerEditor.familiesForUi(), towerEditor.previewFallbackEntryExits());
        String selectedSection = normalizedSelectedSection(towerEditor, report);
        int previewWidth = Math.min(screen.contentWidth(), 320);
        MKWorkspaceVerticalStackSidePreview preview = new MKWorkspaceVerticalStackSidePreview(previewWidth, 440,
                report, selectedSection, sectionKey -> {
            towerEditor.previewSelection(sectionKey);
            screen.flagNeedSetup();
        }, sectionKey -> screen.openWorkspaceFloorPlanNode(stackId, sectionKey),
                controls(screen, editor, stackId, towerEditor), showStackSizingControls);
        content.addWidget(preview);
        content.addConstraintToWidget(new CenterXConstraint(), preview);
    }

    public void addStackSettings(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                 WorkspaceDraftSession editor, String stackId, String labelPrefix) {
        addFloorRows(screen, content, editor, stackId);
        WorkspaceTopologyUiSupport.addResetRow(screen, content, labelPrefix + " Stack", () -> {
            towerEditor(editor, stackId).resetDefaults();
            screen.flagNeedSetup();
        });
    }

    public void addStackSizingRows(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                   WorkspaceDraftSession editor, String stackId, String labelPrefix) {
        WorkspaceVerticalStackDraftEditor towerEditor = towerEditor(editor, stackId);
        MKIntegerSlider widthSlider = new MKIntegerSlider("Width", 180, 20, 3, 45, 2,
                towerEditor.width(), value -> {
            towerEditor.width(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal(labelPrefix + " Width")), widthSlider);

        MKIntegerSlider lengthSlider = new MKIntegerSlider("Length", 180, 20, 3, 45, 2,
                towerEditor.length(), value -> {
            towerEditor.length(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal(labelPrefix + " Length")), lengthSlider);

    }

    public void addFloorPlanEditor(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                   WorkspaceDraftSession editor, String stackId, String sectionKey) {
        MKFloorTopologyPlanPreview.Controls floorControls = floorPlanControls(screen, editor, stackId);
        if (!floorControls.hasFloorTopology(sectionKey)) {
            WorkspaceTopologyUiSupport.addText(screen, layout.settingsContent(), Component.literal(
                    "The selected tower section does not have floor plan controls."));
            return;
        }
        MKFloorTopologyPlanPreview floorPlanPreview = MKFloorTopologyPlanPreview.previewOnly(
                layout.previewWidth(), stackId, sectionKey, floorControls);
        layout.previewContent().addWidget(floorPlanPreview);
        layout.previewContent().addConstraintToWidget(new CenterXConstraint(), floorPlanPreview);
        MKFloorTopologyPlanPreview floorPlanSettings = MKFloorTopologyPlanPreview.settingsOnly(
                layout.settingsWidth(), stackId, sectionKey, floorControls);
        layout.settingsContent().addWidget(floorPlanSettings);
        layout.settingsContent().addConstraintToWidget(new CenterXConstraint(), floorPlanSettings);
        screen.addPaletteOverrideRows(layout.settingsContent(), "Floor Palette Defaults",
                floorEditor(editor, stackId, sectionKey).inheritedPalette(),
                floorEditor(editor, stackId, sectionKey).paletteOverrideOpt(),
                override -> {
                    floorEditor(editor, stackId, sectionKey).paletteOverride(override);
                    screen.flagNeedSetup();
                });
    }

    private String normalizedSelectedSection(WorkspaceVerticalStackDraftEditor towerEditor, MKWorkspaceVerticalStackSizingReport report) {
        String selected = towerEditor.previewSelection();
        boolean valid = report.sections().stream().anyMatch(section -> section.key().equals(selected));
        if (valid) {
            return selected;
        }
        towerEditor.previewSelection("entry");
        return "entry";
    }

    private FloorPlanDraftEditor floorEditor(WorkspaceDraftSession editor, String stackId, String sectionKey) {
        return new FloorPlanDraftEditor(editor, stackId, sectionKey);
    }

    private MKWorkspaceVerticalStackSidePreview.Controls controls(MKWorkspaceScreen screen, WorkspaceDraftSession editor,
                                                      String stackId, WorkspaceVerticalStackDraftEditor towerEditor) {
        return new MKWorkspaceVerticalStackSidePreview.Controls() {
            @Override
            public int stackWidth() {
                return towerEditor.width();
            }

            @Override
            public void stackWidth(int value) {
                towerEditor.width(value);
                screen.flagNeedSetup();
            }

            @Override
            public int stackLength() {
                return towerEditor.length();
            }

            @Override
            public void stackLength(int value) {
                towerEditor.length(value);
                screen.flagNeedSetup();
            }

            @Override
            public int shaftSize() {
                return towerEditor.shaftSize();
            }

            @Override
            public void shaftSize(int value) {
                towerEditor.shaftSize(value);
                screen.flagNeedSetup();
            }

            @Override
            public List<Integer> allowedShaftSizes() {
                return towerEditor.allowedShaftSizes();
            }

            @Override
            public MKVerticalAccessPlacement shaftPlacement() {
                return towerEditor.verticalAccessPlacement();
            }

            @Override
            public void cycleShaftPlacement(boolean reverse) {
                towerEditor.verticalAccessPlacement(WorkspaceTopologyUiSupport.cycleValue(
                        List.of(MKVerticalAccessPlacement.values()), towerEditor.verticalAccessPlacement(), reverse));
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
                    case "entry" -> towerEditor.entryHeight();
                    case "main_floor" -> towerEditor.mainHeight();
                    case "basement_floor" -> towerEditor.basementHeight();
                    case "basement_entry" -> towerEditor.basementEntryHeight();
                    case "basement_cap", "basement_cap_approach" -> towerEditor.basementCapHeight();
                    case "top_cap", "top_cap_approach" -> towerEditor.mainCapHeight();
                    default -> towerEditor.entryHeight();
                };
            }

            @Override
            public void height(String sectionKey, int value) {
                switch (sectionKey) {
                    case "entry" -> towerEditor.entryHeight(value);
                    case "main_floor" -> towerEditor.mainHeight(value);
                    case "basement_floor" -> towerEditor.basementHeight(value);
                    case "basement_entry" -> towerEditor.basementEntryHeight(value);
                    case "basement_cap", "basement_cap_approach" -> towerEditor.basementCapHeight(value);
                    case "top_cap", "top_cap_approach" -> towerEditor.mainCapHeight(value);
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
                        towerEditor.minMainFloors() :
                        towerEditor.minBasementFloors();
            }

            @Override
            public int maxFloors(String sectionKey) {
                return "main_floor".equals(sectionKey) ?
                        towerEditor.mainFloors() :
                        towerEditor.basementFloors();
            }

            @Override
            public void adjustMinFloors(String sectionKey, int delta) {
                if ("main_floor".equals(sectionKey)) {
                    towerEditor.minMainFloors(Math.max(0, Math.min(
                            towerEditor.mainFloors(),
                            towerEditor.minMainFloors() + delta)));
                } else if ("basement_floor".equals(sectionKey)) {
                    towerEditor.minBasementFloors(Math.max(0, Math.min(
                            towerEditor.basementFloors(),
                            towerEditor.minBasementFloors() + delta)));
                }
                screen.flagNeedSetup();
            }

            @Override
            public void adjustMaxFloors(String sectionKey, int delta) {
                if ("main_floor".equals(sectionKey)) {
                    towerEditor.mainFloors(adjustedFloorCount(
                            towerEditor.allowedMainFloorCounts(),
                            towerEditor.mainFloors(), delta));
                } else if ("basement_floor".equals(sectionKey)) {
                    towerEditor.basementFloors(adjustedFloorCount(
                            towerEditor.allowedBasementFloorCounts(),
                            towerEditor.basementFloors(), delta));
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
                        towerEditor.topCapUpperVoidMargin() :
                        towerEditor.bottomCapLowerVoidMargin();
            }

            @Override
            public int marginMax(String sectionKey) {
                int height = "top_cap".equals(sectionKey) ?
                        towerEditor.mainCapHeight() :
                        towerEditor.basementCapHeight();
                return Math.max(0, height - MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT);
            }

            @Override
            public void margin(String sectionKey, int value) {
                if ("top_cap".equals(sectionKey)) {
                    towerEditor.topCapUpperVoidMargin(value);
                } else if ("basement_cap".equals(sectionKey)) {
                    towerEditor.bottomCapLowerVoidMargin(value);
                }
                screen.flagNeedSetup();
            }

            @Override
            public boolean basementEntryEnabled() {
                return towerEditor.basementEntryEnabled();
            }

            @Override
            public void toggleBasementEntry() {
                towerEditor.basementEntryEnabled(!towerEditor.basementEntryEnabled());
                screen.flagNeedSetup();
            }

            @Override
            public boolean topCapApproachEnabled() {
                return towerEditor.topCapApproachEnabled();
            }

            @Override
            public void toggleTopCapApproach() {
                towerEditor.topCapApproachEnabled(!towerEditor.topCapApproachEnabled());
                screen.flagNeedSetup();
            }

            @Override
            public boolean basementCapApproachEnabled() {
                return towerEditor.basementCapApproachEnabled();
            }

            @Override
            public void toggleBasementCapApproach() {
                towerEditor.basementCapApproachEnabled(!towerEditor.basementCapApproachEnabled());
                screen.flagNeedSetup();
            }

            @Override
            public List<MKWorkspaceFamilyHorizontalExitDefinition> exits(String sectionKey) {
                List<MKWorkspaceFamilyHorizontalExitDefinition> exits = familyForSection(editor, stackId, sectionKey)
                        .map(MKWorkspaceRoomFamilyDefinition::horizontalExits)
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
                        .map(exit -> isRequiredVerticalStackExit(sectionKey, exit))
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
                        if (isRequiredVerticalStackExit(sectionKey, exit)) {
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
                    if (isRequiredVerticalStackExit(sectionKey, exit)) {
                        return exit;
                    }
                    MKWorkspaceRoomFamilyDefinition family = selectedFamily(sectionKey).orElse(null);
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
                    if (isRequiredVerticalStackExit(sectionKey, exit)) {
                        return exit;
                    }
                    MKWorkspaceRoomFamilyDefinition family = selectedFamily(sectionKey).orElse(null);
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
                    if (isRequiredVerticalStackExit(sectionKey, exit)) {
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
                    MKWorkspaceRoomFamilyDefinition family = selectedFamily(sectionKey).orElse(null);
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
                return true;
            }

            @Override
            public int floorMinMainPathPieces(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).minMainPathPieces();
            }

            @Override
            public void floorMinMainPathPieces(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).minMainPathPieces(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxMainPathPieces(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).maxMainPathPieces();
            }

            @Override
            public void floorMaxMainPathPieces(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).maxMainPathPieces(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxBranchPiecesBeforeCap(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).maxBranchPiecesBeforeCap();
            }

            @Override
            public void floorMaxBranchPiecesBeforeCap(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).maxBranchPiecesBeforeCap(value);
                screen.flagNeedSetup();
            }

            @Override
            public MKWorkspaceHallwayLeadInMode floorHallwayLeadInMode(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).hallwayLeadInMode();
            }

            @Override
            public void cycleFloorHallwayLeadInMode(String sectionKey, boolean reverse) {
                FloorPlanDraftEditor floorEditor = floorEditor(editor, stackId, sectionKey);
                floorEditor.hallwayLeadInMode(WorkspaceTopologyUiSupport.cycleValue(
                        List.of(MKWorkspaceHallwayLeadInMode.values()), floorEditor.hallwayLeadInMode(), reverse));
                screen.flagNeedSetup();
            }

            @Override
            public int floorManualHallwayLeadInPieces(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).manualHallwayLeadInPieces();
            }

            @Override
            public void floorManualHallwayLeadInPieces(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).manualHallwayLeadInPieces(value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorMainHallwaysEnabled(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).mainHallwaysEnabled();
            }

            @Override
            public void floorMainHallwaysEnabled(String sectionKey, boolean value) {
                floorEditor(editor, stackId, sectionKey).mainHallwaysEnabled(value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorBranchHallwaysEnabled(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).branchHallwaysEnabled();
            }

            @Override
            public void floorBranchHallwaysEnabled(String sectionKey, boolean value) {
                floorEditor(editor, stackId, sectionKey).branchHallwaysEnabled(value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorMainCapApproachEnabled(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).mainCapApproachEnabled();
            }

            @Override
            public void floorMainCapApproachEnabled(String sectionKey, boolean value) {
                floorEditor(editor, stackId, sectionKey).mainCapApproachEnabled(value);
                screen.flagNeedSetup();
            }

            @Override
            public int recommendedHallwayLeadInPieces(String sectionKey) {
                return Math.max(1, Math.ceilDiv(Math.max(towerEditor.width(),
                        towerEditor.length()), 8));
            }

            @Override
            public int floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                return floorEditor(editor, stackId, sectionKey).roomWidth(kind);
            }

            @Override
            public void floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
                floorEditor(editor, stackId, sectionKey).roomWidth(kind, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                return floorEditor(editor, stackId, sectionKey).roomLength(kind);
            }

            @Override
            public void floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
                floorEditor(editor, stackId, sectionKey).roomLength(kind, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                return floorEditor(editor, stackId, sectionKey).roomHeight(kind);
            }

            @Override
            public void floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind, int value) {
                floorEditor(editor, stackId, sectionKey).roomHeight(kind, value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomHeightMax(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).roomHeightMax();
            }

            private Optional<MKWorkspaceFamilyHorizontalExitDefinition> exitForDirection(String sectionKey,
                                                                                         Direction direction) {
                return exits(sectionKey).stream()
                        .filter(exit -> exit.direction() == direction)
                        .findFirst();
            }

            private Optional<MKWorkspaceRoomFamilyDefinition> selectedFamily(String sectionKey) {
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
        WorkspaceVerticalStackDraftEditor towerEditor = towerEditor(editor, stackId);
        return new MKFloorTopologyPlanPreview.Controls() {
            @Override
            public boolean hasFloorTopology(String sectionKey) {
                return true;
            }

            @Override
            public int stackWidth(String sectionKey) {
                return towerEditor.width();
            }

            @Override
            public int stackLength(String sectionKey) {
                return towerEditor.length();
            }

            @Override
            public List<MKWorkspaceFamilyHorizontalExitDefinition> rootExits(String sectionKey) {
                List<MKWorkspaceFamilyHorizontalExitDefinition> exits = familyForSection(editor, stackId, sectionKey)
                        .map(MKWorkspaceRoomFamilyDefinition::horizontalExits)
                        .orElse(List.of());
                return "entry".equals(sectionKey) ? entryExitsWithRequiredSouth(editor, exits) : exits;
            }

            @Override
            public Optional<MKWorkspaceFamilyHorizontalExitDefinition> selectedRootExit(String sectionKey) {
                OptionalInt familyIndex = familyIndexForSection(editor, stackId, sectionKey);
                if (familyIndex.isEmpty()) {
                    return Optional.empty();
                }
                int exitIndex = editor.selectedFamilyExitIndex();
                List<MKWorkspaceFamilyHorizontalExitDefinition> exits =
                        editor.draft().familyDefinitions.get(familyIndex.getAsInt()).horizontalExits();
                return exitIndex >= 0 && exitIndex < exits.size() &&
                        exits.get(exitIndex).direction().getAxis().isHorizontal() ?
                        Optional.of(exits.get(exitIndex)) : Optional.empty();
            }

            @Override
            public boolean rootExitRequired(String sectionKey, Direction direction) {
                if (isRequiredEntryExit(sectionKey, direction)) {
                    return true;
                }
                return rootExits(sectionKey).stream()
                        .filter(exit -> exit.direction() == direction)
                        .findFirst()
                        .map(exit -> isRequiredVerticalStackExit(sectionKey, exit))
                        .orElse(false);
            }

            @Override
            public void selectRootExit(String sectionKey, Direction direction) {
                familyIndexForSection(editor, stackId, sectionKey).ifPresent(familyIndex -> {
                    int exitIndex = editor.findFamilyExitIndexByDirection(familyIndex, direction);
                    editor.selectedFamilyExitIndex(exitIndex == editor.selectedFamilyExitIndex() ? -1 : exitIndex);
                    screen.flagNeedSetup();
                });
            }

            @Override
            public void toggleRootExit(String sectionKey, Direction direction) {
                if (isRequiredEntryExit(sectionKey, direction)) {
                    return;
                }
                familyIndexForSection(editor, stackId, sectionKey).ifPresent(familyIndex -> {
                    int exitIndex = editor.findFamilyExitIndexByDirection(familyIndex, direction);
                    if (exitIndex >= 0) {
                        MKWorkspaceFamilyHorizontalExitDefinition exit =
                                editor.draft().familyDefinitions.get(familyIndex).horizontalExits().get(exitIndex);
                        if (isRequiredVerticalStackExit(sectionKey, exit)) {
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
            public void cycleRootExitPathKind(String sectionKey, boolean reverse) {
                updateSelectedRootExit(sectionKey, exit -> {
                    if (isRequiredVerticalStackExit(sectionKey, exit)) {
                        return exit;
                    }
                    MKWorkspaceRoomFamilyDefinition family = selectedFamily(sectionKey).orElse(null);
                    if (family == null) {
                        return exit;
                    }
                    MKWorkspaceHorizontalExitPathKind nextPathKind = WorkspaceTopologyUiSupport.cycleValue(
                            List.of(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT, MKWorkspaceHorizontalExitPathKind.BRANCH),
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

            @Override
            public void cycleRootExitOpeningProfile(String sectionKey, boolean reverse) {
                updateSelectedRootExit(sectionKey, exit -> {
                    MKWorkspaceRoomFamilyDefinition family = selectedFamily(sectionKey).orElse(null);
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
            public int rootExitSideMin(String sectionKey) {
                return selectedRootExit(sectionKey)
                        .flatMap(exit -> selectedFamily(sectionKey).map(family ->
                                editor.minSideOffset(family, exit.direction(), exit.openingProfileId())))
                        .orElse(0);
            }

            @Override
            public int rootExitSideMax(String sectionKey) {
                return selectedRootExit(sectionKey)
                        .flatMap(exit -> selectedFamily(sectionKey).map(family ->
                                editor.maxSideOffset(family, exit.direction(), exit.openingProfileId())))
                        .orElse(0);
            }

            @Override
            public int rootExitVerticalMax(String sectionKey) {
                return selectedRootExit(sectionKey)
                        .flatMap(exit -> selectedFamily(sectionKey).map(family ->
                                editor.maxVerticalOffset(family, exit.openingProfileId())))
                        .orElse(0);
            }

            @Override
            public void rootExitSideOffset(String sectionKey, int value) {
                OptionalInt familyIndex = familyIndexForSection(editor, stackId, sectionKey);
                if (familyIndex.isPresent() && selectedRootExit(sectionKey).isPresent()) {
                    editor.updateFamilyExitOffsets(familyIndex.getAsInt(), editor.selectedFamilyExitIndex(),
                            value, null);
                    screen.flagNeedSetup();
                }
            }

            @Override
            public void rootExitVerticalOffset(String sectionKey, int value) {
                OptionalInt familyIndex = familyIndexForSection(editor, stackId, sectionKey);
                if (familyIndex.isPresent() && selectedRootExit(sectionKey).isPresent()) {
                    editor.updateFamilyExitOffsets(familyIndex.getAsInt(), editor.selectedFamilyExitIndex(),
                            null, value);
                    screen.flagNeedSetup();
                }
            }

            @Override
            public int floorRootVariantCount(String sectionKey) {
                String baseName = floorRootVariantBaseName(editor, stackId, sectionKey);
                if (baseName.isBlank() || screen.workspace() == null) {
                    return 0;
                }
                return (int) screen.workspace().pieces().stream()
                        .filter(piece -> baseName.equals(piece.tags().get("workspace_base_name")))
                        .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                        .count();
            }

            @Override
            public boolean floorRootVariantsExpanded(String sectionKey) {
                return editor.viewState.floorRootVariantDrawers.getOrDefault(
                        floorRootVariantDrawerKey(stackId, sectionKey), false);
            }

            @Override
            public void toggleFloorRootVariants(String sectionKey) {
                String key = floorRootVariantDrawerKey(stackId, sectionKey);
                boolean expanded = !editor.viewState.floorRootVariantDrawers.getOrDefault(key, false);
                editor.viewState.floorRootVariantDrawers.put(key, expanded);
                screen.flagNeedSetup();
            }

            @Override
            public void addFloorRootVariant(String sectionKey) {
                String baseName = floorRootVariantBaseName(editor, stackId, sectionKey);
                if (!baseName.isBlank()) {
                    PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(screen.anchor(), baseName));
                }
            }

            @Override
            public void openFloorRootVariants(String sectionKey) {
                String topologySlot = floorRootTopologySlot(editor, stackId, sectionKey);
                if (!topologySlot.isBlank()) {
                    screen.openWorkspaceTopologySlotForPrefix(topologySlot);
                }
            }

            @Override
            public int floorHallVariantCount(String sectionKey, boolean main) {
                String baseName = floorHallVariantBaseName(editor, stackId, sectionKey, main);
                if (baseName.isBlank() || screen.workspace() == null) {
                    return 0;
                }
                return (int) screen.workspace().pieces().stream()
                        .filter(piece -> baseName.equals(piece.tags().getOrDefault("workspace_base_name",
                                piece.pieceName())))
                        .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                        .count();
            }

            @Override
            public boolean floorHallVariantsExpanded(String sectionKey, boolean main) {
                return editor.viewState.floorHallVariantDrawers.getOrDefault(
                        floorHallVariantDrawerKey(stackId, sectionKey, main), false);
            }

            @Override
            public void toggleFloorHallVariants(String sectionKey, boolean main) {
                String key = floorHallVariantDrawerKey(stackId, sectionKey, main);
                boolean expanded = !editor.viewState.floorHallVariantDrawers.getOrDefault(key, false);
                editor.viewState.floorHallVariantDrawers.put(key, expanded);
                screen.flagNeedSetup();
            }

            @Override
            public void addFloorHallVariant(String sectionKey, boolean main) {
                String baseName = floorHallVariantBaseName(editor, stackId, sectionKey, main);
                if (!baseName.isBlank()) {
                    PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(screen.anchor(), baseName));
                }
            }

            @Override
            public void openFloorHallVariants(String sectionKey, boolean main) {
                screen.openWorkspaceTopologySlotForPrefix(floorHallTopologySlot(main));
            }

            @Override
            public int floorMinMainPathPieces(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).minMainPathPieces();
            }

            @Override
            public void floorMinMainPathPieces(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).minMainPathPieces(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxMainPathPieces(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).maxMainPathPieces();
            }

            @Override
            public void floorMaxMainPathPieces(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).maxMainPathPieces(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxBranchPiecesBeforeCap(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).maxBranchPiecesBeforeCap();
            }

            @Override
            public void floorMaxBranchPiecesBeforeCap(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).maxBranchPiecesBeforeCap(value);
                screen.flagNeedSetup();
            }

            @Override
            public MKWorkspaceHallwayLeadInMode floorHallwayLeadInMode(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).hallwayLeadInMode();
            }

            @Override
            public void cycleFloorHallwayLeadInMode(String sectionKey, boolean reverse) {
                FloorPlanDraftEditor floorEditor = floorEditor(editor, stackId, sectionKey);
                floorEditor.hallwayLeadInMode(WorkspaceTopologyUiSupport.cycleValue(
                        List.of(MKWorkspaceHallwayLeadInMode.values()), floorEditor.hallwayLeadInMode(), reverse));
                screen.flagNeedSetup();
            }

            @Override
            public int floorManualHallwayLeadInPieces(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).manualHallwayLeadInPieces();
            }

            @Override
            public void floorManualHallwayLeadInPieces(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).manualHallwayLeadInPieces(value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorMainHallwaysEnabled(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).mainHallwaysEnabled();
            }

            @Override
            public void floorMainHallwaysEnabled(String sectionKey, boolean value) {
                floorEditor(editor, stackId, sectionKey).mainHallwaysEnabled(value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorBranchHallwaysEnabled(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).branchHallwaysEnabled();
            }

            @Override
            public void floorBranchHallwaysEnabled(String sectionKey, boolean value) {
                floorEditor(editor, stackId, sectionKey).branchHallwaysEnabled(value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorMainCapApproachEnabled(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).mainCapApproachEnabled();
            }

            @Override
            public void floorMainCapApproachEnabled(String sectionKey, boolean value) {
                floorEditor(editor, stackId, sectionKey).mainCapApproachEnabled(value);
                screen.flagNeedSetup();
            }

            @Override
            public int recommendedHallwayLeadInPieces(String sectionKey) {
                return Math.max(1, Math.ceilDiv(Math.max(towerEditor.width(),
                        towerEditor.length()), 8));
            }

            @Override
            public int layoutFootprintPadding() {
                return 2 * (editor.shellMargin() + editor.exteriorAirMargin());
            }

            @Override
            public float floorSprawl(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).sprawl();
            }

            @Override
            public void floorSprawl(String sectionKey, float value) {
                floorEditor(editor, stackId, sectionKey).sprawl(value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorLinksEnabled(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).linksEnabled();
            }

            @Override
            public void floorLinksEnabled(String sectionKey, boolean value) {
                floorEditor(editor, stackId, sectionKey).linksEnabled(value);
                screen.flagNeedSetup();
            }

            @Override
            public float floorLinkDensity(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).linkDensity();
            }

            @Override
            public void floorLinkDensity(String sectionKey, float value) {
                floorEditor(editor, stackId, sectionKey).linkDensity(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxLinksPerFloor(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).maxLinksPerFloor();
            }

            @Override
            public void floorMaxLinksPerFloor(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).maxLinksPerFloor(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxLinksPerRoom(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).maxLinksPerRoom();
            }

            @Override
            public void floorMaxLinksPerRoom(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).maxLinksPerRoom(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorMaxLinkLength(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).maxLinkLength();
            }

            @Override
            public void floorMaxLinkLength(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).maxLinkLength(value);
                screen.flagNeedSetup();
            }

            @Override
            public MKWorkspaceFloorLinkGenerationMode floorLinkGenerationMode(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).linkGenerationMode();
            }

            @Override
            public void cycleFloorLinkGenerationMode(String sectionKey, boolean reverse) {
                FloorPlanDraftEditor floorEditor = floorEditor(editor, stackId, sectionKey);
                floorEditor.linkGenerationMode(WorkspaceTopologyUiSupport.cycleValue(
                        List.of(MKWorkspaceFloorLinkGenerationMode.values()), floorEditor.linkGenerationMode(),
                        reverse));
                screen.flagNeedSetup();
            }

            @Override
            public float floorLinkDecay(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).linkDecay();
            }

            @Override
            public void floorLinkDecay(String sectionKey, float value) {
                floorEditor(editor, stackId, sectionKey).linkDecay(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorEndpointIntactRadius(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).endpointIntactRadius();
            }

            @Override
            public void floorEndpointIntactRadius(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).endpointIntactRadius(value);
                screen.flagNeedSetup();
            }

            @Override
            public float floorMiddleDecayBonus(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).middleDecayBonus();
            }

            @Override
            public void floorMiddleDecayBonus(String sectionKey, float value) {
                floorEditor(editor, stackId, sectionKey).middleDecayBonus(value);
                screen.flagNeedSetup();
            }

            @Override
            public boolean floorLinkInsertsEnabled(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).linkInsertsEnabled();
            }

            @Override
            public void floorLinkInsertsEnabled(String sectionKey, boolean value) {
                floorEditor(editor, stackId, sectionKey).linkInsertsEnabled(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorInsertDepth(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).insertDepth();
            }

            @Override
            public void floorInsertDepth(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).insertDepth(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorInsertSpacing(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).insertSpacing();
            }

            @Override
            public void floorInsertSpacing(String sectionKey, int value) {
                floorEditor(editor, stackId, sectionKey).insertSpacing(value);
                screen.flagNeedSetup();
            }

            @Override
            public float floorInsertProbability(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).insertProbability();
            }

            @Override
            public void floorInsertProbability(String sectionKey, float value) {
                floorEditor(editor, stackId, sectionKey).insertProbability(value);
                screen.flagNeedSetup();
            }

            @Override
            public float floorInsertMaxDecay(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).insertMaxDecay();
            }

            @Override
            public void floorInsertMaxDecay(String sectionKey, float value) {
                floorEditor(editor, stackId, sectionKey).insertMaxDecay(value);
                screen.flagNeedSetup();
            }

            @Override
            public int floorInsertVariantCount(String sectionKey) {
                String baseName = floorInsertVariantBaseName(editor, stackId, sectionKey);
                if (baseName.isBlank() || screen.workspace() == null) {
                    return 0;
                }
                return (int) screen.workspace().pieces().stream()
                        .filter(piece -> baseName.equals(piece.tags().getOrDefault("workspace_base_name",
                                piece.pieceName())))
                        .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                        .count();
            }

            @Override
            public boolean floorInsertVariantsExpanded(String sectionKey) {
                return editor.viewState.floorInsertVariantDrawers.getOrDefault(
                        floorInsertVariantDrawerKey(stackId, sectionKey), false);
            }

            @Override
            public void toggleFloorInsertVariants(String sectionKey) {
                String key = floorInsertVariantDrawerKey(stackId, sectionKey);
                boolean expanded = !editor.viewState.floorInsertVariantDrawers.getOrDefault(key, false);
                editor.viewState.floorInsertVariantDrawers.put(key, expanded);
                screen.flagNeedSetup();
            }

            @Override
            public void addFloorInsertVariant(String sectionKey) {
                String baseName = floorInsertVariantBaseName(editor, stackId, sectionKey);
                if (!baseName.isBlank()) {
                    PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(screen.anchor(), baseName));
                }
            }

            @Override
            public void openFloorInsertVariants(String sectionKey) {
                screen.openWorkspaceTopologySlotForPrefix(floorInsertTopologySlot());
            }

            @Override
            public long previewSeed(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).previewSeed();
            }

            @Override
            public void rerollPreviewSeed(String sectionKey) {
                floorEditor(editor, stackId, sectionKey).rerollPreviewSeed();
                screen.flagNeedSetup();
            }

            @Override
            public Optional<Long> lockedLayoutSeed(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).lockedLayoutSeed();
            }

            @Override
            public void lockLayoutSeed(String sectionKey) {
                floorEditor(editor, stackId, sectionKey).lockLayoutSeed();
                screen.flagNeedSetup();
            }

            @Override
            public void unlockLayoutSeed(String sectionKey) {
                floorEditor(editor, stackId, sectionKey).unlockLayoutSeed();
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomHeightMin(String sectionKey) {
                return 3;
            }

            @Override
            public int floorRoomHeightMax(String sectionKey) {
                return floorEditor(editor, stackId, sectionKey).roomHeightMax();
            }

            @Override
            public List<MKWorkspaceFloorRoomProfile> roomProfiles(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                return floorEditor(editor, stackId, sectionKey).roomProfiles(kind);
            }

            @Override
            public void floorRoomWidth(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value) {
                floorEditor(editor, stackId, sectionKey).roomWidth(kind, index, value);
                screen.flagNeedSetup();
            }

            @Override
            public void floorRoomLength(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value) {
                floorEditor(editor, stackId, sectionKey).roomLength(kind, index, value);
                screen.flagNeedSetup();
            }

            @Override
            public void floorRoomHeight(String sectionKey, MKWorkspaceFloorRoomKind kind, int index, int value) {
                floorEditor(editor, stackId, sectionKey).roomHeight(kind, index, value);
                screen.flagNeedSetup();
            }

            @Override
            public void addRoomProfile(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                floorEditor(editor, stackId, sectionKey).addRoomProfile(kind);
                screen.flagNeedSetup();
            }

            @Override
            public void removeRoomProfile(String sectionKey, MKWorkspaceFloorRoomKind kind, int index) {
                floorEditor(editor, stackId, sectionKey).removeRoomProfile(kind, index);
                screen.flagNeedSetup();
            }

            @Override
            public int floorRoomVariantCount(String sectionKey, MKWorkspaceFloorRoomKind kind, int index) {
                String baseName = floorRoomVariantBaseName(editor, stackId, sectionKey, kind, index);
                if (baseName.isBlank()) {
                    return 0;
                }
                return (int) screen.workspace().pieces().stream()
                        .filter(piece -> baseName.equals(piece.tags().get("workspace_base_name")))
                        .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                        .count();
            }

            @Override
            public boolean floorRoomVariantsExpanded(String sectionKey, MKWorkspaceFloorRoomKind kind, int index) {
                return editor.viewState.floorRoomVariantDrawers.getOrDefault(
                        floorRoomVariantDrawerKey(stackId, sectionKey, kind, index), false);
            }

            @Override
            public void toggleFloorRoomVariants(String sectionKey, MKWorkspaceFloorRoomKind kind, int index) {
                String key = floorRoomVariantDrawerKey(stackId, sectionKey, kind, index);
                boolean expanded = !editor.viewState.floorRoomVariantDrawers.getOrDefault(key, false);
                editor.viewState.floorRoomVariantDrawers.put(key, expanded);
                screen.flagNeedSetup();
            }

            @Override
            public void addFloorRoomVariant(String sectionKey, MKWorkspaceFloorRoomKind kind, int index) {
                String baseName = floorRoomVariantBaseName(editor, stackId, sectionKey, kind, index);
                if (!baseName.isBlank()) {
                    PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(screen.anchor(), baseName));
                }
            }

            @Override
            public void openFloorRoomVariants(String sectionKey, MKWorkspaceFloorRoomKind kind) {
                screen.openWorkspaceTopologySlotForPrefix(floorRoomTopologySlot(kind));
            }

            @Override
            public void setRoomMainExitDirection(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                                 Direction direction) {
                floorEditor(editor, stackId, sectionKey).setRoomMainExitDirection(kind, index, direction);
                screen.flagNeedSetup();
            }

            @Override
            public void setRoomRandomizeMainExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                                 boolean value) {
                floorEditor(editor, stackId, sectionKey).setRoomRandomizeMainExit(kind, index, value);
                screen.flagNeedSetup();
            }

            @Override
            public void toggleRoomBranchExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                             Direction direction) {
                floorEditor(editor, stackId, sectionKey).toggleRoomBranchExit(kind, index, direction);
                screen.flagNeedSetup();
            }

            @Override
            public void toggleRoomLinkCandidateExit(String sectionKey, MKWorkspaceFloorRoomKind kind, int index,
                                                    Direction direction) {
                floorEditor(editor, stackId, sectionKey).toggleRoomLinkCandidateExit(kind, index, direction);
                screen.flagNeedSetup();
            }

            private Optional<MKWorkspaceRoomFamilyDefinition> selectedFamily(String sectionKey) {
                return familyForSection(editor, stackId, sectionKey);
            }

            private void updateSelectedRootExit(String sectionKey,
                                                java.util.function.Function<MKWorkspaceFamilyHorizontalExitDefinition,
                                                        MKWorkspaceFamilyHorizontalExitDefinition> updater) {
                OptionalInt familyIndex = familyIndexForSection(editor, stackId, sectionKey);
                Optional<MKWorkspaceFamilyHorizontalExitDefinition> exit = selectedRootExit(sectionKey);
                if (familyIndex.isEmpty() || exit.isEmpty()) {
                    return;
                }
                editor.replaceFamilyExit(familyIndex.getAsInt(), editor.selectedFamilyExitIndex(),
                        updater.apply(exit.get()));
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

    private Optional<MKWorkspaceRoomFamilyDefinition> familyForSection(WorkspaceDraftSession editor, String stackId,
                                                                       String sectionKey) {
        OptionalInt index = familyIndexForSection(editor, stackId, sectionKey);
        return index.isPresent() ? Optional.of(editor.draft().familyDefinitions.get(index.getAsInt())) :
                Optional.empty();
    }

    private OptionalInt familyIndexForSection(WorkspaceDraftSession editor, String stackId, String sectionKey) {
        String topologySlotId = stackId + "." + sectionKey;
        Optional<MKWorkspaceVerticalStackSlot> slot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(topologySlotId);
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

    private boolean isRequiredVerticalStackExit(String sectionKey, MKWorkspaceFamilyHorizontalExitDefinition exit) {
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
        WorkspaceVerticalStackDraftEditor towerEditor = towerEditor(editor, stackId);
        addExtrusionRows(screen, content, towerEditor);
        addStairRows(screen, content, towerEditor);
        addFoundationRows(screen, content, towerEditor);
        screen.addPaletteOverrideRows(content, "Stack Palette Defaults",
                editor.resolveParentPlannerScopePalette(stackId),
                towerEditor.paletteOverrideOpt(),
                override -> towerEditor.paletteOverride(override));
    }

    private void addExtrusionRows(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                  WorkspaceVerticalStackDraftEditor towerEditor) {
        MKButton extrusionButton = new MKButton(Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                towerEditor.horizontalExtrusionMode().getSerializedName())), 180, 20);
        extrusionButton.setPressedCallback((button, mouseButton) -> {
            towerEditor.horizontalExtrusionMode(WorkspaceTopologyUiSupport.cycleValue(
                    List.of(MKWorkspaceHorizontalExtrusionMode.values()),
                    towerEditor.horizontalExtrusionMode(),
                    WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Horizontal Extrusion")), extrusionButton);
    }

    private WorkspaceVerticalStackDraftEditor towerEditor(WorkspaceDraftSession editor, String stackId) {
        return new WorkspaceVerticalStackDraftEditor(editor, stackId);
    }

    private void addStairRows(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                              WorkspaceVerticalStackDraftEditor towerEditor) {
        MKButton stairModeButton = new MKButton(Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                towerEditor.stairMode().getSerializedName())), 180, 20);
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            towerEditor.stairMode(WorkspaceTopologyUiSupport.cycleValue(List.of(
                    MKWorkspaceStairMode.AUTO,
                    MKWorkspaceStairMode.RUN_PROFILE,
                    MKWorkspaceStairMode.LADDER,
                    MKWorkspaceStairMode.NONE
            ), towerEditor.stairMode(), WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Mode")),
                stairModeButton);

        MKButton stairRiseButton = new MKButton(Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                towerEditor.stairRiseType().getSerializedName())), 180, 20);
        stairRiseButton.setPressedCallback((button, mouseButton) -> {
            towerEditor.stairRiseType(WorkspaceTopologyUiSupport.cycleValue(
                    List.of(MKWorkspaceStairRiseType.values()), towerEditor.stairRiseType(),
                    WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Rise Type")),
                stairRiseButton);

        MKButton stairWidthButton = new MKButton(
                Component.literal(Integer.toString(towerEditor.stairWidth())), 180, 20);
        stairWidthButton.setPressedCallback((button, mouseButton) -> {
            List<Integer> allowedWidths = MKWorkspaceDimensions.getAllowedStairWidths(towerEditor.shaftSize());
            int snapped = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(towerEditor.shaftSize(),
                    towerEditor.stairWidth());
            towerEditor.stairWidth(WorkspaceTopologyUiSupport.cycleValue(allowedWidths, snapped,
                    WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Width")),
                stairWidthButton);
    }

    private void addFoundationRows(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                   WorkspaceVerticalStackDraftEditor towerEditor) {
        MKWorkspaceFoundationPolicy foundationPolicy = towerEditor.foundationPolicy();
        MKButton modeButton = new MKButton(Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                foundationPolicy.mode().getSerializedName())), 180, 20);
        modeButton.setPressedCallback((button, mouseButton) -> {
            MKWorkspaceFoundationMode mode = WorkspaceTopologyUiSupport.cycleValue(
                    List.of(MKWorkspaceFoundationMode.values()), foundationPolicy.mode(),
                    WorkspaceTopologyUiSupport.isReverseClick(mouseButton));
            towerEditor.foundationPolicy(foundationPolicyForMode(mode, foundationPolicy));
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
                    towerEditor.foundationPolicy(MKWorkspaceFoundationPolicy.uniformBlock(value));
                    screen.refreshPreservingActiveScroll();
                }, false);
                return true;
            });
            WorkspaceTopologyUiSupport.addRow(screen, content,
                    screen.makeWhiteText(Component.literal("Foundation Block")), blockButton);
        }
    }

    private String floorRoomVariantBaseName(WorkspaceDraftSession editor, String stackId, String sectionKey,
                                            MKWorkspaceFloorRoomKind kind, int index) {
        List<MKWorkspaceFloorRoomProfile> profiles = floorEditor(editor, stackId, sectionKey).roomProfiles(kind);
        if (index < 0 || index >= profiles.size()) {
            return "";
        }
        MKWorkspaceFloorRoomProfile profile = profiles.get(index);
        return "floor_plan_" + safeFloorRoomId(stackId) + "_" + safeFloorRoomId(sectionKey) + "_" +
                kind.getSerializedName() + "_" + safeFloorRoomId(profile.id()) + "_" + index;
    }

    private String floorRootVariantBaseName(WorkspaceDraftSession editor, String stackId, String sectionKey) {
        return familyForSection(editor, stackId, sectionKey)
                .map(MKWorkspaceRoomFamilyDefinition::baseName)
                .orElse("");
    }

    private String floorRootTopologySlot(WorkspaceDraftSession editor, String stackId, String sectionKey) {
        return familyForSection(editor, stackId, sectionKey)
                .map(MKWorkspaceRoomFamilyDefinition::topologySlotId)
                .orElse("");
    }

    private String floorRootVariantDrawerKey(String stackId, String sectionKey) {
        return stackId + "|" + sectionKey + "|root";
    }

    private String floorHallVariantBaseName(WorkspaceDraftSession editor, String stackId, String sectionKey,
                                            boolean main) {
        String pathName = main ? "main" : "branch";
        String linearRunId = floorHallLinearRunId(editor, stackId, sectionKey, main);
        if (linearRunId.isBlank()) {
            return "";
        }
        return "floor_plan_" + safeFloorRoomId(stackId) + "_" + safeFloorRoomId(sectionKey) +
                "_linear_run_" + safeFloorRoomId(linearRunId) + "_" + pathName;
    }

    private String floorHallLinearRunId(WorkspaceDraftSession editor, String stackId, String sectionKey,
                                        boolean main) {
        String openingProfileId = floorHallOpeningProfileId(editor, stackId, sectionKey, main);
        if (openingProfileId.isBlank()) {
            return "";
        }
        Optional<MKWorkspaceLinearRunFamilyDefinition> family = editor.draft().linearRunFamilies.stream()
                .filter(linearRun -> !linearRun.topologySlotId().startsWith("keep."))
                .filter(linearRun -> main ? linearRun.allowOnMainPath() : linearRun.allowOnBranchPath())
                .filter(linearRun -> openingProfileId.equals(linearRun.openingProfileId()))
                .findFirst();
        return family.map(MKWorkspaceLinearRunFamilyDefinition::linearRunId)
                .orElse(main ? "floor_main_hallway" : "floor_branch_hallway");
    }

    private String floorHallOpeningProfileId(WorkspaceDraftSession editor, String stackId, String sectionKey,
                                             boolean main) {
        MKWorkspaceHorizontalExitPathKind pathKind = main ? MKWorkspaceHorizontalExitPathKind.MAIN_EXIT :
                MKWorkspaceHorizontalExitPathKind.BRANCH;
        return familyForSection(editor, stackId, sectionKey)
                .flatMap(family -> family.horizontalExits().stream()
                        .filter(exit -> exit.pathKind() == pathKind)
                        .map(MKWorkspaceFamilyHorizontalExitDefinition::openingProfileId)
                        .findFirst())
                .orElse(main ? "main_opening" : "branch_opening");
    }

    private String floorHallTopologySlot(boolean main) {
        return "tower.floor_plan.linear_run." + (main ? "main" : "branch");
    }

    private String floorHallVariantDrawerKey(String stackId, String sectionKey, boolean main) {
        return stackId + "|" + sectionKey + "|hall|" + (main ? "main" : "branch");
    }

    private String floorInsertVariantBaseName(WorkspaceDraftSession editor, String stackId, String sectionKey) {
        return floorEditor(editor, stackId, sectionKey).insertFamily().orElse("");
    }

    private String floorInsertTopologySlot() {
        return "workspace.insert_family.floor_link_hallway";
    }

    private String floorInsertVariantDrawerKey(String stackId, String sectionKey) {
        return stackId + "|" + sectionKey + "|insert";
    }

    private String floorRoomTopologySlot(MKWorkspaceFloorRoomKind kind) {
        return "tower.floor_plan." + kind.getSerializedName();
    }

    private String floorRoomVariantDrawerKey(String stackId, String sectionKey,
                                             MKWorkspaceFloorRoomKind kind, int index) {
        return stackId + "|" + sectionKey + "|" + kind.getSerializedName() + "|" + index;
    }

    private String safeFloorRoomId(String value) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            builder.append(Character.isLetterOrDigit(c) ? c : '_');
        }
        return builder.toString();
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
