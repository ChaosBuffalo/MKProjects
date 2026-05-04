package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspaceFormFamiliesPage extends WorkspacePageBase {
    public static final String ID = "form_families";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Branch Variants"));
        MKText helpText = addHeaderText(context, root, Component.literal(
                "Choose a category first, then edit only the families that belong to that band."));

        MKScrollView scrollView = addScrollBelowHeader(context, root, helpText);
        MKStackLayoutVertical content = createContentStack(context);
        WorkspaceFormDraftEditor editor = context.draftEditor();

        for (MKTowerWorkspaceCategory category : MKTowerWorkspaceCategory.values()) {
            MKText header = context.makeWhiteText(Component.literal(formatTopologyLabel(category.getSerializedName())));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            MKText summary = context.makeWhiteText(Component.literal(editor.familyCount(category) + " families"));
            summary.setWidth(context.contentWidth());
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Open Category"), 180, context.buttonHeight());
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                editor.selectedFamilyCategory(category);
                context.pushState().accept("form_family_category");
                context.flagNeedSetup().run();
                return true;
            });
        }

        finishScrollContent(context, scrollView, content);
        addBackButton(context, root, WorkspaceFormPage.ID);
        return root;
    }

    private String formatTopologyLabel(String key) {
        String[] parts = key.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    builder.append(part.substring(1));
                }
            }
        }
        return builder.toString();
    }
}
