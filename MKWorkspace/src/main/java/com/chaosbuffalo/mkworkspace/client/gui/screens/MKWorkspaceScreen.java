package com.chaosbuffalo.mkworkspace.client.gui.screens;

import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKBlockingModal;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceBlockSwapPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceBackupPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceHomePage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceImportPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceGenerateConfirmPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormIdentityPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceDraftSession;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceDeleteConfirmPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormFamiliesPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormFamilyDetailPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormInsertFamiliesPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormInsertFamilyDetailPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormLinearRunsPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormLinearRunDetailPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFloorPlanPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormOpeningsPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceFormOpeningDetailPage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceManagePage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePageBase;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePieceDisplay;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerNodePage;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspacePlannerClientRegistry;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceTopologySlotEditor;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.WorkspaceUtilitiesPage;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePaletteOverride;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceContentSelectionTags;
import com.chaosbuffalo.mkworkspace.client.gui.screens.workspace.MKWorkspaceClientChangePlan;
import com.chaosbuffalo.mkworkspace.network.packets.RequestWorkspaceChangePacket;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequest;
import net.neoforged.neoforge.network.PacketDistributor;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceSamplePreviewState;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKBlockSlot;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKCreativeBlockPickerPanel;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    private final MKWorkspaceSamplePreviewState samplePreviewState;
    private final List<String> importManifestIds;
    private final List<String> backupManifestFiles;
    private final int totalWorkspacePieces;
    private final int nextPieceOffset;
    private final long pieceRevision;
    private final List<String> initialStates;
    private String selectedTopologyKey;
    private String selectedPlannerStackId;
    private String selectedFloorPlanStackId;
    private String selectedFloorPlanSectionKey;
    private MKWorkspaceStairAuthoringConfig detailStairConfig;
    private BlockPickerRequest blockPickerRequest;
    private MKModal blockPickerModal;
    private ResourceLocation blockSwapSourceBlock;
    private ResourceLocation blockSwapTargetBlock;
    private MKModal unsavedDraftModal;
    private boolean forceClose;
    private boolean wasResized;
    private final WorkspaceTopologySlotEditor topologySlotEditor = new WorkspaceTopologySlotEditor(this);
    private final WorkspaceDraftSession draftSession;
    private List<ScrollViewState> pendingScrollViewStates = List.of();
    private boolean pendingScrollViewRestore;
    private boolean pendingScrollViewReset;
    private UUID pendingChangeRequestId;
    private MKWorkspaceClientChangePlan changePlan;
    private String workspaceChangeMessage = "";
    private boolean workspaceChangeFailed;

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
        this(anchor, workspace, importManifestIds, backupManifestFiles,
                null,
                workspace == null ? 0 : workspace.pieces().size(),
                workspace == null ? 0 : workspace.pieces().size(), 0L);
    }

    public MKWorkspaceScreen(net.minecraft.core.BlockPos anchor, MKStructureWorkspace workspace,
                             List<String> importManifestIds, List<String> backupManifestFiles,
                             MKWorkspaceSamplePreviewState samplePreviewState,
                             int totalWorkspacePieces, int nextPieceOffset, long pieceRevision) {
        this(anchor, workspace, importManifestIds, backupManifestFiles, List.of(), null, null, null, null,
                -1, -1, -1, -1, -1, null, samplePreviewState, totalWorkspacePieces, nextPieceOffset,
                pieceRevision);
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
                              int selectedInsertFamilyIndex,
                              MKWorkspaceStairAuthoringConfig detailStairConfig,
                              MKWorkspaceSamplePreviewState samplePreviewState,
                              int totalWorkspacePieces,
                              int nextPieceOffset,
                              long pieceRevision) {
        super(Component.literal("Tower Workspace"));
        this.anchor = anchor;
        this.workspace = workspace;
        this.samplePreviewState = samplePreviewState;
        this.importManifestIds = List.copyOf(importManifestIds);
        this.backupManifestFiles = List.copyOf(backupManifestFiles);
        this.totalWorkspacePieces = Math.max(totalWorkspacePieces, workspace == null ? 0 : workspace.pieces().size());
        this.nextPieceOffset = nextPieceOffset;
        this.pieceRevision = pieceRevision;
        this.initialStates = List.copyOf(initialStates);
        this.selectedTopologyKey = selectedTopologyKey;
        this.selectedPlannerStackId = selectedPlannerStackId;
        this.selectedFloorPlanStackId = selectedFloorPlanStackId;
        this.selectedFloorPlanSectionKey = selectedFloorPlanSectionKey;
        this.detailStairConfig = detailStairConfig;
        this.draftSession = new WorkspaceDraftSession(this, selectedFamilyIndex,
                selectedFamilyExitIndex, selectedOpeningIndex, selectedLinearRunIndex, selectedInsertFamilyIndex);
    }

    public MKWorkspaceScreen copyWithWorkspace(MKStructureWorkspace updatedWorkspace, List<String> updatedImportManifestIds) {
        return copyWithWorkspace(updatedWorkspace, updatedImportManifestIds, backupManifestFiles);
    }

    public MKWorkspaceScreen copyWithWorkspace(MKStructureWorkspace updatedWorkspace, List<String> updatedImportManifestIds,
                                               List<String> updatedBackupManifestFiles) {
        return copyWithWorkspace(updatedWorkspace, updatedImportManifestIds, updatedBackupManifestFiles,
                updatedWorkspace == null ? 0 : updatedWorkspace.pieces().size(),
                updatedWorkspace == null ? 0 : updatedWorkspace.pieces().size(), 0L);
    }

    public MKWorkspaceScreen copyWithWorkspace(MKStructureWorkspace updatedWorkspace, List<String> updatedImportManifestIds,
                                               List<String> updatedBackupManifestFiles, int updatedTotalPieces,
                                               int updatedNextPieceOffset, long updatedPieceRevision) {
        return copyWithWorkspace(updatedWorkspace, updatedImportManifestIds, updatedBackupManifestFiles,
                samplePreviewState, updatedTotalPieces, updatedNextPieceOffset, updatedPieceRevision);
    }

    public MKWorkspaceScreen copyWithWorkspace(MKStructureWorkspace updatedWorkspace, List<String> updatedImportManifestIds,
                                               List<String> updatedBackupManifestFiles,
                                               MKWorkspaceSamplePreviewState updatedSamplePreviewState,
                                               int updatedTotalPieces,
                                               int updatedNextPieceOffset, long updatedPieceRevision) {
        List<String> refreshStates = getInitialStatesForRefresh(updatedWorkspace, false);
        MKWorkspaceScreen copy = new MKWorkspaceScreen(anchor, updatedWorkspace, updatedImportManifestIds, updatedBackupManifestFiles,
                refreshStates,
                selectedTopologyKey, selectedPlannerStackId, selectedFloorPlanStackId, selectedFloorPlanSectionKey,
                draftSession.selectedFamilyIndex(),
                draftSession.selectedFamilyExitIndex(), draftSession.selectedOpeningIndex(),
                draftSession.selectedLinearRunIndex(),
                draftSession.selectedInsertFamilyIndex(),
                detailStairConfig, updatedSamplePreviewState, updatedTotalPieces, updatedNextPieceOffset,
                updatedPieceRevision);
        copy.copyClientViewStateFrom(this, restoresCurrentPage(refreshStates));
        return copy;
    }

    public MKWorkspaceScreen copyWithChangePlan(MKWorkspaceClientChangePlan updatedPlan) {
        List<String> refreshStates = getInitialStatesForRefresh(workspace, true);
        if (refreshStates.isEmpty() || !WorkspaceGenerateConfirmPage.ID.equals(refreshStates.getLast())) {
            ArrayList<String> confirmationStates = new ArrayList<>(refreshStates);
            confirmationStates.add(WorkspaceGenerateConfirmPage.ID);
            refreshStates = List.copyOf(confirmationStates);
        }
        MKWorkspaceScreen copy = new MKWorkspaceScreen(anchor, workspace, importManifestIds, backupManifestFiles,
                refreshStates, selectedTopologyKey, selectedPlannerStackId, selectedFloorPlanStackId,
                selectedFloorPlanSectionKey, draftSession.selectedFamilyIndex(), draftSession.selectedFamilyExitIndex(),
                draftSession.selectedOpeningIndex(), draftSession.selectedLinearRunIndex(),
                draftSession.selectedInsertFamilyIndex(), detailStairConfig, samplePreviewState,
                totalWorkspacePieces, nextPieceOffset, pieceRevision);
        copy.copyClientViewStateFrom(this, restoresCurrentPage(refreshStates));
        copy.changePlan = updatedPlan;
        copy.pendingChangeRequestId = updatedPlan.requestId();
        return copy;
    }

    public MKWorkspaceScreen copyWithWorkspacePieceChunk(List<MKWorkspacePieceDefinition> pieces, int updatedTotalPieces,
                                                        int updatedNextPieceOffset, long updatedPieceRevision) {
        if (workspace == null || updatedPieceRevision != pieceRevision) {
            return this;
        }
        LinkedHashMap<UUID, MKWorkspacePieceDefinition> mergedPieces = new LinkedHashMap<>();
        for (MKWorkspacePieceDefinition piece : workspace.pieces()) {
            mergedPieces.put(piece.pieceId(), piece);
        }
        for (MKWorkspacePieceDefinition piece : pieces) {
            mergedPieces.put(piece.pieceId(), piece);
        }
        MKStructureWorkspace updatedWorkspace = withPiecesPreservingMetadata(workspace,
                List.copyOf(mergedPieces.values()));
        return copyWithWorkspace(updatedWorkspace, importManifestIds, backupManifestFiles, updatedTotalPieces,
                updatedNextPieceOffset, updatedPieceRevision);
    }

    private boolean restoresCurrentPage(List<String> refreshStates) {
        return !refreshStates.isEmpty() && refreshStates.getLast().equals(getState());
    }

    private void copyClientViewStateFrom(MKWorkspaceScreen source, boolean restoreScrollViews) {
        draftSession.copyViewStateFrom(source.draftSession);
        pendingChangeRequestId = source.pendingChangeRequestId;
        changePlan = source.changePlan;
        workspaceChangeMessage = source.workspaceChangeMessage;
        workspaceChangeFailed = source.workspaceChangeFailed;
        if (!restoreScrollViews) {
            return;
        }
        pendingScrollViewStates = source.getActiveScrollViewStates();
        pendingScrollViewRestore = !pendingScrollViewStates.isEmpty();
        pendingScrollViewReset = source.wasResized;
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addWorkspacePage(new WorkspaceHomePage());
        addWorkspacePage(new WorkspaceImportPage());
        addWorkspacePage(new WorkspaceFormPage());
        addWorkspacePage(new WorkspaceGenerateConfirmPage());
        addWorkspacePage(new WorkspaceFormIdentityPage());
        addWorkspacePage(new WorkspaceFormFamiliesPage());
        WorkspaceFormFamilyDetailPage familyDetailPage = new WorkspaceFormFamilyDetailPage();
        addWorkspacePage(familyDetailPage);
        addWorkspacePage(new WorkspaceFormOpeningsPage());
        addWorkspacePage(new WorkspaceFormOpeningDetailPage());
        addWorkspacePage(new WorkspaceFormLinearRunsPage());
        addWorkspacePage(new WorkspaceFormLinearRunDetailPage());
        addWorkspacePage(new WorkspaceFormInsertFamiliesPage());
        addWorkspacePage(new WorkspaceFormInsertFamilyDetailPage());
        addWorkspacePage(new WorkspaceManagePage());
        addWorkspacePage(new WorkspaceUtilitiesPage());
        addWorkspacePage(new WorkspaceDeleteConfirmPage());
        addWorkspacePage(new WorkspaceBlockSwapPage());
        addWorkspacePage(new WorkspaceBackupPage());
        addWorkspacePage(new WorkspacePlannerNodePage());
        addWorkspacePage(new WorkspaceFloorPlanPage());
        if (NO_STATE.equals(getState())) {
            List<String> statesToPush = initialStates.isEmpty() ? getDefaultInitialStates() : initialStates;
            for (String state : statesToPush) {
                pushState(state);
            }
        }
        restorePendingScrollViewStates();
    }

    private void addWorkspacePage(WorkspacePageBase page) {
        addState(page.id(), () -> page.build(this));
    }

    public boolean hasExistingWorkspacePieces() {
        return workspace != null && totalWorkspacePieces > 0;
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

    public Optional<MKWorkspaceSamplePreviewState> samplePreviewState() {
        return Optional.ofNullable(samplePreviewState);
    }

    public int totalWorkspacePieces() {
        return totalWorkspacePieces;
    }

    public int loadedWorkspacePieces() {
        return workspace == null ? 0 : workspace.pieces().size();
    }

    public boolean loadingWorkspacePieces() {
        return nextPieceOffset < totalWorkspacePieces;
    }

    public MKWorkspaceClientChangePlan changePlan() {
        return changePlan;
    }

    public void requestWorkspaceChange(MKWorkspaceChangeRequest request) {
        pendingChangeRequestId = request.requestId();
        changePlan = null;
        workspaceChangeMessage = "";
        workspaceChangeFailed = false;
        PacketDistributor.sendToServer(new RequestWorkspaceChangePacket(request));
        if (!WorkspaceGenerateConfirmPage.ID.equals(getState())) {
            pushState(WorkspaceGenerateConfirmPage.ID);
        }
        flagNeedSetup();
    }

    public boolean acceptsWorkspaceChange(UUID requestId, BlockPos requestAnchor) {
        return anchor.equals(requestAnchor) &&
                (pendingChangeRequestId == null || requestId.equals(pendingChangeRequestId));
    }

    public void workspaceChangeMessage(String message, boolean failed) {
        workspaceChangeMessage = message == null ? "" : message;
        workspaceChangeFailed = failed;
        if (failed) {
            changePlan = null;
            pendingChangeRequestId = null;
        }
        flagNeedSetup();
    }

    public String workspaceChangeMessage() {
        return workspaceChangeMessage;
    }

    public boolean workspaceChangeFailed() {
        return workspaceChangeFailed;
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

    @Override
    public void onClose() {
        if (!forceClose && draftSession.dirty()) {
            openUnsavedDraftModal();
            return;
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void closeScreen() {
        onClose();
    }

    private void openUnsavedDraftModal() {
        if (unsavedDraftModal != null) {
            return;
        }
        int modalWidth = 380;
        int modalHeight = 150;
        int modalX = width / 2 - modalWidth / 2;
        int modalY = height / 2 - modalHeight / 2;

        MKModal modal = new MKBlockingModal();
        modal.setCloseOnClickOutside(false);
        modal.addWidget(buildUnsavedDraftModalContent(modalX, modalY, modalWidth, modalHeight, modal));
        modal.setOnCloseCallback(() -> {
            if (unsavedDraftModal == modal) {
                unsavedDraftModal = null;
            }
        });
        unsavedDraftModal = modal;
        addModal(modal);
    }

    private MKLayout buildUnsavedDraftModalContent(int xPos, int yPos, int modalWidth, int modalHeight,
                                                   MKModal modal) {
        MKLayout root = new MKLayout(xPos, yPos, modalWidth, modalHeight);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Unsaved Workspace Changes"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText message = makeWhiteText(Component.literal(
                "This draft has changes that have not been applied to the workspace."));
        message.setMultiline(true);
        message.setWidth(modalWidth - 40);
        message.setX(xPos + 20);
        message.setY(yPos + 42);
        root.addWidget(message);

        int buttonY = yPos + modalHeight - BOTTOM_PADDING - BUTTON_HEIGHT;
        int buttonWidth = 100;
        int gap = 12;
        int firstButtonX = xPos + (modalWidth - ((buttonWidth * 3) + (gap * 2))) / 2;

        MKButton apply = new MKButton(Component.literal("Apply"), buttonWidth, BUTTON_HEIGHT);
        apply.setX(firstButtonX);
        apply.setY(buttonY);
        root.addWidget(apply);
        apply.setPressedCallback((button, mouseButton) -> {
            closeModal(modal);
            draftSession.submit();
            return true;
        });

        MKButton discard = new MKButton(Component.literal("Discard"), buttonWidth, BUTTON_HEIGHT);
        discard.setX(firstButtonX + buttonWidth + gap);
        discard.setY(buttonY);
        root.addWidget(discard);
        discard.setPressedCallback((button, mouseButton) -> {
            closeModal(modal);
            draftSession.clearDirty();
            forceClose = true;
            onClose();
            forceClose = false;
            return true;
        });

        MKButton cancel = new MKButton(Component.literal("Cancel"), buttonWidth, BUTTON_HEIGHT);
        cancel.setX(firstButtonX + ((buttonWidth + gap) * 2));
        cancel.setY(buttonY);
        root.addWidget(cancel);
        cancel.setPressedCallback((button, mouseButton) -> {
            closeModal(modal);
            return true;
        });
        return root;
    }

    public void openWorkspaceTopologySlot(String topologyKey) {
        selectedTopologyKey = topologyKey;
        resetTopologySlotOverrides();
        List<MKWorkspacePieceDefinition> pieces = groupPiecesByTopology().getOrDefault(topologyKey, List.of());
        if (!pieces.isEmpty()) {
            MKWorkspacePieceDefinition piece = pieces.getFirst();
            draftSession.selectContentFamily(MKWorkspaceContentSelectionTags.topologySlotId(piece),
                    MKWorkspaceContentSelectionTags.familyId(piece));
        }
        pushState(WorkspaceFormFamiliesPage.ID);
        flagNeedSetup();
    }

    public void selectWorkspaceTopologySlot(String topologyKey) {
        selectedTopologyKey = topologyKey;
        resetTopologySlotOverrides();
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

    public boolean openWorkspaceTopologySlotForBaseName(String baseName) {
        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry : groupPiecesByTopology().entrySet()) {
            if (entry.getValue().stream().anyMatch(piece -> baseName.equals(WorkspacePieceDisplay.getBaseName(piece)))) {
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
            WorkspacePlannerClientRegistry.getClientContributor(draftSession.topologyPlannerId())
                    .selectPlannerNode(draftSession, stackId);
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
            WorkspacePlannerClientRegistry.getClientContributor(draftSession.topologyPlannerId())
                    .selectPlannerNode(draftSession, stackId);
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
        String selectedSlotId = draftSession.selectedContentSlotId();
        String selectedFamilyId = draftSession.selectedContentFamilyId();
        if (selectedSlotId != null && selectedFamilyId != null && workspace != null) {
            return draftSession.filterPendingDeletedVariants(workspace.pieces().stream()
                    .filter(WorkspacePieceDisplay::isManageableTemplatePiece)
                    .filter(piece -> selectedSlotId.equals(MKWorkspaceContentSelectionTags.topologySlotId(piece)))
                    .filter(piece -> selectedFamilyId.equals(MKWorkspaceContentSelectionTags.familyId(piece)))
                    .toList());
        }
        if (selectedTopologyKey == null) {
            return List.of();
        }
        return draftSession.filterPendingDeletedVariants(groupPiecesByTopology()
                .getOrDefault(selectedTopologyKey, List.of()));
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

    public void addDefaultPaletteRows(MKStackLayoutVertical root, String title,
                                      MKWorkspaceMaterialPalette palette,
                                      MKWorkspaceMaterialPalette defaultPalette,
                                      Consumer<MKWorkspaceMaterialPalette> updater) {
        MKText header = makeWhiteText(Component.literal(title));
        root.addWidget(header);
        root.addConstraintToWidget(MarginConstraint.LEFT, header);
        PaletteOverrideGrid grid = new PaletteOverrideGrid();
        addPaletteOverrideEntry(grid, "Floor", defaultPalette.floorBlock(), Optional.of(palette.floorBlock()),
                value -> updater.accept(new MKWorkspaceMaterialPalette(value, palette.wallBlock(),
                        palette.ceilingBlock(), palette.stairBlock(), palette.slabBlock(), palette.ladderBlock())),
                () -> updater.accept(new MKWorkspaceMaterialPalette(defaultPalette.floorBlock(), palette.wallBlock(),
                        palette.ceilingBlock(), palette.stairBlock(), palette.slabBlock(), palette.ladderBlock())));
        addPaletteOverrideEntry(grid, "Wall", defaultPalette.wallBlock(), Optional.of(palette.wallBlock()),
                value -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), value,
                        palette.ceilingBlock(), palette.stairBlock(), palette.slabBlock(), palette.ladderBlock())),
                () -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), defaultPalette.wallBlock(),
                        palette.ceilingBlock(), palette.stairBlock(), palette.slabBlock(), palette.ladderBlock())));
        addPaletteOverrideEntry(grid, "Ceiling", defaultPalette.ceilingBlock(), Optional.of(palette.ceilingBlock()),
                value -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                        value, palette.stairBlock(), palette.slabBlock(), palette.ladderBlock())),
                () -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                        defaultPalette.ceilingBlock(), palette.stairBlock(), palette.slabBlock(),
                        palette.ladderBlock())));
        addPaletteOverrideEntry(grid, "Stair", defaultPalette.stairBlock(), Optional.of(palette.stairBlock()),
                value -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                        palette.ceilingBlock(), value, palette.slabBlock(), palette.ladderBlock())),
                () -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                        palette.ceilingBlock(), defaultPalette.stairBlock(), palette.slabBlock(),
                        palette.ladderBlock())));
        addPaletteOverrideEntry(grid, "Slab", defaultPalette.slabBlock(), Optional.of(palette.slabBlock()),
                value -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                        palette.ceilingBlock(), palette.stairBlock(), value, palette.ladderBlock())),
                () -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                        palette.ceilingBlock(), palette.stairBlock(), defaultPalette.slabBlock(),
                        palette.ladderBlock())));
        addPaletteOverrideEntry(grid, "Ladder", defaultPalette.ladderBlock(), Optional.of(palette.ladderBlock()),
                value -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                        palette.ceilingBlock(), palette.stairBlock(), palette.slabBlock(), value)),
                () -> updater.accept(new MKWorkspaceMaterialPalette(palette.floorBlock(), palette.wallBlock(),
                        palette.ceilingBlock(), palette.stairBlock(), palette.slabBlock(),
                        defaultPalette.ladderBlock())));
        root.addWidget(grid);
        root.addConstraintToWidget(MarginConstraint.LEFT, grid);
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

        int pickerWidth = Math.min(560, Math.max(320, width - 32));
        int pickerHeight = Math.min(440, Math.max(300, height - 32));
        int pickerX = width / 2 - pickerWidth / 2;
        int pickerY = height / 2 - pickerHeight / 2;

        MKModal modal = new MKBlockingModal();
        modal.setCloseOnClickOutside(false);
        modal.addWidget(new MKCreativeBlockPickerPanel(pickerX, pickerY, pickerWidth, pickerHeight,
                Component.literal(blockPickerRequest.title()), blockPickerRequest.currentValue(), value -> {
            blockPickerRequest.selectionCallback().accept(value);
            closeBlockPicker();
        }, this::closeBlockPicker, blockPickerRequest.allowClear()));
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

    private void restorePendingScrollViewStates() {
        if (!pendingScrollViewRestore) {
            return;
        }
        restoreActiveScrollViewStates(pendingScrollViewStates, pendingScrollViewReset);
        pendingScrollViewStates = List.of();
        pendingScrollViewRestore = false;
        pendingScrollViewReset = false;
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
        return WorkspacePieceDisplay.groupManageableTemplatePiecesByTopology(workspace);
    }

    public boolean supportsStairGeneration(MKWorkspacePieceDefinition piece) {
        return WorkspacePieceDisplay.supportsStairGeneration(piece);
    }

    private boolean hasGeneratedStairs(MKWorkspacePieceDefinition piece) {
        return WorkspacePieceDisplay.hasGeneratedStairs(piece);
    }

    private List<String> getDefaultInitialStates() {
        if (workspace != null && totalWorkspacePieces > 0) {
            return List.of("workspace");
        }
        return importManifestIds.isEmpty() ? List.of("form") : List.of("home");
    }

    private List<String> getInitialStatesForRefresh(MKStructureWorkspace updatedWorkspace,
                                                    boolean preserveGenerateConfirmState) {
        String currentState = getState();
        if (WorkspaceFormFamiliesPage.ID.equals(currentState) && selectedTopologyKey != null &&
                updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)) {
            return List.of("workspace", WorkspaceFormFamiliesPage.ID);
        }
        if (WorkspacePlannerNodePage.ID.equals(currentState) && selectedPlannerStackId != null &&
                updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)) {
            return List.of("workspace", WorkspacePlannerNodePage.ID);
        }
        if (WorkspaceFloorPlanPage.ID.equals(currentState) && selectedFloorPlanStackId != null &&
                selectedFloorPlanSectionKey != null && updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)) {
            return List.of("workspace", WorkspacePlannerNodePage.ID, WorkspaceFloorPlanPage.ID);
        }
        if ("backups".equals(currentState) && updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)) {
            return List.of("workspace", "backups");
        }
        if ("utilities".equals(currentState) && updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)) {
            return List.of("workspace", "utilities");
        }
        if ("block_swap".equals(currentState) && updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)) {
            return List.of("workspace", "utilities", "block_swap");
        }
        if ("form".equals(currentState)) {
            return updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)
                    ? List.of("workspace", "form")
                    : List.of("form");
        }
        if (WorkspaceGenerateConfirmPage.ID.equals(currentState)) {
            if (!preserveGenerateConfirmState) {
                return updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)
                        ? List.of("workspace")
                        : List.of("form");
            }
            return updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)
                    ? List.of("workspace", "form", WorkspaceGenerateConfirmPage.ID)
                    : List.of("form", WorkspaceGenerateConfirmPage.ID);
        }
        if (currentState.startsWith("form_")) {
            return updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)
                    ? List.of("workspace", "form", currentState)
                    : List.of("form", currentState);
        }
        return updatedWorkspace != null && hasPiecesForRefresh(updatedWorkspace)
                ? List.of("workspace")
                : List.of("form");
    }

    private boolean hasPiecesForRefresh(MKStructureWorkspace updatedWorkspace) {
        return totalWorkspacePieces > 0 || !updatedWorkspace.pieces().isEmpty();
    }

    private MKStructureWorkspace withPiecesPreservingMetadata(MKStructureWorkspace source,
                                                              List<MKWorkspacePieceDefinition> pieces) {
        return new MKStructureWorkspace(
                source.id(),
                source.anchor(),
                source.namespace(),
                source.structureName(),
                source.topologyProfile(),
                source.dimensions(),
                source.palette(),
                source.stairConfig(),
                source.verticalAccessPlacement(),
                source.shellMargin(),
                source.verticalShellMargin(),
                source.exteriorAirMargin(),
                source.previewMargin(),
                source.verticalAccessSpec(),
                source.familyDefinitions(),
                source.openingProfiles(),
                source.linearRunFamilies(),
                source.insertFamilies(),
                source.createdAt(),
                source.updatedAt(),
                pieces,
                source.layerStates()
        );
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

