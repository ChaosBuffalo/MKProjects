package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class MKPercentageImage extends MKImage{

    public float widthPercentage;
    public float heightPercentage;

    public MKPercentageImage(int x, int y, int width, int height, int sourceWidth, int sourceHeight,
                             int imageU, int imageV, int imageWidth, int imageHeight, ResourceLocation imageLoc) {
        super(x, y, width, height, sourceWidth, sourceHeight, imageU, imageV, imageWidth, imageHeight, imageLoc);
        this.widthPercentage = 1.0f;
        this.heightPercentage = 1.0f;
    }

    public MKPercentageImage(int x, int y, int width, int height, ResourceLocation imageLoc) {
        super(x, y, width, height, imageLoc);
        this.widthPercentage = 1.0f;
        this.heightPercentage = 1.0f;
    }

    public MKPercentageImage setWidthPercentage(float widthPercentage) {
        this.widthPercentage = widthPercentage;
        return this;
    }

    public MKPercentageImage setHeightPercentage(float heightPercentage){
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
    public void draw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX,
                     int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(color.getRedF(), color.getBlueF(), color.getGreenF(), color.getAlphaF());
        RenderSystem.setShaderTexture(0, getImageLoc());
        mkBlitUVSizeDifferent(matrixStack, getX(), getY(), Math.round(getWidth() * getWidthPercentage()),
                Math.round(getHeight() * getHeightPercentage()), (float)getTexU(), (float)getTexV(),
                Math.round(getTexWidth() * getWidthPercentage()), Math.round(getTexHeight() * getHeightPercentage()),
                getSourceWidth(), getSourceHeight());
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
