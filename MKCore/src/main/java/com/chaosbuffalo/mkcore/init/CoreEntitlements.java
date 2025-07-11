package com.chaosbuffalo.mkcore.init;


import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.MKCoreRegistry;
import com.chaosbuffalo.mkcore.core.entitlements.AbilityPoolEntitlement;
import com.chaosbuffalo.mkcore.core.entitlements.AbilitySlotEntitlement;
import com.chaosbuffalo.mkcore.core.entitlements.ArmorClassMasteryEntitlement;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;
import com.chaosbuffalo.mkcore.item.ArmorClass;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

import java.util.function.BiFunction;
import java.util.function.Function;

public class CoreEntitlements {
    private static ResourceKey<MKEntitlement> key(String id) {
        return ResourceKey.create(MKCoreRegistry.ENTITLEMENT_REGISTRY_KEY, MKCore.id(id));
    }

    public static final ResourceKey<MKEntitlement> BASIC_ABILITY_SLOT = key("ability_slot.basic");
    public static final ResourceKey<MKEntitlement> PASSIVE_ABILITY_SLOT = key("ability_slot.passive");
    public static final ResourceKey<MKEntitlement> ULTIMATE_ABILITY_SLOT = key("ability_slot.ultimate");

    public static final ResourceKey<MKEntitlement> ABILITY_POOL_SIZE = key("ability_pool.count");

    public static final ResourceKey<MKEntitlement> ROBE_ARMOR_MASTERY = key("armor_mastery.robes");
    public static final ResourceKey<MKEntitlement> LIGHT_ARMOR_MASTERY = key("armor_mastery.light");
    public static final ResourceKey<MKEntitlement> MEDIUM_ARMOR_MASTERY = key("armor_mastery.medium");
    public static final ResourceKey<MKEntitlement> HEAVY_ARMOR_MASTERY = key("armor_mastery.heavy");


    public static void bootstrap(BootstrapContext<MKEntitlement> context) {

        Function<AbilityGroupId, BiFunction<Component,Component, MKEntitlement>> slot =
                (a) -> (Component n, Component d) -> new AbilitySlotEntitlement(n, d, a);
        register(context, BASIC_ABILITY_SLOT, slot.apply(AbilityGroupId.Basic));
        register(context, PASSIVE_ABILITY_SLOT, slot.apply(AbilityGroupId.Passive));
        register(context, ULTIMATE_ABILITY_SLOT, slot.apply(AbilityGroupId.Ultimate));

        register(context, ABILITY_POOL_SIZE, (n, d) ->
                new AbilityPoolEntitlement(n, d, GameConstants.MAX_ABILITY_POOL_SIZE - GameConstants.DEFAULT_ABILITY_POOL_SIZE));

        Function<ResourceKey<ArmorClass>, BiFunction<Component,Component, MKEntitlement>> armor =
                (a) -> (Component n, Component d) -> new ArmorClassMasteryEntitlement(n, d, a);
        register(context, ROBE_ARMOR_MASTERY,  armor.apply(CoreArmorClasses.ROBES_ARMOR));
        register(context, LIGHT_ARMOR_MASTERY,  armor.apply(CoreArmorClasses.LIGHT_ARMOR));
        register(context, MEDIUM_ARMOR_MASTERY,  armor.apply(CoreArmorClasses.MEDIUM_ARMOR));
        register(context, HEAVY_ARMOR_MASTERY,  armor.apply(CoreArmorClasses.HEAVY_ARMOR));
    }

    private static void register(BootstrapContext<MKEntitlement> context, ResourceKey<MKEntitlement> key, BiFunction<Component,Component, MKEntitlement> builder) {
        var name = Component.translatable(MKEntitlement.nameKey(key.location()));
        var desc = Component.translatable(MKEntitlement.descriptionKey(key.location()));
        context.register(key, builder.apply(name, desc));
    }
}
