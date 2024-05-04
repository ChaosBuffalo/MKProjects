package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.templates.LootItemTemplateEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.*;

public class LootTier {
    public static final Codec<LootTier> CODEC = RecordCodecBuilder.<LootTier>mapCodec(builder -> {
        return builder.group(
                ResourceLocation.CODEC.fieldOf("name").forGetter(i -> i.name),
                Codec.unboundedMap(LootSlot.CODEC, LootItemTemplateEntry.CODEC.listOf()).fieldOf("slotItems").forGetter(LootTier::stableSortedMap)
        ).apply(builder, LootTier::new);
    }).codec();

    private static final List<LootItemTemplateEntry> EMPTY_CHOICES = new ArrayList<>();
    private final ResourceLocation name;
    private final Map<LootSlot, List<LootItemTemplateEntry>> potentialItemsForSlot;

    private LootTier(ResourceLocation name, Map<LootSlot, List<LootItemTemplateEntry>> map) {
        this.name = name;
        this.potentialItemsForSlot = map;
    }

    public LootTier(ResourceLocation name) {
        this.name = name;
        this.potentialItemsForSlot = new HashMap<>();
    }

    private Map<LootSlot, List<LootItemTemplateEntry>> stableSortedMap() {
        Comparator<LootSlot> slotComparator = Comparator.comparing(LootSlot::getName);
        TreeMap<LootSlot, List<LootItemTemplateEntry>> sorted = new TreeMap<>(slotComparator);
        sorted.putAll(potentialItemsForSlot);
        return sorted;
    }

    @Nullable
    public LootItemTemplate chooseItemTemplate(RandomSource random, LootSlot slot) {
        List<LootItemTemplateEntry> slotOptions = potentialItemsForSlot.getOrDefault(slot, EMPTY_CHOICES);
        if (slotOptions.isEmpty()) {
            return null;
        } else {
            RandomCollection<LootItemTemplate> choices = new RandomCollection<>();
            for (LootItemTemplateEntry entry : slotOptions) {
                choices.add(entry.weight, entry.template);
            }
            return choices.next(random);
        }
    }

    @Nullable
    public LootConstructor generateConstructorForSlot(RandomSource random, LootSlot slot) {
        LootItemTemplate template = chooseItemTemplate(random, slot);
        if (template == null) {
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

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    public static <D> LootTier deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
