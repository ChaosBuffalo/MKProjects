package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.pickers.MKCreativePickerEntry;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MKCreativeGridPicker extends MKWidget {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    private static final int ROW_HEIGHT = SLOT_SIZE + SLOT_GAP;

    private List<MKCreativePickerEntry> entries = List.of();
    private ResourceLocation selectedId;
    private double scrollOffset;
    private Consumer<MKCreativePickerEntry> selectionCallback;

    public MKCreativeGridPicker(int x, int y, int width, int height) {
        super(x, y, width, height);
        setCanFocus(true);
    }

    public void setEntries(List<MKCreativePickerEntry> entries) {
        this.entries = List.copyOf(entries);
        clampScroll();
    }

    public void setSelectedId(ResourceLocation selectedId) {
        this.selectedId = selectedId;
    }

    public void setSelectionCallback(Consumer<MKCreativePickerEntry> selectionCallback) {
        this.selectionCallback = selectionCallback;
    }

    public void resetScroll() {
        scrollOffset = 0.0;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY,
                     float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        graphics.fill(x, y, x + width, y + height, 0x66000000);

        int columns = columnCount();
        if (columns <= 0) {
            return;
        }

        enableScissor(x, y, width, height);
        int startRow = Math.max(0, (int) Math.floor(scrollOffset / ROW_HEIGHT));
        int endRow = Math.min(rowCount(), startRow + (height / ROW_HEIGHT) + 2);
        int baseY = y - (int) Math.round(scrollOffset);
        for (int row = startRow; row < endRow; row++) {
            for (int column = 0; column < columns; column++) {
                int entryIndex = (row * columns) + column;
                if (entryIndex >= entries.size()) {
                    break;
                }
                int slotX = x + (column * ROW_HEIGHT);
                int slotY = baseY + (row * ROW_HEIGHT);
                drawEntry(graphics, mc, entries.get(entryIndex), slotX, slotY, mouseX, mouseY);
            }
        }
        RenderSystem.disableScissor();

        if (entries.isEmpty()) {
            graphics.drawCenteredString(mc.font, Component.literal("No matching blocks"),
                    x + (width / 2), y + (height / 2) - 4, 0xAAAAAA);
        }
    }

    private void drawEntry(GuiGraphics graphics, Minecraft mc, MKCreativePickerEntry entry, int slotX, int slotY,
                           int mouseX, int mouseY) {
        graphics.blitSprite(SLOT_SPRITE, slotX, slotY, SLOT_SIZE, SLOT_SIZE);
        if (entry.id().equals(selectedId)) {
            graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x7733AAFF);
        }
        graphics.renderItem(entry.displayStack(), slotX + 1, slotY + 1);
        graphics.renderItemDecorations(mc.font, entry.displayStack(), slotX + 1, slotY + 1);
        if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
            graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x55FFFFFF);
        }
    }

    @Override
    public void longHoverDraw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX,
                              int mouseY, float partialTicks) {
        Optional<MKCreativePickerEntry> hovered = getEntryAt(mouseX, mouseY);
        if (hovered.isPresent() && getScreen() != null) {
            MKCreativePickerEntry entry = hovered.get();
            getScreen().addPostRenderInstruction(new HoveringTextInstruction(
                    List.of(entry.displayName(), Component.literal(entry.id().toString())),
                    new Vec2i(mouseX, mouseY)
            ));
        }
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        Optional<MKCreativePickerEntry> entry = getEntryAt(mouseX, mouseY);
        if (entry.isPresent() && selectionCallback != null) {
            selectionCallback.accept(entry.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseScrollWheel(Minecraft minecraft, double mouseX, double mouseY, double pScrollX,
                                      double pScrollY) {
        scrollOffset -= pScrollY * ROW_HEIGHT * 2;
        clampScroll();
        return true;
    }

    private Optional<MKCreativePickerEntry> getEntryAt(double mouseX, double mouseY) {
        if (!isInBounds(mouseX, mouseY)) {
            return Optional.empty();
        }
        int column = (int) ((mouseX - getX()) / ROW_HEIGHT);
        int row = (int) ((mouseY - getY() + scrollOffset) / ROW_HEIGHT);
        int index = (row * columnCount()) + column;
        if (index < 0 || index >= entries.size()) {
            return Optional.empty();
        }
        return Optional.of(entries.get(index));
    }

    private int columnCount() {
        return Math.max(1, (getWidth() + SLOT_GAP) / ROW_HEIGHT);
    }

    private int rowCount() {
        int columns = columnCount();
        return (int) Math.ceil(entries.size() / (double) columns);
    }

    private void clampScroll() {
        double max = Math.max(0, (rowCount() * ROW_HEIGHT) - getHeight());
        if (scrollOffset < 0) {
            scrollOffset = 0;
        } else if (scrollOffset > max) {
            scrollOffset = max;
        }
    }

    private void enableScissor(int x, int y, int width, int height) {
        int y1 = Minecraft.getInstance().getWindow().getGuiScaledHeight() - y - height;
        double scaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
        RenderSystem.enableScissor((int) Math.round(x * scaleFactor), (int) Math.round(y1 * scaleFactor),
                (int) Math.round(width * scaleFactor), (int) Math.round(height * scaleFactor));
    }
}
