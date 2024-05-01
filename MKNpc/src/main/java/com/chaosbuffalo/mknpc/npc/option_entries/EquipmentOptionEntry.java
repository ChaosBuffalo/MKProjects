package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.options.EquipmentOption;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Map;

public class EquipmentOptionEntry implements INpcOptionEntry {
    public static final Codec<EquipmentOptionEntry> CODEC = Codec.unboundedMap(CommonCodecs.EQUIPMENT_SLOT_CODEC, NpcItemChoice.CODEC)
            .xmap(EquipmentOptionEntry::new, i -> i.itemChoices);

    private final Map<EquipmentSlot, NpcItemChoice> itemChoices;

    private EquipmentOptionEntry(Map<EquipmentSlot, NpcItemChoice> map) {
        itemChoices = new EnumMap<>(EquipmentSlot.class);
        itemChoices.putAll(map);
    }

    public EquipmentOptionEntry() {
        itemChoices = new EnumMap<>(EquipmentSlot.class);
    }

    @Override
    public ResourceLocation getOptionId() {
        return EquipmentOption.NAME;
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
            tag.put(entry.getKey().getName(), entry.getValue().serialize(NbtOps.INSTANCE));
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (String key : nbt.getAllKeys()) {
            EquipmentSlot type = EquipmentSlot.byName(key);
            NpcItemChoice newChoice = NpcItemChoice.CODEC.parse(NbtOps.INSTANCE, nbt.get(key)).getOrThrow(false, MKNpc.LOGGER::error);
            setSlotChoice(type, newChoice);
        }
    }
}
