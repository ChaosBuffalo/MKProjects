package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class AbilityIconText extends IconText {
    private final IAbilityScreen screen;
    private final MKAbilityInfo abilityInfo;

    public AbilityIconText(int x, int y, int height, Font font, int iconWidth, IAbilityScreen screen, MKAbilityInfo abilityInfo) {
        super(x, y, height, abilityInfo.getAbility().getAbilityName(), abilityInfo.getAbility().getAbilityIcon(), font, iconWidth, 1);
        this.screen = screen;
        this.abilityInfo = abilityInfo;
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (screen.allowsDraggingAbilities()) {
            screen.startDraggingAbility(abilityInfo, icon, this);
            return true;
        } else {
            return false;
        }
    }
}
