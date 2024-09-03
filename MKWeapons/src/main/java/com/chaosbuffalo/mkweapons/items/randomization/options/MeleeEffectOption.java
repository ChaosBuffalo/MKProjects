package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.components.MeleeEffectsComponent;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MeleeEffectOption extends EffectOption<IMeleeWeaponEffect> {
    public static final ResourceLocation NAME = MKWeapons.id("melee_effect");
    public static final MapCodec<MeleeEffectOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.EFFECT_SLOT).forGetter(BaseRandomizationOption::getSlot),
            IMeleeWeaponEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(EffectOption::getItemEffects)
    ).apply(builder, MeleeEffectOption::new));

    private MeleeEffectOption(IRandomizationSlot slot, List<IMeleeWeaponEffect> effects) {
        super(NAME, slot, effects);
    }

    public MeleeEffectOption(IRandomizationSlot slot) {
        super(NAME, slot);
    }

    public MeleeEffectOption() {
        this(RandomizationSlotManager.EFFECT_SLOT);
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        getItemEffects().forEach(eff -> {
            IMeleeWeaponEffect newEffect = eff.createTunedEffect(difficulty / GameConstants.MAX_DIFFICULTY);
            MeleeEffectsComponent.addEffect(stack, newEffect);
        });
    }
}