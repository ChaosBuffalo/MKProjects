package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.option_entries.EquipmentOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.chaosbuffalo.mknpc.utils.RandomCollection;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class EquipmentOption extends WorldPermanentOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKNpc.MODID, "equipment");
    private final Map<EquipmentSlot, List<NpcItemChoice>> itemChoices;

    public EquipmentOption() {
        super(NAME);
        itemChoices = new HashMap<>();
    }

    @Override
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, Random random) {
        EquipmentOptionEntry equipmentEntry = new EquipmentOptionEntry();
        for (Map.Entry<EquipmentSlot, List<NpcItemChoice>> entry : itemChoices.entrySet()) {
            RandomCollection<NpcItemChoice> slotChoices = new RandomCollection<>();
            for (NpcItemChoice choice : entry.getValue()) {
                slotChoices.add(choice.weight, choice);
            }
            equipmentEntry.setSlotChoice(entry.getKey(), slotChoices.next());
        }
        return equipmentEntry;
    }

    public EquipmentOption addItemChoice(EquipmentSlot slot, NpcItemChoice choice) {
        if (!itemChoices.containsKey(slot)) {
            itemChoices.put(slot, new ArrayList<>());
        }
        itemChoices.get(slot).add(choice);
        return this;
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        Map<EquipmentSlot, List<NpcItemChoice>> newSlots = dynamic.get("slotOptions")
                .asMap(keyD -> EquipmentSlot.byName(keyD.asString("error")),
                        valueD -> valueD.asList(valD -> {
                            NpcItemChoice newChoice = new NpcItemChoice();
                            newChoice.deserialize(valD);
                            return newChoice;
                        }));
        itemChoices.clear();
        itemChoices.putAll(newSlots);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);

        ImmutableMap.Builder<D, D> mapBuilder = ImmutableMap.builder();
        itemChoices.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getName()))
                .forEach(e -> mapBuilder.put(
                        ops.createString(e.getKey().getName()),
                        ops.createList(e.getValue().stream().map(itemChoice -> itemChoice.serialize(ops)))
                ));

        builder.put(ops.createString("slotOptions"), ops.createMap(mapBuilder.build()));
    }
}
