package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mknpc.network.packets.RequestWorkspacePreflightPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayerState;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
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
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry :
                WorkspacePieceDisplay.groupPiecesByTopology(workspace).entrySet()) {
            String topologyKey = entry.getKey();
            List<MKWorkspacePieceDefinition> pieces = entry.getValue();
            MKWorkspacePieceDefinition templatePiece = pieces.stream()
                    .filter(piece -> piece.variantIndex() == 0)
                    .findFirst()
                    .orElse(pieces.get(0));

            MKText header = screen.makeWhiteText(Component.literal(
                    WorkspacePieceDisplay.buildWorkspaceGroupLabel(templatePiece)));
            header.setWidth(screen.contentWidth());
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            int variantCount = WorkspacePieceDisplay.countVariants(pieces);
            long generatedCount = pieces.stream().filter(WorkspacePieceDisplay::hasGeneratedStairs).count();
            MKText details = screen.makeWhiteText(Component.literal(
                    pieces.size() + " piece" + (pieces.size() == 1 ? "" : "s") + " - " +
                            variantCount + " variant" + (variantCount == 1 ? "" : "s") +
                            " - template " + WorkspacePieceDisplay.getBaseName(templatePiece) +
                            (WorkspacePieceDisplay.supportsStairGeneration(pieces) ?
                                    " - stairs " + generatedCount + "/" + pieces.size() : "")));
            details.setWidth(screen.contentWidth());
            content.addWidget(details);
            content.addConstraintToWidget(MarginConstraint.LEFT, details);

            MKButton openCategory = new MKButton(Component.literal("Open Templates"), 180, screen.buttonHeight());
            content.addWidget(openCategory);
            content.addConstraintToWidget(new CenterXConstraint(), openCategory);
            openCategory.setPressedCallback((button, mouseButton) -> {
                screen.openWorkspaceTopologySlot(topologyKey);
                return true;
            });
        }

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
            screen.pushState(WorkspaceTopologyDefaultsPage.ID);
            screen.flagNeedSetup();
            return true;
        });
    }

    private void addLayerStateSummary(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                      MKStructureWorkspace workspace) {
        if (workspace.layerStates().isEmpty()) {
            addText(screen, content, "Layer state has not been initialized for this workspace yet.");
            return;
        }
        long locked = workspace.layerStates().stream().filter(MKWorkspaceGeneratedLayerState::locked).count();
        long dirty = workspace.layerStates().stream().filter(MKWorkspaceGeneratedLayerState::dirty).count();
        addText(screen, content, "Layer status: " + locked + " locked, " + dirty + " dirty");
        workspace.layerStates().stream()
                .filter(state -> state.locked() || state.dirty())
                .limit(8)
                .forEach(state -> addText(screen, content, "- " + state.layer().getSerializedName() +
                        " " + (state.locked() ? "locked" : "unlocked") +
                        (state.dirty() ? " dirty" : "")));
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
