package com.chaosbuffalo.mkweapons.items.effects.accesory;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.ItemModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class AccessoryModifierEffect extends ItemModifierEffect implements IAccessoryEffect {
    public static final ResourceLocation NAME = new ResourceLocation(MKWeapons.MODID, "accessory_effect.modifier");
    public static final Codec<AccessoryModifierEffect> CODEC =
            AttributeOptionEntry.CODEC.listOf().xmap(AccessoryModifierEffect::new, AccessoryModifierEffect::getModifiers);

    public AccessoryModifierEffect(List<AttributeOptionEntry> modifiers) {
        super(NAME, ChatFormatting.WHITE);
        this.modifiers.addAll(modifiers);
    }
}
