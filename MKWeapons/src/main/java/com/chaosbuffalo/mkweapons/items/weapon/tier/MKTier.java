package com.chaosbuffalo.mkweapons.items.weapon.tier;

import com.chaosbuffalo.mkweapons.items.effects.melee.IMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.IRangedWeaponEffect;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.SimpleTier;

import java.util.List;
import java.util.function.Supplier;

public class MKTier extends SimpleTier implements IMKTier {
    private final List<IMeleeWeaponEffect> meleeEffects;
    private final List<IRangedWeaponEffect> rangedEffects;
    private final String name;
    private final TagKey<Item> tag;

    public MKTier(String name, int pLevel, int pUses, float pSpeed, float pDamage, int pEnchantmentValue,
                  Supplier<Ingredient> pRepairIngredient, TagKey<Block> incorrectBlocksForDrops, TagKey<Block> blockTag, TagKey<Item> itemTag,
                  List<IMeleeWeaponEffect> effects, List<IRangedWeaponEffect> rangedEffects) {
        super(incorrectBlocksForDrops, pUses, pSpeed, pDamage, pEnchantmentValue, pRepairIngredient);
        this.name = name;
        meleeEffects = effects;
        this.rangedEffects = rangedEffects;
        this.tag = itemTag;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TagKey<Item> getPrimaryIngredientTag() {
        return tag;
    }

    @Override
    public List<IMeleeWeaponEffect> getMeleeEffects() {
        return meleeEffects;
    }

    @Override
    public List<IRangedWeaponEffect> getRangedEffects() {
        return rangedEffects;
    }
}
