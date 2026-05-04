package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class WorkspaceFormIdentityPage extends WorkspacePageBase {
    public static final String ID = "form_identity";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(WorkspacePageContext context) {
        MKLayout root = createPanel(context);

        addTitle(context, root, Component.literal("Identity & Bounds"));
        MKText helpText = addHeaderText(context, root, Component.literal(
                "Configure workspace naming and scaffold/export margins. Room geometry now lives entirely in category profiles."));

        MKScrollView scrollView = addScrollBelowHeader(context, root, helpText);
        MKStackLayoutVertical content = createContentStack(context);

        WorkspaceFormDraftEditor editor = context.draftEditor();

        MKTextFieldWidget namespaceField = makeField(context, "Namespace", editor.namespace());
        namespaceField.setTextChangeCallback((field, text) ->
                editor.namespace(text.trim().isBlank() ? "mkdev" : text.trim()));
        MKTextFieldWidget structureNameField = makeField(context, "Structure Name",
                editor.structureName());
        structureNameField.setTextChangeCallback((field, text) ->
                editor.structureName(text.trim().isBlank() ? "tower_workspace" : text.trim()));
        MKTextFieldWidget shellMarginField = makeField(context, "Shell Margin",
                Integer.toString(editor.shellMargin()));
        shellMarginField.setTextChangeCallback((field, text) ->
                editor.shellMargin(parseInt(text, editor.shellMargin())));
        MKTextFieldWidget exteriorAirMarginField = makeField(context, "Exterior Air Margin",
                Integer.toString(editor.exteriorAirMargin()));
        exteriorAirMarginField.setTextChangeCallback((field, text) ->
                editor.exteriorAirMargin(parseInt(text, editor.exteriorAirMargin())));
        MKTextFieldWidget previewMarginField = makeField(context, "Preview Margin",
                Integer.toString(editor.previewMargin()));
        previewMarginField.setTextChangeCallback((field, text) ->
                editor.previewMargin(parseInt(text, editor.previewMargin())));

        addRow(context, content, "mknpc.workspace.field.namespace", namespaceField);
        addRow(context, content, "mknpc.workspace.field.structure_name", structureNameField);
        addRow(context, content, "mknpc.workspace.field.shell_margin", shellMarginField);
        addRow(context, content, "mknpc.workspace.field.exterior_air_margin", exteriorAirMarginField);
        addRow(context, content, "mknpc.workspace.field.preview_margin", previewMarginField);

        finishScrollContent(context, scrollView, content);
        addBackButton(context, root, WorkspaceFormPage.ID);
        return root;
    }

    private MKTextFieldWidget makeField(WorkspacePageContext context, String label, String value) {
        MKTextFieldWidget widget = new MKTextFieldWidget(context.font(), 0, 0, 180, 18,
                Component.literal(label));
        widget.setText(value);
        return widget;
    }

    private void addRow(WorkspacePageContext context, MKStackLayoutVertical root, String translationKey,
                        MKTextFieldWidget field) {
        MKText label = context.makeWhiteText(Component.translatable(translationKey));
        label.setWidth(context.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(field);
        root.addConstraintToWidget(new CenterXConstraint(), field);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
