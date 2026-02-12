package com.chaosbuffalo.mkcore.core.editor;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class PlayerEditorModule implements ISyncGroupProvider {
    private final SyncGroup syncGroup = new SyncGroup();
    private final ParticleEditorSyncComponent particleEditorData = new ParticleEditorSyncComponent();
    protected final MKPlayerData playerData;

    public PlayerEditorModule(MKPlayerData playerData) {
        this.playerData = playerData;
        syncGroup.addPrivate("particle_editor", particleEditorData);
    }

    @Override
    public SyncGroup getSyncGroup() {
        return syncGroup;
    }

    public ParticleEditorSyncComponent getParticleEditorData() {
        return particleEditorData;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        var context = new SyncContext(provider);
        Tag particlesTag = particleEditorData.writeFullValue(context, SyncVisibility.Private);
        if (particlesTag != null) {
            tag.put("particleEditor", particlesTag);
        }
        return tag;
    }

    public void deserialize(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("particleEditor")) {
            CompoundTag particlesTag = nbt.getCompound("particleEditor");
            var context = new SyncContext(provider);
            particleEditorData.handleUpdatePayload(context, particlesTag, SyncVisibility.Private);
            particleEditorData.markDirty();
        }
    }
}
