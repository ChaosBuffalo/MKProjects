package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.fx.particles.ParticleKeyFrame;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeHeightConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeWidthConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeXPosConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.LayoutRelativeYPosConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public class ParticleKeyFrameWidget extends MKLayout {
    private final ParticleKeyFrame keyFrame;
    private final ParticleEditorScreen editor;
    private final MKRectangle rect;

    public ParticleKeyFrameWidget(int x, int y, int width, int height, ParticleKeyFrame frame,
                                  ParticleEditorScreen editor) {
        super(x, y, width, height);
        this.editor = editor;
        this.keyFrame = frame;

        rect = new MKRectangle(x, y, width, height, 0xffaaaaaa);
        addWidget(rect);
        addConstraintToWidget(new LayoutRelativeXPosConstraint(0), rect);
        addConstraintToWidget(new LayoutRelativeYPosConstraint(0), rect);
        addConstraintToWidget(new LayoutRelativeWidthConstraint(1.0f), rect);
        addConstraintToWidget(new LayoutRelativeHeightConstraint(1.0f), rect);
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        MKCore.LOGGER.info("Clicked on key frame: {}, mouse {}, {}, pos: {}, {}", keyFrame, mouseX, mouseY, getX(), getY());
        editor.selectKeyFrame(keyFrame);
        return true;
    }

    @Override
    public void draw(PoseStack matrixStack, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY, float partialTicks) {
        if (isHovered()) {
            rect.setColor(0xffffffff);
        } else if (keyFrame.equals(editor.getCurrentFrame())) {
            rect.setColor(0xffeeeeee);
        } else {
            rect.setColor(0xff888888);
        }
        super.draw(matrixStack, mc, x, y, width, height, mouseX, mouseY, partialTicks);
    }
}
