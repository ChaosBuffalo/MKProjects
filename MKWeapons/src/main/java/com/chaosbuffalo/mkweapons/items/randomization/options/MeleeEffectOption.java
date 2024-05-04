package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.effects.IDifficultyAwareEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MeleeEffectOption extends EffectOption<IMeleeWeaponEffect> {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "melee_effect");
    public static final Codec<MeleeEffectOption> CODEC = RecordCodecBuilder.<MeleeEffectOption>mapCodec(builder -> {
        return builder.group(
                IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.EFFECT_SLOT).forGetter(BaseRandomizationOption::getSlot),
                IMeleeWeaponEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(EffectOption::getItemEffects)
        ).apply(builder, MeleeEffectOption::new);
    }).codec();

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
        stack.getCapability(WeaponsCapabilities.WEAPON_DATA_CAPABILITY).ifPresent(
                x -> getItemEffects().forEach(eff -> {
                    IMeleeWeaponEffect copied = eff.copy();
                    if (copied instanceof IDifficultyAwareEffect tunable) {
                        tunable.tuneEffect(difficulty / GameConstants.MAX_DIFFICULTY);
                    }
                    x.addMeleeWeaponEffect(copied);
                }));
    }
}