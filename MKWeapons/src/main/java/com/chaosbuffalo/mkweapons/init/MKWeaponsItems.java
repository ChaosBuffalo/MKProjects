package com.chaosbuffalo.mkweapons.init;


import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.TestNBTWeaponEffectItem;
import com.chaosbuffalo.mkweapons.items.accessories.MKAccessory;
import com.chaosbuffalo.mkweapons.items.effects.melee.LivingDamageMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RangedModifierEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RapidFireRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.tier.MKWrapperTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.chaosbuffalo.mkweapons.items.weapon.types.WeaponTypeManager;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

@Mod.EventBusSubscriber(modid = MKWeapons.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MKWeaponsItems {

    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MKWeapons.MODID);

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static List<MKMeleeWeapon> WEAPONS = new ArrayList<>();
    public static final UUID RANGED_WEP_UUID = UUID.fromString("dbaf479e-515e-4ebc-94dd-eb5a4014bb64");

    public static MKWrapperTier IRON_TIER = new MKWrapperTier(Tiers.IRON, "iron", Tags.Items.INGOTS_IRON);
    public static MKWrapperTier WOOD_TIER = new MKWrapperTier(Tiers.WOOD, "wood", ItemTags.PLANKS);
    public static MKWrapperTier DIAMOND_TIER = new MKWrapperTier(Tiers.DIAMOND, "diamond", Tags.Items.GEMS_DIAMOND);
    public static MKWrapperTier GOLD_TIER = new MKWrapperTier(Tiers.GOLD, "gold", Tags.Items.INGOTS_GOLD);
    public static MKWrapperTier STONE_TIER = new MKWrapperTier(Tiers.STONE, "stone", Tags.Items.COBBLESTONE);
    public static MKWrapperTier NETHERITE_TIER = new MKWrapperTier(Tiers.NETHERITE, "netherite", Tags.Items.INGOTS_NETHERITE,
            new LivingDamageMeleeWeaponEffect(1.2f));

    public static List<MKBow> BOWS = new ArrayList<>();

    public static Map<IMKTier, Map<IMeleeWeaponType, Item>> WEAPON_LOOKUP = new HashMap<>();

    public static RegistryObject<Item> Haft = REGISTRY.register("haft",
            () -> new Item(new Item.Properties()));

    public static RegistryObject<Item> CopperRing = REGISTRY.register("copper_ring",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

    public static RegistryObject<Item> GoldRing = REGISTRY.register("gold_ring",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

    public static RegistryObject<Item> RoseGoldRing = REGISTRY.register("rose_gold_ring",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

    public static RegistryObject<Item> SilverRing = REGISTRY.register("silver_ring",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

    public static RegistryObject<Item> SilverEarring = REGISTRY.register("silver_earring",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

    public static RegistryObject<Item> CopperEarring = REGISTRY.register("copper_earring",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

    public static RegistryObject<Item> GoldEarring = REGISTRY.register("gold_earring",
            () -> new MKAccessory(new Item.Properties().stacksTo(1)));

    public static void putWeaponForLookup(IMKTier tier, IMeleeWeaponType weaponType, Item item) {
        WEAPON_LOOKUP.computeIfAbsent(tier, t -> new HashMap<>()).put(weaponType, item);
    }

    public static Item lookupWeapon(IMKTier tier, IMeleeWeaponType weaponType) {
        return WEAPON_LOOKUP.get(tier).get(weaponType);
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        if (event.getRegistryKey() != Registries.ITEM) {
            return;
        }
        MeleeWeaponTypes.registerWeaponTypes();

        Map<String, IMKTier> tiers = Map.of(
                "wood", WOOD_TIER,
                "gold", GOLD_TIER,
                "iron", IRON_TIER,
                "stone", STONE_TIER,
                "diamond", DIAMOND_TIER,
                "netherite", NETHERITE_TIER
        );

        WEAPON_LOOKUP.clear();
        BOWS.clear();
        WEAPONS.clear();
        for (Map.Entry<String, IMKTier> mat : tiers.entrySet()) {
            IMKTier tier = mat.getValue();
            for (IMeleeWeaponType weaponType : MeleeWeaponTypes.WEAPON_TYPES.values()) {
                MKMeleeWeapon weapon = new MKMeleeWeapon(tier, weaponType, new Item.Properties());
                WEAPONS.add(weapon);
                WeaponTypeManager.addMeleeWeapon(weapon);
                putWeaponForLookup(tier, weaponType, weapon);
                event.register(Registries.ITEM,
                        MKWeapons.id(String.format("%s_%s", weaponType.getName().getPath(), mat.getKey())),
                        () -> weapon);
            }

            RangedModifierEffect rangedMods = new RangedModifierEffect();
            rangedMods.addAttributeModifier(MKAttributes.RANGED_CRIT,
                    new AttributeModifier(RANGED_WEP_UUID, "Bow Crit", 0.05, AttributeModifier.Operation.ADDITION));
            rangedMods.addAttributeModifier(MKAttributes.RANGED_CRIT_MULTIPLIER,
                    new AttributeModifier(RANGED_WEP_UUID, "Bow Crit", 0.25, AttributeModifier.Operation.ADDITION));
            MKBow bow = new MKBow(
                    new Item.Properties().durability(tier.getUses() * 3),
                    tier,
                    GameConstants.TICKS_PER_SECOND * 2.5f, 4.0f,
                    new RapidFireRangedWeaponEffect(7, .10f),
                    rangedMods
            );
            BOWS.add(bow);
            event.register(Registries.ITEM,
                    new ResourceLocation(MKWeapons.MODID, String.format("longbow_%s", mat.getKey())), () -> bow);
        }
        TestNBTWeaponEffectItem testNBTWeaponEffectItem = new TestNBTWeaponEffectItem(new Item.Properties());
        event.register(Registries.ITEM,
                new ResourceLocation(MKWeapons.MODID, "test_nbt_effect"), () -> testNBTWeaponEffectItem);
    }

    public static void registerItemProperties() {
        for (MKBow bow : BOWS) {
            ItemProperties.register(bow, new ResourceLocation("pull"), (itemStack, world, entity, seed) -> {
                if (entity == null) {
                    return 0.0F;
                } else {
                    return !(entity.getUseItem().getItem() instanceof MKBow mkBow) ? 0.0F :
                            (float) (itemStack.getUseDuration() - entity.getUseItemRemainingTicks()) / mkBow.getDrawTime(itemStack, entity);
                }
            });
            ItemProperties.register(bow, new ResourceLocation("pulling"), (itemStack, world, entity, seed) -> {
                return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
            });
        }
        for (MKMeleeWeapon weapon : WEAPONS) {
            if (weapon.getWeaponType().canBlock()) {
                ItemProperties.register(weapon, new ResourceLocation("blocking"),
                        (itemStack, world, entity, seed) -> entity != null && entity.isUsingItem()
                                && entity.getUseItem() == itemStack ? 1.0F : 0.0F);
            }

        }
    }
}
