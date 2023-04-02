package com.chaosbuffalo.mkweapons.items.weapon.tier;

import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

public class MKTier implements Tier {
    private final Tier itemTier;
    private final List<IMeleeWeaponEffect> weaponEffects;
    private final String name;
    private final TagKey<Item> tag;

    public MKTier(Tier tier, String name, TagKey<Item> tag,
                  IMeleeWeaponEffect... effects) {
        itemTier = tier;
        this.name = name;
        weaponEffects = Arrays.asList(effects);
        this.tag = tag;
    }

    public TagKey<Item> getItemTag() {
        return tag;
    }

    public Ingredient getMajorIngredient() {
        return Ingredient.of(tag);
    }

    public String getName() {
        return name;
    }

    @Override
    public int getUses() {
        return itemTier.getUses();
    }

    @Override
    public float getSpeed() {
        return itemTier.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return itemTier.getAttackDamageBonus();
    }

    @Override
    public int getLevel() {
        return itemTier.getLevel();
    }

    @Override
    public int getEnchantmentValue() {
        return itemTier.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return itemTier.getRepairIngredient();
    }

    public List<IMeleeWeaponEffect> getMeleeWeaponEffects() {
        return weaponEffects;
    }
}
