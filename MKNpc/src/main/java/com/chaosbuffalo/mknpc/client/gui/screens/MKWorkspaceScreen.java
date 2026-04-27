package com.chaosbuffalo.mknpc.client.gui.screens;

import com.chaosbuffalo.mknpc.client.gui.widgets.MKBranchExitMaskWidget;
import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantPacket;
import com.chaosbuffalo.mknpc.network.packets.AddWorkspaceVariantsForAllPacket;
import com.chaosbuffalo.mknpc.network.packets.ClearWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.CreateWorkspacePacket;
import com.chaosbuffalo.mknpc.network.packets.ExportWorkspacePiecesPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateAllWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateWorkspacePacket;
import com.chaosbuffalo.mknpc.network.packets.GenerateWorkspaceStairsPacket;
import com.chaosbuffalo.mknpc.network.packets.LoadWorkspaceFromManifestPacket;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHallwayFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKHorizontalOpeningProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategory;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceCategoryProfile;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceFloorSettings;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureFamilyType;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceFamilyHorizontalExitDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitConnectionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExtrusionMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceHorizontalExitPathKind;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKVerticalAccessPlacement;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceVerticalAccessSpec;
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
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private MKTowerWorkspaceCategory selectedFormCategory;
    private int selectedFamilyIndex;
    private int selectedFamilyExitIndex;
    private int selectedOpeningIndex;
    private int selectedHallwayIndex;
    private MKWorkspaceStairMode detailStairMode;
    private MKWorkspaceStairRiseType detailStairRiseType;
    private int detailFlatRunLength;
    private int detailStairWidth;
    private ResourceLocation detailStairBlock;
    private ResourceLocation detailSlabBlock;
    private ResourceLocation detailLadderBlock;
    private WorkspaceFormDraft formDraft;
    private boolean wasResized;

    private record ScrollViewState(double offsetX, double offsetY) {
    }

    private static class WorkspaceFormDraft {
        private String namespace;
        private String structureName;
        private int shaftSize;
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
        private int mainFloors;
        private int basementFloors;
        private List<MKTowerWorkspaceCategoryProfile> categoryProfiles;
        private List<MKTowerWorkspaceFamilyDefinition> familyDefinitions;
        private List<MKHorizontalOpeningProfile> openingProfiles;
        private List<MKHallwayFamilyDefinition> hallwayFamilies;
    }

    public MKWorkspaceScreen(net.minecraft.core.BlockPos anchor, MKStructureWorkspace workspace, List<String> importManifestIds) {
        this(anchor, workspace, importManifestIds, List.of(), null, null, -1, -1, -1, -1, null, null, 0, 1, null, null, null);
    }

    private MKWorkspaceScreen(net.minecraft.core.BlockPos anchor, MKStructureWorkspace workspace, List<String> importManifestIds,
                              List<String> initialStates,
                              MKTowerWorkspaceCategory selectedFormCategory,
                              String selectedTopologyKey,
                              int selectedFamilyIndex,
                              int selectedFamilyExitIndex,
                              int selectedOpeningIndex,
                              int selectedHallwayIndex,
                              MKWorkspaceStairMode detailStairMode,
                              MKWorkspaceStairRiseType detailStairRiseType, int detailFlatRunLength, int detailStairWidth,
                              ResourceLocation detailStairBlock, ResourceLocation detailSlabBlock,
                              ResourceLocation detailLadderBlock) {
        super(Component.literal("Tower Workspace"));
        this.anchor = anchor;
        this.workspace = workspace;
        this.importManifestIds = List.copyOf(importManifestIds);
        this.initialStates = List.copyOf(initialStates);
        this.selectedFormCategory = selectedFormCategory;
        this.selectedTopologyKey = selectedTopologyKey;
        this.selectedFamilyIndex = selectedFamilyIndex;
        this.selectedFamilyExitIndex = selectedFamilyExitIndex;
        this.selectedOpeningIndex = selectedOpeningIndex;
        this.selectedHallwayIndex = selectedHallwayIndex;
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
                selectedFormCategory, selectedTopologyKey, selectedFamilyIndex, selectedFamilyExitIndex,
                selectedOpeningIndex, selectedHallwayIndex,
                detailStairMode, detailStairRiseType, detailFlatRunLength, detailStairWidth,
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
        addState("form_categories", this::buildFormCategoriesState);
        addState("form_category_detail", this::buildFormCategoryDetailState);
        addState("form_families", this::buildFormFamiliesState);
        addState("form_family_category", this::buildFormFamilyCategoryState);
        addState("form_family_detail", this::buildFormFamilyDetailState);
        addState("form_family_exit_detail", this::buildFormFamilyExitDetailState);
        addState("form_openings", this::buildFormOpeningsState);
        addState("form_opening_detail", this::buildFormOpeningDetailState);
        addState("form_hallways", this::buildFormHallwaysState);
        addState("form_hallway_detail", this::buildFormHallwayDetailState);
        addState("workspace", this::buildWorkspaceState);
        addState("category", this::buildCategoryState);
        List<String> statesToPush = initialStates.isEmpty() ? getDefaultInitialStates() : initialStates;
        for (String state : statesToPush) {
            pushState(state);
        }
    }

    @Override
    public void addRestoreStateCallbacks() {
        ScrollViewState scrollState = getActiveScrollViewState();
        String state = popState();
        boolean resetScrollView = wasResized;
        addPostSetupCallback(() -> {
            pushState(state);
            restoreActiveScrollViewState(scrollState, resetScrollView);
            wasResized = false;
        });
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        wasResized = true;
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
                "Edit the workspace through focused v2 sections. Global screens handle naming, margins, materials, and shared vertical access. Category, family, opening, and hallway data each live in their own list and detail screens."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        MKText summary = makeWhiteText(Component.literal(
                formDraft.namespace + ":" + formDraft.structureName + "  |  " +
                        "shaft " + formDraft.shaftSize +
                        "  |  categories " + formDraft.categoryProfiles.size() +
                        "  |  families " + formDraft.familyDefinitions.size() +
                        "  |  openings " + formDraft.openingProfiles.size() +
                        "  |  hallways " + formDraft.hallwayFamilies.size()));
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

        MKButton categories = new MKButton(Component.literal("Category Profiles"), 220, 20);
        root.addWidget(categories);
        root.addConstraintToWidget(new CenterXConstraint(), categories);
        categories.setY(firstButtonY + ((BUTTON_HEIGHT + BUTTON_GAP) * 3));
        categories.setPressedCallback((button, mouseButton) -> {
            pushState("form_categories");
            flagNeedSetup();
            return true;
        });

        MKButton families = new MKButton(Component.literal("Branch Variants"), 220, 20);
        root.addWidget(families);
        root.addConstraintToWidget(new CenterXConstraint(), families);
        families.setY(firstButtonY + ((BUTTON_HEIGHT + BUTTON_GAP) * 4));
        families.setPressedCallback((button, mouseButton) -> {
            pushState("form_families");
            flagNeedSetup();
            return true;
        });

        MKButton openings = new MKButton(Component.literal("Opening Profiles"), 220, 20);
        root.addWidget(openings);
        root.addConstraintToWidget(new CenterXConstraint(), openings);
        openings.setY(firstButtonY + ((BUTTON_HEIGHT + BUTTON_GAP) * 5));
        openings.setPressedCallback((button, mouseButton) -> {
            pushState("form_openings");
            flagNeedSetup();
            return true;
        });

        MKButton hallways = new MKButton(Component.literal("Hallway Families"), 220, 20);
        root.addWidget(hallways);
        root.addConstraintToWidget(new CenterXConstraint(), hallways);
        hallways.setY(firstButtonY + ((BUTTON_HEIGHT + BUTTON_GAP) * 6));
        hallways.setPressedCallback((button, mouseButton) -> {
            pushState("form_hallways");
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
                "Configure workspace naming and scaffold/export margins. Room geometry now lives entirely in category profiles."));
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
        MKTextFieldWidget shellMarginField = makeField("Shell Margin", Integer.toString(formDraft.shellMargin));
        shellMarginField.setTextChangeCallback((field, text) -> formDraft.shellMargin = parseInt(text, formDraft.shellMargin));
        MKTextFieldWidget exteriorAirMarginField = makeField("Exterior Air Margin", Integer.toString(formDraft.exteriorAirMargin));
        exteriorAirMarginField.setTextChangeCallback((field, text) -> formDraft.exteriorAirMargin = parseInt(text, formDraft.exteriorAirMargin));
        MKTextFieldWidget previewMarginField = makeField("Preview Margin", Integer.toString(formDraft.previewMargin));
        previewMarginField.setTextChangeCallback((field, text) -> formDraft.previewMargin = parseInt(text, formDraft.previewMargin));

        addRow(content, makeLabel("mknpc.workspace.field.namespace"), namespaceField);
        addRow(content, makeLabel("mknpc.workspace.field.structure_name"), structureNameField);
        addRow(content, makeLabel("mknpc.workspace.field.shell_margin"), shellMarginField);
        addRow(content, makeLabel("mknpc.workspace.field.exterior_air_margin"), exteriorAirMarginField);
        addRow(content, makeLabel("mknpc.workspace.field.preview_margin"), previewMarginField);

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "import");

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
                "Adjust placement and stair authoring behavior for the shared shaft. Shaft size and stair width now live on the category page because they directly drive category height bands."));
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

        List<Integer> allowedHeights = currentDraftVerticalAccessSpec().getAllowedReusableHeights(3, 6);
        MKText allowedHeightsText = makeWhiteText(Component.literal(
                "Reusable shaft heights: " + (allowedHeights.isEmpty() ? "none" : allowedHeights.toString())));
        allowedHeightsText.setWidth(CONTENT_WIDTH);
        content.addWidget(allowedHeightsText);
        content.addConstraintToWidget(MarginConstraint.LEFT, allowedHeightsText);

        MKButton stairPlacementButton = new MKButton(getStairPlacementComponent(formDraft.verticalAccessPlacement), 180, 20);
        MKButton stairModeButton = new MKButton(getStairModeComponent(formDraft.stairMode), 180, 20);
        MKButton stairRiseTypeButton = new MKButton(getStairRiseTypeComponent(formDraft.stairRiseType), 180, 20);
        MKButton flatRunButton = new MKButton(Component.literal(Integer.toString(formDraft.stairFlatRunLength)), 180, 20);

        addRow(content, makeLabel("mknpc.workspace.field.stair_placement"), stairPlacementButton);
        addRow(content, makeLabel("mknpc.workspace.field.stair_mode"), stairModeButton);
        addRow(content, makeWhiteText(Component.literal("Rise Type")), stairRiseTypeButton);
        addRow(content, makeWhiteText(Component.literal("Flat Run Length")), flatRunButton);
        stairPlacementButton.setPressedCallback((button, mouseButton) -> {
            formDraft.verticalAccessPlacement = formDraft.verticalAccessPlacement.next();
            button.buttonText = getStairPlacementComponent(formDraft.verticalAccessPlacement);
            return true;
        });
        stairModeButton.setPressedCallback((button, mouseButton) -> {
            formDraft.stairMode = formDraft.stairMode.next();
            snapDraftVerticalAccess();
            button.buttonText = getStairModeComponent(formDraft.stairMode);
            flatRunButton.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            flagNeedSetup();
            return true;
        });
        stairRiseTypeButton.setPressedCallback((button, mouseButton) -> {
            formDraft.stairRiseType = formDraft.stairRiseType.next();
            snapDraftVerticalAccess();
            button.buttonText = getStairRiseTypeComponent(formDraft.stairRiseType);
            flatRunButton.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            flagNeedSetup();
            return true;
        });
        flatRunButton.setPressedCallback((button, mouseButton) -> {
            formDraft.stairFlatRunLength = cycleAllowedFlatRunLength(makeDraftStairConfig(), formDraft.shaftSize,
                    getDraftReferenceRoomHeight(makeDraftStairConfig()), formDraft.stairFlatRunLength);
            snapDraftVerticalAccess();
            button.buttonText = Component.literal(Integer.toString(formDraft.stairFlatRunLength));
            flagNeedSetup();
            return true;
        });

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_identity");

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

    private MKLayout buildFormCategoriesState() {
        ensureFormDraftInitialized();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Category Profiles"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Edit the shaft-driven category bands in one place. Shaft size and stair width live here because they determine which full heights are valid for each category."));
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

        MKButton shaftSizeButton = new MKButton(Component.literal(Integer.toString(formDraft.shaftSize)), 180, 20);
        shaftSizeButton.setPressedCallback((button, mouseButton) -> {
            formDraft.shaftSize = cycleAllowedDraftShaftSize(formDraft.shaftSize);
            snapDraftVerticalAccess();
            flagNeedSetup();
            return true;
        });
        addRow(content, makeWhiteText(Component.literal("Shaft Size")), shaftSizeButton);

        MKButton stairWidthButton = new MKButton(Component.literal(Integer.toString(formDraft.stairWidth)), 180, 20);
        stairWidthButton.setPressedCallback((button, mouseButton) -> {
            formDraft.stairWidth = cycleAllowedStairWidth(formDraft.shaftSize, formDraft.stairWidth);
            snapDraftVerticalAccess();
            flagNeedSetup();
            return true;
        });
        addRow(content, makeWhiteText(Component.literal("Stair Width")), stairWidthButton);

        List<Integer> allowedHeights = allowedFullHeightsForCategory(MKTowerWorkspaceCategory.MAIN);
        MKText allowedHeightsText = makeWhiteText(Component.literal(
                "Allowed band heights: " + (allowedHeights.isEmpty() ? "none" : allowedHeights.toString())));
        allowedHeightsText.setWidth(CONTENT_WIDTH);
        allowedHeightsText.setMultiline(true);
        content.addWidget(allowedHeightsText);
        content.addConstraintToWidget(MarginConstraint.LEFT, allowedHeightsText);

        for (MKTowerWorkspaceCategory category : MKTowerWorkspaceCategory.values()) {
            addCategoryHeightRow(content, category);
        }

        MKButton mainFloorsButton = new MKButton(Component.literal(Integer.toString(formDraft.mainFloors)), 180, 20);
        mainFloorsButton.setPressedCallback((button, mouseButton) -> {
            formDraft.mainFloors = nextAllowedMainFloorCount(formDraft.mainFloors, formDraft.basementFloors);
            formDraft.basementFloors = normalizeBasementFloorCount(formDraft.basementFloors, formDraft.mainFloors);
            flagNeedSetup();
            return true;
        });
        addRow(content, makeWhiteText(Component.literal("Main Floors")), mainFloorsButton);

        MKButton basementFloorsButton = new MKButton(Component.literal(Integer.toString(formDraft.basementFloors)), 180, 20);
        basementFloorsButton.setPressedCallback((button, mouseButton) -> {
            formDraft.basementFloors = nextAllowedBasementFloorCount(formDraft.basementFloors, formDraft.mainFloors);
            formDraft.mainFloors = normalizeMainFloorCount(formDraft.mainFloors, formDraft.basementFloors);
            flagNeedSetup();
            return true;
        });
        addRow(content, makeWhiteText(Component.literal("Basement Floors")), basementFloorsButton);

        MKText allowedFloorsText = makeWhiteText(Component.literal(
                "Allowed floor counts: main " + allowedMainFloorCountsLabel(formDraft.basementFloors) +
                        "  |  basement " + allowedBasementFloorCountsLabel(formDraft.mainFloors)));
        allowedFloorsText.setWidth(CONTENT_WIDTH);
        allowedFloorsText.setMultiline(true);
        content.addWidget(allowedFloorsText);
        content.addConstraintToWidget(MarginConstraint.LEFT, allowedFloorsText);

        for (MKTowerWorkspaceCategory category : MKTowerWorkspaceCategory.values()) {
            addCategoryProfileSection(content, category);
        }

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_categories");

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

    private MKLayout buildFormCategoryDetailState() {
        return buildFormCategoriesState();
    }

    private MKLayout buildFormFamiliesState() {
        ensureFormDraftInitialized();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Branch Variants"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Choose a category first, then edit only the families that belong to that band."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = (2 * BUTTON_HEIGHT) + BUTTON_GAP + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        for (MKTowerWorkspaceCategory category : MKTowerWorkspaceCategory.values()) {
            long count = formDraft.familyDefinitions.stream().filter(family -> family.category() == category).count();
            MKText header = makeWhiteText(Component.literal(formatTopologyLabel(category.getSerializedName())));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);
            MKText summary = makeWhiteText(Component.literal(count + " families"));
            summary.setWidth(CONTENT_WIDTH);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Open Category"), 180, 20);
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                selectedFormCategory = category;
                pushState("form_family_category");
                flagNeedSetup();
                return true;
            });
        }

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_categories");

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

    private MKLayout buildFormFamilyCategoryState() {
        ensureFormDraftInitialized();
        if (selectedFormCategory == null) {
            selectedFormCategory = MKTowerWorkspaceCategory.MAIN;
        }
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal(formatTopologyLabel(selectedFormCategory.getSerializedName()) + " Families"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Edit families for this category only. Families can override footprint and, when shaft access is disabled, choose their own height within the category band."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = (2 * BUTTON_HEIGHT) + BUTTON_GAP + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        List<Integer> familyIndexes = formDraft.familyDefinitions.stream()
                .filter(family -> family.category() == selectedFormCategory)
                .map(formDraft.familyDefinitions::indexOf)
                .toList();
        for (int index : familyIndexes) {
            MKTowerWorkspaceFamilyDefinition family = formDraft.familyDefinitions.get(index);
            MKText header = makeWhiteText(Component.literal(family.baseName()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);
            MKText summary = makeWhiteText(Component.literal(
                    family.roomWidth() + "x" + family.roomLength() + "x" + family.roomHeight() + "  |  " +
                            formatTopologyLabel(family.pieceRole().getSerializedName()) + "  |  exits " +
                            summarizeFamilyExits(family) + "  |  shaft " +
                            (family.supportsVerticalAccess() ? "yes" : "no")));
            summary.setWidth(CONTENT_WIDTH);
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Family"), 180, 20);
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                selectedFamilyIndex = index;
                selectedFamilyExitIndex = -1;
                pushState("form_family_detail");
                flagNeedSetup();
                return true;
            });
        }

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_families");

        MKButton addFamily = new MKButton(Component.literal("Add Family"), 180, 20);
        root.addWidget(addFamily);
        root.addConstraintToWidget(new CenterXConstraint(), addFamily);
        addFamily.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
        addFamily.setPressedCallback((button, mouseButton) -> {
            addFamilyDefinition(selectedFormCategory);
            selectedFamilyIndex = formDraft.familyDefinitions.size() - 1;
            selectedFamilyExitIndex = -1;
            pushState("form_family_detail");
            flagNeedSetup();
            return true;
        });

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        back.setPressedCallback((button, mouseButton) -> {
            switchToExistingState("form_families");
            return true;
        });
        return root;
    }

    private MKLayout buildFormFamilyDetailState() {
        ensureFormDraftInitialized();
        if (selectedFamilyIndex < 0 || selectedFamilyIndex >= formDraft.familyDefinitions.size()) {
            switchToExistingState("form_family_category");
            return buildFormFamilyCategoryState();
        }
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        int index = selectedFamilyIndex;
        MKTowerWorkspaceFamilyDefinition family = formDraft.familyDefinitions.get(index);

        MKText title = makeWhiteText(Component.literal("Family: " + family.baseName()));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Edit one family at a time. Left click a side of the room diagram to open that exit editor below the widget. Left click again to close it. Right click toggles that exit on or off."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = (2 * BUTTON_HEIGHT) + BUTTON_GAP + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        MKTextFieldWidget baseNameField = makeField("Base Name", family.baseName());
        baseNameField.setTextChangeCallback((field, text) -> replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                text.trim().isBlank() ? family.baseName() : text.trim(),
                family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                family.horizontalExits())));
        MKButton categoryButton = new MKButton(Component.literal(formatTopologyLabel(family.category().getSerializedName())), 180, 20);
        categoryButton.setPressedCallback((button, mouseButton) -> {
            MKTowerWorkspaceCategory nextCategory = cycleCategory(family.category());
            MKTowerWorkspaceCategoryProfile nextProfile = getDraftCategoryProfile(nextCategory);
            replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), nextCategory, family.pieceRole(),
                    family.supportsVerticalAccess(),
                    normalizeFamilyWidthForCategory(family.roomWidth(), family.supportsVerticalAccess(), nextProfile),
                    normalizeFamilyLengthForCategory(family.roomLength(), family.supportsVerticalAccess(), nextProfile),
                    normalizeFamilyHeightForCategory(family.roomHeight(), family.supportsVerticalAccess(), nextProfile),
                    family.horizontalExtrusionMode(),
                    family.horizontalExits()));
            selectedFormCategory = nextCategory;
            flagNeedSetup();
            return true;
        });
        MKButton roleButton = new MKButton(Component.literal(formatTopologyLabel(family.pieceRole().getSerializedName())), 180, 20);
        roleButton.setPressedCallback((button, mouseButton) -> {
            replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), family.category(), cycleFamilyRole(family.pieceRole()),
                    family.supportsVerticalAccess(),
                    family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(),
                    family.horizontalExits()));
            flagNeedSetup();
            return true;
        });
        MKButton supportsVerticalButton = new MKButton(Component.literal(family.supportsVerticalAccess() ? "Enabled" : "Disabled"), 180, 20);
        supportsVerticalButton.setPressedCallback((button, mouseButton) -> {
            MKTowerWorkspaceCategoryProfile categoryProfile = getDraftCategoryProfile(family.category());
            boolean supportsVerticalAccess = !family.supportsVerticalAccess();
            replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), family.category(), family.pieceRole(),
                    supportsVerticalAccess,
                    normalizeFamilyWidthForCategory(family.roomWidth(), supportsVerticalAccess, categoryProfile),
                    normalizeFamilyLengthForCategory(family.roomLength(), supportsVerticalAccess, categoryProfile),
                    normalizeFamilyHeightForCategory(family.roomHeight(), supportsVerticalAccess, categoryProfile),
                    family.horizontalExtrusionMode(),
                    family.horizontalExits()));
            flagNeedSetup();
            return true;
        });
        MKButton extrusionModeButton = new MKButton(Component.literal(formatFamilyExtrusionMode(family.horizontalExtrusionMode())), 180, 20);
        extrusionModeButton.setPressedCallback((button, mouseButton) -> {
            replaceFamilyDefinition(index, new MKTowerWorkspaceFamilyDefinition(
                    family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                    family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode().next(),
                    family.horizontalExits()));
            flagNeedSetup();
            return true;
        });
        MKTextFieldWidget roomWidthField = makeField("Room Width", Integer.toString(family.roomWidth()));
        roomWidthField.setTextChangeCallback((field, text) -> replaceFamilyDefinition(index, normalizeFamilyDefinition(
                new MKTowerWorkspaceFamilyDefinition(
                        family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                        parseInt(text, family.roomWidth()), family.roomLength(), family.roomHeight(),
                        family.horizontalExtrusionMode(), family.horizontalExits()))));
        MKTextFieldWidget roomLengthField = makeField("Room Length", Integer.toString(family.roomLength()));
        roomLengthField.setTextChangeCallback((field, text) -> replaceFamilyDefinition(index, normalizeFamilyDefinition(
                new MKTowerWorkspaceFamilyDefinition(
                        family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                        family.roomWidth(), parseInt(text, family.roomLength()), family.roomHeight(),
                        family.horizontalExtrusionMode(), family.horizontalExits()))));

        addRow(content, makeWhiteText(Component.literal("Base Name")), baseNameField);
        addRow(content, makeWhiteText(Component.literal("Category")), categoryButton);
        addRow(content, makeWhiteText(Component.literal("Role")), roleButton);
        addRow(content, makeWhiteText(Component.literal("Vertical Access")), supportsVerticalButton);
        addRow(content, makeWhiteText(Component.literal("Horizontal Extrusion")), extrusionModeButton);
        addRow(content, makeWhiteText(Component.literal("Room Width")), roomWidthField);
        addRow(content, makeWhiteText(Component.literal("Room Length")), roomLengthField);
        if (family.supportsVerticalAccess()) {
            MKText heightSummary = makeWhiteText(Component.literal(
                    "Room Height: " + family.roomHeight() + " (matches " +
                            formatTopologyLabel(family.category().getSerializedName()) + " full height)"));
            heightSummary.setWidth(CONTENT_WIDTH);
            heightSummary.setMultiline(true);
            content.addWidget(heightSummary);
            content.addConstraintToWidget(MarginConstraint.LEFT, heightSummary);
        } else {
            MKTextFieldWidget roomHeightField = makeField("Room Height", Integer.toString(family.roomHeight()));
            roomHeightField.setTextChangeCallback((field, text) -> replaceFamilyDefinition(index, normalizeFamilyDefinition(
                    new MKTowerWorkspaceFamilyDefinition(
                            family.baseName(), family.category(), family.pieceRole(), false,
                            family.roomWidth(), family.roomLength(), parseInt(text, family.roomHeight()),
                            family.horizontalExtrusionMode(), family.horizontalExits()))));
            addRow(content, makeWhiteText(Component.literal("Room Height")), roomHeightField);
        }
        MKText exitLabel = makeWhiteText(Component.literal("Horizontal Exits"));
        content.addWidget(exitLabel);
        content.addConstraintToWidget(MarginConstraint.LEFT, exitLabel);
        MKBranchExitMaskWidget exitWidget = new MKBranchExitMaskWidget(family.horizontalExits())
                .setSelectedDirection(selectedFamilyExitIndex >= 0 && selectedFamilyExitIndex < family.horizontalExits().size() ?
                        family.horizontalExits().get(selectedFamilyExitIndex).direction() : null)
                .setEditCallback(direction -> {
                    int exitIndex = findFamilyExitIndexByDirection(index, direction);
                    if (exitIndex == selectedFamilyExitIndex) {
                        selectedFamilyExitIndex = -1;
                    } else {
                        selectedFamilyExitIndex = exitIndex;
                    }
                    refreshPreservingActiveScroll();
                })
                .setToggleCallback(direction -> {
                    int exitIndex = findFamilyExitIndexByDirection(index, direction);
                    if (exitIndex >= 0) {
                        removeFamilyExit(index, exitIndex);
                        if (selectedFamilyExitIndex == exitIndex) {
                            selectedFamilyExitIndex = -1;
                        } else if (selectedFamilyExitIndex > exitIndex) {
                            selectedFamilyExitIndex--;
                        }
                    } else {
                        addFamilyExitAtDirection(index, direction);
                    }
                    refreshPreservingActiveScroll();
                });
        content.addWidget(exitWidget);
        content.addConstraintToWidget(new CenterXConstraint(), exitWidget);
        MKText exitSummary = makeWhiteText(Component.literal("Current exits: " + summarizeFamilyExits(family)));
        exitSummary.setWidth(CONTENT_WIDTH);
        exitSummary.setMultiline(true);
        content.addWidget(exitSummary);
        content.addConstraintToWidget(MarginConstraint.LEFT, exitSummary);
        if (selectedFamilyExitIndex >= 0 && selectedFamilyExitIndex < family.horizontalExits().size()) {
            addInlineFamilyExitEditor(content, index, selectedFamilyExitIndex, family.horizontalExits().get(selectedFamilyExitIndex));
        }

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_family_category");

        MKButton remove = new MKButton(Component.literal("Remove Family"), 180, 20);
        root.addWidget(remove);
        root.addConstraintToWidget(new CenterXConstraint(), remove);
        remove.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
        remove.setPressedCallback((button, mouseButton) -> {
            removeFamilyDefinition(index);
            selectedFamilyIndex = -1;
            selectedFamilyExitIndex = -1;
            switchToExistingState("form_family_category");
            return true;
        });

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        back.setPressedCallback((button, mouseButton) -> {
            selectedFamilyExitIndex = -1;
            switchToExistingState("form_family_category");
            return true;
        });
        return root;
    }

    private MKLayout buildFormFamilyExitDetailState() {
        return buildFormFamilyDetailState();
    }

    private MKLayout buildFormOpeningsState() {
        ensureFormDraftInitialized();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Opening Profiles"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Choose an opening profile and edit it on its own screen. Opening sizes and path compatibility are authored per profile."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = (2 * BUTTON_HEIGHT) + BUTTON_GAP + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        for (int i = 0; i < formDraft.openingProfiles.size(); i++) {
            int index = i;
            MKHorizontalOpeningProfile opening = formDraft.openingProfiles.get(index);
            MKText header = makeWhiteText(Component.literal(opening.profileId()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);
            MKText summary = makeWhiteText(Component.literal(
                    opening.openingWidth() + "x" + opening.openingHeight() + "  |  " +
                            describePathAccess(opening.allowOnMainPath(), opening.allowOnBranchPath())));
            summary.setWidth(CONTENT_WIDTH);
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Opening"), 180, 20);
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                selectedOpeningIndex = index;
                pushState("form_opening_detail");
                flagNeedSetup();
                return true;
            });
        }

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_family_detail");

        MKButton addProfile = new MKButton(Component.literal("Add Opening"), 180, 20);
        root.addWidget(addProfile);
        root.addConstraintToWidget(new CenterXConstraint(), addProfile);
        addProfile.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
        addProfile.setPressedCallback((button, mouseButton) -> {
            addOpeningProfile();
            selectedOpeningIndex = formDraft.openingProfiles.size() - 1;
            pushState("form_opening_detail");
            flagNeedSetup();
            return true;
        });

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

    private MKLayout buildFormOpeningDetailState() {
        ensureFormDraftInitialized();
        if (selectedOpeningIndex < 0 || selectedOpeningIndex >= formDraft.openingProfiles.size()) {
            switchToExistingState("form_openings");
            return buildFormOpeningsState();
        }
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        int index = selectedOpeningIndex;
        MKHorizontalOpeningProfile opening = formDraft.openingProfiles.get(index);

        MKText title = makeWhiteText(Component.literal("Opening: " + opening.profileId()));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Edit one opening profile at a time. Profiles can be restricted to the main path, branch path, or both."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = (2 * BUTTON_HEIGHT) + BUTTON_GAP + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        MKTextFieldWidget idField = makeField("Profile Id", opening.profileId());
        idField.setTextChangeCallback((field, text) -> replaceOpeningProfile(index, new MKHorizontalOpeningProfile(
                text.trim().isBlank() ? opening.profileId() : text.trim(),
                opening.openingWidth(), opening.openingHeight(), opening.allowOnMainPath(), opening.allowOnBranchPath())));
        MKTextFieldWidget widthField = makeField("Opening Width", Integer.toString(opening.openingWidth()));
        widthField.setTextChangeCallback((field, text) -> replaceOpeningProfile(index, new MKHorizontalOpeningProfile(
                opening.profileId(), parseInt(text, opening.openingWidth()), opening.openingHeight(),
                opening.allowOnMainPath(), opening.allowOnBranchPath())));
        MKTextFieldWidget heightField = makeField("Opening Height", Integer.toString(opening.openingHeight()));
        heightField.setTextChangeCallback((field, text) -> replaceOpeningProfile(index, new MKHorizontalOpeningProfile(
                opening.profileId(), opening.openingWidth(), parseInt(text, opening.openingHeight()),
                opening.allowOnMainPath(), opening.allowOnBranchPath())));
        MKButton mainButton = new MKButton(Component.literal(opening.allowOnMainPath() ? "Enabled" : "Disabled"), 180, 20);
        mainButton.setPressedCallback((button, mouseButton) -> {
            replaceOpeningProfile(index, new MKHorizontalOpeningProfile(opening.profileId(), opening.openingWidth(),
                    opening.openingHeight(), !opening.allowOnMainPath(), opening.allowOnBranchPath()));
            flagNeedSetup();
            return true;
        });
        MKButton branchButton = new MKButton(Component.literal(opening.allowOnBranchPath() ? "Enabled" : "Disabled"), 180, 20);
        branchButton.setPressedCallback((button, mouseButton) -> {
            replaceOpeningProfile(index, new MKHorizontalOpeningProfile(opening.profileId(), opening.openingWidth(),
                    opening.openingHeight(), opening.allowOnMainPath(), !opening.allowOnBranchPath()));
            flagNeedSetup();
            return true;
        });

        addRow(content, makeWhiteText(Component.literal("Profile Id")), idField);
        addRow(content, makeWhiteText(Component.literal("Opening Width")), widthField);
        addRow(content, makeWhiteText(Component.literal("Opening Height")), heightField);
        addRow(content, makeWhiteText(Component.literal("Allow On Main Path")), mainButton);
        addRow(content, makeWhiteText(Component.literal("Allow On Branch Path")), branchButton);

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_openings");

        MKButton remove = new MKButton(Component.literal("Remove Profile"), 180, 20);
        root.addWidget(remove);
        root.addConstraintToWidget(new CenterXConstraint(), remove);
        remove.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
        remove.setPressedCallback((button, mouseButton) -> {
            removeOpeningProfile(index);
            selectedOpeningIndex = -1;
            switchToExistingState("form_openings");
            return true;
        });

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        back.setPressedCallback((button, mouseButton) -> {
            switchToExistingState("form_openings");
            return true;
        });
        return root;
    }

    private MKLayout buildFormHallwaysState() {
        ensureFormDraftInitialized();
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        MKText title = makeWhiteText(Component.literal("Hallway Families"));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Choose a hallway family and edit it on its own screen. Each hallway defines its opening profile, dimensions, slope, path usage, and palette overrides."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = (2 * BUTTON_HEIGHT) + BUTTON_GAP + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        for (int i = 0; i < formDraft.hallwayFamilies.size(); i++) {
            int index = i;
            MKHallwayFamilyDefinition hallway = formDraft.hallwayFamilies.get(index);
            MKText header = makeWhiteText(Component.literal(hallway.hallwayId()));
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);
            MKText summary = makeWhiteText(Component.literal(
                    hallway.openingProfileId() + "  |  " + hallway.length() + "x" + hallway.interiorWidth() + "x" + hallway.interiorHeight() +
                            "  |  slope " + hallway.slopeDelta() + "  |  " +
                            describePathAccess(hallway.allowOnMainPath(), hallway.allowOnBranchPath())));
            summary.setWidth(CONTENT_WIDTH);
            summary.setMultiline(true);
            content.addWidget(summary);
            content.addConstraintToWidget(MarginConstraint.LEFT, summary);

            MKButton openButton = new MKButton(Component.literal("Edit Hallway"), 180, 20);
            content.addWidget(openButton);
            content.addConstraintToWidget(new CenterXConstraint(), openButton);
            openButton.setPressedCallback((button, mouseButton) -> {
                selectedHallwayIndex = index;
                pushState("form_hallway_detail");
                flagNeedSetup();
                return true;
            });
        }

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_opening_detail");

        MKButton addHallway = new MKButton(Component.literal("Add Hallway"), 180, 20);
        root.addWidget(addHallway);
        root.addConstraintToWidget(new CenterXConstraint(), addHallway);
        addHallway.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
        addHallway.setPressedCallback((button, mouseButton) -> {
            addHallwayFamily();
            selectedHallwayIndex = formDraft.hallwayFamilies.size() - 1;
            pushState("form_hallway_detail");
            flagNeedSetup();
            return true;
        });

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

    private MKLayout buildFormHallwayDetailState() {
        ensureFormDraftInitialized();
        if (selectedHallwayIndex < 0 || selectedHallwayIndex >= formDraft.hallwayFamilies.size()) {
            switchToExistingState("form_hallways");
            return buildFormHallwaysState();
        }
        int xPos = width / 2 - PANEL_WIDTH / 2;
        int yPos = height / 2 - PANEL_HEIGHT / 2;
        MKLayout root = new MKLayout(xPos, yPos, PANEL_WIDTH, PANEL_HEIGHT);
        root.setMargins(8, 8, 8, 8);
        root.setPaddingTop(8).setPaddingBot(8);

        int index = selectedHallwayIndex;
        MKHallwayFamilyDefinition hallway = formDraft.hallwayFamilies.get(index);

        MKText title = makeWhiteText(Component.literal("Hallway: " + hallway.hallwayId()));
        root.addWidget(title);
        root.addConstraintToWidget(MarginConstraint.TOP, title);
        root.addConstraintToWidget(new CenterXConstraint(), title);

        MKText helpText = makeWhiteText(Component.literal(
                "Edit one hallway family at a time. Hallways bind to opening profiles and can be allowed on the main path, branch path, or both."));
        helpText.setWidth(CONTENT_WIDTH);
        helpText.setMultiline(true);
        root.addWidget(helpText);
        root.addConstraintToWidget(StackConstraint.VERTICAL, helpText);
        root.addConstraintToWidget(new CenterXConstraint(), helpText);

        int buttonAreaHeight = (2 * BUTTON_HEIGHT) + BUTTON_GAP + BOTTOM_PADDING;
        int scrollHeight = PANEL_HEIGHT - TOP_CONTENT_Y - buttonAreaHeight - 12;
        MKScrollView scrollView = new MKScrollView(xPos + 10, yPos + TOP_CONTENT_Y, SCROLL_WIDTH, scrollHeight);
        scrollView.setScrollVelocity(6.0).setDoScrollX(false).setScrollMarginY(6);
        root.addWidget(scrollView);

        MKStackLayoutVertical content = new MKStackLayoutVertical(0, 0, CONTENT_WIDTH);
        content.setMargins(4, 4, 4, 4);
        content.setPaddingTop(4).setPaddingBot(4);

        addHallwayFieldRow(content, "Hallway Id", hallway.hallwayId(),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        text.trim().isBlank() ? hallway.hallwayId() : text.trim(), hallway.openingProfileId(),
                        hallway.length(), hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.floorBlock(),
                        hallway.wallBlock(), hallway.ceilingBlock())));
        addHallwayFieldRow(content, "Opening Profile Id", hallway.openingProfileId(),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), text.trim().isBlank() ? hallway.openingProfileId() : text.trim(),
                        hallway.length(), hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.floorBlock(),
                        hallway.wallBlock(), hallway.ceilingBlock())));
        addHallwayFieldRow(content, "Length", Integer.toString(hallway.length()),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), parseInt(text, hallway.length()),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.floorBlock(),
                        hallway.wallBlock(), hallway.ceilingBlock())));
        addHallwayFieldRow(content, "Interior Width", Integer.toString(hallway.interiorWidth()),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        parseInt(text, hallway.interiorWidth()), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.floorBlock(),
                        hallway.wallBlock(), hallway.ceilingBlock())));
        addHallwayFieldRow(content, "Interior Height", Integer.toString(hallway.interiorHeight()),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), parseInt(text, hallway.interiorHeight()), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.floorBlock(),
                        hallway.wallBlock(), hallway.ceilingBlock())));
        addHallwayFieldRow(content, "Slope Delta", Integer.toString(hallway.slopeDelta()),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), parseInt(text, hallway.slopeDelta()),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(), hallway.floorBlock(),
                        hallway.wallBlock(), hallway.ceilingBlock())));
        addHallwayFieldRow(content, "Floor Block", hallway.floorBlock().toString(),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(),
                        parseResourceLocation(text, hallway.floorBlock()), hallway.wallBlock(), hallway.ceilingBlock())));
        addHallwayFieldRow(content, "Wall Block", hallway.wallBlock().toString(),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(),
                        hallway.floorBlock(), parseResourceLocation(text, hallway.wallBlock()), hallway.ceilingBlock())));
        addHallwayFieldRow(content, "Ceiling Block", hallway.ceilingBlock().toString(),
                text -> replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                        hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                        hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                        hallway.allowOnMainPath(), hallway.allowOnBranchPath(),
                        hallway.floorBlock(), hallway.wallBlock(), parseResourceLocation(text, hallway.ceilingBlock()))));

        addToggleRow(content, "Allow On Main Path", hallway.allowOnMainPath(), () -> {
            replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                    hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                    hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                    !hallway.allowOnMainPath(), hallway.allowOnBranchPath(),
                    hallway.floorBlock(), hallway.wallBlock(), hallway.ceilingBlock()));
            flagNeedSetup();
        });
        addToggleRow(content, "Allow On Branch Path", hallway.allowOnBranchPath(), () -> {
            replaceHallwayFamily(index, new MKHallwayFamilyDefinition(
                    hallway.hallwayId(), hallway.openingProfileId(), hallway.length(),
                    hallway.interiorWidth(), hallway.interiorHeight(), hallway.slopeDelta(),
                    hallway.allowOnMainPath(), !hallway.allowOnBranchPath(),
                    hallway.floorBlock(), hallway.wallBlock(), hallway.ceilingBlock()));
            flagNeedSetup();
        });

        content.manualRecompute();
        scrollView.addWidget(content);
        scrollView.centerContentX();
        finalizeScrollView(scrollView, "form_hallways");

        MKButton remove = new MKButton(Component.literal("Remove Hallway"), 180, 20);
        root.addWidget(remove);
        root.addConstraintToWidget(new CenterXConstraint(), remove);
        remove.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT - BUTTON_GAP - BUTTON_HEIGHT);
        remove.setPressedCallback((button, mouseButton) -> {
            removeHallwayFamily(index);
            selectedHallwayIndex = -1;
            switchToExistingState("form_hallways");
            return true;
        });

        MKButton back = new MKButton(Component.literal("Back"), 120, 20);
        root.addWidget(back);
        root.addConstraintToWidget(new CenterXConstraint(), back);
        back.setY(yPos + PANEL_HEIGHT - BOTTOM_PADDING - BUTTON_HEIGHT);
        back.setPressedCallback((button, mouseButton) -> {
            switchToExistingState("form_hallways");
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

            MKText header = makeWhiteText(Component.literal(buildWorkspaceGroupLabel(templatePiece)));
            header.setWidth(CONTENT_WIDTH);
            content.addWidget(header);
            content.addConstraintToWidget(MarginConstraint.LEFT, header);

            int variantCount = countVariants(pieces);
            long generatedCount = pieces.stream().filter(this::hasGeneratedStairs).count();
            MKText details = makeWhiteText(Component.literal(
                    pieces.size() + " piece" + (pieces.size() == 1 ? "" : "s") + " - " +
                            variantCount + " variant" + (variantCount == 1 ? "" : "s") +
                            " - template " + getBaseName(templatePiece) +
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
        finalizeScrollView(scrollView, "form_hallway_detail");

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

        MKText title = makeWhiteText(Component.literal(buildWorkspaceGroupLabel(templatePiece)));
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
        finalizeScrollView(scrollView, "workspace");

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
        formDraft.stairMode = workspace != null ? workspace.stairConfig().mode() : MKWorkspaceStairMode.AUTO;
        formDraft.stairRiseType = workspace != null ? workspace.stairConfig().riseType() : MKWorkspaceStairRiseType.STAIR;
        formDraft.stairFlatRunLength = workspace != null ? workspace.stairConfig().flatRunLength() : 0;
        formDraft.stairWidth = workspace != null ? workspace.stairConfig().stairWidth() : 1;
        formDraft.stairBlock = workspace != null ? workspace.stairConfig().stairBlock() : ResourceLocation.parse("minecraft:stone_brick_stairs");
        formDraft.slabBlock = workspace != null ? workspace.stairConfig().slabBlock() : ResourceLocation.parse("minecraft:stone_brick_slab");
        formDraft.ladderBlock = workspace != null ? workspace.stairConfig().ladderBlock() : ResourceLocation.parse("minecraft:ladder");
        formDraft.verticalAccessPlacement = workspace != null ? workspace.verticalAccessSpec().placement() : MKVerticalAccessPlacement.CENTER;
        formDraft.shellMargin = workspace != null ? workspace.shellMargin() : 1;
        formDraft.exteriorAirMargin = workspace != null ? workspace.exteriorAirMargin() : 2;
        formDraft.previewMargin = workspace != null ? workspace.previewMargin() : 4;
        formDraft.floorBlock = workspace != null ? workspace.palette().floorBlock() : ResourceLocation.parse("minecraft:smooth_stone");
        formDraft.wallBlock = workspace != null ? workspace.palette().wallBlock() : ResourceLocation.parse("minecraft:stone_bricks");
        formDraft.ceilingBlock = workspace != null ? workspace.palette().ceilingBlock() : ResourceLocation.parse("minecraft:smooth_stone");
        formDraft.mainFloors = workspace != null ? workspace.floorSettings().mainFloors() : MKTowerWorkspaceFloorSettings.defaultSettings().mainFloors();
        formDraft.basementFloors = workspace != null ? workspace.floorSettings().basementFloors() : MKTowerWorkspaceFloorSettings.defaultSettings().basementFloors();
        formDraft.categoryProfiles = List.copyOf(workspace != null ? workspace.categoryProfiles() :
                MKTowerWorkspaceCategoryProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()));
        formDraft.familyDefinitions = List.copyOf(workspace != null ? workspace.familyDefinitions() :
                MKTowerWorkspaceFamilyDefinition.createDefaults());
        formDraft.openingProfiles = List.copyOf(workspace != null ? workspace.openingProfiles() :
                MKHorizontalOpeningProfile.createDefaults(MKWorkspaceDimensions.defaultDimensions()));
        formDraft.hallwayFamilies = List.copyOf(workspace != null ? workspace.hallwayFamilies() : List.of());
        int requestedShaftSize = workspace != null ? workspace.verticalAccessSpec().shaftSize() :
                MKWorkspaceVerticalAccessSpec.defaultSpec().shaftSize();
        formDraft.shaftSize = requestedShaftSize;
        if (selectedFormCategory == null) {
            selectedFormCategory = MKTowerWorkspaceCategory.ENTRY;
        }
        snapDraftVerticalAccess();
    }

    private com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig makeDraftStairConfig() {
        ensureFormDraftInitialized();
        return makeStairConfig(formDraft.stairMode, formDraft.stairRiseType, formDraft.stairFlatRunLength, formDraft.stairWidth,
                formDraft.stairBlock, formDraft.slabBlock, formDraft.ladderBlock);
    }

    private void submitWorkspaceDraft() {
        ensureFormDraftInitialized();
        snapDraftVerticalAccess();
        PacketDistributor.sendToServer(new CreateWorkspacePacket(buildWorkspaceDraft()));
        PacketDistributor.sendToServer(new GenerateWorkspacePacket(anchor));
    }

    private MKStructureWorkspace buildWorkspaceDraft() {
        ensureFormDraftInitialized();
        snapDraftVerticalAccess();
        MKTowerWorkspaceCategoryProfile entryProfile = getDraftCategoryProfile(MKTowerWorkspaceCategory.ENTRY);
        MKTowerWorkspaceCategoryProfile mainProfile = getDraftCategoryProfile(MKTowerWorkspaceCategory.MAIN);
        MKTowerWorkspaceCategoryProfile basementProfile = getDraftCategoryProfile(MKTowerWorkspaceCategory.BASEMENT);
        MKWorkspaceDimensions dimensions = new MKWorkspaceDimensions(
                mainProfile.roomWidth(),
                mainProfile.roomLength(),
                entryProfile.fullHeight(),
                mainProfile.fullHeight(),
                basementProfile.fullHeight(),
                formDraft.shaftSize,
                deriveLegacyDoorwayWidth(),
                deriveLegacyDoorwayHeight()
        );
        MKWorkspaceMaterialPalette palette = new MKWorkspaceMaterialPalette(formDraft.floorBlock, formDraft.wallBlock, formDraft.ceilingBlock);
        MKWorkspaceStairAuthoringConfig stairConfig = makeDraftStairConfig();
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = new MKWorkspaceVerticalAccessSpec(formDraft.shaftSize,
                formDraft.verticalAccessPlacement, stairConfig);
        long now = System.currentTimeMillis();
        return new MKStructureWorkspace(
                UUID.randomUUID(),
                anchor,
                formDraft.namespace.trim(),
                formDraft.structureName.trim(),
                MKStructureFamilyType.TOWER,
                dimensions,
                palette,
                stairConfig,
                formDraft.verticalAccessPlacement,
                formDraft.shellMargin,
                formDraft.exteriorAirMargin,
                formDraft.previewMargin,
                verticalAccessSpec,
                new MKTowerWorkspaceFloorSettings(formDraft.mainFloors, formDraft.basementFloors),
                formDraft.categoryProfiles,
                formDraft.familyDefinitions,
                formDraft.openingProfiles,
                formDraft.hallwayFamilies,
                now,
                now,
                List.of()
        );
    }

    private MKTowerWorkspaceCategoryProfile getDraftCategoryProfile(MKTowerWorkspaceCategory category) {
        return formDraft.categoryProfiles.stream()
                .filter(profile -> profile.category() == category)
                .findFirst()
                .orElseThrow();
    }

    private void replaceCategoryProfile(MKTowerWorkspaceCategoryProfile updatedProfile) {
        formDraft.categoryProfiles = formDraft.categoryProfiles.stream()
                .map(profile -> profile.category() == updatedProfile.category() ? updatedProfile : profile)
                .toList();
        snapDraftVerticalAccess();
    }

    private int deriveLegacyDoorwayWidth() {
        return getDraftOpeningProfile("main_opening")
                .or(() -> firstCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT))
                .map(MKHorizontalOpeningProfile::openingWidth)
                .orElse(workspace != null ? workspace.dimensions().doorwayWidth() : 3);
    }

    private int deriveLegacyDoorwayHeight() {
        return getDraftOpeningProfile("main_opening")
                .or(() -> firstCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT))
                .map(MKHorizontalOpeningProfile::openingHeight)
                .orElse(workspace != null ? workspace.dimensions().doorwayHeight() : 3);
    }

    private java.util.Optional<MKHorizontalOpeningProfile> getDraftOpeningProfile(String profileId) {
        return formDraft.openingProfiles.stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst();
    }

    private MKWorkspaceVerticalAccessSpec currentDraftVerticalAccessSpec() {
        return new MKWorkspaceVerticalAccessSpec(formDraft.shaftSize, formDraft.verticalAccessPlacement, makeDraftStairConfig());
    }

    private void snapDraftVerticalAccess() {
        int[] footprint = getDraftVerticalAccessFootprint();
        formDraft.shaftSize = MKWorkspaceDimensions.snapToNearestAllowedShaftSize(footprint[0], footprint[1], formDraft.shaftSize);
        formDraft.stairWidth = MKWorkspaceDimensions.snapToNearestAllowedStairWidth(formDraft.shaftSize, formDraft.stairWidth);
        var config = makeDraftStairConfig();
        int referenceHeight = getDraftReferenceRoomHeight(config);
        formDraft.stairFlatRunLength = snapAllowedFlatRunLength(config, formDraft.shaftSize, referenceHeight, formDraft.stairFlatRunLength);
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = currentDraftVerticalAccessSpec();
        int normalizedMainHeight = normalizeCategoryFullHeight(MKTowerWorkspaceCategory.MAIN,
                getDraftCategoryProfile(MKTowerWorkspaceCategory.MAIN).fullHeight(), verticalAccessSpec,
                getDraftCategoryProfile(MKTowerWorkspaceCategory.MAIN).fullHeight());
        formDraft.categoryProfiles = formDraft.categoryProfiles.stream()
                .map(profile -> normalizeCategoryProfile(profile, verticalAccessSpec, normalizedMainHeight))
                .toList();
        formDraft.mainFloors = normalizeMainFloorCount(formDraft.mainFloors, formDraft.basementFloors);
        formDraft.basementFloors = normalizeBasementFloorCount(formDraft.basementFloors, formDraft.mainFloors);
        formDraft.mainFloors = normalizeMainFloorCount(formDraft.mainFloors, formDraft.basementFloors);
        formDraft.familyDefinitions = formDraft.familyDefinitions.stream()
                .map(this::normalizeFamilyDefinition)
                .toList();
    }

    private MKTowerWorkspaceCategoryProfile normalizeCategoryProfile(MKTowerWorkspaceCategoryProfile profile,
                                                                    MKWorkspaceVerticalAccessSpec verticalAccessSpec,
                                                                    int normalizedMainHeight) {
        int roomWidth = Math.max(3, makeOdd(profile.roomWidth()));
        int roomLength = Math.max(3, makeOdd(profile.roomLength()));
        int fullHeight = normalizeCategoryFullHeight(profile.category(), profile.fullHeight(), verticalAccessSpec, normalizedMainHeight);
        return new MKTowerWorkspaceCategoryProfile(
                profile.category(),
                roomWidth,
                roomLength,
                fullHeight
        );
    }

    private int[] getDraftVerticalAccessFootprint() {
        int minWidth = formDraft.categoryProfiles.stream().mapToInt(MKTowerWorkspaceCategoryProfile::roomWidth).min().orElse(9);
        int minLength = formDraft.categoryProfiles.stream().mapToInt(MKTowerWorkspaceCategoryProfile::roomLength).min().orElse(9);
        return new int[]{minWidth, minLength};
    }

    private int cycleAllowedDraftShaftSize(int currentSize) {
        int[] footprint = getDraftVerticalAccessFootprint();
        return cycleAllowedShaftSize(footprint[0], footprint[1], currentSize);
    }

    private int getDraftReferenceRoomHeight(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig) {
        List<Integer> allowedHeights = MKWorkspaceDimensions.getAllowedTowerHeights(stairConfig, formDraft.shaftSize, 3, 4);
        if (!allowedHeights.isEmpty()) {
            return allowedHeights.getFirst();
        }
        return Math.max(3, getDraftCategoryProfile(MKTowerWorkspaceCategory.MAIN).fullHeight());
    }

    private void replaceFamilyDefinition(int index, MKTowerWorkspaceFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(formDraft.familyDefinitions);
        updated.set(index, normalizeFamilyDefinition(updatedFamily));
        formDraft.familyDefinitions = List.copyOf(updated);
    }

    private void removeFamilyDefinition(int index) {
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(formDraft.familyDefinitions);
        updated.remove(index);
        formDraft.familyDefinitions = List.copyOf(updated);
    }

    private void addFamilyDefinition(MKTowerWorkspaceCategory category) {
        MKTowerWorkspaceCategoryProfile profile = getDraftCategoryProfile(category);
        java.util.ArrayList<MKTowerWorkspaceFamilyDefinition> updated = new java.util.ArrayList<>(formDraft.familyDefinitions);
        updated.add(new MKTowerWorkspaceFamilyDefinition(
                nextUniqueFamilyBaseName(),
                category,
                defaultRoleForCategory(category),
                true,
                profile.roomWidth(),
                profile.roomLength(),
                profile.fullHeight(),
                MKWorkspaceHorizontalExtrusionMode.TUNNEL_ONLY,
                defaultHorizontalExitsForNewFamily()
        ));
        formDraft.familyDefinitions = List.copyOf(updated);
    }

    private List<MKWorkspaceFamilyHorizontalExitDefinition> defaultHorizontalExitsForNewFamily() {
        String openingProfileId = firstCompatibleOpeningProfileId(MKWorkspaceHorizontalExitPathKind.MAIN_EXIT)
                .orElseGet(() -> formDraft.openingProfiles.isEmpty() ? "opening_1" : formDraft.openingProfiles.getFirst().profileId());
        return List.of(new MKWorkspaceFamilyHorizontalExitDefinition(
                Direction.SOUTH,
                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT,
                openingProfileId
        ));
    }

    private void replaceFamilyExit(int familyIndex, int exitIndex, MKWorkspaceFamilyHorizontalExitDefinition updatedExit) {
        MKTowerWorkspaceFamilyDefinition family = formDraft.familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        exits.set(exitIndex, updatedExit);
        if (updatedExit.pathKind() != MKWorkspaceHorizontalExitPathKind.BRANCH) {
            for (int i = 0; i < exits.size(); i++) {
                if (i == exitIndex) {
                    continue;
                }
                MKWorkspaceFamilyHorizontalExitDefinition existingExit = exits.get(i);
                if (existingExit.pathKind() == updatedExit.pathKind()) {
                    exits.set(i, new MKWorkspaceFamilyHorizontalExitDefinition(
                            existingExit.direction(),
                            MKWorkspaceHorizontalExitPathKind.BRANCH,
                            ensureCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind.BRANCH,
                                    existingExit.openingProfileId()),
                            existingExit.connectionMode()
                    ));
                }
            }
        }
        replaceFamilyDefinition(familyIndex, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits
        ));
    }

    private void removeFamilyExit(int familyIndex, int exitIndex) {
        MKTowerWorkspaceFamilyDefinition family = formDraft.familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        exits.remove(exitIndex);
        replaceFamilyDefinition(familyIndex, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits
        ));
    }

    private int addFamilyExitAtDirection(int familyIndex, Direction direction) {
        MKTowerWorkspaceFamilyDefinition family = formDraft.familyDefinitions.get(familyIndex);
        java.util.ArrayList<MKWorkspaceFamilyHorizontalExitDefinition> exits = new java.util.ArrayList<>(family.horizontalExits());
        MKWorkspaceHorizontalExitPathKind pathKind = !family.mainExit().isPresent() ?
                MKWorkspaceHorizontalExitPathKind.MAIN_EXIT :
                !family.mainEntry().isPresent() ? MKWorkspaceHorizontalExitPathKind.MAIN_ENTRY :
                        MKWorkspaceHorizontalExitPathKind.BRANCH;
        exits.add(new MKWorkspaceFamilyHorizontalExitDefinition(
                direction,
                pathKind,
                firstCompatibleOpeningProfileId(pathKind)
                        .orElseGet(() -> formDraft.openingProfiles.isEmpty() ? "opening_1" : formDraft.openingProfiles.getFirst().profileId())
        ));
        replaceFamilyDefinition(familyIndex, new MKTowerWorkspaceFamilyDefinition(
                family.baseName(), family.category(), family.pieceRole(), family.supportsVerticalAccess(),
                family.roomWidth(), family.roomLength(), family.roomHeight(), family.horizontalExtrusionMode(), exits
        ));
        return exits.size() - 1;
    }

    private int findFamilyExitIndexByDirection(int familyIndex, Direction direction) {
        List<MKWorkspaceFamilyHorizontalExitDefinition> exits = formDraft.familyDefinitions.get(familyIndex).horizontalExits();
        for (int i = 0; i < exits.size(); i++) {
            if (exits.get(i).direction() == direction) {
                return i;
            }
        }
        return -1;
    }

    private void replaceOpeningProfile(int index, MKHorizontalOpeningProfile updatedProfile) {
        java.util.ArrayList<MKHorizontalOpeningProfile> updated = new java.util.ArrayList<>(formDraft.openingProfiles);
        updated.set(index, updatedProfile);
        formDraft.openingProfiles = List.copyOf(updated);
    }

    private void removeOpeningProfile(int index) {
        java.util.ArrayList<MKHorizontalOpeningProfile> updated = new java.util.ArrayList<>(formDraft.openingProfiles);
        updated.remove(index);
        formDraft.openingProfiles = List.copyOf(updated);
    }

    private void addOpeningProfile() {
        java.util.ArrayList<MKHorizontalOpeningProfile> updated = new java.util.ArrayList<>(formDraft.openingProfiles);
        updated.add(new MKHorizontalOpeningProfile(nextUniqueOpeningProfileId(), 3, 3, false, true));
        formDraft.openingProfiles = List.copyOf(updated);
    }

    private void replaceHallwayFamily(int index, MKHallwayFamilyDefinition updatedFamily) {
        java.util.ArrayList<MKHallwayFamilyDefinition> updated = new java.util.ArrayList<>(formDraft.hallwayFamilies);
        updated.set(index, updatedFamily);
        formDraft.hallwayFamilies = List.copyOf(updated);
    }

    private void removeHallwayFamily(int index) {
        java.util.ArrayList<MKHallwayFamilyDefinition> updated = new java.util.ArrayList<>(formDraft.hallwayFamilies);
        updated.remove(index);
        formDraft.hallwayFamilies = List.copyOf(updated);
    }

    private void addHallwayFamily() {
        String openingProfileId = firstCompatibleOpeningProfileId(MKWorkspaceHorizontalExitPathKind.BRANCH)
                .orElseGet(() -> formDraft.openingProfiles.isEmpty() ? "branch_opening" : formDraft.openingProfiles.getFirst().profileId());
        java.util.ArrayList<MKHallwayFamilyDefinition> updated = new java.util.ArrayList<>(formDraft.hallwayFamilies);
        updated.add(new MKHallwayFamilyDefinition(
                nextUniqueHallwayFamilyId(),
                openingProfileId,
                5,
                3,
                3,
                0,
                false,
                true,
                formDraft.floorBlock,
                formDraft.wallBlock,
                formDraft.ceilingBlock
        ));
        formDraft.hallwayFamilies = List.copyOf(updated);
    }

    private void addCategoryProfileSection(MKStackLayoutVertical content, MKTowerWorkspaceCategory category) {
        MKTowerWorkspaceCategoryProfile profile = getDraftCategoryProfile(category);
        MKText header = makeWhiteText(Component.literal(formatTopologyLabel(category.getSerializedName())));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKText summary = makeWhiteText(Component.literal(
                profile.roomWidth() + "x" + profile.roomLength()));
        summary.setWidth(CONTENT_WIDTH);
        summary.setMultiline(true);
        content.addWidget(summary);
        content.addConstraintToWidget(MarginConstraint.LEFT, summary);

        MKTextFieldWidget roomWidthField = makeField("Room Width", Integer.toString(profile.roomWidth()));
        roomWidthField.setTextChangeCallback((field, text) -> replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                profile.category(), parseInt(text, profile.roomWidth()), profile.roomLength(),
                profile.fullHeight())));
        MKTextFieldWidget roomLengthField = makeField("Room Length", Integer.toString(profile.roomLength()));
        roomLengthField.setTextChangeCallback((field, text) -> replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                profile.category(), profile.roomWidth(), parseInt(text, profile.roomLength()),
                profile.fullHeight())));

        addRow(content, makeWhiteText(Component.literal("Room Width")), roomWidthField);
        addRow(content, makeWhiteText(Component.literal("Room Length")), roomLengthField);
    }

    private void addCategoryHeightRow(MKStackLayoutVertical content, MKTowerWorkspaceCategory category) {
        MKTowerWorkspaceCategoryProfile profile = getDraftCategoryProfile(category);
        MKButton heightButton = new MKButton(Component.literal(Integer.toString(profile.fullHeight())), 180, 20);
        heightButton.setPressedCallback((button, mouseButton) -> {
            replaceCategoryProfile(new MKTowerWorkspaceCategoryProfile(
                    profile.category(), profile.roomWidth(), profile.roomLength(),
                    nextAllowedCategoryFullHeight(profile.category(), profile.fullHeight())));
            flagNeedSetup();
            return true;
        });
        addRow(content,
                makeWhiteText(Component.literal(formatTopologyLabel(category.getSerializedName()) + " Height")),
                heightButton);
    }

    private void addInlineFamilyExitEditor(MKStackLayoutVertical content, int familyIndex, int exitIndex,
                                           MKWorkspaceFamilyHorizontalExitDefinition exit) {
        MKText header = makeWhiteText(Component.literal("Editing " + formatDirection(exit.direction()) + " exit"));
        content.addWidget(header);
        content.addConstraintToWidget(MarginConstraint.LEFT, header);

        MKButton directionButton = new MKButton(Component.literal(formatDirection(exit.direction())), 180, 20);
        directionButton.setPressedCallback((button, mouseButton) -> {
            replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                    cycleCardinalDirection(exit.direction()),
                    exit.pathKind(),
                    exit.openingProfileId(),
                    exit.connectionMode()
            ));
            refreshPreservingActiveScroll();
            return true;
        });
        MKButton pathKindButton = new MKButton(Component.literal(formatTopologyLabel(exit.pathKind().getSerializedName())), 180, 20);
        pathKindButton.setPressedCallback((button, mouseButton) -> {
            MKWorkspaceHorizontalExitPathKind nextPathKind = exit.pathKind().next();
            replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                    exit.direction(),
                    nextPathKind,
                    ensureCompatibleOpeningProfile(nextPathKind, exit.openingProfileId()),
                    exit.connectionMode()
            ));
            refreshPreservingActiveScroll();
            return true;
        });
        MKButton connectionModeButton = new MKButton(Component.literal(formatExitConnectionMode(exit.connectionMode())), 180, 20);
        connectionModeButton.setPressedCallback((button, mouseButton) -> {
            replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                    exit.direction(),
                    exit.pathKind(),
                    exit.openingProfileId(),
                    exit.connectionMode().next()
            ));
            refreshPreservingActiveScroll();
            return true;
        });
        MKButton openingProfileButton = new MKButton(Component.literal(exit.openingProfileId()), 180, 20);
        openingProfileButton.setPressedCallback((button, mouseButton) -> {
            replaceFamilyExit(familyIndex, exitIndex, new MKWorkspaceFamilyHorizontalExitDefinition(
                    exit.direction(),
                    exit.pathKind(),
                    nextOpeningProfileId(exit.pathKind(), exit.openingProfileId()),
                    exit.connectionMode()
            ));
            refreshPreservingActiveScroll();
            return true;
        });
        addRow(content, makeWhiteText(Component.literal("Direction")), directionButton);
        addRow(content, makeWhiteText(Component.literal("Exit Role")), pathKindButton);
        addRow(content, makeWhiteText(Component.literal("Connection")), connectionModeButton);
        addRow(content, makeWhiteText(Component.literal("Opening Profile")), openingProfileButton);
    }

    private List<Integer> allowedFullHeightsForCategory(MKTowerWorkspaceCategory category) {
        MKWorkspaceVerticalAccessSpec verticalAccessSpec = currentDraftVerticalAccessSpec();
        int referenceHeight = getDraftCategoryProfile(MKTowerWorkspaceCategory.MAIN).fullHeight();
        return MKWorkspaceDimensions.getAllowedBandHeights(
                verticalAccessSpec.stairConfig(),
                verticalAccessSpec.shaftSize(),
                referenceHeight,
                3,
                6
        );
    }

    private String allowedFullHeightsForCategoryLabel(MKTowerWorkspaceCategory category) {
        return allowedFullHeightsForCategory(category).toString();
    }

    private List<Integer> allowedMainFloorCounts(int basementFloors) {
        ensureFormDraftInitialized();
        return MKTowerWorkspaceFloorSettings.allowedMainFloorCounts(formDraft.categoryProfiles, basementFloors);
    }

    private String allowedMainFloorCountsLabel(int basementFloors) {
        return allowedMainFloorCounts(basementFloors).toString();
    }

    private List<Integer> allowedBasementFloorCounts(int mainFloors) {
        ensureFormDraftInitialized();
        return MKTowerWorkspaceFloorSettings.allowedBasementFloorCounts(formDraft.categoryProfiles, mainFloors);
    }

    private String allowedBasementFloorCountsLabel(int mainFloors) {
        return allowedBasementFloorCounts(mainFloors).toString();
    }

    private int nextAllowedMainFloorCount(int currentCount, int basementFloors) {
        List<Integer> allowedCounts = allowedMainFloorCounts(basementFloors);
        if (allowedCounts.isEmpty()) {
            return currentCount;
        }
        int normalized = normalizeMainFloorCount(currentCount, basementFloors);
        int currentIndex = allowedCounts.indexOf(normalized);
        if (currentIndex < 0) {
            return allowedCounts.getFirst();
        }
        return allowedCounts.get((currentIndex + 1) % allowedCounts.size());
    }

    private int nextAllowedBasementFloorCount(int currentCount, int mainFloors) {
        List<Integer> allowedCounts = allowedBasementFloorCounts(mainFloors);
        if (allowedCounts.isEmpty()) {
            return currentCount;
        }
        int normalized = normalizeBasementFloorCount(currentCount, mainFloors);
        int currentIndex = allowedCounts.indexOf(normalized);
        if (currentIndex < 0) {
            return allowedCounts.getFirst();
        }
        return allowedCounts.get((currentIndex + 1) % allowedCounts.size());
    }

    private int normalizeMainFloorCount(int requestedCount, int basementFloors) {
        List<Integer> allowedCounts = allowedMainFloorCounts(basementFloors);
        return allowedCounts.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedCount)))
                .orElse(0);
    }

    private int normalizeBasementFloorCount(int requestedCount, int mainFloors) {
        List<Integer> allowedCounts = allowedBasementFloorCounts(mainFloors);
        return allowedCounts.stream()
                .min(java.util.Comparator.comparingInt(value -> Math.abs(value - requestedCount)))
                .orElse(0);
    }

    private int nextAllowedCategoryFullHeight(MKTowerWorkspaceCategory category, int currentHeight) {
        List<Integer> allowedHeights = allowedFullHeightsForCategory(category);
        if (allowedHeights.isEmpty()) {
            return currentHeight;
        }
        int normalized = normalizeCategoryFullHeight(category, currentHeight, currentDraftVerticalAccessSpec(),
                getDraftCategoryProfile(MKTowerWorkspaceCategory.MAIN).fullHeight());
        int currentIndex = allowedHeights.indexOf(normalized);
        if (currentIndex < 0) {
            return allowedHeights.getFirst();
        }
        return allowedHeights.get((currentIndex + 1) % allowedHeights.size());
    }

    private int normalizeCategoryFullHeight(MKTowerWorkspaceCategory category, int requestedHeight,
                                            MKWorkspaceVerticalAccessSpec verticalAccessSpec,
                                            int mainReferenceHeight) {
        return MKWorkspaceDimensions.snapToNearestAllowedBandHeight(
                verticalAccessSpec.stairConfig(),
                verticalAccessSpec.shaftSize(),
                mainReferenceHeight,
                requestedHeight,
                3,
                6
        );
    }

    private MKTowerWorkspaceFamilyDefinition normalizeFamilyDefinition(MKTowerWorkspaceFamilyDefinition family) {
        MKTowerWorkspaceCategoryProfile profile = getDraftCategoryProfile(family.category());
        return new MKTowerWorkspaceFamilyDefinition(
                family.baseName(),
                family.category(),
                family.pieceRole(),
                family.supportsVerticalAccess(),
                normalizeFamilyWidthForCategory(family.roomWidth(), family.supportsVerticalAccess(), profile),
                normalizeFamilyLengthForCategory(family.roomLength(), family.supportsVerticalAccess(), profile),
                normalizeFamilyHeightForCategory(family.roomHeight(), family.supportsVerticalAccess(), profile),
                family.horizontalExtrusionMode(),
                family.horizontalExits()
        );
    }

    private int normalizeFamilyWidthForCategory(int requestedWidth, boolean supportsVerticalAccess,
                                                MKTowerWorkspaceCategoryProfile profile) {
        int width = Math.max(3, makeOdd(requestedWidth));
        return supportsVerticalAccess ? Math.max(width, formDraft.shaftSize) : width;
    }

    private int normalizeFamilyLengthForCategory(int requestedLength, boolean supportsVerticalAccess,
                                                 MKTowerWorkspaceCategoryProfile profile) {
        int length = Math.max(3, makeOdd(requestedLength));
        return supportsVerticalAccess ? Math.max(length, formDraft.shaftSize) : length;
    }

    private int normalizeFamilyHeightForCategory(int requestedHeight, boolean supportsVerticalAccess,
                                                 MKTowerWorkspaceCategoryProfile profile) {
        if (supportsVerticalAccess) {
            return profile.fullHeight();
        }
        return Math.max(MKTowerWorkspaceCategoryProfile.MIN_ROOM_HEIGHT, Math.min(requestedHeight, profile.fullHeight()));
    }

    private int makeOdd(int value) {
        int adjusted = Math.max(1, value);
        return adjusted % 2 == 0 ? adjusted + 1 : adjusted;
    }

    private MKWorkspacePieceRole defaultRoleForCategory(MKTowerWorkspaceCategory category) {
        return switch (category) {
            case ENTRY -> MKWorkspacePieceRole.ENTRY;
            case MAIN -> MKWorkspacePieceRole.FLOOR_MAIN;
            case BASEMENT -> MKWorkspacePieceRole.BASEMENT_MAIN;
            case TOP_CAP -> MKWorkspacePieceRole.TOP_CAP_APPROACH;
            case BASEMENT_CAP -> MKWorkspacePieceRole.BASEMENT_CAP;
        };
    }

    private String nextUniqueFamilyBaseName() {
        int index = 1;
        while (true) {
            String candidate = "family_" + index;
            boolean used = formDraft.familyDefinitions.stream().anyMatch(family -> family.baseName().equals(candidate));
            if (!used) {
                return candidate;
            }
            index++;
        }
    }

    private String nextUniqueOpeningProfileId() {
        int index = 1;
        while (true) {
            String candidate = "opening_" + index;
            boolean used = formDraft.openingProfiles.stream().anyMatch(profile -> profile.profileId().equals(candidate));
            if (!used) {
                return candidate;
            }
            index++;
        }
    }

    private String nextUniqueHallwayFamilyId() {
        int index = 1;
        while (true) {
            String candidate = "hallway_" + index;
            boolean used = formDraft.hallwayFamilies.stream().anyMatch(hallway -> hallway.hallwayId().equals(candidate));
            if (!used) {
                return candidate;
            }
            index++;
        }
    }

    private String summarizeFamilyExits(MKTowerWorkspaceFamilyDefinition family) {
        if (family.horizontalExits().isEmpty()) {
            return "none";
        }
        return family.horizontalExits().stream()
                .map(this::describeFamilyExit)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String describePathAccess(boolean allowOnMainPath, boolean allowOnBranchPath) {
        if (allowOnMainPath && allowOnBranchPath) {
            return "main + branch";
        }
        if (allowOnMainPath) {
            return "main only";
        }
        if (allowOnBranchPath) {
            return "branch only";
        }
        return "disabled";
    }

    private String describeFamilyExit(MKWorkspaceFamilyHorizontalExitDefinition exit) {
        return formatDirection(exit.direction()) + " / " + formatTopologyLabel(exit.pathKind().getSerializedName()) +
                " / " + formatExitConnectionMode(exit.connectionMode()) + " / " + exit.openingProfileId();
    }

    private String formatExitConnectionMode(MKWorkspaceHorizontalExitConnectionMode connectionMode) {
        return switch (connectionMode) {
            case HALLWAY -> "Hallway";
            case DIRECT_ROOM -> "Direct Room";
        };
    }

    private String formatFamilyExtrusionMode(MKWorkspaceHorizontalExtrusionMode mode) {
        return switch (mode) {
            case TUNNEL_ONLY -> "Tunnel Only";
            case FULL_BODY -> "Full Body";
        };
    }

    private String formatDirection(Direction direction) {
        return formatTopologyLabel(direction.getSerializedName());
    }

    private Direction cycleCardinalDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> Direction.NORTH;
        };
    }

    private java.util.Optional<Direction> firstUnusedCardinalDirection(List<MKWorkspaceFamilyHorizontalExitDefinition> exits) {
        for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            boolean used = exits.stream().anyMatch(exit -> exit.direction() == direction);
            if (!used) {
                return java.util.Optional.of(direction);
            }
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<String> firstCompatibleOpeningProfileId(MKWorkspaceHorizontalExitPathKind pathKind) {
        return firstCompatibleOpeningProfile(pathKind).map(MKHorizontalOpeningProfile::profileId);
    }

    private java.util.Optional<MKHorizontalOpeningProfile> firstCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind pathKind) {
        return formDraft.openingProfiles.stream()
                .filter(profile -> pathKind.usesMainPath() ?
                        profile.allowOnMainPath() : profile.allowOnBranchPath())
                .findFirst();
    }

    private String nextOpeningProfileId(MKWorkspaceHorizontalExitPathKind pathKind, String currentProfileId) {
        List<String> compatibleProfiles = formDraft.openingProfiles.stream()
                .map(MKHorizontalOpeningProfile::profileId)
                .filter(profileId -> isCompatibleOpeningProfile(pathKind, profileId))
                .toList();
        if (compatibleProfiles.isEmpty()) {
            return currentProfileId;
        }
        int currentIndex = compatibleProfiles.indexOf(currentProfileId);
        if (currentIndex < 0) {
            return compatibleProfiles.getFirst();
        }
        return compatibleProfiles.get((currentIndex + 1) % compatibleProfiles.size());
    }

    private String ensureCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind pathKind, String currentProfileId) {
        return isCompatibleOpeningProfile(pathKind, currentProfileId) ? currentProfileId :
                firstCompatibleOpeningProfileId(pathKind).orElse(currentProfileId);
    }

    private boolean isCompatibleOpeningProfile(MKWorkspaceHorizontalExitPathKind pathKind, String profileId) {
        return formDraft.openingProfiles.stream()
                .filter(profile -> profile.profileId().equals(profileId))
                .findFirst()
                .map(profile -> pathKind.usesMainPath() ?
                        profile.allowOnMainPath() : profile.allowOnBranchPath())
                .orElse(false);
    }

    private MKTowerWorkspaceCategory cycleCategory(MKTowerWorkspaceCategory current) {
        MKTowerWorkspaceCategory[] values = MKTowerWorkspaceCategory.values();
        return values[(current.ordinal() + 1) % values.length];
    }

    private MKWorkspacePieceRole cycleFamilyRole(MKWorkspacePieceRole current) {
        List<MKWorkspacePieceRole> roles = List.of(
                MKWorkspacePieceRole.ENTRY,
                MKWorkspacePieceRole.FLOOR_MAIN,
                MKWorkspacePieceRole.TOP_CAP_APPROACH,
                MKWorkspacePieceRole.TOP_CAP,
                MKWorkspacePieceRole.BASEMENT_ENTRY,
                MKWorkspacePieceRole.BASEMENT_MAIN,
                MKWorkspacePieceRole.BASEMENT_CAP
        );
        int index = roles.indexOf(current);
        if (index < 0) {
            return roles.getFirst();
        }
        return roles.get((index + 1) % roles.size());
    }

    private ResourceLocation parseResourceLocation(String text, ResourceLocation fallback) {
        try {
            return ResourceLocation.parse(text.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private void addToggleRow(MKStackLayoutVertical root, String label, boolean enabled, Runnable onToggle) {
        MKButton button = new MKButton(Component.literal(enabled ? "Enabled" : "Disabled"), 180, 20);
        button.setPressedCallback((pressed, mouseButton) -> {
            onToggle.run();
            return true;
        });
        addRow(root, makeWhiteText(Component.literal(label)), button);
    }

    private void addHallwayFieldRow(MKStackLayoutVertical root, String label, String value,
                                    java.util.function.Consumer<String> onChange) {
        MKTextFieldWidget field = makeField(label, value);
        field.setTextChangeCallback((widget, text) -> onChange.accept(text));
        addRow(root, makeWhiteText(Component.literal(label)), field);
    }

    private int snapTowerHeight(com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairAuthoringConfig stairConfig,
                                int hallwayWidth, int requestedHeight) {
        return MKWorkspaceDimensions.snapToNearestAllowedTowerHeight(stairConfig, hallwayWidth, requestedHeight, 3, 4);
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

    private void finalizeScrollView(MKScrollView scrollView, String stateName) {
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

    private ScrollViewState getActiveScrollViewState() {
        MKScrollView scrollView = getActiveScrollView();
        if (scrollView == null) {
            return null;
        }
        return new ScrollViewState(scrollView.getOffsetX(), scrollView.getOffsetY());
    }

    private void restoreActiveScrollViewState(ScrollViewState scrollState, boolean resetScrollView) {
        MKScrollView scrollView = getActiveScrollView();
        if (scrollView == null) {
            return;
        }
        if (resetScrollView) {
            scrollView.resetView();
            return;
        }
        if (scrollState == null) {
            return;
        }
        scrollView.setOffsetX(scrollState.offsetX());
        scrollView.setOffsetY(scrollState.offsetY());
        clampScrollViewOffsets(scrollView);
    }

    private void refreshPreservingActiveScroll() {
        ScrollViewState scrollState = getActiveScrollViewState();
        boolean resetScrollView = wasResized;
        addPostSetupCallback(() -> restoreActiveScrollViewState(scrollState, resetScrollView));
        flagNeedSetup();
    }

    private MKScrollView getActiveScrollView() {
        if (children.isEmpty()) {
            return null;
        }
        return findFirstScrollView(children.peekLast());
    }

    private MKScrollView findFirstScrollView(IMKWidget widget) {
        if (widget instanceof MKScrollView scrollView) {
            return scrollView;
        }
        for (IMKWidget child : widget.getChildren()) {
            MKScrollView nested = findFirstScrollView(child);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private Map<String, List<MKWorkspacePieceDefinition>> groupPiecesByTopology() {
        Map<String, List<MKWorkspacePieceDefinition>> grouped = new LinkedHashMap<>();
        List<MKWorkspacePieceDefinition> sortedPieces = workspace.pieces().stream()
                .sorted(Comparator
                        .comparing(this::buildWorkspaceGroupLabel)
                        .thenComparingInt(MKWorkspacePieceDefinition::variantIndex))
                .toList();
        for (MKWorkspacePieceDefinition piece : sortedPieces) {
            String topologyKey = buildWorkspaceGroupKey(piece);
            grouped.computeIfAbsent(topologyKey, ignored -> new java.util.ArrayList<>()).add(piece);
        }
        return grouped;
    }

    private String buildWorkspaceGroupKey(MKWorkspacePieceDefinition piece) {
        String hallwayFamilyId = piece.tags().get("workspace_hallway_family_id");
        if (hallwayFamilyId != null) {
            return "hallway:" + hallwayFamilyId + ":" + piece.tags().getOrDefault("workspace_hallway_path_kind", "branch");
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return "room:" + piece.tags().getOrDefault("workspace_category", "main") + ":" +
                    familyId + ":" + piece.tags().getOrDefault("workspace_horizontal_exits",
                    piece.tags().getOrDefault("workspace_branch_exit_mask", "none"));
        }
        return "legacy:" + piece.tags().getOrDefault("topology_role", piece.role().getSerializedName());
    }

    private String buildWorkspaceGroupLabel(MKWorkspacePieceDefinition piece) {
        String hallwayFamilyId = piece.tags().get("workspace_hallway_family_id");
        if (hallwayFamilyId != null) {
            return "Hallway / " + hallwayFamilyId + " / " +
                    formatTopologyLabel(piece.tags().getOrDefault("workspace_hallway_path_kind", "branch"));
        }
        String familyId = piece.tags().get("workspace_family_id");
        if (familyId != null) {
            return formatTopologyLabel(piece.tags().getOrDefault("workspace_category", "main")) +
                    " / " + familyId +
                    " / exits " + piece.tags().getOrDefault("workspace_horizontal_exits",
                    piece.tags().getOrDefault("workspace_branch_exit_mask", "none"));
        }
        return formatTopologyLabel(piece.tags().getOrDefault("topology_role", piece.role().getSerializedName()));
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


