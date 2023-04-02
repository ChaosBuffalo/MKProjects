package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.serialization.IDynamicMapTypedSerializer;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlotManager;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseRandomizationOption implements IRandomizationOption{
    public static final ResourceLocation INVALID_OPTION = new ResourceLocation(MKWeapons.MODID, "randomization_option.error");
    private final ResourceLocation typeName;
    private IRandomizationSlot slot;
    private double weight;
    private static final String TYPE_ENTRY_NAME = "randomizationType";

    public BaseRandomizationOption(ResourceLocation typeName, IRandomizationSlot slot){
        this(typeName, slot, 1.0);
    }

    public BaseRandomizationOption(ResourceLocation typeName, IRandomizationSlot slot, double weight){
        this(typeName);
        this.slot = slot;
        this.weight = weight;
    }

    public BaseRandomizationOption(ResourceLocation typeName){
        this.typeName = typeName;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean isApplicableToItem(ItemStack stack) {
        return true;
    }

    @Override
    public void setSlot(IRandomizationSlot slot) {
        this.slot = slot;
    }


    @Override
    public IRandomizationSlot getSlot() {
        return slot;
    }

    @Override
    public ResourceLocation getName() {
        return typeName;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        builder.put(ops.createString("slotName"), ops.createString(getSlot().getName().toString()));
        builder.put(ops.createString("weight"), ops.createDouble(getWeight()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        List<String> slotNames = dynamic.get("slots").asList(d ->
                d.asString(LootSlotManager.INVALID_LOOT_SLOT.toString()));
        String slotName = dynamic.get("slotName").asString(RandomizationSlotManager.INVALID_SLOT.toString());
        ResourceLocation slotNameRL = new ResourceLocation(slotName);
        slot = RandomizationSlotManager.getSlotFromName(slotNameRL);
        weight = dynamic.get("weight").asDouble(1.0);
    }

    @Override
    public ResourceLocation getTypeName() {
        return typeName;
    }

    @Override
    public String getTypeEntryName() {
        return TYPE_ENTRY_NAME;
    }

    public static <D> ResourceLocation getType(Dynamic<D> dynamic) {
        return IDynamicMapTypedSerializer.getType(dynamic, TYPE_ENTRY_NAME).orElse(INVALID_OPTION);
    }
}
