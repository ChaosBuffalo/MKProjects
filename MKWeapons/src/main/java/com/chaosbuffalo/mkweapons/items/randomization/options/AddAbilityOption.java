package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityInfo;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AddAbilityOption extends BaseRandomizationOption {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "option.ability");
    private MKAbilityInfo abilityInfo;

    public AddAbilityOption(IRandomizationSlot slot) {
        super(NAME, slot);
    }

    @Deprecated
    public AddAbilityOption(MKAbility ability, IRandomizationSlot slot) {
        this(ability.getDefaultInstance(), slot);
    }

    public AddAbilityOption(MKAbilityInfo abilityInfo, IRandomizationSlot slot) {
        this(slot);
        this.abilityInfo = abilityInfo;
    }

    public AddAbilityOption() {
        this(RandomizationSlotManager.ABILITY_SLOT);
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        if (abilityInfo != null) {
            stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(x -> x.setGrantedAbility(abilityInfo));
        }
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        abilityInfo = MKAbilityInfo.deserialize(dynamic.get("ability"));
    }

    @Override
    public <D> void writeAdditionalData(DynamicOps<D> ops, ImmutableMap.Builder<D, D> builder) {
        super.writeAdditionalData(ops, builder);
        builder.put(ops.createString("ability"), abilityInfo.serialize(ops));
    }
}
