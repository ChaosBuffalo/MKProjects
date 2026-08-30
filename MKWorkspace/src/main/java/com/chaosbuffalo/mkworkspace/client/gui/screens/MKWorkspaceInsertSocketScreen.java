package com.chaosbuffalo.mkworkspace.client.gui.screens;

import com.chaosbuffalo.mkworkspace.network.packets.OpenWorkspaceInsertSocketScreenPacket;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangePayloads;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.MKWorkspaceChangeRequest;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceInsertSocketChangeOperation;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceInsertSocketChangePayload;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertAttachmentFace;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertFamilyKind;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceInsertSocketPlacement;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKBlockingModal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKBlockSlot;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKCreativeBlockPickerPanel;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKIntegerSlider;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKModal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKTextFieldWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MKWorkspaceInsertSocketScreen extends MKScreen {
    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 332;
    private static final int TEXT = 0xFFEDEDED;
    private static final int MUTED_TEXT = 0xFFB8B8B8;
    private static final int ERROR_TEXT = 0xFFFF7777;
    private static final int OK_TEXT = 0xFF8FE38F;

    private enum Page {
        LIST,
        CREATE
    }

    private final OpenWorkspaceInsertSocketScreenPacket.InsertSocketContext socketContext;
    private final MKStructureWorkspace workspace;
    private final MKWorkspaceInsertAttachmentFace templateAttachmentFace;
    private Page page = Page.LIST;
    private String hostFinalState;
    private MKTextFieldWidget familyIdField;
    private MKTextFieldWidget templateFinalStateField;
    private MKIntegerSlider widthSlider;
    private MKIntegerSlider heightSlider;
    private MKIntegerSlider depthSlider;
    private MKIntegerSlider faceUOffsetSlider;
    private MKIntegerSlider faceVOffsetSlider;
    private MKButton createButton;
    private MKModal blockPickerModal;
    private FinalStateBlockSlot hostFinalStateSlot;
    private FinalStateBlockSlot templateFinalStateSlot;
    private final List<MKButton> existingFamilyButtons = new ArrayList<>();

    public MKWorkspaceInsertSocketScreen(OpenWorkspaceInsertSocketScreenPacket.InsertSocketContext socketContext) {
        super(Component.literal("Workspace Insert Socket"));
        this.socketContext = socketContext;
        this.workspace = socketContext.workspace();
        this.templateAttachmentFace =
                MKWorkspaceInsertAttachmentFace.fromDirection(socketContext.socketFacing().getOpposite());
        this.hostFinalState = socketContext.finalState();
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        existingFamilyButtons.clear();
        int panelX = (width - PANEL_WIDTH) / 2;
        int panelY = (height - PANEL_HEIGHT) / 2;

        InsertSocketPanel root = new InsertSocketPanel(panelX, panelY);
        addWidget(root);
        if (page == Page.LIST) {
            setupListPage(root, panelX, panelY);
        } else {
            setupCreatePage(root, panelX, panelY);
        }
    }

    private void setupListPage(MKLayout root, int panelX, int panelY) {
        int controlY = panelY + 136;
        hostFinalStateSlot = new FinalStateBlockSlot(panelX + 12, controlY, this::selectedHostBlockId,
                () -> openBlockPicker("Choose Socket Final State", selectedHostBlockId(),
                        value -> hostFinalState = value.toString()),
                () -> hostFinalState = socketContext.finalState());
        root.addWidget(hostFinalStateSlot);
        addButton(root, panelX + 276, controlY - 1, 34, 18, "Air",
                () -> hostFinalState = "minecraft:air");
        addButton(root, panelX + 314, controlY - 1, 50, 18, "Target",
                () -> hostFinalState = socketContext.finalState());
        addButton(root, panelX + 368, controlY - 1, 42, 18, "Pick",
                () -> openBlockPicker("Choose Socket Final State", selectedHostBlockId(),
                        value -> hostFinalState = value.toString()));
        addButton(root, panelX + PANEL_WIDTH - 86, panelY + PANEL_HEIGHT - 28, 74, 20, "Create", () -> {
            page = Page.CREATE;
            flagNeedSetup();
        });
        addExistingFamilyButtons(root, panelX, panelY);
    }

    private void setupCreatePage(MKLayout root, int panelX, int panelY) {
        int fieldX = panelX + 132;
        int y = panelY + 86;
        familyIdField = addField(root, fieldX, y, 146, nextInsertSocketFamilyId());
        templateFinalStateField = addField(root, fieldX, y + 178, 146, "minecraft:air");

        int sliderX = fieldX;
        int sliderWidth = 190;
        widthSlider = addSlider(root, sliderX, y + 24, sliderWidth, "Width",
                1, maxOdd(socketContext.pieceWidth()), 2, 3);
        heightSlider = addSlider(root, sliderX, y + 54, sliderWidth, "Height",
                1, Math.max(1, socketContext.pieceHeight()), 1, Math.min(3, Math.max(1, socketContext.pieceHeight())));
        depthSlider = addSlider(root, sliderX, y + 84, sliderWidth, "Length",
                1, maxOdd(socketContext.pieceDepth()), 2, 3);
        faceUOffsetSlider = addSlider(root, sliderX, y + 118, sliderWidth, "Offset U",
                0, maxUOffset(), 1, centeredUOffset(widthSlider.value(), depthSlider.value(),
                        templateAttachmentFace));
        faceVOffsetSlider = addSlider(root, sliderX, y + 148, sliderWidth, "Offset V",
                0, maxVOffset(), 1, centeredVOffset(heightSlider.value(), depthSlider.value(),
                        templateAttachmentFace));

        templateFinalStateSlot = new FinalStateBlockSlot(fieldX + 150, y + 178, this::selectedTemplateBlockId,
                () -> openBlockPicker("Choose Template Final State", selectedTemplateBlockId(),
                        value -> templateFinalStateField.setText(value.toString())),
                () -> templateFinalStateField.setText("minecraft:air"));
        root.addWidget(templateFinalStateSlot);
        addButton(root, fieldX + 174, y + 178, 42, 18, "Pick",
                () -> openBlockPicker("Choose Template Final State", selectedTemplateBlockId(),
                        value -> templateFinalStateField.setText(value.toString())));

        createButton = addButton(root, panelX + PANEL_WIDTH - 86, panelY + PANEL_HEIGHT - 28,
                74, 20, "Create", this::createInsertFamily);
        addButton(root, panelX + PANEL_WIDTH - 166, panelY + PANEL_HEIGHT - 28, 74, 20, "Back", () -> {
            page = Page.LIST;
            flagNeedSetup();
        });
        refreshOffsetSliderRanges();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private MKTextFieldWidget addField(MKLayout root, int x, int y, int width, String value) {
        MKTextFieldWidget field = new MKTextFieldWidget(font, x, y, width, 18, Component.literal(""));
        field.setText(value);
        root.addWidget(field);
        return field;
    }

    private MKIntegerSlider addSlider(MKLayout root, int x, int y, int width, String label, int minValue, int maxValue,
                                  int step, int value) {
        MKIntegerSlider slider = new MKIntegerSlider(label, width, 20, minValue, maxValue, step, value,
                ignored -> refreshOffsetSliderRanges());
        slider.setX(x);
        slider.setY(y);
        root.addWidget(slider);
        return slider;
    }

    private MKButton addButton(MKLayout root, int x, int y, int width, int height, String label, Runnable callback) {
        MKButton button = new MKButton(x, y, width, height, Component.literal(label));
        button.setPressedCallback((ignored, mouseButton) -> {
            callback.run();
            return true;
        });
        root.addWidget(button);
        return button;
    }

    private void openBlockPicker(String title, ResourceLocation currentValue, Consumer<ResourceLocation> setter) {
        if (blockPickerModal != null) {
            closeBlockPicker();
        }
        int pickerWidth = Math.min(560, Math.max(320, width - 32));
        int pickerHeight = Math.min(440, Math.max(300, height - 32));
        int pickerX = width / 2 - pickerWidth / 2;
        int pickerY = height / 2 - pickerHeight / 2;

        MKModal modal = new MKBlockingModal();
        modal.setCloseOnClickOutside(false);
        modal.addWidget(new MKCreativeBlockPickerPanel(pickerX, pickerY, pickerWidth, pickerHeight,
                Component.literal(title), currentValue, value -> {
            setter.accept(value);
            closeBlockPicker();
        }, this::closeBlockPicker, true));
        modal.setOnCloseCallback(() -> {
            if (blockPickerModal == modal) {
                blockPickerModal = null;
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
        blockPickerModal = null;
    }

    private ResourceLocation selectedHostBlockId() {
        return Optional.ofNullable(ResourceLocation.tryParse(safeHostFinalState()))
                .orElse(ResourceLocation.withDefaultNamespace("air"));
    }

    private ResourceLocation selectedTemplateBlockId() {
        return Optional.ofNullable(ResourceLocation.tryParse(safeTemplateFinalState()))
                .orElse(ResourceLocation.withDefaultNamespace("air"));
    }

    private void createInsertFamily() {
        requestChange(new MKWorkspaceInsertSocketChangePayload(socketContext.pieceId(),
                socketContext.socketWorldPos(), socketContext.socketFacing(), true, sanitizedFamilyId(),
                widthSlider.value(), heightSlider.value(), depthSlider.value(), faceUOffsetSlider.value(),
                faceVOffsetSlider.value(), safeHostFinalState(), safeTemplateFinalState()));
    }

    private boolean canCreate() {
        if (page != Page.CREATE || widthSlider == null || heightSlider == null || depthSlider == null ||
                faceUOffsetSlider == null || faceVOffsetSlider == null) {
            return false;
        }
        String familyId = sanitizedFamilyId();
        if (familyId.isBlank() || workspace.insertFamilies().stream()
                .anyMatch(family -> family.familyId().equals(familyId))) {
            return false;
        }
        int requestedWidth = widthSlider.value();
        int requestedHeight = heightSlider.value();
        int requestedDepth = depthSlider.value();
        if (!MKWorkspaceInsertSocketPlacement.validateInsertDimensions(requestedWidth, requestedHeight,
                requestedDepth).isEmpty()) {
            return false;
        }
        return MKWorkspaceInsertSocketPlacement.validateFits(hostLocalBounds(), socketContext.socketLocalPos(),
                requestedWidth, requestedHeight, requestedDepth, templateAttachmentFace,
                faceUOffsetSlider.value(),
                faceVOffsetSlider.value()).isEmpty() &&
                collisionFor(requestedWidth, requestedHeight, requestedDepth, templateAttachmentFace,
                        faceUOffsetSlider.value(), faceVOffsetSlider.value()).isEmpty();
    }

    private boolean fitsFamily(MKWorkspaceInsertFamilyDefinition family) {
        return isAttachableFamily(family) && geometryFitsFamily(family) && collisionForFamily(family).isEmpty();
    }

    private boolean geometryFitsFamily(MKWorkspaceInsertFamilyDefinition family) {
        if (family.attachmentFace().isPresent() && family.attachmentFace().get() != templateAttachmentFace) {
            return false;
        }
        int faceUOffset = family.attachmentFace().isPresent() ? family.faceUOffset() : centeredUOffset(family);
        int faceVOffset = family.attachmentFace().isPresent() ? family.faceVOffset() : centeredVOffset(family);
        MKWorkspaceInsertAttachmentFace authoredFace = family.attachmentFace().orElse(templateAttachmentFace);
        return MKWorkspaceInsertSocketPlacement.validateOrientedFits(hostLocalBounds(), socketContext.socketLocalPos(),
                family.width(), family.height(), family.depth(), authoredFace,
                faceUOffset, faceVOffset, templateAttachmentFace, placementTop()).isEmpty();
    }

    private String fitStatus(MKWorkspaceInsertFamilyDefinition family, boolean fits) {
        if (!isAttachableFamily(family)) {
            return "wrong orientation";
        }
        if (family.attachmentFace().isPresent() && family.attachmentFace().get() != templateAttachmentFace) {
            return "wrong face";
        }
        if (!geometryFitsFamily(family)) {
            return "blocked";
        }
        Optional<OpenWorkspaceInsertSocketScreenPacket.OccupiedInsertFootprint> collision =
                collisionForFamily(family);
        if (collision.isPresent()) {
            return "collides with " + collision.get().familyId();
        }
        return family.attachmentFace().isPresent() ? "fits" : "size fits";
    }

    private boolean isAttachableFamily(MKWorkspaceInsertFamilyDefinition family) {
        return socketContext.compatibleInsertFamilyIds().contains(family.familyId());
    }

    private Optional<OpenWorkspaceInsertSocketScreenPacket.OccupiedInsertFootprint> collisionForFamily(
            MKWorkspaceInsertFamilyDefinition family) {
        if (family.attachmentFace().isPresent() && family.attachmentFace().get() != templateAttachmentFace) {
            return Optional.empty();
        }
        int faceUOffset = family.attachmentFace().isPresent() ? family.faceUOffset() : centeredUOffset(family);
        int faceVOffset = family.attachmentFace().isPresent() ? family.faceVOffset() : centeredVOffset(family);
        return collisionFor(family.width(), family.height(), family.depth(), templateAttachmentFace,
                faceUOffset, faceVOffset);
    }

    private Optional<OpenWorkspaceInsertSocketScreenPacket.OccupiedInsertFootprint> collisionFor(int insertWidth,
                                                                                                 int insertHeight,
                                                                                                 int insertDepth,
                                                                                                 MKWorkspaceInsertAttachmentFace attachmentFace,
                                                                                                 int faceUOffset,
                                                                                                 int faceVOffset) {
        BoundingBox candidateBounds = MKWorkspaceInsertSocketPlacement.projectedOrientedInsertBounds(
                socketContext.socketLocalPos(), insertWidth, insertHeight, insertDepth, attachmentFace,
                faceUOffset, faceVOffset, templateAttachmentFace, placementTop());
        return socketContext.occupiedFootprints().stream()
                .filter(footprint -> MKWorkspaceInsertSocketPlacement.intersects(candidateBounds,
                        footprint.bounds()))
                .findFirst();
    }

    private String createFitStatus() {
        if (page != Page.CREATE || widthSlider == null || heightSlider == null || depthSlider == null ||
                faceUOffsetSlider == null || faceVOffsetSlider == null) {
            return "";
        }
        List<String> errors = MKWorkspaceInsertSocketPlacement.validateFits(hostLocalBounds(),
                socketContext.socketLocalPos(), widthSlider.value(), heightSlider.value(), depthSlider.value(),
                templateAttachmentFace, faceUOffsetSlider.value(), faceVOffsetSlider.value());
        if (!errors.isEmpty()) {
            return errors.get(0);
        }
        Optional<OpenWorkspaceInsertSocketScreenPacket.OccupiedInsertFootprint> collision =
                collisionFor(widthSlider.value(), heightSlider.value(), depthSlider.value(),
                        templateAttachmentFace, faceUOffsetSlider.value(), faceVOffsetSlider.value());
        return collision.map(footprint -> "collides with " + footprint.familyId()).orElse("footprint clear");
    }

    private int centeredUOffset(MKWorkspaceInsertFamilyDefinition family) {
        return centeredUOffset(family.width(), family.depth(), templateAttachmentFace);
    }

    private int centeredUOffset(int width, int depth, MKWorkspaceInsertAttachmentFace face) {
        if (face == MKWorkspaceInsertAttachmentFace.WEST || face == MKWorkspaceInsertAttachmentFace.EAST) {
            return depth / 2;
        }
        return width / 2;
    }

    private int centeredVOffset(MKWorkspaceInsertFamilyDefinition family) {
        return centeredVOffset(family.height(), family.depth(), templateAttachmentFace);
    }

    private int centeredVOffset(int height, int depth, MKWorkspaceInsertAttachmentFace face) {
        if (face.isHorizontal()) {
            return 0;
        }
        return depth / 2;
    }

    private Direction placementTop() {
        if (socketContext.socketFacing() == Direction.UP ||
                socketContext.socketFacing() == Direction.DOWN) {
            return Direction.NORTH;
        }
        return Direction.UP;
    }

    private BoundingBox hostLocalBounds() {
        return new BoundingBox(0, 0, 0, socketContext.pieceWidth() - 1,
                socketContext.pieceHeight() - 1, socketContext.pieceDepth() - 1);
    }

    private String fit(String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int max = Math.max(0, maxWidth - font.width(ellipsis));
        String trimmed = text;
        while (!trimmed.isEmpty() && font.width(trimmed) > max) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ellipsis;
    }

    private String nextInsertSocketFamilyId() {
        int index = 1;
        while (true) {
            String candidate = "insert_socket_" + index;
            if (workspace.insertFamilies().stream().noneMatch(family -> family.familyId().equals(candidate))) {
                return candidate;
            }
            index++;
        }
    }

    private String sanitizedFamilyId() {
        String value = familyIdField == null ? "" : familyIdField.getText().trim().toLowerCase();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '_' || ch == '-' || ch == '/') {
                builder.append(ch);
            } else if (ch == ' ') {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    private String safeTemplateFinalState() {
        String value = templateFinalStateField == null ? "" : templateFinalStateField.getText().trim();
        return value.isBlank() ? "minecraft:air" : value;
    }

    private String safeHostFinalState() {
        return hostFinalState == null || hostFinalState.isBlank() ? "minecraft:air" : hostFinalState.trim();
    }

    private int maxOdd(int value) {
        int max = Math.max(1, value);
        return max % 2 == 0 ? Math.max(1, max - 1) : max;
    }

    private int maxUOffset() {
        int width = widthSlider == null ? 3 : widthSlider.value();
        int depth = depthSlider == null ? 3 : depthSlider.value();
        if (templateAttachmentFace == MKWorkspaceInsertAttachmentFace.WEST ||
                templateAttachmentFace == MKWorkspaceInsertAttachmentFace.EAST) {
            return Math.max(0, depth - 1);
        }
        return Math.max(0, width - 1);
    }

    private int maxVOffset() {
        int height = heightSlider == null ? 3 : heightSlider.value();
        int depth = depthSlider == null ? 3 : depthSlider.value();
        if (templateAttachmentFace.isHorizontal()) {
            return Math.max(0, height - 1);
        }
        return Math.max(0, depth - 1);
    }

    private void refreshOffsetSliderRanges() {
        if (faceUOffsetSlider != null) {
            faceUOffsetSlider.setRange(0, maxUOffset(), 1);
        }
        if (faceVOffsetSlider != null) {
            faceVOffsetSlider.setRange(0, maxVOffset(), 1);
        }
    }

    private void addExistingFamilyButtons(MKLayout root, int panelX, int panelY) {
        int buttonX = panelX + 448;
        int buttonY = panelY + 184;
        for (MKWorkspaceInsertFamilyDefinition family : workspace.insertFamilies().stream()
                .filter(family -> family.kind() == MKWorkspaceInsertFamilyKind.INSERT_SOCKET)
                .toList()) {
            MKButton button = addButton(root, buttonX, buttonY, 74, 18, "Place",
                    () -> placeExistingFamily(family.familyId()));
            button.setEnabled(fitsFamily(family));
            existingFamilyButtons.add(button);
            buttonY += 22;
            if (buttonY > panelY + PANEL_HEIGHT - 48) {
                break;
            }
        }
    }

    private void placeExistingFamily(String familyId) {
        requestChange(new MKWorkspaceInsertSocketChangePayload(socketContext.pieceId(),
                socketContext.socketWorldPos(), socketContext.socketFacing(), false, familyId,
                1, 1, 1, 0, 0, safeHostFinalState(), "minecraft:air"));
    }

    private void requestChange(MKWorkspaceInsertSocketChangePayload payload) {
        MKWorkspaceChangeRequest request = new MKWorkspaceChangeRequest(UUID.randomUUID(),
                MKWorkspaceInsertSocketChangeOperation.ID, socketContext.anchor(),
                MKWorkspaceChangePayloads.encode(MKWorkspaceInsertSocketChangePayload.CODEC, payload,
                        "workspace insert socket change"));
        MKWorkspaceScreen workspaceScreen = new MKWorkspaceScreen(socketContext.anchor(), workspace, List.of());
        Minecraft.getInstance().setScreen(workspaceScreen);
        workspaceScreen.requestWorkspaceChange(request);
    }

    private void drawInsertDiagram(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0x802B2B2B);
        graphics.drawString(mc.font, "Template Bounds", x + 6, y + 5, MUTED_TEXT, false);
        int boxX = x + 22;
        int boxY = y + 26;
        int boxW = width - 44;
        int boxH = height - 44;
        graphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0x803A4D65);
        drawOutline(graphics, boxX, boxY, boxW, boxH, 0xFF8FB3D9);
        int dotX = boxX + Math.round((boxW - 1) * normalizedU());
        int dotY = boxY + Math.round((boxH - 1) * normalizedV());
        graphics.fill(dotX - 2, dotY - 2, dotX + 3, dotY + 3, 0xFFFFD166);
        graphics.drawString(mc.font, templateAttachmentFace.getSerializedName(), x + 6, y + height - 13,
                MUTED_TEXT, false);
    }

    private float normalizedU() {
        int maxU = maxUOffset();
        return maxU <= 0 || faceUOffsetSlider == null ? 0.0f :
                Math.max(0.0f, Math.min(1.0f, faceUOffsetSlider.value() / (float) maxU));
    }

    private float normalizedV() {
        int maxV = maxVOffset();
        return maxV <= 0 || faceVOffsetSlider == null ? 0.0f :
                Math.max(0.0f, Math.min(1.0f, faceVOffsetSlider.value() / (float) maxV));
    }

    private Component blockDisplayName(ResourceLocation blockId) {
        return BuiltInRegistries.BLOCK.getOptional(blockId)
                .map(block -> block.getName())
                .orElse(Component.literal(blockId.toString()));
    }

    private void drawOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static class FinalStateBlockSlot extends MKBlockSlot {
        private final Supplier<ResourceLocation> currentBlock;
        private final Runnable onPick;
        private final Runnable onReset;

        private FinalStateBlockSlot(int x, int y, Supplier<ResourceLocation> currentBlock, Runnable onPick,
                                    Runnable onReset) {
            super(x, y);
            this.currentBlock = currentBlock;
            this.onPick = onPick;
            this.onReset = onReset;
        }

        @Override
        public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX,
                         int mouseY, float partialTicks) {
            setBlock(currentBlock.get());
            super.draw(graphics, mc, x, y, width, height, mouseX, mouseY, partialTicks);
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

    private class InsertSocketPanel extends MKLayout {
        private InsertSocketPanel(int x, int y) {
            super(x, y, PANEL_WIDTH, PANEL_HEIGHT);
        }

        @Override
        public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX,
                         int mouseY, float partialTicks) {
            graphics.fill(x, y, x + width, y + height, 0xE0202020);
            graphics.fill(x, y, x + width, y + 22, 0xE0303030);

            int textX = x + 12;
            int textY = y + 8;
            graphics.drawString(mc.font, page == Page.LIST ? "Workspace Insert Socket" : "Create Insert Slot",
                    textX, textY, TEXT, false);
            textY += 24;
            graphics.drawString(mc.font, workspace.namespace() + ":" + workspace.structureName(), textX, textY,
                    TEXT, false);
            textY += 13;
            graphics.drawString(mc.font, "Template: " + socketContext.pieceName(), textX, textY, TEXT, false);
            textY += 13;
            graphics.drawString(mc.font, "Socket local: " + socketContext.socketLocalPos().toShortString() +
                    "  facing " + socketContext.socketFacing().getSerializedName(), textX, textY, MUTED_TEXT, false);
            textY += 13;
            graphics.drawString(mc.font, "Authorial bounds: " + socketContext.pieceWidth() + "w " +
                    socketContext.pieceHeight() + "h " + socketContext.pieceDepth() + "l", textX, textY,
                    MUTED_TEXT, false);
            textY += 13;
            graphics.drawString(mc.font, fit("Socket final_state: " + safeHostFinalState(), PANEL_WIDTH - 24),
                    textX, textY, MUTED_TEXT, false);

            if (page == Page.LIST) {
                drawListPage(graphics, mc, x, y, textX, textY + 30);
            } else {
                drawCreatePage(graphics, mc, x, y, textX, textY + 20);
            }
        }

        private void drawListPage(GuiGraphics graphics, Minecraft mc, int panelX, int panelY, int textX, int textY) {
            graphics.drawString(mc.font, "Socket Final State", textX, textY, TEXT, false);
            graphics.drawString(mc.font, blockDisplayName(selectedHostBlockId()), textX + 26, textY + 22,
                    TEXT, false);
            graphics.drawString(mc.font, fit(safeHostFinalState(), 220), textX + 26, textY + 33,
                    MUTED_TEXT, false);
            textY += 58;
            graphics.drawString(mc.font, "Existing Insert Slots", textX, textY, TEXT, false);
            textY += 13;
            List<MKWorkspaceInsertFamilyDefinition> insertFamilies = workspace.insertFamilies().stream()
                    .filter(family -> family.kind() == MKWorkspaceInsertFamilyKind.INSERT_SOCKET)
                    .toList();
            if (insertFamilies.isEmpty()) {
                graphics.drawString(mc.font, "No insert socket families are defined for this workspace.", textX,
                        textY, MUTED_TEXT, false);
                return;
            }
            for (MKWorkspaceInsertFamilyDefinition family : insertFamilies) {
                boolean fits = fitsFamily(family);
                int color = fits ? OK_TEXT : ERROR_TEXT;
                String status = fitStatus(family, fits);
                graphics.drawString(mc.font, fit(family.familyId() + "  " + family.width() + "x" +
                        family.height() + "x" + family.depth() + "  " + status, 420), textX, textY,
                        color, false);
                textY += 22;
                if (textY > panelY + PANEL_HEIGHT - 18) {
                    graphics.drawString(mc.font, "...", textX, textY, MUTED_TEXT, false);
                    break;
                }
            }
        }

        private void drawCreatePage(GuiGraphics graphics, Minecraft mc, int panelX, int panelY, int textX, int textY) {
            drawInsertDiagram(graphics, mc, panelX + PANEL_WIDTH - 188, panelY + 52, 160, 114);
            graphics.drawString(mc.font, "Family Id", textX, textY + 4, MUTED_TEXT, false);
            graphics.drawString(mc.font, "Dimensions", textX, textY + 33, TEXT, false);
            graphics.drawString(mc.font, "Jigsaw Offset", textX, textY + 127, TEXT, false);
            graphics.drawString(mc.font, "Template final_state", textX, textY + 183, MUTED_TEXT, false);
            graphics.drawString(mc.font, fit(blockDisplayName(selectedTemplateBlockId()).getString(), 190),
                    panelX + 354, textY + 183, TEXT, false);
            String status = createFitStatus();
            graphics.drawString(mc.font, fit(status, 220), textX, textY + 208,
                    "footprint clear".equals(status) ? OK_TEXT : ERROR_TEXT, false);
            if (createButton != null) {
                createButton.setEnabled(canCreate());
            }
        }
    }
}
