package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.item.MKUArmorMaterial;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.armor.MKArmorItem;
import com.chaosbuffalo.mkweapons.items.effects.armor.ArmorModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.melee.ManaDrainWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RangedManaDrainEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RangedModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RapidFireRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.randomization.options.AttributeOptionEntry;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.tier.MKTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

@Mod.EventBusSubscriber(modid = MKUltra.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MKUItems {

    public static final UUID CHEST_UUID = UUID.fromString("434f17f4-4763-4d27-afdb-368e76ab259e");
    public static final UUID LEGGINGS_UUID = UUID.fromString("1ac6cd1d-7416-4757-89e6-b20d3206464d");
    public static final UUID HELMET_UUID = UUID.fromString("dfb52730-bba0-4b22-8458-f6d9ed687b33");
    public static final UUID FEET_UUID = UUID.fromString("9baf459d-e898-402d-9915-af25a217fedd");

    public static MKTier BRONZE_TIER = new MKTier("bronze", 1, 150, 5.0F, 1.0F, 12,
                () -> Ingredient.of(Items.COPPER_INGOT), BlockTags.NEEDS_IRON_TOOL, Tags.Items.INGOTS_COPPER,
            new ManaDrainWeaponEffect(0.5f, 0.5f));

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

    public static RegistryObject<Item> corruptedGauntlets = REGISTRY.register("corrupted_gauntlets",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

    public static RegistryObject<Item> necrotideBand = REGISTRY.register("necrotide_band",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

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

    public static RegistryObject<MKArmorItem> greenKnightHelmet = REGISTRY.register("green_knight_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.HELMET,
                    (new Item.Properties()), new ArmorModifierEffect(gkHelmetAttrs)));

    public static RegistryObject<MKArmorItem> greenKnightLeggings = REGISTRY.register("green_knight_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.LEGGINGS,
                    (new Item.Properties()), new ArmorModifierEffect(gkLegsAttrs)));

    public static RegistryObject<MKArmorItem> greenKnightChestplate = REGISTRY.register("green_knight_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.CHESTPLATE,
                    (new Item.Properties()), new ArmorModifierEffect(gkChestAttrs)));

    public static RegistryObject<MKArmorItem> greenKnightBoots = REGISTRY.register("green_knight_boots",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.BOOTS,
                    (new Item.Properties()), new ArmorModifierEffect(gkBootsAttrs)));

    public static RegistryObject<Item> corruptedPigIronPlate = REGISTRY.register("corrupted_pig_iron_plate",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<MKArmorItem> trooperKnightHelmet = REGISTRY.register("trooper_knight_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.HELMET,
                    (new Item.Properties())));

    public static RegistryObject<MKArmorItem> trooperKnightLeggings = REGISTRY.register("trooper_knight_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.LEGGINGS,
                    (new Item.Properties())));

    public static RegistryObject<MKArmorItem> trooperKnightChestplate = REGISTRY.register("trooper_knight_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.CHESTPLATE,
                    (new Item.Properties())));

    public static RegistryObject<MKArmorItem> trooperKnightBoots = REGISTRY.register("trooper_knight_boots",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.BOOTS,
                    (new Item.Properties())));

    public static RegistryObject<MKArmorItem> seawovenHelmet = REGISTRY.register("seawoven_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.SEAWOVEN_ARMOR, ArmorItem.Type.HELMET,
                    (new Item.Properties()),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                                    new AttributeModifier(HELMET_UUID, "seawoven", 1.0, AttributeModifier.Operation.ADDITION))
                    ))));

    public static RegistryObject<MKArmorItem> seawovenLeggings = REGISTRY.register("seawoven_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.SEAWOVEN_ARMOR, ArmorItem.Type.LEGGINGS,
                    (new Item.Properties()),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    new AttributeModifier(LEGGINGS_UUID, "seawoven", 6.0, AttributeModifier.Operation.ADDITION))
                    ))));

    public static RegistryObject<MKArmorItem> seawovenChestplate = REGISTRY.register("seawoven_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.SEAWOVEN_ARMOR, ArmorItem.Type.CHESTPLATE,
                    (new Item.Properties()),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    new AttributeModifier(CHEST_UUID,"seawoven", 6.0, AttributeModifier.Operation.ADDITION)),
                            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                                    new AttributeModifier(CHEST_UUID, "seawoven", 1.0, AttributeModifier.Operation.ADDITION))
                    ))));

    public static RegistryObject<MKArmorItem> seawovenBoots = REGISTRY.register("seawoven_boots",
            () -> new MKArmorItem(MKUArmorMaterial.SEAWOVEN_ARMOR, ArmorItem.Type.BOOTS,
                    (new Item.Properties()),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    new AttributeModifier(FEET_UUID,"seawoven", 4.0, AttributeModifier.Operation.ADDITION))
                    ))));

    public static RegistryObject<MKArmorItem> ancientBronzeHelmet = REGISTRY.register("ancient_bronze_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL, ArmorItem.Type.HELMET,
                    (new Item.Properties()),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.ARETE,
                                    new AttributeModifier(HELMET_UUID, "ancient_bronze", 0.1, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    ))));

    public static RegistryObject<MKArmorItem> ancientBronzeLeggings = REGISTRY.register("ancient_bronze_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL, ArmorItem.Type.LEGGINGS,
                    (new Item.Properties()),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(Attributes.MOVEMENT_SPEED,
                                    new AttributeModifier(LEGGINGS_UUID, "ancient_bronze", 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    ))));

    public static RegistryObject<MKArmorItem> ancientBronzeChestplate = REGISTRY.register("ancient_bronze_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL, ArmorItem.Type.CHESTPLATE,
                    (new Item.Properties()),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(Attributes.MAX_HEALTH,
                                    new AttributeModifier(CHEST_UUID,"ancient_bronze", 10.0, AttributeModifier.Operation.ADDITION)),
                            new AttributeOptionEntry(Attributes.ATTACK_DAMAGE,
                                    new AttributeModifier(CHEST_UUID, "ancient_bronze", 2.0, AttributeModifier.Operation.ADDITION))
                    ))));

    public static RegistryObject<MKArmorItem> ancientBronzeBoots = REGISTRY.register("ancient_bronze_boots",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL, ArmorItem.Type.BOOTS,
                    (new Item.Properties()),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(Attributes.ATTACK_SPEED,
                                    new AttributeModifier(FEET_UUID,"seawoven", 0.12, AttributeModifier.Operation.MULTIPLY_TOTAL))
                    ))));

    public static RegistryObject<Item> destroyedTrooperHelmet = REGISTRY.register("destroyed_trooper_helmet",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> destroyedTrooperLeggings = REGISTRY.register("destroyed_trooper_leggings",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> destroyedTrooperChestplate = REGISTRY.register("destroyed_trooper_chestplate",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> destroyedTrooperBoots = REGISTRY.register("destroyed_trooper_boots",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> seawovenScrap = REGISTRY.register("seawoven_scrap",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static Map<IMKTier, Map<IMeleeWeaponType, Item>> WEAPON_LOOKUP = new HashMap<>();
    public static List<MKMeleeWeapon> WEAPONS = new ArrayList<>();
    public static List<MKBow> BOWS = new ArrayList<>();

    private static void putWeaponForLookup(IMKTier tier, IMeleeWeaponType weaponType, Item item) {
        WEAPON_LOOKUP.putIfAbsent(tier, new HashMap<>());
        WEAPON_LOOKUP.get(tier).put(weaponType, item);
    }

    public static Item lookupWeapon(IMKTier tier, IMeleeWeaponType weaponType) {
        return WEAPON_LOOKUP.get(tier).get(weaponType);
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent evt) {
        if (evt.getRegistryKey() != ForgeRegistries.ITEMS.getRegistryKey()) {
            return;
        }
        Set<Tuple<String, IMKTier>> materials = new HashSet<>();
        materials.add(new Tuple<>("bronze", BRONZE_TIER));
        WEAPONS.clear();
        BOWS.clear();
        WEAPON_LOOKUP.clear();
        for (Tuple<String, IMKTier> mat : materials) {
            for (IMeleeWeaponType weaponType : MeleeWeaponTypes.WEAPON_TYPES.values()) {
                MKMeleeWeapon weapon = new MKMeleeWeapon(mat.getB(), weaponType,
                        (new Item.Properties()));
                WEAPONS.add(weapon);
                putWeaponForLookup(mat.getB(), weaponType, weapon);
                evt.register(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(MKUltra.MODID,
                        String.format("%s_%s", weaponType.getName().getPath(), mat.getA())), () -> weapon);
            }
            RangedModifierEffect rangedMods = new RangedModifierEffect();
            rangedMods.addAttributeModifier(MKAttributes.RANGED_CRIT,
                    new AttributeModifier(MKWeaponsItems.RANGED_WEP_UUID, "Bow Crit", 0.05, AttributeModifier.Operation.ADDITION));
            rangedMods.addAttributeModifier(MKAttributes.RANGED_CRIT_MULTIPLIER,
                    new AttributeModifier(MKWeaponsItems.RANGED_WEP_UUID, "Bow Crit", 0.25, AttributeModifier.Operation.ADDITION));
            MKBow bow = new MKBow(
                    new Item.Properties().durability(mat.getB().getUses() * 3), mat.getB(),
                    GameConstants.TICKS_PER_SECOND * 2.5f, 4.0f,
                    new RapidFireRangedWeaponEffect(7, .10f),
                    rangedMods,
                    new RangedManaDrainEffect(0.5f, 0.5f)
            );
            BOWS.add(bow);
            evt.register(ForgeRegistries.ITEMS.getRegistryKey(),
                    new ResourceLocation(MKUltra.MODID, String.format("longbow_%s", mat.getA())), () -> bow);
        }
    }

    public static void registerItemProperties() {
        for (MKBow bow : BOWS) {
            ItemProperties.register(bow, new ResourceLocation("pull"), (itemStack, world, entity, seed) -> {
                if (entity == null) {
                    return 0.0F;
                } else {
                    return !(entity.getUseItem().getItem() instanceof MKBow) ? 0.0F :
                            (float) (itemStack.getUseDuration() - entity.getUseItemRemainingTicks()) / bow.getDrawTime(itemStack, entity);
                }
            });
            ItemProperties.register(bow, new ResourceLocation("pulling"), (itemStack, world, entity, seed) -> {
                return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
            });
        }
        for (MKMeleeWeapon weapon : WEAPONS) {
            if (MeleeWeaponTypes.WITH_BLOCKING.contains(weapon.getWeaponType())) {
                ItemProperties.register(weapon, new ResourceLocation("blocking"),
                        (itemStack, world, entity, seed) -> entity != null && entity.isUsingItem()
                                && entity.getUseItem() == itemStack ? 1.0F : 0.0F);
            }

        }


    }

}
