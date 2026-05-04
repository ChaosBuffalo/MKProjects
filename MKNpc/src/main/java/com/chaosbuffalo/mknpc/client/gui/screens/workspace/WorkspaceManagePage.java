package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;

import com.chaosbuffalo.mknpc.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
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

            MKButton openCategory = new MKButton(Component.literal("Open Category"), 180, screen.buttonHeight());
            content.addWidget(openCategory);
            content.addConstraintToWidget(new CenterXConstraint(), openCategory);
            openCategory.setPressedCallback((button, mouseButton) -> {
                screen.openWorkspaceCategory(topologyKey);
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

}
