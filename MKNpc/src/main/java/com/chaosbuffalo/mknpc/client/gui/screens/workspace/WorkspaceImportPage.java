package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.network.packets.LoadWorkspaceFromManifestPacket;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorkspaceImportPage implements WorkspacePage {
    public static final String ID = "import";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        int xPos = context.panelX();
        int yPos = context.panelY();
        MKLayout root = new MKLayout(xPos, yPos, context.panelWidth(), context.panelHeight());
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = context.makeWhiteText(Component.literal("Load Existing Workspace"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = context.makeWhiteText(Component.literal(
                "Choose an exported workspace manifest to rehydrate at this dev block."));
        helpText.setWidth(context.contentWidth());
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = context.buttonHeight() + context.bottomPadding();
        int scrollTop = context.scrollTopAfterHeader(root, helpText);
        int scrollHeight = yPos + context.panelHeight() - buttonAreaHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(xPos + 10, scrollTop, context.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, context.contentWidth());
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);
        for (String manifestId : context.importManifestIds()) {
            MKButton manifestButton = new MKButton(Component.literal(manifestId), context.contentWidth() - 8, 20);
            content.addWidget(manifestButton);
            manifestButton.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new LoadWorkspaceFromManifestPacket(
                        context.anchor(), ResourceLocation.parse(manifestId)));
                return true;
            });
        }
        scrollView.addWidget(content);

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + context.panelHeight() - context.bottomPadding() - context.buttonHeight());
        back.setPressedCallback((button, mouseButton) -> {
            context.switchToExistingState().accept(WorkspaceHomePage.ID);
            return true;
        });
        return root;
    }
}
