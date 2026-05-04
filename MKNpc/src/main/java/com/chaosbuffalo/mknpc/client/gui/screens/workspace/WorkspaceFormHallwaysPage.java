package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorkspaceFormHallwaysPage extends WorkspacePageBase {
    public static final String ID = "form_hallways";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Hallway Families"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Choose a hallway family and edit it on its own screen. Each hallway defines its opening profile, dimensions, slope, path usage, and palette overrides."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKHallwayFamilyDefinition> hallways = editor.hallwayFamilies();

        for (int i = 0; i < hallways.size(); i++) {
            int index = i;
            MKHallwayFamilyDefinition hallway = hallways.get(index);
            MKText header = screen.makeWhiteText(Component.literal(hallway.hallwayId()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            MKText summary = screen.makeWhiteText(Component.literal(
                    hallway.openingProfileId() + "  |  " + hallway.length() + "x" +
                            hallway.interiorWidth() + "x" + hallway.interiorHeight() +
                            "  |  slope " + hallway.slopeDelta() + "  |  " +
                            describePathAccess(hallway.allowOnMainPath(), hallway.allowOnBranchPath())));
            summary.setWidth(screen.contentWidth());
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Hallway"), 180, screen.buttonHeight());
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                editor.selectedHallwayIndex(index);
                screen.pushState("form_hallway_detail");
                screen.flagNeedSetup();
                return true;
            });
        }

        finishScrollContent(screen, scrollView, content);

        MKButton addHallway = addBottomButton(screen, root, Component.literal("Add Hallway"), 180, 1);
        addHallway.setPressedCallback((button, mouseButton) -> {
            editor.selectedHallwayIndex(editor.addHallwayFamily());
            screen.pushState("form_hallway_detail");
            screen.flagNeedSetup();
            return true;
        });

        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }

    private String describePathAccess(boolean main, boolean branch) {
        if (main && branch) {
            return "main + branch";
        }
        if (main) {
            return "main only";
        }
        if (branch) {
            return "branch only";
        }
        return "disabled";
    }
}


