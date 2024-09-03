package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.components.AccessoryEffectsComponent;
import com.chaosbuffalo.mkweapons.items.effects.accesory.IAccessoryEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AccessoryEffectOption extends EffectOption<IAccessoryEffect> {
    public static final ResourceLocation NAME = MKWeapons.id("accessory_effect");
    public static final MapCodec<AccessoryEffectOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.EFFECT_SLOT).forGetter(BaseRandomizationOption::getSlot),
            IAccessoryEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(EffectOption::getItemEffects)
    ).apply(builder, AccessoryEffectOption::new));

    private AccessoryEffectOption(IRandomizationSlot slot, List<IAccessoryEffect> effects) {
        super(NAME, slot, effects);
    }

    public AccessoryEffectOption(IRandomizationSlot slot) {
        super(NAME, slot);
    }

    public AccessoryEffectOption() {
        this(RandomizationSlotManager.EFFECT_SLOT);
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        getItemEffects().forEach(eff -> {
            IAccessoryEffect newEffect = eff.createTunedEffect(difficulty / GameConstants.MAX_DIFFICULTY);
            AccessoryEffectsComponent.addEffect(stack, newEffect);
        });
    }
}
