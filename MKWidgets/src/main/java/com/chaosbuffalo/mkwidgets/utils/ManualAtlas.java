package com.chaosbuffalo.mkwidgets.utils;

import com.chaosbuffalo.mkwidgets.MKWidgets;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKAbstractGui;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKPercentageImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ManualAtlas {

    public final ResourceLocation textureLoc;
    public final int width;
    public final int height;
    private final Map<String, TextureRegion> regions = new HashMap<>();

    public ManualAtlas(ResourceLocation textureLoc, int width, int height){
        this.textureLoc = textureLoc;
        this.width = width;
        this.height = height;
    }

    public TextureRegion addTextureRegion(String regionName, int u, int v, int width, int height){
        TextureRegion region = new TextureRegion(regionName, u, v, width, height);
        this.regions.put(regionName, region);
        return region;
    }

    public void bind(Minecraft minecraft){
        RenderSystem.setShaderTexture(0, textureLoc);
    }

    @Nullable
    public TextureRegion getRegion(String regionName){
        return regions.get(regionName);
    }

    public void drawRegionAtPos(PoseStack matrixStack, String regionName, int xPos, int yPos){
        TextureRegion region = regions.get(regionName);
        if (region == null){
            MKWidgets.LOGGER.info("Skip drawing region {} for manual atlas {}, region not found.", regionName, textureLoc);
            return;
        }
        MKAbstractGui.mkBlitUVSizeSame(matrixStack, xPos, yPos, region.u, region.v, region.width, region.height, width, height);
    }

    public void drawRegionAtPosPartialWidth(PoseStack matrixStack, String regionName, int xPos, int yPos, int partialWidth){
        TextureRegion region = regions.get(regionName);
        if (region == null){
            MKWidgets.LOGGER.info("Skip drawing region {} for manual atlas {}, region not found.", regionName, textureLoc);
            return;
        }
        MKAbstractGui.mkBlitUVSizeSame(matrixStack, xPos, yPos, region.u, region.v, partialWidth, region.height, width, height);
    }

    @Nullable
    public MKImage getImageForRegion(String regionName, int xPos, int yPos, int width, int height){
        TextureRegion region = regions.get(regionName);
        if (region == null){
            MKWidgets.LOGGER.info("Can't get MKImage for region: {} in manual atlas {}, region not found.", regionName, textureLoc);
            return null;
        }
        return new MKImage(xPos, yPos, width, height, this.width, this.height, region.u, region.v, region.width,
                region.height, textureLoc);
    }

    @Nullable
    public MKPercentageImage getPercentageImageForRegion(String regionName, int xPos, int yPos, int width, int height){
        TextureRegion region = regions.get(regionName);
        if (region == null){
            MKWidgets.LOGGER.info("Can't get MKPercentageImage for region: {} in manual atlas {}, region not found.", regionName, textureLoc);
            return null;
        }
        return new MKPercentageImage(xPos, yPos, width, height, this.width ,this.height, region.u, region.v, region.width,
                region.height, textureLoc);
    }

    public int getCenterXOffset(String regionName, String inRegion){
        TextureRegion main = getRegion(regionName);
        TextureRegion other = getRegion(inRegion);
        if (main == null || other == null){
            return 0;
        }
        return (other.width - main.width) / 2;
    }

    public int getCenterYOffset(String regionName, String inRegion){
        TextureRegion main = getRegion(regionName);
        TextureRegion other = getRegion(inRegion);
        if (main == null || other == null){
            return 0;
        }
        return (other.height - main.height) / 2;
    }
}