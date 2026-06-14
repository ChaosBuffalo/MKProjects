package com.chaosbuffalo.mknpc.client.gui.screens;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKBlockingModal;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceBlockSwapPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceBackupPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceHomePage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceImportPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceGenerateConfirmPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormIdentityPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceDraftSession;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceDeleteConfirmPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormFamiliesPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormFamilyDetailPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormLinearRunsPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormLinearRunDetailPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormMaterialsPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFloorPlanPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormOpeningsPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceFormOpeningDetailPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceManagePage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspacePageBase;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspacePieceDisplay;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspacePlannerNodePage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceTopologySlotEditor;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceTopologySlotPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceTopologyDefaultsPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceTemplateGroupsPage;
import com.chaosbuffalo.mknpc.client.gui.screens.workspace.WorkspaceUtilitiesPage;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.pickers.MKCreativeBlockPickerSource;
import com.chaosbuffalo.mkwidgets.client.gui.pickers.MKCreativePickerCategory;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKBlockSlot;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKCreativeGridPicker;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKModal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class MKWorkspaceScreen extends MKScreen {
    private static final int PANEL_WIDTH = 760;
    private static final int PANEL_HEIGHT = 390;
    private static final int TOP_CONTENT_Y = 52;
    private static final int HEADER_SCROLL_GAP = 8;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int BOTTOM_PADDING = 8;
    private static final int CONTENT_WIDTH = 338;
    private static final int TEXT_COLOR = 0xFFFFFF;

    private final net.minecraft.core.BlockPos anchor;
    private final MKStructureWorkspace workspace;
    private final MKWorkspaceMutationPreflight preflight;
    private final List<String> importManifestIds;
    private final List<String> backupManifestFiles;
    private final List<String> initialStates;
    private String selectedTopologyKey;
    private String selectedPlannerStackId;
    private String selectedFloorPlanStackId;
    private String selectedFloorPlanSectionKey;
    private MKWorkspaceStairAuthoringConfig detailStairConfig;
    private BlockPickerRequest blockPickerRequest;
    private MKModal blockPickerModal;
    private String blockPickerCategoryId;
    private String blockPickerQuery = "";
    private ResourceLocation blockSwapSourceBlock;
    private ResourceLocation blockSwapTargetBlock;
    private boolean wasResized;
    private final MKCreativeBlockPickerSource blockPickerSource = new MKCreativeBlockPickerSource();
    private final WorkspaceTopologySlotEditor topologySlotEditor = new WorkspaceTopologySlotEditor(this);
    private final WorkspaceDraftSession draftSession;

    private record ScrollViewState(double offsetX, double offsetY) {
    }

    private record BlockPickerRequest(String title, ResourceLocation currentValue,
                                      Consumer<ResourceLocation> selectionCallback, boolean allowClear) {
    }

    private static class PaletteBlockSlot extends MKBlockSlot {
        private final Runnable onPick;
        private final Runnable onReset;

        private PaletteBlockSlot(ResourceLocation blockId, Runnable onPick, Runnable onReset) {
            this.onPick = onPick;
            this.onReset = onReset;
            setBlock(blockId);
        }

        @Override
        public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                onPick.run();
                return true;
            }
            if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                onReset.run();
                return true;
            }
            return false;
        }
    }

    public MKWorkspaceScreen(net.minecraft.core.BlockPos anchor, MKStructureWorkspace workspace, List<String> importManifestIds) {
        this(anchor, workspace, importManifestIds, List.of());
    }

    public MKWorkspaceScreen(net.minecraft.core.BlockPos anchor, MKStructureWorkspace workspace,
                             List<String> importManifestIds, List<String> backupManifestFiles) {
        this(anchor, workspace, importManifestIds, backupManifestFiles, List.of(), null, null, null, null,
                -1, -1, -1, -1, null, null);
    }

    private MKWorkspaceScreen(net.minecraft.core.BlockPos anchor, MKStructureWorkspace workspace, List<String> importManifestIds,
                              List<String> backupManifestFiles,
                              List<String> initialStates,
                              String selectedTopologyKey,
                              String selectedPlannerStackId,
                              String selectedFloorPlanStackId,
                              String selectedFloorPlanSectionKey,
                              int selectedFamilyIndex,
                              int selectedFamilyExitIndex,
                              int selectedOpeningIndex,
                              int selectedLinearRunIndex,
                              MKWorkspaceStairAuthoringConfig detailStairConfig,
                              MKWorkspaceMutationPreflight preflight) {
        super(Component.literal("Tower Workspace"));
        this.anchor = anchor;
        this.workspace = workspace;
        this.preflight = preflight;
        this.importManifestIds = List.copyOf(importManifestIds);
        this.backupManifestFiles = List.copyOf(backupManifestFiles);
        this.initialStates = List.copyOf(initialStates);
        this.selectedTopologyKey = selectedTopologyKey;
        this.selectedPlannerStackId = selectedPlannerStackId;
        this.selectedFloorPlanStackId = selectedFloorPlanStackId;
        this.selectedFloorPlanSectionKey = selectedFloorPlanSectionKey;
        this.detailStairConfig = detailStairConfig;
        this.draftSession = new WorkspaceDraftSession(this, selectedFamilyIndex,
                selectedFamilyExitIndex, selectedOpeningIndex, selectedLinearRunIndex);
    }

    public MKWorkspaceScreen copyWithWorkspace(MKStructureWorkspace updatedWorkspace, List<String> updatedImportManifestIds) {
        return copyWithWorkspace(updatedWorkspace, updatedImportManifestIds, backupManifestFiles);
    }

    public MKWorkspaceScreen copyWithWorkspace(MKStructureWorkspace updatedWorkspace, List<String> updatedImportManifestIds,
                                               List<String> updatedBackupManifestFiles) {
        return new MKWorkspaceScreen(anchor, updatedWorkspace, updatedImportManifestIds, updatedBackupManifestFiles,
                getInitialStatesForRefresh(updatedWorkspace),
                selectedTopologyKey, selectedPlannerStackId, selectedFloorPlanStackId, selectedFloorPlanSectionKey,
                draftSession.selectedFamilyIndex(),
                draftSession.selectedFamilyExitIndex(), draftSession.selectedOpeningIndex(),
                draftSession.selectedLinearRunIndex(),
                detailStairConfig, preflight);
    }

    public MKWorkspaceScreen copyWithPreflight(MKWorkspaceMutationPreflight updatedPreflight) {
        return new MKWorkspaceScreen(anchor, workspace, importManifestIds, backupManifestFiles,
                getInitialStatesForRefresh(workspace),
                selectedTopologyKey, selectedPlannerStackId, selectedFloorPlanStackId, selectedFloorPlanSectionKey,
                draftSession.selectedFamilyIndex(),
                draftSession.selectedFamilyExitIndex(), draftSession.selectedOpeningIndex(),
                draftSession.selectedLinearRunIndex(),
                detailStairConfig, updatedPreflight);
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWorkspacePage(new WorkspaceHomePage());
        addWorkspacePage(new WorkspaceImportPage());
        addWorkspacePage(new WorkspaceFormPage());
        addWorkspacePage(new WorkspaceGenerateConfirmPage());
        addWorkspacePage(new WorkspaceFormIdentityPage());
        addWorkspacePage(new WorkspaceFormMaterialsPage());
        WorkspaceTopologyDefaultsPage topologyDefaultsPage = new WorkspaceTopologyDefaultsPage();
        addWorkspacePage(topologyDefaultsPage);
        addState(WorkspaceTopologyDefaultsPage.DETAIL_ID, () -> topologyDefaultsPage.build(this));
        addWorkspacePage(new WorkspaceFormFamiliesPage());
        WorkspaceFormFamilyDetailPage familyDetailPage = new WorkspaceFormFamilyDetailPage();
        addWorkspacePage(familyDetailPage);
        addState(WorkspaceFormFamilyDetailPage.EXIT_DETAIL_ID, () -> familyDetailPage.build(this));
        addWorkspacePage(new WorkspaceFormOpeningsPage());
        addWorkspacePage(new WorkspaceFormOpeningDetailPage());
        addWorkspacePage(new WorkspaceFormLinearRunsPage());
        addWorkspacePage(new WorkspaceFormLinearRunDetailPage());
        addWorkspacePage(new WorkspaceManagePage());
        addWorkspacePage(new WorkspaceUtilitiesPage());
        addWorkspacePage(new WorkspaceDeleteConfirmPage());
        addWorkspacePage(new WorkspaceBlockSwapPage());
        addWorkspacePage(new WorkspaceBackupPage());
        addWorkspacePage(new WorkspaceTemplateGroupsPage());
        addWorkspacePage(new WorkspacePlannerNodePage());
        addWorkspacePage(new WorkspaceFloorPlanPage());
        addWorkspacePage(new WorkspaceTopologySlotPage());
        if (NO_STATE.equals(getState())) {
            List<String> statesToPush = initialStates.isEmpty() ? getDefaultInitialStates() : initialStates;
            for (String state : statesToPush) {
                pushState(state);
            }
        }
    }

    private void addWorkspacePage(WorkspacePageBase page) {
        addState(page.id(), () -> page.build(this));
    }

    public boolean hasExistingWorkspacePieces() {
        return workspace != null && !workspace.pieces().isEmpty();
    }

    public Font font() {
        return font;
    }

    public BlockPos anchor() {
        return anchor;
    }

    public MKStructureWorkspace workspace() {
        return workspace;
    }

    public MKWorkspaceMutationPreflight preflight() {
        return preflight;
    }

    public int screenWidth() {
        return width;
    }

    public int screenHeight() {
        return height;
    }

    public int panelWidth() {
        return Math.min(PANEL_WIDTH, Math.max(320, screenWidth() - 24));
    }

    public int panelHeight() {
        return PANEL_HEIGHT;
    }

    public int panelX() {
        return screenWidth() / 2 - panelWidth() / 2;
    }

    public int panelY() {
        return screenHeight() / 2 - panelHeight() / 2;
    }

    public int scrollWidth() {
        return panelWidth() - 20;
    }

    public int contentWidth() {
        return CONTENT_WIDTH;
    }

    public int buttonHeight() {
        return BUTTON_HEIGHT;
    }

    public int buttonGap() {
        return BUTTON_GAP;
    }

    public int bottomPadding() {
        return BOTTOM_PADDING;
    }

    public int topContentY() {
        return TOP_CONTENT_Y;
    }

    public int headerScrollGap() {
        return HEADER_SCROLL_GAP;
    }

    public int textColor() {
        return TEXT_COLOR;
    }

    public List<String> importManifestIds() {
        return importManifestIds;
    }

    public List<String> backupManifestFiles() {
        return backupManifestFiles;
    }

    public ResourceLocation blockSwapSourceBlock() {
        return blockSwapSourceBlock;
    }

    public void setBlockSwapSourceBlock(ResourceLocation value) {
        blockSwapSourceBlock = value;
    }

    public ResourceLocation blockSwapTargetBlock() {
        return blockSwapTargetBlock;
    }

    public void setBlockSwapTargetBlock(ResourceLocation value) {
        blockSwapTargetBlock = value;
    }

    public void closeScreen() {
        onClose();
    }

    public void openWorkspaceTopologySlot(String topologyKey) {
        selectedTopologyKey = topologyKey;
        resetTopologySlotOverrides();
        pushState(WorkspaceTopologySlotPage.ID);
        flagNeedSetup();
    }

    public boolean openWorkspaceTopologySlotForPrefix(String topologyPrefix) {
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry : groupPiecesByTopology().entrySet()) {
            if (entry.getValue().stream().anyMatch(piece -> pieceMatchesTopologyPrefix(piece, topologyPrefix))) {
                openWorkspaceTopologySlot(entry.getKey());
                return true;
            }
        }
        return false;
    }

    private boolean pieceMatchesTopologyPrefix(MKWorkspacePieceDefinition piece, String topologyPrefix) {
        String topologySlotId = piece.tags().get("workspace_topology_slot_id");
        String topologyGroup = piece.tags().get("workspace_topology_group");
        String linearRunFamilyId = piece.tags().get("workspace_linear_run_family_id");
        return matchesPrefix(topologySlotId, topologyPrefix) ||
                matchesPrefix(topologyGroup, topologyPrefix) ||
                matchesPrefix(linearRunFamilyId, topologyPrefix) ||
                matchesPrefix(piece.roleId(), topologyPrefix);
    }

    private boolean matchesPrefix(String value, String prefix) {
        return value != null && (value.equals(prefix) || value.startsWith(prefix + "."));
    }

    public void openWorkspacePlannerNode(String stackId) {
        selectedPlannerStackId = stackId;
        if (stackId != null) {
            draftSession.walledKeepEditor().towerStackTab(stackId);
        }
        pushState(WorkspacePlannerNodePage.ID);
        flagNeedSetup();
    }

    public String selectedPlannerStackId() {
        return selectedPlannerStackId;
    }

    public void openWorkspaceFloorPlanNode(String stackId, String sectionKey) {
        selectedPlannerStackId = stackId;
        if (stackId != null) {
            draftSession.walledKeepEditor().towerStackTab(stackId);
        }
        selectedFloorPlanStackId = stackId;
        selectedFloorPlanSectionKey = sectionKey;
        if (!WorkspacePlannerNodePage.ID.equals(getState())) {
            pushState(WorkspacePlannerNodePage.ID);
        }
        pushState(WorkspaceFloorPlanPage.ID);
        flagNeedSetup();
    }

    public String selectedFloorPlanStackId() {
        return selectedFloorPlanStackId;
    }

    public String selectedFloorPlanSectionKey() {
        return selectedFloorPlanSectionKey;
    }

    public WorkspaceTopologySlotEditor topologySlotEditor() {
        return topologySlotEditor;
    }

    public WorkspaceDraftSession draftSession() {
        return draftSession;
    }

    private WorkspaceDraftSession.Draft draft() {
        return draftSession.draft();
    }

    public String selectedTopologyKey() {
        return selectedTopologyKey;
    }

    public List<MKWorkspacePieceDefinition> selectedTopologySlotPieces() {
        if (selectedTopologyKey == null) {
            return List.of();
        }
        return groupPiecesByTopology().getOrDefault(selectedTopologyKey, List.of());
    }

    public void clearSelectedTopologyKey() {
        selectedTopologyKey = null;
    }

    public void clearSelectedPlannerStackId() {
        selectedPlannerStackId = null;
    }

    public void clearSelectedFloorPlanNode() {
        selectedFloorPlanStackId = null;
        selectedFloorPlanSectionKey = null;
    }

    public int topologySlotShaftWidth() {
        return workspace.dimensions().shaftWidth();
    }

    private MKWorkspaceStairAuthoringConfig topologySlotStairConfig() {
        ensureTopologySlotOverridesInitialized();
        return detailStairConfig;
    }

    public MKWorkspaceStairMode topologySlotStairMode() {
        return topologySlotStairConfig().mode();
    }

    public void topologySlotStairMode(MKWorkspaceStairMode value) {
        MKWorkspaceStairAuthoringConfig config = topologySlotStairConfig();
        detailStairConfig = new MKWorkspaceStairAuthoringConfig(value, config.riseType(), config.stairWidth());
    }

    public MKWorkspaceStairRiseType topologySlotStairRiseType() {
        return topologySlotStairConfig().riseType();
    }

    public void topologySlotStairRiseType(MKWorkspaceStairRiseType value) {
        MKWorkspaceStairAuthoringConfig config = topologySlotStairConfig();
        detailStairConfig = new MKWorkspaceStairAuthoringConfig(config.mode(), value, config.stairWidth());
    }

    public int topologySlotStairWidth() {
        return topologySlotStairConfig().stairWidth();
    }

    public void topologySlotStairWidth(int value) {
        MKWorkspaceStairAuthoringConfig config = topologySlotStairConfig();
        detailStairConfig = new MKWorkspaceStairAuthoringConfig(config.mode(), config.riseType(), value);
    }

    @Override
    public void addRestoreStateCallbacks() {
        List<ScrollViewState> scrollStates = getActiveScrollViewStates();
        String state = popState();
        boolean resetScrollView = wasResized;
        addPostSetupCallback(() -> {
            pushState(state);
            restoreActiveScrollViewStates(scrollStates, resetScrollView);
            wasResized = false;
        });
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        wasResized = true;
    }

    private MKLayout buildCreativeBlockPickerContent(int xPos, int yPos, int pickerWidth, int pickerHeight) {
        int contentWidth = pickerWidth - 42;
        MKLayout root = new MKLayout(xPos, yPos, pickerWidth, pickerHeight);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        if (blockPickerRequest == null) {
            MKText title = makeWhiteText(Component.literal("Choose Block"));
            root.addWidget(title);
            root.addConstraintToWidget(MarginConstraint.TOP, title);
            root.addConstraintToWidget(new CenterXConstraint(), title);

            MKText message = makeWhiteText(Component.literal("No block picker request is active."));
            message.setWidth(contentWidth);
            message.setY(yPos + 64);
            root.addWidget(message);
            root.addConstraintToWidget(new CenterXConstraint(), message);

            MKButton back = new MKButton(Component.literal("Back"), 120, BUTTON_HEIGHT);
            back.setY(yPos + pickerHeight - BOTTOM_PADDING - BUTTON_HEIGHT);
            root.addWidget(back);
            root.addConstraintToWidget(new CenterXConstraint(), back);
            back.setPressedCallback((button, mouseButton) -> {
                closeBlockPicker();
                return true;
            });
            return root;
        }

        MKText title = makeWhiteText(Component.literal(blockPickerRequest.title()));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        List<MKCreativePickerCategory> categories = blockPickerSource.categories(minecraft);
        MKCreativePickerCategory selectedCategory = selectedBlockPickerCategory(categories);

        MKTextFieldWidget searchField = makeField("Search", blockPickerQuery);
        searchField.setWidth(contentWidth);
        searchField.setY(yPos + 34);
        root.addWidget(searchField);
        root.addConstraintToWidget(new CenterXConstraint(), searchField);

        int footerY = yPos + pickerHeight - BOTTOM_PADDING - BUTTON_HEIGHT;
        int pickerTop = yPos + 64;
        int pickerAreaHeight = footerY - pickerTop - 8;
        int categoryWidth = 116;
        int gridX = xPos + 18 + categoryWidth + 8;
        int gridWidth = pickerWidth - categoryWidth - 44;

        MKCreativeGridPicker grid = new MKCreativeGridPicker(gridX, pickerTop, gridWidth, pickerAreaHeight);
        grid.setSelectedId(blockPickerRequest.currentValue());
        if (selectedCategory != null) {
            grid.setEntries(blockPickerSource.entries(minecraft, selectedCategory, blockPickerQuery));
        }
        grid.setSelectionCallback(entry -> {
            blockPickerRequest.selectionCallback().accept(entry.id());
            closeBlockPicker();
        });
        root.addWidget(grid);

        searchField.setTextChangeCallback((field, value) -> {
            blockPickerQuery = value;
            grid.resetScroll();
            if (selectedCategory != null) {
                grid.setEntries(blockPickerSource.entries(minecraft, selectedCategory, value));
            }
        });

        MKScrollView categoryScroll = new MKScrollView(xPos + 12, pickerTop, categoryWidth, pickerAreaHeight);
        categoryScroll.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(categoryScroll);

        MKStackLayoutVertical categoryContent = new MKStackLayoutVertical(0, 0, categoryWidth - 4);
        categoryContent.setPaddingTop(0).setPaddingBot(0);
        categoryScroll.addWidget(categoryContent);
        for (MKCreativePickerCategory category : categories) {
            MKButton categoryButton = new MKButton(category.displayName(), categoryWidth - 8, BUTTON_HEIGHT);
            categoryButton.setTooltip(category.displayName());
            categoryButton.setEnabled(selectedCategory == null || !category.id().equals(selectedCategory.id()));
            categoryButton.setPressedCallback((button, mouseButton) -> {
                blockPickerCategoryId = category.id();
                grid.resetScroll();
                grid.setEntries(blockPickerSource.entries(minecraft, category, searchField.getText()));
                return true;
            });
            categoryContent.addWidget(categoryButton);
        }
        finalizeScrollView(categoryScroll, "creative_block_picker", false);

        MKButton cancel = new MKButton(Component.literal("Cancel"), 100, BUTTON_HEIGHT);
        cancel.setX(xPos + (pickerWidth / 2) - 104);
        cancel.setY(footerY);
        root.addWidget(cancel);
        cancel.setPressedCallback((button, mouseButton) -> {
            closeBlockPicker();
            return true;
        });

        if (blockPickerRequest.allowClear()) {
            MKButton clear = new MKButton(Component.literal("Clear"), 100, BUTTON_HEIGHT);
            clear.setX(xPos + (pickerWidth / 2) + 4);
            clear.setY(footerY);
            root.addWidget(clear);
            clear.setPressedCallback((button, mouseButton) -> {
                blockPickerRequest.selectionCallback().accept(ResourceLocation.withDefaultNamespace("air"));
                closeBlockPicker();
                return true;
            });
        }

        return root;
    }

    private void addRow(MKStackLayoutVertical root, MKText label, MKButton button) {
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private class PaletteOverrideGrid extends MKLayout {
        private static final int COLUMN_COUNT = 2;
        private static final int WIDGETS_PER_ENTRY = 3;
        private static final int ROW_HEIGHT = 36;
        private static final int LABEL_HEIGHT = 11;

        private PaletteOverrideGrid() {
            super(0, 0, CONTENT_WIDTH, ROW_HEIGHT * 3);
        }

        private void addEntry(MKText labelText, PaletteBlockSlot slot, MKText idText) {
            int columnWidth = getColumnWidth();
            labelText.setWidth(columnWidth - 8);
            idText.setWidth(columnWidth - 28);
            addWidget(labelText);
            addWidget(slot);
            addWidget(idText);
        }

        private int getColumnWidth() {
            return getWidth() / COLUMN_COUNT;
        }

        @Override
        public void layoutWidget(IMKWidget widget, int index) {
            int columnWidth = getColumnWidth();
            int entryIndex = index / WIDGETS_PER_ENTRY;
            int entryPart = index % WIDGETS_PER_ENTRY;
            int column = entryIndex % COLUMN_COUNT;
            int row = entryIndex / COLUMN_COUNT;
            int left = getX() + column * columnWidth;
            int top = getY() + row * ROW_HEIGHT;
            if (entryPart == 0) {
                widget.setX(left);
                widget.setY(top);
            } else if (entryPart == 1) {
                widget.setX(left);
                widget.setY(top + LABEL_HEIGHT + 1);
            } else {
                IMKWidget slot = getChild(index - 1);
                widget.setX(left + slot.getWidth() + 5);
                widget.setY(top + LABEL_HEIGHT + 5);
            }
        }
    }

    public void addPaletteOverrideRows(MKStackLayoutVertical root, String title,
                                        MKWorkspaceMaterialPalette inheritedPalette,
                                        Optional<MKWorkspacePaletteOverride> overrideOpt,
                                        Consumer<Optional<MKWorkspacePaletteOverride>> updater) {
        MKText header = makeWhiteText(Component.literal(title));
        root.addWidget(header);
        root.addConstraintToWidget(MarginConstraint.LEFT, header);
        MKWorkspacePaletteOverride override = overrideOpt.orElse(MKWorkspacePaletteOverride.EMPTY);
        Consumer<MKWorkspacePaletteOverride> overrideUpdater = nextOverride ->
                updater.accept(nextOverride.isEmpty() ? Optional.empty() : Optional.of(nextOverride));
        PaletteOverrideGrid grid = new PaletteOverrideGrid();
        addPaletteOverrideEntry(grid, "Floor", inheritedPalette.floorBlock(), override.floorBlockOpt(),
                value -> overrideUpdater.accept(override.withFloorBlock(value)),
                () -> overrideUpdater.accept(override.withFloorBlock(null)));
        addPaletteOverrideEntry(grid, "Wall", inheritedPalette.wallBlock(), override.wallBlockOpt(),
                value -> overrideUpdater.accept(override.withWallBlock(value)),
                () -> overrideUpdater.accept(override.withWallBlock(null)));
        addPaletteOverrideEntry(grid, "Ceiling", inheritedPalette.ceilingBlock(), override.ceilingBlockOpt(),
                value -> overrideUpdater.accept(override.withCeilingBlock(value)),
                () -> overrideUpdater.accept(override.withCeilingBlock(null)));
        addPaletteOverrideEntry(grid, "Stair", inheritedPalette.stairBlock(), override.stairBlockOpt(),
                value -> overrideUpdater.accept(override.withStairBlock(value)),
                () -> overrideUpdater.accept(override.withStairBlock(null)));
        addPaletteOverrideEntry(grid, "Slab", inheritedPalette.slabBlock(), override.slabBlockOpt(),
                value -> overrideUpdater.accept(override.withSlabBlock(value)),
                () -> overrideUpdater.accept(override.withSlabBlock(null)));
        addPaletteOverrideEntry(grid, "Ladder", inheritedPalette.ladderBlock(), override.ladderBlockOpt(),
                value -> overrideUpdater.accept(override.withLadderBlock(value)),
                () -> overrideUpdater.accept(override.withLadderBlock(null)));
        root.addWidget(grid);
        root.addConstraintToWidget(MarginConstraint.LEFT, grid);
        if (overrideOpt.isPresent()) {
            MKButton clear = new MKButton(Component.literal("Inherit All Materials"), 180, 20);
            clear.setPressedCallback((button, mouseButton) -> {
                updater.accept(Optional.empty());
                refreshPreservingActiveScroll();
                return true;
            });
            addRow(root, makeWhiteText(Component.literal("Overrides")), clear);
        }
    }

    private void addPaletteOverrideEntry(PaletteOverrideGrid grid, String label, ResourceLocation inheritedBlock,
                                         Optional<ResourceLocation> overrideBlock,
                                         Consumer<ResourceLocation> setter, Runnable resetter) {
        ResourceLocation displayedBlock = overrideBlock.orElse(inheritedBlock);
        MKText labelText = makeWhiteText(Component.literal(label));
        PaletteBlockSlot slot = new PaletteBlockSlot(displayedBlock,
                () -> openBlockPicker("Choose " + label + " Block", displayedBlock, value -> {
                    setter.accept(value);
                    refreshPreservingActiveScroll();
                }, false),
                () -> {
                    resetter.run();
                    refreshPreservingActiveScroll();
                });
        slot.setTooltip(Component.literal(displayedBlock + "\nLeft-click to choose. Right-click to reset."));
        MKText idText = makeWhiteText(blockDisplayName(displayedBlock));
        idText.setTooltip(displayedBlock.toString());
        grid.addEntry(labelText, slot, idText);
    }

    public void addPaletteBlockPickerRow(MKLayout root, int xPos, int y, String label, ResourceLocation blockId,
                                         ResourceLocation defaultBlock, Consumer<ResourceLocation> setter) {
        int rowX = xPos + 22;

        MKText labelText = makeWhiteText(Component.literal(label));
        labelText.setX(rowX);
        labelText.setY(y);
        labelText.setWidth(66);
        root.addWidget(labelText);

        PaletteBlockSlot slot = new PaletteBlockSlot(blockId,
                () -> openBlockPicker("Choose " + label + " Block", blockId, value -> {
                    setter.accept(value);
                    flagNeedSetup();
                }, false),
                () -> {
                    setter.accept(defaultBlock);
                    flagNeedSetup();
                });
        slot.setTooltip(Component.literal(blockId + "\nLeft-click to choose. Right-click to reset."));
        slot.setX(rowX + 72);
        slot.setY(y - 5);
        root.addWidget(slot);

        MKText idText = makeWhiteText(blockDisplayName(blockId));
        idText.setX(rowX + 96);
        idText.setY(y);
        idText.setWidth(180);
        idText.setTooltip(blockId.toString());
        root.addWidget(idText);
    }

    private MKText makeLabel(String translationKey) {
        MKText text = makeWhiteText(Component.translatable(translationKey));
        text.setWidth(CONTENT_WIDTH);
        return text;
    }

    public MKText makeWhiteText(Component text) {
        return new MKText(font, text).setColor(TEXT_COLOR);
    }

    public int scrollTopAfterHeader(MKLayout root, MKText headerText) {
        root.manualRecompute();
        return Math.max(root.getY() + TOP_CONTENT_Y, headerText.getBottom() + HEADER_SCROLL_GAP);
    }

    public void addBlockPickerRow(MKLayout root, int xPos, int y, String label, ResourceLocation blockId,
                                  Consumer<ResourceLocation> setter, boolean allowClear) {
        int rowX = xPos + 22;

        MKText labelText = makeWhiteText(Component.literal(label));
        labelText.setX(rowX);
        labelText.setY(y);
        labelText.setWidth(66);
        root.addWidget(labelText);

        MKBlockSlot preview = new MKBlockSlot();
        preview.setBlock(blockId);
        preview.setEnabled(false);
        preview.setX(rowX + 72);
        preview.setY(y - 5);
        root.addWidget(preview);

        MKText idText = makeWhiteText(blockDisplayName(blockId));
        idText.setX(rowX + 96);
        idText.setY(y);
        idText.setWidth(130);
        idText.setTooltip(blockId.toString());
        root.addWidget(idText);

        MKButton choose = new MKButton(Component.literal("Choose"), 82, BUTTON_HEIGHT);
        choose.setX(xPos + panelWidth() - 106);
        choose.setY(y - 6);
        root.addWidget(choose);
        choose.setPressedCallback((button, mouseButton) -> {
            openBlockPicker("Choose " + label + " Block", blockId, value -> {
                setter.accept(value);
                preview.setBlock(value);
                idText.setText(blockDisplayName(value));
                idText.setTooltip(value.toString());
            }, allowClear);
            return true;
        });
    }

    public Component blockDisplayName(ResourceLocation blockId) {
        return BuiltInRegistries.BLOCK.getOptional(blockId)
                .map(block -> block.getName())
                .orElse(Component.literal(shortBlockId(blockId)));
    }

    public void openBlockPicker(String title, ResourceLocation currentValue, Consumer<ResourceLocation> setter,
                                boolean allowClear) {
        if (blockPickerModal != null) {
            closeBlockPicker();
        }
        blockPickerRequest = new BlockPickerRequest(title, currentValue, setter, allowClear);
        blockPickerCategoryId = null;
        blockPickerQuery = "";

        int pickerWidth = Math.min(560, Math.max(320, width - 32));
        int pickerHeight = Math.min(440, Math.max(300, height - 32));
        int pickerX = width / 2 - pickerWidth / 2;
        int pickerY = height / 2 - pickerHeight / 2;

        MKModal modal = new MKBlockingModal();
        modal.setCloseOnClickOutside(false);
        modal.addWidget(buildCreativeBlockPickerContent(pickerX, pickerY, pickerWidth, pickerHeight));
        modal.setOnCloseCallback(() -> {
            if (blockPickerModal == modal) {
                blockPickerModal = null;
                clearBlockPickerState();
            }
        });
        blockPickerModal = modal;
        addModal(modal);
    }

    private void closeBlockPicker() {
        if (blockPickerModal != null) {
            closeModal(blockPickerModal);
            return;
        }
        clearBlockPickerState();
    }

    private void clearBlockPickerState() {
        blockPickerRequest = null;
        blockPickerCategoryId = null;
        blockPickerQuery = "";
    }

    private MKCreativePickerCategory selectedBlockPickerCategory(List<MKCreativePickerCategory> categories) {
        if (categories.isEmpty()) {
            return null;
        }
        if (blockPickerCategoryId != null) {
            for (MKCreativePickerCategory category : categories) {
                if (category.id().equals(blockPickerCategoryId)) {
                    return category;
                }
            }
        }
        MKCreativePickerCategory selected = categories.getFirst();
        blockPickerCategoryId = selected.id();
        return selected;
    }

    private String shortBlockId(ResourceLocation blockId) {
        String value = blockId.toString();
        if (value.length() <= 25) {
            return value;
        }
        return "..." + value.substring(value.length() - 22);
    }

    private MKTextFieldWidget makeField(String label, String value) {
        MKTextFieldWidget widget = new MKTextFieldWidget(font, 0, 0, 180, 18, Component.literal(label));
        widget.setText(value);
        return widget;
    }

    public void finalizeScrollView(MKScrollView scrollView, String stateName) {
        finalizeScrollView(scrollView, stateName, true);
    }

    private void finalizeScrollView(MKScrollView scrollView, String stateName, boolean centerX) {
        if (centerX) {
            scrollView.centerContentX();
        }
        scrollView.setToTop();
    }

    private void clampScrollViewOffsets(MKScrollView scrollView) {
        IMKWidget child = scrollView.getChild();
        if (child == null) {
            return;
        }
        if (scrollView.shouldScrollX() && scrollView.isContentWider()) {
            double minOffsetX = scrollView.getWidth() - child.getWidth() - scrollView.getScrollMarginX();
            double maxOffsetX = scrollView.getScrollMarginX();
            scrollView.setOffsetX(Math.max(minOffsetX, Math.min(scrollView.getOffsetX(), maxOffsetX)));
        }
        if (!scrollView.shouldScrollY() || !scrollView.isContentTaller()) {
            scrollView.setOffsetY(Math.min(scrollView.getOffsetY(), scrollView.getScrollMarginY()));
            return;
        }
        double minOffsetY = scrollView.getHeight() - child.getHeight() - scrollView.getScrollMarginY();
        double maxOffsetY = scrollView.getScrollMarginY();
        scrollView.setOffsetY(Math.max(minOffsetY, Math.min(scrollView.getOffsetY(), maxOffsetY)));
    }

    private List<ScrollViewState> getActiveScrollViewStates() {
        List<MKScrollView> scrollViews = getActiveScrollViews();
        if (scrollViews.isEmpty()) {
            return List.of();
        }
        return scrollViews.stream()
                .map(scrollView -> new ScrollViewState(scrollView.getOffsetX(), scrollView.getOffsetY()))
                .toList();
    }

    private void restoreActiveScrollViewStates(List<ScrollViewState> scrollStates, boolean resetScrollView) {
        List<MKScrollView> scrollViews = getActiveScrollViews();
        if (scrollViews.isEmpty()) {
            return;
        }
        if (resetScrollView) {
            for (MKScrollView scrollView : scrollViews) {
                scrollView.resetView();
            }
            return;
        }
        if (scrollStates.isEmpty()) {
            return;
        }
        int count = Math.min(scrollViews.size(), scrollStates.size());
        for (int index = 0; index < count; index++) {
            MKScrollView scrollView = scrollViews.get(index);
            ScrollViewState scrollState = scrollStates.get(index);
            scrollView.setOffsetX(scrollState.offsetX());
            scrollView.setOffsetY(scrollState.offsetY());
            clampScrollViewOffsets(scrollView);
        }
    }

    public void refreshPreservingActiveScroll() {
        List<ScrollViewState> scrollStates = getActiveScrollViewStates();
        boolean resetScrollView = wasResized;
        addPostSetupCallback(() -> restoreActiveScrollViewStates(scrollStates, resetScrollView));
        flagNeedSetup();
    }

    private List<MKScrollView> getActiveScrollViews() {
        if (children.isEmpty()) {
            return List.of();
        }
        ArrayList<MKScrollView> scrollViews = new ArrayList<>();
        collectScrollViews(children.peekLast(), scrollViews);
        return scrollViews;
    }

    private void collectScrollViews(IMKWidget widget, List<MKScrollView> scrollViews) {
        if (widget instanceof MKScrollView scrollView) {
            scrollViews.add(scrollView);
        }
        for (IMKWidget child : widget.getChildren()) {
            collectScrollViews(child, scrollViews);
        }
    }

    private Map<String, List<MKWorkspacePieceDefinition>> groupPiecesByTopology() {
        return WorkspacePieceDisplay.groupPiecesByTopology(workspace);
    }

    public boolean supportsStairGeneration(MKWorkspacePieceDefinition piece) {
        return WorkspacePieceDisplay.supportsStairGeneration(piece);
    }

    private boolean hasGeneratedStairs(MKWorkspacePieceDefinition piece) {
        return WorkspacePieceDisplay.hasGeneratedStairs(piece);
    }

    private List<String> getDefaultInitialStates() {
        if (workspace != null && !workspace.pieces().isEmpty()) {
            return List.of("workspace");
        }
        return importManifestIds.isEmpty() ? List.of("form") : List.of("home");
    }

    private List<String> getInitialStatesForRefresh(MKStructureWorkspace updatedWorkspace) {
        String currentState = getState();
        if (WorkspaceTopologySlotPage.ID.equals(currentState) && selectedTopologyKey != null &&
                updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()) {
            return List.of("workspace", WorkspaceTopologySlotPage.ID);
        }
        if (WorkspacePlannerNodePage.ID.equals(currentState) && selectedPlannerStackId != null &&
                updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()) {
            return List.of("workspace", WorkspacePlannerNodePage.ID);
        }
        if (WorkspaceFloorPlanPage.ID.equals(currentState) && selectedFloorPlanStackId != null &&
                selectedFloorPlanSectionKey != null && updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()) {
            return List.of("workspace", WorkspacePlannerNodePage.ID, WorkspaceFloorPlanPage.ID);
        }
        if (WorkspaceTemplateGroupsPage.ID.equals(currentState) &&
                updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()) {
            return List.of("workspace", WorkspaceTemplateGroupsPage.ID);
        }
        if ("backups".equals(currentState) && updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()) {
            return List.of("workspace", "backups");
        }
        if ("utilities".equals(currentState) && updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()) {
            return List.of("workspace", "utilities");
        }
        if ("block_swap".equals(currentState) && updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()) {
            return List.of("workspace", "utilities", "block_swap");
        }
        if ("form".equals(currentState)) {
            return updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()
                    ? List.of("workspace", "form")
                    : List.of("form");
        }
        if (currentState.startsWith("form_")) {
            return updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()
                    ? List.of("workspace", "form", currentState)
                    : List.of("form", currentState);
        }
        return updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()
                ? List.of("workspace")
                : List.of("form");
    }

    public void switchToExistingState(String stateName) {
        if (stateName.equals(getState())) {
            flagNeedSetup();
            return;
        }
        while (!getState().equals(stateName) && !getState().equals(NO_STATE)) {
            popState();
        }
        if (getState().equals(NO_STATE)) {
            pushState(stateName);
        }
        flagNeedSetup();
    }

    public void goBackOrSwitchTo(String fallbackState) {
        if (!getState().equals(NO_STATE)) {
            popState();
        }
        if (getState().equals(NO_STATE)) {
            pushState(fallbackState);
        }
        flagNeedSetup();
    }

    public void resetTopologySlotOverrides() {
        if (workspace == null) {
            detailStairConfig = new MKWorkspaceStairAuthoringConfig(
                    MKWorkspaceStairMode.AUTO,
                    MKWorkspaceStairRiseType.MIXED,
                    1
            );
            return;
        }
        detailStairConfig = new MKWorkspaceStairAuthoringConfig(
                workspace.stairConfig().mode(),
                workspace.stairConfig().riseType(),
                workspace.stairConfig().stairWidth()
        );
    }

    public void ensureTopologySlotOverridesInitialized() {
        if (detailStairConfig == null) {
            resetTopologySlotOverrides();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int xPos = panelX();
        int yPos = panelY();
        graphics.fill(xPos, yPos, xPos + panelWidth(), yPos + panelHeight(), 0xCC202020);
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        boolean handled = super.mouseReleased(mouseX, mouseY, mouseButton);
        if (getDragState().isPresent()) {
            clearDragState();
            return true;
        }
        return handled;
    }
}

