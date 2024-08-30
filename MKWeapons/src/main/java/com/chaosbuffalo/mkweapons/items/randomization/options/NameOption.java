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

public class NameOption extends BaseRandomizationOption {
    public static final ResourceLocation NAME = MKWeapons.id("name");
    public static final MapCodec<NameOption> MAP_CODEC = RecordCodecBuilder.<NameOption>mapCodec(builder -> {
        return builder.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(i -> i.name),
                IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.NAME_SLOT).forGetter(BaseRandomizationOption::getSlot),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(BaseRandomizationOption::getWeight)
        ).apply(builder, NameOption::new);
    });
    public static final Codec<NameOption> CODEC = MAP_CODEC.codec();

    private final Component name;

    public NameOption(Component name) {
        this(name, RandomizationSlotManager.NAME_SLOT);
    }

    public NameOption(Component name, IRandomizationSlot slot) {
        super(NAME, slot);
        this.name = name;
    }

    public NameOption(Component name, IRandomizationSlot slot, double weight) {
        super(NAME, slot, weight);
        this.name = name;
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        stack.set(DataComponents.CUSTOM_NAME, name);
    }
}
