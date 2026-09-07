package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.network.packets.CancelWorkspaceChangePacket;
import com.chaosbuffalo.mkworkspace.network.packets.ConfirmWorkspaceChangePacket;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeSummaryFormatter;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class WorkspaceGenerateConfirmPage extends WorkspacePageBase {
    public static final String ID = "generate_confirm";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        MKWorkspaceClientChangePlan plan = screen.changePlan();
        addTitle(screen, root, Component.literal(plan == null ? "Confirm Workspace Change" : plan.summary().title()));
        addHeaderText(screen, root, Component.literal(
                "The server prepared this exact change. Confirmation writes one backup before applying it when required."));
        String workspaceLabel = screen.workspace() == null ? "Anchor " + screen.anchor().toShortString() :
                screen.workspace().namespace() + ":" + screen.workspace().structureName();
        MKText workspaceId = addHeaderText(screen, root, Component.literal(workspaceLabel));

        int scrollTop = screen.scrollTopAfterHeader(root, workspaceId);
        int buttonRowsHeight = (screen.buttonHeight() * 3) + (screen.buttonGap() * 2) + screen.bottomPadding();
        int scrollHeight = screen.panelY() + screen.panelHeight() - buttonRowsHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(screen);
        List<String> summaryLines = preflightSummaryLines(screen);
        for (String line : summaryLines) {
            addText(screen, content, line);
        }
        finishScrollContent(screen, scrollView, content);

        MKButton confirm = new MKButton(Component.literal("Apply Workspace Change"), 220, screen.buttonHeight()) {
            @Override
            public boolean isEnabled() {
                MKWorkspaceClientChangePlan current = screen.changePlan();
                return super.isEnabled() && current != null && current.complete() &&
                        current.summary().canConfirm();
            }
        };
        root.addWidget(confirm);
        root.addConstraintToWidget(new com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint(), confirm);
        confirm.setY(screen.panelY() + screen.panelHeight() - screen.bottomPadding() - screen.buttonHeight() -
                (screen.buttonHeight() + screen.buttonGap()));
        confirm.setPressedCallback((button, mouseButton) -> {
            button.setEnabled(false);
            button.buttonText = Component.literal("Applying...");
            MKWorkspaceClientChangePlan current = screen.changePlan();
            if (current != null && current.complete()) {
                PacketDistributor.sendToServer(new ConfirmWorkspaceChangePacket(current.planId()));
            }
            return true;
        });

        MKButton copy = addBottomButton(screen, root, Component.literal("Copy Summary"), 140, 2);
        copy.setPressedCallback((button, mouseButton) -> {
            MKWorkspaceClientChangePlan current = screen.changePlan();
            if (current != null && current.complete()) {
                Minecraft.getInstance().keyboardHandler.setClipboard(
                        MKWorkspaceChangeSummaryFormatter.clipboard(current.summary()));
            }
            button.buttonText = Component.literal("Copied Summary");
            return true;
        });

        MKButton cancel = addBottomButton(screen, root, Component.literal("Cancel"), 120, 0);
        cancel.setPressedCallback((button, mouseButton) -> {
            MKWorkspaceClientChangePlan current = screen.changePlan();
            if (current != null) {
                PacketDistributor.sendToServer(new CancelWorkspaceChangePacket(current.planId()));
            }
            screen.goBackOrSwitchTo("form");
            return true;
        });
        return root;
    }

    private List<String> preflightSummaryLines(MKWorkspaceScreen screen) {
        ArrayList<String> lines = new ArrayList<>();
        MKWorkspaceClientChangePlan plan = screen.changePlan();
        if (plan == null) {
            lines.add(screen.workspaceChangeFailed() && !screen.workspaceChangeMessage().isBlank() ?
                    screen.workspaceChangeMessage() : "Preparing the complete server impact report...");
            return List.copyOf(lines);
        }
        if (!plan.complete()) {
            lines.add("Loading effects: " + plan.receivedEffects() + " / " + plan.totalEffects());
            return List.copyOf(lines);
        }
        lines.addAll(MKWorkspaceChangeSummaryFormatter.lines(plan.summary()));
        return List.copyOf(lines);
    }
}
