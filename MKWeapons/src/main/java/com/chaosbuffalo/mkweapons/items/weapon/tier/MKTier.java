package com.chaosbuffalo.mkweapons.items.weapon.tier;

import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MKTier implements IMKTier {
    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final LazyLoadedValue<Ingredient> repairIngredient;
    private final TagKey<Block> blockTag;
    private final List<IMeleeWeaponEffect> weaponEffects;
    private final String name;
    private final TagKey<Item> tag;

    public MKTier(String name, int pLevel, int pUses, float pSpeed, float pDamage, int pEnchantmentValue,
                   Supplier<Ingredient> pRepairIngredient, TagKey<Block> blockTag, TagKey<Item> itemTag,
                   IMeleeWeaponEffect... effects) {
        this.level = pLevel;
        this.uses = pUses;
        this.speed = pSpeed;
        this.damage = pDamage;
        this.enchantmentValue = pEnchantmentValue;
        this.repairIngredient = new LazyLoadedValue<>(pRepairIngredient);
        this.blockTag = blockTag;
        this.name = name;
        weaponEffects = Arrays.asList(effects);
        this.tag = itemTag;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.repairIngredient.get();
    }

    @Override
    @Nullable
    public TagKey<Block> getTag() { return blockTag; }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Ingredient getMajorIngredient() {
        return Ingredient.of(tag);
    }

    @Override
    public TagKey<Item> getItemTag() {
        return tag;
    }

    @Override
    public List<IMeleeWeaponEffect> getMeleeWeaponEffects() {
        return weaponEffects;
    }
}
