package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorkspaceFormOpeningsPage extends WorkspacePageBase {
    public static final String ID = "form_openings";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Opening Profiles"));
        MKText helpText = addHeaderText(context, root, Component.literal(
                "Choose an opening profile and edit it on its own screen. Opening sizes and path compatibility are authored per profile."));

        MKScrollView scrollView = addScrollBelowHeader(context, root, helpText);
        MKStackLayoutVertical content = createContentStack(context);
        WorkspaceFormDraftEditor editor = context.draftEditor();
        List<MKHorizontalOpeningProfile> openings = editor.openingProfiles();

        for (int i = 0; i < openings.size(); i++) {
            int index = i;
            MKHorizontalOpeningProfile opening = openings.get(index);
            MKText header = context.makeWhiteText(Component.literal(opening.profileId()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            MKText summary = context.makeWhiteText(Component.literal(
                    opening.openingWidth() + "x" + opening.openingHeight() + "  |  " +
                            describePathAccess(opening.allowOnMainPath(), opening.allowOnBranchPath())));
            summary.setWidth(context.contentWidth());
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Opening"), 180, context.buttonHeight());
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                editor.selectedOpeningIndex(index);
                context.pushState("form_opening_detail");
                context.flagNeedSetup();
                return true;
            });
        }

        finishScrollContent(context, scrollView, content);

        MKButton addProfile = addBottomButton(context, root, Component.literal("Add Opening"), 180, 1);
        addProfile.setPressedCallback((button, mouseButton) -> {
            editor.selectedOpeningIndex(editor.addOpeningProfile());
            context.pushState("form_opening_detail");
            context.flagNeedSetup();
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
