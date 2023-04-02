package com.chaosbuffalo.mkcore.client.gui.widgets;

import com.chaosbuffalo.mkcore.client.gui.ParticleEditorScreen;
import com.chaosbuffalo.mkcore.fx.particles.spawn_patterns.ParticleSpawnPattern;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.MKButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;

public class ParticleSpawnPatternWidget extends MKStackLayoutVertical {
    private ParticleSpawnPattern spawnPattern;
    private final Font font;
    private final ParticleEditorScreen editor;

    public ParticleSpawnPatternWidget(int x, int y, int width, ParticleSpawnPattern spawnPattern, Font font,
                                      ParticleEditorScreen editor) {
        super(x, y, width);
        this.spawnPattern = spawnPattern;
        this.font = font;
        this.editor = editor;
        setMargins(4, 4, 4, 4);
        setPaddings(0, 0, 1, 1);
        setup();
    }

    public void setSpawnPattern(ParticleSpawnPattern spawnPattern) {
        this.spawnPattern = spawnPattern;
        setup();
    }

    public void setup() {
        clearWidgets();
        if (spawnPattern != null) {
            SerializableAttributeContainerPanel panel = new SerializableAttributeContainerPanel(
                    getX(), getY(), getWidth(), spawnPattern, font, (attr) -> editor.markDirty());
            addWidget(panel);
            MKButton change = new MKButton(0, 0, spawnPattern.getDescription());
            change.setPressedCallback((button, click) -> {
                editor.promptSetSpawnPattern();
                return true;
            });
            addWidget(change);
        } else {
            MKButton addButton = new MKButton(getX(), getY(), I18n.get("mkcore.particle_editor.set_spawn_pattern"));
            addButton.setPressedCallback((but, click) -> {
                editor.promptSetSpawnPattern();
                return true;
            });
            addWidget(addButton);
        }
        MKButton particleTypeButton = new MKButton(getX(), getY(), this.editor.getParticleName().toString());
        particleTypeButton.setPressedCallback((but, click) -> {
            editor.promptChangeParticleType();
            return true;
        });
        addWidget(particleTypeButton);
    }
}
