package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Image widget that draws only a percentage of its configured source region and output size.
 * <p>
 * This is commonly useful for bars, meters, and progress overlays.
 */
public class MKPercentageImage extends MKImage {

    public float widthPercentage;
    public float heightPercentage;

    /**
     * Creates a percentage image backed by a texture sub-region.
     */
    public MKPercentageImage(int x, int y, int width, int height, int sourceWidth, int sourceHeight,
                             int imageU, int imageV, int imageWidth, int imageHeight, ResourceLocation imageLoc) {
        super(x, y, width, height, sourceWidth, sourceHeight, imageU, imageV, imageWidth, imageHeight, imageLoc);
        this.widthPercentage = 1.0f;
        this.heightPercentage = 1.0f;
    }

    /**
     * Creates a percentage image that initially uses the full source texture.
     */
    public MKPercentageImage(int x, int y, int width, int height, ResourceLocation imageLoc) {
        super(x, y, width, height, imageLoc);
        this.widthPercentage = 1.0f;
        this.heightPercentage = 1.0f;
    }

    /**
     * Sets the horizontal draw fraction.
     *
     * @param widthPercentage fraction in the range typically {@code [0, 1]}
     * @return this widget
     */
    public MKPercentageImage setWidthPercentage(float widthPercentage) {
        this.widthPercentage = widthPercentage;
        return this;
    }

    /**
     * Sets the vertical draw fraction.
     *
     * @param heightPercentage fraction in the range typically {@code [0, 1]}
     * @return this widget
     */
    public MKPercentageImage setHeightPercentage(float heightPercentage) {
        this.heightPercentage = heightPercentage;
        return this;
    }

    public float getHeightPercentage() {
        return heightPercentage;
    }

    public float getWidthPercentage() {
        return widthPercentage;
    }

    @Override
    public void draw(GuiGraphics graphics, Minecraft mc, int x, int y, int width, int height, int mouseX,
                     int mouseY, float partialTicks) {
        graphics.setColor(color.getRedF(), color.getBlueF(), color.getGreenF(), color.getAlphaF());
        graphics.blit(getImageLoc(), getX(), getY(), Math.round(getWidth() * getWidthPercentage()),
                Math.round(getHeight() * getHeightPercentage()), (float) getTexU(), (float) getTexV(),
                Math.round(getTexWidth() * getWidthPercentage()), Math.round(getTexHeight() * getHeightPercentage()),
                getSourceWidth(), getSourceHeight());
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
