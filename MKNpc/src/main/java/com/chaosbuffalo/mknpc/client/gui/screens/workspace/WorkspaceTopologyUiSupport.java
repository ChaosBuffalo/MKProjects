package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.List;

final class WorkspaceTopologyUiSupport {
    private WorkspaceTopologyUiSupport() {
    }

    static void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, MKText label, MKButton button) {
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    static void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, MKText label, MKIntegerSlider slider) {
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(slider);
        root.addConstraintToWidget(new CenterXConstraint(), slider);
    }

    static void addText(MKWorkspaceScreen screen, MKStackLayoutVertical root, Component component) {
        MKText text = screen.makeWhiteText(component);
        text.setWidth(screen.contentWidth());
        text.setMultiline(true);
        root.addWidget(text);
        root.addConstraintToWidget(MarginConstraint.LEFT, text);
    }

    static void addResetRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label, Runnable resetter) {
        MKButton resetButton = new MKButton(Component.literal("Reset Defaults"), 180, screen.buttonHeight());
        resetButton.setPressedCallback((button, mouseButton) -> {
            resetter.run();
            return true;
        });
        addRow(screen, root, screen.makeWhiteText(Component.literal(label)), resetButton);
    }

    static boolean isReverseClick(int mouseButton) {
        return mouseButton == 1;
    }

    static String enabledLabel(boolean enabled) {
        return enabled ? "Enabled" : "Disabled";
    }

    static String formatTopologyLabel(String key) {
        return WorkspacePieceDisplay.formatTopologyLabel(key);
    }

    static <T> T cycleValue(List<T> values, T current, boolean reverse) {
        if (values.isEmpty()) {
            return current;
        }
        int index = values.indexOf(current);
        if (index < 0) {
            return values.getFirst();
        }
        return values.get(Math.floorMod(index + (reverse ? -1 : 1), values.size()));
    }

    static int cycleInteger(int min, int max, int current, boolean reverse) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        int normalized = Math.max(low, Math.min(current, high));
        int next = normalized + (reverse ? -1 : 1);
        if (next < low) {
            return high;
        }
        if (next > high) {
            return low;
        }
        return next;
    }
}
