package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.item.MKUArmorMaterial;
import com.chaosbuffalo.mkweapons.items.armor.MKArmorItem;
import com.chaosbuffalo.mkweapons.items.effects.armor.ArmorModifierEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.google.common.collect.Lists;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.UUID;

public final class MKUItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MKUltra.MODID);

    public static RegistryObject<Item> cleansingSeedProjectileItem = REGISTRY.register("cleansing_seed_projectile",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> spiritBombProjectileItem = REGISTRY.register("spirit_bomb_projectile",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> fireballProjectileItem = REGISTRY.register("fireball_projectile",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> shadowBoltProjectileItem = REGISTRY.register("shadow_bolt_projectile",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> drownProjectileItem = REGISTRY.register("drown_projectile",
            () -> new Item(new Item.Properties()));

    static List<AttributeOptionEntry> gkHelmetAttrs = Lists.newArrayList(
            new AttributeOptionEntry(MKAttributes.COOLDOWN,
                    new AttributeModifier(UUID.fromString("2013a410-ca6d-48a9-a12d-a70a65ec8190"),
                            "Bonus", 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL)));

    static List<AttributeOptionEntry> gkLegsAttrs = Lists.newArrayList(
            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                    new AttributeModifier(UUID.fromString("9b184106-1a7b-444c-8bbe-538bff1f66cd"),
                            "Bonus", 6, AttributeModifier.Operation.ADDITION)),
            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                    new AttributeModifier(UUID.fromString("25f12c51-a841-4ac9-8fbb-02000a19e563"),
                            "Bonus", 1.0, AttributeModifier.Operation.ADDITION)));

    static List<AttributeOptionEntry> gkChestAttrs = Lists.newArrayList(
            new AttributeOptionEntry(Attributes.MAX_HEALTH,
                    new AttributeModifier(UUID.fromString("ea84d132-3e14-40d7-acda-2f8ab0d5f3ad"),
                            "Bonus", 10, AttributeModifier.Operation.ADDITION)),
            new AttributeOptionEntry(MKAttributes.HEAL_BONUS,
                    new AttributeModifier(UUID.fromString("c6359e08-8e0c-4721-b8aa-d55d978f4798"),
                            "Bonus", 2, AttributeModifier.Operation.ADDITION)));

    static List<AttributeOptionEntry> gkBootsAttrs = Lists.newArrayList(
            new AttributeOptionEntry(Attributes.ATTACK_SPEED,
                    new AttributeModifier(UUID.fromString("f0d94451-5a80-4669-954d-bc6f6c39ccd0"),
                            "Bonus", 0.10, AttributeModifier.Operation.MULTIPLY_TOTAL)));

    public static RegistryObject<Item> greenKnightHelmet = REGISTRY.register("green_knight_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.HELMET,
                    (new Item.Properties()), new ArmorModifierEffect(gkHelmetAttrs)));

    public static RegistryObject<Item> greenKnightLeggings = REGISTRY.register("green_knight_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.LEGGINGS,
                    (new Item.Properties()), new ArmorModifierEffect(gkLegsAttrs)));

    public static RegistryObject<Item> greenKnightChestplate = REGISTRY.register("green_knight_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.CHESTPLATE,
                    (new Item.Properties()), new ArmorModifierEffect(gkChestAttrs)));

    public static RegistryObject<Item> greenKnightBoots = REGISTRY.register("green_knight_boots",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.BOOTS,
                    (new Item.Properties()), new ArmorModifierEffect(gkBootsAttrs)));

    public static RegistryObject<Item> corruptedPigIronPlate = REGISTRY.register("corrupted_pig_iron_plate",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> trooperKnightHelmet = REGISTRY.register("trooper_knight_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.HELMET,
                    (new Item.Properties())));

    public static RegistryObject<Item> trooperKnightLeggings = REGISTRY.register("trooper_knight_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.LEGGINGS,
                    (new Item.Properties())));

    public static RegistryObject<Item> trooperKnightChestplate = REGISTRY.register("trooper_knight_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.CHESTPLATE,
                    (new Item.Properties())));

    public static RegistryObject<Item> trooperKnightBoots = REGISTRY.register("trooper_knight_boots",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.BOOTS,
                    (new Item.Properties())));

    public static RegistryObject<Item> destroyedTrooperHelmet = REGISTRY.register("destroyed_trooper_helmet",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> destroyedTrooperLeggings = REGISTRY.register("destroyed_trooper_leggings",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> destroyedTrooperChestplate = REGISTRY.register("destroyed_trooper_chestplate",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> destroyedTrooperBoots = REGISTRY.register("destroyed_trooper_boots",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

}
