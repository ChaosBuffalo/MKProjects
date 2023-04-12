package com.chaosbuffalo.mkultra.item;

import com.chaosbuffalo.mkultra.init.MKUItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

public enum MKUArmorMaterial implements ArmorMaterial {

    GREEN_KNIGHT_ARMOR("mkultra:green_knight", 25, new int[]{3, 6, 7, 3}, 15, SoundEvents.ARMOR_EQUIP_IRON,
            0.0F, 0.0F, () -> {
        return Ingredient.of(Items.IRON_INGOT);
    }),
    TROOPER_KNIGHT_ARMOR("mkultra:trooper_knight", 18, new int[]{1, 4, 5, 2}, 12,
            SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> {
        return Ingredient.of(MKUItems.corruptedPigIronPlate.get());
    });


    private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
    private final String name;
    private final int maxDamageFactor;
    private final int[] damageReductionAmountArray;
    private final int enchantability;
    private final SoundEvent soundEvent;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyLoadedValue<Ingredient> repairMaterial;

    private MKUArmorMaterial(String name, int maxDamageFactor, int[] damageReductionAmountArray, int enchantability,
                             SoundEvent soundEvent, float toughness, float knockbackResistance,
                             Supplier<Ingredient> repairMaterial) {
        this.name = name;
        this.maxDamageFactor = maxDamageFactor;
        this.damageReductionAmountArray = damageReductionAmountArray;
        this.enchantability = enchantability;
        this.soundEvent = soundEvent;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairMaterial = new LazyLoadedValue<>(repairMaterial);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type p_266807_) {
        return MAX_DAMAGE_ARRAY[p_266807_.ordinal()] * this.maxDamageFactor;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type p_267168_) {
        return this.damageReductionAmountArray[p_267168_.ordinal()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantability;
    }

    @Override
    public SoundEvent getEquipSound() {
        return soundEvent;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairMaterial.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
