package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class AddAbilityOption extends BaseRandomizationOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "option.ability");
    public static final Codec<AddAbilityOption> CODEC = ExtraCodecs.lazyInitializedCodec(() -> {
        return RecordCodecBuilder.<AddAbilityOption>mapCodec(builder -> {
            return builder.group(
                    MKCoreRegistry.ABILITIES.getCodec().fieldOf("ability").forGetter(i -> i.ability),
                    IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.ABILITY_SLOT).forGetter(BaseRandomizationOption::getSlot),
                    Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(BaseRandomizationOption::getWeight)
            ).apply(builder, AddAbilityOption::new);
        }).codec();
    });

    private final MKAbility ability;

    public AddAbilityOption(MKAbility ability) {
        this(ability, RandomizationSlotManager.ABILITY_SLOT);
    }

    public AddAbilityOption(MKAbility ability, IRandomizationSlot slot) {
        super(NAME, slot);
        this.ability = ability;
    }

    public AddAbilityOption(MKAbility ability, IRandomizationSlot slot, double weight) {
        super(NAME, slot, weight);
        this.ability = ability;
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(x ->
                x.setAbilityId(ability.getAbilityId()));
    }
}
