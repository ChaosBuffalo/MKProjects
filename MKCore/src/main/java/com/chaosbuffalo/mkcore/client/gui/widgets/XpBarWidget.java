package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKPercentageImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;

public class XpBarWidget extends MKWidget {
    private final MKImage background;
    private final MKPercentageImage yellowBar;
    private final MKPercentageImage blueBar;


    public XpBarWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        background = GuiTextures.CORE_TEXTURES.getImageForRegion(GuiTextures.XP_BAR_BACKGROUND, x, y, width, height);
        yellowBar = GuiTextures.CORE_TEXTURES.getPercentageImageForRegion(GuiTextures.XP_BAR_YELLOW,
                x + 4, y, width - 8, height);
        blueBar = GuiTextures.CORE_TEXTURES.getPercentageImageForRegion(GuiTextures.XP_BAR_BLUE,
                x + 4, y, width - 8, height);
        addWidget(background);
        addWidget(yellowBar);
        addWidget(blueBar);
    }

    @Override
    public IMKWidget setX(int newX) {
        background.setX(newX);
        yellowBar.setX(newX + 4);
        blueBar.setX(newX + 4);
        return super.setX(newX);
    }

    @Override
    public IMKWidget setY(int newY) {
        background.setY(newY);
        yellowBar.setY(newY);
        blueBar.setY(newY);
        return super.setY(newY);
    }

    public void syncPlayerXp(MKPlayerData playerData) {
        int currentXp = playerData.getTalents().getTalentXp();
        int nextLevel = playerData.getTalents().getXpToNextLevel();
        float ratio = (float) currentXp / (float) nextLevel;
        float fifths = ratio;
        while (fifths > 0.2f) {
            fifths -= 0.2f;
        }
        yellowBar.setWidthPercentage(ratio);
        blueBar.setWidthPercentage(fifths / 0.2f);
    }
}
