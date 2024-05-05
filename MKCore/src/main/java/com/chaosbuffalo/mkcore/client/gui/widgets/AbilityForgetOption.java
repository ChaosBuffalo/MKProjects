package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.MarginConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

public class AbilityForgetOption extends MKLayout {

    private final ForgetAbilityModal popup;
    private final ResourceLocation abilityId;

    public AbilityForgetOption(MKAbilityInfo abilityInfo, ForgetAbilityModal popup,
                               Font font) {
        super(0, 0, 200, 16);
        this.popup = popup;
        this.abilityId = abilityInfo.getId();
        IconText iconText = new IconText(0, 0, 16, abilityInfo.getAbility().getAbilityName(), abilityInfo.getAbility().getAbilityIcon(), font, 16, 1);
        this.addWidget(iconText);
        addConstraintToWidget(MarginConstraint.TOP, iconText);
        addConstraintToWidget(MarginConstraint.LEFT, iconText);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (popup.isForgetting(abilityId)) {
            popup.cancelForget(abilityId);
        } else {
            popup.forget(abilityId);
        }
        return true;
    }

    @Override
    public void postDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        boolean isForgetting = popup.isForgetting(abilityId);
        boolean hovered = isHovered();
        if (hovered || isForgetting) {
            int color = isForgetting ? 0x77ff8800 : 0x55ffffff;
            if (hovered && isForgetting) {
                color = 0xaaff8800;
            }
            mkFill(matrixStack, x, y, x + width, y + height, color);
        }
    }
}
