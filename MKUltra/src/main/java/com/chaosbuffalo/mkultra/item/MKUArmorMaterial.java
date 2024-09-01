package com.chaosbuffalo.mkultra.item;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class MKUArmorMaterial {

    public static final DeferredRegister<ArmorMaterial> REGISTER = DeferredRegister.create(Registries.ARMOR_MATERIAL, MKUltra.MODID);

    private static Holder<ArmorMaterial> register(
            String name,
            EnumMap<ArmorItem.Type, Integer> defense,
            int enchantmentValue,
            Holder<SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient
    ) {
        List<ArmorMaterial.Layer> list = List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(REGISTER.getNamespace(), name)));
        return register(name, defense, enchantmentValue, equipSound, toughness, knockbackResistance, repairIngredient, list);
    }


    private static Holder<ArmorMaterial> register(
            String name,
            EnumMap<ArmorItem.Type, Integer> defense,
            int enchantmentValue,
            Holder<SoundEvent> equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngridient,
            List<ArmorMaterial.Layer> layers
    ) {
        EnumMap<ArmorItem.Type, Integer> enummap = new EnumMap<>(ArmorItem.Type.class);

        for (ArmorItem.Type armoritem$type : ArmorItem.Type.values()) {
            enummap.put(armoritem$type, defense.get(armoritem$type));
        }

        return REGISTER.register(name, () -> new ArmorMaterial(enummap, enchantmentValue, equipSound, repairIngridient, layers, toughness, knockbackResistance));
    }


    // FIXME: durability was removed from armor material

    //        GREEN_KNIGHT_ARMOR("mkultra:green_knight", 25, new int[]{3, 6, 7, 3}, 15,
//    SoundEvents.ARMOR_EQUIP_IRON,
//            0.0F, 0.0F,
//            () -> Ingredient.of(Items.IRON_INGOT)),
    public static Holder<ArmorMaterial> GREEN_KNIGHT_ARMOR = register("green_knight", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.HELMET, 3);
        defenses.put(ArmorItem.Type.CHESTPLATE, 6);
        defenses.put(ArmorItem.Type.LEGGINGS, 7);
        defenses.put(ArmorItem.Type.BOOTS, 3);
        defenses.put(ArmorItem.Type.BODY, 6);
    }), 15, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));


    //    TROOPER_KNIGHT_ARMOR("mkultra:trooper_knight", 18, new int[]{1, 4, 5, 2}, 12,
//            SoundEvents.ARMOR_EQUIP_IRON,
//            0.0F, 0.0F,
//            () -> Ingredient.of(MKUItems.corruptedPigIronPlate.get())),
    public static Holder<ArmorMaterial> TROOPER_KNIGHT_ARMOR = register("trooper_knight", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.HELMET, 1);
        defenses.put(ArmorItem.Type.CHESTPLATE, 4);
        defenses.put(ArmorItem.Type.LEGGINGS, 5);
        defenses.put(ArmorItem.Type.BOOTS, 2);
        defenses.put(ArmorItem.Type.BODY, 4);
    }), 12, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(MKUItems.corruptedPigIronPlate.get()));


    //    SEAWOVEN_ARMOR("mkultra:seawoven", 18, new int[]{1, 2, 3, 2}, 14,
//            SoundEvents.ARMOR_EQUIP_LEATHER,
//            0.0f, 0.0f,
//            () -> Ingredient.of(MKUItems.seawovenScrap.get())),
    public static Holder<ArmorMaterial> SEAWOVEN_ARMOR = register("seawoven", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.HELMET, 1);
        defenses.put(ArmorItem.Type.CHESTPLATE, 2);
        defenses.put(ArmorItem.Type.LEGGINGS, 3);
        defenses.put(ArmorItem.Type.BOOTS, 2);
        defenses.put(ArmorItem.Type.BODY, 2);
    }), 14, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(MKUItems.seawovenScrap.get()));


    //    ANCIENT_BRONZE_CHAINMAIL("mkultra:ancient_bronze_chainmail", 22, new int[]{2, 5, 6, 2}, 18,
//            SoundEvents.ARMOR_EQUIP_CHAIN,
//            0.0f, 0.0f,
//            () -> Ingredient.of(Items.COPPER_INGOT)),
//
    public static Holder<ArmorMaterial> ANCIENT_BRONZE_CHAINMAIL = register("ancient_bronze_chainmail", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.HELMET, 2);
        defenses.put(ArmorItem.Type.CHESTPLATE, 5);
        defenses.put(ArmorItem.Type.LEGGINGS, 6);
        defenses.put(ArmorItem.Type.BOOTS, 2);
        defenses.put(ArmorItem.Type.BODY, 5);
    }), 18, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.COPPER_INGOT));
    //    ANCIENT_PRIEST_ROBES("mkultra:ancient_priest_robes", 15, new int[]{1, 3, 4, 1}, 20,
//            SoundEvents.ARMOR_EQUIP_LEATHER,
//            0.0f, 0.0f,
//            () -> Ingredient.of(Items.STRING)),
    public static Holder<ArmorMaterial> ANCIENT_PRIEST_ROBES = register("ancient_priest_robes", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.HELMET, 1);
        defenses.put(ArmorItem.Type.CHESTPLATE, 3);
        defenses.put(ArmorItem.Type.LEGGINGS, 4);
        defenses.put(ArmorItem.Type.BOOTS, 1);
        defenses.put(ArmorItem.Type.BODY, 3);
    }), 20, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.STRING));

    //    ANCIENT_CARDINAL_ROBES("mkultra:ancient_cardinal_robes", 25, new int[]{2, 4, 5, 2}, 30,
//            SoundEvents.ARMOR_EQUIP_LEATHER,
//            0.0f, 0.0f,
//            () -> Ingredient.of(Items.GOLD_INGOT)
//    );
    public static Holder<ArmorMaterial> ANCIENT_CARDINAL_ROBES = register("ancient_cardinal_robes", Util.make(new EnumMap<>(ArmorItem.Type.class), p_323382_ -> {
        p_323382_.put(ArmorItem.Type.BOOTS, 1);
        p_323382_.put(ArmorItem.Type.LEGGINGS, 4);
        p_323382_.put(ArmorItem.Type.CHESTPLATE, 5);
        p_323382_.put(ArmorItem.Type.HELMET, 2);
        p_323382_.put(ArmorItem.Type.BODY, 4);
    }), 30, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));


    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
    }

//    private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
//    private final String name;
//    private final int maxDamageFactor;
//    private final int[] damageReductionAmountArray;
//    private final int enchantability;
//    private final SoundEvent soundEvent;
//    private final float toughness;
//    private final float knockbackResistance;
//    private final LazyLoadedValue<Ingredient> repairMaterial;
//
//
//    private MKUArmorMaterial(String name, int maxDamageFactor, int[] damageReductionAmountArray, int enchantability,
//                             SoundEvent soundEvent, float toughness, float knockbackResistance,
//                             Supplier<Ingredient> repairMaterial) {
//        this.name = name;
//        this.maxDamageFactor = maxDamageFactor;
//        this.damageReductionAmountArray = damageReductionAmountArray;
//        this.enchantability = enchantability;
//        this.soundEvent = soundEvent;
//        this.toughness = toughness;
//        this.knockbackResistance = knockbackResistance;
//        this.repairMaterial = new LazyLoadedValue<>(repairMaterial);
//    }

//    @Override
//    public int getDurabilityForType(ArmorItem.Type p_266807_) {
//        return MAX_DAMAGE_ARRAY[p_266807_.ordinal()] * this.maxDamageFactor;
//    }
//
//    @Override
//    public int getDefenseForType(ArmorItem.Type p_267168_) {
//        return this.damageReductionAmountArray[p_267168_.ordinal()];
//    }
}
