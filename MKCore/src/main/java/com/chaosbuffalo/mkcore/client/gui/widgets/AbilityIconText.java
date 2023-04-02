package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import com.chaosbuffalo.mkwidgets.client.gui.actions.WidgetHoldingDragState;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class AbilityIconText extends IconText {
    private final IAbilityScreen screen;
    private final MKAbility ability;

    public AbilityIconText(int x, int y, int height, Component text, ResourceLocation iconLoc,
                           Font font, int iconWidth, IAbilityScreen screen, MKAbility ability) {
        super(x, y, height, text, iconLoc, font, iconWidth, 1);
        this.screen = screen;
        this.ability = ability;
    }

    public AbilityIconText(int x, int y, int height, Font font, int iconWidth, IAbilityScreen screen, MKAbility ability) {
        super(x, y, height, ability.getAbilityName(), ability.getAbilityIcon(), font, iconWidth, 1);
        this.screen = screen;
        this.ability = ability;
    }

    private WidgetHoldingDragState createDragState() {
        return new WidgetHoldingDragState(new MKImage(0, 0, icon.getWidth(), icon.getHeight(), icon.getImageLoc()));
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (screen.allowsDraggingAbilities()) {
            screen.setDragState(createDragState(), this);
            screen.startDraggingAbility(ability);
            return true;
        } else {
            return false;
        }

    }
}
