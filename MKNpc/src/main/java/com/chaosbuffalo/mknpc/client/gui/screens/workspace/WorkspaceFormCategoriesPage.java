package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.network.chat.Component;

import java.util.List;

public class WorkspaceFormCategoriesPage extends WorkspacePageBase {
    public static final String ID = "form_categories";
    public static final String DETAIL_ID = "form_category_detail";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public MKLayout build(MKWorkspaceScreen screen) {
        WorkspaceDraftSession editor = screen.draftSession();
        editor.ensureInitialized();
        WorkspaceDraftSession.Draft draft = editor.draft();

        MKLayout root = createPanel(screen);
        if (!MKWorkspaceTopologyProfile.TOWER_PROFILE_TYPE.equals(editor.topologyProfileType())) {
            addTitle(screen, root, Component.literal("Topology Defaults"));
            MKText helpText = addHeaderText(screen, root, Component.literal(
                    "Set the broad sizing and stack defaults for this topology before editing individual room and linear-run families."));
            MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
            MKStackLayoutVertical content = createContentStack(screen);
            MKText topologyText = screen.makeWhiteText(Component.literal(
                    "Active topology: " + formatTopologyLabel(editor.topologyProfileType())));
            topologyText.setWidth(screen.contentWidth());
            topologyText.setMultiline(true);
            content.addWidget(topologyText);
            content.addConstraintToWidget(MarginConstraint.LEFT, topologyText);

            addCornerModeRow(screen, content, "NW Corner", "keep.corner.north_west");
            addCornerModeRow(screen, content, "NE Corner", "keep.corner.north_east");
            addCornerModeRow(screen, content, "SE Corner", "keep.corner.south_east");
            addCornerModeRow(screen, content, "SW Corner", "keep.corner.south_west");

            MKIntegerSlider centerWidthSlider = new MKIntegerSlider("Width", 180, 20, 3, 45, 2,
                    editor.walledKeepCenterWidth(), value -> {
                editor.walledKeepCenterWidth(value);
                screen.flagNeedSetup();
            });
            addRow(screen, content, screen.makeWhiteText(Component.literal("Center Width")), centerWidthSlider);

            MKIntegerSlider centerLengthSlider = new MKIntegerSlider("Length", 180, 20, 3, 45, 2,
                    editor.walledKeepCenterLength(), value -> {
                editor.walledKeepCenterLength(value);
                screen.flagNeedSetup();
            });
            addRow(screen, content, screen.makeWhiteText(Component.literal("Center Length")), centerLengthSlider);

            MKIntegerSlider heightSlider = new MKIntegerSlider("Height", 180, 20,
                    editor.allowedFullHeightsForCategory(MKTowerWorkspaceCategory.MAIN), editor.walledKeepCenterHeight(),
                    value -> {
                        editor.walledKeepCenterHeight(value);
                        screen.flagNeedSetup();
                    });
            addRow(screen, content, screen.makeWhiteText(Component.literal("Center Height")), heightSlider);

            addTowerStackFloorRows(screen, content, "keep.center");

            MKIntegerSlider wallHeightSlider = new MKIntegerSlider("Height", 180, 20, 2,
                    MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1, 1,
                    editor.wallHeight(), value -> {
                editor.wallHeight(value);
                screen.flagNeedSetup();
            });
            addRow(screen, content, screen.makeWhiteText(Component.literal("Wall Height")), wallHeightSlider);

            MKIntegerSlider wallTopVoidSlider = new MKIntegerSlider("Margin", 180, 20, 0,
                    Math.max(0, editor.wallHeight() - 1), 1,
                    editor.wallTopVoidMargin(), value -> {
                editor.wallTopVoidMargin(value);
                screen.flagNeedSetup();
            });
            addRow(screen, content, screen.makeWhiteText(Component.literal("Wall Top Void Margin")), wallTopVoidSlider);

            for (String cornerSlot : editor.activeCornerTopologySlots()) {
                addCornerSizingSection(screen, content, cornerSlot);
            }

            MKText perimeterText = screen.makeWhiteText(Component.literal(
                    "Perimeter sides use one linear-run slot each. Choose wall or parapet by assigning one family to each keep.perimeter slot."));
            perimeterText.setWidth(screen.contentWidth());
            perimeterText.setMultiline(true);
            content.addWidget(perimeterText);
            content.addConstraintToWidget(MarginConstraint.LEFT, perimeterText);

            finishScrollContent(screen, scrollView, content);
            addBackButton(screen, root, WorkspaceFormPage.ID);
            return root;
        }
        addTitle(screen, root, Component.literal("Category Profiles"));
        MKText helpText = addHeaderText(screen, root, Component.literal(
                "Edit vertical access settings and the shaft-driven category bands in one place. Stair shape, shaft size, and stair width determine which full heights are valid for each category."));

        MKScrollView scrollView = addScrollBelowHeader(screen, root, helpText);
        MKStackLayoutVertical content = createContentStack(screen);

        MKButton stairPlacementButton = new MKButton(getStairPlacementComponent(draft.verticalAccessPlacement), 180, 20);
        stairPlacementButton.setPressedCallback((button, mouseButton) -> {
            draft.verticalAccessPlacement = cycleValue(
                    List.of(MKVerticalAccessPlacement.values()), draft.verticalAccessPlacement, isReverseClick(mouseButton));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, makeLabel(screen, "mknpc.workspace.field.stair_placement"), stairPlacementButton);

        MKButton stairModeButton = new MKButton(getStairModeComponent(draft.stairMode), 180, 20);
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            draft.stairMode = cycleStairMode(draft.stairMode, isReverseClick(mouseButton));
            editor.snapDraftVerticalAccess();
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, makeLabel(screen, "mknpc.workspace.field.stair_mode"), stairModeButton);

        MKButton stairRiseTypeButton = new MKButton(getStairRiseTypeComponent(draft.stairRiseType), 180, 20);
        stairRiseTypeButton.setPressedCallback((button, mouseButton) -> {
            draft.stairRiseType = cycleValue(
                    List.of(MKWorkspaceStairRiseType.values()), draft.stairRiseType, isReverseClick(mouseButton));
            editor.snapDraftVerticalAccess();
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Rise Type")), stairRiseTypeButton);

        int[] footprint = editor.verticalAccessFootprint();
        MKIntegerSlider shaftSizeSlider = new MKIntegerSlider("Shaft", 180, 20,
                MKWorkspaceDimensions.getAllowedShaftSizes(footprint[0], footprint[1]), draft.shaftSize, value -> {
            draft.shaftSize = value;
            editor.snapDraftVerticalAccess();
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Shaft Size")), shaftSizeSlider);

        MKButton stairWidthButton = new MKButton(Component.literal(Integer.toString(draft.stairWidth)), 180, 20);
        stairWidthButton.setPressedCallback((button, mouseButton) -> {
            draft.stairWidth = cycleAllowedStairWidth(draft.shaftSize, draft.stairWidth, isReverseClick(mouseButton));
            editor.snapDraftVerticalAccess();
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Width")), stairWidthButton);

        List<Integer> allowedHeights = editor.allowedFullHeightsForCategory(MKTowerWorkspaceCategory.MAIN);
        MKText allowedHeightsText = screen.makeWhiteText(Component.literal(
                "Allowed band heights: " + (allowedHeights.isEmpty() ? "none" : allowedHeights.toString())));
        allowedHeightsText.setWidth(screen.contentWidth());
        allowedHeightsText.setMultiline(true);
        content.addWidget(allowedHeightsText);
        content.addConstraintToWidget(MarginConstraint.LEFT, allowedHeightsText);

        for (MKTowerWorkspaceCategory category : MKTowerWorkspaceCategory.values()) {
            addCategoryHeightRow(screen, content, category);
        }

        MKButton mainFloorsButton = new MKButton(Component.literal(Integer.toString(draft.mainFloors)), 180, 20);
        mainFloorsButton.setPressedCallback((button, mouseButton) -> {
            draft.mainFloors = editor.nextAllowedMainFloorCount(draft.mainFloors, draft.basementFloors,
                    isReverseClick(mouseButton));
            draft.basementFloors = editor.normalizeBasementFloorCount(draft.basementFloors, draft.mainFloors);
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Main Floors")), mainFloorsButton);

        MKButton basementFloorsButton = new MKButton(Component.literal(Integer.toString(draft.basementFloors)), 180, 20);
        basementFloorsButton.setPressedCallback((button, mouseButton) -> {
            draft.basementFloors = editor.nextAllowedBasementFloorCount(draft.basementFloors, draft.mainFloors,
                    isReverseClick(mouseButton));
            draft.mainFloors = editor.normalizeMainFloorCount(draft.mainFloors, draft.basementFloors);
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Basement Floors")), basementFloorsButton);

        MKButton topCapApproachButton = new MKButton(Component.literal(enabledLabel(draft.topCapApproachEnabled)), 180, 20);
        topCapApproachButton.setPressedCallback((button, mouseButton) -> {
            draft.topCapApproachEnabled = !draft.topCapApproachEnabled;
            draft.mainFloors = editor.normalizeMainFloorCount(draft.mainFloors, draft.basementFloors);
            draft.basementFloors = editor.normalizeBasementFloorCount(draft.basementFloors, draft.mainFloors);
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Top Cap Approach")), topCapApproachButton);

        MKButton basementCapApproachButton = new MKButton(Component.literal(enabledLabel(draft.basementCapApproachEnabled)), 180, 20);
        basementCapApproachButton.setPressedCallback((button, mouseButton) -> {
            draft.basementCapApproachEnabled = !draft.basementCapApproachEnabled;
            draft.basementFloors = editor.normalizeBasementFloorCount(draft.basementFloors, draft.mainFloors);
            draft.mainFloors = editor.normalizeMainFloorCount(draft.mainFloors, draft.basementFloors);
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Basement Cap Approach")), basementCapApproachButton);

        MKText allowedFloorsText = screen.makeWhiteText(Component.literal(
                "Allowed floor counts: main " + editor.allowedMainFloorCounts(draft.basementFloors) +
                        "  |  basement " + editor.allowedBasementFloorCounts(draft.mainFloors)));
        allowedFloorsText.setWidth(screen.contentWidth());
        allowedFloorsText.setMultiline(true);
        content.addWidget(allowedFloorsText);
        content.addConstraintToWidget(MarginConstraint.LEFT, allowedFloorsText);

        for (MKTowerWorkspaceCategory category : MKTowerWorkspaceCategory.values()) {
            addCategoryProfileSection(screen, content, category);
        }

        finishScrollContent(screen, scrollView, content);
        addBackButton(screen, root, WorkspaceFormPage.ID);
        return root;
    }

    private void addCategoryProfileSection(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                           MKTowerWorkspaceCategory category) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKTowerWorkspaceCategoryProfile profile = editor.getCategoryProfile(category);
        MKText header = screen.makeWhiteText(Component.literal(formatTopologyLabel(category.getSerializedName())));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKText summary = screen.makeWhiteText(Component.literal(
                profile.roomWidth() + "x" + profile.roomLength() +
                        (showCategoryPathControls(editor, category) ? "  |  path " +
                                profile.minMainPathPieces() + "-" + profile.maxMainPathPieces() : "") +
                        "  |  branch cap " + profile.maxBranchPiecesBeforeCap()));
        summary.setWidth(screen.contentWidth());
        summary.setMultiline(true);
        content.addWidget(summary);
        content.addConstraintToWidget(MarginConstraint.LEFT, summary);

        MKIntegerSlider roomWidthSlider = new MKIntegerSlider("Width", 180, 20, 1, 45, 2, profile.roomWidth(),
                value -> editor.replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                        profile.category(), value, profile.roomLength(), profile.fullHeight(),
                        profile.minMainPathPieces(), profile.maxMainPathPieces(),
                        profile.maxBranchPiecesBeforeCap(), profile.paletteOverride())));
        MKIntegerSlider roomLengthSlider = new MKIntegerSlider("Length", 180, 20, 1, 45, 2, profile.roomLength(),
                value -> editor.replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                        profile.category(), profile.roomWidth(), value, profile.fullHeight(),
                        profile.minMainPathPieces(), profile.maxMainPathPieces(),
                        profile.maxBranchPiecesBeforeCap(), profile.paletteOverride())));

        addRow(screen, content, screen.makeWhiteText(Component.literal("Room Width")), roomWidthSlider);
        addRow(screen, content, screen.makeWhiteText(Component.literal("Room Length")), roomLengthSlider);
        if (showCategoryPathControls(editor, category)) {
            MKIntegerSlider minPathSlider = new MKIntegerSlider("Min", 180, 20, 0, 10, 1,
                    profile.minMainPathPieces(), value -> editor.replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                    profile.category(), profile.roomWidth(), profile.roomLength(), profile.fullHeight(),
                    value, Math.max(value, profile.maxMainPathPieces()), profile.maxBranchPiecesBeforeCap(),
                    profile.paletteOverride())));
            MKIntegerSlider maxPathSlider = new MKIntegerSlider("Max", 180, 20, 0, 10, 1,
                    profile.maxMainPathPieces(), value -> editor.replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                    profile.category(), profile.roomWidth(), profile.roomLength(), profile.fullHeight(),
                    Math.min(profile.minMainPathPieces(), value), value, profile.maxBranchPiecesBeforeCap(),
                    profile.paletteOverride())));
            addRow(screen, content, screen.makeWhiteText(Component.literal("Main Path Min")), minPathSlider);
            addRow(screen, content, screen.makeWhiteText(Component.literal("Main Path Max")), maxPathSlider);
        }
        MKIntegerSlider maxBranchBeforeCapSlider = new MKIntegerSlider("Max", 180, 20, 0,
                MKTowerWorkspaceCategoryProfile.DEFAULT_MAX_BRANCH_PIECES_BEFORE_CAP, 1,
                profile.maxBranchPiecesBeforeCap(), value -> editor.replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                profile.category(), profile.roomWidth(), profile.roomLength(), profile.fullHeight(),
                profile.minMainPathPieces(), profile.maxMainPathPieces(), value, profile.paletteOverride())));
        addRow(screen, content, screen.makeWhiteText(Component.literal("Branch Cap Max")), maxBranchBeforeCapSlider);
        screen.addPaletteOverrideRows(content, "Palette Overrides", editor.draftBasePalette(), profile.paletteOverrideOpt(),
                override -> editor.replaceCategoryProfile(editor.copyCategoryProfile(profile, override)));
    }

    private void addCornerSizingSection(MKWorkspaceScreen screen, MKStackLayoutVertical content, String topologySlotId) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKText header = screen.makeWhiteText(Component.literal(formatTopologyLabel(topologySlotId) + " Settings"));
        header.setWidth(screen.contentWidth());
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKIntegerSlider widthSlider = new MKIntegerSlider("Width", 180, 20, 3, 45, 2,
                editor.cornerTowerWidth(topologySlotId), value -> {
            editor.cornerTowerWidth(topologySlotId, value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Corner Width")), widthSlider);

        MKIntegerSlider lengthSlider = new MKIntegerSlider("Length", 180, 20, 3, 45, 2,
                editor.cornerTowerLength(topologySlotId), value -> {
            editor.cornerTowerLength(topologySlotId, value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Corner Length")), lengthSlider);

        MKIntegerSlider heightSlider = new MKIntegerSlider("Height", 180, 20, 3,
                MKWorkspaceDimensions.MAX_BAND_HEIGHT_EXCLUSIVE - 1, 1,
                editor.cornerTowerHeight(topologySlotId), value -> {
            editor.cornerTowerHeight(topologySlotId, value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Corner Height")), heightSlider);

        addTowerStackFloorRows(screen, content, topologySlotId);
    }

    private void addTowerStackFloorRows(MKWorkspaceScreen screen, MKStackLayoutVertical content, String stackId) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKButton mainFloorsButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackMainFloors(stackId))), 180, 20);
        mainFloorsButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackMainFloors(stackId,
                    editor.nextAllowedTowerStackMainFloorCount(stackId, isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Main Floors")), mainFloorsButton);

        MKButton basementFloorsButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackBasementFloors(stackId))), 180, 20);
        basementFloorsButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackBasementFloors(stackId,
                    editor.nextAllowedTowerStackBasementFloorCount(stackId, isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Basement Floors")), basementFloorsButton);

        MKIntegerSlider shaftSizeSlider = new MKIntegerSlider("Shaft", 180, 20,
                editor.allowedTowerStackShaftSizes(stackId), editor.towerStackShaftSize(stackId), value -> {
            editor.towerStackShaftSize(stackId, value);
            screen.flagNeedSetup();
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Shaft Size")), shaftSizeSlider);

        MKButton stairPlacementButton = new MKButton(
                getStairPlacementComponent(editor.towerStackVerticalAccessPlacement(stackId)), 180, 20);
        stairPlacementButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackVerticalAccessPlacement(stackId, cycleValue(List.of(MKVerticalAccessPlacement.values()),
                    editor.towerStackVerticalAccessPlacement(stackId), isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Placement")), stairPlacementButton);

        MKButton stairModeButton = new MKButton(
                getStairModeComponent(editor.towerStackStairMode(stackId)), 180, 20);
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackStairMode(stackId,
                    cycleStairMode(editor.towerStackStairMode(stackId), isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Mode")), stairModeButton);

        MKButton stairRiseButton = new MKButton(
                getStairRiseTypeComponent(editor.towerStackStairRiseType(stackId)), 180, 20);
        stairRiseButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackStairRiseType(stackId, cycleValue(List.of(MKWorkspaceStairRiseType.values()),
                    editor.towerStackStairRiseType(stackId), isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Rise Type")), stairRiseButton);

        MKButton stairWidthButton = new MKButton(
                Component.literal(Integer.toString(editor.towerStackStairWidth(stackId))), 180, 20);
        stairWidthButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackStairWidth(stackId, cycleAllowedStairWidth(
                    editor.towerStackShaftSize(stackId), editor.towerStackStairWidth(stackId),
                    isReverseClick(mouseButton)));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Stair Width")), stairWidthButton);

        MKButton topCapApproachButton = new MKButton(
                Component.literal(enabledLabel(editor.towerStackTopCapApproachEnabled(stackId))), 180, 20);
        topCapApproachButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackTopCapApproachEnabled(stackId, !editor.towerStackTopCapApproachEnabled(stackId));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Top Cap Approach")), topCapApproachButton);

        MKButton basementCapApproachButton = new MKButton(
                Component.literal(enabledLabel(editor.towerStackBasementCapApproachEnabled(stackId))), 180, 20);
        basementCapApproachButton.setPressedCallback((button, mouseButton) -> {
            editor.towerStackBasementCapApproachEnabled(stackId,
                    !editor.towerStackBasementCapApproachEnabled(stackId));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal("Basement Cap Approach")),
                basementCapApproachButton);
    }

    private void addCornerModeRow(MKWorkspaceScreen screen, MKStackLayoutVertical content, String label,
                                  String topologySlotId) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKButton modeButton = new MKButton(Component.literal(editor.uniqueCornerTower(topologySlotId) ? "Unique" : "Shared"),
                180, 20);
        modeButton.setPressedCallback((button, mouseButton) -> {
            editor.uniqueCornerTower(topologySlotId, !editor.uniqueCornerTower(topologySlotId));
            screen.flagNeedSetup();
            return true;
        });
        addRow(screen, content, screen.makeWhiteText(Component.literal(label)), modeButton);
    }

    private void addCategoryHeightRow(MKWorkspaceScreen screen, MKStackLayoutVertical content,
                                      MKTowerWorkspaceCategory category) {
        WorkspaceDraftSession editor = screen.draftSession();
        MKTowerWorkspaceCategoryProfile profile = editor.getCategoryProfile(category);
        MKIntegerSlider heightSlider = new MKIntegerSlider("Height", 180, 20,
                editor.allowedFullHeightsForCategory(category), profile.fullHeight(), value -> {
            editor.replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                    profile.category(), profile.roomWidth(), profile.roomLength(),
                    value,
                    profile.minMainPathPieces(), profile.maxMainPathPieces(),
                    profile.maxBranchPiecesBeforeCap(),
                    profile.paletteOverride()));
            screen.flagNeedSetup();
        });
        addRow(screen, content,
                screen.makeWhiteText(Component.literal(formatTopologyLabel(category.getSerializedName()) + " Height")),
                heightSlider);
    }

    private boolean showCategoryPathControls(WorkspaceDraftSession editor, MKTowerWorkspaceCategory category) {
        return hasMainPathContinuationFamily(editor, category) || !hasMainPathEndingFamily(editor, category);
    }

    private boolean hasMainPathContinuationFamily(WorkspaceDraftSession editor, MKTowerWorkspaceCategory category) {
        return editor.draft().familyDefinitions.stream()
                .filter(family -> family.category() == category)
                .filter(family -> !family.mainPathEnding())
                .flatMap(family -> family.horizontalExits().stream())
                .anyMatch(exit -> exit.pathKind().usesMainPath() &&
                        exit.pathKind() != MKWorkspaceHorizontalExitPathKind.MAIN_ENDING_ENTRY);
    }

    private boolean hasMainPathEndingFamily(WorkspaceDraftSession editor, MKTowerWorkspaceCategory category) {
        return editor.draft().familyDefinitions.stream()
                .filter(family -> family.category() == category)
                .anyMatch(MKTowerWorkspaceFamilyDefinition::mainPathEnding);
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

    private MKText makeLabel(MKWorkspaceScreen screen, String translationKey) {
        MKText text = screen.makeWhiteText(Component.translatable(translationKey));
        text.setWidth(screen.contentWidth());
        return text;
    }

    private String enabledLabel(boolean enabled) {
        return enabled ? "Enabled" : "Disabled";
    }

    private boolean isReverseClick(int mouseButton) {
        return mouseButton == 1;
    }

    private MKWorkspaceStairMode cycleStairMode(MKWorkspaceStairMode current, boolean reverse) {
        return cycleValue(List.of(
                MKWorkspaceStairMode.AUTO,
                MKWorkspaceStairMode.RUN_PROFILE,
                MKWorkspaceStairMode.LADDER,
                MKWorkspaceStairMode.NONE
        ), current, reverse);
    }

    private int cycleAllowedStairWidth(int hallwayWidth, int currentWidth, boolean reverse) {
        List<Integer> allowedWidths = MKWorkspaceDimensions.getAllowedStairWidths(hallwayWidth);
        int snapped = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(hallwayWidth, currentWidth);
        if (allowedWidths.isEmpty()) {
            return currentWidth;
        }
        int index = allowedWidths.indexOf(snapped);
        if (index < 0) {
            return currentWidth;
        }
        int nextIndex = Math.floorMod(index + (reverse ? -1 : 1), allowedWidths.size());
        return allowedWidths.get(nextIndex);
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

    private String formatTopologyLabel(String key) {
        return WorkspacePieceDisplay.formatTopologyLabel(key);
    }

    private Component getStairPlacementComponent(MKVerticalAccessPlacement placement) {
        return Component.translatable("mknpc.workspace.stair_placement." + placement.getSerializedName());
    }

    private Component getStairModeComponent(MKWorkspaceStairMode mode) {
        return Component.literal(formatTopologyLabel(mode.getSerializedName()));
    }

    private Component getStairRiseTypeComponent(MKWorkspaceStairRiseType riseType) {
        return Component.literal(formatTopologyLabel(riseType.getSerializedName()));
    }
}
