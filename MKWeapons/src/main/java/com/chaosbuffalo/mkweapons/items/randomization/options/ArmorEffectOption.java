package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Optional;

public class ArmorEffectOption extends EffectOption<IArmorEffect> {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "armor_effect");

    public ArmorEffectOption(IRandomizationSlot slot) {
        super(NAME, slot);
    }

    public ArmorEffectOption() {
        this(RandomizationSlotManager.EFFECT_SLOT);
    }

    @Override
    protected Optional<IArmorEffect> deserializeEffectToType(@Nullable IItemEffect effect) {
        if (effect instanceof IArmorEffect){
            return Optional.of((IArmorEffect) effect);
        }
        return Optional.empty();
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        stack.getCapability(WeaponsCapabilities.ARMOR_DATA_CAPABILITY).ifPresent(
                x -> getItemEffects().forEach(x::addArmorEffect));
    }
}
