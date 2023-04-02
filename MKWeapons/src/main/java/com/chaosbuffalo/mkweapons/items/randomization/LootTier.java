package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapSerializer;
import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.templates.LootItemTemplateEntry;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class LootTier implements IDynamicMapSerializer {
    private final ResourceLocation name;
    private final Map<LootSlot, List<LootItemTemplateEntry>> potentialItemsForSlot;
    private static final List<LootItemTemplateEntry> EMPTY_CHOICES = new ArrayList<>();

    public LootTier(ResourceLocation name){
        this.name = name;
        this.potentialItemsForSlot = new HashMap<>();
    }

    @Nullable
    public LootItemTemplate chooseItemTemplate(Random random, LootSlot slot){
        List<LootItemTemplateEntry> slotOptions = potentialItemsForSlot.getOrDefault(slot, EMPTY_CHOICES);
        if (slotOptions.isEmpty()){
            return null;
        } else {
            RandomCollection<LootItemTemplate> choices = new RandomCollection<>();
            for (LootItemTemplateEntry entry : slotOptions){
                choices.add(entry.weight, entry.template);
            }
            return choices.next(random);
        }
    }

    @Nullable
    public LootConstructor generateConstructorForSlot(Random random, LootSlot slot){
        LootItemTemplate template = chooseItemTemplate(random, slot);
        if (template == null){
            return null;
        } else {
            return template.generateConstructor(random);
        }
    }

    public void addItemTemplate(LootItemTemplate template, double weight) {
        potentialItemsForSlot.computeIfAbsent(template.getLootSlot(), x -> new ArrayList<>())
                .add(new LootItemTemplateEntry(template, weight));
    }


    public ResourceLocation getName() {
        return name;
    }


    public <D> void deserialize(Dynamic<D> dynamic){
        Map<LootSlot, List<LootItemTemplateEntry>> slotMap = dynamic.get("slotItems").asMap(
                (d -> d.asString().result().map(slotName -> LootSlotManager.getSlotFromName(new ResourceLocation(slotName)))
                                .orElse(LootSlotManager.INVALID)),
                (d -> d.asList(x -> {
                    LootItemTemplateEntry newEntry = new LootItemTemplateEntry();
                    newEntry.deserialize(x);
                    return newEntry;
                })));
        potentialItemsForSlot.clear();
        for (Map.Entry<LootSlot, List<LootItemTemplateEntry>> entry : slotMap.entrySet()){
            if (entry.getKey() != LootSlotManager.INVALID){
                potentialItemsForSlot.put(entry.getKey(), entry.getValue().stream().filter(x -> x.template != null)
                        .collect(Collectors.toList()));
            }
        }
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> dynamicOps, ImmutableMap.Builder<D, D> builder) {
        builder.put(dynamicOps.createString("slotItems"),
                dynamicOps.createMap(potentialItemsForSlot.entrySet().stream().map(lootSlotListEntry ->
                        Pair.of(lootSlotListEntry.getKey().getName(),
                                lootSlotListEntry.getValue().stream()
                                        .map(x -> x.serialize(dynamicOps))
                                        .collect(Collectors.toList()))
                ).collect(Collectors.toMap(pair -> dynamicOps.createString(pair.getFirst().toString()),
                        pair -> dynamicOps.createList(pair.getSecond().stream())))));
    }
}
