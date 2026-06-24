package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

public class WorkspaceTemplateGroupsPage extends WorkspacePageBase {
    public static final String ID = "template_groups";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);
        addTitle(screen, root, Component.literal("Authored Templates"));
        MKText summary = addHeaderText(screen, root, Component.literal(
                "Manage player-authored template variants and stair pieces."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, summary);
        MKStackLayoutVertical content = createContentStack(screen);
        Map<String, List<MKWorkspacePieceDefinition>> groups =
                WorkspacePieceDisplay.groupAuthoredPiecesByTopology(screen.workspace());
        if (groups.isEmpty()) {
            MKText emptyText = screen.makeWhiteText(Component.literal(
                    "No authored template variants have been created yet."));
            emptyText.setWidth(screen.contentWidth());
            emptyText.setMultiline(true);
            content.addWidget(emptyText);
            content.addConstraintToWidget(MarginConstraint.LEFT, emptyText);
        }
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry : groups.entrySet()) {
            addTemplateGroup(screen, content, entry.getKey(), entry.getValue());
        }
        finishScrollContent(screen, scrollView, content);
        addBackButton(screen, root, WorkspaceManagePage.ID);
        return root;
    }

    private void addTemplateGroup(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                  String topologyKey, List<MKWorkspacePieceDefinition> pieces) {
        MKWorkspacePieceDefinition templatePiece = pieces.stream()
                .filter(piece -> piece.variantIndex() == 0)
                .findFirst()
                .orElse(pieces.getFirst());

        MKText header = screen.makeWhiteText(Component.literal(
                WorkspacePieceDisplay.buildWorkspaceGroupLabel(templatePiece)));
        header.setWidth(screen.contentWidth());
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        int variantCount = WorkspacePieceDisplay.countVariants(pieces);
        long generatedCount = pieces.stream().filter(WorkspacePieceDisplay::hasGeneratedStairs).count();
        MKText details = screen.makeWhiteText(Component.literal(
                pieces.size() + " authored piece" + (pieces.size() == 1 ? "" : "s") + " - " +
                        variantCount + " variant" + (variantCount == 1 ? "" : "s") +
                        " - base " + WorkspacePieceDisplay.getBaseName(templatePiece) +
                        (WorkspacePieceDisplay.supportsStairGeneration(pieces) ?
                                " - stairs " + generatedCount + "/" + pieces.size() : "")));
        details.setWidth(screen.contentWidth());
        content.addWidget(details);
        content.addConstraintToWidget(MarginConstraint.LEFT, details);

        MKButton openCategory = new MKButton(Component.literal("Open Group"), 180, screen.buttonHeight());
        content.addWidget(openCategory);
        content.addConstraintToWidget(new CenterXConstraint(), openCategory);
        openCategory.setPressedCallback((button, mouseButton) -> {
            screen.openWorkspaceTopologySlot(topologyKey);
            return true;
        });
    }
}
