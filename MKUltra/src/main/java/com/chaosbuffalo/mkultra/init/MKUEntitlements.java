package com.chaosbuffalo.mkultra.init;


import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.entitlements.SimpleEntitlement;
import com.chaosbuffalo.mkultra.MKUltra;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;


public class MKUEntitlements {

    static ResourceKey<MKEntitlement> key(String id) {
        return ResourceKey.create(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY, MKUltra.id(id));
    }

    public static ResourceKey<MKEntitlement> GreenKnightTier1 = key("green_knight.tier_1");

    public static ResourceKey<MKEntitlement> GreenKnightTier2 = key("green_knight.tier_2");

    public static ResourceKey<MKEntitlement> GreenKnightTier3 = key("green_knight.tier_3");

    public static ResourceKey<MKEntitlement> ClericTier1 = key("cleric.tier_1");

    public static ResourceKey<MKEntitlement> ClericTier2 = key("cleric.tier_2");

    public static ResourceKey<MKEntitlement> ClericTier3 = key("cleric.tier_3");

    public static ResourceKey<MKEntitlement> IntroClericTier1 = key("cleric.intro.tier_1");

    public static ResourceKey<MKEntitlement> NetherMageTier1 = key("nether_mage.tier_1");

    public static ResourceKey<MKEntitlement> NetherMageTier2 = key("nether_mage.tier_2");

    public static ResourceKey<MKEntitlement> NetherMageTier3 = key("nether_mage.tier_3");

    public static ResourceKey<MKEntitlement> IntroNetherMageTier1 = key("nether_mage.intro.tier_1");

    public static ResourceKey<MKEntitlement> ThemcromancerTier1 = key("themcromancer.tier_1");

    public static ResourceKey<MKEntitlement> ThemcromancerTier2 = key("themcromancer.tier_2");

    public static ResourceKey<MKEntitlement> ThemcromancerTier3 = key("themcromancer.tier_3");

    private static void registerPlayerFlag(BootstrapContext<MKEntitlement> context, ResourceKey<MKEntitlement> key) {
        var name = Component.translatable(MKEntitlement.nameKey(key.location()));
        var desc = Component.translatable(MKEntitlement.descriptionKey(key.location()));
        var value = new SimpleEntitlement(name, desc);
        context.register(key, value);
    }


    public static void bootstrap(BootstrapContext<MKEntitlement> context) {
        registerPlayerFlag(context, GreenKnightTier1);
        registerPlayerFlag(context, GreenKnightTier2);
        registerPlayerFlag(context, GreenKnightTier3);

        registerPlayerFlag(context, ClericTier1);
        registerPlayerFlag(context, ClericTier2);
        registerPlayerFlag(context, ClericTier3);
        registerPlayerFlag(context, IntroClericTier1);

        registerPlayerFlag(context, NetherMageTier1);
        registerPlayerFlag(context, NetherMageTier2);
        registerPlayerFlag(context, NetherMageTier3);
        registerPlayerFlag(context, IntroNetherMageTier1);

        registerPlayerFlag(context, ThemcromancerTier1);
        registerPlayerFlag(context, ThemcromancerTier2);
        registerPlayerFlag(context, ThemcromancerTier3);
    }
}
