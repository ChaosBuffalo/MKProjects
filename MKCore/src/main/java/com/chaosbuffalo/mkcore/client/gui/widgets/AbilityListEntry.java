package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.client.gui.IAbilityScreen;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYWithOffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKImage;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class AbilityListEntry extends MKStackLayoutHorizontal {
    private final MKAbilityInfo abilityInfo;
    private final IAbilityScreen screen;
    private final MKImage icon;

    public AbilityListEntry(int x, int y, int height, Font font, IAbilityScreen screen, MKAbilityInfo abilityInfo) {
        super(x, y, height);
        this.abilityInfo = abilityInfo;
        this.screen = screen;
        setPaddingRight(2);
        setPaddingLeft(2);
        icon = new MKImage(0, 0, 16, 16, abilityInfo.getAbilityIcon()) {
            @Override
            public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
                if (screen.allowsDraggingAbilities()) {
                    screen.startDraggingAbility(abilityInfo, icon, this);
                    screen.setSelectedAbility(abilityInfo);
                    return true;
                }
                return false;
            }
        };
        addWidget(icon);
        MKText name = new MKText(font, abilityInfo.getAbilityName());
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
        MKAbilityInfo selected = screen.getSelectedAbility();
        if (selected != null && abilityInfo.getId().equals(selected.getId())) {
            mkFill(matrixStack, x, y, x + width, y + height, 0x99ffffff);
        }
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        screen.setSelectedAbility(abilityInfo);
        return true;
    }
}
