package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.actions.BlockItemDragState;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;

public class MKBlockSlot extends MKWidget {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int SLOT_SIZE = 18;

    private ResourceLocation blockId;

    public MKBlockSlot() {
        this(0, 0);
    }

    public MKBlockSlot(int x, int y) {
        super(x, y, SLOT_SIZE, SLOT_SIZE);
        setCanFocus(true);
        setBlock(Blocks.AIR.builtInRegistryHolder().key().location());
    }

    public ResourceLocation getBlockId() {
        return blockId;
    }

    public void setBlock(ResourceLocation blockId) {
        Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(Blocks.AIR);
        this.blockId = BuiltInRegistries.BLOCK.getKey(block);
        updateTooltip();
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setBlock(Blocks.AIR.builtInRegistryHolder().key().location());
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseRelease(double mouseX, double mouseY, int mouseButton) {
        if (getScreen() != null && isInBounds(mouseX, mouseY)) {
            var dragState = getScreen().getDragState().orElse(null);
            if (dragState instanceof BlockItemDragState blockDragState) {
                setBlock(blockDragState.getBlockId());
                getScreen().clearDragState();
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY,
                     float partialTicks) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        graphics.blitSprite(SLOT_SPRITE, x, y, width, height);

        ItemStack blockStack = getDisplayStack();
        if (!blockStack.isEmpty()) {
            graphics.renderItem(blockStack, x + 1, y + 1);
            graphics.renderItemDecorations(mc.font, blockStack, x + 1, y + 1);
        }

        if (isHovered()) {
            graphics.fill(x, y, x + width, y + height, 0x55FFFFFF);
        }
    }

    private ItemStack getDisplayStack() {
        Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(Blocks.AIR);
        if (block == Blocks.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(block);
    }

    private void updateTooltip() {
        Block block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(Blocks.AIR);
        if (block == Blocks.AIR) {
            setTooltip(Component.literal("Drag a hotbar block here. Right-click to clear."));
        } else {
            setTooltip(Component.literal(blockId.toString()));
        }
    }
}
