package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleKeyFrame;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.lwjgl.glfw.GLFW;

public class ParticleKeyFrameView extends MKScrollView {
    private ParticleAnimation animation;
    private static final int MIN_SIZE = 10;
    private static final int SPACE_PER_TICK = 2;
    private static final int GRID_INTERVAL = 5;
    private final LongitudinalGridStackLayoutVertical layout;
    private final Font font;
    private final ParticleEditorScreen editor;


    public ParticleKeyFrameView(int x, int y, int width, int height, ParticleAnimation animation,
                                Font font, ParticleEditorScreen editor) {
        super(x, y, width, height, true);
        this.animation = animation;
        this.font = font;
        this.editor = editor;
        this.layout = new LongitudinalGridStackLayoutVertical(x, y, SPACE_PER_TICK * GRID_INTERVAL * 100, 1, SPACE_PER_TICK * GRID_INTERVAL, 100,
                0x88ffffff, font);
        layout.setPaddings(2, 2, 2, 2);
        layout.setMarginRight(2);
        addWidget(layout);
        setScrollMarginX(50);
        setScrollMarginY(50);
        setScrollLock(true);


        setup();
    }

    public void setAnimation(ParticleAnimation animation) {
        this.animation = animation;
        setup();
    }

    public ParticleAnimation getAnimation() {
        return animation;
    }

    @Override
    public boolean onMousePressed(Minecraft minecraft, double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_2) {
            editor.selectKeyFrame(null);
            return true;
        }
        return super.onMousePressed(minecraft, mouseX, mouseY, mouseButton);
    }

    public void setup() {
        layout.clearWidgets();
        if (animation != null) {
            int totalTicks = Math.max(animation.getTickLength(), 1);
            layout.setGridCount(Math.max(totalTicks / 5, 50));
            int totalSpace = Math.max(layout.getDesiredWidth(), getWidth());
            layout.setWidth(totalSpace);
            for (ParticleKeyFrame keyFrame : animation.getKeyFrames()) {
                int duration = keyFrame.getDuration();
                int width = Math.max(duration * SPACE_PER_TICK + duration / GRID_INTERVAL, 5);
                int startX = layout.getGridPos(keyFrame.getTickStart() / 5); // * SPACE_PER_TICK + (keyFrame.getTickStart() - 1) / 4;
                ParticleKeyFrameWidget wid = new ParticleKeyFrameWidget(0, 0, width, 20, keyFrame, editor);
                layout.addWidget(wid);
                layout.addConstraintToWidget(new OffsetConstraint(startX + 2, 0, true, false), wid);
            }
        }
        MKButton addButton = new MKButton(0, 0, "Add");
        addButton.setPressedCallback((button, click) -> {
            if (animation != null) {
                ParticleKeyFrame newFrame = new ParticleKeyFrame();
                animation.addKeyFrame(newFrame);
                editor.selectKeyFrame(newFrame);
                setup();
            }
            return true;
        });
        layout.addWidget(addButton);
//        layout.manualRecompute();
        MKCore.LOGGER.info("Layout: {}, {}", layout.getWidth(), layout.getHeight());
    }

}
