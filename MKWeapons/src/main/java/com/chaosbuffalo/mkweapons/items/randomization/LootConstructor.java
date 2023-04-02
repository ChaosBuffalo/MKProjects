package com.chaosbuffalo.mkweapons.items.randomization;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapSerializer;
import com.chaosbuffalo.mkcore.utils.RandomCollection;
import com.chaosbuffalo.mkcore.utils.SerializationUtils;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.options.IRandomizationOption;
import com.chaosbuffalo.mkweapons.items.randomization.options.RandomizationOptionManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.templates.RandomizationTemplate;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class LootConstructor implements IDynamicMapSerializer {

    private ItemStack item;
    private LootSlot slot;
    private final List<IRandomizationOption> permanentOptions;
    private final List<IRandomizationOption> randomizedOptions;
    @Nullable
    private RandomizationTemplate template;

    public LootConstructor(ItemStack item, LootSlot slot, List<IRandomizationOption> permanentOptions){
        this.item = item;
        this.slot = slot;
        this.permanentOptions = new ArrayList<>();
        this.permanentOptions.addAll(permanentOptions);
        this.randomizedOptions = new ArrayList<>();
    }

    public LootConstructor(){
        this(ItemStack.EMPTY, LootSlotManager.INVALID, new ArrayList<>());
    }

    public void addTemplateOptions(RandomizationTemplate template, List<IRandomizationOption> options) {
        this.template = template;
        this.randomizedOptions.addAll(options);
    }

    public ItemStack constructItem(Random random, double difficulty){
        if (item.isEmpty() || slot.equals(LootSlotManager.INVALID)){
            return ItemStack.EMPTY;
        }
        ItemStack newItem = item.copy();
        for (IRandomizationOption option : permanentOptions){
            option.applyToItemStackForSlot(newItem, slot, difficulty);
        }
        if (template != null) {
            for (IRandomizationSlot randomizationSlot : template.getRandomizationSlots()) {
                if (!randomizationSlot.isPermanent()) {
                    List<IRandomizationOption> options = randomizedOptions.stream().filter(x ->
                            x.getSlot().equals(randomizationSlot) && x.isApplicableToItem(newItem))
                            .collect(Collectors.toList());
                    RandomCollection<IRandomizationOption> optionChoices = new RandomCollection<>();
                    for (IRandomizationOption option : options){
                        optionChoices.add(option.getWeight(), option);
                    }
                    if (optionChoices.size() > 0){
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


    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("slot"), ops.createString(slot.getName().toString()));
        builder.put(ops.createString("item"), SerializationUtils.serializeItemStack(ops, item));
        builder.put(ops.createString("permanentOptions"), ops.createList(permanentOptions.stream().map(option -> option.serialize(ops))));
        builder.put(ops.createString("randomizedOptions"), ops.createList(randomizedOptions.stream().map(option -> option.serialize(ops))));
        if (template != null) {
            builder.put(ops.createString("template"), template.serialize(ops));
        }
    }

    public <D> void deserialize(Dynamic<D> dynamic){
        List<IRandomizationOption> options = dynamic.get("permanentOptions").asList(
                RandomizationOptionManager::deserializeOption);
        this.permanentOptions.clear();
        for (IRandomizationOption option : options){
            if (option != null){
                this.permanentOptions.add(option);
            }
        }
        List<IRandomizationOption> randomOptions = dynamic.get("randomizedOptions").asList(
                RandomizationOptionManager::deserializeOption);
        this.randomizedOptions.clear();
        for (IRandomizationOption option : randomOptions) {
            if (option != null) {
                this.permanentOptions.add(option);
            }
        }
        template = dynamic.get("template").map(RandomizationTemplate::deserializeTemplate).result().orElse(null);
        item = dynamic.get("item").map(SerializationUtils::deserializeItemStack).result().orElse(ItemStack.EMPTY);
        slot = dynamic.get("slot").asString().result().map(
                slotName -> LootSlotManager.getSlotFromName(new ResourceLocation(slotName)))
                .orElse(LootSlotManager.INVALID);

    }
}
