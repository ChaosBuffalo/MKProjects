package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
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
import java.util.Optional;
import java.util.function.Consumer;

public class WorkspaceFormHallwayDetailPage extends WorkspacePageBase {
    public static final String ID = "form_hallway_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        List<MKHallwayFamilyDefinition> hallways = editor.hallwayFamilies();
        int index = editor.selectedHallwayIndex();
        if (index < 0 || index >= hallways.size()) {
            screen.switchToExistingState(WorkspaceFormHallwaysPage.ID);
            return new WorkspaceFormHallwaysPage().build(screen);
        }

        MKHallwayFamilyDefinition hallway = hallways.get(index);
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Hallway: " + hallway.hallwayId()));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Edit one hallway family at a time. Hallways bind to opening profiles and can be allowed on the main path, branch path, or both."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);

        addHallwayFieldRow(screen, content, "Hallway Id", hallway.hallwayId(),
                text -> editor.replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        text.trim().isBlank() ? hallway.hallwayId() : text.trim(), hallway.openingProfileId(),
                        hallway.length(), hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.paletteOverride())));
        addHallwayFieldRow(screen, content, "Opening Profile Id", hallway.openingProfileId(),
                text -> editor.replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), text.trim().isBlank() ? hallway.openingProfileId() : text.trim(),
                        hallway.length(), hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.paletteOverride())));
        addHallwayFieldRow(screen, content, "Length", Integer.toString(hallway.length()),
                text -> editor.replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), parseInt(text, hallway.length()),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.paletteOverride())));
        addHallwayFieldRow(screen, content, "Interior Width", Integer.toString(hallway.interiorWidth()),
                text -> editor.replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        parseInt(text, hallway.interiorWidth()), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.paletteOverride())));
        addHallwayFieldRow(screen, content, "Interior Height", Integer.toString(hallway.interiorHeight()),
                text -> editor.replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), parseInt(text, hallway.interiorHeight()), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.paletteOverride())));
        addHallwayFieldRow(screen, content, "Slope Delta", Integer.toString(hallway.slopeDelta()),
                text -> editor.replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), parseInt(text, hallway.slopeDelta()),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.paletteOverride())));
        screen.addPaletteOverrideRows(content, "Palette Overrides", editor.draftBasePalette(),
                hallway.paletteOverrideOpt(),
                override -> editor.replaceHallwayFamily(index, copyHallwayFamily(hallway, override)));

        addToggleRow(screen, content, "Allow On Main Path", hallway.allowOnMainPath(), () -> {
            editor.replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                    hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                    hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                    !hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.paletteOverride()));
            screen.flagNeedSetup();
        });
        addToggleRow(screen, content, "Allow On Branch Path", hallway.allowOnBranchPath(), () -> {
            editor.replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                    hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                    hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                    hallway.allowOnMainPath(), !hallway.allowOnBranchPath(), hallway.paletteOverride()));
            screen.flagNeedSetup();
        });

        finishScrollContent(screen, scrollView, content);

        MKButton remove = addBottomButton(screen, root, Component.literal("Remove Hallway"), 180, 1);
        remove.setPressedCallback((button, mouseButton) -> {
            editor.removeHallwayFamily(index);
            editor.selectedHallwayIndex(-1);
            screen.switchToExistingState(WorkspaceFormHallwaysPage.ID);
            return true;
        });

        addBackButton(screen, root, WorkspaceFormHallwaysPage.ID);
        return root;
    }

    private void addToggleRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
                              boolean enabled, Runnable onToggle) {
        MKButton button = new MKButton(Component.literal(enabled ? "Enabled" : "Disabled"), 180,
                screen.buttonHeight());
        button.setPressedCallback((pressed, mouseButton) -> {
            onToggle.run();
            return true;
        });
        addRow(screen, root, label, button);
    }

    private void addHallwayFieldRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
                                    String value, Consumer<String> onChange) {
        MKTextFieldWidget field = makeField(screen, label, value);
        field.setTextChangeCallback((widget, text) -> onChange.accept(text));
        addRow(screen, root, label, field);
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

    private MKHallwayFamilyDefinition copyHallwayFamily(MKHallwayFamilyDefinition hallway,
                                                        Optional<MKWorkspacePaletteOverride> paletteOverride) {
        return new MKHallwayFamilyDefinition(
                hallway.hallwayId(),
                hallway.openingProfileId(),
                hallway.length(),
                hallway.interiorWidth(),
                hallway.interiorHeight(),
                hallway.slopeDelta(),
                hallway.allowOnMainPath(),
                hallway.allowOnBranchPath(),
                paletteOverride.orElse(null)
        );
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}


