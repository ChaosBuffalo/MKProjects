package com.chaosbuffalo.mkweapons.items.randomization.options;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.capabilities.WeaponsCapabilities;
import com.chaosbuffalo.mkweapons.items.effects.IDifficultyAwareEffect;
import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import com.chaosbuffalo.mkweapons.items.randomization.slots.IRandomizationSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.LootSlot;
import com.chaosbuffalo.mkweapons.items.randomization.slots.RandomizationSlotManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ArmorEffectOption extends EffectOption<IArmorEffect> {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "armor_effect");
    public static final Codec<ArmorEffectOption> CODEC = RecordCodecBuilder.<ArmorEffectOption>mapCodec(builder -> {
        return builder.group(
                IRandomizationSlot.CODEC.optionalFieldOf("slot", RandomizationSlotManager.EFFECT_SLOT).forGetter(BaseRandomizationOption::getSlot),
                IArmorEffect.DISPATCH_CODEC.listOf().fieldOf("effects").forGetter(EffectOption::getItemEffects)
        ).apply(builder, ArmorEffectOption::new);
    }).codec();

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
        stack.getCapability(WeaponsCapabilities.ARMOR_DATA_CAPABILITY).ifPresent(x -> getItemEffects().forEach(eff -> {
            IArmorEffect copied = eff.copy();
            if (copied instanceof IDifficultyAwareEffect tunable) {
                tunable.tuneEffect(difficulty / GameConstants.MAX_DIFFICULTY);
            }
            x.addArmorEffect(copied);
        }));
    }
}
