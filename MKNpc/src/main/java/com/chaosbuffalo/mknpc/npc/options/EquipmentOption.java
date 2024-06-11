package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.options.binding.EquipmentOptionBoundValue;
import com.chaosbuffalo.mknpc.npc.options.binding.IBoundNpcOptionValue;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EquipmentOption extends BindingNpcOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "equipment");
    public static final Codec<EquipmentOption> CODEC = Codec.unboundedMap(CommonCodecs.EQUIPMENT_SLOT_CODEC, NpcItemChoice.CODEC.listOf())
            .xmap(EquipmentOption::new, i -> i.itemChoices);

    private final Map<EquipmentSlot, List<NpcItemChoice>> itemChoices;

    private EquipmentOption(Map<EquipmentSlot, List<NpcItemChoice>> itemChoices) {
        this();
        this.itemChoices.putAll(itemChoices);
    }

    public EquipmentOption() {
        super(NAME);
        itemChoices = new EnumMap<>(EquipmentSlot.class);
    }

    @Override
    protected IBoundNpcOptionValue generateBoundValue(NpcDefinition definition, RandomSource random) {
        EquipmentOptionBoundValue equipmentEntry = new EquipmentOptionBoundValue();
        for (Map.Entry<EquipmentSlot, List<NpcItemChoice>> entry : itemChoices.entrySet()) {
            RandomCollection<NpcItemChoice> slotChoices = new RandomCollection<>();
            for (NpcItemChoice choice : entry.getValue()) {
                slotChoices.add(choice.weight, choice);
            }
            equipmentEntry.setSlotChoice(entry.getKey(), slotChoices.next(random));
        }
        return equipmentEntry;
    }

    public EquipmentOption addItemChoice(EquipmentSlot slot, NpcItemChoice choice) {
        itemChoices.computeIfAbsent(slot, s -> new ArrayList<>()).add(choice);
        return this;
    }
}
