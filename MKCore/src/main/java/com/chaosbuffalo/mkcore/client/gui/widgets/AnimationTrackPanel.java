package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkwidgets.client.gui.constraints.OffsetConstraint;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKRectangle;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class AnimationTrackPanel extends MKStackLayoutVertical {

    private ParticleAnimationTrack track;
    private final ParticleAnimationTrack.AnimationTrackType trackType;
    private final Font font;
    private final ParticleEditorScreen particleEditor;

    public AnimationTrackPanel(int x, int y, int width, ParticleAnimationTrack.AnimationTrackType trackType,
                               Font font, ParticleEditorScreen particleEditor) {
        super(x, y, width);
        this.track = null;
        this.trackType = trackType;
        this.font = font;
        this.particleEditor = particleEditor;
        setupLayout();
    }

    public void setTrack(ParticleAnimationTrack track) {
        this.track = track;
        setupLayout();
    }

    protected void setupLayout() {
        clearWidgets();
        MKLayout header = getHeader();
        addWidget(header);
        MKRectangle divider = new MKRectangle(getX(), getY(), getWidth(), 1, 0xffffffff);
        addWidget(divider);
        if (track != null) {
            SerializableAttributeContainerPanel panel = new SerializableAttributeContainerPanel(
                    0, 0, getWidth(), track, font, (attr) -> particleEditor.markDirty());
            addWidget(panel);
            MKRectangle divider2 = new MKRectangle(getX(), getY(), getWidth(), 1, 0xffffffff);
            addWidget(divider2);
        }

    }

    protected MKLayout getHeader() {
        MKStackLayoutVertical headerLayout = new MKStackLayoutVertical(0, 0, getWidth());
        headerLayout.setPaddings(2, 2, 2, 2);
        headerLayout.setMargins(2, 2, 2, 2);
        Component trackName = getTrackName();
        MKText tracktextName = new MKText(font, trackName);
        tracktextName.setWidth(getWidth());
        tracktextName.setColor(0xffffffff);
        headerLayout.addWidget(tracktextName);
        if (track == null) {
            MKButton setTrack = new MKButton(0, 0, 75, 20, new TranslatableComponent("mkcore.particle_editor.add_track"));
            setTrack.setPressedCallback((button, click) -> particleEditor.promptAddTrack(trackType));
            headerLayout.addWidget(setTrack);
            headerLayout.addConstraintToWidget(new OffsetConstraint(10, 0, true, false), setTrack);
        } else {
            MKButton deleteTrack = new MKButton(0, 0, 75, 20, new TranslatableComponent("mkcore.particle_editor.delete_track"));
            deleteTrack.setPressedCallback((button, click) -> particleEditor.deleteTrackButton(trackType));
            headerLayout.addWidget(deleteTrack);
            headerLayout.addConstraintToWidget(new OffsetConstraint(10, 0, true, false), deleteTrack);
        }
        return headerLayout;
    }

    Component getTrackName() {
        Component trackName = track == null ? new TextComponent("Empty") : track.getDescription();
        switch (trackType) {
            case COLOR:
                return new TranslatableComponent("mkcore.particle_editor.track_type.color", trackName);
            case SCALE:
                return new TranslatableComponent("mkcore.particle_editor.track_type.scale", trackName);
            case MOTION:
                return new TranslatableComponent("mkcore.particle_editor.track_type.motion", trackName);
            case UNKNOWN:
            default:
                return new TranslatableComponent("mkcore.particle_editor.track_type.unknown", trackName);
        }
    }


}
