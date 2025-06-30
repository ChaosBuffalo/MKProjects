package com.chaosbuffalo.mkweapons.init;


import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.MKBow;
import com.chaosbuffalo.mkweapons.items.MKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.TestNBTWeaponEffectItem;
import com.chaosbuffalo.mkweapons.items.accessories.MKCurioAccessory;
import com.chaosbuffalo.mkweapons.items.effects.melee.LivingDamageMeleeWeaponEffect;
import com.chaosbuffalo.mkweapons.items.effects.ranged.RapidFireRangedWeaponEffect;
import com.chaosbuffalo.mkweapons.items.weapon.IMKMeleeWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.IMKRangedWeapon;
import com.chaosbuffalo.mkweapons.items.weapon.tier.IMKTier;
import com.chaosbuffalo.mkweapons.items.weapon.tier.MKWrapperTier;
import com.chaosbuffalo.mkweapons.items.weapon.types.IMeleeWeaponType;
import com.chaosbuffalo.mkweapons.items.weapon.types.MeleeWeaponTypes;
import com.chaosbuffalo.mkweapons.items.weapon.types.WeaponTypeManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
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

    public static List<MKMeleeWeapon> WEAPONS = new ArrayList<>();
    public static final UUID RANGED_WEP_UUID = UUID.fromString("dbaf479e-515e-4ebc-94dd-eb5a4014bb64");

    public static MKWrapperTier IRON_TIER = new MKWrapperTier(Tiers.IRON, "iron", Tags.Items.INGOTS_IRON);
    public static MKWrapperTier WOOD_TIER = new MKWrapperTier(Tiers.WOOD, "wood", ItemTags.PLANKS);
    public static MKWrapperTier DIAMOND_TIER = new MKWrapperTier(Tiers.DIAMOND, "diamond", Tags.Items.GEMS_DIAMOND);
    public static MKWrapperTier GOLD_TIER = new MKWrapperTier(Tiers.GOLD, "gold", Tags.Items.INGOTS_GOLD);
    public static MKWrapperTier STONE_TIER = new MKWrapperTier(Tiers.STONE, "stone", Tags.Items.COBBLESTONES);
    public static MKWrapperTier NETHERITE_TIER = new MKWrapperTier(Tiers.NETHERITE, "netherite", Tags.Items.INGOTS_NETHERITE,
            new LivingDamageMeleeWeaponEffect(1.2f));

    public static List<MKBow> BOWS = new ArrayList<>();

    public static Map<IMKTier, Map<IMeleeWeaponType, Item>> WEAPON_LOOKUP = new HashMap<>();

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

    public static Item lookupWeapon(IMKTier tier, IMeleeWeaponType weaponType) {
        return WEAPON_LOOKUP.get(tier).get(weaponType);
    }

    public static Optional<Holder.Reference<Item>> lookupMelee(IMKTier tier, IMeleeWeaponType weaponType, String sourceMod) {
        return BuiltInRegistries.ITEM.getHolder(ResourceLocation.fromNamespaceAndPath(sourceMod,
                String.format("%s_%s", weaponType.getName().getPath(), tier.getName())));
    }

    public static Optional<Holder.Reference<Item>> lookupMelee(IMKTier tier, IMeleeWeaponType weaponType) {
        return lookupMelee(tier, weaponType, MKWeapons.MODID);
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
                MKMeleeWeapon weapon = new MKMeleeWeapon(tier, weaponType, new Item.Properties()
                        .attributes(MKMeleeWeapon.createAttributes(tier, weaponType)));
                WEAPONS.add(weapon);
                WeaponTypeManager.addMeleeWeapon(weapon);
                putWeaponForLookup(tier, weaponType, weapon);
                event.register(Registries.ITEM,
                        MKWeapons.id(String.format("%s_%s", weaponType.getName().getPath(), mat.getKey())),
                        () -> weapon);
            }

            ResourceLocation modifierId = MKWeapons.id("base." + tier.getName());

            ItemAttributeModifiers defaultAttributes = ItemAttributeModifiers.builder()
                    .add(
                            MKAttributes.RANGED_CRIT,
                            new AttributeModifier(
                                    modifierId, 0.05, AttributeModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.MAINHAND
                    )
                    .add(
                            MKAttributes.RANGED_CRIT_MULTIPLIER,
                            new AttributeModifier(
                                    modifierId, 0.25, AttributeModifier.Operation.ADD_VALUE
                            ),
                            EquipmentSlotGroup.MAINHAND
                    )
                    .build();

            MKBow bow = new MKBow(
                    new Item.Properties()
                            .durability(tier.getUses() * 3)
                            .attributes(defaultAttributes),
                    tier,
                    GameConstants.TICKS_PER_SECOND * 2.5f, 4.0f,
                    new RapidFireRangedWeaponEffect(7, .10f)
            );
            BOWS.add(bow);
            event.register(Registries.ITEM,
                    MKWeapons.id(String.format("longbow_%s", mat.getKey())), () -> bow);
        }
        TestNBTWeaponEffectItem testNBTWeaponEffectItem = new TestNBTWeaponEffectItem(new Item.Properties());
        event.register(Registries.ITEM,
                MKWeapons.id("test_nbt_effect"), () -> testNBTWeaponEffectItem);
    }

    public static void registerItemProperties() {
        registerDefaultRangedWeaponItemProperties(BOWS);
        registerDefaultMeleeWeaponItemProperties(WEAPONS);
    }

    public static <TItem extends Item & IMKMeleeWeapon> void registerDefaultMeleeWeaponItemProperties(Collection<? extends TItem> weapons) {
        for (TItem weapon : weapons) {
            if (weapon.getWeaponType().canBlock()) {
                ItemProperties.register(weapon, ResourceLocation.withDefaultNamespace("blocking"), MKWeaponsItems::defaultMeleeBlockingProperty);
            }
        }
    }

    public static <TItem extends Item & IMKRangedWeapon> void registerDefaultRangedWeaponItemProperties(Collection<? extends TItem> weapons) {
        for (TItem bow : weapons) {
            ItemProperties.register(bow, ResourceLocation.withDefaultNamespace("pull"), MKWeaponsItems::defaultRangedPullProperty);
            ItemProperties.register(bow, ResourceLocation.withDefaultNamespace("pulling"), MKWeaponsItems::defaultRangedPullingProperty);
        }
    }

    private static float defaultRangedPullProperty(ItemStack itemStack, ClientLevel world, LivingEntity entity, int seed) {
        if (entity == null) {
            return 0.0F;
        } else {
            return !(entity.getUseItem().getItem() instanceof MKBow mkBow) ? 0.0F :
                    (float) (itemStack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / mkBow.getDrawTime(itemStack, entity);
        }
    }

    private static float defaultRangedPullingProperty(ItemStack itemStack, ClientLevel world, LivingEntity entity, int seed) {
        return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
    }

    private static float defaultMeleeBlockingProperty(ItemStack itemStack, ClientLevel world, LivingEntity entity, int seed) {
        return entity != null && entity.isUsingItem()
                && entity.getUseItem() == itemStack ? 1.0F : 0.0F;
    }
}
