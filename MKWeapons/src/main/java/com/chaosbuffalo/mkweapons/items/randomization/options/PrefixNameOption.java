package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PrefixNameOption extends BaseRandomizationOption {
    public static final ResourceLocation NAME = MKWeapons.id("prefix_name");
    public static final MapCodec<PrefixNameOption> MAP_CODEC = RecordCodecBuilder.<PrefixNameOption>mapCodec(builder -> {
        return builder.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(i -> i.name),
                IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.NAME_SLOT).forGetter(BaseRandomizationOption::getSlot),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(BaseRandomizationOption::getWeight)
        ).apply(builder, PrefixNameOption::new);
    });
    public static final Codec<PrefixNameOption> CODEC = MAP_CODEC.codec();

    private final Component name;

    public PrefixNameOption(Component name) {
        this(name, RandomizationSlotManager.NAME_SLOT);
    }

    public PrefixNameOption(Component name, IRandomizationSlot slot) {
        super(NAME, slot);
        this.name = name;
    }

    public PrefixNameOption(Component name, IRandomizationSlot slot, double weight) {
        super(NAME, slot, weight);
        this.name = name;
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("mkweapons.prefix.format", name, stack.getItem().getName(stack)));
    }
}
