package com.chaosbuffalo.mkcore.client.gui;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.client.gui.widgets.ParticleKeyFramePanel;
import com.chaosbuffalo.mkcore.client.gui.widgets.ParticleKeyFrameView;
import com.chaosbuffalo.mkcore.client.gui.widgets.ParticleSpawnPatternWidget;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.fx.particles.ParticleKeyFrame;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleColorAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleMotionAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleRenderScaleAnimationTrack;
import com.chaosbuffalo.mkcore.fx.particles.spawn_patterns.ParticleSpawnPattern;
import com.chaosbuffalo.mkcore.network.MKParticleEffectEditorSpawnPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.WriteAnimationPacket;
import com.chaosbuffalo.mkcore.utils.RayTraceUtils;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKLayout;
import com.chaosbuffalo.mkwidgets.client.gui.layouts.MKStackLayoutVertical;
import com.chaosbuffalo.mkwidgets.client.gui.math.IntColor;
import com.chaosbuffalo.mkwidgets.client.gui.screens.MKScreen;
import com.chaosbuffalo.mkwidgets.client.gui.widgets.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Map;

public class ParticleEditorScreen extends MKScreen {

    private ParticleAnimation editing;
    private ParticleKeyFramePanel currentPanel;
    private ParticleKeyFrameView frameView;
    private ParticleKeyFrame currentFrame;
    private ParticleSpawnPatternWidget spawnWidget;
    private ResourceLocation particleName;
    protected final int POPUP_WIDTH = 180;
    protected final int POPUP_HEIGHT = 201;
    protected final int KEY_EDITOR_WIDTH = 250;
    protected final int KEYFRAME_VIEW_HEIGHT = 150;
    private boolean dirty;

    public ParticleEditorScreen() {
        super(new TranslatableComponent("mk.editors.particle_editor.name"));
        editing = ParticleAnimationManager.ANIMATIONS.get(
                new ResourceLocation(MKCore.MOD_ID, "particle_anim.blue_magic")).copy();
        currentFrame = null;
        dirty = false;
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            MKCore.getPlayer(player).ifPresent(x -> {
                if (x.getEditor().getParticleEditorData().getAnimation() != null) {
                    this.editing = x.getEditor().getParticleEditorData().getAnimation();
                }
            });
        }
    }

    public ParticleKeyFrame getCurrentFrame() {
        return currentFrame;
    }

    public void setSpawnWidget(ParticleSpawnPatternWidget spawnWidget) {
        this.spawnWidget = spawnWidget;
    }

    public void setEditing(ParticleAnimation editing) {
        this.editing = editing;
        currentFrame = null;
        frameView.setAnimation(editing);
        currentPanel.setup();
        markDirty();

    }

    public void setSpawnPattern(ParticleSpawnPattern spawnPattern) {
        editing.setSpawnPattern(spawnPattern);
//        if (this.spawnWidget != null){
//            spawnWidget.setSpawnPattern(spawnPattern);
//            spawnWidget.setup();
//        }
        currentPanel.setup();
        markDirty();
    }

    public void markDirty() {
        this.dirty = true;
    }

    @Nullable
    public ParticleSpawnPattern getSpawnPattern() {
        return editing.getSpawnPattern();
    }

    @Override
    public void setupScreen() {
        super.setupScreen();
        addState("base", this::getBase);
        pushState("base");
    }

    public boolean deleteTrackButton(ParticleAnimationTrack.AnimationTrackType trackType) {
        currentFrame.deleteTrack(trackType);
        currentPanel.setup();
        markDirty();
        return true;
    }

    public void setTrackForType(ParticleAnimationTrack.AnimationTrackType type, ResourceLocation trackName) {
        if (currentFrame != null) {
            ParticleAnimationTrack track = ParticleAnimationManager.getAnimationTrack(trackName);
            switch (type) {
                case SCALE:
                    currentFrame.setScaleTrack((ParticleRenderScaleAnimationTrack) track);
                    break;
                case MOTION:
                    currentFrame.setMotionTrack((ParticleMotionAnimationTrack) track);
                    break;
                case COLOR:
                    currentFrame.setColorTrack((ParticleColorAnimationTrack) track);
                    break;
            }
            markDirty();
            currentPanel.setup();
        }
    }

    public void selectKeyFrame(ParticleKeyFrame frame) {
        currentFrame = frame;
        currentPanel.setParticleKeyFrame(frame);
        markDirty();
    }

    public ResourceLocation getParticleName() {
        return ForgeRegistries.PARTICLE_TYPES.getKey(editing.getParticleType());
    }

    public ParticleType<MKParticleData> getParticleType() {
        return editing.getParticleType();
    }

    public void setParticleType(ResourceLocation typeName, ParticleType<MKParticleData> particleType) {
        editing.setParticleType(particleType);
        this.particleName = typeName;
        if (this.spawnWidget != null) {
            spawnWidget.setup();
        }
        markDirty();
    }

    public void promptChangeParticleType() {
        int xPos = (KEY_EDITOR_WIDTH - POPUP_WIDTH) / 2;
        int yPos = (height - POPUP_HEIGHT) / 2;
        MKModal popup = new MKModal();
        MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                GuiTextures.BACKGROUND_180_200, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT);
        if (background != null) {
            background.setColor(new IntColor(0x99555555));
            popup.addWidget(background);
        }
        String promptText = I18n.get("mkcore.particle_editor.choose_particle_type");
        MKText prompt = new MKText(font, promptText, xPos + 6, yPos + 6);
        prompt.setColor(0xffffffff);
        prompt.setWidth(POPUP_WIDTH - 10);
        prompt.setMultiline(true);
        popup.addWidget(prompt);
        MKScrollView scrollview = new MKScrollView(xPos + 5, yPos + 40, POPUP_WIDTH - 10,
                POPUP_HEIGHT - 45, true);
        popup.addWidget(scrollview);
        MKStackLayoutVertical names = new MKStackLayoutVertical(0, 0, scrollview.getWidth());
        names.setPaddingBot(2);
        names.setPaddingTop(2);
        names.setMargins(2, 2, 2, 2);
        names.doSetChildWidth(true);
        ParticleAnimationManager.PARTICLE_TYPES_FOR_EDITOR.forEach((key, value) -> {
            MKButton button = new MKButton(0, 0, new TextComponent(key.toString()));
            button.setPressedCallback((but, click) -> {
                setParticleType(key, value);
                closeModal(popup);
                return true;
            });
            names.addWidget(button);
        });
        scrollview.addWidget(names);
        names.manualRecompute();
        scrollview.resetView();
        addModal(popup);
    }

    public boolean promptSetSpawnPattern() {
        int xPos = (KEY_EDITOR_WIDTH - POPUP_WIDTH) / 2;
        int yPos = (height - POPUP_HEIGHT) / 2;
        MKModal popup = new MKModal();
        MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                GuiTextures.BACKGROUND_180_200, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT);
        if (background != null) {
            background.setColor(new IntColor(0x99555555));
            popup.addWidget(background);
        }
        String promptText = I18n.get("mkcore.particle_editor.choose_spawn_pattern");
        MKText prompt = new MKText(font, promptText, xPos + 6, yPos + 6);
        prompt.setColor(0xffffffff);
        prompt.setWidth(POPUP_WIDTH - 10);
        prompt.setMultiline(true);
        popup.addWidget(prompt);
        MKScrollView scrollview = new MKScrollView(xPos + 5, yPos + 40, POPUP_WIDTH - 10,
                POPUP_HEIGHT - 45, true);
        popup.addWidget(scrollview);
        MKStackLayoutVertical names = new MKStackLayoutVertical(0, 0, scrollview.getWidth());
        names.setPaddingBot(2);
        names.setPaddingTop(2);
        names.setMargins(2, 2, 2, 2);
        names.doSetChildWidth(true);
        ParticleAnimationManager.SPAWN_PATTERN_DESERIALIZERS.keySet().forEach(x -> {
            ParticleSpawnPattern spawnPattern = ParticleAnimationManager.SPAWN_PATTERN_DESERIALIZERS.get(x).get();
            MKButton button = new MKButton(0, 0, spawnPattern.getDescription());
            button.setPressedCallback((but, click) -> {
                setSpawnPattern(spawnPattern);
                markDirty();
                closeModal(popup);
                return true;
            });
            names.addWidget(button);
        });
        scrollview.addWidget(names);
        names.manualRecompute();
        scrollview.resetView();
        addModal(popup);
        return true;
    }

    public void requestSpawn() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            Vec3 eyePos = player.getEyePosition(1.0f);
            Vec3 to = eyePos.add(player.getLookAngle().scale(4.0));
            HitResult result = RayTraceUtils.rayTraceBlocks(player, eyePos, to, true);
            if (result.getType() == HitResult.Type.BLOCK) {
                to = result.getLocation();
            }
            if (editing != null && editing.hasSpawnPattern()) {
                PacketHandler.sendMessageToServer(new MKParticleEffectEditorSpawnPacket(to, editing));
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public boolean promptAddTrack(ParticleAnimationTrack.AnimationTrackType trackType) {

        int xPos = (KEY_EDITOR_WIDTH - POPUP_WIDTH) / 2;
        int yPos = (height - POPUP_HEIGHT) / 2;
        MKModal popup = new MKModal();
        MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                GuiTextures.BACKGROUND_180_200, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT);
        if (background != null) {
            background.setColor(new IntColor(0x99555555));
            popup.addWidget(background);
        }
        String promptText = I18n.get("mkcore.particle_editor.choose_track");
        MKText prompt = new MKText(font, promptText, xPos + 6, yPos + 6);
        prompt.setColor(0xffffffff);
        prompt.setWidth(POPUP_WIDTH - 10);
        prompt.setMultiline(true);
        popup.addWidget(prompt);
        MKScrollView scrollview = new MKScrollView(xPos + 5, yPos + 40, POPUP_WIDTH - 10,
                POPUP_HEIGHT - 45, true);
        popup.addWidget(scrollview);
        MKStackLayoutVertical names = new MKStackLayoutVertical(0, 0, scrollview.getWidth());
        names.setPaddingBot(2);
        names.setPaddingTop(2);
        names.setMargins(2, 2, 2, 2);
        names.doSetChildWidth(true);
        ParticleAnimationManager.getTypeNamesForTrackType(trackType).forEach(x -> {
            MKButton button = new MKButton(0, 0, ParticleAnimationTrack.getDescriptionFromType(x));
            button.setPressedCallback((but, click) -> {
                setTrackForType(trackType, x);
                closeModal(popup);
                return true;
            });
            names.addWidget(button);
        });
        scrollview.addWidget(names);
        names.manualRecompute();
        scrollview.resetView();
        addModal(popup);
        return true;
    }

    public void updateFrameView() {
        frameView.setup();
    }

    protected MKLayout getBase() {
        MKLayout root = new MKLayout(0, 0, width, height);
        root.addWidget(getKeyFrameEditor());
        root.addWidget(getControlPanel());
        root.addWidget(getKeyFrameView());
        return root;
    }

    protected MKLayout getControlPanel() {
        MKLayout root = new MKLayout(0, height - KEYFRAME_VIEW_HEIGHT, KEY_EDITOR_WIDTH, KEYFRAME_VIEW_HEIGHT);
        MKRectangle background = new MKRectangle(root.getX(), root.getY(), root.getWidth(), root.getHeight(), 0xdd111111);
        root.addWidget(background);
        MKStackLayoutVertical layout = new MKStackLayoutVertical(root.getX(), root.getY(), root.getWidth());
        layout.setMargins(10, 10, 10, 10);
        layout.setPaddings(0, 0, 2, 2);
        root.addWidget(layout);

        MKButton spawn = new MKButton(0, 0, "Spawn");
        spawn.setPressedCallback((button, click) -> {
            requestSpawn();
            return true;
        });
        layout.addWidget(spawn);
        MKButton saveButton = new MKButton(0, 0, "Save");
        saveButton.setPressedCallback((btn, click) -> {
            savePromot();
            return true;
        });
        layout.addWidget(saveButton);
        MKButton loadButton = new MKButton(0, 0, "Load");
        loadButton.setPressedCallback((btn, click) -> {
            loadPrompt();
            return true;
        });
        layout.addWidget(loadButton);
        MKButton newButton = new MKButton(0, 0, "New");
        newButton.setPressedCallback((btn, click) -> {
            setEditing(new ParticleAnimation());
            return true;
        });
        layout.addWidget(newButton);
        return root;
    }

    public void savePromot() {
        int xPos = (width - POPUP_WIDTH) / 2;
        int yPos = (height - POPUP_HEIGHT) / 2;
        MKModal popup = new MKModal();
        MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                GuiTextures.BACKGROUND_180_200, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT);
        if (background != null) {
            background.setColor(new IntColor(0x99555555));
            popup.addWidget(background);
        }
        MKStackLayoutVertical layout = new MKStackLayoutVertical(xPos, yPos, POPUP_WIDTH);
        layout.setMargins(5, 5, 5, 5);
        layout.setPaddings(0, 0, 5, 5);
        String promptText = I18n.get("mkcore.particle_editor.prompt_save");
        MKText prompt = new MKText(font, promptText, 0, 0);
        prompt.setColor(0xffffffff);
        prompt.setWidth(POPUP_WIDTH - 10);
        prompt.setMultiline(true);
        layout.addWidget(prompt);
        MKTextFieldWidget textFieldWidget = new MKTextFieldWidget(font, xPos + 2, yPos + 24,
                POPUP_WIDTH - 10, font.lineHeight + 2, new TextComponent(promptText));
        textFieldWidget.getContainedWidget().setMaxLength(500);
        layout.addWidget(textFieldWidget);
        MKButton button = new MKButton(0, 0, "Save") {
            @Override
            public boolean isEnabled() {
                return super.isEnabled() && !textFieldWidget.getText().isEmpty();
            }
        };
        button.setWidth(POPUP_WIDTH - 10);
        button.setPressedCallback((btn, click) -> {
            ResourceLocation saveName = new ResourceLocation(textFieldWidget.getText());
            PacketHandler.sendMessageToServer(new WriteAnimationPacket(saveName, editing));
            closeModal(popup);
            return true;
        });
        layout.addWidget(button);
        popup.addWidget(layout);
        addModal(popup);
        scheduleNextTick(() -> {
            setFocus(textFieldWidget);
        });

    }


    public void loadPrompt() {
        int xPos = (width - POPUP_WIDTH) / 2;
        int yPos = (height - POPUP_HEIGHT) / 2;
        MKModal popup = new MKModal();
        MKImage background = GuiTextures.CORE_TEXTURES.getImageForRegion(
                GuiTextures.BACKGROUND_180_200, xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT);
        if (background != null) {
            background.setColor(new IntColor(0x99555555));
            popup.addWidget(background);
        }
        MKStackLayoutVertical layout = new MKStackLayoutVertical(xPos, yPos, POPUP_WIDTH);
        layout.setMargins(5, 5, 5, 5);
        layout.setPaddings(0, 0, 5, 5);
        MKScrollView scrollView = new MKScrollView(xPos, yPos, POPUP_WIDTH, POPUP_HEIGHT - 20, true);
        scrollView.addWidget(layout);
        popup.addWidget(scrollView);
        for (Map.Entry<ResourceLocation, ParticleAnimation> anim : ParticleAnimationManager.ANIMATIONS.entrySet()) {
            MKButton button = new MKButton(0, 0, anim.getKey().toString());
            button.setWidth(POPUP_WIDTH - 10);
            button.setPressedCallback((btn, click) -> {
                setEditing(anim.getValue().copy());
                closeModal(popup);
                return true;
            });
            layout.addWidget(button);
        }
        addModal(popup);
    }

    protected MKLayout getKeyFrameView() {
        MKLayout root = new MKLayout(KEY_EDITOR_WIDTH, height - KEYFRAME_VIEW_HEIGHT,
                width - KEY_EDITOR_WIDTH, KEYFRAME_VIEW_HEIGHT);
        MKRectangle background = new MKRectangle(root.getX(), root.getY(), root.getWidth(), root.getHeight(), 0xdd000000);
        root.addWidget(background);
        frameView = new ParticleKeyFrameView(root.getX(), root.getY(), root.getWidth(),
                root.getHeight(), editing, font, this);
        root.addWidget(frameView);
        return root;
    }

    public void deleteKeyFrame(ParticleKeyFrame frame) {
        if (editing != null) {
            editing.deleteKeyFrame(frame);
            currentPanel.setParticleKeyFrame(null);
            frameView.setup();
            markDirty();
        }
    }

    protected MKLayout getKeyFrameEditor() {
        MKLayout root = new MKLayout(0, 0, KEY_EDITOR_WIDTH, height - KEYFRAME_VIEW_HEIGHT);
        MKRectangle background = new MKRectangle(0, 0, root.getWidth(), root.getHeight(), 0xdd000000);
        ParticleKeyFramePanel panel = new ParticleKeyFramePanel(0, 0,
                root.getWidth(), root.getHeight(), currentFrame, font, this);
        currentPanel = panel;
        root.addWidget(background);
        root.addWidget(panel);
        return root;

    }

    @Override
    public void tick() {
        super.tick();
        if (dirty) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                MKCore.getPlayer(player).ifPresent(data -> {
                    data.getEditor().getParticleEditorData().update(editing, 0, true);
                });
                dirty = false;
            }
        }
    }
}
