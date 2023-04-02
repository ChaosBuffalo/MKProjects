package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class NameOption extends BaseRandomizationOption{
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "name");

    private Component name;

    public NameOption(Component name) {
        super(NAME, RandomizationSlotManager.NAME_SLOT);
        this.name = name;
    }

    public NameOption(){
        this(new TextComponent("default"));
    }


    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        stack.setHoverName(name);
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("name"), ops.createString(Component.Serializer.toJson(name)));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        name = dynamic.get("name").map(x -> x.asString().result().map(Component.Serializer::fromJson)
                .orElse(new TextComponent("default"))).result().orElse(new TextComponent("default"));
    }
}
