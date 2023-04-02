package com.chaosbuffalo.mkweapons.items.armor;

import com.chaosbuffalo.mkweapons.items.effects.armor.IArmorEffect;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface IMKArmor {

    List<? extends IArmorEffect> getArmorEffects(ItemStack item);

    List<? extends IArmorEffect> getArmorEffects();
}
