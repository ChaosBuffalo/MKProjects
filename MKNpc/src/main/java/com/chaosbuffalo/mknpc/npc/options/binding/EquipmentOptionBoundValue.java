package com.chaosbuffalo.mknpc.npc.options.binding;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.options.EquipmentOption;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Map;

public class EquipmentOptionBoundValue implements IBoundNpcOptionValue {
    public static final Codec<EquipmentOptionBoundValue> CODEC = Codec.unboundedMap(CommonCodecs.EQUIPMENT_SLOT_CODEC, NpcItemChoice.CODEC)
            .xmap(EquipmentOptionBoundValue::new, i -> i.itemChoices);

    private final Map<EquipmentSlot, NpcItemChoice> itemChoices;

    private EquipmentOptionBoundValue(Map<EquipmentSlot, NpcItemChoice> map) {
        itemChoices = new EnumMap<>(EquipmentSlot.class);
        itemChoices.putAll(map);
    }

    public EquipmentOptionBoundValue() {
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
}
