package com.chaosbuffalo.mkcore.core.editor;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.IPlayerSyncComponentProvider;
import com.chaosbuffalo.mkcore.core.player.PlayerSyncComponent;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class PlayerEditorModule implements IPlayerSyncComponentProvider {
    private final PlayerSyncComponent sync = new PlayerSyncComponent("editor");
    private final ParticleEditorSyncComponent particleEditorData = new ParticleEditorSyncComponent();
    protected final MKPlayerData playerData;

    public PlayerEditorModule(MKPlayerData playerData) {
        this.playerData = playerData;
        addSyncPrivate("particle_editor", particleEditorData);
    }

    @Override
    public PlayerSyncComponent getSyncComponent() {
        return sync;
    }

    public ParticleEditorSyncComponent getParticleEditorData() {
        return particleEditorData;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        var context = new SyncContext(provider);
        Tag particlesTag = particleEditorData.writeFullValue(context);
        if (particlesTag != null) {
            tag.put("particleEditor", particlesTag);
        }
        return tag;
    }

    public void deserialize(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("particleEditor")) {
            CompoundTag particlesTag = nbt.getCompound("particleEditor");
            var context = new SyncContext(provider);
            particleEditorData.handleUpdatePayload(context, particlesTag);
            particleEditorData.markDirty();
        }
    }
}
