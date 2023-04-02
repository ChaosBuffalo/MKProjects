package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.GuiTextures;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.IMKWidget;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKPercentageImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKWidget;

public class OnScreenXpBarWidget extends MKWidget {
    private final MKImage background;
    private final MKPercentageImage yellowBar;
    private final MKPercentageImage blueBar;


    public OnScreenXpBarWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
        background = GuiTextures.CORE_TEXTURES.getImageForRegion(GuiTextures.XP_BAR_ON_SCREEN_BACKGROUND, x, y, width, height);
        yellowBar = GuiTextures.CORE_TEXTURES.getPercentageImageForRegion(GuiTextures.XP_BAR_ON_SCREEN_YELLOW,
                x + 2, y, width - 4, height);
        blueBar = GuiTextures.CORE_TEXTURES.getPercentageImageForRegion(GuiTextures.XP_BAR_ON_SCREEN_BLUE,
                x + 2, y, width - 4, height);
        addWidget(background);
        addWidget(yellowBar);
        addWidget(blueBar);
    }

    @Override
    public IMKWidget setX(int newX) {
        background.setX(newX);
        yellowBar.setX(newX + 2);
        blueBar.setX(newX + 2);
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
        if (ratio > 1.0f) {
            ratio = 1.0f;
        }
        float fifths = ratio;
        while (fifths > 0.2f) {
            fifths -= 0.2f;
        }
        yellowBar.setWidthPercentage(ratio);
        blueBar.setWidthPercentage(fifths / 0.2f);
    }
}
