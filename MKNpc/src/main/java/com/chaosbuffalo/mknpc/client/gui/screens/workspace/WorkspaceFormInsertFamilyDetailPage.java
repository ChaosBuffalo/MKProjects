package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
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

public class WorkspaceFormInsertFamilyDetailPage extends WorkspacePageBase {
    public static final String ID = "form_insert_family_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKWorkspaceInsertFamilyDefinition> insertFamilies = editor.insertFamilies();
        int index = editor.selectedInsertFamilyIndex();
        if (index < 0 || index >= insertFamilies.size()) {
            screen.switchToExistingState(WorkspaceFormInsertFamiliesPage.ID);
            return new WorkspaceFormInsertFamiliesPage().build(screen);
        }

        MKWorkspaceInsertFamilyDefinition insertFamily = insertFamilies.get(index);
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Insert: " + insertFamily.familyId()));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Edit one insert family at a time. Width and height include the hallway shell; depth is the authored template length along the generated route."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);

        MKTextFieldWidget idField = makeField(screen, "Family Id", insertFamily.familyId());
        idField.setTextChangeCallback((field, text) -> editor.replaceInsertFamily(index,
                copyInsertFamily(text.trim().isBlank() ? insertFamily.familyId() : text.trim(),
                        insertFamily.kind(), insertFamily.width(), insertFamily.height(), insertFamily.depth())));

        MKButton kindButton = new MKButton(Component.literal(
                WorkspacePieceDisplay.formatTopologyLabel(insertFamily.kind().getSerializedName())),
                180, screen.buttonHeight());
        kindButton.setPressedCallback((button, mouseButton) -> {
            editor.replaceInsertFamily(index, copyInsertFamily(insertFamily.familyId(),
                    cycleKind(insertFamily.kind(), mouseButton == 1), insertFamily.width(), insertFamily.height(),
                    insertFamily.depth()));
            screen.flagNeedSetup();
            return true;
        });

        MKTextFieldWidget widthField = makeField(screen, "Width", Integer.toString(insertFamily.width()));
        widthField.setTextChangeCallback((field, text) -> editor.replaceInsertFamily(index,
                copyInsertFamily(insertFamily.familyId(), insertFamily.kind(),
                        parseInt(text, insertFamily.width()), insertFamily.height(), insertFamily.depth())));

        MKTextFieldWidget heightField = makeField(screen, "Height", Integer.toString(insertFamily.height()));
        heightField.setTextChangeCallback((field, text) -> editor.replaceInsertFamily(index,
                copyInsertFamily(insertFamily.familyId(), insertFamily.kind(),
                        insertFamily.width(), parseInt(text, insertFamily.height()), insertFamily.depth())));

        MKTextFieldWidget depthField = makeField(screen, "Depth", Integer.toString(insertFamily.depth()));
        depthField.setTextChangeCallback((field, text) -> editor.replaceInsertFamily(index,
                copyInsertFamily(insertFamily.familyId(), insertFamily.kind(),
                        insertFamily.width(), insertFamily.height(), parseInt(text, insertFamily.depth()))));

        addRow(screen, content, "Family Id", idField);
        addRow(screen, content, "Kind", kindButton);
        addRow(screen, content, "Width", widthField);
        addRow(screen, content, "Height", heightField);
        addRow(screen, content, "Depth", depthField);

        finishScrollContent(screen, scrollView, content);

        MKButton remove = addBottomButton(screen, root, Component.literal("Remove Insert"), 180, 1);
        remove.setPressedCallback((button, mouseButton) -> {
            editor.removeInsertFamily(index);
            editor.selectedInsertFamilyIndex(-1);
            screen.switchToExistingState(WorkspaceFormInsertFamiliesPage.ID);
            return true;
        });

        addBackButton(screen, root, WorkspaceFormInsertFamiliesPage.ID);
        return root;
    }

    private MKWorkspaceInsertFamilyDefinition copyInsertFamily(String familyId,
                                                               MKWorkspaceInsertFamilyKind kind,
                                                               int width,
                                                               int height,
                                                               int depth) {
        return new MKWorkspaceInsertFamilyDefinition(
                familyId,
                kind,
                Math.max(1, width),
                Math.max(1, height),
                Math.max(1, depth)
        );
    }

    private MKWorkspaceInsertFamilyKind cycleKind(MKWorkspaceInsertFamilyKind current, boolean reverse) {
        List<MKWorkspaceInsertFamilyKind> values = List.of(MKWorkspaceInsertFamilyKind.values());
        int index = values.indexOf(current);
        if (index < 0) {
            return values.getFirst();
        }
        return values.get(Math.floorMod(index + (reverse ? -1 : 1), values.size()));
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
