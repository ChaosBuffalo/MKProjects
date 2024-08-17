package com.chaosbuffalo.mkcore;

import com.chaosbuffalo.mkcore.abilities.MKAbility;
import com.chaosbuffalo.mkcore.abilities.MKAbilityMemories;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientStateTypes;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileCastBehaviorType;
import com.chaosbuffalo.mkcore.abilities.projectiles.ProjectileCastBehaviorTypes;
import com.chaosbuffalo.mkcore.abilities.client_state.AbilityClientStateType;
import com.chaosbuffalo.mkcore.core.MKAttributes;
import com.chaosbuffalo.mkcore.core.damage.MKDamageType;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.MKTalent;
import com.chaosbuffalo.mkcore.effects.MKEffect;
import com.chaosbuffalo.mkcore.init.*;
import com.chaosbuffalo.mkcore.test.MKCoreTestItems;
import com.chaosbuffalo.mkcore.test.MKCoreTestTalents;
import com.chaosbuffalo.mkcore.test.MKTestAbilities;
import com.chaosbuffalo.mkcore.test.MKTestEffects;
import com.chaosbuffalo.mkcore.utils.location.LocationProviderType;
import com.chaosbuffalo.mkcore.utils.location.LocationProviderTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import javax.annotation.Nullable;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MKCoreRegistry {
    public static final ResourceLocation INVALID_ABILITY = ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "ability.invalid");
    public static final ResourceLocation INVALID_TALENT = ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "talent.invalid");
    public static final ResourceLocation INVALID_ENTITLEMENT = ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "entitlement.invalid");

    public static final ResourceKey<Registry<MKAbility>> ABILITY_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.makeRL("abilities"));
    public static final ResourceKey<Registry<MKDamageType>> DAMAGE_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.makeRL("damage_types"));
    public static final ResourceKey<Registry<MKEffect>> EFFECT_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.makeRL("effects"));
    public static final ResourceKey<Registry<MKEntitlement>> ENTITLEMENT_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.makeRL("entitlements"));
    public static final ResourceKey<Registry<MKTalent>> TALENT_REGISTRY_KEY = ResourceKey.createRegistryKey(MKCore.makeRL("talents"));

    public static final ResourceKey<Registry<LocationProviderType<?>>> LOC_PROVIDER_TYPES_NAME = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "loc_provider_types"));
    public static final ResourceKey<Registry<ProjectileCastBehaviorType<?>>> CAST_BEHAVIOR_TYPES_NAME = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "projectile_cast_behavior_types"));
    public static final ResourceKey<Registry<AbilityClientStateType<?>>> CLIENT_STATE_TYPES_NAME = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "ability_client_state_types"));

    public static final Registry<MKAbility> ABILITIES;
    public static final Registry<MKDamageType> DAMAGE_TYPES;
    public static final Registry<MKEffect> EFFECTS;
    public static final Registry<MKTalent> TALENTS;
    public static final Registry<MKEntitlement> ENTITLEMENTS;
    public static final Registry<LocationProviderType<?>> LOCATION_PROVIDER_TYPES;
    public static final Registry<ProjectileCastBehaviorType<?>> CAST_BEHAVIOR_TYPES;
    public static final Registry<AbilityClientStateType<?>> CLIENT_STATE_TYPES;

    static {
        ABILITIES = new RegistryBuilder<>(ABILITY_REGISTRY_KEY).create();
        DAMAGE_TYPES = new RegistryBuilder<>(DAMAGE_TYPE_REGISTRY_KEY).create();
        EFFECTS = new RegistryBuilder<>(EFFECT_REGISTRY_KEY).create();
        TALENTS = new RegistryBuilder<>(TALENT_REGISTRY_KEY).create();
        ENTITLEMENTS = new RegistryBuilder<>(ENTITLEMENT_REGISTRY_KEY).create();
        LOCATION_PROVIDER_TYPES = new RegistryBuilder<>(LOC_PROVIDER_TYPES_NAME).create();
        CLIENT_STATE_TYPES = new RegistryBuilder<>(CLIENT_STATE_TYPES_NAME).create();
        CAST_BEHAVIOR_TYPES = new RegistryBuilder<>(CAST_BEHAVIOR_TYPES_NAME).create();
    }


    @Nullable
    public static MKAbility getAbility(ResourceLocation abilityId) {
        return ABILITIES.get(abilityId);
    }

    @Nullable
    public static MKDamageType getDamageType(ResourceLocation damageTypeId) {
        return DAMAGE_TYPES.get(damageTypeId);
    }

    @Nullable
    public static MKEntitlement getEntitlement(ResourceLocation entitlementId) {
        return ENTITLEMENTS.get(entitlementId);
    }

    @SubscribeEvent
    public static void createRegistries(NewRegistryEvent event) {
        event.register(ABILITIES);
        event.register(DAMAGE_TYPES);
        event.register(EFFECTS);
        event.register(TALENTS);
        event.register(ENTITLEMENTS);
        event.register(LOCATION_PROVIDER_TYPES);
        event.register(CLIENT_STATE_TYPES);
        event.register(CAST_BEHAVIOR_TYPES);
    }

    public static void register(IEventBus modBus) {
        CoreCommands.register(modBus);
        CoreDamageTypes.register(modBus);
        CoreEffects.register(modBus);
        CoreEntities.register(modBus);
        CoreEntitlements.register(modBus);
        CoreParticles.register(modBus);
        CoreSounds.register(modBus);
        CoreTalents.register(modBus);
        MKAbilityMemories.register(modBus);
        MKTestEffects.register(modBus);
        MKTestAbilities.register(modBus);
        MKCoreTestItems.register(modBus);
        MKCoreTestTalents.register(modBus);
        LocationProviderTypes.register(modBus);
        ProjectileCastBehaviorTypes.register(modBus);
        AbilityClientStateTypes.register(modBus);
        MKAttributes.register(modBus);
        CoreAttachments.register(modBus);

    }
}
