package com.chaosbuffalo.mkweapons.items.weapon.tier;

import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class MKWrapperTier implements IMKTier {
    private final Tier itemTier;
    private final List<IMeleeWeaponEffect> weaponEffects;
    private final String name;
    private final TagKey<Item> tag;

    public MKWrapperTier(Tier tier, String name, TagKey<Item> tag,
                         IMeleeWeaponEffect... effects) {
        itemTier = tier;
        this.name = name;
        weaponEffects = Arrays.asList(effects);
        this.tag = tag;
    }

    @Override
    public TagKey<Item> getPrimaryIngredientTag() {
        return tag;
    }

    @Override
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

    @Nonnull
    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return itemTier.getIncorrectBlocksForDrops();
    }

    @Override
    public int getEnchantmentValue() {
        return itemTier.getEnchantmentValue();
    }

    @Nonnull
    @Override
    public Ingredient getRepairIngredient() {
        return itemTier.getRepairIngredient();
    }

    @Override
    public List<IMeleeWeaponEffect> getTierEffects() {
        return weaponEffects;
    }
}
