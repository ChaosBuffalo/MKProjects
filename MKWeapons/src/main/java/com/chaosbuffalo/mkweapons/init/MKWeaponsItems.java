package com.chaosbuffalo.mkweapons.init;


import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.TestNBTWeaponEffectItem;
import com.chaosbuffalo.mkweapons.items.accessories.MKCurioAccessory;
import com.chaosbuffalo.mkweapons.items.effects.melee.LivingDamageMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RapidFireRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.tier.MKWrapperTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.IRangedWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.chaosbuffalo.mkweapons.items.weapon.types.RangedWeaponTypes;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.*;

@EventBusSubscriber(modid = MKWeapons.MODID)
public class MKWeaponsItems {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MKWeapons.MODID);

    public static void register(IEventBus bus) {
        REGISTRY.register(bus);
    }

    public static MKWrapperTier IRON_TIER = new MKWrapperTier(Tiers.IRON, "iron", Tags.Items.INGOTS_IRON,
            List.of(),
            List.of(
                    new RapidFireRangedWeaponEffect(7, .10f)
            ));
    public static MKWrapperTier WOOD_TIER = new MKWrapperTier(Tiers.WOOD, "wood", ItemTags.PLANKS,
            List.of(),
            List.of(
                    new RapidFireRangedWeaponEffect(7, .10f)
            ));
    public static MKWrapperTier DIAMOND_TIER = new MKWrapperTier(Tiers.DIAMOND, "diamond", Tags.Items.GEMS_DIAMOND,
            List.of(),
            List.of(
                    new RapidFireRangedWeaponEffect(7, .10f)
            ));
    public static MKWrapperTier GOLD_TIER = new MKWrapperTier(Tiers.GOLD, "gold", Tags.Items.INGOTS_GOLD,
            List.of(),
            List.of(
                    new RapidFireRangedWeaponEffect(7, .10f)
            ));
    public static MKWrapperTier STONE_TIER = new MKWrapperTier(Tiers.STONE, "stone", Tags.Items.COBBLESTONES,
            List.of(),
            List.of(
                    new RapidFireRangedWeaponEffect(7, .10f)
            ));
    public static MKWrapperTier NETHERITE_TIER = new MKWrapperTier(Tiers.NETHERITE, "netherite", Tags.Items.INGOTS_NETHERITE,
            List.of(
                    new LivingDamageMeleeWeaponEffect(1.2f)
            ),
            List.of(
                    new RapidFireRangedWeaponEffect(7, .10f)
            ));

    public static final List<MKMeleeWeapon> WEAPONS = new ArrayList<>();
    public static final List<MKBow> BOWS = new ArrayList<>();

    public static final Map<IMKTier, Map<IMeleeWeaponType, Item>> WEAPON_LOOKUP = new HashMap<>();
    public static final Map<IMKTier, Map<IRangedWeaponType, Item>> RANGED_LOOKUP = new HashMap<>();

    public static DeferredItem<Item> Haft = REGISTRY.register("haft",
            () -> new Item(new Item.Properties()));

    public static DeferredItem<Item> CopperRing = REGISTRY.register("copper_ring",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    public static DeferredItem<Item> GoldRing = REGISTRY.register("gold_ring",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    public static DeferredItem<Item> RoseGoldRing = REGISTRY.register("rose_gold_ring",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    public static DeferredItem<Item> SilverRing = REGISTRY.register("silver_ring",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    public static DeferredItem<Item> SilverEarring = REGISTRY.register("silver_earring",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    public static DeferredItem<Item> CopperEarring = REGISTRY.register("copper_earring",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    public static DeferredItem<Item> GoldEarring = REGISTRY.register("gold_earring",
            () -> new MKCurioAccessory(new Item.Properties().stacksTo(1)));

    public static void putWeaponForLookup(IMKTier tier, IMeleeWeaponType weaponType, Item item) {
        WEAPON_LOOKUP.computeIfAbsent(tier, t -> new HashMap<>()).put(weaponType, item);
    }

    public static void putWeaponForLookup(IMKTier tier, IRangedWeaponType weaponType, Item item) {
        RANGED_LOOKUP.computeIfAbsent(tier, t -> new HashMap<>()).put(weaponType, item);
    }

    public static Item lookupWeapon(IMKTier tier, IMeleeWeaponType weaponType) {
        return Objects.requireNonNull(WEAPON_LOOKUP.get(tier).get(weaponType));
    }

    public static Item lookupWeapon(IMKTier tier, IRangedWeaponType weaponType) {
        return Objects.requireNonNull(RANGED_LOOKUP.get(tier).get(weaponType));
    }

    public static List<MKMeleeWeapon> getMeleeWeaponsFromMod(String modId) {
        return WEAPONS.stream().filter(item -> {
            var itemId = BuiltInRegistries.ITEM.getKey(item);
            return itemId.getNamespace().equals(modId);
        }).toList();
    }

    public static List<MKBow> getRangedWeaponsFromMod(String modId) {
        return BOWS.stream().filter(item -> {
            var itemId = BuiltInRegistries.ITEM.getKey(item);
            return itemId.getNamespace().equals(modId);
        }).toList();
    }

    static final WeaponTierItemFactory DEFAULT_TIER_FACTORY = new WeaponTierItemFactory() {
        @Override
        public ResourceLocation getMeleeRegistryName(IMKTier tier, IMeleeWeaponType weaponType) {
            return MKWeapons.id(weaponType.getName().getPath() + "_" + tier.getName());
        }

        @Override
        public ResourceLocation getRangedRegistryName(IMKTier tier, IRangedWeaponType rangedWeaponType) {
            return MKWeapons.id(rangedWeaponType.getTypeName() + "_" + tier.getName());
        }
    };

    static final Map<IMKTier, WeaponTierItemFactory> tierFactoryMap = Util.make(new HashMap<>(), map -> {
        Map<IMKTier, WeaponTierItemFactory> tiers = Map.of(
                WOOD_TIER, DEFAULT_TIER_FACTORY,
                GOLD_TIER, DEFAULT_TIER_FACTORY,
                IRON_TIER, DEFAULT_TIER_FACTORY,
                STONE_TIER, DEFAULT_TIER_FACTORY,
                DIAMOND_TIER, DEFAULT_TIER_FACTORY,
                NETHERITE_TIER, DEFAULT_TIER_FACTORY
        );
        map.putAll(tiers);
    });

    public static void registerTierFactory(IMKTier tier, WeaponTierItemFactory factory) {
        tierFactoryMap.put(tier, factory);
    }

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        if (event.getRegistryKey() != Registries.ITEM) {
            return;
        }

        WEAPON_LOOKUP.clear();
        BOWS.clear();
        WEAPONS.clear();
        for (var entry : tierFactoryMap.entrySet()) {
            IMKTier tier = entry.getKey();
            WeaponTierItemFactory factory = entry.getValue();
            for (IMeleeWeaponType weaponType : MeleeWeaponTypes.WEAPON_TYPES.values()) {
                MKMeleeWeapon weapon = factory.createMeleeWeapon(tier, weaponType);
                if (weapon != null) {
                    WEAPONS.add(weapon);
                    putWeaponForLookup(tier, weaponType, weapon);
                    ResourceLocation registryId = factory.getMeleeRegistryName(tier, weaponType);
                    event.register(Registries.ITEM, registryId, () -> weapon);
                }
            }

            MKBow bow = factory.createRangedWeapon(tier, RangedWeaponTypes.LONGBOW);
            if (bow != null) {
                BOWS.add(bow);
                putWeaponForLookup(tier, RangedWeaponTypes.LONGBOW, bow);
                ResourceLocation bowId = factory.getRangedRegistryName(tier, RangedWeaponTypes.LONGBOW);
                event.register(Registries.ITEM, bowId, () -> bow);
            }
        }
        TestNBTWeaponEffectItem testNBTWeaponEffectItem = new TestNBTWeaponEffectItem(new Item.Properties());
        event.register(Registries.ITEM,
                MKWeapons.id("test_nbt_effect"), () -> testNBTWeaponEffectItem);
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            for (MKBow bow : BOWS) {
                event.accept(bow);
            }
            for (MKMeleeWeapon weapon : WEAPONS) {
                event.accept(weapon);
            }
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(Haft.get());
            event.accept(GoldEarring.get());
            event.accept(RoseGoldRing.get());
            event.accept(SilverRing.get());
            event.accept(SilverEarring.get());
            event.accept(CopperRing.get());
            event.accept(CopperEarring.get());
            event.accept(GoldRing.get());
        }
    }

}
