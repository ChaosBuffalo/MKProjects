package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

public class AddAbilityOption extends BaseRandomizationOption{
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "option.ability");
    private MKAbility ability;

    public AddAbilityOption(IRandomizationSlot slot){
        super(NAME, slot);
    }

    public AddAbilityOption(MKAbility ability, IRandomizationSlot slot){
        this(slot);
        this.ability = ability;
    }

    public AddAbilityOption(){
        this(RandomizationSlotManager.ABILITY_SLOT);
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(x ->
                x.setAbilityId(ability.getAbilityId()));
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        ability = MKCoreRegistry.getAbility(new ResourceLocation(dynamic.get("abilityId")
                .asString(MKCoreRegistry.INVALID_ABILITY.toString())));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("abilityId"), ops.createString(ability.getAbilityId().toString()));
    }
}
