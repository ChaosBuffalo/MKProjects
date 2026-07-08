package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mknpc.network.packets.RequestWorkspacePreflightPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayerState;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
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
                workspace.namespace(), workspace.structureName(), screen.totalWorkspacePieces()));

        int buttonAreaHeight = screen.buttonHeight() + screen.bottomPadding();
        int contentTop = screen.scrollTopAfterHeader(root, summary);
        int contentHeight = screen.panelY() + screen.panelHeight() - buttonAreaHeight - 8 - contentTop;
        WorkspacePlannerLayout layout = addPlannerLayout(screen, root, contentTop, contentHeight);
        addPlannerOverview(screen, layout, workspace);
        addTemplateAuthoringSummary(screen, layout.settingsContent(), workspace);
        finishPlannerLayout(screen, layout);

        int footerY = screen.panelY() + screen.panelHeight() - screen.bottomPadding() - screen.buttonHeight();
        int[] footerWidths = {120, 150, 170, 190};
        int footerGap = 8;
        int footerWidth = footerWidths[0] + footerWidths[1] + footerWidths[2] + footerWidths[3] + (footerGap * 3);
        int footerX = screen.panelX() + (screen.panelWidth() - footerWidth) / 2;

        MKButton close = addFooterButton(root, Component.translatable("mknpc.workspace.button.close"),
                footerWidths[0], footerX, footerY);
        close.setPressedCallback((button, mouseButton) -> {
            screen.closeScreen();
            return true;
        });

        MKButton utilities = addFooterButton(root, Component.literal("Utilities"), footerWidths[1],
                footerX + footerWidths[0] + footerGap, footerY);
        utilities.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceUtilitiesPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        MKButton exportAll = addFooterButton(root, Component.translatable("mknpc.workspace.button.export_all"),
                footerWidths[2], footerX + footerWidths[0] + footerWidths[1] + (footerGap * 2), footerY);
        exportAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new ExportWorkspacePiecesPacket(screen.anchor()));
            return true;
        });

        MKButton editTemplates = addFooterButton(root,
                Component.translatable("mknpc.workspace.button.edit_template_settings"), footerWidths[3],
                footerX + footerWidths[0] + footerWidths[1] + footerWidths[2] + (footerGap * 3), footerY);
        editTemplates.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceFormPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        return root;
    }

    private MKButton addFooterButton(MKLayout root, Component label, int width, int x, int y) {
        MKButton button = new MKButton(label, width, 20);
        button.setX(x);
        button.setY(y);
        root.addWidget(button);
        return button;
    }

    private void addPlannerOverview(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                    MKStructureWorkspace workspace) {
        MKStackLayoutVertical content = layout.settingsContent();
        addText(screen, content, "Planner Overview");
        addText(screen, content, "Planner " + workspace.topologyProfile().plannerId() +
                " - pieces " + screen.loadedWorkspacePieces() + "/" + screen.totalWorkspacePieces() +
                " - layers " + workspace.layerStates().size());
        if (screen.loadingWorkspacePieces()) {
            addText(screen, content, "Loading workspace pieces...");
        }
        WorkspacePlannerClientRegistry.getClientContributor(workspace.topologyProfile().plannerId())
                .addWorkspaceOverviewLayout(screen, layout, screen.draftSession());
        addLayerStateSummary(screen, content, workspace);
        addPreflightReport(screen, content, screen.preflight());

        MKButton preflight = new MKButton(Component.literal("Preflight Draft"), 180, screen.buttonHeight());
        content.addWidget(preflight);
        content.addConstraintToWidget(new CenterXConstraint(), preflight);
        preflight.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new RequestWorkspacePreflightPacket(
                    screen.draftSession().buildWorkspaceDraft(), screen.draftSession().acceptedRemaps()));
            return true;
        });

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
        Map<String, List<MKWorkspacePieceDefinition>> authoredGroups =
                WorkspacePieceDisplay.groupAuthoredPiecesByTopology(workspace);
        int variantCount = authoredGroups.values().stream().mapToInt(WorkspacePieceDisplay::countVariants).sum();
        long generatedStairCount = workspace.pieces().stream()
                .filter(WorkspacePieceDisplay::isAuthoredTemplatePiece)
                .filter(WorkspacePieceDisplay::hasGeneratedStairs)
                .count();
        addText(screen, content, "Template Authoring");
        addText(screen, content, authoredGroups.size() + " authored template groups - " + variantCount +
                " variants - " + generatedStairCount + " stair-authored pieces");
        addText(screen, content, "Open template families for variants and piece-level stair generation.");

        MKButton openGroups = new MKButton(Component.literal("Template Families"), 180, screen.buttonHeight());
        content.addWidget(openGroups);
        content.addConstraintToWidget(new CenterXConstraint(), openGroups);
        openGroups.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceFormFamiliesPage.ID);
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
        addRelayoutImpactReport(screen, content, report);
        if (!report.remapSuggestions().isEmpty()) {
            addText(screen, content, "Remap suggestions: " + report.remapSuggestions().size());
            long safeRemaps = report.remapSuggestions().stream()
                    .filter(suggestion -> suggestion.score() >= 85)
                    .count();
            if (safeRemaps > 0) {
                addText(screen, content, "Safe remaps available: " + safeRemaps +
                        " - accepted " + screen.draftSession().acceptedRemapCount());
                MKButton acceptAllSafe = new MKButton(Component.literal("Accept All Safe Remaps"), 180,
                        screen.buttonHeight());
                content.addWidget(acceptAllSafe);
                content.addConstraintToWidget(new CenterXConstraint(), acceptAllSafe);
                acceptAllSafe.setPressedCallback((button, mouseButton) -> {
                    screen.draftSession().acceptAllSafeRemaps(report);
                    PacketDistributor.sendToServer(new RequestWorkspacePreflightPacket(
                            screen.draftSession().buildWorkspaceDraft(), screen.draftSession().acceptedRemaps()));
                    screen.flagNeedSetup();
                    return true;
                });
            }
            for (MKWorkspaceTemplateRemapSuggestion suggestion : report.remapSuggestions()) {
                addText(screen, content, "Remap " + suggestion.orphanedPlannerId() + " -> " +
                        suggestion.targetPlannerId() + " (" + suggestion.score() + ") " + suggestion.reason());
            }
        }
        for (String warning : report.warnings()) {
            addText(screen, content, "Warning: " + warning);
        }
    }
}
