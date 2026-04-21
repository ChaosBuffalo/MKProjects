package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class MobKnownAbility implements IMKSerializable<CompoundTag> {
    private final ResourceLocation id;
    @Nullable
    private final MKAbilityInfo abilityInfo;
    private int priority;
    @Nullable
    private String activationId;

    public MobKnownAbility(MKAbilityInfo abilityInfo, int priority) {
        this(abilityInfo.getId(), abilityInfo, priority, null);
    }

    public MobKnownAbility(MKAbilityInfo abilityInfo, int priority, @Nullable String activationId) {
        this(abilityInfo.getId(), abilityInfo, priority, activationId);
    }

    public MobKnownAbility(ResourceLocation id, @Nullable MKAbilityInfo abilityInfo, int priority, @Nullable String activationId) {
        this.id = id;
        this.abilityInfo = abilityInfo;
        this.priority = priority;
        this.activationId = activationId;
    }

    @Nullable
    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean isAbilityDefinition() {
        return abilityInfo == null;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public @Nullable String getActivationId() {
        return activationId;
    }

    public void setActivationId(@Nullable String activationId) {
        this.activationId = activationId;
    }

    @Override
    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("priority", priority);
        if (activationId != null && !activationId.isBlank()) {
            tag.putString("activationId", activationId);
        }
        return tag;
    }

    @Override
    public boolean deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        priority = tag.getInt("priority");
        activationId = tag.contains("activationId") ? tag.getString("activationId") : null;
        return true;
    }
}
