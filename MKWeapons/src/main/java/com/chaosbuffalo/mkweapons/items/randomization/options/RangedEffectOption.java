package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.serialization.attributes.IScalableAttribute;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public class RangedEffectOption extends EffectOption<IRangedWeaponEffect> {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "ranged_effect");

    public RangedEffectOption(IRandomizationSlot slot) {
        super(NAME, slot);
    }

    public RangedEffectOption() {
        this(RandomizationSlotManager.EFFECT_SLOT);
    }

    @Override
    protected Optional<IRangedWeaponEffect> deserializeEffectToType(@Nullable IItemEffect effect) {
        if (effect instanceof IRangedWeaponEffect) {
            return Optional.of((IRangedWeaponEffect) effect);
        }
        return Optional.empty();
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(
                x -> getItemEffects().forEach(eff -> {
                    IRangedWeaponEffect copied = eff.copy();
                    copied.getAttributes().forEach(attr -> {
                        if (attr instanceof IScalableAttribute sAttr) {
                            sAttr.scale(difficulty / GameConstants.MAX_DIFFICULTY);
                        }
                    });
                    x.addRangedWeaponEffect(copied);
                }));
    }
}
