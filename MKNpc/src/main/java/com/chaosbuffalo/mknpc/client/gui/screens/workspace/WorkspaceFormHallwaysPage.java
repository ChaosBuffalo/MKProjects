package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

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
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Hallway Families"));
        MKText helpText = addHeaderText(context, root, Component.literal(
                "Choose a hallway family and edit it on its own screen. Each hallway defines its opening profile, dimensions, slope, path usage, and palette overrides."));

        MKScrollView scrollView = addScrollBelowHeader(context, root, helpText);
        MKStackLayoutVertical content = createContentStack(context);
        WorkspaceFormDraftEditor editor = context.draftEditor();
        List<MKHallwayFamilyDefinition> hallways = editor.hallwayFamilies();

        for (int i = 0; i < hallways.size(); i++) {
            int index = i;
            MKHallwayFamilyDefinition hallway = hallways.get(index);
            MKText header = context.makeWhiteText(Component.literal(hallway.hallwayId()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            MKText summary = context.makeWhiteText(Component.literal(
                    hallway.openingProfileId() + "  |  " + hallway.length() + "x" +
                            hallway.interiorWidth() + "x" + hallway.interiorHeight() +
                            "  |  slope " + hallway.slopeDelta() + "  |  " +
                            describePathAccess(hallway.allowOnMainPath(), hallway.allowOnBranchPath())));
            summary.setWidth(context.contentWidth());
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Hallway"), 180, context.buttonHeight());
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                editor.selectedHallwayIndex(index);
                context.pushState().accept("form_hallway_detail");
                context.flagNeedSetup().run();
                return true;
            });
        }

        finishScrollContent(context, scrollView, content);

        MKButton addHallway = addBottomButton(context, root, Component.literal("Add Hallway"), 180, 1);
        addHallway.setPressedCallback((button, mouseButton) -> {
            editor.selectedHallwayIndex(editor.addHallwayFamily());
            context.pushState().accept("form_hallway_detail");
            context.flagNeedSetup().run();
            return true;
        });

        addBackButton(context, root, WorkspaceFormPage.ID);
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
