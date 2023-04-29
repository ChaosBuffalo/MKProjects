package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class AbilityIconText extends IconText {
    private final IAbilityScreen screen;
    private final MKAbilityInfo ability;

    public AbilityIconText(int x, int y, int height, Font font, int iconWidth, IAbilityScreen screen, MKAbilityInfo ability) {
        super(x, y, height, ability.getAbilityName(), ability.getAbilityIcon(), font, iconWidth, 1);
        this.screen = screen;
        this.ability = ability;
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (screen.allowsDraggingAbilities()) {
            screen.startDraggingAbility(ability, icon, this);
            return true;
        } else {
            return false;
        }
    }
}
