package com.chaosbuffalo.mkwidgets.client.gui.instructions;

import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class DrawItemInstruction implements IInstruction {
    private final ItemStack stack;
    private final int mouseX;
    private final int mouseY;

    public DrawItemInstruction(ItemStack stack, int mouseX, int mouseY) {
        this.stack = stack;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public void draw(GuiGraphics graphics, Font renderer, int screenWidth, int screenHeight, float partialTicks, MKScreen screen) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        graphics.renderItem(stack, mouseX - 8, mouseY - 8);
        graphics.renderItemDecorations(renderer, stack, mouseX - 8, mouseY - 8);
        graphics.pose().popPose();
    }
}
