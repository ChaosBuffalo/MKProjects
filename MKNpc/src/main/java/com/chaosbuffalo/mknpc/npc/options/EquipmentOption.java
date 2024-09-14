package com.chaosbuffalo.mknpc.npc.options;

import com.chaosbuffalo.mkcore.utils.CommonCodecs;
import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.npc.NpcDefinition;
import com.chaosbuffalo.mknpc.npc.NpcItemChoice;
import com.chaosbuffalo.mknpc.npc.NpcOptionTypes;
import com.chaosbuffalo.mknpc.npc.option_entries.EquipmentOptionEntry;
import com.chaosbuffalo.mknpc.npc.option_entries.INpcOptionEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class EquipmentOption extends WorldPermanentOption {
    public static final ResourceLocation NAME = MKNpc.id("equipment");
    public static final Codec<EquipmentOption> CODEC = Codec.unboundedMap(CommonCodecs.EQUIPMENT_SLOT_CODEC, NpcItemChoice.CODEC.listOf())
            .xmap(EquipmentOption::new, i -> i.itemChoices);
    public static final MapCodec<EquipmentOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.unboundedMap(CommonCodecs.EQUIPMENT_SLOT_CODEC, NpcItemChoice.CODEC.listOf())
                    .fieldOf("equipment").forGetter(i -> i.itemChoices)
    ).apply(builder, EquipmentOption::new));

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
    protected INpcOptionEntry makeOptionEntry(NpcDefinition definition, Level level, RandomSource random) {
        EquipmentOptionEntry equipmentEntry = new EquipmentOptionEntry();
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

    @Override
    public NpcOptionType<? extends NpcDefinitionOption> getType() {
        return NpcOptionTypes.EQUIPMENT.get();
    }
}
