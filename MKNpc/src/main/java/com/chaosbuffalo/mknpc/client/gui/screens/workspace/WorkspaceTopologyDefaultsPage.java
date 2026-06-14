package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

public class WorkspaceTopologyDefaultsPage extends WorkspacePageBase {
    public static final String ID = "topology_defaults";
    public static final String DETAIL_ID = "topology_defaults_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        editor.ensureInitialized();

        MKLayout root = createPanel(screen);
        addTitle(screen, root, Component.literal("Topology Defaults"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Set the broad sizing and stack defaults for this planner before editing individual family overrides."));

        int contentTop = screen.scrollTopAfterHeader(root, helpText);
        int contentHeight = screen.panelY() + screen.panelHeight() - screen.bottomPadding() -
                screen.buttonHeight() - 12 - contentTop;
        WorkspacePlannerLayout layout = addPlannerLayout(screen, root, contentTop, contentHeight);
        MKStackLayoutVertical content = layout.settingsContent();

        MKText topologyText = screen.makeWhiteText(Component.literal(
                "Active planner: " + editor.topologyPlannerId()));
        topologyText.setWidth(screen.contentWidth());
        topologyText.setMultiline(true);
        content.addWidget(topologyText);
        content.addConstraintToWidget(MarginConstraint.LEFT, topologyText);

        WorkspacePlannerUiRegistry.getTopologyUi(editor.topologyPlannerId())
                .addDefaultsLayout(screen, layout, editor);

        finishPlannerLayout(screen, layout);
        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }
}
