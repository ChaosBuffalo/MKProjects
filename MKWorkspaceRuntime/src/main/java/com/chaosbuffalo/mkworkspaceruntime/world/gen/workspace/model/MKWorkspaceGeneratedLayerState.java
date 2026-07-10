package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceGeneratedLayerState(
        MKWorkspaceGeneratedLayer layer,
        int version,
        long sourceSettingsHash,
        long updatedAtEpochMillis,
        boolean locked,
        boolean dirty
) {
    public static final Codec<MKWorkspaceGeneratedLayerState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MKWorkspaceGeneratedLayer.CODEC.fieldOf("layer").forGetter(MKWorkspaceGeneratedLayerState::layer),
                    Codec.INT.fieldOf("version").forGetter(MKWorkspaceGeneratedLayerState::version),
                    Codec.LONG.fieldOf("sourceSettingsHash")
                            .forGetter(MKWorkspaceGeneratedLayerState::sourceSettingsHash),
                    Codec.LONG.fieldOf("updatedAtEpochMillis")
                            .forGetter(MKWorkspaceGeneratedLayerState::updatedAtEpochMillis),
                    Codec.BOOL.fieldOf("locked").forGetter(MKWorkspaceGeneratedLayerState::locked),
                    Codec.BOOL.fieldOf("dirty").forGetter(MKWorkspaceGeneratedLayerState::dirty)
            ).apply(instance, MKWorkspaceGeneratedLayerState::new)
    );

    public static MKWorkspaceGeneratedLayerState unlocked(MKWorkspaceGeneratedLayer layer, long sourceSettingsHash,
                                                          long updatedAtEpochMillis) {
        return new MKWorkspaceGeneratedLayerState(layer, 0, sourceSettingsHash, updatedAtEpochMillis, false, false);
    }

    public MKWorkspaceGeneratedLayerState withLocked(boolean locked) {
        return new MKWorkspaceGeneratedLayerState(layer, version, sourceSettingsHash, updatedAtEpochMillis, locked,
                dirty);
    }

    public MKWorkspaceGeneratedLayerState lock() {
        return new MKWorkspaceGeneratedLayerState(layer, version, sourceSettingsHash, updatedAtEpochMillis, true,
                dirty);
    }

    public MKWorkspaceGeneratedLayerState unlock() {
        return new MKWorkspaceGeneratedLayerState(layer, version, sourceSettingsHash, updatedAtEpochMillis, false,
                dirty);
    }

    public MKWorkspaceGeneratedLayerState markDirty() {
        return new MKWorkspaceGeneratedLayerState(layer, version, sourceSettingsHash, updatedAtEpochMillis, locked,
                true);
    }

    public MKWorkspaceGeneratedLayerState refreshed(long newSourceSettingsHash, long newUpdatedAtEpochMillis) {
        return new MKWorkspaceGeneratedLayerState(layer, version + 1, newSourceSettingsHash, newUpdatedAtEpochMillis,
                locked, false);
    }
}
