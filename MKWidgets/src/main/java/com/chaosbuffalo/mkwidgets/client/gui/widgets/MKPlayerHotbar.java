package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.chaosbuffalo.mkwidgets.client.gui.actions.BlockItemDragState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class MKPlayerHotbar extends MKWidget {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    private static final int SLOT_COUNT = 9;

    public MKPlayerHotbar() {
        this(0, 0);
    }

    public MKPlayerHotbar(int x, int y) {
        super(x, y, (SLOT_SIZE * SLOT_COUNT) + (SLOT_GAP * (SLOT_COUNT - 1)), SLOT_SIZE);
        setCanFocus(true);
        setTooltip(Component.literal("Drag blocks from the hotbar into the palette slots."));
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY,
                     float partialTicks) {
        if (mc.player == null) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        for (int i = 0; i < SLOT_COUNT; i++) {
            int slotX = x + (i * (SLOT_SIZE + SLOT_GAP));
            graphics.blitSprite(SLOT_SPRITE, slotX, y, SLOT_SIZE, SLOT_SIZE);
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, slotX + 1, y + 1);
                graphics.renderItemDecorations(mc.font, stack, slotX + 1, y + 1);
            }
            if (isMouseOverSlot(mouseX, mouseY, i)) {
                graphics.fill(slotX, y, slotX + SLOT_SIZE, y + SLOT_SIZE, 0x55FFFFFF);
            }
        }
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (minecraft.player == null || getScreen() == null) {
            return false;
        }
        int slotIndex = getSlotIndex(mouseX, mouseY);
        if (slotIndex < 0) {
            return false;
        }
        ItemStack stack = minecraft.player.getInventory().getItem(slotIndex);
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
        getScreen().setDragState(new BlockItemDragState(blockId), this);
        return true;
    }

    private boolean isMouseOverSlot(double mouseX, double mouseY, int slotIndex) {
        int slotX = getX() + (slotIndex * (SLOT_SIZE + SLOT_GAP));
        return mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= getY() && mouseY < getY() + SLOT_SIZE;
    }

    private int getSlotIndex(double mouseX, double mouseY) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (isMouseOverSlot(mouseX, mouseY, i)) {
                return i;
            }
        }
        return -1;
    }
}
