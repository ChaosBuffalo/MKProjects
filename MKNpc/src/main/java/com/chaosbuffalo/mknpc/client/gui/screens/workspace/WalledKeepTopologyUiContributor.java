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

public class WalledKeepTopologyUiContributor implements WorkspaceTopologyUiContributor {
    private final TowerStackTopologyPanel towerStackPanel = new TowerStackTopologyPanel();

    @Override
    public ResourceLocation plannerId() {
        return MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID;
    }

    @Override
    public void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor) {
        addWalledKeepSizingSection(screen, content, editor);
        addCornerModeRow(screen, content, editor, "NW Corner", "keep.corner.north_west");
        addCornerModeRow(screen, content, editor, "NE Corner", "keep.corner.north_east");
        addCornerModeRow(screen, content, editor, "SE Corner", "keep.corner.south_east");
        addCornerModeRow(screen, content, editor, "SW Corner", "keep.corner.south_west");
        addPerimeterRows(screen, content, editor);
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal(
                "Perimeter walls use one shared linear-run slot. The keep planner repeats it as whole wall segments."));
        addTowerTabs(screen, content, editor);
    }

    private void addWalledKeepSizingSection(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                            WorkspaceDraftSession editor) {
        MKStructureWorkspace workspaceDraft = editor.buildWorkspaceDraft();
        MKWalledKeepSizingReport report = new MKWalledKeepSizingCalculator().calculate(workspaceDraft);
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal("Structure Footprint"));
        MKWalledKeepFootprintPreview preview = new MKWalledKeepFootprintPreview(
                Math.min(screen.contentWidth(), 260), 190, workspaceDraft, report);
        content.addWidget(preview);
        content.addConstraintToWidget(new CenterXConstraint(), preview);

        String status = report.fitsJigsawCap() ? "fits" : "too large";
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal(
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
                        " | entry path " + report.entryApproachLength()));

        MKButton terrainButton = new MKButton(
                Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                        editor.terrainAdjustment().getSerializedName())), 180, 20);
        terrainButton.setPressedCallback((button, mouseButton) -> {
            editor.terrainAdjustment(WorkspaceTopologyUiSupport.cycleValue(terrainAdjustmentModes(),
                    editor.terrainAdjustment(), WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Terrain Adaptation")), terrainButton);

        List<Integer> allowedSocketSizes = report.allowedCourtyardContentSizes();
        if (allowedSocketSizes.isEmpty()) {
            MKButton noFitButton = new MKButton(Component.literal("No Fit"), 180, 20);
            noFitButton.setTooltip(Component.literal("Current sizing leaves no valid courtyard content socket."));
            WorkspaceTopologyUiSupport.addRow(screen, content,
                    screen.makeWhiteText(Component.literal("Courtyard Socket Size")), noFitButton);
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
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Courtyard Socket Size")), courtyardSizeSlider);
    }

    private void addPerimeterRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor) {
        MKButton perimeterKindButton = new MKButton(
                Component.literal(WorkspaceTopologyUiSupport.formatTopologyLabel(
                        editor.perimeterRunKind().getSerializedName())), 180, 20);
        perimeterKindButton.setPressedCallback((button, mouseButton) -> {
            editor.perimeterRunKind(WorkspaceTopologyUiSupport.cycleValue(List.of(
                            MKWorkspaceLinearRunKind.DEFENSIVE_WALL,
                            MKWorkspaceLinearRunKind.SOLID_WALL,
                            MKWorkspaceLinearRunKind.PARAPET),
                    editor.perimeterRunKind(), WorkspaceTopologyUiSupport.isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Perimeter Kind")), perimeterKindButton);

        MKStructureWorkspace sizingDraft = editor.buildWorkspaceDraft();
        MKWalledKeepSizingReport sizingReport = new MKWalledKeepSizingCalculator().calculate(sizingDraft);
        MKIntegerSlider wallUnitSpanSlider = new MKIntegerSlider("Span", 180, 20, 3, 45, 2,
                editor.wallUnitSpan(), value -> {
            editor.wallUnitSpan(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal(
                "Wall Unit Span (Recommended " + sizingReport.recommendedWallUnitSpan() + ")")),
                wallUnitSpanSlider);

        MKIntegerSlider wallPassageWidthSlider = new MKIntegerSlider("Width", 180, 20, 3, 15, 2,
                editor.wallPassageWidth(), value -> {
            editor.wallPassageWidth(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Wall Passage Width")), wallPassageWidthSlider);

        MKIntegerSlider wallHeightSlider = new MKIntegerSlider("Height", 180, 20, 2,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1, 1,
                editor.wallHeight(), value -> {
            editor.wallHeight(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal("Wall Height")),
                wallHeightSlider);

        MKIntegerSlider wallTopVoidSlider = new MKIntegerSlider("Margin", 180, 20, 0,
                Math.max(0, editor.wallHeight() - 1), 1,
                editor.wallTopVoidMargin(), value -> {
            editor.wallTopVoidMargin(value);
            screen.flagNeedSetup();
        });
        WorkspaceTopologyUiSupport.addRow(screen, content,
                screen.makeWhiteText(Component.literal("Wall Top Void Margin")), wallTopVoidSlider);
        WorkspaceTopologyUiSupport.addResetRow(screen, content, "Wall And Gate Sizing", () -> {
            editor.resetWalledKeepPerimeterDefaults();
            screen.flagNeedSetup();
        });
    }

    private void addTowerTabs(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor) {
        WorkspaceTopologyUiSupport.addText(screen, content, Component.literal("Tower Stack Settings"));
        List<String> stackIds = editor.walledKeepTowerStackTabs();
        MKStackLayoutHorizontal tabRow = new MKStackLayoutHorizontal(0, 0, 20);
        tabRow.setPaddingLeft(2).setPaddingRight(2);
        int gapWidth = Math.max(0, stackIds.size() - 1) * (tabRow.getPaddingLeft() + tabRow.getPaddingRight());
        int tabWidth = Math.max(44, Math.min(100, (screen.contentWidth() - gapWidth) / Math.max(1, stackIds.size())));
        for (String stackId : stackIds) {
            MKButton tabButton = new MKButton(Component.literal(tabButtonLabel(stackId,
                    stackId.equals(editor.walledKeepTowerStackTab()))), tabWidth, 20);
            tabButton.setPressedCallback((button, mouseButton) -> {
                editor.walledKeepTowerStackTab(stackId);
                screen.flagNeedSetup();
                return true;
            });
            tabRow.addWidget(tabButton);
        }
        content.addWidget(tabRow);
        content.addConstraintToWidget(new CenterXConstraint(), tabRow);
        String activeStackId = editor.walledKeepTowerStackTab();
        towerStackPanel.addStackEditor(screen, content, editor, activeStackId, tabLabel(activeStackId, false));
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
        MKButton modeButton = new MKButton(Component.literal(editor.uniqueCornerTower(topologySlotId) ? "Unique" : "Shared"),
                180, 20);
        modeButton.setPressedCallback((button, mouseButton) -> {
            editor.uniqueCornerTower(topologySlotId, !editor.uniqueCornerTower(topologySlotId));
            screen.flagNeedSetup();
            return true;
        });
        WorkspaceTopologyUiSupport.addRow(screen, content, screen.makeWhiteText(Component.literal(label)), modeButton);
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
}
