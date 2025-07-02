package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.item.CoreItemComponents;
import com.chaosbuffalo.mkcore.item.ItemGrantedAbility;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AddAbilityOption extends BaseRandomizationOption {
    public static final ResourceLocation NAME = MKWeapons.id("option.ability");
    public static final MapCodec<AddAbilityOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            MKCoreRegistry.ABILITIES.holderByNameCodec().fieldOf("ability").forGetter(i -> i.ability),
            IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.ABILITY_SLOT).forGetter(BaseRandomizationOption::getSlot),
            Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(BaseRandomizationOption::getWeight)
    ).apply(builder, AddAbilityOption::new));
    public static final Codec<AddAbilityOption> CODEC = MAP_CODEC.codec();


    private final Holder<MKAbility> ability;

    public AddAbilityOption(Holder<MKAbility> ability) {
        this(ability, RandomizationSlotManager.ABILITY_SLOT);
    }

    public AddAbilityOption(Holder<MKAbility> ability, IRandomizationSlot slot) {
        super(NAME, slot);
        this.ability = ability;
    }

    public AddAbilityOption(Holder<MKAbility> ability, IRandomizationSlot slot, double weight) {
        super(NAME, slot, weight);
        this.ability = ability;
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, int slotIndex, double difficulty) {
        stack.set(CoreItemComponents.ITEM_ABILITY, new ItemGrantedAbility(ability));
    }
}
