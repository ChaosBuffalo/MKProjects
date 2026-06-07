package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyPathSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepSizingCalculator;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepSizingReport;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WorkspaceTopologyDefaultsPage extends WorkspacePageBase {
    public static final String ID = "topology_defaults";
    public static final String DETAIL_ID = "topology_defaults_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        editor.ensureInitialized();

        MKLayout root = createPanel(screen);
        addTitle(screen, root, Component.literal("Topology Defaults"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Set the broad sizing and stack defaults for this planner before editing individual family overrides."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);

        MKText topologyText = screen.makeWhiteText(Component.literal(
                "Active planner: " + editor.topologyPlannerId()));
        topologyText.setWidth(screen.contentWidth());
        topologyText.setMultiline(true);
        content.addWidget(topologyText);
        content.addConstraintToWidget(MarginConstraint.LEFT, topologyText);

        WorkspacePlannerUiRegistry.getTopologyUi(editor.topologyPlannerId())
                .addDefaultsSections(screen, content, editor);

        finishScrollContent(screen, scrollView, content);
        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }

    private void addWalledKeepSizingSection(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                            WorkspaceDraftSession editor) {
        MKStructureWorkspace workspaceDraft = editor.buildWorkspaceDraft();
        MKWalledKeepSizingReport report = new MKWalledKeepSizingCalculator().calculate(workspaceDraft);
        MKText header = screen.makeWhiteText(Component.literal("Structure Footprint"));
        header.setWidth(screen.contentWidth());
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKWalledKeepFootprintPreview preview = new MKWalledKeepFootprintPreview(
                Math.min(screen.contentWidth(), 260), 190, workspaceDraft, report);
        content.addWidget(preview);
        content.addConstraintToWidget(new CenterXConstraint(), preview);

        String status = report.fitsJigsawCap() ? "fits" : "too large";
        MKText footprint = screen.makeWhiteText(Component.literal(
                "N " + report.northDistance() + " / S " + report.southDistance() +
                        " / W " + report.westDistance() + " / E " + report.eastDistance() +
                        "\nfootprint " + report.footprintWidth() + " x " + report.footprintLength() +
                        " | required " + report.requiredJigsawRadius() +
                        " / cap " + report.maxDistanceFromCenter() +
                        " (" + status + ", headroom " + report.headroom() + ")" +
                        "\nwall unit " + editor.wallUnitSpan() +
                        " | recommended " + report.recommendedWallUnitSpan() +
                        " | passage " + editor.wallPassageWidth() +
                        " | body " + (editor.wallPassageWidth() + (2 * editor.shellMargin())) +
                        "\nwall segments front " + report.frontBranchSegments() + "+" +
                        report.frontBranchSegments() + ", side " + report.verticalWallSegments() +
                        ", back " + report.backWallSegments() +
                        " | courtyard path " + report.courtyardPathSize() +
                        " | entry path " + report.entryApproachLength()
        ));
        footprint.setWidth(screen.contentWidth());
        footprint.setMultiline(true);
        content.addWidget(footprint);
        content.addConstraintToWidget(MarginConstraint.LEFT, footprint);

        MKButton terrainButton = new MKButton(
                Component.literal(formatTopologyLabel(editor.terrainAdjustment().getSerializedName())), 180, 20);
        terrainButton.setPressedCallback((button, mouseButton) -> {
            editor.terrainAdjustment(cycleValue(terrainAdjustmentModes(), editor.terrainAdjustment(),
                    isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Terrain Adaptation")), terrainButton);

        List<Integer> allowedSocketSizes = report.allowedCourtyardContentSizes();
        if (allowedSocketSizes.isEmpty()) {
            MKButton noFitButton = new MKButton(Component.literal("No Fit"), 180, 20);
            noFitButton.setTooltip(Component.literal("Current sizing leaves no valid courtyard content socket."));
            addRow(screen, content, screen.makeWhiteText(Component.literal("Courtyard Socket Size")), noFitButton);
            return;
        }
        int snappedSize = report.snappedCourtyardContentSize(editor.courtyardContentTemplateSize());
        if (snappedSize != editor.courtyardContentTemplateSize()) {
            editor.courtyardContentTemplateSize(snappedSize);
        }
        MKIntegerSlider courtyardSizeSlider = new MKIntegerSlider("Size", 180, 20,
                allowedSocketSizes, snappedSize, value -> {
            editor.courtyardContentTemplateSize(value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Courtyard Socket Size")),
                courtyardSizeSlider);
    }

    private List<TerrainAdjustment> terrainAdjustmentModes() {
        ArrayList<TerrainAdjustment> modes = new ArrayList<>();
        modes.add(TerrainAdjustment.BEARD_THIN);
        modes.add(TerrainAdjustment.NONE);
        modes.add(TerrainAdjustment.BEARD_BOX);
        modes.add(TerrainAdjustment.BURY);
        modes.add(TerrainAdjustment.ENCAPSULATE);
        return List.copyOf(modes);
    }

    private void addPathDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content) {
        MKText pathHeader = screen.makeWhiteText(Component.literal("Path Depth Defaults"));
        pathHeader.setWidth(screen.contentWidth());
        content.addWidget(pathHeader);
        content.addConstraintToWidget(MarginConstraint.LEFT, pathHeader);
        for (String topologyGroupId : MKWorkspaceTopologyPathSettings.DEFAULT_TOPOLOGY_GROUP_IDS) {
            addTowerPathDefaultsSection(screen, content, topologyGroupId);
        }
    }

    private void addTowerPathDefaultsSection(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                             String topologyGroupId) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKWorkspaceTopologyPathSettings pathSettings = editor.topologyPathSettings(topologyGroupId);
        MKText header = screen.makeWhiteText(Component.literal(formatTopologyLabel(topologyGroupId)));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKText summary = screen.makeWhiteText(Component.literal(
                (showTopologyGroupPathControls(editor, topologyGroupId) ? "path " +
                        pathSettings.minMainPathPieces() + "-" + pathSettings.maxMainPathPieces() + "  |  " : "") +
                        "branch cap " + pathSettings.maxBranchPiecesBeforeCap()));
        summary.setWidth(screen.contentWidth());
        summary.setMultiline(true);
        content.addWidget(summary);
        content.addConstraintToWidget(MarginConstraint.LEFT, summary);
        if (showTopologyGroupPathControls(editor, topologyGroupId)) {
            MKIntegerSlider minPathSlider = new MKIntegerSlider("Min", 180, 20, 0, 10, 1,
                    pathSettings.minMainPathPieces(),
                    value -> editor.topologyPathMinMainPathPieces(topologyGroupId, value));
            MKIntegerSlider maxPathSlider = new MKIntegerSlider("Max", 180, 20, 0, 10, 1,
                    pathSettings.maxMainPathPieces(),
                    value -> editor.topologyPathMaxMainPathPieces(topologyGroupId, value));
            addRow(screen, content, screen.makeWhiteText(Component.literal("Main Path Min")), minPathSlider);
            addRow(screen, content, screen.makeWhiteText(Component.literal("Main Path Max")), maxPathSlider);
        }
        MKIntegerSlider maxBranchBeforeCapSlider = new MKIntegerSlider("Max", 180, 20, 0,
                MKWorkspaceTopologyPathSettings.MAX_BRANCH_PIECES_BEFORE_CAP, 1,
                pathSettings.maxBranchPiecesBeforeCap(),
                value -> editor.topologyPathMaxBranchPiecesBeforeCap(topologyGroupId, value));
        addRow(screen, content, screen.makeWhiteText(Component.literal("Max Branches")), maxBranchBeforeCapSlider);
        addResetRow(screen, content, formatTopologyLabel(topologyGroupId) + " Paths", () -> {
            editor.resetTopologyPathDefaults(topologyGroupId);
            screen.flagNeedSetup();
        });
    }

    private void addCornerSizingSection(MKWorkspaceScreen screen, MKStackLayoutVertical content, String topologySlotId) {
        MKText header = screen.makeWhiteText(Component.literal(formatTopologyLabel(topologySlotId) + " Settings"));
        header.setWidth(screen.contentWidth());
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        addTowerStackSection(screen, content, topologySlotId, "Corner");
    }

    private void addTowerStackSection(MKWorkspaceScreen screen, MKStackLayoutVertical content, String stackId,
                                      String labelPrefix) {
        addTowerStackSizingRows(screen, content, stackId, labelPrefix);
        addTowerStackFloorRows(screen, content, stackId);
        addResetRow(screen, content, labelPrefix + " Stack", () -> {
            screen.draftSession().resetTowerStackDefaults(stackId);
            screen.flagNeedSetup();
        });
    }

    private void addTowerStackSizingRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, String stackId,
                                         String labelPrefix) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKIntegerSlider widthSlider = new MKIntegerSlider("Width", 180, 20, 3, 45, 2,
                editor.towerStackWidth(stackId), value -> {
            editor.towerStackWidth(stackId, value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal(labelPrefix + " Width")), widthSlider);

        MKIntegerSlider lengthSlider = new MKIntegerSlider("Length", 180, 20, 3, 45, 2,
                editor.towerStackLength(stackId), value -> {
            editor.towerStackLength(stackId, value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal(labelPrefix + " Length")), lengthSlider);

        MKIntegerSlider heightSlider = new MKIntegerSlider("Height", 180, 20, 3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1, 1,
                editor.towerStackHeight(stackId), value -> {
            editor.towerStackHeight(stackId, value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal(labelPrefix + " Height")), heightSlider);
    }

    private void addTowerStackFloorRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, String stackId) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKButton minMainFloorsButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackMinMainFloors(stackId))), 180, 20);
        minMainFloorsButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackMinMainFloors(stackId, cycleInteger(0, editor.towerStackMainFloors(stackId),
                    editor.towerStackMinMainFloors(stackId), isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Min Main Floors")), minMainFloorsButton);

        MKButton mainFloorsButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackMainFloors(stackId))), 180, 20);
        mainFloorsButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackMainFloors(stackId,
                    editor.nextAllowedTowerStackMainFloorCount(stackId, isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Max Main Floors")), mainFloorsButton);

        MKButton minBasementFloorsButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackMinBasementFloors(stackId))), 180, 20);
        minBasementFloorsButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackMinBasementFloors(stackId, cycleInteger(0, editor.towerStackBasementFloors(stackId),
                    editor.towerStackMinBasementFloors(stackId), isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Min Basement Floors")),
                minBasementFloorsButton);

        MKButton basementFloorsButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackBasementFloors(stackId))), 180, 20);
        basementFloorsButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackBasementFloors(stackId,
                    editor.nextAllowedTowerStackBasementFloorCount(stackId, isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Max Basement Floors")), basementFloorsButton);

        MKButton basementEntryButton = new MKButton(
                Component.literal(enabledLabel(editor.towerStackBasementEntryEnabled(stackId))), 180, 20);
        basementEntryButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackBasementEntryEnabled(stackId, !editor.towerStackBasementEntryEnabled(stackId));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Basement Entry")), basementEntryButton);

        MKIntegerSlider shaftSizeSlider = new MKIntegerSlider("Shaft", 180, 20,
                editor.allowedTowerStackShaftSizes(stackId), editor.towerStackShaftSize(stackId), value -> {
            editor.towerStackShaftSize(stackId, value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Shaft Size")), shaftSizeSlider);

        MKButton stairPlacementButton = new MKButton(
                getStairPlacementComponent(editor.towerStackVerticalAccessPlacement(stackId)), 180, 20);
        stairPlacementButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackVerticalAccessPlacement(stackId, cycleValue(List.of(MKVerticalAccessPlacement.values()),
                    editor.towerStackVerticalAccessPlacement(stackId), isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Placement")), stairPlacementButton);

        MKButton stairModeButton = new MKButton(
                getStairModeComponent(editor.towerStackStairMode(stackId)), 180, 20);
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackStairMode(stackId,
                    cycleStairMode(editor.towerStackStairMode(stackId), isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Mode")), stairModeButton);

        MKButton stairRiseButton = new MKButton(
                getStairRiseTypeComponent(editor.towerStackStairRiseType(stackId)), 180, 20);
        stairRiseButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackStairRiseType(stackId, cycleValue(List.of(MKWorkspaceStairRiseType.values()),
                    editor.towerStackStairRiseType(stackId), isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Rise Type")), stairRiseButton);

        MKButton stairWidthButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackStairWidth(stackId))), 180, 20);
        stairWidthButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackStairWidth(stackId, cycleAllowedStairWidth(
                    editor.towerStackShaftSize(stackId), editor.towerStackStairWidth(stackId),
                    isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Width")), stairWidthButton);

        MKButton topCapApproachButton = new MKButton(
                Component.literal(enabledLabel(editor.towerStackTopCapApproachEnabled(stackId))), 180, 20);
        topCapApproachButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackTopCapApproachEnabled(stackId, !editor.towerStackTopCapApproachEnabled(stackId));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Top Cap Approach")), topCapApproachButton);

        MKButton basementCapApproachButton = new MKButton(
                Component.literal(enabledLabel(editor.towerStackBasementCapApproachEnabled(stackId))), 180, 20);
        basementCapApproachButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackBasementCapApproachEnabled(stackId,
                    !editor.towerStackBasementCapApproachEnabled(stackId));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Basement Cap Approach")),
                basementCapApproachButton);

        addTowerStackFoundationRows(screen, content, stackId);
        screen.addPaletteOverrideRows(content, "Stack Palette Defaults", editor.draftBasePalette(),
                editor.towerStackPaletteOverrideOpt(stackId),
                override -> editor.towerStackPaletteOverride(stackId, override));
    }

    private void addTowerStackFoundationRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, String stackId) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKWorkspaceFoundationPolicy foundationPolicy = editor.towerStackFoundationPolicy(stackId);
        MKButton modeButton = new MKButton(Component.literal(formatTopologyLabel(
                foundationPolicy.mode().getSerializedName())), 180, 20);
        modeButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackFoundationPolicy(stackId, foundationPolicyForMode(
                    cycleValue(List.of(MKWorkspaceFoundationMode.values()), foundationPolicy.mode(),
                            isReverseClick(mouseButton)),
                    foundationPolicy));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Foundation Mode")), modeButton);

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
            addRow(screen, content, screen.makeWhiteText(Component.literal("Foundation Block")), blockButton);
        }

        if (foundationPolicy.mode() == MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS) {
            addTowerStackFoundationMaskRows(screen, content, stackId, foundationPolicy);
        }
    }

    private void addTowerStackFoundationMaskRows(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                                 String stackId, MKWorkspaceFoundationPolicy foundationPolicy) {
        WorkspaceDraftSession editor = screen.draftSession();
        List<ResourceLocation> maskBlocks = foundationPolicy.maskBlocks();
        for (int maskIndex = 0; maskIndex < maskBlocks.size(); maskIndex++) {
            ResourceLocation blockId = maskBlocks.get(maskIndex);
            int capturedIndex = maskIndex;
            MKButton blockButton = new MKButton(screen.blockDisplayName(blockId), 180, 20);
            blockButton.setTooltip(Component.literal(blockId + "\nLeft-click to choose. Right-click to remove."));
            blockButton.setPressedCallback((button, mouseButton) -> {
                if (isReverseClick(mouseButton)) {
                    updateTowerStackFoundationMaskBlock(screen, stackId, foundationPolicy, capturedIndex, null);
                } else {
                    screen.openBlockPicker("Choose Foundation Mask Block", blockId, value ->
                            updateTowerStackFoundationMaskBlock(screen, stackId, foundationPolicy, capturedIndex, value),
                            false);
                }
                return true;
            });
            addRow(screen, content, screen.makeWhiteText(Component.literal("Mask Block " + (maskIndex + 1))),
                    blockButton);
        }
        MKButton addButton = new MKButton(Component.literal("Add Block"), 180, 20);
        addButton.setPressedCallback((button, mouseButton) -> {
            ResourceLocation defaultBlock = ResourceLocation.parse("minecraft:stone");
            screen.openBlockPicker("Choose Foundation Mask Block", defaultBlock, value -> {
                List<ResourceLocation> updated = new java.util.ArrayList<>(foundationPolicy.maskBlocks());
                if (!updated.contains(value)) {
                    updated.add(value);
                }
                editor.towerStackFoundationPolicy(stackId,
                        MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(updated));
                screen.refreshPreservingActiveScroll();
            }, false);
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Foundation Mask")), addButton);
    }

    private void updateTowerStackFoundationMaskBlock(MKWorkspaceScreen screen, String stackId,
                                                     MKWorkspaceFoundationPolicy foundationPolicy, int maskIndex,
                                                     ResourceLocation blockId) {
        List<ResourceLocation> updated = new java.util.ArrayList<>(foundationPolicy.maskBlocks());
        if (maskIndex < 0 || maskIndex >= updated.size()) {
            return;
        }
        if (blockId == null) {
            updated.remove(maskIndex);
        } else {
            updated.set(maskIndex, blockId);
        }
        screen.draftSession().towerStackFoundationPolicy(stackId,
                MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(updated));
        screen.refreshPreservingActiveScroll();
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
                    MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(List.of(ResourceLocation.parse("minecraft:stone"))) :
                    MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(current.maskBlocks());
        };
    }

    private void addTowerStackBlockRow(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label,
                                       ResourceLocation blockId,
                                       Consumer<ResourceLocation> setter) {
        MKButton blockButton = new MKButton(screen.blockDisplayName(blockId), 180, 20);
        blockButton.setTooltip(Component.literal(blockId.toString()));
        blockButton.setPressedCallback((button, mouseButton) -> {
            screen.openBlockPicker("Choose " + label, blockId, value -> {
                setter.accept(value);
                screen.flagNeedSetup();
            }, false);
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal(label)), blockButton);
    }

    private void addCornerModeRow(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label,
                                  String topologySlotId) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKButton modeButton = new MKButton(Component.literal(editor.uniqueCornerTower(topologySlotId) ? "Unique" : "Shared"),
                180, 20);
        modeButton.setPressedCallback((button, mouseButton) -> {
            editor.uniqueCornerTower(topologySlotId, !editor.uniqueCornerTower(topologySlotId));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal(label)), modeButton);
    }

    private boolean showTopologyGroupPathControls(WorkspaceDraftSession editor, String topologyGroupId) {
        return hasMainPathContinuationFamily(editor, topologyGroupId) || !hasMainPathEndingFamily(editor, topologyGroupId);
    }

    private boolean hasMainPathContinuationFamily(WorkspaceDraftSession editor, String topologyGroupId) {
        return editor.draft().familyDefinitions.stream()
                .filter(family -> family.slotMetadata().topologyGroupId().equals(topologyGroupId))
                .filter(family -> !family.mainPathEnding())
                .flatMap(family -> family.horizontalExits().stream())
                .anyMatch(exit -> exit.pathKind().usesMainPath() &&
                        exit.pathKind() != MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY);
    }

    private boolean hasMainPathEndingFamily(WorkspaceDraftSession editor, String topologyGroupId) {
        return editor.draft().familyDefinitions.stream()
                .filter(family -> family.slotMetadata().topologyGroupId().equals(topologyGroupId))
                .anyMatch(MKTowerWorkspaceFamilyDefinition::mainPathEnding);
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, MKText label, MKButton button) {
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, MKText label, MKIntegerSlider slider) {
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(slider);
        root.addConstraintToWidget(new CenterXConstraint(), slider);
    }

    private void addResetRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label, Runnable resetter) {
        MKButton resetButton = new MKButton(Component.literal("Reset Defaults"), 180, screen.buttonHeight());
        resetButton.setPressedCallback((button, mouseButton) -> {
            resetter.run();
            return true;
        });
        addRow(screen, root, screen.makeWhiteText(Component.literal(label)), resetButton);
    }

    private MKText makeLabel(MKWorkspaceScreen screen, String translationKey) {
        MKText text = screen.makeWhiteText(Component.translatable(translationKey));
        text.setWidth(screen.contentWidth());
        return text;
    }

    private String enabledLabel(boolean enabled) {
        return enabled ? "Enabled" : "Disabled";
    }

    private boolean isReverseClick(int mouseButton) {
        return mouseButton == 1;
    }

    private MKWorkspaceStairMode cycleStairMode(MKWorkspaceStairMode current, boolean reverse) {
        return cycleValue(List.of(
                MKWorkspaceStairMode.AUTO,
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairMode.LADDER,
                MKWorkspaceStairMode.NONE
        ), current, reverse);
    }

    private int cycleAllowedStairWidth(int shaftWidth, int currentWidth, boolean reverse) {
        List<Integer> allowedWidths = MKWorkspaceDimensions.getAllowedStairWidths(shaftWidth);
        int snapped = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(shaftWidth, currentWidth);
        if (allowedWidths.isEmpty()) {
            return currentWidth;
        }
        int index = allowedWidths.indexOf(snapped);
        if (index < 0) {
            return currentWidth;
        }
        int nextIndex = Math.floorMod(index + (reverse ? -1 : 1), allowedWidths.size());
        return allowedWidths.get(nextIndex);
    }

    private <T> T cycleValue(List<T> values, T current, boolean reverse) {
        if (values.isEmpty()) {
            return current;
        }
        int index = values.indexOf(current);
        if (index < 0) {
            return values.getFirst();
        }
        int nextIndex = Math.floorMod(index + (reverse ? -1 : 1), values.size());
        return values.get(nextIndex);
    }

    private int cycleInteger(int min, int max, int current, boolean reverse) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        int normalized = Math.max(low, Math.min(current, high));
        int next = normalized + (reverse ? -1 : 1);
        if (next < low) {
            return high;
        }
        if (next > high) {
            return low;
        }
        return next;
    }

    private String formatTopologyLabel(String key) {
        return WorkspacePieceDisplay.formatTopologyLabel(key);
    }

    private Component getStairPlacementComponent(MKVerticalAccessPlacement placement) {
        return Component.translatable("mknpc.workspace.stair_placement." + placement.getSerializedName());
    }

    private Component getStairModeComponent(MKWorkspaceStairMode mode) {
        return Component.literal(formatTopologyLabel(mode.getSerializedName()));
    }

    private Component getStairRiseTypeComponent(MKWorkspaceStairRiseType riseType) {
        return Component.literal(formatTopologyLabel(riseType.getSerializedName()));
    }
}
