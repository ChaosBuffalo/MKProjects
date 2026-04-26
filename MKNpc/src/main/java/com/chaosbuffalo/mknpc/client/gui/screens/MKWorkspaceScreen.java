package com.chaosbuffalo.mknpc.client.gui.screens;

import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantPacket;
import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantsForAllPacket;
import com.chaosbuffalo.mknpc.network.packets.ClearWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.CreateWorkspacePacket;
import com.chaosbuffalo.mknpc.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateAllWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateWorkspacePacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.LoadWorkspaceFromManifestPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessTags;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterXConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.StackConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKBlockSlot;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKPlayerHotbar;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MKWorkspaceScreen extends MKScreen {
    private static final int PANEL_WIDTH = 380;
    private static final int PANEL_HEIGHT = 390;
    private static final int SCROLL_WIDTH = PANEL_WIDTH - 20;
    private static final int TOP_CONTENT_Y = 52;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 4;
    private static final int BOTTOM_PADDING = 8;
    private static final int CONTENT_WIDTH = PANEL_WIDTH - 42;
    private static final int TEXT_COLOR = 0xFFFFFF;

    private final net.minecraft.core.BlockPos anchor;
    private final MKStructureWorkspace workspace;
    private final List<String> importManifestIds;
    private final List<String> initialStates;
    private String selectedTopologyKey;
    private MKWorkspaceStairMode detailStairMode;
    private MKWorkspaceStairRiseType detailStairRiseType;
    private int detailFlatRunLength;
    private int detailStairWidth;
    private ResourceLocation detailStairBlock;
    private ResourceLocation detailSlabBlock;
    private ResourceLocation detailLadderBlock;
    private WorkspaceFormDraft formDraft;

    private static class WorkspaceFormDraft {
        private String namespace;
        private String structureName;
        private int roomWidth;
        private int roomLength;
        private int entranceHeight;
        private int roomHeight;
        private int basementHeight;
        private int hallwayWidth;
        private int doorwayWidth;
        private int doorwayHeight;
        private MKVerticalAccessPlacement verticalAccessPlacement;
        private int shellMargin;
        private int exteriorAirMargin;
        private int previewMargin;
        private MKWorkspaceStairMode stairMode;
        private MKWorkspaceStairRiseType stairRiseType;
        private int stairFlatRunLength;
        private int stairWidth;
        private ResourceLocation floorBlock;
        private ResourceLocation wallBlock;
        private ResourceLocation ceilingBlock;
        private ResourceLocation stairBlock;
        private ResourceLocation slabBlock;
        private ResourceLocation ladderBlock;
    }

    public MKWorkspaceScreen(net.minecraft.core.BlockPos anchor, MKStructureWorkspace workspace, List<String> importManifestIds) {
        this(anchor, workspace, importManifestIds, List.of(), null, null, null, 0, 1, null, null, null);
    }

    private MKWorkspaceScreen(net.minecraft.core.BlockPos anchor, MKStructureWorkspace workspace, List<String> importManifestIds,
                              List<String> initialStates,
                              String selectedTopologyKey, MKWorkspaceStairMode detailStairMode,
                              MKWorkspaceStairRiseType detailStairRiseType, int detailFlatRunLength, int detailStairWidth,
                              ResourceLocation detailStairBlock, ResourceLocation detailSlabBlock,
                              ResourceLocation detailLadderBlock) {
        super(Component.literal("Tower Workspace"));
        this.anchor = anchor;
        this.workspace = workspace;
        this.importManifestIds = List.copyOf(importManifestIds);
        this.initialStates = List.copyOf(initialStates);
        this.selectedTopologyKey = selectedTopologyKey;
        this.detailStairMode = detailStairMode;
        this.detailStairRiseType = detailStairRiseType;
        this.detailFlatRunLength = detailFlatRunLength;
        this.detailStairWidth = detailStairWidth;
        this.detailStairBlock = detailStairBlock;
        this.detailSlabBlock = detailSlabBlock;
        this.detailLadderBlock = detailLadderBlock;
    }

    public MKWorkspaceScreen copyWithWorkspace(MKStructureWorkspace updatedWorkspace, List<String> updatedImportManifestIds) {
        return new MKWorkspaceScreen(anchor, updatedWorkspace, updatedImportManifestIds, getInitialStatesForRefresh(updatedWorkspace),
                selectedTopologyKey, detailStairMode, detailStairRiseType, detailFlatRunLength, detailStairWidth,
                detailStairBlock, detailSlabBlock, detailLadderBlock);
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addState("home", this::buildHomeState);
        addState("import", this::buildImportState);
        addState("form", this::buildFormState);
        addState("form_identity", this::buildFormIdentityState);
        addState("form_vertical", this::buildFormVerticalState);
        addState("form_materials", this::buildFormMaterialsState);
        addState("workspace", this::buildWorkspaceState);
        addState("category", this::buildCategoryState);
        List<String> statesToPush = initialStates.isEmpty() ? getDefaultInitialStates() : initialStates;
        for (String state : statesToPush) {
            pushState(state);
        }
    }

    private MKLayout buildHomeState() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.translatable("mknpc.workspace.screen.title"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal("Create a new workspace or load an exported one."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        MKButton createNew = new MKButton(Component.literal("Create New Workspace"), 220, 20);
        root.addWidget(createNew);
        root.addConstraintToWidget(new CenterXConstraint(), createNew);
        createNew.setY(yPos + 120);
        createNew.setPressedCallback((button, mouseButton) -> {
            switchToExistingState("form");
            return true;
        });

        MKButton loadExisting = new MKButton(Component.literal("Load Existing Workspace"), 220, 20);
        root.addWidget(loadExisting);
        root.addConstraintToWidget(new CenterXConstraint(), loadExisting);
        loadExisting.setY(yPos + 120 + BUTTON_HEIGHT + BUTTON_GAP);
        loadExisting.setPressedCallback((button, mouseButton) -> {
            if (!importManifestIds.isEmpty()) {
                switchToExistingState("import");
            }
            return true;
        });

        if (importManifestIds.isEmpty()) {
            MKText emptyText = makeWhiteText(Component.literal("No exported workspace manifests found."));
            emptyText.setWidth(CONTENT_WIDTH);
            emptyText.setMultiline(true);
            emptyText.setY(yPos + 120 + ((BUTTON_HEIGHT + BUTTON_GAP) * 2));
            root.addWidget(emptyText);
            root.addConstraintToWidget(new CenterXConstraint(), emptyText);
        }

        return root;
    }

    private MKLayout buildImportState() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Load Existing Workspace"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal("Choose an exported workspace manifest to rehydrate at this dev block."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = BUTTON_HEIGHT + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);
        for (String manifestId : importManifestIds) {
            MKButton manifestButton = new MKButton(Component.literal(manifestId), CONTENT_WIDTH - 8, 20);
            content.addWidget(manifestButton);
            manifestButton.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new LoadWorkspaceFromManifestPacket(anchor, ResourceLocation.parse(manifestId)));
                return true;
            });
        }
        scrollView.addWidget(content);

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        back.setPressedCallback((button, mouseButton) -> {
            switchToExistingState("home");
            return true;
        });
        return root;
    }

    private MKLayout buildFormState() {
        ensureFormDraftInitialized();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Workspace Configuration"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Split the workspace setup into smaller sections. Identity covers naming and margins, vertical access covers shaft sizing and height bands, and materials covers block palette choices."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        MKText summary = makeWhiteText(Component.literal(
                formDraft.namespace + ":" + formDraft.structureName + "  |  " +
                        formDraft.roomWidth + "x" + formDraft.roomLength + " rooms  |  shaft " + formDraft.hallwayWidth +
                        "  |  heights " + formDraft.roomHeight + "/" + formDraft.entranceHeight + "/" + formDraft.basementHeight));
        summary.setWidth(CONTENT_WIDTH);
        summary.setMultiline(true);
        root.addWidget(summary);
        root.addConstraintToWidget(StackConstraint.VERTICAL, summary);
        root.addConstraintToWidget(new CenterXConstraint(), summary);

        int firstButtonY = yPos + 130;
        MKButton identity = new MKButton(Component.literal("Identity & Bounds"), 220, 20);
        root.addWidget(identity);
        root.addConstraintToWidget(new CenterXConstraint(), identity);
        identity.setY(firstButtonY);
        identity.setPressedCallback((button, mouseButton) -> {
            pushState("form_identity");
            flagNeedSetup();
            return true;
        });

        MKButton vertical = new MKButton(Component.literal("Vertical Access"), 220, 20);
        root.addWidget(vertical);
        root.addConstraintToWidget(new CenterXConstraint(), vertical);
        vertical.setY(firstButtonY + BUTTON_HEIGHT + BUTTON_GAP);
        vertical.setPressedCallback((button, mouseButton) -> {
            pushState("form_vertical");
            flagNeedSetup();
            return true;
        });

        MKButton materials = new MKButton(Component.literal("Materials"), 220, 20);
        root.addWidget(materials);
        root.addConstraintToWidget(new CenterXConstraint(), materials);
        materials.setY(firstButtonY + ((BUTTON_HEIGHT + BUTTON_GAP) * 2));
        materials.setPressedCallback((button, mouseButton) -> {
            pushState("form_materials");
            flagNeedSetup();
            return true;
        });

        if (workspace != null && !workspace.pieces().isEmpty()) {
            MKButton backToWorkspace = new MKButton(Component.translatable("mknpc.workspace.button.back_to_workspace"), 180, 20);
            root.addWidget(backToWorkspace);
            root.addConstraintToWidget(new CenterXConstraint(), backToWorkspace);
            backToWorkspace.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
            backToWorkspace.setPressedCallback((button, mouseButton) -> {
                switchToExistingState("workspace");
                return true;
            });
        }

        MKButton generate = new MKButton(Component.translatable("mknpc.workspace.screen.generate"), 200, 20);
        root.addWidget(generate);
        root.addConstraintToWidget(new CenterXConstraint(), generate);
        generate.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        generate.setPressedCallback((button, mouseButton) -> {
            submitWorkspaceDraft();
            return true;
        });

        return root;
    }

    private MKLayout buildFormIdentityState() {
        ensureFormDraftInitialized();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Identity & Bounds"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Configure workspace naming, room footprint, doorway sizing, and export margins."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = BUTTON_HEIGHT + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        MKTextFieldWidget namespaceField = makeField("Namespace", formDraft.namespace);
        namespaceField.setTextChangeCallback((field, text) -> formDraft.namespace = text.trim().isBlank() ? "mkdev" : text.trim());
        MKTextFieldWidget structureNameField = makeField("Structure Name", formDraft.structureName);
        structureNameField.setTextChangeCallback((field, text) -> formDraft.structureName = text.trim().isBlank() ? "tower_workspace" : text.trim());
        MKTextFieldWidget roomWidthField = makeField("Room Width", Integer.toString(formDraft.roomWidth));
        roomWidthField.setTextChangeCallback((field, text) -> {
            formDraft.roomWidth = parseInt(text, formDraft.roomWidth);
            rebaseDraftForFootprintChange();
        });
        MKTextFieldWidget roomLengthField = makeField("Room Length", Integer.toString(formDraft.roomLength));
        roomLengthField.setTextChangeCallback((field, text) -> {
            formDraft.roomLength = parseInt(text, formDraft.roomLength);
            rebaseDraftForFootprintChange();
        });
        MKTextFieldWidget doorWidthField = makeField("Door Width", Integer.toString(formDraft.doorwayWidth));
        doorWidthField.setTextChangeCallback((field, text) -> formDraft.doorwayWidth = parseInt(text, formDraft.doorwayWidth));
        MKTextFieldWidget doorHeightField = makeField("Door Height", Integer.toString(formDraft.doorwayHeight));
        doorHeightField.setTextChangeCallback((field, text) -> formDraft.doorwayHeight = parseInt(text, formDraft.doorwayHeight));
        MKTextFieldWidget shellMarginField = makeField("Shell Margin", Integer.toString(formDraft.shellMargin));
        shellMarginField.setTextChangeCallback((field, text) -> formDraft.shellMargin = parseInt(text, formDraft.shellMargin));
        MKTextFieldWidget exteriorAirMarginField = makeField("Exterior Air Margin", Integer.toString(formDraft.exteriorAirMargin));
        exteriorAirMarginField.setTextChangeCallback((field, text) -> formDraft.exteriorAirMargin = parseInt(text, formDraft.exteriorAirMargin));
        MKTextFieldWidget previewMarginField = makeField("Preview Margin", Integer.toString(formDraft.previewMargin));
        previewMarginField.setTextChangeCallback((field, text) -> formDraft.previewMargin = parseInt(text, formDraft.previewMargin));

        addRow(content, makeLabel("mknpc.workspace.field.namespace"), namespaceField);
        addRow(content, makeLabel("mknpc.workspace.field.structure_name"), structureNameField);
        addRow(content, makeLabel("mknpc.workspace.field.room_width"), roomWidthField);
        addRow(content, makeLabel("mknpc.workspace.field.room_length"), roomLengthField);
        addRow(content, makeLabel("mknpc.workspace.field.door_width"), doorWidthField);
        addRow(content, makeLabel("mknpc.workspace.field.door_height"), doorHeightField);
        addRow(content, makeLabel("mknpc.workspace.field.shell_margin"), shellMarginField);
        addRow(content, makeLabel("mknpc.workspace.field.exterior_air_margin"), exteriorAirMarginField);
        addRow(content, makeLabel("mknpc.workspace.field.preview_margin"), previewMarginField);

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        scrollView.setToTop();

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        back.setPressedCallback((button, mouseButton) -> {
            switchToExistingState("form");
            return true;
        });
        return root;
    }

    private MKLayout buildFormVerticalState() {
        ensureFormDraftInitialized();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Vertical Access"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Adjust the shared shaft size, valid room heights, and stair authoring profile without scanning the rest of the form."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = BUTTON_HEIGHT + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        MKButton hallWidthButton = new MKButton(Component.literal(Integer.toString(formDraft.hallwayWidth)), 180, 20);
        MKButton roomHeightButton = new MKButton(Component.literal(Integer.toString(formDraft.roomHeight)), 180, 20);
        MKButton entranceHeightButton = new MKButton(Component.literal(Integer.toString(formDraft.entranceHeight)), 180, 20);
        MKButton basementHeightButton = new MKButton(Component.literal(Integer.toString(formDraft.basementHeight)), 180, 20);
        MKButton stairPlacementButton = new MKButton(getStairPlacementComponent(formDraft.verticalAccessPlacement), 180, 20);
        MKButton stairModeButton = new MKButton(getStairModeComponent(formDraft.stairMode), 180, 20);
        MKButton stairRiseTypeButton = new MKButton(getStairRiseTypeComponent(formDraft.stairRiseType), 180, 20);
        MKButton flatRunButton = new MKButton(Component.literal(Integer.toString(formDraft.stairFlatRunLength)), 180, 20);
        MKButton stairWidthButton = new MKButton(Component.literal(Integer.toString(formDraft.stairWidth)), 180, 20);

        addRow(content, makeLabel("mknpc.workspace.field.hall_width"), hallWidthButton);
        addRow(content, makeLabel("mknpc.workspace.field.room_height"), roomHeightButton);
        addRow(content, makeLabel("mknpc.workspace.field.entrance_height"), entranceHeightButton);
        addRow(content, makeWhiteText(Component.literal("Basement Height")), basementHeightButton);
        addRow(content, makeLabel("mknpc.workspace.field.stair_placement"), stairPlacementButton);
        addRow(content, makeLabel("mknpc.workspace.field.stair_mode"), stairModeButton);
        addRow(content, makeWhiteText(Component.literal("Rise Type")), stairRiseTypeButton);
        addRow(content, makeWhiteText(Component.literal("Flat Run Length")), flatRunButton);
        addRow(content, makeWhiteText(Component.literal("Stair Width")), stairWidthButton);

        hallWidthButton.setPressedCallback((button, mouseButton) -> {
            formDraft.hallwayWidth = cycleAllowedShaftSize(formDraft.roomWidth, formDraft.roomLength, formDraft.hallwayWidth);
            formDraft.stairWidth = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(formDraft.hallwayWidth, formDraft.stairWidth);
            rebaseDraftVerticalHeights();
            hallWidthButton.buttonText = Component.literal(Integer.toString(formDraft.hallwayWidth));
            stairWidthButton.buttonText = Component.literal(Integer.toString(formDraft.stairWidth));
            roomHeightButton.buttonText = Component.literal(Integer.toString(formDraft.roomHeight));
            entranceHeightButton.buttonText = Component.literal(Integer.toString(formDraft.entranceHeight));
            basementHeightButton.buttonText = Component.literal(Integer.toString(formDraft.basementHeight));
            flatRunButton.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            return true;
        });
        stairPlacementButton.setPressedCallback((button, mouseButton) -> {
            formDraft.verticalAccessPlacement = formDraft.verticalAccessPlacement.next();
            button.buttonText = getStairPlacementComponent(formDraft.verticalAccessPlacement);
            return true;
        });
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            formDraft.stairMode = formDraft.stairMode.next();
            snapDraftVerticalHeights();
            button.buttonText = getStairModeComponent(formDraft.stairMode);
            roomHeightButton.buttonText = Component.literal(Integer.toString(formDraft.roomHeight));
            entranceHeightButton.buttonText = Component.literal(Integer.toString(formDraft.entranceHeight));
            basementHeightButton.buttonText = Component.literal(Integer.toString(formDraft.basementHeight));
            flatRunButton.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            return true;
        });
        stairRiseTypeButton.setPressedCallback((button, mouseButton) -> {
            formDraft.stairRiseType = formDraft.stairRiseType.next();
            snapDraftVerticalHeights();
            button.buttonText = getStairRiseTypeComponent(formDraft.stairRiseType);
            roomHeightButton.buttonText = Component.literal(Integer.toString(formDraft.roomHeight));
            entranceHeightButton.buttonText = Component.literal(Integer.toString(formDraft.entranceHeight));
            basementHeightButton.buttonText = Component.literal(Integer.toString(formDraft.basementHeight));
            flatRunButton.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            return true;
        });
        flatRunButton.setPressedCallback((button, mouseButton) -> {
            formDraft.stairFlatRunLength = cycleAllowedFlatRunLength(makeDraftStairConfig(), formDraft.hallwayWidth,
                    formDraft.roomHeight, formDraft.stairFlatRunLength);
            snapDraftVerticalHeights();
            button.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            roomHeightButton.buttonText = Component.literal(Integer.toString(formDraft.roomHeight));
            entranceHeightButton.buttonText = Component.literal(Integer.toString(formDraft.entranceHeight));
            basementHeightButton.buttonText = Component.literal(Integer.toString(formDraft.basementHeight));
            return true;
        });
        stairWidthButton.setPressedCallback((button, mouseButton) -> {
            formDraft.stairWidth = cycleAllowedStairWidth(formDraft.hallwayWidth, formDraft.stairWidth);
            snapDraftVerticalHeights();
            button.buttonText = Component.literal(Integer.toString(formDraft.stairWidth));
            roomHeightButton.buttonText = Component.literal(Integer.toString(formDraft.roomHeight));
            entranceHeightButton.buttonText = Component.literal(Integer.toString(formDraft.entranceHeight));
            basementHeightButton.buttonText = Component.literal(Integer.toString(formDraft.basementHeight));
            flatRunButton.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            return true;
        });
        roomHeightButton.setPressedCallback((button, mouseButton) -> {
            formDraft.roomHeight = cycleAllowedTowerHeight(makeDraftStairConfig(), formDraft.hallwayWidth, formDraft.roomHeight);
            snapDraftVerticalHeights();
            button.buttonText = Component.literal(Integer.toString(formDraft.roomHeight));
            entranceHeightButton.buttonText = Component.literal(Integer.toString(formDraft.entranceHeight));
            basementHeightButton.buttonText = Component.literal(Integer.toString(formDraft.basementHeight));
            flatRunButton.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            return true;
        });
        entranceHeightButton.setPressedCallback((button, mouseButton) -> {
            formDraft.entranceHeight = cycleAllowedEntranceHeight(makeDraftStairConfig(), formDraft.hallwayWidth,
                    formDraft.roomHeight, formDraft.entranceHeight);
            button.buttonText = Component.literal(Integer.toString(formDraft.entranceHeight));
            return true;
        });
        basementHeightButton.setPressedCallback((button, mouseButton) -> {
            formDraft.basementHeight = cycleAllowedEntranceHeight(makeDraftStairConfig(), formDraft.hallwayWidth,
                    formDraft.roomHeight, formDraft.basementHeight);
            button.buttonText = Component.literal(Integer.toString(formDraft.basementHeight));
            return true;
        });

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        scrollView.setToTop();

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        back.setPressedCallback((button, mouseButton) -> {
            switchToExistingState("form");
            return true;
        });
        return root;
    }

    private MKLayout buildFormMaterialsState() {
        ensureFormDraftInitialized();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Materials"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Edit the room shell and stair palette without the rest of the layout controls in view."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        MKBlockSlot floorSlot = new MKBlockSlot();
        floorSlot.setBlock(formDraft.floorBlock);
        MKBlockSlot wallSlot = new MKBlockSlot();
        wallSlot.setBlock(formDraft.wallBlock);
        MKBlockSlot ceilingSlot = new MKBlockSlot();
        ceilingSlot.setBlock(formDraft.ceilingBlock);
        MKBlockSlot stairBlockSlot = new MKBlockSlot();
        stairBlockSlot.setBlock(formDraft.stairBlock);
        MKBlockSlot slabBlockSlot = new MKBlockSlot();
        slabBlockSlot.setBlock(formDraft.slabBlock);
        MKBlockSlot ladderBlockSlot = new MKBlockSlot();
        ladderBlockSlot.setBlock(formDraft.ladderBlock);
        MKPlayerHotbar hotbar = new MKPlayerHotbar();
        addPaletteSection(root, xPos, yPos + 120, hotbar, floorSlot, wallSlot, ceilingSlot, stairBlockSlot, slabBlockSlot,
                ladderBlockSlot);

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        back.setPressedCallback((button, mouseButton) -> {
            formDraft.floorBlock = floorSlot.getBlockId();
            formDraft.wallBlock = wallSlot.getBlockId();
            formDraft.ceilingBlock = ceilingSlot.getBlockId();
            formDraft.stairBlock = stairBlockSlot.getBlockId();
            formDraft.slabBlock = slabBlockSlot.getBlockId();
            formDraft.ladderBlock = ladderBlockSlot.getBlockId();
            switchToExistingState("form");
            return true;
        });
        return root;
    }

    private MKLayout buildLegacyFormState() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.translatable("mknpc.workspace.screen.title"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.translatable("mknpc.workspace.screen.help"));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonCount = workspace != null && !workspace.pieces().isEmpty() ? 2 : 1;
        int buttonAreaHeight = (buttonCount * BUTTON_HEIGHT) + ((buttonCount - 1) * BUTTON_GAP) + BOTTOM_PADDING;
        int paletteAreaHeight = 74;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - paletteAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        MKText namespaceLabel = makeLabel("mknpc.workspace.field.namespace");
        MKTextFieldWidget namespaceField = makeField("Namespace", valueOrDefault(workspace != null ? workspace.namespace() : null, "mkdev"));
        MKText nameLabel = makeLabel("mknpc.workspace.field.structure_name");
        MKTextFieldWidget structureNameField = makeField("Structure Name", valueOrDefault(workspace != null ? workspace.structureName() : null, "tower_workspace"));

        MKText roomWidthLabel = makeLabel("mknpc.workspace.field.room_width");
        MKTextFieldWidget roomWidthField = makeField("Room Width", Integer.toString(workspace != null ? workspace.dimensions().roomWidth() : 9));
        MKText roomLengthLabel = makeLabel("mknpc.workspace.field.room_length");
        MKTextFieldWidget roomLengthField = makeField("Room Length", Integer.toString(workspace != null ? workspace.dimensions().roomLength() : 9));
        int initialHallWidth = MKWorkspaceDimensions.snapToNearestAllowedShaftSize(
                workspace != null ? workspace.dimensions().roomWidth() : 9,
                workspace != null ? workspace.dimensions().roomLength() : 9,
                workspace != null ? workspace.dimensions().hallwayWidth() : 3);
        MKWorkspaceStairMode initialStairMode = workspace != null ? workspace.stairConfig().mode() : MKWorkspaceStairMode.AUTO;
        MKWorkspaceStairRiseType initialRiseType = workspace != null ? workspace.stairConfig().riseType() : MKWorkspaceStairRiseType.STAIR;
        MKText hallWidthLabel = makeLabel("mknpc.workspace.field.hall_width");
        final int[] selectedShaftSize = {initialHallWidth};
        MKButton hallWidthButton = new MKButton(Component.literal(Integer.toString(selectedShaftSize[0])), 180, 20);
        MKText entranceHeightLabel = makeLabel("mknpc.workspace.field.entrance_height");
        final int[] selectedEntranceHeight = {
                MKWorkspaceDimensions.snapToNearestAllowedEntranceHeight(
                        makeStairConfig(initialStairMode, initialRiseType,
                                workspace != null ? workspace.stairConfig().flatRunLength() : 0,
                                workspace != null ? workspace.stairConfig().stairWidth() : 1,
                                workspace != null ? workspace.stairConfig().stairBlock() : ResourceLocation.parse("minecraft:stone_brick_stairs"),
                                workspace != null ? workspace.stairConfig().slabBlock() : ResourceLocation.parse("minecraft:stone_brick_slab"),
                                workspace != null ? workspace.stairConfig().ladderBlock() : ResourceLocation.parse("minecraft:ladder")),
                        initialHallWidth,
                        workspace != null ? workspace.dimensions().roomHeight() : 5,
                        workspace != null ? workspace.dimensions().entranceHeight() : 7, 3, 4)
        };
        MKButton entranceHeightButton = new MKButton(Component.literal(Integer.toString(selectedEntranceHeight[0])), 180, 20);
        MKText roomHeightLabel = makeLabel("mknpc.workspace.field.room_height");
        final int[] selectedRoomHeight = {
                MKWorkspaceDimensions.snapToNearestAllowedTowerHeight(
                        makeStairConfig(initialStairMode, initialRiseType,
                                workspace != null ? workspace.stairConfig().flatRunLength() : 0,
                                workspace != null ? workspace.stairConfig().stairWidth() : 1,
                                workspace != null ? workspace.stairConfig().stairBlock() : ResourceLocation.parse("minecraft:stone_brick_stairs"),
                                workspace != null ? workspace.stairConfig().slabBlock() : ResourceLocation.parse("minecraft:stone_brick_slab"),
                                workspace != null ? workspace.stairConfig().ladderBlock() : ResourceLocation.parse("minecraft:ladder")),
                        initialHallWidth,
                        workspace != null ? workspace.dimensions().roomHeight() : 5, 3, 4)
        };
        MKButton roomHeightButton = new MKButton(Component.literal(Integer.toString(selectedRoomHeight[0])), 180, 20);
        MKText basementHeightLabel = makeWhiteText(Component.literal("Basement Height"));
        final int[] selectedBasementHeight = {
                MKWorkspaceDimensions.snapToNearestAllowedEntranceHeight(
                        makeStairConfig(initialStairMode, initialRiseType,
                                workspace != null ? workspace.stairConfig().flatRunLength() : 0,
                                workspace != null ? workspace.stairConfig().stairWidth() : 1,
                                workspace != null ? workspace.stairConfig().stairBlock() : ResourceLocation.parse("minecraft:stone_brick_stairs"),
                                workspace != null ? workspace.stairConfig().slabBlock() : ResourceLocation.parse("minecraft:stone_brick_slab"),
                                workspace != null ? workspace.stairConfig().ladderBlock() : ResourceLocation.parse("minecraft:ladder")),
                        initialHallWidth,
                        workspace != null ? workspace.dimensions().roomHeight() : 5,
                        workspace != null ? workspace.dimensions().basementHeight() : 5, 3, 4)
        };
        MKButton basementHeightButton = new MKButton(Component.literal(Integer.toString(selectedBasementHeight[0])), 180, 20);

        MKText doorWidthLabel = makeLabel("mknpc.workspace.field.door_width");
        MKTextFieldWidget doorWidthField = makeField("Door Width", Integer.toString(workspace != null ? workspace.dimensions().doorwayWidth() : 3));
        MKText doorHeightLabel = makeLabel("mknpc.workspace.field.door_height");
        MKTextFieldWidget doorHeightField = makeField("Door Height", Integer.toString(workspace != null ? workspace.dimensions().doorwayHeight() : 3));

        MKText shellMarginLabel = makeLabel("mknpc.workspace.field.shell_margin");
        MKTextFieldWidget shellMarginField = makeField("Shell Margin", Integer.toString(workspace != null ? workspace.shellMargin() : 1));
        MKText exteriorAirMarginLabel = makeLabel("mknpc.workspace.field.exterior_air_margin");
        MKTextFieldWidget exteriorAirMarginField = makeField("Exterior Air Margin", Integer.toString(workspace != null ? workspace.exteriorAirMargin() : 2));
        MKText stairPlacementLabel = makeLabel("mknpc.workspace.field.stair_placement");
        final MKVerticalAccessPlacement[] selectedPlacement = {
                workspace != null ? workspace.verticalAccessPlacement() : MKVerticalAccessPlacement.CENTER
        };
        MKButton stairPlacementButton = new MKButton(getStairPlacementComponent(selectedPlacement[0]), 180, 20);
        MKText stairModeLabel = makeLabel("mknpc.workspace.field.stair_mode");
        final MKWorkspaceStairMode[] selectedStairMode = {
                workspace != null ? workspace.stairConfig().mode() : MKWorkspaceStairMode.AUTO
        };
        MKButton stairModeButton = new MKButton(getStairModeComponent(selectedStairMode[0]), 180, 20);
        final MKWorkspaceStairRiseType[] selectedRiseType = {
                workspace != null ? workspace.stairConfig().riseType() : MKWorkspaceStairRiseType.STAIR
        };
        MKText stairRiseTypeLabel = makeWhiteText(Component.literal("Rise Type"));
        MKButton stairRiseTypeButton = new MKButton(getStairRiseTypeComponent(selectedRiseType[0]), 180, 20);
        final int[] selectedFlatRunLength = {
                MKWorkspaceDimensions.snapToNearestAllowedFlatRunLength(
                        makeStairConfig(initialStairMode, initialRiseType,
                                workspace != null ? workspace.stairConfig().flatRunLength() : 0,
                                workspace != null ? workspace.stairConfig().stairWidth() : 1,
                                workspace != null ? workspace.stairConfig().stairBlock() : ResourceLocation.parse("minecraft:stone_brick_stairs"),
                                workspace != null ? workspace.stairConfig().slabBlock() : ResourceLocation.parse("minecraft:stone_brick_slab"),
                                workspace != null ? workspace.stairConfig().ladderBlock() : ResourceLocation.parse("minecraft:ladder")),
                        initialHallWidth,
                        selectedRoomHeight[0],
                        workspace != null ? workspace.stairConfig().flatRunLength() : 0,
                        4)
        };
        MKText flatRunLabel = makeWhiteText(Component.literal("Flat Run Length"));
        MKButton flatRunButton = new MKButton(Component.literal(Integer.toString(selectedFlatRunLength[0])), 180, 20);
        final int[] selectedStairWidth = {MKWorkspaceDimensions.snapToNearestAllowedStairWidth(
                selectedShaftSize[0],
                workspace != null ? workspace.stairConfig().stairWidth() : 1)};
        MKText stairWidthLabel = makeWhiteText(Component.literal("Stair Width"));
        MKButton stairWidthButton = new MKButton(Component.literal(Integer.toString(selectedStairWidth[0])), 180, 20);
        MKText previewMarginLabel = makeLabel("mknpc.workspace.field.preview_margin");
        MKTextFieldWidget previewMarginField = makeField("Preview Margin", Integer.toString(workspace != null ? workspace.previewMargin() : 4));
        MKBlockSlot floorSlot = new MKBlockSlot();
        floorSlot.setBlock(workspace != null ? workspace.palette().floorBlock() : ResourceLocation.parse("minecraft:smooth_stone"));
        MKBlockSlot wallSlot = new MKBlockSlot();
        wallSlot.setBlock(workspace != null ? workspace.palette().wallBlock() : ResourceLocation.parse("minecraft:stone_bricks"));
        MKBlockSlot ceilingSlot = new MKBlockSlot();
        ceilingSlot.setBlock(workspace != null ? workspace.palette().ceilingBlock() : ResourceLocation.parse("minecraft:smooth_stone"));
        MKBlockSlot stairBlockSlot = new MKBlockSlot();
        stairBlockSlot.setBlock(workspace != null ? workspace.stairConfig().stairBlock() : ResourceLocation.parse("minecraft:stone_brick_stairs"));
        MKBlockSlot slabBlockSlot = new MKBlockSlot();
        slabBlockSlot.setBlock(workspace != null ? workspace.stairConfig().slabBlock() : ResourceLocation.parse("minecraft:stone_brick_slab"));
        MKBlockSlot ladderBlockSlot = new MKBlockSlot();
        ladderBlockSlot.setBlock(workspace != null ? workspace.stairConfig().ladderBlock() : ResourceLocation.parse("minecraft:ladder"));
        MKPlayerHotbar hotbar = new MKPlayerHotbar();
        addRow(content, namespaceLabel, namespaceField);
        addRow(content, nameLabel, structureNameField);
        addRow(content, roomWidthLabel, roomWidthField);
        addRow(content, roomLengthLabel, roomLengthField);
        addRow(content, hallWidthLabel, hallWidthButton);
        addRow(content, roomHeightLabel, roomHeightButton);
        addRow(content, entranceHeightLabel, entranceHeightButton);
        addRow(content, basementHeightLabel, basementHeightButton);
        addRow(content, doorWidthLabel, doorWidthField);
        addRow(content, doorHeightLabel, doorHeightField);
        addRow(content, shellMarginLabel, shellMarginField);
        addRow(content, exteriorAirMarginLabel, exteriorAirMarginField);
        addRow(content, stairPlacementLabel, stairPlacementButton);
        addRow(content, stairModeLabel, stairModeButton);
        addRow(content, stairRiseTypeLabel, stairRiseTypeButton);
        addRow(content, flatRunLabel, flatRunButton);
        addRow(content, stairWidthLabel, stairWidthButton);
        addRow(content, previewMarginLabel, previewMarginField);


        Runnable rebaseForFootprintChange = () -> {
            selectedShaftSize[0] = MKWorkspaceDimensions.getAllowedShaftSizes(
                    parseInt(roomWidthField, 9),
                    parseInt(roomLengthField, 9)
            ).getFirst();
            hallWidthButton.buttonText = Component.literal(Integer.toString(selectedShaftSize[0]));
            selectedStairWidth[0] = MKWorkspaceDimensions.getAllowedStairWidths(selectedShaftSize[0]).getFirst();
            stairWidthButton.buttonText = Component.literal(Integer.toString(selectedStairWidth[0]));
            rebaseDependentHeightsToMinimum(
                    selectedShaftSize[0],
                    selectedStairMode[0],
                    selectedRiseType[0],
                    selectedFlatRunLength,
                    selectedStairWidth[0],
                    stairBlockSlot.getBlockId(),
                    slabBlockSlot.getBlockId(),
                    ladderBlockSlot.getBlockId(),
                    selectedRoomHeight,
                    roomHeightButton,
                    selectedEntranceHeight,
                    entranceHeightButton,
                    selectedBasementHeight,
                    basementHeightButton,
                    flatRunButton
            );
        };
        roomWidthField.setTextChangeCallback((field, text) -> rebaseForFootprintChange.run());
        roomLengthField.setTextChangeCallback((field, text) -> rebaseForFootprintChange.run());
        hallWidthButton.setPressedCallback((button, mouseButton) -> {
            selectedShaftSize[0] = cycleAllowedShaftSize(parseInt(roomWidthField, 9), parseInt(roomLengthField, 9),
                    selectedShaftSize[0]);
            button.buttonText = Component.literal(Integer.toString(selectedShaftSize[0]));
            rebaseDependentHeightsToMinimum(
                    selectedShaftSize[0],
                    selectedStairMode[0],
                    selectedRiseType[0],
                    selectedFlatRunLength,
                    selectedStairWidth[0],
                    stairBlockSlot.getBlockId(),
                    slabBlockSlot.getBlockId(),
                    ladderBlockSlot.getBlockId(),
                    selectedRoomHeight,
                    roomHeightButton,
                    selectedEntranceHeight,
                    entranceHeightButton,
                    selectedBasementHeight,
                    basementHeightButton,
                    flatRunButton
            );
            return true;
        });

        stairPlacementButton.setPressedCallback((button, mouseButton) -> {
            selectedPlacement[0] = selectedPlacement[0].next();
            button.buttonText = getStairPlacementComponent(selectedPlacement[0]);
            return true;
        });
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            selectedStairMode[0] = selectedStairMode[0].next();
            button.buttonText = getStairModeComponent(selectedStairMode[0]);
            var config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = snapTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            selectedFlatRunLength[0] = snapAllowedFlatRunLength(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedFlatRunLength[0]);
            flatRunButton.buttonText = Component.literal(Integer.toString(selectedFlatRunLength[0]));
            config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = snapTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            roomHeightButton.buttonText = Component.literal(Integer.toString(selectedRoomHeight[0]));
            selectedEntranceHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedEntranceHeight[0]);
            entranceHeightButton.buttonText = Component.literal(Integer.toString(selectedEntranceHeight[0]));
            selectedBasementHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedBasementHeight[0]);
            basementHeightButton.buttonText = Component.literal(Integer.toString(selectedBasementHeight[0]));
            return true;
        });
        stairRiseTypeButton.setPressedCallback((button, mouseButton) -> {
            selectedRiseType[0] = selectedRiseType[0].next();
            button.buttonText = getStairRiseTypeComponent(selectedRiseType[0]);
            var config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = snapTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            selectedFlatRunLength[0] = snapAllowedFlatRunLength(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedFlatRunLength[0]);
            flatRunButton.buttonText = Component.literal(Integer.toString(selectedFlatRunLength[0]));
            config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = snapTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            roomHeightButton.buttonText = Component.literal(Integer.toString(selectedRoomHeight[0]));
            selectedEntranceHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedEntranceHeight[0]);
            entranceHeightButton.buttonText = Component.literal(Integer.toString(selectedEntranceHeight[0]));
            selectedBasementHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedBasementHeight[0]);
            basementHeightButton.buttonText = Component.literal(Integer.toString(selectedBasementHeight[0]));
            return true;
        });
        flatRunButton.setPressedCallback((button, mouseButton) -> {
            selectedFlatRunLength[0] = cycleAllowedFlatRunLength(
                    makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                            stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId()),
                    selectedShaftSize[0],
                    selectedRoomHeight[0],
                    selectedFlatRunLength[0]);
            button.buttonText = Component.literal(Integer.toString(selectedFlatRunLength[0]));
            var config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = snapTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            roomHeightButton.buttonText = Component.literal(Integer.toString(selectedRoomHeight[0]));
            selectedEntranceHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedEntranceHeight[0]);
            entranceHeightButton.buttonText = Component.literal(Integer.toString(selectedEntranceHeight[0]));
            selectedBasementHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedBasementHeight[0]);
            basementHeightButton.buttonText = Component.literal(Integer.toString(selectedBasementHeight[0]));
            return true;
        });
        stairWidthButton.setPressedCallback((button, mouseButton) -> {
            selectedStairWidth[0] = cycleAllowedStairWidth(selectedShaftSize[0], selectedStairWidth[0]);
            button.buttonText = Component.literal(Integer.toString(selectedStairWidth[0]));
            var config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = snapTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            selectedFlatRunLength[0] = snapAllowedFlatRunLength(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedFlatRunLength[0]);
            flatRunButton.buttonText = Component.literal(Integer.toString(selectedFlatRunLength[0]));
            config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = snapTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            roomHeightButton.buttonText = Component.literal(Integer.toString(selectedRoomHeight[0]));
            selectedEntranceHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedEntranceHeight[0]);
            entranceHeightButton.buttonText = Component.literal(Integer.toString(selectedEntranceHeight[0]));
            selectedBasementHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedBasementHeight[0]);
            basementHeightButton.buttonText = Component.literal(Integer.toString(selectedBasementHeight[0]));
            return true;
        });
        entranceHeightButton.setPressedCallback((button, mouseButton) -> {
            selectedEntranceHeight[0] = cycleAllowedEntranceHeight(
                    makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                            stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId()),
                    selectedShaftSize[0],
                    selectedRoomHeight[0],
                    selectedEntranceHeight[0]);
            button.buttonText = Component.literal(Integer.toString(selectedEntranceHeight[0]));
            return true;
        });
        roomHeightButton.setPressedCallback((button, mouseButton) -> {
            var config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = cycleAllowedTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            selectedFlatRunLength[0] = snapAllowedFlatRunLength(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedFlatRunLength[0]);
            flatRunButton.buttonText = Component.literal(Integer.toString(selectedFlatRunLength[0]));
            config = makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                    stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId());
            selectedRoomHeight[0] = snapTowerHeight(config, selectedShaftSize[0], selectedRoomHeight[0]);
            button.buttonText = Component.literal(Integer.toString(selectedRoomHeight[0]));
            selectedEntranceHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedEntranceHeight[0]);
            entranceHeightButton.buttonText = Component.literal(Integer.toString(selectedEntranceHeight[0]));
            selectedBasementHeight[0] = snapEntranceHeight(config, selectedShaftSize[0], selectedRoomHeight[0],
                    selectedBasementHeight[0]);
            basementHeightButton.buttonText = Component.literal(Integer.toString(selectedBasementHeight[0]));
            return true;
        });
        basementHeightButton.setPressedCallback((button, mouseButton) -> {
            selectedBasementHeight[0] = cycleAllowedEntranceHeight(
                    makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0], selectedStairWidth[0],
                            stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(), ladderBlockSlot.getBlockId()),
                    selectedShaftSize[0],
                    selectedRoomHeight[0],
                    selectedBasementHeight[0]);
            button.buttonText = Component.literal(Integer.toString(selectedBasementHeight[0]));
            return true;
        });

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        scrollView.setToTop();

        int paletteTop = yPos + TOP_CONTENT_Y + scrollHeight + 6;
        addPaletteSection(root, xPos, paletteTop, hotbar, floorSlot, wallSlot, ceilingSlot, stairBlockSlot, slabBlockSlot,
                ladderBlockSlot);

        if (workspace != null && !workspace.pieces().isEmpty()) {
            MKButton backToWorkspace = new MKButton(Component.translatable("mknpc.workspace.button.back_to_workspace"), 180, 20);
            root.addWidget(backToWorkspace);
            root.addConstraintToWidget(new CenterXConstraint(), backToWorkspace);
            backToWorkspace.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
            backToWorkspace.setPressedCallback((button, mouseButton) -> {
                switchToExistingState("workspace");
                return true;
            });
        }

        MKButton generate = new MKButton(Component.translatable("mknpc.workspace.screen.generate"), 180, 20);
        root.addWidget(generate);
        root.addConstraintToWidget(new CenterXConstraint(), generate);
        generate.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        generate.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new CreateWorkspacePacket(
                    anchor,
                    namespaceField.getText().trim(),
                    structureNameField.getText().trim(),
                    parseInt(roomWidthField, 9),
                    parseInt(roomLengthField, 9),
                    snapEntranceHeight(makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0],
                                    selectedStairWidth[0], stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(),
                                    ladderBlockSlot.getBlockId()),
                            selectedShaftSize[0], selectedRoomHeight[0], selectedEntranceHeight[0]),
                    snapTowerHeight(makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0],
                                    selectedStairWidth[0], stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(),
                                    ladderBlockSlot.getBlockId()),
                            selectedShaftSize[0], selectedRoomHeight[0]),
                    snapEntranceHeight(makeStairConfig(selectedStairMode[0], selectedRiseType[0], selectedFlatRunLength[0],
                                    selectedStairWidth[0], stairBlockSlot.getBlockId(), slabBlockSlot.getBlockId(),
                                    ladderBlockSlot.getBlockId()),
                            selectedShaftSize[0], selectedRoomHeight[0], selectedBasementHeight[0]),
                    selectedShaftSize[0],
                    parseInt(doorWidthField, 3),
                    parseInt(doorHeightField, 3),
                    selectedPlacement[0],
                    parseInt(shellMarginField, 1),
                    parseInt(exteriorAirMarginField, 2),
                    parseInt(previewMarginField, 4),
                    selectedStairMode[0],
                    selectedRiseType[0],
                    selectedFlatRunLength[0],
                    selectedStairWidth[0],
                    floorSlot.getBlockId(),
                    wallSlot.getBlockId(),
                    ceilingSlot.getBlockId(),
                    stairBlockSlot.getBlockId(),
                    slabBlockSlot.getBlockId(),
                    ladderBlockSlot.getBlockId()
            ));
            PacketDistributor.sendToServer(new GenerateWorkspacePacket(anchor));
            return true;
        });

        return root;
    }

    private MKLayout buildWorkspaceState() {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.translatable("mknpc.workspace.screen.manage_title"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText summary = makeWhiteText(Component.translatable("mknpc.workspace.screen.manage_summary",
                workspace.namespace(), workspace.structureName(), workspace.pieces().size()));
        summary.setWidth(CONTENT_WIDTH);
        summary.setMultiline(true);
        root.addWidget(summary);
        root.addConstraintToWidget(StackConstraint.VERTICAL, summary);
        root.addConstraintToWidget(new CenterXConstraint(), summary);

        int buttonCount = 4;
        int buttonAreaHeight = (buttonCount * BUTTON_HEIGHT) + ((buttonCount - 1) * BUTTON_GAP) + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 8;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        if (workspace.pieces().stream().anyMatch(this::supportsStairGeneration)) {
            MKButton generateAllStairs = new MKButton(Component.literal("Generate All Stairs"), 180, 20);
            content.addWidget(generateAllStairs);
            content.addConstraintToWidget(new CenterXConstraint(), generateAllStairs);
            generateAllStairs.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new GenerateAllWorkspaceStairsPacket(anchor));
                return true;
            });
        }

        for (Map.Entry<String, List<MKWorkspacePieceDefinition>> entry : groupPiecesByTopology().entrySet()) {
            String topologyKey = entry.getKey();
            List<MKWorkspacePieceDefinition> pieces = entry.getValue();
            MKWorkspacePieceDefinition templatePiece = pieces.stream()
                    .filter(piece -> piece.variantIndex() == 0)
                    .findFirst()
                    .orElse(pieces.get(0));

            MKText header = makeWhiteText(Component.translatable("mknpc.workspace.section.header",
                    formatTopologyLabel(topologyKey), getBaseName(templatePiece)));
            header.setWidth(CONTENT_WIDTH);
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            int variantCount = countVariants(pieces);
            long generatedCount = pieces.stream().filter(this::hasGeneratedStairs).count();
            MKText details = makeWhiteText(Component.literal(
                    pieces.size() + " piece" + (pieces.size() == 1 ? "" : "s") + " - " +
                            variantCount + " variant" + (variantCount == 1 ? "" : "s") +
                            (supportsStairGeneration(pieces) ? " - stairs " + generatedCount + "/" + pieces.size() : "")));
            details.setWidth(CONTENT_WIDTH);
            content.addWidget(details);
            content.addConstraintToWidget(MarginConstraint.LEFT, details);

            MKButton openCategory = new MKButton(Component.literal("Open Category"), 180, 20);
            content.addWidget(openCategory);
            content.addConstraintToWidget(new CenterXConstraint(), openCategory);
            openCategory.setPressedCallback((button, mouseButton) -> {
                selectedTopologyKey = topologyKey;
                resetCategoryOverrides();
                pushState("category");
                flagNeedSetup();
                return true;
            });
        }

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        scrollView.setToTop();

        MKButton close = new MKButton(Component.translatable("mknpc.workspace.button.close"), 120, 20);
        root.addWidget(close);
        root.addConstraintToWidget(new CenterXConstraint(), close);
        close.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        close.setPressedCallback((button, mouseButton) -> {
            onClose();
            return true;
        });

        MKButton addCopyForAll = new MKButton(Component.translatable("mknpc.workspace.button.add_copy_for_all"), 180, 20);
        root.addWidget(addCopyForAll);
        root.addConstraintToWidget(new CenterXConstraint(), addCopyForAll);
        addCopyForAll.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
        addCopyForAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new AddWorkspaceVariantsForAllPacket(anchor));
            return true;
        });

        MKButton exportAll = new MKButton(Component.translatable("mknpc.workspace.button.export_all"), 180, 20);
        root.addWidget(exportAll);
        root.addConstraintToWidget(new CenterXConstraint(), exportAll);
        exportAll.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT
                - BUTTON_GAP - BUTTON_HEIGHT);
        exportAll.setPressedCallback((button, mouseButton) -> {
            PacketDistributor.sendToServer(new ExportWorkspacePiecesPacket(anchor));
            return true;
        });

        MKButton editTemplates = new MKButton(Component.translatable("mknpc.workspace.button.edit_template_settings"), 180, 20);
        root.addWidget(editTemplates);
        root.addConstraintToWidget(new CenterXConstraint(), editTemplates);
        editTemplates.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT
                - BUTTON_GAP - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
        editTemplates.setPressedCallback((button, mouseButton) -> {
            pushState("form");
            flagNeedSetup();
            return true;
        });

        return root;
    }

    private MKLayout buildCategoryState() {
        if (selectedTopologyKey == null) {
            switchToExistingState("workspace");
            return buildWorkspaceState();
        }
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        List<MKWorkspacePieceDefinition> pieces = groupPiecesByTopology().getOrDefault(selectedTopologyKey, List.of());
        if (pieces.isEmpty()) {
            selectedTopologyKey = null;
            switchToExistingState("workspace");
            return buildWorkspaceState();
        }
        boolean stairCategory = supportsStairGeneration(pieces);
        MKWorkspacePieceDefinition templatePiece = pieces.stream()
                .filter(piece -> piece.variantIndex() == 0)
                .findFirst()
                .orElse(pieces.get(0));
        int categoryHeight = templatePiece.effectiveDimensions().roomHeight();

        ensureCategoryOverridesInitialized();
        detailStairWidth = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(workspace.dimensions().hallwayWidth(), detailStairWidth);
        detailFlatRunLength = snapAllowedFlatRunLength(
                makeStairConfig(detailStairMode, detailStairRiseType, detailFlatRunLength, detailStairWidth,
                        detailStairBlock, detailSlabBlock, detailLadderBlock),
                workspace.dimensions().hallwayWidth(),
                categoryHeight,
                detailFlatRunLength);

        MKText title = makeWhiteText(Component.literal(formatTopologyLabel(selectedTopologyKey)));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText summary = makeWhiteText(Component.literal(stairCategory
                ? "Manage variants and generate stairs into the shaft for an exact template or variant."
                : "Manage variants for this template category."));
        summary.setWidth(CONTENT_WIDTH);
        summary.setMultiline(true);
        root.addWidget(summary);
        root.addConstraintToWidget(StackConstraint.VERTICAL, summary);
        root.addConstraintToWidget(new CenterXConstraint(), summary);

        int buttonCount = stairCategory ? 3 : 2;
        int buttonAreaHeight = (buttonCount * BUTTON_HEIGHT) + BUTTON_GAP + BOTTOM_PADDING;
        int paletteAreaHeight = stairCategory ? 74 : 0;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - paletteAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        if (stairCategory) {
            MKText stairModeLabel = makeLabel("mknpc.workspace.field.stair_mode");
            MKButton stairModeButton = new MKButton(getStairModeComponent(detailStairMode), 180, 20);
            MKText runLengthLabel = makeWhiteText(Component.literal("Flat Run Length"));
            MKButton runLengthButton = new MKButton(Component.literal(Integer.toString(detailFlatRunLength)), 180, 20);
            addRow(content, stairModeLabel, stairModeButton);
            stairModeButton.setPressedCallback((button, mouseButton) -> {
                detailStairMode = detailStairMode.next();
                detailFlatRunLength = snapAllowedFlatRunLength(
                        makeStairConfig(detailStairMode, detailStairRiseType, detailFlatRunLength, detailStairWidth,
                                detailStairBlock, detailSlabBlock, detailLadderBlock),
                        workspace.dimensions().hallwayWidth(),
                        categoryHeight,
                        detailFlatRunLength);
                button.buttonText = getStairModeComponent(detailStairMode);
                runLengthButton.buttonText = Component.literal(Integer.toString(detailFlatRunLength));
                return true;
            });
            MKText riseTypeLabel = makeWhiteText(Component.literal("Rise Type"));
            MKButton riseTypeButton = new MKButton(getStairRiseTypeComponent(detailStairRiseType), 180, 20);
            addRow(content, riseTypeLabel, riseTypeButton);
            riseTypeButton.setPressedCallback((button, mouseButton) -> {
                detailStairRiseType = detailStairRiseType.next();
                detailFlatRunLength = snapAllowedFlatRunLength(
                        makeStairConfig(detailStairMode, detailStairRiseType, detailFlatRunLength, detailStairWidth,
                                detailStairBlock, detailSlabBlock, detailLadderBlock),
                        workspace.dimensions().hallwayWidth(),
                        categoryHeight,
                        detailFlatRunLength);
                button.buttonText = getStairRiseTypeComponent(detailStairRiseType);
                runLengthButton.buttonText = Component.literal(Integer.toString(detailFlatRunLength));
                return true;
            });
            addRow(content, runLengthLabel, runLengthButton);
            runLengthButton.setPressedCallback((button, mouseButton) -> {
                detailFlatRunLength = cycleAllowedFlatRunLength(
                        makeStairConfig(detailStairMode, detailStairRiseType, detailFlatRunLength, detailStairWidth,
                                detailStairBlock, detailSlabBlock, detailLadderBlock),
                        workspace.dimensions().hallwayWidth(),
                        categoryHeight,
                        detailFlatRunLength);
                button.buttonText = Component.literal(Integer.toString(detailFlatRunLength));
                return true;
            });
            MKText widthLabel = makeWhiteText(Component.literal("Stair Width"));
            MKButton widthButton = new MKButton(Component.literal(Integer.toString(detailStairWidth)), 180, 20);
            addRow(content, widthLabel, widthButton);
            widthButton.setPressedCallback((button, mouseButton) -> {
                detailStairWidth = cycleAllowedStairWidth(workspace.dimensions().hallwayWidth(), detailStairWidth);
                detailFlatRunLength = snapAllowedFlatRunLength(
                        makeStairConfig(detailStairMode, detailStairRiseType, detailFlatRunLength, detailStairWidth,
                                detailStairBlock, detailSlabBlock, detailLadderBlock),
                        workspace.dimensions().hallwayWidth(),
                        categoryHeight,
                        detailFlatRunLength);
                button.buttonText = Component.literal(Integer.toString(detailStairWidth));
                runLengthButton.buttonText = Component.literal(Integer.toString(detailFlatRunLength));
                return true;
            });
        }

        for (MKWorkspacePieceDefinition piece : pieces) {
            MKText pieceText = makeWhiteText(Component.literal(describePiece(piece)));
            pieceText.setWidth(CONTENT_WIDTH);
            content.addWidget(pieceText);
            content.addConstraintToWidget(MarginConstraint.LEFT, pieceText);

            if (supportsStairGeneration(piece)) {
                MKText stairStatus = makeWhiteText(Component.literal("Stairs: " +
                        (hasGeneratedStairs(piece) ? "Generated" : "Not Generated")));
                stairStatus.setWidth(CONTENT_WIDTH);
                content.addWidget(stairStatus);
                content.addConstraintToWidget(MarginConstraint.LEFT, stairStatus);
            }

            if (stairCategory && supportsStairGeneration(piece)) {
                String pieceName = piece.pieceName();
                MKButton generateStairs = new MKButton(Component.literal("Generate Stairs"), 180, 20);
                content.addWidget(generateStairs);
                content.addConstraintToWidget(new CenterXConstraint(), generateStairs);
                generateStairs.setPressedCallback((button, mouseButton) -> {
                    PacketDistributor.sendToServer(new GenerateWorkspaceStairsPacket(anchor, pieceName, detailStairMode,
                            detailStairRiseType, detailFlatRunLength, detailStairWidth, detailStairBlock,
                            detailSlabBlock, detailLadderBlock));
                    return true;
                });

                MKButton clearStairs = new MKButton(Component.literal("Clear Stairs"), 180, 20);
                content.addWidget(clearStairs);
                content.addConstraintToWidget(new CenterXConstraint(), clearStairs);
                clearStairs.setPressedCallback((button, mouseButton) -> {
                    PacketDistributor.sendToServer(new ClearWorkspaceStairsPacket(anchor, pieceName));
                    return true;
                });
            }
        }

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        scrollView.setToTop();

        if (stairCategory) {
            String baseName = getBaseName(templatePiece);
            int paletteTop = yPos + TOP_CONTENT_Y + scrollHeight + 6;
            MKPlayerHotbar hotbar = new MKPlayerHotbar();
            MKBlockSlot stairBlockSlot = new MKBlockSlot();
            stairBlockSlot.setBlock(detailStairBlock);
            MKBlockSlot slabBlockSlot = new MKBlockSlot();
            slabBlockSlot.setBlock(detailSlabBlock);
            MKBlockSlot ladderBlockSlot = new MKBlockSlot();
            ladderBlockSlot.setBlock(detailLadderBlock);
            addPaletteSection(root, xPos, paletteTop, hotbar, null, null, null, stairBlockSlot, slabBlockSlot, ladderBlockSlot);

            MKButton back = new MKButton(Component.literal("Back"), 120, 20);
            root.addWidget(back);
            root.addConstraintToWidget(new CenterXConstraint(), back);
            back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
            back.setPressedCallback((button, mouseButton) -> {
                detailStairBlock = stairBlockSlot.getBlockId();
                detailSlabBlock = slabBlockSlot.getBlockId();
                detailLadderBlock = ladderBlockSlot.getBlockId();
                selectedTopologyKey = null;
                switchToExistingState("workspace");
                return true;
            });

            MKButton reset = new MKButton(Component.literal("Use Workspace Defaults"), 180, 20);
            root.addWidget(reset);
            root.addConstraintToWidget(new CenterXConstraint(), reset);
            reset.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
            reset.setPressedCallback((button, mouseButton) -> {
                resetCategoryOverrides();
                flagNeedSetup();
                return true;
            });

            MKButton addCopy = new MKButton(Component.translatable("mknpc.workspace.button.add_copy"), 180, 20);
            root.addWidget(addCopy);
            root.addConstraintToWidget(new CenterXConstraint(), addCopy);
            addCopy.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT
                    - BUTTON_GAP - BUTTON_HEIGHT);
            addCopy.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(anchor, baseName));
                return true;
            });
        } else {
            String baseName = getBaseName(templatePiece);

            MKButton back = new MKButton(Component.literal("Back"), 120, 20);
            root.addWidget(back);
            root.addConstraintToWidget(new CenterXConstraint(), back);
            back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
            back.setPressedCallback((button, mouseButton) -> {
                selectedTopologyKey = null;
                switchToExistingState("workspace");
                return true;
            });

            MKButton addCopy = new MKButton(Component.translatable("mknpc.workspace.button.add_copy"), 180, 20);
            root.addWidget(addCopy);
            root.addConstraintToWidget(new CenterXConstraint(), addCopy);
            addCopy.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
            addCopy.setPressedCallback((button, mouseButton) -> {
                PacketDistributor.sendToServer(new AddWorkspaceVariantPacket(anchor, baseName));
                return true;
            });
        }

        return root;
    }

    private void addRow(MKStackLayoutVertical root, MKText label, MKTextFieldWidget field) {
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(field);
        root.addConstraintToWidget(new CenterXConstraint(), field);
    }

    private void addRow(MKStackLayoutVertical root, MKText label, MKButton button) {
        root.addWidget(label);
        root.addConstraintToWidget(MarginConstraint.LEFT, label);
        root.addWidget(button);
        root.addConstraintToWidget(new CenterXConstraint(), button);
    }

    private MKText makeLabel(String translationKey) {
        MKText text = makeWhiteText(Component.translatable(translationKey));
        text.setWidth(CONTENT_WIDTH);
        return text;
    }

    private MKText makeWhiteText(Component text) {
        return new MKText(font, text).setColor(TEXT_COLOR);
    }

    private void addPaletteSection(MKLayout root, int xPos, int paletteTop, MKPlayerHotbar hotbar,
                                   MKBlockSlot floorSlot, MKBlockSlot wallSlot, MKBlockSlot ceilingSlot,
                                   MKBlockSlot stairBlockSlot, MKBlockSlot slabBlockSlot, MKBlockSlot ladderBlockSlot) {
        MKText hotbarLabel = makeLabel("mknpc.workspace.field.hotbar");
        hotbarLabel.setWidth(CONTENT_WIDTH);
        hotbarLabel.setY(paletteTop);
        root.addWidget(hotbarLabel);
        root.addConstraintToWidget(new CenterXConstraint(), hotbarLabel);

        hotbar.setY(paletteTop + 12);
        root.addWidget(hotbar);
        root.addConstraintToWidget(new CenterXConstraint(), hotbar);

        int firstRowY = paletteTop + 36;
        int slotGroupWidth = (3 * 18) + (2 * 20);
        int firstRowX = xPos + (PANEL_WIDTH / 2) - slotGroupWidth - 14;
        int secondRowX = xPos + (PANEL_WIDTH / 2) + 14;

        if (floorSlot != null && wallSlot != null && ceilingSlot != null) {
            addSlotWithLabel(root, floorSlot, "Floor", firstRowX, firstRowY);
            addSlotWithLabel(root, wallSlot, "Wall", firstRowX + 38, firstRowY);
            addSlotWithLabel(root, ceilingSlot, "Ceiling", firstRowX + 76, firstRowY);
        }
        addSlotWithLabel(root, stairBlockSlot, "Stair", secondRowX, firstRowY);
        addSlotWithLabel(root, slabBlockSlot, "Slab", secondRowX + 38, firstRowY);
        addSlotWithLabel(root, ladderBlockSlot, "Ladder", secondRowX + 76, firstRowY);
    }

    private void addSlotWithLabel(MKLayout root, MKBlockSlot slot, String label, int x, int y) {
        MKText slotLabel = makeWhiteText(Component.literal(label));
        slotLabel.setY(y);
        slotLabel.setX(x);
        root.addWidget(slotLabel);
        slot.setX(x);
        slot.setY(y + 10);
        root.addWidget(slot);
    }

    private MKTextFieldWidget makeField(String label, String value) {
        MKTextFieldWidget widget = new MKTextFieldWidget(font, 0, 0, 180, 18, Component.literal(label));
        widget.setText(value);
        return widget;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private int parseInt(MKTextFieldWidget field, int fallback) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private void ensureFormDraftInitialized() {
        if (formDraft != null) {
            return;
        }
        formDraft = new WorkspaceFormDraft();
        formDraft.namespace = valueOrDefault(workspace != null ? workspace.namespace() : null, "mkdev");
        formDraft.structureName = valueOrDefault(workspace != null ? workspace.structureName() : null, "tower_workspace");
        formDraft.roomWidth = workspace != null ? workspace.dimensions().roomWidth() : 9;
        formDraft.roomLength = workspace != null ? workspace.dimensions().roomLength() : 9;
        formDraft.stairMode = workspace != null ? workspace.stairConfig().mode() : MKWorkspaceStairMode.AUTO;
        formDraft.stairRiseType = workspace != null ? workspace.stairConfig().riseType() : MKWorkspaceStairRiseType.STAIR;
        formDraft.stairFlatRunLength = workspace != null ? workspace.stairConfig().flatRunLength() : 0;
        formDraft.stairWidth = workspace != null ? workspace.stairConfig().stairWidth() : 1;
        formDraft.stairBlock = workspace != null ? workspace.stairConfig().stairBlock() : ResourceLocation.parse("minecraft:stone_brick_stairs");
        formDraft.slabBlock = workspace != null ? workspace.stairConfig().slabBlock() : ResourceLocation.parse("minecraft:stone_brick_slab");
        formDraft.ladderBlock = workspace != null ? workspace.stairConfig().ladderBlock() : ResourceLocation.parse("minecraft:ladder");
        formDraft.hallwayWidth = MKWorkspaceDimensions.snapToNearestAllowedShaftSize(formDraft.roomWidth, formDraft.roomLength,
                workspace != null ? workspace.dimensions().hallwayWidth() : 3);
        formDraft.roomHeight = MKWorkspaceDimensions.snapToNearestAllowedTowerHeight(makeDraftStairConfig(), formDraft.hallwayWidth,
                workspace != null ? workspace.dimensions().roomHeight() : 5, 3, 4);
        formDraft.entranceHeight = MKWorkspaceDimensions.snapToNearestAllowedEntranceHeight(makeDraftStairConfig(),
                formDraft.hallwayWidth, formDraft.roomHeight, workspace != null ? workspace.dimensions().entranceHeight() : 7,
                3, 4);
        formDraft.basementHeight = MKWorkspaceDimensions.snapToNearestAllowedEntranceHeight(makeDraftStairConfig(),
                formDraft.hallwayWidth, formDraft.roomHeight, workspace != null ? workspace.dimensions().basementHeight() : 5,
                3, 4);
        formDraft.doorwayWidth = workspace != null ? workspace.dimensions().doorwayWidth() : 3;
        formDraft.doorwayHeight = workspace != null ? workspace.dimensions().doorwayHeight() : 3;
        formDraft.verticalAccessPlacement = workspace != null ? workspace.verticalAccessPlacement() : MKVerticalAccessPlacement.CENTER;
        formDraft.shellMargin = workspace != null ? workspace.shellMargin() : 1;
        formDraft.exteriorAirMargin = workspace != null ? workspace.exteriorAirMargin() : 2;
        formDraft.previewMargin = workspace != null ? workspace.previewMargin() : 4;
        formDraft.floorBlock = workspace != null ? workspace.palette().floorBlock() : ResourceLocation.parse("minecraft:smooth_stone");
        formDraft.wallBlock = workspace != null ? workspace.palette().wallBlock() : ResourceLocation.parse("minecraft:stone_bricks");
        formDraft.ceilingBlock = workspace != null ? workspace.palette().ceilingBlock() : ResourceLocation.parse("minecraft:smooth_stone");
        snapDraftVerticalHeights();
    }

    private void rebaseDraftForFootprintChange() {
        formDraft.hallwayWidth = MKWorkspaceDimensions.getAllowedShaftSizes(formDraft.roomWidth, formDraft.roomLength).getFirst();
        formDraft.stairWidth = MKWorkspaceDimensions.getAllowedStairWidths(formDraft.hallwayWidth).getFirst();
        rebaseDraftVerticalHeights();
    }

    private void rebaseDraftVerticalHeights() {
        var baseConfig = makeDraftStairConfig();
        formDraft.roomHeight = MKWorkspaceDimensions.getAllowedTowerHeights(baseConfig, formDraft.hallwayWidth, 3, 4).getFirst();
        formDraft.stairFlatRunLength = MKWorkspaceDimensions.getAllowedFlatRunLengths(baseConfig, formDraft.hallwayWidth,
                formDraft.roomHeight, 4).getFirst();
        snapDraftVerticalHeights();
        formDraft.entranceHeight = MKWorkspaceDimensions.getAllowedEntranceHeights(makeDraftStairConfig(),
                formDraft.hallwayWidth, formDraft.roomHeight, 3, 4).getFirst();
        formDraft.basementHeight = MKWorkspaceDimensions.getAllowedEntranceHeights(makeDraftStairConfig(),
                formDraft.hallwayWidth, formDraft.roomHeight, 3, 4).getFirst();
    }

    private void snapDraftVerticalHeights() {
        var config = makeDraftStairConfig();
        formDraft.roomHeight = snapTowerHeight(config, formDraft.hallwayWidth, formDraft.roomHeight);
        formDraft.stairFlatRunLength = snapAllowedFlatRunLength(config, formDraft.hallwayWidth, formDraft.roomHeight,
                formDraft.stairFlatRunLength);
        config = makeDraftStairConfig();
        formDraft.roomHeight = snapTowerHeight(config, formDraft.hallwayWidth, formDraft.roomHeight);
        formDraft.entranceHeight = snapEntranceHeight(config, formDraft.hallwayWidth, formDraft.roomHeight, formDraft.entranceHeight);
        formDraft.basementHeight = snapEntranceHeight(config, formDraft.hallwayWidth, formDraft.roomHeight, formDraft.basementHeight);
    }

    private com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig makeDraftStairConfig() {
        ensureFormDraftInitialized();
        return makeStairConfig(formDraft.stairMode, formDraft.stairRiseType, formDraft.stairFlatRunLength, formDraft.stairWidth,
                formDraft.stairBlock, formDraft.slabBlock, formDraft.ladderBlock);
    }

    private void submitWorkspaceDraft() {
        ensureFormDraftInitialized();
        snapDraftVerticalHeights();
        PacketDistributor.sendToServer(new CreateWorkspacePacket(
                anchor,
                formDraft.namespace.trim(),
                formDraft.structureName.trim(),
                formDraft.roomWidth,
                formDraft.roomLength,
                formDraft.entranceHeight,
                formDraft.roomHeight,
                formDraft.basementHeight,
                formDraft.hallwayWidth,
                formDraft.doorwayWidth,
                formDraft.doorwayHeight,
                formDraft.verticalAccessPlacement,
                formDraft.shellMargin,
                formDraft.exteriorAirMargin,
                formDraft.previewMargin,
                formDraft.stairMode,
                formDraft.stairRiseType,
                formDraft.stairFlatRunLength,
                formDraft.stairWidth,
                formDraft.floorBlock,
                formDraft.wallBlock,
                formDraft.ceilingBlock,
                formDraft.stairBlock,
                formDraft.slabBlock,
                formDraft.ladderBlock
        ));
        PacketDistributor.sendToServer(new GenerateWorkspacePacket(anchor));
    }

    private int cycleAllowedTowerHeight(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
                                        int hallwayWidth, int currentHeight) {
        List<Integer> allowedHeights = MKWorkspaceDimensions.getAllowedTowerHeights(stairConfig, hallwayWidth, 3, 4);
        int snapped = snapTowerHeight(stairConfig, hallwayWidth, currentHeight);
        int index = allowedHeights.indexOf(snapped);
        if (index < 0) {
            return allowedHeights.getFirst();
        }
        return allowedHeights.get((index + 1) % allowedHeights.size());
    }

    private int cycleAllowedEntranceHeight(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
                                           int hallwayWidth, int roomHeight, int currentHeight) {
        List<Integer> allowedHeights = MKWorkspaceDimensions.getAllowedEntranceHeights(stairConfig, hallwayWidth, roomHeight,
                3, 4);
        int snapped = snapEntranceHeight(stairConfig, hallwayWidth, roomHeight, currentHeight);
        int index = allowedHeights.indexOf(snapped);
        if (index < 0) {
            return allowedHeights.getFirst();
        }
        return allowedHeights.get((index + 1) % allowedHeights.size());
    }

    private int snapTowerHeight(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
                                int hallwayWidth, int requestedHeight) {
        return MKWorkspaceDimensions.snapToNearestAllowedTowerHeight(stairConfig, hallwayWidth, requestedHeight, 3, 4);
    }

    private int snapEntranceHeight(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
                                   int hallwayWidth, int roomHeight, int requestedHeight) {
        return MKWorkspaceDimensions.snapToNearestAllowedEntranceHeight(stairConfig, hallwayWidth, roomHeight,
                requestedHeight, 3, 4);
    }

    private int cycleAllowedShaftSize(int roomWidth, int roomLength, int currentSize) {
        List<Integer> allowedSizes = MKWorkspaceDimensions.getAllowedShaftSizes(roomWidth, roomLength);
        int snapped = MKWorkspaceDimensions.snapToNearestAllowedShaftSize(roomWidth, roomLength, currentSize);
        int index = allowedSizes.indexOf(snapped);
        if (index < 0) {
            return allowedSizes.getFirst();
        }
        return allowedSizes.get((index + 1) % allowedSizes.size());
    }

    private int cycleAllowedStairWidth(int hallwayWidth, int currentWidth) {
        List<Integer> allowedWidths = MKWorkspaceDimensions.getAllowedStairWidths(hallwayWidth);
        int snapped = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(hallwayWidth, currentWidth);
        int index = allowedWidths.indexOf(snapped);
        if (index < 0) {
            return allowedWidths.getFirst();
        }
        return allowedWidths.get((index + 1) % allowedWidths.size());
    }

    private void rebaseDependentHeightsToMinimum(int hallwayWidth,
                                                 MKWorkspaceStairMode stairMode,
                                                 MKWorkspaceStairRiseType riseType,
                                                 int[] flatRunLength,
                                                 int stairWidth,
                                                 ResourceLocation stairBlock,
                                                 ResourceLocation slabBlock,
                                                 ResourceLocation ladderBlock,
                                                 int[] roomHeight,
                                                 MKButton roomHeightButton,
                                                 int[] entranceHeight,
                                                 MKButton entranceHeightButton,
                                                 int[] basementHeight,
                                                 MKButton basementHeightButton,
                                                 MKButton flatRunButton) {
        var baseConfig = makeStairConfig(stairMode, riseType, flatRunLength[0], stairWidth, stairBlock, slabBlock, ladderBlock);
        List<Integer> allowedRoomHeights = MKWorkspaceDimensions.getAllowedTowerHeights(baseConfig, hallwayWidth, 3, 4);
        roomHeight[0] = allowedRoomHeights.getFirst();
        roomHeightButton.buttonText = Component.literal(Integer.toString(roomHeight[0]));

        flatRunLength[0] = MKWorkspaceDimensions.getAllowedFlatRunLengths(baseConfig, hallwayWidth, roomHeight[0], 4).getFirst();
        flatRunButton.buttonText = Component.literal(Integer.toString(flatRunLength[0]));

        var resolvedConfig = makeStairConfig(stairMode, riseType, flatRunLength[0], stairWidth, stairBlock, slabBlock, ladderBlock);
        roomHeight[0] = MKWorkspaceDimensions.getAllowedTowerHeights(resolvedConfig, hallwayWidth, 3, 4).getFirst();
        roomHeightButton.buttonText = Component.literal(Integer.toString(roomHeight[0]));

        entranceHeight[0] = MKWorkspaceDimensions.getAllowedEntranceHeights(resolvedConfig, hallwayWidth, roomHeight[0], 3, 4).getFirst();
        entranceHeightButton.buttonText = Component.literal(Integer.toString(entranceHeight[0]));

        basementHeight[0] = MKWorkspaceDimensions.getAllowedEntranceHeights(resolvedConfig, hallwayWidth, roomHeight[0], 3, 4).getFirst();
        basementHeightButton.buttonText = Component.literal(Integer.toString(basementHeight[0]));
    }

    private int cycleAllowedFlatRunLength(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
                                          int hallwayWidth, int roomHeight, int currentFlatRunLength) {
        List<Integer> allowedFlatRuns = MKWorkspaceDimensions.getAllowedFlatRunLengths(stairConfig, hallwayWidth, roomHeight, 4);
        int snapped = snapAllowedFlatRunLength(stairConfig, hallwayWidth, roomHeight, currentFlatRunLength);
        int index = allowedFlatRuns.indexOf(snapped);
        if (index < 0) {
            return allowedFlatRuns.getFirst();
        }
        return allowedFlatRuns.get((index + 1) % allowedFlatRuns.size());
    }

    private int snapAllowedFlatRunLength(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
                                         int hallwayWidth, int roomHeight, int requestedFlatRunLength) {
        return MKWorkspaceDimensions.snapToNearestAllowedFlatRunLength(stairConfig, hallwayWidth, roomHeight,
                requestedFlatRunLength, 4);
    }

    private int countVariants(List<MKWorkspacePieceDefinition> pieces) {
        return (int) pieces.stream().filter(piece -> piece.variantIndex() > 0).count();
    }

    private Map<String, List<MKWorkspacePieceDefinition>> groupPiecesByTopology() {
        Map<String, List<MKWorkspacePieceDefinition>> grouped = new LinkedHashMap<>();
        List<MKWorkspacePieceDefinition> sortedPieces = workspace.pieces().stream()
                .sorted(Comparator
                        .comparing((MKWorkspacePieceDefinition piece) -> piece.tags().getOrDefault("topology_role", piece.role().getSerializedName()))
                        .thenComparingInt(MKWorkspacePieceDefinition::variantIndex))
                .toList();
        for (MKWorkspacePieceDefinition piece : sortedPieces) {
            String topologyKey = piece.tags().getOrDefault("topology_role", piece.role().getSerializedName());
            grouped.computeIfAbsent(topologyKey, ignored -> new java.util.ArrayList<>()).add(piece);
        }
        return grouped;
    }

    private String getBaseName(MKWorkspacePieceDefinition piece) {
        return piece.tags().getOrDefault("workspace_base_name", piece.pieceName());
    }

    private String describePiece(MKWorkspacePieceDefinition piece) {
        String label = piece.variantIndex() == 0 ? "template" : "variant " + piece.variantIndex();
        return label + ": " + piece.pieceName();
    }

    private String formatTopologyLabel(String key) {
        String[] parts = key.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    builder.append(part.substring(1));
                }
            }
        }
        return builder.toString();
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

    private boolean supportsStairGeneration(List<MKWorkspacePieceDefinition> pieces) {
        return pieces.stream().anyMatch(this::supportsStairGeneration);
    }

    private boolean supportsStairGeneration(MKWorkspacePieceDefinition piece) {
        return MKWorkspaceVerticalAccessTags.supportsVerticalAccess(piece.tags());
    }

    private boolean hasGeneratedStairs(MKWorkspacePieceDefinition piece) {
        return !piece.generatedStairPositions().isEmpty() &&
                !"none".equals(piece.tags().getOrDefault("generated_stair_mode", "none"));
    }

    private List<String> getDefaultInitialStates() {
        if (workspace != null && !workspace.pieces().isEmpty()) {
            return List.of("workspace");
        }
        return importManifestIds.isEmpty() ? List.of("form") : List.of("home");
    }

    private List<String> getInitialStatesForRefresh(MKStructureWorkspace updatedWorkspace) {
        String currentState = getState();
        if ("category".equals(currentState) && selectedTopologyKey != null && updatedWorkspace != null && !updatedWorkspace.pieces().isEmpty()) {
            return List.of("workspace", "category");
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

    private void switchToExistingState(String stateName) {
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

    private void resetCategoryOverrides() {
        if (workspace == null) {
            detailStairMode = MKWorkspaceStairMode.AUTO;
            detailStairRiseType = MKWorkspaceStairRiseType.STAIR;
            detailFlatRunLength = 0;
            detailStairWidth = 1;
            detailStairBlock = ResourceLocation.parse("minecraft:stone_brick_stairs");
            detailSlabBlock = ResourceLocation.parse("minecraft:stone_brick_slab");
            detailLadderBlock = ResourceLocation.parse("minecraft:ladder");
            return;
        }
        detailStairMode = workspace.stairConfig().mode();
        detailStairRiseType = workspace.stairConfig().riseType();
        detailFlatRunLength = workspace.stairConfig().flatRunLength();
        detailStairWidth = workspace.stairConfig().stairWidth();
        detailStairBlock = workspace.stairConfig().stairBlock();
        detailSlabBlock = workspace.stairConfig().slabBlock();
        detailLadderBlock = workspace.stairConfig().ladderBlock();
    }

    private void ensureCategoryOverridesInitialized() {
        if (detailStairMode == null || detailStairRiseType == null || detailStairBlock == null || detailSlabBlock == null ||
                detailLadderBlock == null) {
            resetCategoryOverrides();
        }
    }

    private com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig makeStairConfig(
            MKWorkspaceStairMode stairMode, MKWorkspaceStairRiseType riseType, int flatRunLength, int stairWidth,
            ResourceLocation stairBlock, ResourceLocation slabBlock, ResourceLocation ladderBlock) {
        return new com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig(
                stairMode, riseType, flatRunLength, stairWidth, stairBlock, slabBlock, ladderBlock);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        graphics.fill(xPos, yPos, xPos + PANEL_WIDTH, yPos + PANEL_HEIGHT, 0xCC202020);
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

