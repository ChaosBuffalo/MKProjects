package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceGeneratedLayerState;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
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
        finishPlannerLayout(screen, layout);

        int footerY = screen.panelY() + screen.panelHeight() - screen.bottomPadding() - screen.buttonHeight();
        int[] footerWidths = {90, 150, 120, 150, 160};
        int footerGap = 8;
        int footerWidth = footerWidths[0] + footerWidths[1] + footerWidths[2] + footerWidths[3] + footerWidths[4] +
                (footerGap * 4);
        int footerX = screen.panelX() + (screen.panelWidth() - footerWidth) / 2;

        MKButton close = addFooterButton(root, Component.translatable("mknpc.workspace.button.close"),
                footerWidths[0], footerX, footerY);
        close.setPressedCallback((button, mouseButton) -> {
            screen.closeScreen();
            return true;
        });

        MKButton apply = new MKButton(Component.literal("Apply Changes"), footerWidths[1], screen.buttonHeight()) {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && screen.draftSession().dirty();
            }
        };
        apply.setX(footerX + footerWidths[0] + footerGap);
        apply.setY(footerY);
        root.addWidget(apply);
        apply.setPressedCallback((button, mouseButton) -> {
            screen.draftSession().submit();
            return true;
        });

        MKButton utilities = addFooterButton(root, Component.literal("Utilities"), footerWidths[2],
                footerX + footerWidths[0] + footerWidths[1] + (footerGap * 2), footerY);
        utilities.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceUtilitiesPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        MKButton exportAll = addFooterButton(root, Component.translatable("mknpc.workspace.button.export_all"),
                footerWidths[3],
                footerX + footerWidths[0] + footerWidths[1] + footerWidths[2] + (footerGap * 3), footerY);
        exportAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new ExportWorkspacePiecesPacket(screen.anchor()));
            return true;
        });

        MKButton workspaceSettings = addFooterButton(root,
                Component.literal("Workspace Settings"), footerWidths[4],
                footerX + footerWidths[0] + footerWidths[1] + footerWidths[2] + footerWidths[3] +
                        (footerGap * 4), footerY);
        workspaceSettings.setPressedCallback((button, mouseButton) -> {
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
        addTemplateAuthoringSummary(screen, layout.previewContent(), workspace);
        addLayerStateSummary(screen, content, workspace);
        addText(screen, content, "All persistent changes are preflighted when Apply Changes is selected.");

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
        addText(screen, content, authoredGroups.size() + " authored content families - " + variantCount +
                " variants - " + generatedStairCount + " stair-authored pieces");
        addText(screen, content, "Browse topology slots and their content families for template authoring.");

        MKButton openGroups = new MKButton(Component.literal("Template Families"), 180, screen.buttonHeight());
        content.addWidget(openGroups);
        content.addConstraintToWidget(new CenterXConstraint(), openGroups);
        openGroups.setPressedCallback((button, mouseButton) -> {
            screen.pushState(WorkspaceFormFamiliesPage.ID);
            screen.flagNeedSetup();
            return true;
        });
    }

}
