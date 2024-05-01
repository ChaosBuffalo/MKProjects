package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class NameOption extends BaseRandomizationOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "name");
    public static final Codec<NameOption> CODEC = RecordCodecBuilder.<NameOption>mapCodec(builder -> {
        return builder.group(
                ExtraCodecs.COMPONENT.fieldOf("name").forGetter(i -> i.name),
                IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.NAME_SLOT).forGetter(BaseRandomizationOption::getSlot),
                Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(BaseRandomizationOption::getWeight)
        ).apply(builder, NameOption::new);
    }).codec();

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
        stack.setHoverName(name);
    }
}
