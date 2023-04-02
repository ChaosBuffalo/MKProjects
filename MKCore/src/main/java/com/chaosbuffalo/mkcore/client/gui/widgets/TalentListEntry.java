package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.TalentPage;
import com.chaosbuffalo.mkcore.core.talents.TalentTreeRecord;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYWithOffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class TalentListEntry extends MKStackLayoutHorizontal {

    private final TalentTreeRecord record;
    private final TalentPage screen;

    public TalentListEntry(int x, int y, int height, Font font, TalentPage screen, TalentTreeRecord record) {
        super(x, y, height);
        this.record = record;
        this.screen = screen;
        setPaddingRight(2);
        setPaddingLeft(2);
        setMarginLeft(6);
        MKText name = new MKText(font, record.getTreeDefinition().getName());
        name.setWidth(100);
        name.setColor(0xffffffff);
        addWidget(name);
        addConstraintToWidget(new CenterYWithOffsetConstraint(1), name);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        screen.setCurrentTree(record);
        return true;
    }

    @Override
    public void postDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()) {
            mkFill(matrixStack, x, y, x + width, y + height, 0x55ffffff);
        }
        if (record.equals(screen.getCurrentTree())) {
            mkFill(matrixStack, x, y, x + width, y + height, 0x99ffffff);
        }
    }
}
