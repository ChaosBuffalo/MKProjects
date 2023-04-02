package com.chaosbuffalo.mknpc.npc;

import com.chaosbuffalo.mkcore.utils.SerializationUtils;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class NpcItemChoice implements INBTSerializable<CompoundTag> {
    public ItemStack item;
    public double weight;
    public float dropChance;

    public NpcItemChoice(ItemStack item, double weight, float dropChance) {
        this.item = item.isEmpty() ? item : item.copy();
        this.weight = weight;
        this.dropChance = dropChance;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setDropChance(float dropChance) {
        this.dropChance = dropChance;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public NpcItemChoice(){
        this(ItemStack.EMPTY, 1.0, 0.0f);
    }

    public static void livingEquipmentAssign(LivingEntity entity, EquipmentSlot slot, NpcItemChoice choice) {
        entity.setItemSlot(slot, choice.item.copy());
        if (entity instanceof Mob){
            Mob mobEntity = (Mob) entity;
            mobEntity.setDropChance(slot, choice.dropChance);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag itemTag = new CompoundTag();
        item.save(itemTag);
        tag.put("item", itemTag);
        tag.putDouble("weight", weight);
        tag.putFloat("dropChance", dropChance);
        return tag;
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        weight = dynamic.get("weight").asDouble(1.0);
        dropChance = dynamic.get("dropChance").asFloat(0.0f);
        item = dynamic.get("item").result().map(SerializationUtils::deserializeItemStack).orElse(ItemStack.EMPTY);
    }

    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createMap(ImmutableMap.of(
                ops.createString("weight"), ops.createDouble(weight),
                ops.createString("dropChance"), ops.createFloat(dropChance),
                ops.createString("item"), SerializationUtils.serializeItemStack(ops, item)
        ));
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        CompoundTag itemTag = nbt.getCompound("item");
        item = ItemStack.of(itemTag);
        weight = nbt.getDouble("weight");
        dropChance = nbt.getFloat("dropChance");
    }
}
