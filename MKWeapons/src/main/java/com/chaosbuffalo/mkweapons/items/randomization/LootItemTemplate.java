package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapSerializer;
import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.options.IRandomizationOption;
import com.chaosbuffalo.mkweapons.items.randomization.options.RandomizationOptionManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplate;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplateEntry;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class LootItemTemplate implements IDynamicMapSerializer {
    private LootSlot lootSlot;
    private final List<RandomizationItemEntry> potentialItems;
    private final List<IRandomizationOption> options;
    private final Map<ResourceLocation, RandomizationTemplateEntry> templates;

    public LootItemTemplate() {
        this(LootSlotManager.INVALID);
    }

    public LootItemTemplate(LootSlot lootSlot) {
        this.lootSlot = lootSlot;
        this.potentialItems = new ArrayList<>();
        this.options = new ArrayList<>();
        this.templates = new HashMap<>();
    }

    public LootSlot getLootSlot() {
        return lootSlot;
    }

    public void addItem(Item item){
        addItem(item, 1.0);
    }

    public void addItem(Item item, double weight){
        addItemStack(new ItemStack(item), weight);
    }

    public void addItemStack(ItemStack item, double weight){
        potentialItems.add(new RandomizationItemEntry(item, weight));
    }

    public void addRandomizationOption(IRandomizationOption option){
        options.add(option);
    }

    public void addTemplate(RandomizationTemplate template, double weight){
        this.templates.put(template.getName(), new RandomizationTemplateEntry(template, weight));
    }

    @Nullable
    public RandomizationTemplate getTemplate(ResourceLocation name){
        RandomizationTemplateEntry entry = templates.get(name);
        return entry != null ? entry.template : null;
    }

    @Nullable
    public LootConstructor generateConstructorForTemplateName(Random random, ResourceLocation templateName){
        RandomizationTemplate template = getTemplate(templateName);
        if (template != null){
            return generateConstructorForTemplate(random, template);
        } else {
            return null;
        }
    }

    @Nullable
    public LootConstructor generateConstructor(Random random) {
        RandomizationTemplate template = chooseTemplate(random);
        if (template != null) {
            return generateConstructorForTemplate(random, template);
        } else {
            return null;
        }
    }

    public LootConstructor generateConstructorForTemplate(Random random, RandomizationTemplate template){
        ItemStack stack = chooseItem(random).copy();
        List<IRandomizationOption> chosenOptions = new ArrayList<>();
        for (IRandomizationSlot randomizationSlot : template.getRandomizationSlots()){
            if (randomizationSlot.isPermanent()) {
                List<IRandomizationOption> options = this.options.stream().filter(x ->
                        x.getSlot().equals(randomizationSlot) && x.isApplicableToItem(stack))
                        .collect(Collectors.toList());
                RandomCollection<IRandomizationOption> optionChoices = new RandomCollection<>();
                for (IRandomizationOption option : options){
                    optionChoices.add(option.getWeight(), option);
                }
                if (optionChoices.size() > 0){
                    chosenOptions.add(optionChoices.next(random));
                } else {
                    MKWeapons.LOGGER.debug("No choices for slot: {} in template: {} generated loot slot: {}",
                            randomizationSlot.getName(), template.getName(), lootSlot.getName());
                }
            }
        }
        LootConstructor constructor = new LootConstructor(stack, lootSlot, chosenOptions);
        List<IRandomizationSlot> templateSlots = template.getRandomizationSlots().stream()
                .filter(x -> !x.isPermanent()).collect(Collectors.toList());
        if (!templateSlots.isEmpty()) {
            constructor.addTemplateOptions(template, options);
        }
        return constructor;
    }


    public void addItemStack(ItemStack item) {
        addItemStack(item, 1.0);
    }

    public ItemStack chooseItem(Random random){
        if (potentialItems.isEmpty()){
            return ItemStack.EMPTY;
        } else {
            RandomCollection<ItemStack> choices = new RandomCollection<>();
            for (RandomizationItemEntry entry : potentialItems){
                choices.add(entry.weight, entry.item);
            }
            return choices.next(random);
        }
    }

    @Nullable
    public RandomizationTemplate chooseTemplate(Random random){
        RandomCollection<RandomizationTemplate> choices = new RandomCollection<>();
        for (RandomizationTemplateEntry entry : templates.values()){
            choices.add(entry.weight, entry.template);
        }
        if (choices.size() > 0) {
            return choices.next(random);
        } else {
            return null;
        }

    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        lootSlot = LootSlotManager.getSlotFromName(dynamic.get("lootSlot").asString().map(ResourceLocation::new).result().orElse(LootSlotManager.INVALID_LOOT_SLOT));
        List<RandomizationItemEntry> itemEntries = dynamic.get("potentialItems").asList(x -> {
            RandomizationItemEntry newEntry = new RandomizationItemEntry();
            newEntry.deserialize(x);
            return newEntry;
        });
        potentialItems.clear();
        potentialItems.addAll(itemEntries);
        List<IRandomizationOption> opts = dynamic.get("options").asList(RandomizationOptionManager::deserializeOption);
        options.clear();
        options.addAll(opts);
        List<RandomizationTemplateEntry> temps = dynamic.get("templates").asList(x -> {
            RandomizationTemplateEntry newEntry = new RandomizationTemplateEntry();
            newEntry.deserialize(x);
            return newEntry;
        });
        templates.clear();
        temps.stream().filter(x -> x.template != null).forEach(x -> templates.put(x.template.getName(), x));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> dynamicOps, ImmutableMap.Builder<D, D> builder) {
        builder.put(dynamicOps.createString("potentialItems"), dynamicOps.createList(potentialItems.stream().map(x -> x.serialize(dynamicOps))));
        builder.put(dynamicOps.createString("lootSlot"), dynamicOps.createString(lootSlot.getName().toString()));
        builder.put(dynamicOps.createString("options"), dynamicOps.createList(options.stream().map(x -> x.serialize(dynamicOps))));
        builder.put(dynamicOps.createString("templates"), dynamicOps.createList(templates.values().stream().map(x -> x.serialize(dynamicOps))));
    }
}
