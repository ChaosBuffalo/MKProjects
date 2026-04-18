package com.chaosbuffalo.mkcore.core.editor;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.sync.v2.ISyncGroupProvider;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

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
        tag.put("particleEditor", particleEditorData.serializeStorage());
        return tag;
    }

    public void deserialize(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("particleEditor")) {
            particleEditorData.deserializeStorage(nbt.getCompound("particleEditor"));
            particleEditorData.markDirty();
        }
    }
}
