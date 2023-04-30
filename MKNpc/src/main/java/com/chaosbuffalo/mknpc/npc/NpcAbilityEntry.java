package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class NpcAbilityEntry implements INBTSerializable<CompoundTag> {
    private MKAbilityInfo abilityInfo;
    private int priority;
    private double chance;

    public NpcAbilityEntry() {
        priority = 1;
        chance = 1.0;
    }

    public NpcAbilityEntry(MKAbilityInfo abilityInfo, int priority, double chance) {
        this.priority = priority;
        this.abilityInfo = abilityInfo;
        this.chance = chance;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public double getChance() {
        return chance;
    }

    public MKAbilityInfo getAbilityInfo() {
        return abilityInfo;
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        abilityInfo = MKAbilityInfo.deserialize(dynamic.get("ability"));
        chance = dynamic.get("chance").asDouble(1.0);
        priority = dynamic.get("priority").asInt(1);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createMap(ImmutableMap.of(
                ops.createString("priority"), ops.createInt(getPriority()),
                ops.createString("chance"), ops.createDouble(getChance()),
                ops.createString("ability"), abilityInfo.serialize(ops)
        ));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("ability", abilityInfo.serializeWithId());
        tag.putInt("priority", getPriority());
        tag.putDouble("chance", getChance());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        abilityInfo = MKAbilityInfo.fromIdTag(nbt.getCompound("ability"));
        priority = nbt.getInt("priority");
        chance = nbt.getDouble("chance");
    }
}
