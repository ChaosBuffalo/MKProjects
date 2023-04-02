package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkwidgets.client.gui.instructions.HoveringTextInstruction;
import com.chaosbuffalo.mkwidgets.client.gui.math.Vec2i;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TalentButton extends MKButton {

    private static final ResourceLocation TALENT_SLOT_GRAPHIC = new ResourceLocation(MKCore.MOD_ID,
            "textures/talents/talent_slot.png");
    private static final ResourceLocation TALENT_SLOT_OVERLAY = new ResourceLocation(MKCore.MOD_ID,
            "textures/talents/talent_slot_complete.png");
    public static final int SLOT_WIDTH = 16;
    public static final int SLOT_HEIGHT = 16;
    private static final int OVERLAY_WIDTH = 2;
    private static final int OVERLAY_HEIGHT = 2;
    public static final int HEIGHT = 42;
    public static final int WIDTH = 42;
    public static final int SLOT_Y_OFFSET = 4;
    public static final int TEXT_OFFSET = 4;
    public static final int SLOT_X_OFFSET = (WIDTH - SLOT_WIDTH) / 2;
    private final List<Component> tooltip;

    public final int index;
    public final String line;
    public final TalentRecord record;

    public TalentButton(int index, String line, TalentRecord record,
                        int x, int y) {
        super(x, y, WIDTH, HEIGHT, record.getNode().getTalent().getTalentName());
        this.index = index;
        this.line = line;
        this.record = record;
        this.tooltip = new ArrayList<>();
        MKTalent talent = record.getNode().getTalent();
        tooltip.add(talent.getTalentName());
        tooltip.add(talent.getTalentTypeName());
        tooltip.add(talent.getTalentDescription(record));
    }


    @Override
    public boolean isInBounds(double x, double y) {
        if (this.skipBoundsCheck()) {
            return true;
        }
        return x >= this.getX() + SLOT_X_OFFSET &&
                y >= this.getY() + SLOT_Y_OFFSET &&
                x < this.getX() + SLOT_X_OFFSET + SLOT_WIDTH &&
                y < this.getY() + SLOT_Y_OFFSET + SLOT_HEIGHT;
    }


    @Override
    public void draw(PoseStack matrixStack, Minecraft minecraft, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible()) {
            Font fontrenderer = minecraft.font;
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, TALENT_SLOT_GRAPHIC);
            RenderSystem.enableBlend();
            mkBlitUVSizeSame(matrixStack, this.getX() + SLOT_X_OFFSET,
                    this.getY() + SLOT_Y_OFFSET,
                    0, 0,
                    SLOT_WIDTH, SLOT_HEIGHT,
                    SLOT_WIDTH, SLOT_HEIGHT);
            ResourceLocation icon;
            if (record.getRank() > 0) {
                icon = record.getNode().getTalent().getFilledIcon();
            } else {
                icon = record.getNode().getTalent().getIcon();
            }
            RenderSystem.setShaderTexture(0, icon);
            mkBlitUVSizeSame(matrixStack, this.getX() + SLOT_X_OFFSET,
                    this.getY() + SLOT_Y_OFFSET,
                    0, 0,
                    SLOT_WIDTH, SLOT_HEIGHT, SLOT_WIDTH, SLOT_HEIGHT);
            if (record.getRank() == record.getNode().getMaxRanks()) {
                RenderSystem.setShaderTexture(0, TALENT_SLOT_OVERLAY);
                mkBlitUVSizeSame(matrixStack,
                        this.getX() + SLOT_X_OFFSET - OVERLAY_WIDTH / 2,
                        this.getY() + SLOT_Y_OFFSET - OVERLAY_HEIGHT / 2,
                        0, 0,
                        SLOT_WIDTH + OVERLAY_WIDTH,
                        SLOT_HEIGHT + OVERLAY_HEIGHT,
                        SLOT_WIDTH + OVERLAY_WIDTH,
                        SLOT_HEIGHT + OVERLAY_HEIGHT);
            }
            int textColor = 14737632;
            if (!this.isEnabled()) {
                textColor = 10526880;
            } else if (isHovered()) {
                textColor = 16777120;
            }
//            drawCenteredString(matrixStack, fontrenderer, this.buttonText, this.getX() + this.getWidth() / 2,
//                    this.getY() + SLOT_Y_OFFSET + SLOT_HEIGHT + OVERLAY_HEIGHT + TEXT_OFFSET, textColor);
            int rank = record.getRank();
            int maxRank = record.getNode().getMaxRanks();
            int rankOffset = SLOT_Y_OFFSET + SLOT_HEIGHT + OVERLAY_HEIGHT + TEXT_OFFSET;
            mkFill(matrixStack, this.getX(), this.getY() + rankOffset - 2,
                    getX() + getWidth(), getY() + rankOffset + fontrenderer.lineHeight + 2,
                    0xff264747);
            String rankText = String.format("%d/%d", rank, maxRank);
            drawCenteredString(matrixStack, fontrenderer, rankText,
                    this.getX() + this.getWidth() / 2,
                    this.getY() + SLOT_Y_OFFSET + SLOT_HEIGHT + OVERLAY_HEIGHT + TEXT_OFFSET,
                    textColor);
            if (isHovered()) {
                if (getScreen() != null) {
                    getScreen().addPostRenderInstruction(new HoveringTextInstruction(tooltip,
                            getParentCoords(new Vec2i(mouseX, mouseY))));
                }
            }
        }
    }
}
