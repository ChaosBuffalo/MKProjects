package com.chaosbuffalo.mkcore.core.editor;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.ParticleAnimationEditorSyncPacket;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public class ParticleEditorSyncComponent implements ISyncObject {
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    private ParticleAnimation animation;
    private boolean dirty;
    private int currentFrame;

    public ParticleEditorSyncComponent() {
        this.currentFrame = -1;
        this.animation = null;
        this.dirty = false;
    }

    public ParticleAnimation getAnimation() {
        return animation;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void update(ParticleAnimation animation, int currentKeyFrame, boolean sync) {
        this.currentFrame = currentKeyFrame;
        setAnimationAndSpawn(animation, false);
        if (sync) {
            PacketHandler.sendMessageToServer(new ParticleAnimationEditorSyncPacket(animation, currentKeyFrame));
        }
    }

    public void setAnimationAndSpawn(ParticleAnimation animation, boolean flagDirty) {
        this.animation = animation;
        if (flagDirty) {
            markDirty();
        }
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context) {
        CompoundTag syncTag = new CompoundTag();
        if (animation != null) {
            syncTag.put("animation", animation.serialize(NbtOps.INSTANCE));
        }
        syncTag.putInt("currentKeyFrame", currentFrame);
        return syncTag;
    }

    @Override
    public @Nullable Tag writeUpdateValue(SyncContext context) {
        if (isDirty()) {
            dirty = false;
            return writeFullValue(context);
        }
        return null;
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
        if (valueTag instanceof CompoundTag syncTag) {
            if (syncTag.contains("animation")) {
                this.animation = ParticleAnimation.deserializeFromDynamic(
                        ParticleAnimationManager.RAW_EFFECT,
                        new Dynamic<>(NbtOps.INSTANCE, syncTag.getCompound("animation")));
            } else {
                this.animation = null;
            }
        }
    }
}
