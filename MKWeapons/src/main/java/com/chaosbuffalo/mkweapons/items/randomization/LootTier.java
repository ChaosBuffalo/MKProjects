package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mkweapons.MKWeaponsRegistry;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.templates.LootItemTemplateEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;

import javax.annotation.Nullable;
import java.util.*;

public class LootTier {
    public static final Codec<LootTier> DIRECT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(LootSlot.CODEC, LootItemTemplateEntry.CODEC.listOf()).fieldOf("slotItems").forGetter(LootTier::stableSortedMap)
    ).apply(builder, LootTier::new));

    public static final Codec<Holder<LootTier>> REFERENCE_CODEC = RegistryFixedCodec.create(MKWeaponsRegistry.LOOT_TIER_REGISTRY_KEY);
    public static final Codec<ResourceKey<LootTier>> KEY_CODEC = ResourceKey.codec(MKWeaponsRegistry.LOOT_TIER_REGISTRY_KEY);

    private final Map<LootSlot, List<LootItemTemplateEntry>> potentialItemsForSlot;

    private LootTier(Map<LootSlot, List<LootItemTemplateEntry>> map) {
        this.potentialItemsForSlot = map;
    }

    public LootTier() {
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
        List<LootItemTemplateEntry> slotOptions = potentialItemsForSlot.get(slot);
        if (slotOptions == null || slotOptions.isEmpty()) {
            return null;
        } else {
            RandomCollection<LootItemTemplate> choices = new RandomCollection<>();
            for (LootItemTemplateEntry entry : slotOptions) {
                choices.add(entry.weight(), entry.template());
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

    public Set<LootSlot> getSlots() {
        return Collections.unmodifiableSet(potentialItemsForSlot.keySet());
    }
}
