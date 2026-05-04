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
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorkspaceFormOpeningDetailPage extends WorkspacePageBase {
    public static final String ID = "form_opening_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKHorizontalOpeningProfile> openings = editor.openingProfiles();
        int index = editor.selectedOpeningIndex();
        if (index < 0 || index >= openings.size()) {
            screen.switchToExistingState(WorkspaceFormOpeningsPage.ID);
            return new WorkspaceFormOpeningsPage().build(screen);
        }

        MKHorizontalOpeningProfile opening = openings.get(index);
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Opening: " + opening.profileId()));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Edit one opening profile at a time. Profiles can be restricted to the main path, branch path, or both."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);

        MKTextFieldWidget idField = makeField(screen, "Profile Id", opening.profileId());
        idField.setTextChangeCallback((field, text) -> editor.replaceOpeningProfile(index,
                new MKHorizontalOpeningProfile(
                        text.trim().isBlank() ? opening.profileId() : text.trim(),
                        opening.openingWidth(),
                        opening.openingHeight(),
                        opening.allowOnMainPath(),
                        opening.allowOnBranchPath())));
        MKTextFieldWidget widthField = makeField(screen, "Opening Width", Integer.toString(opening.openingWidth()));
        widthField.setTextChangeCallback((field, text) -> editor.replaceOpeningProfile(index,
                new MKHorizontalOpeningProfile(
                        opening.profileId(),
                        parseInt(text, opening.openingWidth()),
                        opening.openingHeight(),
                        opening.allowOnMainPath(),
                        opening.allowOnBranchPath())));
        MKTextFieldWidget heightField = makeField(screen, "Opening Height", Integer.toString(opening.openingHeight()));
        heightField.setTextChangeCallback((field, text) -> editor.replaceOpeningProfile(index,
                new MKHorizontalOpeningProfile(
                        opening.profileId(),
                        opening.openingWidth(),
                        parseInt(text, opening.openingHeight()),
                        opening.allowOnMainPath(),
                        opening.allowOnBranchPath())));
        MKButton mainButton = new MKButton(Component.literal(opening.allowOnMainPath() ? "Enabled" : "Disabled"), 180,
                screen.buttonHeight());
        mainButton.setPressedCallback((button, mouseButton) -> {
            editor.replaceOpeningProfile(index, new MKHorizontalOpeningProfile(opening.profileId(),
                    opening.openingWidth(), opening.openingHeight(), !opening.allowOnMainPath(),
                    opening.allowOnBranchPath()));
            screen.flagNeedSetup();
            return true;
        });
        MKButton branchButton = new MKButton(Component.literal(opening.allowOnBranchPath() ? "Enabled" : "Disabled"), 180,
                screen.buttonHeight());
        branchButton.setPressedCallback((button, mouseButton) -> {
            editor.replaceOpeningProfile(index, new MKHorizontalOpeningProfile(opening.profileId(),
                    opening.openingWidth(), opening.openingHeight(), opening.allowOnMainPath(),
                    !opening.allowOnBranchPath()));
            screen.flagNeedSetup();
            return true;
        });

        addRow(screen, content, "Profile Id", idField);
        addRow(screen, content, "Opening Width", widthField);
        addRow(screen, content, "Opening Height", heightField);
        addRow(screen, content, "Allow On Main Path", mainButton);
        addRow(screen, content, "Allow On Branch Path", branchButton);

        finishScrollContent(screen, scrollView, content);

        MKButton remove = addBottomButton(screen, root, Component.literal("Remove Profile"), 180, 1);
        remove.setPressedCallback((button, mouseButton) -> {
            editor.removeOpeningProfile(index);
            editor.selectedOpeningIndex(-1);
            screen.switchToExistingState(WorkspaceFormOpeningsPage.ID);
            return true;
        });

        addBackButton(screen, root, WorkspaceFormOpeningsPage.ID);
        return root;
    }

    private MKTextFieldWidget makeField(MKWorkspaceScreen screen, String label, String value) {
        MKTextFieldWidget widget = new MKTextFieldWidget(screen.font(), 0, 0, 180, 18,
                Component.literal(label));
        widget.setText(value);
        return widget;
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
                        MKTextFieldWidget field) {
        MKText labelText = screen.makeWhiteText(Component.literal(label));
        labelText.setWidth(screen.contentWidth());
        root.addWidget(labelText);
        root.addConstraintToWidget(MarginConstraint.LEFT, labelText);
        root.addWidget(field);
        root.addConstraintToWidget(new CenterXConstraint(), field);
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label, MKButton button) {
        MKText labelText = screen.makeWhiteText(Component.literal(label));
        labelText.setWidth(screen.contentWidth());
        root.addWidget(labelText);
        root.addConstraintToWidget(MarginConstraint.LEFT, labelText);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}


