package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Map;

public class EquipmentOptionEntry implements INpcOptionEntry {
    private final Map<EquipmentSlot, NpcItemChoice> itemChoices;

    public EquipmentOptionEntry() {
        itemChoices = new EnumMap<>(EquipmentSlot.class);
    }

    public void setSlotChoice(EquipmentSlot slot, NpcItemChoice choice) {
        itemChoices.put(slot, choice);
    }

    @Override
    public void applyToEntity(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            applyItemChoices(livingEntity);
        }
    }

    public void applyItemChoices(LivingEntity entity) {
        for (Map.Entry<EquipmentSlot, NpcItemChoice> entry : itemChoices.entrySet()) {
            entry.getValue().equip(entity, entry.getKey());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<EquipmentSlot, NpcItemChoice> entry : itemChoices.entrySet()) {
            tag.put(entry.getKey().getName(), entry.getValue().serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            EquipmentSlot type = EquipmentSlot.byName(key);
            NpcItemChoice newChoice = new NpcItemChoice();
            newChoice.deserializeNBT(nbt.getCompound(key));
            setSlotChoice(type, newChoice);
        }
    }
}
