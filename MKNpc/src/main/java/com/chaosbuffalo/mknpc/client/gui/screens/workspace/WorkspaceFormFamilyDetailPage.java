package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKBranchExitMaskWidget;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
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

import java.util.List;

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
            screen.switchToExistingState(WorkspaceFormFamilyCategoryPage.ID);
            return new WorkspaceFormFamilyCategoryPage().build(screen);
        }

        int index = editor.selectedFamilyIndex();
        MKTowerWorkspaceFamilyDefinition family = editor.draft().familyDefinitions.get(index);

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
        MKButton categoryButton = new MKButton(Component.literal(formatTopologyLabel(family.category().getSerializedName())), 180, 20);
        categoryButton.setPressedCallback((button, mouseButton) -> {
            MKTowerWorkspaceCategory nextCategory = cycleCategory(family.category(), isReverseClick(mouseButton));
            MKTowerWorkspaceCategoryProfile nextProfile = editor.getCategoryProfile(nextCategory);
            editor.replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), nextCategory, family.pieceRole(),
                    family.supportsVerticalAccess(),
                    editor.normalizeFamilyWidthForCategory(family.roomWidth(), family.supportsVerticalAccess(), nextProfile),
                    editor.normalizeFamilyLengthForCategory(family.roomLength(), family.supportsVerticalAccess(), nextProfile),
                    editor.normalizeFamilyHeightForCategory(family.roomHeight(), family.supportsVerticalAccess(), nextProfile),
                    family.horizontalExtrusionMode(),
                    family.horizontalExits(),
                    family.topVoidMargin(),
                    family.bottomVoidMargin(),
                    family.paletteOverride()));
            editor.selectedFamilyCategory(nextCategory);
            screen.flagNeedSetup();
            return true;
        });
        MKButton roleButton = new MKButton(Component.literal(formatTopologyLabel(family.pieceRole().getSerializedName())), 180, 20);
        roleButton.setPressedCallback((button, mouseButton) -> {
            editor.replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), family.category(), cycleFamilyRole(family.pieceRole(), isReverseClick(mouseButton)),
                    family.supportsVerticalAccess(),
                    family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                    family.horizontalExits(),
                    family.topVoidMargin(),
                    family.bottomVoidMargin(),
                    family.paletteOverride()));
            screen.flagNeedSetup();
            return true;
        });
        MKButton supportsVerticalButton = new MKButton(Component.literal(family.supportsVerticalAccess() ? "Enabled" : "Disabled"), 180, 20);
        supportsVerticalButton.setPressedCallback((button, mouseButton) -> {
            MKTowerWorkspaceCategoryProfile categoryProfile = editor.getCategoryProfile(family.category());
            boolean supportsVerticalAccess = !family.supportsVerticalAccess();
            editor.replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), family.category(), family.pieceRole(),
                    supportsVerticalAccess,
                    editor.normalizeFamilyWidthForCategory(family.roomWidth(), supportsVerticalAccess, categoryProfile),
                    editor.normalizeFamilyLengthForCategory(family.roomLength(), supportsVerticalAccess, categoryProfile),
                    editor.normalizeFamilyHeightForCategory(family.roomHeight(), supportsVerticalAccess, categoryProfile),
                    family.horizontalExtrusionMode(),
                    family.horizontalExits(),
                    supportsVerticalAccess ? 0 : family.topVoidMargin(),
                    supportsVerticalAccess ? 0 : family.bottomVoidMargin(),
                    family.paletteOverride()));
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
        MKIntegerSlider roomWidthSlider = new MKIntegerSlider("Width", 180, 20, 1, 45, 2, family.roomWidth(),
                value -> editor.replaceFamilyDefinition(index, editor.normalizeFamilyDefinition(
                        new MKTowerWorkspaceFamilyDefinition(
                                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                                value, family.roomLength(), family.roomHeight(),
                                family.horizontalExtrusionMode(), family.horizontalExits(),
                                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()))));
        MKIntegerSlider roomLengthSlider = new MKIntegerSlider("Length", 180, 20, 1, 45, 2, family.roomLength(),
                value -> editor.replaceFamilyDefinition(index, editor.normalizeFamilyDefinition(
                        new MKTowerWorkspaceFamilyDefinition(
                                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                                family.roomWidth(), value, family.roomHeight(),
                                family.horizontalExtrusionMode(), family.horizontalExits(),
                                family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()))));

        addRow(screen, content, screen.makeWhiteText(Component.literal("Base Name")), baseNameField);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Category")), categoryButton);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Role")), roleButton);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Vertical Access")), supportsVerticalButton);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Horizontal Extrusion")), extrusionModeButton);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Room Width")), roomWidthSlider);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Room Length")), roomLengthSlider);
        if (family.supportsVerticalAccess()) {
            MKText heightSummary = screen.makeWhiteText(Component.literal(
                    "Room Height: " + family.roomHeight() + " (matches " +
                            formatTopologyLabel(family.category().getSerializedName()) + " full height)"));
            heightSummary.setWidth(screen.contentWidth());
            heightSummary.setMultiline(true);
            content.addWidget(heightSummary);
            content.addConstraintToWidget(MarginConstraint.LEFT, heightSummary);
        } else {
            MKTextFieldWidget roomHeightField = makeField(screen, "Room Height", Integer.toString(family.roomHeight()));
            roomHeightField.setTextChangeCallback((field, text) -> editor.replaceFamilyDefinition(index, editor.normalizeFamilyDefinition(
                    new MKTowerWorkspaceFamilyDefinition(
                            family.baseName(), family.category(), family.pieceRole(), false,
                            family.roomWidth(), family.roomLength(), parseInt(text, family.roomHeight()),
                            family.horizontalExtrusionMode(), family.horizontalExits(),
                            family.topVoidMargin(), family.bottomVoidMargin(), family.paletteOverride()))));
            addRow(screen, content, screen.makeWhiteText(Component.literal("Room Height")), roomHeightField);
            int minInteriorHeight = MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT;
            int topMarginMax = Math.max(0, family.roomHeight() - family.bottomVoidMargin() - minInteriorHeight);
            MKIntegerSlider topVoidMarginSlider = new MKIntegerSlider("Margin", 180, 20, 0, topMarginMax, 1,
                    clamp(family.topVoidMargin(), 0, topMarginMax), value ->
                    editor.replaceFamilyDefinition(index, editor.normalizeFamilyDefinition(new MKTowerWorkspaceFamilyDefinition(
                            family.baseName(), family.category(), family.pieceRole(), false,
                            family.roomWidth(), family.roomLength(), family.roomHeight(),
                            family.horizontalExtrusionMode(), family.horizontalExits(),
                            value, family.bottomVoidMargin(), family.paletteOverride()))));
            int bottomMarginMax = Math.max(0, family.roomHeight() - family.topVoidMargin() - minInteriorHeight);
            MKIntegerSlider bottomVoidMarginSlider = new MKIntegerSlider("Margin", 180, 20, 0, bottomMarginMax, 1,
                    clamp(family.bottomVoidMargin(), 0, bottomMarginMax), value ->
                    editor.replaceFamilyDefinition(index, editor.normalizeFamilyDefinition(new MKTowerWorkspaceFamilyDefinition(
                            family.baseName(), family.category(), family.pieceRole(), false,
                            family.roomWidth(), family.roomLength(), family.roomHeight(),
                            family.horizontalExtrusionMode(), family.horizontalExits(),
                            family.topVoidMargin(), value, family.paletteOverride()))));
            addRow(screen, content, screen.makeWhiteText(Component.literal("Top Void Margin")), topVoidMarginSlider);
            addRow(screen, content, screen.makeWhiteText(Component.literal("Bottom Void Margin")), bottomVoidMarginSlider);
        }
        screen.addPaletteOverrideRows(content, "Palette Overrides", editor.resolveCategoryPalette(family.category()),
                family.paletteOverrideOpt(),
                override -> editor.replaceFamilyDefinition(index, editor.copyFamilyDefinition(family, override)));

        MKText exitLabel = screen.makeWhiteText(Component.literal("Horizontal Exits"));
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
            screen.switchToExistingState(WorkspaceFormFamilyCategoryPage.ID);
            return true;
        });

        MKButton back = addBackButton(screen, root, WorkspaceFormFamilyCategoryPage.ID);
        back.setPressedCallback((button, mouseButton) -> {
            editor.selectedFamilyExitIndex(-1);
            screen.switchToExistingState(WorkspaceFormFamilyCategoryPage.ID);
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
                    List.of(MKWorkspaceHorizontalExitPathKind.values()), exit.pathKind(), isReverseClick(mouseButton));
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

    private String formatDirection(Direction direction) {
        return formatTopologyLabel(direction.getSerializedName());
    }

    private Direction cycleCardinalDirection(Direction direction, boolean reverse) {
        return cycleValue(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST), direction, reverse);
    }

    private MKTowerWorkspaceCategory cycleCategory(MKTowerWorkspaceCategory current, boolean reverse) {
        return cycleValue(List.of(MKTowerWorkspaceCategory.values()), current, reverse);
    }

    private MKWorkspacePieceRole cycleFamilyRole(MKWorkspacePieceRole current, boolean reverse) {
        List<MKWorkspacePieceRole> roles = List.of(
                MKWorkspacePieceRole.ENTRY,
                MKWorkspacePieceRole.FLOOR_MAIN,
                MKWorkspacePieceRole.TOP_CAP_APPROACH,
                MKWorkspacePieceRole.TOP_CAP,
                MKWorkspacePieceRole.BASEMENT_ENTRY,
                MKWorkspacePieceRole.BASEMENT_MAIN,
                MKWorkspacePieceRole.BASEMENT_CAP_APPROACH,
                MKWorkspacePieceRole.BASEMENT_CAP
        );
        return cycleValue(roles, current, reverse);
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
