package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.options.IRandomizationOption;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplate;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplateEntry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LootItemTemplate {
    public static final Codec<LootItemTemplate> CODEC = RecordCodecBuilder.<LootItemTemplate>mapCodec(builder -> {
        return builder.group(
                LootSlot.CODEC.fieldOf("lootSlot").forGetter(LootItemTemplate::getLootSlot),
                RandomizationItemEntry.CODEC.listOf().fieldOf("potentialItems").forGetter(i -> i.potentialItems),
                IRandomizationOption.CODEC.listOf().fieldOf("options").forGetter(i -> i.options),
                RandomizationTemplateEntry.CODEC.listOf().fieldOf("templates").forGetter(i -> new ArrayList<>(i.templates.values()))
        ).apply(builder, LootItemTemplate::new);
    }).codec();

    private final LootSlot lootSlot;
    private final List<RandomizationItemEntry> potentialItems;
    private final List<IRandomizationOption> options;
    private final Map<ResourceLocation, RandomizationTemplateEntry> templates;

    private LootItemTemplate(LootSlot lootSlot, List<RandomizationItemEntry> potentialItems, List<IRandomizationOption> options,
                             List<RandomizationTemplateEntry> templates) {
        this.lootSlot = lootSlot;
        this.potentialItems = potentialItems;
        this.options = options;
        this.templates = new HashMap<>(templates.size());
        templates.forEach(x -> this.templates.put(x.template.getName(), x));
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

    public void addItem(Item item) {
        addItem(item, 1.0);
    }

    public void addItem(Item item, double weight) {
        addItemStack(new ItemStack(item), weight);
    }

    public void addItemStack(ItemStack item, double weight) {
        potentialItems.add(new RandomizationItemEntry(item, weight));
    }

    public void addRandomizationOption(IRandomizationOption option) {
        options.add(option);
    }

    public void addTemplate(RandomizationTemplate template, double weight) {
        this.templates.put(template.getName(), new RandomizationTemplateEntry(template, weight));
    }

    @Nullable
    public RandomizationTemplate getTemplate(ResourceLocation name) {
        RandomizationTemplateEntry entry = templates.get(name);
        return entry != null ? entry.template : null;
    }

    @Nullable
    public LootConstructor generateConstructorForTemplateName(RandomSource random, ResourceLocation templateName) {
        RandomizationTemplate template = getTemplate(templateName);
        if (template != null) {
            return generateConstructorForTemplate(random, template);
        } else {
            return null;
        }
    }

    @Nullable
    public LootConstructor generateConstructor(RandomSource random) {
        RandomizationTemplate template = chooseTemplate(random);
        if (template != null) {
            return generateConstructorForTemplate(random, template);
        } else {
            return null;
        }
    }

    public LootConstructor generateConstructorForTemplate(RandomSource random, RandomizationTemplate template) {
        ItemStack stack = chooseItem(random).copy();
        List<IRandomizationOption> chosenOptions = new ArrayList<>();
        for (IRandomizationSlot randomizationSlot : template.getRandomizationSlots()) {
            if (randomizationSlot.isPermanent()) {
                List<IRandomizationOption> options = this.options.stream().filter(x ->
                                x.getSlot().equals(randomizationSlot) && x.isApplicableToItem(stack))
                        .collect(Collectors.toList());
                RandomCollection<IRandomizationOption> optionChoices = new RandomCollection<>();
                for (IRandomizationOption option : options) {
                    optionChoices.add(option.getWeight(), option);
                }
                if (optionChoices.size() > 0) {
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

    public ItemStack chooseItem(RandomSource random) {
        if (potentialItems.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            RandomCollection<ItemStack> choices = new RandomCollection<>();
            for (RandomizationItemEntry entry : potentialItems) {
                choices.add(entry.weight, entry.item);
            }
            return choices.next(random);
        }
    }

    @Nullable
    public RandomizationTemplate chooseTemplate(RandomSource random) {
        RandomCollection<RandomizationTemplate> choices = new RandomCollection<>();
        for (RandomizationTemplateEntry entry : templates.values()) {
            choices.add(entry.weight, entry.template);
        }
        if (choices.size() > 0) {
            return choices.next(random);
        } else {
            return null;
        }

    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    public static <D> LootItemTemplate deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
