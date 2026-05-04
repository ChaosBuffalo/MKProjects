package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
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
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Opening Profiles"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Choose an opening profile and edit it on its own screen. Opening sizes and path compatibility are authored per profile."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKHorizontalOpeningProfile> openings = editor.openingProfiles();

        for (int i = 0; i < openings.size(); i++) {
            int index = i;
            MKHorizontalOpeningProfile opening = openings.get(index);
            MKText header = screen.makeWhiteText(Component.literal(opening.profileId()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            MKText summary = screen.makeWhiteText(Component.literal(
                    opening.openingWidth() + "x" + opening.openingHeight() + "  |  " +
                            describePathAccess(opening.allowOnMainPath(), opening.allowOnBranchPath())));
            summary.setWidth(screen.contentWidth());
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Opening"), 180, screen.buttonHeight());
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                editor.selectedOpeningIndex(index);
                screen.pushState("form_opening_detail");
                screen.flagNeedSetup();
                return true;
            });
        }

        finishScrollContent(screen, scrollView, content);

        MKButton addProfile = addBottomButton(screen, root, Component.literal("Add Opening"), 180, 1);
        addProfile.setPressedCallback((button, mouseButton) -> {
            editor.selectedOpeningIndex(editor.addOpeningProfile());
            screen.pushState("form_opening_detail");
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


