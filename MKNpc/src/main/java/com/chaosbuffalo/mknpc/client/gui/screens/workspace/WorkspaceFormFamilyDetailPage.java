package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKBranchExitMaskWidget;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFoundationPolicy;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomGeometry;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologySlotMetadata;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public class WorkspaceFormFamilyDetailPage extends WorkspacePageBase {
    public static final String ID = "form_family_detail";
    public static final String EXIT_DETAIL_ID = "form_family_exit_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        editor.ensureInitialized();
        if (editor.selectedFamilyIndex() < 0 ||
                editor.selectedFamilyIndex() >= editor.draft().familyDefinitions.size()) {
            screen.switchToExistingState(WorkspaceFormFamiliesPage.ID);
            return new WorkspaceFormFamiliesPage().build(screen);
        }

        int index = editor.selectedFamilyIndex();
        MKTowerWorkspaceFamilyDefinition family = editor.draft().familyDefinitions.get(index);
        MKWorkspaceTopologySlotMetadata slotMetadata = MKWorkspaceTopologySlotMetadata.fromFamily(family);

        MKLayout root = createPanel(screen);
        addTitle(screen, root, Component.literal("Family: " + family.baseName()));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Edit one family at a time. Left click a side of the room diagram to open that exit editor below the widget. Left click again to close it. Right click toggles that exit on or off."));

        int buttonAreaHeight = (2 * screen.buttonHeight()) + screen.buttonGap() + screen.bottomPadding();
        int scrollTop = screen.scrollTopAfterHeader(root, helpText);
        int scrollHeight = screen.panelY() + screen.panelHeight() - buttonAreaHeight - 12 - scrollTop;
        MKScrollView scrollView = new MKScrollView(screen.panelX() + 10, scrollTop,
                screen.scrollWidth(), scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = createContentStack(screen);

        MKTextFieldWidget baseNameField = makeField(screen, "Base Name", family.baseName());
        baseNameField.setTextChangeCallback((field, text) -> editor.replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                text.trim().isBlank() ? family.baseName() : text.trim(),
                family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits(), family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride())));
        MKTextFieldWidget topologySlotField = makeField(screen, "Topology Slot", family.topologySlotId());
        topologySlotField.setTextChangeCallback((field, text) ->
                editor.replaceFamilyTopologySlotId(index, text.trim().isBlank() ? family.topologySlotId() : text.trim()));
        MKTextFieldWidget verticalGroupField = makeField(screen, "Vertical Access Group",
                family.verticalAccessGroupId());
        verticalGroupField.setTextChangeCallback((field, text) ->
                editor.replaceFamilyVerticalAccessGroupId(index,
                        text.trim().isBlank() ? family.verticalAccessGroupId() : text.trim()));
        MKWorkspaceFoundationPolicy inheritedFoundation = editor.resolveFamilyInheritedFoundation(family);
        MKButton foundationModeButton = new MKButton(Component.literal(formatFamilyFoundationLabel(family, inheritedFoundation)),
                180, 20);
        foundationModeButton.setPressedCallback((button, mouseButton) -> {
            editor.replaceFamilyFoundationPolicyOverride(index,
                    nextFamilyFoundationOverride(family.foundationPolicyOverrideOpt(), inheritedFoundation,
                            isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        MKButton extrusionModeButton = new MKButton(Component.literal(formatFamilyExtrusionMode(family.horizontalExtrusionMode())), 180, 20);
        extrusionModeButton.setPressedCallback((button, mouseButton) -> {
            editor.replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                    family.roomWidth(), family.roomLength(), family.roomHeight(),
                    cycleValue(List.of(MKWorkspaceHorizontalExtrusionMode.values()), family.horizontalExtrusionMode(),
                            isReverseClick(mouseButton)),
                    family.horizontalExits(),
                    family.topVoidMargin(),
                    family.bottomVoidMargin(),
                    family.paletteOverride()));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Base Name")), baseNameField);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Topology Slot")), topologySlotField);
        if (family.supportsVerticalAccess()) {
            addRow(screen, content, screen.makeWhiteText(Component.literal("Vertical Access Group")), verticalGroupField);
        }
        addRow(screen, content, screen.makeWhiteText(Component.literal("Foundation Mode")), foundationModeButton);
        addFoundationBlockPickerRow(screen, content, index, family);
        addFoundationMaskRows(screen, content, index, family);
        addReadOnlyRow(screen, content, "Topology Category",
                formatTopologyLabel(slotMetadata.category().getSerializedName()));
        addReadOnlyRow(screen, content, "Topology Role",
                formatTopologyLabel(slotMetadata.pieceRole().getSerializedName()));
        addRow(screen, content, screen.makeWhiteText(Component.literal("Horizontal Extrusion")), extrusionModeButton);
        addGeometryRows(screen, content, editor, index, family);
        screen.addPaletteOverrideRows(content, "Palette Overrides", editor.resolveFamilyInheritedPalette(family),
                family.paletteOverrideOpt(),
                override -> editor.replaceFamilyDefinition(index, editor.copyFamilyDefinition(family, override)));

        MKText exitLabel = screen.makeWhiteText(Component.literal("Family Exits"));
        content.addWidget(exitLabel);
        content.addConstraintToWidget(MarginConstraint.LEFT, exitLabel);
        MKBranchExitMaskWidget exitWidget = new MKBranchExitMaskWidget(family.horizontalExits())
                .setSelectedDirection(editor.selectedFamilyExitIndex() >= 0 &&
                        editor.selectedFamilyExitIndex() < family.horizontalExits().size() ?
                        family.horizontalExits().get(editor.selectedFamilyExitIndex()).direction() : null)
                .setEditCallback(direction -> {
                    int exitIndex = editor.findFamilyExitIndexByDirection(index, direction);
                    editor.selectedFamilyExitIndex(exitIndex == editor.selectedFamilyExitIndex() ? -1 : exitIndex);
                    screen.refreshPreservingActiveScroll();
                })
                .setToggleCallback(direction -> {
                    int exitIndex = editor.findFamilyExitIndexByDirection(index, direction);
                    if (exitIndex >= 0) {
                        editor.removeFamilyExit(index, exitIndex);
                        if (editor.selectedFamilyExitIndex() == exitIndex) {
                            editor.selectedFamilyExitIndex(-1);
                        } else if (editor.selectedFamilyExitIndex() > exitIndex) {
                            editor.selectedFamilyExitIndex(editor.selectedFamilyExitIndex() - 1);
                        }
                    } else {
                        editor.addFamilyExitAtDirection(index, direction);
                    }
                    screen.refreshPreservingActiveScroll();
                });
        content.addWidget(exitWidget);
        content.addConstraintToWidget(new CenterXConstraint(), exitWidget);
        MKText exitSummary = screen.makeWhiteText(Component.literal("Current exits: " + summarizeFamilyExits(family)));
        exitSummary.setWidth(screen.contentWidth());
        exitSummary.setMultiline(true);
        content.addWidget(exitSummary);
        content.addConstraintToWidget(MarginConstraint.LEFT, exitSummary);
        if (editor.selectedFamilyExitIndex() >= 0 && editor.selectedFamilyExitIndex() < family.horizontalExits().size()) {
            addInlineFamilyExitEditor(screen, content, index, editor.selectedFamilyExitIndex(),
                    family.horizontalExits().get(editor.selectedFamilyExitIndex()));
        }

        finishScrollContent(screen, scrollView, content);

        MKButton remove = addBottomButton(screen, root, Component.literal("Remove Family"), 180, 1);
        remove.setPressedCallback((button, mouseButton) -> {
            editor.removeFamilyDefinition(index);
            editor.selectedFamilyIndex(-1);
            editor.selectedFamilyExitIndex(-1);
            screen.switchToExistingState(WorkspaceFormFamiliesPage.ID);
            return true;
        });

        MKButton back = addBackButton(screen, root, WorkspaceFormFamiliesPage.ID);
        back.setPressedCallback((button, mouseButton) -> {
            editor.selectedFamilyExitIndex(-1);
            screen.switchToExistingState(WorkspaceFormFamiliesPage.ID);
            return true;
        });
        return root;
    }

    private void addInlineFamilyExitEditor(MKWorkspaceScreen screen, MKStackLayoutVertical content, int familyIndex,
                                           int exitIndex, MKWorkspaceFamilyHorizontalExitDefinition exit) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKText header = screen.makeWhiteText(Component.literal("Editing " + formatDirection(exit.direction()) + " exit"));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        if (exit.isVerticalAccess()) {
            return;
        }

        MKButton directionButton = new MKButton(Component.literal(formatDirection(exit.direction())), 180, 20);
        directionButton.setPressedCallback((button, mouseButton) -> {
            MKTowerWorkspaceFamilyDefinition family = editor.draft().familyDefinitions.get(familyIndex);
            Direction nextDirection = cycleCardinalDirection(exit.direction(), isReverseClick(mouseButton));
            editor.replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                    nextDirection,
                    exit.pathKind(),
                    exit.openingProfileId(),
                    exit.connectionMode(),
                    editor.clampSideOffset(family, nextDirection, exit.openingProfileId(), exit.sideOffset()),
                    editor.clampVerticalOffset(family, exit.openingProfileId(), exit.verticalOffset())
            ));
            screen.refreshPreservingActiveScroll();
            return true;
        });
        MKButton pathKindButton = new MKButton(Component.literal(formatTopologyLabel(exit.pathKind().getSerializedName())), 180, 20);
        pathKindButton.setPressedCallback((button, mouseButton) -> {
            MKTowerWorkspaceFamilyDefinition family = editor.draft().familyDefinitions.get(familyIndex);
            MKWorkspaceHorizontalExitPathKind nextPathKind = cycleValue(
                    List.of(MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY, MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                            MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY, MKWorkspaceHorizontalExitPathKind.BRANCH,
                            MKWorkspaceHorizontalExitPathKind.BRANCH_CAP_ENTRY),
                    exit.pathKind(), isReverseClick(mouseButton));
            String nextOpeningProfileId = editor.ensureCompatibleOpeningProfile(nextPathKind, exit.openingProfileId());
            editor.replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                    exit.direction(),
                    nextPathKind,
                    nextOpeningProfileId,
                    exit.connectionMode(),
                    editor.clampSideOffset(family, exit.direction(), nextOpeningProfileId, exit.sideOffset()),
                    editor.clampVerticalOffset(family, nextOpeningProfileId, exit.verticalOffset())
            ));
            screen.refreshPreservingActiveScroll();
            return true;
        });
        MKButton connectionModeButton = new MKButton(Component.literal(formatExitConnectionMode(exit.connectionMode())), 180, 20);
        connectionModeButton.setPressedCallback((button, mouseButton) -> {
            editor.replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                    exit.direction(),
                    exit.pathKind(),
                    exit.openingProfileId(),
                    cycleValue(List.of(MKWorkspaceHorizontalExitConnectionMode.values()), exit.connectionMode(),
                            isReverseClick(mouseButton)),
                    exit.sideOffset(),
                    exit.verticalOffset()
            ));
            screen.refreshPreservingActiveScroll();
            return true;
        });
        MKButton openingProfileButton = new MKButton(Component.literal(exit.openingProfileId()), 180, 20);
        openingProfileButton.setPressedCallback((button, mouseButton) -> {
            MKTowerWorkspaceFamilyDefinition family = editor.draft().familyDefinitions.get(familyIndex);
            String nextOpeningProfileId = editor.nextOpeningProfileId(exit.pathKind(), exit.openingProfileId(),
                    isReverseClick(mouseButton));
            editor.replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                    exit.direction(),
                    exit.pathKind(),
                    nextOpeningProfileId,
                    exit.connectionMode(),
                    editor.clampSideOffset(family, exit.direction(), nextOpeningProfileId, exit.sideOffset()),
                    editor.clampVerticalOffset(family, nextOpeningProfileId, exit.verticalOffset())
            ));
            screen.refreshPreservingActiveScroll();
            return true;
        });
        MKTowerWorkspaceFamilyDefinition family = editor.draft().familyDefinitions.get(familyIndex);
        int sideMin = editor.minSideOffset(family, exit.direction(), exit.openingProfileId());
        int sideMax = editor.maxSideOffset(family, exit.direction(), exit.openingProfileId());
        int verticalMin = 0;
        int verticalMax = editor.maxVerticalOffset(family, exit.openingProfileId());
        MKIntegerSlider sideOffsetSlider = new MKIntegerSlider("Side", 180, 20, sideMin, sideMax,
                clamp(exit.sideOffset(), sideMin, sideMax),
                value -> editor.updateFamilyExitOffsets(familyIndex, exitIndex, value, null));
        MKIntegerSlider verticalOffsetSlider = new MKIntegerSlider("Vertical", 180, 20, verticalMin, verticalMax,
                clamp(exit.verticalOffset(), verticalMin, verticalMax),
                value -> editor.updateFamilyExitOffsets(familyIndex, exitIndex, null, value));
        addRow(screen, content, screen.makeWhiteText(Component.literal("Direction")), directionButton);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Exit Role")), pathKindButton);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Connection")), connectionModeButton);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Opening Profile")), openingProfileButton);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Side Offset")), sideOffsetSlider);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Vertical Offset")), verticalOffsetSlider);
    }

    private void addFoundationBlockPickerRow(MKWorkspaceScreen screen, MKStackLayoutVertical content, int familyIndex,
                                             MKTowerWorkspaceFamilyDefinition family) {
        Optional<MKWorkspaceFoundationPolicy> overrideOpt = family.foundationPolicyOverrideOpt();
        if (overrideOpt.isEmpty() || overrideOpt.get().mode() != MKWorkspaceFoundationMode.UNIFORM_STATE) {
            return;
        }
        MKWorkspaceFoundationPolicy foundationPolicy = overrideOpt.get();
        ResourceLocation blockId = foundationPolicy.foundationBlockOpt()
                .orElse(ResourceLocation.parse("minecraft:stone"));
        MKButton blockButton = new MKButton(screen.blockDisplayName(blockId), 180, 20);
        blockButton.setTooltip(Component.literal(blockId.toString()));
        blockButton.setPressedCallback((button, mouseButton) -> {
            screen.openBlockPicker("Choose Foundation Block", blockId, value -> {
                screen.draftSession().replaceFamilyFoundationPolicy(familyIndex,
                        MKWorkspaceFoundationPolicy.uniformBlock(value));
                screen.refreshPreservingActiveScroll();
            }, false);
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Foundation Block")), blockButton);
    }

    private void addFoundationMaskRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, int familyIndex,
                                       MKTowerWorkspaceFamilyDefinition family) {
        Optional<MKWorkspaceFoundationPolicy> overrideOpt = family.foundationPolicyOverrideOpt();
        if (overrideOpt.isEmpty() || overrideOpt.get().mode() != MKWorkspaceFoundationMode.MASKED_EXTEND_BOTTOM_BLOCKS) {
            return;
        }
        MKWorkspaceFoundationPolicy foundationPolicy = overrideOpt.get();
        List<ResourceLocation> maskBlocks = foundationPolicy.maskBlocks();
        for (int maskIndex = 0; maskIndex < maskBlocks.size(); maskIndex++) {
            ResourceLocation blockId = maskBlocks.get(maskIndex);
            int capturedIndex = maskIndex;
            MKButton blockButton = new MKButton(screen.blockDisplayName(blockId), 180, 20);
            blockButton.setTooltip(Component.literal(blockId + "\nLeft-click to choose. Right-click to remove."));
            blockButton.setPressedCallback((button, mouseButton) -> {
                if (isReverseClick(mouseButton)) {
                    updateFoundationMaskBlock(screen, familyIndex, family, capturedIndex, null);
                } else {
                    screen.openBlockPicker("Choose Foundation Mask Block", blockId, value ->
                            updateFoundationMaskBlock(screen, familyIndex, family, capturedIndex, value), false);
                }
                return true;
            });
            addRow(screen, content, screen.makeWhiteText(Component.literal("Mask Block " + (maskIndex + 1))),
                    blockButton);
        }
        MKButton addButton = new MKButton(Component.literal("Add Block"), 180, 20);
        addButton.setPressedCallback((button, mouseButton) -> {
            ResourceLocation defaultBlock = ResourceLocation.parse("minecraft:stone");
            screen.openBlockPicker("Choose Foundation Mask Block", defaultBlock, value -> {
                List<ResourceLocation> updated = new java.util.ArrayList<>(family.foundationPolicy().maskBlocks());
                if (!updated.contains(value)) {
                    updated.add(value);
                }
                screen.draftSession().replaceFamilyFoundationPolicy(familyIndex,
                        MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(updated));
                screen.refreshPreservingActiveScroll();
            }, false);
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Foundation Mask")), addButton);
    }

    private void updateFoundationMaskBlock(MKWorkspaceScreen screen, int familyIndex,
                                           MKTowerWorkspaceFamilyDefinition family, int maskIndex,
                                           @Nullable ResourceLocation blockId) {
        List<ResourceLocation> updated = new java.util.ArrayList<>(family.foundationPolicy().maskBlocks());
        if (maskIndex < 0 || maskIndex >= updated.size()) {
            return;
        }
        if (blockId == null) {
            updated.remove(maskIndex);
        } else {
            updated.set(maskIndex, blockId);
        }
        screen.draftSession().replaceFamilyFoundationPolicy(familyIndex,
                MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(updated));
        screen.refreshPreservingActiveScroll();
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, MKText label, MKTextFieldWidget field) {
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(field);
        root.addConstraintToWidget(new CenterXConstraint(), field);
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, MKText label, MKButton button) {
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private void addRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, MKText label, MKIntegerSlider slider) {
        label.setWidth(screen.contentWidth());
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(slider);
        root.addConstraintToWidget(new CenterXConstraint(), slider);
    }

    private MKTextFieldWidget makeField(MKWorkspaceScreen screen, String label, String value) {
        MKTextFieldWidget widget = new MKTextFieldWidget(screen.font(), 0, 0, 180, 18, Component.literal(label));
        widget.setText(value);
        return widget;
    }

    private String summarizeFamilyExits(MKTowerWorkspaceFamilyDefinition family) {
        if (family.horizontalExits().isEmpty()) {
            return "none";
        }
        return family.horizontalExits().stream()
                .map(this::describeFamilyExit)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String describeFamilyExit(MKWorkspaceFamilyHorizontalExitDefinition exit) {
        if (exit.isVerticalAccess()) {
            return formatDirection(exit.direction()) + " / " + formatTopologyLabel(exit.pathKind().getSerializedName());
        }
        return formatDirection(exit.direction()) + " / " + formatTopologyLabel(exit.pathKind().getSerializedName()) +
                " / " + formatExitConnectionMode(exit.connectionMode()) + " / " + exit.openingProfileId() +
                " / side " + exit.sideOffset() + " / up " + exit.verticalOffset();
    }

    private String formatExitConnectionMode(MKWorkspaceHorizontalExitConnectionMode connectionMode) {
        return switch (connectionMode) {
            case HALLWAY -> "Hallway";
            case DIRECT_ROOM -> "Direct Room";
            case NO_CONNECTION -> "No Connection";
        };
    }

    private String formatFamilyExtrusionMode(MKWorkspaceHorizontalExtrusionMode mode) {
        return switch (mode) {
            case TUNNEL_ONLY -> "Tunnel Only";
            case FULL_BODY -> "Full Body";
            case NO_EXTRUSION -> "No Extrusion";
        };
    }

    private String formatFoundationMode(MKWorkspaceFoundationMode mode) {
        return formatTopologyLabel(mode.getSerializedName());
    }

    private String formatFamilyFoundationLabel(MKTowerWorkspaceFamilyDefinition family,
                                               MKWorkspaceFoundationPolicy inheritedFoundation) {
        return family.foundationPolicyOverrideOpt()
                .map(policy -> formatFoundationMode(policy.mode()))
                .orElse("Inherit (" + formatFoundationMode(inheritedFoundation.mode()) + ")");
    }

    private Optional<MKWorkspaceFoundationPolicy> nextFamilyFoundationOverride(
            Optional<MKWorkspaceFoundationPolicy> currentOverride,
            MKWorkspaceFoundationPolicy inheritedFoundation,
            boolean reverse) {
        List<MKWorkspaceFoundationMode> modes = List.of(MKWorkspaceFoundationMode.values());
        int currentIndex = currentOverride
                .map(policy -> modes.indexOf(policy.mode()) + 1)
                .orElse(0);
        int optionCount = modes.size() + 1;
        int nextIndex = Math.floorMod(currentIndex + (reverse ? -1 : 1), optionCount);
        if (nextIndex == 0) {
            return Optional.empty();
        }
        MKWorkspaceFoundationMode nextMode = modes.get(nextIndex - 1);
        MKWorkspaceFoundationPolicy currentPolicy = currentOverride.orElse(inheritedFoundation);
        return Optional.of(foundationPolicyForMode(nextMode, currentPolicy));
    }

    private MKWorkspaceFoundationPolicy foundationPolicyForMode(MKWorkspaceFoundationMode mode,
                                                                MKWorkspaceFoundationPolicy current) {
        return switch (mode) {
            case NONE -> MKWorkspaceFoundationPolicy.none();
            case UNIFORM_STATE -> current.foundationBlockOpt()
                    .map(MKWorkspaceFoundationPolicy::uniformBlock)
                    .orElse(MKWorkspaceFoundationPolicy.uniformBlock(ResourceLocation.parse("minecraft:stone")));
            case EXTEND_BOTTOM_BLOCKS -> MKWorkspaceFoundationPolicy.extendBottomBlocks();
            case MASKED_EXTEND_BOTTOM_BLOCKS -> current.maskBlocks().isEmpty() ?
                    MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(List.of(ResourceLocation.parse("minecraft:stone"))) :
                    MKWorkspaceFoundationPolicy.maskedExtendBottomBlocks(current.maskBlocks());
        };
    }

    private void addReadOnlyRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, String label, String value) {
        MKText labelText = screen.makeWhiteText(Component.literal(label));
        labelText.setWidth(screen.contentWidth());
        root.addWidget(labelText);
        root.addConstraintToWidget(MarginConstraint.LEFT, labelText);
        MKText valueText = screen.makeWhiteText(Component.literal(value));
        valueText.setWidth(screen.contentWidth());
        valueText.setMultiline(true);
        root.addWidget(valueText);
        root.addConstraintToWidget(MarginConstraint.LEFT, valueText);
    }

    private void addGeometryRows(MKWorkspaceScreen screen, MKStackLayoutVertical root, WorkspaceDraftSession editor,
                                 int familyIndex, MKTowerWorkspaceFamilyDefinition family) {
        int resolvedWidth = editor.resolvedFamilyRoomWidth(family);
        int resolvedLength = editor.resolvedFamilyRoomLength(family);
        int resolvedHeight = editor.resolvedFamilyRoomHeight(family);
        boolean stackBacked = editor.familyHasTopologyStack(family);
        addReadOnlyRow(screen, root, "Resolved Room",
                resolvedWidth + "x" + resolvedLength + "x" + resolvedHeight +
                        (stackBacked ? " from topology stack" : ""));
        addDimensionRows(screen, root, editor, familyIndex, family, "Room Width", "Width",
                family.roomWidthOverrideOpt(), resolvedWidth, DimensionAxis.WIDTH, stackBacked);
        addDimensionRows(screen, root, editor, familyIndex, family, "Room Length", "Length",
                family.roomLengthOverrideOpt(), resolvedLength, DimensionAxis.LENGTH, stackBacked);
        if (family.supportsVerticalAccess()) {
            addReadOnlyRow(screen, root, "Room Height", resolvedHeight +
                    (stackBacked && family.roomHeightOverrideOpt().isEmpty() ? " from topology stack" : ""));
            return;
        }
        if (stackBacked) {
            addDimensionModeRow(screen, root, editor, familyIndex, family, "Room Height Mode",
                    family.roomHeightOverrideOpt(), resolvedHeight, DimensionAxis.HEIGHT);
        }
        if (!stackBacked || family.roomHeightOverrideOpt().isPresent()) {
            MKTextFieldWidget roomHeightField = makeField(screen, "Room Height", Integer.toString(resolvedHeight));
            roomHeightField.setTextChangeCallback((field, text) ->
                    editor.replaceFamilyDefinition(familyIndex, editor.normalizeFamilyDefinition(
                            copyFamilyWithGeometry(family, family.roomWidth(), family.roomLength(),
                                    parseInt(text, resolvedHeight)))));
            addRow(screen, root, screen.makeWhiteText(Component.literal("Room Height")), roomHeightField);
        }
        addVoidMarginRows(screen, root, editor, familyIndex, family, resolvedHeight);
    }

    private void addDimensionRows(MKWorkspaceScreen screen, MKStackLayoutVertical root, WorkspaceDraftSession editor,
                                  int familyIndex, MKTowerWorkspaceFamilyDefinition family, String rowLabel,
                                  String sliderLabel, OptionalInt overrideValue, int resolvedValue,
                                  DimensionAxis axis, boolean stackBacked) {
        if (stackBacked) {
            addDimensionModeRow(screen, root, editor, familyIndex, family, rowLabel + " Mode",
                    overrideValue, resolvedValue, axis);
        }
        if (!stackBacked || overrideValue.isPresent()) {
            MKIntegerSlider slider = new MKIntegerSlider(sliderLabel, 180, 20, 1, 45, 2, resolvedValue,
                    value -> editor.replaceFamilyDefinition(familyIndex,
                            editor.normalizeFamilyDefinition(copyFamilyWithDimension(family, axis, value))));
            addRow(screen, root, screen.makeWhiteText(Component.literal(rowLabel)), slider);
        }
    }

    private void addDimensionModeRow(MKWorkspaceScreen screen, MKStackLayoutVertical root, WorkspaceDraftSession editor,
                                     int familyIndex, MKTowerWorkspaceFamilyDefinition family, String rowLabel,
                                     OptionalInt overrideValue, int resolvedValue, DimensionAxis axis) {
        MKButton modeButton = new MKButton(Component.literal(formatDimensionMode(overrideValue, resolvedValue)),
                180, 20);
        modeButton.setPressedCallback((button, mouseButton) -> {
            int nextValue = overrideValue.isPresent() ? 0 : resolvedValue;
            editor.replaceFamilyDefinition(familyIndex,
                    editor.normalizeFamilyDefinition(copyFamilyWithDimension(family, axis, nextValue)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, root, screen.makeWhiteText(Component.literal(rowLabel)), modeButton);
    }

    private void addVoidMarginRows(MKWorkspaceScreen screen, MKStackLayoutVertical root, WorkspaceDraftSession editor,
                                   int familyIndex, MKTowerWorkspaceFamilyDefinition family, int resolvedHeight) {
        int minInteriorHeight = MKWorkspaceRoomGeometry.MIN_ROOM_HEIGHT;
        int topMarginMax = Math.max(0, resolvedHeight - family.bottomVoidMargin() - minInteriorHeight);
        MKIntegerSlider topVoidMarginSlider = new MKIntegerSlider("Margin", 180, 20, 0, topMarginMax, 1,
                clamp(family.topVoidMargin(), 0, topMarginMax), value ->
                editor.replaceFamilyDefinition(familyIndex, editor.normalizeFamilyDefinition(new MKTowerWorkspaceFamilyDefinition(
                        family.baseName(), family.category(), family.pieceRole(), false,
                        family.roomWidth(), family.roomLength(), family.roomHeight(),
                        family.horizontalExtrusionMode(), family.horizontalExits(),
                        value, family.bottomVoidMargin(), family.paletteOverride()))));
        int bottomMarginMax = Math.max(0, resolvedHeight - family.topVoidMargin() - minInteriorHeight);
        MKIntegerSlider bottomVoidMarginSlider = new MKIntegerSlider("Margin", 180, 20, 0, bottomMarginMax, 1,
                clamp(family.bottomVoidMargin(), 0, bottomMarginMax), value ->
                editor.replaceFamilyDefinition(familyIndex, editor.normalizeFamilyDefinition(new MKTowerWorkspaceFamilyDefinition(
                        family.baseName(), family.category(), family.pieceRole(), false,
                        family.roomWidth(), family.roomLength(), family.roomHeight(),
                        family.horizontalExtrusionMode(), family.horizontalExits(),
                        family.topVoidMargin(), value, family.paletteOverride()))));
        addRow(screen, root, screen.makeWhiteText(Component.literal("Top Void Margin")), topVoidMarginSlider);
        addRow(screen, root, screen.makeWhiteText(Component.literal("Bottom Void Margin")), bottomVoidMarginSlider);
    }

    private MKTowerWorkspaceFamilyDefinition copyFamilyWithDimension(MKTowerWorkspaceFamilyDefinition family,
                                                                     DimensionAxis axis, int value) {
        return switch (axis) {
            case WIDTH -> copyFamilyWithGeometry(family, value, family.roomLength(), family.roomHeight());
            case LENGTH -> copyFamilyWithGeometry(family, family.roomWidth(), value, family.roomHeight());
            case HEIGHT -> copyFamilyWithGeometry(family, family.roomWidth(), family.roomLength(), value);
        };
    }

    private MKTowerWorkspaceFamilyDefinition copyFamilyWithGeometry(MKTowerWorkspaceFamilyDefinition family,
                                                                    int roomWidth, int roomLength, int roomHeight) {
        return new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                roomWidth, roomLength, roomHeight, family.horizontalExtrusionMode(), family.horizontalExits(),
                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride());
    }

    private String formatDimensionMode(OptionalInt overrideValue, int resolvedValue) {
        return overrideValue.isPresent() ? "Override (" + overrideValue.getAsInt() + ")" :
                "Inherit (" + resolvedValue + ")";
    }

    private enum DimensionAxis {
        WIDTH,
        LENGTH,
        HEIGHT
    }

    private String formatDirection(Direction direction) {
        return formatTopologyLabel(direction.getSerializedName());
    }

    private Direction cycleCardinalDirection(Direction direction, boolean reverse) {
        return cycleValue(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST), direction, reverse);
    }

    private boolean isReverseClick(int mouseButton) {
        return mouseButton == 1;
    }

    private <T> T cycleValue(List<T> values, T current, boolean reverse) {
        if (values.isEmpty()) {
            return current;
        }
        int index = values.indexOf(current);
        if (index < 0) {
            return values.getFirst();
        }
        int nextIndex = Math.floorMod(index + (reverse ? -1 : 1), values.size());
        return values.get(nextIndex);
    }

    private int clamp(int value, int min, int max) {
        int orderedMin = Math.min(min, max);
        int orderedMax = Math.max(min, max);
        return Math.max(orderedMin, Math.min(orderedMax, value));
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private String formatTopologyLabel(String key) {
        return WorkspacePieceDisplay.formatTopologyLabel(key);
    }
}
