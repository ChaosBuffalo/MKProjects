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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LootItemTemplate {
    public static final Codec<LootItemTemplate> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            LootSlot.CODEC.fieldOf("lootSlot").forGetter(LootItemTemplate::getLootSlot),
            RandomizationItemEntry.CODEC.listOf().fieldOf("potentialItems").forGetter(i -> i.potentialItems),
            IRandomizationOption.CODEC.listOf().fieldOf("options").forGetter(i -> i.options),
            RandomizationTemplateEntry.CODEC.listOf().fieldOf("templates").forGetter(i -> i.templates)
    ).apply(builder, LootItemTemplate::new));

    private final LootSlot lootSlot;
    private final List<RandomizationItemEntry> potentialItems;
    private final List<IRandomizationOption> options;
    private final List<RandomizationTemplateEntry> templates;

    private LootItemTemplate(LootSlot lootSlot, List<RandomizationItemEntry> potentialItems, List<IRandomizationOption> options,
                             List<RandomizationTemplateEntry> templates) {
        this.lootSlot = lootSlot;
        this.potentialItems = potentialItems;
        this.options = options;
        this.templates = List.copyOf(templates);
    }

    public LootItemTemplate(LootSlot lootSlot) {
        this.lootSlot = lootSlot;
        this.potentialItems = new ArrayList<>();
        this.options = new ArrayList<>();
        this.templates = new ArrayList<>();
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
        templates.add(new RandomizationTemplateEntry(template, weight));
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

    @Nullable
    public LootConstructor generateConstructorForTemplate(RandomSource random, RandomizationTemplate template) {
        if (potentialItems.isEmpty()) {
            // cannot construct if no item candidates
            return null;
        }
        ItemStack stack = chooseItem(random).copy();
        List<IRandomizationOption> chosenOptions = new ArrayList<>();
        List<IRandomizationSlot> templateSlots = new ArrayList<>();
        for (IRandomizationSlot randomizationSlot : template.getRandomizationSlots()) {
            if (randomizationSlot.isPermanent()) {
                RandomCollection<IRandomizationOption> optionChoices = new RandomCollection<>();
                for (IRandomizationOption option : this.options) {
                    if (option.getSlot().equals(randomizationSlot) && option.isApplicableToItem(stack)) {
                        optionChoices.add(option.getWeight(), option);
                    }
                }
                if (optionChoices.size() > 0) {
                    chosenOptions.add(optionChoices.next(random));
                } else {
                    MKWeapons.LOGGER.debug("No choices for slot: {} in template: {} generated loot slot: {}",
                            randomizationSlot.getName(), template.getName(), lootSlot.getName());
                }
            } else {
                templateSlots.add(randomizationSlot);
            }
        }
        LootConstructor constructor = new LootConstructor(stack, lootSlot, chosenOptions);
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
                choices.add(entry.weight(), entry.item());
            }
            return choices.next(random);
        }
    }

    @Nullable
    public RandomizationTemplate chooseTemplate(RandomSource random) {
        RandomCollection<RandomizationTemplate> choices = new RandomCollection<>();
        for (RandomizationTemplateEntry entry : templates) {
            choices.add(entry.weight(), entry.template());
        }
        if (choices.size() > 0) {
            return choices.next(random);
        } else {
            return null;
        }

    }

    public <D> D serialize(DynamicOps<D> ops) {
        return CODEC.encodeStart(ops, this).getOrThrow();
    }

    public static <D> LootItemTemplate deserialize(Dynamic<D> dynamic) {
        return CODEC.parse(dynamic).getOrThrow();
    }
}
