package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import com.chaosbuffalo.mkwidgets.client.gui.actions.WidgetHoldingDragState;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYWithOffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class AbilityListEntry extends MKStackLayoutHorizontal {
    private final MKAbility ability;
    private final IAbilityScreen screen;
    private final MKImage icon;


    public AbilityListEntry(int x, int y, int height, Font font, IAbilityScreen screen, MKAbility ability) {
        super(x, y, height);
        this.ability = ability;
        this.screen = screen;
        setPaddingRight(2);
        setPaddingLeft(2);
        icon = new MKImage(0, 0, 16, 16, ability.getAbilityIcon()) {
            @Override
            public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
                if (screen.allowsDraggingAbilities()) {
                    screen.setDragState(new WidgetHoldingDragState(new MKImage(0, 0, icon.getWidth(),
                            icon.getHeight(), icon.getImageLoc())), this);
                    screen.startDraggingAbility(ability);
                    screen.setSelectedAbility(ability);
                    return true;
                }
                return false;
            }
        };
        addWidget(icon);
        MKText name = new MKText(font, ability.getAbilityName());
        name.setWidth(100);
        name.setColor(0xffffffff);
        addWidget(name);
        addConstraintToWidget(new CenterYWithOffsetConstraint(1), name);
    }

    @Override
    public void postDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()) {
            mkFill(matrixStack, x, y, x + width, y + height, 0x55ffffff);
        }
        if (ability.equals(screen.getSelectedAbility())) {
            mkFill(matrixStack, x, y, x + width, y + height, 0x99ffffff);
        }
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        screen.setSelectedAbility(ability);
        return true;
    }
}
