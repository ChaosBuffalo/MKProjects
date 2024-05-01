package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.options.IRandomizationOption;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplate;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LootConstructor {

    private final ItemStack item;
    private final LootSlot slot;
    private final List<IRandomizationOption> permanentOptions;
    private final List<IRandomizationOption> randomizedOptions;
    @Nullable
    private RandomizationTemplate template;

    public LootConstructor(ItemStack item, LootSlot slot, List<IRandomizationOption> permanentOptions) {
        this.item = item;
        this.slot = slot;
        this.permanentOptions = new ArrayList<>();
        this.permanentOptions.addAll(permanentOptions);
        this.randomizedOptions = new ArrayList<>();
    }

    public void addTemplateOptions(RandomizationTemplate template, List<IRandomizationOption> options) {
        this.template = template;
        this.randomizedOptions.addAll(options);
    }

    public ItemStack constructItem(RandomSource random, double difficulty) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack newItem = item.copy();
        for (IRandomizationOption option : permanentOptions) {
            option.applyToItemStackForSlot(newItem, slot, difficulty);
        }
        if (template != null) {
            for (IRandomizationSlot randomizationSlot : template.getRandomizationSlots()) {
                if (!randomizationSlot.isPermanent()) {
                    List<IRandomizationOption> options = randomizedOptions.stream()
                            .filter(x -> x.getSlot().equals(randomizationSlot) && x.isApplicableToItem(newItem))
                            .toList();
                    RandomCollection<IRandomizationOption> optionChoices = new RandomCollection<>();
                    for (IRandomizationOption option : options) {
                        optionChoices.add(option.getWeight(), option);
                    }
                    if (optionChoices.size() > 0) {
                        IRandomizationOption opt = optionChoices.next(random);
                        opt.applyToItemStackForSlot(newItem, slot, difficulty);
                    } else {
                        MKWeapons.LOGGER.debug("No choices for randomizationSlot: {} in template: {} generated loot lootSlot: {}",
                                randomizationSlot.getName(), template.getName(), slot.getName());
                    }
                }
            }
        }
        return newItem;
    }
}
