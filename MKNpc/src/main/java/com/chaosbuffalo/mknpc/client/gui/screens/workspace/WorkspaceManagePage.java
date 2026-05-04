package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

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
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);
        MKStructureWorkspace workspace = context.workspace();

        addTitle(context, root, Component.translatable("mknpc.workspace.screen.manage_title"));
        MKText summary = addHeaderText(context, root, Component.translatable("mknpc.workspace.screen.manage_summary",
                workspace.namespace(), workspace.structureName(), workspace.pieces().size()));

        int buttonCount = 4;
        int buttonAreaHeight = (buttonCount * context.buttonHeight()) +
                ((buttonCount - 1) * context.buttonGap()) + context.bottomPadding();
        int scrollTop = context.scrollTopAfterHeader(root, summary);
        int scrollHeight = context.panelY() + context.panelHeight() - buttonAreaHeight - 8 - scrollTop;
        MKScrollView scrollView = new MKScrollView(context.panelX() + 10, scrollTop,
                context.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(context);
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry :
                WorkspacePieceDisplay.groupPiecesByTopology(workspace).entrySet()) {
            String topologyKey = entry.getKey();
            List<MKWorkspacePieceDefinition> pieces = entry.getValue();
            MKWorkspacePieceDefinition templatePiece = pieces.stream()
                    .filter(piece -> piece.variantIndex() == 0)
                    .findFirst()
                    .orElse(pieces.get(0));

            MKText header = context.makeWhiteText(Component.literal(
                    WorkspacePieceDisplay.buildWorkspaceGroupLabel(templatePiece)));
            header.setWidth(context.contentWidth());
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            int variantCount = WorkspacePieceDisplay.countVariants(pieces);
            long generatedCount = pieces.stream().filter(WorkspacePieceDisplay::hasGeneratedStairs).count();
            MKText details = context.makeWhiteText(Component.literal(
                    pieces.size() + " piece" + (pieces.size() == 1 ? "" : "s") + " - " +
                            variantCount + " variant" + (variantCount == 1 ? "" : "s") +
                            " - template " + WorkspacePieceDisplay.getBaseName(templatePiece) +
                            (WorkspacePieceDisplay.supportsStairGeneration(pieces) ?
                                    " - stairs " + generatedCount + "/" + pieces.size() : "")));
            details.setWidth(context.contentWidth());
            content.addWidget(details);
            content.addConstraintToWidget(MarginConstraint.LEFT, details);

            MKButton openCategory = new MKButton(Component.literal("Open Category"), 180, context.buttonHeight());
            content.addWidget(openCategory);
            content.addConstraintToWidget(new CenterXConstraint(), openCategory);
            openCategory.setPressedCallback((button, mouseButton) -> {
                context.openWorkspaceCategory().accept(topologyKey);
                return true;
            });
        }

        finishScrollContent(context, scrollView, content);

        MKButton close = addBottomButton(context, root, Component.translatable("mknpc.workspace.button.close"), 120, 0);
        close.setPressedCallback((button, mouseButton) -> {
            context.closeScreen().run();
            return true;
        });

        MKButton utilities = addBottomButton(context, root, Component.literal("Utilities"), 180, 1);
        utilities.setPressedCallback((button, mouseButton) -> {
            context.pushState().accept(WorkspaceUtilitiesPage.ID);
            context.flagNeedSetup().run();
            return true;
        });

        MKButton exportAll = addBottomButton(context, root,
                Component.translatable("mknpc.workspace.button.export_all"), 180, 2);
        exportAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new ExportWorkspacePiecesPacket(context.anchor()));
            return true;
        });

        MKButton editTemplates = addBottomButton(context, root,
                Component.translatable("mknpc.workspace.button.edit_template_settings"), 180, 3);
        editTemplates.setPressedCallback((button, mouseButton) -> {
            context.pushState().accept(WorkspaceFormPage.ID);
            context.flagNeedSetup().run();
            return true;
        });

        return root;
    }

}
