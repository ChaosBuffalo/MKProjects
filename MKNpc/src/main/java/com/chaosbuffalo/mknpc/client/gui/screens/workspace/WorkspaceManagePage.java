package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mknpc.network.packets.RequestWorkspacePreflightPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayerState;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;

public class WorkspaceManagePage extends WorkspacePageBase {
    public static final String ID = "workspace";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);
        MKStructureWorkspace workspace = screen.workspace();

        addTitle(screen, root, Component.translatable("mknpc.workspace.screen.manage_title"));
        MKText summary = addHeaderText(screen, root, Component.translatable("mknpc.workspace.screen.manage_summary",
                workspace.namespace(), workspace.structureName(), workspace.pieces().size()));

        int buttonCount = 4;
        int buttonAreaHeight = (buttonCount * screen.buttonHeight()) +
                ((buttonCount - 1) * screen.buttonGap()) + screen.bottomPadding();
        int scrollTop = screen.scrollTopAfterHeader(root, summary);
        int scrollHeight = screen.panelY() + screen.panelHeight() - buttonAreaHeight - 8 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(screen);
        addPlannerOverview(screen, content, workspace);
        addTemplateAuthoringSummary(screen, content, workspace);

        finishScrollContent(screen, scrollView, content);

        MKButton close = addBottomButton(screen, root, Component.translatable("mknpc.workspace.button.close"), 120, 0);
        close.setPressedCallback((button, mouseButton) -> {
            screen.closeScreen();
            return true;
        });

        MKButton utilities = addBottomButton(screen, root, Component.literal("Utilities"), 180, 1);
        utilities.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceUtilitiesPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        MKButton exportAll = addBottomButton(screen, root,
                Component.translatable("mknpc.workspace.button.export_all"), 180, 2);
        exportAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new ExportWorkspacePiecesPacket(screen.anchor()));
            return true;
        });

        MKButton editTemplates = addBottomButton(screen, root,
                Component.translatable("mknpc.workspace.button.edit_template_settings"), 180, 3);
        editTemplates.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceFormPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        return root;
    }

    private void addPlannerOverview(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    MKStructureWorkspace workspace) {
        addText(screen, content, "Planner Overview");
        addText(screen, content, "Planner " + workspace.topologyProfile().plannerId() +
                " - pieces " + workspace.pieces().size() +
                " - layers " + workspace.layerStates().size());
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID.equals(workspace.topologyProfile().plannerId())) {
            MKWalledKeepSizingReport report = new MKWalledKeepSizingCalculator().calculate(workspace);
            MKWalledKeepFootprintPreview preview = new MKWalledKeepFootprintPreview(
                    Math.min(screen.contentWidth(), 260), 190, workspace, report);
            content.addWidget(preview);
            content.addConstraintToWidget(new CenterXConstraint(), preview);
        }
        addPlannerNavigation(screen, content, workspace);
        addLayerStateSummary(screen, content, workspace);
        addPreflightReport(screen, content, screen.preflight());

        MKButton preflight = new MKButton(Component.literal("Preflight Draft"), 180, screen.buttonHeight());
        content.addWidget(preflight);
        content.addConstraintToWidget(new CenterXConstraint(), preflight);
        preflight.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new RequestWorkspacePreflightPacket(
                    screen.draftSession().buildWorkspaceDraft()));
            return true;
        });

        MKButton editPlanner = new MKButton(Component.literal("Planner Settings"), 180, screen.buttonHeight());
        content.addWidget(editPlanner);
        content.addConstraintToWidget(new CenterXConstraint(), editPlanner);
        editPlanner.setPressedCallback((button, mouseButton) -> {
            openPlannerSettings(screen);
            return true;
        });
    }

    private void addPlannerNavigation(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                      MKStructureWorkspace workspace) {
        addText(screen, content, "Planner Navigation");
        if (MKWorkspaceTopologyProfile.WALLED_KEEP_PLANNER_ID.equals(workspace.topologyProfile().plannerId())) {
            addPlannerButton(screen, content, "Keep Footprint", null);
            addPlannerButton(screen, content, "Perimeter & Courtyard", null);
            for (String stackId : screen.draftSession().walledKeepTowerStackTabs()) {
                addPlannerButton(screen, content,
                        WorkspacePieceDisplay.formatTopologyLabel(stackId), stackId);
            }
            return;
        }
        addPlannerButton(screen, content, "Tower Planner", null);
    }

    private void addPlannerButton(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                  String label, String towerStackId) {
        MKButton button = new MKButton(Component.literal(label), 180, screen.buttonHeight());
        content.addWidget(button);
        content.addConstraintToWidget(new CenterXConstraint(), button);
        button.setPressedCallback((pressedButton, mouseButton) -> {
            if (towerStackId != null) {
                screen.draftSession().walledKeepTowerStackTab(towerStackId);
            }
            openPlannerSettings(screen);
            return true;
        });
    }

    private void openPlannerSettings(MKWorkspaceScreen screen) {
        screen.pushState(WorkspaceFormPage.ID);
        screen.pushState(WorkspaceTopologyDefaultsPage.ID);
        screen.flagNeedSetup();
    }

    private void addLayerStateSummary(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                      MKStructureWorkspace workspace) {
        if (workspace.layerStates().isEmpty()) {
            addText(screen, content, "Layer state has not been initialized for this workspace yet.");
        }
        long locked = workspace.layerStates().stream().filter(MKWorkspaceGeneratedLayerState::locked).count();
        long dirtyCount = workspace.layerStates().stream().filter(MKWorkspaceGeneratedLayerState::dirty).count();
        addText(screen, content, "Generation protection: " + locked + " locked layers, " +
                dirtyCount + " pending updates.");
        addText(screen, content, "Protected settings unlock from their planner menus after impact review.");
    }

    private void addTemplateAuthoringSummary(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                             MKStructureWorkspace workspace) {
        Map<String, List<MKWorkspacePieceDefinition>> groups = WorkspacePieceDisplay.groupPiecesByTopology(workspace);
        int variantCount = groups.values().stream().mapToInt(WorkspacePieceDisplay::countVariants).sum();
        long generatedStairCount = workspace.pieces().stream()
                .filter(WorkspacePieceDisplay::hasGeneratedStairs)
                .count();
        addText(screen, content, "Template Authoring");
        addText(screen, content, groups.size() + " template groups - " + variantCount +
                " variants - " + generatedStairCount + " stair-authored pieces");
        addText(screen, content, "Open template groups for variants and piece-level stair generation.");

        MKButton openGroups = new MKButton(Component.literal("Template Groups"), 180, screen.buttonHeight());
        content.addWidget(openGroups);
        content.addConstraintToWidget(new CenterXConstraint(), openGroups);
        openGroups.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceTemplateGroupsPage.ID);
            screen.flagNeedSetup();
            return true;
        });
    }

    private void addPreflightReport(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    MKWorkspaceMutationPreflight preflight) {
        if (preflight == null) {
            addText(screen, content, "No impact report loaded.");
            return;
        }
        MKWorkspaceInvalidationReport report = preflight.report();
        addText(screen, content, "Impact Report");
        addText(screen, content, report.summary());
        addText(screen, content, "Safety " + report.safety().getSerializedName() +
                " - operation " + report.recommendedOperation());
        if (!report.invalidatedLayers().isEmpty()) {
            addText(screen, content, "Invalidates: " + report.invalidatedLayers().stream()
                    .map(layer -> layer.getSerializedName())
                    .reduce((left, right) -> left + ", " + right)
                    .orElse(""));
        }
        if (!report.affectedPlannerIds().isEmpty()) {
            addText(screen, content, "Affected ids: " + report.affectedPlannerIds().size());
        }
        if (!report.preservedTemplateBindings().isEmpty()) {
            addText(screen, content, "Preserves bindings: " + report.preservedTemplateBindings().size());
        }
        if (!report.orphanedTemplateBindings().isEmpty()) {
            addText(screen, content, "Orphaned bindings: " + report.orphanedTemplateBindings().size());
        }
        if (!report.remapSuggestions().isEmpty()) {
            addText(screen, content, "Remap suggestions: " + report.remapSuggestions().size());
        }
        for (String warning : report.warnings()) {
            addText(screen, content, "Warning: " + warning);
        }
    }

    private void addText(MKWorkspaceScreen screen, MKStackLayoutVertical content, String text) {
        MKText widget = screen.makeWhiteText(Component.literal(text));
        widget.setWidth(screen.contentWidth());
        content.addWidget(widget);
        content.addConstraintToWidget(MarginConstraint.LEFT, widget);
    }

}
