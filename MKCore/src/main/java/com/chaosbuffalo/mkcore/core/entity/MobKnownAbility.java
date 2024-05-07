package com.chaosbuffalo.mkcore.core.entity;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class MobKnownAbility implements IMKSerializable<CompoundTag> {
    public final MKAbilityInfo abilityInfo;
    public int priority;

    public MobKnownAbility(MKAbilityInfo abilityInfo, int priority) {
        this.abilityInfo = abilityInfo;
        this.priority = priority;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public ResourceLocation getId() {
        return abilityInfo.getId();
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("priority", priority);
        return tag;
    }

    @Override
    public boolean deserialize(CompoundTag tag) {
        priority = tag.getInt("priority");
        return true;
    }
}
