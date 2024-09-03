package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.components.ArmorEffectsComponent;
import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ArmorEffectOption extends EffectOption<IArmorEffect> {
    public static final ResourceLocation NAME = MKWeapons.id("armor_effect");
    public static final MapCodec<ArmorEffectOption> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.EFFECT_SLOT).forGetter(BaseRandomizationOption::getSlot),
            IArmorEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(EffectOption::getItemEffects)
    ).apply(builder, ArmorEffectOption::new));
    public static final Codec<ArmorEffectOption> CODEC = MAP_CODEC.codec();

    private ArmorEffectOption(IRandomizationSlot slot, List<IArmorEffect> effects) {
        super(NAME, slot, effects);
    }

    public ArmorEffectOption(IRandomizationSlot slot) {
        super(NAME, slot);
    }

    public ArmorEffectOption() {
        this(RandomizationSlotManager.EFFECT_SLOT);
    }

    @Override
    public void applyToItemStackForSlot(ItemStack stack, LootSlot slot, double difficulty) {
        getItemEffects().forEach(eff -> {
            IArmorEffect newEffect = eff.createTunedEffect(difficulty / GameConstants.MAX_DIFFICULTY);
            ArmorEffectsComponent.addEffect(stack, newEffect);
        });
    }
}
