package com.chaosbuffalo.mkcore.abilities2;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.abilities2.runtime.PersistedAbilityRuntimeState;
import com.chaosbuffalo.mkcore.core.persona.IPersonaExtension;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class AbilityRuntimePersonaExtension implements IPersonaExtension {
    public static final ResourceLocation NAME = MKCore.makeRL("abilities2_runtime");

    private final Persona persona;
    private PersistedAbilityRuntimeState snapshot = PersistedAbilityRuntimeState.EMPTY;
    private boolean captureLiveRuntimeOnSerialize = false;

    public AbilityRuntimePersonaExtension(Persona persona) {
        this.persona = Objects.requireNonNull(persona, "persona");
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        if (persona.isActive() && (captureLiveRuntimeOnSerialize || snapshot.isEmpty())) {
            snapshot = MKCore.getAbilityRuntimeService().capturePersonaRuntime(persona);
        }
        return snapshot.isEmpty() ? null : snapshot.serialize(provider);
    }

    @Override
    public void deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        snapshot = PersistedAbilityRuntimeState.deserialize(provider, tag);
        captureLiveRuntimeOnSerialize = false;
    }

    public PersistedAbilityRuntimeState getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(PersistedAbilityRuntimeState snapshot) {
        this.snapshot = Objects.requireNonNull(snapshot, "snapshot");
    }

    public void setCaptureLiveRuntimeOnSerialize(boolean captureLiveRuntimeOnSerialize) {
        this.captureLiveRuntimeOnSerialize = captureLiveRuntimeOnSerialize;
    }
}
