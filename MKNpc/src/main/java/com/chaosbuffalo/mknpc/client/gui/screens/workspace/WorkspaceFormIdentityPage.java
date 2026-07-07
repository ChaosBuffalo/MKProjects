package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;

public class WorkspaceFormIdentityPage extends WorkspacePageBase {
    public static final String ID = "form_identity";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        MKLayout root = createPanel(screen);

        addTitle(screen, root, Component.literal("Identity & Bounds"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Configure workspace naming, topology profile, and scaffold/export margins."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);

        WorkspaceDraftSession editor = screen.draftSession();

        MKTextFieldWidget namespaceField = makeField(screen, "Namespace", editor.namespace());
        namespaceField.setTextChangeCallback((field, text) ->
                editor.namespace(text.trim().isBlank() ? "mkdev" : text.trim()));
        MKTextFieldWidget structureNameField = makeField(screen, "Structure Name",
                editor.structureName());
        structureNameField.setTextChangeCallback((field, text) ->
                editor.structureName(text.trim().isBlank() ? "tower_workspace" : text.trim()));
        MKTextFieldWidget shellMarginField = makeField(screen, "Shell Margin",
                Integer.toString(editor.shellMargin()));
        shellMarginField.setTextChangeCallback((field, text) ->
                editor.shellMargin(parseInt(text, editor.shellMargin())));
        MKTextFieldWidget exteriorAirMarginField = makeField(screen, "Exterior Air Margin",
                Integer.toString(editor.exteriorAirMargin()));
        exteriorAirMarginField.setTextChangeCallback((field, text) ->
                editor.exteriorAirMargin(parseInt(text, editor.exteriorAirMargin())));
        MKTextFieldWidget previewMarginField = makeField(screen, "Preview Margin",
                Integer.toString(editor.previewMargin()));
        previewMarginField.setTextChangeCallback((field, text) ->
                editor.previewMargin(parseInt(text, editor.previewMargin())));
        MKButton topologyButton = new MKButton(topologyPlannerLabel(editor.topologyPlannerId()),
                180, screen.buttonHeight());
        topologyButton.setPressedCallback((button, mouseButton) -> {
            WorkspacePlannerClientRegistry.PlannerClientDefinition nextPlanner = nextTopologyPlanner(
                    editor.topologyPlannerId());
            editor.topologyPlannerId(nextPlanner.getPlannerId());
            screen.flagNeedSetup();
            return true;
        });

        addRow(screen, content, "mknpc.workspace.field.namespace", namespaceField);
        addRow(screen, content, "mknpc.workspace.field.structure_name", structureNameField);
        addRow(screen, content, "Topology Profile", topologyButton);
        addRow(screen, content, "mknpc.workspace.field.shell_margin", shellMarginField);
        addRow(screen, content, "mknpc.workspace.field.exterior_air_margin", exteriorAirMarginField);
        addRow(screen, content, "mknpc.workspace.field.preview_margin", previewMarginField);
        addDefaultPaletteControls(screen, content, editor);

        finishScrollContent(screen, scrollView, content);
        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }

    private MKTextFieldWidget makeField(MKWorkspaceScreen screen, String label, String value) {
        MKTextFieldWidget widget = new MKTextFieldWidget(screen.font(), 0, 0, 180, 18,
                Component.literal(label));
        widget.setText(value);
        return widget;
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String translationKey,
                        MKTextFieldWidget field) {
        MKText label = screen.makeWhiteText(Component.translatable(translationKey));
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(field);
        root.addConstraintToWidget(new CenterXConstraint(), field);
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String labelText,
                        MKButton button) {
        MKText label = screen.makeWhiteText(Component.literal(labelText));
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private void addDefaultPaletteControls(MKWorkspaceScreen screen, MKStackLayoutVertical root,
                                           WorkspaceDraftSession editor) {
        MKText header = screen.makeWhiteText(Component.literal("Default Palette"));
        header.setWidth(screen.contentWidth());
        root.addWidget(header);
        root.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKWorkspaceMaterialPalette defaultPalette = MKWorkspaceMaterialPalette.defaultPalette();
        addPaletteRow(screen, root, "Floor", editor.floorBlock(), defaultPalette.floorBlock(), editor::floorBlock);
        addPaletteRow(screen, root, "Wall", editor.wallBlock(), defaultPalette.wallBlock(), editor::wallBlock);
        addPaletteRow(screen, root, "Ceiling", editor.ceilingBlock(), defaultPalette.ceilingBlock(),
                editor::ceilingBlock);
        addPaletteRow(screen, root, "Stair", editor.stairBlock(), defaultPalette.stairBlock(), editor::stairBlock);
        addPaletteRow(screen, root, "Slab", editor.slabBlock(), defaultPalette.slabBlock(), editor::slabBlock);
        addPaletteRow(screen, root, "Ladder", editor.ladderBlock(), defaultPalette.ladderBlock(), editor::ladderBlock);
    }

    private void addPaletteRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label,
                               ResourceLocation blockId, ResourceLocation defaultBlock,
                               Consumer<ResourceLocation> setter) {
        MKButton button = new MKButton(screen.blockDisplayName(blockId), 180, screen.buttonHeight());
        button.setTooltip(Component.literal(blockId + "\nLeft-click to choose. Right-click to reset."));
        addRow(screen, root, label, button);
        button.setPressedCallback((pressedButton, mouseButton) -> {
            if (mouseButton == 1) {
                setter.accept(defaultBlock);
                screen.refreshPreservingActiveScroll();
                return true;
            }
            screen.openBlockPicker("Choose " + label + " Block", blockId, value -> {
                setter.accept(value);
                screen.refreshPreservingActiveScroll();
            }, false);
            return true;
        });
    }

    private Component topologyPlannerLabel(ResourceLocation plannerId) {
        return WorkspacePlannerClientRegistry.plannerClientDefinitions().stream()
                .filter(definition -> definition.getPlannerId().equals(plannerId))
                .findFirst()
                .map(WorkspacePlannerClientRegistry.PlannerClientDefinition::getDisplayName)
                .orElseGet(() -> Component.literal(WorkspacePieceDisplay.formatTopologyLabel(plannerId.getPath())));
    }

    private WorkspacePlannerClientRegistry.PlannerClientDefinition nextTopologyPlanner(ResourceLocation currentPlannerId) {
        List<WorkspacePlannerClientRegistry.PlannerClientDefinition> definitions =
                WorkspacePlannerClientRegistry.plannerClientDefinitions();
        if (definitions.isEmpty()) {
            throw new IllegalStateException("No workspace planner client definitions are registered");
        }
        for (int i = 0; i < definitions.size(); i++) {
            if (definitions.get(i).getPlannerId().equals(currentPlannerId)) {
                return definitions.get((i + 1) % definitions.size());
            }
        }
        return definitions.get(0);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}


