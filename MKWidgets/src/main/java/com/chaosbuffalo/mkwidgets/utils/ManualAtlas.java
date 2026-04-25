package com.chaosbuffalo.mkwidgets.utils;

import com.chaosbuffalo.mkwidgets.MKWidgets;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKAbstractGui;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKPercentageImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Small helper for manually describing named regions inside a texture atlas.
 * <p>
 * This is useful when a UI uses a shared texture and wants lightweight lookup, drawing, and widget creation
 * by region name without introducing a more elaborate atlas system.
 */
public class ManualAtlas {

    public final ResourceLocation textureLoc;
    public final int width;
    public final int height;
    private final Map<String, TextureRegion> regions = new HashMap<>();

    /**
     * @param textureLoc atlas texture location
     * @param width full atlas width
     * @param height full atlas height
     */
    public ManualAtlas(ResourceLocation textureLoc, int width, int height) {
        this.textureLoc = textureLoc;
        this.width = width;
        this.height = height;
    }

    /**
     * Registers a named region within the atlas.
     *
     * @param regionName region lookup name
     * @param u source u coordinate
     * @param v source v coordinate
     * @param width region width
     * @param height region height
     * @return the created texture region
     */
    public TextureRegion addTextureRegion(String regionName, int u, int v, int width, int height) {
        TextureRegion region = new TextureRegion(regionName, u, v, width, height);
        this.regions.put(regionName, region);
        return region;
    }

    /**
     * Binds the atlas texture to the GUI shader texture slot.
     *
     * @param minecraft active client instance
     */
    public void bind(Minecraft minecraft) {
        RenderSystem.setShaderTexture(0, textureLoc);
    }

    /**
     * Looks up a named region in the atlas.
     *
     * @param regionName region name
     * @return matching region, or {@code null} if the name is unknown
     */
    @Nullable
    public TextureRegion getRegion(String regionName) {
        return regions.get(regionName);
    }

    /**
     * Draws the full named region at the given position.
     */
    public void drawRegionAtPos(GuiGraphics graphics, String regionName, int xPos, int yPos) {
        TextureRegion region = regions.get(regionName);
        if (region == null) {
            MKWidgets.LOGGER.info("Skip drawing region {} for manual atlas {}, region not found.", regionName, textureLoc);
            return;
        }
        graphics.blit(textureLoc, xPos, yPos, region.u, region.v, region.width, region.height, width, height);
    }

    /**
     * Draws only the left portion of a region.
     */
    public void drawRegionAtPosPartialWidth(GuiGraphics graphics, String regionName, int xPos, int yPos, int partialWidth) {
        TextureRegion region = regions.get(regionName);
        if (region == null) {
            MKWidgets.LOGGER.info("Skip drawing region {} for manual atlas {}, region not found.", regionName, textureLoc);
            return;
        }
        graphics.blit(textureLoc, xPos, yPos, region.u, region.v, partialWidth, region.height, width, height);
    }

    /**
     * Creates an {@link MKImage} configured to draw the named region.
     *
     * @return an image widget for the region, or {@code null} if the region does not exist
     */
    @Nullable
    public MKImage getImageForRegion(String regionName, int xPos, int yPos, int width, int height) {
        TextureRegion region = regions.get(regionName);
        if (region == null) {
            MKWidgets.LOGGER.info("Can't get MKImage for region: {} in manual atlas {}, region not found.", regionName, textureLoc);
            return null;
        }
        return new MKImage(xPos, yPos, width, height, this.width, this.height, region.u, region.v, region.width,
                region.height, textureLoc);
    }

    /**
     * Creates an {@link MKPercentageImage} configured to draw the named region.
     *
     * @return a percentage image widget for the region, or {@code null} if the region does not exist
     */
    @Nullable
    public MKPercentageImage getPercentageImageForRegion(String regionName, int xPos, int yPos, int width, int height) {
        TextureRegion region = regions.get(regionName);
        if (region == null) {
            MKWidgets.LOGGER.info("Can't get MKPercentageImage for region: {} in manual atlas {}, region not found.", regionName, textureLoc);
            return null;
        }
        return new MKPercentageImage(xPos, yPos, width, height, this.width, this.height, region.u, region.v, region.width,
                region.height, textureLoc);
    }

    /**
     * Computes the x offset required to center one region inside another.
     */
    public int getCenterXOffset(String regionName, String inRegion) {
        TextureRegion main = getRegion(regionName);
        TextureRegion other = getRegion(inRegion);
        if (main == null || other == null) {
            return 0;
        }
        return (other.width - main.width) / 2;
    }

    /**
     * Computes the y offset required to center one region inside another.
     */
    public int getCenterYOffset(String regionName, String inRegion) {
        TextureRegion main = getRegion(regionName);
        TextureRegion other = getRegion(inRegion);
        if (main == null || other == null) {
            return 0;
        }
        return (other.height - main.height) / 2;
    }
}
