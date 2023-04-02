package com.chaosbuffalo.mkwidgets.client.gui.widgets;

import com.chaosbuffalo.mkwidgets.client.gui.math.IntColor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class MKImage extends MKWidget {
    private ResourceLocation imageLoc;
    private int texU;
    private int texV;
    private int texWidth;
    private int texHeight;
    private int sourceWidth;
    private int sourceHeight;
    protected IntColor color;

    public MKImage(int x, int y, int width, int height, ResourceLocation imageLoc) {
        this(x, y, width, height, width, height, 0, 0, width, height, imageLoc);
    }

    public MKImage(int x, int y, int width, int height, int sourceWidth,
                   int sourceHeight, int imageU, int imageV,
                   int imageWidth, int imageHeight,
                   ResourceLocation imageLoc){
        super(x, y, width, height);
        this.imageLoc = imageLoc;
        texU = imageU;
        texV = imageV;
        this.sourceHeight = sourceHeight;
        this.sourceWidth = sourceWidth;
        texWidth = imageWidth;
        texHeight = imageHeight;
        color = new IntColor(0xffffffff);
    }

    public MKImage setTexU(int value) {
        texU = value;
        return this;
    }

    public IntColor getColor() {
        return color;
    }

    public void setColor(IntColor color) {
        this.color = color;
    }

    public int getSourceWidth() {
        return sourceWidth;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public MKImage setSourceHeight(int sourceHeight) {
        this.sourceHeight = sourceHeight;
        return this;
    }

    public MKImage setSourceWidth(int sourceWidth) {
        this.sourceWidth = sourceWidth;
        return this;
    }

    public MKImage setTexV(int value) {
        texV = value;
        return this;
    }

    public MKImage setImageLoc(ResourceLocation imageLoc) {
        this.imageLoc = imageLoc;
        return this;
    }

    public ResourceLocation getImageLoc() {
        return imageLoc;
    }

    public int getTexU() {
        return texU;
    }

    public int getTexV() {
        return texV;
    }

    public MKImage setTexWidth(int value) {
        texWidth = value;
        return this;
    }

    public MKImage setTexHeight(int value) {
        texHeight = value;
        return this;
    }

    public int getTexWidth() {
        return texWidth;
    }

    public int getTexHeight() {
        return texHeight;
    }

    @Override
    public void draw(PoseStack matrixStack, Minecraft mc, int x, int y,
                     int width, int height, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(color.getRedF(), color.getBlueF(), color.getGreenF(), color.getAlphaF());
        RenderSystem.setShaderTexture(0, getImageLoc());
        mkBlitUVSizeDifferent(matrixStack, getX(), getY(), getWidth(),
                getHeight(), (float)getTexU(), (float)getTexV(), getTexWidth(), getTexHeight(), getSourceWidth(), getSourceHeight());
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
