package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.serialization.attributes.IScalableAttribute;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public class AccessoryEffectOption extends EffectOption<IAccessoryEffect> {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "accessory_effect");

    public AccessoryEffectOption(IRandomizationSlot slot) {
        super(NAME, slot);
    }

    public AccessoryEffectOption() {
        this(RandomizationSlotManager.EFFECT_SLOT);
    }

    @Override
    protected Optional<IAccessoryEffect> deserializeEffectToType(@Nullable IItemEffect effect) {
        if (effect instanceof IAccessoryEffect) {
            return Optional.of((IAccessoryEffect) effect);
        }
        return Optional.empty();
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        MKAccessory.getAccessoryHandler(stack).ifPresent(x -> getItemEffects().forEach(eff -> {
            IAccessoryEffect copied = eff.copy();
            copied.getAttributes().forEach(attr -> {
                if (attr instanceof IScalableAttribute sAttr) {
                    sAttr.scale(difficulty / GameConstants.MAX_DIFFICULTY);
                }
            });
            x.addEffect(copied);
        }));
    }
}
