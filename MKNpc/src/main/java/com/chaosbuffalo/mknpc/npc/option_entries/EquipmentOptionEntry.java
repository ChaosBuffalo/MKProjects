package com.chaosbuffalo.mknpc.npc.option_entries;

import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.NpcOptionEntryTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Map;

public class EquipmentOptionEntry implements INpcOptionEntry {
    public static final Codec<EquipmentOptionEntry> CODEC = Codec.unboundedMap(EquipmentSlot.CODEC, NpcItemChoice.CODEC)
            .xmap(EquipmentOptionEntry::new, i -> i.itemChoices);
    public static final MapCodec<EquipmentOptionEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.unboundedMap(EquipmentSlot.CODEC, NpcItemChoice.CODEC).fieldOf("equipment").forGetter(i -> i.itemChoices)
    ).apply(builder, EquipmentOptionEntry::new));

    private final Map<EquipmentSlot, NpcItemChoice> itemChoices;

    private EquipmentOptionEntry(Map<EquipmentSlot, NpcItemChoice> map) {
        itemChoices = new EnumMap<>(EquipmentSlot.class);
        itemChoices.putAll(map);
    }

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

    @Override
    public NpcOptionEntryType<? extends INpcOptionEntry> getType() {
        return NpcOptionEntryTypes.EQUIPMENT.get();
    }

    public void applyItemChoices(LivingEntity entity) {
        for (Map.Entry<EquipmentSlot, NpcItemChoice> entry : itemChoices.entrySet()) {
            entry.getValue().equip(entity, entry.getKey());
        }
    }
}
