package com.chaosbuffalo.mkultra.item;

import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.init.MKUItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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


    public static Holder<ArmorMaterial> GREEN_KNIGHT_ARMOR = register("green_knight", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.BOOTS, 3);
        defenses.put(ArmorItem.Type.LEGGINGS, 7);
        defenses.put(ArmorItem.Type.CHESTPLATE, 6);
        defenses.put(ArmorItem.Type.HELMET, 3);
        defenses.put(ArmorItem.Type.BODY, 6);
    }), 15, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
    public static final int GREEN_KNOX_ARMOR_DURABILITY = 25;


    public static Holder<ArmorMaterial> TROOPER_KNIGHT_ARMOR = register("trooper_knight", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.BOOTS, 2);
        defenses.put(ArmorItem.Type.LEGGINGS, 4);
        defenses.put(ArmorItem.Type.CHESTPLATE, 5);
        defenses.put(ArmorItem.Type.HELMET, 1);
        defenses.put(ArmorItem.Type.BODY, 5);
    }), 12, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, () -> Ingredient.of(MKUItems.corruptedPigIronPlate.get()));
    public static final int TROOPER_KNIGHT_ARMOR_DURABILITY = 18;


    public static Holder<ArmorMaterial> SEAWOVEN_ARMOR = register("seawoven", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.BOOTS, 2);
        defenses.put(ArmorItem.Type.LEGGINGS, 2);
        defenses.put(ArmorItem.Type.CHESTPLATE, 3);
        defenses.put(ArmorItem.Type.HELMET, 1);
        defenses.put(ArmorItem.Type.BODY, 3);
    }), 14, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(MKUItems.seawovenScrap.get()));
    public static final int SEAWOVEN_ARMOR_DURABILITY = 18;


    public static Holder<ArmorMaterial> ANCIENT_BRONZE_CHAINMAIL = register("ancient_bronze_chainmail", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.BOOTS, 2);
        defenses.put(ArmorItem.Type.LEGGINGS, 5);
        defenses.put(ArmorItem.Type.CHESTPLATE, 6);
        defenses.put(ArmorItem.Type.HELMET, 2);
        defenses.put(ArmorItem.Type.BODY, 6);
    }), 18, SoundEvents.ARMOR_EQUIP_CHAIN, 0.0F, 0.0F, () -> Ingredient.of(Items.COPPER_INGOT));
    public static final int ANCIENT_BRONZE_CHAINMAIL_DURABILITY = 22;


    public static Holder<ArmorMaterial> ANCIENT_PRIEST_ROBES = register("ancient_priest_robes", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.BOOTS, 1);
        defenses.put(ArmorItem.Type.LEGGINGS, 3);
        defenses.put(ArmorItem.Type.CHESTPLATE, 4);
        defenses.put(ArmorItem.Type.HELMET, 1);
        defenses.put(ArmorItem.Type.BODY, 4);
    }), 20, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.STRING));
    public static final int ANCIENT_PRIEST_ROBES_DURABILITY = 15;


    public static Holder<ArmorMaterial> ANCIENT_CARDINAL_ROBES = register("ancient_cardinal_robes", Util.make(new EnumMap<>(ArmorItem.Type.class), defenses -> {
        defenses.put(ArmorItem.Type.BOOTS, 1);
        defenses.put(ArmorItem.Type.LEGGINGS, 4);
        defenses.put(ArmorItem.Type.CHESTPLATE, 5);
        defenses.put(ArmorItem.Type.HELMET, 2);
        defenses.put(ArmorItem.Type.BODY, 5);
    }), 30, SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, () -> Ingredient.of(Items.IRON_INGOT));
    public static final int ANCIENT_CARDINAL_ROBES_DURABILITY = 25;


    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
    }
}
