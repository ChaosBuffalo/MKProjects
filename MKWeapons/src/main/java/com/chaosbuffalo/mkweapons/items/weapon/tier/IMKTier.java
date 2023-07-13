package com.chaosbuffalo.mkweapons.items.weapon.tier;

import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface IMKTier extends Tier {

    String getName();

    Ingredient getMajorIngredient();

    TagKey<Item> getItemTag();

    List<IMeleeWeaponEffect> getMeleeWeaponEffects();
}
