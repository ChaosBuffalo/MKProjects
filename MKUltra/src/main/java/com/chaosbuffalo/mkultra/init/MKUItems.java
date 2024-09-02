package com.chaosbuffalo.mkultra.init;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkultra.MKUltra;
import com.chaosbuffalo.mkultra.item.MKUArmorMaterial;
import com.chaosbuffalo.mkweapons.init.MKWeaponsItems;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.accessories.MKCurioAccessory;
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
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.*;

@EventBusSubscriber(modid = MKUltra.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class MKUItems {

    public static final UUID CHEST_UUID = UUID.fromString("434f17f4-4763-4d27-afdb-368e76ab259e");
    public static final UUID LEGGINGS_UUID = UUID.fromString("1ac6cd1d-7416-4757-89e6-b20d3206464d");
    public static final UUID HELMET_UUID = UUID.fromString("dfb52730-bba0-4b22-8458-f6d9ed687b33");
    public static final UUID FEET_UUID = UUID.fromString("9baf459d-e898-402d-9915-af25a217fedd");

    static AttributeModifier createTransitionalModifier(UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        ResourceLocation modifierId = MKUltra.id(uuid.toString() + "." + name.toLowerCase(Locale.ROOT).replace(' ', '_'));
        return new AttributeModifier(modifierId, amount, operation);
    }


    public static MKTier BRONZE_TIER = new MKTier("bronze", 1, 150, 5.0F, 1.0F, 12,
            () -> Ingredient.of(Items.COPPER_INGOT), BlockTags.INCORRECT_FOR_IRON_TOOL, BlockTags.NEEDS_IRON_TOOL, Tags.Items.INGOTS_COPPER,
            new ManaDrainWeaponEffect(0.5f, 0.5f));

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MKUltra.MODID);

    public static DeferredHolder<Item, Item> cleansingSeedProjectileItem = REGISTRY.register("cleansing_seed_projectile",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> spiritBombProjectileItem = REGISTRY.register("spirit_bomb_projectile",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> fireballProjectileItem = REGISTRY.register("fireball_projectile",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> shadowBoltProjectileItem = REGISTRY.register("shadow_bolt_projectile",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> drownProjectileItem = REGISTRY.register("drown_projectile",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> holyWordProjectileItem = REGISTRY.register("holy_word_projectile",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> corruptedGauntlets = REGISTRY.register("corrupted_gauntlets",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    public static DeferredHolder<Item, Item> necrotideBand = REGISTRY.register("necrotide_band",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    static List<AttributeOptionEntry> gkHelmetAttrs = List.of(
            new AttributeOptionEntry(MKAttributes.COOLDOWN,
                    createTransitionalModifier(UUID.fromString("2013a410-ca6d-48a9-a12d-a70a65ec8190"),
                            "Bonus", 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                    EquipmentSlotGroup.HEAD));

    static List<AttributeOptionEntry> gkLegsAttrs = List.of(
            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                    createTransitionalModifier(UUID.fromString("9b184106-1a7b-444c-8bbe-538bff1f66cd"),
                            "Bonus", 6, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.LEGS),
            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                    createTransitionalModifier(UUID.fromString("25f12c51-a841-4ac9-8fbb-02000a19e563"),
                            "Bonus", 1.0, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.LEGS));

    static List<AttributeOptionEntry> gkChestAttrs = List.of(
            new AttributeOptionEntry(Attributes.MAX_HEALTH,
                    createTransitionalModifier(UUID.fromString("ea84d132-3e14-40d7-acda-2f8ab0d5f3ad"),
                            "Bonus", 10, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.CHEST),
            new AttributeOptionEntry(MKAttributes.HEAL_BONUS,
                    createTransitionalModifier(UUID.fromString("c6359e08-8e0c-4721-b8aa-d55d978f4798"),
                            "Bonus", 2, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.CHEST));

    static List<AttributeOptionEntry> gkBootsAttrs = List.of(
            new AttributeOptionEntry(Attributes.ATTACK_SPEED,
                    createTransitionalModifier(UUID.fromString("f0d94451-5a80-4669-954d-bc6f6c39ccd0"),
                            "Bonus", 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                    EquipmentSlotGroup.FEET));

    public static DeferredHolder<Item, MKArmorItem> greenKnightHelmet = REGISTRY.register("green_knight_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .durability(ArmorItem.Type.HELMET.getDurability(MKUArmorMaterial.GREEN_KNOX_ARMOR_DURABILITY)),
                    new ArmorModifierEffect(gkHelmetAttrs)));

    public static DeferredHolder<Item, MKArmorItem> greenKnightLeggings = REGISTRY.register("green_knight_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(MKUArmorMaterial.GREEN_KNOX_ARMOR_DURABILITY)),
                    new ArmorModifierEffect(gkLegsAttrs)));

    public static DeferredHolder<Item, MKArmorItem> greenKnightChestplate = REGISTRY.register("green_knight_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(MKUArmorMaterial.GREEN_KNOX_ARMOR_DURABILITY)),
                    new ArmorModifierEffect(gkChestAttrs)));

    public static DeferredHolder<Item, MKArmorItem> greenKnightBoots = REGISTRY.register("green_knight_boots",
            () -> new MKArmorItem(MKUArmorMaterial.GREEN_KNIGHT_ARMOR, ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.BOOTS.getDurability(MKUArmorMaterial.GREEN_KNOX_ARMOR_DURABILITY)),
                    new ArmorModifierEffect(gkBootsAttrs)));

    public static DeferredHolder<Item, Item> corruptedPigIronPlate = REGISTRY.register("corrupted_pig_iron_plate",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, MKArmorItem> trooperKnightHelmet = REGISTRY.register("trooper_knight_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .durability(ArmorItem.Type.HELMET.getDurability(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR_DURABILITY))
            ));

    public static DeferredHolder<Item, MKArmorItem> trooperKnightLeggings = REGISTRY.register("trooper_knight_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR_DURABILITY))
            ));

    public static DeferredHolder<Item, MKArmorItem> trooperKnightChestplate = REGISTRY.register("trooper_knight_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR_DURABILITY))
            ));

    public static DeferredHolder<Item, MKArmorItem> trooperKnightBoots = REGISTRY.register("trooper_knight_boots",
            () -> new MKArmorItem(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR, ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.BOOTS.getDurability(MKUArmorMaterial.TROOPER_KNIGHT_ARMOR_DURABILITY))
            ));

    public static DeferredHolder<Item, MKArmorItem> seawovenHelmet = REGISTRY.register("seawoven_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.SEAWOVEN_ARMOR, ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .durability(ArmorItem.Type.HELMET.getDurability(MKUArmorMaterial.SEAWOVEN_ARMOR_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                                    createTransitionalModifier(HELMET_UUID, "seawoven", 1.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.HEAD)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> seawovenLeggings = REGISTRY.register("seawoven_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.SEAWOVEN_ARMOR, ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(MKUArmorMaterial.SEAWOVEN_ARMOR_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    createTransitionalModifier(LEGGINGS_UUID, "seawoven", 6.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.LEGS)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> seawovenChestplate = REGISTRY.register("seawoven_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.SEAWOVEN_ARMOR, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(MKUArmorMaterial.SEAWOVEN_ARMOR_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    createTransitionalModifier(CHEST_UUID, "seawoven", 6.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST),
                            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                                    createTransitionalModifier(CHEST_UUID, "seawoven", 1.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> seawovenBoots = REGISTRY.register("seawoven_boots",
            () -> new MKArmorItem(MKUArmorMaterial.SEAWOVEN_ARMOR, ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.BOOTS.getDurability(MKUArmorMaterial.SEAWOVEN_ARMOR_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    createTransitionalModifier(FEET_UUID, "seawoven", 4.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.FEET)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientBronzeHelmet = REGISTRY.register("ancient_bronze_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL, ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .durability(ArmorItem.Type.HELMET.getDurability(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.ARETE,
                                    createTransitionalModifier(HELMET_UUID, "ancient_bronze", 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                                    EquipmentSlotGroup.HEAD)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientBronzeLeggings = REGISTRY.register("ancient_bronze_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(Attributes.MOVEMENT_SPEED,
                                    createTransitionalModifier(LEGGINGS_UUID, "ancient_bronze", 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                                    EquipmentSlotGroup.LEGS)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientBronzeChestplate = REGISTRY.register("ancient_bronze_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(Attributes.MAX_HEALTH,
                                    createTransitionalModifier(CHEST_UUID, "ancient_bronze", 10.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST),
                            new AttributeOptionEntry(Attributes.ATTACK_DAMAGE,
                                    createTransitionalModifier(CHEST_UUID, "ancient_bronze", 2.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientBronzeBoots = REGISTRY.register("ancient_bronze_boots",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL, ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.BOOTS.getDurability(MKUArmorMaterial.ANCIENT_BRONZE_CHAINMAIL_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(Attributes.ATTACK_SPEED,
                                    createTransitionalModifier(FEET_UUID, "ancient_bronze", 0.12, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                                    EquipmentSlotGroup.FEET)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientPriestHelmet = REGISTRY.register("ancient_priest_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_PRIEST_ROBES, ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .durability(ArmorItem.Type.HELMET.getDurability(MKUArmorMaterial.ANCIENT_PRIEST_ROBES_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                                    createTransitionalModifier(HELMET_UUID, "ancient_priest", 2.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.HEAD)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientPriestLeggings = REGISTRY.register("ancient_priest_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_PRIEST_ROBES, ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(MKUArmorMaterial.ANCIENT_PRIEST_ROBES_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    createTransitionalModifier(LEGGINGS_UUID, "ancient_priest", 10.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.LEGS)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientPriestChestplate = REGISTRY.register("ancient_priest_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_PRIEST_ROBES, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(MKUArmorMaterial.ANCIENT_PRIEST_ROBES_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    createTransitionalModifier(CHEST_UUID, "ancient_priest", 10.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST),
                            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                                    createTransitionalModifier(CHEST_UUID, "ancient_priest", 1.5, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientPriestBoots = REGISTRY.register("ancient_priest_boots",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_PRIEST_ROBES, ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.BOOTS.getDurability(MKUArmorMaterial.ANCIENT_PRIEST_ROBES_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.SPELL_CRIT_MULTIPLIER,
                                    createTransitionalModifier(FEET_UUID, "ancient_priest", 0.25, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.FEET)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientCardinalHelmet = REGISTRY.register("ancient_cardinal_helmet",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_CARDINAL_ROBES, ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .durability(ArmorItem.Type.HELMET.getDurability(MKUArmorMaterial.ANCIENT_CARDINAL_ROBES_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                                    createTransitionalModifier(HELMET_UUID, "ancient_cardinal", 3.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.HEAD)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientCardinalLeggings = REGISTRY.register("ancient_cardinal_leggings",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_CARDINAL_ROBES, ArmorItem.Type.LEGGINGS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.LEGGINGS.getDurability(MKUArmorMaterial.ANCIENT_CARDINAL_ROBES_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    createTransitionalModifier(LEGGINGS_UUID, "ancient_cardinal", 15.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.LEGS)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientCardinalChestplate = REGISTRY.register("ancient_cardinal_chestplate",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_CARDINAL_ROBES, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties()
                            .durability(ArmorItem.Type.CHESTPLATE.getDurability(MKUArmorMaterial.ANCIENT_CARDINAL_ROBES_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.MAX_MANA,
                                    createTransitionalModifier(CHEST_UUID, "ancient_cardinal", 15.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST),
                            new AttributeOptionEntry(MKAttributes.MANA_REGEN,
                                    createTransitionalModifier(CHEST_UUID, "ancient_cardinal", 2.25, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.CHEST)
                    ))));

    public static DeferredHolder<Item, MKArmorItem> ancientCardinalBoots = REGISTRY.register("ancient_cardinal_boots",
            () -> new MKArmorItem(MKUArmorMaterial.ANCIENT_CARDINAL_ROBES, ArmorItem.Type.BOOTS,
                    new Item.Properties()
                            .durability(ArmorItem.Type.HELMET.getDurability(MKUArmorMaterial.ANCIENT_CARDINAL_ROBES_DURABILITY)),
                    new ArmorModifierEffect(List.of(
                            new AttributeOptionEntry(MKAttributes.SPELL_CRIT_MULTIPLIER,
                                    createTransitionalModifier(FEET_UUID, "ancient_cardinal", 0.35, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.FEET)
                    ))));

    public static DeferredHolder<Item, Item> destroyedTrooperHelmet = REGISTRY.register("destroyed_trooper_helmet",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> destroyedTrooperLeggings = REGISTRY.register("destroyed_trooper_leggings",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> destroyedTrooperChestplate = REGISTRY.register("destroyed_trooper_chestplate",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> destroyedTrooperBoots = REGISTRY.register("destroyed_trooper_boots",
            () -> new Item(new Item.Properties()));

    public static DeferredHolder<Item, Item> seawovenScrap = REGISTRY.register("seawoven_scrap",
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
    public static void registerItems(RegisterEvent event) {
        if (event.getRegistryKey() != Registries.ITEM) {
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
                event.register(Registries.ITEM, MKUltra.id(
                        String.format("%s_%s", weaponType.getName().getPath(), mat.getA())), () -> weapon);
            }
            RangedModifierEffect rangedMods = new RangedModifierEffect();
            rangedMods.addAttributeModifier(MKAttributes.RANGED_CRIT,
                    createTransitionalModifier(MKWeaponsItems.RANGED_WEP_UUID, "Bow Crit", 0.05, AttributeModifier.Operation.ADD_VALUE));
            rangedMods.addAttributeModifier(MKAttributes.RANGED_CRIT_MULTIPLIER,
                    createTransitionalModifier(MKWeaponsItems.RANGED_WEP_UUID, "Bow Crit", 0.25, AttributeModifier.Operation.ADD_VALUE));
            MKBow bow = new MKBow(
                    new Item.Properties().durability(mat.getB().getUses() * 3), mat.getB(),
                    GameConstants.TICKS_PER_SECOND * 2.5f, 4.0f,
                    new RapidFireRangedWeaponEffect(7, .10f),
                    rangedMods,
                    new RangedManaDrainEffect(0.5f, 0.5f)
            );
            BOWS.add(bow);
            event.register(Registries.ITEM,
                    MKUltra.id(String.format("longbow_%s", mat.getA())), () -> bow);
        }
    }

    public static void registerItemProperties() {
        for (MKBow bow : BOWS) {
            ItemProperties.register(bow, ResourceLocation.withDefaultNamespace("pull"), (itemStack, world, entity, seed) -> {
                if (entity == null) {
                    return 0.0F;
                } else {
                    return !(entity.getUseItem().getItem() instanceof MKBow mkBow) ? 0.0F :
                            (float) (itemStack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / mkBow.getDrawTime(itemStack, entity);
                }
            });
            ItemProperties.register(bow, ResourceLocation.withDefaultNamespace("pulling"), (itemStack, world, entity, seed) -> {
                return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
            });
        }
        for (MKMeleeWeapon weapon : WEAPONS) {
            if (weapon.getWeaponType().canBlock()) {
                ItemProperties.register(weapon, ResourceLocation.withDefaultNamespace("blocking"),
                        (itemStack, world, entity, seed) -> entity != null && entity.isUsingItem()
                                && entity.getUseItem() == itemStack ? 1.0F : 0.0F);
            }

        }


    }

}
