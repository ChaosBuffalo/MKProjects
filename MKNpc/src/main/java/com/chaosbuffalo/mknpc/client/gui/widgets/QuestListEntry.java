package com.chaosbuffalo.mknpc.client.gui.widgets;

import com.chaosbuffalo.mknpc.client.gui.screens.QuestPage;
import com.chaosbuffalo.mknpc.quest.data.player.PlayerQuestChainInstance;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.CenterYWithOffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutHorizontal;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class QuestListEntry extends MKStackLayoutHorizontal {
    private final Font font;
    private final PlayerQuestChainInstance playerQuestChain;
    private final QuestPage screen;

    public QuestListEntry(int x, int y, int height, Font font, PlayerQuestChainInstance playerQuestChain,
                          QuestPage screen) {
        super(x, y, height);
        this.font = font;
        this.setPaddingRight(2);
        this.setPaddingLeft(2);
        this.setMarginLeft(6);
        this.screen = screen;
        this.playerQuestChain = playerQuestChain;
        MKText name = new MKText(font, playerQuestChain.getQuestName());
        name.setWidth(100);
        name.setColor(0xffffffff);
        this.addWidget(name);
        this.addConstraintToWidget(new CenterYWithOffsetConstraint(1), name);
    }

    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        this.screen.setCurrentQuest(playerQuestChain);
        return true;
    }

    public void postDraw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (this.isHovered()) {
            mkFill(matrixStack, x, y, x + width, y + height, 0x55ffffff);
        }
//
        if (playerQuestChain.equals(screen.getCurrentQuest())) {
            mkFill(matrixStack, x, y, x + width, y + height, 0x99ffffff);
        }
    }
}
