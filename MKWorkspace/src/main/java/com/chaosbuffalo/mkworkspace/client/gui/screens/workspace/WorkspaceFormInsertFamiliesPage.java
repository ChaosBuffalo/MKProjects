package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorkspaceFormInsertFamiliesPage extends WorkspacePageBase {
    public static final String ID = "form_insert_families";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Insert Slots"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Choose a user-declared insert slot. A slot defines socket-compatible bounds and owns a non-placeable scaffold; content families are managed separately."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKWorkspaceInsertFamilyDefinition> insertFamilies = editor.insertSlots();

        for (int i = 0; i < insertFamilies.size(); i++) {
            int index = i;
            MKWorkspaceInsertFamilyDefinition insertFamily = insertFamilies.get(index);
            MKText header = screen.makeWhiteText(Component.literal(insertFamily.slotId()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            MKText summary = screen.makeWhiteText(Component.literal(
                    WorkspacePieceDisplay.formatTopologyLabel(insertFamily.kind().getSerializedName()) +
                            "  |  " + insertFamily.width() + "x" + insertFamily.height() + "x" +
                            insertFamily.depth()));
            summary.setWidth(screen.contentWidth());
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Insert"), 180, screen.buttonHeight());
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                editor.selectedInsertFamilyIndex(index);
                screen.pushState(WorkspaceFormInsertFamilyDetailPage.ID);
                screen.flagNeedSetup();
                return true;
            });
        }

        finishScrollContent(screen, scrollView, content);

        MKButton addInsertFamily = addBottomButton(screen, root, Component.literal("Add Insert"), 180, 1);
        addInsertFamily.setPressedCallback((button, mouseButton) -> {
            editor.selectedInsertFamilyIndex(editor.addInsertFamily());
            screen.pushState(WorkspaceFormInsertFamilyDetailPage.ID);
            screen.flagNeedSetup();
            return true;
        });

        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }
}
