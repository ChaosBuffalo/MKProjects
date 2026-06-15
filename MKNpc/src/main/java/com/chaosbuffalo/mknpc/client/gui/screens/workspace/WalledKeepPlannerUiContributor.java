package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepSizingCalculator;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKWalledKeepSizingReport;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

import java.util.ArrayList;
import java.util.List;

public class WalledKeepPlannerUiContributor implements WorkspacePlannerUiContributor {
    private final TowerStackTopologyPanel towerStackPanel = new TowerStackTopologyPanel();

    @Override
    public ResourceLocation plannerId() {
        return MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID;
    }

    @Override
    public void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor) {
        addKeepLayoutSettings(screen, content, editor, false);
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal(
                "Perimeter walls use one shared linear-run slot. The keep planner repeats it as whole wall segments."));
        addTowerTabs(screen, content, editor);
    }

    @Override
    public void addDefaultsLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                  WorkspaceDraftSession editor) {
        addKeepLayoutSettings(screen, layout, editor, false);
        WorkspaceTopologyUiSupport.addText(screen, layout.settingsContent(), Component.literal(
                "Perimeter walls use one shared linear-run slot. The keep planner repeats it as whole wall segments."));
        addTowerTabs(screen, layout.settingsContent(), editor);
    }

    @Override
    public void addWorkspaceOverviewSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                             WorkspaceDraftSession editor) {
        editor.ensureInitialized();
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal("Walled Keep Settings"));
        addKeepLayoutSettings(screen, content, editor, true);
        addTowerSizingTabs(screen, content, editor);
    }

    @Override
    public void addWorkspaceOverviewLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                           WorkspaceDraftSession editor) {
        editor.ensureInitialized();
        WorkspaceTopologyUiSupport.addText(screen, layout.settingsContent(), Component.literal("Walled Keep Settings"));
        addKeepLayoutSettings(screen, layout, editor, true);
        addTowerSizingTabs(screen, layout.settingsContent(), editor);
    }

    @Override
    public void selectPlannerNode(WorkspaceDraftSession editor, String nodeId) {
        keepEditor(editor).verticalStackTab(nodeId);
    }

    @Override
    public void addPlannerNodeLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                     WorkspaceDraftSession editor, String nodeId, String label,
                                     boolean topLevelPlanner) {
        towerStackPanel.addStackEditor(screen, layout, editor, nodeId, label, topLevelPlanner);
    }

    @Override
    public void addFloorPlanLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                   WorkspaceDraftSession editor, String stackId, String floorRole) {
        towerStackPanel.addFloorPlanEditor(screen, layout, editor, stackId, floorRole);
    }

    private void addKeepLayoutSettings(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                       WorkspaceDraftSession editor, boolean navigablePreview) {
        addWalledKeepSizingSection(screen, content, editor, navigablePreview);
        addKeepPaletteRows(screen, content, editor);
        addCornerModeRow(screen, content, editor, "NW Corner", "keep.corner.north_west");
        addCornerModeRow(screen, content, editor, "NE Corner", "keep.corner.north_east");
        addCornerModeRow(screen, content, editor, "SE Corner", "keep.corner.south_east");
        addCornerModeRow(screen, content, editor, "SW Corner", "keep.corner.south_west");
        addPerimeterRows(screen, content, editor);
    }

    private void addKeepLayoutSettings(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                       WorkspaceDraftSession editor, boolean navigablePreview) {
        MKWalledKeepSizingReport report = addWalledKeepPreviewSection(screen, layout.previewContent(), editor,
                navigablePreview);
        addWalledKeepSizingControlRows(screen, layout.settingsContent(), editor, report);
        addKeepPaletteRows(screen, layout.settingsContent(), editor);
        addCornerModeRow(screen, layout.settingsContent(), editor, "NW Corner", "keep.corner.north_west");
        addCornerModeRow(screen, layout.settingsContent(), editor, "NE Corner", "keep.corner.north_east");
        addCornerModeRow(screen, layout.settingsContent(), editor, "SE Corner", "keep.corner.south_east");
        addCornerModeRow(screen, layout.settingsContent(), editor, "SW Corner", "keep.corner.south_west");
        addPerimeterRows(screen, layout.settingsContent(), editor);
    }

    private void addKeepPaletteRows(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor) {
        screen.addPaletteOverrideRows(content, "Keep Palette Defaults", editor.palette(),
                editor.topologyGroupPaletteOverride("keep"),
                override -> editor.topologyGroupPaletteOverride("keep", override));
    }

    private void addWalledKeepSizingSection(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                            WorkspaceDraftSession editor, boolean navigable) {
        MKWalledKeepSizingReport report = addWalledKeepPreviewSection(screen, content, editor, navigable);
        addWalledKeepSizingControlRows(screen, content, editor, report);
    }

    private MKWalledKeepSizingReport addWalledKeepPreviewSection(MKWorkspaceScreen screen,
                                                                 MKStackLayoutVertical content,
                                                                 WorkspaceDraftSession editor,
                                                                 boolean navigable) {
        MKStructureWorkspace workspaceDraft = editor.buildWorkspaceDraft();
        MKWalledKeepSizingReport report = new MKWalledKeepSizingCalculator().calculate(workspaceDraft);
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal("Structure Footprint"));
        MKWalledKeepFootprintPreview preview = navigable
                ? new MKWalledKeepFootprintPreview(Math.min(screen.contentWidth(), 260), 190, workspaceDraft,
                report, target -> handlePlannerPreviewNavigation(screen, target))
                : new MKWalledKeepFootprintPreview(Math.min(screen.contentWidth(), 260), 190, workspaceDraft, report);
        content.addWidget(preview);
        content.addConstraintToWidget(new CenterXConstraint(), preview);

        WalledKeepDraftEditor keepEditor = keepEditor(editor);
        String status = report.fitsJigsawCap() ? "fits" : "too large";
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal(
                "N " + report.northDistance() + " / S " + report.southDistance() +
                        " / W " + report.westDistance() + " / E " + report.eastDistance() +
                        "\nfootprint " + report.footprintWidth() + " x " + report.footprintLength() +
                        " | required " + report.requiredJigsawRadius() +
                        " / cap " + report.maxDistanceFromCenter() +
                        " (" + status + ", headroom " + report.headroom() + ")" +
                        "\nwall unit " + keepEditor.wallUnitSpan() +
                        " | recommended " + report.recommendedWallUnitSpan() +
                        " | passage " + keepEditor.wallPassageWidth() +
                        " | body " + (keepEditor.wallPassageWidth() + (2 * editor.shellMargin())) +
                        "\nwall segments front " + report.frontBranchSegments() + "+" +
                        report.frontBranchSegments() + ", side " + report.verticalWallSegments() +
                        ", back " + report.backWallSegments() +
                        " | courtyard path " + report.courtyardPathSize() +
                        " | entry path " + report.entryApproachLength()));
        return report;
    }

    private void addWalledKeepSizingControlRows(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                                WorkspaceDraftSession editor, MKWalledKeepSizingReport report) {
        WalledKeepDraftEditor keepEditor = keepEditor(editor);
        MKButton terrainButton = new MKButton(
                Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                        keepEditor.terrainAdjustment().getSerializedName())), 180, 20);
        terrainButton.setPressedCallback((button, mouseButton) -> {
            keepEditor.terrainAdjustment(WorkspaceTopologyUiSupport.cycleValue(terrainAdjustmentModes(),
                    keepEditor.terrainAdjustment(), WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Terrain Adaptation")), terrainButton);

        MKIntegerSlider pathInnerMarginSlider = new MKIntegerSlider("Margin", 180, 20, 0, 24, 1,
                keepEditor.courtyardPathInnerMargin(), value -> {
            keepEditor.courtyardPathInnerMargin(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Courtyard Path Inner Margin")), pathInnerMarginSlider);

        List<Integer> allowedSocketSizes = report.allowedCourtyardContentSizes();
        if (allowedSocketSizes.isEmpty()) {
            MKButton noFitButton = new MKButton(Component.literal("No Fit"), 180, 20);
            noFitButton.setTooltip(Component.literal("Current sizing leaves no valid courtyard content socket."));
            WorkspaceTopologyUiSupport.addRow(screen, content,
                    screen.makeWhiteText(Component.literal("Courtyard Socket Size")), noFitButton);
            return;
        }
        int snappedSize = report.snappedCourtyardContentSize(keepEditor.courtyardContentTemplateSize());
        if (snappedSize != keepEditor.courtyardContentTemplateSize()) {
            keepEditor.courtyardContentTemplateSize(snappedSize);
        }
        MKIntegerSlider courtyardSizeSlider = new MKIntegerSlider("Size", 180, 20,
                allowedSocketSizes, snappedSize, value -> {
            keepEditor.courtyardContentTemplateSize(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Courtyard Socket Size")), courtyardSizeSlider);
    }

    private void addPerimeterRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor) {
        WalledKeepDraftEditor keepEditor = keepEditor(editor);
        MKButton perimeterKindButton = new MKButton(
                Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                        keepEditor.perimeterRunKind().getSerializedName())), 180, 20);
        perimeterKindButton.setPressedCallback((button, mouseButton) -> {
            keepEditor.perimeterRunKind(WorkspaceTopologyUiSupport.cycleValue(List.of(
                            MKWorkspaceLinearRunKind.DEFENSIVE_WALL,
                            MKWorkspaceLinearRunKind.SOLID_WALL,
                            MKWorkspaceLinearRunKind.PARAPET),
                    keepEditor.perimeterRunKind(), WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Perimeter Kind")), perimeterKindButton);

        MKStructureWorkspace sizingDraft = editor.buildWorkspaceDraft();
        MKWalledKeepSizingReport sizingReport = new MKWalledKeepSizingCalculator().calculate(sizingDraft);
        MKIntegerSlider wallUnitSpanSlider = new MKIntegerSlider("Span", 180, 20, 3, 45, 2,
                keepEditor.wallUnitSpan(), value -> {
            keepEditor.wallUnitSpan(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal(
                "Wall Unit Span (Recommended " + sizingReport.recommendedWallUnitSpan() + ")")),
                wallUnitSpanSlider);

        MKIntegerSlider wallPassageWidthSlider = new MKIntegerSlider("Width", 180, 20, 3, 15, 2,
                keepEditor.wallPassageWidth(), value -> {
            keepEditor.wallPassageWidth(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Wall Passage Width")), wallPassageWidthSlider);

        MKIntegerSlider wallHeightSlider = new MKIntegerSlider("Height", 180, 20, 2,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1, 1,
                keepEditor.wallHeight(), value -> {
            keepEditor.wallHeight(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Wall Height")),
                wallHeightSlider);

        MKIntegerSlider wallTopVoidSlider = new MKIntegerSlider("Margin", 180, 20, 0,
                Math.max(0, keepEditor.wallHeight() - 1), 1,
                keepEditor.wallTopVoidMargin(), value -> {
            keepEditor.wallTopVoidMargin(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Wall Top Void Margin")), wallTopVoidSlider);
        WorkspaceTopologyUiSupport.addResetRow(screen, content, "Wall And Gate Sizing", () -> {
            keepEditor.resetPerimeterDefaults();
            screen.flagNeedSetup();
        });
    }

    private void addTowerTabs(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor) {
        addTowerTabSelector(screen, content, editor, "Vertical Stack Settings");
        String activeStackId = keepEditor(editor).verticalStackTab();
        String label = tabLabel(activeStackId, false);
        towerStackPanel.addStackSizingRows(screen, content, editor, activeStackId, label);
        towerStackPanel.addStackEditor(screen, content, editor, activeStackId, label, false);
    }

    private void addTowerSizingTabs(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor) {
        addTowerTabSelector(screen, content, editor, "Tower Stack Sizing");
        String activeStackId = keepEditor(editor).verticalStackTab();
        towerStackPanel.addStackSizingRows(screen, content, editor, activeStackId, tabLabel(activeStackId, false));
    }

    private void addTowerTabSelector(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                     WorkspaceDraftSession editor, String heading) {
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal(heading));
        WalledKeepDraftEditor keepEditor = keepEditor(editor);
        List<String> stackIds = keepEditor.verticalStackTabs();
        MKStackLayoutHorizontal tabRow = new MKStackLayoutHorizontal(0, 0, 20);
        tabRow.setPaddingLeft(2).setPaddingRight(2);
        int gapWidth = Math.max(0, stackIds.size() - 1) * (tabRow.getPaddingLeft() + tabRow.getPaddingRight());
        int tabWidth = Math.max(44, Math.min(100, (screen.contentWidth() - gapWidth) / Math.max(1, stackIds.size())));
        for (String stackId : stackIds) {
            MKButton tabButton = new MKButton(Component.literal(tabButtonLabel(stackId,
                    stackId.equals(keepEditor.verticalStackTab()))), tabWidth, 20);
            tabButton.setPressedCallback((button, mouseButton) -> {
                keepEditor.verticalStackTab(stackId);
                screen.flagNeedSetup();
                return true;
            });
            tabRow.addWidget(tabButton);
        }
        content.addWidget(tabRow);
        content.addConstraintToWidget(new CenterXConstraint(), tabRow);
    }

    private String tabButtonLabel(String stackId, boolean active) {
        String label = switch (stackId) {
            case "keep.center" -> "Center";
            case "keep.corner.shared" -> "Shared";
            case "keep.corner.north_west" -> "NW";
            case "keep.corner.north_east" -> "NE";
            case "keep.corner.south_east" -> "SE";
            case "keep.corner.south_west" -> "SW";
            default -> stackId;
        };
        return active ? "[" + label + "]" : label;
    }

    private void addCornerModeRow(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                  WorkspaceDraftSession editor, String label, String topologySlotId) {
        WalledKeepDraftEditor keepEditor = keepEditor(editor);
        MKButton modeButton = new MKButton(Component.literal(keepEditor.uniqueCornerTower(topologySlotId) ? "Unique" : "Shared"),
                180, 20);
        modeButton.setPressedCallback((button, mouseButton) -> {
            keepEditor.uniqueCornerTower(topologySlotId, !keepEditor.uniqueCornerTower(topologySlotId));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal(label)), modeButton);
    }

    private void handlePlannerPreviewNavigation(MKWorkspaceScreen screen,
                                                MKWalledKeepFootprintPreview.NavigationTarget target) {
        switch (target.type()) {
            case BACK -> screen.goBackOrSwitchTo(WorkspaceManagePage.ID);
            case PLANNER_STACK -> screen.openWorkspacePlannerNode(target.id());
            case TEMPLATE_SLOT -> screen.openWorkspaceTopologySlotForPrefix(target.id());
            case NONE -> {
            }
        }
    }

    private String tabLabel(String stackId, boolean active) {
        String label = switch (stackId) {
            case "keep.center" -> "Center";
            case "keep.corner.shared" -> "Corner Towers";
            case "keep.corner.north_west" -> "NW Corner";
            case "keep.corner.north_east" -> "NE Corner";
            case "keep.corner.south_east" -> "SE Corner";
            case "keep.corner.south_west" -> "SW Corner";
            default -> stackId;
        };
        return active ? "[" + label + "]" : label;
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

    private WalledKeepDraftEditor keepEditor(WorkspaceDraftSession editor) {
        return new WalledKeepDraftEditor(editor);
    }
}
