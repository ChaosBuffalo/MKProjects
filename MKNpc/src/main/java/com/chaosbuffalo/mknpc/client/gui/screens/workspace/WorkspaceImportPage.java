package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;

import com.chaosbuffalo.mknpc.network.packets.LoadWorkspaceFromManifestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorkspaceImportPage extends WorkspacePageBase {
    public static final String ID = "import";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Load Existing Workspace"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Choose an exported workspace manifest to rehydrate at this dev block."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);

        MKStackLayoutVertical content = createContentStack(screen);
        for (String manifestId : screen.importManifestIds()) {
            MKButton manifestButton = new MKButton(Component.literal(manifestId), screen.contentWidth() - 8, 20);
            content.addWidget(manifestButton);
            manifestButton.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new LoadWorkspaceFromManifestPacket(
                        screen.anchor(), ResourceLocation.parse(manifestId)));
                return true;
            });
        }
        finishScrollContent(screen, scrollView, content);

        addBackButton(screen, root, WorkspaceHomePage.ID);
        return root;
    }
}
