package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.fx.particles.ParticleKeyFrame;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKScrollView;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.TextComponent;

public class ParticleKeyFramePanel extends MKScrollView {
    private ParticleKeyFrame particleKeyFrame;
    private final ParticleEditorScreen particleEditor;
    private final Font font;

    public ParticleKeyFramePanel(int x, int y, int width, int height, ParticleKeyFrame frame,
                                 Font font, ParticleEditorScreen particleEditor) {
        super(x, y, width, height, true);
        this.particleKeyFrame = frame;
        this.particleEditor = particleEditor;
        this.font = font;
        setup();
    }

    public void setParticleKeyFrame(ParticleKeyFrame particleKeyFrame) {
        this.particleKeyFrame = particleKeyFrame;
        setup();
    }

    public void setup() {
        clearWidgets();
        MKStackLayoutVertical layout = new MKStackLayoutVertical(getX(), getY(), getWidth());
        layout.setMargins(4, 0, 5, 5);
        layout.setPaddings(0, 0, 5, 5);
        addWidget(layout);
        particleEditor.setSpawnWidget(null);
        if (particleKeyFrame != null) {
            MKButton backButton = new MKButton(0, 0, "Back");
            backButton.setPressedCallback((btn, click) -> {
                particleEditor.selectKeyFrame(null);
                return true;
            });
            layout.addWidget(backButton);
            SerializableAttributeContainerPanel panel = new SerializableAttributeContainerPanel(
                    0, 0, getWidth(), particleKeyFrame, font, (attr) -> {
                particleEditor.updateFrameView();
            });
            layout.addWidget(panel);
            MKRectangle divider2 = new MKRectangle(getX(), getY(), getWidth(), 1, 0xffffffff);
            layout.addWidget(divider2);

            AnimationTrackPanel colorPanel = new AnimationTrackPanel(getX(), getY(), getWidth(),
                    ParticleAnimationTrack.AnimationTrackType.COLOR, font, particleEditor);
            if (particleKeyFrame.hasColorTrack()) {
                colorPanel.setTrack(particleKeyFrame.getColorTrack());
            }
            layout.addWidget(colorPanel);
            AnimationTrackPanel scalePanel = new AnimationTrackPanel(getX(), getY(), getWidth(),
                    ParticleAnimationTrack.AnimationTrackType.SCALE, font, particleEditor);
            if (particleKeyFrame.hasScaleTrack()) {
                scalePanel.setTrack(particleKeyFrame.getScaleTrack());
            }
            layout.addWidget(scalePanel);
            AnimationTrackPanel motionPanel = new AnimationTrackPanel(getX(), getY(), getWidth(),
                    ParticleAnimationTrack.AnimationTrackType.MOTION, font, particleEditor);
            if (particleKeyFrame.hasMotionTrack()) {
                motionPanel.setTrack(particleKeyFrame.getMotionTrack());
            }
            layout.addWidget(motionPanel);
            MKButton delete = new MKButton(0, 0, "Delete");
            delete.setPressedCallback((button, click) -> {
                particleEditor.deleteKeyFrame(particleKeyFrame);
                return true;
            });
            layout.addWidget(delete);
        } else {
            ParticleSpawnPatternWidget pattern = new ParticleSpawnPatternWidget(0, 0,
                    getWidth(), particleEditor.getSpawnPattern(), font, particleEditor);
            particleEditor.setSpawnWidget(pattern);
            layout.addWidget(pattern);
            MKText text = new MKText(font, new TextComponent("Click a current key frame or add a new one to edit"));
            text.setColor(0xffffffff);
            text.setWidth(layout.getWidth());
            text.setMultiline(true);
            layout.addWidget(text);
        }

    }
}
