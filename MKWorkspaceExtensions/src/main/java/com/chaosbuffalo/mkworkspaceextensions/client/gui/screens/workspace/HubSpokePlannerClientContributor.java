package com.chaosbuffalo.mkworkspaceextensions.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceDraftSession;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerClientContributor;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerDraftAdapter;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerLayout;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlanner;
import com.chaosbuffalo.mkworkspaceextensions.world.gen.workspace.planner.HubSpokePlannerSettings;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceVerticalStackSettings;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class HubSpokePlannerClientContributor implements WorkspacePlannerClientContributor {
    @Override
    public ResourceLocation plannerId() {
        return HubSpokePlanner.PLANNER_ID;
    }

    @Override
    public WorkspacePlannerDraftAdapter createDraftAdapter() {
        return new HubSpokeWorkspaceDraftAdapter();
    }

    @Override
    public void addDefaultsSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                    WorkspaceDraftSession editor) {
        addOverviewText(screen, content, editor);
        addFootprintSliders(screen, content, editor);
        addSpokeTemplateControls(screen, content, editor);
        addCornerModeControls(screen, content, editor);
        addHeightSliders(screen, content, editor);
    }

    @Override
    public void addDefaultsLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                  WorkspaceDraftSession editor) {
        removeObsoleteSyntheticStack(editor);
        addPreview(screen, layout, editor);
        addDefaultsSections(screen, layout.settingsContent(), editor);
    }

    @Override
    public void addWorkspaceOverviewSections(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                             WorkspaceDraftSession editor) {
        editor.ensureInitialized();
        removeObsoleteSyntheticStack(editor);
        addOverviewText(screen, content, editor);
        addFootprintSliders(screen, content, editor);
        addCornerModeControls(screen, content, editor);
        addHeightSliders(screen, content, editor);
    }

    @Override
    public void addWorkspaceOverviewLayout(MKWorkspaceScreen screen, WorkspacePlannerLayout layout,
                                           WorkspaceDraftSession editor) {
        editor.ensureInitialized();
        removeObsoleteSyntheticStack(editor);
        addPreview(screen, layout, editor);
        addOverviewText(screen, layout.settingsContent(), editor);
        addFootprintSliders(screen, layout.settingsContent(), editor);
        addSpokeTemplateControls(screen, layout.settingsContent(), editor);
        addCornerModeControls(screen, layout.settingsContent(), editor);
        addHeightSliders(screen, layout.settingsContent(), editor);
    }

    private void addPreview(MKWorkspaceScreen screen, WorkspacePlannerLayout layout, WorkspaceDraftSession editor) {
        HubSpokeFootprintPreview preview = new HubSpokeFootprintPreview(Math.min(screen.contentWidth(), 260), 190,
                editor.buildWorkspaceDraft());
        layout.previewContent().addWidget(preview);
        layout.previewContent().addConstraintToWidget(new CenterXConstraint(), preview);
    }

    private void addOverviewText(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor) {
        int centerSize = family(editor, HubSpokePlanner.CENTER_SLOT)
                .map(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .orElse(15);
        int spokeWidth = family(editor, HubSpokePlanner.SPOKE_SLOT)
                .map(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .orElse(5);
        int cornerSize = family(editor, HubSpokePlanner.CORNER_SLOT)
                .map(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .orElse(7);
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(editor.draft().topologyProfile);
        addText(screen, content, Component.literal(
                "Hub Spoke Planner\nflat octagonal platform | 1 center + 4 cardinal spokes + 4 corner branches\n" +
                        "authoring templates: center, " + settings.spokeTemplates().size() + " spoke, " +
                        cornerTemplateCount(settings) + " corner | inserts: 0\n" +
                        "corner attachment: " + HubSpokePlanner.cornerAttachmentMode(centerSize, spokeWidth,
                                cornerSize).name().toLowerCase()));
    }

    private void addFootprintSliders(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                     WorkspaceDraftSession editor) {
        int centerSize = odd(family(editor, HubSpokePlanner.CENTER_SLOT)
                .map(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .orElse(15));
        int spokeWidth = clampOdd(family(editor, HubSpokePlanner.SPOKE_SLOT)
                .map(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .orElse(5), 3, centerSize);
        int cornerSize = clampOdd(family(editor, HubSpokePlanner.CORNER_SLOT)
                .map(MKWorkspaceRoomFamilyDefinition::roomWidth)
                .orElse(7), 3, centerSize);

        addSliderRow(screen, content, Component.literal("Center Size"),
                oddSlider("Size", 7, 31, centerSize, value -> {
                    int snapped = odd(value);
                    updateFamily(editor, HubSpokePlanner.CENTER_SLOT,
                            family -> copyFamily(family, snapped, snapped, family.roomHeight()));
                    constrainDependentFootprints(editor, snapped);
                    screen.flagNeedSetup();
                }));
        addSliderRow(screen, content, Component.literal("Spoke Width"),
                oddSlider("Width", 3, centerSize, spokeWidth, value -> {
                    int snapped = clampOdd(value, 3, centerSize);
                    updateFamily(editor, HubSpokePlanner.SPOKE_SLOT,
                            family -> copyFamily(family, snapped, family.roomLength(), family.roomHeight()));
                    screen.flagNeedSetup();
                }));
        addSliderRow(screen, content, Component.literal("Corner Size"),
                oddSlider("Size", 3, centerSize, cornerSize, value -> {
                    int snapped = clampOdd(value, 3, centerSize);
                    updateFamily(editor, HubSpokePlanner.CORNER_SLOT,
                            family -> copyFamily(family, snapped, snapped, family.roomHeight()));
                    screen.flagNeedSetup();
                }));
    }

    private void addSpokeTemplateControls(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                          WorkspaceDraftSession editor) {
        HubSpokePlannerSettings settings = HubSpokePlannerSettings.from(editor.draft().topologyProfile);
        addText(screen, content, Component.literal("Spoke Templates"));
        for (String error : settings.validationErrors()) {
            addText(screen, content, Component.literal(error));
        }
        if (settings.spokeTemplates().size() < HubSpokePlannerSettings.MAX_SPOKE_TEMPLATES) {
            MKButton addButton = new MKButton(Component.literal("Add Spoke Template"), 180, screen.buttonHeight());
            addButton.setPressedCallback((button, mouseButton) -> {
                applySettings(editor, settings.withAddedSpokeTemplate());
                screen.flagNeedSetup();
                return true;
            });
            content.addWidget(addButton);
            content.addConstraintToWidget(new CenterXConstraint(), addButton);
        } else {
            addText(screen, content, Component.literal("Maximum spoke templates: " +
                    HubSpokePlannerSettings.MAX_SPOKE_TEMPLATES));
        }

        for (int index = 0; index < settings.spokeTemplates().size(); index++) {
            HubSpokePlannerSettings.SpokeTemplate template = settings.spokeTemplates().get(index);
            addText(screen, content, Component.literal(template.label() + "  " + directionSummary(template)));
            int templateIndex = index;
            addSliderRow(screen, content, Component.literal("Length"),
                    oddSlider("Length", HubSpokePlannerSettings.MIN_SPOKE_LENGTH,
                            HubSpokePlannerSettings.MAX_SPOKE_LENGTH, template.length(), value -> {
                                HubSpokePlannerSettings updated = currentSettings(editor)
                                        .withSpokeTemplate(templateIndex,
                                                currentSettings(editor).spokeTemplates().get(templateIndex)
                                                        .withLength(value));
                                applySettings(editor, updated);
                                screen.flagNeedSetup();
                            }));
            addSliderRow(screen, content, Component.literal("Height"),
                    new MKIntegerSlider("Height", 180, 20, HubSpokePlanner.MIN_PLATFORM_HEIGHT,
                            HubSpokePlanner.MAX_PLATFORM_HEIGHT, 1, template.height(), value -> {
                                HubSpokePlannerSettings updated = currentSettings(editor)
                                        .withSpokeTemplate(templateIndex,
                                                currentSettings(editor).spokeTemplates().get(templateIndex)
                                                        .withHeight(value));
                                applySettings(editor, updated);
                                screen.flagNeedSetup();
                            }));
            addDirectionMaskWidget(screen, content, editor, template, templateIndex);
            if (settings.spokeTemplates().size() > 1) {
                MKButton removeButton = new MKButton(Component.literal("Remove Spoke Template"), 180,
                        screen.buttonHeight());
                removeButton.setPressedCallback((button, mouseButton) -> {
                    applySettings(editor, currentSettings(editor).withRemovedSpokeTemplate(templateIndex));
                    screen.flagNeedSetup();
                    return true;
                });
                content.addWidget(removeButton);
                content.addConstraintToWidget(new CenterXConstraint(), removeButton);
            }
        }
    }

    private void addCornerModeControls(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                       WorkspaceDraftSession editor) {
        addText(screen, content, Component.literal("Corner Templates"));
        HubSpokePlannerSettings settings = currentSettings(editor);
        MKButton modeButton = new MKButton(Component.literal(cornerModeLabel(settings.cornerTemplateMode())),
                180, screen.buttonHeight());
        modeButton.setPressedCallback((widget, mouseButton) -> {
            HubSpokePlannerSettings.CornerTemplateMode nextMode = nextCornerMode(currentSettings(editor)
                    .cornerTemplateMode());
            applySettings(editor, currentSettings(editor).withCornerTemplateMode(nextMode));
            ensureCornerFamiliesForMode(editor, nextMode);
            screen.flagNeedSetup();
            return true;
        });
        addButtonRow(screen, content, Component.literal("Mode"), modeButton);
    }

    private void addCornerModeRow(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                  WorkspaceDraftSession editor, String label, String topologySlotId) {
        HubSpokePlannerSettings settings = currentSettings(editor);
        MKButton button = new MKButton(Component.literal(settings.uniqueCorner(topologySlotId) ? "Unique" : "Shared"),
                180, screen.buttonHeight());
        button.setPressedCallback((widget, mouseButton) -> {
            HubSpokePlannerSettings current = currentSettings(editor);
            boolean unique = !current.uniqueCorner(topologySlotId);
            applySettings(editor, current.withCornerMode(topologySlotId, unique));
            if (unique) {
                ensureCornerFamily(editor, topologySlotId);
            }
            screen.flagNeedSetup();
            return true;
        });
        addButtonRow(screen, content, Component.literal(label), button);
    }

    private void addDirectionMaskWidget(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                        WorkspaceDraftSession editor,
                                        HubSpokePlannerSettings.SpokeTemplate template, int templateIndex) {
        HubSpokeDirectionMaskWidget widget = new HubSpokeDirectionMaskWidget(74, template.validDirections(),
                direction -> {
                    HubSpokePlannerSettings current = currentSettings(editor);
                    HubSpokePlannerSettings.SpokeTemplate currentTemplate = current.spokeTemplates().get(templateIndex);
                    applySettings(editor, current.withSpokeTemplate(templateIndex,
                            currentTemplate.withDirection(direction,
                                    !currentTemplate.validDirections().contains(direction))));
                    screen.flagNeedSetup();
                });
        content.addWidget(widget);
        content.addConstraintToWidget(new CenterXConstraint(), widget);
    }

    private void addHeightSliders(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor) {
        addPieceHeightSlider(screen, content, editor, "Center Height", HubSpokePlanner.CENTER_SLOT);
        addPieceHeightSlider(screen, content, editor, "Corner Height", HubSpokePlanner.CORNER_SLOT);
    }

    private void addPieceHeightSlider(MKWorkspaceScreen screen, MKStackLayoutVertical content, WorkspaceDraftSession editor,
                                      String label, String topologySlotId) {
        int height = family(editor, topologySlotId)
                .map(MKWorkspaceRoomFamilyDefinition::roomHeight)
                .orElse(HubSpokePlanner.MIN_PLATFORM_HEIGHT);
        MKIntegerSlider heightSlider = new MKIntegerSlider("Height", 180, 20, HubSpokePlanner.MIN_PLATFORM_HEIGHT,
                HubSpokePlanner.MAX_PLATFORM_HEIGHT, 1, height, value -> {
            updateFamily(editor, topologySlotId,
                    family -> copyFamily(family, family.roomWidth(), family.roomLength(), value));
            removeObsoleteSyntheticStack(editor);
            screen.flagNeedSetup();
        });
        addSliderRow(screen, content, Component.literal(label), heightSlider);
    }

    private MKIntegerSlider oddSlider(String label, int min, int max, int current,
                                      java.util.function.IntConsumer callback) {
        java.util.List<Integer> values = new java.util.ArrayList<>();
        for (int value = odd(min); value <= max; value += 2) {
            values.add(value);
        }
        return new MKIntegerSlider(label, 180, 20, values, clampOdd(current, min, max), callback::accept);
    }

    private Optional<MKWorkspaceRoomFamilyDefinition> family(WorkspaceDraftSession editor, String topologySlotId) {
        return editor.familyDefinitions().stream()
                .filter(family -> family.topologySlotId().equals(topologySlotId))
                .findFirst();
    }

    private void updateFamily(WorkspaceDraftSession editor, String topologySlotId,
                              UnaryOperator<MKWorkspaceRoomFamilyDefinition> updater) {
        editor.draft().familyDefinitions = editor.draft().familyDefinitions.stream()
                .map(family -> family.topologySlotId().equals(topologySlotId) ? updater.apply(family) : family)
                .toList();
        editor.markDirty();
    }

    private void ensureCornerFamily(WorkspaceDraftSession editor, String topologySlotId) {
        if (family(editor, topologySlotId).isPresent()) {
            return;
        }
        MKWorkspaceRoomFamilyDefinition shared = family(editor, HubSpokePlanner.CORNER_SLOT)
                .orElseGet(() -> HubSpokePlanner.defaultRoomFamilyDefinitions(MKWorkspaceDimensions.defaultDimensions()).stream()
                        .filter(family -> HubSpokePlanner.CORNER_SLOT.equals(family.topologySlotId()))
                        .findFirst()
                        .orElseThrow());
        MKWorkspaceRoomFamilyDefinition unique = MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                topologySlotId.replace('.', '_'),
                MKWorkspaceTopologySlotMetadata.explicit(topologySlotId, "corner", "room", true),
                shared.verticalAccessGroupId(),
                shared.supportsVerticalAccess(),
                shared.roomWidth(),
                shared.roomLength(),
                shared.roomHeight(),
                shared.horizontalExtrusionMode(),
                shared.horizontalExits(),
                shared.topVoidMargin(),
                shared.bottomVoidMargin(),
                shared.foundationPolicyOverride(),
                shared.paletteOverride()
        );
        java.util.ArrayList<MKWorkspaceRoomFamilyDefinition> families =
                new java.util.ArrayList<>(editor.draft().familyDefinitions);
        families.add(unique);
        editor.draft().familyDefinitions = List.copyOf(families);
        editor.markDirty();
    }

    private void ensureCornerFamiliesForMode(WorkspaceDraftSession editor,
                                             HubSpokePlannerSettings.CornerTemplateMode mode) {
        if (mode == HubSpokePlannerSettings.CornerTemplateMode.PAIRED) {
            ensureCornerFamily(editor, "hub_spoke.corner.north_west");
            ensureCornerFamily(editor, "hub_spoke.corner.north_east");
        } else if (mode == HubSpokePlannerSettings.CornerTemplateMode.UNIQUE) {
            for (String slot : HubSpokePlanner.concreteCornerSlots()) {
                ensureCornerFamily(editor, slot);
            }
        }
    }

    private void constrainDependentFootprints(WorkspaceDraftSession editor, int centerSize) {
        updateFamily(editor, HubSpokePlanner.SPOKE_SLOT,
                family -> copyFamily(family, Math.min(family.roomWidth(), centerSize),
                        family.roomLength(), family.roomHeight()));
        updateFamily(editor, HubSpokePlanner.CORNER_SLOT,
                family -> {
                    int size = Math.min(Math.max(family.roomWidth(), family.roomLength()), centerSize);
                    size = clampOdd(size, 3, centerSize);
                    return copyFamily(family, size, size, family.roomHeight());
                });
    }

    private HubSpokePlannerSettings currentSettings(WorkspaceDraftSession editor) {
        return HubSpokePlannerSettings.from(editor.draft().topologyProfile);
    }

    private void applySettings(WorkspaceDraftSession editor, HubSpokePlannerSettings settings) {
        editor.draft().topologyProfile = settings.applyTo(editor.draft().topologyProfile);
        removeObsoleteSyntheticStack(editor);
        editor.markDirty();
    }

    private void removeObsoleteSyntheticStack(WorkspaceDraftSession editor) {
        if (editor.draft().topologyProfile.verticalStackSettings(HubSpokePlanner.PRIMARY_DIMENSION_STACK_ID).isEmpty()) {
            return;
        }
        editor.draft().topologyProfile = editor.draft().topologyProfile.withoutPlannerSettingsEntry(
                MKWorkspaceVerticalStackSettings.PLANNER_ID,
                HubSpokePlanner.PRIMARY_DIMENSION_STACK_ID);
        editor.markDirty();
    }

    private MKWorkspaceRoomFamilyDefinition copyFamily(MKWorkspaceRoomFamilyDefinition family, int width, int length,
                                                       int height) {
        return MKWorkspaceRoomFamilyDefinition.forTopologySlot(
                family.baseName(),
                family.slotMetadata(),
                family.verticalAccessGroupId(),
                family.supportsVerticalAccess(),
                odd(width),
                odd(length),
                Math.max(HubSpokePlanner.MIN_PLATFORM_HEIGHT, Math.min(HubSpokePlanner.MAX_PLATFORM_HEIGHT, height)),
                family.horizontalExtrusionMode(),
                family.horizontalExits(),
                family.topVoidMargin(),
                family.bottomVoidMargin(),
                family.foundationPolicyOverride(),
                family.paletteOverride()
        );
    }

    private int odd(int value) {
        int normalized = Math.max(1, value);
        return normalized % 2 == 0 ? normalized + 1 : normalized;
    }

    private int clampOdd(int value, int min, int max) {
        int clamped = Math.max(min, Math.min(value, max));
        if (clamped % 2 == 0) {
            clamped = clamped == max ? clamped - 1 : clamped + 1;
        }
        return Math.max(min, Math.min(clamped, max));
    }

    private void addText(MKWorkspaceScreen screen, MKStackLayoutVertical content, Component component) {
        MKText text = screen.makeWhiteText(component);
        text.setWidth(screen.contentWidth());
        text.setMultiline(true);
        content.addWidget(text);
        content.addConstraintToWidget(MarginConstraint.LEFT, text);
    }

    private void addSliderRow(MKWorkspaceScreen screen, MKStackLayoutVertical content, Component label,
                              MKIntegerSlider slider) {
        MKText text = screen.makeWhiteText(label);
        text.setWidth(screen.contentWidth());
        content.addWidget(text);
        content.addConstraintToWidget(MarginConstraint.LEFT, text);
        content.addWidget(slider);
        content.addConstraintToWidget(new CenterXConstraint(), slider);
    }

    private void addButtonRow(MKWorkspaceScreen screen, MKStackLayoutVertical content, Component label,
                              MKButton button) {
        MKText text = screen.makeWhiteText(label);
        text.setWidth(screen.contentWidth());
        content.addWidget(text);
        content.addConstraintToWidget(MarginConstraint.LEFT, text);
        content.addWidget(button);
        content.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private int cornerTemplateCount(HubSpokePlannerSettings settings) {
        if (settings.cornerTemplateMode() == HubSpokePlannerSettings.CornerTemplateMode.PAIRED) {
            return 2;
        }
        if (settings.cornerTemplateMode() == HubSpokePlannerSettings.CornerTemplateMode.UNIQUE) {
            return 4;
        }
        if (settings.cornerTemplateMode() == HubSpokePlannerSettings.CornerTemplateMode.SHARED) {
            return 1;
        }
        int count = 0;
        if (settings.anySharedCorner()) {
            count++;
        }
        for (String slot : HubSpokePlanner.concreteCornerSlots()) {
            if (settings.uniqueCorner(slot)) {
                count++;
            }
        }
        return count;
    }

    private HubSpokePlannerSettings.CornerTemplateMode nextCornerMode(
            HubSpokePlannerSettings.CornerTemplateMode mode) {
        return switch (mode) {
            case SHARED -> HubSpokePlannerSettings.CornerTemplateMode.PAIRED;
            case PAIRED -> HubSpokePlannerSettings.CornerTemplateMode.UNIQUE;
            case UNIQUE, CUSTOM -> HubSpokePlannerSettings.CornerTemplateMode.SHARED;
        };
    }

    private String cornerModeLabel(HubSpokePlannerSettings.CornerTemplateMode mode) {
        return switch (mode) {
            case SHARED -> "Shared";
            case PAIRED -> "Paired";
            case UNIQUE -> "Unique";
            case CUSTOM -> "Custom";
        };
    }

    private String directionSummary(HubSpokePlannerSettings.SpokeTemplate template) {
        StringBuilder builder = new StringBuilder("[");
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            if (template.validDirections().contains(direction)) {
                if (builder.length() > 1) {
                    builder.append(' ');
                }
                builder.append(directionLabel(direction));
            }
        }
        if (builder.length() == 1) {
            builder.append("none");
        }
        return builder.append(']').toString();
    }

    private String directionLabel(Direction direction) {
        return direction.getSerializedName().substring(0, 1).toUpperCase();
    }
}
